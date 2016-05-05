package playground.solrmarc.index.indexer;

//import playground.solrmarc.index.collector.AbstractValueCollector;
import playground.solrmarc.index.collector.MultiValueCollector;
import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import playground.solrmarc.index.extractor.MultiValueWrapperSingleValueExtractor;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;
import playground.solrmarc.index.mapping.AbstractValueMapping;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.marc4j.marc.Record;

public class MultiValueIndexer extends AbstractValueIndexer<Collection<String>>
{
    public MultiValueIndexer(final String solrFieldName, final AbstractMultiValueExtractor extractor,
            final AbstractValueMapping<Collection<String>>[] mappings,
            final MultiValueCollector collector)
    {
        super(solrFieldName, extractor, mappings, collector);
    }

    public MultiValueIndexer(final String solrFieldName, final AbstractSingleValueExtractor extractor,
            final AbstractValueMapping<Collection<String>>[] mappings,
            final MultiValueCollector collector)
    {
        super(solrFieldName, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
    }

    public MultiValueIndexer(Collection<String> solrFieldNames, final AbstractMultiValueExtractor extractor,
            final AbstractValueMapping<Collection<String>>[] mappings,
            final MultiValueCollector collector)
    {
        super(solrFieldNames, extractor, mappings, collector);
    }

    public MultiValueIndexer(Collection<String> solrFieldNames, final AbstractMultiValueExtractor extractor,
            final Collection<AbstractMultiValueMapping> mappings,
            final MultiValueCollector collector)
    {
        super(solrFieldNames, extractor, mappings, collector);
    }

    public MultiValueIndexer(Collection<String> solrFieldNames, final AbstractSingleValueExtractor extractor,
            final AbstractValueMapping<Collection<String>>[] mappings,
            final MultiValueCollector collector)
    {
        super(solrFieldNames, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
    }
    
    public MultiValueIndexer(List<String> fieldnames, AbstractSingleValueExtractor extractor,
            List<AbstractMultiValueMapping> mappings, MultiValueCollector collector)
    {
        super(fieldnames, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
    }

    @Override
    public Collection<String> getFieldData(Record record) throws Exception
    {
        Collection<String> values;
        if (extractor == null)
        {
            values = Collections.emptyList();
        }
        else 
        {
            values = extractor.extract(record);
        }
        if (values == null)
        {
            values = Collections.emptyList();
        }
        for (final AbstractValueMapping<Collection<String>> mapping : mappings)
        {
            if (mapping != null) 
            {
                values = mapping.map(values);
            }
        }
        Collection<String> result = collector.collect(values);
        return (result);
    }

}
