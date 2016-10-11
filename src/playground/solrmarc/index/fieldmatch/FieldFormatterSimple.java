package playground.solrmarc.index.fieldmatch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.marc4j.marc.VariableField;

import playground.solrmarc.tools.Utils;

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
    public void addAfterField(Collection<String> result) {}

}
