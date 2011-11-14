package edu.stanford;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

//import org.apache.lucene.document.*;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.*;

import edu.stanford.StanfordIndexer.PubDateGroup;

/**
 * junit4 tests for Stanford University publication fields for blacklight index
 * @author Naomi Dushay
 */
public class PublicationTests extends AbstractStanfordBlacklightTest 
{
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("pubDateTests.mrc");
		mappingTestInit();
	}


	/**
	 * assure publication field is populated correctly  
	 */
@Test
	public void testPublication()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_search";
//		assertTextFieldProperties(fldName);
//		assertFieldOmitsNorms(fldName);
//		assertFieldMultiValued(fldName);
//		assertFieldIndexed(fldName);
		String publTestFilePath = testDataParentPath + File.separator + "publicationTests.mrc";

		// 260ab
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260ababc", fldName, "Paris : Gauthier-Villars ; Chicago : University of Chicago Press");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260abbc", fldName, "Washington, D.C. : first b : second b U.S. G.P.O.");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260ab3", fldName, "London : Vogue");
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260crightbracket", fldName, "[i.e. Bruxelles : Moens");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260crightbracket", fldName, "i.e. Bruxelles : Moens");
		
		// 260a contains s.l. (unknown - sin location, presumably)
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260aunknown", fldName, "[S.l.] : Insight Press");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260aunknown", fldName, "Insight Press");
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260abaslbc", fldName, "[Philadelphia] : Some name [s.l.] : another name");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260abaslbc", fldName, "[Philadelphia] : Some name another name");		
		
		// 260b contains s.n. (unknown - sin name, presumably)
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260bunknown", fldName, "Victoria, B.C. : [s.n.]");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260bunknown", fldName, "Victoria, B.C.");

		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260abunknown", fldName, "[S.l. : s.n.");
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260abunknown", fldName, "S.l. : s.n.");
		
		// test searching
		createIxInitVars("publicationTests.mrc");
		assertSingleResult("260aunknown", fldName, "Insight");
		assertSingleResult("260bunknown", fldName, "victoria"); // downcased
		
		// these codes should be skipped
		assertDocHasNoField("260abunknown", fldName);  // 260a s.l, 260b s.n.
		assertZeroResults(fldName, "s.l.");
		assertZeroResults(fldName, "s.n.");
	}
	
	/**
	 * assure publication field is populated correctly  
	 */
@Test
	public void testVernPublication()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_pub_search";
//		assertTextFieldProperties(fldName);
//		assertFieldOmitsNorms(fldName);
//		assertFieldMultiValued(fldName);
//		assertFieldIndexed(fldName);
		String publTestFilePath = testDataParentPath + File.separator + "publicationTests.mrc";
	
		// 260ab from 880
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "vern260abc", fldName, "vern260a : vern260b");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "vern260abcg", fldName, "vern260a : vern260b");

		// test searching
		createIxInitVars("publicationTests.mrc");
		Set<String> docIds = new HashSet<String>();
		docIds.add("vern260abc");
		docIds.add("vern260abcg");
		assertSearchResults(fldName, "vern260a", docIds);
	}


	/**
	 * assure publication country field is populated correctly  
	 */
@Test
	public void testPublicationCountry()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_country";
//		assertTextFieldProperties(fldName);
//		assertFieldOmitsNorms(fldName);
//		assertFieldNotMultiValued(fldName);
//		assertFieldIndexed(fldName);
		String pubTestFilePath = testDataParentPath + File.separator + "publicationTests.mrc";

		// 008[15-17]  via translation map
		solrFldMapTest.assertSolrFldValue(pubTestFilePath, "008mdu", fldName, "Maryland, United States");
		solrFldMapTest.assertSolrFldValue(pubTestFilePath, "008ja", fldName, "Japan");
		
		// these codes should be skipped
		createIxInitVars("publicationTests.mrc");
		assertDocHasNoField("008vp", fldName);  // "Various places"
		assertDocHasNoField("008xx", fldName);  // "No place, unknown, or undetermined"
	}
	

	/**
	 * assure pub dates later than current year +1 are ignored
	 */
