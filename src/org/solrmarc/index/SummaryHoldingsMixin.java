package org.solrmarc.index;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.indexer.ValueIndexerFactory;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;

public class SummaryHoldingsMixin extends SolrIndexerMixin
{
    // process summary holdings info
    
    
    public Set<String> getSummaryHoldingsInfo(Record record, String libraryMapName, String locationMapName) throws Exception
    {
        Set<String> result = new LinkedHashSet<String>();
        Set<String> ivyresult = new LinkedHashSet<String>();
        String fieldsToUseStr = "852|853|863|866|867";
        String fieldsToUse[] = fieldsToUseStr.split("[|]");
        AbstractMultiValueMapping libraryMap = ValueIndexerFactory.instance().createMultiValueMapping(libraryMapName);
        AbstractMultiValueMapping locationMap = ValueIndexerFactory.instance().createMultiValueMapping(locationMapName);
        
        List<VariableField> fields = record.getVariableFields();
        DataField libraryField = null;
        for (int i = 0; i < fields.size(); i++)
        {
            String holdingsField;
            VariableField vf = fields.get(i);
            if (!(vf instanceof DataField))  continue;
            DataField df = (DataField)vf;
            if (!df.getTag().startsWith("8")) continue; 
            if (df.getTag().equals("852"))  
            {
                libraryField = df;
                if (getSubfieldVal(libraryField, "hiz", null) != null)
                {
                    holdingsField = buildHoldingsField(libraryField, libraryMap, locationMap, "", getSubfieldVal(libraryField, 'z', ""), "", getSubfieldVal(libraryField, "hi", ""));
                    addHoldingsField(result, ivyresult, holdingsField);
                }
            }
            else if (df.getTag().equals("853"))  continue; // ignore 853's here.
            else if (df.getTag().equals("866"))  
            {
                holdingsField = buildHoldingsField(libraryField, libraryMap, locationMap, getSubfieldVal(df, 'a', ""), getSubfieldVal(df, 'z', ""), "Library has", null);
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("867"))
            {
                holdingsField = buildHoldingsField(libraryField, libraryMap, locationMap, getSubfieldVal(df, "z+a", ""), getSubfieldVal(df, "-z", ""), "Suppl text holdings", null);
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("868"))
            {
                holdingsField = buildHoldingsField(libraryField, libraryMap, locationMap, getSubfieldVal(df, 'a', ""), getSubfieldVal(df, 'z', ""), "Index text holdings", null);
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
                    holdingsField = buildHoldingsField(libraryField, libraryMap, locationMap, processEncodedField(df, labelField), getSubfieldVal(df, 'z', ""), "Library has", null);
                    addHoldingsField(result, ivyresult, holdingsField);
                }
                else if (labelField != null && j > i + 1) 
                {
                    VariableField nvf = fields.get(j-1);
                    DataField ndf = (DataField)nvf;
                    holdingsField = buildHoldingsField(libraryField, libraryMap, locationMap, processEncodedFieldRange(df, ndf, labelField), getSubfieldVal(df, 'z', ""), "Library has", null);
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
        boolean getAfter_a = subfieldTags.contains("-");
//        boolean addlineBreak = subfieldTags.contains("/");
        for (Subfield sf : subfields)
        {
            if (!subfieldTags.contains("a") && ! getBefore_a  && ! getAfter_a && subfieldTags.contains(""+sf.getCode()))
            {
                String spacer = ((result.length() > 0) ? " " : "");
//                if (addlineBreak && sf.getCode() == 'z') spacer = "<br>";
                result = result + ((result.length() > 0) ? spacer : "") + sf.getData();
            }
            else if (sf.getCode() == 'a')
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

    private String buildHoldingsField(DataField libraryField, AbstractMultiValueMapping libraryMap, AbstractMultiValueMapping locationMap, String holdingsValue, String publicNote, String holdingsType, String callNumber) throws Exception
    {
        if (libraryField == null || ((holdingsValue == null || holdingsValue.length() == 0) && (publicNote.length() == 0 && (callNumber == null || callNumber.length() == 0)))) return(null);
        String libraryName = libraryField.getSubfield('b') != null ? libraryMap.mapSingle(libraryField.getSubfield('b').getData()) : null;
        String locName = libraryField.getSubfield('c') != null ? locationMap.mapSingle(libraryField.getSubfield('c').getData()) : null;
        if (libraryName == null) libraryName = "";
        if (locName == null) locName = "";
        if (callNumber == null) callNumber = "";
        return(libraryName +"|"+ locName +"|"+ holdingsValue+"|"+publicNote+"|"+holdingsType+"|"+callNumber);
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
            result.append(" (").append(alt).append(")");
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
            result.append(" (").append(alt).append(")");
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
        

}
