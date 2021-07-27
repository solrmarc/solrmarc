package org.solrmarc.mixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.log4j.Logger;
import org.marc4j.MarcException;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.util.JsonParser;
import org.solrmarc.callnum.CallNumUtils;
import org.solrmarc.callnum.LCCallNumber;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.index.SolrIndexerMixin;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.tools.StringNaturalCompare;
import org.solrmarc.tools.Utils;

import org.solrmarc.tools.SolrMarcDataException;
import org.solrmarc.tools.SolrMarcDataException.eDataErrorLevel;


public class JSONCustomLocationMixin extends SolrIndexerMixin
{
    final static long oneDayInMillis = 1000 * 60 * 60 * 24; 
    static long currentMillisInited = 0L;
    static String JSONLookupURL = null;
    Map<String, String> libraryNameMap = null;
    Map<String, String> locationNameMap = null;
    Map<String, String> locationShadowedMap = null;
    Map<String, String> libraryAvailabilityMap = null;  /*  On Shelf or Request */
    Map<String, String> locationAvailabilityMap = null;  /*  Online  or  On Shelf  or Request */
    Map<String, String> libraryCirculatingMap = null;  /*  true or false */
    Map<String, String> locationCirculatingMap = null;  /*  true or false */
    private final static Logger logger = Logger.getLogger(JSONCustomLocationMixin.class);

    boolean isInited()
    {
        if (JSONLookupURL != null && libraryNameMap != null &&  System.currentTimeMillis() - currentMillisInited < oneDayInMillis ) 
        {
            return (true);
        }
        return(false);
    }
    
    final static MarcFactory factory = MarcFactory.newInstance();

    Set<String> callNumberFieldList = null;
    Set<String> callNumberFieldListNo050 = null;
    Map<String, Set<String>> callNumberClusterMap = null;
    Map<String, Set<String>> callNumberClusterMapNo050 = null;
    Set<String> callNumberLCFieldList = null;

    Comparator<String> normedComparator = new Comparator<String>()
    {
        public int compare(String s1, String s2)
        {
            String s1Norm = s1.replaceAll("[. ]", "");
            String s2Norm = s2.replaceAll("[. ]", "");
            return s1Norm.compareToIgnoreCase(s2Norm);
        }
    };

    String bestSingleCallNumber = null;
    String bestSingleLCCallNumber = null;
    List<VariableField> trimmedHoldingsList = null;
    String bestAuthor = null;
    String bestAuthorCutter = null;
    String pubYear = null;
    private String bestDate;
    Pattern datePattern = Pattern.compile("[^0-9]*((20|1[56789])[0-9][0-9])[^0-9]*.*");

    /**
     * This routine can be overridden in a sub-class to perform some processing that need to be done once for each record, and which may be needed by several indexing specifications, especially custom methods. The default version does nothing.
     * 
     * @param record
     *            - The MARC record that is being indexed.
     * @throws Exception
     */
    public void perRecordInit(Record record)
    {
        if (!isInited())
        {
            JSONLookupURL = System.getProperty("solrmarc.sirsi.location.url");
            initMapsFromJSON(JSONLookupURL);
            currentMillisInited = System.currentTimeMillis();
        }
        
        trimmedHoldingsList = getTrimmedHoldingsList(record, "999");

        callNumberFieldListNo050 = getCallNumberFieldSetNo050(record, trimmedHoldingsList);
        callNumberFieldList = getCallNumberFieldSet(record, callNumberFieldListNo050);
        callNumberLCFieldList = getLCCallNumberFieldSet090(record, callNumberFieldListNo050);
        callNumberClusterMapNo050 = getCallNumbersCleanedConflated(callNumberFieldListNo050, true);
        callNumberClusterMap = getCallNumbersCleanedConflated(callNumberFieldList, true);
        String valueArr[] = callNumberLCFieldList.toArray(new String[0]);
        //Comparator<String> comp = new StringNaturalCompare();
        //Arrays.sort(valueArr, comp);

        bestSingleCallNumber = getBestSingleCallNumber(callNumberClusterMap);
        bestSingleLCCallNumber = valueArr.length > 0 ? normalizeLCCallNumber(valueArr[0]) : null;
        List<String> author =  SolrIndexer.instance().getFieldListAsList(record, "100a:110a:111a:130a");
        if (author.size() > 0)
        {
            bestAuthor = author.get(0);
            bestAuthorCutter = org.solrmarc.callnum.Utils.getCutterFromAuthor(bestAuthor);
        }
        else
        {
            bestAuthor = null;
            bestAuthorCutter = null;
        }
        List<String> dates =  SolrIndexer.instance().getFieldListAsList(record, "008[7-10]:260c:264c");
        bestDate = "";
        for (String date : dates)
        {
            Matcher m = datePattern.matcher(date);
            if (m.matches()) 
            {
                bestDate = m.group(1);
                break;
            }
        }
        if (bestSingleCallNumber  != null && bestSingleCallNumber.equals(bestSingleLCCallNumber))
        {
            if (valueArr.length == 0)
            {
                callNumberLCFieldList = getLCCallNumberFieldSet090(record, callNumberFieldListNo050);
            }
        }
    }

    private List<VariableField> getTrimmedHoldingsList(Record record, String holdingsTag)
    {
        List<VariableField> result = record.getVariableFields(holdingsTag);
        removeShadowed999sFromList(record, result);
        removeLostHoldings(result);
        // this line (and the called method) added in response to JIRA ISSUE LIBSRVSRCHDISCOV-377
        removeOrderRecords(result);
        return result;
    }

    private void removeShadowed999sFromList(Record record, List<VariableField> fields999)
    {
        Iterator<VariableField> iter = fields999.iterator();
        while (iter.hasNext())
        {
            VariableField field = iter.next();
            DataField df = (DataField) field;
            if (df.getSubfield('3') != null) 
            {
                iter.remove();
            }
        }
    }

    private void removeLostHoldings(List<VariableField> fields999)
    {
        // String mapName = loadTranslationMap(null, "shadowed_location_map.properties");
//        AbstractMultiValueMapping locationMap = ValueIndexerFactory.instance().createMultiValueMapping("shadowed_location_map.properties");
        Iterator<VariableField> iter = fields999.iterator();
        while (iter.hasNext())
        {
            VariableField field = iter.next();
            DataField df = (DataField) field;
            Subfield currentLocation = df.getSubfield('k');
            Subfield homeLocation = df.getSubfield('l');
            Subfield shadowed = df.getSubfield('3');
            if ((currentLocation != null && locationShadowedMap.get(currentLocation.getData()).equals("HIDDEN")) || 
                    (homeLocation != null && locationShadowedMap.get(homeLocation.getData()).equals("HIDDEN")) ||
                    (shadowed != null && shadowed.getData().length() > 0))
            { 
                iter.remove();
            }
        }
    }

