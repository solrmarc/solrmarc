package playground.solrmarc.index.mapping.impl;

import playground.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import playground.solrmarc.index.mapping.AbstractSingleValueMapping;

import java.util.List;

public class SingleValuePatternMapping implements AbstractSingleValueMapping
{
    private final List<PatternMapping> patternMappings;

    public SingleValuePatternMapping(List<PatternMapping> patternMappings) {
        this.patternMappings = patternMappings;
    }

    @Override
    public String map(String value) {
        return PatternMapping.mapSingleValue(patternMappings, value);
    }
}
