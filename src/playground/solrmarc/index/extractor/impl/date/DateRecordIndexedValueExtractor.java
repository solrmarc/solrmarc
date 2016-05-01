package playground.solrmarc.index.extractor.impl.date;

//import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.marc4j.marc.Record;

import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class DateRecordIndexedValueExtractor extends AbstractMultiValueExtractor
{
    private final static String currentDate;

    static
    {
        final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        currentDate = df.format(new Date());
    }

    @Override
    public Collection<String> extract(final Record record)
    {
        return Collections.singletonList(currentDate);
    }
}
