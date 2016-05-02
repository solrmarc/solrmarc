package playground.solrmarc.index.fieldmatch;

import java.util.List;

import playground.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import playground.solrmarc.index.mapping.impl.MultiValuePatternMapping;
import playground.solrmarc.index.mapping.impl.PatternMappingFactory;

public final class FieldFormatterPatternMapped extends FieldFormatterMapped
{
    // static TranslationMappingFactory mapFactory = null;

    public FieldFormatterPatternMapped(String patternMapSpec)
    {
        super(new MultiValuePatternMapping(PatternMappingFactory.pattermMappingsFromString(patternMapSpec)));
    }
    
    public FieldFormatterPatternMapped(String[] patternMapSpecs)
    {
        super(new MultiValuePatternMapping(PatternMappingFactory.pattermMappingsFromStrings(patternMapSpecs, 0)));
    }
 
    public FieldFormatterPatternMapped(FieldFormatter toDecorate, String patternMapSpec)
    {
        super(toDecorate, new MultiValuePatternMapping(PatternMappingFactory.pattermMappingsFromString(patternMapSpec)));
    }
    
    public FieldFormatterPatternMapped(FieldFormatter toDecorate, String[] patternMapSpecs)
    {
        super(toDecorate, new MultiValuePatternMapping(PatternMappingFactory.pattermMappingsFromStrings(patternMapSpecs, 0)));
    }
}
