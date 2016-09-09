package org.solrmarc.index;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.extractor.impl.custom.Mixin;

@SuppressWarnings("deprecation")
public class SolrIndexer implements Mixin
{
    private static SolrIndexer fakeInstanceToMakeScriptsWork = new SolrIndexer();  
    public static SolrIndexer instance()
    {
        return fakeInstanceToMakeScriptsWork;
    }
    private  SolrIndexer() 
    { /* Do-Nothing constructor, for fake Instance To Make Scripts Work */ }
   
    public  SolrIndexer(final String propertiesMapFile, final String[] propertyDirs) 
    { /* Backwards compatibility constructor, the parameters are all ignored */ }

    public void getFieldListCollector(Record record, String tagStr, String mapStr,  Collection<String> collector)
    {
        SolrIndexerShim.instance().getFieldListCollector(record, tagStr, mapStr, collector);
    }
    
    public Set<String> getFieldList(Record record, String tagStr)
    {
        return SolrIndexerShim.instance().getFieldList(record, tagStr);
    }
   
    public Set<String> getMappedFieldList(Record record, String tagStr, String mapStr)
    {
        return SolrIndexerShim.instance().getMappedFieldList(record, tagStr, mapStr);
    }

    public List<String> getFieldListAsList(Record record, String tagStr) 
    {
        return SolrIndexerShim.instance().getFieldListAsList(record, tagStr);
    }

    public String getFirstFieldVal(Record record, String tagStr) 
    {
        return SolrIndexerShim.instance().getFirstFieldVal(record, tagStr);
    }

    public String getFirstFieldVal(Record record, String mapName, String tagStr)
    {
        return SolrIndexerShim.instance().getFirstFieldVal(record, mapName, tagStr);
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
        SolrIndexerShim.instance().getSubfieldDataCollector(record, fldTag, subfldsStr, separator, collector);
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
        SolrIndexerShim.instance().getSubfieldDataCollector(record, fldTag, subfldStr, beginIx, endIx, collector);
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
        return SolrIndexerShim.instance().getSubfieldDataAsSet(record, fldTag, subfldsStr, separator);
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
        return SolrIndexerShim.instance().getSubfieldDataAsSet(record, fldTag, subfldStr, beginIx, endIx);
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
        return SolrIndexerShim.instance().removeTrailingPunct(record, fieldSpec);
    }

    /**
     * Stub more advanced version of getDate that looks in the 008 field as well as the 260c field
     * this routine does some simple sanity checking to ensure that the date to return makes sense. 
     * @param record - the marc record object
     * @return 260c or 008[7-10] or 008[11-14], "cleaned" per org.solrmarc.tools.Utils.cleanDate()
     */
    
    public String getPublicationDate(final Record record)
    {
        return SolrIndexerShim.instance().getPublicationDate(record);
    }

    public Set<String> getFullTextUrls(Record record)
    {
        return SolrIndexerShim.instance().getFullTextUrls(record);
    }

    public Set<String> getSupplUrls(Record record)
    {
        return SolrIndexerShim.instance().getSupplUrls(record);
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
        return SolrIndexerShim.instance().getAllSubfields(record, fieldSpec, separator);
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
        return SolrIndexerShim.instance().getAllAlphaSubfields(record, fieldSpec, firstAllJoin);
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
        return SolrIndexerShim.instance().getAllSubfieldsAsList(record, fieldSpec, separator);
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
        return SolrIndexerShim.instance().getAllSearchableFields(record, lowerBoundStr, upperBoundStr);
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
        return SolrIndexerShim.instance().getAllSearchableFieldsAsSet(record, lowerBoundStr, upperBoundStr);
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
        return SolrIndexerShim.instance().getSortableTitle(record);
    }

    public List<VariableField> getFieldSetMatchingTagList(Record record, String tagList)
    {
        return SolrIndexerShim.instance().getFieldSetMatchingTagList(record, tagList);
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
        return SolrIndexerShim.instance().loadTranslationMap(translationMapSpec);
    }
    
    public String loadTranslationMap(String ignore, String translationMapSpec) 
    {
        return SolrIndexerShim.instance().loadTranslationMap(ignore, translationMapSpec);
    }
    
    /**
     * Get the appropriate Map object from populated transMapMap
     * @param mapName the name of the translation map to find
     * @return populated Map object
     */
    public Object findMap(String mapName)
    {
        return SolrIndexerShim.instance().loadTranslationMap(mapName);
    }

    public Collection<String> remap(Collection<String> valuesToMap, Object translationMap, boolean b) throws Exception
    {
        return SolrIndexerShim.instance().remap(valuesToMap, translationMap, b);
    }
    
    public String remap(String valueToMap, Object translationMap, boolean b) throws Exception
    {
        return SolrIndexerShim.instance().remap(valueToMap, translationMap, b);
    }

    public String getDataFromVariableField(VariableField vf, String subfldTags, String separator, boolean cleanIt)
    {
        return SolrIndexerShim.instance().getDataFromVariableField(vf, subfldTags, separator, cleanIt);
    }
}
