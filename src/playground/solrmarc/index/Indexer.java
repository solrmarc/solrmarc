package playground.solrmarc.index;

import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.index.indexer.IndexerSpecException;
import playground.solrmarc.index.indexer.ValueIndexerFactory;
import playground.solrmarc.solr.SolrProxy;
import playground.solrmarc.solr.SolrRuntimeException;

import org.apache.solr.common.SolrInputDocument;
import org.marc4j.MarcError;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Indexer
{
//    public class RecordAndResult { 
//        final Record record;
//        final SolrInputDocument solrDoc;
//        public RecordAndResult(Record rec, SolrInputDocument solrDoc)
//        {
//            this.record = rec;
//            this.solrDoc = solrDoc;
//        }
//    };
    
    protected final List<AbstractValueIndexer<?>> indexers;
    protected SolrProxy solrProxy;
    public EnumSet<eErrorHandleVal> errHandle = EnumSet.noneOf(eErrorHandleVal.class);
    protected final BlockingQueue<AbstractMap.SimpleEntry<Record, SolrInputDocument>> errQ;

    public enum eErrorHandleVal
    {
        RETURN_ERROR_RECORDS, INDEX_ERROR_RECORDS;
    };
    
    public Indexer(final List<AbstractValueIndexer<?>> indexers, final SolrProxy solrProxy)
    {
        this.indexers = indexers;
        this.solrProxy = solrProxy;
        errQ = new LinkedBlockingQueue<AbstractMap.SimpleEntry<Record, SolrInputDocument>>();
    }

    public boolean isSet(eErrorHandleVal val)
    {
        return(errHandle.contains(val));
    }
    
    public void setErr(eErrorHandleVal val)
    {
        errHandle.add(val);
    }
    
//    public int index(final MarcReader reader) throws Exception
//    {
//        int cnt = 0;
//        while (reader.hasNext())
//        {
//            final Record record = reader.next();
//            final Map<String, Object> document = index(record);
//            solrProxy.addDoc(document, false, true);
//            cnt++;
//        }
//        return(cnt);
//    }
    
    /**
     * indexToSolr  - Reads in a MARC Record, produces SolrInputDocument for it, sends that document to solr
     *                This is the single threaded version that does each of those action sequentially
     * @param reader
     * @return
     * @throws Exception
     */
    public int[] indexToSolr(final MarcReader reader) throws Exception
    {
        int cnts[] = new int[] { 0, 0, 0 };
        while (reader.hasNext())
        {
            final Record record = reader.next();
            cnts[0]++;
 //           ErrorHandler errors = MarcReaderFactory.instance().getErrors();
            SolrInputDocument document = null;
            try {
                document = indexToSolrDoc(record);
                cnts[1]++;
            }
            catch (Exception e)
            {
                if (isSet(eErrorHandleVal.RETURN_ERROR_RECORDS))
                {
                    errQ.add(new AbstractMap.SimpleEntry<Record, SolrInputDocument>(record, null));
                }               
            }
            try { 
                if (document != null)
                {
                    solrProxy.addDoc(document);
                    cnts[2]++;
                }
            }
            catch (SolrRuntimeException sse)
            {
                if (isSet(eErrorHandleVal.RETURN_ERROR_RECORDS))
                {
                    errQ.add(new AbstractMap.SimpleEntry<Record, SolrInputDocument>(record, document));
                }
            }
        }
        return(cnts);
    }
    
    private void addExceptionsToMap(SolrInputDocument document, List<IndexerSpecException> perRecordExceptions)
    {
        if (perRecordExceptions != null)
        {
            for (IndexerSpecException e : perRecordExceptions)
            {
      //          if (e.getSolrField() == null) e.setSolrFieldAndSpec("marc_error", null);
                String specMessage = e.getSpecMessage();
    //            if (!specMessage.equals(lastSpec))
    //            {
    //                text.append(specMessage);
    //            }
    //            lastSpec = specMessage;
                document.addField("marc_error", e.getMessage());
                for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
                {
                    document.addField("marc_error", e.getSolrField()+ " : " + cause.getMessage());
                }
            }
        }
    }

//    if (errors.hasErrors())
//    {
//        addErrorsToMap(fieldsMap, errors);
//    }

    
    private void addErrorsToMap(SolrInputDocument document, List<MarcError> errors)
    {
        for (Object err : errors)
        {
            document.addField("marc_error", err.toString());
        }
    }

    private Map<String, Object> index(final Record record) throws Exception 
    {
        final Map<String, Object> document = new HashMap<>();
        for (final AbstractValueIndexer<?> indexer : indexers)
        {
            try { 
                final Collection<String> data = indexer.getFieldData(record);
                for (String fieldName : indexer.getSolrFieldNames())
                {
                    if (data.size() == 0)
                    {
                        /* do_nothing() */
                    }
                    else if (data.size() == 1)
                    {
                        document.put(fieldName, data.iterator().next());
                    }
                    else
                    {
                        for (String dataVal : data)
                        {
                            document.put(fieldName, dataVal);
                        }
                    }
                }
            }
            catch (InvocationTargetException ioe)
            {
                Throwable wrapped = ioe.getTargetException();
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    document.put("marc_error", indexer.getSolrFieldNames().toString() + wrapped.getMessage());
                else throw(ioe);
            }
            catch (IllegalArgumentException e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
            catch (IndexerSpecException e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
            catch (Exception e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
        }
        return document;
    }
    
    protected SolrInputDocument indexToSolrDoc(final Record record) throws Exception
    {
        SolrInputDocument inputDoc = new SolrInputDocument();
        ValueIndexerFactory.instance().clearPerRecordErrors();
        for (final AbstractValueIndexer<?> indexer : indexers)
        {
            try { 
                final Collection<String> data = indexer.getFieldData(record);

                for (String fieldName : indexer.getSolrFieldNames())
                {
                    if (data.size() == 0)
                    {
                        /* do_nothing() */
                    }
                    else
                    {
                        for (String dataVal : data)
                        {
                            inputDoc.addField(fieldName, dataVal, 1.0f );
                        }
                    }
                }
            }
            catch (IllegalArgumentException e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    inputDoc.addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
            catch (IndexerSpecException e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    inputDoc.addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
            catch (InvocationTargetException ioe)
            {
                Throwable wrapped = ioe.getTargetException();
                Exception wrappedE = (wrapped instanceof Exception) ? (Exception)wrapped : null;
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                {
                    inputDoc.addField("marc_error", indexer.getSolrFieldNames().toString() + wrapped.getMessage());
                }
                else if (wrappedE != null)
                {
                    throw(wrappedE);
                }
                else
                {
                    throw(ioe);
                }
            }
            catch (Exception e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    inputDoc.addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
        }
        if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
        {
            if (record.hasErrors() )
            {
                addErrorsToMap(inputDoc, record.getErrors());
            }
            List<IndexerSpecException> perRecordExceptions = ValueIndexerFactory.instance().getPerRecordErrors();
            addExceptionsToMap(inputDoc, perRecordExceptions);
        }

        return inputDoc;
    }
}
