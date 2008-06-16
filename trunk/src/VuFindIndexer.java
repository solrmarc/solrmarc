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


import java.util.LinkedHashSet;
import java.util.Set;

import org.marc4j.marc.Record;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class VuFindIndexer extends SolrIndexer
{

    public VuFindIndexer(String propertiesMapFile) throws Exception
    {
        super(propertiesMapFile);
    }
    
    public String getFormat(Record record)
    {
        String leaderChar = getFirstFieldVal(record, "000[7]").toUpperCase();
        String t007Char = getFirstFieldVal(record, "007[0]"); 
        if (t007Char != null)  t007Char = t007Char.toUpperCase();
        Set<String> titleH = new LinkedHashSet<String>();
        addSubfieldDataToSet(record, titleH, "245", "h");       
                
        // check with folks to see if leader is more likely
        if(leaderChar.equals("M"))      {   return "Book";   }        
        if(leaderChar.equals("S"))      {   return "Journal";  }        
        // check the h subfield of the 245 field
        if (Utils.setItemContains(titleH, "electronic resource"))
                                        {   return "Electronic";  }        
        // check the 007
        if(t007Char == null)        	{   return null;  }           
        if(t007Char.equals("A"))        {   return "Map";  }           
        if(t007Char.equals("G"))        {   return "Slide";  }        
        if(t007Char.equals("H"))        {   return "Microfilm";  }        
        if(t007Char.equals("K"))        {   return "Photo";  }        
        if(t007Char.equals("S"))        {   return "Audio";  }        
        if(t007Char.equals("V") || 
           t007Char.equals("M"))        {   return "Video";  }                           
        return "";
    }
    
    public String getCallNumberLabel(Record record)
    {
        String val = getFirstFieldVal(record, "090a:050a");
        String vals[] = val.split("[^A-Za-z]+", 2);
        if (vals.length == 0 || vals[0] == null || vals[0].length() == 0) return(null);
        return(vals[0]);
    }

}
