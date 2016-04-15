package playground.solrmarc.index.extractor.impl.patternMapping;

import playground.solrmarc.index.utils.StringReader;
import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;

import java.util.*;
import java.util.regex.Pattern;

public class PatternMappingValueExtractorFactory extends AbstractValueExtractorFactory {
    private static final Map<String, List<PatternMapping>> PATTERN_MAPPINGS = new HashMap<>();
    private static final Pattern MAPPING_SPLITTING_PATTERN = Pattern.compile("=>");
    private static final Comparator<PatternMapping> PATTERN_MAPPING_COMPARATOR = new Comparator<PatternMapping>() {
        @Override
        public int compare(final PatternMapping o1, final PatternMapping o2) {
            return Integer.compare(o1.getOrderIndex(), o2.getOrderIndex());
        }
    };

    public static List<PatternMapping> getPatternMappingsForName(String name) {
        List<PatternMapping> mappings = PATTERN_MAPPINGS.get(name);
        if (mappings == null) {
            mappings = new ArrayList<>();
            PATTERN_MAPPINGS.put(name, mappings);
        }
        return mappings;
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration) {
        final boolean isPatternMap = solrFieldName.startsWith("pattern_map.");
        final int index = solrFieldName.indexOf(".pattern_");
        if (!isPatternMap) {
            return false;
        } else if (index == -1 || !Character.isDigit(solrFieldName.charAt(index + ".pattern_".length()))) {
            throw new IllegalArgumentException("A pattern impl name should end with '.pattern_[index]', where '[index]' is a number: " + solrFieldName + " = " + mappingConfiguration);
        }
        return true;
    }

    private void addPatternMapping(final String name, final PatternMapping mapping) {
        List<PatternMapping> mappings = getPatternMappingsForName(name);
        mappings.add(mapping);
        Collections.sort(mappings, PATTERN_MAPPING_COMPARATOR);
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration) {
        final String patterns = mappingConfiguration.readAll();
        final String[] split = MAPPING_SPLITTING_PATTERN.split(patterns, 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("A pattern impl has to have two patterns divided by '=>'. " + solrFieldName + " = " + patterns);
        }
        final int index = solrFieldName.indexOf(".pattern_");
        final String patternName = solrFieldName.substring(0, index);
        final int patternIndex = Integer.parseInt(solrFieldName.substring(index + ".pattern_".length(), solrFieldName.length()));
        final PatternMapping patternMapping = new PatternMapping(split[0], split[1], patternIndex);
        addPatternMapping(patternName, patternMapping);

        // A pattern impl doesn't extract something, so no impl will be created.
        return null;
    }

}
