package playground.solrmarc.index.fieldmatch;

import playground.solrmarc.index.mapping.impl.TranslationMappingFactory;

public final class FieldFormatterTranslationMapped extends FieldFormatterMapped
{
    static TranslationMappingFactory mapFactory = null;

    public FieldFormatterTranslationMapped(FieldFormatter toDecorate, String translationMapPropertyFileSpec)
    {
        super(toDecorate);
        if (mapFactory == null)  mapFactory = new TranslationMappingFactory();
        map = mapFactory.createMultiValueMapping(translationMapPropertyFileSpec);
    }
}
