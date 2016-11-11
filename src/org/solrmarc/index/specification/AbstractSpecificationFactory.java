package org.solrmarc.index.specification;

import org.marc4j.marc.impl.Verifier;
import org.solrmarc.index.specification.conditional.Condition;
//import org.solrmarc.index.specification.conditional.ConditionalParser;

//import playground.solrmarc.index.fieldmatch.FieldFormatterSubstring;

public class AbstractSpecificationFactory
{
    public static boolean canHandle(final String indexConfiguration)
    {
        return (indexConfiguration.matches("[{]*(LNK)?[0-9][0-9][0-9].*"));
    }

    public static SingleSpecification makeSingleSpecification(final String tag, final String subfields)
    {
        return makeSingleSpecification(tag, subfields, null, null);
    }

    public static SingleSpecification makeSingleSpecification(final String tag, final String subfields, final String position)
    {
        return makeSingleSpecification(tag, subfields, position, null);
    }

    public static SingleSpecification makeSingleSpecification(final String tag, final String subfields, final String position, Condition cond)
    {
        SingleSpecification spec;
        if (Verifier.isControlField(tag))
        {
            spec = new SingleControlFieldSpecification(tag, cond);
        }
        else if (tag.startsWith("LNK"))
        {
            spec = new SingleLinkedDataFieldSpecification(tag, subfields, cond);
        }
        else
        {
            spec = new SingleDataFieldSpecification(tag, subfields, cond);
        }
        if (position == null || position.length() == 0)
        {
            return(spec);
        }
        try {
            int offset = Integer.parseInt(position.replaceAll("\\[([0-9]+)(-[0-9]+)?\\]", "$1"));
            String endOffsetStr = position.replaceAll("\\[([0-9]+)(-)?([0-9]+)?\\]", "$3");
            int endOffset = offset;
            if (endOffsetStr != null && endOffsetStr.length() > 0) endOffset = Integer.parseInt(endOffsetStr);
            spec.setSubstring(offset, endOffset+1);
        }
        catch (NumberFormatException nfe) { /* eat it */ }
        return(spec);
    }
}
