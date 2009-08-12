package edu.stanford;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.apache.lucene.document.*;
import org.junit.*;

import edu.stanford.StanfordIndexer.PubDateGroup;

/**
 * junit4 tests for Stanford University publication fields for blacklight index
 * @author Naomi Dushay
 */
public class PublicationTests extends BibIndexTest 
{

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("pubDateTests.mrc");
	}


	/**
	 * assure pub dates later than current year +1 are ignored
	 */
@Test
	public void testPubDateTooLate()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date";
		assertZeroResults(fldName, "9999", sis);
		assertZeroResults(fldName, "6666", sis);
		assertZeroResults(fldName, "22nd century", sis);
		assertZeroResults(fldName, "23rd century", sis);
		assertZeroResults(fldName, "24th century", sis);
		assertZeroResults(fldName, "8610s", sis);
	}


	/**
	 * assure pub dates of < 500 are ignored
	 */
@Test
	public void testPubDateTooEarly()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date";
		assertZeroResults(fldName, "0000", sis);
		assertZeroResults(fldName, "0019", sis);
		assertZeroResults(fldName, "0059", sis);
		assertZeroResults(fldName, "0197", sis);
		assertZeroResults(fldName, "0204", sis);
	}

	/**
	 * test that auto-correction of pub date in 008 by checking value in 260c
	 */
@Test
	public void testPubDateAutoCorrect()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date";
		
		assertDocHasNoFieldValue("pubDate0059", fldName, "0059", sis); 
		assertDocHasFieldValue("pubDate0059", fldName, "2005", sis); 
		
		assertDocHasNoFieldValue("pubDate0197-1", fldName, "0197", sis); 
//		assertDocHasFieldValue("pubDate0197-1", fldName, "1970s", sis); 
		assertDocHasFieldValue("pubDate0197-1", fldName, "1970", sis); 
		assertDocHasNoFieldValue("pubDate0197-2", fldName, "0197", sis); 
//		assertDocHasFieldValue("pubDate0197-2", fldName, "1970s", sis); 
		assertDocHasFieldValue("pubDate0197-2", fldName, "1970", sis); 

		// correct
		assertDocHasFieldValue("pubDate0500", fldName, "0500", sis); 
		assertDocHasFieldValue("pubDate0801", fldName, "0801", sis); 
		assertDocHasFieldValue("pubDate0960", fldName, "0960", sis); 
		assertDocHasFieldValue("pubDate0963", fldName, "0963", sis); 

		assertDocHasNoFieldValue("pubDate0204", fldName, "0204", sis); 
		assertDocHasFieldValue("pubDate0204", fldName, "2004", sis); 

		assertDocHasNoFieldValue("pubDate0019", fldName, "0019", sis); 
		// TODO: yeah, i wish ...
//		assertDocHasFieldValue("pubDate0019", fldName, "20th century", sis); 
//		assertDocHasNoFieldValue("pubDate0965", fldName, "0965", sis); 
//		assertDocHasFieldValue("pubDate0965", fldName, "1965", sis);
//		assertDocHasNoFieldValue("pubDate0980", fldName, "0980", sis); 
//		assertDocHasFieldValue("pubDate0980", fldName, "1980", sis); 
//		assertDocHasNoFieldValue("pubDate0999", fldName, "0999", sis); 
//		assertDocHasFieldValue("pubDate0999", fldName, "1999", sis); 
		assertDocHasNoField("410024", fldName, sis);
	}


	/**
	 * test pub_date_search field population.
	 */
@Test
	public final void testPubDateSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date_search";
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		assertFieldOmitsNorms(fldName, solrCore);
	
		pubDateSearchTests(fldName);
	}


	/**
	 * test pub_date_sort field population.
	 */
