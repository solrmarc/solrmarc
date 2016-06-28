package org.solrmarc.index.extractor.impl.direct;


import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.extractor.formatter.FieldFormatterBase;
import org.solrmarc.index.extractor.formatter.FieldFormatterDecorator;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.specification.AbstractSpecificationFactory;
import org.solrmarc.index.specification.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;

public class DirectMultiValueExtractor extends AbstractMultiValueExtractor
{
    private Specification fieldsAndSubfieldSpec;

    public DirectMultiValueExtractor(final String fieldsAndSubfields)
    {
        fieldsAndSubfieldSpec = AbstractSpecificationFactory.createSpecification(fieldsAndSubfields);
    }

    public DirectMultiValueExtractor(Specification fieldSpec)
    {
        fieldsAndSubfieldSpec = fieldSpec;
    }

    public Specification getFieldsAndSubfieldSpec()
    {
        return fieldsAndSubfieldSpec;
    }

    public void setFieldsAndSubfieldSpec(Specification fieldsAndSubfieldSpec)
    {
        this.fieldsAndSubfieldSpec = fieldsAndSubfieldSpec;
    }

    public void addFormatter(FieldFormatterDecorator fmt)
    {
        this.fieldsAndSubfieldSpec.addFormatter(fmt);
    }
    
    public final Collection<FieldMatch> getFieldMatches(final Record record)
    {
        Collection<FieldMatch> result = fieldsAndSubfieldSpec.getFieldMatches(record);
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

    public void addCleanVal(eCleanVal cleanVal)
    {
        fieldsAndSubfieldSpec.addCleanVal(cleanVal);
    }

    public void setCleanVal(EnumSet<eCleanVal> of)
    {
        fieldsAndSubfieldSpec.setCleanVal(of);        
    }
    
    public void setJoinVal(eJoinVal joinVal)
    {
        fieldsAndSubfieldSpec.setJoinVal(joinVal);        
    }

    public void setSeparator(String separator)
    {
        fieldsAndSubfieldSpec.setSeparator(separator);
    }

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
        fieldsAndSubfieldSpec.setSubstring(start, end);
    }

    public void setFormatPatterns(String[] mapParts)
    {
        fieldsAndSubfieldSpec.setFormatPatterns(mapParts);
    }


}
