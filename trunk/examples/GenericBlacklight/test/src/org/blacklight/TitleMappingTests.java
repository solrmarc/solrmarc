package org.blacklight;

import org.junit.*;

/**
 * junit4 tests for generic Blacklight example - title fields
 * @author Naomi Dushay
 */
public class TitleMappingTests extends AbstractMappingTests {
	
// TODO:  test right-to-left scripts ... oy!

// 245a "main title" fields

	/**
	 * title search field: title_t
	 * title_t = custom, getLinkedFieldCombined(245a)
	 *   should have both latin and vernacular 
	 *   tokenized by Solr when written to index, so trailing punct can stay
	 */
@Test
	public final void titleSearchTest()
	{
// FIXME: getLinkedCombined  removes trailing punct for 880s	
		// trailing slash - chinese
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "title_t", "Ci an zhou bian /");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "title_t", "次按驟變");
		// trailing slash - japanese
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "title_t", "Kubo Sakae 'Kazanbaichi' o yomu /");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "title_t", "久保栄「火山灰地」を読む");
		// trailing colon - korean
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "title_t", "Ajikto kŭrŏk chŏrŏk sasimnikka :");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "title_t", "아직도　그럭　저럭　사십니까");
	}
	
	/**
	 * title display field: title_display
	 * title_display = custom, removeTrailingPunct(245a)
	 */
@Test
	public final void titleDisplayTest()
	{
		// trailing slash 
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00314247", "title_display", "Kubo Sakae \"Kazanbaichi\" o yomu");
		// trailing colon
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "title_display", "Shodede-yam Yehudiyim");
	}

	/**
	 * title display field - vernacular (linked 880)
	 * title_vern_display = custom, getLinkedField(245a)
	 */
@Test
	public final void titleVernDisplayTest()
	{
		// trailing slash - chinese
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "title_vern_display", "次按驟變");
		// trailing slash - japanese
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "title_vern_display", "久保栄「火山灰地」を読む");
		// trailing colon - korean
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "title_vern_display", "아직도　그럭　저럭　사십니까");
	}


//245b "subtitle" fields

	/**
	 * subtitle search field: subtitle_t
	 * subtitle_t = custom, getLinkedFieldCombined(245b)
	 *   should have both latin and vernacular 
	 *   tokenized by Solr when written to index, so trailing punct can stay
	 */
@Test
	public final void subtitleSearchTest()
	{
		// trailing slash
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "subtitle_t", "a Native American elder has her say : an oral history /");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "subtitle_t", "tʻeoriul-metʻoduri narkvevi /");

		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005461726", "subtitle_t", "min jald al-dhāt ilá ṣidq al-sharḥ.");
								solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005461726", "subtitle_t", "من جلد الذات إلى صدق الشرح");

		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "subtitle_t", "kangnam yŏin kwa sin pʻalbulchʻul : Kim Hong-sin setʻae rŭpʻo.");
// FIXME: getLinkedCombined  removes trailing punct for 880s	
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "subtitle_t", "강남　여인　과　신　팔불출 : 金　洪信　세태　르포");
	}

	/**
	 * subtitle_display = custom, removeTrailingPunct(245b)
	 */
@Test
	public final void subtitleDisplayTest()
	{
		// trailing slash
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "subtitle_display", "a Native American elder has her say : an oral history");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "subtitle_display", "tʻeoriul-metʻoduri narkvevi");
		// trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "subtitle_display", "kangnam yŏin kwa sin pʻalbulchʻul : Kim Hong-sin setʻae rŭpʻo");
	}
	
	/**
	 * subtitle display field - vernacular (linked 880)
	 * subtitle_vern_display = custom, getLinkedField(245b)
	 */
@Test
	public final void subtitleVernDisplayTest()
	{
		// trailing period
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "94120425", "subtitle_vern_display", "강남　여인　과　신　팔불출 : 金　洪信　세태　르포");
								solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005461726", "subtitle_vern_display", "من جلد الذات إلى صدق الشرح");
	}


// additional title searching fields

	/**
	 * title_addl_t = custom, getLinkedFieldCombined(245abnps:130[a-z]:240[a-gk-s]:210ab:222ab:242abnp:243[a-gk-s]:246[a-gnp]:247[a-gnp])
	 *   should have both latin and vernacular 
	 *   tokenized by Solr when written to index, so trailing punct can stay
	 */
@Test
	public final void additionalTitleSearchTest()
	{
		// no 245 subfields other than a, b, c, h in small test file
		// 130
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "77826928", "title_addl_t", "Koryŏsa. Yŏlchŏn. Selections");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "77826928", "title_addl_t", "高麗史. 列傅. Selections");
		// 240
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "title_addl_t", "Mishnah berurah");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "title_addl_t", "‏משנה ברורה");

		// 246
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "title_addl_t", "Obraz zhizni rabotnika promyshlennosti");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00313831", "title_addl_t", "Critique for all seasons : Akbar Ganji's conversation with Abdullah Nuri");
		      solrFldMapTest.assertSolrFldValue(marc30recTestFile, "87931798", "title_addl_t", "‏اشنايى با حوزه‌هاى علميۀ شيعه در طول تاريخ");
		// 210, 222, 242, 243, 247 - none in small test file
	}

	/**
	 * title_added_entry_t = custom, getLinkedFieldCombined(700[gk-pr-t]:710[fgk-t]:711fgklnpst:730[a-gk-t]:740anp)
	 *   should have both latin and vernacular 
	 *   tokenized by Solr when written to index, so trailing punct can stay
	 */
@Test
	public final void addedEntrySearchTest()
	{
// FIXME:  inconsistent trailing punct:  linked fields, pattern vs. string of chars for subfields
		// 700
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "title_added_entry_t", "Oraḥ ḥayim");
       	solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "title_added_entry_t", "‏אורח חיים");
		// 710
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "53029833", "title_added_entry_t", "Constitution");
		// 711, 730 - none in small test file
		// 740
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "title_added_entry_t", "Jewish pirates.");
		//  no linked 740 other than subfield a
// FIXME:  trailing period ...
			     solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005553155", "title_added_entry_t", "ביטוח זקנה");
	}

@Test
	/**
	 * title_series_t = custom, getLinkedFieldCombined(440anpv:490av)
	 */
	public final void seriesSearchTest()
	{
		// 440
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "96933325", "title_series_t", "Sipihr-i farhang va jāmiʻah ; 1");
		      solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00313831", "title_series_t", "‏فرهنگ  عمومى");
		      
		// 490
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "title_series_t", "Lin Xingzhi zuo pin ji ; 51");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "title_series_t", "林行止作品集 ; 51");
	}


// title sort field

	/**
	 * title_sort = custom, getSortableTitle
	 */
@Test
	public final void titleSortTest()
	{	
		// ignore first three chars
		solrFldMapTest.assertSolrFldHasNoValue(marc30recTestFile, "2003546302", "title_sort", "al-ḥarb fī al-alfīyah al-thālithah");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2003546302", "title_sort", "ḥarb fī al-alfīyah al-thālithah");
	}

}