@Test
	public final void testPubDateSort() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date_sort";
		assertSortFldProps(fldName, solrCore, sis);
	
		// list of doc ids in correct publish date sort order
		List<String> expectedOrderList = new ArrayList<String>(50);
		
		// TODO: invalid/missing dates are designated as last or first in solr
		//  schema file, but are first here (as this is lucene context).
		//  "ties" show up in document order here (order of occurrence in data 
		// file)
		expectedOrderList.add("pubDate6666"); 
		expectedOrderList.add("pubDate1uuu"); 
		expectedOrderList.add("pubDate9999"); 
		expectedOrderList.add("pubDate0000"); 
		expectedOrderList.add("pubDate0019"); 
		
		expectedOrderList.add("pubDate00uu");   // "1st century"
		expectedOrderList.add("pubDate01uu");   // "2nd century"
		expectedOrderList.add("pubDate02uu");   // "3rd century"
		expectedOrderList.add("pubDate03uu");   // "4th century"
		expectedOrderList.add("pubDate0500");   // 0500
		expectedOrderList.add("pubDate08uu");   // "9th century"
		expectedOrderList.add("pubDate0801");   // 0801
		expectedOrderList.add("pubDate09uu");   // "10th century"
		expectedOrderList.add("pubDate0960");   // 0960
		expectedOrderList.add("pubDate0963");   // 0963
		expectedOrderList.add("pubDate10uu");   // "11th century" 
		expectedOrderList.add("pubDate11uu");   // "12th century" 
		expectedOrderList.add("pubDate12uu");   // "13th century" 
		expectedOrderList.add("pubDate13uu");   // "14th century" 
		expectedOrderList.add("pubDate16uu");   // "17th century" 
		expectedOrderList.add("pubDate19uu");   // "20th century" 
		expectedOrderList.add("p19uu");   // "20th century"
		expectedOrderList.add("s190u");   // "1900s"
		expectedOrderList.add("r1900");   // "1900"
		expectedOrderList.add("pubDate195u");   // "1950s"
		expectedOrderList.add("s195u");   // "1950s"
		expectedOrderList.add("g1958");   // "1958"
		expectedOrderList.add("w1959");   // "1959"ˇ
		expectedOrderList.add("bothDates008");  // "1964"
		expectedOrderList.add("pubDate0197-1");  // 1970
		expectedOrderList.add("contRes");       // "1984"
		expectedOrderList.add("y1989");   // "1989"
		expectedOrderList.add("b199u");   // "1990s"
		expectedOrderList.add("k1990");   // "1990"
		expectedOrderList.add("m1991");   // "1991"
		expectedOrderList.add("e1997");   // "1997"
		expectedOrderList.add("c1998");   // "1998"
		expectedOrderList.add("w1999");   // "1999"
		expectedOrderList.add("pubDate20uu");   // "21st century" 
		expectedOrderList.add("o20uu");   // "21st century"
		expectedOrderList.add("x200u");   // "2000s"
		expectedOrderList.add("firstDateOnly008");  // "2000"
		expectedOrderList.add("f2000");   // "2000"
		expectedOrderList.add("q2001");   // "2001"
		expectedOrderList.add("pubDate0204");  // 2004
		expectedOrderList.add("pubDate0059");  // 2005
		expectedOrderList.add("z2006");   // "2006"
		expectedOrderList.add("v2007");   // "2007"
		expectedOrderList.add("b2008");   // "2008"
		expectedOrderList.add("z2009");   // "2009"
		expectedOrderList.add("pubDate2010");   // "2010"
				
		// get search results sorted by pub_date_sort field
		List<Document> results = getSortedDocs("collection", "Catalog", "pub_date_sort", sis);
		Document firstDoc = results.get(0);
		if (firstDoc.getField("id") != null) {
			String firstDocId = firstDoc.getField("id").stringValue();
			assertTrue("9999 pub date should not sort first", firstDocId != "pubDate9999");
		}		
		
		// we know we have documents that are not in the expected order list
		int expDocIx = 0;
		for (Document doc : results) 
		{
			if (expDocIx < expectedOrderList.size() - 1) 
			{
				// we haven't found all docs in the expected list yet
				Field f = doc.getField("id");  // pub_date_sort isn't stored
				if (f != null) 
				{
					String docId = f.stringValue();
					if (docId.equals(expectedOrderList.get(expDocIx + 1))) 
						expDocIx++;
				}
			}
			else break;  // we found all the documents in the expected order list
		}		

		if (expDocIx != expectedOrderList.size() - 1) 
		{
			String lastCorrDocId = expectedOrderList.get(expDocIx);
			fail("Publish Date Sort Order is incorrect.  Last correct document was " + lastCorrDocId);
		}
	}


	/**
	 * pub date ascending sort should start with oldest and go to newest 
	 *  (missing dates sort order tested in another method)
	 */
