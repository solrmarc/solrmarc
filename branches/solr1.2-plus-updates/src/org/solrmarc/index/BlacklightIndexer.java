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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.solrmarc.tools.Utils;

/**
 * Custom functions for indexing marc files for the Blacklight project
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class BlacklightIndexer extends SolrIndexer
{

    /**
     * Default constructor
     * @param propertiesMapFile
     * @throws ParseException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws Exception
     */
    public BlacklightIndexer(final String propertiesMapFile, final String solrMarcDir)
        throws FileNotFoundException, IOException, ParseException
    {
            super(propertiesMapFile, solrMarcDir);
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
    public Set<String> getRecordingFormat(final Record record, String mapName)
    {
        try
        {
            mapName = loadTranslationMap(null, mapName);
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
        Set<String> result = new LinkedHashSet<String>();
        String leader = record.getLeader().toString();
        String leaderChar = leader.substring(6, 7).toUpperCase();
        Set<String> titleH = new LinkedHashSet<String>();
        addSubfieldDataToSet(record, titleH, "245", "h");       
                
        if("J".equals(leaderChar) || "I".equals(leaderChar) || 
                (Utils.setItemContains(titleH, "videorecording")))
        {
            Set<String> form = new LinkedHashSet<String>();
            addSubfieldDataToSet(record, form, "999", "t");
//            Set<String> labels = Utils.remap(form, findMap("recording_format_facet"));
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
        String val = getFirstFieldVal(record, "999a:090a:050a");
        String result = null;
        if (val == null || val.length() == 0) { 
            return(null);
            }
        
        String vals[] = val.split("[^A-Z]+", 2);
        
        if (vals.length == 0 || vals[0] == null || vals[0].length() == 0 ||  vals[0].length() > 3) 
        {
            return(null);
        }
        else
        {
            String prefix = vals[0];
            while (result == null && prefix.length() > 0)
            {
                result = Utils.remap(prefix, findMap(mapName), false);
                prefix = prefix.substring(0, prefix.length()-1);
            }
        }
        int partNum = Utils.isNumber(part) ? Integer.parseInt(part) : 0;
        if (result == null || partNum == 0) return(result);
        String resultParts[] = result.split("[|]");
        if (partNum-1 >= resultParts.length) return(null);
        return(resultParts[partNum-1]);
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
    
    /**
     * Extract the info from an 880 linked field from a record
     * @param record
     * @return OCLC number
     */
    public Set<String> getLinkedField(final Record record, String fieldSpec)
    {
        Set<String> set = getFieldList(record,"8806");
        
        if (set.isEmpty())  {
            return(null);
        }
        
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

}
