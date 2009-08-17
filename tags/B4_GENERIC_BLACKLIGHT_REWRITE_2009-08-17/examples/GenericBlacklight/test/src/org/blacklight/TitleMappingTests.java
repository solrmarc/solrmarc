package org.blacklight;

import java.io.File;

import org.junit.*;

import org.solrmarc.index.SolrFieldMappingTest;

/**
 * junit4 tests for generic Blacklight example
 * @author Naomi Dushay
 */
public class TitleMappingTests {
	
	private SolrFieldMappingTest solrFldMapTest = null;
	private String siteDir = "bldemo";
	private String marcFileDir = siteDir + File.separator + 
									"test" + File.separator +
									"data" + File.separator;
	private String marc30recTestFile = marcFileDir + "test_data.utf8.mrc";
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
	
//Title DISPLAY Fields  (Search, Display, Facet, Sort)



// Title SEARCH Fields  (Search, Display, Facet, Sort)

@Test
	/**
	 * title_t = custom, removeTrailingPunct(245a)
	 *    test for trailing punc removal ...
	 */
	public final void TitleSearchTest()
	{
		// NOTE:  removing trailing punctuation makes no difference for a search field of type text
		// trailing slash 
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00314247", "title_t", "Kubo Sakae \"Kazanbaichi\" o yomu");
		// trailing colon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "title_t", "Shodede-yam Yehudiyim");
	}
	
@Test
	/**
	 * sub_title_t = custom, removeTrailingPunct(245b)
	 *    test for trailing punc removal ...
	 */
	public final void SubTitleSearchTest()
	{
		// NOTE:  removing trailing punctuation makes no difference for a search field of type text
		// trailing slash
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "sub_title_t", "a Native American elder has her say : an oral history");
	}

@Test
	/**
	 * alt_titles_t = custom, removeTrailingPunct(240b:700t:710t:711t:440a:490a:505a:830a)
	 *    test for all fields trailing punc removal ...
	 */
	public final void AltTitleSearchTest()
	{
		// no 240b in small test file
		// 700t - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "alt_titles_t", "Oraḥ ḥayim");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008308175", "alt_titles_t", "Rje btsun bla ma ma hā gu ru Padma-ʼbyuṅ-gnas la gsol ba ʼdebs pa byin rlabs bdud rtsiʼi char rgyun");
		// 710t - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "53029833", "alt_titles_t", "Constitution");
		// no 711
		// 440a - trailing semicolon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "96933325", "alt_titles_t", "Sipihr-i farhang va jāmiʻah");
		// 490a - trailing semicolon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "alt_titles_t", "Lin Xingzhi zuo pin ji");
// FIXME: retain period!		
		// 505a - trailing period to keep
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "43037890", "alt_titles_t", "t. 1. S drevneĭshikh vremen po 1870 g.");
		// no 830a in small test file
	}

@Test
	/**
	 * title_added_entry_t = custom, removeTrailingPunct(700t)
	 */
	public final void TitleAddedEntryTest()
	{
		// 700t - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "alt_titles_t", "Oraḥ ḥayim");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008308175", "alt_titles_t", "Rje btsun bla ma ma hā gu ru Padma-ʼbyuṅ-gnas la gsol ba ʼdebs pa byin rlabs bdud rtsiʼi char rgyun");
	}

//Title FACET Fields

//Title SORT Field




}
