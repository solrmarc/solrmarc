package playground.solrmarc.index.extractor.formatter;

import java.util.List;

import playground.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import playground.solrmarc.index.mapping.impl.MultiValuePatternMapping;
import playground.solrmarc.index.mapping.impl.PatternMappingFactory;

public final class FieldFormatterPatternMapped extends FieldFormatterMapped
{
    // static TranslationMappingFactory mapFactory = null;

    public FieldFormatterPatternMapped(String patternMapSpec)
    {
        super(PatternMappingFactory.getMultiValuePattermMappingsFromString(patternMapSpec));
    }
    
    public FieldFormatterPatternMapped(String[] patternMapSpecs)
    {
        super(PatternMappingFactory.getMultiValuePattermMappingsFromStrings(patternMapSpecs, 0));
    }
 
    public FieldFormatterPatternMapped(FieldFormatter toDecorate, String patternMapSpec)
    {
        super(toDecorate, PatternMappingFactory.getMultiValuePattermMappingsFromString(patternMapSpec));
    }
    
    public FieldFormatterPatternMapped(FieldFormatter toDecorate, String[] patternMapSpecs)
    {
        super(toDecorate, PatternMappingFactory.getMultiValuePattermMappingsFromStrings(patternMapSpecs, 0));
    }
}
