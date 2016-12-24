package org.solrmarc.index.specification;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.extractor.formatter.FieldFormatterBase;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.specification.conditional.Condition;

public class SingleDataFieldSpecification extends SingleSpecification
{
    String subfields;
    Pattern subfieldPattern;

    public SingleDataFieldSpecification(String tag, String subfields, Condition cond, FieldFormatter fmt)
    {
        super(tag, cond);
        this.subfields = subfields;
        subfieldPattern = makePattern(subfields);
        this.fmt = fmt;
    }

    public SingleDataFieldSpecification(String tag, String subfields, Condition cond)
    {
        super(tag, cond);
        this.subfields = subfields;
        subfieldPattern = makePattern(subfields);
        if (subfields != null && subfields.length() == 1) 
            fmt = new FieldFormatterBase(false).setJoinVal(eJoinVal.SEPARATE);
        else 
            fmt = new FieldFormatterBase(false).setJoinVal(eJoinVal.JOIN).setSeparator(" ");
    }

    public SingleDataFieldSpecification(String tag, String subfields)
    {
        this(tag, subfields, null);
    }

    protected SingleDataFieldSpecification(SingleDataFieldSpecification toClone)
    {
        super(toClone.tag, toClone.cond);
        this.subfields = toClone.subfields;
        this.subfieldPattern = toClone.subfieldPattern;
        this.fmt = (FieldFormatter) (toClone.fmt.isThreadSafe() ? toClone.fmt : toClone.fmt.makeThreadSafeCopy());
    }

    private final static Pattern makePattern(String subfields)
    {
        if (subfields == null || subfields.length() == 0) return (Pattern.compile("."));
        else if (subfields.startsWith("[") && subfields.endsWith("]")) return (Pattern.compile(subfields));
        else return (Pattern.compile("[" + subfields + "]"));
    }

    public void setFormatter(FieldFormatter fmt)
    {
        this.fmt = fmt;
    }

    // public String getSubfields()
    // {
    // return subfields;
    // }
    //
    @Override
    public void addFieldValues(Collection<String> result, VariableField vf) throws Exception
    {
        DataField df = (DataField) vf;
        StringBuilder sb = fmt.start();
        if (fmt.hasFieldFormat())
        {
            sb.append(fmt.getFieldFormat());
        } 
        fmt.addTag(sb, df);
        fmt.addIndicators(sb, df);
        int cnt = 0;
        for (Subfield subfield : df.getSubfields())
        {
            final String codeStr = "" + subfield.getCode();
            Matcher matcher = subfieldPattern.matcher(codeStr);
            if (matcher.matches())
            {
                fmt.addSeparator(sb,cnt);
                fmt.addCode(sb,codeStr);
                Collection<String> prepped = fmt.prepData(vf, (subfield.getCode() == 'a'), subfield.getData());
                for (String val : prepped)
                {
                    val = fmt.handleSubFieldFormat(codeStr, val);
                    fmt.addVal(sb, codeStr, val);
                    fmt.addAfterSubfield(sb, result);
                }
                cnt++;
            }
        }
        fmt.addAfterField(sb, result);
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new SingleDataFieldSpecification(this);
    }

}
