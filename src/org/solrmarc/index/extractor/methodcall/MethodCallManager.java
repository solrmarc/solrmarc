package org.solrmarc.index.extractor.methodcall;

import org.marc4j.marc.Record;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class MethodCallManager
{
    /* a static singleton manager */
    private static MethodCallManager theManager = new MethodCallManager();

    private final Map<Method, String> perRecordInitMap = new HashMap<>();
    private final Map<String, AbstractExtractorMethodCall<?>> extractorMethodCalls = new HashMap<>();
    private final Map<String, AbstractMappingMethodCall<?>> mappingMethodCalls = new HashMap<>();

    public static MethodCallManager instance()
    {
        return (theManager);
    }

    private Set<Class<?>> classes = new HashSet<>();

    /* private to prevent multiple instances from being created */
    private MethodCallManager()
    {
    }

    private boolean isPerRecordInitMethod(Method method)
    {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || !parameterTypes[0].equals(Record.class)
                || !void.class.isAssignableFrom(method.getReturnType())
                || !Modifier.isPublic(method.getModifiers()))
            return(false);
        return(true);
    }

    
    private boolean isValidExtractorMethod(Method method)
    {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0 || !parameterTypes[0].equals(Record.class)
                || (!Collection.class.isAssignableFrom(method.getReturnType())
                        && !String.class.isAssignableFrom(method.getReturnType()))
                || !Modifier.isPublic(method.getModifiers()))
        {
            return false;
        }
        for (int i = 1; i < parameterTypes.length; i++)
        {
            if (parameterTypes[i] != String.class)
            {
                return false;
            }
        }
        return true;
    }

    private boolean isValidMappingMethod(Method method)
    {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (!(parameterTypes.length > 0 && Modifier.isPublic(method.getModifiers())&& 
                ((Collection.class.isAssignableFrom(parameterTypes[0])                // its first parameter is a collction
                    && Collection.class.isAssignableFrom(method.getReturnType())) ||  // and it returns a collection 
                (parameterTypes[0].equals(String.class)                               // OR its first parameter is a String
                    && method.getReturnType().equals(String.class))                   // and it returns a String
                )))
        {
            return false;
        }
        for (int i = 1; i < parameterTypes.length; i++)
        {
            if (parameterTypes[i] != String.class)
            {
                return false;
            }
        }
        return true;
    }

    public void add(Object mixin)
    {
        final Class<?> addedParentClass = getAddedParentClass(mixin);
        Class<?> currentClass = mixin.getClass();
        boolean addAsDefault = true;
        while (currentClass != null && currentClass != Object.class)
        {
            addAsDefault &= (currentClass != addedParentClass);
            add(mixin, currentClass, addAsDefault);
            currentClass = currentClass.getSuperclass();
        }
    }

    private void add(Object mixin, Class clazz, boolean addMethodsAsDefault)
    {
        classes.add(clazz);
        Method hasPerRecordInit = null;
        for (final Method method : clazz.getDeclaredMethods())
        {
            if (isPerRecordInitMethod(method))
            {
                hasPerRecordInit = method;
                if (!perRecordInitMap.containsKey(method))
                {
                    perRecordInitMap.put(method, "");
                }
            }          
        }
        for (final Method method : clazz.getDeclaredMethods())
        {
            if (isValidExtractorMethod(method))
            {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                AbstractExtractorMethodCall<?> methodCall = null;
                if (Collection.class.isAssignableFrom(method.getReturnType()))
                {
                    methodCall = createMultiValueExtractorMethodCall(mixin, method, hasPerRecordInit, parameterTypes.length);
                }
                else if (String.class.isAssignableFrom(method.getReturnType()))
                {
                    methodCall = createSingleValueExtractorMethodCall(mixin, method, hasPerRecordInit, parameterTypes.length);
                }
                if (addMethodsAsDefault)
                {
                    // If there is already a custom method with the same name as this one replace 
                    // the default method definition with a placeholder.
                    if (extractorMethodCalls.containsKey(toCacheKey(method, parameterTypes)))
                    {
                        extractorMethodCalls.put(toCacheKey(method, parameterTypes), null);
                    }
                    else 
                    {
                        extractorMethodCalls.put(toCacheKey(method, parameterTypes), methodCall);
                    }
                }
                extractorMethodCalls.put(toCacheKey(mixin, method, parameterTypes), methodCall);
            }
            else if (isValidMappingMethod(method))
            {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                AbstractMappingMethodCall<?> methodCall = null;
                if (Collection.class.isAssignableFrom(method.getReturnType()))
                {
                    methodCall = createMultiValueMappingMethodCall(mixin, method);
                }
                else if (method.getReturnType().equals(String.class))
                {
                    methodCall = createSingleValueMappingMethodCall(mixin, method);
                }
                if (addMethodsAsDefault)
                {
                    mappingMethodCalls.put(toCacheKey(method, parameterTypes), methodCall);
                }
                mappingMethodCalls.put(toCacheKey(mixin, method, parameterTypes), methodCall);
            }
        }
    }

    protected AbstractMappingMethodCall<?> createMultiValueMappingMethodCall(Object object, Method method)
    {
        return new MultiValueMappingMethodCall(object, method);
    }

    protected AbstractMappingMethodCall<?> createSingleValueMappingMethodCall(Object object, Method method)
    {
        return new SingleValueMappingMethodCall(object, method);
    }

    protected SingleValueExtractorMethodCall createSingleValueExtractorMethodCall(Object object, Method method, Method perRecordInit, int numParameters)
    {
        return new SingleValueExtractorMethodCall(object, method, perRecordInit, numParameters);
    }

    protected MultiValueExtractorMethodCall createMultiValueExtractorMethodCall(Object object, Method method, Method perRecordInit, int numParameters)
    {
        return new MultiValueExtractorMethodCall(object, method, perRecordInit, numParameters);
    }

    /**
     * Given a mixin, this method finds a parent class of the mixin which was
     * added before.
     *
     * @param mixin
     *            the object which should be added.
     * @return previous added parent class or null, if none was added before.
     */
    private Class getAddedParentClass(Object mixin)
    {
        Class<?> mixinClass = mixin.getClass().getSuperclass();
        while (mixinClass != null)
        {
            if (classes.contains(mixinClass))
            {
                return mixinClass;
            }
            mixinClass = mixinClass.getSuperclass();
        }
        return null;
    }

    public AbstractExtractorMethodCall<?> getExtractorMethodCallForContext(MethodCallContext context)
    {
        final String key = toCacheKey(context.getObjectName(), context.getMethodName(), context.getParameterTypes());
        return extractorMethodCalls.get(key);
    }

    public AbstractMappingMethodCall<?> getMappingMethodCallForContext(MethodCallContext context)
    {
        final String key = toCacheKey(context.getObjectName(), context.getMethodName(), context.getParameterTypes());
        return mappingMethodCalls.get(key);
    }

    private String toCacheKey(Object mixin, Method method, Class<?>... pameterTypes)
    {
        return toCacheKey(mixin.getClass().getName(), method.getName(), pameterTypes);
    }

    private String toCacheKey(Method method, Class<?>... parameterTypes)
    {
        return toCacheKey(null, method.getName(), parameterTypes);
    }

    private String toCacheKey(String className, String methodName, Class<?>... parameterTypes)
    {
        return className + ';' + methodName + ';' + parameterTypes.length;
    }

    public String loadedExtractorMixinsToString()
    {
        // get all Extractor Mixins and return them in a printable string
        return loadedExtractorMixinsToString(getLoadedExtractorMixinsMatches(null, null, -1));
    }
    
    public List<AbstractExtractorMethodCall<?>> getLoadedExtractorMixinsMatches(String classNameToMatch, String methodNameToMatch, int numParameters)
    {
        List<AbstractExtractorMethodCall<?>> result = new ArrayList<>();
        for (final String key : extractorMethodCalls.keySet())
        {
            // Method calls are added twice. Once with a class name, once with
            // 'null' as class name.
            // It doesn't matter which one we take, but we don't want to show
            // both entries.
            if (!key.startsWith("null"))
            {
                final AbstractExtractorMethodCall<?> call = extractorMethodCalls.get(key);
                if (classNameToMatch == null || classNameToMatch.equals(call.getObjectName()))
                {
                    if (methodNameToMatch == null || methodNameToMatch.equals(call.getMethodName()))
                    {
                        if (numParameters == -1 || numParameters == call.getNumParameters())
                        {
                            result.add(call);
                        }
                    }
                }
            }
        }
        return(result);
    }
    
    public String loadedExtractorMixinsToString(List<AbstractExtractorMethodCall<?>> matches)
    {
        List<String> lines = new ArrayList<>(matches.size());
        for (final AbstractExtractorMethodCall<?> call : matches)
        {
            lines.add("- " + call.getObjectName() + "::" + call.getMethodName());
        }
        Collections.sort(lines);
        final StringBuilder buffer = new StringBuilder();
        for (final String line : lines)
        {
            buffer.append(line).append('\n');
        }
        return buffer.toString();
    }

