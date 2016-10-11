package org.solrmarc.index.mapping.impl;

import java.util.Collection;

import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.extractor.methodcall.MultiValueMappingMethodCall;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public class MethodCallMultiValueMapping extends AbstractMultiValueMapping implements ExternalMethod
{

    private final Object[] parameters;
    private final MultiValueMappingMethodCall methodCall;

    public MethodCallMultiValueMapping(MultiValueMappingMethodCall methodCall, String[] parameters)
    {
        this.methodCall = methodCall;
        this.parameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, this.parameters, 1, parameters.length);
    }
    
    private MethodCallMultiValueMapping(MethodCallMultiValueMapping toClone)
    {
        this.methodCall = (MultiValueMappingMethodCall) toClone.methodCall.makeThreadSafeCopy();
        this.parameters = new Object[toClone.parameters.length];
        System.arraycopy(toClone.parameters, 0, this.parameters, 0, toClone.parameters.length);
    }

    @Override
    public Collection<String> map(Collection<String> value) throws Exception
    {
        return (Collection<String>) (methodCall.invoke(value, parameters));
    }

    @Override
    public boolean isThreadSafe()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return(new MethodCallMultiValueMapping(this));
    }
}
