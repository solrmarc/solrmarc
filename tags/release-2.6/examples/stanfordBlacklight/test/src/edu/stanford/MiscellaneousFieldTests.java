package edu.stanford;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.*;

import static org.junit.Assert.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 */
public class MiscellaneousFieldTests extends AbstractStanfordBlacklightTest {
	
	/**
	 * Test correct document id - the id is from 001 with an a in front
	 */
@Test 
	public final void testId() 
		throws ParserConfigurationException, SAXException, IOException
	{
		String fldName = "id";
		createIxInitVars("idTests.mrc");
//		assertTextFieldProperties(fldName);
//		assertFieldNotMultiValued(fldName);		
//		assertFieldStored(fldName);
//		assertFieldIndexed(fldName);
		
        int numDocs = getNumMatchingDocs("collection", "sirsi");
        assertEquals("Number of documents in index incorrect: ", 3, numDocs);
        assertDocNotPresent("001noSubNo004");
        assertDocPresent("001suba");
        assertDocNotPresent("001and004nosub");
        assertDocNotPresent("004noSuba");
        assertDocPresent("001subaAnd004nosub");
        assertDocNotPresent("004noSuba");
        assertDocPresent("001subaAnd004suba");
        assertDocNotPresent("004suba");
        
        assertSingleResult("001suba", fldName, "\"001suba\"");
        assertSingleResult("001subaAnd004nosub", fldName, "\"001subaAnd004nosub\"");
        assertSingleResult("001subaAnd004suba", fldName, "\"001subaAnd004suba\"");
	}
	

	/**
	 * Test that there is no field created when the map is missing
	 *  the value to be mapped and when the map has value set to null
	 */
@Test
	public final void testMapMissingValue() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "language";
		createIxInitVars("langTests.mrc");
	
		assertZeroResults(fldName, "null");
		assertZeroResults(fldName, "\\?\\?\\?");
		assertZeroResults(fldName, "mis");     // 008mis041ak
		assertZeroResults(fldName, "Miscellaneous languages");
		assertZeroResults(fldName, "mul");     // 008mul041atha
		assertZeroResults(fldName, "Multiple languages"); 
		assertZeroResults(fldName, "und");
		assertZeroResults(fldName, "zxx");
	}

	
	/**
	 * Test population of allfields
	 */
@Test
	public final void testAllSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "all_search";
		createIxInitVars("allfieldsTests.mrc");
//		assertSearchFldOneValProps(fldName);
		
		String docId = "allfields1";

		// 245 just for good measure
        assertSingleResult(docId, fldName, "should"); 
        
        // 0xx fields are not included except 024, 027, 028
        assertSingleResult(docId, fldName, "2777802000"); // 024
        assertSingleResult(docId, fldName, "90620"); // 024
        assertSingleResult(docId, fldName, "technical"); // 027
        assertSingleResult(docId, fldName, "vibrations"); // 027
        assertZeroResults(fldName, "ocolcm");  // 035
        assertZeroResults(fldName, "orlob");  // 040

        // 3xx fields ARE included
        assertSingleResult(docId, fldName, "sound"); // 300
        assertSingleResult(docId, fldName, "annual");  // 310
        
        // 6xx subject fields - we're including them, even though
        // fulltopic is all subfields of all 600, 610, 630, 650, 655
        // fullgeographic is all subfields of all 651
        //   b/c otherwise standard numbers and other things are doubled here,
        //   but topics are not.
        
        // 9xx fields are NOT included
        assertZeroResults(fldName, "EDATA");  // 946
        assertZeroResults(fldName, "pamphlet");  // 947
        assertZeroResults(fldName, "stacks");  // 999
	}


	/**
	 * raw marc display field should have appropriate properties.
	 */
@Test
	public final void testMarc21Field() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("allfieldsTests.mrc");
	    String fldName = "marc21";
//	    assertDisplayFieldProperties(fldName);
//	    assertFieldNotMultiValued(fldName);
	}


	/**
	 * open search field should be stored, not indexed
	 */
@Test
	public final void testOpenSearch() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("allfieldsTests.mrc");
	    String fldName = "open_search";
//	    assertDisplayFieldProperties(fldName);
	}

	/**
	 * spell fields should be stored and indexed
	 */
@Test
	public final void testSpellFields() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("allfieldsTests.mrc");
		Set<String> fields = new HashSet<String>(3);
		fields.add("spell");
		fields.add("spell_title");
		fields.add("spell_author");
		fields.add("spell_subject");
		for (String fldName : fields) {
//		    assertTextFieldProperties(fldName);
//		    assertFieldOmitsNorms(fldName);
//		    assertFieldIndexed(fldName);
//		    assertFieldStored(fldName);
		}
//		assertFieldNotMultiValued("spell");
//		assertFieldMultiValued("spell_title");
//		assertFieldMultiValued("spell_author");
//		assertFieldMultiValued("spell_subject");
	}


	/**
	 * test preservation of field ordering from marc21 input to marc21 stored in record
	 */
//@Test
	public final void testFieldOrdering() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("fieldOrdering.mrc");
		SolrDocumentList docList = this.getDocList(docIDfname, "1");
//		int solrDocNum = getSingleDocNum(docIDfname, "1");
//		DocumentProxy doc = getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum);
		SolrDocument doc = docList.iterator().next();
		String marc21 = doc.getFieldValue("marc21").toString();
		int ix650 = marc21.indexOf("650first");
		int ix600 = marc21.indexOf("600second");
		assertTrue("fields are NOT in the original order", ix650 < ix600);
	}
	

	/**
	 * Hokey horrible way to create a test index.
	 */
/*
//@Test
    public final void makeBLIndex() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("unicornWHoldings.mrc");
	}
*/

}