@Test
	public void testPubDateTooLate()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date";
		assertZeroResults(fldName, "9999");
		assertZeroResults(fldName, "6666");
		assertZeroResults(fldName, "22nd century");
		assertZeroResults(fldName, "23rd century");
		assertZeroResults(fldName, "24th century");
		assertZeroResults(fldName, "8610s");
	}


	/**
	 * assure pub dates of < 500 are ignored
	 */
@Test
	public void testPubDateTooEarly()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date";
		assertZeroResults(fldName, "0000");
		assertZeroResults(fldName, "0019");
		assertZeroResults(fldName, "0059");
		assertZeroResults(fldName, "0197");
		assertZeroResults(fldName, "0204");
	}

	/**
	 * test that auto-correction of pub date in 008 by checking value in 260c
	 */
@Test
	public void testPubDateAutoCorrect()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date";
		
		assertDocHasNoFieldValue("pubDate0059", fldName, "0059"); 
		assertDocHasFieldValue("pubDate0059", fldName, "2005"); 
		
		assertDocHasNoFieldValue("pubDate0197-1", fldName, "0197"); 
//		assertDocHasFieldValue("pubDate0197-1", fldName, "1970s"); 
		assertDocHasFieldValue("pubDate0197-1", fldName, "1970"); 
		assertDocHasNoFieldValue("pubDate0197-2", fldName, "0197"); 
//		assertDocHasFieldValue("pubDate0197-2", fldName, "1970s"); 
		assertDocHasFieldValue("pubDate0197-2", fldName, "1970"); 

		// correct
		assertDocHasFieldValue("pubDate0500", fldName, "0500"); 
		assertDocHasFieldValue("pubDate0801", fldName, "0801"); 
		assertDocHasFieldValue("pubDate0960", fldName, "0960"); 
		assertDocHasFieldValue("pubDate0963", fldName, "0963"); 

		assertDocHasNoFieldValue("pubDate0204", fldName, "0204"); 
		assertDocHasFieldValue("pubDate0204", fldName, "2004"); 

		assertDocHasNoFieldValue("pubDate0019", fldName, "0019"); 
		// TODO: yeah, i wish ...
//		assertDocHasFieldValue("pubDate0019", fldName, "20th century"); 
//		assertDocHasNoFieldValue("pubDate0965", fldName, "0965"); 
//		assertDocHasFieldValue("pubDate0965", fldName, "1965");
//		assertDocHasNoFieldValue("pubDate0980", fldName, "0980"); 
//		assertDocHasFieldValue("pubDate0980", fldName, "1980"); 
//		assertDocHasNoFieldValue("pubDate0999", fldName, "0999"); 
//		assertDocHasFieldValue("pubDate0999", fldName, "1999"); 
		assertDocHasNoField("410024", fldName);
	}


	/**
	 * test pub_date_search field population.
	 */
@Test
	public final void testPubDateSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "pub_date_search";
//		assertTextFieldProperties(fldName);
//		assertFieldNotMultiValued(fldName);
//		assertFieldNotStored(fldName);
//		assertFieldIndexed(fldName);
//		assertFieldOmitsNorms(fldName);
	
		pubDateSearchTests(fldName);
	}


	/**
	 * test pub_date_sort field population and ascending sort.
	 */
