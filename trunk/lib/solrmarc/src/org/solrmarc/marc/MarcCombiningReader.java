package org.solrmarc.marc;

import java.util.List;

import org.apache.log4j.Logger;
import org.marc4j.ErrorHandler;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;


/**
 * @author rh9ec
 * 
 * Binary Marc records have a maximum size of 99999 bytes.  In the data dumps from 
 * the Sirsi/Dynix Virgo system if a record with all of its holdings information 
 * attached would be greater that that size, the records is written out multiple
 * times with each subsequent record containing a subset of the total holdings information.
 * This class reads ahead to determine when the next record in a Marc file is actually 
 * a continuation of the same record.   When this occurs, the holdings information in the
 * next record is appended to/merged with the in-memory Marc record representation already 
 * read. 
 *
 */

public class MarcCombiningReader implements MarcReader
{
    Record currentRecord = null;
    Record nextRecord = null;
    MarcReader reader;
    String idsToMerge = null;
    ErrorHandler nextErrors;
    ErrorHandler currentErrors;
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcFilteredReader.class.getName());

    
    /**
     * Constructor for a "combining" Marc reader, that looks ahead at the Marc file to determine 
     * when the next record is a continuation of the currently read record.  
     * 
     * @param reader - The Lower level MarcReader that returns Marc4J Record objects that are read from a Marc file.
     * @param idsToMerge - string representing a regular expression matching those fields to be merged for continuation records.
     */
    MarcCombiningReader(MarcReader reader, String idsToMerge)
    {
        this.reader = reader;
        this.idsToMerge = idsToMerge;
        this.nextErrors = null;
        this.currentErrors = null;
    }
    
    /**
     * Constructor for a "combining" Marc reader, that looks ahead at the Marc file to determine 
     * when the next record is a continuation of the currently read record.  Because this reader 
     * needs to have two records in memory to determine when the subsequent record is a continuation,
     * if Error Handling is being performed, this constructor needs to be used, so that the errors 
     * from the "next" record are not appended to the results for the "current" record.
     * Call this constructor in the following way:
     *          ErrorHandler errors2 = errors;
     *          errors = new ErrorHandler();
     *          reader = new MarcCombiningReader(reader, errors, errors2, combineConsecutiveRecordsFields);
     *          
     * @param reader - The Lower level MarcReader that returns Marc4J Record objects that are read from a Marc file.
     * @param currentErrors - ErrorHandler Object to use for attaching errors to a record.
     * @param nextErrors - ErrorHandler Object that was passed into the lower level MarcReader
     * @param idsToMerge - string representing a regular expression matching those fields to be merged for continuation records.
     */
    MarcCombiningReader(MarcReader reader, ErrorHandler currentErrors, ErrorHandler nextErrors, String idsToMerge)
    {
        this.reader = reader;
        this.idsToMerge = idsToMerge;
        this.nextErrors = nextErrors;
        this.currentErrors = currentErrors;
    }
   
    public boolean hasNext()
    {
        if (currentRecord == null) 
        { 
            currentRecord = next(); 
        }
        return(currentRecord != null);
    }

    public Record next()
    {
        if (currentRecord != null) 
        { 
            Record tmp = currentRecord; 
            currentRecord = null; 
            return(tmp);
        }
        
        else if (currentRecord == null)
        {
            if (nextRecord != null) 
            { 
                currentRecord = nextRecord;
                copyErrors(currentErrors, nextErrors);
                nextRecord = null; 
            }
            if (!reader.hasNext()) 
            {
                return ((currentRecord != null) ? next() : null);
            }
                       
            try {
                nextRecord = reader.next();
            }
            catch (MarcException me)
            {
                //System.err.println("Error reading Marc Record: "+ me.getMessage());  
//              exception = new SolrMarcException(me.getMessage(), me.getCause());
//              exception.printMessage("Error reading Marc record:");
//              exception.printStackTrace();
                logger.error("Error reading Marc Record.");
                logger.error(me.getMessage());
            }
            while (currentRecord != null && nextRecord != null &&
                    currentRecord.getControlNumber() != null && 
                    currentRecord.getControlNumber().equals(nextRecord.getControlNumber()))
            {
                currentRecord = combineRecords(currentRecord, nextRecord);
                mergeErrors(currentErrors, nextErrors);
                if (reader.hasNext())
                {
                    try {
                        nextRecord = reader.next();
                    }
                    catch (MarcException me)
                    {
                        //System.err.println("Error reading Marc Record: "+ me.getMessage());  
    //                  exception = new SolrMarcException(me.getMessage(), me.getCause());
    //                  exception.printMessage("Error reading Marc record:");
    //                  exception.printStackTrace();
                        logger.error("Error reading Marc Record.");
                        logger.error(me.getMessage());
                    }
                }
                else 
                {
                    nextRecord = null;
                }
            }
            return(next());
        }
        return(null);
    }

    private void copyErrors(ErrorHandler currentErr, ErrorHandler nextErr)
    {
        if (currentErr != null && nextErr != null)
        {
            currentErr.reset();
            mergeErrors(currentErr, nextErr);
        }
    }

    private void mergeErrors(ErrorHandler currentErr, ErrorHandler nextErr)
    {
        if (currentErr != null && nextErr != null)
        {
            currentErr.addErrors(nextErr.getErrors());
        }
    }

    private Record combineRecords(Record currentRecord, Record nextRecord)
    {
        List fields = nextRecord.getVariableFields();
        for (Object field : fields)
        {
            if (field instanceof ControlField)
            {
                ControlField cf = (ControlField) field;
                if (cf.getTag().matches(idsToMerge))
                {
                    currentRecord.addVariableField(cf);
                }
            }
            else if (field instanceof DataField)
            {
                DataField df = (DataField) field;
                if (df.getTag().matches(idsToMerge))
                {
                    currentRecord.addVariableField(df);
                }
            }
        }
        return(currentRecord);
    }

}
