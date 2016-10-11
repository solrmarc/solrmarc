package playground.solrmarc.index.collector.impl;

import playground.solrmarc.index.collector.MultiValueCollector;

import java.util.Collection;
import java.util.Iterator;

public class JoiningMultiValueCollector extends MultiValueCollector {
    public final static String KEYWORD = "join";
    private final String seperator;

    public JoiningMultiValueCollector(final String seperator) {
        this.seperator = seperator;
    }

    public JoiningMultiValueCollector() {
        this(" ");
    }

    @Override
    public Collection<String> collect(final Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        final Iterator<String> iterator = values.iterator();
        builder.append(iterator.next());
        while (iterator.hasNext()) {
            builder.append(seperator);
            builder.append(iterator.next());
        }
        return builder.toString();
    }
}
