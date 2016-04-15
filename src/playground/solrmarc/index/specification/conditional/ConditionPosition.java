package playground.solrmarc.index.specification.conditional;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import java_cup.runtime.Symbol;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.indexer.IndexerSpecException;


public class ConditionPosition extends Condition
{
    int offset;
    int endOffset;
    final String value;
    final Pattern valuePattern;
    int op;
    
    public ConditionPosition(String s1, String s2, int op)
    {
        offset = Integer.parseInt(s1.replaceAll("\\[([0-9]+)(-[0-9]+)?\\]", "$1"));
        endOffset = offset;
        String endOffsetStr = s1.replaceAll("\\[([0-9]+)(-)?([0-9]+)?\\]", "$3");
        if (endOffsetStr != null && endOffsetStr.length() > 0) 
            endOffset = Integer.parseInt(endOffsetStr);
        this.op = op;
        this.value = s2;
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
        if (! (f instanceof ControlField)) return(false);
        
        final String data = ((ControlField)f).getData();
        if (data.length() < offset) return(false); 
        final String posVal = data.substring(offset, endOffset+1);
        
        switch (op) {
            case sym.EQU:  return(posVal.equals(value));
            case sym.NEQ:  return(!posVal.equals(value));
            case sym.MATCH:  return(valuePattern.matcher(posVal).matches());
        }
        return(false);
    }
   
}
