package playground.solrmarc.index.extractor.impl.constant;

import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.marc4j.marc.Record;

public class ConstantSingleValueExtractor implements AbstractSingleValueExtractor {
    private final String constantValue;

    public ConstantSingleValueExtractor(final String constantValue) {
        this.constantValue = constantValue;
    }

    @Override
    public String extract(final Record record) {
        return constantValue;
    }
}
