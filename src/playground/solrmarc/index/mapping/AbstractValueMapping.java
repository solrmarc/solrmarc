package playground.solrmarc.index.mapping;

public interface AbstractValueMapping<T>
{
    String[] DEFAULT_KEYS = { "__DEFAULT", "" };
    String displayRawIfMissing = "displayRawIfMissing"; 

    T map(final T value) throws Exception;
}
