package org.solrmarc.index.driver;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.marc.Record;
import org.solrmarc.index.driver.Indexer.eErrorHandleVal;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrRuntimeException;


public class ChunkIndexerThread extends Thread
{
    private final static Logger logger = Logger.getLogger(ChunkIndexerThread.class);
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
        @SuppressWarnings("unused")
        SolrInputDocument firstDoc = chunk.iterator().next();
        logger.debug("Adding chunk of "+inChunk+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
        try {
            // If all goes well, this is all we need. Add the docs, and count the docs
            int cnt = solrProxy.addDocs(chunk);
            synchronized ( cnts ) { cnts[2] += cnt; }
            logger.debug("Added chunk of "+cnt+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
        }
        catch (Exception e)
        {
            Iterator<SolrInputDocument> docI = chunk.iterator();
            Iterator<Record> recI = chunkRecord.iterator();
            
            if (inChunk == 1)
            {
                SolrInputDocument doc = docI.next();
                Record rec = recI.next();
                singleRecordError(doc, rec, e);
            }
            else if (inChunk > 20)
            {
                logger.debug("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
                int newChunkSize = inChunk / 4;
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
                    // Split the chunk into 4 sub-chunks, and start a ChunkIndexerThread for each of them.
                    subChunk[i] = new ChunkIndexerThread("SolrUpdateOnError_"+id1+"_"+id2, newChunk, newChunkRecord, errQ, solrProxy, cnts);
                    subChunk[i].start();
                }
                for (int i = 0; i < 4; i++)
                {
                    // Now wait for each of the 4 sub-chunks to finish.
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
                logger.debug("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
                // error on bulk update, resubmit one-by-one
                int num1 = 0;
                while (docI.hasNext() && recI.hasNext())
                {
                    SolrInputDocument doc = docI.next();
                    Record rec = recI.next();
                    logger.debug("Adding single doc with id : "+ doc.getFieldValue("id").toString());
                    try
                    {
                        @SuppressWarnings("unused")
                        int num = solrProxy.addDoc(doc);
                        num1++;
                        logger.debug("Added single doc with id : "+ doc.getFieldValue("id").toString());
                    }
                    catch (Exception e1)
                    {
                        singleRecordError(doc, rec, e1);
                    }
                }
                synchronized ( cnts ) { cnts[2] += num1; }
            }
        }
    }

    private void singleRecordError(SolrInputDocument doc, Record rec, Exception e1)
    {
        logger.error("Failed on single doc with id : "+ doc.getFieldValue("id").toString());
        if (e1 instanceof SolrRuntimeException && e1.getCause() instanceof SolrException)
        {
            SolrException cause = (SolrException)e1.getCause();
            logger.error(cause.getMessage());
        }
        if (errQ != null)
        {
            errQ.add(new AbstractMap.SimpleEntry<Record, SolrInputDocument>(rec, doc));
        }
    }
}
