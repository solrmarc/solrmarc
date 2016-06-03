package playground.solrmarc.index.extractor.impl.script;

import bsh.BshMethod;
import bsh.Interpreter;
import playground.solrmarc.index.extractor.methodcall.AbstractExtractorMethodCall;

import java.util.Collection;

public class ScriptMultiValueMethodCall extends AbstractExtractorMethodCall<Collection<String>>
{
    private final Interpreter interpreter;
    private final BshMethod method;

    public ScriptMultiValueMethodCall(final Interpreter interpreter, final BshMethod method,
            final String scriptFileName)
    {
        super(scriptFileName, method.getName(), false);
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

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> invoke(final Object[] parameters) throws Exception
    {
        return (Collection<String>) method.invoke(parameters, interpreter);
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

}
