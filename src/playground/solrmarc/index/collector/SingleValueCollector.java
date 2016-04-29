package playground.solrmarc.index.collector;

import java.util.Collection;
import java.util.Collections;

public class SingleValueCollector implements AbstractValueCollector<String>
{
    @Override
    public Collection<String> collect(String value)
    {
        return Collections.singletonList(value);
    }
}
