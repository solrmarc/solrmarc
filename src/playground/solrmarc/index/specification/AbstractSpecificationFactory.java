package playground.solrmarc.index.specification;

import org.marc4j.marc.impl.Verifier;

import playground.solrmarc.index.specification.conditional.Condition;
import playground.solrmarc.index.specification.conditional.ConditionalParser;

public class AbstractSpecificationFactory
{
    private static ConditionalParser parser = null;

    private static boolean do_debug_parse = true;

    public static boolean canHandle(final String indexConfiguration)
    {
        return (indexConfiguration.matches("[{]*(LNK)?[0-9][0-9][0-9].*"));

        // return ((indexConfiguration.length() >= 3 &&
        // Utils.isNumber(indexConfiguration.trim().substring(0, 3)) ) ||
        // (indexConfiguration.length() >= 6 &&
        // indexConfiguration.trim().substring(0, 3).equals("LNK") &&
        // Utils.isNumber(indexConfiguration.trim().substring(3, 6))) ||
        // indexConfiguration.startsWith("{") && indexConfiguration
        //
        // );
    }

    public static ConditionalParser getParser()
    {
        return parser;
    }

    public static Specification createSpecification(final String specificationStr)
    {
        if (parser == null) parser = new ConditionalParser(do_debug_parse);
        Specification result = null;
        try
        {
            result = (Specification) parser.parse(specificationStr, do_debug_parse);
        }
        finally
        {
        }
        // catch (ParseError e)
        // {
        //
        // }
        if (result == null || ConditionalParser.getErrors().size() > 0)
        {
//            result = new ErrorSpecification(ConditionalParser.getErrors());
        }
        result.setSpecLabel(specificationStr);
        return (result);

        // int start = 0;
        // int colonIndex = specificationStr.indexOf(':');
        //
        // Specification result;
        // if (colonIndex != -1)
        // {
        // CompositeSpecification tmpSpec = new CompositeSpecification();
        // while (colonIndex != -1)
        // {
        // final String spec = specificationStr.substring(start, colonIndex);
        // tmpSpec.addSpec(makeSingleSpecification(spec));
        // start = colonIndex+1;
        // colonIndex = specificationStr.indexOf(':', start);
        // }
        // result = tmpSpec;
        // }
        // else
        // {
        // result = makeSingleSpecification(specificationStr);
        // }
        // return(result);
    }

    public static SingleSpecification makeSingleSpecification(String specStr)
    {
        int tagEnd = 3;
        if (specStr.startsWith("LNK"))
        {
            tagEnd = 6;
        }
        final String tag = specStr.substring(0, tagEnd);
        final String subfields = specStr.substring(tagEnd);
        return makeSingleSpecification(tag, subfields);
    }

    public static SingleSpecification makeSingleSpecification(String tag, String subfields)
    {
        return makeSingleSpecification(tag, subfields, null);
    }

    public static SingleSpecification makeSingleSpecification(final String tag, final String subfields, Condition cond)
    {
        if (Verifier.isControlField(tag))
        {
            final String s1 = subfields;
            if (subfields == null || subfields.length() == 0)
            {
                return (new SingleControlFieldSpecification(tag));
            }
            int offset = Integer.parseInt(s1.replaceAll("\\[([0-9]+)(-[0-9]+)?\\]", "$1"));
            String endOffsetStr = s1.replaceAll("\\[([0-9]+)(-)?([0-9]+)?\\]", "$3");
            int endOffset = offset;
            if (endOffsetStr != null && endOffsetStr.length() > 0) endOffset = Integer.parseInt(endOffsetStr);
            return (new SingleControlFieldSpecification(tag, offset, endOffset, cond));
        }
        else if (tag.startsWith("LNK"))
        {
            return (new SingleLinkedDataFieldSpecification(tag, subfields, cond));
        }
        else
        {
            return (new SingleDataFieldSpecification(tag, subfields, cond));
        }
    }
}
