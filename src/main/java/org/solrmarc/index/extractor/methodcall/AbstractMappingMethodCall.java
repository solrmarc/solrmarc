package org.solrmarc.index.extractor.methodcall;

public abstract class AbstractMappingMethodCall<T>
{
    private final String objectName;
    private final String methodName;

    protected AbstractMappingMethodCall(final String objectName, final String methodName)
    {
        this.objectName = objectName;
        this.methodName = methodName;
    }

    /**
     * The parameters[0] will be overridden with the record!
     *
     * @param incoming
     *            current record
     * @param parameters
     *            the parameters of this call.
     * @return the return value of this call.
     * @throws Exception
     *            in case of error
     */
    public T invoke(final T incoming, final Object[] parameters) throws Exception
    {
        parameters[0] = incoming;
        return invoke(parameters);
    }

    /**
     *
     * @param parameters  the parameters of this call
     * @return            the value of this call
     * @throws Exception  when runtime error
     */
    public abstract T invoke(final Object[] parameters) throws Exception;

    public String getObjectName()
    {
        return objectName;
    }

    public String getMethodName()
    {
        return methodName;
    }
}
