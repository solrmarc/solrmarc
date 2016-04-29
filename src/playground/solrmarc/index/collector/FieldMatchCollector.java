package playground.solrmarc.index.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import playground.solrmarc.index.fieldmatch.FieldMatch;

public class FieldMatchCollector implements AbstractValueCollector<Collection<FieldMatch>> 
{
	public static final String UNIQUE = "unique";
	boolean unique = false;
	
	public FieldMatchCollector(boolean unique) 
	{
		this.unique = unique;
	}

	public FieldMatchCollector() 
	{
		unique = false;
	}

	public boolean isUnique() 
	{
		return unique;
	}

	public void setUnique(boolean unique) 
	{
		this.unique = unique;
	}

	public Collection<String> makeCollection()
	{
		Collection<String> results;
		if (unique)
		{
			results = new LinkedHashSet<String>();
		}
		else
		{
			results = new ArrayList<String>();
		}
		return(results);
	}
	
	@Override
	public Collection<String> collect(Collection<FieldMatch> values) throws Exception 
	{
		Collection<String> results = makeCollection();
		for (FieldMatch fm : values)
		{
			fm.addValuesTo(results);
		}
		return results;
	}

}
