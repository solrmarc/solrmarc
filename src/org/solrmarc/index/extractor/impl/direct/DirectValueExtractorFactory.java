package org.solrmarc.index.extractor.impl.direct;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.specification.AbstractSpecificationFactory;
import org.solrmarc.index.utils.StringReader;


public class DirectValueExtractorFactory extends AbstractValueExtractorFactory
{

    @Override
    public boolean canHandle(final String solrFieldName, final String indexConfiguration)
    {
        return (AbstractSpecificationFactory.canHandle(indexConfiguration));
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader indexConfiguration)
    {
//        int commaIndex = indexConfiguration.indexOf(',');
//        if (commaIndex <= -1)
//        {
//            return new DirectMultiValueExtractor(indexConfiguration.readAll());
//        }
//        else
//        {
//            return new DirectMultiValueExtractor(indexConfiguration.readString(commaIndex));
//        }
        return null;
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(String solrFieldName, String[] parts)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
