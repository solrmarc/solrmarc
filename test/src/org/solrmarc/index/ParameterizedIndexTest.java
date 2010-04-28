package org.solrmarc.index;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ParameterizedIndexTest
{
    String config;
    String recordFilename;
    String fieldToCheck;
    String expectedValue;
    static String dataDirectory;
    static String dataFile;
    
    public ParameterizedIndexTest(String config, String recordFilename, String fieldToCheck, String expectedValue)
    {
        this.config = config;
        this.recordFilename = recordFilename;
        this.fieldToCheck = fieldToCheck;
        this.expectedValue = expectedValue;
    }
    
    @Test
    /**
     * for each line specified in the test file 
     *    (see org.solrmarc.index.indexValues javadoc below)
     * run the indicated test data file through MarcMappingOnly, which will get the
     * marc to solr mappings specified in yourSite_index.properties file, and
     * look for the indicated solr field value in the indicated solr field.
     */
    public void verifyIndexingResults() throws Exception 
    {
        MarcMappingOnly marcMappingTest = new MarcMappingOnly();
        marcMappingTest.init(new String[]{config, "id"});
        String recordToLookAt = null;  // null means just get the first record from the named file
        if (recordFilename.matches("[^(]*[(][^)]*[)]"))
        {
            String recParts[] = recordFilename.split("[()]");
            recordFilename = recParts[0];
            recordToLookAt = recParts[1];
        }
        String fullRecordFilename = dataDirectory + File.separator + recordFilename;
        Object solrFldValObj;
        if (fieldToCheck.matches("^[0-9].*"))
        {
            solrFldValObj = marcMappingTest.lookupRawRecordValue(recordToLookAt, fullRecordFilename, fieldToCheck);
        }
        else 
        {
            Map<String, Object> solrFldName2ValMap = marcMappingTest.getIndexMapForRecord(recordToLookAt, fullRecordFilename);
            solrFldValObj = solrFldName2ValMap.get(fieldToCheck);
        }
        String expected[];
        if (expectedValue.length() > 0)
            expected = expectedValue.split("[|]");
        else
            expected = new String[0];
        String received[] = null;
        if (solrFldValObj == null)
        {
            if (expected.length != 0)
            {
                System.out.println("No value assigned for Solr field " + fieldToCheck + " in Solr document " + recordFilename);
                fail("No value assigned for Solr field " + fieldToCheck + " in Solr document " + recordFilename);
            }
            received = new String[0];
        }
        if (solrFldValObj instanceof String)
        {
            received = new String[1];
            received[0] = solrFldValObj.toString();
        }
        else if (solrFldValObj instanceof Collection)
        {
            received = new String[((Collection<Object>)solrFldValObj).size()];
            int i = 0;
            for (Object fldVal : (Collection<Object>) solrFldValObj)
            {
                received[i++] = fldVal.toString();
            }
        }
        
        for (String expect : expected)
        {
            boolean foundIt = false;
            for (String receive : received)
            {
                if (expect.equals(receive))
                {    
                    foundIt = true;
                    break;
                }
                // System.out.println("DEBUG: value is [" + fldVal + "]");
            }
            if (!foundIt)
            {
                System.out.println("Solr field " + fieldToCheck + " did not have any value matching " + expect);
                for (String receive : received)
                {
                    if (expect.equals(receive))
                    {    
                        foundIt = true;
                        break;
                    }
                    System.out.println("DEBUG: found value [" + receive + "]");
                }
            }
            assertTrue("Solr field " + fieldToCheck + " did not have any value matching " + expect, foundIt);
        }
        for (String receive : received)
        {
            boolean foundIt = false;
            for (String expect : expected)
            {
                if (expect.equals(receive))
                {    
                    foundIt = true;
                    break;
                }
                // System.out.println("DEBUG: value is [" + fldVal + "]");
            }
            if (!foundIt)
            {
                System.out.println("Solr field " + fieldToCheck + " did not have any value matching " + receive);
            }
            assertTrue("Solr field " + fieldToCheck + " had extra unexpected value " + receive, foundIt);
        }
        System.out.println(config + " : " + recordFilename + " : " + fieldToCheck + " --> " + expectedValue);
    }
    
    @Parameters
    /**
     * reads in the file at test.data.path/test.data.file (usually 
     *    test.data.path = yourSiteDirectory/test/data
     *    test.data.file = indextest.txt
	 *   and puts the tests indicated there into a collection of arrays, where 
	 *   each item in the collection has this structure:
     *     it[0] = config.properties filed
     *     it[1] = name of file containing marc records to be indexed for test
     *     it[2] = name of solr field to be checked in resulting solr doc
     *     it[3] = value expected in solr field
     */
    public static Collection indexValues() throws Exception
    {
        dataDirectory = System.getProperty("test.data.path");
        dataFile = System.getProperty("test.data.file");
        String fullIndexTestFilename = dataDirectory + File.separator + dataFile;
        File file = new File(fullIndexTestFilename);
        BufferedReader rIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        List result = new LinkedList();
        while (( line = rIn.readLine()) != null)
        {
        	if (line.startsWith("#") || line.trim().length() == 0) continue;
            String split[] = line.split(", ", 4);
            if (split.length == 4) 
                result.add(split);
        }
        return(result);
    
    }
}
