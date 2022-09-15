package org.solrmarc.index.extractor.impl.patternMapping;


import java.util.*;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;

public class PatternMappingValueExtractorFactory extends AbstractValueExtractorFactory
{
    private static final Map<String, List<PatternMapping>> PATTERN_MAPPINGS = new HashMap<>();

    public static List<PatternMapping> getPatternMappingsForName(String name)
    {
        List<PatternMapping> mappings = PATTERN_MAPPINGS.get(name);
        if (mappings == null)
        {
            mappings = new ArrayList<>();
            PATTERN_MAPPINGS.put(name, mappings);
        }
        return mappings;
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        final boolean isPatternMap = solrFieldName.startsWith("pattern_map.");
        final int index = solrFieldName.indexOf(".pattern_");
        if (!isPatternMap)
        {
            return false;
        }
        else if (index == -1 || !Character.isDigit(solrFieldName.charAt(index + ".pattern_".length())))
        {
            throw new IllegalArgumentException(
                    "A pattern impl name should end with '.pattern_[index]', where '[index]' is a number: "
                            + solrFieldName + " = " + mappingConfiguration);
        }
        return true;
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(String solrFieldName, String[] parts)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
