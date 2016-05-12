package playground.solrmarc.index.specification.conditional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.indexer.FullConditionalParser;
import playground.solrmarc.index.indexer.FullSym;

public class ConditionSubfield extends Condition
{
    char sfCode;
    final String value;
    final Pattern valuePattern;
    int op;

    public ConditionSubfield(String offsetStr, String value, int op)
    {
        this(null, offsetStr, value, op);
    }

    public ConditionSubfield(String fieldTag, String offsetStr, String value, int op)
    {
        super(fieldTag);
        this.sfCode = offsetStr.charAt(0);
        this.value = value;
        this.op = op;

        if (op == FullSym.MATCH)
        {
            Pattern tmp;
            try
            {
                tmp = Pattern.compile(value);
            }
            catch (PatternSyntaxException pse)
            {
                tmp = Pattern.compile("");
                FullConditionalParser.addError("Invalid Regular Expression in Condition: " + value);
                FullConditionalParser.addError(pse.getMessage());
            }
            valuePattern = tmp;
        }
        else
        {
            valuePattern = null;
        }
    }

    @Override
    public boolean matches(final VariableField f)
    {
        if (f instanceof ControlField) return (false);
        final List<Subfield> sfl = ((DataField) f).getSubfields(sfCode);
        for (Subfield sf : sfl)
        {
            String sfVal = (sf == null) ? null : sf.getData();
            switch (op) {
                case FullSym.EQU:
                {
                    if (sfVal.equals(value)) return (true);
                    break;
                }
                case FullSym.CONTAINS:
                {
                    if (sfVal.contains(value)) return (true);
                    break;
                }
                case FullSym.LT:
                {
                    if (sfVal.startsWith(value)) return (true);
                    break;
                }
                case FullSym.GT:
                {
                    if (sfVal.endsWith(value)) return (true);
                    break;
                }
                case FullSym.NEQ:
                {
                    if (sfVal.equals(value)) return (false);
                    break;
                }
                case FullSym.MATCH:
                {
                    if (valuePattern != null && valuePattern.matcher(sfVal).matches()) return (true);
                    break;
                }
            }
        }
        return (op == FullSym.NEQ ? true : false);
    }
}
