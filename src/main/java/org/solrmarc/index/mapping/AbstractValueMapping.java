package org.solrmarc.index.mapping;

public interface AbstractValueMapping<T>
{
    String[] DEFAULT_KEYS = { "__DEFAULT", "" };
    String displayRawIfMissing = "displayRawIfMissing"; 
    String throwExceptionIfMissing = "throwExceptionIfMissing"; 

    boolean ifApplies(char subfieldCode) ;
    T map(final T value) throws Exception;
}
