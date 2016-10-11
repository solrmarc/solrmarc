package playground.solrmarc.index.extractor.methodcall;

import org.marc4j.marc.Record;

public abstract class AbstractMethodCall<T> {
    private final String objectName;
    private final String methodName;

    protected AbstractMethodCall(final String objectName, final String methodName) {
        this.objectName = objectName;
        this.methodName = methodName;
    }

    /**
     * The parameters[0] will be overridden with the record!
     *
     * @param record     current record
     * @param parameters the parameters of this call.
     * @return the return value of this call.
     */
    public T invoke(final Record record, final Object[] parameters) throws Exception {
        parameters[0] = record;
        return invoke(parameters);
    }

    public abstract T invoke(final Object[] parameters) throws Exception;

    public String getObjectName() {
        return objectName;
    }

    public String getMethodName() {
        return methodName;
    }
}

