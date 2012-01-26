package org.blacklight;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.tools.CallNumUtils;
import org.solrmarc.tools.StringNaturalCompare;
import org.solrmarc.tools.Utils;


/**
 * Custom functions for indexing marc files for the Blacklight project
 * @author Robert Haschart
 * @version $Id: BlacklightIndexer.java 219 2008-10-28 19:14:39Z rh9ec@virginia.edu $
 *
 */

public class BlacklightIndexer extends SolrIndexer
{
    /**
     * Main program instantiation for doing the indexing
     * @param args
     */

    /**
     * Default constructor
     * @param propertiesMapFile
     * @throws ParseException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws Exception
     */
    Map<String, String> addnlShadowedIds = null;
    Map<String, String> boundWithIds = null;
    Map<String, String> dateFirstAddedMap = null;
    String extraIdsFilename = "AllShadowedIds.txt";
    String boundWithsFilename = "BoundWith.txt";
    Set<String> combinedFormat = null;
    String publicationDate = null;
    Set<String> callNumberFieldList = null;
    Set<String> callNumberFieldListNo050 = null;
    Map<String, Set<String>> callNumberClusterMap = null;
    Map<String, Set<String>> callNumberClusterMapNo050 = null;
    Comparator<String> normedComparator = null;
    String bestSingleCallNumber = null;
    List<?> trimmedHoldingsList = null;
    
    public BlacklightIndexer(final String propertiesMapFile, final String propertyPaths[])
    {
        super(propertiesMapFile, propertyPaths);
        normedComparator = new Comparator<String>() 
        {
            public int compare(String s1, String s2)
            {
                String s1Norm = s1.replaceAll("[. ]", "");
                String s2Norm = s2.replaceAll("[. ]", "");
                return s1Norm.compareToIgnoreCase(s2Norm);
            }
        };
    }
    
    /**
     * Add a record to the Solr index
     * @param record
     */
//    public void indexRecord(DocumentBuilder builder, Record record)
//    {        
//        addField(builder, "id", getFirstFieldVal(record, "001"));
//        addFields(builder, "author_text", record, "100a:110a:111a:130a");
//        addFields(builder, "published_text", getFieldList(record, "260a"));
//        addFields(builder, "material_type_text", getFieldList(record, "300a"));
//        addFields(builder, "notes_text", getFieldList(record, "500a:505a"));
//        addFields(builder, "uniform_title_text", getFieldList(record, "240a"));
//        addField(builder, "marc_display", writeRaw(record));
//        addField(builder, "marc_text", writeXml(record));
//        addField(builder, "title_text", getFieldVals(record, "245a:245b", " "));
//        addField(builder, "call_number_facet", getFirstFieldVal(record, "999a:090a:050a"));
//        addField(builder, "year_multisort_i", getDate(record));
//                
//        addField(builder, "source_facet", "Library Catalog");
//        addFields(builder, "subject_era_facet", getFieldList(record, "650d:650y:651y:655y"));
//        addFields(builder, "topic_form_genre_facet", getFieldList(record, "650a:650b:650x:655a"));
//        addFields(builder, "subject_geographic_facet", getFieldList(record, "650c:650z:651a:651x:651z:655z"));
//        addField(builder, "broad_formats_facet", getBroadFormat(record));
//        addField(builder, "format_facet", getFormat(record));
//        addFields(builder, "language_facet", getLanguage(record));
//        addFields(builder, "location_facet", getLocation(record));
//        addFields(builder, "library_facet", getLibrary(record));
//        addFields(builder, "instrument_facet", getInstrument(record));
//        addFields(builder, "recording_type_facet", getRecordingType(record));
//        addFields(builder, "recordings_and_scores_facet", getRecordingAndScore(record));
//        addFields(builder, "recording_format_facet", getRecordingFormat(record));
//        addFields(builder, "music_catagory_facet", getMusicCatagory(record));
//            
//    }


    /**
     * Return a collection of recordings and scores from a record
     * @param record
     * @return A collection of recordings and/or scores
     */
    public Set<String> getRecordingAndScore(final Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        String leader = record.getLeader().toString();
        String leaderChar = leader.substring(6, 7).toUpperCase();
                
        if("C".equals(leaderChar) || "D".equals(leaderChar))
        {
            result.add("Scores");
            result.add("Recordings and/or Scores");
        }
        
        if("J".equals(leaderChar))
        {
            result.add("Recordings");
            result.add("Recordings and/or Scores");
        }
        
        return result;
    }
 
    /**
     * Return a collection of recording formats from a record
     * @param record
     * @return Collection of recording formats
     */
    public Set<String> getRecordingFormat(final Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        String leader = record.getLeader().toString();
        String leaderChar = leader.substring(6, 7).toUpperCase();
        Set<String> titleH = getSubfieldDataAsSet(record, "245", "h", null);
                
        if("J".equals(leaderChar) || "I".equals(leaderChar) || 
                (Utils.setItemContains(titleH, "videorecording")))
        {
            Set<String> form = getSubfieldDataAsSet(record, "999", "t", null);
            return(form);
        }
        return(result);
    }

    /**
     * This routine can be overridden in a sub-class to perform some processing that need to be done once 
     * for each record, and which may be needed by several indexing specifications, especially custom methods.
     * The default version does nothing.
     * 
     * @param record -  The MARC record that is being indexed.
     */
    protected void perRecordInit(Record record)
    {
        String fieldSpec = "999awi';'";
        trimmedHoldingsList = getTrimmedHoldingsList(record, "999");        
        
        callNumberFieldListNo050 = getCallNumberFieldSetNo050(record, trimmedHoldingsList);
        callNumberFieldList = getCallNumberFieldSet(record, callNumberFieldListNo050);
        callNumberClusterMapNo050 =  getCallNumbersCleanedConflated(callNumberFieldListNo050, true);
        callNumberClusterMap =  getCallNumbersCleanedConflated(callNumberFieldList, true);
        bestSingleCallNumber = getBestSingleCallNumber(callNumberClusterMap);
        combinedFormat = null;
        publicationDate = null;
    }
    
    private List<?> getTrimmedHoldingsList(Record record, String holdingsTag)
    {
        List<?> result = record.getVariableFields(holdingsTag);
        loadExtraShadowedIds(extraIdsFilename, boundWithsFilename);
        addBoundWithHoldings(record, result);
        removeShadowed999sFromList(record, result);
        removeLostHoldings(result);
        return result;
    }
    
    private void addBoundWithHoldings(Record record, List<?> fields999)
    {
        if (record.getControlNumber() == null || record.getControlNumber().length() < 2) return;
        String boundWithStr = boundWithIds.get(record.getControlNumber().substring(1));
        if (boundWithStr != null)
        {
            String holdingsParts[] = boundWithStr.split("\\|");
            DataField df = new DataFieldImpl();
            df.addSubfield(new SubfieldImpl('a', holdingsParts[7]));
            df.addSubfield(new SubfieldImpl('w', holdingsParts[6]));
            df.addSubfield(new SubfieldImpl('i', holdingsParts[1]));
            if (!holdingsParts[2].equals(holdingsParts[3]))
            {
                df.addSubfield(new SubfieldImpl('k', holdingsParts[2]));
            }
            df.addSubfield(new SubfieldImpl('l', holdingsParts[3]));
            df.addSubfield(new SubfieldImpl('m', holdingsParts[4]));
            df.addSubfield(new SubfieldImpl('t', holdingsParts[5]));
            df.setId(new Long(2));
            df.setTag("999");
            df.setIndicator1(' ');
            df.setIndicator2(' ');
            ((List<VariableField>)fields999).add(df);
        }
    }

