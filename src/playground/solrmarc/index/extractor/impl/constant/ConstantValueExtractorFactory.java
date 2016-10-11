package playground.solrmarc.index.extractor.impl.constant;

import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.utils.StringReader;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;

import java.util.ArrayList;
import java.util.List;

public class ConstantValueExtractorFactory extends AbstractValueExtractorFactory {

    public boolean canHandle(final String solrFieldName, final String mappingConfiguration) {
        return mappingConfiguration.startsWith("\"") && mappingConfiguration.endsWith("\"");
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration) {
        final List<String> values = parseMappingConfiguration(mappingConfiguration.readAll());
        if (values.isEmpty()) {
            return null;
        } else if (values.size() == 1) {
            return new ConstantSingleValueExtractor(values.get(0));
        } else {
            return new ConstantMultiValueExtractor(values);
        }
    }

    private List<String> parseMappingConfiguration(final String mappingConfiguration) {
        final List<String> values = new ArrayList<>();
        final char[] chars = mappingConfiguration.toCharArray();
        boolean isQuoted = false;
        boolean isEscaped = false;
        StringBuilder buffer = new StringBuilder();
        for (char c : chars) {
            switch (c) {
                case '\t':
                case ' ':
                    if (isQuoted) {
                        buffer.append(c);
                    }
                    break;
                case '|':
                    if (isQuoted) {
                        buffer.append(c);
                    } else {
                        final String value = buffer.toString().trim();
                        if (!value.isEmpty()) {
                            values.add(value);
                        }
                        buffer = new StringBuilder();
                    }
                    break;
                case '\"':
                    if (isEscaped) {
                        isEscaped = false;
                        buffer.append(c);
                    } else {
                        isQuoted = !isQuoted;
                    }
                    break;
                case '\\':
                    if (isEscaped) {
                        buffer.append(c);
                    }
                    isEscaped = !isEscaped;
                    break;
                default:
                    buffer.append(c);
            }
        }
        if (!buffer.toString().isEmpty()) {
            values.add(buffer.toString());
        }
        return values;
    }
}
