package playground.solrmarc.index.fieldmatch;

import org.marc4j.marc.VariableField;


public class FieldFormatterSubstring extends FieldFormatterDecorator
{
    public final static String KEYWORD = "substring";
    private final int startOffset;
    private final int endOffset;
    
    public FieldFormatterSubstring(int startOffset, int endOffset)
    {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public FieldFormatterSubstring(int startOffset)
    {
        this(startOffset, startOffset);
    }
    
    public FieldFormatterSubstring(String startStr, String endStr)
    {
        this.startOffset = Integer.parseInt(startStr);
        this.endOffset = Integer.parseInt(endStr);;
    }

    public FieldFormatterSubstring(String startStr)
    {
        this(startStr, startStr);
    }

    @Override
    public String cleanData(VariableField vf, boolean isSubfieldA, String data)
    {
        try
        {
            data = data.substring(startOffset, endOffset + 1);
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            data = "";
        }
        return super.cleanData(vf, isSubfieldA, data);
    }
}