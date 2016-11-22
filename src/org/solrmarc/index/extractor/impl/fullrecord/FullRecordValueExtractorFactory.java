package org.solrmarc.index.extractor.impl.fullrecord;

import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;


public class FullRecordValueExtractorFactory extends AbstractValueExtractorFactory
{
    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        final String mapping = mappingConfiguration.toLowerCase().trim();
        return mapping.startsWith("FullRecordAs".toLowerCase()) || mapping.startsWith("xml")
                || mapping.startsWith("raw") || mapping.startsWith("json") || mapping.startsWith("json2")
                || mapping.startsWith("text");
    }

    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final String mapping)
    {
        String mappingLC = mapping.toLowerCase();
        if (mappingLC.startsWith("raw") || mappingLC.startsWith("FullRecordAsMARC".toLowerCase()))
        {
            return new FullRecordAsMarcValueExtractor();
        }
        else if (mappingLC.startsWith("xml") || mappingLC.startsWith("FullRecordAsXML".toLowerCase()))
        {
            return new FullRecordAsXMLValueExtractor();
        }
        else if (mappingLC.startsWith("json2") || mappingLC.startsWith("FullRecordAsJSON2".toLowerCase()))
        {
            return new FullRecordAsJSON2ValueExtractor();
        }
        else if (mappingLC.startsWith("json") || mappingLC.startsWith("FullRecordAsJSON".toLowerCase()))
        {
            return new FullRecordAsJSONValueExtractor();
        }
        else if (mappingLC.startsWith("text") || mappingLC.startsWith("FullRecordAsText".toLowerCase()))
        {
            return new FullRecordAsTextValueExtractor();
        }
        throw new IllegalArgumentException("Unknown impl configuration: " + solrFieldName + " = " + mapping);
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(String solrFieldName, String[] parts)
    {
        return createExtractor(solrFieldName, parts[0]);
    }
}
