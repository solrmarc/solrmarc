package playground.solrmarc.index.indexer;

import playground.solrmarc.index.collector.AbstractValueCollector;
import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;
import playground.solrmarc.index.mapping.AbstractValueMapping;

import java.util.ArrayList;
import java.util.Collection;

import org.marc4j.marc.Record;

public abstract class AbstractValueIndexer<T>
{
    private Collection<String> solrFieldNames;
    private final AbstractValueExtractor<T> extractor;
    protected final AbstractValueMapping<T>[] mappings;
    private final AbstractValueCollector<T> collector;

    public AbstractValueIndexer(final String solrFieldName, final AbstractValueExtractor<T> extractor,
            final AbstractValueMapping<T>[] mappings, final AbstractValueCollector<T> collector)
    {
        this.solrFieldNames = new ArrayList<String>();
        String[] fieldNames = solrFieldName.split("[ ]?,[ ]?");
        for (String fName : fieldNames)
        {
            this.solrFieldNames.add(fName);
        }
        this.extractor = extractor;
        this.mappings = mappings;
        this.collector = collector;
    }

    public AbstractValueIndexer(final Collection<String> solrFieldNames, final AbstractValueExtractor<T> extractor,
            final AbstractValueMapping<T>[] mappings, final AbstractValueCollector<T> collector)
    {
        this.solrFieldNames = new ArrayList<String>();
        this.solrFieldNames.addAll(solrFieldNames);
        this.extractor = extractor;
        this.mappings = mappings;
        this.collector = collector;
    }

    public AbstractValueIndexer(final Collection<String> solrFieldNames, final AbstractValueExtractor<T> extractor,
            final Collection<AbstractMultiValueMapping> mappings, final AbstractValueCollector<T> collector)
    {
        this.solrFieldNames = new ArrayList<String>();
        this.solrFieldNames.addAll(solrFieldNames);
        this.extractor = extractor;
        @SuppressWarnings("unchecked")
        AbstractValueMapping<T>[] tmp = new AbstractValueMapping[mappings.size()];
        this.mappings = mappings.toArray(tmp);
        this.collector = collector;
    }

    public Collection<String> getFieldData(Record record) throws Exception
    {
        T values = extractor.extract(record);
        if (values == null)
        {
            return null;
        }
        for (final AbstractValueMapping<T> mapping : mappings)
        {
            values = mapping.map(values);
        }
        Collection<String> result = collector.collect(values);
        return (result);
    }

    public Collection<String> getSolrFieldNames()
    {
        return solrFieldNames;
    }
}
