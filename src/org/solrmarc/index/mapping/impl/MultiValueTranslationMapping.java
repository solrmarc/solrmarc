package org.solrmarc.index.mapping.impl;


import java.util.*;
import java.util.regex.Pattern;

import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public class MultiValueTranslationMapping extends AbstractMultiValueMapping
{
    private final static Pattern SEPARATOR_PATTERN = Pattern.compile("[|]");
    private final Properties translationMapping;
    private final String defaultValue;
    private boolean displayRaw = false;
    
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
        String dRaw = translationMapping.getProperty(displayRawIfMissing);
        if (dRaw!= null && dRaw.equals("true")) 
            displayRaw = true;
        this.defaultValue = property;
    }

    @Override
    public Collection<String> map(final Collection<String> values)
    {
        List<String> mappedValues = new ArrayList<>(values.size());
        for (String value : values)
        {
            final String translation = (value == null) ? defaultValue : translationMapping.getProperty(value, defaultValue);
            if (translation != null && !translation.equals("null"))
            {
                if (translation.contains("|"))
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
            else if (displayRaw)
            {
                mappedValues.add(value);
            }
        }
        return mappedValues;
    }
}
