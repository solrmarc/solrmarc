package org.solrmarc.index.indexer;

//import playground.org.solrmarc.index.collector.AbstractValueCollector;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import org.solrmarc.index.collector.MultiValueCollector;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.solrmarc.index.extractor.MultiValueWrapperSingleValueExtractor;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.AbstractValueMapping;

import org.marc4j.marc.Record;

public class MultiValueIndexer extends AbstractValueIndexer<Collection<String>>
{
    private boolean onlyIfEmpty = false;
    private boolean onlyIfUnique = false;

    public MultiValueIndexer(final String solrFieldNamesStr, final AbstractMultiValueExtractor extractor,
            final AbstractValueMapping<Collection<String>>[] mappings,
            final MultiValueCollector collector)
    {
        super(solrFieldNamesStr, extractor, mappings, collector);
    }

    public MultiValueIndexer(String solrFieldNamesStr, final AbstractMultiValueExtractor extractor,
            final Collection<AbstractMultiValueMapping> mappings,
            final MultiValueCollector collector)
    {
        super(solrFieldNamesStr, extractor, mappings.toArray(new AbstractMultiValueMapping[0]), collector);
    }

//    public MultiValueIndexer(final String solrFieldName, final AbstractSingleValueExtractor extractor,
//            final AbstractValueMapping<Collection<String>>[] mappings,
//            final MultiValueCollector collector)
//    {
//        super(solrFieldName, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
//    }

//    public MultiValueIndexer(Collection<String> solrFieldNames, final AbstractMultiValueExtractor extractor,
//            final AbstractValueMapping<Collection<String>>[] mappings,
//            final MultiValueCollector collector)
//    {
//        super(solrFieldNames, extractor, mappings, collector);
//    }

    public MultiValueIndexer(String solrFieldNamesStr, final AbstractSingleValueExtractor extractor,
            final AbstractValueMapping<Collection<String>>[] mappings,
            final MultiValueCollector collector)
    {
        super(solrFieldNamesStr, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
    }

    public MultiValueIndexer(String solrFieldNamesStr, AbstractSingleValueExtractor extractor,
            Collection<AbstractMultiValueMapping> mappings, MultiValueCollector collector)
    {
        super(solrFieldNamesStr, new MultiValueWrapperSingleValueExtractor(extractor), mappings.toArray(new AbstractMultiValueMapping[0]), collector);
    }

//    public MultiValueIndexer(Collection<String> solrFieldNames, final AbstractMultiValueExtractor extractor,
//            final Collection<AbstractMultiValueMapping> mappings,
//            final MultiValueCollector collector)
//    {
//        super(solrFieldNames, extractor, mappings, collector);
//    }
//
//    public MultiValueIndexer(Collection<String> solrFieldNames, final AbstractSingleValueExtractor extractor,
//            final AbstractValueMapping<Collection<String>>[] mappings,
//            final MultiValueCollector collector)
//    {
//        super(solrFieldNames, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
//    }
//
//    public MultiValueIndexer(Collection<String> fieldnames, AbstractSingleValueExtractor extractor,
//            Collection<AbstractMultiValueMapping> mappings, MultiValueCollector collector)
//    {
//        super(fieldnames, new MultiValueWrapperSingleValueExtractor(extractor), mappings, collector);
//    }

    // used for making a ThreadSafe copy of the indexer
    public MultiValueIndexer(String solrFieldNamesStr, AbstractMultiValueExtractor extractor,
            AbstractMultiValueMapping[] mappings, MultiValueCollector collector, String specLabel, AtomicLong totalElapsedTime)
    {
        super(solrFieldNamesStr, extractor, mappings, collector);
        this.totalElapsedTime = totalElapsedTime;
        this.setSpecLabel(specLabel);
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

    @Override
    public void setIfEmpty()
    {
        onlyIfEmpty = true;
    }
    @Override
    public boolean getOnlyIfEmpty()
    {
        return(onlyIfEmpty);
    }

    @Override
    public void setIfUnique()
    {
        onlyIfUnique = true;
    }
    @Override
    public boolean getOnlyIfUnique()
    {
        return(onlyIfUnique);
    }
}
