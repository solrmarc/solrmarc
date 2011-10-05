package org.solrmarc.tools;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.solrmarc.testUtils.IndexTest;
import org.xml.sax.SAXException;


public class IndexSmokeTest extends IndexTest
{
    private final String testDataFname = "selectedRecs.mrc";
    /**
     * creates an index from the indicated test file, and initializes 
     *  necessary variables
     */
    public void createIxInitVars(String testDataFname) 
    {
        docIDfname = "id";

        String solrPath = System.getProperty("solr.path");
        String solrDataDir = System.getProperty("solr.data.dir");
        if (solrPath == null)
            fail("property solr.path must be defined for the tests to run");

        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");

        String testConfigFname = System.getProperty("test.config.file");
        if (testConfigFname == null)
            fail("property test.config.file must be defined for the tests to run");

        createIxInitVars(testConfigFname, solrPath, solrDataDir, testDataParentPath, testDataFname);

//      createNewTestIndex(testDataParentPath + File.separator + testDataFname, configPropFile, solrPath, solrDataDir, solrmarcPath, siteSpecificPath);
//      solrCore = getSolrCore(solrPath, solrDataDir);
//      sis = getSolrIndexSearcher(solrCore);
    }
    /**
     * Test assignment of Book format
     *   includes monographic series
     */
@Test
    public final void testForSmoke() 
    {
        createIxInitVars(testDataFname);
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");

        MarcStreamReader reader = null;
        try
        {
            reader = new MarcStreamReader(new FileInputStream(testDataParentPath + File.separator + testDataFname));
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (reader != null && reader.hasNext())
        {
            Record rec = reader.next();
            String id = rec.getControlNumber();
            if (id != null)
            {
                assertDocPresent(id);
            }
        }
        System.out.println("Test testForSmoke is successful");
    }

}
