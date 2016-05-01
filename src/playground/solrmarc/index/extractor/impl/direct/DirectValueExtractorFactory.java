package playground.solrmarc.index.extractor.impl.direct;

import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.specification.AbstractSpecificationFactory;
import playground.solrmarc.index.utils.StringReader;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;

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
        int commaIndex = indexConfiguration.indexOf(',');
        if (commaIndex <= -1)
        {
            return new DirectMultiValueExtractor(indexConfiguration.readAll());
        }
        else
        {
            return new DirectMultiValueExtractor(indexConfiguration.readString(commaIndex));
        }
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(String solrFieldName, String[] parts)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