@Test
	public void testPubDateSortAscending()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date_sort";
		
		// list of doc ids in correct publish date sort order
		List<String> expectedOrderList = new ArrayList<String>(50);
		
		expectedOrderList.add("pubDate2010");   // "2010"
		expectedOrderList.add("z2009");   // "2009"
		expectedOrderList.add("b2008");   // "2008"
		expectedOrderList.add("v2007");   // "2007"
		expectedOrderList.add("z2006");   // "2006"
		expectedOrderList.add("pubDate0059");  // 2005
		expectedOrderList.add("pubDate0204");  // 2004
		expectedOrderList.add("q2001");   // "2001"
		expectedOrderList.add("firstDateOnly008");  // "2000"
		expectedOrderList.add("f2000");   // "2000"
		expectedOrderList.add("x200u");   // "2000s"
		expectedOrderList.add("pubDate20uu");   // "21st century" 
		expectedOrderList.add("o20uu");   // "21st century"
		expectedOrderList.add("w1999");   // "1999"
		expectedOrderList.add("c1998");   // "1998"
		expectedOrderList.add("e1997");   // "1997"
		expectedOrderList.add("m1991");   // "1991"
		expectedOrderList.add("k1990");   // "1990"
		expectedOrderList.add("b199u");   // "1990s"
		expectedOrderList.add("y1989");   // "1989"
		expectedOrderList.add("contRes");       // "1984"
		expectedOrderList.add("pubDate0197-1");  // 1970
		expectedOrderList.add("bothDates008");  // "1964"
		expectedOrderList.add("w1959");   // "1959"ˇ
		expectedOrderList.add("g1958");   // "1958"
		expectedOrderList.add("pubDate195u");   // "1950s"
		expectedOrderList.add("s195u");   // "1950s"
		expectedOrderList.add("r1900");   // "1900"
		expectedOrderList.add("s190u");   // "1900s"
		expectedOrderList.add("pubDate19uu");   // "20th century" 
		expectedOrderList.add("p19uu");   // "20th century"
		expectedOrderList.add("pubDate16uu");   // "17th century" 
		expectedOrderList.add("pubDate13uu");   // "14th century" 
		expectedOrderList.add("pubDate12uu");   // "13th century" 
		expectedOrderList.add("pubDate11uu");   // "12th century" 
		expectedOrderList.add("pubDate10uu");   // "11th century" 
		expectedOrderList.add("pubDate0963");   // 0963
		expectedOrderList.add("pubDate0960");   // 0960
		expectedOrderList.add("pubDate09uu");   // "10th century"
		expectedOrderList.add("pubDate0801");   // 0801
		expectedOrderList.add("pubDate08uu");   // "9th century"
		expectedOrderList.add("pubDate0500");   // 0500
		expectedOrderList.add("pubDate03uu");   // "4th century"
		expectedOrderList.add("pubDate02uu");   // "3rd century"
		expectedOrderList.add("pubDate01uu");   // "2nd century"
		expectedOrderList.add("pubDate00uu");   // "1st century"

		// TODO: invalid/missing dates are designated as last or first in solr
		//  schema file, but are last here in forward sort order (as this is 
		//  lucene context).  In this test, we are doing a "reverse" lucene
		//  sort, so they show as last for this test, in rever doc id order.
		expectedOrderList.add("pubDate1uuu"); 
		expectedOrderList.add("pubDate9999"); 
		expectedOrderList.add("pubDate6666"); 
		expectedOrderList.add("pubDate0000"); 
		expectedOrderList.add("pubDate0019"); 			
		
		// get search results sorted by pub_date_sort field
		List<Document> results = getReverseSortedDocs("collection", "Catalog", "pub_date_sort", sis);
		Document firstDoc = results.get(0);
		if (firstDoc.getField("id") != null) {
			String firstDocId = firstDoc.getField("id").stringValue();
			assertTrue("0000 pub date should not sort first", firstDocId != "pubDate0000");
		}		
		
		// we know we have documents that are not in the expected order list
		int expDocIx = 0;
		for (Document doc : results) 
		{
			if (expDocIx < expectedOrderList.size() - 1) 
			{
				// we haven't found all docs in the expected list yet
				Field f = doc.getField("id");  // pub_date_sort isn't stored
				if (f != null) 
				{
					String docId = f.stringValue();
					if (docId.equals(expectedOrderList.get(expDocIx + 1))) 
						expDocIx++;
				}
			}
			else break;  // we found all the documents in the expected order list
		}		

		if (expDocIx != expectedOrderList.size() - 1) 
		{
			String lastCorrDocId = expectedOrderList.get(expDocIx);
			fail("Publish Date Asc Sort Order is incorrect.  Last correct document was " + lastCorrDocId);
		}
	}


	/**
	 * test pub_date_group_facet field population.
	 */
