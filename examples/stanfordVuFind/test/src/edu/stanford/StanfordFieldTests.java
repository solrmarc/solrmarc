package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

import static org.junit.Assert.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 *
 */
public class StanfordFieldTests extends AbstractStanfordVufindTest {
	
	/**
	 * Test correct document id - the id is from 001 with an a in front
	 */
@Test 
	public final void testId() 
		throws ParserConfigurationException, SAXException, IOException
	{
		String fldName = "id";
		createIxInitVars("idTests.mrc");
		// assert field has correct properties
		assertStringFieldProperties(fldName);
		assertFieldNotMultiValued(fldName);		
		assertFieldStored(fldName);
		assertFieldIndexed(fldName);
		
//        int numDocs = sis.getReader().numDocs();
//        assertEquals("Number of documents in index incorrect: ", 3, numDocs);
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
	 * Test method for {@link edu.stanford.StanfordIndexer#getPubDate(org.marc4j.marc.Record)}.
	 */
@Test
	public final void testGetPubDate() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "publishDate";
		createIxInitVars("pubDateTests.mrc");
		assertStringFieldProperties(fldName);
		// may become multivalued eventually - vanilla VuFind has it that way
		assertFieldNotMultiValued(fldName);		
		assertFieldStored(fldName);
		// indexed for GetMoreLikeThis search
		assertFieldIndexed(fldName);

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
		assertDocHasFieldValue("pubDate21uu", fldName, "22nd century"); 
		assertDocHasFieldValue("pubDate22uu", fldName, "23rd century"); 
		assertDocHasFieldValue("pubDate23uu", fldName, "24th century"); 
// TODO:  No pub date when unknown?  or "unknown"?
		assertDocHasNoField("bothDatesBlank", fldName); 
		assertDocHasNoField("pubDateuuuu", fldName); 
		// decided to make xuuu also unassigned
		assertDocHasNoFieldValue("pubDate1uuu", fldName, "after 1000"); 
		assertDocHasNoField("pubDate1uuu", fldName); 
		
		// field is indexed - search for values
        assertSingleResult("bothDates008", fldName, "\"1964\"");
        assertSingleResult("pubDate195u", fldName, "\"1950s\"");
        assertSingleResult("pubDate01uu", fldName, "\"2nd century\"");
        assertSingleResult("pubDate19uu", fldName, "\"20th century\"");
        assertZeroResults(fldName, "\"after 1000\"");
	}

	/**
	 * Test population of language field
	 */
@Test
	public final void testLanguages() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// TODO: change this field name to language_facet
		String fldName = "language";
		createIxInitVars("langTests.mrc");
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		assertFieldStored(fldName);
		// indexed for GetMoreLikeThis search
		assertFieldIndexed(fldName);

		assertDocHasFieldValue("008spa", fldName, "Spanish"); 
		assertDocHasFieldValue("008mul041atha", fldName, "Thai"); 
		assertDocHasNoFieldValue("008mul041atha", fldName, "mul"); 
		assertDocHasNoFieldValue("008mul041atha", fldName, "Multiple languages"); 
		assertDocHasNoFieldValue("008mul041atha", fldName, "null"); 
		assertDocHasFieldValue("008eng3041a", fldName, "English"); 
		assertDocHasFieldValue("008eng3041a", fldName, "German"); 
		assertDocHasFieldValue("008eng3041a", fldName, "Russian"); 
		assertDocHasFieldValue("008eng2041a041h", fldName, "English"); 
		assertDocHasFieldValue("008eng2041a041h", fldName, "Greek, Ancient (to 1453)"); 
		assertDocHasNoFieldValue("008eng2041a041h", fldName, "Russian"); 
		assertDocHasFieldValue("008fre041d", fldName, "French"); 
		assertDocHasFieldValue("008fre041d", fldName, "Spanish"); 
		assertDocHasFieldValue("008nor041ad", fldName, "Norwegian"); 
		assertDocHasFieldValue("008nor041ad", fldName, "Swedish"); 
		assertDocHasNoFieldValue("008mis041ak", fldName, "Italian"); 
		assertDocHasNoFieldValue("008mis041ak", fldName, "mis"); 
		assertDocHasNoFieldValue("008mis041ak", fldName, "Miscellaneous languages"); 
		assertDocHasNoFieldValue("008mis041ak", fldName, "null"); 
		// cases where no field should exist
		assertDocHasNoField("008blank", fldName); 
		assertDocHasNoField("008zxx", fldName); 
		assertDocHasNoField("008und", fldName); 
		assertDocHasNoField("008mis", fldName); 
		assertDocHasNoField("008mul", fldName); 

		// field is indexed - search for values
		Set<String> docIds = new HashSet<String>();
		docIds.add("008eng3041a");
		docIds.add("008eng2041a041h");
		assertSearchResults(fldName, "English", docIds);
	}

	/**
	 * Test that there is no field created when the map is missing
	 *  the value to be mapped and when the map has value set to null
	 */
@Test
	public final void testMapMissingValue() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// TODO: change this field name to language_facet
		String fldName = "language";
		createIxInitVars("langTests.mrc");
	
