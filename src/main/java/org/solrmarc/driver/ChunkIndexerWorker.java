package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.marc.Record;

/**
 *  <h1>ChunkIndexerThread</h1>
 *
 *  This class implements sending batches of documents to Solr.  It implements retries to cope with
 *  the issue where one bad document in a batch will cause all subsequent solr input documents in
 *  the batch to be skipped.
 *  <p>
 *  To accomplish this the class will divide the batch into several smaller segments, and re-try sending
 *  those smaller batches.   Eventually a sub-batch containing the problem record will be sent one-by-one
 *  to insure that all valid documents are correctly sent to Solr, while only the documents containing
 *  errors are skipped.
 *  </p>
 *  <p>
 *  If the parameter errQ is not null the records that cause an error will be appended to that list and
 *  can subsequently be logged or fixed and retried.
 *  </p>
 *
 * @author rh9ec
 *
 */

public class ChunkIndexerWorker implements Runnable
{
    private final static Logger logger = LogManager.getLogger(ChunkIndexerWorker.class);
    final String threadName;
    final Collection<SolrInputDocument> docs;
    final Collection<RecordAndDoc> recordAndDocs;
    final Indexer indexer;
    final BlockingQueue<RecordAndDoc> errQ;
    String firstDocId = null;
    String lastDocId = null;
    boolean trackProgress = false;

    public ChunkIndexerWorker(String threadName, Collection<RecordAndDoc> recordAndDocs,
            BlockingQueue<RecordAndDoc> errQ, Indexer indexer)
    {
        this.threadName = threadName;
        this.recordAndDocs = recordAndDocs;
        this.docs = buildDocList(recordAndDocs);
        this.errQ = errQ;
        this.indexer = indexer;
        this.trackProgress = Boolean.parseBoolean(System.getProperty("org.solrmarc.track.solr.progress", "false"));
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
        Thread.currentThread().setName(threadName);
        int inChunk = docs.size();
//        if (logger.isDebugEnabled())
//        {
//            int totalSize = 0;
//            for (SolrInputDocument doc : docs)
//            {
//                doc.values().
//            }
//        }
        logger.debug("Adding chunk of "+inChunk+ " documents -- starting with id : "+firstDocId);
        try {
            // If all goes well, this is all we need. Add the docs, count the docs, and, if desired, return the docs that contain errors
            int cnt = indexer.solrProxy.addDocs(docs);
            indexer.addToCnt(2, cnt);
            logger.debug("Added chunk of "+cnt+ " documents -- starting with id : "+firstDocId);
            if (trackProgress || logger.isDebugEnabled())
            {
                logger.info("Total sent so far: " + indexer.getCounts()[2]);
            }

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
                indexer.singleRecordSolrError(recDoc, e, errQ);
            }
            else if (inChunk > 20)
            {
                logger.warn("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDocId);
                logger.info("   exception reported is: ", e);
                int newChunkSize = inChunk / 4;
                Runnable subChunk[] = new Runnable[4];

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
                    subChunk[i] = new ChunkIndexerWorker("SolrUpdateOnError_"+id1+"_"+id2, newRecDoc, errQ, indexer);
                    subChunk[i].run();
                }
            }
            // less than 20 in the chunk resubmit records one-by-one
            else
            {
                logger.warn("Failed on chunk of "+inChunk+ " documents -- starting with id : "+firstDocId);
                logger.warn("   exception reported is: ", e);
                // error on bulk update, resubmit one-by-one
                while (recDocI.hasNext())
                {
                    RecordAndDoc recDoc = recDocI.next();

                    indexer.indexSingleDocument(recDoc);
                }
            }
        }
    }
}
