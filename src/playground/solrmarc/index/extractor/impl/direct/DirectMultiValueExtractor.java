package playground.solrmarc.index.extractor.impl.direct;

import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import playground.solrmarc.index.fieldmatch.FieldFormatter;
import playground.solrmarc.index.fieldmatch.FieldFormatterBase;
import playground.solrmarc.index.fieldmatch.FieldMatch;
import playground.solrmarc.index.specification.AbstractSpecificationFactory;
import playground.solrmarc.index.specification.Specification;

import org.marc4j.marc.Record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

public class DirectMultiValueExtractor implements AbstractMultiValueExtractor
{
	public static final String UNIQUE = "unique";
	public static final String FIRST = "first";
	private FieldFormatter fmt;
    private Specification fieldsAndSubfieldSpec;	
	
	public Specification getFieldsAndSubfieldSpec()
	{
		return fieldsAndSubfieldSpec;
	}

	public void setFieldsAndSubfieldSpec(Specification fieldsAndSubfieldSpec)
	{
		this.fieldsAndSubfieldSpec = fieldsAndSubfieldSpec;
	}

	public FieldFormatter getFormatter()
	{
		return fmt;
	}

	public void setFormatter(FieldFormatter fmt)
	{
		this.fmt = fmt;
		if (fieldsAndSubfieldSpec != null)
			fieldsAndSubfieldSpec.setFormatter(fmt);
	}

	boolean unique = false;
	boolean firstOnly = false;
	
	public boolean isUnique() 
	{
		return unique;
	}

	public void setUnique(boolean unique) 
	{
		this.unique = unique;
	}

	public boolean firstOnly()
	{
		return(firstOnly);
	}
	
	public void setFirstOnly(boolean firstOnly) 
	{
		this.firstOnly = firstOnly;
	}

    public DirectMultiValueExtractor(final String fieldsAndSubfields) 
    {
        fieldsAndSubfieldSpec = AbstractSpecificationFactory.createSpecification(fieldsAndSubfields);
        fmt = new FieldFormatterBase(false);
    }
    
    public DirectMultiValueExtractor(Specification fieldSpec) 
    {
        fieldsAndSubfieldSpec = fieldSpec;
        fmt = new FieldFormatterBase(false);
    }

    public final Collection<FieldMatch> getFieldMatches(final Record record) 
    {
        Collection<FieldMatch> result = fieldsAndSubfieldSpec.getFieldMatches(record);
        return result;
    }

    @Override
    public Collection<String> extract(final Record record) throws Exception
    {
        Collection<String> result = makeEmptyResult();
        extract(result, record);
        return(result);
    }

    public void extract(Collection<String> result, final Record record) throws Exception
    {
        for (FieldMatch fm : getFieldMatches(record))
        {
            fm.addValuesTo(result);
        }
    }

    private Collection<String> makeEmptyResult()
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

}
