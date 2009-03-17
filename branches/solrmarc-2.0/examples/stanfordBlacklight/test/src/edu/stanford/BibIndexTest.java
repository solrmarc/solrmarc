package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.solrmarc.index.IndexTest;
import org.solrmarc.index.OldIndexTest;

/**
 * Site Specific code used for testing the stanford Bibliographic index
 * @author Naomi Dushay
 */
public abstract class BibIndexTest extends OldIndexTest {

//	// Note:  the hardcodings below are only used when the tests are
//	//  invoked without the properties set
//	//   the properties ARE set when the tests are invoke via ant.
//	{
//        solrmarcPath = System.getProperty("solrmarc.path");
//        if (solrmarcPath == null)
//            solrmarcPath = new File("lib" + File.separator + "solrmarc").getAbsolutePath();
//		
//		siteSpecificPath = System.getProperty("solrmarc.site.path");
//		if (siteSpecificPath == null)
//			siteSpecificPath = new File("examples" + File.separator + "stanfordBlacklight").getAbsolutePath(); 
//		
//        String configPropDir = System.getProperty("test.config.dir");
//        if (configPropDir == null)
//            configPropDir = siteSpecificPath;
//        
//        configPropFile = System.getProperty("test.config.file");
//		if (configPropFile == null)
//		    configPropFile = configPropDir + File.separator + "bibix_config.properties";
//		
//		solrPath = System.getProperty("solr.path");
//		if (solrPath == null)
////			solrPath = siteSpecificPath + File.separator + "solr";
//		    solrPath = File.separator + "Users" + File.separator + "naomi" + 
//		                File.separator + "NGDE" + File.separator + "solr" +
//		                File.separator + "solr1.4";
//
////		testDir = solrmarcPath + File.separator + "test";
//        testDir = "test";
////		testDir = ".." + File.separator + ".." + File.separator + "test";
//        testDataParentPath = testDir + File.separator + "data";
//		testDataPath = testDataParentPath + File.separator + "allfieldsTests.mrc";
//		solrDataDir = System.getProperty("solr.data.dir");
//		if (solrDataDir == null)
//			solrDataDir = solrPath + File.separator + "data";
//	}

	
	public void createIxInitVars(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException 
	{
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
            solrPath = new File("examples/stanfordVufind/test/solr").getAbsolutePath();


        String testDataParentPath = System.getProperty("test.data.path");
        
        if (testDataParentPath == null)  testDataParentPath = "test/data";

        createIxInitVars(null, solrPath, null, testDataParentPath, testDataFname);
	}

}
