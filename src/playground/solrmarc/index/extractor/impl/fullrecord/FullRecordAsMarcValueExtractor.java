package playground.solrmarc.index.extractor.impl.fullrecord;

import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;

import java.io.ByteArrayOutputStream;

public class FullRecordAsMarcValueExtractor extends AbstractFullRecordValueExtractor {
    public FullRecordAsMarcValueExtractor() 
    {
        super();
    }
   
    protected MarcWriter makeNewWriter(ByteArrayOutputStream outputStream)
    {
        return new MarcStreamWriter(outputStream, "UTF-8", true);
    }

}
