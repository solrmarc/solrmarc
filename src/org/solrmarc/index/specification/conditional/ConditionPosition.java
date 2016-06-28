package org.solrmarc.index.specification.conditional;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.indexer.FullConditionalParser;
import org.solrmarc.index.indexer.FullSym;


public class ConditionPosition extends Condition
{
    int offset;
    int endOffset;
    final String value;
    final Pattern valuePattern;
    int op;

    public ConditionPosition(String offsetStr, String value, int op)
    {
        this(null, offsetStr, value, op);
    }

    public ConditionPosition(String fieldTag, String offsetStr, String value, int op)
    {
        super(fieldTag);
        offset = Integer.parseInt(offsetStr.replaceAll("\\[([0-9]+)(-[0-9]+)?\\]", "$1"));
        endOffset = offset;
        String endOffsetStr = offsetStr.replaceAll("\\[([0-9]+)(-)?([0-9]+)?\\]", "$3");
        if (endOffsetStr != null && endOffsetStr.length() > 0) endOffset = Integer.parseInt(endOffsetStr);
        this.op = op;
        this.value = value;
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
        if (!(f instanceof ControlField)) return (false);

        final String data = ((ControlField) f).getData();
        if (data.length() < offset) return (false);
        final String posVal = data.substring(offset, endOffset + 1);

        switch (op) {
            case FullSym.EQU:
                return (posVal.equals(value));
            case FullSym.CONTAINS:
                return (posVal.contains(value));
            case FullSym.LT:
                return (posVal.startsWith(value));
            case FullSym.GT:
                return (posVal.endsWith(value));
            case FullSym.NEQ:
                return (!posVal.equals(value));
            case FullSym.MATCH:
                return (valuePattern.matcher(posVal).matches());
        }
        return (false);
    }

}
