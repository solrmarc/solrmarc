package org.solrmarc.index;

import java.util.*;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;

/**
 *  Utility Test class to determine if a marc record will map data to the 
 *   solr fields as expected.
 * @author Naomi Dushay
 * @version $Id$
 *
 */
public class SolrFieldMappingTest 
{
     // Initialize logging category
//    static Logger logger = Logger.getLogger(SolrFieldMappingTest.class.getName());


    /** marcMappingTest instance used to do the field mapping */
	private MarcMappingTest marcMappingTest = null;


	/**
     *  Constructor
     * @param configPropsName - name of xxx _config.properties file
     * @param idFldName - name of unique key field in solr document
     */
    public SolrFieldMappingTest(String configPropsName, String idFldName) 
    {
    	marcMappingTest = new MarcMappingTest(new String[] {configPropsName, idFldName});
    }

    /**
     * assert that when the file of marc records is processed, there will be 
     *  a solr document with the given id containing at least one instance of 
     *   the expected solr field with the expected value
     * @param mrcFileName - absolute path of file of marc records (name must end in .mrc or .marc or .xml)
     * @param solrDocId - value of solr unique key field for the solr document to checked
     * @param expectedFldName - the name of the solr field to be checked
     * @param expectedFldVal - the value expected to be in at least one instance of the solr field for the indicated solr document
     */
    public void assertSolrFldHasValue(String mrcFileName, String solrDocId, String expectedFldName, String expectedFldVal) 
    {
    	Map<String,Object> solrFldName2ValMap = marcMappingTest.getIndexMapForRecord(solrDocId, mrcFileName);
    	
        Object solrFldValObj = solrFldName2ValMap.get(expectedFldName);
        if (solrFldValObj instanceof String)
        	assertEquals("didn't get expected value for solr field " + expectedFldName, solrFldValObj.toString(), expectedFldVal);
        else if (solrFldValObj instanceof Collection)
        {
        	// look for a match of at least one of the values
        	boolean foundIt = false;
        	for (String fldVal : (Collection<String>) solrFldValObj) 
        	{
        		if (fldVal.equals(expectedFldVal))
        			foundIt = true;
        	}
        	assertTrue("solr field " + expectedFldName + " did not have any value matching " + expectedFldVal, foundIt);
        }
    }    

}
