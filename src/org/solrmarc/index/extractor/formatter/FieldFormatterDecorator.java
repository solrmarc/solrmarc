package org.solrmarc.index.extractor.formatter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.marc4j.marc.VariableField;

public class FieldFormatterDecorator implements FieldFormatter, Cloneable
{
    private FieldFormatter toDecorate;

    public FieldFormatterDecorator(FieldFormatter decorate)
    {
        toDecorate = decorate;
    }
    
    public FieldFormatterDecorator()
    {
    }

    public void decorate(FieldFormatter decorate)
    {
        toDecorate = decorate;
    }
    
    public FieldFormatterDecorator clone() throws CloneNotSupportedException
    {
        return (FieldFormatterDecorator)super.clone();
    }
    
    @Override
    public String getFieldTagFmt()
    {
        return (toDecorate.getFieldTagFmt());
    }

    @Override
    public FieldFormatter setFieldTagFmt(String fieldTagFmt)
    {
        toDecorate.setFieldTagFmt(fieldTagFmt);
        return(this);
    }

    @Override
    public String getIndicatorFmt()
    {
        return (toDecorate.getIndicatorFmt());
    }

    @Override
    public FieldFormatter setIndicatorFmt(String indicatorFmt)
    {
        toDecorate.setIndicatorFmt(indicatorFmt);
        return(this);
    }

    @Override
    public String getSfCodeFmt(char sfcode)
    {
        return (toDecorate.getSfCodeFmt(sfcode));
    }

    @Override
    public FieldFormatter setSfCodeFmt(String[] sfCodeFmt)
    {
        toDecorate.setSfCodeFmt(sfCodeFmt);
        return(this);
    }

    @Override
    public String getSeparator()
    {
        return (toDecorate.getSeparator());
    }

    @Override
    public FieldFormatter setSeparator(String separator)
    {
        toDecorate.setSeparator(separator);
        return(this);
    }

    @Override
    public eJoinVal getJoinVal()
    {
        return (toDecorate.getJoinVal());
    }

    @Override
    public FieldFormatter setJoinVal(eJoinVal joinVal)
    {
        toDecorate.setJoinVal(joinVal);
        return(this);
    }

    @Override
    public FieldFormatter setSubstring(int offset, int endOffset)
    {
        return(toDecorate.setSubstring(offset, endOffset));
    }


    @Override
    public EnumSet<eCleanVal> getCleanVal()
    {
        return toDecorate.getCleanVal();
    }

    @Override
    public FieldFormatter setCleanVal(EnumSet<eCleanVal> cleanVal)
    {
        toDecorate.setCleanVal(cleanVal);
        return(this);
    }

    @Override
    public FieldFormatter addCleanVal(eCleanVal cleanVal)
    {
        toDecorate.addCleanVal(cleanVal);
        return(this);
    }

    @Override
    public Collection<String> start()
    {
        return (toDecorate.start());
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
    
    // @Override
    // public Collection<String> prepData(VariableField vf, boolean isSubfieldA,
    // String data)
    // {
    // final String cleaned = cleanData(vf, isSubfieldA, data);
    // final List<String> cleanedDataAsList = (cleaned == null ||
    // cleaned.length() == 0) ? Collections.emptyList() :
    // Collections.singletonList(cleaned);
    // Collection<String> result = handleMapping(cleanedDataAsList);
    // return(result);
    // }

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
        return (toDecorate.cleanData(vf, isSubfieldA, data));
    }

    @Override
    public Collection<String> handleMapping(Collection<String> cleaned) throws Exception
    {
        return (toDecorate.handleMapping(cleaned));
    }

    @Override
    public Collection<String> makeResult()
    {
        return (toDecorate.makeResult());
    }

    @Override
    public Collection<String> prepData(VariableField vf, boolean isSubfieldA, String data) throws Exception
    {
        final String cleaned = cleanData(vf, isSubfieldA, data);
        final List<String> cleanedDataAsList = (cleaned == null || cleaned.length() == 0) ? emptyList
                : Collections.singletonList(cleaned);
        Collection<String> result = handleMapping(cleanedDataAsList);
        return (result);
    }

    @Override
    public String handleSubFieldFormat(String sfCode, String mappedDataVal)
    {
        return (toDecorate.handleSubFieldFormat(sfCode, mappedDataVal));
    }

}
