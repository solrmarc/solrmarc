package org.solrmarc.index.driver;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.marc.Record;
import org.solrmarc.index.driver.RecordAndDocError.eErrorLocationVal;
import org.solrmarc.index.driver.RecordAndDocError.eErrorSeverity;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrRuntimeException;


public class ChunkIndexerThread extends Thread
{
    private final static Logger logger = Logger.getLogger(ChunkIndexerThread.class);
    final Collection<SolrInputDocument> docs;
    final Collection<RecordAndDocError> recordAndDocs;
    final SolrProxy solrProxy;
    final BlockingQueue<RecordAndDocError> errQ;

    final int cnts[];
    
//    public ChunkIndexerThread(String name, Collection<SolrInputDocument> chunk, Collection<Record> chunkRecord, 
//                       BlockingQueue<RecordAndDocError> errQ, 
//                       SolrProxy solrProxy, final int cnts[])
//    {
//        super(name);
//        this.chunk = chunk;
//        this.chunkRecord = chunkRecord;
//        this.cnts = cnts;
//        this.errQ = errQ;
//        this.solrProxy = solrProxy;
//    }
    
    public ChunkIndexerThread(String threadName, Collection<RecordAndDocError> recordAndDocs,
            BlockingQueue<RecordAndDocError> errQ, SolrProxy solrProxy, int[] cnts)
    {
        super(threadName);
        this.recordAndDocs = recordAndDocs;
        this.docs = buildDocList(recordAndDocs);
        this.cnts = cnts;
        this.errQ = errQ;
        this.solrProxy = solrProxy;
    }

    private Collection<SolrInputDocument> buildDocList(final Collection<RecordAndDocError> recordAndDocs)
    {
        Collection<SolrInputDocument> docs = new ArrayList<>(recordAndDocs.size());
        for (RecordAndDocError recDoc : recordAndDocs)
        {
            docs.add(recDoc.doc);
        }
        return docs;
    }

    @Override 
    public void run()
    {
        int inChunk = docs.size();
        @SuppressWarnings("unused")
        SolrInputDocument firstDoc = docs.iterator().next();
        logger.debug("Adding chunk of "+inChunk+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
        try {
            // If all goes well, this is all we need. Add the docs, and count the docs
            int cnt = solrProxy.addDocs(docs);
            synchronized ( cnts ) { cnts[2] += cnt; }
            logger.debug("Added chunk of "+cnt+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
            
            if (errQ != null)
            {
                for (RecordAndDocError recDoc : recordAndDocs)
                {
                    if (!recDoc.errLocs.isEmpty())
                    {
                        errQ.add(recDoc);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Iterator<RecordAndDocError> recDocI = recordAndDocs.iterator();
            
            if (inChunk == 1)
            {
                RecordAndDocError recDoc = recDocI.next();
                Indexer.singleRecordSolrError(recDoc, e, errQ);
            }
            else if (inChunk > 20)
            {
                logger.debug("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDoc.getFieldValue("id").toString());
                int newChunkSize = inChunk / 4;
                Thread subChunk[] = new Thread[4];
                
                for (int i = 0; i < 4; i++)
                {
                    List<RecordAndDocError> newRecDoc = new ArrayList<>(newChunkSize);
                    String id1 = null, id2 = null;
                    for (int j = 0; j < newChunkSize; j++)
                    {
                        Record rec;
                        if (recDocI.hasNext()) 
                        {
                            RecordAndDocError recDoc = recDocI.next();
                            newRecDoc.add(recDoc);
                            if (id1 == null) id1 = recDoc.getRec().getControlNumber();
                            id2 = recDoc.getRec().getControlNumber();
                        }
                    }
                    // Split the chunk into 4 sub-chunks, and start a ChunkIndexerThread for each of them.
                    subChunk[i] = new ChunkIndexerThread("SolrUpdateOnError_"+id1+"_"+id2, newRecDoc, errQ, solrProxy, cnts);
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
                while (recDocI.hasNext())
                {
                    RecordAndDocError recDoc = recDocI.next();
                    SolrInputDocument doc = recDoc.getDoc();
                 //   Record rec = recDoc.getRec();
                    logger.debug("Adding single doc with id : "+ doc.getFieldValue("id").toString());
                    try
                    {
                        @SuppressWarnings("unused")
                        int num = solrProxy.addDoc(doc);
                        num1++;
                        if (errQ != null && !recDoc.errLocs.isEmpty())
                        {
                            errQ.add(recDoc);
                        }
                        logger.debug("Added single doc with id : "+ doc.getFieldValue("id").toString());
                    }
                    catch (Exception e1)
                    {
                        Indexer.singleRecordSolrError(recDoc, e1, errQ);
                    }
                }
                synchronized ( cnts ) { cnts[2] += num1; }
            }
        }
    }
}
