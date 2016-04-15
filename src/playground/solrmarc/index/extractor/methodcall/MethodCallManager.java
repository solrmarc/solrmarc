package playground.solrmarc.index.extractor.methodcall;


import org.marc4j.marc.Record;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


public class MethodCallManager {
    private final Map<String, AbstractMethodCall<?>> methodCalls = new HashMap<>();

    private Set<Class<?>> classes = new HashSet<>();

    private boolean isValidMixinMethod(Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0
                || !parameterTypes[0].equals(Record.class)
                || (!Collection.class.isAssignableFrom(method.getReturnType()) && !String.class.isAssignableFrom(method.getReturnType()))
                || !Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        for (int i = 1; i < parameterTypes.length; i++) {
            if (parameterTypes[i] != String.class) {
                return false;
            }
        }
        return true;
    }

    public void add(Object mixin) {
        final Class<?> addedParentClass = getAddedParentClass(mixin);
        Class<?> currentClass = mixin.getClass();
        boolean addAsDefault = true;
        while (currentClass != null && currentClass != Object.class) {
            addAsDefault &= (currentClass != addedParentClass);
            add(mixin, currentClass, addAsDefault);
            currentClass = currentClass.getSuperclass();
        }
    }

    private void add(Object mixin, Class clazz, boolean addMethodsAsDefault) {
        classes.add(clazz);
        for (final Method method : clazz.getDeclaredMethods()) {
            if (isValidMixinMethod(method)) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                AbstractMethodCall<?> methodCall = null;
                if (Collection.class.isAssignableFrom(method.getReturnType())) {
                    methodCall = createMultiValueMethodCall(mixin, method);
                } else if (String.class.isAssignableFrom(method.getReturnType())) {
                    methodCall = createSingleValueMethodCall(mixin, method);
                }
                if (addMethodsAsDefault) {
                    methodCalls.put(toCacheKey(method, parameterTypes), methodCall);
                }
                methodCalls.put(toCacheKey(mixin, method, parameterTypes), methodCall);
            }
        }
    }

    protected SingleValueMethodCall createSingleValueMethodCall(Object object, Method method) {
        return new SingleValueMethodCall(object, method);
    }

    protected MultiValueMethodCall createMultiValueMethodCall(Object object, Method method) {
        return new MultiValueMethodCall(object, method);
    }

    /**
     * Given a mixin, this method finds a parent class of the mixin
     * which was added before.
     *
     * @param mixin the object which should be added.
     * @return previous added parent class or null, if none was added before.
     */
    private Class getAddedParentClass(Object mixin) {
        Class<?> mixinClass = mixin.getClass().getSuperclass();
        while (mixinClass != null) {
            if (classes.contains(mixinClass)) {
                return mixinClass;
            }
            mixinClass = mixinClass.getSuperclass();
        }
        return null;
    }

    public AbstractMethodCall<?> getMethodCallForContext(MethodCallContext context) {
        final String key = toCacheKey(context.getObjectName(), context.getMethodName(), context.getParameterTypes());
        return methodCalls.get(key);
    }

    private String toCacheKey(Object mixin, Method method, Class<?>... pameterTypes) {
        return toCacheKey(mixin.getClass().getName(), method.getName(), pameterTypes);
    }

    private String toCacheKey(Method method, Class<?>... parameterTypes) {
        return toCacheKey(null, method.getName(), parameterTypes);
    }

    private String toCacheKey(String className, String methodName, Class<?>... parameterTypes) {
        return className + ';' + methodName + ';' + parameterTypes.length;
    }

    public String loadedMixinsToString() {
        List<String> lines = new ArrayList<>(methodCalls.size());
        for (final String key : methodCalls.keySet()) {
            // Method calls are added twice. Once with a class name, once with 'null' as class name.
            // It doesn't matter which one we take, but we don't want to show both entries.
            if (!key.startsWith("null")) {
                final AbstractMethodCall<?> call = methodCalls.get(key);
                lines.add("- " + call.getObjectName() + "::" + call.getMethodName());
            }
        }
        Collections.sort(lines);
        final StringBuilder buffer = new StringBuilder();
        for (final String line : lines) {
            buffer.append(line).append('\n');
        }
        return buffer.toString().trim();
    }
}
