package org.solrmarc.index.specification;

import java.util.Collection;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.specification.conditional.Condition;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.VariableField;


public class SingleControlFieldSpecification extends SingleSpecification
{
    public SingleControlFieldSpecification(String tag, Condition cond)
    {
        super(tag, cond);
    }

    public SingleControlFieldSpecification(String tag)
    {
        this(tag, null);
    }

    private SingleControlFieldSpecification(SingleControlFieldSpecification toClone)
    {
        super(toClone);
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
    public void addFieldValues(Collection<String> result, VariableField vf) throws Exception
    {
        addControlFieldValues(result, vf, fmt);
    }
    
    public static void addControlFieldValues(Collection<String> result, VariableField vf, FieldFormatter fmt) throws Exception
    {
        final String data;
        data = ((ControlField) vf).getData();
        StringBuilder sb = fmt.start();
        fmt.addTag(sb, vf);
        Collection<String> prepped = fmt.prepData(vf, false, data);
        for (String val : prepped)
        {
            val = fmt.handleSubFieldFormat(" ", vf, val);
            fmt.addVal(sb, null, val);
            fmt.addAfterSubfield(sb, result);
        }
        fmt.addAfterField(sb, result);
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new SingleControlFieldSpecification(this);
    }
}
