package org.solrmarc.index.driver;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.solr.common.SolrInputDocument;
import org.marc4j.marc.Record;
import org.solrmarc.solr.SolrProxy;


public class ChunkIndexerThread extends Thread
{
    final Collection<SolrInputDocument> chunk;
    final Collection<Record> chunkRecord;
    final SolrProxy solrProxy;
    final BlockingQueue<AbstractMap.SimpleEntry<Record, SolrInputDocument>> errQ;

    final int cnts[];
    
    public ChunkIndexerThread(String name, Collection<SolrInputDocument> chunk, Collection<Record> chunkRecord, 
                       BlockingQueue<AbstractMap.SimpleEntry<Record, SolrInputDocument>> errQ, 
                       SolrProxy solrProxy, final int cnts[])
    {
        super(name);
        this.chunk = chunk;
        this.chunkRecord = chunkRecord;
        this.cnts = cnts;
        this.errQ = errQ;
        this.solrProxy = solrProxy;
    }
    
    @Override 
    public void run()
    {
        int inChunk = chunk.size();
        SolrInputDocument firstDoc = chunk.iterator().next();
//        System.err.println("Adding chunk of "+inChunk+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
        try {
            // If all goes well, this is all we need. Add the docs, and count the docs
            int cnt = solrProxy.addDocs(chunk);
            synchronized ( cnts ) { cnts[2] += cnt; }
//            System.err.println("Added chunk of "+cnt+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
        }
        catch (Exception e)
        {
            if (inChunk > 20)
            {
//                System.err.println("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
                int newChunkSize = inChunk / 4;
                Iterator<SolrInputDocument> docI = chunk.iterator();
                Iterator<Record> recI = chunkRecord.iterator();
                Thread subChunk[] = new Thread[4];
                
                for (int i = 0; i < 4; i++)
                {
                    List<SolrInputDocument> newChunk = new ArrayList<>(newChunkSize);
                    List<Record> newChunkRecord = new ArrayList<>(newChunkSize);
                    String id1 = null, id2 = null;
                    for (int j = 0; j < newChunkSize; j++)
                    {
                        Record rec;
                        if (docI.hasNext()) 
                        {
                            newChunk.add(docI.next());
                        }
                        if (recI.hasNext()) 
                        {
                            newChunkRecord.add(rec = recI.next());
                            if (id1 == null) id1 = rec.getControlNumber();
                            id2 = rec.getControlNumber();
                        }
                    }
                    subChunk[i] = new ChunkIndexerThread("SolrUpdateOnError_"+id1+"_"+id2, newChunk, newChunkRecord, errQ, solrProxy, cnts);
                    subChunk[i].start();
                }
                for (int i = 0; i < 4; i++)
                {
                    try
                    {
                        subChunk[i].join();
                    }
                    catch (InterruptedException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
            // less than 20 in the chunk resubmit one-by-one
            else 
            { 
//                System.err.println("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
                // error on bulk update, resubmit one-by-one
                int num1 = 0;
                Iterator<SolrInputDocument> docI = chunk.iterator();
                Iterator<Record> recI = chunkRecord.iterator();
                while (docI.hasNext() && recI.hasNext())
                {
                    SolrInputDocument doc = docI.next();
                    Record rec = recI.next();
//                    System.err.println("Adding single doc with id : "+ doc.getFieldValue("id").toString());
                    try
                    {
                        int num = solrProxy.addDoc(doc);
                        num1++;
//                        System.err.println("Added single doc with id : "+ doc.getFieldValue("id").toString());
                    }
                    catch (Exception e1)
                    {
                        System.err.println("Failed on single doc with id : "+ doc.getFieldValue("id").toString());
                        errQ.add(new AbstractMap.SimpleEntry<Record, SolrInputDocument>(rec, doc));
                    }
                }
                synchronized ( cnts ) { cnts[2] += num1; }
            }
        }
    }
}
