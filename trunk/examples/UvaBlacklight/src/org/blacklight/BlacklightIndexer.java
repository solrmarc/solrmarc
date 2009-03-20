package org.blacklight;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.marc.MarcImporter;
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
    public BlacklightIndexer(final String propertiesMapFile, final String propertyPaths[])
        throws FileNotFoundException, IOException, ParseException
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
     * Extract a cleaned call number from a record
     * @param record
     * @return Clean call number
     */
    public String getCallNumberCleaned(final Record record)
    {
        String val = getFirstFieldVal(record, "999a:090a:050a");
        if (val == null || val.length() == 0) {
            return(null);
        }
        val = val.trim().replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".").toLowerCase();
        return(val);
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
    	String mapName1 = null;
        String mapName2 = null;
        String mapName3 = null;
    	try
        {
            mapName1 = loadTranslationMap(null, "format_maps.properties(broad_format)");
            mapName2 = loadTranslationMap(null, "format_maps.properties(format_007)");
            mapName3 = loadTranslationMap(null, "format_maps.properties(format)");
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Set<String> result = getFieldList(record, "999t");
        result = Utils.remap(result, findMap(mapName3), false);
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
        return(result);
    }
    
    public Set<String> getShadowedLocation(final Record record, String propertiesMap)
    {
        String mapName = null;
        try
        {
            mapName = loadTranslationMap(null, "location_map.properties");
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Set<String> fields = getFieldList(record, "999kl';'");
        Set<String> result = new LinkedHashSet<String>();
        for (String field : fields)
        {
            String fparts[] = field.split(";");
            if (fparts.length == 1)
            {
                String mappedFpart = Utils.remap(fparts[0], findMap(mapName), true);
                if (mappedFpart != null) result.add(mappedFpart);
            }
            else if (fparts.length == 2)
            {
                String mappedFpart1 = Utils.remap(fparts[0], findMap(mapName), true);
                String mappedFpart2 = Utils.remap(fparts[1], findMap(mapName), true);
                if (mappedFpart1 != null && mappedFpart2 != null)
                {
                    result.add(mappedFpart1);
                    result.add(mappedFpart2);
                }
            }
        }
        return(result);        
    }
}
