package playground.solrmarc.index.collector.impl;

import playground.solrmarc.index.collector.MultiValueCollector;

import java.util.Collection;
import java.util.Collections;

public class FirstObjectMultiValueCollector extends MultiValueCollector 
{
    public final static String KEYWORD = "first";
    @Override
    public Collection<String> collect(final Collection<String> values) 
    {
        if (values == null || values.isEmpty()) 
        {
            return(values);
        }
        return Collections.singletonList(values.iterator().next());
    }
}