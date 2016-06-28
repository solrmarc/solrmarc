package org.solrmarc.index.extractor;

import java.util.Collection;
import java.util.Collections;

import org.marc4j.marc.Record;

public class MultiValueWrapperSingleValueExtractor extends AbstractMultiValueExtractor
{

    private final AbstractSingleValueExtractor wrapped;

    public MultiValueWrapperSingleValueExtractor(AbstractSingleValueExtractor toWrap)
    {
        wrapped = toWrap;
    }

    @Override
    public Collection<String> extract(Record record) throws Exception
    {
        final String result = wrapped.extract(record);
        if (result == null) return (Collections.EMPTY_LIST);
        else return (Collections.singletonList(result));
    }
}
