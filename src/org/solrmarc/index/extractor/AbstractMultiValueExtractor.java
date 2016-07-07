package org.solrmarc.index.extractor;

import java.util.ArrayList;
import java.util.Collection;

import org.marc4j.marc.Record;

public abstract class AbstractMultiValueExtractor implements AbstractValueExtractor<Collection<String>>
{
    @Override
    public Collection<String> extract(final Record record) throws Exception
    {
        Collection<String> result = makeEmptyResult();
        extract(result, record);
        return (result);
    }

    private Collection<String> makeEmptyResult()
    {
        Collection<String> results = new ArrayList<String>();
        return (results);
    }

    protected void extract(Collection<String> result, Record record) throws Exception
    {
    }

}
