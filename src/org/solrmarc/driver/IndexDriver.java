package org.solrmarc.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.marc4j.MarcReader;
import org.solrmarc.driver.RecordAndDoc.eErrorLocationVal;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.marc.MarcReaderFactory;
import org.solrmarc.solr.DevNullProxy;
import org.solrmarc.solr.SolrCoreLoader;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrRuntimeException;
import org.solrmarc.solr.StdOutProxy;
import org.solrmarc.tools.PropertyUtils;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;


public class IndexDriver extends Boot
{
    public final static Logger logger = Logger.getLogger(IndexDriver.class);

    Properties readerProps;
    ValueIndexerFactory indexerFactory = null;

    List<AbstractValueIndexer<?>> indexers;
    Indexer indexer;
    MarcReader reader;
    SolrProxy solrProxy;
    boolean verbose;
    int numIndexed[];
    String homeDirStrs[];
    String [] args;
    OptionSpec<String> readOpts;
    OptionSpec<String> configSpecs;
    OptionSpec<String> homeDirs;
    OptionSpec<File> solrjDir;
    OptionSpec<String> solrjClass;
    OptionSpec<File> errorMarcErrOutFile;
    OptionSpec<File> errorIndexErrOutFile;
    OptionSpec<File> errorSolrErrOutFile;
    OptionSpec<String> files;

    OptionSet options = null;
    
    public static void main(String[] args)
    {
        IndexDriver driver = new IndexDriver(args);
        driver.execute();
    }

    public IndexDriver(String [] args)
    {
        this.args = args;
    }
    
    public void execute()
    {
        processArgs(args);
        initializeFromOptions();

        List<String> inputFiles = options.valuesOf(files);
        logger.info("Opening input files: "+ Arrays.toString(inputFiles.toArray()));
        this.configureReader(inputFiles);
        
        this.processInput();
    }
    
    
    public void processArgs(String args[])
    {
        OptionParser parser = new OptionParser(  );
        readOpts = parser.acceptsAll(Arrays.asList( "r", "reader_opts"), "file containing MARC Reader options").withRequiredArg().defaultsTo("resources/marcreader.properties");
        configSpecs = parser.acceptsAll(Arrays.asList( "c", "config"), "index specification file to use").withRequiredArg();
        homeDirs = parser.accepts("dir", "directory to look in for scripts, mixins, and translation maps").withRequiredArg().ofType( String.class );
        solrjDir = parser.accepts("solrj", "directory to look in for jars required for SolrJ").withRequiredArg().ofType( File.class );
        solrjClass = parser.accepts("solrjClassName", "Classname of class to use for talking to solr").withRequiredArg().ofType( String.class ).defaultsTo("");
        errorMarcErrOutFile = parser.accepts("marcerr", "File to write records with errors.(not yet implemented)").withRequiredArg().ofType( File.class );
        errorIndexErrOutFile = parser.accepts("indexerr", "File to write the solr documents for records with errors.(not yet implemented)").withRequiredArg().ofType( File.class );
        errorSolrErrOutFile = parser.accepts("solrerr", "File to write the solr documents for records with errors.(not yet implemented)").withRequiredArg().ofType( File.class );
        parser.accepts("debug", "non-multithreaded debug mode");
        parser.acceptsAll(Arrays.asList( "solrURL", "u"), "URL of Remote Solr to use").withRequiredArg();
        parser.acceptsAll(Arrays.asList("print", "stdout"), "write output to stdout in user readable format");//.availableUnless("sorlURL");
        parser.acceptsAll(Arrays.asList("null"), "discard all output, and merely show errors and warnings");//.availableUnless("sorlURL");
        parser.acceptsAll(Arrays.asList("?", "help"), "show this usage information").forHelp();
        //parser.mutuallyExclusive("stdout", "solrURL");
        files = parser.nonOptions().ofType( String.class );

        options = null;
        try {
            options = parser.parse(args );
        }
        catch (OptionException uoe)
        {
            try
            {
                System.err.println(uoe.getMessage());
                parser.printHelpOn(System.err);
            }
            catch (IOException e)
            {
            }
            System.exit(1);
        }
        if (args.length == 0 || options.has("help")) 
        {
            try
            {
                parser.printHelpOn(System.err);
            }
            catch (IOException e)
            {
            }
            System.exit(0);
        }
        if (options.has("dir"))
        {
            homeDirStrs = options.valueOf(homeDirs).split("[|]");
        }
        else 
        {
            homeDirStrs = new String[]{ Boot.getDefaultHomeDir() };
        }
        File solrJPath = ((options.has(solrjDir)) ? options.valueOf(solrjDir) : new File("lib-solrj"));
        
        try { 
            if (solrJPath.isAbsolute()) 
            {
                Boot.extendClasspathWithSolJJarDir(null, solrJPath);
            }
            else
            {
                Boot.extendClasspathWithSolJJarDir(homeDirStrs, solrJPath);
            }
        }
        catch (IndexerSpecException ise)
        {
            logger.fatal("Fatal error: Failure to load SolrJ", ise);
            logger.error("Exiting...");
            System.exit(10);
        }
    }

