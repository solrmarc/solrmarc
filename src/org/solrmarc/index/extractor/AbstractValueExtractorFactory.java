package org.solrmarc.index.extractor;

import org.solrmarc.index.utils.StringReader;

public abstract class AbstractValueExtractorFactory
{

    public abstract boolean canHandle(final String solrFieldName, final String mappingConfiguration);

    public abstract AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration);

    public abstract AbstractValueExtractor<?> createExtractor(final String solrFieldName, final String[] parts);
}
