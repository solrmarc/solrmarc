package org.solrmarc.index.extractor.methodcall;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class MultiValueExtractorMethodCall extends AbstractExtractorMethodCall<Collection<String>>
{
    private final Object mixin;
    private final Method method;
    private final Method perRecordInit;
    //private final Method isThreadSafe;

    public MultiValueExtractorMethodCall(final Object mixin, final Method method, final Method perRecordInit, int numParameters)
    {
        super(mixin.getClass().getName(), method.getName(), perRecordInit != null, numParameters);
        this.mixin = mixin;
        this.method = method;
        this.perRecordInit = perRecordInit;
        
        if (!Collection.class.isAssignableFrom(this.method.getReturnType()))
        {
            throw new IllegalArgumentException(
                    "The method's return type has to be assignable to Collection:\nMixin class:  "
                            + mixin.getClass().getName() + "\nMixin method: " + method.toString());
        }
    }
    
    private MultiValueExtractorMethodCall(MultiValueExtractorMethodCall toClone)
    {
        super(toClone.mixin.getClass().getName(), toClone.method.getName(), toClone.perRecordInit != null, toClone.getNumParameters());
        this.mixin = AbstractMethodCallFactory.createObjectForSpecifiedClass(toClone.mixin.getClass());
        this.method = toClone.method;
        this.perRecordInit = toClone.perRecordInit;
    }

    @Override 
    protected boolean perRecordInitCalled(Object[] record)
    {
        return (MethodCallManager.instance().alreadyCalledFor(this.mixin, record[0]));
    }
    
    @Override
    public void invokePerRecordInit(final Object[] record) throws Exception
    {
        perRecordInit.invoke(mixin, record);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> invoke(final Object[] parameters) throws Exception
    {
        return (Collection<String>) method.invoke(mixin, parameters);
    }

    @Override
    public boolean isThreadSafe()
    {
        if (perRecordInit != null) return false;
        try
        {
            Method isThreadSafe = mixin.getClass().getMethod("isThreadSafe");
            if (isThreadSafe.getReturnType() != boolean.class)
                return(false);
            boolean isSafe = (boolean) isThreadSafe.invoke(mixin);
            return(isSafe);
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            return(false);
        }
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new MultiValueExtractorMethodCall(this);
    }
}
