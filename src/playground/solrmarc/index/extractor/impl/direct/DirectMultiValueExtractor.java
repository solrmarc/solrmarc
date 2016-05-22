package playground.solrmarc.index.extractor.impl.direct;

import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import playground.solrmarc.index.extractor.formatter.FieldFormatter;
import playground.solrmarc.index.extractor.formatter.FieldFormatterBase;
import playground.solrmarc.index.extractor.formatter.FieldFormatterDecorator;
import playground.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import playground.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import playground.solrmarc.index.indexer.IndexerSpecException;
import playground.solrmarc.index.specification.AbstractSpecificationFactory;
import playground.solrmarc.index.specification.Specification;

import org.marc4j.marc.Record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;

public class DirectMultiValueExtractor extends AbstractMultiValueExtractor
{
//    public static final String UNIQUE = "unique";
//    public static final String FIRST = "first";
//    private FieldFormatter fmt;
    private Specification fieldsAndSubfieldSpec;

    public DirectMultiValueExtractor(final String fieldsAndSubfields)
    {
        fieldsAndSubfieldSpec = AbstractSpecificationFactory.createSpecification(fieldsAndSubfields);
 //       fmt = fieldsAndSubfieldSpec.getFormatter();
    }

    public DirectMultiValueExtractor(Specification fieldSpec)
    {
        fieldsAndSubfieldSpec = fieldSpec;
 //       fmt = fieldsAndSubfieldSpec.getFormatter();
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
    
//    public FieldFormatter getFormatter()
//    {
//        return fmt;
//    }
//
//    public void setFormatter(FieldFormatter fmt)
//    {
//        this.fmt = fmt;
//        if (fieldsAndSubfieldSpec != null) fieldsAndSubfieldSpec.setFormatter(fmt);
//    }

//    boolean unique = false;
//    boolean firstOnly = false;
//
//    public boolean isUnique()
//    {
//        return unique;
//    }
//
//    public void setUnique(boolean unique)
//    {
//        this.unique = unique;
//    }
//
//    public boolean firstOnly()
//    {
//        return (firstOnly);
//    }
//
//    public void setFirstOnly(boolean firstOnly)
//    {
//        this.firstOnly = firstOnly;
//    }

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


}
