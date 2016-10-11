package playground.solrmarc.index.specification;

import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.specification.conditional.Condition;

public class SingleLinkedDataFieldSpecification extends SingleDataFieldSpecification
{    
    public SingleLinkedDataFieldSpecification(String tag, String subfields, Condition cond)
    {
        super(tag, subfields, cond);
        tags[0] = "880";
    }
        
    public SingleLinkedDataFieldSpecification(String tag, String subfields)
    {
        this(tag, subfields, null);
    }
    
    @Override
    protected SingleLinkedDataFieldSpecification getMatchingSpec(String tag, VariableField f)
    {
        String stag = this.tag;
        if (tag.equals("880") && ((DataField)f).getSubfield('6') != null && 
            ((DataField)f).getSubfield('6').getData().startsWith(stag))
        {
            return(this);
        }
        return null;
    }
}
