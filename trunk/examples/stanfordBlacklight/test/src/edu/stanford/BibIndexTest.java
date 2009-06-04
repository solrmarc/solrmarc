package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.After;
import org.solrmarc.index.IndexTest;

/**
 * Site Specific code used for testing the stanford Bibliographic index
 * @author Naomi Dushay
 */
public abstract class BibIndexTest extends IndexTest {

	// Note:  the hardcodings below are only used when the tests are
	//  invoked without the properties set
	//   the properties ARE set when the tests are invoke via ant.
	{
        solrmarcPath = System.getProperty("solrmarc.path");
        if (solrmarcPath == null)
            solrmarcPath = new File("lib" + File.separator + "solrmarc").getAbsolutePath();
		
		siteSpecificPath = System.getProperty("solrmarc.site.path");
		if (siteSpecificPath == null)
			siteSpecificPath = new File("examples" + File.separator + "stanfordBlacklight").getAbsolutePath(); 
		
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
		    solrPath = ngdeDir + File.separator + "solr" +
		                File.separator + "solr1.4";

		testDataParentPath = System.getProperty("test.data.parent.path");
		if (testDataParentPath == null)
			testDataParentPath = ngdeDir + File.separator + "solrmarc" +
        				File.separator + "stanfordSolrmarc" + File.separator + "examples" +
        				File.separator + "stanfordBlacklight" + 
        				File.separator + "test" + File.separator + "data";
        				
		testDataPath = testDataParentPath + File.separator + "allfieldsTests.mrc";
		solrDataDir = System.getProperty("solr.data.dir");
		if (solrDataDir == null)
			solrDataDir = solrPath + File.separator + "data";
	}

	
	public void createIxInitVars(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException 
	{
		createNewTestIndex(testDataParentPath + File.separator + testDataFname, configPropFile, solrPath, solrDataDir, solrmarcPath, siteSpecificPath);
		solrCore = getSolrCore(solrPath, solrDataDir);
		sis = getSolrIndexSearcher(solrCore);
	}
	
	/**
	 * ensure IndexSearcher and SolrCore are reset for next test
	 */
@After
	public void tearDown()
	{
	    if (solrCore != null)
	    {
	        solrCore.close();
	        solrCore = null;
	    }
	}

}
