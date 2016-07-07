package org.solrmarc.driver;


import org.apache.log4j.Logger;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.marc4j.MarcError;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.driver.RecordAndDoc.eErrorLocationVal;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrRuntimeException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Indexer
{
    private final static Logger logger = Logger.getLogger(Indexer.class);
    
    protected final List<AbstractValueIndexer<?>> indexers;
    protected SolrProxy solrProxy;
    public EnumSet<eErrorHandleVal> errHandle = EnumSet.noneOf(eErrorHandleVal.class);
    protected final BlockingQueue<RecordAndDoc> errQ;
    protected boolean shuttingDown = false;
    protected boolean isShutDown = false;

    public enum eErrorHandleVal
    {
        RETURN_ERROR_RECORDS, INDEX_ERROR_RECORDS;
    };
    
    public Indexer(final List<AbstractValueIndexer<?>> indexers, final SolrProxy solrProxy)
    {
        this.indexers = indexers;
        this.solrProxy = solrProxy;
        errQ = new LinkedBlockingQueue<RecordAndDoc>();
    }

    public boolean isSet(eErrorHandleVal val)
    {
        return(errHandle.contains(val));
    }
    
    public void setErr(eErrorHandleVal val)
    {
        errHandle.add(val);
    }
        
    /**
     * indexToSolr  - Reads in a MARC Record, produces SolrInputDocument for it, sends that document to solr
     *                This is the single threaded version that does each of those action sequentially
     * @param reader
     * @return array containing number of records read, number of records indexed, and number of records sent to solr 
     */
    public int[] indexToSolr(final MarcReader reader)
    {
        int cnts[] = new int[] { 0, 0, 0 };
        while (reader.hasNext() && !shuttingDown)
        {
            final Record record = reader.next();
            cnts[0]++;
            RecordAndDoc recDoc = indexToSolrDoc(record);
            cnts[1]++;
            if (recDoc.getErrLvl() != eErrorSeverity.NONE) 
            {
                if (isSet(eErrorHandleVal.RETURN_ERROR_RECORDS) && !isSet(eErrorHandleVal.INDEX_ERROR_RECORDS))
                {
                    errQ.add(recDoc);
                }   
            }
            try { 
                if (recDoc.getDoc() != null)
                {
                    solrProxy.addDoc(recDoc.getDoc());
                    cnts[2]++;
                    if (recDoc.getErrLvl() != eErrorSeverity.NONE && isSet(eErrorHandleVal.RETURN_ERROR_RECORDS)) 
                    {
                        if (isSet(eErrorHandleVal.RETURN_ERROR_RECORDS))
                        {
                            errQ.add(recDoc);
                        }   
                    }
                }
            }
            catch (SolrRuntimeException sse)
            {
                singleRecordSolrError(recDoc, sse, errQ);
            }
        }

        if (shuttingDown)
        {
            endProcessing();
        }
        return(cnts);
    }
    
//    protected EnumSet<eErrorLocationVal> getErrLocVals(SolrInputDocument[] documentParts)
//    {
//        EnumSet<eErrorLocationVal> eLocVals = EnumSet.noneOf(eErrorLocationVal.class);
//        if (!documentParts[2].isEmpty()) eLocVals.add(eErrorLocationVal.INDEXING_ERROR);
//        if (!documentParts[1].isEmpty()) eLocVals.add(eErrorLocationVal.MARC_ERROR);
//        return eLocVals;
//    }

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

    private eErrorSeverity addExceptionsToMap(SolrInputDocument document, List<IndexerSpecException> perRecordExceptions, eErrorSeverity errLvl)
    {
        if (perRecordExceptions != null)
        {
            for (IndexerSpecException e : perRecordExceptions)
            {
                @SuppressWarnings("unused")
                String specMessage = e.getSpecMessage();
                document.addField("marc_error", e.getMessage());
                errLvl = eErrorSeverity.max(errLvl, e.getErrLvl());
                for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
                {
                    document.addField("marc_error", e.getSolrField()+ " : " + cause.getMessage());
                }
            }
        }
        return(errLvl);
    }

    private void addMarcErrorsToMap(SolrInputDocument document, List<MarcError> errors)
    {
        for (Object err : errors)
        {
            document.addField("marc_error", err.toString());
        }
    }
    
    protected RecordAndDoc indexToSolrDoc(final Record record) 
    {
        Map<String,SolrInputField> emptyMap = new LinkedHashMap<>();
        SolrInputDocument[] inputDocs = new SolrInputDocument[]{ new SolrInputDocument(emptyMap), new SolrInputDocument(emptyMap), new SolrInputDocument(emptyMap) } ;
        RecordAndDoc recDoc = new RecordAndDoc(record);
        eErrorSeverity errLvl = eErrorSeverity.NONE;
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
            catch (InvocationTargetException ioe)
            {
                Throwable wrapped = ioe.getTargetException();
                Exception wrappedE = (wrapped instanceof Exception) ? (Exception)wrapped : null;
                if (wrappedE != null && wrappedE instanceof IndexerSpecException)
                {
                    errLvl = eErrorSeverity.max(errLvl, ((IndexerSpecException)wrappedE).getErrLvl());
                }
                else
                {
                    errLvl = eErrorSeverity.ERROR;
                }
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + wrappedE.getMessage());
                recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
            }
            catch (IndexerSpecException e)
            {
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                errLvl = eErrorSeverity.max(errLvl, e.getErrLvl());
                recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
            }
            catch (Exception e)
            {
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                errLvl = eErrorSeverity.ERROR;
                recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
            }
        }
        if (record.hasErrors() )
        {
            addMarcErrorsToMap(inputDocs[1], record.getErrors());
            recDoc.addErrLoc(eErrorLocationVal.MARC_ERROR);
        }
        List<IndexerSpecException> perRecordExceptions = ValueIndexerFactory.instance().getPerRecordErrors();
        if (perRecordExceptions != null) 
        {
            errLvl = addExceptionsToMap(inputDocs[2], perRecordExceptions, errLvl);
            recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
        }
        recDoc.setDoc(combineDocWithErrors(inputDocs, isSet(eErrorHandleVal.INDEX_ERROR_RECORDS)));
        recDoc.setMaxErrLvl(errLvl);
        
        return recDoc;
    }
    
    public static void singleRecordSolrError(RecordAndDoc recDoc, Exception e1, BlockingQueue<RecordAndDoc> errQ)
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

    public boolean isShutDown()
    {
        return isShutDown;
    }

    public void shutDown()
    {
        shuttingDown = true;
    }

    public void endProcessing()
    {
        try
        {
            logger.info("Commiting updates to Solr");
            solrProxy.commit(false);
        }
        catch (IOException e)
        {
        }
        this.isShutDown = true;
    }

}
