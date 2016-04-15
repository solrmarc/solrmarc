package playground.solrmarc.index.extractor.impl.script;


import bsh.BshMethod;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.UtilEvalError;
import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;
import playground.solrmarc.index.extractor.methodcall.MethodCallContext;
import playground.solrmarc.index.extractor.methodcall.MethodCallMultiValueExtractor;
import playground.solrmarc.index.extractor.methodcall.MethodCallSingleValueExtractor;
import playground.solrmarc.index.utils.StringReader;
import playground.solrmarc.tools.Utils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class ScriptValueExtractorFactory extends AbstractValueExtractorFactory {
    // TODO: Use dynamic paths
    private final static String paths[] = new String[]{"index_scripts"};
    private final static Logger logger = Logger.getLogger(ScriptValueExtractorFactory.class);
    // TODO: Use SolrIndexer instead of Object.
    private final static Object SOLR_INDEXER = null;
    private final static Map<String, Interpreter> INTERPRETERS = new HashMap<>();
    private static Map<String, BshMethod> methods = new HashMap<>();

    private Interpreter getInterpreter(final String scriptFileName) {
        Interpreter bsh = INTERPRETERS.get(scriptFileName);
        if (bsh != null) {
            return bsh;
        }
        logger.debug("Load bean shell script: " + scriptFileName);
        bsh = new Interpreter();
        bsh.setClassLoader(this.getClass().getClassLoader());
        InputStream script = Utils.getPropertyFileInputStream(paths, scriptFileName);
        String scriptContents;
        try {
            scriptContents = Utils.readStreamIntoString(script);
            bsh.eval(scriptContents);
            bsh.set("indexer", SOLR_INDEXER);
            bsh.setOut(System.out);
            bsh.setErr(System.err);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read script: " + scriptFileName, e);
        } catch (EvalError e) {
            throw new IllegalArgumentException("Unable to evaluate script: " + scriptFileName, e);
        }
        INTERPRETERS.put(scriptFileName, bsh);
        return bsh;
    }

    private BshMethod getBeanShellMethod(final Interpreter interpreter, final String methodName, final Class[] parameterTypes) {
        BshMethod method = methods.get(methodName + Arrays.toString(parameterTypes));
        if (method != null) {
            return method;
        }
        try {
            method = interpreter.getNameSpace().getMethod(methodName, parameterTypes);
            methods.put(methodName + Arrays.toString(parameterTypes), method);
            return method;
        } catch (UtilEvalError e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration) {
        return mappingConfiguration.trim().startsWith("script");
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration) {
        MethodCallContext context = MethodCallContext.getContextForMappingConfiguration(mappingConfiguration);
        final String scriptFileName = context.getObjectName();

        final Interpreter interpreter = getInterpreter(scriptFileName);
        final BshMethod method = getBeanShellMethod(interpreter, context.getMethodName(), context.getParameterTypes());
        if (method == null) {
            throw new IllegalStateException("Couldn't find bean shell method " + context.getMethodName() + Arrays.toString(context.getParameters()) + " in script file " + scriptFileName);
        }
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
            return new MethodCallMultiValueExtractor(new ScriptMultiValueMethodCall(interpreter, method, scriptFileName), context.getParameters());
        } else {
            return new MethodCallSingleValueExtractor(new ScriptSingleValueMethodCall(interpreter, method, scriptFileName), context.getParameters());
        }
    }
}
