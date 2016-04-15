package playground.solrmarc.index.extractor.impl.date;

import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.marc4j.marc.Record;


public class DateOfPublicationValueExtractor implements AbstractSingleValueExtractor {
    @Override
    public String extract(final Record record) {
        // TODO: Use VuFindIndexer#getPublicationDate(Record)
        return null;
    }
}
