package playground.solrmarc.index.extractor.methodcall;

import org.marc4j.marc.Record;

public abstract class AbstractExtractorMethodCall<T>
{
    private final String objectName;
    private final String methodName;
    private final boolean hasPerRecordInit;

    protected AbstractExtractorMethodCall(final String objectName, final String methodName, final boolean hasPerRecordInit)
    {
        this.objectName = objectName;
        this.methodName = methodName;
        this.hasPerRecordInit = hasPerRecordInit;
    }

    /**
     * The parameters[0] will be overridden with the record!
     *
     * @param record
     *            current record
     * @param parameters
     *            the parameters of this call.
     * @return the return value of this call.
     */
    public T invoke(final Record record, final Object[] parameters) throws Exception
    {
        parameters[0] = record;
        if (hasPerRecordInit && !perRecordInitCalled(new Object[]{record}))
        {
            invokePerRecordInit(new Object[]{record});
        }
        return invoke(parameters);
    }

    protected abstract boolean perRecordInitCalled(Object[] record);

    protected abstract void invokePerRecordInit(Object[] record) throws Exception;

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
