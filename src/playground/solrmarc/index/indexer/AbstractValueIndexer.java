package playground.solrmarc.index.indexer;

import playground.solrmarc.index.collector.MultiValueCollector;
import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;
import playground.solrmarc.index.mapping.AbstractValueMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.marc4j.marc.Record;

public abstract class AbstractValueIndexer<T>
{
    private Collection<String> solrFieldNames;
    protected final AbstractValueExtractor<T> extractor;
    protected final AbstractValueMapping<T>[] mappings;
    protected final MultiValueCollector collector;
    private String specLabel;
    private List<String> parseErrors;

    public AbstractValueIndexer(final String solrFieldName, final AbstractValueExtractor<T> extractor,
            final AbstractValueMapping<T>[] mappings, final MultiValueCollector collector)
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
            final AbstractValueMapping<T>[] mappings, final MultiValueCollector collector)
    {
        this.solrFieldNames = new ArrayList<String>();
        this.solrFieldNames.addAll(solrFieldNames);
        this.extractor = extractor;
        this.mappings = mappings;
        this.collector = collector;
    }

    public AbstractValueIndexer(final Collection<String> solrFieldNames, final AbstractValueExtractor<T> extractor,
            final Collection<AbstractMultiValueMapping> mappings, final MultiValueCollector collector)
    {
        this.solrFieldNames = new ArrayList<String>();
        this.solrFieldNames.addAll(solrFieldNames);
        this.extractor = extractor;
        @SuppressWarnings("unchecked")
        AbstractValueMapping<T>[] tmp = new AbstractValueMapping[mappings.size()];
        this.mappings = mappings.toArray(tmp);
        this.collector = collector;
    }
    
    public String getSpecLabel()
    {
        return specLabel;
    }

    public void setSpecLabel(String specLabel)
    {
        this.specLabel = specLabel;
    }

    public List<String> getParseErrors()
    {
        return parseErrors;
    }

    public void setParseErrors(List<String> parseErrors)
    {
        this.parseErrors = parseErrors;
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getFieldData(Record record) throws Exception
    {
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
        return (result);
    }

    public Collection<String> getSolrFieldNames()
    {
        return solrFieldNames;
    }
}
