package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;

public class MarcReaderThread extends Thread
{
    private final static Logger logger = Logger.getLogger(MarcReaderThread.class);
    private final MarcReader reader;
    private final Indexer indexer;
    private final BlockingQueue<RecordAndCnt> readQ;
    private boolean doneReading = false;

    public MarcReaderThread(final MarcReader reader, final Indexer indexer, final BlockingQueue<RecordAndCnt> readQ, AtomicInteger cnts[])
    {
        super("MarcReader-Thread");
        this.reader = reader;
        this.indexer = indexer;
        this.readQ = readQ;
    }

    @Override
    public void run()
    {
        RecordAndCnt recordAndCnt = null;
        while (!Thread.currentThread().isInterrupted())
        {
            recordAndCnt = indexer.getRecord(reader);
            if (recordAndCnt == null) break;
            try {
                readQ.put(recordAndCnt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
        if (Thread.currentThread().isInterrupted())
        {
            flushReadQueue(recordAndCnt);
        }
        doneReading = true;
    }

    private void flushReadQueue(RecordAndCnt recordAndCnt)
    {
        Collection<RecordAndCnt> discardedRecords = new ArrayList<>();
        readQ.drainTo(discardedRecords);
        if (discardedRecords.size() > 0)
        {
            String id = discardedRecords.iterator().next().getRecord().getControlNumber();
            logger.warn("Reader Thread: discarding unprocessed records starting with record: "+ id);
        }
        else
        {
            String id = (recordAndCnt != null && recordAndCnt.getRecord() != null) ? recordAndCnt.getRecord().getControlNumber() : "<none>";
            logger.warn("Reader Thread Interrupted: last record processed was: "+ id);
        }
        indexer.addToCnt(0, -discardedRecords.size());
    }

    public boolean isDoneReading(boolean shuttingDown)
    {
        if (shuttingDown && doneReading && readQ.size() > 0)
        {
            flushReadQueue(null);
        }
        return doneReading;
    }
}
