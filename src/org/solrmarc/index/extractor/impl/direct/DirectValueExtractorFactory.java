package org.solrmarc.index.extractor.impl.direct;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.specification.AbstractSpecificationFactory;


public class DirectValueExtractorFactory extends AbstractValueExtractorFactory
{
    @Override
    public boolean canHandle(final String solrFieldName, final String indexConfiguration)
    {
        return (AbstractSpecificationFactory.canHandle(indexConfiguration));
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(String solrFieldName, String[] parts)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
