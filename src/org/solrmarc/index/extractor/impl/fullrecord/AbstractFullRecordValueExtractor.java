package org.solrmarc.index.extractor.impl.fullrecord;

import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.AbstractSingleValueExtractor;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * This is a base class for all full record extractors. It writes a record to
 * the writer and reads the formatted text from the writer's outputStream. The
 * formatted text will be the return value of the extraction.
 */
public abstract class AbstractFullRecordValueExtractor extends AbstractSingleValueExtractor
{
    protected MarcWriter writer;
    private final ByteArrayOutputStream outputStream;

    /**
     * @param writer
     *            a marc writer which writes to outputStream.
     * @param outputStream
     *            the stream which collects the output of the writer.
     */
    public AbstractFullRecordValueExtractor()
    {
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public String extract(final Record record) throws UnsupportedEncodingException
    {
        outputStream.reset();
        writer = makeNewWriter(outputStream);
        writer.write(record);
        writer.close();
        writer = null;
        return outputStream.toString("UTF8");
    }

    abstract protected MarcWriter makeNewWriter(ByteArrayOutputStream outputStream2);

}