    private void loadExtraShadowedIds(String addnlShadowedFilename, String boundWithFilename)
    {
        if (addnlShadowedIds == null)
        {
            addnlShadowedIds = new LinkedHashMap<String, String>();
            InputStream addnlIdsStream = Utils.getPropertyFileInputStream(propertyFilePaths, addnlShadowedFilename);
            BufferedReader addnlIdsReader = new BufferedReader(new InputStreamReader(addnlIdsStream));
            String line;
            try
            {
                while ((line = addnlIdsReader.readLine()) != null)
                {
                    String linepts[] = line.split("\\|");
                    if (linepts.length == 1) 
                    {
                        addnlShadowedIds.put(linepts[0], "");
                    }
                    else
                    {
                        String existing = addnlShadowedIds.get(linepts[0]);
                        if (existing == null) addnlShadowedIds.put(linepts[0], "|" + linepts[1] + "|"); 
                        else if (existing.equals("")) continue;
                        else addnlShadowedIds.put(linepts[0], existing + linepts[1] + "|");
                    }
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (boundWithIds == null)
        {
            boundWithIds = new LinkedHashMap<String, String>();
            InputStream addnlIdsStream = null;
            try {
                addnlIdsStream = Utils.getPropertyFileInputStream(propertyFilePaths, boundWithFilename);
                BufferedReader addnlIdsReader = new BufferedReader(new InputStreamReader(addnlIdsStream));
                String line;
                while ((line = addnlIdsReader.readLine()) != null)
                {
                    String linepts[] = line.split("\\|", 2);
                    String existing = boundWithIds.get(linepts[0]);
                    if (existing == null) boundWithIds.put(linepts[0], linepts[1]); 
                    else 
                    {
                        //addnlShadowedIds.put(linepts[0], existing + linepts[1] + "|");
                    }
                }

            }
            catch (IllegalArgumentException iae)
            {
                // couldn't find BoundWith.txt file, but don't have a cow man
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    

    private void removeShadowed999sFromList(Record record, List<?> fields999)
    {
        String extraString = null;
        if (addnlShadowedIds != null)
        {
            extraString = addnlShadowedIds.get(record.getControlNumber());
        }  
        if (extraString == null) return;
        else if (extraString.equals(""))  return; // this will list locations 
        else
        {
            Iterator<?> iter = fields999.iterator();
            while (iter.hasNext())
            {
                Object field = iter.next();
                DataField df = (DataField)field;
                String barcode = df.getSubfield('i') != null ? df.getSubfield('i').getData() : "";
                if (extraString != null && extraString.contains("|" + barcode + "|"))
                {
                    iter.remove();
                }
            }
        }
    }

    private void removeLostHoldings(List<?> fields999)
    {
        String mapName = loadTranslationMap(null, "shadowed_location_map.properties");
        Map<String, String> locationMap = findMap(mapName);
        Iterator<?> iter = fields999.iterator();
        while (iter.hasNext())
        {
            Object field = iter.next();
            DataField df = (DataField)field;
            Subfield currentLocation = df.getSubfield('k');
            Subfield homeLocation = df.getSubfield('l');
            if (currentLocation != null)
            {
                if (Utils.remap(currentLocation.getData(), locationMap, true).equals("HIDDEN"))
                {
                    iter.remove();
                    continue;
                }
            }
            if (homeLocation != null)
            {
                if (Utils.remap(homeLocation.getData(), locationMap, true).equals("HIDDEN"))
                {
                    iter.remove();
                }
            }

        }
    }
    
    private String getBestSingleCallNumber(Map<String, Set<String>>resultNormed)
    {
        if (resultNormed == null || resultNormed.size() == 0) {
            return(null);
        }
        String[] bestSet =  getBestCallNumberSubset(resultNormed);
        if (bestSet.length == 0) return(null);
        String result = bestSet[0];
        String resultParts[] = result.split(":", 2);
        if (resultParts[0].equals("LC"))
        {
            result = resultParts[0]+":"+resultParts[1].trim().replaceAll("[^A-Za-z0-9.]", " ").replaceAll("\\s\\s+", " ")
                        .replaceAll("\\s?\\.\\s?", ".");
        }        
        return(result);
    }
    
    private String[] getBestCallNumberSubset(Map<String, Set<String>>resultNormed)
    {
        if (resultNormed == null || resultNormed.size() == 0) {
            return(null);
        }
        int maxEntries = 0;
        //String maxEntriesKey = null;
        Set<String> maxEntrySet = null;
        int maxLCEntries = 0;
        //String maxLCEntriesKey = null;
        Set<String> maxLCEntrySet = null;
        Set<String> keys = resultNormed.keySet();
        for (String key : keys)
        {
            Set<String> values = resultNormed.get(key);
            if (values.size() > maxEntries)
            {
                maxEntries = values.size();
                //maxEntriesKey = key;
                maxEntrySet = values;
            }
            String firstNum = values.iterator().next();
            String parts[] = firstNum.split(":", 2);
            if (parts[0].equals("LC") || 
                ( parts[0].equals("") && CallNumUtils.isValidLC(parts[1])) && values.size() > maxLCEntries)
            {
                maxLCEntries = values.size();
                //maxLCEntriesKey = key;
                maxLCEntrySet = values;
            }
        }
        if (maxLCEntrySet == null)
        {
            maxLCEntrySet = maxEntrySet;
        }
        String valueArr[] = maxLCEntrySet.toArray(new String[0]);
        Comparator<String> comp = new StringNaturalCompare();
        Arrays.sort(valueArr, comp);
        return(valueArr);
    }
    
    /**
     * Since there are several routines that grab and process LC Call Numbers for a given record, 
     * this code is called once per record to gather the list of call numbers, rather than creating that 
     * list within each implementation of the custom indexing functions.
     * 
     * @param record -  The MARC record that is being indexed.
     */
    private Set<String> getCallNumberFieldSetNo050(final Record record, List<?> fields999)
    {
        boolean processExtraShadowedIds = true; //fieldSpec.contains("';'");
    
     //   List<?> fields999 = record.getVariableFields("999");
        //Set<String> fieldList = getFieldList(record, fieldSpec);
        if (fields999.isEmpty())  {
            return(null);
        }
        Set<String> fieldList = new LinkedHashSet<String>();
        if (processExtraShadowedIds)
        {
            loadExtraShadowedIds(extraIdsFilename, boundWithsFilename);
            String extraString = addnlShadowedIds.get(record.getControlNumber());
          
            for (Object field : fields999)
            {
                DataField df = ((DataField)(field));
                String barCode = (df.getSubfield('i') != null) ? df.getSubfield('i').getData() : "";
                String numberScheme = (df.getSubfield('w') != null) ? df.getSubfield('w').getData() : "";
                if (numberScheme.equals("MONO-SER") || numberScheme.equals("LCPER"))  numberScheme = "LC";
                String callNumber = (df.getSubfield('a') != null) ? df.getSubfield('a').getData() : "";
                if (callNumber.startsWith("MSS") || callNumber.startsWith("Mss")) callNumber = callNumber.replaceFirst("MSS[ ]?", "M@");
                if (extraString == null || extraString.equals("") || !extraString.contains("|" + barCode + "|"))
                {
                    if (numberScheme.length() > 0 && callNumber.length() > 0) 
                        fieldList.add(numberScheme + ":" + callNumber);
                }
            }
        }
        return(fieldList);
    }
    /**
     * Since there are several routines that grab and process LC Call Numbers for a given record, 
     * this code is called once per record to gather the list of call numbers, rather than creating that 
     * list within each implementation of the custom indexing functions.
     * 
     * @param record -  The MARC record that is being indexed.
     */
    private Set<String> getCallNumberFieldSet(final Record record, Set<String> startingFieldList)
    {
        Set<String> fieldList = new LinkedHashSet<String>();
        if (startingFieldList != null) 
        {
            fieldList.addAll(startingFieldList);
        }

        // discard LC numbers that aren't valid according to the CallNumUtil routine
        boolean hasLCNumber = false;
        for (String field : fieldList)
        {
            String fieldParts[] = field.split(":", 2);
            if (fieldParts[0].equals("LC") || 
                (fieldParts[0].equals("") && CallNumUtils.isValidLC(field)))
            {
                hasLCNumber = true;
                break;
            }
        }
        // if there are no 999 fields with valid LC Call Numbers then look in the 050ab field
        if (!hasLCNumber)
        {
            Set<String> fList2 = getFieldList(record, "050ab");
            for (String field : fList2)
            {
                if (CallNumUtils.isValidLC(field))
                {
                    fieldList.add("LC:"+field);
                }
            }
        }
        return(fieldList);
    }

    /**
     * Extract a set of cleaned call numbers from a record
     * @param record
     * @return Clean call number
     */
    private Map<String, Set<String>> getCallNumbersCleanedConflated(Set<String> fieldList, boolean expectColon)
    {
        Map<String, Set<String>> resultNormed = new TreeMap<String, Set<String>>();
        if (fieldList == null || fieldList.size() == 0)  return(null);
        for (String callNumPlus : fieldList)
        {
            String parts[] = callNumPlus.split(":", 2);
            String prefix = null;
            String callNumPart = null;
            if (!expectColon || parts.length == 1)
            {
                prefix = "";
                callNumPart = parts[0];
            }
            else
            {
                prefix = parts[0]+":";
                callNumPart = parts[1];
            }
            String val = callNumPart.trim().replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".");
            String nVal = val.replaceAll("^([A-Z][A-Z]?[A-Z]?) ([0-9])", "$1$2");
            if (!nVal.equals(val) && !val.startsWith("MSS"))
            {
                val = nVal;
            }
            String key = val.substring(0, Math.min(val.length(), 5)).toUpperCase();
            val = prefix+val;
            if (resultNormed.containsKey(key))
            {
                Set<String> set = resultNormed.get(key);
                set.add(val);
                resultNormed.put(key, set);
            }
            else
            {
                Set<String> set = new TreeSet<String>(normedComparator);
                set.add(val);
                resultNormed.put(key, set);
            }
        }
        return(resultNormed);
    }
 
   /**
     * Extract call number prefix from a record
     * @param record
     * @return Call number prefix
     * @deprecated
     */
    public String getCallNumberPrefix(final Record record, String mapName, String part)
    {
        try
        {
            mapName = loadTranslationMap(null, mapName);
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String useRecord = getFirstFieldVal(record, "050a:090a");
        if (useRecord == null || useRecord.matches("[0-9].*"))  
        {
            return(null);
        }
        
        String val = getFirstFieldVal(record, "999a");
        String result = null;
        if (val == null || val.length() == 0) { 
            return(null);
            }
        
        String vals[] = val.split("[^A-Za-z]+", 2);
        String prefix = vals[0];
        
        if (vals.length == 0 || vals[0] == null || vals[0].length() == 0 ||  vals[0].length() > 3 || !vals[0].toUpperCase().equals(vals[0])) 
        {
            return(null);
        }
        else
        {
            while (result == null && prefix.length() > 0)
            {
                result = Utils.remap(prefix, findMap(mapName), false);
                if (result == null)
                {
                    prefix = prefix.substring(0, prefix.length()-1);
                }
            }
        }
        int partNum = Utils.isNumber(part) ? Integer.parseInt(part) : 0;
        if (result == null) return(result);
        if (partNum == 0) return(prefix + " - " + result.replaceAll("[|]", " - "));
        String resultParts[] = result.split("[|]");
        if (partNum-1 >= resultParts.length) return(null);
        return(prefix.substring(0,1) + " - " + resultParts[partNum-1]);
    }
 
 
    /**
     * Extract call number prefix from a record
     * @param record
     * @return Call number prefix
     */
    public String getCallNumberPrefixNew(final Record record, String mapName, String part)
    {
        try
        {
            mapName = loadTranslationMap(null, mapName);
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        String useRecord = getFirstFieldVal(record, "050a:090a");
//        if (useRecord == null || useRecord.matches("[0-9].*"))  
//        {
//            return(null);
//        }
        
        String val = bestSingleCallNumber;
        String result = null;
        if (val == null || val.length() == 0)
        { 
            return(null);
        }
        String valParts[] = val.split(":", 2);
        if (!valParts[0].equals("LC"))
        {
            return(null);
        }
        String vals[] = valParts[1].split("[^A-Za-z]+", 2);
        String prefix = vals[0];
        
        if (vals.length == 0 || vals[0] == null || vals[0].length() == 0 ||  vals[0].length() > 3 || !vals[0].toUpperCase().equals(vals[0])) 
        {
            return(null);
        }
        else
        {
            while (result == null && prefix.length() > 0)
            {
                result = Utils.remap(prefix, findMap(mapName), false);
                if (result == null && prefix.length() == 2)
                {
                    break;
                }
                if (result == null)
                {
                    prefix = prefix.substring(0, prefix.length()-1);
                }
            }
        }
        if (mapName.equals("callnumber_map"))
        {
            int partNum = Utils.isNumber(part) ? Integer.parseInt(part) : 0;
            if (result == null) return(result);
            if (partNum == 0) return(prefix + " - " + result.replaceAll("[|]", " - "));
            String resultParts[] = result.split("[|]");
            if (partNum-1 >= resultParts.length) return(null);
            return(prefix.substring(0,1) + " - " + resultParts[partNum-1]);
        }
        else // detailed call number map
        {
            if (result == null) return(result);
            if (result.startsWith("{"))
            {
                String shelfKey = CallNumUtils.getLCShelfkey(valParts[1], record.getControlNumber(), super.getErrorHandler());
                String keyDigits = shelfKey.substring(4, 8);
                String ranges[] = result.replaceAll("[{]", "").split("[}]");
                for (String range : ranges)
                {
                    String rangeParts[] = range.split("[-=]", 3);
                    if (keyDigits.compareTo(rangeParts[0])>= 0 && keyDigits.compareTo(rangeParts[1])<= 0 )
                    {
                        return(prefix + rangeParts[0].replaceFirst("^0+", "") + "-" + 
                                prefix + rangeParts[1].replaceFirst("^0+", "") + " - " + rangeParts[2]);
                    }
                }
                return(null);
            }
            else if (result.startsWith(prefix.substring(0,1)) && result.matches("["+prefix.substring(0,1)+"][A-Z]-["+prefix.substring(0,1)+"][A-Z] - .*"))
            {
                return(result);                
            }
            else 
            {
                return(prefix + " - " + result);
            }
               
        }
    }

    /**
     * Get the specified subfields from the specified MARC field, returned as
     *  a set of strings to become lucene document field values
     * @param record
     * @param fldTag - the field name, e.g. 245
     * @param subfldsStr - the string containing the desired subfields
     * @returns the result set of strings 
     */
   /* @SuppressWarnings("unchecked")
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
                        String subfldsStrLC = subfldsStr.toLowerCase();
                        int index = subfldsStrLC.indexOf(sf.getCode());
                        if (index != -1)
                        {
	                        if (buffer.length() > 0)  
	                        {
	                            if (Character.isUpperCase(subfldsStr.charAt(index)))
	                            {
	                                resultSet.add(buffer.toString());
	                                buffer = new StringBuffer("");
	                            }
	                            else 
	                            {
	                                buffer.append(separator != null ? separator : " ");
	                            }
	                        }
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
                resultSet.add(((ControlField) vf).getData().trim());
            }
        }
        return resultSet;
    } */
    
    /* 
     * Extract a single cleaned call number from a record
    * @param record
    * @return Clean call number
    */
   public String getCallNumberCleanedNew(final Record record, String sortable)
   {
       boolean sortableFlag = (sortable != null && ( sortable.equals("sortable") || sortable.equals("true")));
       String result = bestSingleCallNumber;
       if (result == null) return(result);
       String resultParts[] = result.split(":", 2);
       if (sortableFlag && ( resultParts[0].equals("LC") || (resultParts[0].equals("") && CallNumUtils.isValidLC(resultParts[1]))))
           result = CallNumUtils.getLCShelfkey(resultParts[1], record.getControlNumber(), super.getErrorHandler());
       else if (resultParts[1].startsWith("M@"))
           result = result.replaceAll("M@", "MSS ");
       return(result);

   }
 
   public Set<String> getCallNumbersCleanedNewNo050(final Record record, String conflatePrefixes)
   {
       return(getCallNumbersCleanedNew(record, conflatePrefixes, this.callNumberFieldListNo050, this.callNumberClusterMapNo050));
   }

   
   public Set<String> getCallNumbersCleanedNew(final Record record, String conflatePrefixes)
   {
       return(getCallNumbersCleanedNew(record, conflatePrefixes, this.callNumberFieldList, this.callNumberClusterMap));
   }

   /**
    * Extract a set of cleaned call numbers from a record
    * @param record
    * @return Clean call number
    */
    public Set<String> getCallNumbersCleanedNew(final Record record, String conflatePrefixes, 
                                                Set<String> localCallNumberFieldList, 
                                                Map<String, Set<String>> localCallNumberClusterMap)
    {
        boolean conflate = !conflatePrefixes.equalsIgnoreCase("false");
        
        if (!conflate)
        {
            Set<String> fieldList = localCallNumberFieldList;
            if (fieldList == null || fieldList.isEmpty())  
            {
                return(null);
            }

            Comparator<String> comp = new StringNaturalCompare();
            Set<String> resultNormed = new TreeSet<String>(comp);
            for (String field : fieldList)
            {
                String fieldParts[] = field.split(":", 2);
                String callNum = fieldParts[1];
                String val = callNum.trim().replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".");
                String nVal = val.replaceAll("^([A-Z][A-Z]?[A-Z]?) ([0-9])", "$1$2");
                if (!nVal.equals(val))
                {
                    val = nVal;
                }
                val = val.replaceFirst("M@", "MSS ");
                resultNormed.add(val);
            }
            return resultNormed;
        }
        else
        {
            Map<String, Set<String>> resultNormed = localCallNumberClusterMap;
            if (resultNormed == null || resultNormed.size() == 0) return(null);
            Set<String> keys = resultNormed.keySet();
            Set<String> results = new TreeSet<String>(normedComparator);
            for (String key : keys)
            {
                Set<String> values = resultNormed.get(key);
                String valueArr[] = values.toArray(new String[0]);
                for (int i = 0; i < valueArr.length; i++)
                {
                    valueArr[i] = getCallNum(valueArr[i]);
                }
                if (valueArr.length == 1)
                {
                    results.add(valueArr[0].replaceAll("M@", "MSS "));
                }
                else
                {
                    String prefix = valueArr[0];
                    for (int i = 1; i < valueArr.length; i++)
                    {
                        prefix = getCommonPrefix(prefix, valueArr[i], normedComparator);
                    }
                    if (prefix.lastIndexOf(' ') != -1)
                    {
                        prefix = prefix.substring(0, prefix.lastIndexOf(' '));
                    }
                    StringBuffer sb = new StringBuffer(prefix);
                    String sep = " ";
                    for (int i = 0; i < valueArr.length; i++)
                    {
                        valueArr[i] = valueArr[i].substring(prefix.length());
                    }
                    Comparator<String> comp = new StringNaturalCompare();
                    Arrays.sort(valueArr, comp);
                    for (int i = 0; i < valueArr.length; i++)
                    {
                        if (valueArr[i].length() > 0) 
                        {
                            sb.append(sep+valueArr[i]);
                            sep = ",";
                        }
                    }
                    if (prefix.startsWith("M@"))
                    {
                        if (sb.length() > 100 || valueArr.length > 2)
                        {
                            int cntBoxes = 0, cntFolders = 0, cntVolumes = 0;
                            for (int i = 0; i < valueArr.length; i++)
                            {
                                if (valueArr[i].contains("Box")) cntBoxes++;
                                if (valueArr[i].contains("Folder")) cntFolders++;   
                                if (valueArr[i].contains("Volume")) cntVolumes++;   
                            }
                            String label = "Boxes";
                            if (cntFolders > cntBoxes && cntFolders > cntVolumes) label = "Folders";
                            else if (cntVolumes > cntBoxes && cntVolumes > cntFolders) label = "Volumes";
                            prefix = prefix.replaceFirst("M@", "MSS ");
                            results.add(prefix + " (" + valueArr.length + " " + label + ")");
                        }
                        else
                        {
                            String value = sb.toString();
                            value = value.replaceAll("M@", "MSS ");
                            results.add(value);
                        }
                    }
                    else
                    {
                        if (sb.length() > 100 || valueArr.length > 10)
                        {
                            results.add(prefix + " (" + valueArr.length + " volumes)");
                        }
                        else
                        {
                            results.add(sb.toString());
                        }
                    }
                }
            }
            return (results);
        }
    }
    
    private String getCallNum(final String callNum)
    {
        String callNumParts[] = callNum.split(":", 2);
        return (callNumParts[1]);
    }

/** 
    * Extract a single cleaned call number from a record
    * @param record
    * @return Clean call number
    * @deprecated
    */
   public String getCallNumberCleaned(final Record record, String fieldSpec, String sortable)
   {
       Set<String> fieldList = getFieldList(record, fieldSpec);
       if (fieldList.isEmpty())  {
           return(null);
       }
       Map<String, Set<String>> resultNormed = getCallNumbersCleanedConflated(fieldList, false);
       if (resultNormed == null || resultNormed.size() == 0) {
           return(null);
       }
       boolean sortableFlag = (sortable != null && ( sortable.equals("sortable") || sortable.equals("true")));
       int maxEntries = 0;
       String maxEntriesKey = null;
       Set<String> maxEntrySet = null;
       Set<String> keys = resultNormed.keySet();
       for (String key : keys)
       {
           Set<String> values = resultNormed.get(key);
           if (values.size() > maxEntries)
           {
               maxEntries = values.size();
               maxEntriesKey = key;
               maxEntrySet = values;
           }
       }
       String valueArr[] = maxEntrySet.toArray(new String[0]);
       Comparator<String> comp = new StringNaturalCompare();
       Arrays.sort(valueArr, comp);
       String result = valueArr[0];
       result = result.trim().replaceAll(":", " ").replaceAll("\\s\\s+", " ")
                             .replaceAll("\\s?\\.\\s?", ".").replaceAll("[(][0-9]* volumes[)]", "");
       if (sortableFlag) 
           result = CallNumUtils.getLCShelfkey(result, null, super.getErrorHandler());
       return(result);

   }
   
   /**
    * Extract a set of cleaned call numbers from a record
    * @param record
    * @return Clean call number
    * @deprecated
    */
    public Set<String> getCallNumbersCleaned(final Record record, String fieldSpec, String conflatePrefixes)
    {
        boolean conflate = !conflatePrefixes.equalsIgnoreCase("false");
        boolean processExtraShadowedIds = fieldSpec.contains("';'");

        //int conflateThreshhold = conflate ? Integer.parseInt(conflatePrefixes) : 0;
        Set<String> fieldList = getFieldList(record, fieldSpec);
        if (fieldList.isEmpty())  {
            return(null);
        }
        if (processExtraShadowedIds)
        {
            loadExtraShadowedIds(extraIdsFilename, boundWithsFilename);
            Set<String> newFieldList = new LinkedHashSet<String>();
            String extraString = addnlShadowedIds.get(record.getControlNumber());
          
            for (String field : fieldList)
            {
                String fieldparts[] = field.split(";");
                if (fieldparts.length != 2) continue;
                if (extraString == null || extraString.equals("") || !extraString.contains("|" + fieldparts[1] + "|"))
                {
                    newFieldList.add(fieldparts[0]);
                }
            }
            fieldList = newFieldList;
        }
        if (!conflate)
        {
            Comparator<String> comp = new StringNaturalCompare();
            Set<String> resultNormed = new TreeSet<String>(comp);
            for (String callNum : fieldList)
            {
                String val = callNum.trim().replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".");
                String nVal = val.replaceAll("^([A-Z][A-Z]?[A-Z]?) ([0-9])", "$1$2");
                if (!nVal.equals(val))
                {
                    val = nVal;
                }
                resultNormed.add(val);
            }
            return resultNormed;
        }
        else
        {
            Comparator<String> normedComparator = new Comparator<String>() 
            {
                public int compare(String s1, String s2)
                {
                    String s1Norm = s1.replaceAll("[. ]", "");
                    String s2Norm = s2.replaceAll("[. ]", "");
                    return s1Norm.compareToIgnoreCase(s2Norm);
                }
            };

            Map<String, Set<String>> resultNormed = getCallNumbersCleanedConflated(fieldList, false);
            Set<String> keys = resultNormed.keySet();
            Set<String> results = new TreeSet<String>(normedComparator);
            for (String key : keys)
            {
                Set<String> values = resultNormed.get(key);
                String valueArr[] = values.toArray(new String[0]);
                if (valueArr.length == 1)
                {
                    results.add(valueArr[0]);
                }
                else
                {
                    String prefix = valueArr[0];
                    for (int i = 1; i < valueArr.length; i++)
                    {
                        prefix = getCommonPrefix(prefix, valueArr[i], normedComparator);
                    }
                    if (prefix.lastIndexOf(' ') != -1)
                    {
                        prefix = prefix.substring(0, prefix.lastIndexOf(' '));
                    }
                    StringBuffer sb = new StringBuffer(prefix);
                    String sep = " ";
                    for (int i = 0; i < valueArr.length; i++)
                    {
                        valueArr[i] = valueArr[i].substring(prefix.length());
                    }
                    Comparator<String> comp = new StringNaturalCompare();
                    Arrays.sort(valueArr, comp);
                    for (int i = 0; i < valueArr.length; i++)
                    {
                        if (valueArr[i].length() > 0) 
                        {
                            sb.append(sep+valueArr[i]);
                            sep = ",";
                        }
                    }
                    if (sb.length() > 100 || valueArr.length > 10)
                    {
                        results.add(prefix + " (" + valueArr.length + " volumes)");
                    }
                    else
                    {
                        results.add(sb.toString());
                    }
                }
            }
            return (results);
        }
    }
 
    final static String stateTable[][] = 
    {{"Alabama", "Ala."}, {"Alaska", "Alaska"}, {"Arizona", "Ariz."}, {"Arkansas", "Ark."}, {"California", "Calif."}, 
     {"Colorado", "Colo."}, {"Connecticut", "Conn."}, {"Delaware", "Del."}, {"Florida", "Fla."}, {"Georgia", "Ga."}, 
     {"Hawaii", "Hawaii"}, {"Idaho", "Idaho"}, {"Illinois", "Ill."}, {"Indiana", "Ind."}, {"Iowa", "Iowa"},  
     {"Kansas", "Kan."},{"Kentucky", "Ky."}, {"Louisiana", "La."}, {"Maine", "Maine"}, {"Maryland", "Md."}, 
     {"Massachusetts", "Mass."}, {"Michigan", "Mich."}, {"Minnesota", "Minn."}, {"Mississippi", "Miss."}, {"Missouri", "Mo."},
     {"Montana", "Mont."}, {"Nebraska", "Neb."}, {"Nevada", "Nev."}, {"New Hampshire", "N.H."}, {"New Jersey", "N.J."},  
     {"New Mexico", "N.M."},{"New York", "N.Y."}, {"North Carolina", "N.C."}, {"North Dakota", "N.D."}, {"Ohio", "Ohio"}, 
     {"Oklahoma", "Okla."}, {"Oregon", "Or."}, {"Pennsylvania", "Pa."}, {"Rhode Island", "R.I."}, {"South Carolina", "S.C."},  
     {"South Dakota", "S.D."},{"Tennessee", "Tenn."}, {"Texas", "Tex."}, {"Utah", "Utah"}, {"Vermont", "Vt."}, 
     {"Virginia", "Va."}, {"Washington", "Wash."}, {"West Virginia", "W. Va."}, {"Wisconsin", "Wis."}, {"Wyoming", "Wyo."}, 
     {"New York (State)", "N.Y."}, {"District of Columbia", "D.C."}, {"Puerto Rico", "P.R."}, {"Virgin Islands", "V.I."}, 
     {"Alberta", "Alta."}, {"British Columbia", "B.C."}, {"Manitoba", "Man."}, {"Newfoundland and Labrador", "N.L."}, 
     {"New Brunswick", "N.B."}, {"Northwest Territories", "N.W.T."}, {"Nova Scotia", "N.S."}, {"Nunavut", "Nunavut"}, 
     {"Ontario", "Ont."}, {"Prince Edward Island", "P.E.I."}, {"Quebec", "Que'bec"}, {"Saskatoon", "Sask."}, {"Yukon", "Yukon"},
     {"Australian Capital Territory", "A.C.T."}, {"New South Wales", "N.S.W."}, {"Northern Territory", "N.T."}, {"Queensland", "Qld."}, 
     {"South Australia", "S. Aust."}, {"Tasmania", "Tas."}, {"Victoria", "Vic."}, {"Western Australia", "W.A." }};
    
    static Map<String, String> stateMap = null;
    
    private String getStateNameAbbrev(String stateName)
    {
        if (stateMap == null)
        {
            stateMap = new LinkedHashMap<String, String>();
            for (int i = 0; i < stateTable.length; i++)
            {
                stateMap.put(stateTable[i][0], stateTable[i][1]);
            }
        }
        return(stateMap.get(stateName));
    }

    final static String locationTypeNames[] = {"State", "Republic", "Principality", "Province", "Township", "County", "Town",
                                               "Judicial district", "Prefecture", "Region", "District", "Dept.", "Kingdom", 
                                               "Canton", "City", "Division", "Duchy", "Emirate", "Government", "Country",
                                               /* India: */ "Princely State", "Presidency", "Tahsil", "Taluka", "Tehsil", "Thana",
                                               /* China: */ "Sheng",
                                               /* Denmark: */  "Amt", "Herred",
                                               /* Thailand: */  "Amphoe",
                                               /* France: */ "Comte´",
                                               /* South/Central America: */ "Corregimiento", "Distrito Federal", "Intendancy", "Partido", 
                                               /* Religious: */ "Diocese", "diocese", "Archdiocese", "Archdeaconry", "Ecclesiastical principality",
                                               /* Poland: */ "Voivodeship", "Powiat",
                                               /* Germany:*/ "Landkreis", "Kreis", "Bezirk", "Electorate", "Grafschaft",
                                               /* Czech: */ "Okres", 
                                               /* Russia: */ "Oblast'", "Oblast", "Kray", 
                                               /* Hungary: */ "Comitat",
                                               /* Romania: */ "Judet",
                                               /* Indonesia: */ "Kabupaten", 
                                               /* Former: */ "Ancient city", "Ancient sanctuary", "Ancient site", "Extinct city", "Concentration camp", "Colony", "Site",
                                               /* Descriptive: */ "Peninsula", "Coast", "Cape", "Harbor", "Island", "Lake", "Oasis", "Tribal area" };
    static Set<String> locationTypeNameSet = null;

    private static boolean isLocationTypeWord(String name)
    {
        if (locationTypeNameSet == null)
        {
            locationTypeNameSet = new LinkedHashSet<String>();
            for (String locType : locationTypeNames)
            {
                locationTypeNameSet.add(locType);
            }
        }
        if (locationTypeNameSet.contains(name))  return(true);
        return(false);
    }
    
    private boolean isEqualsOrContains(String string1, String string2)
    {
        if (string1.equals(string2))  return(true);
        if (string1.contains(" and "+ string2)) return(true);
        if (string1.contains(string2 + " and ")) return(true);
        if (string1.contains(", "+ string2)) return(true);
        String tmp = getStateNameAbbrev(string2);
        if (tmp != null && tmp.equals(string1)) 
            return(true);
        return(false);
    }

    private Set<String> getSet650z(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        List<VariableField> fields = (List<VariableField>)record.getVariableFields("650");
        for (VariableField f : fields)
        {
            DataField df = (DataField)f;
            List<Subfield> sfs = (List<Subfield>)df.getSubfields();
            boolean prevWasZ = false;
            StringBuffer part = new StringBuffer();
            boolean hadPrevZ = false;
            int zCount = 0;
            for (Subfield sf : sfs)
            {
                if (sf.getCode() == 'z')
                {
                    zCount++;
                    if (zCount > 2)
                    {
                        prevWasZ = true;
                    }
                    if (prevWasZ)
                    {
                        String data = Utils.cleanData(sf.getData());
                        if (data.equals("South America") || data.equals("Central America") || data.equals("United States"))
                        {
                            //part.insert(0, data+"#");
                        }
                        else
                        {
                            part.append("#");
                            part.append(Utils.cleanData(sf.getData()));
                        }
                    }
                    else
                    {
                        if (hadPrevZ)
                        {
                            hadPrevZ = true;
                        }
                        part.append(Utils.cleanData(sf.getData()));
                    }
                    prevWasZ = true;
                    hadPrevZ = true;
                }
                else
                {
                    zCount = 0;
                    if (prevWasZ)
                    {
                        result.add(part.toString());
                        part.setLength(0);
                        prevWasZ = false;
                    }
                }
            }
            if (prevWasZ)
            {
                result.add(part.toString());
                part.setLength(0);
                prevWasZ = false;
            }
        }
        return(result);   
    }
    
    public Set<String> getRegionFacet(final Record record)
    {
        Set<String> result = removeTrailingPunct(record, "651a");
        Set<String> sub650z = getSet650z(record);
        for (String fields650 : sub650z)
        {
            String parts[] = fields650.split("#");
            parts[0] = Utils.cleanData(parts[0]);
            result.add(parts[0]);
            for (int i = 1; i < parts.length; i++)
            {
                if (i == 2)  // 650  0$aEthnology$zRussia (Federation)$zSiberia$xResearch$zPoland$xHistory$vCongresses.  0$aLabor movement$zBrazil$zNatal$zDurban.
                {
                //  things to decide :  $z Colombia $z Zipaquira $z South America    or $z Germany $z Berlin $z Tiergarten or $z Nicaragua $z Rivas (Dept.)$z Central America  or  $z Italy $z Sicily  $z Camarina (Extinct city)
                    parts[0] = parts[1].replaceAll("((\\p{L}\\p{M}*|\\.|[- ])+(\\p{L}\\p{M}*|\\.))[ ]?\\(((\\p{L}\\p{M}*|\\.|[- ])+).*", "$1, $4");
                    i = 2;  
                }
                parts[i] = Utils.cleanData(parts[i]);
                String abbrev = getStateNameAbbrev(parts[0]);
                if (abbrev != null)
                {
                    parts[i] = parts[i] + " (" + abbrev + ")";
                }
                else if (parts[i].endsWith(")"))
                {
                    if (!parts[i].contains("("))
                    {
                        parts[i] = parts[i].substring(0, parts[i].length()-1) + " (" + parts[0] + ")";
                    }
                    else if (parts[i].matches(".*[ ]?\\((\\p{L}\\p{M}*|\\.|[- ])+\\)"))
                    {
                        String subparts[] = parts[i].split("[ ]?\\(", 2);
                        if (subparts.length > 1)
                        {
                            subparts[1] = subparts[1].substring(0, subparts[1].length()-1);
                            if (!subparts[1].equals(parts[0]) && isLocationTypeWord(subparts[1]))
                            {
                                parts[i] = subparts[0] + " (" + parts[0] + " : " + subparts[1] + ")";
                            }
                            else if (!isEqualsOrContains(subparts[1], parts[0]))
                            {
                                parts[i] = parts[i].substring(0, parts[i].length()-1) + ", " + parts[0] + ")";
                            }
                            //else leave parts[i] as is.
                            else
                            {
                                parts[i] = parts[i];
                            }
                        }
                        else
                        {
                            parts[i] = parts[i].substring(0, parts[i].length()-1) + ", " + parts[0] + ")";
                        }
                    }
                    else  //  things to decide :  $z Germany $z Blah (Something : District) or $z Italy $z Satricum (Lazio : Extinct city)
                    {
                        if (parts[i].matches("(\\p{L}\\p{M}*|\\.|[- ])+[ ]?\\((\\p{L}\\p{M}*|\\.|[- ])+ : (\\p{L}\\p{M}*|\\.|[- ])+\\)"))
                        {    
                        // equivalent of, but with expanded character sets to include unicode accented letters and accent marks : 
                        //    parts[i] = parts[i].replaceFirst("([-A-Za-z ]+[A-Za-z])[ ]?\\(([-A-Za-z ]+) : ([-A-Za-z ]+)\\)", 
                        //                                     "$1 ($2, "+parts[0]+" : $3)");
                            parts[i] = parts[i].replaceFirst("((\\p{L}\\p{M}*|\\.|[- ])+(\\p{L}\\p{M}*|\\.))[ ]?\\(((\\p{L}\\p{M}*|\\.|[- ])+) : ((\\p{L}\\p{M}*|\\.|[- ])+)\\)", 
                                                             "$1 ($4, "+parts[0]+" : $6)");
                        }
                        else
                            parts[i] = parts[i];
                    }
                }
                else 
                {
                    parts[i] = parts[i] + " (" + parts[0] + ")";
                }                
                result.add(parts[i]);
            }
        }
        return(result);
    }

    private String getURLLabelFrom3andZ(DataField df, String defaultLabel)
    {
        String label = "";
        
        List<Subfield> subs = (List<Subfield>)df.getSubfields();
        for (Subfield sf : subs)
        {
            if (sf.getCode() == 'z' || sf.getCode() == '3')
            {
                label = label + sf.getData() + " ";
            }
        }
        label = label.replaceAll("[ ]+$", "");
        if (label.length()== 0) label = defaultLabel;
        return(label);
    }
    
    private String buildParsableURLString(DataField df, String defaultLabel)
    {
        String label = getURLLabelFrom3andZ(df, defaultLabel);
        String url = df.getSubfield('u').getData(); 
        String result = url + "||" + label;
        return(result);
    }
    
    public Set<String> getLabelledURL(final Record record, String defaultLabel)
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        Set<String> backupResultSet = new LinkedHashSet<String>();
        List<?> urlFields = record.getVariableFields("856");
        for (Object field : urlFields)
        {
            if (field instanceof DataField)
            {
                DataField dField = (DataField)field;
                if (dField.getIndicator1() == '4' && dField.getIndicator2() == '0')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
                else if (dField.getIndicator1() == '4' && dField.getIndicator2() == '1')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
//                if (dField.getIndicator1() == '4' && dField.getIndicator2() == '2')
//                {
//                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related Info";
//                    if (dField.getSubfield('u') != null) 
//                    {
//                        resultSet.add(buildParsableURLString(dField, label));
//                    }
//                }
                else if (dField.getIndicator1() == '4' && dField.getIndicator2() == ' ')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '0')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '1')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
//                if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '2')
//                {
//                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related Info";
//                    if (dField.getSubfield('u') != null) 
//                    {
//                        backupResultSet.add(buildParsableURLString(dField, label));
//                    }
//                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == ' ')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
            }
        }
        if (resultSet.size() == 0 && backupResultSet.size() != 0)
        {
            return(backupResultSet);
        }
        return(resultSet);
    }
    
    public Set<String> getLabelledURLnew(final Record record, String defaultLabel)
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        Set<String> backupResultSet = new LinkedHashSet<String>();
        List<?> urlFields = record.getVariableFields("856");
        for (Object field : urlFields)
        {
            if (field instanceof DataField)
            {
                DataField dField = (DataField)field;
                if (dField.getIndicator1() == '4' && dField.getIndicator2() == '0')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
                else if (dField.getIndicator1() == '4' && dField.getIndicator2() == '1')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related resources";
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label));
                    }
                }
//                if (dField.getIndicator1() == '4' && dField.getIndicator2() == '2')
//                {
//                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related Info";
//                    if (dField.getSubfield('u') != null) 
//                    {
//                        resultSet.add(buildParsableURLString(dField, label));
//                    }
//                }
                else if (dField.getIndicator1() == '4' && dField.getIndicator2() == ' ')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : defaultLabel;
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '0')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '1')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related resources";
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label));
                    }
                }
