package playground.solrmarc.index.mapping.impl;

import playground.solrmarc.index.extractor.methodcall.AbstractMappingMethodCall;
import playground.solrmarc.index.extractor.methodcall.MethodCallContext;
import playground.solrmarc.index.extractor.methodcall.MethodCallManager;
import playground.solrmarc.index.extractor.methodcall.MultiValueMappingMethodCall;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;
import playground.solrmarc.index.mapping.AbstractValueMappingFactory;
import playground.solrmarc.index.utils.StringReader;

import java.util.Collection;

public class MethodCallMappingFactory extends AbstractValueMappingFactory
{
    protected final MethodCallManager methodCallManager;

    public MethodCallMappingFactory()
    {
        this(MethodCallManager.instance());
    }

    public MethodCallMappingFactory(final MethodCallManager methodCallManager)
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

    @Override
    public boolean canHandle(String mappingConfiguration)
    {
        return (mappingConfiguration.startsWith("custom_map"));
    }

    private AbstractMultiValueMapping createMultiValueMapping(MethodCallContext context)
    {
        final AbstractMappingMethodCall<?> methodCall = methodCallManager.getMappingMethodCallForContext(context);
        if (methodCall instanceof MultiValueMappingMethodCall)
        {
            return new MethodCallMultiValueMapping((MultiValueMappingMethodCall) methodCall, context.getParameters());
        }
        else
        {
            throw new IllegalArgumentException("Unknown mapping method: " + context.toString()
                    + ". Known methods are: \n" + methodCallManager.loadedExtractorMixinsToString());
        }
    }

    @Override
    public AbstractMultiValueMapping createMultiValueMapping(String mappingConfiguration)
    {
        MethodCallContext context = MethodCallContext
                .parseContextFromMappingSpecification(new StringReader(mappingConfiguration));
        return createMultiValueMapping(context);
    }

    @Override
    public AbstractMultiValueMapping createMultiValueMapping(String[] mapParts)
    {
        MethodCallContext context = MethodCallContext.parseContextFromMapParts(mapParts);
        return createMultiValueMapping(context);
    }

}
