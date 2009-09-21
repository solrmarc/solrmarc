package org.blacklight;


import org.junit.*;

/**
 * junit4 tests for Generic Blacklight example, 
 *  other than tests for:
 *    title fields (in org.blacklight.TitleMappingTests)
 *    author fields (in org.blacklight.AuthorMappingTests)
 *    subject fields (in org.blacklight.SubjectMappingTests)
 *    publication fields (in org.blacklight.PublicationMappingTests)
 *    call number fields (in org.blacklight.CallnumMappingTests)
 *   
 * @author Naomi Dushay
 */
public class OtherMappingTests extends AbstractMappingTests {
	

@Test
	public final void smokeTest()
	{
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "id", "00282214");
	}

// title field tests in org.blacklight.TitleMappingTests
// author field tests in org.blacklight.AuthorMappingTests
// subject field tests in org.blacklight.SubjectMappingTests
// publication field tests in org.blacklight.PublicationMappingTests
// call number field tests in org.blacklight.CallnumMappingTests


	/**
	 * format = 007[0-1]:000[6-7]:000[6], (map.format), first
	 */
@Test
	public final void formatFacetTest()
	{
		// 007[0-1] - he ... not in table, so move on
		//   he
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "85910001", "format", "he");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "85910001", "format", "Unknown");
		
		// NOTE: all records in small test file are 006[6-7] am
		// 006[6-7] - am  ... which is not found, so  006[6] "a" matches Book
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "85910001", "format", "Book");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "format", "Book");
	}


	/**
	 * language_facet = 008[35-37]:041a:041d, language_map.properties
	 */
@Test
	public final void languageTest()
	{
		//  two 041a in same record
		solrFldMapTest.assertSolrFldValue(marcFileDir + "2008308175.mrc", "2008308175", "language_facet", "Tibetan");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008308175", "language_facet", "English");
		// 041a - sanengkan  (probably should be three separate subfield a ...)
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "2001417245", "language_facet", "sanengkan");	
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2001417245", "language_facet", "Sanskrit");
		
		// no 041d in small test file
	}

	/**
	 * url_fulltext_display = custom, getFullTextUrls
	 */
//@Test
// NOTE: - no full text Urls in small test file
	public final void urlFullTextTest()
	{
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "", "url_fulltext_display", "");
	}

	/**
	 * url_suppl_display = custom, getSupplUrls
	 */
@Test
	public final void urlSupplTest()
	{
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "url_suppl_display", "http://www.loc.gov/catdir/toc/ecip0719/2007020969.html");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "url_suppl_display", "http://www.loc.gov/catdir/enhancements/fy0808/2007020969-d.html");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "url_suppl_display", "http://www.loc.gov/catdir/enhancements/fy0808/2007020969-s.html");
	}
	
	/**
	 * material_type_display = custom, removeTrailingPunct(300a)
	 */
@Test
	public final void materialTypeTest()
	{
		// trailing semicolon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "material_type_display", "375 p.");
		// trailing colon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "material_type_display", "283 p.");
		// just v.
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "43037890", "material_type_display", "v.");
		// raw value   "24, 128, 2 p. ;"
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "78908283", "material_type_display", "24, 128, 2 p.");
// FIXME:  this is ugly
		// raw value   "v. <1   > :"
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "2001417245", "material_type_display", "v. <1   > :");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2001417245", "material_type_display", "v. <1   >");
		// raw value   "items 1-<13> of <13> ;"
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "2005553155", "material_type_display", "items 1-<13> of <13> ;");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005553155", "material_type_display", "items 1-<13> of <13>");
	}

	/**
	 * isbn_t = 020a, (pattern_map.isbn_clean)
	 */
@Test
	public final void ISBNTest()
	{
// FIXME:  isbn pattern can be exactly 10 or 13 digits and can end in X ???!!!
		// parenthetical suffix
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "isbn_t", "9780743297790");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "isbn_t", "0743297792");
	}


}
