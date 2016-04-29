package playground.solrmarc.index.specification.conditional;

import java.util.List;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

public abstract class Condition
{
    protected final String fieldTag;

    public Condition()
    {
        this(null);
    }

    public Condition(String fieldTag)
    {
        this.fieldTag = fieldTag;
    }

    public boolean matches(final Record r, final VariableField f)
    {
        if (fieldTag != null)
        {
            List<VariableField> vfs = r.getVariableFields(fieldTag);
            boolean result = false;
            for (VariableField vf : vfs)
            {
                result |= matches(vf);
            }
            return (result);
        }
        else
        {
            return (matches(f));
        }
    }

    public boolean matches(final VariableField f)
    {
        return (false);
    }

}