package playground.solrmarc.index.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.marc4j.marc.VariableField;

import playground.solrmarc.index.fieldmatch.FieldMatch;
import playground.solrmarc.index.specification.SingleSpecification;

public class MultiValueFieldMatchCollector implements AbstractValueCollector<Collection<FieldMatch>> 
{

    @Override
    public Object collect(Collection<FieldMatch> values)
    {
        List<String> result = new ArrayList<String>();
        for (FieldMatch fm : values)
        {
            SingleSpecification spec = fm.getSpec();
            VariableField vf = fm.getVf();
            spec.addFieldValues(result, vf);
        }
        return result;
    }

}
