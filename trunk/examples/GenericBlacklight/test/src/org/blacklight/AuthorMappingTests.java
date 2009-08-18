package org.blacklight;

import org.junit.*;

/**
 * junit4 tests for generic Blacklight example - author fields
 * @author Naomi Dushay
 */
public class AuthorMappingTests extends AbstractMappingTests
{
	/**
	 * primary author search field: author_t
	 * author_t = custom, getLinkedFieldCombined(100abcegqu:110abcdegnu:111acdegjnqu)
	 *   should have both latin and vernacular 
	 *   tokenized by Solr when written to index, so trailing punct can stay
	 */
@Test
	public final void AuthorSearchTest()
	{
		// 100 
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282371", "author_t", "Ayaz, Shaikh,");
		//   aq subfields
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "43037890", "author_t", "Vi︠a︡tkin, M. P. (Mikhail Porfirʹevich),");
		//   acd subfields
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "author_t", "Israel Meir, ha-Kohen,");
// FIXME: getLinkedCombined  removes trailing punct for 880s	
		//    100 linked - trailing comma
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "author_t", "김　홍신");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "author_t", "吉田一");
		// 110
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "53029833", "author_t", "Korea (North)");
		// no 111 in small test file
	}
	

	/**
	 * additional author search field: author_addl_t
	 * author_addl_t = custom, getLinkedFieldCombined(700abcegqu:710abcdegnu:711acdegjnqu)
	 *   should have both latin and vernacular 
	 *   tokenized by Solr when written to index, so trailing punct can stay
	 */
@Test
	public final void AddlAuthorSearchTest()
	{
		// 700 
		//      subfields a
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2001417245", "author_addl_t", "Shyamachar, A. B.");
		//      subfields ad
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "author_addl_t", "Strong Medicine,");
		//      subfields acdtf
// FIXME:  can't get this line to work.  utf-8 issues?
//		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008308175", "author_addl_t", "Ṅag-dbaṅ-blo-bzaṅ-rgya-mtsho,     Dalai Lama V,");
//FIXME: getLinkedCombined  removes trailing punct for 880s	
		// 700 linked
		//     subfield a - trailing comma
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "77826928", "author_addl_t", "李 民樹");
		
		// 710a - leave parens
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "53029833", "author_addl_t", "Korea (North)");
		//   710a - trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008305903", "author_addl_t", "Sroṅ-btsan dpe mdzod khaṅ.");
		// no 711 in small test file
	}


// display fields

	/**
	 * author display field: author_display
	 * author_display = custom, removeTrailingPunct(100abcdq:110:111)
	 */
@Test
	public final void AuthorDisplayTest()
	{
		// 100
		//   a subfields
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "author_display", "Finkel, Chaim Jacob");
		//   ad subfields
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "author_display", "Israel Meir, ha-Kohen, 1838-1933");
		//   adq subfields
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "43037890", "author_display", "Vi︠a︡tkin, M. P. (Mikhail Porfirʹevich), 1895-1967");
		// 110
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "53029833", "author_display", "Korea (North)");
	}

	/**
	 * vernacular author display field (linked 880)
	 * author_vern_display = custom, getLinkedField(100abcdq:110:111)
	 */
@Test
	public final void VernAuthorDisplayTest()
	{
		// 100 linked
		//  subfield a
			     solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00313831", "author_vern_display", "‏نورى، عبد الله");
		// subfields  ad
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "author_vern_display", "김　홍신, 1947-");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "author_vern_display", "吉田一, 1934-");
		// no linked 110 or 111
	}


	/**
	 * author_sort = custom, getSortableAuthor
	 * which is 
	 *  1. the main entry author, if there is one (100, 110, or 111)
	 * followed by 
	 *  2. the main entry uniform title (240), if there is one - not including 
	 *    non-filing chars as noted in 2nd indicator
	 * followed by
	 *  3.  the 245 title, not including non-filing chars as noted in ind 2
	 *     and not including subfield c
	 * with ascii punctuation stripped
	 */
@Test
	public final void AuthorSortTest()
	{	
		// note:  small test file has no 100, 110 or 111 with non-filing chars
		// 100 then 240
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "author_sort", "Finkel Chaim Jacob Jewish pirates Hebrew Shodedeyam Yehudiyim sipurim mafliʼim hamevusasim ʻal ʻuvdot hisṭoriyot");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "author_sort", "Israel Meir haKohen 18381933 Mishnah berurah Sefer Mishnah berurah ṿehu perush yafeh umenupeh ʻal Shulḥan ʻarukh Oraḥ ḥayim asher ḥiber Yosef Ḳaro  ʻim ḥidushe dinim shehishmiṭ hagaʼon  ṿehimtsiʼam  Mosheh Iserlish");
		// 110 then 240
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92117465", "author_sort", "Korea North Laws etc Pukhan pŏmnyŏngjip");
		// 100 then 245  (no 240)
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00314247", "author_sort", "Yoshida Hajime 1934 Kubo Sakae Kazanbaichi o yomu");
	}

}
