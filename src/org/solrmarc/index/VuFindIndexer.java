package org.solrmarc.index;
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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.marc4j.marc.Record;
import org.solrmarc.tools.Utils;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class VuFindIndexer extends SolrIndexer
{

	/**
	 * Default constructor
	 * @param propertiesMapFile
	 * @throws Exception
	 */
    public VuFindIndexer(final String propertiesMapFile) throws FileNotFoundException, IOException, ParseException 
    {
        super(propertiesMapFile);
    }
    
    /**
     * Returns the format from a record
     * @param record
     * @return Record format
     */
    public String getFormat(final Record record)
    {
        String leaderChar = getFirstFieldVal(record, "000[7]").toUpperCase();
        String t007Char = getFirstFieldVal(record, "007[0]"); 
        
        if (t007Char != null)  {
        	t007Char = t007Char.toUpperCase(Locale.US);
        }
        
        Set<String> titleH = new LinkedHashSet<String>();
        addSubfieldDataToSet(record, titleH, "245", "h");       
                
        // check with folks to see if leader is more likely
        if("M".equals(leaderChar))      {   return "Book";   }        
        if("S".equals(leaderChar))      {   return "Journal";  }        
        // check the h subfield of the 245 field
        if (Utils.setItemContains(titleH, "electronic resource"))
                                        {   return "Electronic";  }        
        // check the 007
        if(t007Char == null)        	{   return null;  }           
        if("A".equals(t007Char))        {   return "Map";  }           
        if("G".equals(t007Char))        {   return "Slide";  }        
        if("H".equals(t007Char))        {   return "Microfilm";  }        
        if("K".equals(t007Char))        {   return "Photo";  }        
        if("S".equals(t007Char))        {   return "Audio";  }        
        if("V".equals(t007Char) || 
           "M".equals(t007Char))        {   return "Video";  }                           
        return "";
    }
    
    /**
     * Extract the call number label from a record
     * @param record
     * @return Call number label
     */
    public String getCallNumberLabel(final Record record)
    {
        String val = getFirstFieldVal(record, "090a:050a");
        String vals[] = val.split("[^A-Za-z]+", 2);
        
        if (vals.length == 0 || vals[0] == null || vals[0].length() == 0) {
        	return(null);
        }
        
        return(vals[0]);
    }

}
