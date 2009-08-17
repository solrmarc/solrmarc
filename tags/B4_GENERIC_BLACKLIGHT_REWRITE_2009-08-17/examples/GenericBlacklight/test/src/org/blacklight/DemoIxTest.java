package org.blacklight;

import static org.junit.Assert.fail;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.solrmarc.index.IndexTest;

/**
 * Site Specific code used for testing the Generic Blacklight example
 * @author Naomi Dushay
 */
public abstract class DemoIxTest extends IndexTest {

	// Note:  the hardcodings below are only used when the tests are
	//  invoked without the properties set
	//   the properties ARE set when the tests are invoke via ant.
	{
        solrmarcPath = System.getProperty("solrmarc.path");
        if (solrmarcPath == null)
            solrmarcPath = new File("lib" + File.separator + "solrmarc").getAbsolutePath();
		
		siteSpecificPath = System.getProperty("solrmarc.site.path");
		if (siteSpecificPath == null)
			siteSpecificPath = new File("examples" + File.separator + "GenericBlacklight").getAbsolutePath(); 
		
        String configPropDir = System.getProperty("test.config.dir");
        if (configPropDir == null)
            configPropDir = siteSpecificPath;
        
        configPropFile = System.getProperty("test.config.file");
		if (configPropFile == null)
		    configPropFile = configPropDir + File.separator + "bibix_config.properties";
		
		String ngdeDir = File.separator + "Users" + File.separator + "naomi" + 
        					File.separator + "NGDE";
		solrPath = System.getProperty("solr.path");
		if (solrPath == null)
//			solrPath = siteSpecificPath + File.separator + "solr";
		    solrPath = ngdeDir + File.separator + "blacklight" + 
		    			File.separator + "bl-demo" + File.separator + "jetty" +
		                File.separator + "solr";
		
		
		testDataParentPath = System.getProperty("test.data.parent.path");
		if (testDataParentPath == null)
			testDataParentPath = "examples" + 
						File.separator + "GenericBlacklight" + 
        				File.separator + "test" + File.separator + "data";
		
//        testDir = "test";
//        testDataParentPath = testDir + File.separator + "data";
        testDataPath = testDataParentPath + File.separator + "test_data.utf8.mrc";
		
		solrDataDir = System.getProperty("solr.data.dir");
		if (solrDataDir == null)
			solrDataDir = solrPath + File.separator + "data";
		
	}

	
	public void createIxInitVars(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException 
	{
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
        {
            fail("property solr.path must be defined for the tests to run" );
        }

        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
        {
            fail("property test.data.path must be defined for the tests to run" );
        }
        createIxInitVars(null, solrPath, null, testDataParentPath, testDataFname);
	}
	
}