//                if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '2')
//                {
//                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related Info";
//                    if (dField.getSubfield('u') != null) 
//                    {
//                        backupResultSet.add(buildParsableURLString(dField, label));
//                    }
//                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == ' ')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : defaultLabel;
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label));
                    }
                }
            }
        }
        if (resultSet.size() == 0 && backupResultSet.size() != 0)
        {
            return(backupResultSet);
        }
        return(resultSet);
    }
    
    private String getCommonPrefix(String string1, String string2, Comparator comp)
    {
        int l1 = string1.length();
        int l2 = string2.length();
        int l = Math.min(l1, l2);
        int prefixLen = l;
        for (int i = 0; i < l; i++)
        {
            if (comp.compare(string1.substring(i, i+1), string2.substring(i, i+1))!= 0)
            {
                prefixLen = i;
                break;
            }
        }
        return(string1.substring(0, prefixLen));
    }

    /**
     * Extract the OCLC number from a record
     * @param record
     * @return OCLC number
     */
    public String getOclcNum(final Record record)
    {
        Set<String> set = getFieldList(record, "035a");
        
        if (set.isEmpty())  {
            return(null);
        }
        
        Iterator<String> iter = set.iterator();
        
        while (iter.hasNext())
        {
            String value = (String)iter.next();
            if (value.contains("(OCoLC)"))  
            {
                value = value.replaceAll("\\(OCoLC\\)", "");
                return(value);
            }
        }
        return null;
    }
    
    public Set<String> getCombinedFormatNew(final Record record)
    {    
        // part1_format_facet = 000[6]:007[0], format_maps.properties(broad_format), first
        // part2_format_facet = 999t, format_maps.properties(format)

        String mapName1 = loadTranslationMap(null, "format_maps.properties(broad_format)");
        String mapName1a = loadTranslationMap(null, "format_maps.properties(broad_format_electronic)");
        String mapName2 = loadTranslationMap(null, "format_maps.properties(format_007)");
        String mapName3 = loadTranslationMap(null, "format_maps.properties(format)");

        Set<String> result = getFieldList(record, "999t");
        result = Utils.remap(result, findMap(mapName3), false);

        Set<String> f245h = getFieldList(record, "245h");
        if (Utils.setItemContains(f245h, "cartographic material"))
        {
            result.add("Map");
        }
        if (Utils.setItemContains(f245h, "videorecording"))
        {
            result.add("Video");
        }
        Set<String> urls = getFieldList(record, "856u");
        Set<String> format_007_raw = getFieldList(record, "007[0-1]");
        if (Utils.setItemContains(format_007_raw, "cr") || Utils.setItemContains(result, "Online"))
        {
            String other007 = null;
            String broadFormat = getFirstFieldVal(record, null, "000[6-7]");
            if (format_007_raw.size() >= 1)
            {
                for (String str007 : format_007_raw)
                {
                    if (!str007.equals("cr"))
                    {
                        other007 = str007;
                        break;
                    }
                }
            }
            if (other007 != null && other007.startsWith("v")) 
            {
                result.add("Online"); // Online
                result.add(Utils.remap("v", findMap(mapName1a), true)); // Streaming Video
                result.add(Utils.remap("v", findMap(mapName2), true));  // Video
            }
            else if (broadFormat.equals("am")) 
            {
                result.add("Online"); // Online
                result.add(Utils.remap("am", findMap(mapName1a), true)); // eBook
                result.add(Utils.remap("a", findMap(mapName1), true));  // Book
            }
            else if (broadFormat.equals("as"))
            {
                result.add("Online"); // Online
                result.add(Utils.remap("as", findMap(mapName1a), true)); // Online
                result.add(Utils.remap("as", findMap(mapName1), true));  // Journal/Magazine
            }
            else if (broadFormat.startsWith("m"))
            {
                result.add(Utils.remap("m", findMap(mapName1), true));
            }
        }
        else if (Utils.setItemContains(urls, "serialssolutions"))
        {
            String serialsFormat = Utils.remap("as", findMap(mapName1), true);
            if (serialsFormat != null) result.add(serialsFormat);
        }
        else
        {
            String format_007 = getFirstFieldVal(record, mapName2, "007[0]");
            if (format_007 != null) 
            {
                result.add(format_007);
            }
            else 
            {
                String broadFormat = getFirstFieldVal(record, mapName1, "000[6-7]:000[6]");
                if (broadFormat != null) result.add(broadFormat);
            }
        }
        return(result);
    }
    
    @Override
    public Set<String> getFormatMapped(Record record, String propertyFileName)
    {
        return(getCombinedFormatNew2(record));
    }
    
    public Set<String> getCombinedFormatNew2(final Record record)
    {    
        if (combinedFormat != null) return(combinedFormat);
        // part1_format_facet = 000[6]:007[0], format_maps.properties(broad_format), first
        // part2_format_facet = 999t, format_maps.properties(format)

        String mapName1 = loadTranslationMap(null, "format_maps.properties(broad_format)");
        String mapName1a = loadTranslationMap(null, "format_maps.properties(broad_format_electronic)");
        String mapName2 = loadTranslationMap(null, "format_maps.properties(format_007)");
        String mapName3 = loadTranslationMap(null, "format_maps.properties(format)");

        List<?> fields999 = this.trimmedHoldingsList;
//        if (addnlShadowedIds != null)
//        {
//            removeShadowed999sFromList(record, fields999);
//        }
        Set<String> result = getSubfieldFromFieldList(fields999, 't');
        combinedFormat = Utils.remap(result, findMap(mapName3), false);

        Set<String> f245h = getFieldList(record, "245h");
        if (Utils.setItemContains(f245h, "cartographic material"))
        {
            combinedFormat.add("Map");
        }
        Set<String> urls = getFieldList(record, "856u");
        Set<String> format_007_raw = getFieldList(record, "007[0-1]");
        if (Utils.setItemContains(format_007_raw, "cr") || Utils.setItemContains(combinedFormat, "Online"))
        {
            String other007 = null;
            String broadFormat = getFirstFieldVal(record, null, "000[6-7]");
            if (format_007_raw.size() >= 1)
            {
                for (String str007 : format_007_raw)
                {
                    if (!str007.equals("cr"))
                    {
                        other007 = str007;
                        break;
                    }
                }
            }
            List<?> field856 = record.getVariableFields("856");
            boolean isOnline = false;
            for (Object f : field856)
            {
                if (f instanceof DataField)
                {
                    DataField df = (DataField)f;
                    if ((df.getIndicator1() == '4' && df.getIndicator2() == '0') ||
                        (df.getIndicator1() == '1' && df.getIndicator2() == '1'))
                    {
                        isOnline = true;
                    }
                }
            }
            if (other007 != null && other007.startsWith("v")) 
            {
                if (isOnline) combinedFormat.add(Utils.remap("v", findMap(mapName1a), true)); // Streaming Video
                Set<String> f300e = getFieldList(record, "300e");
                String broadFormatLetter = getFirstFieldVal(record, null, "000[6]");
                if (broadFormatLetter.equals("o") || broadFormatLetter.equals("a") || broadFormatLetter.equals("j") || broadFormatLetter.equals("m") ||
                        Utils.setItemContains(f300e, "videocassette") || Utils.setItemContains(f300e, "DVD") || Utils.setItemContains(f300e, "videodisc"))
                {
                    combinedFormat.add("Includes Video");
                    if (!combinedFormat.contains("Video")) combinedFormat.remove("Video");
                }
                else
                {
                    combinedFormat.add("Video");
                    boolean ff = false;  //  ff means found format;
                    if (combinedFormat.contains("VHS") || combinedFormat.contains("DVD") || combinedFormat.contains("Laser Disc")) ff = true;
                    if (!ff)
                    {
                        Set<String> field538a = getFieldList(record, "538a");
                        if (Utils.setItemContains(field538a, "VHS")) { combinedFormat.add("VHS"); ff = true; }
                        if (Utils.setItemContains(field538a, "DVD")) { combinedFormat.add("DVD"); ff = true; }
                    }
                    if (!ff)
                    {
                        Set<String> field300a = getFieldList(record, "300a");
                        if (Utils.setItemContains(field300a, "videocassette")) { combinedFormat.add("VHS"); ff = true; }
                        if (Utils.setItemContains(field300a, "videodisc"))     { combinedFormat.add("DVD"); ff = true; }
                    }
                    if (!ff)
                    {
                        Set<String> field500a = getFieldList(record, "500a");
                        if (Utils.setItemContains(field500a, "videocassette")) { combinedFormat.add("VHS"); ff = true; }
                        if (Utils.setItemContains(field500a, "videodisc"))     { combinedFormat.add("DVD"); ff = true; }
                    }
                }
            }
            else if (broadFormat.equals("am")) 
            {
                if (isOnline) combinedFormat.add(Utils.remap("am", findMap(mapName1a), true)); // eBook
                combinedFormat.add(Utils.remap("a", findMap(mapName1), true));  // Book
            }
            else if (broadFormat.equals("as"))
            {
                if (isOnline) combinedFormat.add(Utils.remap("as", findMap(mapName1a), true)); // Online
                combinedFormat.add(Utils.remap("as", findMap(mapName1), true));  // Journal/Magazine
            }
            else if (broadFormat.startsWith("m"))
            {
                combinedFormat.add(Utils.remap("m", findMap(mapName1), true));
            }
        }
        else if (Utils.setItemContains(urls, "serialssolutions"))
        {
            String serialsFormat = Utils.remap("as", findMap(mapName1), true);
            if (serialsFormat != null) combinedFormat.add(serialsFormat);
        }
        else
        {
            String format_007 = getFirstFieldVal(record, mapName2, "007[0]");
            String broadFormat2Letters = getFirstFieldVal(record, null, "000[6-7]");
            String broadFormatLetter = getFirstFieldVal(record, null, "000[6]");
            Set<String> broadFormatTmpSet = new LinkedHashSet<String>();
            broadFormatTmpSet.add(broadFormat2Letters);
            Set<String> broadFormat = Utils.remap(broadFormatTmpSet, findMap(mapName1), false);
            if (broadFormat.isEmpty())
            {
                broadFormatTmpSet.add(broadFormatLetter);
                broadFormat = Utils.remap(broadFormatTmpSet, findMap(mapName1), false);
            }
            if (format_007 != null) 
            {
                if (format_007.equals("Video"))
                {
                    // handle video-ness from 007 field below
                }
                else if (!format_007.equals("Map") || (!broadFormat.isEmpty() && (broadFormat.contains("Map") || broadFormat.contains("Book"))))
                {
                    combinedFormat.add(format_007);
                }
                else
                {
                    format_007 = format_007;
                }
            }
            if (!broadFormat.isEmpty()) 
            {
                combinedFormat.addAll(broadFormat);
            }
            int videoness = 0;
            if (broadFormatLetter.equals("g")) videoness++;
            String Val008_33 = getFirstFieldVal(record, null, "008[33]");
            Set<String> field300a = null;
            if (format_007 != null && format_007.equals("Video")) videoness++;
            if (Val008_33 != null && Val008_33.equals("v")) videoness++;
            if (videoness < 2 && Utils.setItemContains(f245h, "videorecording")) videoness++;
            if (videoness < 2)
            {
                field300a = getFieldList(record, "300a");
                if (Utils.setItemContains(field300a, "ideocassette") ||  Utils.setItemContains(field300a, "ideodisc"))  
                {
                    videoness++;
                }
            }
            if (videoness < 2)
            {
                Set<String> field650a = getFieldList(record, "650a");
                if (Utils.setItemContains(field650a, "film")) 
                {
                    videoness++;
                }
            }
            if (videoness >= 2 && Utils.setItemContains(f245h, "electronic resource")) videoness--;
            if (videoness >= 2)
            {
                if (broadFormatLetter.equals("a"))
                {
                    combinedFormat.remove("Book");
                }
                combinedFormat.add("Video");
                boolean ff = false;  //  ff means found format;
                if (combinedFormat.contains("VHS") || combinedFormat.contains("DVD") || combinedFormat.contains("Laser Disc")) ff = true;
                if (!ff)
                {
                    Set<String> field538a = getFieldList(record, "538a");
                    if (Utils.setItemContains(field538a, "VHS")) { combinedFormat.add("VHS"); ff = true; }
                    if (Utils.setItemContains(field538a, "DVD")) { combinedFormat.add("DVD"); ff = true; }
                }
                if (!ff)
                {
                    if (field300a == null) field300a = getFieldList(record, "300a");
                    // omit the v to catch both upper and lower case 
                    if (Utils.setItemContains(field300a, "ideocassette")) 
                    { 
                        Set<String> field300c = getFieldList(record, "300c");
                        if (Utils.setItemContains(field300c, "3/4"))  combinedFormat.add("U-matic"); 
                        else combinedFormat.add("VHS");
                        ff = true;
                    }
                    // omit the v to catch both upper and lower case 
                    if (Utils.setItemContains(field300a, "ideodisc"))  
                    { 
                        Set<String> field300c = getFieldList(record, "300c");
                        if (Utils.setItemContains(field300c, "12"))  combinedFormat.add("Laser Disc"); 
                        else combinedFormat.add("DVD");
                        ff = true;
                    }
                }
                if (!ff)
                {
                    Set<String> field500a = getFieldList(record, "500a");
                    if (Utils.setItemContains(field500a, "VHS")) { combinedFormat.add("VHS"); ff = true; }
                    if (Utils.setItemContains(field500a, "DVD")) { combinedFormat.add("DVD"); ff = true; }
                }
            }
            if (videoness == 1 && format_007 != null && format_007.equals("Video"))
            {
                combinedFormat.add("Includes Video");
                if (combinedFormat.contains("Video")) combinedFormat.remove("Video");
            }
           //     if (broadFormat != null && format_007 != null) System.out.println("format diff for item: "+ record.getControlNumber()+" : format_007 = "+format_007+ "  broadFormat = " + broadFormat);
        }
        return(combinedFormat);
    }

    private Set<String> getSubfieldFromFieldList(List<?> fields999, char code)
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        for (Object field : fields999)
        {
            DataField df = (DataField)field;
            Subfield subfield = df.getSubfield(code);            
            if (subfield != null) resultSet.add(subfield.getData());
        }
        return resultSet;
    }

    public Set<String> getCombinedFormat(final Record record)
    {    
    	// part1_format_facet = 000[6]:007[0], format_maps.properties(broad_format), first
    	// part2_format_facet = 999t, format_maps.properties(format)

	    String mapName1 = loadTranslationMap(null, "format_maps.properties(broad_format)");
	    String mapName2 = loadTranslationMap(null, "format_maps.properties(format_007)");
	    String mapName3 = loadTranslationMap(null, "format_maps.properties(format)");

        Set<String> result = getFieldList(record, "999t");
        result = Utils.remap(result, findMap(mapName3), false);

        Set<String> urls = getFieldList(record, "856u");
	    if (Utils.setItemContains(urls, "serialssolutions"))
	    {
            String serialsFormat = Utils.remap("as", findMap(mapName1), true);
            if (serialsFormat != null) result.add(serialsFormat);
	    }
	    else
	    {
	        String format_007 = getFirstFieldVal(record, mapName2, "007[0]");
            if (format_007 != null) 
            {
                result.add(format_007);
            }
            else 
            {
                String broadFormat = getFirstFieldVal(record, mapName1, "000[6-7]:000[6]");
                if (broadFormat != null) 
                {
                    if (broadFormat.contains("|"))
                        broadFormat = broadFormat.substring(0, broadFormat.indexOf('|'));
                    result.add(broadFormat);
                }
            }
	    }
        return(result);
    }
    
