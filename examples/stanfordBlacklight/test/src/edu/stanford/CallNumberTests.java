package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University call number fields for blacklight index
 * @author Naomi Dushay
 */
public class CallNumberTests extends BibIndexTest {

	private final String govDocStr = "Government Document";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("callNumberTests.mrc");
	}	

	/**
	 * lc_1letter_facet contains the first letter of an LC call number along 
	 *  with a user friendly description of the broad topic indicated by the 
	 *  letter.
	 */
@Test
	public final void testLC1LetterFacet() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "lc_1letter_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		// LC classification values
		// single char LC classification
		assertSingleResult("6661112", fldName, "\"Z - Bibliography, Library Science, Information Resources\"", sis);
		// two char LC classification
		assertSingleResult("999LC22", fldName, "\"C - Historical Sciences (Archaeology, Genealogy)\"", sis);
		assertSingleResult("1033119", fldName, "\"B - Philosophy, Psychology, Religion\"", sis);
		// mixed one char and two char classification values
		String val = "\"D - World History\"";
		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC1dec");
		docIds.add("2913114");
		docIds.add("3400092");
		assertSearchResults(fldName, val, docIds, sis);
		// mixed 2 and 3 three char LC classification
		val = "\"K - Law\"";
		docIds.clear();
		docIds.add("999LC3NoDec");
		docIds.add("999LC3Dec");
		docIds.add("999LC3DecSpace");
		assertSearchResults(fldName, val, docIds, sis);
						
		// LCPER
		assertSingleResult("460947", fldName, "\"E - History of the Americas (General)\"", sis);
		
		// bad LC values should not be found
		// bad start chars for LC
		assertZeroResults(fldName, "I*", sis); // IN PROCESS and illegal LC
		assertZeroResults(fldName, "X*", sis); // illegal LC
		assertZeroResults(fldName, "W*", sis); // illegal LC
		// only N call number in test data is "NO CALL NUMBER"
		assertZeroResults(fldName, "N*", sis);

		// SUDOC
		assertResultSize(fldName, "\"" + govDocStr + "\"", 3, sis);
	}

	/**
	 * lc_alpha_facet contains the first alpha portion of the local LC call 
	 *  number along with a user friendly description of the topic indicated by  
	 *  the letters.
	 */
@Test
	public final void testLCAlphaFacet() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "lc_alpha_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		// single char LC classification
		assertSingleResult("6661112", fldName, "\"Z - Bibliography, Library Science, Information Resources\"", sis);
		// LC 999 one letter, space before Cutter
		assertSingleResult("7772223", fldName, "\"F - History of the Americas (Local)\"", sis);
		assertSingleResult("999LC1dec", fldName, "\"D - World History\"", sis);

		// two char LC classification
		assertZeroResults(fldName, "\"B - Philosophy, Psychology, Religion\"", sis);
		assertSingleResult("1033119", fldName, "\"BX - Christian Denominations\"", sis);
		// LC 999 two letters, space before Cutter
		assertSingleResult("999LC2", fldName, "\"HG - Finance\"", sis);
		assertZeroResults(fldName, "\"C - Auxiliary Sciences of History (General)\"", sis);
		assertSingleResult("999LC22", fldName, "\"CB - History of Civilization\"", sis);		
		assertSingleResult("2913114", fldName, "\"DH - Low Countries (History)\"", sis);
		assertSingleResult("1732616", fldName, "\"QA - Mathematics\"", sis); 
		assertSingleResult("115472", fldName, "\"HC - Economic History & Conditions\"", sis); 
		// mult values for a single doc
		assertSingleResult("3400092", fldName, "\"DC - France (History)\"", sis);

		// three char LC classification
		assertZeroResults(fldName, "\"K - Law\"", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC3NoDec");
		docIds.add("999LC3Dec");
		docIds.add("999LC3DecSpace");
		assertSearchResults(fldName, "\"KJH - Law of Andorra\"", docIds, sis);
		
		// bad values should not be found
		assertZeroResults(fldName, "NO*", sis);  // NO CALL NUMBER
		assertZeroResults(fldName, "I*", sis);  // illegal LC char, "IN PROCESS"
		assertZeroResults(fldName, "X*", sis);  // illegal LC char
		assertZeroResults(fldName, "W*", sis);  // illegal LC char, "WITHDRAWN"
		
		// LCPER
		assertSingleResult("460947", fldName, "\"E - History of the Americas (General)\"", sis);

		// SUDOC
// TODO: not sure if there should be a lc_xxx_facet value for SUDOCs
		assertResultSize(fldName, "\"" + govDocStr + "\"", 0, sis);
/*
		docIds.clear();
		docIds.add("2557826");
		docIds.add("2678655");
		docIds.add("5511738");
		assertSearchResults(fldName, govDocStr, docIds, sis);
*/
	}

	/**
	 * lc_b4cutter_facet contains the portion of local LC call numbers before 
	 * the Cutter.
	 */
