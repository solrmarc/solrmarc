package org.solrmarc.index.specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.extractor.impl.direct.FieldMatch;
import org.solrmarc.index.specification.conditional.Condition;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;


public abstract class Specification implements ExternalMethod
{
    abstract public void addConditional(Condition cond);
    abstract public boolean conditionalMatches(Record record, VariableField vf);

    abstract public String[] getTags();

    String specLabel;
    public final static Pattern dotPattern = Pattern.compile(".");

    public List<FieldMatch> getFieldMatches(Record record)
    {
        final String tags[] = getTags();
        List<VariableField> fields = record.getVariableFields(tags);
        List<FieldMatch> result = new ArrayList<FieldMatch>(fields.size());
        for (VariableField vf : fields)
        {
            Specification specUsed = this.getMatchingSpec(vf.getTag(), vf);
            if (specUsed != null && (specUsed.conditionalMatches(record, vf)))
            {
                result.add(new FieldMatch(vf, specUsed));
            }
        }
        return (result);
    }

    public boolean hasDuplicateTags()
    {
        return (false);
    }

    abstract protected Specification getMatchingSpec(String tag, VariableField f);

    abstract public void addFieldValues(Collection<String> result, VariableField vf) throws Exception;

    public String getSpecLabel()
    {
        return specLabel;
    }

    public void setSpecLabel(String specLabel)
    {
        this.specLabel = specLabel;
    }

    public abstract void setFormatter(FieldFormatter fmt);

    public abstract void addCleanVal(FieldFormatter.eCleanVal cleanVal);

    public abstract void setCleanVal(EnumSet<FieldFormatter.eCleanVal> of);

    public abstract void setJoinVal(FieldFormatter.eJoinVal cleanVal);

    public abstract void setSubstring(int offset, int endOffset);

    public abstract void setSeparator(String string);

    public abstract void setFormatPatterns(String[] mapParts);

    public abstract void addMap(AbstractMultiValueMapping valueMapping);
}
