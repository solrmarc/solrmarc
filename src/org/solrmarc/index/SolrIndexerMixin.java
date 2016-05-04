package org.solrmarc.index;

import playground.solrmarc.index.extractor.impl.custom.Mixin;

public class SolrIndexerMixin implements Mixin
{
    protected static SolrIndexer indexer = new SolrIndexer();
}
