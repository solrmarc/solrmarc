package org.solrmarc.index.extractor.impl.fullrecord;

import java.io.ByteArrayOutputStream;

import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;

public class FullRecordAsTextValueExtractor extends AbstractFullRecordValueExtractor
{
    public FullRecordAsTextValueExtractor()
    {
        // Special case: extract() is overridden, so the parameters aren't used.
        super();
    }

    @Override
    public String extract(final Record record)
    {
        return record.toString().replaceAll("\n", "<br/>");
    }

    @Override
    protected MarcWriter makeNewWriter(ByteArrayOutputStream outputStream2)
    {
        return null;
    }

}
