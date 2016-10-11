package org.solrmarc.index.mapping.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.extractor.methodcall.SingleValueMappingMethodCall;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;


public class MethodCallSingleValueMapping extends AbstractMultiValueMapping implements ExternalMethod
{

    private final Object[] parameters;
    private final SingleValueMappingMethodCall methodCall;

    public MethodCallSingleValueMapping(SingleValueMappingMethodCall methodCall, String[] parameters)
    {
        this.methodCall = methodCall;
        this.parameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, this.parameters, 1, parameters.length);
    }

    private MethodCallSingleValueMapping(MethodCallSingleValueMapping toClone)
    {
        this.methodCall = (SingleValueMappingMethodCall) toClone.methodCall.makeThreadSafeCopy();
        this.parameters = new Object[toClone.parameters.length];
        System.arraycopy(toClone.parameters, 0, this.parameters, 0, toClone.parameters.length);
    }

    @Override
    public Collection<String> map(Collection<String> values) throws Exception
    {
        Collection<String> result = new ArrayList<String>(values.size());
        for (String value : values)
        {
            String oneResult = (String) (methodCall.invoke(value, parameters));

            if (oneResult != null) result.add(oneResult);
        }
        return(result);
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
        return(new MethodCallSingleValueMapping(this));
    }
}
