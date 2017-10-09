package org.solrmarc.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.tools.DataUtil;
import org.solrmarc.tools.Utils;


/**
 * class SolrIndexer
 *
 * This class exists solely for backwards compatibility purposes.  The intention is that if a previous custom function
 * was being used, one that provides the same functionality can be found here.  Furthermore if there were any helper functions
 * that could have been used to create your own custom indexing functions those helper functions should be found here as well.
 *
 * In most cases the methods found here are merely shims to translate the desired method to use the newer functionality that
 * is now available.
 *
 *
 * @author rh9ec
 *
 */


@Deprecated
public class SolrIndexerShim
{
    //private Map<String, AbstractValueIndexer<?>> indexerCache = new ConcurrentHashMap<String, AbstractValueIndexer<?>>();
    private ThreadLocal<Map<String, AbstractValueIndexer<?>>> indexerCache =
        new ThreadLocal<Map<String, AbstractValueIndexer<?>>>()
        {
            @Override
            protected Map<String, AbstractValueIndexer<?>> initialValue()
            {
                return new HashMap<>();
            }
        };

    /** map of translation maps.  keys are names of translation maps;
     *  values are the translation maps (hence, it's a map of maps) */
    //private Map<String, Object> transMapMap = new HashMap<String, Object>();
    private ConcurrentMap<String, Object> transMapMap = new ConcurrentHashMap<String, Object>();

    private  SolrIndexerShim()
    { /* private constructor */ }

    private static SolrIndexerShim theSolrIndexer;

    public static SolrIndexerShim instance()
    {
        if (theSolrIndexer == null) theSolrIndexer = new SolrIndexerShim();
        return(theSolrIndexer);
    }

    private AbstractValueIndexer<?> getOrCreateIndexerFullSpec(String fullSpec)
    {
        if (indexerCache.get().containsKey(fullSpec))
        {
            return(indexerCache.get().get(fullSpec));
        }
        else
        {
            AbstractValueIndexer<?> indexer;
            synchronized (ValueIndexerFactory.instance())
            {
                indexer = ValueIndexerFactory.instance().createValueIndexer("", fullSpec);
                indexerCache.get().put(fullSpec, indexer);
            }
            return(indexer);
        }
    }

    private AbstractValueIndexer<?> getOrCreateIndexerMapped(String tagStr, String map)
    {
        String key = (map == null) ? tagStr : tagStr + ", " +  map;
        return getOrCreateIndexerFullSpec(key);
    }

    private AbstractValueIndexer<?> getOrCreateIndexer(String tagStr, String separator)
    {
        String key = (separator == null) ? tagStr : tagStr + ", join(\""+separator+"\")";
        return getOrCreateIndexerFullSpec(key);
    }

    private AbstractValueIndexer<?> getOrCreateIndexer(String tagStr, int start, int end)
    {
        String key = (start == -1 && end == -1) ? tagStr : tagStr + "[" + start + "-" + end + "]";
        return getOrCreateIndexerFullSpec(key);
    }

