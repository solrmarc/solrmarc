package playground.solrmarc.index.indexer;

import playground.solrmarc.index.collector.AbstractValueCollector;
import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import playground.solrmarc.index.extractor.MultiValueWrapperSingleValueExtractor;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;
import playground.solrmarc.index.mapping.AbstractValueMapping;

import java.util.Collection;

public class MultiValueIndexer extends AbstractValueIndexer<Collection<String>> 
{
    public MultiValueIndexer(final String solrFieldName, 
            final AbstractMultiValueExtractor extractor, 
            final AbstractValueMapping<Collection<String>>[] mappings, 
            final AbstractValueCollector<Collection<String>> collector) 
    {
        super(solrFieldName, extractor, mappings, collector);
    }
    
    public MultiValueIndexer(final String solrFieldName, 
            final AbstractSingleValueExtractor extractor, 
            final AbstractValueMapping<Collection<String>>[] mappings, 
            final AbstractValueCollector<Collection<String>> collector) 
    {
        super(solrFieldName, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
    }
    
    public MultiValueIndexer(Collection<String> solrFieldNames, 
            final AbstractMultiValueExtractor extractor, 
            final AbstractValueMapping<Collection<String>>[] mappings, 
            final AbstractValueCollector<Collection<String>> collector) 
    {
        super(solrFieldNames, extractor, mappings, collector);
    }
    
    public MultiValueIndexer(Collection<String> solrFieldNames, 
            final AbstractMultiValueExtractor extractor, 
            final Collection<AbstractMultiValueMapping> mappings, 
            final AbstractValueCollector<Collection<String>> collector) 
    {
        super(solrFieldNames, extractor, mappings, collector);
    }
    
    public MultiValueIndexer(Collection<String> solrFieldNames, 
            final AbstractSingleValueExtractor extractor, 
            final AbstractValueMapping<Collection<String>>[] mappings, 
            final AbstractValueCollector<Collection<String>> collector) 
    {
        super(solrFieldNames, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
    }
}