@Test
	public final void testPubDateGroupFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date_group_facet";
		createIxInitVars("pubDateTests.mrc");
		assertFacetFldProps(fldName, solrCore, sis);
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("pubDate2010");
		docIds.add("z2009");
		docIds.add("b2008");
		assertSearchResults(fldName, "\"" + PubDateGroup.THIS_YEAR.toString() + "\"", docIds, sis);
		docIds.add("v2007");
		docIds.add("z2006");
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_3_YEARS.toString() + "\"", docIds, sis);
		docIds.add("j2005");
		docIds.add("q2001");
		docIds.add("f2000");
		docIds.add("firstDateOnly008"); //2000
		docIds.add("w1999");
		docIds.add("x200u");
		docIds.add("pubDate20uu");  
		docIds.add("o20uu");
		docIds.add("pubDate0059");  // 2005
		docIds.add("pubDate0204");  // 2004
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_10_YEARS.toString() + "\"", docIds, sis);
		docIds.add("c1998");
		docIds.add("e1997");
		docIds.add("m1991");
		docIds.add("k1990");
		docIds.add("b199u");
		docIds.add("y1989");
		docIds.add("contRes");  // 1984
		docIds.add("bothDates008"); // 1964
		docIds.add("w1959");
		docIds.add("pubDate19uu");
		docIds.add("p19uu");
		docIds.add("pubDate0197-1");
		docIds.add("pubDate0197-2");
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_50_YEARS.toString() + "\"", docIds, sis);

		docIds.clear();
		docIds.add("pubDate00uu");   // "1st century"
		docIds.add("pubDate01uu");   // "2nd century"
		docIds.add("pubDate02uu");   // "3rd century"
		docIds.add("pubDate03uu");   // "4th century"
		docIds.add("pubDate08uu");   // "9th century"
		docIds.add("pubDate09uu");   // "10th century"
		docIds.add("pubDate10uu");   // "11th century" 
		docIds.add("pubDate11uu");   // "12th century" 
		docIds.add("pubDate12uu");   // "13th century" 
		docIds.add("pubDate13uu");   // "14th century" 
		docIds.add("pubDate16uu");   // "17th century" 
		docIds.add("s190u");   // "1900s"
		docIds.add("b1899"); 
		docIds.add("r1900");
		docIds.add("pubDate195u");   // "1950s"
		docIds.add("s195u");   // "1950s"
		docIds.add("g1958");
		docIds.add("pubDate0500");
		docIds.add("pubDate0801");
		docIds.add("pubDate0960");
		docIds.add("pubDate0963");
		// TODO: would like to correct these (see autocorrect test)
		docIds.add("pubDate0965"); // should be 1965
		docIds.add("pubDate0980"); // should be 1980
		docIds.add("pubDate0999"); // should be 1999
		assertSearchResults(fldName, "\"" + PubDateGroup.MORE_THAN_50_YEARS_AGO.toString() + "\"", docIds, sis);
	}


	/**
	 * test pub_date_facet field population.
	 */
@Test
	public final void testPubDateFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date";
		createIxInitVars("pubDateTests.mrc");
        assertStringFieldProperties(fldName, solrCore, sis);
        assertFieldIndexed(fldName, solrCore);
        assertFieldStored(fldName, solrCore);
		
		pubDateSearchTests(fldName);
	}


	/**
	 * test pub_date_display field population.
	 */
