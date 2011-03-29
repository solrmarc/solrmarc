package org.blacklight;

import org.junit.*;

/**
 * junit4 tests for generic Blacklight example - call number fields
 * @author Naomi Dushay
 */
public class CallnumMappingTests extends AbstractMappingTests
{
	
	/**
	 * call number display field: lc_callnum_display
	 * lc_callnum_display = 050ab, first
	 */
@Test
	public final void lcDisplayTest()
	{
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "lc_callnum_display", "PK2788.9.A9 F55 1998");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "lc_callnum_display", "G535 .F54 1984");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92117465", "lc_callnum_display", "KPC13 .K67 1990");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "lc_callnum_display", "BM520.88.A53 I88 1992b");

		// odd ones -- check for valid LC in custom method??
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282371", "lc_callnum_display", "MLCME 2002/02660 (D)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "85910001", "lc_callnum_display", "Microfiche 90/61328 (P)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "lc_callnum_display", "MLCSN 96/3906 (H)");
	}
	
	/**
	 * call number facet field: lc_1letter_facet
	 *   first letter of LC call number mapped to a description
	 * lc_1letter_facet = 050a[0], callnumber_map.properties, first
	 */
@Test
	public final void lc1LetterFacetTest()
	{
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "lc_1letter_facet", "P - Language & Literature");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "lc_1letter_facet", "G - Geography, Anthropology, Recreation");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92117465", "lc_1letter_facet", "K - Law");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "lc_1letter_facet", "B - Philosophy, Psychology, Religion");

// FIXME:  don't want first character under these circumstances ... but can't do pattern and translation mapping together
		// odd ones - this is not good.
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282371", "lc_1letter_facet", "M - Music");
//		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282371", "lc_1letter_facet", "MLCME 2002/02660 (D)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "85910001", "lc_1letter_facet", "M - Music");
//		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "85910001", "lc_1letter_facet", "Microfiche 90/61328 (P)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "lc_1letter_facet", "M - Music");
//		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "lc_1letter_facet", "MLCSN 96/3906 (H)");
	}

	/**
	 * call number facet field: lc_alpha_facet
	 *   letters at beginning of LC call number (alpha prefix of classification)
	 * lc_alpha_facet = 050a, (pattern_map.lc_alpha), first
	 */
@Test
	public final void lcAlphaFacetTest()
	{
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "lc_alpha_facet", "PK");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "lc_alpha_facet", "G");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92117465", "lc_alpha_facet", "KPC");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "lc_alpha_facet", "BM");
	
		// odd ones - skip 'em
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "00282371", "lc_alpha_facet", "MLC");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "00282371", "lc_alpha_facet", "MLCME");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "00282371", "lc_alpha_facet", "MLCME 2002/02660 (D)");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "85910001", "lc_alpha_facet", "M");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "85910001", "lc_alpha_facet", "Microfiche 90/61328 (P)");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "90142413", "lc_alpha_facet", "MLC");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "90142413", "lc_alpha_facet", "MLCSN");
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "90142413", "lc_alpha_facet", "MLCSN 96/3906 (H)");
	}
	
	/**
	 * call number facet field: lc_b4cutter_facet
	 *   LC call number before the cutter (i.e. the LC classification portion)
	 * lc_b4cutter_facet = 050a[0], callnumber_map.properties, first
	 */
@Test
	public final void lcB4CutterFacetTest()
	{
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "lc_b4cutter_facet", "PK2788.9.A9");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "lc_b4cutter_facet", "G535");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92117465", "lc_b4cutter_facet", "KPC13");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "lc_b4cutter_facet", "BM520.88.A53");
	
		// odd ones
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282371", "lc_b4cutter_facet", "MLCME 2002/02660 (D)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "85910001", "lc_b4cutter_facet", "Microfiche 90/61328 (P)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "lc_b4cutter_facet", "MLCSN 96/3906 (H)");
	}

}
