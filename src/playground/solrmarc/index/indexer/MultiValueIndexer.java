package playground.solrmarc.index.indexer;

import playground.solrmarc.index.collector.AbstractValueCollector;
import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.mapping.AbstractValueMapping;

import java.util.Collection;

public class MultiValueIndexer extends AbstractValueIndexer<Collection<String>> 
{
    public MultiValueIndexer(final String solrFieldName, 
            final AbstractValueExtractor<Collection<String>> extractor, 
            final AbstractValueMapping<Collection<String>>[] mappings, 
            final AbstractValueCollector<Collection<String>> collector) 
    {
        super(solrFieldName, extractor, mappings, collector);
    }
}
