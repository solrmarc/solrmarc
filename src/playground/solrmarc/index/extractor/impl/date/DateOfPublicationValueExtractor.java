package playground.solrmarc.index.extractor.impl.date;

import java.util.Collection;

//import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.marc4j.marc.Record;

import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;


public class DateOfPublicationValueExtractor implements AbstractMultiValueExtractor 
{
    @Override
    public Collection<String> extract(final Record record) 
    {
        // TODO: Use VuFindIndexer#getPublicationDate(Record)
        return null;
    }
}