//    public Set<String> getLocationWithShadowing(final Record record, String propertiesMap)
//    {
//        String mapName = loadTranslationMap(null, propertiesMap);
//
//        Set<String> fields = getFieldList(record, "999kl';'");
//        Set<String> result = new LinkedHashSet<String>();
//        for (String field : fields)
//        {
//            String fparts[] = field.split(";");
//            if (fparts.length == 1)
//            {
//                String mappedFpart = Utils.remap(fparts[0], findMap(mapName), true);
//                if (mappedFpart != null) result.add(mappedFpart);
//            }
//            else if (fparts.length == 2)
//            {
//                String mappedFpart1 = Utils.remap(fparts[0], findMap(mapName), true);
//                String mappedFpart2 = Utils.remap(fparts[1], findMap(mapName), true);
//                if (mappedFpart1 != null && mappedFpart1.equals("-") && mappedFpart2 != null)
//                {
//                    result.add(mappedFpart2);
//                }
//                else if (mappedFpart1 != null  && mappedFpart2 != null)
//                {
//                    result.add(mappedFpart1);
//                    result.add(mappedFpart2);
//                }
//            }
//        }
//        return(result);        
//    }

    public Set<String>getCustomLibrary(final Record record, String visibilityMap, String libraryMap)
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        List<?> fields999 = trimmedHoldingsList;
        String visMapName = loadTranslationMap(null, visibilityMap);
        String libMapName = loadTranslationMap(null, libraryMap);
        for ( DataField field : (List<DataField>)fields999 )
        {
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            String lib = (libF != null ? libF.getData() : null);
            String mappedLib = Utils.remap(lib, findMap(libMapName), true);
            if (mappedLib == null || resultSet.contains(mappedLib))  continue;
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            String mappedHomeVis = Utils.remap(homeLoc, findMap(visMapName), true);
            if (mappedHomeVis.equals("HIDDEN") )
            {
                continue;
            }
            if (curLoc != null)
            {
                String mappedCurVis = Utils.remap(curLoc, findMap(visMapName), true);
                if (mappedCurVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
            }
            resultSet.add(mappedLib);
        }
//        for ( DataField field : (List<DataField>)fields999 )
//        {
//            Subfield curLocF = field.getSubfield('k');
//            Subfield homeLocF = field.getSubfield('l');
//            Subfield libF = field.getSubfield('m');
//            String lib = (libF != null ? libF.getData() : null);
//            String mappedLib = Utils.remap(lib, findMap(libMapName), true);
//            if (mappedLib != null && !resultSet.contains(mappedLib))  
//            {
//                resultSet.add("NOT "+mappedLib);
//            }
//        }
        return(resultSet);
    }
    
    public Set<String>getCustomLocation(final Record record, String locationMap, String visibilityMap, String libraryMap)
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        List<?> fields999 = trimmedHoldingsList;
        String locMapName = loadTranslationMap(null, locationMap);
        String visMapName = loadTranslationMap(null, visibilityMap);
        String libMapName = loadTranslationMap(null, libraryMap);
        for ( DataField field : (List<DataField>)fields999 )
        {
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            String lib = (libF != null ? libF.getData() : null);
            String mappedHomeVis = Utils.remap(homeLoc, findMap(visMapName), true);
            String mappedHomeLoc = Utils.remap(homeLoc, findMap(locMapName), true);
            if (mappedHomeVis.equals("VISIBLE") && mappedHomeLoc == null)
            {
                String combinedLocMapped = Utils.remap(homeLoc + "__" + lib, findMap(locMapName), true);
                if (combinedLocMapped != null) mappedHomeLoc = combinedLocMapped;
            }
            String mappedLib = Utils.remap(lib, findMap(libMapName), true);
            if (curLoc != null)
            {
                String mappedCurLoc = Utils.remap(curLoc, findMap(locMapName), true);
                String mappedCurVis = Utils.remap(curLoc, findMap(visMapName), true);
                if (mappedCurVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
                if (mappedCurLoc != null) 
                {
                    if (mappedCurLoc.contains("$m") && mappedLib != null)
                    {
          //              mappedCurLoc.replaceAll("$l", mappedHomeLoc);
                        mappedCurLoc = mappedCurLoc.replaceAll("[$]m", mappedLib);
                    }
                    else if (mappedCurLoc.contains("$m") && mappedLib == null)
                    {
          //              mappedCurLoc.replaceAll("$l", mappedHomeLoc);
                        mappedCurLoc = mappedCurLoc.replaceAll("[$]m[ ]?", "");
                    }
                    resultSet.add(mappedCurLoc);
                    continue;   // Used
                }
            }
            if (mappedHomeVis.equals("HIDDEN"))  continue; // this copy of the item is Hidden, go no further
            if (mappedHomeLoc != null && mappedHomeLoc.contains("$"))
            {
                if (mappedLib != null)
                    mappedHomeLoc = mappedHomeLoc.replaceAll("[$]m", mappedLib);
                else
                    mappedHomeLoc = mappedHomeLoc.replaceAll("[$]m", "Library");

            }
            if (mappedHomeLoc != null) resultSet.add(mappedHomeLoc);
        }
        return(resultSet);
    }
    
    public Set<String> getCustomLanguage(final Record record, String propertiesMap)
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        String mapName = loadTranslationMap(null, propertiesMap);
        String primaryLanguage = getFirstFieldVal(record, mapName, "008[35-37]");
        Set<String> otherLanguages = getFieldList(record, "041a:041d");
        otherLanguages = Utils.remap(otherLanguages, findMap(mapName), true);
        Set<String> translatedFrom = getFieldList(record, "041h");
        translatedFrom = Utils.remap(translatedFrom, findMap(mapName), true);
        Set<String> subtitleLanguage = getFieldList(record, "041b");
        subtitleLanguage = Utils.remap(subtitleLanguage, findMap(mapName), true);
        Set<String> format = getCombinedFormat(record);
        boolean isBook = Utils.setItemContains(format, "Book") || Utils.setItemContains(format, "Journal");
        boolean isDVD = Utils.setItemContains(format, "DVD") ;
        Set<String> notesFields = getFieldList(record, "500a");
        boolean isTranslated = Utils.setItemContains(notesFields, "[Tt]ranslat((ed)|(ion))");
        if (primaryLanguage != null)  resultSet.add(primaryLanguage);
        if (primaryLanguage != null && Utils.setItemContains(otherLanguages, primaryLanguage))
        {
            otherLanguages.remove(primaryLanguage);
        }
        if (isBook && isTranslated && otherLanguages.size() == 1 && translatedFrom.size() == 0)
        {
            copySetWithSuffix(resultSet, otherLanguages, " (translated from)");
        }
        else 
        {
            if (isDVD)
                copySetWithSuffix(resultSet, otherLanguages, " (dubbed in)");
            else
                copySetWithSuffix(resultSet, otherLanguages, " (also in)");
            
            if (primaryLanguage != null && Utils.setItemContains(translatedFrom, primaryLanguage))
            {
                translatedFrom.remove(primaryLanguage);
            }
            copySetWithSuffix(resultSet, translatedFrom, " (translated from)");
        }
        copySetWithSuffix(resultSet, subtitleLanguage, (isBook ? " (summary in)" : " (subtitles in)") );
        return(resultSet);
    }
    
    private void copySetWithSuffix(Set<String> resultSet, Set<String> languageList, String suffix)
    {
        for (String language : languageList)
        {
            String toAdd = language + suffix;
            resultSet.add(toAdd);
        }  
    }

    public String getShadowedLocation(final Record record, String propertiesMap, String returnHidden, String processExtra)
    {
        if (record.getControlNumber()== null || record.getControlNumber().length() < 2) return(null);
        boolean processExtraShadowedIds = processExtra.startsWith("extraIds");
        if (processExtraShadowedIds)
        {
            loadExtraShadowedIds(extraIdsFilename, boundWithsFilename);
        }
        boolean returnHiddenRecs = returnHidden.startsWith("return");
        String mapName = loadTranslationMap(null, propertiesMap);
        
        Set<String> fields = getFieldList(record, "999ikl';'");
        boolean visible = false;
        String extraString = null;
        if (processExtraShadowedIds && boundWithIds != null && boundWithIds.containsKey(record.getControlNumber().substring(1)))
        {
            String boundWithHolding = boundWithIds.get(record.getControlNumber().substring(1));
            String fparts[] = boundWithHolding.split("\\|");
            String mappedFpartCurrent = Utils.remap(fparts[2], findMap(mapName), true);
            String mappedFpartHome = Utils.remap(fparts[3], findMap(mapName), true);
            if (mappedFpartCurrent.equals("VISIBLE") && mappedFpartHome.equals("VISIBLE"))
            {
                visible = true;
            }
        }
        else 
        {
            if (processExtraShadowedIds && addnlShadowedIds.containsKey(record.getControlNumber()))
            {
                extraString = addnlShadowedIds.get(record.getControlNumber());
            }  
            if ("".equals(extraString))  visible = false;
            else
            {
                for (String field : fields)
                {
                    String fparts[] = field.split(";");
                    if (extraString != null && extraString.contains("|" + fparts[0] + "|"))
                    {
                        // this holding is marked as Hidden via the addnlShadowedIds data file
                        // so simply continue, and unless another non-Hidden holding is found the 
                        // record will be not visible.
                        continue;
                    }
                    else if (fparts.length == 2)
                    {
                        String mappedFpart = Utils.remap(fparts[1], findMap(mapName), true);
                        if (mappedFpart.equals("VISIBLE"))  visible = true;
                    }
                    else if (fparts.length == 3)
                    {
                        String mappedFpart1 = Utils.remap(fparts[1], findMap(mapName), true);
                        String mappedFpart2 = Utils.remap(fparts[2], findMap(mapName), true);
                        if (mappedFpart1.equals("VISIBLE") && mappedFpart2.equals("VISIBLE"))
                        {
                            visible = true;
                        }
                    }
                }
            }
        }
        String result = (visible ? "VISIBLE" : "HIDDEN"); 
        if (!visible && !returnHiddenRecs)
        {
            return(null);
        }
        return(result);
    }
    
    public String getPublicationDate(final Record record)
    {
        if (publicationDate != null) return(publicationDate);
        
        String field008 = getFirstFieldVal(record, "008");
        String pubDateFull = getFieldVals(record, "260c", ", ");
        String pubDateJustDigits = pubDateFull.replaceAll("[^0-9]", "");       
        String pubDate260c = getDate(record);
        if (field008 == null || field008.length() < 16) 
        {
            return(pubDate260c);
        }
        String field008_d1 = field008.substring(7, 11);
        String field008_d2 = field008.substring(11, 15);
        String retVal = null;
        char dateType = field008.charAt(6);
        if (dateType == 'r' && field008_d2.equals(pubDate260c)) retVal = field008_d2;
        else if (field008_d1.equals(pubDate260c))               retVal = field008_d1;
        else if (field008_d2.equals(pubDate260c))               retVal = field008_d2;
        else if (pubDateJustDigits.length() == 4 && pubDate260c != null &&
                 pubDate260c.matches("(20|19|18|17|16|15)[0-9][0-9]"))
                                                                retVal = pubDate260c;
        else if (field008_d1.matches("(20|1[98765432])[0-9][0-9]"))        
                                                                retVal = field008_d1;
        else if (field008_d2.matches("(20|1[98765432])[0-9][0-9]"))        
                                                                retVal = field008_d2;
        else                                                    retVal = pubDate260c;
        publicationDate = retVal;
        return(retVal);
    }
    
    /**
     * returns the publication date groupings from a record, if it is present
     * @param record
     * @return Set of Strings containing the publication date groupings associated
     *   with the publish date
     */
    public Set<String> getPubDateGroups(final Record record, String Mapfilename)
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        int cYearInt = Calendar.getInstance().get(Calendar.YEAR); 
        String mapName = loadTranslationMap(null, Mapfilename);
        
        // get the pub date, with decimals assigned for inclusion in ranges
        String publicationDate =  getPublicationDate(record);
        if (publicationDate != null)
        {
            int year = Integer.parseInt(publicationDate);
            // "this year" and "last three years" are for 4 digits only
            if ( year >= (cYearInt - 1))   resultSet.add("thisyear");
            if ( year >= (cYearInt - 2))   resultSet.add("lasttwoyears");
            if ( year >= (cYearInt - 3))   resultSet.add("lastthreeyears");
            if ( year >= (cYearInt - 5))   resultSet.add("lastfiveyears");
            if ( year >= (cYearInt - 10))  resultSet.add("lasttenyears");
            if ( year >= (cYearInt - 20))  resultSet.add("lasttwentyyears");
            if ( year >= (cYearInt - 50))  resultSet.add("last50years");
            if (year < (cYearInt - 50) && (year > -1.0))
                resultSet.add("morethan50years");
        }
        resultSet = Utils.remap(resultSet, findMap(mapName), true);
        return resultSet;   
    }
    
    
    public Set<String> getMusicCompositionForm(Record record)
    {
        if (!isMusicalFormat(record)) return(null);
        String typeMapName = loadTranslationMap(null, "music_maps.properties(composition_type)");
        Set<String> result = getFieldList(record, "008[18-19]:047a");
        result = Utils.remap(result, findMap(typeMapName), false);
        return(result);
    }
    
    /**
     * get the era field values from 045a as a Set of Strings
     */
    public Set<String> getMusicEra(Record record)
    {
        if (!isMusicalFormat(record)) return(null);
        
        Set<String> result = super.getEra(record);
        String locMapName = loadTranslationMap(null, "composition_era_map.properties");
        Map<String, String> compositionMap = findMap(locMapName);
        result = Utils.remap(result, compositionMap, false);
        
        DataField field045 = (DataField)record.getVariableField("045");
        if (field045 == null) return(result);
        char indicator1 = field045.getIndicator1();
        
        List<?> subfields = ((DataField)field045).getSubfields('b');
        if (indicator1 == '0' || indicator1 == '1') 
        {
            for (Subfield sf : (List<Subfield>)subfields )
            {
                String value = sf.getData();
                if (value.startsWith("c")) result.add("B.C.");
                else if(value.startsWith("d"))
                {
                    String valueDate = getDateFrom045b(value);
                    addDate(result, valueDate);
                }
            }
        }
        else if (indicator1 == '2' && subfields.size() == 2) // range of dates
        {
            Iterator<Subfield> iter = ((List<Subfield>)subfields).iterator();
            Subfield sf1 = iter.next();
            Subfield sf2 = iter.next();
            if (sf1.getData().startsWith("c") && sf2.getData().startsWith("c")) 
            {
                result.add("B.C.");
            }
            else if (sf2.getData().startsWith("d"))
            {
                String date1;
                if (sf1.getData().startsWith("c"))
                {
                    result.add("B.C.");
                    date1 = "0000";
                }
                else
                {
                    date1 = getDateFrom045b(sf1.getData());
                }
                String date2 = getDateFrom045b(sf2.getData());
                if (date1 != null && date2 != null)
                {
                    int date1val = Integer.parseInt(date1);
                    int date2val = Integer.parseInt(date2);
                    for (int i = date1val; i <= date2val; i += 10)
                    {
                        addDate(result, (""+(i+10000)).substring(1,5));
                    }
                }
                else if (date1 != null)
                {
                    int date1val = Integer.parseInt(date1);
                    addDate(result, (""+(date1val+10000)).substring(1,5));
                }
                else if (date2 != null)
                {
                    int date2val = Integer.parseInt(date1);
                    addDate(result, (""+(date2val+10000)).substring(1,5));
                }
            }
        }
        return(result);
    }

    private String getDateFrom045b(String dateStr)
    {
        String result = dateStr.replaceFirst("[ ]*d[ ]*", "");
        if (!result.matches("([01][0-9]|[2][0])[0-9][0-9].*")) return(null);
        result = result.substring(0, 3) + "0";
        return (result);
    }

    private boolean isMusicalFormat(Record record)
    {
        Leader leader = record.getLeader();
        char type = leader.getTypeOfRecord();
        if (type == 'c' || type == 'd' || type == 'j') return(true);
        return(false);
    }

    private void addDate(Set<String> result, String valueDate)
    {
        if (valueDate == null) return;
        if (valueDate.compareTo("1700") >= 0)
        {
            result.add(valueDate+"'s");
        }
        int century = Integer.parseInt(valueDate.substring(0, 2)) +1;
        String suffix = century == 1 || century == 21 ? "st" : century == 2 ? "nd" : century == 3 ? "rd" : "th";
        result.add("" + century + suffix + " Century"); 
    }
    
    // process summary holdings info
    
    
    public Set<String> getSummaryHoldingsInfo(Record record, String libraryMapName, String locationMapName)
    {
        Set<String> result = new LinkedHashSet<String>();
        Set<String> ivyresult = new LinkedHashSet<String>();
        String fieldsToUseStr = "852|853|863|866|867";
        String fieldsToUse[] = fieldsToUseStr.split("[|]");
        String libMapName = loadTranslationMap(null, libraryMapName);
        String locMapName = loadTranslationMap(null, locationMapName);
        List<VariableField> fields = record.getVariableFields();
        DataField libraryField = null;
        for (int i = 0; i < fields.size(); i++)
        {
            String holdingsField;
            VariableField vf = fields.get(i);
            if (!(vf instanceof DataField))  continue;
            DataField df = (DataField)vf;
            if (df.getTag().equals("852"))  
            {
                libraryField = df;
                if (getSubfieldVal(libraryField, 'z', null) != null)
                {
                    holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, "", getSubfieldVal(libraryField, 'z', ""), "");
                    addHoldingsField(result, ivyresult, holdingsField);
                }
            }
            else if (df.getTag().equals("853"))  continue; // ignore 853's here.
            else if (df.getTag().equals("866"))  
            {
                holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, getSubfieldVal(df, 'a', ""), getSubfieldVal(df, 'z', ""), "Library has");
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("867"))
            {
                holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, getSubfieldVal(df, "z+a", ""), getSubfieldVal(df, "-z", ""), "Suppl text holdings");
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("868"))
            {
                holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, getSubfieldVal(df, 'a', ""), getSubfieldVal(df, 'z', ""), "Index text holdings");
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("863"))
            {
                // look ahead for other 863's to combine                
                String linktag = df.getSubfield('8') != null ? df.getSubfield('8').getData() : null;
                int j = i+1;
                for (; j < fields.size(); j++)
                {
                    VariableField nvf = fields.get(j);
                    if (!(nvf instanceof DataField))  break;
                    DataField ndf = (DataField)nvf;
                    String nlinktag = ndf.getSubfield('8') != null ? ndf.getSubfield('8').getData() : null;
                    if (linktag == null || nlinktag == null || !getLinkPrefix(linktag).equals(getLinkPrefix(nlinktag))) 
                        break;                   
                }
                DataField labelField = null;
                if (linktag != null) labelField = getLabelField(record, getLinkPrefix(linktag));
                if (labelField != null && j == i + 1) 
                {
                    holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, processEncodedField(df, labelField), getSubfieldVal(df, 'z', ""), "Library has");
                    addHoldingsField(result, ivyresult, holdingsField);
                }
                else if (labelField != null && j > i + 1) 
                {
                    VariableField nvf = fields.get(j-1);
                    DataField ndf = (DataField)nvf;
                    holdingsField = buildHoldingsField(libraryField, libMapName, locMapName, processEncodedFieldRange(df, ndf, labelField), getSubfieldVal(df, 'z', ""), "Library has");
                    addHoldingsField(result, ivyresult, holdingsField);
                    i = j - 1;
                }
            }
        }
        if (ivyresult.size() != 0)
        {
            for (String ivy : ivyresult)
            {
                result.add(ivy);
            }
        }
        return(result);
    }

    private void addHoldingsField(Set<String> result, Set<String> ivyresult, String holdingsField)
    {
        if (holdingsField != null)
        {
            if (holdingsField.startsWith("Ivy"))
                ivyresult.add(holdingsField);
            else
                result.add(holdingsField);
        }
    }

    private String getSubfieldVal(DataField df, String subfieldTags, String defValue)
    {
        List<Subfield> subfields = (List<Subfield>)df.getSubfields();
        if (subfields.size() == 0)  return(defValue);
        String result = "";
        boolean found_a = false;
        boolean getBefore_a = subfieldTags.contains("+");
        for (Subfield sf : subfields)
        {
            if (sf.getCode() == 'a')
            {
                if (subfieldTags.contains(""+sf.getCode()))
                {
                    result = result + ((result.length() > 0) ? " " : "") + sf.getData();
                }
                found_a = true;
            }
            else if (getBefore_a && !found_a && sf.getCode() != 'a' && subfieldTags.contains(""+sf.getCode()) ) 
            {
                result = result + ((result.length() > 0) ? " " : "") + sf.getData();
            }
            else if (!getBefore_a && found_a && sf.getCode() != 'a' && subfieldTags.contains(""+sf.getCode()) )
            {
                result = result + ((result.length() > 0) ? " " : "") + sf.getData();
            }
        }
        return result;
    }
    
    private String getSubfieldVal(DataField df, char subfieldTag, String defValue)
    {
        List<Subfield> subfields = (List<Subfield>)df.getSubfields(subfieldTag);
        if (subfields.size() == 0)  return(defValue);
        String result = "";
        for (Subfield sf : subfields)
        {
            result = result + sf.getData();
        }
        return result;
    }

    private String buildHoldingsField(DataField libraryField, String libMapName, String locMapName, String holdingsValue, String publicNote, String holdingsType)
    {
        if (libraryField == null || ((holdingsValue == null || holdingsValue.length() == 0) && (publicNote.length() == 0 ))) return(null);
        String libraryName = libraryField.getSubfield('b') != null ? Utils.remap(libraryField.getSubfield('b').getData(), findMap(libMapName), false) : null;
        String locName = libraryField.getSubfield('c') != null ? Utils.remap(libraryField.getSubfield('c').getData(), findMap(locMapName), false) : null;
        if (libraryName == null) libraryName = "";
        if (locName == null) locName = "";
        return(libraryName +"|"+ locName +"|"+ holdingsValue+"|"+publicNote+"|"+holdingsType);
    }

    private String processEncodedField(DataField df, DataField labelField)
    {
        boolean normalize_date = false;
        if (labelField == null) return(null);
        StringBuffer result = new StringBuffer();
        for (char subfield = 'a'; subfield <= 'f'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data = getSubfieldVal(df, subfield, null);
            if (label == null || data == null) break;
            if (subfield != 'a')  result.append(", ");
            if (label.startsWith("(") && label.endsWith(")")) label = "";
            result.append(label);
            result.append(data);
        }
        StringBuffer alt = new StringBuffer();
        for (char subfield = 'g'; subfield <= 'h'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data = getSubfieldVal(df, subfield, null);
            if (label == null || data == null) break;
            if (subfield != 'g')  alt.append(", ");
            alt.append(label);
            alt.append(data);
        }
        if (alt.length() != 0)
        {
            result.append(" ("+alt+")");
        }
        String year = null;
        StringBuffer date = new StringBuffer();
        if (normalize_date)
        {
            for (char subfield = 'i'; subfield <= 'm'; subfield++)
            {
                boolean appendComma = false;
                String label = getSubfieldVal(labelField, subfield, null);
                String data = getSubfieldVal(df, subfield, null);
                if (label == null || data == null) break;
            //    if (subfield != 'i')  result.append(", ");
                if (label.equalsIgnoreCase("(month)") || label.equalsIgnoreCase("(season)"))
                {
                    data = expandMonthOrSeason(data);
                }
                else if (year != null && !label.equalsIgnoreCase("(day)"))
                {
                    date.append(year);
                    year = null;
                }
                else
                {
                    appendComma = true;
                }
                if (label.equalsIgnoreCase("(year)"))
                {
                    year = data;
                }
                else if (label.equalsIgnoreCase("(day)"))
                {
                    date.append(" ").append(data);
                    if (appendComma) date.append(", ");
                }
                else
                {
                    date.append(data);
                    if (appendComma) date.append(", ");
                }
            }
            if (year != null) date.append(year);
        }
        else
        {
            boolean prependStr = false;
            String strToPrepend = "";
            for (char subfield = 'i'; subfield <= 'm'; subfield++)
            {
                String label = getSubfieldVal(labelField, subfield, null);
                String data = getSubfieldVal(df, subfield, null);
                if (label == null || data == null) break;
                if (label.equalsIgnoreCase("(month)") || label.equalsIgnoreCase("(season)"))
                {
                    data = expandMonthOrSeason(data);
                    strToPrepend = ":";
                }
                else if (label.equalsIgnoreCase("(day)"))
                {
                    data = expandMonthOrSeason(data);
                    strToPrepend = " ";
                }
                if (prependStr)
                {
                    date.append(strToPrepend).append(data);
                }
                else
                {
                    date.append(data);
                }
                prependStr = true;
            }
        }
        if (date.length() > 0)
        {
            if (result.length() > 0)  result.append(" (").append(date).append(")");
            else result.append(date);
        }    
        return result.toString();
    }
    
    private String processEncodedFieldRange(DataField df1, DataField df2, DataField labelField)
    {
        boolean normalize_date = false;
        if (labelField == null) return(null);
        StringBuffer result = new StringBuffer();
        StringBuffer vol1 = new StringBuffer();
        StringBuffer vol2 = new StringBuffer();
        for (char subfield = 'a'; subfield <= 'f'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data1 = getSubfieldVal(df1, subfield, null);
            String data2 = getSubfieldVal(df2, subfield, null);
            if (label == null || data1 == null || data2 == null) break;
            if (subfield != 'a')  
            {
                vol1.append(", ");
                vol2.append(", ");
            }
            if (label.startsWith("(") && label.endsWith(")")) label = "";
            vol1.append(label);
            vol1.append(data1);
            vol2.append(label);
            vol2.append(data2);
        }
        result.append(rangify(vol1.toString(), vol2.toString()));
        StringBuffer alt = new StringBuffer();
        for (char subfield = 'g'; subfield <= 'h'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data1 = getSubfieldVal(df1, subfield, null);
            String data2 = getSubfieldVal(df2, subfield, null);
            if (label == null || data1 == null || data2 == null) break;
            if (subfield != 'g')  alt.append(", ");
            alt.append(label);
            alt.append(rangify(data1, data2));
        }
        if (alt.length() != 0)
        {
            result.append(" ("+alt+")");
        }
        StringBuffer date1 = new StringBuffer();
        StringBuffer date2 = new StringBuffer();
        {
            boolean prependStr = false;
            String strToPrepend = "";
            for (char subfield = 'i'; subfield <= 'm'; subfield++)
            {
                String label = getSubfieldVal(labelField, subfield, null);
                String data1 = getSubfieldVal(df1, subfield, null);
                String data2 = getSubfieldVal(df2, subfield, null);
                if (label == null || data1 == null || data2 == null) break;
                if (label.equalsIgnoreCase("(month)") || label.equalsIgnoreCase("(season)"))
                {
                    data1 = expandMonthOrSeason(data1);
                    data2 = expandMonthOrSeason(data2);
                    strToPrepend = ":";
                }
                else if (label.equalsIgnoreCase("(day)"))
                {
                    strToPrepend = " ";
                }
                if (prependStr)
                {
                    date1.append(strToPrepend).append(data1);
                    date2.append(strToPrepend).append(data2);
                }
                else
                {
                    date1.append(data1);
                    date2.append(data2);
                }
                prependStr = true;
            }
        }
        if (date1.length() > 0 && date2.length() > 0)
        {
            if (result.length() > 0)  result.append(" (").append(rangify(date1.toString(), date2.toString())).append(")");
            else result.append(rangify(date1.toString(), date2.toString()));
        }    
        return result.toString();
    }

    private Object rangify(String data1, String data2)
    {
        int i;
        if (data1.equals(data2)) return(data1);
        for (i = 0; i < data1.length() && i < data2.length(); i++)
        {
            if (data1.charAt(i) != data2.charAt(i)) break;
        }
        int preBackstep = i;
        if ( i < data1.length() && i < data2.length() && Character.isDigit(data1.charAt(i)) && Character.isDigit(data2.charAt(i)))
        {
            while (Character.isDigit(data1.charAt(i)) && Character.isDigit(data2.charAt(i)) &&
                i > 0 && Character.isDigit(data1.charAt(i-1)) && Character.isDigit(data2.charAt(i-1)))
            {
                i--;
            }
        }
        else if ( i < data1.length() && i < data2.length() && Character.isLetter(data1.charAt(i)) && Character.isLetter(data2.charAt(i)))
        {
            while (Character.isLetter(data1.charAt(i)) && Character.isLetter(data2.charAt(i)) &&
                i > 0 && Character.isLetter(data1.charAt(i-1)) && Character.isLetter(data2.charAt(i-1)))
            {
                i--;
            }
        }
        String result;
        if (i <= 3 && data1.length() > 6  && data2.length() > 6 && preBackstep < 6)
            result = data1 + "-" + data2;
        else if ( i < data1.length() && i < data2.length())
            result = data1.substring(0, i) + data1.substring(i) + "-" + data2.substring(i);
        else 
            result = data1;
        return result;
    }

    private String expandMonthOrSeason(String data)
    {
        data = data.replaceAll("01", "Jan");
        data = data.replaceAll("02", "Feb");
        data = data.replaceAll("03", "Mar");
        data = data.replaceAll("04", "Apr");
        data = data.replaceAll("05", "May");
        data = data.replaceAll("06", "Jun");
        data = data.replaceAll("07", "Jul");
        data = data.replaceAll("08", "Aug");
        data = data.replaceAll("09", "Sept");
        data = data.replaceAll("10", "Oct");
        data = data.replaceAll("11", "Nov");
        data = data.replaceAll("12", "Dec");
        data = data.replaceAll("21", "Spring");
        data = data.replaceAll("22", "Summer");
        data = data.replaceAll("23", "Autumn");
        data = data.replaceAll("24", "Winter");
        return(data);

    }

    private DataField getLabelField(Record record, String linkPrefix)
    {
        if (linkPrefix == null) return(null);
        List<VariableField> fields = (List<VariableField>)record.getVariableFields("853");
        for (VariableField vf : fields)
        {
            if (!(vf instanceof DataField))  continue;
            DataField df = (DataField)vf;
            String link = df.getSubfield('8') != null ? df.getSubfield('8').getData() : null;
            if (link != null && link.equals(linkPrefix))
            {
                return(df);
            }
        }
        return(null);
    }

    private String getLinkPrefix(String linktag)
    {
        String prefix = null;
        int index;
        if ((index = linktag.indexOf('.')) == -1) 
            prefix = linktag;
        else 
            prefix = linktag.substring(0, index);
        return(prefix);
    }
        
    public Set<String> getJournalTitleText(Record record, String fieldSpec)
    {
        Set<String> result = new LinkedHashSet<String>();
        Set<String> format = getCombinedFormatNew2(record);
        if (Utils.setItemContains(format, "Journal/Magazine"))
        {
            result.addAll(SolrIndexer.getFieldList(record, fieldSpec));
        }
        return(result);
    }
    
    public Set<String> getJournalTitleFacet(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        Set<String> format = getCombinedFormatNew2(record);
        if (Utils.setItemContains(format, "Journal/Magazine"))
        {
            result.add(getSortableTitle(record));
        }
        return(result);
    }
    
    public String getDateFirstAdded(Record record, String dateFirstAddedFilename)
    {
        if (dateFirstAddedMap == null)
        {
            dateFirstAddedMap = new LinkedHashMap<String,String>();
            try
            {
                InputStream dateFirstAddedStream = Utils.getPropertyFileInputStream(propertyFilePaths, dateFirstAddedFilename);
                BufferedReader reader = new BufferedReader(new InputStreamReader(dateFirstAddedStream));
                String line;
                while ((line = reader.readLine())!= null)
                {
                    String parts[] = line.split("[\t ]");
                    dateFirstAddedMap.put(parts[0], parts[1]);
                }
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String id = record.getControlNumber();
        String result = dateFirstAddedMap.get(id);
        return(result);
    }   

    /**
     * treats indicator 2 as the number of non-filing indicators to exclude,
     * removes ascii punctuation
     * @param DataField with ind2 containing # non-filing chars, or has value ' '
     * @param skipSubFldc true if subfield c contents should be skipped
     * @return StringBuffer of the contents of the subfields - with a trailing
     *         space
     */
    @SuppressWarnings("unchecked")
    protected StringBuffer getAlphaSubfldsAsSortStr(DataField df, String subfieldstoSkip)
    {
        StringBuffer result = new StringBuffer();
        int nonFilingInt = getInd2AsInt(df);
        boolean firstSubfld = true;

        List<Subfield> subList = df.getSubfields();
        for (Subfield sub : subList)
        {
            char subcode = sub.getCode();
            if (Character.isLetter(subcode) && (subfieldstoSkip == null  || subfieldstoSkip.indexOf(subcode)== -1))
            {
                String data = sub.getData();
                if (firstSubfld)
                {
                    if (nonFilingInt < data.length() - 1)
                        data = data.substring(nonFilingInt);
                    firstSubfld = false;
                }
                // eliminate ascii punctuation marks from sorting as well
                result.append(data.replaceAll("( |-)+", " ").replaceAll("\\p{Punct}", "").trim() + ' ');
            }
        }
        return result;
    }

    /**
     * returns string for author sort:  a string containing
     *  1. the main entry author, if there is one 
     *  2. the main entry uniform title (240), if there is one - not including 
     *    non-filing chars as noted in 2nd indicator
     * followed by
     *  3.  the 245 title, not including non-filing chars as noted in ind 2
     */
    @SuppressWarnings("unchecked")
    public String getSortableAuthor(final Record record)
    {
        StringBuffer resultBuf = new StringBuffer();

        DataField df = (DataField) record.getVariableField("100");
        // main entry personal name
        if (df != null)
            resultBuf.append(getAlphaSubfldsAsSortStr(df, null));

        df = (DataField) record.getVariableField("110");
        // main entry corporate name
        if (df != null)
            resultBuf.append(getAlphaSubfldsAsSortStr(df, null));

        df = (DataField) record.getVariableField("111");
        // main entry meeting name
        if (df != null)
            resultBuf.append(getAlphaSubfldsAsSortStr(df, null));

        // need to sort fields missing 100/110/111 last
        if (resultBuf.length() == 0)
        {
            return(null);
        }
        // Solr field properties should convert to lowercase, but I'll do it here anyway.
        String result = resultBuf.toString().trim().toLowerCase().replaceAll("[^0-9a-z ]+", "");
        return(result);
    }
    
    public String getSortableTitleCleaned(final Record record)
    {
        StringBuilder resultBuf = new StringBuilder();

        // uniform title
        DataField df = (DataField) record.getVariableField("130");
        if (df != null)
            resultBuf.append(getAlphaSubfldsAsSortStr(df, null));

        // 245 (required) title statement
        df = (DataField) record.getVariableField("245");
        if (df != null)
            resultBuf.append(getAlphaSubfldsAsSortStr(df, "ch"));

        String result = resultBuf.toString().trim().toLowerCase().replaceAll("[^0-9a-z ]", "");
        return(result);
    }

    
}
