package org.solrmarc.index;

import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.impl.custom.Mixin;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;


/**
 * class SolrIndexerMixin
 * 
 * This class exists solely for backwards compatibility purposes.  The intention is that if a previous custom mixin class
 * was being used, it will be able to compile by referencing this class.  
 * 
 * 
 * @author rh9ec
 *
 */

public class SolrIndexerMixin implements Mixin
{
    @SuppressWarnings("deprecation")
    protected static SolrIndexerShim indexer = SolrIndexerShim.instance();

    public void addError(IndexerSpecException error)
    {
        ValueIndexerFactory.instance().addPerRecordError(error);
    }

    public void perRecordInit(Record record) throws Exception
    {
    }
}
