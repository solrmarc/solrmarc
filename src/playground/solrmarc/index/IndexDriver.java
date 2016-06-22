package playground.solrmarc.index;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.marc4j.MarcReader;
import org.solrmarc.marc.MarcReaderFactory;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.index.indexer.IndexerSpecException;
import playground.solrmarc.index.indexer.ValueIndexerFactory;
import playground.solrmarc.solr.SolrCoreLoader;
import playground.solrmarc.solr.SolrProxy;
import playground.solrmarc.solr.SolrRuntimeException;
import playground.solrmarc.solr.StdOutProxy;

public class IndexDriver
{
    Properties readerProps;
    ValueIndexerFactory indexerFactory = null;

    List<AbstractValueIndexer<?>> indexers;
    Indexer indexer;
    MarcReader reader;
    SolrProxy solrProxy;

    public IndexDriver()
    {
        indexerFactory = ValueIndexerFactory.instance();
    }
    
    public ValueIndexerFactory getIndexerFactory()
    {
        return indexerFactory;
    }

    public void configureReader(File readerProperties, List<String> inputFilenames) throws FileNotFoundException, IOException 
    {
        readerProps = new Properties();
        readerProps.load(new FileInputStream(readerProperties));
        reader = MarcReaderFactory.instance().makeReader(readerProps, inputFilenames);
    }
    
    public void configureIndexer(File indexSpecification, boolean multiThreaded) 
                throws IllegalAccessException, InstantiationException, IOException
    {
//        try
//        {
            indexers = indexerFactory.createValueIndexers(indexSpecification);
//        }
//        catch (IllegalAccessException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        catch (InstantiationException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        catch (FileNotFoundException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        boolean includeErrors = (readerProps.getProperty("marc.include_errors", "false").equals("true"));
        indexer = null;
        if (multiThreaded)
            indexer = new ThreadedIndexer(indexers, solrProxy, 500, 1000);
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
    
    public int processInput() throws Exception
    {
        int numIndexed = indexer.indexToSolr(reader);
        return numIndexed;
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
        IndexDriver indexDriver = new IndexDriver();
        File f1 = new File(options.valueOf(readOpts));
   //     String inputfile = "records/uva_001.mrc";
        List<String> inputFiles = options.valuesOf(files);
        try
        {
            indexDriver.configureReader(f1, inputFiles);
        }
        catch (FileNotFoundException e1)
        {
            System.err.println("Fatal error: Exception opening reader properties input stream" + f1.getName());
            System.exit(1);
        }
        catch (IOException e1)
        {
            System.err.println("Fatal error: Exception opening reader properties input stream" + f1.getName());
            System.exit(1);
        }
        
     //   String solrURL = "http://libsvr40.lib.virginia.edu:8080/solrgis/nextgen";
        String solrURL = options.has("stdout") ? "stdout" : options.valueOf("solrURL").toString();
        boolean multithread = options.has("stdout") ? false : true;
        try {
            indexDriver.configureOutput(solrURL);
        }
        catch (SolrRuntimeException sre)
        {
            //System.err.println("Error connecting to solr at URL "+solrURL);
            System.err.println(sre.getMessage());
            System.exit(6);
        }

        File f2 = options.valueOf(configSpec);
        try
        {
            indexDriver.configureIndexer(f2, multithread);
        }
        catch (IOException e1)
        {
            System.err.println("Error opening or reading index configuration: " + f2.getName());
            System.exit(2);
        }
        catch (IllegalAccessException e)
        {
            System.err.println("Error processing index configuration: " + f2.getName());
            e.printStackTrace();
            System.exit(3);
        }
        catch (InstantiationException e)
        {
            System.err.println("Error processing index configuration: " + f2.getName());
            e.printStackTrace();
            System.exit(4);
        }

        List<IndexerSpecException> exceptions = indexDriver.indexerFactory.getValidationExceptions();
        if (!exceptions.isEmpty())
        {
            System.err.println(getTextForExceptions(exceptions));
            System.exit(5);
        }
        else
        {
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            int numIndexed = 0;
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
            System.err.println(""+numIndexed+ " records indexed in "+ (endTime - startTime) / 1000.0 + " seconds");
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
