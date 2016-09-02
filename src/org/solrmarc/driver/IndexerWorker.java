package org.solrmarc.driver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.marc4j.marc.Record;
import org.solrmarc.driver.Indexer.eErrorHandleVal;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.tools.SolrMarcIndexerException;

public class IndexerWorker implements Runnable
{
    private final static Logger logger = Logger.getLogger(IndexerWorker.class);
    private AtomicInteger cnts[];
    private final BlockingQueue<Record> readQ;
    private final BlockingQueue<RecordAndDoc> docQ;
    private Indexer indexer;
    private MarcReaderThread readerThread;
    private int threadCount;
    
    public IndexerWorker(MarcReaderThread readerThread, BlockingQueue<Record> readQ, BlockingQueue<RecordAndDoc> docQ, Indexer indexer, AtomicInteger cnts[], int threadCount)
    {
        this.readerThread = readerThread;
        this.readQ = readQ;
        this.docQ = docQ;
        this.indexer = indexer;
        this.cnts = cnts;
        this.threadCount = threadCount;
    }

    @Override 
    public void run()
    {
        // if this isn't the first Indexer Worker Thread make a thread safe instance duplicate of the indexer
        // this primarily means making a new instance object for each External Method class
        if (threadCount > 0)
        {
            indexer = (threadCount == 0) ? indexer : indexer.makeThreadSafeCopy();
        }
        Thread.currentThread().setName("RecordIndexer-Thread-"+threadCount);
        while ((! readerThread.isDoneReading() || !readQ.isEmpty()) && !readerThread.isInterrupted() && !Thread.currentThread().isInterrupted() )
        {
            try
            {
                Record rec = readQ.poll(10, TimeUnit.MILLISECONDS);
                if (rec == null) 
                    continue;
//                System.out.println(rec.getControlNumber() + " :  read in thread "+ threadCount);
                long id = Long.parseLong(rec.getControlNumber().substring(1))* 100 + threadCount;
                rec.setId((long)id);
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
                boolean offerWorked = docQ.offer(recDoc);
                while (!offerWorked) 
                {
                    try {
                        synchronized (docQ) { docQ.wait(); }

                        offerWorked = docQ.offer(recDoc);
                    }
                    catch (InterruptedException ie)
                    {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                if (offerWorked) cnts[1].incrementAndGet();
            }
            catch (InterruptedException e)
            {
                logger.warn("Interrupted while waiting for a record to appear in the read queue.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
