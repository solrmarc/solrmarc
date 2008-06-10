
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.marc4j.marc.Record;

public class BlacklightIndexer extends SolrIndexer
{

    public BlacklightIndexer(String propertiesMapFile) throws Exception
    {
        super(propertiesMapFile);
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


    public Set<String> getRecordingAndScore(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        String leader = record.getLeader().toString();
        String leaderChar = leader.substring(6, 7).toUpperCase();
                
        if(leaderChar.equals("C") || leaderChar.equals("D"))
        {
            result.add("Scores");
            result.add("Recordings and/or Scores");
        }
        
        if(leaderChar.equals("J"))
        {
            result.add("Recordings");
            result.add("Recordings and/or Scores");
        }
        
        return result;
    }
 
    
    public Set<String> getRecordingFormat(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        String leader = record.getLeader().toString();
        String leaderChar = leader.substring(6, 7).toUpperCase();
        Set<String> titleH = new LinkedHashSet<String>();
        addSubfieldDataToSet(record, titleH, "245", "h");       
                
        if(leaderChar.equals("J") || leaderChar.equals("I") || 
                (Utils.setItemContains(titleH, "videorecording")))
        {
            Set<String> form = new LinkedHashSet<String>();
            addSubfieldDataToSet(record, form, "999", "t");
//            Set<String> labels = Utils.remap(form, findMap("recording_format_facet"));
            return(form);
        }
        return(result);
    }

    public String getCallNumberPrefix(Record record)
    {
        String val = getFirstFieldVal(record, "999a:090a:050a");
        if (val == null || val.length() == 0) return(null);
        String vals[] = val.split("[^A-Za-z]+", 2);
        if (vals.length == 0 || vals[0] == null || vals[0].length() == 0) return(null);
        return(vals[0]);
    }

    public String getCallNumberCleaned(Record record)
    {
        String val = getFirstFieldVal(record, "999a:090a:050a");
        if (val == null || val.length() == 0) return(null);
        val = val.trim().replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".").toLowerCase();
        return(val);
    }
    
    public String getOclcNum(Record record)
    {
        Set<String> set = getFieldList(record, "035a");
        if (set.isEmpty())  return(null);
        Iterator iter = set.iterator();
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

}
