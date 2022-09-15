package org.solrmarc.index.extractor.impl.script;

import bsh.BshMethod;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.UtilEvalError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.extractor.methodcall.MethodCallContext;
import org.solrmarc.index.extractor.methodcall.MethodCallMultiValueExtractor;
import org.solrmarc.index.extractor.methodcall.MethodCallSingleValueExtractor;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.tools.PropertyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ScriptValueExtractorFactory extends AbstractValueExtractorFactory
{
    // TODO: Use dynamic paths
    private final static Logger logger = LogManager.getLogger(ScriptValueExtractorFactory.class);
    // TODO: Use SolrIndexer instead of Object.
    private final static Map<String, Interpreter> INTERPRETERS = new HashMap<>();
    private static Map<String, BshMethod> methods = new HashMap<>();

    private Interpreter getInterpreter(final String scriptFileName)
    {
        Interpreter bsh = INTERPRETERS.get(scriptFileName);
        if (bsh != null)
        {
            return bsh;
        }
        logger.debug("Load bean shell script: " + scriptFileName);
        bsh = new Interpreter();
        bsh.setClassLoader(this.getClass().getClassLoader());
        String paths[] = new String[ValueIndexerFactory.instance().getHomeDirs().length];
        for (int i = 0 ; i < paths.length; i++)
        { 
            paths[i] = (ValueIndexerFactory.instance().getHomeDirs())[i] + File.separator + "index_scripts";
        }
        String[] inputSource = new String[1];
        InputStream script = PropertyUtils.getPropertyFileInputStream(paths, scriptFileName, false, inputSource);
        try
        {
            bsh.setOut(System.out);
            bsh.setErr(System.err);
            bsh.eval(new InputStreamReader(script), bsh.getNameSpace(), inputSource[0]);
            bsh.set("indexer", SolrIndexer.instance());
        }
        catch (EvalError e)
        {
            throw new IllegalArgumentException("Unable to evaluate script: " + scriptFileName, e);
        }
        try
        {
            script.close();
        }
        catch (IOException e)
        {
        }
        INTERPRETERS.put(scriptFileName, bsh);
        return bsh;
    }

    private BshMethod getBeanShellMethod(final Interpreter interpreter, final String methodName,
            final Class<?>[] parameterTypes)
    {
        BshMethod method = methods.get(methodName + Arrays.toString(parameterTypes));
        if (method != null)
        {
            return method;
        }
        try
        {
            method = interpreter.getNameSpace().getMethod(methodName, parameterTypes);
            methods.put(methodName + Arrays.toString(parameterTypes), method);
            return method;
        }
        catch (UtilEvalError e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        return mappingConfiguration.trim().startsWith("script");
    }

    private AbstractValueExtractor<?> createExtractor(final String solrFieldName, MethodCallContext context)
    {
        final String scriptFileName = context.getObjectName();

        final Interpreter interpreter = getInterpreter(scriptFileName);
        final BshMethod method = getBeanShellMethod(interpreter, context.getMethodName(), context.getParameterTypes());
        if (method == null)
        {
            throw new IllegalStateException("Couldn't find bean shell method " + context.getMethodName()
                    + Arrays.toString(context.getParameters()) + " in script file " + scriptFileName);
        }
        if (Collection.class.isAssignableFrom(method.getReturnType()))
        {
            return new MethodCallMultiValueExtractor(
                    new ScriptMultiValueMethodCall(interpreter, method, scriptFileName, context.getParameters().length), context.getParameters());
        }
        else
        {
            return new MethodCallSingleValueExtractor(
                    new ScriptSingleValueMethodCall(interpreter, method, scriptFileName, context.getParameters().length), context.getParameters());
        }
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(String solrFieldName, String[] parts)
    {
        MethodCallContext context = MethodCallContext.parseContextFromExtractorParts(parts);
        return(createExtractor(solrFieldName, context));
    }
}
