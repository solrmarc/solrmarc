package org.solrmarc.index.specification;

import java.util.Collection;
import java.util.EnumSet;

import org.marc4j.marc.VariableField;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.extractor.formatter.FieldFormatterBase;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.specification.conditional.Condition;


public abstract class SingleSpecification extends Specification
{
 //   protected static FieldFormatter SINGLE_FMT = new FieldFormatterBase(true);
    protected String tag;
    Condition cond = null;
    protected FieldFormatter fmt;
    // protected FieldMatchCollector coll;
    String[] tags;

    public SingleSpecification(String tag, Condition cond)
    {
        this.tag = tag;
        this.cond = cond;
        tags = new String[1];
        tags[0] = tag;
        fmt = new FieldFormatterBase(true);
    }

    protected SingleSpecification(SingleSpecification toClone)
    {
        this.tag = toClone.tag;
        this.cond = toClone.cond;
        this.tags = new String[1];
        tags[0] = toClone.tag;
        if (toClone.fmt.isThreadSafe())
            this.fmt = toClone.fmt;
        else
            this.fmt = (FieldFormatter)toClone.fmt.makeThreadSafeCopy();
    }

    @Override
    public void addConditional(Condition cond)
    {
        if (this.cond == null) this.cond = cond;
    }

    public String getTag()
    {
        return tag;
    }

    @Override
    public String[] getTags()
    {
        return tags;
    }

    @Override
    protected SingleSpecification getMatchingSpec(final String tag, final VariableField f)
    {
        if (specMatches(tag, f))
        {
            return (this);
        }
        return null;
    }

    protected boolean specMatches(String tag, VariableField f)
    {
        String stag = this.tag;
        return(tag.equals(stag));        
    }

    abstract public void addFieldValues(Collection<String> result, VariableField vf) throws Exception;

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
    
    public void addCleanVal(eCleanVal cleanVal)
    {
        fmt.addCleanVal(cleanVal);
    }

    public void setCleanVal(EnumSet<eCleanVal> of)
    {
        fmt.setCleanVal(of);
    }

    public void setJoinVal(eJoinVal joinVal)
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
    public boolean isThreadSafe()
    {
        return(fmt.isThreadSafe());
    }
}
