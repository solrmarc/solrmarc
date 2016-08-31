package org.solrmarc.index.extractor.impl.script;

import org.solrmarc.index.extractor.methodcall.AbstractExtractorMethodCall;

import bsh.BshMethod;
import bsh.Interpreter;

public class ScriptSingleValueMethodCall extends AbstractExtractorMethodCall<String>
{
    private final Interpreter interpreter;
    private final BshMethod method;

    public ScriptSingleValueMethodCall(final Interpreter interpreter, final BshMethod method, final String scriptFileName, int numParameters)
    {
        super(scriptFileName, method.getName(), false, numParameters);
        this.interpreter = interpreter;
        this.method = method;

        if (this.interpreter == null)
        {
            throw new NullPointerException("Interpreter is null.");
        }
        else if (!String.class.isAssignableFrom(this.method.getReturnType()))
        {
            throw new IllegalArgumentException("The method's return type has to be assignable to String:\nScript:  "
                    + scriptFileName + "\nMethod: " + method.toString());
        }
    }
    
    private ScriptSingleValueMethodCall(ScriptSingleValueMethodCall toClone)
    {
        super(toClone.getObjectName(), toClone.method.getName(), false, toClone.getNumParameters());
        this.interpreter = new Interpreter(toClone.interpreter);
        this.method = toClone.method;        
    }
    
    @Override
    public String invoke(final Object[] parameters) throws Exception
    {
       Object result = method.invoke(parameters, interpreter);
       if (result instanceof String)  
           return((String)result);
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
        return new ScriptSingleValueMethodCall(this);
    }
}