    public void initializeFromOptions()
    {
        File f1 = new File(options.valueOf(readOpts));
        try
        {
            configureReaderProps(f1);
        }
        catch (IOException e1)
        {
            logger.fatal("Fatal error: Exception opening reader properties input stream: " + f1.getName());
            logger.error("Exiting...");
            System.exit(1);
        }
        
     //   String solrURL = "http://libsvr40.lib.virginia.edu:8080/solrgis/nextgen";
        String solrJClassName = solrjClass.value(options);
        String solrURL = options.has("solrURL") ? options.valueOf("solrURL").toString() : options.has("null") ? "devnull" : "stdout";
        boolean multithread = options.has("solrURL") && !options.has("debug") ? true : false;
        try {
            this.configureOutput(solrURL, solrJClassName);
        }
        catch (SolrRuntimeException sre)
        {
            logger.error("Error connecting to solr at URL "+solrURL, sre);
            logger.error("Normally the should exit.  For testing purposes continuing and writing out solr records insetad.");
//            logger.error("Exiting...");
//            System.exit(6);
            this.configureOutput("stdout", solrJClassName);
        }
        String specs = options.valueOf(configSpecs);
        try
        {
            logger.info("Reading and compiling index specifications: "+ specs);
            this.configureIndexer(specs, multithread);
        }
        catch (IOException | IllegalAccessException | InstantiationException e1)
        {
            logger.error("Error opening or reading index configurations: " + specs, e1);
            logger.error("Exiting...");
            System.exit(2);
        }
        List<IndexerSpecException> exceptions = this.indexerFactory.getValidationExceptions();
        if (!exceptions.isEmpty())
        {
            logger.error("Error processing index configurations: " + specs);
            logTextForExceptions(exceptions);
            logger.error("Exiting...");
            System.exit(5);
        }

    }

    public void configureReaderProps(File readerProperties) throws FileNotFoundException, IOException 
    {
        readerProps = new Properties();
        readerProps.load(new FileInputStream(readerProperties));
    }
    
    public void configureReader(List<String> inputFilenames) 
    {
        reader = MarcReaderFactory.instance().makeReader(readerProps, ValueIndexerFactory.getHomeDirs(), inputFilenames);
    }
    
    public void configureIndexer(String indexSpecifications, boolean multiThreaded) 
                throws IllegalAccessException, InstantiationException, IOException
    {
        // You must set the HomeDir before instantiating the ValueIndexerFactory
        // since that directory is used as the location to look for java source files to compile and include
        // If it is unspecified, the program looks in 
        ValueIndexerFactory.setHomeDirs(homeDirStrs);
        indexerFactory = ValueIndexerFactory.instance();
        String[] indexSpecs = indexSpecifications.split("[ ]*,[ ]*");
        File[] specFiles = new File[indexSpecs.length];
        int i = 0;
        for (String indexSpec : indexSpecs)
        {
            File specFile = new File(indexSpec);
            if (!specFile.isAbsolute()) specFile = PropertyUtils.findFirstExistingFile(homeDirStrs, indexSpec);
            specFiles[i++] = specFile;
        }
        
        indexers = indexerFactory.createValueIndexers(specFiles);
        boolean includeErrors = (readerProps.getProperty("marc.include_errors", "false").equals("true"));
        indexer = null;
        // System.err.println("Reading and compiling index specification: "+ indexSpecification);
        if (multiThreaded)
            indexer = new ThreadedIndexer(indexers, solrProxy, 640);
        else 
            indexer = new Indexer(indexers, solrProxy);
        
        indexer.setErr(Indexer.eErrorHandleVal.RETURN_ERROR_RECORDS);
        if (includeErrors)
        {
            indexer.setErr(Indexer.eErrorHandleVal.INDEX_ERROR_RECORDS);
        }
    }
    
