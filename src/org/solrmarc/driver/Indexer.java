package org.solrmarc.driver;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.marc4j.MarcError;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.driver.RecordAndDoc.eErrorLocationVal;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrRuntimeException;
import org.solrmarc.tools.SolrMarcIndexerException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The single-threaded reference implementation of the indexing process.
 * Reads a record, builds the SolrInputDocument, and sends it to the SolrProxy.
 * The various methods that it uses to accomplish these tasks are used by the
 * various multi-threaded classes.
 *
 * @author rh9ec
 *
 */
public class Indexer
{
    private final static Logger logger = Logger.getLogger(Indexer.class);

    protected final List<AbstractValueIndexer<?>> indexers;
    protected SolrProxy solrProxy;
    protected final BlockingQueue<RecordAndDoc> errQ;
    protected final BlockingQueue<String> delQ;
    protected boolean shuttingDown = false;
    protected boolean viaInterrupt = false;
    protected boolean isShutDown = false;
    private int cnts[] = new int[] { 0, 0, 0 };

    EnumSet<eErrorHandleVal> errHandle = EnumSet.noneOf(eErrorHandleVal.class);
    public enum eErrorHandleVal
    {
        RETURN_ERROR_RECORDS, INDEX_ERROR_RECORDS;
    };

    public Indexer(final List<AbstractValueIndexer<?>> indexers, final SolrProxy solrProxy)
    {
        this.indexers = indexers;
        this.solrProxy = solrProxy;
        errQ = new LinkedBlockingQueue<RecordAndDoc>();
        delQ = new LinkedBlockingQueue<String>();
    }

    protected Indexer(Indexer toClone)
    {
        indexers = new ArrayList<AbstractValueIndexer<?>>();
        for (AbstractValueIndexer<?> indexer : toClone.indexers)
        {
            this.indexers.add(ValueIndexerFactory.makeThreadSafeCopy(indexer));
        }
        this.solrProxy = toClone.solrProxy;
        this.errQ = toClone.errQ;
        this.delQ = toClone.delQ;
        this.errHandle = toClone.errHandle;
    }

    protected Indexer makeThreadSafeCopy()
    {
        return (new Indexer(this));
    }

    boolean isSet(eErrorHandleVal val)
    {
        return (errHandle.contains(val));
    }

    void setErr(eErrorHandleVal val)
    {
        errHandle.add(val);
    }

    /**
     * indexToSolr - Reads in a MARC Record, produces SolrInputDocument for it,
     * sends that document to solr This is the single threaded version that does
     * each of those action sequentially
     *
     * @param reader
     * @return array containing number of records read, number of records
     *         indexed, and number of records sent to solr
     */
    public int[] indexToSolr(final MarcReader reader)
    {
        resetCnts();
        while (!shuttingDown)
        {
            RecordAndCnt recordAndCnt = getRecord(reader);
            if (recordAndCnt == null) break;

            RecordAndDoc recDoc = null;
            try {
                recDoc = getIndexDoc(recordAndCnt.getRecord(), recordAndCnt.getCnt());
            }
            catch (SolrMarcIndexerException smie)
            {
                break;
            }
            if (recDoc != null)
            {
                indexSingleDocument(recDoc);
            }
        }

        if (shuttingDown)
        {
            endProcessing();
        }
        return (cnts);
    }