@Test
	public final void testLCb4CutterFacet() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "lc_b4cutter_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
	
		// bad values should not be found
		assertZeroResults(fldName, "NO CALL NUMBER", sis);
		assertZeroResults(fldName, "IN PROCESS", sis);
		assertZeroResults(fldName, "I*", sis);
		assertZeroResults(fldName, "X*", sis);
		assertZeroResults(fldName, "WITHDRAWN", sis);
		assertZeroResults(fldName, "W*", sis);
		assertZeroResults(fldName, "110978984448763", sis);
			
		// search for LC values
		assertZeroResults(fldName, "Z", sis);
		assertSingleResult("6661112", fldName, "Z3871", sis);
		assertSingleResult("999LC1dec", fldName, "D764.7", sis);
		assertZeroResults(fldName, "C", sis);
		assertZeroResults(fldName, "CB", sis);
		assertSingleResult("999LC22", fldName, "CB3", sis);		
		assertZeroResults(fldName, "D810", sis);
		assertSingleResult("2913114", fldName, "DH135", sis);
		assertZeroResults(fldName, "K", sis);
		assertZeroResults(fldName, "KJ", sis);
		assertZeroResults(fldName, "KJH", sis);
		assertSingleResult("999LC3NoDec", fldName, "KJH2678", sis);
		assertSingleResult("999LC3DecSpace", fldName, "KJH66.6", sis);
		assertSingleResult("1033119", fldName, "BX4659", sis);
		// tricky cutter
		assertZeroResults(fldName, "HC241", sis);
		assertSingleResult("115472", fldName, "HC241.25", sis);
		assertSingleResult("3400092", fldName, "DC34.5", sis);
				
		// LCPER
		assertSingleResult("460947", fldName, "E184", sis);
	
		// SUDOC
		assertResultSize(fldName, "\"" + govDocStr + "\"", 0, sis);
	}


	/**
	 * lc-callnum contains local lc call numbers.  It is used for call number 
	 * searches
	 */
