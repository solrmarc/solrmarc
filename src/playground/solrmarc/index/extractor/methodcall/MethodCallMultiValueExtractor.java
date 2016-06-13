package playground.solrmarc.index.extractor.methodcall;

import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import playground.solrmarc.index.indexer.IndexerSpecException;

import org.marc4j.marc.Record;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class MethodCallMultiValueExtractor extends AbstractMultiValueExtractor
{
    private final AbstractExtractorMethodCall<Collection<String>> methodCall;
    private final Object[] parameters;

    public MethodCallMultiValueExtractor(final AbstractExtractorMethodCall<Collection<String>> methodCall,
            final Object[] staticParameters)
    {
        if (methodCall == null)
        {
            throw new NullPointerException("CustomObject is null");
        }
        else if (staticParameters == null)
        {
            throw new NullPointerException("Custom parameters is null");
        }

        this.methodCall = methodCall;
        this.parameters = new Object[staticParameters.length + 1];
        System.arraycopy(staticParameters, 0, this.parameters, 1, staticParameters.length);
    }

    @Override
    public Collection<String> extract(final Record record) throws Exception
    {
        return methodCall.invoke(record, parameters);
    }
}
