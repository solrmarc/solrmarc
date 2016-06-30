package org.solrmarc.index.driver;


import org.apache.log4j.Logger;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.MarcError;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.driver.Indexer.eErrorHandleVal;
import org.solrmarc.index.driver.RecordAndDocError.eErrorLocationVal;
import org.solrmarc.index.driver.RecordAndDocError.eErrorSeverity;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrRuntimeException;

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
    private final static Logger logger = Logger.getLogger(Indexer.class);
    
    protected final List<AbstractValueIndexer<?>> indexers;
    protected SolrProxy solrProxy;
    public EnumSet<eErrorHandleVal> errHandle = EnumSet.noneOf(eErrorHandleVal.class);
    protected final BlockingQueue<RecordAndDocError> errQ;

    public enum eErrorHandleVal
    {
        RETURN_ERROR_RECORDS, INDEX_ERROR_RECORDS;
    };
    
    public Indexer(final List<AbstractValueIndexer<?>> indexers, final SolrProxy solrProxy)
    {
        this.indexers = indexers;
        this.solrProxy = solrProxy;
        errQ = new LinkedBlockingQueue<RecordAndDocError>();
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
            SolrInputDocument[] documentParts = null;
            documentParts = indexToSolrDoc(record);
            cnts[1]++;
            SolrInputDocument document = combineDocWithErrors(documentParts, isSet(eErrorHandleVal.INDEX_ERROR_RECORDS));
            EnumSet<eErrorLocationVal> errLocVals = getErrLocVals(documentParts);
            if (!documentParts[1].isEmpty() || !documentParts[2].isEmpty()) 
            {
                if (isSet(eErrorHandleVal.RETURN_ERROR_RECORDS) && !isSet(eErrorHandleVal.INDEX_ERROR_RECORDS))
                {
                    errQ.add(new RecordAndDocError(record, document, errLocVals, eErrorSeverity.ERROR));
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
                singleRecordSolrError(new RecordAndDocError(record, document, errLocVals, eErrorSeverity.ERROR), sse, errQ);
            }
        }
        return(cnts);
    }
    
    protected EnumSet<eErrorLocationVal> getErrLocVals(SolrInputDocument[] documentParts)
    {
        EnumSet<eErrorLocationVal> eLocVals = EnumSet.noneOf(eErrorLocationVal.class);
        if (!documentParts[2].isEmpty()) eLocVals.add(eErrorLocationVal.INDEXING_ERROR);
        if (!documentParts[1].isEmpty()) eLocVals.add(eErrorLocationVal.MARC_ERROR);
        return eLocVals;
    }

    protected SolrInputDocument combineDocWithErrors(SolrInputDocument[] documentParts, boolean includeErrors)
    {
        SolrInputDocument result = documentParts[0];
        if (includeErrors)
        {
            if (!documentParts[1].isEmpty()) result.putAll(documentParts[1]);
            if (!documentParts[2].isEmpty()) result.putAll(documentParts[2]);
        }
        return result;
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

    
    private void addMarcErrorsToMap(SolrInputDocument document, List<MarcError> errors)
    {
        for (Object err : errors)
        {
            document.addField("marc_error", err.toString());
        }
    }

//    private Map<String, Object> index(final Record record) throws Exception 
//    {
//        final Map<String, Object> document = new HashMap<>();
//        for (final AbstractValueIndexer<?> indexer : indexers)
//        {
//            try { 
//                final Collection<String> data = indexer.getFieldData(record);
//                for (String fieldName : indexer.getSolrFieldNames())
//                {
//                    if (data.size() == 0)
//                    {
//                        /* do_nothing() */
//                    }
//                    else if (data.size() == 1)
//                    {
//                        document.put(fieldName, data.iterator().next());
//                    }
//                    else
//                    {
//                        for (String dataVal : data)
//                        {
//                            document.put(fieldName, dataVal);
//                        }
//                    }
//                }
//            }
//            catch (InvocationTargetException ioe)
//            {
//                Throwable wrapped = ioe.getTargetException();
//                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
//                    document.put("marc_error", indexer.getSolrFieldNames().toString() + wrapped.getMessage());
//                else throw(ioe);
//            }
//            catch (IllegalArgumentException e)
//            {
//                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
//                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
//                else throw(e);
//            }
//            catch (IndexerSpecException e)
//            {
//                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
//                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
//                else throw(e);
//            }
//            catch (Exception e)
//            {
//                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
//                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
//                else throw(e);
//            }
//        }
//        return document;
//    }
    
    protected SolrInputDocument[] indexToSolrDoc(final Record record) 
    {
        SolrInputDocument[] inputDocs = new SolrInputDocument[]{ new SolrInputDocument(), new SolrInputDocument(), new SolrInputDocument() } ;
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
                            inputDocs[0].addField(fieldName, dataVal, 1.0f );
                        }
                    }
                }
            }
//            catch (IllegalArgumentException e)
//            {
//                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
//            }
//            catch (IndexerSpecException e)
//            {
//                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
//            }
            catch (InvocationTargetException ioe)
            {
                Throwable wrapped = ioe.getTargetException();
                Exception wrappedE = (wrapped instanceof Exception) ? (Exception)wrapped : null;
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + wrappedE.getMessage());
            }
            catch (Exception e)
            {
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
            }
        }
        if (record.hasErrors() )
        {
            addMarcErrorsToMap(inputDocs[1], record.getErrors());
        }
        List<IndexerSpecException> perRecordExceptions = ValueIndexerFactory.instance().getPerRecordErrors();
        addExceptionsToMap(inputDocs[2], perRecordExceptions);

        return inputDocs;
    }
    

    public static void singleRecordSolrError(RecordAndDocError recDoc, Exception e1, BlockingQueue<RecordAndDocError> errQ)
    {
        logger.error("Failed on single doc with id : "+ recDoc.getRec().getControlNumber());
        if (e1 instanceof SolrRuntimeException && e1.getCause() instanceof SolrException)
        {
            SolrException cause = (SolrException)e1.getCause();
            logger.error(cause.getMessage());
        }
        if (errQ != null)
        {
            recDoc.addErrLoc(eErrorLocationVal.SOLR_ERROR);
            recDoc.errLvl = eErrorSeverity.ERROR;
            errQ.add(recDoc);
        }
    }

}
