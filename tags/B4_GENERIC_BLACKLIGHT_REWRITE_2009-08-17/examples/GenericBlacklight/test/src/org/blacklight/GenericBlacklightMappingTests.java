package org.blacklight;

import java.io.File;

import org.junit.*;
import static org.junit.Assert.*;

import org.solrmarc.index.SolrFieldMappingTest;

/**
 * junit4 tests for generic Blacklight example
 * @author Naomi Dushay
 */
public class GenericBlacklightMappingTests {
	
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
	

@Test
	public final void smokeTest()
	{
		solrFldMapTest.assertSolrFldValue(marcFileDir + "00282214.mrc", "00282214", "id", "00282214");
	}


// TITLE Fields  (Search, Display, Facet, Sort)

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

// AUTHOR Fields  (Search, Display, Facet, Sort)
	
@Test
	/**
	 * author_t = custom, removeTrailingPunct(110a:111a:130a:700a:710a:711a)
	 */
	public final void AuthorSearchTest()
	{
		// 110a - leave parens
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "53029833", "author_t", "Korea (North)");
		// no 111 in small test file
		// 130a - trailing period 
// FIXME: can't get diacritic right for this test
//		solrFldMapTest.assertSolrFldHasValue(marc30recTestFile, "77826928", "author_t", "Koryŏsa");
		// 700a - trailing comma
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "77826928", "author_t", "Yi, Min-su");
		//   700a - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008308175", "author_t", "Cordell, Dennis");
		// 710a - leave parens
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "53029833", "author_t", "Korea (North)");
		//   710a - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008305903", "author_t", "Sroṅ-btsan dpe mdzod khaṅ");
		// no 711 in small test file
	}

// SUBJECT fields  (Search, Display, Facet, Sort)

@Test
	/**
	 * subject_t = custom, removeTrailingPunct(600a:610a:611a:630a:650a:651a:655a:690a)
	 */
	public final void SubjectSearchTest()
	{
		// 600a - no trailing punct
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "78908283", "subject_t", "Gautama Buddha");
		//  600a - comma
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "subject_t", "Karo, Joseph ben Ephraim");
		// 610a - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00313831", "subject_t", "Iran");
		// no 611 in small test file
		// no 630 in small test file
		// 650a - no trailing punct
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00314247", "subject_t", "Japanese drama");
		//   650a - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2003546302", "subject_t", "Warfare, Conventional");
		// 651a - leave parens
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2004310986", "subject_t", "Bon-brgya (China)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "geographic_subject_facet", "Economic history");
		// no 655 in small test file
// FIXME:  don't use 690 here -- local field!!!
		// no 690 in small test file
	}
	
@Test
	/**
	 * subject_era_facet = custom, removeTrailingPunct(650d:650y:651y:655y)
	 */
	public final void SubjectEraFacetTest()
	{
		// no 650d in small test file
		// 650y - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005461726", "subject_era_facet", "20th century");
		// 651y - trailing hyphen
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "subject_era_facet", "1990-");
		//     651y - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "subject_era_facet", "2005-2015");
		// no 655 in small test file
	}

@Test
	/**
	 * geographic_subject_facet = custom, removeTrailingPunct(650z:651a)
	 */
	public final void GeographicSubjectFacetTest()
	{
		// 650z - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "87931798", "geographic_subject_facet", "Islamic countries");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "87931798", "geographic_subject_facet", "Islamic countries.");
// FIXME:  should NOT be 651a, i don't think.  checked stanford.
		// 651a - leave parens
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2004310986", "geographic_subject_facet", "Bon-brgya (China)");
		// FIXME:  this is a 651a that definitely doesn't belong as geographic!!
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "2009373513", "geographic_subject_facet", "Economic history");	
	}

// FORMAT fields  (Search, Display, Facet, Sort)

@Test
	/**
	 * format_facet = 007[0-1]:000[6-7]:000[6], (map.format), first
	 */
	public final void FormatFacetTest()
	{
		// 007[0-1] - he ... not in table, so move on
		//   he
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "85910001", "format_facet", "he");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "85910001", "format_facet", "Unknown");
		
		// NOTE: all records in small test file are 006[6-7] am
		// 006[6-7] - am  ... which is not found, so  006[6] "a" matches Book
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "85910001", "format_facet", "Book");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "format_facet", "Book");
	}

@Test
	/**
	 * format_code_t = 007[0-1]:000[6-7]:000[6], (map.format_code), first
	 */
	public final void FormatCodeTest()
	{
		// 007[0-1] - he ... not in table, so move on
		//   he
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "85910001", "format_code_t", "he");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "85910001", "format_code_t", "unknown");

		// NOTE: all records in small test file are 006[6-7] am
		// 006[6-7] - am  ... which is not found, so  006[6] "a" matches Book
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "85910001", "format_code_t", "book");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "format_code_t", "book");
	}


// OTHER (not title, author, subject or format) fields (Search, Display, Facet, Sort)
@Test
	/**
	 * published_t = custom, removeTrailingPunct(260a)
	 */
	public final void PublishedDisplayTest()
	{
		// trailing colon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "published_t", "Karācī");
		// trailing comma and square brackets
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "43037890", "published_t", "Moskva");
		// unmatched bracket, trailing colon, trailing question mark
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "published_t", "Jerusalem?");
		// unmatched bracket, trailing colon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2004310986", "published_t", "Lha-sa");
		// unmatched bracket, trailing comma
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005553155", "published_t", "Israel");
	}

@Test
	/**
	 * language_facet = 008[35-37]:041a:041d, language_map.properties
	 */
	public final void LanguageTest()
	{
		//  two 041a in same record
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008308175", "language_facet", "Tibetan");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008308175", "language_facet", "English");
		// 041a - sanengkan  (probably should be three separate subfield a ...)
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "2001417245", "language_facet", "sanengkan");	
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2001417245", "language_facet", "Sanskrit");
		
		// no 041d in small test file
	}

@Test
	/**
	 * material_type_t = custom, removeTrailingPunct(300a)
	 */
	public final void MaterialTypeTest()
	{
		// trailing semicolon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "material_type_t", "375 p.");
		// trailing colon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "material_type_t", "283 p.");
		// just v.
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "43037890", "material_type_t", "v.");
		// raw value   "24, 128, 2 p. ;"
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "78908283", "material_type_t", "24, 128, 2 p.");
// FIXME:  this is ugly
		// raw value   "v. <1   > :"
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "2001417245", "material_type_t", "v. <1   > :");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2001417245", "material_type_t", "v. <1   >");
		// raw value   "items 1-<13> of <13> ;"
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "2005553155", "material_type_t", "items 1-<13> of <13> ;");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005553155", "material_type_t", "items 1-<13> of <13>");
	}

@Test
	/**
	 * isbn_t = 020a, (pattern_map.isbn_clean)
	 */
	public final void ISBNTest()
	{
// FIXME:  isbn pattern can be exactly 10 or 13 digits and can end in X ???!!!
		// parenthetical suffix
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "isbn_t", "9780743297790");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "isbn_t", "0743297792");
	}


@Test
/**
 * 
 */
public final void Test()
{
	fail("write this test");
}



}
