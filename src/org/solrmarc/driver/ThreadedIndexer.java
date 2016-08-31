package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.solr.SolrProxy;

public class ThreadedIndexer extends Indexer
{   
    private final static Logger logger = Logger.getLogger(ThreadedIndexer.class);
    private final BlockingQueue<Record> readQ;
    private final BlockingQueue<RecordAndDoc> docQ;
    MarcReaderThread readerThread;
    ExecutorService indexerExecutor;
    ExecutorService solrExecutor;
   
    boolean doneReading = false;
    final int buffersize; 
    final AtomicInteger cnts[] = new AtomicInteger[]{ new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0)} ;
    
    public ThreadedIndexer(List<AbstractValueIndexer<?>> indexers, SolrProxy solrProxy, int buffersize)
    {
        super(indexers, solrProxy);
        readQ = new ArrayBlockingQueue<Record>(buffersize);
        docQ = new ArrayBlockingQueue<RecordAndDoc>(buffersize);
       // indexerExecutor = Executors.newSingleThreadExecutor();
        indexerExecutor = Executors.newFixedThreadPool(2);
        solrExecutor = Executors.newFixedThreadPool(4);
        this.buffersize = buffersize;
    }

    @Override
    public boolean isShutDown()
    {
        return isShutDown;
    }

    @Override
    public void shutDown()
    {
        shuttingDown = true;
        readerThread.interrupt();
        indexerExecutor.shutdownNow();
        solrExecutor.shutdownNow();
    }

    @Override
    public int[] indexToSolr(final MarcReader reader) 
    {
        cnts[0].set(0); cnts[1].set(0); cnts[2].set(0);
        readerThread = new MarcReaderThread(reader, readQ, cnts);      
        readerThread.start();

        IndexerWorker worker1 = new IndexerWorker(readerThread, readQ, docQ, this, cnts, 0);
//        IndexerWorker worker2 = new IndexerWorker(readerThread, readQ, docQ, this, cnts, 1);
        indexerExecutor.execute(worker1);
//        indexerExecutor.execute(worker2);
        while (!done())
        {
            if (docQ.remainingCapacity() == 0 || indexerThreadsAreDone())
            {
                final ArrayList<RecordAndDoc> chunk = new ArrayList<RecordAndDoc>(docQ.size());
                synchronized (docQ)
                {
                    docQ.drainTo(chunk);
                    docQ.notifyAll();
                }
                RecordAndDoc firstDoc = chunk.get(0);
                String threadName = null;
                try {
                    String firstID = firstDoc.getRec().getControlNumber();
                    String lastID = chunk.get(chunk.size()-1).getRec().getControlNumber();
                    threadName = "SolrUpdate-"+firstID+"-"+lastID;
                }
                catch (Exception e)
                {
                    threadName = "Anonymous";
                }
                final BlockingQueue<RecordAndDoc> errQVal = (this.isSet(eErrorHandleVal.RETURN_ERROR_RECORDS)) ? this.errQ : null;
                Runnable runnableChunk = new ChunkIndexerThread(threadName, chunk, errQVal, this.solrProxy, cnts); 
                logger.debug("Starting IndexerThread: "+ threadName);
                solrExecutor.execute(runnableChunk);
            }
            else 
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        indexerExecutor.shutdown();
        try {
            indexerExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        solrExecutor.shutdown();
        try {
            solrExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        try {
//            indexerThread.join();
//        }
//        catch (InterruptedException ie)
//        {
//            readerThread.interrupt();
//            indexerThread.interrupt();
//            while (indexerThread.isAlive())
//            {
//                try {
//                    indexerThread.join();
//                }
//                catch (InterruptedException ie2)
//                {
//                    // Its already dying, so go ahead and wait.
//                }
//            }
//        }
        if (shuttingDown)
        {
            this.endProcessing();
        }
        int intCnts[] = new int[3];
        intCnts[0] = cnts[0].get();
        intCnts[1] = cnts[1].get();
        intCnts[2] = cnts[2].get();
        return(intCnts);
    }

    private boolean indexerThreadsAreDone()
    {
        return readerThread.isDoneReading() && readQ.isEmpty() ;
    }

    private boolean done()
    {
        return (readerThread.isDoneReading() && readQ.isEmpty() && docQ.isEmpty());
    }

}
