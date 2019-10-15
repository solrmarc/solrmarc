package org.solrmarc.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import joptsimple.OptionSet;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.marc4j.MarcReader;
import org.marc4j.MarcReaderConfig;
import org.marc4j.MarcReaderFactory;
import org.solrmarc.driver.RecordAndDoc.eErrorLocationVal;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.marc.SolrMarcMarcReaderFactory;
import org.solrmarc.solr.DevNullProxy;
import org.solrmarc.solr.SolrCoreLoader;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrRuntimeException;
import org.solrmarc.solr.StdOutProxy;
import org.solrmarc.solr.XMLOutProxy;
import org.solrmarc.tools.PropertyUtils;


/**
 * Uses the command-line arguments to create a MarcReader, a collection of AbstractValueIndexer
 * objects, and a SolrProxy object and then passes them to the Indexer class which loops through
 * the MARC records, builds SolrInputDocuments and then sends them to Solr
 *
 * @author rh9ec
 *
 */
public class IndexDriver extends BootableMain
{
    private static Logger logger = null;

    protected Properties readerProps;
    protected MarcReaderConfig readerConfig;
    protected ValueIndexerFactory indexerFactory = null;

    protected List<AbstractValueIndexer<?>> indexers;
    protected Indexer indexer;
    protected MarcReader reader;
    protected SolrProxy solrProxy;
    protected int[] numIndexed;
    protected String[] args;
    protected long startTime;
    protected Thread shutdownSimulator = null;

    /**
     * The main entry point of the SolrMarc indexing process. Typically called by the Boot class.
     *
     * @param args - The command-line arguments passed to the program
     */
    public static void main(String[] args)
    {
        IndexDriver driver = new IndexDriver(args);
        driver.execute();
    }

    /**
     * Provided as an optional entry-point for the SolrMarc indexing process.  It merely stores the
     * command-line arguments so then can be used by the method execute.
     *
     * @param args - The command-line arguments passed to the program
     */
    public IndexDriver(String[] args)
    {
        logger = Logger.getLogger(IndexDriver.class);
        this.args = args;
    }

    /**
     *  Creates a MarcReader, a collection of AbstractValueIndexer objects, and a SolrProxy object 
     *  based on the values in the command-line arguments.  It creates a Indexer object
     *  and calls processInput which passes the MarcReader to the Indexer object to index all of the 
     *  MARC records.
     */
    public void execute()
    {
        processArgs(args, true);
        indexerFactory = ValueIndexerFactory.initialize(homeDirStrs);
        initializeFromOptions();

        List<String> inputFiles = options.valuesOf(files);
        logger.info("Opening input files: " + Arrays.toString(inputFiles.toArray()));
        this.configureReader(inputFiles);
        if (deleteRecordByIdFile.value(options) != null)
        {
            this.processDeletes();
        }

        this.processInput();
    }

