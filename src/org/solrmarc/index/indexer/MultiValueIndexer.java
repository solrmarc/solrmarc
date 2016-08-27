package org.solrmarc.index.indexer;

//import playground.solrmarc.index.collector.AbstractValueCollector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.marc4j.marc.Record;
import org.solrmarc.index.collector.MultiValueCollector;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.solrmarc.index.extractor.MultiValueWrapperSingleValueExtractor;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.AbstractValueMapping;

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
        long start = System.nanoTime();
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
        long end = System.nanoTime();
        totalElapsedTime.addAndGet(end - start);
        return (result);
    }
}
