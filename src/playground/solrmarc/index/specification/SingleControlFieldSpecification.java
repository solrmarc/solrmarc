package playground.solrmarc.index.specification;

import java.util.Collection;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.specification.conditional.Condition;

public class SingleControlFieldSpecification extends SingleSpecification
{
//    int start;
//    int end;

//    public SingleControlFieldSpecification(String tag, int start, int end, Condition cond)
//    {
//        super(tag, cond);
//        this.start = start;
//        this.end = end;
//    }
//
//    public SingleControlFieldSpecification(String tag, int start, Condition cond)
//    {
//        this(tag, start, start + 1, cond);
//    }

    public SingleControlFieldSpecification(String tag, Condition cond)
    {
        super(tag, cond);
    }

//    public SingleControlFieldSpecification(String tag, int start, int end)
//    {
//        this(tag, start, end, null);
//    }
//
//    public SingleControlFieldSpecification(String tag, int start)
//    {
//        this(tag, start, start + 1, null);
//    }
//
    public SingleControlFieldSpecification(String tag)
    {
        this(tag, null);
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
        final String data;
//        if (start == -1 && end == -1)
//        {
            data = ((ControlField) vf).getData();
//        }
//        else
//        {
//            data = ((ControlField) vf).getData().substring(start, end + 1);
//        }
        fmt.start();
        fmt.addTag(vf);
        Collection<String> prepped = fmt.prepData(vf, false, data);
        for (String val : prepped)
        {
            fmt.addVal(val);
            fmt.addAfterSubfield(result);
        }
        fmt.addAfterField(result);
    }
}
