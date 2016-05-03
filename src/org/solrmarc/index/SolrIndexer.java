package org.solrmarc.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
//import org.solrmarc.tools.Utils;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.fieldmatch.FieldMatch;
import playground.solrmarc.index.specification.AbstractSpecificationFactory;
import playground.solrmarc.index.specification.Specification;

/**
 * class SolrIndexer
 * 
 * This class exists solely for backwards compatibility purposes.  The intention is that if a previous custom function
 * was being used, one that provides the same functionality can be found here.  Furthermore if there were many helper functions
 * that could have been used to create your own custom indexing functions those helper functions should be found here as well.
 * 
 * In most cases the methods found here are merely shims to translate the desired method to use the newer functionality that 
 * is now available.
 * 
 * 
 * @author rh9ec
 *
 */


public class SolrIndexer
{
    static Map<String, Specification> specCache = new HashMap<String, Specification>(); 
    
    private static Specification getOrCreateSpecification(String tagStr)
    {
        if (specCache.containsKey(tagStr))
        {
            return(specCache.get(tagStr));
        }
        else
        {
            Specification spec = AbstractSpecificationFactory.createSpecification(tagStr);
            specCache.put(tagStr,  spec);
            return(spec);
        }
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
    public static void getFieldListCollector(Record record, String tagStr,  Collection<String> collector)
    {
        Specification spec = getOrCreateSpecification(tagStr);
        for (FieldMatch fm : spec.getFieldMatches(record))
        {
            try
            {
                fm.addValuesTo(collector);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return;
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
    public static Set<String> getFieldList(Record record, String tagStr)
    {
        Set<String> result = new LinkedHashSet<String>();
        getFieldListCollector(record, tagStr, result);
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
    public static List<String> getFieldListAsList(Record record, String tagStr) 
    {
        List<String> result = new ArrayList<String>();
        getFieldListCollector(record, tagStr, result);
        return result;
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
    public static String getFirstFieldVal(Record record, String tagStr) 
    {
        Set<String> result = getFieldList(record, tagStr);
        Iterator<String> iter = result.iterator();
        if (iter.hasNext())
            return iter.next();
        else
            return null;
    }

    public static boolean isControlField(String fieldTag)
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
    public static void getSubfieldDataCollector(Record record, String fldTag, String subfldsStr, 
                                                String separator, Collection<String> collector)
    {
        // Process Leader
        if (fldTag.equals("000"))
        {
            collector.add(record.getLeader().toString());
            return;
        }

        // Loop through Data and Control Fields
        // int iTag = new Integer(fldTag).intValue();
        List<VariableField> varFlds = record.getVariableFields(fldTag);
        for (VariableField vf : varFlds)
        {
            if (!isControlField(fldTag) && subfldsStr != null)
            {
                // DataField
                DataField dfield = (DataField) vf;

                if (subfldsStr.length() > 1 || separator != null)
                {
                    // concatenate subfields using specified separator or space
                    StringBuffer buffer = new StringBuffer("");
                    List<Subfield> subFlds = dfield.getSubfields();
                    for (Subfield sf : subFlds)
                    {
                        if (subfldsStr.indexOf(sf.getCode()) != -1)
                        {
                            if (buffer.length() > 0)
                                buffer.append(separator != null ? separator : " ");
                            buffer.append(sf.getData().trim());
                        }
                    }
                    if (buffer.length() > 0)
                        collector.add(buffer.toString());
                }
                else
                {
                    // get all instances of the single subfield
                    List<Subfield> subFlds = dfield.getSubfields(subfldsStr.charAt(0));
                    for (Subfield sf : subFlds)
                    {
                        collector.add(sf.getData().trim());
                    }
                }
            }
            else
            {
                // Control Field
                collector.add(((ControlField) vf).getData().trim());
            }
        }
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
    public static void getSubfieldDataCollector(Record record, String fldTag, String subfldStr, 
                       int beginIx, int endIx, Collection<String> collector)
    {
        // Process Leader
        if (fldTag.equals("000"))
        {
            collector.add(record.getLeader().toString().substring(beginIx, endIx));
            return;
        }

        // Loop through Data and Control Fields
        List<VariableField> varFlds = record.getVariableFields(fldTag);
        for (VariableField vf : varFlds)
        {
            if (!isControlField(fldTag) && subfldStr != null)
            {
                // Data Field
                DataField dfield = (DataField) vf;
                if (subfldStr.length() > 1)
                {
                    // automatic concatenation of grouped subfields
                    StringBuffer buffer = new StringBuffer("");
                    List<Subfield> subFlds = dfield.getSubfields();
                    for (Subfield sf : subFlds)
                    {
                        if (subfldStr.indexOf(sf.getCode()) != -1 && 
                                sf.getData().length() >= endIx)
                        {
                            if (buffer.length() > 0)
                                buffer.append(" ");
                            buffer.append(sf.getData().substring(beginIx, endIx));
                        }
                    }
                    collector.add(buffer.toString());
                }
                else
                {
                    // get all instances of the single subfield
                    List<Subfield> subFlds = dfield.getSubfields(subfldStr.charAt(0));
                    for (Subfield sf : subFlds)
                    {
                        if (sf.getData().length() >= endIx)
                            collector.add(sf.getData().substring(beginIx, endIx));
                    }
                }
            }
            else  // Control Field
            {
                String cfldData = ((ControlField) vf).getData();
                if (cfldData.length() >= endIx)
                    collector.add(cfldData.substring(beginIx, endIx));
            }
        }
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
    public static Set<String> getSubfieldDataAsSet(Record record, String fldTag, String subfldsStr, String separator)
    {
        Set<String> result = new LinkedHashSet<String>();
        SolrIndexer.getSubfieldDataCollector(record, fldTag, subfldsStr, separator, result);
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
    public static Set<String> getSubfieldDataAsSet(Record record, String fldTag, String subfldStr, int beginIx, int endIx)
    {
        Set<String> result = new LinkedHashSet<String>();
        SolrIndexer.getSubfieldDataCollector(record, fldTag, subfldStr, beginIx, endIx, result);
        return result;
    }
 

}
