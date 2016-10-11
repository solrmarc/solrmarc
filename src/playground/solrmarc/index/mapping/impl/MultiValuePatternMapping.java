package playground.solrmarc.index.mapping.impl;

import playground.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiValuePatternMapping implements AbstractMultiValueMapping {
    private final List<PatternMapping> patternMappings;

    public MultiValuePatternMapping(List<PatternMapping> patternMappings) {
        this.patternMappings = patternMappings;
    }

    @Override
    public Collection<String> map(final Collection<String> values) {
        List<String> mappedValues = new ArrayList<>(values.size());
        for (String value : values) 
        {
            PatternMapping.mapValues(patternMappings, value, mappedValues);
        }
        return mappedValues;
    }
}
