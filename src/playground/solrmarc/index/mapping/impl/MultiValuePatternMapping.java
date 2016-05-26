package playground.solrmarc.index.mapping.impl;

import playground.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class MultiValuePatternMapping extends AbstractMultiValueMapping
{
    private final List<PatternMapping> patternMappings;
    private final boolean applyAll;

    public MultiValuePatternMapping(List<PatternMapping> patternMappings, boolean applyAll)
    {
        this.patternMappings = patternMappings;
        this.applyAll = applyAll;
    }

    public MultiValuePatternMapping(List<PatternMapping> patternMappings)
    {
        this.patternMappings = patternMappings;
        this.applyAll = false;
    }

    @Override
    public Collection<String> map(final Collection<String> values)
    {
        List<String> mappedValues = new ArrayList<>(values.size());
        if (applyAll)
        {
            for (String value : values)
            {
                value = PatternMapping.mapSingleValue(patternMappings, value);
                if (value != null && value.length() > 0) mappedValues.add(value);
            }
        }
        else
        {
            for (String value : values)
            {
                PatternMapping.mapValues(patternMappings, value, mappedValues);
            }
        }
        return mappedValues;
    }
}
