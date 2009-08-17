package org.solrmarc.marc;

import java.util.List;

import org.apache.log4j.Logger;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class MarcCombiningReader implements MarcReader
{
    Record currentRecord = null;
    Record nextRecord = null;
    MarcReader reader;
    String idsToMerge = null;

    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcFilteredReader.class.getName());

    MarcCombiningReader(MarcReader reader, String idsToMerge)
    {
        this.reader = reader;
        this.idsToMerge = idsToMerge;
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
