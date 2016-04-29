package playground.solrmarc.index.collector;

import java.util.Collection;

public interface AbstractValueCollector<T> {
//    Object collect(final T values);
    Collection<String> collect(final T values) throws Exception;
}