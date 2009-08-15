package org.blacklight;

import java.io.File;

import org.junit.*;

import org.solrmarc.index.SolrFieldMappingTest;

/**
 * junit4 tests for generic Blacklight example
 * @author Naomi Dushay
 */
public class SimpleTests {
	
	private SolrFieldMappingTest solrFldMapTest = null;
	private String siteDir = "bldemo";
	private String marcFileDir = siteDir + File.separator + 
									"test" + File.separator +
									"data" + File.separator;
@Before
	public final void setup() 
	{
		// these properties must be set or MarcHandler can't initialize properly
        System.setProperty("solrmarc.path", "lib" + File.separator + "solrmarc");
		System.setProperty("solrmarc.site.path", siteDir); 
    	System.setProperty("marc.source", "FILE");

    	// needed to get through initialization; overridden in individual tests
    	System.setProperty("marc.path", marcFileDir + "00282214.mrc");

    	solrFldMapTest = new SolrFieldMappingTest(siteDir + File.separator + "demo_config.properties", "id");
	}
	

@Test
	public final void smokeTest()
	{
		solrFldMapTest.assertSolrFldHasValue(marcFileDir + "00282214.mrc", "00282214", "id", "00282214");
	}

}
