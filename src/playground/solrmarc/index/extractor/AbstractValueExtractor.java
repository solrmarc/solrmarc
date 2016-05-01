package playground.solrmarc.index.extractor;

import org.marc4j.marc.Record;

public interface AbstractValueExtractor<T>
{
    T extract(final Record record) throws Exception;

    void setUnique(boolean unique);

    boolean isUnique();
}
