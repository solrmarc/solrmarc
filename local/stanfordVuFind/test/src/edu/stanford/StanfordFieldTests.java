package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.index.IndexReader;
import org.xml.sax.SAXException;

import org.junit.*;

import static org.junit.Assert.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 *
 */
public class StanfordFieldTests extends BibIndexTest {
	
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);		
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
        int numDocs = sis.getReader().numDocs();
        assertEquals("Number of documents in index incorrect: ", 3, numDocs);
        assertDocNotPresent("001noSubNo004", sis);
        assertDocPresent("001suba", sis);
        assertDocNotPresent("001and004nosub", sis);
        assertDocNotPresent("004noSuba", sis);
        assertDocPresent("001subaAnd004nosub", sis);
        assertDocNotPresent("004noSuba", sis);
        assertDocPresent("001subaAnd004suba", sis);
        assertDocNotPresent("004suba", sis);
        
        assertSingleResult("001suba", fldName, "\"001suba\"", sis);
        assertSingleResult("001subaAnd004nosub", fldName, "\"001subaAnd004nosub\"", sis);
        assertSingleResult("001subaAnd004suba", fldName, "\"001subaAnd004suba\"", sis);
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
		assertStringFieldProperties(fldName, solrCore);
		// may become multivalued eventually - vanilla VuFind has it that way
		assertFieldNotMultiValued(fldName, solrCore);		
		assertFieldStored(fldName, solrCore);
		// indexed for GetMoreLikeThis search
		assertFieldIndexed(fldName, solrCore);

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
		
		// field is indexed - search for values
        assertSingleResult("bothDates008", fldName, "\"1964\"", sis);
        assertSingleResult("pubDate195u", fldName, "\"1950s\"", sis);
        assertSingleResult("pubDate01uu", fldName, "\"2nd century\"", sis);
        assertSingleResult("pubDate19uu", fldName, "\"20th century\"", sis);
        assertZeroResults(fldName, "\"after 1000\"", sis);
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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		// indexed for GetMoreLikeThis search
		assertFieldIndexed(fldName, solrCore);

		assertDocHasFieldValue("008spa", fldName, "Spanish", sis); 
		assertDocHasFieldValue("008mul041atha", fldName, "Thai", sis); 
		assertDocHasNoFieldValue("008mul041atha", fldName, "mul", sis); 
		assertDocHasNoFieldValue("008mul041atha", fldName, "Multiple languages", sis); 
		assertDocHasNoFieldValue("008mul041atha", fldName, "null", sis); 
		assertDocHasFieldValue("008eng3041a", fldName, "English", sis); 
		assertDocHasFieldValue("008eng3041a", fldName, "German", sis); 
		assertDocHasFieldValue("008eng3041a", fldName, "Russian", sis); 
		assertDocHasFieldValue("008eng2041a041h", fldName, "English", sis); 
		assertDocHasFieldValue("008eng2041a041h", fldName, "Greek, Ancient (to 1453)", sis); 
		assertDocHasNoFieldValue("008eng2041a041h", fldName, "Russian", sis); 
		assertDocHasFieldValue("008fre041d", fldName, "French", sis); 
		assertDocHasFieldValue("008fre041d", fldName, "Spanish", sis); 
		assertDocHasFieldValue("008nor041ad", fldName, "Norwegian", sis); 
		assertDocHasFieldValue("008nor041ad", fldName, "Swedish", sis); 
		assertDocHasNoFieldValue("008mis041ak", fldName, "Italian", sis); 
		assertDocHasNoFieldValue("008mis041ak", fldName, "mis", sis); 
		assertDocHasNoFieldValue("008mis041ak", fldName, "Miscellaneous languages", sis); 
		assertDocHasNoFieldValue("008mis041ak", fldName, "null", sis); 
		// cases where no field should exist
		assertDocHasNoField("008blank", fldName, sis); 
		assertDocHasNoField("008zxx", fldName, sis); 
		assertDocHasNoField("008und", fldName, sis); 
		assertDocHasNoField("008mis", fldName, sis); 
		assertDocHasNoField("008mul", fldName, sis); 

		// field is indexed - search for values
		Set<String> docIds = new HashSet<String>();
		docIds.add("008eng3041a");
		docIds.add("008eng2041a041h");
		assertSearchResults(fldName, "English", docIds, sis);
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
	
		assertDocHasNoField("008blank", fldName, sis); 
		assertDocHasNoField("008zxx", fldName, sis); 
		assertDocHasNoField("008und", fldName, sis); 
		assertDocHasNoField("008mis", fldName, sis); 
		assertDocHasNoField("008mul", fldName, sis); 

		assertZeroResults(fldName, "null", sis);
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);

		// 300abc
        assertDocHasFieldValue("300111", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.", sis); 
        // 300abce
        assertDocHasFieldValue("300222", fldName, "271 p. : ill. ; 21 cm. + answer book.", sis); 
        // 300 3afg
        assertDocHasFieldValue("300333", fldName, "1 box 2 x 4 x 3 1/2 ft.", sis); 
        // 300aafafc - in order ...
        assertDocHasFieldValue("300444", fldName, "diary 1 volume (463 pages) ; 17 cm. x 34.5 cm.", sis); 
        
		// string field requires exact match
		assertSingleResult("300111", fldName, "\"1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.\"", sis);
		assertSingleResult("300222", fldName, "\"271 p. : ill. ; 21 cm. + answer book.\"", sis);
		assertSingleResult("300333", fldName, "\"1 box 2 x 4 x 3 1/2 ft.\"", sis);
		assertSingleResult("300444", fldName, "\"diary 1 volume (463 pages) ; 17 cm. x 34.5 cm.\"", sis);
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
		assertFieldNotMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldNotIndexed(fldName, solrCore);

        assertDocHasFieldValue("editionMV", fldName, "Special education version", sis); 
        assertDocHasFieldValue("editionMV", fldName, "Medium-high voice ed.", sis); 
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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
        assertSingleResult("115472", fldName, "\"Applied Physics Department\"", sis);
        assertSingleResult("229800", fldName, "\"Archive of Recorded Sound\"", sis);
        assertSingleResult("345228", fldName, "\"Art & Architecture\"", sis);
        assertSingleResult("460947", fldName, "\"Falconer (Biology)\"", sis);
        assertSingleResult("804724", fldName, "\"Off-campus (Newark)\"", sis);
        assertSingleResult("919006", fldName, "\"Swain (Chemistry & Chem. Engineering)\"", sis);
        assertSingleResult("1147269", fldName, "Classics", sis);
        assertSingleResult("1391080", fldName, "\"Green - Current Periodicals & Microtext\"", sis);
        assertSingleResult("1505065", fldName, "\"Branner (Earth Sciences & Maps)\"", sis);
        assertSingleResult("1618836", fldName, "\"Cubberley (Education)\"", sis);
        assertSingleResult("1732616", fldName, "\"Mathematical & Computer Sciences\"", sis);
        assertSingleResult("1849258", fldName, "Engineering", sis);
        assertZeroResults(fldName, "\"Government Documents\"", sis);
        assertSingleResult("2678655", fldName, "\"Jackson (Business)\"", sis);
        assertSingleResult("2797607", fldName, "Meyer", sis);
        assertSingleResult("3027805", fldName, "\"Miller (Hopkins Marine Station)\"", sis);
        assertSingleResult("3142611", fldName, "Physics", sis);
        assertSingleResult("3400092", fldName, "\"Hoover Institution Archives\"", sis);
        assertSingleResult("3743949", fldName, "\"Hoover Institution\"", sis);
        assertSingleResult("4258089", fldName, "\"Special Collections & University Archives\"", sis);
        assertSingleResult("4428936", fldName, "\"Tanner (Philosophy)\"", sis);
        assertSingleResult("4823592", fldName, "\"Crown (Law)\"", sis);
        assertSingleResult("5666387", fldName, "Music", sis);
        assertSingleResult("6676531", fldName, "\"East Asia\"", sis);

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
        assertSearchResults(fldName, "\"Green (Humanities & Social Sciences)\"", docIds, sis);

        docIds.clear();
        docIds.add("1033119");
        docIds.add("1962398");
        docIds.add("2328381");
        docIds.add("2913114");
        assertSearchResults(fldName, "\"Stanford Auxiliary Library (SAL 1&2)\"", docIds, sis);

        docIds.clear();
        docIds.add("690002");
        docIds.add("2328381");
        docIds.add("3941911");
        docIds.add("7651581");
        // education - withdrawn;  SAL3 stacks
        docIds.add("2214009");
        assertSearchResults(fldName, "\"Off-campus (SAL3)\"", docIds, sis);

        docIds.clear();
        docIds.add("7370014");
        // ask@lane
        docIds.add("7233951");
        assertSearchResults(fldName, "\"Lane (Medical)\"", docIds, sis);

        docIds.clear();
        docIds.add("6493823");
		// INTERNET
        docIds.add("7117119");
        assertSearchResults(fldName, "\"Stanford University Libraries\"", docIds, sis);
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldNotIndexed(fldName, solrCore);
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
		assertTextFieldProperties(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
		String docId = "allfields1";

		// 245 just for good measure
        assertSingleResult(docId, fldName, "should", sis); 
        
        // 0xx fields are not included except 024, 027, 028
        assertSingleResult(docId, fldName, "2777802000", sis); // 024
        assertSingleResult(docId, fldName, "90620", sis); // 024
        assertSingleResult(docId, fldName, "technical", sis); // 027
        assertSingleResult(docId, fldName, "vibrations", sis); // 027
        assertZeroResults(fldName, "ocolcm", sis);  // 035
        assertZeroResults(fldName, "orlob", sis);  // 040

        // 3xx fields ARE included
        assertSingleResult(docId, fldName, "sound", sis); // 300
        assertSingleResult(docId, fldName, "annual", sis);  // 310
        
        // 6xx subject fields - we're including them, even though
        // fulltopic is all subfields of all 600, 610, 630, 650, 655
        // fullgeographic is all subfields of all 651
        //   b/c otherwise standard numbers and other things are doubled here,
        //   but topics are not.
        
        // 9xx fields are NOT included
        assertZeroResults(fldName, "EDATA", sis);  // 946
        assertZeroResults(fldName, "pamphlet", sis);  // 947
        assertZeroResults(fldName, "stacks", sis);  // 999
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
		assertFieldMultiValued("author", solrCore);
		assertFieldMultiValued("author-letter", solrCore); // removed
		assertFieldMultiValued("authorStr", solrCore);
		assertFieldMultiValued("title", solrCore);
		assertFieldMultiValued("titleStr", solrCore);
		assertFieldMultiValued("edition", solrCore);
*/
		assertFieldMultiValued("physical", solrCore);
		// we separate LC and dewey
/*
		assertFieldMultiValued("callnumber", solrCore);
		assertFieldMultiValued("callnumber-first", solrCore);
		assertFieldMultiValued("callnumber-subject", solrCore);
		assertFieldMultiValued("callnumber-label", solrCore);
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
		IndexReader ir = getIndexReader(solrPath, solrDataDir);
		// none of the following fields are being used by VuFind code, but they
		//  are either populated or at least declared in the VuFind/solr/conf/schema.xml
		assertFieldNotPresent("institution", ir);
		assertFieldNotPresent("langcode", ir);
		assertFieldNotPresent("auth_title", ir);
		assertFieldNotPresent("auth_titleStr", ir);
		assertFieldNotPresent("auth_author", ir);
		assertFieldNotPresent("auth_authorStr", ir);
		assertFieldNotPresent("auth_author2", ir);
		assertFieldNotPresent("auth_author2Str", ir);
		assertFieldNotPresent("author2-role", ir);
		assertFieldNotPresent("dateSpan", ir);
		assertFieldNotPresent("description", ir);
		assertFieldNotPresent("callnumber-a", ir);
		assertFieldNotPresent("genre", ir);
		assertFieldNotPresent("fullgenre", ir);
		assertFieldNotPresent("genreStr", ir);
		assertFieldNotPresent("subgenre", ir);
		assertFieldNotPresent("author-letter", ir);
		assertFieldNotPresent("publisherStr", ir);
	}

	/**
	 * test that explicitly named string fields in VuFind index do not have
	 *  termVectors.
	 */
@Test
	public final void testStrFldsNoTV() 
			throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("unicornWHoldings.mrc");
		assertFieldHasNoTermVectors("author", solrCore);
		assertFieldHasNoTermVectors("era", solrCore);
		assertFieldHasNoTermVectors("format", solrCore);
		// genre fields are no more for Stanford
		// assertFieldHasNoTermVectors("genreStr", solrCore);
		assertFieldHasNoTermVectors("geographicStr", solrCore);
		assertFieldHasNoTermVectors("language", solrCore);
		assertFieldHasNoTermVectors("publishDate", solrCore);
		assertFieldHasNoTermVectors("title_short", solrCore);
		assertFieldHasNoTermVectors("topicStr", solrCore);
	}	

	/**
	 * test the fields that we are no longer storing (to save space in index)
	 */
@Test
	public final void testFldsNoLongerStored() 
			throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("displayFieldsTests.mrc");
		assertFieldNotStored("series", solrCore);
		assertFieldNotStored("series2", solrCore);
		assertFieldNotStored("author2Str", solrCore);
		assertFieldNotStored("subtopic", solrCore);
		assertFieldNotStored("fullgeographic", solrCore);
		assertFieldNotStored("geographic", solrCore);
		assertFieldNotStored("subgeographic", solrCore);
		// most call number fields, but we changed to our own anyway
		//  to split out LC and Dewey call numbers
	}	
}
