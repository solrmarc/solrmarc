package playground.solrmarc.index.extractor.impl.fullrecord;

import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;

import java.io.ByteArrayOutputStream;

public class FullRecordAsXMLValueExtractor extends AbstractFullRecordValueExtractor 
{
    public FullRecordAsXMLValueExtractor() 
    {
        super();
    }

    @Override
    protected MarcWriter makeNewWriter(ByteArrayOutputStream outputStream)
    {
        return new MarcXmlWriter(outputStream, "UTF-8");
    }

}
