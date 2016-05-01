package playground.solrmarc.index.fieldmatch;

import java.util.List;

import playground.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import playground.solrmarc.index.mapping.impl.MultiValuePatternMapping;
import playground.solrmarc.index.mapping.impl.PatternMappingFactory;

public final class FieldFormatterPatternMapped extends FieldFormatterMapped
{
    // static TranslationMappingFactory mapFactory = null;

    public FieldFormatterPatternMapped(FieldFormatter toDecorate, String patternMapSpec)
    {
        super(toDecorate);
        List<PatternMapping> pm = PatternMappingFactory.pattermMappingsFromString(patternMapSpec);
        map = new MultiValuePatternMapping(pm);
    }
    
    public FieldFormatterPatternMapped(FieldFormatter toDecorate, String[] patternMapSpecs)
    {
        super(toDecorate);
        List<PatternMapping> pm = PatternMappingFactory.pattermMappingsFromStrings(patternMapSpecs);
        map = new MultiValuePatternMapping(pm);
    }
}
