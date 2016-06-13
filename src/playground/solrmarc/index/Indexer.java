package playground.solrmarc.index;

import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.solr.SolrProxy;

import org.apache.solr.common.SolrInputDocument;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Indexer
{
    protected final List<AbstractValueIndexer<?>> indexers;
    protected final SolrProxy solrProxy;

    public Indexer(final List<AbstractValueIndexer<?>> indexers, final SolrProxy solrProxy)
    {
        this.indexers = indexers;
        this.solrProxy = solrProxy;
    }

    public int index(final MarcReader reader) throws Exception
    {
        int cnt = 0;
        while (reader.hasNext())
        {
            final Record record = reader.next();
            final Map<String, Object> document = index(record);
            solrProxy.addDoc(document, false, true);
            cnt++;
        }
        return(cnt);
    }
    
    public int indexToSolr(final MarcReader reader) throws Exception
    {
        int cnt = 0;
        while (reader.hasNext())
        {
            final Record record = reader.next();
            final SolrInputDocument document = indexToSolrDoc(record);
            solrProxy.addDoc(document);
            cnt++;
        }
        return(cnt);
    }

    private Map<String, Object> index(final Record record) throws Exception
    {
        final Map<String, Object> document = new HashMap<>();
        for (final AbstractValueIndexer<?> indexer : indexers)
        {
            final Collection<String> data = indexer.getFieldData(record);
            for (String fieldName : indexer.getSolrFieldNames())
            {
                if (data.size() == 0)
                {
                    /* do_nothing() */
                }
                else if (data.size() == 1)
                {
                    document.put(fieldName, data.iterator().next());
                }
                else
                {
                    for (String dataVal : data)
                    {
                        document.put(fieldName, dataVal);
                    }
                }
            }
        }
        return document;
    }
    
    protected SolrInputDocument indexToSolrDoc(final Record record) throws Exception
    {
        SolrInputDocument inputDoc = new SolrInputDocument();
        for (final AbstractValueIndexer<?> indexer : indexers)
        {
            final Collection<String> data = indexer.getFieldData(record);
            for (String fieldName : indexer.getSolrFieldNames())
            {
                if (data.size() == 0)
                {
                    /* do_nothing() */
                }
                else
                {
                    for (String dataVal : data)
                    {
                        inputDoc.addField(fieldName, dataVal, 1.0f );
                    }
                }
  
            }
        }
        return inputDoc;
    }
}
