package playground.solrmarc.index.indexer;

import java.util.Collection;

import org.marc4j.marc.Record;

import playground.solrmarc.index.collector.FieldMatchCollector;
import playground.solrmarc.index.extractor.AbstractMultiValueFieldMatchExtractor;
import playground.solrmarc.index.fieldmatch.FieldMatch;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;
import playground.solrmarc.index.mapping.AbstractValueMapping;

public class MultiValueFieldMatchIndexer extends AbstractValueIndexer<Collection<String>>
{

    final private AbstractMultiValueFieldMatchExtractor fmExtractor;
    final private FieldMatchCollector fmCollector;
    
    public MultiValueFieldMatchIndexer(String solrFieldName, AbstractMultiValueFieldMatchExtractor multiValueExtractor,
            AbstractMultiValueMapping[] mappings, FieldMatchCollector collector)
    {
        super(solrFieldName, null, mappings, null);
        fmExtractor = multiValueExtractor;
        fmCollector = collector;
    }
    
    @Override
    public Collection<String> getFieldData(Record record) throws Exception
    {
        Collection<FieldMatch> values = fmExtractor.extract(record);
        if (values == null)
        {
            return null;
        }
        Collection<String> intermediate = (Collection<String>)fmCollector.collect(values);
        for (final AbstractValueMapping<Collection<String>> mapping : mappings)
        {
            intermediate = mapping.map(intermediate);
        }
        return(intermediate);
    }


}
