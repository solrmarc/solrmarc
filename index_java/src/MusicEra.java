package org.solrmarc.mixin;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
//import org.solrmarc.tools.Utils;

import org.solrmarc.index.SolrIndexer;
import org.solrmarc.index.extractor.impl.custom.Mixin;


public class MusicEra implements Mixin {
    
    /**
     * get the era field values from 045a as a Set of Strings
     * @throws Exception 
     */
    public Set<String> getEra(Record record) 
    {
        Set<String> result = new LinkedHashSet<String>();
        String eraField = SolrIndexer.instance().getFirstFieldVal(record, "045a");
        if (eraField == null)
            return result;

        if (eraField.length() == 4)
        {
            eraField = eraField.toLowerCase();
            char eraStart1 = eraField.charAt(0);
            char eraStart2 = eraField.charAt(1);
            char eraEnd1 = eraField.charAt(2);
            char eraEnd2 = eraField.charAt(3);
            if (eraStart2 == 'l')  eraEnd2 = '1';
            if (eraEnd2 == 'l')    eraEnd2 = '1';
            if (eraStart2 == 'o')  eraEnd2 = '0';
            if (eraEnd2 == 'o')    eraEnd2 = '0';
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
            if (eraStart1 >= 'a' && eraStart1 <= 'y' && eraStart2 >= '0' && eraStart2 <= '9')
                return getEra(result, eraStart1, eraStart2, eraStart1, eraStart2);
        }
        return result;
    }

    /**
     * get the two eras indicated by the four passed characters, and add them
     *  to the result parameter (which is a set).  The characters passed in are
     *  from the 045a.
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
        return result;
    }

   /**
     * get the era field values from 045a as a Set of Strings
     */
    public Set<String> getMusicEra(Record record) 
    {
        if (!isMusicalFormat(record)) return(null);
        
        Set<String> result = getEra(record);
//        String locMapName = loadTranslationMap(null, "composition_era_map.properties");
//        Map<String, String> compositionMap = findMap(locMapName);
//        result = Utils.remap(result, compositionMap, false);
        
        DataField field045 = (DataField)record.getVariableField("045");
        if (field045 == null) return(result);
        char indicator1 = field045.getIndicator1();
        
        List<Subfield> subfields = ((DataField)field045).getSubfields('b');
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
}