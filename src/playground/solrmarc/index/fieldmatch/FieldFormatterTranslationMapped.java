package playground.solrmarc.index.fieldmatch;

import playground.solrmarc.index.mapping.impl.TranslationMappingFactory;

public final class FieldFormatterTranslationMapped extends FieldFormatterMapped
{
    static TranslationMappingFactory mapFactory = new TranslationMappingFactory();

    public FieldFormatterTranslationMapped(FieldFormatter toDecorate, String translationMapPropertyFileSpec)
    {
        super(toDecorate, mapFactory.createMultiValueMapping(translationMapPropertyFileSpec));
    }
    
    public FieldFormatterTranslationMapped(String translationMapPropertyFileSpec)
    {
        super(mapFactory.createMultiValueMapping(translationMapPropertyFileSpec));
    }
}