@Test
	public final void testLCCallNumsForSearching() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "lc_callnum";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldIndexed(fldName, solrCore); // will be used in shelflist browse
		
		assertFieldNotStored(fldName, solrCore);  
	
		// lc-b4cutter field is stored - retrieve values from specific documents
	
		// LC 999 one letter
		assertSingleResult("6661112", fldName, "Z3871.Z8", sis);
		// LC 999 one letter, space before Cutter
		assertSingleResult("7772223", fldName, "\"F1356 .M464 2005\"", sis);
		// LC 999 one letter, decimal digits and space before Cutter
		assertSingleResult("999LC1dec", fldName, "\"D764.7 .K72 1990\"", sis);
		// LC 999 two letters, space before Cutter
		assertSingleResult("999LC2", fldName, "\"HG6046 .V28 1986\"", sis);
		assertSingleResult("999LC22", fldName, "\"CB3 .A6 SUPPL. V.31\"", sis);
		// LC 999 two letters, no space before Cutter
		assertSingleResult("999LC2NoDec", fldName, "\"PQ2678.I26 P54 1992\"", sis);
		// LC 999 three letters, no space before Cutter
		assertSingleResult("999LC3NoDec", fldName, "\"KJH2678.I26 P54 1992\"", sis);
		// LC 999 three letters, decimal digit, no space before Cutter
		assertSingleResult("999LC3Dec", fldName, "\"KJH666.4.I26 P54 1992\"", sis);
		// LC 999 three letters, decimal digit, space before Cutter
		assertSingleResult("999LC3DecSpace", fldName, "\"KJH66.6 .I26 P54 1992\"", sis);
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertSingleResult("2913114", fldName, "\"DH135 .P6 I65\"", sis);
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertSingleResult("3400092", fldName, "\"DC34.5 .A78 L4 1996\"", sis);
	
		// LC 999, LC 050, tough cutter
		assertSingleResult("115472", fldName, "\"HC241.25 .I4 D47\"", sis);
		assertSingleResult("1033119", fldName, "\"BX4659.E85 W44\"", sis);
		assertSingleResult("1033119", fldName, "\"BX4659 .E85 W44 1982\"", sis);
		// 082 Dewey, LC 999, 050 (same value)
		assertSingleResult("1732616", fldName, "\"QA273 .C83 1962\"", sis); 
	
		//  bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertZeroResults(fldName, "\"NO CALL NUMBER\"", sis);
		assertZeroResults(fldName, "\"IN PROCESS\"", sis);
		assertZeroResults(fldName, "X*", sis);
	
		// LCPER 999
		assertSingleResult("460947", fldName, "\"E184.S75 R47A V.1 1980\"", sis); 
		
		// SUDOC 999 
		assertZeroResults(fldName, "\"" + govDocStr + "\"", sis); 
	
		// ALPHANUM 999 
		assertZeroResults(fldName, "SUSEL", sis); 
		assertZeroResults(fldName, "MFILM*", sis); 
		assertZeroResults(fldName, "MCD*", sis); 
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertZeroResults(fldName, "\"INTERNET RESOURCE\"", sis); 
	}

	/**
	 * dewey_1digit_facet contains the hundreds digit of a Dewey call number 
	 *  along with a user friendly description of the broad topic so indicated
	 */
@Test
	public final void testDewey1DigitFacet() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey_1digit_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertSingleResult("690002", fldName, "\"100s - Philosophy & Psychology\"", sis);
		assertSingleResult("2328381", fldName, "\"800s - Literature\"", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("2214009");
		docIds.add("1849258");
		assertSearchResults(fldName, "\"300s - Social Sciences\"", docIds, sis);
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "\"000s - Computer Science, Information & General Works\"", docIds, sis);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "\"900s - History & Geography\"", docIds, sis);
	}

	/**
	 * dewey_2digit_facet contains the hundred and tens digits of a local Dewey
	 *  call number (e.g 710s), along with a user friendly description of the 
	 *  topic indicated by the numbers.
	 */
@Test
	public final void testDewey2DigitFacet() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey_2digit_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertSingleResult("690002", fldName, "\"150s - Psychology\"", sis);
		assertSingleResult("2328381", fldName, "\"820s - English & Old English Literatures\"", sis);
		assertSingleResult("1849258", fldName, "\"350s - Public Administration\"", sis);	
		assertSingleResult("2214009", fldName, "\"370s - Education\"", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("11");
		assertSearchResults(fldName, "\"000s - Computer Science, Information & General Works\"", docIds, sis);
		docIds.clear();
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "\"020s - Library & Information Sciences\"", docIds, sis);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "\"990s - General History of Other Areas\"", docIds, sis);
	}


	/**
	 * dewey-b4cutter contains the portion of local Dewey call numbers 
	 * before the Cutter.  
	 */
