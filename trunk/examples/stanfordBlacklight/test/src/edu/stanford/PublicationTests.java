package edu.stanford;

import static org.junit.Assert.fail;

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
	
	/**
	 * test pub_date_search field population.
	 */
@Test
	public final void testPubDateSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date_search";
		createIxInitVars("pubDateTests.mrc");
		// may become multivalued eventually 
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		assertFieldOmitsNorms(fldName, solrCore);
	
		testPubDate(fldName);
	}


	/**
	 * test pub_date_sort field population.
	 */
@Test
	public final void testPubDateSort() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date_sort";
		createIxInitVars("pubDateTests.mrc");
		assertSortFldProps(fldName, solrCore, sis);
	
// FIXME:  need secondary sort, and need to know how to deal with docs without a pub date assigned
	
		// list of doc ids in correct publish date sort order
		List<String> expectedOrderList = new ArrayList<String>(50);
		expectedOrderList.add("pubDate00uu");   // "1st century"
		expectedOrderList.add("pubDate01uu");   // "2nd century"
		expectedOrderList.add("pubDate02uu");   // "3rd century"
		expectedOrderList.add("pubDate03uu");   // "4th century"
		expectedOrderList.add("pubDate08uu");   // "9th century"
		expectedOrderList.add("pubDate09uu");   // "10th century"
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
		expectedOrderList.add("j2005");   // "2005"
		expectedOrderList.add("z2006");   // "2006"
		expectedOrderList.add("v2007");   // "2007"
		expectedOrderList.add("b2008");   // "2008"
		expectedOrderList.add("z2009");   // "2009"		
		expectedOrderList.add("pubDate21uu");   // "22nd century" 
		expectedOrderList.add("pubDate22uu");   // "23rd century" 
		expectedOrderList.add("pubDate23uu");   // "24th century" 
		
		
		// get search results sorted by pub_date_sort field
		List<Document> results = getSortedDocs("collection", "Catalog", "pub_date_sort", sis);
		
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

		// put future dates somewhere for error spotting
		docIds.add("pubDate21uu");   // "21st century"
		docIds.add("pubDate22uu");   // "22nd century" 
		docIds.add("pubDate23uu");   // "23rd century" 
				
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
		
		testPubDate(fldName);
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
		// may become multivalued eventually 
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
		assertDocHasFieldValue("pubDate21uu", fldName, "22nd century", sis); 
		assertDocHasFieldValue("pubDate22uu", fldName, "23rd century", sis); 
		assertDocHasFieldValue("pubDate23uu", fldName, "24th century", sis); 
// TODO:  No pub date when unknown?  or "unknown"?
		assertDocHasNoField("bothDatesBlank", fldName, sis); 
		assertDocHasNoField("pubDateuuuu", fldName, sis); 
		// decided to make xuuu also unassigned
		assertDocHasNoFieldValue("pubDate1uuu", fldName, "after 1000", sis); 
		assertDocHasNoField("pubDate1uuu", fldName, sis); 
	}


	/**
	 * Test publication_display field 
	 */
@Test
	public final void testPublicationDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "publication_display";
		createIxInitVars("displayFieldsTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2601", fldName, "Paris : Impr. Vincent, 1798 [i.e. Bruxelles : Moens, 1883]", sis); 
	}


	private void testPubDate(String fldName)
			throws ParserConfigurationException, IOException, SAXException 
	{
		// field is indexed - search for values
		assertSingleResult("bothDates008", fldName, "\"1964\"", sis);
		assertSingleResult("pubDate01uu", fldName, "\"2nd century\"", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("s195u");
		docIds.add("pubDate195u");
		assertSearchResults(fldName, "\"1950s\"", docIds, sis);
		docIds.clear();
		docIds.add("p19uu");
		docIds.add("pubDate19uu");
		assertSearchResults(fldName, "\"20th century\"", docIds, sis);

		assertZeroResults(fldName, "\"after 1000\"", sis);
	}

}
