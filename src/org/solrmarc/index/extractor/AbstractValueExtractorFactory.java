package org.solrmarc.index.extractor;

public abstract class AbstractValueExtractorFactory
{
    public abstract boolean canHandle(final String solrFieldName, final String mappingConfiguration);

    public abstract AbstractValueExtractor<?> createExtractor(final String solrFieldName, final String[] parts);
}
