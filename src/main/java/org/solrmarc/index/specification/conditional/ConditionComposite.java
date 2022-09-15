package org.solrmarc.index.specification.conditional;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.indexer.FullSym;


public class ConditionComposite extends Condition
{
    final Condition o1;
    final Condition o2;
    int op;

    public ConditionComposite(Condition o1, Condition o2, int op)
    {
        this.o1 = o1;
        this.o2 = o2;
        this.op = op;
    }

    public ConditionComposite(Condition o1, int op)
    {
        this.o1 = o1;
        this.o2 = null;
        this.op = op;
    }

    @Override
    public boolean matches(final Record r, final VariableField f)
    {
        switch (op) {
            case FullSym.AND:
                return (o1.matches(r, f) && o2.matches(r, f));
            case FullSym.OR:
                return (o1.matches(r, f) || o2.matches(r, f));
            case FullSym.NOT:
                return (!o1.matches(r, f));
        }
        return (false);
    }
}
