package playground.solrmarc.index.specification.conditional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import java_cup.runtime.Symbol;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.indexer.IndexerSpecException;


public class ConditionSubfield extends Condition
{
    char sfCode;
    final String value;
    final Pattern valuePattern;
    int op;
       
    public ConditionSubfield(String s1, String s2, int op)
    {
        this.sfCode = s1.charAt(0);
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
        final List<Subfield> sfl = ((DataField)f).getSubfields(sfCode);
        for (Subfield sf : sfl)
        {
            String sfVal = (sf == null) ? null : sf.getData();
            switch (op) {
                case sym.EQU:  { if (sfVal.equals(value)) return(true); break; }
                case sym.NEQ:  { if (sfVal.equals(value)) return(false); break; }
                case sym.MATCH: { if (valuePattern != null && valuePattern.matcher(sfVal).matches()) return(true); break; }
            }
        }
        return(op == sym.NEQ ? true : false);
    }
}