    public void configureOutput(String solrURL, String solrJClassName)
    {
        if (solrURL.equals("stdout"))
        {
            try
            {
                PrintStream out = new PrintStream(System.out, true, "UTF-8");
                System.setOut(out);
                solrProxy = new StdOutProxy(out);
            }
            catch (UnsupportedEncodingException e)
            {
                // since the encoding is hard-coded, and is valid, this Exception cannot occur.
            }
        }
        else if (solrURL.equals("devnull"))
        {
            solrProxy = new DevNullProxy();
        }
        else 
        {
            solrProxy = SolrCoreLoader.loadRemoteSolrServer(solrURL, solrJClassName, true);
        }
    }
    
    public void processInput()
    {
        String inEclipseStr = System.getProperty("runInEclipse");
        boolean inEclipse = "true".equalsIgnoreCase(inEclipseStr);
        Thread shutdownSimulator = null;
        if (inEclipse) 
        {
            shutdownSimulator = new ShutdownSimulator();
            shutdownSimulator.start();
        }
        Thread shutdownHook = new MyShutdownThread(indexer);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        
        numIndexed = indexer.indexToSolr(reader);
        
        endTime = System.currentTimeMillis();
        
        if (!indexer.isShutDown()) 
        {
            if (shutdownSimulator != null)
                shutdownSimulator.interrupt();
            if (!indexer.shuttingDown)  
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            indexer.endProcessing();
        }
        reportResultsAndTime(startTime, endTime);
        if (indexer.errQ.size() > 0)
        {
            handleRecordErrors();
        }
    }

    private void reportResultsAndTime(long startTime, long endTime)
    {
        logger.info(""+numIndexed[0]+ " records read");
        logger.info(""+numIndexed[1]+ " records indexed  and ");
        long minutes = ((endTime - startTime) / 1000) / 60;
        long seconds = (endTime - startTime) / 1000  - (minutes * 60);
        long hundredths = (endTime - startTime) / 10  - (minutes * 6000) - (seconds * 100) + 100;
        String hundredthsStr = (""+hundredths).substring(1);
        String minutesStr = ((minutes > 0) ? ""+minutes+" minute"+((minutes != 1)?"s ":" ") : "");
        String secondsStr = ""+seconds+"."+hundredthsStr+" seconds";
        logger.info(""+numIndexed[2]+ " records sent to Solr in "+ minutesStr + secondsStr);
    }

    private void handleRecordErrors()
    {
        Collection<RecordAndDoc> errQ = indexer.errQ;
        int[][] errTypeCnt = new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
        for (final RecordAndDoc entry : errQ)
        {
            if (entry.errLocs.contains(eErrorLocationVal.MARC_ERROR))      errTypeCnt[0][entry.getErrLvl().ordinal()] ++;
            if (entry.errLocs.contains(eErrorLocationVal.INDEXING_ERROR))  errTypeCnt[1][entry.getErrLvl().ordinal()] ++;
            if (entry.errLocs.contains(eErrorLocationVal.SOLR_ERROR))      errTypeCnt[2][entry.getErrLvl().ordinal()] ++;
        }
        showErrReport("MARC", errTypeCnt[0]);
        showErrReport("Index", errTypeCnt[1]);
        showErrReport("Solr", errTypeCnt[2]);
    }

