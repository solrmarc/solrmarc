package playground.solrmarc.index.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.marc4j.marc.Record;

public abstract class AbstractMultiValueExtractor implements AbstractValueExtractor<Collection<String>>
{
//    @Override
//    public boolean isUnique()
//    {
//        return true;
//    }
//
//    @Override
//    public void setUnique(boolean unique)
//    {
//        //
//    }
//    
    @Override
    public Collection<String> extract(final Record record) throws Exception
    {
        Collection<String> result = makeEmptyResult();
        extract(result, record);
        return (result);
    }

    private Collection<String> makeEmptyResult()
    {
        Collection<String> results;
//        if (isUnique())
//        {
//            results = new LinkedHashSet<String>();
//        }
//        else
        {
            results = new ArrayList<String>();
        }
        return (results);
    }

    protected void extract(Collection<String> result, Record record) throws Exception
    {
    }

}
