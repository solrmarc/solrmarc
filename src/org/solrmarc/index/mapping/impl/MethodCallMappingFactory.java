package org.solrmarc.index.mapping.impl;


import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final static Pattern mapShortcut1 = Pattern.compile("(map|filter)[A-Za-z0-9]+");
    private final static Pattern mapShortcut2 = Pattern.compile("(([a-z]+[.])*[A-Z][A-Za-z0-9_]*)::((map|filter)[A-Za-z0-9]+)");

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
        return (mappingConfiguration.startsWith("custom_map") || 
                mapShortcut1.matcher(mappingConfiguration).matches() ||
                mapShortcut2.matcher(mappingConfiguration).matches());
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
        MethodCallContext context;
        Matcher shortcut1 = mapShortcut1.matcher(mapParts[0]);
        Matcher shortcut2 = mapShortcut2.matcher(mapParts[0]);
        if (shortcut1.matches())
        {
            String[] extraParts = new String[mapParts.length+2];
            extraParts[0] = "custom_map";
            extraParts[1] = null;
            System.arraycopy(mapParts, 0, extraParts, 2, mapParts.length);
            context = MethodCallContext.parseContextFromMapParts(extraParts);
        }
        else if (shortcut2.matches())
        {
            String[] extraParts = new String[mapParts.length+2];
            extraParts[0] = "custom_map";
            extraParts[1] = shortcut2.group(1);
            extraParts[2] = shortcut2.group(3);
            System.arraycopy(mapParts, 1, extraParts, 3, mapParts.length-1);
            context = MethodCallContext.parseContextFromMapParts(extraParts);
        }
        else
        {
            context = MethodCallContext.parseContextFromMapParts(mapParts);
        }
        return createMultiValueMapping(context);
    }
}
