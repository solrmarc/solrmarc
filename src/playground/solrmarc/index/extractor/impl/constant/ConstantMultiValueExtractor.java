package playground.solrmarc.index.extractor.impl.constant;

import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.marc4j.marc.Record;

import java.util.Collection;
import java.util.Collections;

public class ConstantMultiValueExtractor implements AbstractMultiValueExtractor
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

    @Override
    public Collection<String> extract(final Record record)
    {
        return constantValues;
    }
}
