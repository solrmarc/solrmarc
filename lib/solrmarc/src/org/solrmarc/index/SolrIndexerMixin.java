package org.solrmarc.index;

import org.marc4j.marc.Record;

public abstract class SolrIndexerMixin
{    
    protected SolrIndexer indexer = null;
    
    public SolrIndexerMixin()
    {
        // place holder does nothing
    }
    
    public void perRecordInit(Record record)
    {
        
    }
    
    public final void setMainIndexer(SolrIndexer mainIndexer)
    {
        indexer = mainIndexer;
    }
}