    private void processDeletes()
    {
        File deleteFile = deleteRecordByIdFile.value(options);
        if (deleteFile.exists() && deleteFile.canRead())
        {
            try
            {
                BufferedReader delReader = new BufferedReader(new FileReader(deleteFile));
                String line;
                while ((line = delReader.readLine()) != null)
                {
                    indexer.delQ.add(line.trim());
                }
                delReader.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void initializeFromOptions()
    {
        String[] inputSource = new String[1];
        String propertyFileAsURLStr = PropertyUtils.getPropertyFileAbsoluteURL(homeDirStrs, options.valueOf(readOpts), true, inputSource);

        try
        {
            configureReaderProps(propertyFileAsURLStr);
        }
        catch (IOException e1)
        {
            logger.fatal("Fatal error: Exception opening reader properties input stream: " + inputSource[0]);
            logger.error("Exiting...");
            System.exit(1);
        }

        boolean multithread = options.has("solrURL") && !options.has("debug") ? true : false;
        try
        {
            this.configureOutput(options);
        }
        catch (SolrRuntimeException sre)
        {
            logger.debug("", sre);
            logger.error("Exiting...");
            System.exit(6);
        }
        String specs = options.valueOf(configSpecs);
        try
        {
            logger.info("Reading and compiling index specifications: " + specs);
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

    final static String [] solrmarcPropertyStrings = {
            "solrmarc.indexer.chunksize",
            "solrmarc.indexer.threadcount",
            "solrmarc.solrj.threadcount",
            "solrmarc.track.solr.progress",
            "solrmarc.terminate.on.marc.exception",
            "solrmarc.output.redirect",
            "solrmarc.indexer.test.fire.method",
            "solrmarc.method.report",
    };

    private void configureReaderProps(String propertyFileURLStr) throws FileNotFoundException, IOException
    {
        List<String> propertyStringsToCopy = Arrays.asList(solrmarcPropertyStrings);
        readerProps = new Properties();
        if (propertyFileURLStr != null)
        {
            readerProps.load(PropertyUtils.getPropertyFileInputStream(propertyFileURLStr));
            Enumeration<?> iter = readerProps.propertyNames();
            while (iter.hasMoreElements())
            {
                String propertyName = iter.nextElement().toString();
                if (propertyName.startsWith("solrmarc.") && propertyStringsToCopy.contains(propertyName) && System.getProperty(propertyName) == null)
                {
                    System.setProperty(propertyName, readerProps.getProperty(propertyName));
                }
            }
            try {
                readerConfig = new MarcReaderConfig(readerProps);
            }
            catch(NoClassDefFoundError ncdfe)
            {
                readerConfig = null;
            }
        }
    }

    private void configureReader(List<String> inputFilenames)
    {
        try
        {
            reader = MarcReaderFactory.makeReader((MarcReaderConfig)readerConfig, ValueIndexerFactory.instance().getHomeDirs(), inputFilenames);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        catch (NoClassDefFoundError ncdfe)
        {
            logger.warn("Using SolrMarc with a marc4j version < 2.8 uses deprecated code in SolrMarc");
            reader = SolrMarcMarcReaderFactory.instance().makeReader(readerProps, ValueIndexerFactory.instance().getHomeDirs(), inputFilenames);
        }
    }

    protected  void configureIndexer(String indexSpecifications, boolean multiThreaded)
            throws IllegalAccessException, InstantiationException, IOException
    {
        String[] indexSpecs = indexSpecifications.split("[ ]*,[ ]*");
        File[] specFiles = new File[indexSpecs.length];
        int i = 0;
        for (String indexSpec : indexSpecs)
        {
            File specFile = new File(indexSpec);
            if (!specFile.isAbsolute())
            {
                specFile = PropertyUtils.findFirstExistingFile(homeDirStrs, indexSpec);
            }
            logger.info("Opening index spec file: " + specFile);
            specFiles[i++] = specFile;
        }
        indexers = indexerFactory.createValueIndexers(specFiles);
        boolean includeErrors = Boolean.parseBoolean(PropertyUtils.getProperty(readerProps, "marc.include_errors", "false"));
        boolean returnErrors = Boolean.parseBoolean(PropertyUtils.getProperty(readerProps, "marc.return_errors", "false"));
        int chunkSize = Integer.parseInt(System.getProperty("solrmarc.indexer.chunksize", "640"));
        indexer = null;
        if (multiThreaded) indexer = new ThreadedIndexer(indexers, solrProxy, chunkSize);
        else               indexer = new Indexer(indexers, solrProxy);

        if (returnErrors)
        {
            indexer.setErr(Indexer.eErrorHandleVal.RETURN_ERROR_RECORDS);
        }
        if (includeErrors)
        {
            indexer.setErr(Indexer.eErrorHandleVal.INDEX_ERROR_RECORDS);
        }
    }

    protected void configureOutput(OptionSet options)
    {
        String solrJClassName = solrjClass.value(options);
        String solrURL = options.has("solrURL") ? options.valueOf("solrURL").toString() : options.has("null") ? "devnull" : "stdout";
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
        else if (solrURL.equals("xml"))
        {
            try
            {
                PrintStream out = new PrintStream(System.out, true, "UTF-8");
                System.setOut(out);
                solrProxy = new XMLOutProxy(out);
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
            try  {
                solrProxy = SolrCoreLoader.loadRemoteSolrServer(solrURL, solrJClassName, true);
            }
            catch (SolrRuntimeException sre) 
            {
                logger.error("Error connecting to solr at URL " + solrURL + " : " + sre.getMessage());
                throw(sre);
            }
        }
    }

    protected void processInput()
    {
        String inEclipseStr = System.getProperty("runInEclipse");
        boolean inEclipse = "true".equalsIgnoreCase(inEclipseStr);
        String systemClassPath = System.getProperty("java.class.path");
        logger.debug("System Class Path = " + systemClassPath);
        if (!systemClassPath.contains("solrmarc_core"))
        {
            inEclipse = true;
        }
        shutdownSimulator = new ShutdownSimulator(inEclipse);
        shutdownSimulator.start();
        Thread shutdownHook = new MyShutdownThread(indexer, shutdownSimulator);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        startTime = System.currentTimeMillis();
        long endTime = startTime;

        try
        {
            numIndexed = indexer.indexToSolr(reader);
        }
        catch (Exception e)
        {
            if (!indexer.viaInterrupt)
            {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            logger.fatal("ERROR: Error while invoking indexToSolr");
            logger.fatal(e);
        }

        endTime = System.currentTimeMillis();
        if (!indexer.viaInterrupt)
        {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }
        indexer.endProcessing();

        boolean perMethodReport = Boolean.parseBoolean(PropertyUtils.getProperty(readerProps, "solrmarc.method.report", "false"));
        reportResultsAndTime(numIndexed, startTime, endTime, indexer, (indexer.shuttingDown) ? false : perMethodReport);
        if (!indexer.viaInterrupt && indexer.errQ.size() > 0)
        {
            handleRecordErrors();
        }

        if (!indexer.viaInterrupt && shutdownSimulator != null)
        {
            shutdownSimulator.interrupt();
        }
        indexer.setIsShutDown();
        if (indexer.shuttingDown && indexer.viaInterrupt)
        {
            try
            {
                Thread.sleep(5000L);
            }
            catch (InterruptedException ie)
            {
                endTime = startTime;
            }
        }
    }

    protected void reportResultsAndTime(int[] numIndexed, long startTime, long endTime, Indexer indexer, boolean perMethodReport)
    {
        logger.info("" + numIndexed[0] + " records read");
        logger.info("" + numIndexed[1] + " records indexed  and ");
        long minutes = ((endTime - startTime) / 1000) / 60;
        long seconds = (endTime - startTime) / 1000 - (minutes * 60);
        long hundredths = (endTime - startTime) / 10 - (minutes * 6000) - (seconds * 100) + 100;
        String hundredthsStr = ("" + hundredths).substring(1);
        String minutesStr = ((minutes > 0) ? "" + minutes + " minute" + ((minutes != 1) ? "s " : " ") : "");
        String secondsStr = "" + seconds + "." + hundredthsStr + " seconds";
        logger.info("" + numIndexed[2] + " records sent to Solr in " + minutesStr + secondsStr);
        if (perMethodReport)
        {
            indexer.reportPerMethodTime();
        }
    }

    private void handleRecordErrors()
    {
        Collection<RecordAndDoc> errQ = indexer.errQ;
        int[][] errTypeCnt = new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
        for (final RecordAndDoc entry : errQ)
        {
            if (!entry.errLocs.isEmpty())
            {
                logger.debug("Error Rec id = " + entry.rec.getControlNumber());
            }
            if (entry.errLocs.contains(eErrorLocationVal.MARC_ERROR))
            {
                errTypeCnt[0][entry.getErrLvl().ordinal()]++;
            }
            if (entry.errLocs.contains(eErrorLocationVal.INDEXING_ERROR))
            {
                errTypeCnt[1][entry.getErrLvl().ordinal()]++;
            }
            if (entry.errLocs.contains(eErrorLocationVal.SOLR_ERROR))
            {
                errTypeCnt[2][entry.getErrLvl().ordinal()]++;
            }
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
                logger.info(errorLvlCnt[i] + " records have " + errLocStr + " errors of level: " + IndexerSpecException.eErrorSeverity.values()[i].toString());
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

    protected void logTextForExceptions(List<IndexerSpecException> exceptions)
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
                logger.log(priority, e.getSolrField() + " : " + cause.getMessage());
            }
        }
    }

    private Priority getPriorityForSeverity(eErrorSeverity level)
    {
        switch (level) {
            case NONE:  return (Level.DEBUG);
            case INFO:  return (Level.INFO);
            case WARN:  return (Level.WARN);
            case ERROR: return (Level.ERROR);
            case FATAL: return (Level.FATAL);
        }
        return (Level.DEBUG);
    }

    /**
     * <h1>MyShutdownThread</h1> This class implements a shutdown hook that is
     * installed in the Java Runtime. If a user attempts to terminate the import
     * process, this hook will signal the threads that are handling the import
     * (via Thread.interrupt) and they will shutdown cleanly, and commit the
     * changes to Solr before allowing the program to terminate.
     *
     * @author rh9ec
     *
     */
    class MyShutdownThread extends Thread
    {
        private Indexer indexer;
        private Thread killItToDie;

        public MyShutdownThread(Indexer ind, Thread shutdownSimulator)
        {
            indexer = ind;
            killItToDie = shutdownSimulator;
        }

        @Override
        public void run()
        {
            logger.info("Starting Shutdown hook");

            if (!indexer.isShutDown())
            {
                logger.info("Stopping main indexer loop");
                indexer.shutDown(true);
            }
            while (!indexer.isShutDown())
            {
                try
                {
                    sleep(2000L);
                }
                catch (InterruptedException e)
                {
                }
            }
            logger.info("Finished Shutdown hook");
            LogManager.shutdown();
            try
            {
                sleep(1000L);
            }
            catch (InterruptedException e)
            {
                indexer = null;
            }
            killItToDie.interrupt();
        }
    }

    /**
     * <h1>ShutdownSimulator</h1> A small class that is only useful for
     * debugging purposes. Specifically for debugging the shutdown hook. The
     * Eclipse Java Development Environment is unable to shutdown a process it
     * is running in a way that the shutdown hook is invoked, instead Eclipse
     * merely summarily destroys the process, which is unhelpful. <br/>
     * To enable this feature, you must define the system property
     * "runInEclipse" as true, usually via the VM arguments panel on the
     * Arguments tab in the debug configuration dialog. -DrunInEclipse=true Then
     * when the program is running in Eclipse, you will need to click in the
     * Console window, and press [ENTER] to simulate a CTRL-C being sent to the
     * program.
     *
     * @author rh9ec
     *
     */
    class ShutdownSimulator extends Thread
    {
        boolean inEclipse;
        public ShutdownSimulator(boolean inEclipse)
        {
            this.inEclipse = inEclipse;
        }
        @Override
        public void run()
        {
            setName("Eclipse-Shutdown-Simulator-Thread");
            if (inEclipse)
            {
                System.out.println("You're using Eclipse; click in this console and "
                    + "press ENTER to call System.exit() and run the shutdown routine.");
            }
            while (true)
            {
                try
                {
                    if (inEclipse && System.in.available() > 0)
                    {
                        System.in.read();
                        System.exit(0);
                    }
                    else
                    {
                        sleep(2000L);
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
