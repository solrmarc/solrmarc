package playground.solrmarc.index.extractor.methodcall;

import java.lang.reflect.Method;

public class SingleValueExtractorMethodCall extends AbstractExtractorMethodCall<String>
{
    private final Object mixin;
    private final Method method;
    private final Method perRecordInit;

    public SingleValueExtractorMethodCall(final Object mixin, final Method method, final Method perRecordInit)
    {
        super(mixin.getClass().getSimpleName(), method.getName(), perRecordInit != null);
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

    @Override 
    protected boolean perRecordInitCalled(Object[] record)
    {
        return (MethodCallManager.instance().alreadyCalledFor(perRecordInit, record[0]));
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

}
