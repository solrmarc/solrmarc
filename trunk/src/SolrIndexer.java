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

import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

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
    
    private SolrIndexer()
    {
        mapMap = new HashMap<String, Map<String, String>>();
        fieldMap = new HashMap<String, String[]>();
        indexDate = new Date();
    }    
    
    public SolrIndexer(String propertiesMapFile) throws FileNotFoundException, IOException, ParseException
    {
        this();
        Properties props = new Properties();
        props.load(new FileInputStream(propertiesMapFile));
        if (!fillMapFromProperties(props)) 
        {
            throw new ParseException("Invalid data found in indexer properties file", 0);
        }
    }
    
    protected boolean fillMapFromProperties(Properties props)
    {
        boolean valid = true;
        Enumeration en = props.propertyNames();
        while (en.hasMoreElements())
        {
            String property = (String)en.nextElement();
            if (!property.startsWith("map") && !property.startsWith("pattern_map"))
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
                        String values2[] = values[1].trim().split("[, ]+", 2);
                        fieldDef[0] = property;
                        fieldDef[1] = "custom";
                        fieldDef[2] = values2[0];
                        fieldDef[3] = values2.length > 1 ? values2[1] : null;
                    }
                    else if (values[0].equals("xml") || values[0].equals("raw") || 
                               values[0].equals("date") || values[0].equals("index_date") ||
                               values[0].equals("era"))
                       {
                           fieldDef[0] = property;
                           fieldDef[1] = "std";
                           fieldDef[2] = values[0];
                           fieldDef[3] = values.length > 1 ? values[1].trim() : null;
                       }
                    else if (values[0].equalsIgnoreCase("FullRecordAsXML") || 
                             values[0].equalsIgnoreCase("FullRecordAsMARC") || 
                               values[0].equalsIgnoreCase("DateOfPublication"))
                       {
                           fieldDef[0] = property;
                           fieldDef[1] = "std";
                           fieldDef[2] = values[0];
                           fieldDef[3] = values.length > 1 ? values[1].trim() : null;
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
                        if (values2[0].equals("first") || (values2.length > 1 && values2[1].equals("first")))
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
                        if (values2[0].equalsIgnoreCase("DeleteRecordIfFieldEmpty")||
                            (values2.length > 1 && values2[1].equalsIgnoreCase("DeleteRecordIfFieldEmpty")))
                        {
                            fieldDef[1] = "DeleteRecordIfFieldEmpty";
                        }
                        fieldDef[2] = values[0];
                        fieldDef[3] = null;
                        if (!values2[0].equals("all") && !values2[0].equals("first") &&
	                        !values2[0].startsWith("join") && 
	                        !values2[0].equalsIgnoreCase("DeleteRecordIfFieldEmpty"))
	                    {
	                           // assume its a translation map definition
	                        fieldDef[3] = values2[0].trim();
	                    }
                    }
                    if (fieldDef[3] != null)
                    {
                        try {
                            fieldDef[3] = loadTranslationMap(props, fieldDef[3]); 
                           }
                           catch (FileNotFoundException e)
                           {
                            System.err.println("Error: Unable to find file containing specified translation map ("+ fieldDef[3] + ")");
                            valid = false;
                           }
                           catch (IOException e)
                           {
                            System.err.println("Error: Problems reading specified translation map ("+ fieldDef[3] + ")");
                            valid = false;
                           }
                    }
                }
                fieldMap.put(index, fieldDef);
            }
            else if (property.startsWith("map") || property.startsWith("pattern_map"))
            {
                // ignore entry, handled separately
            }
        }
        
        // Now verify that the data read to configure the indexer is not invalid.
        
        int size = fieldMap.size();
        Iterator<String> keys = fieldMap.keySet().iterator();
        while (keys.hasNext())
        {
            String key = keys.next();
            String fieldVal[] = fieldMap.get(key);
            String indexField = fieldVal[0];
            String indexType = fieldVal[1];
            String indexParm = fieldVal[2];
            String mapName = fieldVal[3];
            if (mapName != null && findMap(mapName) == null)
            {
                System.err.println("Error: Specified translation map ("+ mapName + ") not found in properties file");
                valid = false;
            }
            if (indexType.equals("custom"))
            {
                try
                {
                    Method method = getClass().getMethod(indexParm, new Class[]{Record.class});
                    Class retval = method.getReturnType();
//                    if (!method.isAccessible())
//                    {
//                        System.err.println("Error: Unable to invoke custom indexing function "+indexParm);
//                        valid = false;
//                    }
                    if (!(Set.class.isAssignableFrom(retval) || String.class.isAssignableFrom(retval))) 
                    {
                        System.err.println("Error: Return type of custom indexing function "+indexParm+ " must be either String or Set<String>");
                        valid = false;
                    }
                }
                catch (SecurityException e)
                {
                    System.err.println("Error: Unable to invoke custom indexing function "+indexParm);
                    valid = false;
                }
                catch (NoSuchMethodException e)
                {
                    System.err.println("Error: Unable to find custom indexing function "+indexParm);
                    valid = false;
                }
                catch (IllegalArgumentException e)
                {
                    System.err.println("Error: Unable to find custom indexing function "+indexParm);
                    valid = false;
                }
            }
        }
        return(valid);
    }
    
    
    private String loadTranslationMap(Properties props, String translationMapSpec) throws FileNotFoundException, IOException
    {
        String mapName = null;
        if (translationMapSpec.length() == 0)
        {
            return(null);
        }
        else if (translationMapSpec.startsWith("(") && translationMapSpec.endsWith(")"))
        {
            // map entries are in current properties file.
            String mapKeyPrefix = translationMapSpec.replaceAll("[\\(\\)]", "");
            mapName = mapKeyPrefix;
            loadTranslationMapValues(props, mapKeyPrefix, mapName);
        }
        else if (translationMapSpec.contains("(") && translationMapSpec.endsWith(")"))
        {
            String mapSpec[] = translationMapSpec.split("(//s|[()])+");
            String propFilename = mapSpec[0];
            String mapKeyPrefix = mapSpec[1];
            mapName = mapSpec[1];
            loadTranslationMapValues(propFilename, mapKeyPrefix, mapName);
        }
        else 
        {
            String propFilename = translationMapSpec;
            String mapKeyPrefix = "";
            mapName = translationMapSpec.replaceAll(".properties", "");
            loadTranslationMapValues(propFilename, mapKeyPrefix, mapName);
        }
        return(mapName);
    }

    private void loadTranslationMapValues(String propFilename, String mapKeyPrefix, String mapName) throws FileNotFoundException, IOException 
    {
        Properties props = new Properties();
        props.load(new FileInputStream(propFilename));
        loadTranslationMapValues(props, mapKeyPrefix, mapName);
    }

    private void loadTranslationMapValues(Properties props, String mapKeyPrefix, String mapName) 
    {
        boolean valid = true;
        Enumeration en = props.propertyNames();
        while (en.hasMoreElements())
        {
            String property = (String)en.nextElement();
            if (mapKeyPrefix.length() == 0 || property.startsWith(mapKeyPrefix))
            {
                String mapKey = property.substring(mapKeyPrefix.length());
                if (mapKey.startsWith(".")) mapKey = mapKey.substring(1);
                String value = props.getProperty(property);
                value = value.trim();
                if (value.equals("null"))  value = null;
                
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

    public Map<String, Object> map(Record record)
    {
        Map<String, Object> indexMap = new HashMap<String, Object>();
        int size = fieldMap.size();
        Iterator<String> keys = fieldMap.keySet().iterator();
        while (keys.hasNext())
        {
            String key = keys.next();
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
                	return(indexMap);
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
        return(indexMap);
    }
   
    private void handleCustom(Map<String, Object> indexMap, String indexField, String mapName, Record record, String indexParm)
    {
        try
        {
            Method method = getClass().getMethod(indexParm, new Class[]{Record.class});
            Object retval = method.invoke(this, new Object[]{record});
            if (retval instanceof Set) 
            {
                addFields(indexMap, indexField, mapName, (Set<String>)retval);
            }
            else if (retval instanceof String)
            {
                addField(indexMap, indexField, mapName, (String)retval);
            }
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String getStd(Record record, String indexParm)
    {
        if (indexParm.equals("raw") || indexParm.equalsIgnoreCase("FullRecordAsMARC"))  
        {
            return(writeRaw(record));
        }    
        else if (indexParm.equals("xml") || indexParm.equalsIgnoreCase("FullRecordAsXML"))  
        {
            return(writeXml(record));
        }    
        else if (indexParm.equals("date") || indexParm.equalsIgnoreCase("DeleteRecordIfFieldEmpty"))  
        {
            return(getDate(record));
        }    
        else if (indexParm.equals("index_date"))  
        {
            return(getCurrentDate());
        }    
        return null;
    }
    
    public static Set<String> getEra(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        String eraField = getFirstFieldVal(record, "045a");
        if (eraField == null )
        {
            return(result);
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
            if (eraStart1 >= 'a' && eraStart1 <= 'y' && eraStart2 >= '0' && eraStart2 <= '9')
            {
                return (getEra(result, eraStart1, eraStart2, eraStart1, eraStart2));
            }
        }
        return(result);
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
                        result.add(""+eraVal+eraVal2);
                    }
                }
                result.add(""+eraVal);            
            }
        }
        return(result);
    }
    
    protected void addField(Map<String, Object> indexMap, String indexField, String mapName, String fieldVal)
    {
        if (mapName != null && findMap(mapName) != null)
        {
            fieldVal = Utils.remap(fieldVal, findMap(mapName), false);     
        }
        if (fieldVal != null && fieldVal.length() > 0 )
        {
            indexMap.put(indexField, fieldVal);
        }
    }
       
    protected void addField(Map<String, Object> indexMap, String indexField, String fieldVal)
    {
        addField(indexMap, indexField, null, fieldVal);      
    }
        
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
//        Iterator<String> iter = fields.iterator();
//        
//        while(iter.hasNext())
//        {
//            String fieldVal = iter.next();
//            addField(builder, indexField, null, fieldVal);            
//        }
    }
    
    public static Set<String> getFieldList(Record record, String tagStr)
    {
        String[] tags = tagStr.split(":");
        Set<String> result = new LinkedHashSet<String>();
        for (int i = 0; i < tags.length; i++)
        {
            if (tags[i].length() < 3)  
            {
                System.err.println("Invalid tag specified: "+ tags[i]);
                continue;
            }
            String tag = tags[i].substring(0,3);
            String subfield = null;
            if (!(tags[i].length() == 3 || tags[i].charAt(3) == '['))  
            {
                subfield = tags[i].substring(3);
            }
            int bracket;
            if ((bracket = tags[i].indexOf('[')) != -1)
            {
                String sub[] = tags[i].substring(bracket+1).split("[\\]\\[\\-, ]+");
                int substart = Integer.parseInt(sub[0]);
                int subend = (sub.length > 1 ) ? Integer.parseInt(sub[1])+1 : substart+1;
                addSubfieldDataToSet(record, result, tag,  subfield, substart, subend);                
            }   
            else
            {
                addSubfieldDataToSet(record, result, tag,  subfield);
            }
        }
        return(result);
    }     
    
    public String getFieldVals(Record record, String tagStr, String seperator)
    {
        Set<String> result = getFieldList(record, tagStr);
        return(Utils.join(result, seperator));
    }
    
    public static String getFirstFieldVal(Record record, String tagStr )
    {
        Set<String> result = getFieldList(record, tagStr);
        Iterator<String> iter = result.iterator();
        if (iter.hasNext()) return(iter.next());
        return(null);
    }     
    
    public String getFirstFieldVal(Record record, String mapName, String tagStr )
    {
        Set<String> result = getFieldList(record, tagStr);
        if (mapName != null && findMap(mapName) != null)
        {
            result = Utils.remap(result, findMap(mapName), false);
        }
        Iterator<String> iter = result.iterator();
        if (iter.hasNext()) return(iter.next());
        return(null);
    }     
    
    public String getTitle(Record record)
    {
        DataField titleField = (DataField) record.getVariableField("245");
        String thisTitle = "";
        if (titleField != null && titleField.getSubfield('a') != null)
        {
            
            thisTitle = Utils.cleanData(titleField.getSubfield('a').getData());
                        
            // check for a subfield b
            if(titleField.getSubfield('b') != null)
            {
                thisTitle += " " + titleField.getSubfield('b').getData();
                thisTitle = Utils.cleanData(thisTitle);
            }
        } 
        return(thisTitle);
    }

    public String getDate(Record record)
    {
        String date = getFieldVals(record, "260c", ", ");
        date = Utils.cleanDate(date);
        return(date);
    }
    
    public String getCurrentDate()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String val = df.format(indexDate);
        return(val);
    }
    
    protected Map<String, String> findMap(String mapName)
    {
        if (mapName.startsWith("pattern_map:"))
        {
            mapName = mapName.substring("pattern_map:".length());
        }
        if (mapMap.containsKey(mapName))
        {
            return(mapMap.get(mapName));
        }
        return null;
    }

    protected static void addSubfieldDataToSet(Record record, Set<String> set, String field, String subfield)
    {
        if (field.equals("000"))
        {
            Leader leader = record.getLeader();
            String val = leader.toString();
            set.add(val);
            return;
        }
       List fields = record.getVariableFields(field);
        Iterator fldIter = fields.iterator();
        while (fldIter.hasNext())
        {
            if (subfield != null)
            {
                DataField dfield = (DataField)fldIter.next();
                List sub = dfield.getSubfields(subfield.charAt(0));
                Iterator iter = sub.iterator();
                while (iter.hasNext())
                {
                    Subfield s = (Subfield)(iter.next());
                    String data = s.getData();
                    data = Utils.cleanData(data);
                    set.add(data);
                }
            }
            else
            {
                ControlField cfield = (ControlField)fldIter.next();
                set.add(cfield.getData());
            }
        }
    }
    
    protected static void addSubfieldDataToSet(Record record, Set<String> set, String field, String subfield, int substringStart, int substringEnd)
    {
        if (field.equals("000"))
        {
            Leader leader = record.getLeader();
            String val = leader.toString().substring(substringStart, substringEnd);
            set.add(val);
            return;
        }
        List fields = record.getVariableFields(field);
        Iterator fldIter = fields.iterator();
        while (fldIter.hasNext())
        {
            if (subfield != null)
            {
                DataField dfield = (DataField)fldIter.next();
                List sub = dfield.getSubfields(subfield.charAt(0));
                Iterator iter = sub.iterator();
                while (iter.hasNext())
                {
                    Subfield s = (Subfield)(iter.next());
                    set.add(s.getData().substring(substringStart, substringEnd));
                }
            }
            else
            {
                ControlField cfield = (ControlField)fldIter.next();
                set.add(cfield.getData().substring(substringStart, substringEnd));
            }
        }
    }
    
    protected String writeRaw(Record record)
    {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // TODO: see if this works better 
        //MarcWriter writer = new MarcXmlWriter(out, false);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return(result);
    }

    /**
     * Write a MarcXML formated file to index
     * @param record
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return(tmp);
    }
    
}
