package org.solrmarc.marc;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.tools.Utils;

import java.util.Set;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
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
