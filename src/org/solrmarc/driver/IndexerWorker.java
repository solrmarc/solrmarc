package org.solrmarc.driver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.marc4j.marc.Record;
import org.solrmarc.tools.SolrMarcIndexerException;

public class IndexerWorker implements Runnable
{
    private final static Logger logger = Logger.getLogger(IndexerWorker.class);
    private final BlockingQueue<RecordAndCnt> readQ;
    private final BlockingQueue<RecordAndDoc> docQ;
    private Indexer indexer;
    private MarcReaderThread readerThread;
    private int threadCount;
    private boolean doneWorking = false;
    private boolean interrupted = false;

    public IndexerWorker(MarcReaderThread readerThread, BlockingQueue<RecordAndCnt> readQ, BlockingQueue<RecordAndDoc> docQ, Indexer indexer, int threadCount)
    {
        this.readerThread = readerThread;
        this.readQ = readQ;
        this.docQ = docQ;
        this.indexer = indexer;
        this.threadCount = threadCount;
        this.doneWorking = false;
    }

    public boolean isDoneWorking()
    {
        return doneWorking;
    }

    public void setInterrupted()
    {
        interrupted = true;
    }

    public boolean isInterrupted()
    {
        return(interrupted);
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
        while ((! readerThread.isDoneReading(false) || !readQ.isEmpty()) && !readerThread.isInterrupted() && !isInterrupted() )
        {
            try
            {
                RecordAndCnt recordAndCnt = readQ.poll(100, TimeUnit.MILLISECONDS);
                if (recordAndCnt == null)  continue;
                int count = recordAndCnt.getCnt();
                Record record = recordAndCnt.getRecord();

                if (isInterrupted())  break;
                RecordAndDoc recDoc = null;
                try {
                    recDoc = indexer.getIndexDoc(record, count);
                }
                catch (SolrMarcIndexerException smie)
                {
                    indexer.shutDown(false);
                    break;
                }
                if (recDoc == null) continue;

                if (isInterrupted())  break;
                try {
                    docQ.put(recDoc);
                }
                catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            catch (InterruptedException e)
            {
                logger.warn("Interrupted while waiting for a record to appear in the read queue.");
                interrupted = true;
                Thread.currentThread().interrupt();
            }
        }
        doneWorking = true;
    }
}
