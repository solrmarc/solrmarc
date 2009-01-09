package org.solrmarc.tools;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General utility functions for solrmarc
 * 
 * @author Wayne Graham
 * @version $Id$
 */

public final class Utils {
	
	private final static Pattern pattern = Pattern.compile("\\d{1,4}");
	private static Matcher matcher;
	private final static DecimalFormat timeFormat = new DecimalFormat("00.00");
	
	/**
	 * Default Constructor
	 * It's private, so it can't be instantiated by other objects
	 *
	 */	
	private Utils(){ }
	
    /**
     * Check first for a particular property in the System Properties, so that the -Dprop="value" command line arg 
     * mechanism can be used to override values defined in the passed in property file.  This is especially useful
     * for defining the marc.source property to define which file to operate on, in a shell script loop.
     * @param props - property set in which to look.
     * @param propname - name of the property to lookup.
     * @returns String - value stored for that property (or null if it doesn't exist) 
     */
    public static String getProperty(Properties props, String propname)
    {
        String prop;
        if ((prop = System.getProperty(propname)) != null)
        {
            return(prop);
        }
        if ((prop = props.getProperty(propname)) != null)
        {
            return(prop);
        }
        return null;
    }
    
    public static Properties loadProperties(String propertyPath, String propertyFileName)
    {
        Utils utilObj = new Utils();
        String fullPropertyPath = propertyFileName;
        if (propertyPath != null) fullPropertyPath = propertyPath + File.separator + propertyFileName;
        InputStream in = null;
        try {
            in = new FileInputStream(fullPropertyPath);
        }
        catch (FileNotFoundException fnfe)
        {
            URL url = utilObj.getClass().getClassLoader().getResource(propertyFileName);
            if (url == null) url = utilObj.getClass().getResource("/"+propertyFileName);
            if (url == null) url = utilObj.getClass().getClassLoader().getResource(propertyPath+"/"+propertyFileName);
            if (url == null) url = utilObj.getClass().getResource("/"+propertyPath+"/"+propertyFileName);
            try
            {
                in = url.openStream();
            }
            catch (IOException e)
            {
            }           
        }
        // load the properties
        Properties props = new Properties();
        try
        {
            props.load(in);
            in.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(props);
    }

	/**
	 * Cleans non-digits from a String
	 * @param date String to parse
	 * @return Numeric part of date String (or null)
	 */
	public static String cleanDate(final String date){
		
		matcher = pattern.matcher(date);
		
		String cleanDate = null; // raises DD-anomaly
		
		if(matcher.find()){	
			cleanDate = matcher.group();
		} 
		
		return cleanDate;
	}
    
    /**
     * Cleans trailing characters from a String
     * @param title String to parse
     * @return Indexable String
     */
    public static String cleanData(String data)
    {
        String newData = data; 
        String oldData;
        do {
            oldData = newData;
            newData = newData.trim();
            newData = newData.replaceAll(" *([,/;:])$", "");
            if (newData.length() == 0) return(newData);
            if (newData.endsWith("."))
            {
                if (newData.matches(".*\\w\\w\\.$"))
                {
                    newData = newData.substring(0, newData.length()-1);
                }
                else if (newData.matches(".*\\p{L}\\p{L}\\.$"))
                {
                    newData = newData.substring(0, newData.length()-1);
                }
                else if (newData.matches(".*\\w\\p{InCombiningDiacriticalMarks}?\\w\\p{InCombiningDiacriticalMarks}?\\.$"))
                {
                    newData = newData.substring(0, newData.length()-1);
                }
            }
            if (newData.length() > 0 && newData.charAt(0) == '[' && newData.endsWith("]") && 
                    newData.indexOf('[', 1) == -1 && newData.lastIndexOf(']', newData.length()-2) == -1)
            {
                newData = newData.substring(1, newData.length()-1);
            }
            else if (newData.length() > 0 && newData.charAt(0) == '[' && newData.indexOf(']') == -1)
            {
                newData = newData.substring(1);                
            }
            else if (newData.endsWith("]") && newData.indexOf('[') == -1)
            {
                newData = newData.substring(0, newData.length()-1);                
            }
        } while (! newData.equals(oldData));
//        if (!newData.equals(data))  
//        {
//            System.out.println(data + " -> "+ newData); 
//            oldData = newData;
//        }
        return newData ;       
    }

	/**
	 * Call cleanData on an entire set of Strings has a side effect
     * of deleting entries that are identical when they are cleaned.
	 * @param values - the set to clean
	 * @return Set<String> - the "same" set with all of its entries cleaned.
	 */
    private static Set<String> cleanData(Set<String> values)
    {
        Set<String> result = new LinkedHashSet<String>();
        for (String entry : values)
        {
            String cleaned = cleanData(entry);
            result.add(cleaned);
        }
        return(result);
    }
	
	/**
	 * Calculate time from milliseconds
	 * @param totalTime Time in milliseconds
	 * @return Time in the format mm:ss.ss
	 */
	public static String calcTime(final long totalTime)
    {
		return totalTime / 60000 + ":" + timeFormat.format((totalTime % 60000 ) / 1000);
	}
	
	/**
	 * Test if a String has a numeric equivalent
	 * @param number String representation of a number
	 * @return True if String is a number; False if it is not
	 */
	public static boolean isNumber(final String number)
    {
		boolean isNumber; // fix for dd-anomaly
		
		try {
			Integer.parseInt(number);
			isNumber = true;
		}
        catch(NumberFormatException nfe)
        {
			// eat the exception
			isNumber = false;
		}
		
		return isNumber;		
	}

	/**
	 * Remap a field
	 * @param fieldVal
	 * @param map
	 * @param copyEntryIfNotInMap
	 * @return
	 */
    public static String remap(String fieldVal, Map<String, String> map, boolean copyEntryIfNotInMap)
    {
        String result = null;
        
        if (map.keySet().contains("pattern_0"))
        {
            for (int i = 0; i < map.keySet().size(); i++)
            {
                String patternStr = map.get("pattern_"+i);
                String parts[] = patternStr.split("=>");
                if (containsMatch(fieldVal, parts[0]))
                {
                    String newVal = parts[1];
                    if (parts[1].contains("$"))
                    {
                        newVal = fieldVal.replaceAll(parts[0], parts[1]);
                    }
                    result = newVal;             
                }
            }
        }
        if (map.containsKey(fieldVal))
        {
            result = map.get(fieldVal);
        }
        else if (copyEntryIfNotInMap)
        {
            result = fieldVal;
        }                      
        return result;
    }

    /**
     * 
     * @param set
     * @param map
     * @param copyEntryIfNotInMap
     * @return
     */
    public static Set<String> remap(Set<String> set, Map<String, String> map, boolean copyEntryIfNotInMap)
    {
        if (map == null)  return(set);
        Iterator<String> iter = set.iterator();
        Set<String> result = new LinkedHashSet<String>();
        
        while(iter.hasNext())
        {
            String val = iter.next();
            if (map.keySet().contains("pattern_0"))
            {
                for (int i = 0; i < map.keySet().size(); i++)
                {
                    String patternStr = map.get("pattern_"+i);
                    String parts[] = patternStr.split("=>");
                    if (containsMatch(val, parts[0]))
                    {
                    	String newVal = parts[1];
                    	if (parts[1].contains("$"))
                    	{
                    		newVal = val.replaceAll(parts[0], parts[1]);
                    	}
                    	result.add(newVal);                    	
                    }
                }
            }
            else
            {            
                String mappedVal = remap(val, map, copyEntryIfNotInMap);
                if (mappedVal != null)
                {
                    result.add(mappedVal);
                }
            }
        }
        return result;
    }

    private static boolean containsMatch(String val, String pattern)
    {
        String rep = val.replaceFirst(pattern, "###match###");
        
        if (!rep.equals(val)) {
        	return true;
        }
        
        return false;
    }
    
    /**
     * Test if a set contains a specified pattern
     * @param set Set of marc fields to test
     * @param pattern Regex String pattern to match
     * @return If the set contains the pattern, return true, else false
     */
    public static boolean setItemContains(Set<String> set, String pattern)
    {
        if (set.isEmpty()) {
        	return(false);
        }
        
        Iterator<String> iter = set.iterator();        
       
        while (iter.hasNext())
        {
            String value = (String)iter.next();
            
            if (containsMatch(value, pattern)) {
            	return true;
            }
                
        }
        return false;
    }  

    /**
     * Join two fields together with seperator
     * @param set Set of marc fields to join
     * @param separator Separation character to put between 
     * @return Joined fields
     */
    public static String join(Set<String> set, String separator)
    {
        Iterator<String> iter = set.iterator();
        //String result = "";
        StringBuffer result = new StringBuffer("");
       
        while(iter.hasNext())
        {
            //result += iter.next();
        	result.append(iter.next());
            if (iter.hasNext())  {
            	//result += separator;
            	result.append(separator);
            }
        }
        
        return result.toString();
    }

    public static Set<String> trimNearDuplicates(Set<String> locations)
    {
        locations = cleanData(locations);
        if (locations.size() <= 1) return(locations);
        Object locArr[] = locations.toArray();
        int size = locArr.length;
        for (int i = 0; i < size; i++)
        {
            boolean copyStrI = true;
            for (int j = 0; j < size; j++)
            {
                if (i == j) continue;
                if (locArr[j].toString().contains(locArr[i].toString())) { copyStrI = false; break; }                       
            }
            if (copyStrI == false) locations.remove(locArr[i]);   
        }
        return locations;
    }


}
