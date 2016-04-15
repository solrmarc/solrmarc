package playground.solrmarc.index.extractor.impl.date;

import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.marc4j.marc.Record;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateRecordIndexedValueExtractor implements AbstractSingleValueExtractor {
    private final static String currentDate;

    static {
        final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        currentDate = df.format(new Date());
    }

    @Override
    public String extract(final Record record) {
        return currentDate;
    }
}
