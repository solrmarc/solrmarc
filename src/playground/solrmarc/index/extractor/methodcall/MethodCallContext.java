package playground.solrmarc.index.extractor.methodcall;

import playground.solrmarc.index.utils.StringReader;
import org.marc4j.marc.Record;
import playground.solrmarc.tools.Utils;

import java.util.Arrays;

public class MethodCallContext {
    private String typeName;
    private String objectName;
    private String methodName;
    private String[] parameters;
    private Class<?>[] parameterTypes;

    protected MethodCallContext(final String typeName, final String objectName, final String methodName, final String[] parameters, final Class<?>[] parameterTypes) {
        this.typeName = typeName;
        this.objectName = objectName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
    }

    public static MethodCallContext getContextForMappingConfiguration(StringReader mappingConfiguration) {
        final String typeName = getTypeName(mappingConfiguration);
        final String objectName = getObjectName(mappingConfiguration);
        mappingConfiguration.skipUntilAfter(',');
        final String methodName = getMethodName(mappingConfiguration);
        final String[] parameters = getParameters(mappingConfiguration);
        final Class<?>[] parameterTypes = getParameterTypes(parameters);
        return new MethodCallContext(typeName, objectName, methodName, parameters, parameterTypes);
    }

    protected static String getTypeName(final StringReader mappingConfiguration) {
        final int index = mappingConfiguration.indexOfFirst('(', ',');
        if (index == -1) {
            throw new IllegalArgumentException("Syntax error: missing open parenthesis or comma in " + mappingConfiguration.toString());
        }
        return mappingConfiguration.readString(index);
    }

    protected static String getObjectName(final StringReader mappingConfiguration) {
        if (mappingConfiguration.charAt(0) == '(') {
            mappingConfiguration.skip(1);
            final String mixinName = mappingConfiguration.readStringUntil(')').trim();
            mappingConfiguration.skip(1); // skip the closing parenthesis.
            return mixinName;
        }
        return null;
    }

    protected static String getMethodName(final StringReader mappingConfiguration) {
        final int index = mappingConfiguration.indexOfFirst('(', ',');
        if (index < 0) {
            return mappingConfiguration.readAll().trim();
        } else {
            return mappingConfiguration.readString(index).trim();
        }
    }

    protected static Class[] getParameterTypes(final String[] parameters) {
        final Class[] parameterTypes = new Class[parameters.length + 1];
        parameterTypes[0] = Record.class;
        Arrays.fill(parameterTypes, 1, parameters.length + 1, String.class);
        return parameterTypes;
    }

    protected static String[] getParameters(final StringReader mappingConfiguration) {
        final int indexOpenParenthesis = mappingConfiguration.indexOf('(');
        if (indexOpenParenthesis <= -1) {
            return new String[0];
        }
        final int indexCloseParenthesis = mappingConfiguration.indexOf(')');
        final String parametersString = mappingConfiguration.readString(indexOpenParenthesis + 1, indexCloseParenthesis).trim();
        if (parametersString.isEmpty()) {
            return new String[0];
        }
        mappingConfiguration.skipUntilAfter(')');
        String[] parameters = parametersString.split("\\s*(?<=[^\\\\]),\\s*");
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = Utils.cleanData(parameters[i]).replaceAll("^\"(.*)\"$", "$1");
        }
        return parameters;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameters() {
        return parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public String toString() {
        return objectName + "." + methodName + Arrays.toString(parameterTypes);
    }
}
