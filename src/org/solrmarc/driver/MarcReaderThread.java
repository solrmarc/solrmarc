package org.solrmarc.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

public class MarcReaderThread extends Thread
{
    private final static Logger logger = Logger.getLogger(MarcReaderThread.class);
    private AtomicInteger cnts[];
    private final MarcReader reader;
    private final BlockingQueue<Record> readQ;
    private boolean doneReading = false;
    
    public MarcReaderThread(final MarcReader reader, BlockingQueue<Record> readQ, AtomicInteger cnts[])
    {
        super("MarcReader-Thread");
        this.reader = reader;
        this.readQ = readQ;
        this.cnts = cnts;
    }
    
    @Override 
    public void run()
    {
        Record record = null;
        while (!Thread.currentThread().isInterrupted())
        {
            try {
                if (reader.hasNext())
                    record = reader.next();
                else 
                    break;
            }
            catch (MarcException me)
            {
                logger.error("Unrecoverable Error in MARC record data", me);
                if (Boolean.parseBoolean(System.getProperty("solrmarc.terminate.on.marc.exception", "true")))
                    break;
                else 
                {
                    logger.warn("Trying to continue after MARC record data error");
                    continue;
                }
            }

            cnts[0].incrementAndGet();
            while (readQ.offer(record) == false)
            {
                try
                {
                    // queue is full, wait until it drains sowewhat
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (Thread.currentThread().isInterrupted())
        {
            Collection<Record> discardedRecords = new ArrayList<>();
            readQ.drainTo(discardedRecords);
            if (discardedRecords.size() > 0)
            {
                String id = discardedRecords.iterator().next().getControlNumber();
                logger.warn("Reader Thread: discarding unprocessed records starting with record: "+ id);
            }
            else
            {
                String id = (record != null) ? record.getControlNumber() : "<none>";
                logger.warn("Reader Thread Interrupted: last record processed was: "+ id);
            }
            cnts[0].addAndGet(-discardedRecords.size());
        }
        doneReading = true;
    }

    public boolean isDoneReading()
    {
        return doneReading;
    }
}
