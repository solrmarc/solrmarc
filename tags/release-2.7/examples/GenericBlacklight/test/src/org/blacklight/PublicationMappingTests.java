package org.blacklight;

import org.junit.*;

/**
 * junit4 tests for generic Blacklight example - publication fields
 * @author Naomi Dushay
 */
public class PublicationMappingTests extends AbstractMappingTests
{
	/**
	 * publication display field: published_display
	 * published_display = custom, removeTrailingPunct(260a)
	 */
@Test
	public final void pubDisplayTest()
	{
		// 260a
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "43037890", "published_display", "Moskva");
	}

	/**
	 * vernacular publication display field (linked 880)
	 * published_vern_display = custom, getLinkedField(260a)
	 */
@Test
	public final void vernPubDisplayTest()
	{
		// 260a linked
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2009373513", "published_vern_display", "臺北縣板橋市");
	}

	/**
	 * publication date field for display and facet: pub_date
	 * pub_date = 260c
	 */
@Test
	public final void pubDateTest()
	{
		// 008 first date and 260c agree
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "pub_date", "1998");
		// 008 first date and 260c agree;  there is a diff 008 second date
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "79930185", "pub_date", "1978");
		// 008 second date and 260c agree
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "85910001", "pub_date", "1946");
		// 260c has c1984;  008 first date is 1984
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "pub_date", "1984");
		// 260c is "1365-   [1986-      ]"  008 first date is 1986
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "87931798", "pub_date", "1986");
		// 260c is "752-<[768]> [1992-<2007 or 2008>]"  008 first date is 1992, second is 9999
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "pub_date", "1992");
		// 260c is "1373 [1994 or 1995]", 008 first date is 1994
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "96933325", "pub_date", "1994");
		// 260c is "1962-<2001>]"  008 first date is 1965   008 second date is 9999 
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "2005553155", "pub_date", "1962");

		//  need examples of:
		// 1950s ...   195u
		// xxth century  19uu
	}

// pub_date_sort is a copy field from pub_date

	/**
	 * publication date field for sorting: pub_date_sort
	 * pub_date_sort = custom, getPubDateSort
	 */
	public final void pubDateSortTest()
	{
		// need examples of 195u and 19uu for sorting.
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "", "pub_date_sort", "");
	}
}
