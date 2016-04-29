package playground.solrmarc.index.fieldmatch;

import java.util.Collection;

public final class FieldFormatterSimple extends FieldFormatterDecorator
{
    public FieldFormatterSimple(FieldFormatter toDecorate)
    {
        super(toDecorate);
    }

    @Override
    public void addAfterSubfield(Collection<String> result)
    {
        result.add(buffer.toString());
        buffer.setLength(0);
    }

    @Override
    public void addAfterField(Collection<String> result)
    {
    }

}
