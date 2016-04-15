package playground.solrmarc.index.extractor.methodcall;


import java.lang.reflect.Method;


public class SingleValueMethodCall extends AbstractMethodCall<String> {
    private final Object mixin;
    private final Method method;

    public SingleValueMethodCall(final Object mixin, final Method method) {
        super(mixin.getClass().getSimpleName(), method.getName());
        this.mixin = mixin;
        this.method = method;

        if (!String.class.isAssignableFrom(this.method.getReturnType())) {
            throw new IllegalArgumentException("The method's return type has to be assignable to String:\nMixin class:  " + mixin.getClass().getName() + "\nMixin method: " + method.toString());
        }
    }

    @Override
    public String invoke(final Object[] parameters) throws Exception {
        return (String) method.invoke(mixin, parameters);
    }
}
