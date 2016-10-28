package org.solrmarc.index.extractor.impl.script;

import bsh.BshMethod;
import bsh.Interpreter;

import java.util.Collection;

import org.solrmarc.index.extractor.methodcall.AbstractExtractorMethodCall;

public class ScriptMultiValueMethodCall extends AbstractExtractorMethodCall<Collection<String>>
{
    private final Interpreter interpreter;
    private final BshMethod method;

    public ScriptMultiValueMethodCall(final Interpreter interpreter, final BshMethod method,
            final String scriptFileName, int numParameters)
    {
        super(scriptFileName, method.getName(), false, numParameters);
        this.interpreter = interpreter;
        this.method = method;

        if (this.interpreter == null)
        {
            throw new NullPointerException("Interpreter is null.");
        }
        else if (!Collection.class.isAssignableFrom(this.method.getReturnType()))
        {
            throw new IllegalArgumentException("The method's return type has to be assignable to Collection:\nScript:  "
                    + scriptFileName + "\nMethod: " + method.toString());
        }
    }
 
    private ScriptMultiValueMethodCall(ScriptMultiValueMethodCall toClone)
    {
        super(toClone.getObjectName(), toClone.method.getName(), false, toClone.getNumParameters());
        this.interpreter = toClone.interpreter;
        this.method = toClone.method;        
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> invoke(final Object[] parameters) throws Exception
    {
        Object result;
        synchronized(interpreter)
        {
            result = method.invoke(parameters, interpreter);
        }
        if (result instanceof Collection)  
            return((Collection<String>)result);
        else 
            return(null);
    }

    @Override
    public void invokePerRecordInit(Object[] record) throws Exception
    {
        // TODO Implement perRecordInit support in scripts.
    }

    @Override
    protected boolean perRecordInitCalled(Object[] record)
    {
        // TODO Implement perRecordInit support in scripts.
        return false;
    }

    @Override
    public boolean isThreadSafe()
    {
        return false;
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new ScriptMultiValueMethodCall(this);
    }

    @Override
    public Class<?> getObjectClass()
    {
        return null;
    }
}
