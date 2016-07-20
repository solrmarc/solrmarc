package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.marc.Record;
import org.solrmarc.solr.SolrProxy;
/**
 *  <h1>ChunkIndexerThread</h1> 
 * 
 *  This class implements sending batches of documents to Solr.  It implements retries to cope with 
 *  the issue where one bad document in a batch will cause all subsequent solr input documents in 
 *  the batch to be skipped.
 *  <br/>
 *  To accomplish this the class will divide the batch into several smaller segments, and re-try sending 
 *  those smaller batches.   Eventually a sub-batch containing the problem record will be sent one-by-one
 *  to insure that all valid documents are correctly sent to Solr, while only the documents containing 
 *  errors are skipped.   
 *  <br/>
 *  If the parameter errQ is not null the records that cause an error will be appended to that list and 
 *  can subsequently be logged or fixed and retried.
 * 
 *  
 * @author rh9ec
 *
 */

public class ChunkIndexerThread extends Thread
{
    private final static Logger logger = Logger.getLogger(ChunkIndexerThread.class);
    final Collection<SolrInputDocument> docs;
    final Collection<RecordAndDoc> recordAndDocs;
    final SolrProxy solrProxy;
    final BlockingQueue<RecordAndDoc> errQ;
    String firstDocId = null;
    String lastDocId = null;

    final int cnts[];
        
    public ChunkIndexerThread(String threadName, Collection<RecordAndDoc> recordAndDocs,
            BlockingQueue<RecordAndDoc> errQ, SolrProxy solrProxy, int[] cnts)
    {
        super(threadName);
        this.recordAndDocs = recordAndDocs;
        this.docs = buildDocList(recordAndDocs);
        this.cnts = cnts;
        this.errQ = errQ;
        this.solrProxy = solrProxy;
    }

    private Collection<SolrInputDocument> buildDocList(final Collection<RecordAndDoc> recordAndDocs)
    {
        Collection<SolrInputDocument> docs = new ArrayList<>(recordAndDocs.size());
        for (RecordAndDoc recDoc : recordAndDocs)
        {
            String docID = controlNumOrDefault(recDoc.getRec(), "Rec with No 001");
            if (firstDocId == null)  firstDocId = docID;
            docs.add(recDoc.doc);
            lastDocId = docID;
        }
        return docs;
    }

    private final String controlNumOrDefault(final Record rec, final String label)
    {
        String docID = rec.getControlNumber();
        if (docID == null) docID = label;
        return(docID);
    }

    @Override 
    public void run()
    {
        int inChunk = docs.size();
        logger.debug("Adding chunk of "+inChunk+ " documents -- starting with id : "+firstDocId);
        try {
            // If all goes well, this is all we need. Add the docs, count the docs, and if desired return the docs with errors
            int cnt = solrProxy.addDocs(docs);
            synchronized ( cnts ) { cnts[2] += cnt; }
            logger.debug("Added chunk of "+cnt+ " documents -- starting with id : "+firstDocId);
            
            if (errQ != null)
            {
                for (RecordAndDoc recDoc : recordAndDocs)
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
            Iterator<RecordAndDoc> recDocI = recordAndDocs.iterator();
            
            if (inChunk == 1)
            {
                RecordAndDoc recDoc = recDocI.next();
                Indexer.singleRecordSolrError(recDoc, e, errQ);
            }
            else if (inChunk > 20)
            {
                logger.debug("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDocId);
                int newChunkSize = inChunk / 4;
                Thread subChunk[] = new Thread[4];
                
                for (int i = 0; i < 4; i++)
                {
                    List<RecordAndDoc> newRecDoc = new ArrayList<>(newChunkSize);
                    String id1 = null, id2 = null;
                    for (int j = 0; j < newChunkSize; j++)
                    {
                        if (recDocI.hasNext()) 
                        {
                            RecordAndDoc recDoc = recDocI.next();
                            newRecDoc.add(recDoc);
                            String docID = controlNumOrDefault(recDoc.getRec(), "RecCnt_" + (newChunkSize * 4 + j));
                            if (id1 == null) id1 = docID;
                            id2 = docID;
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
            // less than 20 in the chunk resubmit records one-by-one
            else 
            { 
                logger.debug("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDocId);
                // error on bulk update, resubmit one-by-one
                int num1 = 0;
                while (recDocI.hasNext())
                {
                    RecordAndDoc recDoc = recDocI.next();
                    SolrInputDocument doc = recDoc.getDoc();
                 //   Record rec = recDoc.getRec();
                    logger.debug("Adding single doc with id : "+ controlNumOrDefault(recDoc.getRec(), "RecCnt_" + (num1)));
                    try
                    {
                        @SuppressWarnings("unused")
                        int num = solrProxy.addDoc(doc);
                        num1++;
                        if (errQ != null && !recDoc.errLocs.isEmpty())
                        {
                            errQ.add(recDoc);
                        }
                        logger.debug("Added single doc with id : "+ controlNumOrDefault(recDoc.getRec(), "RecCnt_" + (num1-1)));
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
