package playground.solrmarc.index.extractor.impl.constant;

import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.marc4j.marc.Record;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ConstantMultiValueExtractor extends AbstractMultiValueExtractor
{
    private final Collection<String> constantValues;

    public ConstantMultiValueExtractor(final Collection<String> constantValues)
    {
        this.constantValues = constantValues;
    }

    public ConstantMultiValueExtractor(String string)
    {
        this.constantValues = Collections.singletonList(string);
    }

    public ConstantMultiValueExtractor(String[] parts)
    {
        this.constantValues = Arrays.asList(parts);
    }

    @Override
    public Collection<String> extract(final Record record)
    {
        return constantValues;
    }

    @Override
    protected void extract(Collection<String> result, Record record) throws Exception
    {        
    }
}
