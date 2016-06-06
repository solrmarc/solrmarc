package playground.solrmarc.index.extractor.methodcall;

import java.lang.reflect.Method;
import java.util.Collection;

import playground.solrmarc.index.indexer.IndexerSpecException;

public class MultiValueExtractorMethodCall extends AbstractExtractorMethodCall<Collection<String>>
{
    private final Object mixin;
    private final Method method;
    private final Method perRecordInit;

    public MultiValueExtractorMethodCall(final Object mixin, final Method method, final Method perRecordInit)
    {
        super(mixin.getClass().getSimpleName(), method.getName(), perRecordInit != null);
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

    @Override 
    protected boolean perRecordInitCalled(Object[] record)
    {
        return (MethodCallManager.instance().alreadyCalledFor(perRecordInit, record[0]));
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
    
}
