package playground.solrmarc.index.collector;

import java.util.Collection;

public class MultiValueCollector implements AbstractValueCollector<Collection<String>> 
{
    @Override
    public Collection<String> collect(final Collection<String> values) {
        return values;
    }
}
