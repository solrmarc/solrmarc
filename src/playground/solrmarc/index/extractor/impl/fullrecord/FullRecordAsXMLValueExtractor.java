package playground.solrmarc.index.extractor.impl.fullrecord;

import org.marc4j.Constants;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.xml.sax.helpers.AttributesImpl;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.stream.StreamResult;

public class FullRecordAsXMLValueExtractor extends AbstractFullRecordValueExtractor
{
    private final static String spaces = "                                          ";
    private final StringBuilder sb = new StringBuilder();

    public FullRecordAsXMLValueExtractor()
    {
        super();
    }

    @Override
    public String extract(final Record record) throws UnsupportedEncodingException
    {
        sb.setLength(0);
//        writer = makeNewWriter(new StreamResult(strWriter));
//        writer.write(record);
//        writer.close();
//        writer = null;
        return toXMLString(record, false);
    }

    protected static final String CONTROL_FIELD = "controlfield";
    protected static final String DATA_FIELD = "datafield";
    protected static final String SUBFIELD = "subfield";
    protected static final String COLLECTION = "collection";
    protected static final String RECORD = "record";
    protected static final String LEADER = "leader";

    private class Tag 
    {
        final String name;
        final String value;
        public Tag(String name, String value)
        {
            this.name = name; 
            this.value = value;
        }
    }

    public String toXMLString(Record record, boolean indent) 
    {
        startDocument(sb);
        startElement(sb, COLLECTION, indent, 0, new Tag("xmlns","http://www.loc.gov/MARC21/slim"));
        startElement(sb, RECORD, indent, 2);
        if (indent)  indent(sb, 2);
        startElement(sb, LEADER, indent, 4);
        sb.append(record.getLeader().toString());
        endElement(sb, LEADER);

        for (ControlField field : record.getControlFields())
        {
            startElement(sb, CONTROL_FIELD, indent, 4, new Tag("tag", field.getTag()));
            sb.append(getDataElement(field.getData()));
            endElement(sb, CONTROL_FIELD);
        }

        for (DataField field : record.getDataFields())
        {
            startElement(sb, DATA_FIELD, indent, 4, new Tag("tag", field.getTag()), 
                                         new Tag("ind1", String.valueOf(field.getIndicator1())),
                                         new Tag("ind2", String.valueOf(field.getIndicator2())));
            for (Subfield subfield : field.getSubfields())
            {
                startElement(sb, SUBFIELD, indent, 6, new Tag("code", ""+subfield.getCode()));
                sb.append(getDataElement(subfield.getData()));
                endElement(sb, SUBFIELD);
            }
            endElement(sb, DATA_FIELD, indent, 4);
        }
        endElement(sb, RECORD);
        endElement(sb, COLLECTION);
        return(sb.toString());
    }

    
    private void indent(StringBuilder sb, int i)
    {
        // TODO Auto-generated method stub
        
    }

    private void startDocument(StringBuilder sb)
    {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");        
    }

    private String getDataElement(String data)
    {
        return data.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void startElement(StringBuilder sb, String qname, boolean indent, int indentCnt, Tag ... tags )
    {
        if (indent) sb.append("\n").append(spaces.substring(0, indentCnt));
        sb.append("<").append(qname);
        for (Tag tag : tags)
        {
            sb.append(" ").append(tag.name).append("=\"").append(tag.value).append("\"");
        }
        sb.append(">");      
    }
    
    private void endElement(StringBuilder sb, String qname, boolean indent, int indentCnt)
    {
        sb.append("</").append(qname).append(">");      
    }

    private void endElement(StringBuilder sb, String qname)
    {
        endElement(sb, qname, false, 0);     
    }

    @Override
    protected MarcWriter makeNewWriter(ByteArrayOutputStream outputStream)
    {
        return new MarcXmlWriter(outputStream, "UTF8");
    }

    protected MarcWriter makeNewWriter(StreamResult streamResult)
    {
        return new MarcXmlWriter(streamResult);
    }

}