		assertDocHasNoField("008blank", fldName); 
		assertDocHasNoField("008zxx", fldName); 
		assertDocHasNoField("008und", fldName); 
		assertDocHasNoField("008mis", fldName); 
		assertDocHasNoField("008mul", fldName); 

		assertZeroResults(fldName, "null");
	}


	/**
	 * Test population of physical field -- it became multiValued to avoid errors
	 */
@Test
	public final void testPhysical() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "physical";
		createIxInitVars("physicalTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldStored(fldName);
		assertFieldIndexed(fldName);

		// 300abc
        assertDocHasFieldValue("300111", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in."); 
        // 300abce
        assertDocHasFieldValue("300222", fldName, "271 p. : ill. ; 21 cm. + answer book."); 
        // 300 3afg
        assertDocHasFieldValue("300333", fldName, "1 box 2 x 4 x 3 1/2 ft."); 
        // 300aafafc - in order ...
        assertDocHasFieldValue("300444", fldName, "diary 1 volume (463 pages) ; 17 cm. x 34.5 cm."); 
        
		// string field requires exact match
		assertSingleResult("300111", fldName, "\"1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.\"");
		assertSingleResult("300222", fldName, "\"271 p. : ill. ; 21 cm. + answer book.\"");
		assertSingleResult("300333", fldName, "\"1 box 2 x 4 x 3 1/2 ft.\"");
		assertSingleResult("300444", fldName, "\"diary 1 volume (463 pages) ; 17 cm. x 34.5 cm.\"");
	}
	
	/**
	 * Test population of edition field -- it became multiValued to avoid errors
	 *  note: edition is a display field
	 */
// TODO: indexer skips over records with multiple vals for single val fields
//@Test
	public final void testEdition() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "edition";
		createIxInitVars("editionTests.mrc");
		assertFieldNotMultiValued(fldName);
		assertStringFieldProperties(fldName);
		assertFieldStored(fldName);
		assertFieldNotIndexed(fldName);

        assertDocHasFieldValue("editionMV", fldName, "Special education version"); 
        assertDocHasFieldValue("editionMV", fldName, "Medium-high voice ed."); 
	}
	
	
	/**
	 * Test population of building field (a facet)
	 */
@Test
	public final void testBuilding() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building";
		createIxInitVars("buildingTests.mrc");
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
        assertSingleResult("115472", fldName, "\"Applied Physics Department\"");
        assertSingleResult("229800", fldName, "\"Archive of Recorded Sound\"");
        assertSingleResult("345228", fldName, "\"Art & Architecture\"");
        assertSingleResult("460947", fldName, "\"Falconer (Biology)\"");
        assertSingleResult("804724", fldName, "\"Off-campus (Newark)\"");
        assertSingleResult("919006", fldName, "\"Swain (Chemistry & Chem. Engineering)\"");
        assertSingleResult("1147269", fldName, "Classics");
        assertSingleResult("1391080", fldName, "\"Green - Current Periodicals & Microtext\"");
        assertSingleResult("1505065", fldName, "\"Branner (Earth Sciences & Maps)\"");
        assertSingleResult("1618836", fldName, "\"Cubberley (Education)\"");
        assertSingleResult("1732616", fldName, "\"Mathematical & Computer Sciences\"");
        assertSingleResult("1849258", fldName, "Engineering");
        assertZeroResults(fldName, "\"Government Documents\"");
        assertSingleResult("2678655", fldName, "\"Jackson (Business)\"");
        assertSingleResult("2797607", fldName, "Meyer");
        assertSingleResult("3027805", fldName, "\"Miller (Hopkins Marine Station)\"");
        assertSingleResult("3142611", fldName, "Physics");
        assertSingleResult("3400092", fldName, "\"Hoover Institution Archives\"");
        assertSingleResult("3743949", fldName, "\"Hoover Institution\"");
        assertSingleResult("4258089", fldName, "\"Special Collections & University Archives\"");
        assertSingleResult("4428936", fldName, "\"Tanner (Philosophy)\"");
        assertSingleResult("4823592", fldName, "\"Crown (Law)\"");
        assertSingleResult("5666387", fldName, "Music");
        assertSingleResult("6676531", fldName, "\"East Asia\"");

        Set<String> docIds = new HashSet<String>();
        docIds.add("1033119");
        docIds.add("1261173");
        docIds.add("2557826");
        docIds.add("3941911");
        docIds.add("4114632");
        docIds.add("2442876");  //GRN-REF Green - Reference
        docIds.add("1111");  // ILB = Inter-Library Borrowing
        docIds.add("2222");  // SPEC-DESK = Special Collections Loan Desk
        // checked out
        docIds.add("575946");
        // NOT  3277173  (withdrawn)
        assertSearchResults(fldName, "\"Green (Humanities & Social Sciences)\"", docIds);

        docIds.clear();
        docIds.add("1033119");
        docIds.add("1962398");
        docIds.add("2328381");
        docIds.add("2913114");
        assertSearchResults(fldName, "\"Stanford Auxiliary Library (SAL 1&2)\"", docIds);

        docIds.clear();
        docIds.add("690002");
        docIds.add("2328381");
        docIds.add("3941911");
        docIds.add("7651581");
        // education - withdrawn;  SAL3 stacks
        docIds.add("2214009");
        assertSearchResults(fldName, "\"Off-campus (SAL3)\"", docIds);

        docIds.clear();
        docIds.add("7370014");
        // ask@lane
        docIds.add("7233951");
        assertSearchResults(fldName, "\"Lane (Medical)\"", docIds);

        docIds.clear();
        docIds.add("6493823");
		// INTERNET
        docIds.add("7117119");
        assertSearchResults(fldName, "\"Stanford University Libraries\"", docIds);
	}

	/**
	 * Test properties of fullrecord field
	 */
