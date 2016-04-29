package playground.solrmarc.index.fieldmatch;

import java.util.Collection;

import playground.solrmarc.index.mapping.AbstractMultiValueMapping;

public class FieldFormatterMapped extends FieldFormatterDecorator
{
    AbstractMultiValueMapping map;

    public FieldFormatterMapped(FieldFormatter toDecorate)
    {
        super(toDecorate);
        this.map = null;
    }

    public FieldFormatterMapped(FieldFormatter toDecorate, AbstractMultiValueMapping map)
    {
        super(toDecorate);
        this.map = map;
    }

    @Override
    public Collection<String> handleMapping(Collection<String> cleaned) throws Exception
    {
        Collection<String> result = super.handleMapping(cleaned);
        result = map.map(result);
        return (result);
    }

}
