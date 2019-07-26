package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.solr.SolrProxy;

/**
 *  The multi-threaded, producer-consumer implementation of the Indexer class, AKA where the magic happens.
 *  It creates a MarcReaderThread that reads records and places them in a queue
 *  It creates (one or more) ThreadedIndexer objects that each take records from the read queue, creates the
 *  SolrInputDocument for that record, and places the result in a document queue.  Then when enough SolrInputDocument
 *  are in the document queue, this class will gather them up and hand them off to a ChunkIndexWorker that
 *  will manage the sending of that chunk of records to Solr.
 *
 * @author rh9ec
 *
 */
public class ThreadedIndexer extends Indexer
{
    private final static Logger logger = Logger.getLogger(ThreadedIndexer.class);
    private final BlockingQueue<RecordAndCnt> readQ;
    private final BlockingQueue<RecordAndDoc> docQ;
    private final int numThreadIndexers;
    private final int numSolrjWorkers;
    MarcReaderThread readerThread = null;
    Thread thisThread = null;
    ExecutorService indexerExecutor;
    ThreadPoolExecutor solrExecutor;

    IndexerWorker[] workers = null;

    boolean doneReading = false;
    final int buffersize;
    final AtomicInteger cnts[];

    public ThreadedIndexer(List<AbstractValueIndexer<?>> indexers, SolrProxy solrProxy, int buffersize)
    {
        super(indexers, solrProxy);
        readQ = new ArrayBlockingQueue<RecordAndCnt>(buffersize);
        docQ = new ArrayBlockingQueue<RecordAndDoc>(buffersize * 3);
        cnts = new AtomicInteger[]{ new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0)};
        int num = 1;
        try {
            num = Integer.parseInt(System.getProperty("solrmarc.indexer.threadcount", "1"));
        }
        catch (NumberFormatException nfe)
        {
            num = 1;
        }
        finally
        {
            numThreadIndexers = num;
        }
        num = 4;
        try {
            num = Integer.parseInt(System.getProperty("solrmarc.solrj.threadcount", "4"));
        }
        catch (NumberFormatException nfe)
        {
            num = 4;
        }
        finally
        {
            numSolrjWorkers = num;
        }
        indexerExecutor = Executors.newFixedThreadPool(numThreadIndexers);
        ///solrExecutor = Executors.newFixedThreadPool(numSolrjWorkers);
        solrExecutor = new ThreadPoolExecutor(numSolrjWorkers, numSolrjWorkers * 3, 10000L, TimeUnit.MILLISECONDS,
                                              new ArrayBlockingQueue<Runnable>(numSolrjWorkers * 4));
        this.buffersize = buffersize;
    }

    private ThreadedIndexer(ThreadedIndexer toClone)
    {
        super(toClone);
        readQ = toClone.readQ;
        docQ = toClone.docQ;
        cnts = toClone.cnts;
        buffersize = toClone.buffersize;
        numThreadIndexers = toClone.numThreadIndexers;
        numSolrjWorkers = toClone.numSolrjWorkers;
    }

    @Override
    public Indexer makeThreadSafeCopy()
    {
        return (new ThreadedIndexer(this));
    }

    @Override
    void shutDown(boolean viaInterrupt)
    {
        logger.warn("ThreadedIndexer ShutDown Called!");
        this.viaInterrupt = viaInterrupt;
        shuttingDown = true;
        if (readerThread != null) readerThread.interrupt();
        if (workers != null)
        {
            for (IndexerWorker worker : workers)
            {
                worker.setInterrupted();
            }
        }
        thisThread.interrupt();
        logger.warn("ThreadedIndexer ShutDown Exits");
    }

    @Override
    void resetCnts()
    {
        cnts[0].set(0); cnts[1].set(0); cnts[2].set(0);
    }

    @Override
    int incrementCnt(int cntNum)
    {
        return(cnts[cntNum].incrementAndGet());
    }

    @Override
    int addToCnt(int cntNum, int amount)
    {
        return(cnts[cntNum].addAndGet(amount));
    }

    @Override
    public int[] indexToSolr(final MarcReader reader)
    {
        thisThread = Thread.currentThread();
        resetCnts();
        readerThread = new MarcReaderThread(reader, this, readQ, cnts);
        readerThread.start();

        workers = new IndexerWorker[numThreadIndexers];
        for (int i = 0; i < numThreadIndexers; i++)
        {
            workers[i] = new IndexerWorker(readerThread, readQ, docQ, this, i);
        }
        for (int i = 0; i < numThreadIndexers; i++)
        {
            indexerExecutor.execute(workers[i]);
        }
        // while the (one or more) IndexerWorker threads are chugging along taking records from
        // the readQ and building the solr index document, this thread will await the docQ being filled
        // and when it is full, it will lock the queue, copy all the documents to a separate buffer
        // and create a ChunkIndexerWorker to manage sending those records to Solr.
        while (!done(workers))
        {
            if (shuttingDown)
            {
                logger.warn("ThreadedIndexer at top of loop, shutting down");
            }
            if (docQ.size() > buffersize || indexerThreadsAreDone(workers) ||
                (shuttingDown && docQ.size() > 0))
            {
                int chunkSize = Math.min(buffersize, docQ.size());
                final ArrayList<RecordAndDoc> chunk = new ArrayList<RecordAndDoc>(chunkSize);
                if (shuttingDown)
                {
                    logger.warn("ThreadedIndexer flushing "+ chunkSize + " docs from docQ, which contains "+docQ.size() + " documents");
                }
                if (docQ.drainTo(chunk, chunkSize) > 0)
                {
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
                    Runnable runnableChunk = new ChunkIndexerWorker(threadName, chunk, errQVal, this);
                    logger.debug("Starting IndexerThread: "+ threadName);
                    logger.debug("   approx number in solrj executor service: "+ solrExecutor.getQueue().size());
                    while (runnableChunk != null) 
                    {
                        try {
                            solrExecutor.execute(runnableChunk);
                            runnableChunk = null;
                        }
                        catch (RejectedExecutionException  rje)
                        {
                            try
                            {
                                logger.debug("Solrj thread pool full, blocking");
                                solrExecutor.getQueue().put(runnableChunk);
                                logger.debug("Solrj thread pool no longer full, un-blocking");
                                runnableChunk = null;
                            }
                            catch (InterruptedException e)
                            {
                                logger.debug("Solrj thread pool interrupted, re-trying");
                            }
                        }
                    }
                }
            }
            else if (shuttingDown && docQ.size() == 0)
            {
                break;
            }
            else
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    logger.warn("ThreadedIndexer Interrupted!");
                }
            }
        }
        if (shuttingDown)
        {
            logger.warn("ThreadedIndexer exited main while loop");
        }

        if (Thread.interrupted() || shuttingDown)
        {
            indexerExecutor.shutdownNow();
        }
        else
        {
            indexerExecutor.shutdown();
        }

        try {
            indexerExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.info("Done with all indexing, finishing writing records to solr");

        solrExecutor.shutdown();
        boolean done = false;
        while (done == false)
        {
            try {
                done = solrExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
            }
        }
        logger.info("Done writing records to solr");

        return(getCounts());
    }

    @Override
    int[] getCounts()
    {
        int intCnts[] = new int[3];
        intCnts[0] = cnts[0].get();
        intCnts[1] = cnts[1].get();
        intCnts[2] = cnts[2].get();
        return(intCnts);
    }

    public ExecutorService getSolrExecutor()
    {
        return solrExecutor;
    }

    private boolean indexerThreadsAreDone(IndexerWorker[] workers)
    {
        for (IndexerWorker worker : workers)
        {
            if (!worker.isDoneWorking()) return(false);
        }
        return(true);
    }

    private boolean done(IndexerWorker[] workers)
    {
        if (!readerThread.isDoneReading(shuttingDown) || !readQ.isEmpty()) return(false);
        if (!docQ.isEmpty()) return(false);
        return indexerThreadsAreDone(workers);
    }
}
