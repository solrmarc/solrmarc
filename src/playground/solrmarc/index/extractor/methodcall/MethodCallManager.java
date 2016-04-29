package playground.solrmarc.index.extractor.methodcall;

import org.marc4j.marc.Record;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class MethodCallManager
{
    /* a static singleton manager */
    private static MethodCallManager theManager = new MethodCallManager();

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
        if (parameterTypes.length == 0 || !Collection.class.isAssignableFrom(parameterTypes[0])
                || !Collection.class.isAssignableFrom(method.getReturnType())
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
        for (final Method method : clazz.getDeclaredMethods())
        {
            if (isValidExtractorMethod(method))
            {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                AbstractExtractorMethodCall<?> methodCall = null;
                if (Collection.class.isAssignableFrom(method.getReturnType()))
                {
                    methodCall = createMultiValueExtractorMethodCall(mixin, method);
                }
                else if (String.class.isAssignableFrom(method.getReturnType()))
                {
                    methodCall = createSingleValueExtractorMethodCall(mixin, method);
                }
                if (addMethodsAsDefault)
                {
                    extractorMethodCalls.put(toCacheKey(method, parameterTypes), methodCall);
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

    protected SingleValueExtractorMethodCall createSingleValueExtractorMethodCall(Object object, Method method)
    {
        return new SingleValueExtractorMethodCall(object, method);
    }

    protected MultiValueExtractorMethodCall createMultiValueExtractorMethodCall(Object object, Method method)
    {
        return new MultiValueExtractorMethodCall(object, method);
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
        List<String> lines = new ArrayList<>(extractorMethodCalls.size());
        for (final String key : extractorMethodCalls.keySet())
        {
            // Method calls are added twice. Once with a class name, once with
            // 'null' as class name.
            // It doesn't matter which one we take, but we don't want to show
            // both entries.
            if (!key.startsWith("null"))
            {
                final AbstractExtractorMethodCall<?> call = extractorMethodCalls.get(key);
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
}
