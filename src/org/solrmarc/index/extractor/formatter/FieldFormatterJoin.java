package org.solrmarc.index.extractor.formatter;

import java.util.Collection;

import org.solrmarc.tools.DataUtil;


//import playground.solrmarc.tools.Utils;

public class FieldFormatterJoin extends FieldFormatterDecorator
{
    public final static String KEYWORD = "join";
    private final String joinSeparator;
    
    public FieldFormatterJoin(FieldFormatter toDecorate)
    {
        super(toDecorate);
        joinSeparator = null;
    }

    public FieldFormatterJoin(FieldFormatter toDecorate, String separator)
    {
        super(toDecorate);
        joinSeparator = separator;
        toDecorate.setSeparator(separator);
    }

    public FieldFormatterJoin()
    {
        joinSeparator = null;
    }

    public FieldFormatterJoin(String separator)
    {
        joinSeparator = separator;
    }
    
    @Override
    public void decorate(FieldFormatter decorate)
    {
        super.decorate(decorate);
        super.setSeparator(joinSeparator);
    }

    @Override
    public void addAfterSubfield(Collection<String> result)
    {
    }

    @Override
    public void addAfterField(Collection<String> result)
    {
        if (buffer.length() == 0) return;
        final String field = (this.getCleanVal().contains(eCleanVal.CLEAN_END)) ? DataUtil.cleanData(buffer.toString()) : buffer.toString();
        if (field.length() > 0) result.add(field);
        buffer.setLength(0);
    }
}
