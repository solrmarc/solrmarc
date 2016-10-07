package org.solrmarc.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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

public class IndexDriver extends BootableMain
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
    String[] args;
    long startTime;
    Thread shutdownSimulator = null;

    public static void main(String[] args)
    {
        IndexDriver driver = new IndexDriver(args);
        driver.execute();
    }

    public IndexDriver(String[] args)
    {
        this.args = args;
    }

    public void execute()
    {
        processArgs(args, true);
        indexerFactory = ValueIndexerFactory.initialize(homeDirStrs);
        initializeFromOptions();

        List<String> inputFiles = options.valuesOf(files);
        logger.info("Opening input files: " + Arrays.toString(inputFiles.toArray()));
        this.configureReader(inputFiles);

        this.processInput();
    }

    public void initializeFromOptions()
    {
        String inputSource[] = new String[1];
        String propertyFileAsURLStr = PropertyUtils.getPropertyFileAbsoluteURL(homeDirStrs, options.valueOf(readOpts), true, inputSource);

        // File f1 = new File(options.valueOf(readOpts));
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

        // String solrURL = "http://libsvr40.lib.virginia.edu:8080/solrgis/nextgen";
        String solrJClassName = solrjClass.value(options);
        String solrURL = options.has("solrURL") ? options.valueOf("solrURL").toString() : options.has("null") ? "devnull" : "stdout";
        boolean multithread = options.has("solrURL") && !options.has("debug") ? true : false;
        try
        {
            this.configureOutput(solrURL, solrJClassName);
        }
        catch (SolrRuntimeException sre)
        {
            logger.error("Error connecting to solr at URL " + solrURL + " : " + sre.getMessage());
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

    public void configureReaderProps(String propertyFileURLStr) throws FileNotFoundException, IOException
    {
        readerProps = new Properties();
        if (propertyFileURLStr != null)
        {
            readerProps.load(PropertyUtils.getPropertyFileInputStream(propertyFileURLStr));
        }
    }

    public void configureReader(List<String> inputFilenames)
    {
        reader = MarcReaderFactory.instance().makeReader(readerProps, ValueIndexerFactory.instance().getHomeDirs(), inputFilenames);
    }

    public void configureIndexer(String indexSpecifications, boolean multiThreaded)
            throws IllegalAccessException, InstantiationException, IOException
    {
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
        boolean includeErrors = Boolean.parseBoolean(PropertyUtils.getProperty(readerProps, "marc.include_errors", "false"));
        boolean returnErrors = Boolean.parseBoolean(PropertyUtils.getProperty(readerProps, "marc.return_errors", "false"));
        indexer = null;
        if (multiThreaded) indexer = new ThreadedIndexer(indexers, solrProxy, 640);
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
            if (!indexer.shuttingDown) Runtime.getRuntime().removeShutdownHook(shutdownHook);
            logger.fatal("ERROR: Error while invoking indexToSolr");
            logger.fatal(e);
        }

        endTime = System.currentTimeMillis();

        if (!indexer.shuttingDown) Runtime.getRuntime().removeShutdownHook(shutdownHook);
        indexer.endProcessing();

        boolean perMethodReport = Boolean.parseBoolean(PropertyUtils.getProperty(readerProps, "solrmarc.method.report", "false"));
        reportResultsAndTime(numIndexed, startTime, endTime, indexer, (indexer.shuttingDown) ? false : perMethodReport);
        if (! indexer.shuttingDown && indexer.errQ.size() > 0)
        {
            handleRecordErrors();
        }

        if (!indexer.shuttingDown && shutdownSimulator != null) shutdownSimulator.interrupt();
        indexer.setIsShutDown();
        if (indexer.shuttingDown)
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException ie)
            {
                endTime = startTime;
            }
        }
    }

    private void reportResultsAndTime(int[] numIndexed, long startTime, long endTime, Indexer indexer, boolean perMethodReport)
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
        if (perMethodReport) indexer.reportPerMethodTime();
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
                logger.info("" + errorLvlCnt[i] + " records have " + errLocStr + " errors of level: " + eErrorSeverity.values()[i].toString());
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
            // System.err.println("Starting Shutdown hook");
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
//            int[] counts = indexer.getCounts();
//            long endTime = System.currentTimeMillis();
//            reportResultsAndTime(counts, startTime, endTime, indexer, false);
            logger.info("Finished Shutdown hook");
            LogManager.shutdown();
            try
            {
                sleep(1000);
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
