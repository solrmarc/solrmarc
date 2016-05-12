package org.solrmarc.index;

import playground.solrmarc.index.extractor.impl.custom.Mixin;

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
    protected static SolrIndexer indexer = new SolrIndexer();
}
