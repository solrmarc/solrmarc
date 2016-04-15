package playground.solrmarc.index.extractor.methodcall;


import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.marc4j.marc.Record;


public class MethodCallSingleValueExtractor implements AbstractSingleValueExtractor {
    private final AbstractMethodCall<String> methodCall;
    private final Object[] parameters;

    public MethodCallSingleValueExtractor(final AbstractMethodCall<String> methodCall, final Object[] parameters) {
        if (methodCall == null) {
            throw new NullPointerException("CustomObject is null");
        } else if (parameters == null) {
            throw new NullPointerException("Custom parameters is null");
        }

        this.methodCall = methodCall;
        this.parameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, this.parameters, 1, parameters.length);
    }

    @Override
    public String extract(final Record record) throws Exception {
        return methodCall.invoke(record, parameters);
    }
}
