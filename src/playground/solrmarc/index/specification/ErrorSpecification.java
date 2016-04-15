package playground.solrmarc.index.specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.fieldmatch.FieldFormatter;
import playground.solrmarc.index.fieldmatch.FieldMatch;
import playground.solrmarc.index.specification.conditional.Condition;

public class ErrorSpecification extends SingleSpecification
{
	List<String> errorMessages;
	
	public ErrorSpecification(String message)
	{
		super(null, null);
		errorMessages = Collections.singletonList(message);
	}
	
	public ErrorSpecification(List<String> messages)
	{
		super(null, null);
		errorMessages = messages;		
	}
	
	@Override
	public void addConditional(Condition cond) {}

	@Override
	public String[] getTags() {return null;}

	@Override
	protected SingleSpecification getMatchingSpec(String tag, VariableField f) {return null;}

	@Override
    public List<FieldMatch> getFieldMatches(Record record)
    {
        List<FieldMatch> result = new ArrayList<FieldMatch>(1);
        result.add(new FieldMatch(null, this));
        return(result);    
    }

	@Override
	public void addFieldValues(Collection<String> result, VariableField vf)
	{
		if (result.size() == 0)
		{
//			result.add("spec is "+ super.specLabel);
			result.addAll(errorMessages);
		}
	}

	@Override
	public void setFormatter(FieldFormatter fmt) {}

}
