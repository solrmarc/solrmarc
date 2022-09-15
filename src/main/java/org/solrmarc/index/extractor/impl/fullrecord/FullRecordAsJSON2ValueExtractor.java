package org.solrmarc.index.extractor.impl.fullrecord;

import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcWriter;

import java.io.ByteArrayOutputStream;

public class FullRecordAsJSON2ValueExtractor extends AbstractFullRecordValueExtractor
{
    public FullRecordAsJSON2ValueExtractor()
    {
        super();
    }

    @Override
    protected MarcWriter makeNewWriter(ByteArrayOutputStream outputStream)
    {
        return new MarcJsonWriter(outputStream, MarcJsonWriter.MARC_IN_JSON);
    }
}
