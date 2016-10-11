package playground.solrmarc.index.extractor.impl.date;

import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;
import playground.solrmarc.index.utils.StringReader;


public class DateValueExtractorFactory extends AbstractValueExtractorFactory {

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration) {
        final String mapping = mappingConfiguration.toLowerCase();
        return mapping.startsWith("date".toLowerCase())
                || mapping.startsWith("dateOfPublication".toLowerCase())
                || mapping.startsWith("dateRecordIndexed".toLowerCase())
                || mapping.startsWith("index_date".toLowerCase());
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration) {
        final String mapping = mappingConfiguration.readAll().toLowerCase();
        if (mapping.startsWith("date".toLowerCase())
                || mapping.startsWith("dateOfPublication".toLowerCase())) {
            return new DateOfPublicationValueExtractor();
        } else if (mapping.startsWith("dateRecordIndexed".toLowerCase())
                || mapping.startsWith("index_date".toLowerCase())) {
            return new DateRecordIndexedValueExtractor();
        }
        throw new IllegalArgumentException("Unknown impl configuration: " + solrFieldName + " = " + mappingConfiguration);

    }
}
