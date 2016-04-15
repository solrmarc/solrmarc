package playground.solrmarc.index.extractor.methodcall;


import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.marc4j.marc.Record;

import java.util.Collection;


public class MethodCallMultiValueExtractor implements AbstractMultiValueExtractor {
    private final AbstractMethodCall<Collection<String>> methodCall;
    private final Object[] parameters;

    public MethodCallMultiValueExtractor(final AbstractMethodCall<Collection<String>> methodCall, final Object[] parameters) {
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
    public Collection<String> extract(final Record record) throws Exception {
        return methodCall.invoke(record, parameters);
    }
}
