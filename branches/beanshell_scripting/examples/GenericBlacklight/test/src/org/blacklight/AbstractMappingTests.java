package org.blacklight;

import java.io.File;

import org.junit.*;

import org.solrmarc.index.SolrFieldMappingTest;

/**
 * Abstract class for junit4 tests for generic Blacklight mapping tests
 * @author Naomi Dushay
 */
public class AbstractMappingTests {
	
	/** SolrFieldMappingTest object to be used in specific tests */
	protected SolrFieldMappingTest solrFldMapTest = null;
	
	private String siteDir = "bldemo";
	protected String marcFileDir = siteDir + File.separator + 
									"test" + File.separator +
									"data" + File.separator;
	protected String marc30recTestFile = marcFileDir + "test_data.utf8.mrc";

@Before
	public void setup() 
	{
		// these properties must be set or MarcHandler can't initialize properly
        System.setProperty("solrmarc.path", "lib" + File.separator + "solrmarc");
		System.setProperty("solrmarc.site.path", siteDir); 
    	System.setProperty("marc.source", "FILE");

    	// needed to get through initialization; overridden in individual tests
    	System.setProperty("marc.path", marc30recTestFile);

    	solrFldMapTest = new SolrFieldMappingTest(siteDir + File.separator + "demo_config.properties", "id");
	}

}