    private void showErrReport(String errLocStr, int[] errorLvlCnt)
    {
        for (int i = 0; i < errorLvlCnt.length; i++)
        {
            if (errorLvlCnt[i] > 0) 
            {
                logger.info( "" + errorLvlCnt[i] + " records have "+errLocStr+" errors of level: "+eErrorSeverity.values()[i].toString());
            }
        }
    }

    @SuppressWarnings("unused")
    private String getTextForExceptions(List<IndexerSpecException> exceptions)
    {
        StringBuilder text = new StringBuilder();
        String lastSpec = "";
        for (IndexerSpecException e : exceptions)
        {
            String specMessage = e.getSpecMessage();
            if (!specMessage.equals(lastSpec))
            {
                text.append(specMessage).append("\n");
            }
            text.append(e.getMessage()).append("\n");
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
            {
                text.append(e.getSolrField()).append(" : ").append(cause.getMessage()).append("\n");
            }
        }
        return (text.toString());
    }
    
    private void logTextForExceptions(List<IndexerSpecException> exceptions)
    {
        String lastSpec = "";
        for (IndexerSpecException e : exceptions)
        {
            eErrorSeverity level = e.getErrLvl();
            Priority priority = getPriorityForSeverity(level);
            String specMessage = e.getSpecMessage();
            if (!specMessage.equals(lastSpec))
            {
                logger.log(priority, specMessage);
            }
            logger.log(priority, e.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
            {
                logger.log(priority, e.getSolrField()+" : "+cause.getMessage());
            }
        }
    }

    private Priority getPriorityForSeverity(eErrorSeverity level)
    {
        switch (level) {
            case NONE:  return(Level.DEBUG);
            case INFO:  return(Level.INFO);
            case WARN:  return(Level.WARN);
            case ERROR: return(Level.ERROR);
            case FATAL: return(Level.FATAL);
        }
        return(Level.DEBUG);
    }

    
    /**
     *  <h1>MyShutdownThread</h1>
     *  This class implements a shutdown hook that is installed in the Java Runtime.  If a user attempts to terminate
     *  the import process, this hook will signal the threads that are handling the import (via Thread.interrupt) and they 
     *  will shutdown cleanly, and commit the changes to Solr before allowing the program to terminate.
     * 
     * @author rh9ec
     *
     */
    class MyShutdownThread extends Thread 
    {
        private Indexer indexer;
        public MyShutdownThread(Indexer ind)
        {
            indexer = ind;
        }
        
        public void run()
        {
            //System.err.println("Starting Shutdown hook");
            logger.info("Starting Shutdown hook");
            
            if (!indexer.isShutDown()) 
            {
                logger.info("Stopping main indexer loop");
                indexer.shutDown();
            }
            while (!indexer.isShutDown())
            {
                try
                {
                    sleep(2000);
                }
                catch (InterruptedException e)
                {
                }
            }
            logger.info("Finished Shutdown hook");
        }
    }
    
    /**
     *  <h1>ShutdownSimulator</h1>
     *  A small class that is only useful for debugging purposes.  Specifically for debugging the shutdown hook.
     *  The Eclipse Java Development Environment is unable to shutdown a process it is running in a way that the
     *  shutdown hook is invoked, instead Eclipse merely summarily destroys the process, which is unhelpful.
     *  <br/>
     *  To enable this feature, you must define the system property "runInEclipse" as true, usually via the 
     *  VM arguments panel on the Arguments tab in the debug configuration dialog.    -DrunInEclipse=true
     *  Then when the program is running in Eclipse, you will need to click in the Console window, and press [ENTER]
     *  to simulate a CTRL-C being sent to the program.
     * 
     * @author rh9ec
     *
     */
    class ShutdownSimulator extends Thread
    {
        public void run()
        {
            System.out.println("You're using Eclipse; click in this console and " +
                            "press ENTER to call System.exit() and run the shutdown routine.");
            while (true) 
            {
                try {
                    if (System.in.available() > 0)
                    {
                        System.in.read();
                        System.exit(0);
                    }
                    else
                    {
                        sleep(2000);
                    }
                } 
                catch (IOException e) 
                {
                    break;
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
            
        }
    }

}
