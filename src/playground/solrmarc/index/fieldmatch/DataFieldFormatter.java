package playground.solrmarc.index.fieldmatch;

import java.util.Collection;

import org.marc4j.marc.DataField;

public abstract class DataFieldFormatter extends FieldFormatter
{
    public abstract void addTag(DataField df, StringBuilder buffer);
    public abstract void addIndicators(DataField df, StringBuilder buffer);
    public abstract void addCode(String codeStr, StringBuilder buffer);
    public abstract void addVal(String data, StringBuilder buffer);
    public abstract void addSeperator(int cnt, StringBuilder buffer);
    public abstract void addAfterSubfield(Collection<String> result, StringBuilder buffer);
    public abstract void addAtEnd(Collection<String> result, StringBuilder buffer);
}
