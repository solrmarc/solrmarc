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

//import playground.solrmarc.index.fieldmatch.FieldFormatterJoin;
//import playground.solrmarc.index.fieldmatch.FieldFormatterSimple;

public class SingleDataFieldSpecification extends SingleSpecification
{
//    static FieldFormatter SINGLE_FMT = new FieldFormatterSimple(new FieldFormatterBase(true));
//    static FieldFormatter JOIN_FMT = new FieldFormatterJoin(new FieldFormatterBase(true));

    String subfields;
    Pattern subfieldPattern;
//    public final String separator = null;
    public final boolean cleanAll = true;
    public final boolean cleanEnd = true;

    public SingleDataFieldSpecification(String tag, String subfields, Condition cond, FieldFormatter fmt)
    {
        super(tag, cond);
        this.subfields = subfields;
        subfieldPattern = makePattern(subfields);
        this.fmt = fmt;
        // if (subfield)
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
        fmt.start();
        fmt.addTag(df);
        fmt.addIndicators(df);
        int cnt = 0;
        for (Subfield subfield : df.getSubfields())
        {
            final String codeStr = "" + subfield.getCode();
            Matcher matcher = subfieldPattern.matcher(codeStr);
            if (matcher.matches())
            {
                fmt.addSeparator(cnt);
                fmt.addCode(codeStr);
                Collection<String> prepped = fmt.prepData(vf, (subfield.getCode() == 'a'), subfield.getData());
                for (String val : prepped)
                {
                    val = fmt.handleSubFieldFormat(codeStr, val);
                    fmt.addVal(val);
                    fmt.addAfterSubfield(result);
                }
                cnt++;
            }
        }
        fmt.addAfterField(result);
    }

}
