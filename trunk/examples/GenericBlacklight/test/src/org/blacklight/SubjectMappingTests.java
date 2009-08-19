package org.blacklight;

import org.junit.*;

/**
 * junit4 tests for generic Blacklight example - subject fields
 * @author Naomi Dushay
 */
public class SubjectMappingTests extends AbstractMappingTests
{
	
// search fields
	
	/**
	 * subject search field: subject_t
	 * subject_t = custom, getLinkedFieldCombined(600[a-u]:610[a-u]:611[a-u]:630[a-t]:650[a-e]:651ae:653a:654[a-e]:655[a-c])
	 *   should have both latin and vernacular 
	 *   tokenized by Solr when written to index, so trailing punct can stay
	 */
@Test
	public final void SubjectSearchTest()
	{
		// 600 a
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "78908283", "subject_t", "Gautama Buddha");
		//    600 ad
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00314247", "subject_t", "Kubo, Sakae, 1901-1958. Kazanbaichi");
		// 610
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00313831", "subject_t", "Iran. Vizārat-i Kishvar");
		// 611, 630 - none in small test file
		// 650 a (from az)
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92117465", "subject_t", "Law");
		// 651a
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2004310986", "subject_t", "Bon-brgya (China)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005553155", "subject_t", "Israel");
		// 653 aa
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "subject_t", "Industrial workers; life style");
		// 654, 655 - none in small test file

		// linked 600 ad
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "subject_t", "久保栄, 1901-1958. Kazanbaichi");
	}
	
	/**
	 * additional subject search field: subject_addl_t
	 * subject_additional_t = custom, getLinkedFieldCombined(600[v-z]:610[v-z]:611[v-z]:630[v-z]:650[v-z]:651[v-z]:654[v-z]:655[v-z])
	 *   should have both latin and vernacular 
	 *   tokenized by Solr when written to index, so trailing punct can stay
	 */
@Test
	public final void AddlSubjectSearchTest()
	{
		// 600v
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "78908283", "subject_additional_t", "Poetry");
		// 610xv
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00313831", "subject_additional_t", "Officials and employees Interviews");
		//   610v
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00313831", "subject_additional_t", "Interviews");
		// 611, 630 - none in small test file
		// 650 zzv
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2007020969", "subject_additional_t", "New Jersey Bridgeton Biography");
		//   650x
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "96933325", "subject_additional_t", "Ethnic identity");
		//   650yx
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "subject_additional_t", "20th century History and criticism");
		//   650zx
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "subject_additional_t", "Japan History");
		// 651xv
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008308202", "subject_additional_t", "History Miscellanea");
		//   651xy
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "subject_additional_t", "Economic conditions 1997-");
		//   651y
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "subject_additional_t", "2005-2015");
		// no 654, 655 in small test file
		
		// no linked versions of any of these
	}


// Facet Fields

	/**
	 * topic facet field: subject_topic_facet
	 *
// TODO:  consider using getAllSubfields() custom method that allows for separator (so it looks more like LCSH heading)
	 * subject_topic_facet = custom, removeTrailingPunct(600abcdq:610ab:630a:650a:653a:654ab:655ab)
	 */
@Test
	public final void TopicFacetTest()
	{
		// 600 a
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "78908283", "subject_topic_facet", "Gautama Buddha");
		//     600 ad
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00314247", "subject_topic_facet", "Kubo, Sakae, 1901-1958");
		// 610 ab
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00313831", "subject_topic_facet", "Iran. Vizārat-i Kishvar");
		// 630 - none in small test file
		// 650 a (from az)
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92117465", "subject_topic_facet", "Law");
		// 653 aa
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "90142413", "subject_topic_facet", "Industrial workers; life style");
		// no 654, 655 in small test file
	}

	/**
	 * era facet field: subject_era_facet
	 * subject_era_facet = custom, removeTrailingPunct(650y:651y:654y:655y)
	 */
@Test
	public final void EraFacetTest()
	{
		// 650y
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "subject_era_facet", "20th century");
		// 651y
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "subject_era_facet", "2005-2015");
		// no 654, 655 in small test file
	}
	
	/**
	 * geographic facet field: subject_geo_facet
	 * subject_geo_facet = custom, removeTrailingPunct(651a:650z)
	 */
@Test
	public final void GeographicFacetTest()
	{
		// 651a
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2004310986", "subject_geo_facet", "Bon-brgya (China)");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005553155", "subject_geo_facet", "Israel");
		// 650z
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2008543486", "subject_geo_facet", "Japan");
	}

}