@Test
	public final void testPubDateDisplay() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date";
		createIxInitVars("pubDateTests.mrc");
        assertStringFieldProperties(fldName, solrCore, sis);
        assertFieldIndexed(fldName, solrCore);
        assertFieldStored(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);		

		assertDocHasFieldValue("firstDateOnly008", fldName, "2000", sis); 
		assertDocHasFieldValue("bothDates008", fldName, "1964", sis); 
		assertDocHasFieldValue("contRes", fldName, "1984", sis); 
		assertDocHasFieldValue("pubDate195u", fldName, "1950s", sis);
		assertDocHasFieldValue("pubDate00uu", fldName, "1st century", sis); 
		assertDocHasFieldValue("pubDate01uu", fldName, "2nd century", sis); 
		assertDocHasFieldValue("pubDate02uu", fldName, "3rd century", sis); 
		assertDocHasFieldValue("pubDate03uu", fldName, "4th century", sis);
		assertDocHasFieldValue("pubDate08uu", fldName, "9th century", sis);
		assertDocHasFieldValue("pubDate09uu", fldName, "10th century", sis);
		assertDocHasFieldValue("pubDate10uu", fldName, "11th century", sis); 
		assertDocHasFieldValue("pubDate11uu", fldName, "12th century", sis); 
		assertDocHasFieldValue("pubDate12uu", fldName, "13th century", sis); 
		assertDocHasFieldValue("pubDate13uu", fldName, "14th century", sis); 
		assertDocHasFieldValue("pubDate16uu", fldName, "17th century", sis); 
		assertDocHasFieldValue("pubDate19uu", fldName, "20th century", sis); 
		assertDocHasFieldValue("pubDate20uu", fldName, "21st century", sis); 

		// No pub date when unknown
		assertDocHasNoField("bothDatesBlank", fldName, sis); 
		assertDocHasNoField("pubDateuuuu", fldName, sis); 
		// xuuu is unassigned
		assertDocHasNoFieldValue("pubDate1uuu", fldName, "after 1000", sis); 
		assertDocHasNoField("pubDate1uuu", fldName, sis); 
		
		// future dates are ignored
		assertDocHasNoField("pubDate21uu", fldName, sis);   // ignored, not "22nd century" 
		assertDocHasNoField("pubDate22uu", fldName, sis);   // ignored, not "23rd century" 
		assertDocHasNoField("pubDate23uu", fldName, sis);   // ignored, not "24th century" 
		assertDocHasNoField("pubDate9999", fldName, sis);   // ignored, not 9999
		assertDocHasNoField("pubDate99uu", fldName, sis);   // ignored, not "100th century' 
		assertDocHasNoField("pubDate6666", fldName, sis);   // ignored, not 6666
		assertDocHasNoField("pubDate861u", fldName, sis);   // ignored, not 8610s
	}


	private void pubDateSearchTests(String fldName)
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("bothDates008", fldName, "\"1964\"", sis);
		assertSingleResult("pubDate01uu", fldName, "\"2nd century\"", sis);
		assertSingleResult("pubDate2010", fldName, "\"2010\"", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("s195u");
		docIds.add("pubDate195u");
		assertSearchResults(fldName, "\"1950s\"", docIds, sis);
		docIds.clear();
		docIds.add("p19uu");
		docIds.add("pubDate19uu");
		assertSearchResults(fldName, "\"20th century\"", docIds, sis);

		assertZeroResults(fldName, "\"after 1000\"", sis);
		// future dates are ignored/skipped
		assertZeroResults(fldName, "\"6666\"", sis);
		assertZeroResults(fldName, "\"8610s\"", sis);
		assertZeroResults(fldName, "\"9999\"", sis);
		assertZeroResults(fldName, "\"23rd century\"", sis);
		assertZeroResults(fldName, "\"22nd century\"", sis);

		// dates before 500 are ignored/skipped
		assertZeroResults(fldName, "\"0000\"", sis);
		assertZeroResults(fldName, "\"0019\"", sis);

		// corrected values
		docIds.clear();
		docIds.add("pubDate0059");
		docIds.add("j2005");
		assertSearchResults(fldName, "2005", docIds, sis);
		docIds.clear();
		docIds.add("pubDate0197-1");
		docIds.add("pubDate0197-2");
		assertSearchResults(fldName, "1970", docIds, sis);
	}

}
