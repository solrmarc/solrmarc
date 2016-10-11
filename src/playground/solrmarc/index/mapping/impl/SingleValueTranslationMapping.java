package playground.solrmarc.index.mapping.impl;

import playground.solrmarc.index.mapping.AbstractSingleValueMapping;

import java.util.Properties;

public class SingleValueTranslationMapping implements AbstractSingleValueMapping
{
    private final Properties translationMapping;
    private final String defaultValue;

    public SingleValueTranslationMapping(Properties translationMapping) {
        this.translationMapping = translationMapping;

        String property = null;
        for (final String defaultKey : DEFAULT_KEYS) {
            property = translationMapping.getProperty(defaultKey);
            if (property != null) {
                break;
            }
        }
        this.defaultValue = property;
    }

    @Override
    public String map(final String value) {
        return translationMapping.getProperty(value, defaultValue);
    }
}
