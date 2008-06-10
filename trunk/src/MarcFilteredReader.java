

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import java.util.Set;


public class MarcFilteredReader  implements MarcReader
{
    String includeRecordIfFieldPresent = null;
    String includeRecordIfFieldContains = null;
    String includeRecordIfFieldMissing = null;
    String includeRecordIfFieldDoesntContain = null;
    Record currentRecord = null;
    MarcReader reader;
    
    public MarcFilteredReader(MarcReader r, String ifFieldPresent, String ifFieldMissing)
    {
        if (ifFieldPresent != null)
        {
            String present[] = ifFieldPresent.split("/", 2);
            includeRecordIfFieldPresent = present[0];
            if (present.length > 1)
            {
                includeRecordIfFieldContains = present[1];
            }
        }
        if (ifFieldMissing != null)
        {
            String missing[] = ifFieldMissing.split("/", 2);
            includeRecordIfFieldMissing = missing[0];
            if (missing.length > 1)
            {
                includeRecordIfFieldDoesntContain = missing[1];
            }
        }
        reader = r;
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
        while (currentRecord == null)
        {
            if (!reader.hasNext()) return(null);
            Record rec = null;
            try {
                rec = reader.next();
            }
            catch (MarcException me)
            {
                System.err.println("Error reading Marc Record: "+ me.getMessage());               
            }
            if (rec != null && includeRecordIfFieldPresent != null)
            {
                Set<String> fields = SolrIndexer.getFieldList(rec, includeRecordIfFieldPresent);
                if (fields.size() != 0)
                {
                    if (includeRecordIfFieldContains == null || Utils.setItemContains(fields, includeRecordIfFieldContains))
                    {
                        currentRecord = rec;
                    }
                }
            }
            if (rec != null && includeRecordIfFieldMissing != null)
            {
                Set<String> fields = SolrIndexer.getFieldList(rec, includeRecordIfFieldMissing);
                if ((fields.size() == 0 && includeRecordIfFieldDoesntContain == null) ||
                    (fields.size() != 0 && includeRecordIfFieldDoesntContain != null && !Utils.setItemContains(fields, includeRecordIfFieldDoesntContain)))
                {
                    currentRecord = rec;
                }
            }
        }
        return(currentRecord);
    }

}
