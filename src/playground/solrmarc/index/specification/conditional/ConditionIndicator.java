package playground.solrmarc.index.specification.conditional;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.indexer.IndexerSpecException;


public class ConditionIndicator extends Condition
{
    int indicatorNum;
    final String value;
    final Pattern valuePattern;
    int op;
      
    public ConditionIndicator(String s1, String s2, int op)
    {
        this.indicatorNum = (s1.equals("1")) ? 1 : 2;
        this.value = s2;
        this.op = op;
        if (op == sym.MATCH)
        {
            Pattern tmp;
            try {
            	tmp = Pattern.compile(value);
            }
            catch (PatternSyntaxException pse)
            {
            	tmp = Pattern.compile("");
            	ConditionalParser.addError("Invalid Regular Expression in Condition: " + value);
            	ConditionalParser.addError(pse.getMessage());
            }
            valuePattern = tmp;
        }
        else
        {
            valuePattern = null;
        }
    }
    
    public boolean matches(VariableField f)
    {
        if (f instanceof ControlField) return(false);
        final char indVal = (indicatorNum == 1) ? ((DataField)f).getIndicator1() : ((DataField)f).getIndicator2();
        switch (op) {
            case sym.EQU:  return(value.charAt(0) == indVal);
            case sym.NEQ:  return(value.charAt(0) != indVal);
            case sym.MATCH:  return(valuePattern.matcher(""+indVal).matches());
        }
        return(false);
    }
}
