package org.blacklight;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.solrmarc.tools.StringNaturalCompare;
import org.solrmarc.tools.Utils;

public class HathiIndexer extends BlacklightIndexer
{
    static Comparator compare = new StringNaturalCompare();
    
    public HathiIndexer(final String propertiesMapFile, final String propertyPaths[])
    {
        super(propertiesMapFile, propertyPaths);
    }

    public Set<String> getHathiURL(final Record record, String defaultURL, String defaultLabel)
    {
        Set<String> result = new LinkedHashSet<String>();
//        List<?> field856s = record.getVariableFields("856");
//        if (field856s.size() > 0)
//        {
//            for (DataField df : (List<DataField>) field856s)
//            {
//                Subfield rights = df.getSubfield('r');
//                if (rights != null && (rights.getData().equals("pd") || rights.getData().equals("pdus") || rights.getData().equals("world")))
//                {
//                    Subfield labelField = df.getSubfield('z');
//                    Subfield urlField = df.getSubfield('u');
//                    if (urlField == null) continue;
//                    String label = (labelField != null) ? labelField.getData() : "";
//                    if (defaultLabel.contains("%"))
//                    {
//                        label = defaultLabel.replaceFirst("%", label);
//                        label = label.replaceAll(" [ ]+", " ");
//                    }
//                    else 
//                    {
//                        label = defaultLabel + label;
//                    }
//                    // default URL prefix is   http://hdl.handle.net/2027/
//                    String value = urlField.getData().trim() + "||" + label;
//                    result.add(value);
//                }
//            }
//        }
//        else 
        {
            Map<String, String> sortedMap = new TreeMap<String, String>(compare);
            List<?> field974s = record.getVariableFields("974");
            for (DataField df : (List<DataField>) field974s)
            {
                Subfield rights = df.getSubfield('r');
                if (rights != null && (rights.getData().equals("pd") || rights.getData().equals("pdus") || 
                    rights.getData().equals("world") || rights.getData().startsWith("cc")))
                {
                    Subfield labelField = df.getSubfield('z');
                    Subfield identField = df.getSubfield('u');
                    if (identField == null) continue;
                    String label = (labelField != null) ? labelField.getData() : "";
                    String sortlabel = (labelField != null) ? labelField.getData() : identField.getData();
                    if (defaultLabel.contains("%"))
                    {
                        label = defaultLabel.replaceFirst("%", label);
                        label = label.replaceAll(" [ ]+", " ");
                    }
                    else 
                    {
                        label = defaultLabel + label;
                    }
                    // default URL prefix is   http://hdl.handle.net/2027/
                    String value = defaultURL + identField.getData().trim() + "||" + label;
                    sortedMap.put(sortlabel, value);
                }
            }
            for (String key : sortedMap.keySet())
            {
                String value = sortedMap.get(key);
                result.add(value);
            }
        }
        return(result);
    }
    
    public Set<String> getHathiURLUse856(final Record record, String defaultURL, String defaultLabel)
    {
        Set<String> result = new LinkedHashSet<String>();
        List<?> field856s = record.getVariableFields("856");
        if (field856s.size() > 0)
        {
            for (DataField df : (List<DataField>) field856s)
            {
                Subfield rights = df.getSubfield('r');
                if (rights != null && (rights.getData().equals("pd") || rights.getData().equals("pdus") ||
                    rights.getData().equals("world") || rights.getData().startsWith("cc")))
                {
                    Subfield labelField = df.getSubfield('z');
                    Subfield urlField = df.getSubfield('u');
                    if (urlField == null) continue;
                    String label = (labelField != null) ? labelField.getData() : "";
                    if (defaultLabel.contains("%"))
                    {
                        label = defaultLabel.replaceFirst("%", label);
                        label = label.replaceAll(" [ ]+", " ");
                    }
                    else 
                    {
                        label = defaultLabel + label;
                    }
                    // default URL prefix is   http://hdl.handle.net/2027/
                    String value = urlField.getData().trim() + "||" + label;
                    result.add(value);
                }
            }
        }
        else 
        {
            Map<String, String> sortedMap = new TreeMap<String, String>(compare);
            List<?> field974s = record.getVariableFields("974");
            for (DataField df : (List<DataField>) field974s)
            {
                Subfield rights = df.getSubfield('r');
                if (rights != null && (rights.getData().equals("pd") || rights.getData().equals("pdus") || rights.getData().equals("world")))
                {
                    Subfield labelField = df.getSubfield('z');
                    Subfield identField = df.getSubfield('u');
                    if (identField == null) continue;
                    String label = (labelField != null) ? labelField.getData() : "";
                    String sortlabel = (labelField != null) ? labelField.getData() : identField.getData();
                    if (defaultLabel.contains("%"))
                    {
                        label = defaultLabel.replaceFirst("%", label);
                        label = label.replaceAll(" [ ]+", " ");
                    }
                    else 
                    {
                        label = defaultLabel + label;
                    }
                    // default URL prefix is   http://hdl.handle.net/2027/
                    String value = defaultURL + identField.getData().trim() + "||" + label;
                    sortedMap.put(sortlabel, value);
                }
            }
            for (String key : sortedMap.keySet())
            {
                String value = sortedMap.get(key);
                result.add(value);
            }
        }
        return(result);
    }
     
    public Set<String> getHathiFormat(final Record record)
    {    
        // part1_format_facet = 000[6]:007[0], format_maps.properties(broad_format), first
        // part2_format_facet = 999t, format_maps.properties(format)

        String mapName1 = loadTranslationMap(null, "format_maps.properties(broad_format)");
        String mapName1a = loadTranslationMap(null, "format_maps.properties(broad_format_electronic)");
        String mapName2 = loadTranslationMap(null, "format_maps.properties(format_007)");

        Set<String> result = new LinkedHashSet<String>();

        Set<String> f245h = getFieldList(record, "245h");
        if (Utils.setItemContains(f245h, "cartographic material"))
        {
            result.add("Map");
        }
        String broadFormat = getFirstFieldVal(record, mapName1, "000[6-7]:000[6]");
        if (broadFormat != null) result.add(broadFormat);
        result.add("Online"); // Online

        if (broadFormat.equals("Book")) 
        {
            result.add(Utils.remap("am", findMap(mapName1a), true)); // eBook
        }
        return(result);
    }

}
