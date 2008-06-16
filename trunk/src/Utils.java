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


import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General untility functions for solrmarc
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
            if (newData.endsWith("."))
            {
                if (newData.matches(".*\\w\\w\\w\\.$"))
                {
                    newData = newData.substring(0, newData.length()-1);
                }
            }
            if (newData.startsWith("[") && newData.endsWith("]"))
            {
                newData = newData.substring(1, newData.length()-1);
            }
            else if (newData.startsWith("[") && newData.indexOf("]") == -1)
            {
                newData = newData.substring(1);                
            }
            else if (newData.endsWith("]") && newData.indexOf("[") == -1)
            {
                newData = newData.substring(0, newData.length()-1);                
            }
        } while (! newData.equals(oldData));
//        if (!newData.equals(data))  
//        {
//            System.out.println(data + " -> "+ newData); 
//            oldData = newData;
//        }
        return(newData);       
    }

	/**
	 * Cleans trailing characters from a String
	 * @param title String to parse
	 * @return Indexable String
	 */
//	public static String cleanTitle(final String title)
//    {
//		final int titleLength = title.length();		
//		final String lastCharacter = title.substring(titleLength - 1);
//		
//		String cleanTitle;
//		
//		if("/".equals(lastCharacter) || ":".equals(lastCharacter))
//        {
//			cleanTitle = title.substring(0, titleLength - 1);
//		} else {
//			cleanTitle = title;
//		}
//
//		return cleanTitle.trim();
//		
//	}
	
	/**
	 * Calculate time from milliseconds
	 * @param totalTime Time in milliseconds
	 * @return Time in the format mm:ss.ss
	 */
	public static String calcTime(final long totalTime)
    {
		final long minutes = totalTime / 60000;
		final long seconds = ( totalTime % 60000 ) / 1000;
		
		return minutes + ":" + timeFormat.format(seconds);
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

    public static String remap(String fieldVal, Map<String, String> map, boolean copyEntryIfNotInMap)
    {
        String result = null;
        if (map.containsKey(fieldVal))
        {
            result = map.get(fieldVal);
        }
        else if (copyEntryIfNotInMap)
        {
            result = fieldVal;
 //           System.out.println("Missing Value: "+ result);
        }                      
        return result;
    }

    public static Set<String> remap(Set<String> s, Map<String, String> map, boolean copyEntryIfNotInMap)
    {
        if (map == null)  return(s);
        Iterator<String> iter = s.iterator();
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
                        result.add(parts[1]);
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
        return(result);
    }

    private static boolean containsMatch(String val, String pattern)
    {
        String rep = val.replaceFirst(pattern, "###match###");
        if (!rep.equals(val)) return(true);
        return(false);
    }
    
    public static boolean setItemContains(Set set, String pattern)
    {
        if (set.isEmpty())  return(false);
        Iterator iter = set.iterator();        
        while (iter.hasNext())
        {
            String value = (String)iter.next();
            if (containsMatch(value, pattern)) 
                return(true);
        }
        return false;
    }  

    static String join(Set<String> s, String sep)
    {
        Iterator<String> iter = s.iterator();
        String result = "";
        while(iter.hasNext())
        {
            result += iter.next();
            if (iter.hasNext())  result += "; ";
        }
        return(result);
    }


}
