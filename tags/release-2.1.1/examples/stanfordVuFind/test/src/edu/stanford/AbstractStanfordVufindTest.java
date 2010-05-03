package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.solrmarc.index.IndexTest;

/**
 * Site Specific code used for testing the stanford Bibliographic index
 * @author Naomi Dushay
 *
 */
public abstract class AbstractStanfordVufindTest extends IndexTest {

	// Note:  the hardcodings below are only used when the tests are
	//  invoked without the properties set
	//   the properties ARE set when the tests are invoke via ant.
	{
        // !!! NOTE:  this one is local and must be modified for your installation !!!
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
        {
            String ngdeDir = File.separator + "Users" + File.separator + "naomi" 
                    + File.separator + "NGDE";
            solrPath = ngdeDir + File.separator + "solr" + File.separator + "solr1.4";
//            solrPath = ngdeDir + File.separator + "jetty" + File.separator
 //                   + "solr";
            System.setProperty("solr.path", solrPath);
        }

        String solrmarcPath = System.getProperty("solrmarc.path");
        if (solrmarcPath == null) {
            solrmarcPath = new File("lib" + File.separator + "solrmarc").getAbsolutePath();
            System.setProperty("solrmarc.path", solrmarcPath);
        }
        
        String solrmarcSitePath = System.getProperty("solrmarc.site.path");
        if (solrmarcSitePath == null) {
            solrmarcSitePath = new File("examples" + File.separator + "stanfordVufind").getAbsolutePath(); 
            System.setProperty("solrmarc.site.path", solrmarcSitePath);
        }
        
        String configPropDir = System.getProperty("test.config.dir");
        if (configPropDir == null)
            configPropDir = solrmarcSitePath;
        
        String configPropFile = System.getProperty("test.config.file");
        if (configPropFile == null) {
            configPropFile = solrmarcSitePath + File.separator + "bibix_config.properties";
            System.setProperty("test.config.file", configPropFile);
        }
                
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
        {
            testDataParentPath = System.getProperty("test.data.parent.path");
            if (testDataParentPath == null)
                testDataParentPath = "examples" + File.separator
                        + "stanfordBlacklight" + File.separator + "test"
                        + File.separator + "data";

            // testDir = "test";
            // testDataParentPath = testDir + File.separator + "data";
//          testDataPath = testDataParentPath + File.separator + "allfieldsTests.mrc";

            System.setProperty("test.data.path", testDataParentPath);
        }

        String solrDataDir = System.getProperty("solr.data.dir");
        if (solrDataDir == null)
            solrDataDir = solrPath + File.separator + "data";
	}

	
	public void createIxInitVars(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException 
	{
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
            solrPath = new File("examples/stanfordVufind/test/solr").getAbsolutePath();


        String testDataParentPath = System.getProperty("test.data.path");
        
        if (testDataParentPath == null)  testDataParentPath = "test/data";

	    createIxInitVars(System.getProperty("test.config.file"), solrPath, null, testDataParentPath, testDataFname);
	}

}
