package playground.solrmarc.index;


import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.solr.SolrProxy;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Indexer
{
    private final List<AbstractValueIndexer<?>> indexers;
    private final SolrProxy solrProxy;

    public Indexer(final List<AbstractValueIndexer<?>> indexers, final SolrProxy solrProxy)
    {
        this.indexers = indexers;
        this.solrProxy = solrProxy;
    }

    public void index(final MarcReader reader) throws Exception
    {
        while (reader.hasNext())
        {
            final Record record = reader.next();
            final Map<String, Object> document = index(record);
            solrProxy.addDoc(document, false, false);
        }
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
}
