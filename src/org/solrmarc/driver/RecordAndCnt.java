package org.solrmarc.driver;

import org.marc4j.marc.Record;

/**
 * This class is used to return a MARC record from a MarcReader object. 
 * It also returns the total count of records returned so far, since they
 * can be processed out of order when multi-threaded indexing is enabled
 * The count is only used for error reporting purposes.
 *  
 * @author rh9ec
 *
 */
public class RecordAndCnt
{
    final private Record record;
    final private int    cnt;    
   
    /**
     * Constructor for a RecordAndInt object for which contains a MARC record and an int
     *
     * @param record - The MARC record being processed.
     * @param cnt - The total number of MARC records returned so far.
     */
    RecordAndCnt(Record record, int cnt)
    {
        this.record = record;
        this.cnt = cnt;
    }

    Record getRecord()
    {
        return record;
    }

    int getCnt()
    {
        return cnt;
    }

}
