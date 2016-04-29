package playground.solrmarc.index.fieldmatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.marc4j.marc.VariableField;

public class FieldFormatterDecorator extends FieldFormatter 
{
	final private FieldFormatter toDecorate;
	
	public FieldFormatterDecorator(FieldFormatter toDecorate)
	{
		this.toDecorate = toDecorate;
	}

	@Override
	public String getFieldTagFmt() 
	{
		return(toDecorate.getFieldTagFmt());
	}

	@Override
	public void setFieldTagFmt(String fieldTagFmt) 
	{
		toDecorate.setFieldTagFmt(fieldTagFmt);
	}

	@Override
	public String getIndicatorFmt() 
	{
		return(toDecorate.getIndicatorFmt());
	}

	@Override
	public void setIndicatorFmt(String indicatorFmt)
	{
		toDecorate.setIndicatorFmt(indicatorFmt);
	}

	@Override
	public String getSfCodeFmt()
	{
		return(toDecorate.getSfCodeFmt());
	}

	@Override
	public void setSfCodeFmt(String sfCodeFmt)
	{
		toDecorate.setSfCodeFmt(sfCodeFmt);
	}

	@Override
	public String getSeparator()
	{
		return(toDecorate.getSfCodeFmt());
	}

	@Override
	public void setSeparator(String separator)
	{
		toDecorate.setSeparator(separator);
	}

	/* (non-Javadoc)
	 * @see playground.solrmarc.index.fieldmatch.FieldFormatter#setSeparator(java.lang.String)
	 */
	@Override
	public void setUnique() 
	{
		toDecorate.setUnique();
	}

	@Override
	public void unsetUnique() 
	{
		toDecorate.unsetUnique();
	}
	
	/* (non-Javadoc)
	 * @see playground.solrmarc.index.fieldmatch.FieldFormatter#getSeparator()
	 */
	@Override
	public boolean isUnique() 
	{
		return toDecorate.isUnique();
	}
	
	@Override
	public EnumSet<eCleanVal> getCleanVal()
	{
		return toDecorate.getCleanVal();
	}

	@Override
	public void setCleanVal(EnumSet<eCleanVal> cleanVal)
	{
		toDecorate.setCleanVal(cleanVal);
	}


	@Override
	public void addCleanVal(eCleanVal cleanVal)
	{
		toDecorate.addCleanVal(cleanVal);
	}

	@Override
	public Collection<String> start()
	{
		return(toDecorate.start());
	}

	@Override
	public void addTag(VariableField vf)
	{
		toDecorate.addTag(vf);
	}

	@Override
	public void addIndicators(VariableField vf)
	{
		toDecorate.addIndicators(vf);
	}

	@Override
	public void addCode(String codeStr)
	{
		toDecorate.addCode(codeStr);
	}

//    @Override
//	public Collection<String> prepData(VariableField vf, boolean isSubfieldA, String data)
//    {
//    	final String cleaned = cleanData(vf, isSubfieldA, data);
//    	final List<String> cleanedDataAsList = (cleaned == null || cleaned.length() == 0) ? Collections.emptyList() : Collections.singletonList(cleaned);
//    	Collection<String> result = handleMapping(cleanedDataAsList);
//    	return(result);
//    }

	@Override
	public void addVal(String data)
	{
		toDecorate.addVal(data);
	}

	@Override
	public void addSeparator(int cnt)
	{
		toDecorate.addSeparator(cnt);
	}

	@Override
	public void addAfterSubfield(Collection<String> result)
	{
		toDecorate.addAfterSubfield(result);		
	}

	@Override
	public void addAfterField(Collection<String> result)
	{
		toDecorate.addAfterField(result);		
	}

	@Override
	public String cleanData(VariableField vf, boolean isSubfieldA, String data)
	{
		return(toDecorate.cleanData(vf, isSubfieldA, data));
	}

	@Override
	public Collection<String> handleMapping(Collection<String> cleaned) throws Exception
	{
		return(toDecorate.handleMapping(cleaned));
	}

	@Override
	public Collection<String> makeResult()
	{
		return(toDecorate.makeResult());
	}
}
