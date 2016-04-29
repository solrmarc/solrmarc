package playground.solrmarc.index.fieldmatch;

import java.util.Collection;

import playground.solrmarc.tools.Utils;

public class FieldFormatterJoin extends FieldFormatterDecorator
{
    public final static String KEYWORD = "join";

    public FieldFormatterJoin(FieldFormatter toDecorate)
    {
        super(toDecorate);
    }

    public FieldFormatterJoin(FieldFormatter toDecorate, String separator)
    {
        super(toDecorate);
        setSeparator(separator);
    }

    @Override
    public void addAfterSubfield(Collection<String> result)
    {
    }

    @Override
    public void addAfterField(Collection<String> result)
    {
        if (buffer.length() == 0) return;
        final String field = (this.getCleanVal().contains(eCleanVal.CLEAN_END)) ? Utils.cleanData(buffer.toString())
                : buffer.toString();
        if (field.length() > 0) result.add(field);
        buffer.setLength(0);
    }
}
