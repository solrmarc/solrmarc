package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.driver.Indexer.eErrorHandleVal;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.tools.SolrMarcIndexerException;

public class IndexerThread implements Runnable
{
    private final static Logger logger = Logger.getLogger(IndexerThread.class);
    private volatile int cnts[];
    private final BlockingQueue<Record> readQ;
    private final BlockingQueue<RecordAndDoc> docQ;
    private final Indexer indexer;
    private MarcReaderThread readerThread;
    private final ExecutorService solrExecutor;

    
    public IndexerThread(MarcReaderThread readerThread, BlockingQueue<Record> readQ, BlockingQueue<RecordAndDoc> docQ, Indexer indexer, ExecutorService solrExecutor, int cnts[])
    {
        this.readerThread = readerThread;
        this.readQ = readQ;
        this.docQ = docQ;
        this.indexer = indexer; //.clone();
        this.solrExecutor = solrExecutor;
        this.cnts = cnts;
    }
    

    @Override 
    public void run()
    {
        Thread.currentThread().setName("RecordIndexer-Thread");
        cnts[1] = 0;
        while ((! readerThread.isDoneReading() || !readQ.isEmpty()) && !readerThread.isInterrupted() && !Thread.currentThread().isInterrupted() )
        {
            try
            {
                Record rec = readQ.poll(10, TimeUnit.MILLISECONDS);
                if (rec == null) 
                    continue;
                RecordAndDoc recDoc = indexer.indexToSolrDoc(rec);
                if (recDoc.getSolrMarcIndexerException() != null)
                {
                    SolrMarcIndexerException smie = recDoc.getSolrMarcIndexerException();
                    String recCtrlNum = recDoc.rec.getControlNumber();
                    String idMessage = smie.getMessage();
                    if (smie.getLevel() == SolrMarcIndexerException.IGNORE)
                    {
                        logger.info("Ignored record " + (recCtrlNum != null ? recCtrlNum : "") + idMessage + " (record count " + cnts[0] + ")");
                    }
                    else if (smie.getLevel() == SolrMarcIndexerException.DELETE)
                    {            
                        logger.info("Deleted record " + (recCtrlNum != null ? recCtrlNum : "") + idMessage + " (record count " + cnts[0] + ")");
                        indexer.delQ.add(recDoc);
                    }
                    else if (smie.getLevel() == SolrMarcIndexerException.EXIT)
                    {
                        logger.info("Serious Error flagged in record " + (recCtrlNum != null ? recCtrlNum : "") + idMessage + " (record count " + cnts[0] + ")");
                        logger.info("Terminating indexing.");
                        Thread.currentThread().interrupt();
                        continue;
                    }
                }
                if (recDoc.getErrLvl() != eErrorSeverity.NONE) 
                {
                    if (indexer.isSet(eErrorHandleVal.RETURN_ERROR_RECORDS) && !indexer.isSet(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    {
                        indexer.errQ.add(recDoc);
                    }               
                    if (!indexer.isSet(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    {
                        logger.debug("Skipping error record: " + recDoc.rec.getControlNumber());
                        continue;
                    }
                }
                cnts[1]++;
                boolean offerWorked = docQ.offer(recDoc);
                if (!offerWorked  || (!readerThread.isAlive() && readQ.isEmpty() && !docQ.isEmpty()))
                {
                    final Collection<RecordAndDoc> chunk = new ArrayList<RecordAndDoc>(docQ.size());
                    RecordAndDoc firstDoc = docQ.peek();
                    String threadName = null;
                    try {
                        String firstID = firstDoc.getRec().getControlNumber();
                        String lastID = recDoc.getRec().getControlNumber();
                        threadName = "SolrUpdate-"+firstID+"-"+lastID;
                    }
                    catch (Exception e)
                    {
                        threadName = "Anonymous";
                    }
                    docQ.drainTo(chunk);
                    final BlockingQueue<RecordAndDoc> errQVal = (indexer.isSet(eErrorHandleVal.RETURN_ERROR_RECORDS)) ? indexer.errQ : null;
                    Runnable runnableChunk = new ChunkIndexerThread(threadName, chunk, errQVal, indexer.solrProxy, cnts); 
                    logger.debug("Starting IndexerThread: "+ threadName);
                    solrExecutor.execute(runnableChunk);
                    if (!offerWorked) 
                    {
                        docQ.offer(recDoc);
                    }
                }
            }
            catch (InterruptedException e)
            {
                logger.warn("Interrupted while waiting for a record to appear in the read queue.");
                Thread.currentThread().interrupt();
            }
        }
        if ( Thread.currentThread().isInterrupted())
        {
            logger.warn("Indexer Thread Interrupted: shuttingDown solr ExecutorService");
            solrExecutor.shutdownNow();
            try
            {
                solrExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            final Collection<RecordAndDoc> discardedDocs = new ArrayList<RecordAndDoc>(docQ.size());
            docQ.drainTo(discardedDocs);
            if (discardedDocs.size() > 0)
            {
                String id = discardedDocs.iterator().next().getRec().getControlNumber();
                logger.warn("Indexer Thread Interrupted: discarding unprocessed records starting with record: "+ id);
            }
            else
            {
                logger.warn("Indexer Thread Interrupted: last record processed not known");
            }
            cnts[1] -= discardedDocs.size();
        }
        else
        {
            logger.debug("Indexer Thread finished: shuttingDown solr ExecutorService slowly");
            solrExecutor.shutdown();
            try
            {
                logger.debug("Indexer Thread finished: awaiting termination");
                boolean terminated = solrExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                logger.debug("Indexer Thread finished: all terminated : " + terminated);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
