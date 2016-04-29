package playground.solrmarc.index.mapping.impl;

import playground.solrmarc.index.mapping.AbstractMultiValueMapping;

import java.util.*;
import java.util.regex.Pattern;

public class MultiValueTranslationMapping implements AbstractMultiValueMapping
{
    private final static Pattern SEPARATOR_PATTERN = Pattern.compile("[|]");
    private final Properties translationMapping;
    private final String defaultValue;

    public MultiValueTranslationMapping(Properties translationMapping)
    {
        this.translationMapping = translationMapping;

        String property = null;
        for (final String defaultKey : DEFAULT_KEYS)
        {
            property = translationMapping.getProperty(defaultKey);
            if (property != null)
            {
                break;
            }
        }
        this.defaultValue = property;
    }

    @Override
    public Collection<String> map(final Collection<String> values)
    {
        List<String> mappedValues = new ArrayList<>(values.size());
        for (String value : values)
        {
            final String translation = translationMapping.getProperty(value, defaultValue);
            if (translation != null && translation.contains("|"))
            {
                // TODO: This splitting should be done in the factory somehow.
                String[] translationParts = SEPARATOR_PATTERN.split(translation);
                Collections.addAll(mappedValues, translationParts);
            }
            else
            {
                mappedValues.add(translation);
            }
        }
        return mappedValues;
    }
}
