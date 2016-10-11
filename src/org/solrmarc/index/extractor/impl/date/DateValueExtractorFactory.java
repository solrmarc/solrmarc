package org.solrmarc.index.extractor.impl.date;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.utils.StringReader;


public class DateValueExtractorFactory extends AbstractValueExtractorFactory
{

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        final String mapping = mappingConfiguration.toLowerCase();
        return mapping.startsWith("date".toLowerCase()) || mapping.startsWith("dateOfPublication".toLowerCase())
                || mapping.startsWith("dateRecordIndexed".toLowerCase())
                || mapping.startsWith("index_date".toLowerCase());
    }

    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final String mapping)
    {
        if (mapping.startsWith("dateRecordIndexed".toLowerCase()) || mapping.startsWith("index_date".toLowerCase()))
        {
            return new DateRecordIndexedValueExtractor();
        }
        else if (mapping.startsWith("date".toLowerCase()) || mapping.startsWith("dateOfPublication".toLowerCase()))
        {
            return new DateOfPublicationValueExtractor();
        }
         
        throw new IllegalArgumentException("Unknown impl configuration: " + solrFieldName + " = " + mapping);

    }
    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration)
    {
        final String mapping = mappingConfiguration.readAll().toLowerCase();
        return createExtractor(solrFieldName, mapping);

    }
    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final String[] parts)
    {
        return createExtractor(solrFieldName, parts[0]);
    }
}
