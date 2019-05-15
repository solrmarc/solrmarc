package org.solrmarc.index.mapping;

public interface AbstractValueMapping<T>
{
    String[] DEFAULT_KEYS = { "__DEFAULT", "" };
    String displayRawIfMissing = "displayRawIfMissing"; 
    String throwExceptionIfMissing = "throwExceptionIfMissing"; 

    T map(final T value) throws Exception;
}
