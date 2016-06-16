package playground.solrmarc.index;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import org.marc4j.MarcReader;
import org.solrmarc.marc.MarcReaderFactory;

import playground.solrmarc.index.Indexer.eErrorHandleVal;
import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.index.indexer.IndexerSpecException;
import playground.solrmarc.index.indexer.ValueIndexerFactory;
import playground.solrmarc.solr.SolrCoreLoader;
import playground.solrmarc.solr.SolrProxy;

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

    public void configureReader(File readerProperties, InputStream is) throws FileNotFoundException, IOException
    {
        readerProps = new Properties();
        readerProps.load(new FileInputStream(readerProperties));
        reader = MarcReaderFactory.instance().makeReader(is, readerProps);
    }
    
    public void configureIndexer(File indexSpecification, boolean multiThreaded)
    {
        try
        {
            indexers = indexerFactory.createValueIndexers(indexSpecification);
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        solrProxy = SolrCoreLoader.loadRemoteSolrServer(solrURL, true, false);
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
        IndexDriver indexDriver = new IndexDriver();
        File f1 = new File("resources/marcreader.properties");
        String inputfile = "records/uva_001.mrc";
        InputStream marcdata;
        try
        {
            marcdata = new BufferedInputStream(new FileInputStream(inputfile));
            indexDriver.configureReader(f1, marcdata);
        }
        catch (FileNotFoundException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        String solrURL = "http://libsvr40.lib.virginia.edu:8080/solrgis/nextgen";
        indexDriver.configureOutput(solrURL);

        File f2 = new File("resources/blacklight_index.properties");
        indexDriver.configureIndexer(f2, true);

        List<IndexerSpecException> exceptions = indexDriver.indexerFactory.getValidationExceptions();
        if (!exceptions.isEmpty())
        {
            System.out.println(getTextForExceptions(exceptions));
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
            System.out.println(""+numIndexed+ " records indexed in "+ (endTime - startTime) / 1000.0 + " seconds");
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
                text.append(specMessage);
            }
            text.append(e.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
            {
                text.append(e.getSolrField()).append(" : ").append(cause.getMessage());
            }
        }
        return (text.toString());
    }

}
