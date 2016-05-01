package playground.solrmarc.index.extractor.methodcall;

import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;
import playground.solrmarc.index.utils.StringReader;

import java.util.Collection;

public abstract class AbstractMethodCallFactory extends AbstractValueExtractorFactory
{
    protected final MethodCallManager methodCallManager;

    public AbstractMethodCallFactory()
    {
        this(MethodCallManager.instance());
    }

    public AbstractMethodCallFactory(final MethodCallManager methodCallManager)
    {
        this.methodCallManager = methodCallManager;
    }

    public void addMethodsFromClasses(Collection<Class<?>> classes)
    {
        try
        {
            for (final Class<?> aClass : classes)
            {
                methodCallManager.add(aClass.newInstance());
            }
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, MethodCallContext context)
    {
        final AbstractExtractorMethodCall<?> methodCall = methodCallManager.getExtractorMethodCallForContext(context);
        if (methodCall instanceof MultiValueExtractorMethodCall)
        {
            return new MethodCallMultiValueExtractor((MultiValueExtractorMethodCall) methodCall,
                    context.getParameters());
        }
        else if (methodCall instanceof SingleValueExtractorMethodCall)
        {
            return new MethodCallSingleValueExtractor((SingleValueExtractorMethodCall) methodCall,
                    context.getParameters());
        }
        else
        {
            throw new IllegalArgumentException("Unknown method: " + context.toString() + ". Known methods are: \n"
                    + methodCallManager.loadedExtractorMixinsToString());
        }
    }

    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration)
    {
        MethodCallContext context = MethodCallContext.parseContextFromExtractorSpecification(mappingConfiguration);
        return createExtractor(solrFieldName, context);
    }
    
    @Override
    public AbstractValueExtractor<?> createExtractor(final String solrFieldName, final String[] parts)
    {
        MethodCallContext context = MethodCallContext.parseContextFromExtractorParts(parts);
        return createExtractor(solrFieldName, context);
    }
}
