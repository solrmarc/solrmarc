package org.solrmarc.index.mapping.impl;


import java.util.Collection;

import org.solrmarc.index.extractor.methodcall.AbstractMappingMethodCall;
import org.solrmarc.index.extractor.methodcall.MethodCallContext;
import org.solrmarc.index.extractor.methodcall.MethodCallManager;
import org.solrmarc.index.extractor.methodcall.MultiValueMappingMethodCall;
import org.solrmarc.index.extractor.methodcall.SingleValueMappingMethodCall;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.AbstractValueMappingFactory;

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
        else if (methodCall instanceof SingleValueMappingMethodCall)
        {
            return new MethodCallSingleValueMapping((SingleValueMappingMethodCall) methodCall, context.getParameters());
        }
        else
        {
            throw new IndexerSpecException("Unknown custom mapping method: " + context.toString()
                    + " -- Known methods are: \n" + methodCallManager.loadedMappingMixinsToString());
        }
    }

    @Override
    public AbstractMultiValueMapping createMultiValueMapping(String mappingConfiguration)
    {
        // method unused, but must be present
        return null;
    }

    @Override
    public AbstractMultiValueMapping createMultiValueMapping(String[] mapParts)
    {
        MethodCallContext context = MethodCallContext.parseContextFromMapParts(mapParts);
        return createMultiValueMapping(context);
    }

}
