package org.solrmarc.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;


public class IndexDriver
{
    private final static Logger logger = Logger.getLogger(IndexDriver.class);
    Properties readerProps;
    ValueIndexerFactory indexerFactory = null;

    List<AbstractValueIndexer<?>> indexers;
    Indexer indexer;
    MarcReader reader;
    SolrProxy solrProxy;
    boolean verbose;
    int numIndexed[];

    public IndexDriver(String homeDirStr)
    {
        ValueIndexerFactory.setHomeDir(homeDirStr);
        indexerFactory = ValueIndexerFactory.instance();
        numIndexed = new int[]{ 0, 0, 0};
    }
    
    public ValueIndexerFactory getIndexerFactory()
    {
        return indexerFactory;
    }

    public void configureReaderProps(File readerProperties) throws FileNotFoundException, IOException 
    {
        readerProps = new Properties();
        readerProps.load(new FileInputStream(readerProperties));
    }
    
    public void configureReader(List<String> inputFilenames) 
    {
        reader = MarcReaderFactory.instance().makeReader(readerProps, inputFilenames);
    }
    
    public void configureIndexer(File indexSpecification, boolean multiThreaded) 
                throws IllegalAccessException, InstantiationException, IOException
    {
        indexers = indexerFactory.createValueIndexers(indexSpecification);
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
    
    public void configureOutput(String solrURL)
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (solrURL.equals("devnull"))
        {
            solrProxy = new DevNullProxy();
        }
        else 
        {
            solrProxy = SolrCoreLoader.loadRemoteSolrServer(solrURL, true);
        }
    }
    
    public void processInput()
    {
        String inEclipseStr = System.getProperty("runInEclipse");
        boolean inEclipse = "true".equalsIgnoreCase(inEclipseStr);
        if (inEclipse) 
        {
            Thread shutdownSimulator = new ShutdownSimulator();
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
            if (!indexer.shuttingDown)  
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            indexer.endProcessing();
        }
        logger.info(""+numIndexed[0]+ " records read");
        logger.info(""+numIndexed[1]+ " records indexed  and ");
        long minutes = ((endTime - startTime) / 1000) / 60;
        long seconds = (endTime - startTime) / 1000  - (minutes * 60);
        long hundredths = (endTime - startTime) / 10  - (minutes * 6000) - (seconds * 100) + 100;
        String hundredthsStr = (""+hundredths).substring(1);
        String minutesStr = ((minutes > 0) ? ""+minutes+" minute"+((minutes != 1)?"s ":" ") : "");
        String secondsStr = ""+seconds+"."+hundredthsStr+" seconds";
        logger.info(""+numIndexed[2]+ " records sent to Solr in "+ minutesStr + secondsStr);
        if (getErrors().size() > 0)
        {
            Collection<RecordAndDoc> errQ = getErrors();
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

    }

    public Collection<RecordAndDoc>  getErrors()
    {
        return(indexer.errQ); 
    }

    private static void extendClasspathWithJar(URLClassLoader sysLoader, File jarfile)
    {
        URL urls[] = sysLoader.getURLs();
        URL ujar;
        try
        {
            ujar = jarfile.toURI().toURL();
        }
        catch (MalformedURLException e)
        {
            // This shouldn't happen since the jarfile is passed in as a file from a directory
            e.printStackTrace();
            return;
        }

        String ujars = ujar.toString();
        for (int i = 0; i < urls.length; i++)
        {
            if (urls[i].toString().equalsIgnoreCase(ujars)) return;
        }
        Class<URLClassLoader> sysClass = URLClassLoader.class;
        try {
            Method method = sysClass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[] {ujar});
        } 
        catch (Throwable t) 
        {
            t.printStackTrace();
        }
    }

    private static void extendClasspath(File dir)
    {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        for (File file : dir.listFiles())
        {
            if (file.getName().endsWith(".jar"))
            {
                extendClasspathWithJar(sysLoader, file);
            }
        }
        try
        {
            Class.forName("org.apache.solr.common.SolrInputDocument");
        }
        catch (ClassNotFoundException e2)
        {
//            try
//            {
//                sysLoader.loadClass("org.apache.solr.common.SolrInputDocument");
//            }
//            catch (ClassNotFoundException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            throw new IndexerSpecException(e2, eErrorSeverity.FATAL, "Unable to load class \"org.apache.solr.common.SolrInputDocument\". Probably cannot find or read the SolrJ libraries");
        }
    }   
    
