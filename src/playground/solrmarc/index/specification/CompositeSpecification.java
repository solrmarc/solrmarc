package playground.solrmarc.index.specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.extractor.formatter.FieldFormatter;
import playground.solrmarc.index.extractor.formatter.FieldFormatterDecorator;
import playground.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import playground.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import playground.solrmarc.index.extractor.impl.direct.FieldMatch;
import playground.solrmarc.index.specification.conditional.Condition;

public class CompositeSpecification extends Specification
{
    List<SingleSpecification> pieces = null;
    List<String> tagsUsed = null;
    String tags[] = null;
    boolean duplicateTags = false;

    public CompositeSpecification()
    {

    }

    public CompositeSpecification(Specification spec)
    {
        addSpec(spec);
    }

    @Override
    public boolean hasDuplicateTags()
    {
        return duplicateTags;
    }

    public void addSpec(Specification spec)
    {
        if (pieces == null) pieces = new ArrayList<SingleSpecification>();
        if (tagsUsed == null) tagsUsed = new ArrayList<String>();

        if (spec instanceof SingleSpecification)
        {
            if (tagsUsed.contains(((SingleSpecification) spec).tag))
            {
                duplicateTags = true;
            }
            else
            {
                tagsUsed.add(((SingleSpecification) spec).tag);
            }
            pieces.add((SingleSpecification) spec);
        }
        else
        {
            for (String t : ((CompositeSpecification) spec).tagsUsed)
            {
                if (tagsUsed.contains(t))
                {
                    duplicateTags = true;
                }
                else
                {
                    tagsUsed.add(t);
                }
            }
            pieces.addAll(((CompositeSpecification) spec).pieces);
        }

    }

    public void addConditional(Condition cond)
    {
        for (Specification spec : pieces)
        {
            spec.addConditional(cond);
        }
    }

    @Override
    public List<FieldMatch> getFieldMatches(Record record)
    {
        List<FieldMatch> result = null;
        if (!hasDuplicateTags())
        {
            return (super.getFieldMatches(record));
        }
        else
        {
            result = new ArrayList<FieldMatch>();
            for (SingleSpecification spec : pieces)
            {
                List<VariableField> fields = record.getVariableFields(spec.tag);
                for (VariableField vf : fields)
                {
                    if (spec.cond == null || spec.cond.matches(record, vf))
                    {
                        result.add(new FieldMatch(vf, spec));
                    }
                }
            }
            return (result);
        }
    }

    public String[] getTags()
    {
        if (tags == null) tags = tagsUsed.toArray(new String[tagsUsed.size()]);
        return (tags);
    }

    @Override
    protected SingleSpecification getMatchingSpec(String tag, VariableField f)
    {
        for (SingleSpecification spec : pieces)
        {
            SingleSpecification thatMatches = spec.getMatchingSpec(tag, f);
            if (thatMatches != null) return (thatMatches);
        }
        return null;
    }

    @Override
    public void addFieldValues(Collection<String> result, VariableField vf) throws Exception
    {
        for (SingleSpecification spec : pieces)
        {
            spec.addFieldValues(result, vf);
        }
    }

    @Override
    public void addFormatter(FieldFormatterDecorator fmt)
    {
        for (SingleSpecification spec : pieces)
        {
            try
            {
                spec.addFormatter(fmt.clone());
            }
            catch (CloneNotSupportedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void setFormatter(FieldFormatter fmt)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.setFormatter(fmt);
        }
    }

    public void addCleanVal(eCleanVal cleanVal)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.addCleanVal(cleanVal);
        }
    }

    public void setCleanVal(EnumSet<eCleanVal> of)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.setCleanVal(of);
        }
    }
    
    public void setJoinVal(eJoinVal joinVal)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.setJoinVal(joinVal);
        }
    }
    
    public void setSubstring(int offset, int endOffset)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.setSubstring(offset, endOffset);
        }
    }
    
    public void setSeparator(String separator)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.setSeparator(separator);
        }
    }
   
    public void setFormatPatterns(final String[] mapParts)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.setFormatPatterns( mapParts);
        }
    }

}
