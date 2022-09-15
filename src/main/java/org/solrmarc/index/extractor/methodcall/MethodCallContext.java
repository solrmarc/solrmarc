package org.solrmarc.index.extractor.methodcall;

import org.marc4j.marc.Record;

import java.util.Arrays;

public class MethodCallContext
{
    private String typeName;
    private String objectName;
    private String methodName;
    private String[] parameters;
    private Class<?>[] parameterTypes;

    protected MethodCallContext(final String typeName, final String objectName, final String methodName,
            final String[] parameters, final Class<?>[] parameterTypes)
    {
        this.typeName = typeName;
        this.objectName = objectName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
    }

    public static MethodCallContext parseContextFromExtractorParts(String[] extParts)
    {
        final String typeName = extParts[0];
        final String objectName = extParts[1];
      //  mappingConfiguration.skipUntilAfter(',');
        final String methodName = extParts[2];
        final String[] parameters = getParameters(extParts, 3);
        final Class<?>[] parameterTypes = getExtractorParameterTypes(parameters);
        return new MethodCallContext(typeName, objectName, methodName, parameters, parameterTypes);
    }

    public static MethodCallContext parseContextFromMapParts(String[] mapParts)
    {
        final String typeName = mapParts[0];
        final String objectName = mapParts[1];
        // mappingConfiguration.skipUntilAfter(' ');
        final String methodName = mapParts[2];
        final String[] parameters = getParameters(mapParts, 3);
        final Class<?>[] parameterTypes = getMappingParameterTypes(parameters);
        return new MethodCallContext(typeName, objectName, methodName, parameters, parameterTypes);
    }

    protected static Class<?>[] getExtractorParameterTypes(final String[] parameters)
    {
        final Class<?>[] parameterTypes = new Class[parameters.length + 1];
        parameterTypes[0] = Record.class;
        Arrays.fill(parameterTypes, 1, parameters.length + 1, String.class);
        return parameterTypes;
    }

    protected static Class<?>[] getMappingParameterTypes(final String[] parameters)
    {
        final Class<?>[] parameterTypes = new Class[parameters.length + 1];
        parameterTypes[0] = Object.class;
        Arrays.fill(parameterTypes, 1, parameters.length + 1, String.class);
        return parameterTypes;
    }

    private static String[] getParameters(String[] mapParts, int numToDiscard)
    {
        if (mapParts.length <= numToDiscard)
        {
            return new String[0];
        }
        else
        {
            String result[] = new String[mapParts.length - numToDiscard];
            System.arraycopy(mapParts, numToDiscard, result, 0, mapParts.length - numToDiscard);
            return result;
        }
    }

    public String getTypeName()
    {
        return typeName;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public String[] getParameters()
    {
        return parameters;
    }

    public Class<?>[] getParameterTypes()
    {
        return parameterTypes;
    }

    @Override
    public String toString()
    {
        return objectName + "." + methodName + Arrays.toString(parameterTypes);
    }

}
