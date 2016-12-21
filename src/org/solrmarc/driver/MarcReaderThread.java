package org.solrmarc.driver;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

public class MarcReaderThread extends Thread
{
    private final static Logger logger = Logger.getLogger(MarcReaderThread.class);
    private AtomicInteger cnts[];
    private final MarcReader reader;
    private final Indexer indexer;
    private final BlockingQueue<AbstractMap.SimpleEntry<Integer, Record>> readQ;
    private boolean doneReading = false;

    public MarcReaderThread(final MarcReader reader, final Indexer indexer, BlockingQueue<AbstractMap.SimpleEntry<Integer, Record>> readQ, AtomicInteger cnts[])
    {
        super("MarcReader-Thread");
        this.reader = reader;
        this.indexer = indexer;
        this.readQ = readQ;
        this.cnts = cnts;
    }

    @Override
    public void run()
    {
        Record record = null;
        while (!Thread.currentThread().isInterrupted())
        {
            record = indexer.getRecord(reader);
            if (record == null) break;
            try {
                readQ.put(new AbstractMap.SimpleEntry<Integer, Record>(cnts[0].get(), record));
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        if (Thread.currentThread().isInterrupted())
        {
            flushReadQueue(record);
        }
        doneReading = true;
    }

    private void flushReadQueue(Record record)
    {
        Collection<AbstractMap.SimpleEntry<Integer, Record>> discardedRecords = new ArrayList<>();
        readQ.drainTo(discardedRecords);
        if (discardedRecords.size() > 0)
        {
            String id = discardedRecords.iterator().next().getValue().getControlNumber();
            logger.warn("Reader Thread: discarding unprocessed records starting with record: "+ id);
        }
        else
        {
            String id = (record != null) ? record.getControlNumber() : "<none>";
            logger.warn("Reader Thread Interrupted: last record processed was: "+ id);
        }
        cnts[0].addAndGet(-discardedRecords.size());
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