    // this method added in response to JIRA ISSUE LIBSRVSRCHDISCOV-377
    private void removeOrderRecords(List<VariableField> fields999)
    {
        Iterator<VariableField> iter = fields999.iterator();
        while (iter.hasNext())
        {
            VariableField field = iter.next();
            DataField df = (DataField) field;
            Subfield callNumber = df.getSubfield('a');
            if (callNumber != null && callNumber.getData().matches(".*[Oo][Rr][Dd][Ee][Rr][- ]*0.*"))
            {
                iter.remove();
            }
        }
    }

    private String getBestSingleCallNumber(Map<String, Set<String>> resultNormed)
    {
        if (resultNormed == null || resultNormed.size() == 0)
        {
            return (null);
        }
        String[] bestSet = getBestCallNumberSubset(resultNormed);
        if (bestSet.length == 0) return (null);
        String result = normalizeLCCallNumber(bestSet[0]);
        return (result);
    }

    private String normalizeLCCallNumber(String callNum)
    {
        String result = callNum;
        String resultParts[] = callNum.split(":", 2);
        if (resultParts[0].equals("LC"))
        {
            result = resultParts[0] + ":" + resultParts[1].trim().replaceAll("[^A-Za-z0-9.]", " ").replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".");
        }
        return(result);
    }

    private String[] getBestCallNumberSubset(Map<String, Set<String>> resultNormed)
    {
        if (resultNormed == null || resultNormed.size() == 0)
        {
            return (null);
        }
        int maxEntries = 0;
        // String maxEntriesKey = null;
        Set<String> maxEntrySet = null;
        int maxLCEntries = 0;
        // String maxLCEntriesKey = null;
        Set<String> maxLCEntrySet = null;
        Set<String> keys = resultNormed.keySet();
        for (String key : keys)
        {
            Set<String> values = resultNormed.get(key);
            if (values.size() > maxEntries)
            {
                maxEntries = values.size();
                // maxEntriesKey = key;
                maxEntrySet = values;
            }
            String firstNum = values.iterator().next();
            String parts[] = firstNum.split(":", 2);
            if ((parts[0].equals("LC") || parts[0].equals("")) && CallNumUtils.isValidLC(parts[1]) && values.size() > maxLCEntries)
            {
                maxLCEntries = values.size();
                // maxLCEntriesKey = key;
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
        return (valueArr);
    }

    /**
     * Since there are several routines that grab and process LC Call Numbers for a given record, this code is called once per record to gather the list of call numbers, rather than creating that list within each implementation of the custom indexing functions.
     * 
     * @param record
     *            - The MARC record that is being indexed.
     */
    private Set<String> getCallNumberFieldSetNo050(final Record record, List<VariableField> fields999)
    {
        if (fields999.isEmpty())
        {
            return (null);
        }
        Set<String> fieldList = new LinkedHashSet<String>();
        for (VariableField field : fields999)
        {
            DataField df = ((DataField) (field));
            String numberScheme = (df.getSubfield('w') != null) ? df.getSubfield('w').getData() : "";
            if (numberScheme.equals("MONO-SER") || numberScheme.equals("LCPER")) numberScheme = "LC";
            String callNumber = (df.getSubfield('a') != null) ? df.getSubfield('a').getData() : "";
            if (callNumber.startsWith("MSS") || callNumber.startsWith("Mss")) callNumber = callNumber.replaceFirst("MSS[ ]?", "M@");
            if (numberScheme.length() > 0 && callNumber.length() > 0) fieldList.add(numberScheme + ":" + callNumber);
        }
        return (fieldList);
    }

    /**
     * Since there are several routines that grab and process LC Call Numbers for a given record, this code is called once 
     * per record to gather the list of call numbers, rather than creating that list within each implementation of the custom indexing functions.
     * 
     * @param record
     *            - The MARC record that is being indexed.
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
            if (fieldParts[0].equals("LC") || (fieldParts[0].equals("") && CallNumUtils.isValidLC(field)))
            {
                hasLCNumber = true;
                break;
            }
        }
        // if there are no 999 fields with valid LC Call Numbers then look in the 050ab field
        if (!hasLCNumber)
        {
            List<String> fList2 = get050Entries(record);
            for (String field : fList2)
            {
                if (CallNumUtils.isValidLC(field))
                {
                    fieldList.add("LC:" + field);
                }
            }
        }
        return (fieldList);
    }

    /**
     * Since there are several routines that grab and process LC Call Numbers for a given record, this code is called once 
     * per record to gather the list of call numbers, rather than creating that list within each implementation of the custom indexing functions.
     * 
     * @param record
     *            - The MARC record that is being indexed.
     */
    private Set<String> getLCCallNumberFieldSet090(final Record record, Set<String> startingFieldList)
    {
        Set<String> fieldList = new LinkedHashSet<String>();
        List<String> fList1 = SolrIndexer.instance().getFieldListAsList(record, "090ab");
        if (fList1.size() == 0)
        {
            fList1 = get050Entries(record);
        }
        for (String field : fList1)
        {
            if (CallNumUtils.isValidLC(field))
            {
                fieldList.add("LC:" + field);
            }
        }
        if (startingFieldList != null)
        {
            for (String field : startingFieldList)
            {
                String fieldParts[] = field.split(":", 2);
                // dont add LC numbers that aren't valid according to the CallNumUtil routine
                if ((fieldParts[0].equals("LC") || fieldParts[0].equals("")) && CallNumUtils.isValidLC(fieldParts[1]))
                    fieldList.add(field);
            }
        }
        return (fieldList);
    }

    private List<String> get050Entries(Record record)
    {
        List<VariableField> vfs = record.getVariableFields("050");
        List<String> result = new ArrayList<String>();
        for (VariableField vf : vfs)
        {
            DataField df = (DataField)vf;
            Subfield sf[] = df.getSubfields("ab").toArray(new Subfield[0]);
            if (df.getIndicator2() == '4' && sf[0].getData().startsWith("AE"))
            {
                continue;
            }
            for (int i = 0; i < sf.length; i++)
            {
                if (i == 0 && sf.length > 1 && sf[1].getCode() == 'b')
                {
                    String val = sf[i].getData() + " " + sf[i+1].getData();
                    result.add(val);
                    i++;
                }
                else 
                {
                    String val = sf[i].getData();
                    result.add(val);
                }
            }
        }
        return(result);
    }
    /**
     * Extract a set of cleaned call numbers from a record
     * 
     * @param record
     * @return Clean call number
     */
    private Map<String, Set<String>> getCallNumbersCleanedConflated(Set<String> fieldList, boolean expectColon)
    {
        Map<String, Set<String>> resultNormed = new TreeMap<String, Set<String>>();
        if (fieldList == null || fieldList.size() == 0) return (null);
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
                prefix = parts[0] + ":";
                callNumPart = parts[1];
            }
            String val = callNumPart.trim().replaceAll("\\s\\s+", " ").replaceAll("\\s?\\.\\s?", ".");
            String nVal = val.replaceAll("^([A-Z][A-Z]?[A-Z]?) ([0-9])", "$1$2");
            if (!nVal.equals(val) && !val.startsWith("MSS"))
            {
                val = nVal;
            }
            String key = val.substring(0, Math.min(val.length(), 5)).toUpperCase();
            val = prefix + val;
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
        return (resultNormed);
    }

    /**
     * Extract call number prefix from a record
     * 
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
            return (null);
        }
        String valParts[] = val.split(":", 2);
        if (!valParts[0].equals("LC"))
        {
            return (null);
        }
        String vals[] = valParts[1].split("[^A-Za-z]+", 2);
        String prefix = vals[0];

        if (vals.length == 0 || vals[0] == null || vals[0].length() == 0 || vals[0].length() > 3 || !vals[0].toUpperCase().equals(vals[0]))
        {
            return (null);
        }
        else
        {
            while (result == null && prefix.length() > 0)
            {
                result = transMap.mapSingle(prefix);
                if (result == null && prefix.length() == 1)
                {
                    break;
                }
                if (result == null)
                {
                    prefix = prefix.substring(0, prefix.length() - 1);
                }
            }
        }
        if (mapName.equals("callnumber_map.properties"))
        {
            int partNum = Utils.isNumber(part) ? Integer.parseInt(part) : 0;
            if (result == null) return (result);
            if (partNum == 0) return (prefix + " - " + result.replaceAll("[|]", " - "));
            String resultParts[] = result.split("[|]");
            if (partNum - 1 >= resultParts.length) return (null);
            return (prefix.substring(0, 1) + " - " + resultParts[partNum - 1]);
        }
        else // detailed call number map
        {
            if (result == null) return (result);
            if (result.startsWith("{"))
            {
                String shelfKey = getLCShelfkey(valParts[1], record.getControlNumber());
                String keyDigits = shelfKey.substring(4, 8);
                String ranges[] = result.replaceAll("[{]", "").split("[}]");
                for (String range : ranges)
                {
                    String rangeParts[] = range.split("[-=]", 3);
                    if (keyDigits.compareTo(rangeParts[0]) >= 0 && keyDigits.compareTo(rangeParts[1]) <= 0)
                    {
                        return (prefix + rangeParts[0].replaceFirst("^0+", "") + "-" + prefix + rangeParts[1].replaceFirst("^0+", "") + " - " + rangeParts[2]);
                    }
                }
                return (null);
            }
            else if (result.startsWith(prefix.substring(0, 1)) && result.matches("[" + prefix.substring(0, 1) + "][A-Z]-[" + prefix.substring(0, 1) + "][A-Z] - .*"))
            {
                return (result);
            }
            else
            {
                return (prefix + " - " + result);
            }

        }
    }

    /*
     * Extract a single cleaned call number from a record
     * 
     * @param record
     * 
     * @return Clean call number
     */
    public String getCallNumberCleanedNew(final Record record, String sortable)
    {
        boolean sortableFlag = (sortable != null && (sortable.equals("sortable") || sortable.equals("true")));
        String result = bestSingleCallNumber;
        if (result == null) return (result);
        String resultParts[] = result.split(":", 2);
        if (sortableFlag && (resultParts[0].equals("LC") || (resultParts[0].equals("") && CallNumUtils.isValidLC(resultParts[1])))) 
        {
            result = getLCShelfkey(resultParts[1], record.getControlNumber());
        }
        else if (resultParts[1].startsWith("M@")) 
            result = result.replaceAll("M@", "MSS ");
        return (result);

    }

    public String getLCShelfkey(String lcCallNum, String controlNum)
    {
        try {
            String result = CallNumUtils.getLCShelfkey(lcCallNum, controlNum);
            return(result);
        }
        catch (IllegalArgumentException iae)
        {
            throw new SolrMarcDataException(eDataErrorLevel.INFO, iae.getMessage());
        }        
    }
    
    public String getShelfKey(final Record record)
    {
        String callnum = bestSingleCallNumber;
        String result = null;
        if (callnum == null) return (null);
        String resultParts[] = callnum.split(":", 2);
        if ((resultParts[0].equals("LC") || resultParts[0].equals("")) && CallNumUtils.isValidLC(resultParts[1]))
        {
            result = getLCShelfkey(resultParts[1], record.getControlNumber());
        }
        return (result);
    }

    public String getReverseShelfKey(final Record record)
    {
        String shelfKey = getShelfKey(record);
        if (shelfKey == null) return (shelfKey);
        String revShelfKey = CallNumUtils.getReverseShelfKey(shelfKey);
        return (revShelfKey);
    }

    public String getLCShelfKey(final Record record)
    {
        String callnum = bestSingleLCCallNumber;
        String result = null;
        if (callnum == null) return (null);
        String resultParts[] = callnum.split(":", 2);
        if ((resultParts[0].equals("LC") || resultParts[0].equals("")) && CallNumUtils.isValidLC(resultParts[1]))
        {
            result = getLCShelfkey(resultParts[1], record.getControlNumber());
        }
        return (result);
    }

    public String getReverseLCShelfKey(final Record record)
    {
        String shelfKey = getLCShelfKey(record);
        if (shelfKey == null) return (shelfKey);
        String revShelfKey = CallNumUtils.getReverseShelfKey(shelfKey);
        return (revShelfKey);
    }

    public String getUniquishLCCallNumber(final Record record)
    {
        String callnum = bestSingleLCCallNumber;
        if (callnum == null) return (null);
        if (bestAuthorCutter != null && !callnum.contains(bestAuthorCutter) && callnum.matches(".*[A-Z][0-9]+")) 
        {
            callnum = callnum + "." + bestAuthorCutter;
        }
        if (bestDate != null && !callnum.contains(bestDate))
        {
            callnum = callnum + " " + bestDate;
        }
        return(callnum);
    }
    
    public String getTrimmedUniquishLCCallNumber(final Record record)
    {
        String callnum = getUniquishLCCallNumber(record);
        if (callnum == null) return (null);
        String result = null;
        String resultParts[] = callnum.split(":", 2);
        if ((resultParts[0].equals("LC") || resultParts[0].equals("")) && CallNumUtils.isValidLC(resultParts[1]))
        {
            result = resultParts[1];
        }
        return (result);
    }
    
    public String getUniquishLCShelfKey(final Record record, String uniqueness)
    {
        String callnum = getUniquishLCCallNumber(record);
        if (callnum == null) return (null);
        if (uniqueness.equals("unique")) 
        {
            callnum = callnum + " " + record.getControlNumber();
        }
        String result = null;
        String resultParts[] = callnum.split(":", 2);
        if ((resultParts[0].equals("LC") || resultParts[0].equals("")) && CallNumUtils.isValidLC(resultParts[1]))
        {
            LCCallNumber callNum = new LCCallNumber(resultParts[1]);
            result = callNum.getPaddedShelfKey();
        }
        return (result);
    }

    public String getUniquishReverseLCShelfKey(final Record record, String uniqueness)
    {
        String shelfKey = getUniquishLCShelfKey(record, uniqueness);
        if (shelfKey == null) return (shelfKey);
        String revShelfKey = CallNumUtils.getReverseShelfKey(shelfKey);
        return (revShelfKey);
    }

    public String getUniquishLCShelfKeyIfNotShadowed(final Record record, String uniqueness) throws Exception
    {
        String shadowedLocation = getShadowedLocation(record, "returnHidden");
        if (shadowedLocation != null && shadowedLocation.equals("VISIBLE"))
        {
            if (uniqueness.startsWith("uniqu"))
            {
                return(getUniquishLCShelfKey(record, uniqueness));
            }
            else
            {
                return(getLCShelfKey(record));
            }
        }
        return(null);
    }
   
    public String getUniquishReverseLCShelfKeyIfNotShadowed(final Record record, String uniqueness) throws Exception
    {
        String shadowedLocation = getShadowedLocation(record, "returnHidden");
        if (shadowedLocation != null && shadowedLocation.equals("VISIBLE"))
        {
            if (uniqueness.startsWith("uniqu"))
            {
                return(getUniquishReverseLCShelfKey(record, uniqueness));
            }
            else
            {
                return(getReverseLCShelfKey(record));
            }
        }
        return(null);
    }
    
    public String getLCShelfKeyIfNotShadowed(final Record record, String returnHidden) throws Exception
    {
        String shadowedLocation = getShadowedLocation(record, "true");
        if (shadowedLocation.equals("VISIBLE"))
        {
            return(getLCShelfKey(record));
        }
        return(null);
    }
   
    public String getReverseLCShelfKeyIfNotShadowed(final Record record, String propertiesMap, String returnHidden, String processExtra) throws Exception
    {
        String shadowedLocation = getShadowedLocation(record, "true");
        if (shadowedLocation.equals("VISIBLE"))
        {
            return(getReverseLCShelfKey(record));
        }
        return(null);
    }
    public String getBestLCCallNumber(final Record record)
    {
        return(bestSingleLCCallNumber);
    }

    public Set<String> getCallNumbersCleanedNewNo050(final Record record, String conflatePrefixes)
    {
        return (getCallNumbersCleanedNew(record, conflatePrefixes, this.callNumberFieldListNo050, this.callNumberClusterMapNo050));
    }

    public Set<String> getCallNumbersCleanedNew(final Record record, String conflatePrefixes)
    {
        return (getCallNumbersCleanedNew(record, conflatePrefixes, this.callNumberFieldList, this.callNumberClusterMap));
    }

    private String getCommonPrefix(String string1, String string2, Comparator<String> comp)
    {
        int l1 = string1.length();
        int l2 = string2.length();
        int l = Math.min(l1, l2);
        int prefixLen = l;
        for (int i = 0; i < l; i++)
        {
            if (comp.compare(string1.substring(i, i + 1), string2.substring(i, i + 1)) != 0)
            {
                prefixLen = i;
                break;
            }
        }
        return (string1.substring(0, prefixLen));
    }

    private String getCallNum(final String callNum)
    {
        String callNumParts[] = callNum.split(":", 2);
        return (callNumParts[1]);
    }

    /**
     * Extract a set of cleaned call numbers from a record
     * 
     * @param record
     * @return Clean call number
     */
    public Set<String> getCallNumbersCleanedNew(final Record record, String conflatePrefixes, Set<String> localCallNumberFieldList, Map<String, Set<String>> localCallNumberClusterMap)
    {
        boolean conflate = !conflatePrefixes.equalsIgnoreCase("false");

        if (!conflate)
        {
            Set<String> fieldList = localCallNumberFieldList;
            if (fieldList == null || fieldList.isEmpty())
            {
                return (null);
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
            if (resultNormed == null || resultNormed.size() == 0) return (null);
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
                            sb.append(sep).append(valueArr[i]);
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

    public Set<String> getCustomLibrary(final Record record) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        List<VariableField> fields999 = trimmedHoldingsList;
//        AbstractMultiValueMapping visMap = ValueIndexerFactory.instance().createMultiValueMapping(visibilityMap);
//        AbstractMultiValueMapping libMap = ValueIndexerFactory.instance().createMultiValueMapping(libraryMap);

        for (VariableField vfield :fields999)
        {
            DataField field = (DataField)vfield;
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            Subfield shadowF = field.getSubfield('3');
            String lib = (libF != null ? libF.getData() : null);
            String mappedLib = libraryNameMap.get(lib);
            if (mappedLib == null || resultSet.contains(mappedLib)) continue;
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            boolean shadow = (shadowF != null ? shadowF.getData().length() > 0 : false);
            String mappedHomeVis = locationShadowedMap.get(homeLoc);
            if (shadow || mappedHomeVis.equals("HIDDEN"))
            {
                continue;
            }
            if (curLoc != null)
            {
                String mappedCurVis = locationShadowedMap.get(curLoc);
                if (mappedCurVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
            }
            resultSet.add(mappedLib);
        }
        return (resultSet);
    }

    public Set<String> getCustomLocationWithOverride(final Record record, String locationOverrideMap) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        List<VariableField> fields999 = trimmedHoldingsList;
        AbstractMultiValueMapping locOverrideMap = ValueIndexerFactory.instance().createMultiValueMapping(locationOverrideMap);
//        AbstractMultiValueMapping visMap = ValueIndexerFactory.instance().createMultiValueMapping(visibilityMap);
//        AbstractMultiValueMapping libMap = ValueIndexerFactory.instance().createMultiValueMapping(libraryMap);
        for (VariableField vfield : fields999)
        {
            DataField field = (DataField)vfield;
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            Subfield shadowF = field.getSubfield('3');
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            String lib = (libF != null ? libF.getData() : null);
            boolean shadow = (shadowF != null ? shadowF.getData().length() > 0 : false);
            String mappedHomeVis = locationShadowedMap.get(homeLoc);
            String mappedHomeLocName = locationNameMap.get(homeLoc);
            String mappedHomeLoc = locOverrideMap.mapSingle(homeLoc);
            if (mappedHomeLoc == null) mappedHomeLoc = mappedHomeLocName;
            
            if (!shadow && mappedHomeVis.equals("VISIBLE") && mappedHomeLoc == null)
            {
                String combinedLocMapped = locationNameMap.get(homeLoc + "__" + lib);
                if (combinedLocMapped != null) mappedHomeLoc = combinedLocMapped;
            }
            String mappedLib = libraryNameMap.get(lib);
            if (curLoc != null)
            {
                String mappedCurLoc = locOverrideMap.mapSingle(curLoc);
                if (mappedCurLoc == null) mappedCurLoc = locationNameMap.get(curLoc);
                String mappedCurVis = locationShadowedMap.get(curLoc);
                if (shadow || mappedCurVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
                if (mappedCurLoc != null && mappedCurLoc.length() > 0)
                {
                    if (mappedCurLoc.contains("$m") && mappedLib != null)
                    {
                        mappedCurLoc = mappedCurLoc.replaceAll("[$]m", mappedLib);
                    }
                    else if (mappedCurLoc.contains("$m") && mappedLib == null)
                    {
                        mappedCurLoc = mappedCurLoc.replaceAll("[$]m[ ]?", "");
                    }
                    if (mappedCurLoc.contains("$l") && mappedHomeLocName != null)
                    {
                        mappedCurLoc = mappedCurLoc.replaceAll("[$]l", mappedHomeLocName);
                    }
                    else if (mappedCurLoc.contains("$l") && mappedHomeLocName == null)
                    {
                        mappedCurLoc = mappedCurLoc.replaceAll("[ ]?[$]l", "");
                    }
                    resultSet.add(mappedCurLoc);
                    continue; // Used
                }
            }
            if (mappedHomeVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
            if (mappedHomeLoc != null && mappedHomeLoc.contains("$"))
            {
                if (mappedLib != null) mappedHomeLoc = mappedHomeLoc.replaceAll("[$]m", mappedLib);
                else mappedHomeLoc = mappedHomeLoc.replaceAll("[$]m", "Library");
                if (mappedHomeLocName != null) mappedHomeLoc = mappedHomeLoc.replaceAll("[$]l", mappedHomeLocName);
                else mappedHomeLoc = mappedHomeLoc.replaceAll("[$]l", "Undefined");
            }
            if (mappedHomeLoc != null) resultSet.add(mappedHomeLoc);
        }
        return (resultSet);
    }

    public Set<String> getCustomLocation(final Record record) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        List<VariableField> fields999 = trimmedHoldingsList;
//        AbstractMultiValueMapping locMap = ValueIndexerFactory.instance().createMultiValueMapping(locationMap);
//        AbstractMultiValueMapping visMap = ValueIndexerFactory.instance().createMultiValueMapping(visibilityMap);
//        AbstractMultiValueMapping libMap = ValueIndexerFactory.instance().createMultiValueMapping(libraryMap);
        for (VariableField vfield : fields999)
        {
            DataField field = (DataField)vfield;
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            Subfield shadowF = field.getSubfield('3');
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            String lib = (libF != null ? libF.getData() : null);
            boolean shadow = (shadowF != null ? shadowF.getData().length() > 0 : false);
            String mappedHomeVis = locationShadowedMap.get(homeLoc);
            String mappedHomeLoc = locationNameMap.get(homeLoc);
            if (!shadow && mappedHomeVis.equals("VISIBLE") && mappedHomeLoc == null)
            {
                String combinedLocMapped = locationNameMap.get(homeLoc + "__" + lib);
                if (combinedLocMapped != null) mappedHomeLoc = combinedLocMapped;
            }
            String mappedLib = libraryNameMap.get(lib);
            if (curLoc != null)
            {
                String mappedCurLoc = locationNameMap.get(curLoc);
                String mappedCurVis = locationShadowedMap.get(curLoc);
                if (shadow || mappedCurVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
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
                    continue; // Used
                }
            }
            if (mappedHomeVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
//            if (mappedHomeLoc != null && mappedHomeLoc.contains("$"))
//            {
//                if (mappedLib != null) mappedHomeLoc = mappedHomeLoc.replaceAll("[$]m", mappedLib);
//                else mappedHomeLoc = mappedHomeLoc.replaceAll("[$]m", "Library");
//
//            }
            if (mappedHomeLoc != null) resultSet.add(mappedHomeLoc);
        }
        return (resultSet);
    }

    public String getCustomCirculating(final Record record) throws Exception
    {
        String result = "false";
        boolean circulating = false;
        List<VariableField> fields999 = trimmedHoldingsList;
//        AbstractMultiValueMapping locMap = ValueIndexerFactory.instance().createMultiValueMapping(locationMap);
//        AbstractMultiValueMapping visMap = ValueIndexerFactory.instance().createMultiValueMapping(visibilityMap);
//        AbstractMultiValueMapping libMap = ValueIndexerFactory.instance().createMultiValueMapping(libraryMap);
        for (VariableField vfield : fields999)
        {
            DataField field = (DataField)vfield;
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            Subfield shadowF = field.getSubfield('3');
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            String lib = (libF != null ? libF.getData() : null);
            boolean shadow = (shadowF != null ? shadowF.getData().length() > 0 : false);
            String libCirculating = libraryCirculatingMap.get(lib);
            if (curLoc != null)
            {
                String curLocCirculating = locationCirculatingMap.get(curLoc);
                String mappedCurVis = locationShadowedMap.get(curLoc);
                if (shadow || mappedCurVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
                if (curLocCirculating != null)
                {
                    if (libCirculating.equals("true") && curLocCirculating.equals("true"))
                        circulating = true;
                    continue; 
                }
            }
            String mappedHomeVis = locationShadowedMap.get(homeLoc);
            if (mappedHomeVis.equals("HIDDEN")) continue; // this copy of the item is Hidden, go no further
//            if (mappedHomeLoc != null && mappedHomeLoc.contains("$"))
//            {
//                if (mappedLib != null) mappedHomeLoc = mappedHomeLoc.replaceAll("[$]m", mappedLib);
//                else mappedHomeLoc = mappedHomeLoc.replaceAll("[$]m", "Library");
//
//            }
            String homeLocCirculating = locationCirculatingMap.get(homeLoc);
            if (libCirculating.equals("true") && homeLocCirculating.equals("true"))
                circulating = true;   
        }
        return (circulating ? "true" : "false");
    }

    public Set<String> getCustomAvailabilityForLocation(final Record record) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        List<VariableField> fields999 = trimmedHoldingsList;
        for (VariableField vfield : fields999)
        {
            DataField field = (DataField)vfield;
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
            Subfield libF = field.getSubfield('m');
            Subfield shadowF = field.getSubfield('3');
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
            String lib = (libF != null ? libF.getData() : null);
            boolean shadow = (shadowF != null ? shadowF.getData().length() > 0 : false);
            if (!shadow)
            {
                addCustomAvailabilityForLocation(resultSet, curLoc, homeLoc, lib);
            }
        }
        return (resultSet);
    }
    /*
     C:/Users/rh9ec/Development/Projects/solrmarc-3.0/test/data/records/locations_unique.txt  C:/Users/rh9ec/Development/Projects/solrmarc-3.0/test/data/translation_maps/location_availability_map.properties   C:/Users/rh9ec/Development/Projects/solrmarc-3.0/test/data/translation_maps/shadowed_location_map.properties  C:/Users/rh9ec/Development/Projects/solrmarc-3.0/test/data/translation_maps/library_map.properties
     */
    
//    public static void main(String[] args)
//    {
//        try
//        {
//            testCustomAvailabilityForLocation(args[0], args[1], args[2]);
//        }
//        catch (Exception e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//    
//    public static void testCustomAvailabilityForLocation(String filename, String homeDir, String sirsiAvailabilityURL) throws Exception
//    {
//        System.setProperty("solrmarc.sirsi.location.url", sirsiAvailabilityURL);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
//        String homeDirs[] = new String[1];
//        homeDirs[0] = homeDir;
//        ValueIndexerFactory.initialize(homeDirs);
//        String line;
//        while ((line = reader.readLine()) != null)
//        {
//            Set<String> resultSet = new LinkedHashSet<String>();
//
//            String lineParts[] = line.split("[|]");
//            String sampleID = lineParts[0];
//            String curLoc = lineParts[1];
//            String homeLoc = lineParts[2];
//            String lib = lineParts[3];
//            addCustomAvailabilityForLocation(resultSet, curLoc, homeLoc, lib);
//            StringBuilder sb = new StringBuilder();
//            for (String member : resultSet)
//            {
//                sb.append(" : ");
//                sb.append(member);
//            }
//            if (sb.length() == 0) sb.append(" : HIDDEN");
//            System.out.println(line + sb.toString());
//        }
//        reader.close();
//    }
    
    public void addCustomAvailabilityForLocation(Set<String> resultSet, String curLoc, String homeLoc, String lib) throws Exception
    {    
        String mappedHomeVis = locationShadowedMap.get(homeLoc);
        String mappedHomeAvail = locationAvailabilityMap.get(homeLoc);
        if (homeLoc.equals("INTERNET") || (curLoc != null && curLoc.equals("INTERNET")))
        {
            resultSet.add("Online");
            return;
        }
        String mappedLibAvailability = libraryAvailabilityMap.get(lib);
        if (curLoc != null)
        {
            String mappedCurAvail = locationAvailabilityMap.get(curLoc);
            String mappedCurVis = locationShadowedMap.get(curLoc);
            if (mappedCurVis.equals("HIDDEN")) return; // this copy of the item is Hidden, go no further
            if (mappedCurVis.equals("VISIBLE") )
            {
                if (mappedLibAvailability.equals("Request"))
                {
                    resultSet.add(mappedLibAvailability);
                    return;
                }
            }
            if (mappedCurAvail != null)
            {
                resultSet.add(mappedCurAvail);
                return; // Used
            }
        }
        if (mappedHomeVis.equals("HIDDEN")) return; // this copy of the item is Hidden, go no further
        if (mappedLibAvailability.equals("Request"))
        {
            resultSet.add(mappedLibAvailability);
            return;
        }
        if (mappedHomeAvail != null)
        {
            resultSet.add(mappedHomeAvail);
        }
    }

    public String getShadowedLocation(final Record record, String returnHidden) throws Exception
    {
        if (record.getControlNumber() == null || record.getControlNumber().length() < 2) return (null);
        boolean returnHiddenRecs = returnHidden.startsWith("return") || returnHidden.equals("true");

        boolean visible = false;
        List<VariableField> vfields = record.getVariableFields("999");
        for (VariableField vfield : (List<VariableField>) vfields)
        {
            DataField field = (DataField)vfield;
            Subfield callnumF = field.getSubfield('a');
            Subfield curLocF = field.getSubfield('k');
            Subfield homeLocF = field.getSubfield('l');
//            Subfield libF = field.getSubfield('m');
            Subfield shadowF = field.getSubfield('3');
            String callnum = (callnumF != null ? callnumF.getData() : null);
            String curLoc = (curLocF != null ? curLocF.getData() : null);
            String homeLoc = (homeLocF != null ? homeLocF.getData() : null);
//            String lib = (libF != null ? libF.getData() : null);
            boolean shadow = (shadowF != null ? shadowF.getData().length() > 0 : false);
            if (shadow && homeLoc != null && homeLoc.equals("INTERNET"))
            {
            	shadow = false;
            }
            // this test (and the change above to return aikl instead of ikl) added in response to JIRA ISSUE LIBSRVSRCHDISCOV-377
            if (callnum.matches(".*[Oo][Rr][Dd][Ee][Rr][- ]*0.*"))
            {
                continue;
            }
            else if (shadow == true || ( curLoc == null && homeLoc == null))
            {
                continue;
            }
            else if (curLoc == null)
            {
                String mappedHomeLoc = locationShadowedMap.get(homeLoc);
                if (mappedHomeLoc.equals("VISIBLE")) visible = true;
            }
            else if (curLoc != null && curLoc.equals("RSRVSHADOW"))
            {
                String mappedHomeLoc = locationShadowedMap.get(homeLoc);
                if (mappedHomeLoc.equals("VISIBLE"))
                {
                    visible = true;
                }
            }
            else if (curLoc != null && homeLoc != null)
            {
                String mappedHomeLoc = locationShadowedMap.get(homeLoc);
                String mappedCurLoc = locationShadowedMap.get(curLoc);
                if (mappedHomeLoc.equals("VISIBLE") && mappedCurLoc.equals("VISIBLE"))
                {
                    visible = true;
                }
            }
        }
        String result = (visible ? "VISIBLE" : "HIDDEN");
        if (!visible && !returnHiddenRecs)
        {
            return (null);
        }
        return (result);
    }

    public Set<String> getLibLocType(final Record record, String libMatch, String locMatch, String typeMatch)
    {
        List<VariableField> lvf = record.getVariableFields("999");
        Set<String> result = new LinkedHashSet<String>();
        for (VariableField vf : lvf)
        {
            DataField df = (DataField)vf;
            Subfield lib = df.getSubfield('m');
            Subfield loc = df.getSubfield('l');
            Subfield type = df.getSubfield('t');
            if (lib != null && lib.getData().matches(libMatch) && 
                loc != null && loc.getData().matches(locMatch) && 
                type != null && type.getData().matches(typeMatch))
            {
                String resultStr = lib.getData() + "_" + loc.getData() + "_" + type.getData();
                result.add(resultStr);
            }
        }
        return(result);
    }
    
    //-----------------------------------------------------------------------------------------------------
    
    public Set<String> getSummaryHoldingsInfo(Record record) throws Exception
    {
        Set<String> result = new LinkedHashSet<String>();
        Set<String> ivyresult = new LinkedHashSet<String>();
        String fieldsToUseStr = "852|853|863|866|867";
        String fieldsToUse[] = fieldsToUseStr.split("[|]");
        
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
                    holdingsField = buildHoldingsField(libraryField, "", getSubfieldVal(libraryField, 'z', ""), "", getSubfieldVal(libraryField, "hi", ""));
                    addHoldingsField(result, ivyresult, holdingsField);
                }
            }
            else if (df.getTag().equals("853"))  continue; // ignore 853's here.
            else if (df.getTag().equals("866"))  
            {
                holdingsField = buildHoldingsField(libraryField, getSubfieldVal(df, 'a', ""), getSubfieldVal(df, 'z', ""), "Library has", null);
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("867"))
            {
                holdingsField = buildHoldingsField(libraryField, getSubfieldVal(df, "z+a", ""), getSubfieldVal(df, "-z", ""), "Suppl text holdings", null);
                addHoldingsField(result, ivyresult, holdingsField);
            }
            else if (df.getTag().equals("868"))
            {
                holdingsField = buildHoldingsField(libraryField, getSubfieldVal(df, 'a', ""), getSubfieldVal(df, 'z', ""), "Index text holdings", null);
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
                    holdingsField = buildHoldingsField(libraryField, processEncodedField(df, labelField), getSubfieldVal(df, 'z', ""), "Library has", null);
                    addHoldingsField(result, ivyresult, holdingsField);
                }
                else if (labelField != null && j > i + 1) 
                {
                    VariableField nvf = fields.get(j-1);
                    DataField ndf = (DataField)nvf;
                    holdingsField = buildHoldingsField(libraryField, processEncodedFieldRange(df, ndf, labelField), getSubfieldVal(df, 'z', ""), "Library has", null);
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

    private String buildHoldingsField(DataField libraryField, String holdingsValue, String publicNote, String holdingsType, String callNumber) throws Exception
    {
        if (libraryField == null || ((holdingsValue == null || holdingsValue.length() == 0) && (publicNote.length() == 0 && (callNumber == null || callNumber.length() == 0)))) return(null);
        String libraryName = libraryField.getSubfield('b') != null ? libraryNameMap.get(libraryField.getSubfield('b').getData()) : null;
        String locName = libraryField.getSubfield('c') != null ? locationNameMap.get(libraryField.getSubfield('c').getData()) : null;
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
        List<String> vol1 = new ArrayList<String>();
        List<String> vol2 = new ArrayList<String>();
        for (char subfield = 'a'; subfield <= 'f'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data1 = getSubfieldVal(df1, subfield, null);
            String data2 = getSubfieldVal(df2, subfield, null);
            if (label == null || data1 == null || data2 == null) break;
            if (label.startsWith("(") && label.endsWith(")")) label = " ";
            vol1.add(label + data1);
            vol2.add(label + data2);
        }
        result.append(rangifyArray(vol1, vol2, true));
        List<String> alt1 = new ArrayList<String>();
        List<String> alt2 = new ArrayList<String>();
        for (char subfield = 'g'; subfield <= 'h'; subfield++)
        {
            String label = getSubfieldVal(labelField, subfield, null);
            String data1 = getSubfieldVal(df1, subfield, null);
            String data2 = getSubfieldVal(df2, subfield, null);
            if (label == null || data1 == null || data2 == null) break;
            alt1.add(label);
            alt2.add(label);
            alt1.add(data1);
            alt2.add(data2);
        }
        if (alt1.size() > 0)
        {
            result.append(" (").append(rangifyArray(alt1, alt2, true)).append(")");
        }
        List<String> date1 = new ArrayList<String>();
        List<String> date2 = new ArrayList<String>();
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
                else if (label.equalsIgnoreCase("(day)") || label.equalsIgnoreCase("(unit)"))
                {
                    strToPrepend = " ";
                }
                if (prependStr)
                {
                    date1.add(strToPrepend);
                    date2.add(strToPrepend);
                }
                date1.add(data1);
                date2.add(data2);
                prependStr = true;
            }
        }
        if (date1.size() > 0 && date2.size() > 0)
        {
            if (result.length() > 0)  result.append(" (").append(rangifyArray(date1, date2, false)).append(")");
            else result.append(rangifyArray(date1, date2, false));
        }    
        return result.toString();
    }

    private String rangifyArray(List<String> dataArr1, List<String> dataArr2, boolean insertCommas)
    {
        StringBuilder result = new StringBuilder();
        int i;
        if (dataArr1.size() == 1 && dataArr2.size() == 1 && dataArr1.get(0).equals(dataArr2.get(0)))
        {
            return(dataArr1.get(0));
        }
        for (i = 0; i < dataArr1.size() && i < dataArr2.size(); i++)
        {
            if (dataArr1.get(i).equals(dataArr2.get(i)))
            {
                if (insertCommas && i > 0) result.append(", ");
                result.append(dataArr1.get(i));
            }
            else
            {
                if (insertCommas && i > 0) result.append(", ");
                String separator = insertCommas ? ", " : "";
                result.append(joinRemainder(dataArr1, i, separator)).append("-").append(joinRemainder(dataArr2, i, separator));
                break;
            }
        }

        return result.toString();
    }

    public static String joinRemainder(List<String> strings, int startAt, String separator) 
    {
        if (strings == null || strings.size() <= startAt)
        {
            return "";
        } 
        else if (strings.size() == startAt-1)
        {
            return strings.get(startAt);
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(strings.get(startAt));
            for (int i = 1; i + startAt < strings.size(); i++)
            {
                sb.append(separator).append(strings.get(startAt+i));
            }
            return sb.toString();
        }
    }
    
/*    private Object rangify(String data1, String data2)
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
*/
    
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
            
    //-----------------------------------------------------------------------------------------------------
    
    public final static int NO_ARRAY = 0;
    
    public final static int LIBRARIES_ARRAY = 1;

    public final static int LOCATIONS_ARRAY = 2;

    private void initMapsFromJSON(String urlSirsiAvailability)
    {
        if (JSONLookupURL == null)
        {
            JSONLookupURL = System.getProperty("solrmarc.sirsi.location.url");
        }
        if (JSONLookupURL == null)
        {
        	JSONLookupURL = System.getenv("SOLRMARC_SIRSI_LOCATION_URL");
        }
        if (JSONLookupURL == null)
        {
            JSONLookupURL = "https://ils-connector-ws-dev.internal.lib.virginia.edu/v4/availability/list";
        }
        URL url;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try
        {
            url = new URL(JSONLookupURL);
            logger.info("Establishing Connection to URL: "+ JSONLookupURL );
            connection = (HttpURLConnection)url.openConnection();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser parser = new JsonParser(JsonParser.OPT_INTERN_KEYWORDS |
                    JsonParser.OPT_UNQUOTED_KEYWORDS |
                    JsonParser.OPT_SINGLE_QUOTE_STRINGS);
            parser.setInput("availability/list", reader, false);
            parseAllInput(parser);
            logger.info("Data successfully read from URL: "+ JSONLookupURL );          
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            logger.error("Error connecting or reading from : "+ JSONLookupURL , e);
        }
        finally {
            if (reader != null)  try { reader.close(); } catch (IOException ioe) {}
            if (connection != null) connection.disconnect();
        }
    }

    int parseAllInput(JsonParser parser)
    {
        int code = parser.getEventCode();
        String curKey = null;
        int inArray = NO_ARRAY;
        int parserLevel = 0;
        
        while (true) {
            final String mname = parser.getMemberName();

            switch (code) {
                case JsonParser.EVT_OBJECT_BEGIN:
                    if (parserLevel == 0)
                    {
                        //record = factory.newRecord();
                    } 
                    parserLevel++;
                    
                    break;
                case JsonParser.EVT_OBJECT_ENDED:
                    parserLevel--;
                    if (parserLevel == 0) 
                    {
                        return 0;
                    } 

                    break;
                case JsonParser.EVT_ARRAY_BEGIN:
                    if (mname.equals("libraries")) 
                    {
                        inArray = LIBRARIES_ARRAY;
                        libraryNameMap = new LinkedHashMap<String, String>();
                        libraryAvailabilityMap = new LinkedHashMap<String, String>();
                        libraryCirculatingMap =  new LinkedHashMap<String, String>();
                    } 
                    else if (mname.equals("locations")) 
                    {
                        inArray = LOCATIONS_ARRAY;
                        locationNameMap = new LinkedHashMap<String, String>();
                        locationShadowedMap = new LinkedHashMap<String, String>();
                        locationAvailabilityMap = new LinkedHashMap<String, String>();
                        locationCirculatingMap = new LinkedHashMap<String, String>();
                    }
                    
                    break;
                case JsonParser.EVT_ARRAY_ENDED:
                    if (mname.equals("libraries")) 
                    {
                        inArray = NO_ARRAY;
                    } 
                    else if (mname.equals("locationss")) 
                    {
                        inArray = NO_ARRAY;
                    }
                    
                    break;
                case JsonParser.EVT_OBJECT_MEMBER:
                    String value = parser.getMemberValue();
                    if (JsonParser.isQuoted(value)) 
                    {
                        value = JsonParser.stripQuotes(value);
                    }

                    if (mname.equals("key")) 
                    {
                        curKey = value;
                    } 
                    else if (mname.equals("on_shelf"))
                    {
                        if (inArray == LIBRARIES_ARRAY) 
                        {
                            libraryAvailabilityMap.put(curKey,  (Boolean.parseBoolean(value) ? "On shelf" : "Request"));
                        }
                        else if (inArray == LOCATIONS_ARRAY)
                        {
                            locationAvailabilityMap.put(curKey,  (Boolean.parseBoolean(value) ? "On shelf" : "Request"));
                        }
                    } 
                    else if (mname.equals("online"))
                    {
                        if (inArray == LOCATIONS_ARRAY && Boolean.parseBoolean(value) == true)
                        {
                            locationAvailabilityMap.put(curKey,  "Online");
                        }
                    } 
                    else if (mname.equals("shadowed"))
                    {
                       if (inArray == LOCATIONS_ARRAY)
                        {
                            locationShadowedMap.put(curKey,  (Boolean.parseBoolean(value) ? "HIDDEN" : "VISIBLE"));
                        }
                    } 
                    else if (mname.equals("description")) 
                    {
                        if (inArray == LIBRARIES_ARRAY) 
                        {
                            libraryNameMap.put(curKey,  value);
                        }
                        else if (inArray == LOCATIONS_ARRAY)
                        {
                            locationNameMap.put(curKey,  value);
                        }
                    } 
                    else if (mname.equals("circulating")) 
                    {
                        if (inArray == LIBRARIES_ARRAY) 
                        {
                            libraryCirculatingMap.put(curKey,   (Boolean.parseBoolean(value) ? "true" : "false"));
                        }
                        else if (inArray == LOCATIONS_ARRAY)
                        {
                            locationCirculatingMap.put(curKey,   (Boolean.parseBoolean(value) ? "true" : "false"));
                        }
                    } 

                    break;
                case JsonParser.EVT_INPUT_ENDED:
                    throw new MarcException("Premature end of input in JSON file");
            }
            code = parser.next();
        }

        // return record;
    }

}
