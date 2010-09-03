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
	
	protected String siteDir = null;
	protected String marcFileDir = null;
	protected String marc30recTestFile = null;

@Before
	public void setup() 
	{
		// these properties must be set or MarcHandler can't initialize properly
        siteDir = ".";

        marcFileDir = System.getProperty("test.data.path", siteDir + File.separator + "test" + File.separator + "data");
        marcFileDir = new File(marcFileDir).getAbsolutePath();
        if (!marcFileDir.endsWith(File.separator)) marcFileDir = marcFileDir + File.separator;
        marc30recTestFile = new File(marcFileDir, "test_data.utf8.mrc").getAbsolutePath();
        
        System.setProperty("solrmarc.path", "lib" + File.separator + "solrmarc");
		System.setProperty("solrmarc.site.path", siteDir); 
    	System.setProperty("marc.source", "FILE");

    	// needed to get through initialization; overridden in individual tests
    	System.setProperty("marc.path", marc30recTestFile);

    	solrFldMapTest = new SolrFieldMappingTest("demo_config.properties", "id");
	}

}
