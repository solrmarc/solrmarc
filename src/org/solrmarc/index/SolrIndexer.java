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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
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
    private Map<String, Map<String, String>> mapMap = null;
    private Map<String, String[]> fieldMap = null;
    private Date indexDate = null;

    private String solrMarcDir;
    
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcImporter.class.getName());

    /**
     * private constructor
     */
    private SolrIndexer()
    {
        mapMap = new HashMap<String, Map<String, String>>();
        fieldMap = new HashMap<String, String[]>();
        indexDate = new Date();
    }
    
    /**
     * Constructor
     * @param propertiesMapFile
     * @param dir
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    public SolrIndexer(String propertiesMapFile, String dir)
        throws FileNotFoundException, IOException, ParseException
    {
        this();
        solrMarcDir = dir;
        Properties props = new Properties();
        props.load(new FileInputStream(solrMarcDir + "/" + propertiesMapFile));
        String errorMsg = fillMapFromProperties(props);
        if (errorMsg != null)
        {
            throw new ParseException(errorMsg, 0);
        }
    }

    /**
     * Parse the properties file and load parameters into Map
     * @param props Properties to load
     * @return If the properties are valid
     */
    protected String fillMapFromProperties(Properties props)
        throws ParseException
    {
        String errorMsg = null;
        Enumeration<?> en = props.propertyNames();
        
        while (en.hasMoreElements())
        {
            String property = (String) en.nextElement();
            
            if (!property.startsWith("map") &&
                !property.startsWith("pattern_map"))
            {
                String index = property;
                String value = props.getProperty(property);
                String fieldDef[] = new String[4];
                fieldDef[3] = null;
                if (value.startsWith("\""))
                {
                    fieldDef[0] = property;
                    fieldDef[1] = "constant";
                    fieldDef[2] = value.trim().replaceAll("\"", "");
                }
                else
                {
                    String values[] = value.split("[, ]+", 2);
                    if (values[0].equals("custom"))
                    {
                        fieldDef[0] = property;
                        fieldDef[1] = "custom";
                        // allow for mapping of custom fields via _index.properties
                        String lastValues[];
                        if (values[1].indexOf("()") != -1)
                        {
                            values[1] = values[1].replace("()", "");
                        }
                        if (values[1].indexOf(',') != -1 && values[1].indexOf('(') != -1 && values[1].indexOf('(') < values[1].indexOf(','))
                        {
                            lastValues = values[1].trim().split("\\) *,", 2);
                        }
                        else // no parens
                        {
                            lastValues = values[1].trim().split(" *,", 2);                         
                        }
                        fieldDef[2] = lastValues[0].trim();
                        fieldDef[3] = lastValues.length > 1 ? lastValues[1].trim() : null;
                    }
                    else if (values[0].equals("xml") ||
                             values[0].equals("raw") ||
                             values[0].equals("date") ||
                             values[0].equals("index_date") ||
                             values[0].equals("era"))
                    {
                        fieldDef[0] = property;
                        fieldDef[1] = "std";
                        fieldDef[2] = values[0];
                        fieldDef[3] = values.length > 1 ? values[1].trim() : null;
                    }
                    else if (values[0].equalsIgnoreCase("FullRecordAsXML") ||
                             values[0].equalsIgnoreCase("FullRecordAsMARC") ||
                             values[0].equalsIgnoreCase("DateOfPublication") ||
                             values[0].equalsIgnoreCase("DateRecordIndexed"))
                    {
                        fieldDef[0] = property;
                        fieldDef[1] = "std";
                        fieldDef[2] = values[0];
                        fieldDef[3] =
                                values.length > 1 ? values[1].trim() : null;
                    }
                    else if (values.length == 1)
                    {
                        fieldDef[0] = property;
                        fieldDef[1] = "all";
                        fieldDef[2] = values[0];
                        fieldDef[3] = null;
                    }
                    else
                    {
                        String values2[] = values[1].trim().split("[ ]*,[ ]*", 2);
                        fieldDef[0] = property;
                        fieldDef[1] = "all";
                        if (values2[0].equals("first") ||
                            (values2.length > 1 && values2[1].equals("first")))
                        {
                            fieldDef[1] = "first";
                        }
                        if (values2[0].startsWith("join"))
                        {
                            fieldDef[1] = values2[0];
                        }
                        if ((values2.length > 1 && values2[1].startsWith("join")))
                        {
                            fieldDef[1] = values2[1];
                        }
                        if (values2[0].equalsIgnoreCase("DeleteRecordIfFieldEmpty") ||
                            (values2.length > 1 && values2[1].equalsIgnoreCase("DeleteRecordIfFieldEmpty")))
                        {
                            fieldDef[1] = "DeleteRecordIfFieldEmpty";
                        }
                        fieldDef[2] = values[0];
                        fieldDef[3] = null;
                        if (!values2[0].equals("all") &&
                            !values2[0].equals("first") &&
                            !values2[0].startsWith("join") &&
                            !values2[0].equalsIgnoreCase("DeleteRecordIfFieldEmpty"))
                        {
                            // assume its a translation map definition
                            fieldDef[3] = values2[0].trim();
                        }
                    }
                    if (fieldDef[3] != null)
                    {
                        try
                        {
                            fieldDef[3] = loadTranslationMap(props, fieldDef[3]);
                        }
                        catch (FileNotFoundException e)
                        {
                            //System.err.println("Error: Unable to find file containing specified translation map (" +
                              //                 fieldDef[3] + ")");
                            logger.error("Unable to find file containing specified translation map (" + fieldDef[3] + ")");
                            errorMsg = "Unable to find file containing specified translation map (" + fieldDef[3] + ")";
                        }
                        catch (IOException e)
                        {
//                            System.err.println("Error: Problems reading specified translation map (" +
//                                               fieldDef[3] + ")");
                            logger.error("Error: Problems reading specified translation map (" + fieldDef[3] + ")");
                            errorMsg = "Error: Problems reading specified translation map (" + fieldDef[3] + ")";
                        }
                    }
                }
                fieldMap.put(index, fieldDef);
            }
            else if (property.startsWith("map") ||
                     property.startsWith("pattern_map"))
            {
                // ignore entry, handled separately
            }
        }

        // Now verify that the data read to configure the indexer is valid.

        for (String key : fieldMap.keySet())
        {
            String fieldVal[] = fieldMap.get(key);
            //String indexField = fieldVal[0]; // never read locally
            String indexType = fieldVal[1];
            String indexParm = fieldVal[2];
            String mapName = fieldVal[3];
            
            // Process Map Field
            if (mapName != null && findMap(mapName) == null)
            {
//                System.err.println("Error: Specified translation map (" +
//                                   mapName + ") not found in properties file");
                logger.error("Specified translation map (" + mapName + ") not found in properties file");
                errorMsg = "Specified translation map (" + mapName + ") not found in properties file";
            }
            
            // Process Custom Field
            if (indexType.equals("custom"))
            {
                try
                {
                    Method method = null;
                    if (indexParm.indexOf("(") != -1)
                    {
                        String functionName = indexParm.substring(0, indexParm.indexOf('('));
                        String parmStr = indexParm.substring(indexParm.indexOf('(')+1, indexParm.lastIndexOf(')'));
                        String parms[] = parmStr.trim().split(",");
                        int numparms = parms.length;
                        Class<?> parmClasses[] = new Class<?>[numparms+1];
                        parmClasses[0] = Record.class;
                        for (int i = 0 ; i < numparms; i++)  { parmClasses[i+1] = String.class; }
                        method = getClass().getMethod(functionName, parmClasses);
                    }
                    else 
                    {
                        method = getClass().getMethod(indexParm, new Class[] { Record.class });
                    }
                    Class<?> retval = method.getReturnType();
                    // if (!method.isAccessible())
                    // {
                    // System.err.println("Error: Unable to invoke custom
                    // indexing function "+indexParm);
                    // valid = false;
                    // }
                    if (!(Set.class.isAssignableFrom(retval) || String.class.isAssignableFrom(retval)))
                    {
//                        System.err.println("Error: Return type of custom indexing function " +
//                                           indexParm +
//                                           " must be either String or Set<String>");
                        logger.error("Error: Return type of custom indexing function " + indexParm +" must be either String or Set<String>");
                        errorMsg = "Error: Return type of custom indexing function " + indexParm +" must be either String or Set<String>";
                    }
                }
                catch (SecurityException e)
                {
//                    System.err.println("Error: Unable to invoke custom indexing function " +
//                                       indexParm);
                    logger.error("Unable to invoke custom indexing function " + indexParm);
                    logger.debug(e.getCause(), e);
                    errorMsg = "Unable to invoke custom indexing function " + indexParm;
                }
                catch (NoSuchMethodException e)
                {
//                    System.err.println("Error: Unable to find custom indexing function " +
//                                       indexParm);
                    logger.error("Unable to find custom indexing function " + indexParm);
                    logger.debug(e.getCause());
                    errorMsg = "Unable to find custom indexing function " + indexParm;
                }
                catch (IllegalArgumentException e)
                {
//                    System.err.println("Error: Unable to find custom indexing function " +
//                                       indexParm);
                    logger.error("Unable to find custom indexing function " + indexParm);
                    logger.debug(e.getCause());
                    errorMsg = "Unable to find custom indexing function " + indexParm;
                }
            }
        }
        return errorMsg;
    }

    protected String loadTranslationMap(Properties props, String translationMapSpec) throws FileNotFoundException, IOException
    {
        String mapName = null;
        if (translationMapSpec.length() == 0)
        {
            return (null);
        }
        else if (translationMapSpec.startsWith("(") &&
                 translationMapSpec.endsWith(")"))
        {
            // map entries are in current properties file.
            String mapKeyPrefix = translationMapSpec.replaceAll("[\\(\\)]", "");
            mapName = mapKeyPrefix;
            loadTranslationMapValues(props, mapKeyPrefix, mapName);
        }
        else if (translationMapSpec.contains("(") &&
                 translationMapSpec.endsWith(")"))
        {
            String mapSpec[] = translationMapSpec.split("(//s|[()])+");
            String propFilename = mapSpec[0];
            String mapKeyPrefix = mapSpec[1];
            mapName = mapSpec[1];
            if (findMap(mapName) == null)
            {
                loadTranslationMapValues(propFilename, mapKeyPrefix, mapName);
            }
        }
        else
        {
            String propFilename = translationMapSpec;
            String mapKeyPrefix = "";
            mapName = translationMapSpec.replaceAll(".properties", "");
            if (findMap(mapName) == null)
            {
                loadTranslationMapValues(propFilename, mapKeyPrefix, mapName);
            }
        }
        return (mapName);
    }

    private void loadTranslationMapValues(String propFilename, String mapKeyPrefix, String mapName) throws FileNotFoundException, IOException
    {
        Properties props = new Properties();
        System.err.println("Loading Custom Map: " + solrMarcDir + "/" + propFilename);
        props.load(new FileInputStream(solrMarcDir + "/" + propFilename));
        loadTranslationMapValues(props, mapKeyPrefix, mapName);
    }

    private void loadTranslationMapValues(Properties props, String mapKeyPrefix, String mapName)
    {
       // boolean valid = true; // never read locally
        Enumeration<?> en = props.propertyNames();
        while (en.hasMoreElements())
        {
            String property = (String) en.nextElement();
            if (mapKeyPrefix.length() == 0 || property.startsWith(mapKeyPrefix))
            {
                String mapKey = property.substring(mapKeyPrefix.length());
                if (mapKey.startsWith(".")) mapKey = mapKey.substring(1);
                String value = props.getProperty(property);
                value = value.trim();
                if (value.equals("null")) value = null;

                Map<String, String> subMap;
                if (mapMap.containsKey(mapName))
                {
                    subMap = mapMap.get(mapName);
                }
                else
                {
                    subMap = new LinkedHashMap<String, String>();
                    mapMap.put(mapName, subMap);
                }
                subMap.put(mapKey, value);

            }
        }
    }

    /**
     * 
     * @param record
     * @return
     */
    public Map<String, Object> map(Record record)
    {
        Map<String, Object> indexMap = new HashMap<String, Object>();
        //int size = fieldMap.size(); //never read locally 
        for (String key : fieldMap.keySet())
        {
            String fieldVal[] = fieldMap.get(key);
            String indexField = fieldVal[0];
            String indexType = fieldVal[1];
            String indexParm = fieldVal[2];
            String mapName = fieldVal[3];
            if (indexType.equals("constant"))
            {
                addField(indexMap, indexField, indexParm);
            }
            else if (indexType.equals("first"))
            {
                addField(indexMap, indexField, getFirstFieldVal(record, mapName, indexParm));
            }
            else if (indexType.equals("all"))
            {
                addFields(indexMap, indexField, mapName, getFieldList(record, indexParm));
            }
            else if (indexType.equals("DeleteRecordIfFieldEmpty"))
            {
                Set<String> fields = getFieldList(record, indexParm);
                if (mapName != null && findMap(mapName) != null)
                {
                    fields = Utils.remap(fields, findMap(mapName), false);
                }
                if (fields.size() != 0)
                {
                    addFields(indexMap, indexField, null, fields);
                }
                else  // no entries produced for field => generate no record in Solr
                {
                    indexMap = new HashMap<String, Object>();
                    return (indexMap);
                }
            }
            else if (indexType.startsWith("join"))
            {
                String joinChar = " ";
                if (indexType.contains("(") && indexType.endsWith(")"))
                {
                    joinChar = indexType.replace("join(", "").replace(")", "");
                }
                addField(indexMap, indexField, getFieldVals(record, indexParm, joinChar));
            }
            else if (indexType.equals("std"))
            {
                if (indexParm.equals("era"))
                {
                    addFields(indexMap, indexField, mapName, getEra(record));
                }
                else
                {
                    addField(indexMap, indexField, getStd(record, indexParm));
                }
            }
            else if (indexType.equals("custom"))
            {
                handleCustom(indexMap, indexField, mapName, record, indexParm);
            }
        }
        return (indexMap);
    }

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
                String parms[] = parmStr.trim().split(",");
                int numparms = parms.length;
                Class parmClasses[] = new Class[numparms+1];
                parmClasses[0] = Record.class;
                Object objParms[] = new Object[numparms+1];
                objParms[0] = record;                
                for (int i = 0 ; i < numparms; i++)  { parmClasses[i+1] = String.class; objParms[i+1] = dequote(parms[i].trim()); }
                method = getClass().getMethod(functionName, parmClasses);
                retval = method.invoke(this, objParms);
            }
            else 
            {
                method = getClass().getMethod(indexParm, new Class[]{Record.class});
                retval = method.invoke(this, new Object[]{record});
            }
            if (retval instanceof Set) 
            {
                addFields(indexMap, indexField, mapName, (Set<String>) retval);
            }
            else if (retval instanceof String)
            {
                addField(indexMap, indexField, mapName, (String) retval);
            }
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
        {
            return(str.substring(1, str.length()-1));
        }
        return(str);
    }

    private String getStd(Record record, String indexParm)
    {
        if (indexParm.equals("raw") ||
            indexParm.equalsIgnoreCase("FullRecordAsMARC"))
        {
            return (writeRaw(record));
        }
        else if (indexParm.equals("xml") ||
                 indexParm.equalsIgnoreCase("FullRecordAsXML"))
        {
            return (writeXml(record));
        }
        else if (indexParm.equals("date") ||
                 indexParm.equalsIgnoreCase("DateOfPublication"))
        {
            return (getDate(record));
        }
        else if (indexParm.equals("index_date") ||
                 indexParm.equalsIgnoreCase("DateRecordIndexed"))
        {
            return (getCurrentDate());
        }
        return null;
    }

    /**
     * 
     * @param record
     * @return
     */
    public static Set<String> getEra(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        String eraField = getFirstFieldVal(record, "045a");
        if (eraField == null)
        {
            return (result);
        }
        if (eraField.length() == 4)
        {
            eraField = eraField.toLowerCase();
            char eraStart1 = eraField.charAt(0);
            char eraStart2 = eraField.charAt(1);
            char eraEnd1 = eraField.charAt(2);
            char eraEnd2 = eraField.charAt(3);
            if (eraStart2 == 'l') eraEnd2 = '1';
            if (eraEnd2 == 'l') eraEnd2 = '1';
            if (eraStart2 == 'o') eraEnd2 = '0';
            if (eraEnd2 == 'o') eraEnd2 = '0';
            return (getEra(result, eraStart1, eraStart2, eraEnd1, eraEnd2));
        }
        else if (eraField.length() == 5)
        {
            char eraStart1 = eraField.charAt(0);
            char eraStart2 = eraField.charAt(1);

            char eraEnd1 = eraField.charAt(3);
            char eraEnd2 = eraField.charAt(4);
            char gap = eraField.charAt(2);
            if (gap == ' ' || gap == '-')
            {
                return (getEra(result, eraStart1, eraStart2, eraEnd1, eraEnd2));
            }
        }
        else if (eraField.length() == 2)
        {
            char eraStart1 = eraField.charAt(0);
            char eraStart2 = eraField.charAt(1);
            if (eraStart1 >= 'a' && eraStart1 <= 'y' && eraStart2 >= '0' &&
                eraStart2 <= '9')
            {
                return (getEra(result, eraStart1, eraStart2, eraStart1,
                               eraStart2));
            }
        }
        return (result);
    }

    /**
     * 
     * @param result
     * @param eraStart1
     * @param eraStart2
     * @param eraEnd1
     * @param eraEnd2
     * @return
     */
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
        return (result);
    }

    /**
     * 
     * @param indexMap
     * @param indexField
     * @param mapName
     * @param fieldVal
     */
    protected void addField(Map<String, Object> indexMap, String indexField, String mapName, String fieldVal)
    {
        if (mapName != null && findMap(mapName) != null)
        {
            fieldVal = Utils.remap(fieldVal, findMap(mapName), false);
        }
        if (fieldVal != null && fieldVal.length() > 0)
        {
            indexMap.put(indexField, fieldVal);
        }
    }

    /**
     * 
     * @param indexMap
     * @param indexField
     * @param fieldVal
     */
    protected void addField(Map<String, Object> indexMap, String indexField, String fieldVal)
    {
        addField(indexMap, indexField, null, fieldVal);
    }

    /**
     * 
     * @param indexMap
     * @param indexField
     * @param mapName
     * @param fields
     */
    protected void addFields(Map<String, Object> indexMap, String indexField, String mapName, Set<String> fields)
    {
        if (mapName != null && findMap(mapName) != null)
        {
            fields = Utils.remap(fields, findMap(mapName), false);
        }
        if (!fields.isEmpty())
        {
            if (fields.size() == 1)
            {
                String value = fields.iterator().next();
                indexMap.put(indexField, value);
            }
            else
            {
                indexMap.put(indexField, fields);
            }
        }
    }

    /**
     * 
     * @param record
     * @param tagStr
     * @return
     */
    public static Set<String> getFieldList(Record record, String tagStr)
    {
        String[] tags = tagStr.split(":");
        Set<String> result = null;
        Set<String> tmpResult = null;
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
                tmpResult = getSubfieldDataAsSet(record, tag, subfield, substart, subend);
            } 
            else 
            {
                tmpResult = getSubfieldDataAsSet(record, tag, subfield);
            }
            if (tmpResult != null)
            {
                if (result == null) result = tmpResult;
                else result.addAll(tmpResult);
            }
        }
        return (result);
    }

    /**
     * 
     * @param record
     * @param tagStr
     * @param seperator
     * @return
     */
    public String getFieldVals(Record record, String tagStr, String seperator)
    {
        Set<String> result = getFieldList(record, tagStr);
        return (org.solrmarc.tools.Utils.join(result, seperator));
    }

    /**
     * 
     * @param record
     * @param tagStr
     * @return
     */
    public static String getFirstFieldVal(Record record, String tagStr)
    {
        Set<String> result = getFieldList(record, tagStr);
        Iterator<String> iter = result.iterator();
        if (iter.hasNext()) return (iter.next());
        return (null);
    }

    /**
     * 
     * @param record
     * @param mapName
     * @param tagStr
     * @return
     */
    public String getFirstFieldVal(Record record, String mapName, String tagStr)
    {
        Set<String> result = getFieldList(record, tagStr);
        if (mapName != null && findMap(mapName) != null)
        {
            result = Utils.remap(result, findMap(mapName), false);
        }
        Iterator<String> iter = result.iterator();
        if (iter.hasNext()) return (iter.next());
        return (null);
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
            {
                thisTitle += " " + titleField.getSubfield('b').getData();
            }
        }
        thisTitle = Utils.cleanData(thisTitle);
        return (thisTitle);
    }
    
    /**
     * Get the title from a record
     * @param record
     * @return Recrod's title (245a and 245b)
     */
    public String getSortableTitle(Record record)
    {
        DataField titleField = (DataField) record.getVariableField("245");
        String thisTitle = getTitle(record);
        
        if (titleField != null)
        {
            if (titleField.getIndicator2() != '0')
            {
                thisTitle = thisTitle.substring(((int)(titleField.getIndicator2() - '0')));
            }
        }
        thisTitle = thisTitle.toLowerCase();
        return (thisTitle);
    }

    /**
     * Get the date from a record
     * @param record
     * @return 
     */
    public String getDate(Record record)
    {
        String date = getFieldVals(record, "260c", ", ");
        date = Utils.cleanDate(date);
        return (date);
    }

    /**
     * Return the current date
     * @return
     */
    public String getCurrentDate()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String val = df.format(indexDate);
        return (val);
    }

    /**
     * 
     * @param mapName
     * @return
     */
    protected Map<String, String> findMap(String mapName)
    {
        if (mapName.startsWith("pattern_map:"))
        {
            mapName = mapName.substring("pattern_map:".length());
        }
        if (mapMap.containsKey(mapName))
        {
            return (mapMap.get(mapName));
        }
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
    protected static Set<String> getSubfieldDataAsSet(Record record, String field, String subfield)
    {
        // Process Leader
        Set<String> result = new LinkedHashSet<String>();
        if (field.equals("000"))
        {
            Leader leader = record.getLeader();
            String val = leader.toString();
            result.add(val);
            return result;
        }
        
        // Loop through Data and Control Fields
        List<VariableField> varFlds = record.getVariableFields(field);
        for (VariableField vf : varFlds)
        {
            int iField = new Integer(field).intValue();
            if (iField > 9) {
                // This field is a DataField
                if (subfield != null) 
                {
                    DataField dfield = (DataField) vf;

                    if (subfield.length() > 1) 
                    {
                        // Allow automatic concatenation of grouped subfields
                        StringBuffer buffer = new StringBuffer("");
                        List<Subfield> subFlds = dfield.getSubfields();
                        for (Subfield sf : subFlds) 
                        {
                            if (subfield.indexOf(sf.getCode()) != -1)
                            {
                                if (buffer.length() > 0) buffer.append(" ");
                                buffer.append(sf.getData().trim());
                            }
                        }                        
                        result.add(buffer.toString());
                    } 
                    else 
                    {
                        // Just get the singly defined subfield
                        List<Subfield> subFlds = dfield.getSubfields(subfield.charAt(0));
                        for (Subfield sf : subFlds)                         
                        {
                            result.add(sf.getData().trim());
                        }
                    }
                }
            } 
            else 
            {
                // This field is a Control Field
                ControlField cfield = (ControlField) vf;
                result.add(cfield.getData());
            }
        }
        return(result);
    }

    /**
     * Get the specified substring of subfield values from the specified MARC field, returned as
     *  a set of strings to become lucene document field values
     * @param record
     * @param fldTag - the field name, e.g. 245
     * @param subfldsStr - the string containing the desired subfields
     * @param beginIdx - the beginning index of the substring of the subfield value
     * @param endIdx - the ending index of the substring of the subfield value
     * @return the result set of strings 
     */
    @SuppressWarnings("unchecked")
    protected static Set<String> getSubfieldDataAsSet(Record record, String field, String subfield, int beginIdx, int endIdx)
    {
        // Process Leader
        Set<String> result = new LinkedHashSet<String>();
        if (field.equals("000"))
        {
            Leader leader = record.getLeader();
            String val = leader.toString().substring(beginIdx, endIdx);
            result.add(val);
            return result;
        }
        
        // Loop through Data and Control Fields
        List<VariableField> varFlds = record.getVariableFields(field);
        for (VariableField vf : varFlds) 
        {
            int iField = new Integer(field).intValue();
            if (iField > 9 && subfield != null) 
            {
                // This is a data field
                DataField dfield = (DataField) vf;
                if (subfield.length() > 1) 
                {
                    // Allow automatic concatenation of grouped subfields
                    StringBuffer buffer = new StringBuffer("");
                    List<Subfield> subFlds = dfield.getSubfields();
                    for (Subfield sf : subFlds) 
                    {
                        if (subfield.indexOf(sf.getCode()) != -1)
                        {
                            if (sf.getData().length() >= endIdx)
                            {
                                if (buffer.length() > 0) buffer.append(" ");
                                buffer.append(sf.getData().substring(beginIdx, endIdx));
                            }
                        }
                    }                        
                    result.add(buffer.toString());
                } 
                else 
                {
                    // Just get the singly defined subfield
                    List<Subfield> subFlds = dfield.getSubfields(subfield.charAt(0));
                    for (Subfield sf : subFlds)                         
                    {
                        if (sf.getData().length() >= endIdx)
                        {
                            result.add(sf.getData().substring(beginIdx, endIdx));
                        }
                    }
                }
            }
            else
            {
                // This is a control field
                ControlField cfield = (ControlField) vf;
                if (cfield.getData().length() >= endIdx)
                {
                    result.add(cfield.getData().substring(beginIdx, endIdx));
                }
            }
        }
        return(result);
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
        return (result);
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
    
    public Set<String> removeTrailingPunct(Record record, String fieldSpec)
    {
        Set<String> result = getFieldList(record, fieldSpec);
        Set<String> newResult = new LinkedHashSet<String>();
        for (String s : result)
        {
            newResult.add(Utils.cleanData(s));
        }
        return(newResult);
    }
}