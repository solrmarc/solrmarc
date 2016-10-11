package org.solrmarc.index.extractor.methodcall;

import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.ExternalMethod;

import java.util.Collection;

public class MethodCallMultiValueExtractor extends AbstractMultiValueExtractor implements ExternalMethod
{
    private final AbstractExtractorMethodCall<Collection<String>> methodCall;
    private final Object[] parameters;

    public MethodCallMultiValueExtractor(final AbstractExtractorMethodCall<Collection<String>> methodCall,
            final Object[] staticParameters)
    {
        if (methodCall == null)
        {
            throw new NullPointerException("CustomObject is null");
        }
        else if (staticParameters == null)
        {
            throw new NullPointerException("Custom parameters is null");
        }

        this.methodCall = methodCall;
        this.parameters = new Object[staticParameters.length + 1];
        System.arraycopy(staticParameters, 0, this.parameters, 1, staticParameters.length);
    }

    @SuppressWarnings("unchecked")
    private MethodCallMultiValueExtractor(MethodCallMultiValueExtractor toClone)
    {
        this.methodCall = (AbstractExtractorMethodCall<Collection<String>>) toClone.methodCall.makeThreadSafeCopy();
        this.parameters = new Object[toClone.parameters.length];
        System.arraycopy(toClone.parameters, 0, this.parameters, 0, toClone.parameters.length);
    }

    @Override
    public Collection<String> extract(final Record record) throws Exception
    {
        return methodCall.invoke(record, parameters);
    }

    @Override
    public boolean isThreadSafe()
    {
        return methodCall.isThreadSafe();
    }
    
    @Override
    public Object makeThreadSafeCopy()
    {
        return new MethodCallMultiValueExtractor(this);
    }

}
