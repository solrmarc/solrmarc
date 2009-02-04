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
public abstract class BibIndexTest extends IndexTest {

	// Note:  the hardcodings below are only used when the tests are
	//  invoked without the properties set
	//   the properties ARE set when the tests are invoke via ant.
	{
		String parentDir = File.separator + "Users" + File.separator + "naomi" + File.separator + "NGDE";
		
		solrmarcPath = System.getProperty("solrmarc.path");
		if (solrmarcPath == null)
			solrmarcPath = parentDir + File.separator + "solrmarc" + File.separator + "stanfordSolrmarc";
		
		siteSpecificPath = System.getProperty("solrmarc.site.path");
		if (siteSpecificPath == null)
			siteSpecificPath = solrmarcPath + File.separator + "local" + File.separator + "stanfordVuFind"; 
		
		configPropFile = siteSpecificPath + File.separator + "bibix_config.properties";
		
		solrPath = System.getProperty("solr.path");
		if (solrPath == null)
			solrPath = parentDir +  File.separator + "vufind" + File.separator + "vufind - local svn" +  File.separator + "solr";

		testDir = solrmarcPath + File.separator + "test";
		testDataParentPath = testDir + File.separator + "data";
		testDataPath = testDataParentPath + File.separator + "allfieldsTests.mrc";
		solrDataDir = System.getProperty("solr.data.dir");
		if (solrDataDir == null)
			solrDataDir = testDir + File.separator + "solr" + File.separator + "data";
	}

	
	public void createIxInitVars(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException 
	{
		createNewTestIndex(testDataParentPath + File.separator + testDataFname, configPropFile, solrPath, solrDataDir, solrmarcPath, siteSpecificPath);
		solrCore = getSolrCore(solrPath, solrDataDir);
		sis = getSolrIndexSearcher(solrCore);
	}

}
