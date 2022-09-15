package org.solrmarc.index.extractor.impl.date;

import java.util.Collection;

//import playground.org.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;


public class DateOfPublicationValueExtractor extends AbstractMultiValueExtractor
{
    @Override
    public Collection<String> extract(final Record record)
    {
        // TODO: Use VuFindIndexer#getPublicationDate(Record)
        return null;
    }
}
