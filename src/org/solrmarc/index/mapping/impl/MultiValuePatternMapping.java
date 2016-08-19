package org.solrmarc.index.mapping.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public class MultiValuePatternMapping extends AbstractMultiValueMapping
{
    private final List<PatternMapping> patternMappings;
    private final boolean applyAll;
    private final boolean keepRaw; 
    
    public MultiValuePatternMapping(List<PatternMapping> patternMappings, boolean applyAll, boolean keepRaw)
    {
        this.patternMappings = patternMappings;
        this.applyAll = applyAll;
        this.keepRaw = keepRaw;
    }

    public MultiValuePatternMapping(List<PatternMapping> patternMappings)
    {
        this.patternMappings = patternMappings;
        this.applyAll = false;
        this.keepRaw = false;
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
        if (mappedValues.size()== 0 && keepRaw == true)
        {
            mappedValues.addAll(values);
        }
        return mappedValues;
    }
}
