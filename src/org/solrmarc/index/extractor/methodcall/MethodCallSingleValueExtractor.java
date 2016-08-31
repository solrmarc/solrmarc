package org.solrmarc.index.extractor.methodcall;

import java.util.Collection;
import java.util.Collections;

import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.ExternalMethod;

public class MethodCallSingleValueExtractor extends AbstractMultiValueExtractor implements ExternalMethod
{
    private final AbstractExtractorMethodCall<String> methodCall;
    private final Object[] parameters;

    public MethodCallSingleValueExtractor(final AbstractExtractorMethodCall<String> methodCall,
            final Object[] parameters)
    {
        if (methodCall == null)
        {
            throw new NullPointerException("CustomObject is null");
        }
        else if (parameters == null)
        {
            throw new NullPointerException("Custom parameters is null");
        }

        this.methodCall = methodCall;
        this.parameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, this.parameters, 1, parameters.length);
    }
    
    @SuppressWarnings("unchecked")
    private MethodCallSingleValueExtractor(MethodCallSingleValueExtractor toClone)
    {
        this.methodCall = (AbstractExtractorMethodCall<String>) toClone.methodCall.makeThreadSafeCopy();
        this.parameters = toClone.parameters;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> extract(final Record record) throws Exception
    {
        Object result = methodCall.invoke(record, parameters);
        if (result == null)
            return(Collections.EMPTY_LIST);
        else
            return(Collections.singletonList((String)result));
    }

    @Override
    public boolean isThreadSafe()
    {
        return methodCall.isThreadSafe();
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new MethodCallSingleValueExtractor(this);
    }
}