@Test
	public final void testFullrecordProps() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "fullrecord";
		createIxInitVars("physicalTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldNotMultiValued(fldName);
		assertFieldStored(fldName);
		assertFieldNotIndexed(fldName);
	}
	
	/**
	 * Test population of allfields
	 */
@Test
	public final void testAllfields() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "allfields";
		createIxInitVars("allfieldsTests.mrc");
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldNotMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
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
	 * test fields that need to be multivalued for stanford data (but aren't
	 *  in vanilla vufind schema)
	 */
	public final void testStanfordMultivals() 
			throws IOException, ParserConfigurationException, SAXException
	{
		createIxInitVars("unicornWHoldings.mrc");
/*  not any more - we'll let catalogers fix.  Hope this improves author facet a bit.
		assertFieldMultiValued("author");
		assertFieldMultiValued("author-letter"); // removed
		assertFieldMultiValued("authorStr");
		assertFieldMultiValued("title");
		assertFieldMultiValued("titleStr");
		assertFieldMultiValued("edition");
*/
		assertFieldMultiValued("physical");
		// we separate LC and dewey
/*
		assertFieldMultiValued("callnumber");
		assertFieldMultiValued("callnumber-first");
		assertFieldMultiValued("callnumber-subject");
		assertFieldMultiValued("callnumber-label");
*/
	}

	/**
	 * test that fields are not present in index.  Used to ensure VuFind fields
	 *  Stanford doesn't use are not being populated.
	 */
@Test
	public final void testRemovedFields() 
			throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("unicornWHoldings.mrc");
//		IndexReader ir = sis.getReader();//getIndexReader(solrPath, solrDataDir);
//		// none of the following fields are being used by VuFind code, but they
//		//  are either populated or at least declared in the VuFind/solr/conf/schema.xml
//		assertFieldNotPresent("institution", ir);
//		assertFieldNotPresent("langcode", ir);
//		assertFieldNotPresent("auth_title", ir);
//		assertFieldNotPresent("auth_titleStr", ir);
//		assertFieldNotPresent("auth_author", ir);
//		assertFieldNotPresent("auth_authorStr", ir);
//		assertFieldNotPresent("auth_author2", ir);
//		assertFieldNotPresent("auth_author2Str", ir);
//		assertFieldNotPresent("author2-role", ir);
//		assertFieldNotPresent("dateSpan", ir);
//		assertFieldNotPresent("description", ir);
//		assertFieldNotPresent("callnumber-a", ir);
//		assertFieldNotPresent("genre", ir);
//		assertFieldNotPresent("fullgenre", ir);
//		assertFieldNotPresent("genreStr", ir);
//		assertFieldNotPresent("subgenre", ir);
//		assertFieldNotPresent("author-letter", ir);
//		assertFieldNotPresent("publisherStr", ir);
//
	}

	/**
	 * test that explicitly named string fields in VuFind index do not have
	 *  termVectors.
	 */
@Test
	public final void testStrFldsNoTV() 
			throws ParserConfigurationException, IOException, SAXException
	{
//		createIxInitVars("unicornWHoldings.mrc");
//		assertFieldHasNoTermVectors("author");
//		assertFieldHasNoTermVectors("era");
//		assertFieldHasNoTermVectors("format");
//		// genre fields are no more for Stanford
//		// assertFieldHasNoTermVectors("genreStr");
//		assertFieldHasNoTermVectors("geographicStr");
//		assertFieldHasNoTermVectors("language");
//		assertFieldHasNoTermVectors("publishDate");
//		assertFieldHasNoTermVectors("title_short");
//		assertFieldHasNoTermVectors("topicStr");
	}	

	/**
	 * test the fields that we are no longer storing (to save space in index)
	 */
@Test
	public final void testFldsNoLongerStored() 
			throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("displayFieldsTests.mrc");
		assertFieldNotStored("series");
		assertFieldNotStored("series2");
		assertFieldNotStored("author2Str");
		assertFieldNotStored("subtopic");
		assertFieldNotStored("fullgeographic");
		assertFieldNotStored("geographic");
		assertFieldNotStored("subgeographic");
		// most call number fields, but we changed to our own anyway
		//  to split out LC and Dewey call numbers
	}	
}
