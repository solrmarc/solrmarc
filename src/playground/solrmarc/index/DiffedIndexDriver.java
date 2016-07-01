package playground.solrmarc.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

public class DiffedIndexDriver
{

    public static void main(String[] args)
    {
        ValueIndexerFactory indexerFactory = ValueIndexerFactory.instance();
        List<AbstractValueIndexer<?>> indexers = null;

        File f1 = new File("resources/marcreader.properties");
        Properties readerProps = new Properties();
        try
        {
            readerProps.load(new FileInputStream(f1));
        }
        catch (FileNotFoundException e2)
        {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        catch (IOException e2)
        {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        
        
        File f2 = new File("resources/blacklight_index.properties");
        try
        {
            indexers = indexerFactory.createValueIndexers(f2);
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
        List<IndexerSpecException> exceptions = indexerFactory.getValidationExceptions();
        if (!exceptions.isEmpty())
        {
            System.out.println(getTextForExceptions(exceptions));
        }
        else
        {
            String solrURL = "http://libsvr40.lib.virginia.edu:8080/solrgis/nextgen";
            SolrProxy solrProxy = SolrCoreLoader.loadRemoteSolrServer(solrURL, true, false);
            Indexer indexer = new ThreadedIndexer(indexers, solrProxy, 1000, 1000);
            indexer.setErr(Indexer.eErrorHandleVal.RETURN_ERROR_RECORDS);
            indexer.setErr(Indexer.eErrorHandleVal.INDEX_ERROR_RECORDS);
//            Indexer indexer = new Indexer(indexers, solrProxy);
            
            MarcReader reader;
//            String inputfile = "resources/specTestRecs.mrc";
            String inputfile = "records/uva_001.mrc";
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            int numIndexed = 0;
            try
            {
                reader = MarcReaderFactory.instance().makeReader(new FileInputStream(inputfile), readerProps);
                numIndexed = indexer.indexToSolr(reader);               
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally {
                try
                {
                    endTime = System.currentTimeMillis();
                    solrProxy.commit(false);
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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
