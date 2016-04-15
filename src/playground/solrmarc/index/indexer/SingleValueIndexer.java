package playground.solrmarc.index.indexer;


import playground.solrmarc.index.collector.AbstractValueCollector;
import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.mapping.AbstractValueMapping;


public class SingleValueIndexer extends AbstractValueIndexer<String>
{
    public SingleValueIndexer(final String solrFieldName, final AbstractValueExtractor<String> extractor, final AbstractValueMapping<String>[] mappings, final AbstractValueCollector<String> collector)
    {
        super(solrFieldName, extractor, mappings, collector);
    }
}
