package org.solrmarc.index.extractor.impl.constant;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;

public class ConstantValueExtractorFactory extends AbstractValueExtractorFactory
{
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        return mappingConfiguration.startsWith("\"") && mappingConfiguration.endsWith("\"");
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(String solrFieldName, String[] parts)
    {
        return new ConstantMultiValueExtractor(parts);
    }
}