@Test
	public final void testPubDateSortAsc() 
			throws ParserConfigurationException, IOException, SAXException, InvocationTargetException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException 
	{
		String fldName = "pub_date_sort";
//		assertSortFldProps(fldName);
	
		// list of doc ids in correct publish date sort order
		List<String> expectedOrderList = new ArrayList<String>(50);
		
		
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
        expectedOrderList.add("pubDate0965");   // 0963
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
        
		// TODO: invalid/missing dates are designated as last or first in solr
        //  schema file, but are first here (as this is lucene context).
        //  "ties" show up in document order here (order of occurrence in data 
        // file)
        expectedOrderList.add("pubDate1uuu"); 
        expectedOrderList.add("pubDate9999"); 
        expectedOrderList.add("pubDate6666"); 
        expectedOrderList.add("pubDate0000"); 
        expectedOrderList.add("pubDate0019"); 
        expectedOrderList.add("410024"); 
		
		// get search results sorted by pub_date_sort field
		// pub_date_sort isn't stored, so we must look at id field		
		
	    SolrDocumentList docList = getAscSortDocs("collection", "sirsi", "pub_date_sort");

		
		// we know we have documents that are not in the expected order list
		int expDocIx = 0;
		boolean first = true;
		for (SolrDocument doc : docList) 
		{
			if (first)
			{
			    assertTrue("9999 pub date should not sort first", doc.getFirstValue(docIDfname).toString() != "pubDate9999");
			    first = false;
			}
		    if (expDocIx < expectedOrderList.size() - 1) 
			{
				// we haven't found all docs in the expected list yet
			    String docId = doc.getFieldValue(docIDfname).toString();
	            if (docId.equals(expectedOrderList.get(expDocIx ))) 
	                expDocIx++;
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
	 * pub date descending sort should start with oldest and go to newest 
	 *  (missing dates sort order tested in another method)
	 */
@Test
	public void testPubDateSortDesc()
			throws ParserConfigurationException, IOException, SAXException, NoSuchMethodException, InstantiationException, InvocationTargetException, ClassNotFoundException, IllegalAccessException 
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
        expectedOrderList.add("410024"); 
		
        // get search results sorted by pub_date_sort field
        // pub_date_sort isn't stored, so we must look at id field      
        
        SolrDocumentList docList = getDescSortDocs("collection", "sirsi", "pub_date_sort");

        
        // we know we have documents that are not in the expected order list
        int expDocIx = 0;
        boolean first = true;
        for (SolrDocument doc : docList) 
        {
            if (first)
            {
                assertTrue("0 pub date should not sort first", doc.getFirstValue(docIDfname).toString() != "pubDate0000");
                first = false;
            }
            if (expDocIx < expectedOrderList.size() - 1) 
            {
                // we haven't found all docs in the expected list yet
                String docId = doc.getFieldValue(docIDfname).toString();
                if (docId.equals(expectedOrderList.get(expDocIx + 1))) 
                    expDocIx++;
            }
            else break;  // we found all the documents in the expected order list
        }       

		if (expDocIx != expectedOrderList.size() - 1) 
		{
			String lastCorrDocId = expectedOrderList.get(expDocIx);
			fail("Publish Date Desc Sort Order is incorrect.  Last correct document was " + lastCorrDocId);
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
//		assertFacetFieldProperties(fldName);
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("pubDate2010");
		assertSearchResults(fldName, "\"" + PubDateGroup.THIS_YEAR.toString() + "\"", docIds);
        docIds.add("z2009");
        docIds.add("b2008");
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_3_YEARS.toString() + "\"", docIds);
        docIds.add("v2007");
        docIds.add("z2006");
		docIds.add("j2005");
		docIds.add("q2001");
		docIds.add("x200u");
		docIds.add("pubDate20uu");  
		docIds.add("o20uu");
		docIds.add("pubDate0059");  // 2005
		docIds.add("pubDate0204");  // 2004
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_10_YEARS.toString() + "\"", docIds);
		docIds.add("f2000");
		docIds.add("firstDateOnly008"); //2000
		docIds.add("w1999");
		docIds.add("c1998");
		docIds.add("e1997");
		docIds.add("m1991");
		docIds.add("k1990");
		docIds.add("b199u");
		docIds.add("y1989");
		docIds.add("contRes");  // 1984
		docIds.add("bothDates008"); // 1964
        docIds.add("pubDate19uu");
        docIds.add("pubDate195u");
        docIds.add("p19uu");
        docIds.add("s195u");
		docIds.add("pubDate0197-1");
		docIds.add("pubDate0197-2");
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_50_YEARS.toString() + "\"", docIds);

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
//		docIds.add("pubDate195u");   // "1950s"
//		docIds.add("s195u");   // "1950s"
		docIds.add("g1958");
        docIds.add("w1959");
		docIds.add("pubDate0500");
		docIds.add("pubDate0801");
		docIds.add("pubDate0960");
        docIds.add("pubDate0963");
//		// TODO: would like to correct these (see autocorrect test)
		docIds.add("pubDate0965"); // should be 1965
		docIds.add("pubDate0980"); // should be 1980
		docIds.add("pubDate0999"); // should be 1999
		assertSearchResults(fldName, "\"" + PubDateGroup.MORE_THAN_50_YEARS_AGO.toString() + "\"", docIds);
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
//        assertStringFieldProperties(fldName);
//        assertFieldIndexed(fldName);
//        assertFieldStored(fldName);
		
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
//        assertStringFieldProperties(fldName);
//        assertFieldIndexed(fldName);
//        assertFieldStored(fldName);
//		assertFieldNotMultiValued(fldName);		

		assertDocHasFieldValue("firstDateOnly008", fldName, "2000"); 
		assertDocHasFieldValue("bothDates008", fldName, "1964"); 
		assertDocHasFieldValue("contRes", fldName, "1984"); 
		assertDocHasFieldValue("pubDate195u", fldName, "1950s");
		assertDocHasFieldValue("pubDate00uu", fldName, "1st century"); 
		assertDocHasFieldValue("pubDate01uu", fldName, "2nd century"); 
		assertDocHasFieldValue("pubDate02uu", fldName, "3rd century"); 
		assertDocHasFieldValue("pubDate03uu", fldName, "4th century");
		assertDocHasFieldValue("pubDate08uu", fldName, "9th century");
		assertDocHasFieldValue("pubDate09uu", fldName, "10th century");
		assertDocHasFieldValue("pubDate10uu", fldName, "11th century"); 
		assertDocHasFieldValue("pubDate11uu", fldName, "12th century"); 
		assertDocHasFieldValue("pubDate12uu", fldName, "13th century"); 
		assertDocHasFieldValue("pubDate13uu", fldName, "14th century"); 
		assertDocHasFieldValue("pubDate16uu", fldName, "17th century"); 
		assertDocHasFieldValue("pubDate19uu", fldName, "20th century"); 
		assertDocHasFieldValue("pubDate20uu", fldName, "21st century"); 

		// No pub date when unknown
		assertDocHasNoField("bothDatesBlank", fldName); 
		assertDocHasNoField("pubDateuuuu", fldName); 
		// xuuu is unassigned
		assertDocHasNoFieldValue("pubDate1uuu", fldName, "after 1000"); 
		assertDocHasNoField("pubDate1uuu", fldName); 
		
		// future dates are ignored
		assertDocHasNoField("pubDate21uu", fldName);   // ignored, not "22nd century" 
		assertDocHasNoField("pubDate22uu", fldName);   // ignored, not "23rd century" 
		assertDocHasNoField("pubDate23uu", fldName);   // ignored, not "24th century" 
		assertDocHasNoField("pubDate9999", fldName);   // ignored, not 9999
		assertDocHasNoField("pubDate99uu", fldName);   // ignored, not "100th century' 
		assertDocHasNoField("pubDate6666", fldName);   // ignored, not 6666
		assertDocHasNoField("pubDate861u", fldName);   // ignored, not 8610s
	}


	private void pubDateSearchTests(String fldName)
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("bothDates008", fldName, "\"1964\"");
		assertSingleResult("pubDate01uu", fldName, "\"2nd century\"");
		assertSingleResult("pubDate2010", fldName, "\"2010\"");
		Set<String> docIds = new HashSet<String>();
		docIds.add("s195u");
		docIds.add("pubDate195u");
		assertSearchResults(fldName, "\"1950s\"", docIds);
		docIds.clear();
		docIds.add("p19uu");
		docIds.add("pubDate19uu");
		assertSearchResults(fldName, "\"20th century\"", docIds);

		assertZeroResults(fldName, "\"after 1000\"");
		// future dates are ignored/skipped
		assertZeroResults(fldName, "\"6666\"");
		assertZeroResults(fldName, "\"8610s\"");
		assertZeroResults(fldName, "\"9999\"");
		assertZeroResults(fldName, "\"23rd century\"");
		assertZeroResults(fldName, "\"22nd century\"");

		// dates before 500 are ignored/skipped
		assertZeroResults(fldName, "\"0000\"");
		assertZeroResults(fldName, "\"0019\"");

		// corrected values
		docIds.clear();
		docIds.add("pubDate0059");
		docIds.add("j2005");
		assertSearchResults(fldName, "2005", docIds);
		docIds.clear();
		docIds.add("pubDate0197-1");
		docIds.add("pubDate0197-2");
		assertSearchResults(fldName, "1970", docIds);
	}

}
