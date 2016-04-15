package playground.solrmarc.index.specification.conditional;

import org.marc4j.marc.VariableField;


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
      
    public boolean matches(VariableField f)
    {
        switch (op) {
            case sym.AND:  return(o1.matches(f) && o2.matches(f));
            case sym.OR:   return(o1.matches(f) || o2.matches(f));
            case sym.NOT:  return(!o1.matches(f));
        }
        return(false);
    }
}
