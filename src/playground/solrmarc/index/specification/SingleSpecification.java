package playground.solrmarc.index.specification;

import java.util.Collection;
import java.util.EnumSet;

import org.marc4j.marc.VariableField;

import playground.solrmarc.index.fieldmatch.FieldFormatter;
import playground.solrmarc.index.fieldmatch.FieldFormatterBase;
import playground.solrmarc.index.fieldmatch.FieldFormatterDecorator;
import playground.solrmarc.index.fieldmatch.FieldFormatter.eCleanVal;
import playground.solrmarc.index.specification.conditional.Condition;

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
    protected SingleSpecification getMatchingSpec(String tag, VariableField f)
    {
        String stag = this.tag;
        if (tag.equals(stag))
        {
            return (this);
        }
        return null;
    }

    abstract public void addFieldValues(Collection<String> result, VariableField vf) throws Exception;

    @Override 
    public void addFormatter(FieldFormatterDecorator newFmt)
    {
        newFmt.decorate(this.fmt);
        this.fmt = newFmt;
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



}
