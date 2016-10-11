package playground.solrmarc.index.mapping;

public interface AbstractValueMapping<T> {
    String[] DEFAULT_KEYS = {
            "displayRawIfMissing",
            "__DEFAULT",
            ""
    };

    T map(final T value);
}
