package org.solrmarc.index;

import java.util.*;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;

/**
 * Utility Test class to determine if a marc record will map data to the Solr
 * fields as expected.
 * 
 * @author Naomi Dushay
 * @version $Id$
 * 
 */
public class SolrFieldMappingTest
{
    // Initialize logging category
    // static Logger logger =
    // Logger.getLogger(SolrFieldMappingTest.class.getName());

    /** marcMappingTest instance used to do the field mapping */
    private MarcMappingOnly marcMappingTest = null;

    /**
     * Constructor
     * 
     * @param configPropsName -
     *            name of xxx _config.properties file
     * @param idFldName -
     *            name of unique key field in Solr document
     */
    public SolrFieldMappingTest(String configPropsName, String idFldName)
    {
        marcMappingTest = new MarcMappingOnly(new String[] { configPropsName, idFldName });
    }

    /**
     * assert that when the file of marc records is processed, there will be a
     * Solr document with the given id containing at least one instance of the
     * expected Solr field with the expected value
     * 
     * @param mrcFileName -
     *            absolute path of file of marc records (name must end in .mrc
     *            or .marc or .xml)
     * @param solrDocId -
     *            value of Solr unique key field for the Solr document to
     *            checked
     * @param expectedFldName -
     *            the name of the Solr field to be checked
     * @param expectedFldVal -
     *            the value expected to be in at least one instance of the Solr
     *            field for the indicated Solr document
     */
    public void assertSolrFldValue(String mrcFileName, String solrDocId,
            String expectedFldName, String expectedFldVal)
    {
        Map<String, Object> solrFldName2ValMap = marcMappingTest.getIndexMapForRecord(solrDocId, mrcFileName);

        Object solrFldValObj = solrFldName2ValMap.get(expectedFldName);
        if (solrFldValObj == null)
            fail("No value assigned for Solr field " + expectedFldName + " in Solr document " + solrDocId);
        if (solrFldValObj instanceof String)
            assertEquals("didn't get expected value for Solr field " + expectedFldName + " -- ", expectedFldVal, solrFldValObj.toString());
        else if (solrFldValObj instanceof Collection)
        {
            // look for a match of at least one of the values
            boolean foundIt = false;
            for (String fldVal : (Collection<String>) solrFldValObj)
            {
                if (fldVal.equals(expectedFldVal))
                    foundIt = true;
                // System.out.println("DEBUG: value is [" + fldVal + "]");
            }
            assertTrue("Solr field " + expectedFldName + " did not have any value matching " + expectedFldVal, foundIt);
        }
    }

    /**
     * assert that when the file of marc records is processed, the Solr document
     * with the given id does NOT contain an instance of the indicated field
     * with the indicated value
     * 
     * @param mrcFileName -
     *            absolute path of file of marc records (name must end in .mrc
     *            or .marc or .xml)
     * @param solrDocId -
     *            value of Solr unique key field for the Solr document to
     *            checked
     * @param expectedFldName -
     *            the name of the Solr field to be checked
     * @param expectedFldVal -
     *            the value that should be in NO instance of the Solr field for
     *            the indicated Solr document
     */
    public void assertSolrFldHasNoValue(String mrcFileName, String solrDocId,
            String expectedFldName, String expectedFldVal)
    {
        Map<String, Object> solrFldName2ValMap = marcMappingTest .getIndexMapForRecord(solrDocId, mrcFileName);

        Object solrFldValObj = solrFldName2ValMap.get(expectedFldName);
        if (solrFldValObj instanceof String)
            assertFalse("Solr field " + expectedFldName
                    + " unexpectedly has value [" + expectedFldVal + "]", solrFldValObj.toString().equals(expectedFldVal));
        else if (solrFldValObj instanceof Collection)
        {
            // make sure none of the values match
            for (String fldVal : (Collection<String>) solrFldValObj)
            {
                if (fldVal.equals(expectedFldVal))
                    fail("Solr field " + expectedFldName + " unexpectedly has value [" + expectedFldVal + "]");
            }
        }
    }
}
