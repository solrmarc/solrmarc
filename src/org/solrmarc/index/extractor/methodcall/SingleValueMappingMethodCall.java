package org.solrmarc.index.extractor.methodcall;

import java.lang.reflect.Method;

import org.solrmarc.index.extractor.ExternalMethod;

public class SingleValueMappingMethodCall extends AbstractMappingMethodCall<String> implements ExternalMethod
{
    private final Object mixin;
    private final Method method;

    public SingleValueMappingMethodCall(final Object mixin, final Method method)
    {
        super(mixin.getClass().getSimpleName(), method.getName());
        this.mixin = mixin;
        this.method = method;

        if (!this.method.getReturnType().equals(String.class))
        {
            throw new IllegalArgumentException(
                    "The method's return type has to be assignable to String:\nMixin class:  "
                            + mixin.getClass().getName() + "\nMixin method: " + method.toString());
        }
    }

    private SingleValueMappingMethodCall(SingleValueMappingMethodCall toClone)
    {
        super(toClone.getObjectName(), toClone.getMethodName());
        this.mixin = AbstractMethodCallFactory.createObjectForSpecifiedClass(toClone.mixin.getClass());
        this.method = toClone.method;        
    }

    @Override
    public String invoke(final Object[] parameters) throws Exception
    {
        return (String) method.invoke(mixin, parameters);
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
        return(new SingleValueMappingMethodCall(this));
    }
}
