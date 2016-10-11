package playground.solrmarc.index.specification.conditional;

import org.marc4j.marc.VariableField;

public abstract class Condition
{
    public Condition()
    {
        super();
    }

    public abstract boolean matches(VariableField f);
}