    /**
     * Get <code>Collection</code> of Strings as indicated by tagStr. For each field
     * spec in the tagStr that is NOT about bytes (i.e. not a 008[7-12] type fieldspec),
     * the result string is the concatenation of all the specific subfields.
     *
     * @param record -
     *            the marc record object
     * @param tagStr
     *            string containing which field(s)/subfield(s) to use. This is a
     *            series of: marc "tag" string (3 chars identifying a marc
     *            field, e.g. 245) optionally followed by characters identifying
     *            which subfields to use. Separator of colon indicates a
     *            separate value, rather than concatenation. 008[5-7] denotes
     *            bytes 5-7 of the 008 field (0 based counting) 100[a-cf-z]
     *            denotes the bracket pattern is a regular expression indicating
     *            which subfields to include. Note: if the characters in the
     *            brackets are digits, it will be interpreted as particular
     *            bytes, NOT a pattern. 100abcd denotes subfields a, b, c, d are
     *            desired.
     * @param collector
     *            object in which to collect the data from the fields described by
     *            <code>tagStr</code>. A <code>Set</code> will automatically de-dupe
     *            values, a <code>List</code> will allow values to repeat.
     * @throws Exception
     */
    private void getFieldListCollector(Record record, AbstractValueIndexer<?> indexer,  Collection<String> collector)
    {
        try
        {
            indexer.getFieldData(record, collector);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getFieldListCollector(Record record, String tagStr, String mapStr,  Collection<String> collector)
    {
        AbstractValueIndexer<?> indexer = getOrCreateIndexerMapped(tagStr, mapStr);
        getFieldListCollector(record, indexer, collector);
    }


    /**
     * Get Set of Strings as indicated by tagStr. For each field spec in the
     * tagStr that is NOT about bytes (i.e. not a 008[7-12] type fieldspec), the
     * result string is the concatenation of all the specific subfields.
     *
     * @param record -
     *            the marc record object
     * @param tagStr
     *            string containing which field(s)/subfield(s) to use. This is a
     *            series of: marc "tag" string (3 chars identifying a marc
     *            field, e.g. 245) optionally followed by characters identifying
     *            which subfields to use. Separator of colon indicates a
     *            separate value, rather than concatenation. 008[5-7] denotes
     *            bytes 5-7 of the 008 field (0 based counting) 100[a-cf-z]
     *            denotes the bracket pattern is a regular expression indicating
     *            which subfields to include. Note: if the characters in the
     *            brackets are digits, it will be interpreted as particular
     *            bytes, NOT a pattern. 100abcd denotes subfields a, b, c, d are
     *            desired.
     * @return the contents of the indicated marc field(s)/subfield(s), as a set
     *         of Strings.
     * @throws Exception
     */
//    public Set<String> getFieldList(Record record, String tagStr)
//    {
//        Set<String> result = new LinkedHashSet<String>();
//        getFieldListCollector(record, tagStr, null, result);
//        return result;
//    }

    public Set<String> getFieldList(Record record, String tagStr)
    {
        Set<String> result = new LinkedHashSet<String>();
        getFieldListCollector(record, tagStr, null, result);
        return result;
    }

    public Set<String> getMappedFieldList(Record record, String tagStr, String mapStr)
    {
        Set<String> result = new LinkedHashSet<String>();
        getFieldListCollector(record, tagStr, mapStr, result);
        return result;
    }

    /**
     * Get <code>List</code> of Strings as indicated by tagStr. For each field spec in the
     * tagStr that is NOT about bytes (i.e. not a 008[7-12] type fieldspec), the
     * result string is the concatenation of all the specific subfields.
     *
     * @param record -
     *            the marc record object
     * @param tagStr
     *            string containing which field(s)/subfield(s) to use. This is a
     *            series of: marc "tag" string (3 chars identifying a marc
     *            field, e.g. 245) optionally followed by characters identifying
     *            which subfields to use. Separator of colon indicates a
     *            separate value, rather than concatenation. 008[5-7] denotes
     *            bytes 5-7 of the 008 field (0 based counting) 100[a-cf-z]
     *            denotes the bracket pattern is a regular expression indicating
     *            which subfields to include. Note: if the characters in the
     *            brackets are digits, it will be interpreted as particular
     *            bytes, NOT a pattern. 100abcd denotes subfields a, b, c, d are
     *            desired.
     * @return the contents of the indicated marc field(s)/subfield(s).
     * @throws Exception
     */
    public List<String> getFieldListAsList(Record record, String tagStr)
    {
        List<String> result = new ArrayList<String>();
        getFieldListCollector(record, tagStr, null, result);
        return result;
    }

    /**
     * Get all field values specified by tagStr, joined as a single string.
     * @param record - the marc record object
     * @param tagStr string containing which field(s)/subfield(s) to use. This 
     *  is a series of: marc "tag" string (3 chars identifying a marc field, 
     *  e.g. 245) optionally followed by characters identifying which subfields 
     *  to use.
     * @param separator string separating values in the result string
     * @return single string containing all values of the indicated marc
     *         field(s)/subfield(s) concatenated with separator string
     */
    public String getFieldVals(Record record, String tagStr, String separator)
    {
        Set<String> result = getFieldList(record, tagStr);
        return org.solrmarc.tools.Utils.join(result, separator);
    }

    /**
     * Get the first value specified by the tagStr
     * @param record - the marc record object
     * @param tagStr string containing which field(s)/subfield(s) to use. This
     *  is a series of: marc "tag" string (3 chars identifying a marc field,
     *  e.g. 245) optionally followed by characters identifying which subfields
     *  to use.
     * @return first value of the indicated marc field(s)/subfield(s) as a string
     * @throws Exception
     */
    public String getFirstFieldVal(Record record, String tagStr)
    {
        Set<String> result = getFieldList(record, tagStr);
        Iterator<String> iter = result.iterator();
        if (iter.hasNext())
            return iter.next();
        else
            return null;
    }

    /**
     * Get the first field value, which is mapped to another value. If there is
     * no mapping for the value, use the mapping for the empty key, if it
     * exists, o.w., use the mapping for the __DEFAULT key, if it exists.
     * @param record - the marc record object
     * @param mapName - name of translation map to use to xform values
     * @param tagStr - which field(s)/subfield(s) to use
     * @return first value as a string
     */
    public String getFirstFieldVal(Record record, String mapName, String tagStr)
    {
        Set<String> result = getMappedFieldList(record, tagStr, mapName);
        Iterator<String> iter = result.iterator();
        return (iter.hasNext())? iter.next() : null;
    }

    public boolean isControlField(String fieldTag)
    {
        if (fieldTag.matches("00[0-9]"))
        {
            return (true);
        }
        return (false);
    }

    /**
     * Get the specified subfields from the specified MARC field, returned as a
     * set of strings to become lucene document field values
     *
     * @param record     the MARC record object
     * @param fldTag     the field name, e.g. 245
     * @param subfldsStr the string containing the desired subfields
     * @param separator  the separator string to insert between subfield items (if null, a " " will be
     *                   used)
     * @param collector  an object to accumulate the data indicated by <code>fldTag</code> and
     *                   <code>subfldsStr</code>.
     */
    public void getSubfieldDataCollector(Record record, String fldTag, String subfldsStr,
                                                String separator, Collection<String> collector)
    {
        AbstractValueIndexer<?> indexer = getOrCreateIndexer(fldTag+subfldsStr, separator);
        getFieldListCollector(record, indexer, collector);
        return;
    }

    /**
     * Get the specified substring of subfield values from the specified MARC
     * field, returned as  a set of strings to become lucene document field values
     * @param record - the marc record object
     * @param fldTag - the field name, e.g. 008
     * @param subfldStr - the string containing the desired subfields
     * @param beginIx - the beginning index of the substring of the subfield value
     * @param endIx - the ending index of the substring of the subfield value
     * @param collector  an object to accumulate the data indicated by <code>fldTag</code> and
     *                   <code>subfldsStr</code>.
     */
    public void getSubfieldDataCollector(Record record, String fldTag, String subfldStr,
                       int beginIx, int endIx, Collection<String> collector)
    {
        AbstractValueIndexer<?> indexer = getOrCreateIndexer(fldTag+subfldStr, beginIx, endIx);
        getFieldListCollector(record, indexer, collector);
        return;
    }

    /**
     * Get the specified subfields from the specified MARC field, returned as a
     * set of strings to become lucene document field values
     *
     * @param record     the marc record object
     * @param fldTag     the field name, e.g. 245
     * @param subfldsStr the string containing the desired subfields
     * @param separator  the separator string to insert between subfield items
     *                   (if <code>null</code>, a " " will be used)
     * @return           a Set of String, where each string is the concatenated contents of all the
     *                   desired subfield values from a single instance of the <code>fldTag</code>
     */
    public Set<String> getSubfieldDataAsSet(Record record, String fldTag, String subfldsStr, String separator)
    {
        Set<String> result = new LinkedHashSet<String>();
        getSubfieldDataCollector(record, fldTag, subfldsStr, separator, result);
        return result;
    }

    /**
     * Get the specified substring of subfield values from the specified MARC
     * field, returned as  a set of strings to become lucene document field values
     * @param record    the marc record object
     * @param fldTag    the field name, e.g. 008
     * @param subfldStr the string containing the desired subfields
     * @param beginIx   the beginning index of the substring of the subfield value
     * @param endIx     the ending index of the substring of the subfield value
     * @return          the result set of strings
     */
    public Set<String> getSubfieldDataAsSet(Record record, String fldTag, String subfldStr, int beginIx, int endIx)
    {
        Set<String> result = new LinkedHashSet<String>();
        getSubfieldDataCollector(record, fldTag, subfldStr, beginIx, endIx, result);
        return result;
    }

    /**
     * remove trailing punctuation (default trailing characters to be removed)
     *    See org.solrmarc.tools.Utils.cleanData() for details on the
     *     punctuation removal
     * @param record marc record object
     * @param fieldSpec - the field to have trailing punctuation removed
     * @return Set of strings containing the field values with trailing
     *         punctuation removed
     */
    public Set<String> removeTrailingPunct(Record record, String fieldSpec)
    {
        Set<String> result = getFieldList(record, fieldSpec);
        Set<String> newResult = new LinkedHashSet<String>();
        for (String s : result)
        {
            newResult.add(DataUtil.cleanData(s));
        }
        return newResult;
    }

    /**
     * Stub more advanced version of getDate that looks in the 008 field as well as the 260c field
     * this routine does some simple sanity checking to ensure that the date to return makes sense.
     * @param record - the marc record object
     * @return 260c or 008[7-10] or 008[11-14], "cleaned" per org.solrmarc.tools.Utils.cleanDate()
     */

    public String getPublicationDate(final Record record)
    {
        List<String> result = new ArrayList<String>();
        AbstractValueIndexer<?> indexer = getOrCreateIndexerFullSpec("008[7-10]:008[11-14]:260c:264c?(ind2=1||ind2=4),clean, first, " +
                    "map(\"(^|.*[^0-9])((20|1[5-9])[0-9][0-9])([^0-9]|$)=>$2\",\".*[^0-9].*=>\")");
        getFieldListCollector(record, indexer, result);
        return (result.size() == 0) ? "" : result.iterator().next();
    }

    public Set<String> getFullTextUrls(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        AbstractValueIndexer<?> indexer = getOrCreateIndexer("{856uz3}?((ind1 = 4 || (ind1 = 7 & $x startsWith \"http\")) && (ind2 = 0 || (ind2 = 1 )))", "||");
        getFieldListCollector(record, indexer, result);
        return result;
    }

    public Set<String> getSupplUrls(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        AbstractValueIndexer<?> indexer = getOrCreateIndexer("{856uz3}?((ind1 = 4 || (ind1 = 7 & $x startsWith \"http\")) && (ind2 = 2 || (ind2 = 1)))", "||");
        getFieldListCollector(record, indexer, result);
        return result;
    }

    /**
     * extract all the subfields requested in requested marc fields. Each
     * instance of each marc field will be put in a separate result (but the
     * subfields will be concatenated into a single value for each marc field)
     *
     * @param record
     *            marc record object
     * @param fieldSpec -
     *            the desired marc fields and subfields as given in the
     *            xxx_index.properties file
     * @param separator -
     *            the character to use between subfield values in the solr field
     *            contents
     * @return Set of values (as strings) for solr field
     */
    public Set<String> getAllSubfields(final Record record, String fieldSpec, String separator)
    {
        Set<String> result = new LinkedHashSet<String>();
        AbstractValueIndexer<?> indexer = getOrCreateIndexer(fieldSpec, separator);
        getFieldListCollector(record, indexer, result);
        return result;
    }

    /**
     * extract all the subfields requested in requested marc fields. Each
     * instance of each marc field will be put in a separate result (but the
     * subfields will be concatenated into a single value for each marc field)
     *
     * @param record
     *            marc record object
     * @param fieldSpec -
     *            the desired marc fields and subfields as given in the
     *            xxx_index.properties file
     * @param separator -
     *            the character to use between subfield values in the solr field
     *            contents
     * @return Set of values (as strings) for solr field
     */
    public Set<String> getAllAlphaSubfields(final Record record, String fieldSpec, String firstAllJoin)
    {
        Set<String> result = new LinkedHashSet<String>();
        String [] pieces = fieldSpec.split(":");
        String fieldSpecWithAll = Utils.join(pieces, "[a-z]:") + "[a-z]" + ", " + firstAllJoin;
        AbstractValueIndexer<?> indexer = getOrCreateIndexerFullSpec(fieldSpecWithAll);
        getFieldListCollector(record, indexer, result);
        return result;
    }

    /**
     * For each occurrence of a marc field in the fieldSpec list, extract the
     * contents of all subfields except the ones specified, concatenate the
     * subfield contents with a space separator and add the string to the result
     * set.
     *
     * @param record -
     *            the marc record
     * @param fieldSpec -
     *            the marc fields (e.g. 600:655) in which we will grab the
     *            alphabetic subfield contents for the result set. The field may
     *            not be a control field (must be 010 or greater)
     * @return a set of strings, where each string is the concatenated values of
     *         all the alphabetic subfields.
     */
    public Set<String> getAllAlphaExcept(final Record record, String fieldSpec)
    {
        Set<String> result = new LinkedHashSet<String>();
        String [] pieces = fieldSpec.split(":");
        StringBuilder sb = new StringBuilder();
        for (String piece : pieces)
        {
            sb.append(piece.substring(0, 3)).append("[^").append(piece.substring(3)).append("0-9]:");
        }
        sb.setLength(sb.length()-1);
        String fieldSpecWithAll = sb.toString();
        AbstractValueIndexer<?> indexer = getOrCreateIndexerFullSpec(fieldSpecWithAll);
        getFieldListCollector(record, indexer, result);
        return result;
    }

    /**
     * extract all the subfields requested in requested marc fields. Each
     * instance of each marc field will be put in a separate result (but the
     * subfields will be concatenated into a single value for each marc field)
     *
     * @param record
     *            marc record object
     * @param fieldSpec -
     *            the desired marc fields and subfields as given in the
     *            xxx_index.properties file
     * @param separator -
     *            the character to use between subfield values in the solr field
     *            contents
     * @return Set of values (as strings) for solr field
     */
    public List<String> getAllSubfieldsAsList(final Record record, String fieldSpec, String separator)
    {
        List<String> result = new ArrayList<String>();
        AbstractValueIndexer<?> indexer = getOrCreateIndexer(fieldSpec, separator);
        getFieldListCollector(record, indexer, result);
        return result;
    }

    /**
     * Loops through all datafields and creates a field for "all fields"
     * searching. Shameless stolen from Vufind Indexer Custom Code
     *
     * @param record
     *            marc record object
     * @param lowerBoundStr -
     *            the "lowest" marc field to include (e.g. 100). defaults to 100
     *            if value passed doesn't parse as an integer
     * @param upperBoundStr -
     *            one more than the "highest" marc field to include (e.g. 900
     *            will include up to 899). Defaults to 900 if value passed
     *            doesn't parse as an integer
     * @return a string containing ALL subfields of ALL marc fields within the
     *         range indicated by the bound string arguments.
     */
    public String getAllSearchableFields(final Record record, String lowerBoundStr, String upperBoundStr)
    {
        StringBuffer buffer = new StringBuffer("");
        int lowerBound = localParseInt(lowerBoundStr, 100);
        int upperBound = localParseInt(upperBoundStr, 900);

        List<DataField> fields = record.getDataFields();
        for (DataField field : fields)
        {
            // Get all fields starting with the 100 and ending with the 839
            // This will ignore any "code" fields and only use textual fields
            int tag = localParseInt(field.getTag(), -1);
            if ((tag >= lowerBound) && (tag < upperBound))
            {
                // Loop through subfields
                List<Subfield> subfields = field.getSubfields();
                for (Subfield subfield : subfields)
                {
                    if (buffer.length() > 0)
                        buffer.append(" ");
                    buffer.append(subfield.getData());
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Loops through all datafields and creates a field for "all fields"
     * searching. Shameless stolen from Vufind Indexer Custom Code
     *
     * @param record
     *            marc record object
     * @param lowerBoundStr -
     *            the "lowest" marc field to include (e.g. 100). defaults to 100
     *            if value passed doesn't parse as an integer
     * @param upperBoundStr -
     *            one more than the "highest" marc field to include (e.g. 900
     *            will include up to 899). Defaults to 900 if value passed
     *            doesn't parse as an integer
     * @return a Set of strings containing ALL subfields of ALL marc fields within the
     *         range indicated by the bound string arguments, with one string for each field encountered.
     */
    public Set<String> getAllSearchableFieldsAsSet(final Record record, String lowerBoundStr, String upperBoundStr)
    {
        Set<String> result = new LinkedHashSet<String>();
        int lowerBound = localParseInt(lowerBoundStr, 100);
        int upperBound = localParseInt(upperBoundStr, 900);

        List<DataField> fields = record.getDataFields();
        for (DataField field : fields)
        {
            // Get all fields starting with the 100 and ending with the 839
            // This will ignore any "code" fields and only use textual fields
            int tag = localParseInt(field.getTag(), -1);
            if ((tag >= lowerBound) && (tag < upperBound))
            {
                // Loop through subfields
                StringBuffer buffer = new StringBuffer("");
                List<Subfield> subfields = field.getSubfields();
                for (Subfield subfield : subfields)
                {
                    if (buffer.length() > 0)
                        buffer.append(" ");
                    buffer.append(subfield.getData());
                }
                result.add(buffer.toString());
            }
        }
        return result;
    }

    /**
     * Get the title (245ab) from a record, without non-filing chars as
     * specified in 245 2nd indicator, and lowercased.
     * @param record - the marc record object
     * @return 245a and 245b values concatenated, with trailing punct removed,
     *         and with non-filing characters omitted. Null returned if no
     *         title can be found.
     *
     * @see SolrIndexerShim#getTitle
     */
    public String getSortableTitle(Record record)
    {
        List<String> result = new ArrayList<String>();
        AbstractValueIndexer<?> indexer = getOrCreateIndexerFullSpec("245abkp,titleSortLower,first");
        getFieldListCollector(record, indexer, result);
        return (result.size() == 0) ? "" : result.iterator().next();
    }

    /**
     * return an int for the passed string
     * @param str
     * @param defValue - default value, if string doesn't parse into int
     */
    private int localParseInt(String str, int defValue)
    {
        int value = defValue;
        try
        {
            value = Integer.parseInt(str);
        }
        catch (NumberFormatException nfe)
        {
            // provided value is not valid numeric string
            // Ignoring it and moving happily on.
        }
        return (value);
    }

    public List<VariableField> getFieldSetMatchingTagList(Record record, String tagList)
    {
        String tags[] = tagList.split(":");
        for (int i = 0; i < tags.length; i++)
        {
            String tag = tags[i].substring(0, 3);
            if (tag == "LNK") tag = tags[i].substring(0, 6);
            tags[i] = tag;
        }
        return(record.getVariableFields(tags));
    }

    /**
     * public interface callable from custom indexing scripts to
     * load the translation map into transMapMap
     * Simply implements a stub that calls the createMultiValueMapping method
     * @param translationMapSpec the specification of a translation map -
     *   could be name of a _map.properties file, or some subset of entries in a
     *   _map.properties file
     * @return the name of the translation map to be used in a subsequent call to FindMap
     */
    public String loadTranslationMap(String translationMapSpec)
    {
        if (findMap(translationMapSpec) == null)
        {
            AbstractMultiValueMapping map = ValueIndexerFactory.instance().createMultiValueMapping(translationMapSpec);
            transMapMap.putIfAbsent(translationMapSpec, map);
        }
        return(translationMapSpec);
    }

    public String loadTranslationMap(String ignore, String translationMapSpec)
    {
        return(loadTranslationMap(translationMapSpec));
    }

    /**
     * Get the appropriate Map object from populated transMapMap
     * @param mapName the name of the translation map to find
     * @return populated Map object
     */
    public Object findMap(String mapName)
    {
        if (transMapMap.containsKey(mapName))
            return(transMapMap.get(mapName));

        return null;
    }

    public Collection<String> remap(Collection<String> valuesToMap, Object translationMap, boolean b) throws Exception
    {
        if (translationMap instanceof AbstractMultiValueMapping)
        {
            AbstractMultiValueMapping map = (AbstractMultiValueMapping) translationMap;
            return(map.map(valuesToMap));
        }
        return null;
    }

    public String remap(String valueToMap, Object translationMap, boolean b) throws Exception
    {
        if (translationMap instanceof AbstractMultiValueMapping)
        {
            AbstractMultiValueMapping map = (AbstractMultiValueMapping) translationMap;
            return(map.mapSingle(valueToMap));
        }
        return null;
    }

    public String getDataFromVariableField(VariableField vf, String subfldTags, String separator, boolean cleanIt)
    {
        if (subfldTags.length() > 1 && !subfldTags.startsWith("["))
            subfldTags = '[' + subfldTags + ']';
        Pattern subfieldPattern = Pattern.compile(subfldTags.length() == 0 ? "." : subfldTags);
        DataField marcField = (DataField) vf;
        StringBuffer buffer = new StringBuffer("");
        List<Subfield> subfields = marcField.getSubfields();
        for (Subfield subfield : subfields)
        {
            Matcher matcher = subfieldPattern.matcher("" + subfield.getCode());
            if (matcher.matches())
            {
                if (buffer.length() > 0)
                    buffer.append(separator != null ? separator : " ");
                buffer.append(subfield.getData().trim());
            }
        }
        if (buffer.length() > 0)
            return(cleanIt ? DataUtil.cleanData(buffer.toString()) : buffer.toString());
        else
            return(null);
    }


}
