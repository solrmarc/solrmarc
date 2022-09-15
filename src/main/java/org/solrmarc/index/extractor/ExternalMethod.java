package org.solrmarc.index.extractor;

public interface ExternalMethod
{
    boolean isThreadSafe();

    Object makeThreadSafeCopy();
}
