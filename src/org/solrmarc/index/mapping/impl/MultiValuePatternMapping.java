package org.solrmarc.index.mapping.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public class MultiValuePatternMapping extends AbstractMultiValueMapping
{
    private final String mappingName;
    private final List<PatternMapping> patternMappings;
    private final boolean filter;
    private final boolean applyAll;
    private final boolean keepRaw; 

    public MultiValuePatternMapping(String mappingName, List<PatternMapping> patternMappings, boolean filter, boolean applyAll, boolean keepRaw)
    {
        this.mappingName = mappingName;
        this.patternMappings = patternMappings;
        this.filter = filter;
        this.applyAll = applyAll;
        this.keepRaw = keepRaw;
    }

    public MultiValuePatternMapping(String mappingName, List<PatternMapping> patternMappings)
    {
        this.mappingName = mappingName;
        this.patternMappings = patternMappings;
        this.filter = false;
        this.applyAll = false;
        this.keepRaw = false;
    }

    @Override
    public Collection<String> map(final Collection<String> values)
    {
        List<String> mappedValues = new ArrayList<>(values.size());
        if (filter)
        {
            if (applyAll)
            {
                for (String value : values)
                {
                    value = PatternMapping.filterSingleValue(patternMappings, value);
                    if (value != null && value.length() > 0) mappedValues.add(value);
                }
            }
            else
            {
                for (String value : values)
                {
                    PatternMapping.filterValues(patternMappings, value, mappedValues);
                }
            }
        }
        else if (applyAll)
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
