package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.tools.SolrMarcIndexerException;

public class ThreadedIndexer extends Indexer
{   
    private final static Logger logger = Logger.getLogger(ThreadedIndexer.class);
    private final BlockingQueue<Record> readQ;
    private final BlockingQueue<RecordAndDoc> docQ;
    MarcReaderThread readerThread;
    Thread indexerThread;
    ExecutorService solrExecutor;
   
    boolean doneReading = false;
    final int buffersize; 
    final int cnts[] = new int[3];
    
    public ThreadedIndexer(List<AbstractValueIndexer<?>> indexers, SolrProxy solrProxy, int buffersize)
    {
        super(indexers, solrProxy);
        readQ = new ArrayBlockingQueue<Record>(buffersize);
        docQ = new ArrayBlockingQueue<RecordAndDoc>(buffersize);
        doneReading = false;
//        solrExecutor = Executors.newSingleThreadExecutor();
        solrExecutor = Executors.newFixedThreadPool(4);
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
        readerThread = new MarcReaderThread(reader, readQ, cnts);      
        indexerThread = new Thread(new IndexerThread(readerThread, readQ, docQ, this, solrExecutor, cnts));
        
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
