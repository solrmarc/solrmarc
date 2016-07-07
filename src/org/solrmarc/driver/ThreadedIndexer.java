package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.solr.SolrProxy;

public class ThreadedIndexer extends Indexer
{   
    private final static Logger logger = Logger.getLogger(ThreadedIndexer.class);
    private final BlockingQueue<Record> readQ;
    private final BlockingQueue<RecordAndDoc> docQ;
//    private final BlockingQueue<RecordAndDocError> alreadyReadQ;
    private final BlockingQueue<Thread> threadQ;
    Thread readerThread;
    Thread indexerThread;
   
    boolean doneReading = false;
    final int buffersize; 
    final int cnts[] = new int[3];
    
    public ThreadedIndexer(List<AbstractValueIndexer<?>> indexers, SolrProxy solrProxy, int buffersize)
    {
        super(indexers, solrProxy);
        readQ = new ArrayBlockingQueue<Record>(buffersize);
        docQ = new ArrayBlockingQueue<RecordAndDoc>(buffersize);
 //       alreadyReadQ = new ArrayBlockingQueue<RecordAndDocError>(buffersize);
        threadQ = new LinkedBlockingQueue<Thread>();
        doneReading = false;
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
        indexerThread.interrupt();
    }


    
    @Override
    public int[] indexToSolr(final MarcReader reader) 
    {
        cnts[0] = cnts[1] = cnts[2] = 0;
        readerThread = new Thread("MarcReader-Thread") { 
            @Override 
            public void run()
            {
                cnts[0] = 0;
                Record record = null;
                while (reader.hasNext() && !this.isInterrupted())
                {
                    record = reader.next();
                    cnts[0] ++;
                    while (readQ.offer(record) == false)
                    {
                        try
                        {
                            // queue is full, wait until it drains sowewhat
                            sleep(10);
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                if (this.isInterrupted())
                {
                    Collection<Record> discardedRecords = new ArrayList<>();
                    readQ.drainTo(discardedRecords);
                    if (discardedRecords.size() > 0)
                    {
                        String id = discardedRecords.iterator().next().getControlNumber();
                        logger.warn("Reader Thread: discarding unprocessed records starting with record: "+ id);
                    }
                    else
                    {
                        String id = (record != null) ? record.getControlNumber() : "<none>";
                        logger.warn("Reader Thread Interrupted: last record processed was: "+ id);
                    }
                    cnts[0] -= discardedRecords.size();
                }
                else
                {
                    doneReading = true;
                }
            }
        };
        
        indexerThread = new Thread("RecordIndexer-Thread") { 
            @Override 
            public void run()
            {
                cnts[1] = 0;
                while ((! doneReading || !readQ.isEmpty()) && !readerThread.isInterrupted() && !this.isInterrupted() )
                {
                    try
                    {
                        Record rec = readQ.poll(10, TimeUnit.MILLISECONDS);
                        if (rec == null) 
                            continue;
                        RecordAndDoc recDoc = indexToSolrDoc(rec);
             //           recDoc.doc = combineDocWithErrors(documentParts, isSet(eErrorHandleVal.INDEX_ERROR_RECORDS));
                        if (recDoc.getErrLvl() != eErrorSeverity.NONE) 
                        {
                            if (isSet(eErrorHandleVal.RETURN_ERROR_RECORDS) && !isSet(eErrorHandleVal.INDEX_ERROR_RECORDS))
                            {
                                errQ.add(recDoc);
                            }               
                            if (!isSet(eErrorHandleVal.INDEX_ERROR_RECORDS))
                                continue;
                        }
                        cnts[1] ++;
                        boolean offer1Worked = docQ.offer(recDoc);
                        // boolean offer2Worked = alreadyReadQ.offer(record);
                        if (!offer1Worked  || (doneReading && readQ.isEmpty() && !docQ.isEmpty()))
                        {
                            final Collection<RecordAndDoc> chunk = new ArrayList<RecordAndDoc>(docQ.size());
                      //      final Collection<Record> chunkRecord = new ArrayList<Record>(docQ.size());
                            RecordAndDoc firstDoc = docQ.peek();
                            String threadName = null;
                            try {
                                String firstID = firstDoc.getDoc().getField("id").getValues().iterator().next().toString();
                                String lastID = recDoc.getDoc().getField("id").getValues().iterator().next().toString();
                                threadName = "SolrUpdate-"+firstID+"-"+lastID;
                            }
                            catch (Exception e)
                            {
                                threadName = "Anonymous";
                            }
                            docQ.drainTo(chunk);
                            final BlockingQueue<RecordAndDoc> errQVal = (isSet(eErrorHandleVal.RETURN_ERROR_RECORDS)) ? errQ : null;
                            Thread chunkThread = new ChunkIndexerThread(threadName, chunk, errQVal, solrProxy, cnts); 
                            logger.debug("Starting IndexerThread: "+ threadName);
                            chunkThread.start();
                            threadQ.add(chunkThread);
                            if (!offer1Worked) 
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
                if ( this.isInterrupted())
                {
                    for (Thread t : threadQ)
                    {
                        t.interrupt();
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
                for (Thread t : threadQ)
                {
                    while (t.isAlive())
                    {
                        try
                        {
                            logger.debug("Waiting on thread: "+t.getName());
                            t.join();
                            logger.debug("Done with thread: "+t.getName());
                        }
                        catch (InterruptedException e)
                        {
                            if (this.isInterrupted() && t.isInterrupted())
                            {
                                // Its already dying, so go ahead and wait.
                            }
                            else
                            {
                                t.interrupt();
                            }
                        }
                    }
                }
            }
        };
        
        readerThread.start();
        indexerThread.start();
        try {
            indexerThread.join();
        }
        catch (InterruptedException ie)
        {
            readerThread.interrupt();
            indexerThread.interrupt();
            while (indexerThread.isAlive())
            {
                try {
                    indexerThread.join();
                }
                catch (InterruptedException ie2)
                {
                    // Its already dying, so go ahead and wait.
                }
            }
        }
        if (shuttingDown)
        {
            this.endProcessing();
        }
        return(cnts);
    }

}
