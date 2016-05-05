package playground.solrmarc.index.extractor.methodcall;

import java.util.Collection;
import java.util.Collections;

//import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.marc4j.marc.Record;

import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;

public class MethodCallSingleValueExtractor extends AbstractMultiValueExtractor
{
    private final AbstractExtractorMethodCall<String> methodCall;
    private final Object[] parameters;

    public MethodCallSingleValueExtractor(final AbstractExtractorMethodCall<String> methodCall,
            final Object[] parameters)
    {
        if (methodCall == null)
        {
            throw new NullPointerException("CustomObject is null");
        }
        else if (parameters == null)
        {
            throw new NullPointerException("Custom parameters is null");
        }

        this.methodCall = methodCall;
        this.parameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, this.parameters, 1, parameters.length);
    }

    @Override
    public Collection<String> extract(final Record record) throws Exception
    {
        Object result = methodCall.invoke(record, parameters);
        if (result == null)
            return(Collections.EMPTY_LIST);
        else
            return(Collections.singletonList((String)result));
    }
}
