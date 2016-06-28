package org.solrmarc.index;

import java.util.LinkedList;
import java.util.List;

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
    protected static SolrIndexer indexer = SolrIndexer.instance();
//    protected List<IndexerSpecException> errors;
//    
//    public List<IndexerSpecException> getErrors()
//    {
//        return errors;
//    }
//
//    public boolean hasErrors()
//    {
//        return(errors != null && !errors.isEmpty());
//    }
//
    public void addError(IndexerSpecException error)
    {
        ValueIndexerFactory.instance().addPerRecordError(error);
    }
    
    public void perRecordInit()
    {
//        errors = null;       
    }
}
