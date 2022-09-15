package org.solrmarc.index.extractor.impl.date;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;


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
        if (mapping.toLowerCase().startsWith("daterecordindexed") || mapping.toLowerCase().startsWith("index_date"))
        {
            return new DateRecordIndexedValueExtractor();
        }
        else if (mapping.toLowerCase().startsWith("date") || mapping.toLowerCase().startsWith("dateOfPublication"))
        {
            return new DateOfPublicationValueExtractor();
        }

        throw new IllegalArgumentException("Unknown impl configuration: " + solrFieldName + " = " + mapping);

    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final String[] parts)
    {
        return createExtractor(solrFieldName, parts[0]);
    }
}
