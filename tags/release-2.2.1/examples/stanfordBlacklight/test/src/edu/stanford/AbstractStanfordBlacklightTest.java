package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import static org.junit.Assert.fail;
import org.solrmarc.testUtils.IndexTest;
import org.solrmarc.testUtils.SolrFieldMappingTest;

/**
 * Site Specific code used for testing the Stanford Blacklight index
 * @author Naomi Dushay
 */
public abstract class AbstractStanfordBlacklightTest extends IndexTest {

	/** testDataParentPath is used for mapping tests - full path is needed */
    String testDataParentPath = null;
	
	/** SolrFieldMappingTest object to be used in specific tests */
	protected SolrFieldMappingTest solrFldMapTest = null;

    
	// hardcodings below are only used when the tests are invoked without the
	//  properties set
	// the properties ARE set when the tests are invoke via ant.
	{
        // !!! NOTE:  this one is local and must be modified for your installation !!!
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
        {
            solrPath = "home" + File.separator + "solrmarc" + File.separator
                    + "jetty" + File.separator + "solr";
            System.setProperty("solr.path", solrPath);
        }

//        String solrmarcPath = System.getProperty("solrmarc.path");
//        if (solrmarcPath == null) {
//            solrmarcPath = new File("lib" + File.separator + "solrmarc").getAbsolutePath();
//            System.setProperty("solrmarc.path", solrmarcPath);
//        }
//		
//		String solrmarcSitePath = System.getProperty("solrmarc.site.path");
//		if (solrmarcSitePath == null) {
//			solrmarcSitePath = new File("examples" + File.separator + "stanfordBlacklight").getAbsolutePath(); 
//            System.setProperty("solrmarc.site.path", solrmarcSitePath);
//		}
//		
        String configPropDir = System.getProperty("test.config.dir");
//        System.err.println("configPropDir = "+ configPropDir);
//        if (configPropDir == null)
//            configPropDir = solrmarcSitePath;
        
        String configPropFile = System.getProperty("test.config.file");
//        System.err.println("configPropFile = "+ configPropFile);
		if (configPropFile == null) {
		    configPropFile = new File(configPropDir, "sw_config.properties").getAbsolutePath();
            System.setProperty("test.config.file", configPropFile);
		}
//        System.err.println("configPropFile = "+ configPropFile);
		
		testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
        {
            testDataParentPath = System.getProperty("test.data.parent.path");
            if (testDataParentPath == null)
                testDataParentPath = "examples" + File.separator
                        + "stanfordBlacklight" + File.separator + "test"
                        + File.separator + "data";

            // testDir = "test";
            // testDataParentPath = testDir + File.separator + "data";
//    		testDataPath = testDataParentPath + File.separator + "allfieldsTests.mrc";

            System.setProperty("test.data.path", testDataParentPath);
        }
		
		String solrDataDir = System.getProperty("solr.data.dir");
		if (solrDataDir == null)
			solrDataDir = solrPath + File.separator + "data";
	}

	/**
	 * initialization for mapping tests
	 */
	public void mappingTestInit() 
	{
		docIDfname = "id";
		
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
        {
            fail("property test.data.path must be defined for the tests to run");
        }
		String anyTestFile = new File(testDataParentPath, "pubDateTests.mrc").getAbsolutePath();

		// these properties must be set or MarcHandler can't initialize properly
		System.setProperty("marc.source", "FILE");
		// needed to get through initialization; overridden in individual tests
		System.setProperty("marc.path", anyTestFile);
        String testConfigFname = System.getProperty("test.config.file");

		solrFldMapTest = new SolrFieldMappingTest(testConfigFname, docIDfname);
	}



	/**
	 * creates an index from the indicated test file, and initializes 
	 *  necessary variables
	 */
	public void createIxInitVars(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException 
	{
		docIDfname = "id";

		String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
            fail("property solr.path must be defined for the tests to run");

        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");

        String testConfigFname = System.getProperty("test.config.file");
        if (testConfigFname == null)
            fail("property test.config.file must be defined for the tests to run");

        createIxInitVars(testConfigFname, solrPath, null, testDataParentPath, testDataFname);

//		createNewTestIndex(testDataParentPath + File.separator + testDataFname, configPropFile, solrPath, solrDataDir, solrmarcPath, siteSpecificPath);
//		solrCore = getSolrCore(solrPath, solrDataDir);
//		sis = getSolrIndexSearcher(solrCore);
	}
	
}