@Test
	public final void testDeweyB4CutterFacet() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey_b4cutter_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertZeroResults(fldName, "159", sis);
		assertSingleResult("690002", fldName, "159.32", sis);
		assertZeroResults(fldName, "827", sis);
		assertSingleResult("2328381", fldName, "827.5", sis); 
		assertZeroResults(fldName, "352", sis);
		assertSingleResult("1849258", fldName, "352.042", sis); 
		assertZeroResults(fldName, "370", sis);
		assertSingleResult("2214009", fldName, "370.1", sis); 
		assertZeroResults(fldName, "WITHDRAWN", sis);
		
		assertSingleResult("1", fldName, "001", sis); 
		assertSingleResult("11", fldName, "001.123", sis); 
		assertSingleResult("2", fldName, "022", sis); 
		assertSingleResult("22", fldName, "022.456", sis); 
		assertSingleResult("3", fldName, "999", sis); 
		assertSingleResult("31", fldName, "999.85", sis); 
	}

	/**
	 * dewey-callnum contains local dewey call numbers.  It is used for call
	 *  number searches
	 */
@Test
	public final void testDeweyCallnumsForSearching() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey_callnum";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldIndexed(fldName, solrCore); 
		assertFieldNotStored(fldName, solrCore);  
		
		assertSingleResult("690002", fldName, "\"159.32 .W211\"", sis); 
		assertSingleResult("2328381", fldName, "\"827.5 .S97TG\"", sis); 
		assertSingleResult("1849258", fldName, "\"352.042 .C594 ED.2\"", sis); 
		assertSingleResult("2214009", fldName, "\"370.1 .S655\"", sis); 
		assertSingleResult("1", fldName, "\"001 .N44\"", sis); 
		assertSingleResult("11", fldName, "\"001.123 .N44\"", sis); 
		assertSingleResult("2", fldName, "\"022 .N47\"", sis); 
		assertSingleResult("22", fldName, "\"022.456 .S655\"", sis); 
		assertSingleResult("3", fldName, "\"999 .F67\"", sis); 
		assertSingleResult("31", fldName, "\"999.85 .P84\"", sis); 
	}


	/**
	 * test addition of leading zeros to Dewey call numbers with fewer than
	 *  three digits before the decimal (or implied decimal)
	 */
@Test
	public final void testDeweyLeadingZeros() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey_1digit_facet";
		Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "\"000s - Computer Science, Information & General Works\"", docIds, sis);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "\"900s - History & Geography\"", docIds, sis);

		fldName = "dewey_2digit_facet";
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		assertSearchResults(fldName, "\"000s - Computer Science, Information & General Works\"", docIds, sis);
		docIds.clear();
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "\"020s - Library & Information Sciences\"", docIds, sis);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "\"990s - General History of Other Areas\"", docIds, sis);

		fldName = "dewey_b4cutter_facet";
		assertSingleResult("1", fldName, "001", sis); 
		assertSingleResult("11", fldName, "001.123", sis); 
		assertSingleResult("2", fldName, "022", sis); 
		assertSingleResult("22", fldName, "022.456", sis); 
		assertSingleResult("3", fldName, "999", sis); 
		assertSingleResult("31", fldName, "999.85", sis); 
	}


	/**
	 * access facet should be "Online" for call number "INTERNET RESOURCE"
	 */
@Test
	public final void testAccessOnlineFrom999() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String afld = "access_facet";
		String fldVal = StanfordIndexer.Access.ONLINE.toString();
		Set<String> docIds = new HashSet<String>();
		docIds.add("6280316");
		docIds.add("7117119");
		docIds.add("7531910");
		assertSearchResults(afld, fldVal, docIds, sis);
	}


	/**
	 * test that SHELBYTITL, SHELBYSER and STORBYTITL locations cause call 
	 *  numbers to be ignored
	 */
@Test
	public final void testIgnoreShelbyLocations() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "lc_b4cutter_facet";
		assertZeroResults(fldName, "PQ9661", sis);
	}

	/**
	 * test the lc_1letter_facet value for LC call numbers beginning with P
	 */
@Test
	public final void testLCstartingP() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "lc_1letter_facet";
		String fldVal = "P - Language & Literature";

		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC2NoDec");
		docIds.add("2328381");
		
		assertSearchResults(fldName, "\"" + fldVal + "\"", docIds, sis);
	}



}
