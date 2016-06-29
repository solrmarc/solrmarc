package org.solrmarc.index.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.marc.MarcReaderFactory;
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

    public IndexDriver(String homeDirStr)
    {
        ValueIndexerFactory.setHomeDir(homeDirStr);
        indexerFactory = ValueIndexerFactory.instance();
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
            indexer = new ThreadedIndexer(indexers, solrProxy, 640, 1000);
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
        else 
        {
            solrProxy = SolrCoreLoader.loadRemoteSolrServer(solrURL, true);
        }
    }
    
    public int[] processInput() throws Exception
    {
        int numHandled[] = indexer.indexToSolr(reader);
        return numHandled;
    }

    public void endProcessing()
    {
        try
        {
            solrProxy.commit(false);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        OptionParser parser = new OptionParser(  );
        OptionSpec<String> readOpts = parser.acceptsAll(Arrays.asList( "r", "reader_opts"), "file containing MARC Reader options").withRequiredArg().defaultsTo("resources/marcreader.properties");
        OptionSpec<File> configSpec = parser.acceptsAll(Arrays.asList( "c", "config"), "index specification file to use").withRequiredArg().ofType( File.class );
        OptionSpec<File> homeDir = parser.accepts("dir", "directory to look in for scripts, mixins, and translation maps").withRequiredArg().ofType( File.class );
        parser.accepts("debug", "non-multithreaded debug mode");
        parser.acceptsAll(Arrays.asList( "solrURL", "u"), "URL of Remote Solr to use").withRequiredArg();
        parser.acceptsAll(Arrays.asList("print", "stdout"), "write output to stdout in user readable format");//.availableUnless("sorlURL");
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
        IndexDriver indexDriver = new IndexDriver(homeDirStr);
        File f1 = new File(options.valueOf(readOpts));
   //     String inputfile = "records/uva_001.mrc";
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
        String solrURL = options.has("solrURL") ? options.valueOf("solrURL").toString() : "stdout";
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
            logger.error(getTextForExceptions(exceptions));
            System.exit(5);
        }
        else
        {
            logger.info("Opening input files: "+ Arrays.toString(inputFiles.toArray()));
            indexDriver.configureReader(inputFiles);
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            int numIndexed[] = {0, 0, 0};
            try
            {
                numIndexed = indexDriver.processInput();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally {
                endTime = System.currentTimeMillis();
                indexDriver.endProcessing();
            }
            logger.info(""+numIndexed[0]+ " records read");
            logger.info(""+numIndexed[1]+ " records indexed  and ");
            logger.info(""+numIndexed[2]+ " records sent to Solr in "+ (endTime - startTime) / 1000.0 + " seconds");
        }
    }

        
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

}