    protected void indexSingleDocument(RecordAndDoc recDoc)
    {
        try {
            if (recDoc.getDoc() != null)
            {
                solrProxy.addDoc(recDoc.getDoc());
                incrementCnt(2);
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
        catch (Exception e)
        {
            singleRecordSolrError(recDoc, e, errQ);
        }
    }

    void resetCnts()
    {
        cnts[0] = cnts[1] = cnts[2] = 0;
    }

    int incrementCnt(int cntNum)
    {
        return(++cnts[cntNum]);
    }

    int addToCnt(int cntNum, int amount)
    {
        cnts[cntNum] += amount;
        return(cnts[cntNum]);
    }

    int[] getCounts()
    {
        return(cnts);
    }

    RecordAndCnt getRecord(MarcReader reader)
    {
        Record record = null;
        while (record == null)
        {
            try {
                if (reader.hasNext()) record = reader.next();
                else return(null);
            }
            catch (MarcException me)
            {
                logger.error("Unrecoverable Error in MARC record data", me);
                if (Boolean.parseBoolean(System.getProperty("solrmarc.terminate.on.marc.exception", "true")))
                {
                    return(null);
                }
                else
                {
                    logger.warn("Trying to continue after MARC record data error");
                    record = null;
                }
            }
        }
        int cnt = incrementCnt(0);
        return (new RecordAndCnt(record, cnt));
    }

    RecordAndDoc getIndexDoc(Record record, int count)
    {
        RecordAndDoc recDoc = null;
        recDoc = indexToSolrDoc(record);
        if (recDoc.getSolrMarcIndexerException() != null)
        {
            SolrMarcIndexerException smie = recDoc.getSolrMarcIndexerException();
            String recCtrlNum = recDoc.rec.getControlNumber();
            String idMessage = smie.getMessage() != null ? smie.getMessage() : "";
            if (smie.getLevel() == SolrMarcIndexerException.IGNORE)
            {
                logger.info("Record will be Ignored " + (recCtrlNum != null ? recCtrlNum : "") + " " + idMessage + " (record count " + count + ")");
                return(null);
            }
            else if (smie.getLevel() == SolrMarcIndexerException.DELETE)
            {
                logger.info("Record will be Deleted " + (recCtrlNum != null ? recCtrlNum : "") + " " + idMessage + " (record count " + count + ")");
                delQ.add(recCtrlNum);
                return(null);
            }
            else if (smie.getLevel() == SolrMarcIndexerException.EXIT)
            {
                logger.info("Serious Error flagged in record " + (recCtrlNum != null ? recCtrlNum : "") + " " + idMessage + " (record count " + count + ")");
                logger.info("Terminating indexing.");
                throw new SolrMarcIndexerException(SolrMarcIndexerException.EXIT);
            }
        }
        if (recDoc.getErrLvl() != eErrorSeverity.NONE)
        {
            if (isSet(eErrorHandleVal.RETURN_ERROR_RECORDS) && !isSet(eErrorHandleVal.INDEX_ERROR_RECORDS))
            {
                errQ.add(recDoc);
            }
            if (recDoc.getErrLvl() == eErrorSeverity.FATAL && recDoc.ise != null)
            {
                String recCtrlNum = recDoc.rec.getControlNumber();
                String idMessage = recDoc.ise.getMessage() != null ? recDoc.ise.getMessage() : "";
                String indSpec = recDoc.ise.getSpecMessage() != null ? recDoc.ise.getSpecMessage() : "";
                logger.info("Fatal Error returned for record " + (recCtrlNum != null ? recCtrlNum : "") + " : " + idMessage + " (record count " + count + ")");
                logger.info("Fatal Error from by index spec  " + (recCtrlNum != null ? recCtrlNum : "") + " : " + indSpec + " (record count " + count + ")");
                logger.info("Terminating indexing.");
                throw new SolrMarcIndexerException(SolrMarcIndexerException.EXIT);
            }
            if (!isSet(eErrorHandleVal.INDEX_ERROR_RECORDS))
            {
                logger.debug("Skipping error record: " + recDoc.rec.getControlNumber());
                return(null);
            }
        }
        incrementCnt(1);
        return(recDoc);
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
                    document.addField("marc_error", e.getSolrField() + " : " + cause.getMessage());
                }
            }
        }
        return (errLvl);
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
        Map<String, SolrInputField> emptyMap = new LinkedHashMap<>();
        SolrInputDocument[] inputDocs = new SolrInputDocument[] { new SolrInputDocument(emptyMap), new SolrInputDocument(emptyMap), new SolrInputDocument(emptyMap) };
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
                            inputDocs[0].addField(fieldName, dataVal, 1.0f);
                        }
                    }
                }
            }
            catch (OutOfMemoryError oome)
            {
                logger.error("OOMError in record: " + recDoc.rec.getControlNumber());
                logger.error("while processing index specification: " + indexer.getSpecLabel());
                logger.error("number of per record exceptions: "
                        + ((ValueIndexerFactory.instance().getPerRecordErrors() != null)
                                ? ValueIndexerFactory.instance().getPerRecordErrors().size() : 0));
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + oome.getMessage());
                errLvl = eErrorSeverity.FATAL;
                recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
            }
            catch (InvocationTargetException ioe)
            {
                Throwable wrapped = ioe.getTargetException();
                // Exception wrappedE = (wrapped instanceof Exception) ?
                // (Exception)wrapped : null;
                if (wrapped != null && wrapped instanceof IndexerSpecException)
                {
                    logger.debug("Exception in record: " + recDoc.rec.getControlNumber());
                    logger.debug("while processing index specification: " + indexer.getSpecLabel());
                    errLvl = eErrorSeverity.max(errLvl, ((IndexerSpecException) wrapped).getErrLvl());
                    ((IndexerSpecException)wrapped).setSolrFieldAndSpec(indexer.getSolrFieldNamesStr(), indexer.getSpecLabel());
                    recDoc.setIndexerSpecException((IndexerSpecException)wrapped);
                }
                else if (wrapped != null && wrapped instanceof OutOfMemoryError)
                {
                    logger.error("OOMError in record: " + recDoc.rec.getControlNumber());
                    logger.error("while processing index specification: " + indexer.getSpecLabel());
                    inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + wrapped.getMessage());
                    errLvl = eErrorSeverity.FATAL;
                    recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
                }
                else if (wrapped != null && wrapped instanceof IllegalArgumentException)
                {
                    logger.warn("Exception in record: " + recDoc.rec.getControlNumber());
                    logger.warn("while processing index specification: " + indexer.getSpecLabel());
                    if (wrapped != null)
                    {
                        logger.debug(wrapped);
                    }
                    errLvl = eErrorSeverity.ERROR;
                }
                else
                {
                    logger.warn("Exception in record: " + recDoc.rec.getControlNumber());
                    logger.warn("while processing index specification: " + indexer.getSpecLabel());
                    if (wrapped != null)
                    {
                        logger.warn(wrapped);
                    }
                    errLvl = eErrorSeverity.ERROR;
                }
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + wrapped.getMessage());
                recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
            }
            catch (SolrMarcIndexerException e)
            {
                recDoc.setSolrMarcIndexerException(e);
            }
            catch (IndexerSpecException e)
            {
                logger.warn("Exception in record: " + recDoc.rec.getControlNumber());
                logger.warn("while processing index specification: " + indexer.getSpecLabel());
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                errLvl = eErrorSeverity.max(errLvl, e.getErrLvl());
                recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
            }
            catch (Exception e)
            {
                logger.warn("Exception in record: " + recDoc.rec.getControlNumber());
                logger.warn("while processing index specification: " + indexer.getSpecLabel());
                inputDocs[2].addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                errLvl = eErrorSeverity.ERROR;
                recDoc.addErrLoc(eErrorLocationVal.INDEXING_ERROR);
            }
        }
        if (record.hasErrors())
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
        ValueIndexerFactory.instance().doneWithRecord(record);
        return recDoc;
    }

    protected void singleRecordSolrError(RecordAndDoc recDoc, Exception e1, BlockingQueue<RecordAndDoc> errQ)
    {
        logger.error("Failed on single doc with id : " + recDoc.getRec().getControlNumber());
        if (e1 instanceof SolrRuntimeException && e1.getCause() instanceof SolrException)
        {
            SolrException cause = (SolrException) e1.getCause();
            logger.error(cause.getMessage());
        }
        else if (e1 instanceof SolrRuntimeException && e1.getCause() instanceof InvocationTargetException)
        {
            InvocationTargetException cause = (InvocationTargetException) e1.getCause();
            Throwable target = cause.getTargetException();
            logger.error(target.getMessage());
        }
        else
        {
            logger.error(e1);
        }
        if (errQ != null)
        {
            recDoc.addErrLoc(eErrorLocationVal.SOLR_ERROR);
            recDoc.errLvl = eErrorSeverity.ERROR;
            errQ.add(recDoc);
        }
    }

    boolean isShutDown()
    {
        return isShutDown;
    }

    void setIsShutDown()
    {
        isShutDown = true;
    }

    void shutDown(boolean viaInterrupt)
    {
        this.viaInterrupt = viaInterrupt;
        shuttingDown = true;
    }

    void endProcessing()
    {
        if (delQ.size() > 0)
        {
            logger.info("Deleting "+delQ.size()+ " records ");
        }
        for (String recCtrlNum : delQ)
        {
          //  String recCtrlNum = recDoc.rec.getControlNumber();
            logger.debug("Deleting record " + (recCtrlNum != null ? recCtrlNum : ""));
            try
            {
                solrProxy.delete(recCtrlNum);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            logger.info("Commiting updates to Solr");
            solrProxy.commit(false);
        }
        catch (IOException e)
        {
        }
    }

    private static long time(long time, TimeUnit unit)
    {
        return unit.convert(time, TimeUnit.NANOSECONDS);
    }

    void reportPerMethodTime()
    {
        logger.info("Elapsed time per indexing method:");
        for (final AbstractValueIndexer<?> indexer : indexers)
        {
            long elaspedTime = indexer.getTotalElapsedTime();
            long minutes = time(elaspedTime, TimeUnit.MINUTES);
            long seconds = time(elaspedTime, TimeUnit.SECONDS);
            long millis = time(elaspedTime, TimeUnit.MILLISECONDS);
            millis -= seconds * 1000;
            seconds -= minutes * 60;
            String elapsedStr = String.format("%d min, %d.%03d sec", minutes, seconds, millis);
            logger.info(elapsedStr + "  ---" + indexer.getSolrFieldNames().toString() + ":" + indexer.getSpecLabel());
        }
    }
}
