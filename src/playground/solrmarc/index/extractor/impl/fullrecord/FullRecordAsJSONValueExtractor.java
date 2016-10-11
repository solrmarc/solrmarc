package playground.solrmarc.index.extractor.impl.fullrecord;

import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcWriter;

import java.io.ByteArrayOutputStream;

public class FullRecordAsJSONValueExtractor extends AbstractFullRecordValueExtractor 
{
    public FullRecordAsJSONValueExtractor() 
    {
        super();
    }

    @Override
    protected MarcWriter makeNewWriter(ByteArrayOutputStream outputStream)
    {
        return new MarcJsonWriter(outputStream, MarcJsonWriter.MARC_JSON);
    }
    
}
