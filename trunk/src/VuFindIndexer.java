

import java.util.LinkedHashSet;
import java.util.Set;

import org.marc4j.marc.Record;

public class VuFindIndexer extends SolrIndexer
{

    public VuFindIndexer(String propertiesMapFile) throws Exception
    {
        super(propertiesMapFile);
    }
    
    public String getFormat(Record record)
    {
        String leaderChar = getFirstFieldVal(record, "000[7]").toUpperCase();
        String t007Char = getFirstFieldVal(record, "007[0]").toUpperCase();
        Set<String> titleH = new LinkedHashSet<String>();
        addSubfieldDataToSet(record, titleH, "245", "h");       
                
        // check with folks to see if leader is more likely
        if(leaderChar.equals("M"))      {   return "Book";   }        
        if(leaderChar.equals("S"))      {   return "Journal";  }        
        // check the h subfield of the 245 field
        if (Utils.setItemContains(titleH, "electronic resource"))
                                        {   return "Electronic";  }        
        // check the 007
        if(t007Char.equals("A"))        {   return "Map";  }           
        if(t007Char.equals("G"))        {   return "Slide";  }        
        if(t007Char.equals("H"))        {   return "Microfilm";  }        
        if(t007Char.equals("K"))        {   return "Photo";  }        
        if(t007Char.equals("S"))        {   return "Audio";  }        
        if(t007Char.equals("V") || 
           t007Char.equals("M"))        {   return "Video";  }                           
        return "";
    }
    
    public String getCallNumberLabel(Record record)
    {
        String val = getFirstFieldVal(record, "090a:050a");
        String vals[] = val.split("[^A-Za-z]+", 2);
        if (vals.length == 0 || vals[0] == null || vals[0].length() == 0) return(null);
        return(vals[0]);
    }

}
