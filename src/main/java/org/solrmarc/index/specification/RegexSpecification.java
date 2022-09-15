package org.solrmarc.index.specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.extractor.formatter.FieldFormatterBase;
import org.solrmarc.index.extractor.impl.direct.FieldMatch;
import org.solrmarc.index.specification.conditional.Condition;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.RecordImpl;
import org.marc4j.marc.VariableField;

import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public class RegexSpecification extends Specification
{
    Pattern regex = null;
    Condition cond = null;
    boolean duplicateTags = false;
    protected FieldFormatter fmt;


    public RegexSpecification(String regexStr)
    {
       this.regex = Pattern.compile(regexStr);
       this.cond = null;
       fmt = makeRawFieldFormattter();
    }
    
    public RegexSpecification(String regexStr, Condition cond)
    {
       this.regex = Pattern.compile(regexStr);;
       this.cond = cond;
       fmt = makeRawFieldFormattter();
    }

    private final static String[] defaultSubFieldCodeFmt = { "*=> $%sf %d", " => %d"};
    
    private FieldFormatter makeRawFieldFormattter()
    {
        FieldFormatter f = new FieldFormatterBase(false);
        f.setIndicatorFmt("%1%2");
        f.setFieldTagFmt("%tag ");
        f.setSfCodeFmt(defaultSubFieldCodeFmt);
        f.setJoinVal(FieldFormatter.eJoinVal.JOIN);
        return f;
    }

    private RegexSpecification(RegexSpecification toClone)
    {
        this.regex = toClone.regex;
        this.cond = toClone.cond;
        this.duplicateTags = toClone.duplicateTags;
        if (toClone.fmt.isThreadSafe())
            this.fmt = toClone.fmt;
        else
            this.fmt = (FieldFormatter)toClone.fmt.makeThreadSafeCopy();
    }

    @Override
    public boolean hasDuplicateTags()
    {
        return duplicateTags;
    }

    @Override
    public List<FieldMatch> getFieldMatches(Record record)
    {
        List<FieldMatch> result = null;
        result = new ArrayList<FieldMatch>();
        List<VariableField> fields = ((RecordImpl)record).getVariableFieldsWithLeader();
        for (VariableField vf : fields)
        {
            if (regex.matcher(vf.getTag()).matches() && (cond == null || cond.matches(record, vf)))
            {
                result.add(new FieldMatch(vf, this));
            }
        }
        return (result);
    }

    @Override
    public void addFieldValues(Collection<String> result, VariableField vf) throws Exception
    {
        if (vf instanceof ControlField)
        {
            SingleControlFieldSpecification.addControlFieldValues(result, vf, fmt);
        }
        else 
        {
            SingleDataFieldSpecification.addDataFieldValues(result, vf, fmt, dotPattern);
        }
    }

    @Override
    protected Specification getMatchingSpec(String tag, VariableField f)
    {
        return this;
    }


    @Override
    public Object makeThreadSafeCopy()
    {
        return new RegexSpecification(this);
    }

    @Override 
    public void addMap(AbstractMultiValueMapping valueMapping)
    {
        if (fmt != null) fmt.addMap(valueMapping);
    }

    @Override
    public void setFormatter(FieldFormatter fmt)
    {
        this.fmt = fmt;
    }

    public void addCleanVal(FieldFormatter.eCleanVal cleanVal)
    {
        fmt.addCleanVal(cleanVal);
    }

    public void setCleanVal(EnumSet<FieldFormatter.eCleanVal> of)
    {
        fmt.setCleanVal(of);
    }

    public void setJoinVal(FieldFormatter.eJoinVal joinVal)
    {
        fmt.setJoinVal(joinVal);
    }

    public void setSubstring(int offset, int endOffset)
    {
        fmt.setSubstring(offset, endOffset);
    }

    public void setSeparator(String separator)
    {
        fmt.setSeparator(separator);
    }

    public void setFormatPatterns(String[] mapParts)
    {
        fmt.setSfCodeFmt(mapParts);
    }

    @Override
    public boolean conditionalMatches(Record record, VariableField vf)
    {
        return  (cond == null || cond.matches(record, vf));
    }

    @Override
    public boolean isThreadSafe()
    {
        return true;
    }

    @Override
    public void addConditional(Condition cond)
    {
        if (this.cond == null) this.cond = cond;
    }

    @Override
    public String[] getTags()
    {
        return null;
    }


}
