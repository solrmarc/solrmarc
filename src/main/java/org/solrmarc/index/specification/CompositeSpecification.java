package org.solrmarc.index.specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.extractor.impl.direct.FieldMatch;
import org.solrmarc.index.specification.conditional.Condition;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import org.solrmarc.index.mapping.AbstractMultiValueMapping;

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

    private CompositeSpecification(CompositeSpecification toClone)
    {
        this.pieces = new ArrayList<SingleSpecification>(toClone.pieces.size());
        for (SingleSpecification spec : toClone.pieces)
        {
            this.pieces.add((SingleSpecification)((spec.isThreadSafe()) ? spec : spec.makeThreadSafeCopy()));
        }
        this.tagsUsed = toClone.tagsUsed;
        this.tags = toClone.tags;
        this.duplicateTags = toClone.duplicateTags;
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
            if (tagsUsed.contains(((SingleSpecification) spec).tags[0]))
            {
                duplicateTags = true;
            }
            else
            {
                tagsUsed.add(((SingleSpecification) spec).tags[0]);
            }
            pieces.add((SingleSpecification) spec);
        }
        else
        {
            if (spec.hasDuplicateTags())  this.duplicateTags = true;
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
            List<VariableField> fields = record.getVariableFields(getTags());
            for (SingleSpecification spec : pieces)
            {
                for (VariableField vf : fields)
                {
                    if (spec.specMatches(vf.getTag(), vf) && (spec.cond == null || spec.cond.matches(record, vf)))
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
    public void addMap(AbstractMultiValueMapping valueMapping)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.addMap(valueMapping);
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

    public void addCleanVal(FieldFormatter.eCleanVal cleanVal)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.addCleanVal(cleanVal);
        }
    }

    public void setCleanVal(EnumSet<FieldFormatter.eCleanVal> of)
    {
        for (SingleSpecification spec : pieces)
        {
            spec.setCleanVal(of);
        }
    }

    public void setJoinVal(FieldFormatter.eJoinVal joinVal)
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

    @Override
    public boolean isThreadSafe()
    {
        for (SingleSpecification spec : pieces)
        {
            if (!spec.isThreadSafe()) return(false);
        }
        return(true);
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new CompositeSpecification(this);
    }

    @Override
    public boolean conditionalMatches(Record record, VariableField vf)
    {
        throw new RuntimeException("invalid invocation of conditionMatches on CompositeSpecification");
    }

}
