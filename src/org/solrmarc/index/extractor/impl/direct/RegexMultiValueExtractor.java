package org.solrmarc.index.extractor.impl.direct;


import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.specification.RegexSpecification;
import org.solrmarc.index.specification.Specification;
import org.solrmarc.index.specification.conditional.Condition;

import java.util.Collection;
import java.util.EnumSet;
import java.util.regex.Pattern;

public class RegexMultiValueExtractor extends AbstractMultiValueExtractor implements ExternalMethod, ModifyableMultiValueExtractor
{
    private RegexSpecification fieldRegex;

    public RegexMultiValueExtractor(String regex)
    {
        this.fieldRegex = new RegexSpecification(regex);
    }
    
    public RegexMultiValueExtractor(String regex,  Condition cond)
    {
        this.fieldRegex = new RegexSpecification(regex, cond);
    }
    
    public RegexMultiValueExtractor(RegexSpecification fieldRegex)
    {
        this.fieldRegex = fieldRegex;
    }
    
    private RegexMultiValueExtractor(RegexMultiValueExtractor toClone)
    {
        fieldRegex = toClone.fieldRegex;
    }

//    public Specification getFieldsAndSubfieldSpec()
//    {
//        return fieldsAndSubfieldSpec;
//    }
//
//    public void setFieldsAndSubfieldSpec(Specification fieldsAndSubfieldSpec)
//    {
//        this.fieldsAndSubfieldSpec = fieldsAndSubfieldSpec;
//    }
//
//    public void addMap(AbstractMultiValueMapping valueMapping)
//    {
//        fieldsAndSubfieldSpec.addMap(valueMapping);
//    }
//
    public final Collection<FieldMatch> getFieldMatches(final Record record)
    {
        Collection<FieldMatch> result = fieldRegex.getFieldMatches(record);
        return result;
    }

    @Override
    public void extract(Collection<String> result, final Record record) throws Exception
    {
        for (FieldMatch fm : getFieldMatches(record))
        {
            fm.addValuesTo(result);
        }
    }

    @Override
    public void addCleanVal(eCleanVal cleanVal)
    {
        fieldRegex.addCleanVal(cleanVal);
    }

    @Override
    public void setCleanVal(EnumSet<eCleanVal> of)
    {
        fieldRegex.setCleanVal(of);        
    }
    
    @Override
    public void setJoinVal(eJoinVal joinVal)
    {
        fieldRegex.setJoinVal(joinVal);        
    }

    @Override
    public void setSeparator(String separator)
    {
        fieldRegex.setSeparator(separator);
    }

//    public void setSubstring(String startStr, String endStr)
//    {
//        int start, end;
//        try {
//            start = Integer.parseInt(startStr);
//        }
//        catch (NumberFormatException nfe)
//        {
//            throw new IndexerSpecException("Illegal substring specification: " + startStr);
//        }
//        if (endStr.equals("toEnd"))
//        {
//            fieldsAndSubfieldSpec.setSubstring(start, -1);
//        }
//        else 
//        {
//            try {
//                end = Integer.parseInt(endStr);
//            }
//            catch (NumberFormatException nfe)
//            {
//                throw new IndexerSpecException("Illegal substring specification: " + endStr);
//            }
//            if (start < 0 || end < 0 || start > end)
//            {
//                throw new IndexerSpecException("Illegal substring interval: " + start + " " + end);
//            }
//            fieldsAndSubfieldSpec.setSubstring(start, end);
//        }
//    }

    @Override
    public void setFormatPatterns(String[] mapParts)
    {
        fieldRegex.setFormatPatterns(mapParts);
    }

    @Override
    public boolean isThreadSafe()
    {
        return fieldRegex.isThreadSafe();
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new RegexMultiValueExtractor(this);
    }

    @Override
    public void setSubstring(String startStr, String endStr)
    {
        int start, end;
        try {
            start = Integer.parseInt(startStr);
        }
        catch (NumberFormatException nfe)
        {
            throw new IndexerSpecException("Illegal substring specification: " + startStr);
        }
        if (endStr.equals("toEnd"))
        {
            fieldRegex.setSubstring(start, -1);
        }
        else 
        {
            try {
                end = Integer.parseInt(endStr);
            }
            catch (NumberFormatException nfe)
            {
                throw new IndexerSpecException("Illegal substring specification: " + endStr);
            }
            if (start < 0 || end < 0 || start > end)
            {
                throw new IndexerSpecException("Illegal substring interval: " + start + " " + end);
            }
            fieldRegex.setSubstring(start, end);
        }
    }

    @Override
    public void addMap(AbstractMultiValueMapping valueMapping)
    {
        // TODO Auto-generated method stub
        
    }
}
