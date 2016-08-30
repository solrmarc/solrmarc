package org.solrmarc.index.indexer;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import org.marc4j.marc.Record;
import org.solrmarc.index.collector.MultiValueCollector;
import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.AbstractValueMapping;

import com.rits.cloning.Cloner;

public abstract class AbstractValueIndexer<T>
{
    private Collection<String> solrFieldNames;
    protected final AbstractValueExtractor<T> extractor;
    protected AbstractValueMapping<T>[] mappings;
    protected final MultiValueCollector collector;
    private String specLabel;
    protected AtomicLong totalElapsedTime;
 //   private List<String> parseErrors;

    public AbstractValueIndexer(final String solrFieldNames, final AbstractValueExtractor<T> extractor,
            final AbstractValueMapping<T>[] mappings, final MultiValueCollector collector)
    {
        setSolrFieldNames(solrFieldNames);
        this.extractor = extractor;
        this.mappings = mappings;
        this.collector = collector;
        totalElapsedTime = new AtomicLong(0);
    }

    public AbstractValueIndexer(final Collection<String> solrFieldNames, final AbstractValueExtractor<T> extractor,
            final AbstractValueMapping<T>[] mappings, final MultiValueCollector collector)
    {
        setSolrFieldNames(solrFieldNames);
        this.extractor = extractor;
        this.mappings = mappings;
        this.collector = collector;
        totalElapsedTime = new AtomicLong(0);
    }

    public AbstractValueIndexer(final Collection<String> solrFieldNames, final AbstractValueExtractor<T> extractor,
            final Collection<AbstractMultiValueMapping> mappings, final MultiValueCollector collector)
    {
        setSolrFieldNames(solrFieldNames);
        this.extractor = extractor;
        @SuppressWarnings("unchecked")
        AbstractValueMapping<T>[] tmp = new AbstractValueMapping[mappings.size()];
        this.mappings = mappings.toArray(tmp);
        this.collector = collector;
        totalElapsedTime = new AtomicLong(0);
    }
    
    protected AbstractValueIndexer(AbstractValueIndexer<T> toClone)
    {
        this.solrFieldNames = toClone.solrFieldNames;
        this.extractor = Cloner.standard().deepClone(toClone.extractor);
        this.collector = toClone.collector;
        this.totalElapsedTime = toClone.totalElapsedTime;
        this.specLabel = toClone.specLabel;
    }
//    
//    public AbstractValueIndexer<?> clone()
//    {
//        return(new AbstractValueIndexer(this));
//    }
    
    public String getSpecLabel()
    {
        return specLabel;
    }

    public void setSpecLabel(String specLabel)
    {
        this.specLabel = specLabel;
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getFieldData(Record record) throws Exception
    {
        long start = System.nanoTime();
        if (extractor == null) return Collections.emptyList();
        T values = extractor.extract(record);
        if (values == null)
        {
            return  Collections.emptyList();
        }
        for (final AbstractValueMapping<T> mapping : mappings)
        {
            values = mapping.map(values);
        }
        Collection<String> result = null;
        if (values instanceof Collection) 
            result = collector.collect((Collection<String>)values);
        else if (values instanceof String)
            result = collector.collect(Collections.singletonList((String)values));
        long end = System.nanoTime();
        totalElapsedTime.addAndGet(end - start);
        return (result);
    }
   
    @SuppressWarnings("unchecked")
    public void getFieldData(Record record, Collection<String> result) throws Exception
    {
        long start = System.nanoTime();
        if (extractor == null) return;
        T values = extractor.extract(record);
        if (values == null)
        {
            return;
        }
        for (final AbstractValueMapping<T> mapping : mappings)
        {
            values = mapping.map(values);
        }
        if (values instanceof Collection) 
            result.addAll(collector.collect((Collection<String>)values));
        else if (values instanceof String)
            result.addAll(collector.collect(Collections.singletonList((String)values)));
        long end = System.nanoTime();
        totalElapsedTime.addAndGet(end - start);
    }

    
    public Long getTotalElapsedTime()
    {
        return totalElapsedTime.get();
    }

    public Collection<String> getSolrFieldNames()
    {
        return solrFieldNames;
    }
    
    public void setSolrFieldNames(String solrFieldNames)
    {
        this.solrFieldNames = new ArrayList<String>();
        if (solrFieldNames != null)
        {
            // The trim and whitespace in the pattern for split may well be unnecessary since the string  
            // should have had all whitespace removed,  but just in case.
            String[] fieldNames = solrFieldNames.trim().split("[ \\t]*,[ \\t]*");
            for (String fName : fieldNames)
            {
                this.solrFieldNames.add(fName);
            }    
        }
    }

    public void setSolrFieldNames(Collection<String> solrFieldNames)
    {
        this.solrFieldNames = new ArrayList<String>();
        this.solrFieldNames.addAll(solrFieldNames);
    }
}
