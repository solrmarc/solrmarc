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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.Logger;
import org.marc4j.*;
import org.marc4j.marc.*;
import org.solrmarc.marc.MarcImporter;
import org.solrmarc.tools.Utils;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class SolrIndexer
{
	/** map: keys are solr field names, values inform how to get solr field values */
    private Map<String, String[]> fieldMap = null;

    /** map of translation maps.  keys are names of translation maps; 
     *  values are the translation maps (hence, it's a map of maps) */
    private Map<String, Map<String, String>> transMapMap = null;

    /** current datestamp for indexed solr document */
    private Date indexDate = null;

    /** list of path to look for property files in */
    private String propertyFilePaths[];
        
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcImporter.class.getName());

    /**
     * private constructor; initializes fieldMap, transMapMap and indexDate to empty
     *  objects
     */
    private SolrIndexer()
    {
        fieldMap = new HashMap<String, String[]>();
        transMapMap = new HashMap<String, Map<String, String>>();
        indexDate = new Date();
    }
    
    /**
     * Constructor
     * @param indexingPropsFile the x_index.properties file mapping solr
     *  field names to values in the marc records
     * @param propertyDirs - array of directories holding properties files     
     */
    public SolrIndexer(String indexingPropsFile, String propertyDirs[])
    {
        this();
        propertyFilePaths = propertyDirs;
        Properties indexingProps = Utils.loadProperties(propertyFilePaths, indexingPropsFile);
        fillMapFromProperties(indexingProps);
    }

    /**
     * Parse the properties file and load parameters into fieldMap.  Also
     *  populate transMapMap and indexDate
     * @param props _index.properties as Properties object
     */
    protected void fillMapFromProperties(Properties props)
    {
        Enumeration<?> en = props.propertyNames();
        
        while (en.hasMoreElements())
        {
            String propName = (String) en.nextElement();
            
            // ignore map, pattern_map; they are handled separately
            if (!propName.startsWith("map") &&
                !propName.startsWith("pattern_map"))
            {
                String propValue = props.getProperty(propName);
                String fieldDef[] = new String[4];
                fieldDef[0] = propName;
                fieldDef[3] = null;
                if (propValue.startsWith("\""))
                {
                	// value is a constant if it starts with a quote
                    fieldDef[1] = "constant";
                    fieldDef[2] = propValue.trim().replaceAll("\"", "");
                }
                else  // not a constant
                {
					// split it into two pieces at first comma or space 
                    String values[] = propValue.split("[, ]+", 2);
                    if (values[0].equals("custom"))
                    {
                        fieldDef[1] = "custom";

                        // parse sections of custom value assignment line in 
                        //   _index.properties file
                        String lastValues[];
                        // get rid of empty parens
                        if (values[1].indexOf("()") != -1)
                            values[1] = values[1].replace("()", "");

                        // index of first open paren after custom method name
                        int parenIx = values[1].indexOf('(');

                        // index of first unescaped comma after method name
                        int commaIx = Utils.getIxUnescapedComma(values[1]);

                        if (parenIx != -1 && commaIx != -1 && parenIx < commaIx)
                        	// remainder should be split after close paren
                        	//  followed by comma (optional spaces in between)
                            lastValues = values[1].trim().split("\\) *,", 2);
                        else // no parens - split comma preceded by optional spaces
                            lastValues = values[1].trim().split(" *,", 2);                         

                        fieldDef[2] = lastValues[0].trim();
     
                        fieldDef[3] = lastValues.length > 1 ? lastValues[1].trim() : null; 
                        // is this a translation map?
                        if (fieldDef[3] != null && fieldDef[3].contains("map"))
                        {
                            try
                            {
                                fieldDef[3] = loadTranslationMap(props, fieldDef[3]);
                        	}
                            catch (IllegalArgumentException e)
                        	{
                                logger.error("Unable to find file containing specified translation map (" + fieldDef[3] + ")");
                                throw new IllegalArgumentException("Error: Problems reading specified translation map (" + fieldDef[3] + ")");
                        	}
                        }
                    }  // end custom
                    else if (values[0].equals("xml") ||
                             values[0].equals("raw") ||
                             values[0].equals("date") ||
                             values[0].equals("index_date") ||
                             values[0].equals("era"))
                    {
                        fieldDef[1] = "std";
                        fieldDef[2] = values[0];
                        fieldDef[3] = values.length > 1 ? values[1].trim() : null;
                        // NOTE:  assuming no translation map here
                    }
                    else if (values[0].equalsIgnoreCase("FullRecordAsXML") ||
                             values[0].equalsIgnoreCase("FullRecordAsMARC") ||
                             values[0].equalsIgnoreCase("DateOfPublication") ||
                             values[0].equalsIgnoreCase("DateRecordIndexed"))
                    {
                        fieldDef[1] = "std";
                        fieldDef[2] = values[0];
                        fieldDef[3] = values.length > 1 ? values[1].trim() : null;
                        // NOTE:  assuming no translation map here
                    }
                    else if (values.length == 1)
                    {
                        fieldDef[1] = "all";
                        fieldDef[2] = values[0];
                        fieldDef[3] = null;
                    }
                    else // other cases of field definitions
                    {
                        String values2[] = values[1].trim().split("[ ]*,[ ]*", 2);
                        fieldDef[1] = "all";
                        if (values2[0].equals("first") ||
                        		(values2.length > 1 && values2[1].equals("first")))
                            fieldDef[1] = "first";
                        
                        if (values2[0].startsWith("join"))
                            fieldDef[1] = values2[0];

                        if ((values2.length > 1 && values2[1].startsWith("join")))
                            fieldDef[1] = values2[1];

                        if (values2[0].equalsIgnoreCase("DeleteRecordIfFieldEmpty") ||
                            (values2.length > 1 && values2[1].equalsIgnoreCase("DeleteRecordIfFieldEmpty")))
                            fieldDef[1] = "DeleteRecordIfFieldEmpty";

                        fieldDef[2] = values[0];
                        fieldDef[3] = null;
                        
                        // might we have a translation map?
                        if (!values2[0].equals("all") &&
                            !values2[0].equals("first") &&
                            !values2[0].startsWith("join") &&
                            !values2[0].equalsIgnoreCase("DeleteRecordIfFieldEmpty"))
                        {
                            fieldDef[3] = values2[0].trim();
		                    if (fieldDef[3] != null)
		                    {
		                        try
		                        {
		                            fieldDef[3] = loadTranslationMap(props, fieldDef[3]);
		                        }
		                        catch (IllegalArgumentException e)
		                        {
		                            logger.error("Unable to find file containing specified translation map (" + fieldDef[3] + ")");
		                            throw new IllegalArgumentException("Error: Problems reading specified translation map (" + fieldDef[3] + ")");
		                        }
		                    }
		                }
                    } // other cases of field definitions
                    
                } // not a constant

                fieldMap.put(propName, fieldDef);

            } // if not map or pattern_map

        }  // while enumerating through property names
        
        // verify that fieldMap is valid
        verifyCustomMethodsAndTransMaps();
    }

	/**
	 * Verify that custom methods are available and translation maps are in 
	 *  transMapMap
	*/
	private void verifyCustomMethodsAndTransMaps() 
    {
        for (String key : fieldMap.keySet())
        {
            String fieldMapVal[] = fieldMap.get(key);
            String indexType = fieldMapVal[1];
            String indexParm = fieldMapVal[2];
            String mapName = fieldMapVal[3];
            
            if (indexType.equals("custom"))
            	verifyCustomMethodExists(indexParm);
            
            // check that translation maps are present in transMapMap
            if (mapName != null && findMap(mapName) == null)
            {
//                System.err.println("Error: Specified translation map (" + mapName + ") not found in properties file");
                logger.error("Specified translation map (" + mapName + ") not found in properties file");
                throw new IllegalArgumentException("Specified translation map (" + mapName + ") not found in properties file");
            }
        }
	}
            
	/**
	 * verify that custom methods defined in the _index properties file are
	 *   present and accounted for
	 * @param indexParm - name of custom function plus args
	 */
    private void verifyCustomMethodExists(String indexParm) 
    {
        try
        {
            Method method = null;
            int parenIx = indexParm.indexOf("(");
            if (parenIx != -1)
            {
                String functionName = indexParm.substring(0, parenIx);
                String parmStr = indexParm.substring(parenIx + 1, indexParm.lastIndexOf(')'));
                // parameters are separated by unescaped commas
                String parms[] = parmStr.trim().split("[^\\\\],");
                int numparms = parms.length;
                Class<?> parmClasses[] = new Class<?>[numparms+1];
                parmClasses[0] = Record.class;
                for (int i = 0 ; i < numparms; i++)  { 
                	parmClasses[i+1] = String.class; 
                }
                method = getClass().getMethod(functionName, parmClasses);
            }
            else 
                method = getClass().getMethod(indexParm, new Class[] { Record.class });

            Class<?> retval = method.getReturnType();
            // if (!method.isAccessible())
            // {
            //   System.err.println("Error: Unable to invoke custom indexing function "+indexParm);
            //   valid = false;
            // }
            if (!(Set.class.isAssignableFrom(retval) || String.class.isAssignableFrom(retval) ||
                    Map.class.isAssignableFrom(retval)) )
            {
//                System.err.println("Error: Return type of custom indexing function " + indexParm + " must be either String or Set<String>");
                logger.error("Error: Return type of custom indexing function " + indexParm + " must be String or Set<String> or Map<String, String>");
                throw new IllegalArgumentException("Error: Return type of custom indexing function " + indexParm + " must be String or Set<String> or Map<String, String>");
            }
        }
        catch (SecurityException e)
        {
//            System.err.println("Error: Unable to invoke custom indexing function " + indexParm);
            logger.error("Unable to invoke custom indexing function " + indexParm);
            logger.debug(e.getCause(), e);
            throw new IllegalArgumentException("Unable to invoke custom indexing function " + indexParm);
        }
        catch (NoSuchMethodException e)
        {
//            System.err.println("Error: Unable to find custom indexing function " + indexParm);
            logger.error("Unable to find custom indexing function " + indexParm);
            logger.debug(e.getCause());
            throw new IllegalArgumentException("Unable to find custom indexing function " + indexParm);
        }
        catch (IllegalArgumentException e)
        {
//            System.err.println("Error: Unable to find custom indexing function " + indexParm);
            logger.error("Unable to find custom indexing function " + indexParm);
            logger.debug(e.getCause());
            throw new IllegalArgumentException("Unable to find custom indexing function " + indexParm);
        }
    }


    /**
     * load the translation map into transMapMap
     * @param indexProps _index.properties as Properties object
     * @param translationMapSpec the specification of a translation map - 
     *   could be name of a _map.properties file, or something in _index 
     *   properties ...
     * @return the name of the translation map
     */
    protected String loadTranslationMap(Properties indexProps, String translationMapSpec) 
    {
        if (translationMapSpec.length() == 0)
            return null;

        String mapName = null;
        String mapKeyPrefix = null;
        if (translationMapSpec.startsWith("(") &&
                 translationMapSpec.endsWith(")"))
        {
            // translation map entries are in passed Properties object
            mapName = translationMapSpec.replaceAll("[\\(\\)]", "");
            mapKeyPrefix = mapName;
            loadTranslationMapValues(indexProps, mapName, mapKeyPrefix);
        }
        else
        {
        	// translation map is a separate file
        	String transMapFname = null;
        	if (translationMapSpec.contains("(") &&
                 translationMapSpec.endsWith(")"))
        	{
        		String mapSpec[] = translationMapSpec.split("(//s|[()])+");
        		transMapFname = mapSpec[0];
        		mapName = mapSpec[1];
        		mapKeyPrefix = mapName;
        	}
        	else
        	{
                transMapFname = translationMapSpec;
                mapName = translationMapSpec.replaceAll(".properties", "");
                mapKeyPrefix = "";
        	}
        	
            if (findMap(mapName) == null)
                loadTranslationMapValues(transMapFname, mapName, mapKeyPrefix);
        }
        
        return mapName;
    }

    /**
     * Load translation map into transMapMap.  Look for translation map in 
     * site specific directory first; if not found, look in solrmarc top 
     * directory
     * @param transMapName name of translation map file to load
     * @param mapName - the name of the Map to go in transMapMap (the key in transMapMap)
     * @param mapKeyPrefix - any prefix on individual Map keys (entries in the
     *   value in transMapMap) 
     */
    private void loadTranslationMapValues(String transMapName, String mapName, String mapKeyPrefix)
    {
    	Properties props = null;
        props = Utils.loadProperties(propertyFilePaths, transMapName);
        logger.info("Loading Custom Map: " + transMapName);
        loadTranslationMapValues(props, mapName, mapKeyPrefix);
    }

    /**
     * populate transMapMap
     * @param transProps - the translation map as a Properties object
     * @param mapName - the name of the Map to go in transMapMap (the key in transMapMap)
     * @param mapKeyPrefix - any prefix on individual Map keys (entries in the
     *   value in transMapMap) 
     */
    private void loadTranslationMapValues(Properties transProps, String mapName, String mapKeyPrefix)
    {
        Enumeration<?> en = transProps.propertyNames();
        while (en.hasMoreElements())
        {
            String property = (String) en.nextElement();
            if (mapKeyPrefix.length() == 0 || property.startsWith(mapKeyPrefix))
            {
                String mapKey = property.substring(mapKeyPrefix.length());
                if (mapKey.startsWith(".")) 
                	mapKey = mapKey.substring(1);
                String value = transProps.getProperty(property);
                value = value.trim();
                if (value.equals("null")) 
                	value = null;

                Map<String, String> valueMap;
                if (transMapMap.containsKey(mapName))
                    valueMap = transMapMap.get(mapName);
                else
                {
                    valueMap = new LinkedHashMap<String, String>();
                    transMapMap.put(mapName, valueMap);
                }

                valueMap.put(mapKey, value);
            }
        }
    }

    /**
     * Given a record, return a Map of solr fields (keys are field names, values
     *  are an Object containing the values (a Set or a String)
     */
    public Map<String, Object> map(Record record)
    {
        Map<String, Object> indexMap = new HashMap<String, Object>();
        
        for (String key : fieldMap.keySet())
        {
            String fieldVal[] = fieldMap.get(key);
            String indexField = fieldVal[0];
            String indexType = fieldVal[1];
            String indexParm = fieldVal[2];
            String mapName = fieldVal[3];
            
            if (indexType.equals("constant"))
                addField(indexMap, indexField, indexParm);
            else if (indexType.equals("first"))
                addField(indexMap, indexField, getFirstFieldVal(record, mapName, indexParm));
            else if (indexType.equals("all"))
                addFields(indexMap, indexField, mapName, getFieldList(record, indexParm));
            else if (indexType.equals("DeleteRecordIfFieldEmpty"))
            {
                Set<String> fields = getFieldList(record, indexParm);
                if (mapName != null && findMap(mapName) != null)
                    fields = Utils.remap(fields, findMap(mapName), true);

                if (fields.size() != 0)
                    addFields(indexMap, indexField, null, fields);
                else  // no entries produced for field => generate no record in Solr
                    return new HashMap<String, Object>();
            }
            else if (indexType.startsWith("join"))
            {
                String joinChar = " ";
                if (indexType.contains("(") && indexType.endsWith(")"))
                    joinChar = indexType.replace("join(", "").replace(")", "");
                addField(indexMap, indexField, getFieldVals(record, indexParm, joinChar));
            }
            else if (indexType.equals("std"))
            {
                if (indexParm.equals("era"))
                    addFields(indexMap, indexField, mapName, getEra(record));
                else
                    addField(indexMap, indexField, getStd(record, indexParm));
            }
            else if (indexType.equals("custom"))
                handleCustom(indexMap, indexField, mapName, record, indexParm);
        }
        return indexMap;
    }

    /**
     * do the processing indicated by a custom function, putting the solr
     *  field name and value into the indexMap parameter
     */
    private void handleCustom(Map<String, Object> indexMap, String indexField, String mapName, Record record, String indexParm)
    {
        try
        {
            Method method;
            Object retval;
            if (indexParm.indexOf("(") != -1)
            {
                String functionName = indexParm.substring(0, indexParm.indexOf('('));
                String parmStr = indexParm.substring(indexParm.indexOf('(')+1, indexParm.lastIndexOf(')'));
                // parameters are separated by unescaped commas
				String parms[] = parmStr.trim().split("(?<=[^\\\\]),");
                int numparms = parms.length;
                Class parmClasses[] = new Class[numparms+1];
                parmClasses[0] = Record.class;
                Object objParms[] = new Object[numparms+1];
                objParms[0] = record;                
                for (int i = 0 ; i < numparms; i++)  { 
                	parmClasses[i+1] = String.class; 
                	objParms[i+1] = dequote(parms[i].trim()); 
                }
                method = getClass().getMethod(functionName, parmClasses);
                retval = method.invoke(this, objParms);
            }
            else 
            {
                method = getClass().getMethod(indexParm, new Class[]{Record.class});
                retval = method.invoke(this, new Object[]{record});
            }
            if (retval instanceof Map) 
                indexMap.putAll((Map<String, String>)retval);         
            else if (retval instanceof Set) 
                addFields(indexMap, indexField, mapName, (Set<String>) retval);
            else if (retval instanceof String)
                addField(indexMap, indexField, mapName, (String) retval);
        }
        catch (SecurityException e)
        {
            //e.printStackTrace();
            logger.error(e.getCause());
        }
        catch (NoSuchMethodException e)
        {
            //e.printStackTrace();
            logger.error(e.getCause());
        }
        catch (IllegalArgumentException e)
        {
            //e.printStackTrace();
            logger.error(e.getCause());
        }
        catch (IllegalAccessException e)
        {
            //e.printStackTrace();
            logger.error(e.getCause());
        }
        catch (InvocationTargetException e)
        {
            //e.printStackTrace();
            logger.error(e.getCause());
        }
    }

    private String dequote(String str)
    {
        if (str.length() > 2 && str.charAt(0) == '"' && str.charAt(str.length()-1) == '"')
            return str.substring(1, str.length()-1);

        return str;
    }

    /**
     * get values that don't require parsing specified record fields:
     *   raw, xml, date, index_date ...
     * @param indexParm - what type of value to return
     */
    private String getStd(Record record, String indexParm)
    {
        if (indexParm.equals("raw") ||
        		indexParm.equalsIgnoreCase("FullRecordAsMARC"))
            return writeRaw(record);
        else if (indexParm.equals("xml") ||
                 	indexParm.equalsIgnoreCase("FullRecordAsXML"))
            return writeXml(record);
        else if (indexParm.equals("date") ||
        			indexParm.equalsIgnoreCase("DateOfPublication"))
            return getDate(record);
        else if (indexParm.equals("index_date") ||
        			indexParm.equalsIgnoreCase("DateRecordIndexed"))
            return getCurrentDate();
        return null;
    }

    /**
     * get the era field values from 045a as a Set of Strings
     */
    public static Set<String> getEra(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        String eraField = getFirstFieldVal(record, "045a");
        if (eraField == null)
            return result;

        if (eraField.length() == 4)
        {
            eraField = eraField.toLowerCase();
            char eraStart1 = eraField.charAt(0);
            char eraStart2 = eraField.charAt(1);
            char eraEnd1 = eraField.charAt(2);
            char eraEnd2 = eraField.charAt(3);
            if (eraStart2 == 'l') 
            	eraEnd2 = '1';
            if (eraEnd2 == 'l') 
            	eraEnd2 = '1';
            if (eraStart2 == 'o') 
            	eraEnd2 = '0';
            if (eraEnd2 == 'o') 
            	eraEnd2 = '0';
            return getEra(result, eraStart1, eraStart2, eraEnd1, eraEnd2);
        }
        else if (eraField.length() == 5)
        {
            char eraStart1 = eraField.charAt(0);
            char eraStart2 = eraField.charAt(1);

            char eraEnd1 = eraField.charAt(3);
            char eraEnd2 = eraField.charAt(4);
            char gap = eraField.charAt(2);
            if (gap == ' ' || gap == '-')
                return getEra(result, eraStart1, eraStart2, eraEnd1, eraEnd2);
        }
        else if (eraField.length() == 2)
        {
            char eraStart1 = eraField.charAt(0);
            char eraStart2 = eraField.charAt(1);
            if (eraStart1 >= 'a' && eraStart1 <= 'y' && 
            		eraStart2 >= '0' && eraStart2 <= '9')
                return getEra(result, eraStart1, eraStart2, eraStart1, eraStart2);
        }
        return result;
    }

    public static Set<String> getEra(Set<String> result, char eraStart1, char eraStart2, char eraEnd1, char eraEnd2)
    {
        if (eraStart1 >= 'a' && eraStart1 <= 'y' && eraEnd1 >= 'a' && eraEnd1 <= 'y')
        {
            for (char eraVal = eraStart1; eraVal <= eraEnd1; eraVal++)
            {
                if (eraStart2 != '-' || eraEnd2 != '-')
                {
                    char loopStart = (eraVal != eraStart1) ? '0' : Character.isDigit(eraStart2) ? eraStart2 : '0';
                    char loopEnd = (eraVal != eraEnd1) ? '9' : Character.isDigit(eraEnd2) ? eraEnd2 : '9';
                    for (char eraVal2 = loopStart; eraVal2 <= loopEnd; eraVal2++)
                    {
                        result.add("" + eraVal + eraVal2);
                    }
                }
                result.add("" + eraVal);
            }
        }
        return result;
    }

    protected void addField(Map<String, Object> indexMap, String indexField, String mapName, String fieldVal)
    {
        if (mapName != null && findMap(mapName) != null)
            fieldVal = Utils.remap(fieldVal, findMap(mapName), true);

        if (fieldVal != null && fieldVal.length() > 0)
            indexMap.put(indexField, fieldVal);
    }

    protected void addField(Map<String, Object> indexMap, String indexField, String fieldVal)
    {
        addField(indexMap, indexField, null, fieldVal);
    }

    protected void addFields(Map<String, Object> indexMap, String indexField, String mapName, Set<String> fields)
    {
        if (mapName != null && findMap(mapName) != null)
            fields = Utils.remap(fields, findMap(mapName), true);

        if (!fields.isEmpty())
        {
            if (fields.size() == 1)
            {
                String value = fields.iterator().next();
                indexMap.put(indexField, value);
            }
            else
                indexMap.put(indexField, fields);
        }
    }

    /**
     * Get Set of Strings as indicated by tagStr
     * @param record
     * @param tagStr which field(s)/subfield(s) to use
     * @return
     */
    public static Set<String> getFieldList(Record record, String tagStr)
    {
        String[] tags = tagStr.split(":");
        Set<String> result = new LinkedHashSet<String>();
        for (int i = 0; i < tags.length; i++)
        {
            // Check to ensure tag length is at least 3 characters
            if (tags[i].length() < 3)
            {
                System.err.println("Invalid tag specified: " + tags[i]);
                continue;
            }
            
            // Get Field Tag
            String tag = tags[i].substring(0, 3);

            // Process Subfields
            String subfield = tags[i].substring(3);
            int bracket;
            if ((bracket = tags[i].indexOf('[')) != -1)
            {
                String sub[] = tags[i].substring(bracket+1).split("[\\]\\[\\-, ]+");
                int substart = Integer.parseInt(sub[0]);
                int subend = (sub.length > 1 ) ? Integer.parseInt(sub[1])+1 : substart+1;
                result.addAll(getSubfieldDataAsSet(record, tag, subfield, substart, subend));
            } 
            else 
            {
                String separator = null;
                if (subfield.indexOf('\'') != -1) 
                {
                    separator = subfield.substring(subfield.indexOf('\'')+1, subfield.length()-1);
                    subfield = subfield.substring(0, subfield.indexOf('\''));
                }
                result.addAll(getSubfieldDataAsSet(record, tag, subfield, separator));
            }
        }
        return result;
    }

    /**
     * Get all field values joined as a single string.
     * @param record
     * @param tagStr which field(s)/subfield(s) to use
     * @param separator string separating values in the result string
     * @return single string containing all values joined with separator string
     */
    public String getFieldVals(Record record, String tagStr, String separator)
    {
        Set<String> result = getFieldList(record, tagStr);
        return org.solrmarc.tools.Utils.join(result, separator);
    }

    /**
     * Get the first value according to the tagStr
     * @param record
     * @param tagStr which field(s)/subfield(s) to use
     * @return first value as a string
     */
    public static String getFirstFieldVal(Record record, String tagStr)
    {
        Set<String> result = getFieldList(record, tagStr);
        Iterator<String> iter = result.iterator();
        if (iter.hasNext()) 
        	return iter.next();
        else
        	return null;
    }

    /**
     * Get the first field value, which is mapped to another value
     * @param record
     * @param mapName - name of translation map to use to xform values
     * @param tagStr - which field(s)/subfield(s) to use
     * @return first value as a string
     */
    public String getFirstFieldVal(Record record, String mapName, String tagStr)
    {
        Set<String> result = getFieldList(record, tagStr);
        if (mapName != null && findMap(mapName) != null)
        {
            result = Utils.remap(result, findMap(mapName), false);
            if (findMap(mapName).containsKey(""))
            {
                result.add(findMap(mapName).get(""));
            }
        }
        Iterator<String> iter = result.iterator();
        if (iter.hasNext()) 
        	return iter.next();
        else
        	return null;
    }

    /**
     * Get the title from a record
     * @param record
     * @return Recrod's title (245a and 245b)
     */
    public String getTitle(Record record)
    {
        DataField titleField = (DataField) record.getVariableField("245");
        String thisTitle = "";
        
        if (titleField != null && titleField.getSubfield('a') != null)
        {
            thisTitle = titleField.getSubfield('a').getData();

            // check for a subfield b
            if (titleField.getSubfield('b') != null)
                thisTitle += " " + titleField.getSubfield('b').getData();
        }
        return Utils.cleanData(thisTitle);
    }
    
    /**
     * Get the title from a record, without non-filing chars as specified
     *   in 245 2nd indicator
     * @param record
     * @return Recrod's title (245a and 245b)
     */
    public String getSortableTitle(Record record)
    {
        DataField titleField = (DataField) record.getVariableField("245");
        String thisTitle = getTitle(record);
        
        if (titleField != null && 
        		titleField.getIndicator2() > '0' && 
        		titleField.getIndicator2() <= '9')
            thisTitle = thisTitle.substring(((int)(titleField.getIndicator2() - '0')));

        return thisTitle.toLowerCase();
    }

    /**
     * Return the date in 260c as a string
     */
    public String getDate(Record record)
    {
        String date = getFieldVals(record, "260c", ", ");
        return Utils.cleanDate(date);
    }

    /**
     * Return the index datestamp as a string
     */
    public String getCurrentDate()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        return df.format(indexDate);
    }

    /**
     * Get the appropriate Map object from populated transMapMap 
     * @param mapName the translation map to find
     * @return populated Map object
     */
    protected Map<String, String> findMap(String mapName)
    {
        if (mapName.startsWith("pattern_map:"))
            mapName = mapName.substring("pattern_map:".length());

        if (transMapMap.containsKey(mapName))
            return (transMapMap.get(mapName));

        return null;
    }

    /**
     * Get the specified subfields from the specified MARC field, returned as
     *  a set of strings to become lucene document field values
     * @param record
     * @param fldTag - the field name, e.g. 245
     * @param subfldsStr - the string containing the desired subfields
     * @returns the result set of strings 
     */
    @SuppressWarnings("unchecked")
    protected static Set<String> getSubfieldDataAsSet(Record record, String fldTag, String subfldsStr, String separator)
    {
        Set<String> resultSet = new LinkedHashSet<String>();

        // Process Leader
        if (fldTag.equals("000"))
        {
        	resultSet.add(record.getLeader().toString());
            return resultSet;
        }
        
        // Loop through Data and Control Fields
        int iTag = new Integer(fldTag).intValue();
        List<VariableField> varFlds = record.getVariableFields(fldTag);
        for (VariableField vf : varFlds)
        {
            if (iTag > 9 && subfldsStr != null) 
            {
                // DataField
                DataField dfield = (DataField) vf;

                if (subfldsStr.length() > 1 || separator != null) 
                {
                    // Allow automatic concatenation of grouped subfields
                    StringBuffer buffer = new StringBuffer("");
                    List<Subfield> subFlds = dfield.getSubfields();
                    for (Subfield sf : subFlds) 
                    {
	                    if (subfldsStr.indexOf(sf.getCode()) != -1)
                        {
	                        if (buffer.length() > 0)  
	                        	buffer.append(separator != null ? separator : " ");
                            buffer.append(sf.getData().trim());
                        }
                    }                        
	                if (buffer.length() > 0) 
	                	resultSet.add(buffer.toString());
                } 
                else 
                {
	                // get all instances of the single subfield
	                List<Subfield> subFlds = dfield.getSubfields(subfldsStr.charAt(0));
	                for (Subfield sf : subFlds)                         
	                {
	                    resultSet.add(sf.getData().trim());
	                }
                }
            }
            else 
            {
                // Control Field
                resultSet.add(((ControlField) vf).getData());
            }
        }
        return resultSet;
    }

    /**
     * Get the specified substring of subfield values from the specified MARC field, returned as
     *  a set of strings to become lucene document field values
     * @param record
     * @param fldTag - the field name, e.g. 245
     * @param subfldsStr - the string containing the desired subfields
     * @param beginIx - the beginning index of the substring of the subfield value
     * @param endIx - the ending index of the substring of the subfield value
     * @return the result set of strings 
     */
    @SuppressWarnings("unchecked")
    protected static Set<String> getSubfieldDataAsSet(Record record, String fldTag, String subfield, int beginIx, int endIx)
    {
        Set<String> resultSet = new LinkedHashSet<String>();

        // Process Leader
        if (fldTag.equals("000"))
        {
            resultSet.add(record.getLeader().toString().substring(beginIx, endIx));
            return resultSet;
        }
        
        // Loop through Data and Control Fields
        int iTag = new Integer(fldTag).intValue();
        List<VariableField> varFlds = record.getVariableFields(fldTag);
        for (VariableField vf : varFlds) 
        {
            if (iTag > 9 && subfield != null) 
            {
                // Data Field
                DataField dfield = (DataField) vf;
                if (subfield.length() > 1) 
                {
                    // automatic concatenation of grouped subfields
                    StringBuffer buffer = new StringBuffer("");
                    List<Subfield> subFlds = dfield.getSubfields();
                    for (Subfield sf : subFlds) 
                    {
                        if (subfield.indexOf(sf.getCode()) != -1 && 
                        		sf.getData().length() >= endIx)
                        {
                            if (buffer.length() > 0) 
                            	buffer.append(" ");
                            buffer.append(sf.getData().substring(beginIx, endIx));
                        }
                    }
                    resultSet.add(buffer.toString());
                }                        
                else 
                {
                    // get all instances of the single subfield
                    List<Subfield> subFlds = dfield.getSubfields(subfield.charAt(0));
                    for (Subfield sf : subFlds)                         
                    {
                        if (sf.getData().length() >= endIx)
                            resultSet.add(sf.getData().substring(beginIx, endIx));
                    }
                }
            }
            else  // Control Field
            {
            	String cfldData = ((ControlField) vf).getData();
                if (cfldData.length() >= endIx)
                    resultSet.add(cfldData.substring(beginIx, endIx));
            }
        }
        return resultSet;
    }

    /**
     * Write a marc record as a binary string
     * @param record record to write
     * @return Binary marc output
     */
    protected String writeRaw(Record record)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcWriter writer = new MarcStreamWriter(out, "UTF-8");
        writer.write(record);
        writer.close();

        String result = null;
        try
        {
            result = out.toString("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            //  e.printStackTrace();
            logger.error(e.getCause());
        }
        return result;
    }

    /**
     * Write a MarcXML formated file to index
     * @param record record to output as XML
     * @return String of MarcXML
     */
    protected String writeXml(Record record)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // TODO: see if this works better 
        //MarcWriter writer = new MarcXmlWriter(out, false);
        MarcWriter writer = new MarcXmlWriter(out, "UTF-8");
        writer.write(record);
        writer.close();

        String tmp = null;
        try
        {
            tmp = out.toString("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // e.printStackTrace();
            logger.error(e.getCause());
        }
        return tmp;
    }
    
    /**
     * remove trailing punctuation (default trailing characters to be removed)
     * @param record
     * @param fieldSpec - the field to have trailing punctuation removed
     * @return Set of strings containing the field values with trailing 
     *   punctuation removed
     */
    public Set<String> removeTrailingPunct(Record record, String fieldSpec)
    {
        Set<String> result = getFieldList(record, fieldSpec);
        Set<String> newResult = new LinkedHashSet<String>();
        for (String s : result)
        {
            newResult.add(Utils.cleanData(s));
        }
        return newResult;
    }
    
    /**
     * extract all the subfields in a given marc field
     * @param record
     * @param marcFieldNum - the marc field number as a string (e.g. "245")
     * @return
     */
    public Set<String> getAllSubfields(final Record record, String fieldSpec, String separator)
    {
        Set<String> result = new LinkedHashSet<String>();

        String[] tags = fieldSpec.split(":");
        for (int i = 0; i < tags.length; i++)
        {
            // Check to ensure tag length is at least 3 characters
            if (tags[i].length() < 3)
            {
                System.err.println("Invalid tag specified: " + tags[i]);
                continue;
            }
            
            // Get Field Tag
            String tag = tags[i].substring(0, 3);

            // Process Subfields
            String subfieldtags = tags[i].substring(3);

            List<?> marcFieldList =  record.getVariableFields(tag);
            if (!marcFieldList.isEmpty()) 
            {
                Pattern subfieldPattern = Pattern.compile(subfieldtags.length() == 0 ? "." : subfieldtags);
                Iterator<?> fieldIter = marcFieldList.iterator();
                while (fieldIter.hasNext())
                {
                    DataField marcField = (DataField)fieldIter.next();
                    StringBuffer buffer = new StringBuffer("");
                
                    List<Subfield> subfields = marcField.getSubfields();
                    Iterator<Subfield> iter = subfields.iterator();
        
                    Subfield subfield;
        
                    while (iter.hasNext()) 
                    {
                        subfield = iter.next();
                        Matcher matcher = subfieldPattern.matcher(""+subfield.getCode());
                        if (matcher.matches())
                        {
                            if (buffer.length() > 0)  
                            	buffer.append(separator);
                            buffer.append(subfield.getData());
                        }
                    }
                    if (buffer.length() > 0) 
                    	result.add(Utils.cleanData(buffer.toString()));
                }
            }
        }

        return result;
    }
    
    /**
     * Extract the info from an 880 linked field from a record
     * @param record
     * @return linked field
     */
    public Set<String> getLinkedField(final Record record, String fieldSpec)
    {
        Set<String> set = getFieldList(record,"8806");
        
        if (set.isEmpty())
            return null;
        
        String[] tags = fieldSpec.split(":");
        Set<String> result = new LinkedHashSet<String>();
        for (int i = 0; i < tags.length; i++)
        {
            // Check to ensure tag length is at least 3 characters
            if (tags[i].length() < 3)
            {
                System.err.println("Invalid tag specified: " + tags[i]);
                continue;
            }
            
            // Get Field Tag
            String tag = tags[i].substring(0, 3);

            // Process Subfields
            String subfield = tags[i].substring(3);
            List<?> fields = record.getVariableFields("880");
            Iterator<?> fldIter = fields.iterator();
            while (fldIter.hasNext())
            {
                DataField dfield = (DataField) fldIter.next();
                Subfield link = dfield.getSubfield('6');
                if (link.getData().startsWith(tag))
                {
                    List<?> subList = dfield.getSubfields();
                    Iterator<?> subIter = subList.iterator();
                    StringBuffer buf = new StringBuffer("");
                    while(subIter.hasNext())
                    {
                        Subfield subF = (Subfield)subIter.next();
                        if (subfield.indexOf(subF.getCode()) != -1)
                        {
                            if (buf.length() > 0) buf.append(" ");
                            buf.append(subF.getData());
                        }
                    }
                    result.add(Utils.cleanData(buf.toString()));
                }
            }
        }
        return result;
    }

    /**
     * Extract the info from an 880 linked field from a record
     * @param record
     * @return linked field
     */
    public Set<String> getLinkedFieldCombined(final Record record, String fieldSpec)
    {
        Set<String> result1 = getLinkedField(record, fieldSpec);
        Set<String> result2 = getFieldList(record, fieldSpec);
        
        if (result1 != null) 
        	result2.addAll(result1);
        return result2;
    }

}
