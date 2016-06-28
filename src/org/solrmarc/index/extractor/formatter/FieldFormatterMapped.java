package org.solrmarc.index.extractor.formatter;

import java.util.Collection;

import org.solrmarc.index.mapping.AbstractMultiValueMapping;


public class FieldFormatterMapped extends FieldFormatterDecorator
{
    final AbstractMultiValueMapping map;


    public FieldFormatterMapped(FieldFormatter toDecorator, AbstractMultiValueMapping map)
    {
        super(toDecorator); 
        this.map = map;
    }
    
    public FieldFormatterMapped(AbstractMultiValueMapping map)
    {
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
