package org.blacklight;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;
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
    String extraIdsFilename = "extra_data/AllShadowedIds.txt";
    
    public BlacklightIndexer(final String propertiesMapFile, final String propertyPaths[])
    {
        super(propertiesMapFile, propertyPaths);
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
     * Extract call number prefix from a record
     * @param record
     * @return Call number prefix
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
     * Extract a cleaned call number from a record
    * @param record
    * @return Clean call number
    */
   public String getCallNumberCleaned(final Record record)
   {
       String val = getFirstFieldVal(record, "999a");
       if (val == null || val.length() == 0) {
           return(null);
       }
       val = val.trim().replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".");
       return(val);
   }
   
    /**
     * Extract a cleaned call number from a record
     * @param record
     * @return Clean call number
     */
    public Set<String> getCallNumbersCleaned(final Record record, String fieldSpec, String conflatePrefixes)
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
        boolean processExtraShadowedIds = fieldSpec.contains("';'");

        boolean conflate = !conflatePrefixes.equalsIgnoreCase("false");
        //int conflateThreshhold = conflate ? Integer.parseInt(conflatePrefixes) : 0;
        Set<String> fieldList = getFieldList(record, fieldSpec);
        if (fieldList.isEmpty())  {
            return(null);
        }
        if (processExtraShadowedIds)
        {
            loadExtraShadowedIds(extraIdsFilename);
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
        if (conflate)
        {
            Map<String, Set<String>> resultNormed = new TreeMap<String, Set<String>>();
            for (String callNum : fieldList)
            {
                String val = callNum.trim().replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".");
                String nVal = val.replaceAll("^([A-Z][A-Z]?[A-Z]?) ([0-9])", "$1$2");
                if (!nVal.equals(val))
                {
                    val = nVal;
                }
                String key = val.substring(0, Math.min(val.length(), 5)).toUpperCase();
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
            return(results);
        }
        else 
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
                if (broadFormat != null) result.add(broadFormat);
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
    private void loadExtraShadowedIds(String filename)
    {
        if (addnlShadowedIds == null)
        {
            addnlShadowedIds = new LinkedHashMap<String, String>();
            InputStream addnlIdsStream = Utils.getPropertyFileInputStream(null, filename);
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
    }
    
    public String getShadowedLocation(final Record record, String propertiesMap, String returnHidden, String processExtra)
    {
        boolean processExtraShadowedIds = processExtra.startsWith("extraIds");
        if (processExtraShadowedIds)
        {
            loadExtraShadowedIds(extraIdsFilename);
        }
        boolean returnHiddenRecs = returnHidden.startsWith("return");
        String mapName = loadTranslationMap(null, propertiesMap);
        
        Set<String> fields = getFieldList(record, "999ikl';'");
        boolean visible = false;
        String extraString = null;
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
        String result = (visible ? "VISIBLE" : "HIDDEN"); 
        if (!visible && !returnHiddenRecs)
        {
            return(null);
        }
        return(result);
    }
    
    public String getPublicationDate(final Record record)
    {
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

}
