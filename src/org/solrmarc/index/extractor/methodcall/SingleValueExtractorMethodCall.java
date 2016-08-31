package org.solrmarc.index.extractor.methodcall;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class SingleValueExtractorMethodCall extends AbstractExtractorMethodCall<String>
{
    private final Object mixin;
    private final Method method;
    private final Method perRecordInit;

    public SingleValueExtractorMethodCall(final Object mixin, final Method method, final Method perRecordInit, int numParameters)
    {
        super(mixin.getClass().getName(), method.getName(), perRecordInit != null, numParameters);
        this.mixin = mixin;
        this.method = method;
        this.perRecordInit = perRecordInit;

        if (!String.class.isAssignableFrom(this.method.getReturnType()))
        {
            throw new IllegalArgumentException(
                    "The method's return type has to be assignable to String:\nMixin class:  "
                            + mixin.getClass().getName() + "\nMixin method: " + method.toString());
        }
    }
    
    private SingleValueExtractorMethodCall(SingleValueExtractorMethodCall toClone)
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
    public void invokePerRecordInit(Object[] record) throws Exception
    {
        perRecordInit.invoke(mixin, record);
    }

    @Override
    public String invoke(final Object[] parameters) throws Exception
    {
        return (String) method.invoke(mixin, parameters);
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
        return new SingleValueExtractorMethodCall(this);
    }

}