    @SuppressWarnings("unused")
    public static void main(String[] args)
    {
        OptionParser parser = new OptionParser(  );
        OptionSpec<String> readOpts = parser.acceptsAll(Arrays.asList( "r", "reader_opts"), "file containing MARC Reader options").withRequiredArg().defaultsTo("resources/marcreader.properties");
        OptionSpec<File> configSpec = parser.acceptsAll(Arrays.asList( "c", "config"), "index specification file to use").withRequiredArg().ofType( File.class );
        OptionSpec<File> homeDir = parser.accepts("dir", "directory to look in for scripts, mixins, and translation maps").withRequiredArg().ofType( File.class );
        OptionSpec<File> solrjDir = parser.accepts("solrj", "directory to look in for jars required for SolrJ").withRequiredArg().ofType( File.class );
        OptionSpec<File> errorMarcErrOutFile = parser.accepts("marcerr", "File to write records with errors.").withRequiredArg().ofType( File.class );
        OptionSpec<File> errorIndexErrOutFile = parser.accepts("indexerr", "File to write the solr documents for records with errors.").withRequiredArg().ofType( File.class );
        OptionSpec<File> errorSolrErrOutFile = parser.accepts("solrerr", "File to write the solr documents for records with errors.").withRequiredArg().ofType( File.class );
        parser.accepts("debug", "non-multithreaded debug mode");
        parser.acceptsAll(Arrays.asList( "solrURL", "u"), "URL of Remote Solr to use").withRequiredArg();
        parser.acceptsAll(Arrays.asList("print", "stdout"), "write output to stdout in user readable format");//.availableUnless("sorlURL");
        parser.acceptsAll(Arrays.asList("null"), "discard all output, and merely show errors and warnings");//.availableUnless("sorlURL");
        parser.acceptsAll(Arrays.asList("?", "help"), "show this usage information").forHelp();
        //parser.mutuallyExclusive("stdout", "solrURL");
        OptionSpec<String> files = parser.nonOptions().ofType( String.class );

        OptionSet options = null;
        try {
            options = parser.parse(args );
        }
        catch (OptionException uoe)
        {
            try
            {
                parser.printHelpOn( System.err );
                System.exit(0);
            }
            catch (IOException e)
            {
            }
        }
        if (options.has("help")) 
        {
            try
            {
                parser.printHelpOn(System.err);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.exit(0);
        }
        String homeDirStr = ".";
        if (options.has("dir"))
        {
            homeDirStr = options.valueOf(homeDir).getAbsolutePath();
        }
        final File solrJPath;
        if (options.has(solrjDir))
        {
            solrJPath = options.valueOf(solrjDir);
        }
        else
        {
            solrJPath = new File(homeDirStr, "lib-solrj");
        }
        try
        {
            extendClasspath(solrJPath);
        }
        catch (IndexerSpecException ise)
        {
            logger.fatal("Fatal error: Failure to load SolrJ", ise);
            System.exit(10);
            
        }
        
        IndexDriver indexDriver = new IndexDriver(homeDirStr);
        File f1 = new File(options.valueOf(readOpts));
        List<String> inputFiles = options.valuesOf(files);
        try
        {
            indexDriver.configureReaderProps(f1);
        }
        catch (FileNotFoundException e1)
        {
            logger.fatal("Fatal error: Exception opening reader properties input stream: " + f1.getName());
            System.exit(1);
        }
        catch (IOException e1)
        {
            logger.fatal("Fatal error: Exception opening reader properties input stream: " + f1.getName());
            System.exit(1);
        }
        
     //   String solrURL = "http://libsvr40.lib.virginia.edu:8080/solrgis/nextgen";
        String solrURL = options.has("solrURL") ? options.valueOf("solrURL").toString() : options.has("null") ? "devnull" : "stdout";
        boolean multithread = options.has("solrURL") && !options.has("debug") ? true : false;
        try {
            indexDriver.configureOutput(solrURL);
        }
        catch (SolrRuntimeException sre)
        {
            logger.error("Error connecting to solr at URL "+solrURL, sre);
            System.exit(6);
        }
        File f2 = options.valueOf(configSpec);
        try
        {
            logger.info("Reading and compiling index specification: "+ f2.getName());
            indexDriver.configureIndexer(f2, multithread);
        }
        catch (IOException e1)
        {
            logger.error("Error opening or reading index configuration: " + f2.getName(), e1);
            System.exit(2);
        }
        catch (IllegalAccessException e)
        {
            logger.error("Error processing index configuration: " + f2.getName(), e);
            System.exit(3);
        }
        catch (InstantiationException e)
        {
            logger.error("Error processing index configuration: " + f2.getName(), e);
            System.exit(4);
        }

        List<IndexerSpecException> exceptions = indexDriver.indexerFactory.getValidationExceptions();
        if (!exceptions.isEmpty())
        {
            logger.error("Error processing index configuration: " + f2.getName());
            logTextForExceptions(exceptions);
            System.exit(5);
        }
        else
        {
            logger.info("Opening input files: "+ Arrays.toString(inputFiles.toArray()));
            indexDriver.configureReader(inputFiles);
            
            indexDriver.processInput();
            
//            }
//            finally {
//                indexDriver.endProcessing();
//            }
//            logger.info(""+numIndexed[0]+ " records read");
//            logger.info(""+numIndexed[1]+ " records indexed  and ");
//            long minutes = ((endTime - startTime) / 1000) / 60;
//            long seconds = (endTime - startTime) / 1000  - (minutes * 60);
//            long hundredths = (endTime - startTime) / 10  - (minutes * 6000) - (seconds * 100) + 100;
//            String hundredthsStr = (""+hundredths).substring(1);
//            String minutesStr = ((minutes > 0) ? ""+minutes+" minute"+((minutes != 1)?"s ":" ") : "");
//            String secondsStr = ""+seconds+"."+hundredthsStr+" seconds";
//            logger.info(""+numIndexed[2]+ " records sent to Solr in "+ minutesStr + secondsStr);
//            if (indexDriver.getErrors().size() > 0)
//            {
//                Collection<RecordAndDoc> errQ = indexDriver.getErrors();
//                int[][] errTypeCnt = new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
//                for (final RecordAndDoc entry : errQ)
//                {
//                    if (entry.errLocs.contains(eErrorLocationVal.MARC_ERROR))      errTypeCnt[0][entry.getErrLvl().ordinal()] ++;
//                    if (entry.errLocs.contains(eErrorLocationVal.INDEXING_ERROR))  errTypeCnt[1][entry.getErrLvl().ordinal()] ++;
//                    if (entry.errLocs.contains(eErrorLocationVal.SOLR_ERROR))      errTypeCnt[2][entry.getErrLvl().ordinal()] ++;
//                }
//                showErrReport("MARC", errTypeCnt[0]);
//                showErrReport("Index", errTypeCnt[1]);
//                showErrReport("Solr", errTypeCnt[2]);
//            }
        }
    }

        
    private static void showErrReport(String errLocStr, int[] errorLvlCnt)
    {
        for (int i = 0; i < errorLvlCnt.length; i++)
        {
            if (errorLvlCnt[i] > 0) logger.info( "" + errorLvlCnt[i] + " records have "+errLocStr+" errors of level: "+eErrorSeverity.values()[i].toString());
        }
    }

    @SuppressWarnings("unused")
    private static String getTextForExceptions(List<IndexerSpecException> exceptions)
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
    
    private static void logTextForExceptions(List<IndexerSpecException> exceptions)
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

    private static Priority getPriorityForSeverity(eErrorSeverity level)
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

    class ShutdownSimulator extends Thread
    {
        public void run()
        {
            System.out.println("You're using Eclipse; click in this console and " +
                            "press ENTER to call System.exit() and run the shutdown routine.");
            try {
                System.in.read();
            } 
            catch (IOException e) 
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
    
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

}
