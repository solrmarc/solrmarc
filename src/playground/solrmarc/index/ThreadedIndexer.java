package playground.solrmarc.index;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.solr.SolrProxy;

public class ThreadedIndexer extends Indexer
{   
    private final BlockingQueue<Record> readQ;
    private final BlockingQueue<SolrInputDocument> docQ;
    private final BlockingQueue<Record> alreadyReadQ;
    private final BlockingQueue<Thread> threadQ;
   
    boolean doneReading = false;
    final int buffersize; 
    final int chunksize; 
    final int cnts[] = new int[3];
    
    public ThreadedIndexer(List<AbstractValueIndexer<?>> indexers, SolrProxy solrProxy, int buffersize, int chunksize)
    {
        super(indexers, solrProxy);
        readQ = new ArrayBlockingQueue<Record>(buffersize);
        docQ = new ArrayBlockingQueue<SolrInputDocument>(buffersize);
        alreadyReadQ = new ArrayBlockingQueue<Record>(buffersize);
        threadQ = new LinkedBlockingQueue<Thread>();
        doneReading = false;
        this.buffersize = buffersize;
        this.chunksize = chunksize;
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
                        Record record = readQ.poll(10, TimeUnit.MILLISECONDS);
                        if (record == null) 
                            continue;
                        try { 
                            final SolrInputDocument document = indexToSolrDoc(record);
                            if (document.containsKey("marc_error"))
                            {
                                if (errHandle.contains(eErrorHandleVal.RETURN_ERROR_RECORDS))
                                    errQ.add(new AbstractMap.SimpleEntry<Record, SolrInputDocument>(record, document));
                                if (! errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                                    continue;
                            }
                            cnts[1] ++;
                            boolean offer1Worked = docQ.offer(document);
                            boolean offer2Worked = alreadyReadQ.offer(record);
                            if (!offer1Worked  || (doneReading && readQ.isEmpty() && !docQ.isEmpty()))
                            {
                                final Collection<SolrInputDocument> chunk = new ArrayList<SolrInputDocument>(docQ.size());
                                final Collection<Record> chunkRecord = new ArrayList<Record>(alreadyReadQ.size());
                                SolrInputDocument firstDoc = docQ.peek();
                                String threadName = null;
                                try {
                                    String firstID = firstDoc.getField("id").getValues().iterator().next().toString();
                                    String lastID = document.getField("id").getValues().iterator().next().toString();
                                    threadName = "SolrUpdate-"+firstID+"-"+lastID;
                                }
                                catch (Exception e)
                                {
                                    threadName = "Anonymous";
                                }
                                docQ.drainTo(chunk);
                                alreadyReadQ.drainTo(chunkRecord);
                                docQ.clear();
                                alreadyReadQ.clear();
                                Thread chunkThread = new ChunkIndexerThread(threadName, chunk, chunkRecord, errQ, solrProxy, cnts); 
                                chunkThread.start();
                                threadQ.add(chunkThread);
                                if (!offer1Worked) 
                                {
                                    docQ.offer(document);
                                    alreadyReadQ.offer(record);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            errQ.add(new AbstractMap.SimpleEntry<Record, SolrInputDocument>(record, null));
                        }
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (! docQ.isEmpty() )
                {
                    solrProxy.addDocs(docQ);
                    docQ.clear();
                }
            }
        };
        
        readerThread.start();
        indexerThread.start();
        indexerThread.join();
        for (Thread t : threadQ)
        {
            if (t.isAlive()) t.join();
        }
        return(cnts);
    }

}
