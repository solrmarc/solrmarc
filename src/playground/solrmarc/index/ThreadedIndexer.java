package playground.solrmarc.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.solr.SolrProxy;

public class ThreadedIndexer extends Indexer
{
    private final BlockingQueue<Record> readQ;
    private final BlockingQueue<SolrInputDocument> docQ;
    private final BlockingQueue<SolrResponse> respQ;
    private final BlockingQueue<Record> errQ;
    boolean doneReading = false;
    final int buffersize; 
    final int chunksize; 
    final int cnts[] = new int[3];
    
    public ThreadedIndexer(List<AbstractValueIndexer<?>> indexers, SolrProxy solrProxy, int buffersize, int chunksize)
    {
        super(indexers, solrProxy);
        readQ = new ArrayBlockingQueue<Record>(buffersize);
        docQ = new ArrayBlockingQueue<SolrInputDocument>(buffersize);
        respQ = new ArrayBlockingQueue<SolrResponse>(buffersize);
        errQ = new ArrayBlockingQueue<Record>(buffersize);
        doneReading = false;
        this.buffersize = buffersize;
        this.chunksize = chunksize;
    }

    @Override
    public int indexToSolr(final MarcReader reader) throws Exception
    {
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
                        Record record = readQ.take();
                        try { 
                            final SolrInputDocument document = indexToSolrDoc(record);
                            cnts[1] ++;
                            if (! docQ.offer(document) )
                            {
                                Collection<SolrInputDocument> chunk = new ArrayList<SolrInputDocument>(docQ.size());
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
                                Thread chunkThread = new Thread(threadName) {
                                    @Override 
                                    public void run()
                                    {
                                        solrProxy.addDocs(chunk);
                                    }
                                };
                                docQ.drainTo(chunk);
                                docQ.clear();
                                docQ.offer(document);
                                chunkThread.start();
                            }
                        }
                        catch (Exception e)
                        {
                            errQ.add(record);
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
//        while (reader.hasNext())
//        {
//            final Record record = reader.next();
//            try { 
//                final SolrInputDocument document = indexToSolrDoc(record);
//                if (! docQ.offer(document) )
//                {
//                    solrProxy.addDocs(docQ);
//                    docQ.clear();
//                    docQ.offer(document);
//                }
//                cnt++;
//            }
//            catch (Exception e)
//            {
//                errQ.add(record);
//            }
//        }
        indexerThread.join();
        int cnt = cnts[0];
        return(cnt);
    }

}
