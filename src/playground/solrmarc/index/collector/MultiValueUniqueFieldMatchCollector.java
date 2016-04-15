package playground.solrmarc.index.collector;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.marc4j.marc.VariableField;

import playground.solrmarc.index.fieldmatch.FieldMatch;
import playground.solrmarc.index.specification.SingleSpecification;

public class MultiValueUniqueFieldMatchCollector extends MultiValueFieldMatchCollector 
{
    public final static String KEYWORD = "unique";

    @Override
    public Object collect(Collection<FieldMatch> values)
    {
        Set<String> result = new LinkedHashSet<String>();
        for (FieldMatch fm : values)
        {
            SingleSpecification spec = fm.getSpec();
            VariableField vf = fm.getVf();
            /* Collection<String> = */spec.addFieldValues(result, vf);
        }
        return result;
    }

}