//    public String loadedExtractorMixinsToString(String methodNameToMatch)
//    {
//        List<String> lines = new ArrayList<>(extractorMethodCalls.size());
//        for (final String key : extractorMethodCalls.keySet())
//        {
//            // Method calls are added twice. Once with a class name, once with
//            // 'null' as class name.
//            // It doesn't matter which one we take, but we don't want to show
//            // both entries.
//            if (!key.startsWith("null"))
//            {
//                final AbstractExtractorMethodCall<?> call = extractorMethodCalls.get(key);
//                if (methodNameToMatch == null || methodNameToMatch.equals(call.getMethodName()))
//                {
//                    lines.add("- " + call.getObjectName() + "::" + call.getMethodName());
//                }
//            }
//        }
//        Collections.sort(lines);
//        final StringBuilder buffer = new StringBuilder();
//        for (final String line : lines)
//        {
//            buffer.append(line).append('\n');
//        }
//        return buffer.toString();
//    }

    public String loadedMappingMixinsToString()
    {
        List<String> lines = new ArrayList<>(mappingMethodCalls.size());
        for (final String key : mappingMethodCalls.keySet())
        {
            // Method calls are added twice. Once with a class name, once with
            // 'null' as class name.
            // It doesn't matter which one we take, but we don't want to show
            // both entries.
            if (!key.startsWith("null"))
            {
                final AbstractMappingMethodCall<?> call = mappingMethodCalls.get(key);
                lines.add("- " + call.getObjectName() + "::" + call.getMethodName());
            }
        }
        Collections.sort(lines);
        final StringBuilder buffer = new StringBuilder();
        for (final String line : lines)
        {
            buffer.append(line).append('\n');
        }
        return buffer.toString().trim();
    }

//    public final String getRecordLastCalledFor(Method perRecordInit)
//    {
//        final String result = perRecordInitMap.get(perRecordInit);
//        return(result);
//    }
    
    public final boolean alreadyCalledFor(Method perRecordInit, Object record)
    {
        String recId = "";
        if (record instanceof Record)
        {
            Record rec = (Record)record;
            recId = rec.getControlNumber();
        }
        final String result = perRecordInitMap.get(perRecordInit);
        if (!result.equals(recId))
        {
            perRecordInitMap.put(perRecordInit, recId);
            return(false);
        }
        return(true);
    }

    
//    public final void setRecordLastCalledFor(Method perRecordInit, String recordID)
//    {
//        perRecordInitMap.put(perRecordInit, recordID);
//    }
}
