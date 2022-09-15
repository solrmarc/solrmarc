package org.solrmarc.index.extractor.impl.direct;


import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.specification.Specification;

import java.util.Collection;
import java.util.EnumSet;

public class DirectMultiValueExtractor extends AbstractMultiValueExtractor implements ExternalMethod, ModifyableMultiValueExtractor
{
    private Specification fieldsAndSubfieldSpec;

    public DirectMultiValueExtractor(Specification fieldSpec)
    {
        fieldsAndSubfieldSpec = fieldSpec;
    }
    
    private DirectMultiValueExtractor(DirectMultiValueExtractor toClone)
    {
        if (toClone.fieldsAndSubfieldSpec.isThreadSafe())
            fieldsAndSubfieldSpec = toClone.fieldsAndSubfieldSpec;
        else
            fieldsAndSubfieldSpec = (Specification)toClone.fieldsAndSubfieldSpec.makeThreadSafeCopy();
    }

    public Specification getFieldsAndSubfieldSpec()
    {
        return fieldsAndSubfieldSpec;
    }

    public void setFieldsAndSubfieldSpec(Specification fieldsAndSubfieldSpec)
    {
        this.fieldsAndSubfieldSpec = fieldsAndSubfieldSpec;
    }

    public void addMap(AbstractMultiValueMapping valueMapping)
    {
        fieldsAndSubfieldSpec.addMap(valueMapping);
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
        if (endStr.equals("toEnd"))
        {
            fieldsAndSubfieldSpec.setSubstring(start, -1);
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
            fieldsAndSubfieldSpec.setSubstring(start, end);
        }
    }

    public void setFormatPatterns(String[] mapParts)
    {
        fieldsAndSubfieldSpec.setFormatPatterns(mapParts);
    }

    @Override
    public boolean isThreadSafe()
    {
        return fieldsAndSubfieldSpec.isThreadSafe();
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new DirectMultiValueExtractor(this);
    }
}
