package org.solrmarc.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;
import org.solrmarc.callnum.CallNumUtils;
import org.solrmarc.tools.PropertyUtils;
import org.solrmarc.tools.StringNaturalCompare;
import org.solrmarc.tools.Utils;

import playground.solrmarc.index.indexer.ValueIndexerFactory;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;

public class CustomLocationMixin extends SolrIndexerMixin
{
    Map<String, String> addnlShadowedIds = null;
    Map<String, String> boundWithIds = null;
    Map<String, String> dateFirstAddedMap = null;
    Map<String, String[]> booklistData = null;
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

    /**
     * This routine can be overridden in a sub-class to perform some processing that need to be done once 
     * for each record, and which may be needed by several indexing specifications, especially custom methods.
     * The default version does nothing.
     * 
     * @param record -  The MARC record that is being indexed.
     * @throws Exception 
     */
    protected void perRecordInit(Record record) throws Exception
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
    
    private List<?> getTrimmedHoldingsList(Record record, String holdingsTag) throws Exception
    {
        List<?> result = record.getVariableFields(holdingsTag);
        loadExtraShadowedIds(extraIdsFilename, boundWithsFilename);
        addBoundWithHoldings(record, result);
        removeShadowed999sFromList(record, result);
        removeLostHoldings(result);
        //  this line (and the called method) added in response to JIRA ISSUE LIBSRVSRCHDISCOV-377
        removeOrderRecords(result);
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
        final String propertyFilePaths[] = {"extra_data"}; 
        if (addnlShadowedIds == null)
        {
            addnlShadowedIds = new LinkedHashMap<String, String>();
            InputStream addnlIdsStream = PropertyUtils.getPropertyFileInputStream(propertyFilePaths, addnlShadowedFilename);
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
                addnlIdsStream = PropertyUtils.getPropertyFileInputStream(propertyFilePaths, boundWithFilename);
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

    private void removeLostHoldings(List<?> fields999) throws Exception
    {
    //    String mapName = loadTranslationMap(null, "shadowed_location_map.properties");
        AbstractMultiValueMapping locationMap = ValueIndexerFactory.instance().createMultiValueMapping("shadowed_location_map.properties");
        Iterator<?> iter = fields999.iterator();
        while (iter.hasNext())
        {
            Object field = iter.next();
            DataField df = (DataField)field;
            Subfield currentLocation = df.getSubfield('k');
            Subfield homeLocation = df.getSubfield('l');
            if (currentLocation != null)
            {
                if (locationMap.mapSingle(currentLocation.getData()).equals("HIDDEN"))
                {
                    iter.remove();
                    continue;
                }
            }
            if (homeLocation != null)
            {
                if (locationMap.mapSingle(homeLocation.getData()).equals("HIDDEN"))
                {
                    iter.remove();
                }
            }

        }
    }
    
    //  this method added in response to JIRA ISSUE LIBSRVSRCHDISCOV-377
    private void removeOrderRecords(List<?> fields999)
    {
        Iterator<?> iter = fields999.iterator();
        while (iter.hasNext())
        {
            Object field = iter.next();
            DataField df = (DataField)field;
            Subfield callNumber = df.getSubfield('a');
            if (callNumber != null && callNumber.getData().matches(".*[Oo][Rr][Dd][Ee][Rr][- ]*0.*"))
            {
                iter.remove();
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
            Set<String> fList2 = SolrIndexer.instance().getFieldList(record, "050ab");
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
     * @throws Exception 
     */
    public String getCallNumberPrefixNew(final Record record, String mapName, String part) throws Exception
    {
        AbstractMultiValueMapping transMap = ValueIndexerFactory.instance().createMultiValueMapping(mapName);
        
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
                result = transMap.mapSingle(prefix);
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
                String shelfKey = CallNumUtils.getLCShelfkey(valParts[1], record.getControlNumber(), null);
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
    
    public Set<String>getCustomLibrary(final Record record, String visibilityMap, String libraryMap) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        List<?> fields999 = trimmedHoldingsList;
        AbstractMultiValueMapping visMap = ValueIndexerFactory.instance().createMultiValueMapping(visibilityMap);
        AbstractMultiValueMapping libMap = ValueIndexerFactory.instance().createMultiValueMapping(libraryMap);

        for ( DataField field : (List<DataField>)fields999 )
        {
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            String lib = (libF != null ? libF.getData() : null);
            String mappedLib = libMap.mapSingle(lib);
            if (mappedLib == null || resultSet.contains(mappedLib))  continue;
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            String mappedHomeVis = visMap.mapSingle(homeLoc);
            if (mappedHomeVis.equals("HIDDEN") )
            {
                continue;
            }
            if (curLoc != null)
            {
                String mappedCurVis = visMap.mapSingle(curLoc);
                if (mappedCurVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
            }
            resultSet.add(mappedLib);
        }
        return(resultSet);
    }
    
    public Set<String>getCustomLocation(final Record record, String locationMap, String visibilityMap, String libraryMap) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        List<?> fields999 = trimmedHoldingsList;
        AbstractMultiValueMapping locMap = ValueIndexerFactory.instance().createMultiValueMapping(locationMap);
        AbstractMultiValueMapping visMap = ValueIndexerFactory.instance().createMultiValueMapping(visibilityMap);
        AbstractMultiValueMapping libMap = ValueIndexerFactory.instance().createMultiValueMapping(libraryMap);
        for ( DataField field : (List<DataField>)fields999 )
        {
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            String lib = (libF != null ? libF.getData() : null);
            String mappedHomeVis = visMap.mapSingle(homeLoc);
            String mappedHomeLoc = locMap.mapSingle(homeLoc);
            if (mappedHomeVis.equals("VISIBLE") && mappedHomeLoc == null)
            {
                String combinedLocMapped = locMap.mapSingle(homeLoc + "__" + lib);
                if (combinedLocMapped != null) mappedHomeLoc = combinedLocMapped;
            }
            String mappedLib = libMap.mapSingle(lib);
            if (curLoc != null)
            {
                String mappedCurLoc = locMap.mapSingle(curLoc);
                String mappedCurVis = visMap.mapSingle(curLoc);
                if (mappedCurVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
                if (mappedCurLoc != null) 
                {
                    if (mappedCurLoc.contains("$m") && mappedLib != null)
                    {
                        mappedCurLoc = mappedCurLoc.replaceAll("[$]m", mappedLib);
                    }
                    else if (mappedCurLoc.contains("$m") && mappedLib == null)
                    {
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
    
//    public Set<String> getCustomLanguage(final Record record, String propertiesMap)
//    {
//        Set<String> resultSet = new LinkedHashSet<String>();
//        AbstractMultiValueMapping map = ValueIndexerFactory.instance().createMultiValueMapping(propertiesMap);
//
//        String primaryLanguage = SolrIndexer.instance().getFirstFieldVal(record, propertiesMap, "008[35-37]");
//        Collection<String> otherLanguages = SolrIndexer.instance().getFieldList(record, "041a:041d");
//        otherLanguages = map.map(otherLanguages);
//        Collection<String> translatedFrom = SolrIndexer.instance().getFieldList(record, "041h");
//        translatedFrom = map.map(translatedFrom);
//        Collection<String> subtitleLanguage = SolrIndexer.instance().getFieldList(record, "041b");
//        subtitleLanguage = map.map(subtitleLanguage);
//        Collection<String> format = SolrIndexer.instance().getCombinedFormat(record);
//        boolean isBook = Utils.setItemContains(format, "Book") || Utils.setItemContains(format, "Journal");
//        boolean isDVD = Utils.setItemContains(format, "DVD") ;
//        Collection<String> notesFields = SolrIndexer.instance().getFieldList(record, "500a");
//        boolean isTranslated = Utils.setItemContains(notesFields, "[Tt]ranslat((ed)|(ion))");
//        if (primaryLanguage != null)  resultSet.add(primaryLanguage);
//        if (primaryLanguage != null && Utils.setItemContains(otherLanguages, primaryLanguage))
//        {
//            otherLanguages.remove(primaryLanguage);
//        }
//        if (isBook && isTranslated && otherLanguages.size() == 1 && translatedFrom.size() == 0)
//        {
//            copySetWithSuffix(resultSet, otherLanguages, " (translated from)");
//        }
//        else 
//        {
//            if (isDVD)
//                copySetWithSuffix(resultSet, otherLanguages, " (dubbed in)");
//            else
//                copySetWithSuffix(resultSet, otherLanguages, " (also in)");
//            
//            if (primaryLanguage != null && Utils.setItemContains(translatedFrom, primaryLanguage))
//            {
//                translatedFrom.remove(primaryLanguage);
//            }
//            copySetWithSuffix(resultSet, translatedFrom, " (translated from)");
//        }
//        copySetWithSuffix(resultSet, subtitleLanguage, (isBook ? " (summary in)" : " (subtitles in)") );
//        return(resultSet);
//    }
    
//    private void copySetWithSuffix(Collection<String> resultSet, Collection<String> languageList, String suffix)
//    {
//        for (String language : languageList)
//        {
//            String toAdd = language + suffix;
//            resultSet.add(toAdd);
//        }  
//    }

    public String getShadowedLocation(final Record record, String propertiesMap, String returnHidden, String processExtra) throws Exception
    {
        if (record.getControlNumber()== null || record.getControlNumber().length() < 2) return(null);
        boolean processExtraShadowedIds = processExtra.startsWith("extraIds");
        if (processExtraShadowedIds)
        {
            loadExtraShadowedIds(extraIdsFilename, boundWithsFilename);
        }
        boolean returnHiddenRecs = returnHidden.startsWith("return");
        AbstractMultiValueMapping map = ValueIndexerFactory.instance().createMultiValueMapping(propertiesMap);
        //  String mapName = loadTranslationMap(null, propertiesMap);
        
        Set<String> fields = SolrIndexer.instance().getFieldList(record, "999aikl';'");
        boolean visible = false;
        String extraString = null;
        if (processExtraShadowedIds && boundWithIds != null && boundWithIds.containsKey(record.getControlNumber().substring(1)))
        {
            String boundWithHolding = boundWithIds.get(record.getControlNumber().substring(1));
            String fparts[] = boundWithHolding.split("\\|");
            String mappedFpartCurrent = map.mapSingle(fparts[2]);
            String mappedFpartHome = map.mapSingle(fparts[3]);
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
                    //  this test (and the change above to return aikl instead of ikl) added in response to JIRA ISSUE LIBSRVSRCHDISCOV-377
                    if (fparts[0].matches(".*[Oo][Rr][Dd][Ee][Rr][- ]*0.*"))
                    {
                        continue;
                    }
                    else if (extraString != null && extraString.contains("|" + fparts[1] + "|"))
                    {
                        // this holding is marked as Hidden via the addnlShadowedIds data file
                        // so simply continue, and unless another non-Hidden holding is found the 
                        // record will be not visible.
                        continue;
                    }
                    else if (fparts.length == 3)
                    {
                        String mappedFpart = map.mapSingle(fparts[2]);
                        if (mappedFpart.equals("VISIBLE"))  visible = true;
                    }
                    else if (fparts.length == 4 || fparts[3].equals("RSRVSHADOW"))
                    {
                        String mappedFpart1 = map.mapSingle(fparts[2]);
                        if (mappedFpart1.equals("VISIBLE"))
                        {
                            visible = true;
                        }
                    }
                    else if (fparts.length == 4)
                    {
                        String mappedFpart1 = map.mapSingle(fparts[2]);
                        String mappedFpart2 = map.mapSingle(fparts[3]);
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
    
//    public String getPublicationDate(final Record record)
//    {
//        if (publicationDate != null) return(publicationDate);
//        
//        String field008 = SolrIndexer.instance().getFirstFieldVal(record, "008");
//        String pubDateFull = SolrIndexer.instance().getFieldVals(record, "260c", ", ");
//        String pubDateJustDigits = pubDateFull.replaceAll("[^0-9]", "");       
//        String pubDate260c = SolrIndexer.instance().getDate(record);
//        if (field008 == null || field008.length() < 16) 
//        {
//            return(pubDate260c);
//        }
//        String field008_d1 = field008.substring(7, 11);
//        String field008_d2 = field008.substring(11, 15);
//        String retVal = null;
//        char dateType = field008.charAt(6);
//        if (dateType == 'r' && field008_d2.equals(pubDate260c)) retVal = field008_d2;
//        else if (field008_d1.equals(pubDate260c))               retVal = field008_d1;
//        else if (field008_d2.equals(pubDate260c))               retVal = field008_d2;
//        else if (pubDateJustDigits.length() == 4 && pubDate260c != null &&
//                 pubDate260c.matches("(20|19|18|17|16|15)[0-9][0-9]"))
//                                                                retVal = pubDate260c;
//        else if (field008_d1.matches("(20|1[98765432])[0-9][0-9]"))        
//                                                                retVal = field008_d1;
//        else if (field008_d2.matches("(20|1[98765432])[0-9][0-9]"))        
//                                                                retVal = field008_d2;
//        else                                                    retVal = pubDate260c;
//        publicationDate = retVal;
//        return(retVal);
//    }
//    
    /**
     * returns the publication date groupings from a record, if it is present
     * @param record
     * @return Set of Strings containing the publication date groupings associated
     *   with the publish date
     * @throws Exception 
     */
    public Collection<String> getPubDateGroups(final Record record, String mapfilename) throws Exception
    {
        Collection<String> resultSet = new LinkedHashSet<String>();
        int cYearInt = Calendar.getInstance().get(Calendar.YEAR); 
        AbstractMultiValueMapping map = ValueIndexerFactory.instance().createMultiValueMapping(mapfilename);
        
        // get the pub date, with decimals assigned for inclusion in ranges
        String publicationDate = SolrIndexer.instance().getPublicationDate(record);
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
        resultSet = map.map(resultSet);
        return resultSet;   
    }
    

}
