package org.solrmarc.index.extractor;

import java.util.Collection;
import java.util.Collections;

import org.marc4j.marc.Record;

public class MultiValueWrapperSingleValueExtractor extends AbstractMultiValueExtractor implements ExternalMethod
{

    private final AbstractSingleValueExtractor wrapped;

    public MultiValueWrapperSingleValueExtractor(AbstractSingleValueExtractor toWrap)
    {
        wrapped = toWrap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> extract(Record record) throws Exception
    {
        final String result = wrapped.extract(record);
        if (result == null) return (Collections.EMPTY_LIST);
        else return (Collections.singletonList(result));
    }

    @Override
    public boolean isThreadSafe()
    {
        return (wrapped instanceof ExternalMethod) ? ((ExternalMethod)wrapped).isThreadSafe() : true;
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        if (wrapped instanceof ExternalMethod)
        {
            return ((ExternalMethod)wrapped).makeThreadSafeCopy();
        }
        return(null);
    }
}
