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
        try { 
            extract(StaticMarcTestRecords.testRecord[0]);
        }
        catch (InvocationTargetException ite)
        {
            throw new IndexerSpecException(ite.getTargetException(), "Error on test invocation of custom method: " + methodCall.getMethodName());
        }
        catch (Exception e)
        {
            throw new IndexerSpecException(e, "Error on test invocation of custom method: " + methodCall.getMethodName());
        }

    }

    @Override
    public Collection<String> extract(final Record record) throws Exception
    {
        return methodCall.invoke(record, parameters);
    }
}
