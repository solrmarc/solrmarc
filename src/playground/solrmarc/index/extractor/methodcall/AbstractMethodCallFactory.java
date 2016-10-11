package playground.solrmarc.index.extractor.methodcall;


import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;
import playground.solrmarc.index.utils.StringReader;

import java.util.Collection;


public abstract class AbstractMethodCallFactory extends AbstractValueExtractorFactory {
    protected final MethodCallManager methodCallManager;

    public AbstractMethodCallFactory() {
        this(new MethodCallManager());
    }

    public AbstractMethodCallFactory(final MethodCallManager methodCallManager) {
        this.methodCallManager = methodCallManager;
    }

    public void addMethodsFromClasses(Collection<Class<?>> classes) {
        try {
            for (final Class<?> aClass : classes) {
                methodCallManager.add(aClass.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration) {
        MethodCallContext context = MethodCallContext.getContextForMappingConfiguration(mappingConfiguration);
        final AbstractMethodCall<?> methodCall = methodCallManager.getMethodCallForContext(context);
        if (methodCall instanceof MultiValueMethodCall) {
            return new MethodCallMultiValueExtractor((MultiValueMethodCall) methodCall, context.getParameters());
        } else if (methodCall instanceof SingleValueMethodCall) {
            return new MethodCallSingleValueExtractor((SingleValueMethodCall) methodCall, context.getParameters());
        } else {
            throw new IllegalArgumentException("Unknown method: " + context.toString() + ". Known methods are: \n" + methodCallManager.loadedMixinsToString());
        }
    }
}
