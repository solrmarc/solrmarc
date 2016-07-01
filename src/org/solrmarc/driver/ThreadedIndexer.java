package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.driver.RecordAndDoc.eErrorLocationVal;
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
    public int[] indexToSolr(final MarcReader reader) throws Exception
    {
        cnts[0] = cnts[1] = cnts[2] = 0;
        Thread readerThread = new Thread("MarcReader-Thread") { 
            @Override 
            public void run()
            {
                cnts[0] = 0;
                while (reader.hasNext())
                {
                    final Record record = reader.next();
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
                        }
                    }
                }
                doneReading = true;
            }
        };
        
        Thread indexerThread = new Thread("RecordIndexer-Thread") { 
            @Override 
            public void run()
            {
                cnts[1] = 0;
                while (! doneReading || !readQ.isEmpty())
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
                            docQ.clear();
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
                        logger.debug("Interrupted while waiting for a record to appear in the read queue.  No biggie.  Looping.");
                    }
                }
            }
        };
        
        readerThread.start();
        indexerThread.start();
        indexerThread.join();
        for (Thread t : threadQ)
        {
            t.join();
        }
        return(cnts);
    }

}
