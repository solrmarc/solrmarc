package edu.stanford;

import static org.junit.Assert.*;
import static edu.stanford.Utils.getVolumeSortCallnum;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.solrmarc.tools.CallNumUtils;
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
	 * callnum_1_facet, for LC, contains the first letter of an LC call number
	 *  along with a user friendly description of the broad topic indicated by
	 *  the letter. Dewey and GovDoc values are tested in separate methods.
	 */
@Test
	public final void testLevel1FacetLC() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_1_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		// LC values
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
	}

	/**
	 * callnum_2_facet, for LC, contains the first alpha portion of the local LC
	 *  call number along with a user friendly description of the topic  
	 *  indicated by the letters.   Dewey and GovDoc values are tested in 
	 *  separate methods.
	 */
@Test
	public final void testLevel2FacetLC() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_2_facet";
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
	}

	/**
	 * callnum_3_facet, for LC, contains the portion of local LC call numbers
	 *  before the Cutter.
	 */
@Test
	public final void testLevel3FacetLC() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_3_facet";
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
	}


	/**
	 * callnum_search contains all local call numbers, except those that are 
	 *  ignored, such as "NO CALL NUMBER"  It includes "bad" LC call numbers, 
	 *  such as those beginning with X;  it includes MFILM and MCD call numbers
	 *  and so on.  Testing Dewey call number search is in a separate method.
	 */
@Test
	public final void testSearchLC() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_search";
		assertFieldMultiValued(fldName, solrCore);
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore); 
		assertFieldNotStored(fldName, solrCore); 
	
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

		//   this is a bad LC value, but not a bad call number, so it is included
		assertSingleResult("7233951", fldName, "\"X578 .S64 1851\"", sis);
	
		// LCPER 999
		assertSingleResult("460947", fldName, "\"E184.S75 R47A V.1 1980\"", sis); 
		
		// SUDOC 999 
		assertSingleResult("5511738", fldName, "\"Y 4.AG 8/1:108-16\"", sis);
		assertSingleResult("2678655", fldName, "\"GA 1.13:RCED-85-88\"", sis);
		assertZeroResults(fldName, "\"" + govDocStr + "\"", sis); 
	
		// ALPHANUM 999 
		assertSingleResult("4578538", fldName, "\"SUSEL-69048\"", sis); 
		assertSingleResult("1261173", fldName, "\"MFILM N.S. 1350 REEL 230 NO. 3741\"", sis); 
		assertSingleResult("1261173", fldName, "MFILM", sis); 
		assertSingleResult("1234673", fldName, "MCD", sis); 
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertZeroResults(fldName, "\"INTERNET RESOURCE\"", sis); 
	}

	/**
	 * callnum_1_facet, for dewey, should be DEWEY_TOP_FACET_VAL
	 */
@Test
	public final void testLevel1FacetDewey() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_1_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("690002");
		docIds.add("2328381");
		docIds.add("2214009");
		docIds.add("1849258");
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		docIds.add("3");
		docIds.add("31");
		docIds.add("DeweyVol");
		assertSearchResults(fldName, "\"" + StanfordIndexer.DEWEY_TOP_FACET_VAL + "\"", docIds, sis);
		assertSearchResults(fldName, "\"Dewey Classification\"", docIds, sis);
	}
	

	/**
	 * callnum_2_facet, for dewey, contains the hundreds digit of a Dewey call
	 *  number along with a user friendly description of the broad topic so 
	 *  indicated
	 */
@Test
	public final void testLevel2FacetDewey() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_2_facet";
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
	 * callnum_3_facet, for dewey, contains the hundred and tens digits of a 
	 *  Dewey call number (e.g 710s), along with a user friendly description of 
	 *  the topic indicated by the numbers.
	 */
@Test
	public final void testLevel3FacetDewey() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_3_facet";
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
	 * callnum_4_facet, for dewey, contains the portion of the Dewey call 
	 * numbers before the Cutter.  
	 */
@Test
	public final void testLevel4FacetDewey() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_4_facet";
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
	 * callnum_search contains local call numbers.  LC and other searching
	 *  are tested in another method.
	 */
@Test
	public final void testSearchDewey() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_search";
		assertFieldMultiValued(fldName, solrCore);
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore); 
		assertFieldNotStored(fldName, solrCore);  
		
		assertSingleResult("690002", fldName, "\"159.32 .W211\"", sis); 
		assertSingleResult("2328381", fldName, "\"827.5 .S97TG\"", sis); 
		assertSingleResult("1849258", fldName, "\"352.042 .C594 ED.2\"", sis); 
		assertSingleResult("2214009", fldName, "\"370.1 .S655\"", sis); 
		assertSingleResult("1", fldName, "\"1 .N44\"", sis); 
		assertSingleResult("11", fldName, "\"1.123 .N44\"", sis); 
		assertSingleResult("2", fldName, "\"22 .N47\"", sis); 
		assertSingleResult("22", fldName, "\"22.456 .S655\"", sis); 
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
		String fldName = "callnum_2_facet";
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

		fldName = "callnum_3_facet";
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

		fldName = "callnum_4_facet";
		assertSingleResult("1", fldName, "001", sis); 
		assertSingleResult("11", fldName, "001.123", sis); 
		assertSingleResult("2", fldName, "022", sis); 
		assertSingleResult("22", fldName, "022.456", sis); 
		assertSingleResult("3", fldName, "999", sis); 
		assertSingleResult("31", fldName, "999.85", sis); 
	}


	/**
	 * Call number top level facet should be GOV_DOC_TOP_FACET_VAL if the "type" of 
	 *  call number indicated in the 999 is "SUDOC"
	 */
@Test
	public final void testGovtDocFromSUDOC() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_1_facet";
		Set<String> docIds = new HashSet<String>();
		docIds.add("2557826");
		docIds.add("5511738");
		docIds.add("2678655");
		assertSearchResults(fldName, "\"" + StanfordIndexer.GOV_DOC_TOP_FACET_VAL + "\"", docIds, sis);
		assertSearchResults(fldName, "\"" + govDocStr + "\"", docIds, sis);
	}


	/**
	 * Call number top level facet should be "Gov't Doc" if the location is 
	 *  a gov doc location, regardless of the type of call number
	 */
@Test
	public final void testGovDocFromLocation() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		tearDown();
		createIxInitVars("callNumberGovDocTests.mrc");
		String fldName = "callnum_1_facet";
		Set<String> docIds = new HashSet<String>();
		docIds.add("brit");  // lc
		docIds.add("calif");
		docIds.add("fed");
		docIds.add("intl");
		docIds.add("ssrcdocs");
		docIds.add("ssrcfiche");  // dewey
		docIds.add("ssrcnwdoc");
		docIds.add("sudoc");   // not a gov doc location, but sudoc call number
		assertSearchResults(fldName, "\"" + StanfordIndexer.GOV_DOC_TOP_FACET_VAL + "\"", docIds, sis);
		
		assertZeroResults(fldName, "\"300s - Social Sciences\"", sis);

		// This is an LC call number, but the item has a gov doc location
//		assertZeroResults(fldName, "\"Z - Bibliography, Library Science, Information Resources\"", sis);
	}


	/**
	 * Call number top level facet should be both the LC call number stuff AND
	 *  "Gov't Doc" if the "type" of call number is LC and the location is 
	 *  a gov doc location.
	 * If the call number is labeled LC, but does not parse, and the location is
	 *  a gov doc location, then the top level facet hsould be gov doc only.
	 */
@Test
	public final void testLevel2FacetGovDoc() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		tearDown();
		createIxInitVars("callNumberGovDocTests.mrc");
		String fldName = "callnum_2_facet";

		assertSingleResult("brit", fldName, "\"" + StanfordIndexer.GOV_DOC_BRIT_FACET_VAL + "\"", sis);
		assertSingleResult("calif", fldName, "\"" + StanfordIndexer.GOV_DOC_CALIF_FACET_VAL + "\"", sis);
		assertSingleResult("intl", fldName, "\"" + StanfordIndexer.GOV_DOC_INTL_FACET_VAL + "\"", sis);
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("fed");
		docIds.add("ssrcdocs");
		docIds.add("ssrcfiche");
		docIds.add("ssrcnwdoc");
		assertSearchResults(fldName, "\"" + StanfordIndexer.GOV_DOC_FED_FACET_VAL + "\"", docIds, sis);

		assertSingleResult("sudoc", fldName, "\"" + StanfordIndexer.GOV_DOC_UNKNOWN_FACET_VAL + "\"", sis);
		
		assertZeroResults(fldName, "\"" + govDocStr + "\"", sis);
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
		String fldName = "callnum_3_facet";
		assertZeroResults(fldName, "PQ9661", sis);
	}

	/**
	 * test the lc_1letter_facet value for LC call numbers beginning with P
	 */
@Test
	public final void testLCstartingP() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "callnum_1_facet";
		String fldVal = "P - Language & Literature";

		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC2NoDec");
		docIds.add("2328381");
		
		assertSearchResults(fldName, "\"" + fldVal + "\"", docIds, sis);
	}


	/**
	 * callnum_sort should contain shelfkey versions of "lopped" call
	 *  numbers (call numbers without volume info)
	 */
@Test
	public final void testCallnumSort() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "callnum_sort";

		// LC: no volume info
		String callnum = "HG6046 .V28 1986";
		String shelfkey = CallNumUtils.getLCShelfkey(callnum, "999LC2");
		assertSingleResult("999LC2", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis);

		// LC: volume info to lop off
		callnum = "CB3 .A6 SUPPL. V.31";
		shelfkey = CallNumUtils.getLCShelfkey("CB3 .A6 SUPPL.", "999LC22");
		assertSingleResult("999LC22", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis);
		
		// LCPER
		callnum = "E184.S75 R47A V.1 1980";
		shelfkey = CallNumUtils.getLCShelfkey("E184.S75 R47A", "460947");
		assertSingleResult("460947", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis);

		// this is a bad LC value, but not a bad call number, so it is included
		// (it's actually sudoc)
		callnum = "X578 .S64 1851";
		shelfkey = CallNumUtils.getLCShelfkey(callnum, "7233951");
		assertSingleResult("7233951", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis);
		
		//  bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertZeroResults(fldName, "\"NO CALL NUMBER\"", sis);
		assertZeroResults(fldName, "\"IN PROCESS\"", sis);

		// Dewey: no vol info
		callnum = "999.85 .P84";
		shelfkey = CallNumUtils.getDeweyShelfKey(callnum);
		assertSingleResult("31", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis); 
		
		// Dewey: vol info to lop off
		callnum = "666 .F67 VOL. 5";
		shelfkey = CallNumUtils.getDeweyShelfKey("666 .F67");
		assertSingleResult("DeweyVol", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis); 
		
// TODO: implement SUDOC volume lopping and shelving key processing		

		// SUDOC 999  -  uses raw callno
		callnum = "Y 4.AG 8/1:108-16";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		assertSingleResult("5511738", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis); 
		
		callnum = "GA 1.13:RCED-85-88";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		assertSingleResult("2678655", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis); 

		assertZeroResults(fldName, "\"" + govDocStr + "\"", sis); 
	
// TODO: implement ALPHANUM volume lopping and shelving key processing		

		// ALPHANUM 999 - uses raw callno
		callnum = "SUSEL-69048";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		assertSingleResult("4578538", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis); 
		
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		assertSingleResult("1261173", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis); 

		callnum = "MCD Brendel Plays Beethoven's Eroica variations";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		assertSingleResult("1234673", fldName, "\"" + shelfkey.toLowerCase() + "\"", sis); 
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertZeroResults(fldName, "\"INTERNET RESOURCE\"", sis); 
	}


	/**
	 * callnum_reverse_sort should contain reverse shelfkey versions of 
	 *  "lopped" call numbers (call numbers without volume info)
	 */
@Test
	public final void testCallnumReverseSort() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "callnum_reverse_sort";
		// LC: no volume info
		String callnum = "HG6046 .V28 1986";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		String shelfkey = CallNumUtils.getLCShelfkey(lopped, "999LC2");
		String reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("999LC2", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis);

		// LC: volume info to lop off
		callnum = "CB3 .A6 SUPPL. V.31";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = CallNumUtils.getLCShelfkey(lopped, "999LC22");
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("999LC22", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis);
		
		// LCPER
		callnum = "E184.S75 R47A V.1 1980";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = CallNumUtils.getLCShelfkey(lopped, "460947");
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("460947", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis);

		// this is a bad LC value, but not a bad call number, so it is included
		// (it's actually sudoc)
		callnum = "X578 .S64 1851";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = CallNumUtils.getLCShelfkey(lopped, "7233951");
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("7233951", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis);
		
		//  bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertZeroResults(fldName, "\"NO CALL NUMBER\"", sis);
		assertZeroResults(fldName, "\"IN PROCESS\"", sis);

		// Dewey: no vol info
		callnum = "999.85 .P84";
		lopped = CallNumUtils.removeDeweyVolSuffix(callnum);
		shelfkey = CallNumUtils.getDeweyShelfKey(lopped);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("31", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis); 
		
		// Dewey: vol info to lop off
		callnum = "352.042 .C594 ED.2";
		lopped = CallNumUtils.removeDeweyVolSuffix(callnum);
		shelfkey = CallNumUtils.getDeweyShelfKey(lopped);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("1849258", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis); 
		
// TODO: implement SUDO volume lopping and shelving key processing	
		
		// SUDOC 999 
		callnum = "Y 4.AG 8/1:108-16";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("5511738", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis); 
		
		callnum = "GA 1.13:RCED-85-88";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("2678655", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis); 

		shelfkey = CallNumUtils.normalizeSuffix(govDocStr);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertZeroResults(fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis); 
	
// TODO: implement ALPHANUM volume lopping and shelving key processing		

		// ALPHANUM 999 - uses raw callno
		callnum = "SUSEL-69048";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("4578538", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis); 
		
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("1261173", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis); 

		callnum = "MCD Brendel Plays Beethoven's Eroica variations";
		shelfkey = CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("1234673", fldName, "\"" + reverseShelfkey.toLowerCase() + "\"", sis); 
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertZeroResults(fldName, "\"INTERNET RESOURCE\"", sis); 
	}

	/**
	 * unit test for edu.stanford.Utils.getVolumeSortCallnum() 
	 */
@Test
	public final void testVolumeSortCallnum() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		boolean isSerial = true;
		String reversePeriodStr = new String(CallNumUtils.reverseNonAlphanum('.'));
		String reverseSpaceStr = new String(CallNumUtils.reverseNonAlphanum(' '));
		String reverseHyphenStr = new String(CallNumUtils.reverseNonAlphanum('-'));
		
		// LC
		String callnum = "M453 .Z29 Q1 L V.2"; 
		String lopped = "M453 .Z29 Q1 L"; 
		assertEquals("M   0453.000000 Z0.290000 Q0.100000 L V.000002", getVolumeSortCallnum(callnum, lopped, !isSerial));
		String reversePrefix = "M   0453.000000 Z0.290000 Q0.100000 L 4" + reversePeriodStr + "ZZZZZX";
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, isSerial).startsWith(reversePrefix));
		
		callnum = "M453 .Z29 Q1 L SER.2"; 
		assertEquals("M   0453.000000 Z0.290000 Q0.100000 L SER.000002", getVolumeSortCallnum(callnum, "M453 .Z29 Q1 L", !isSerial));
		reversePrefix = "M   0453.000000 Z0.290000 Q0.100000 L 7L8" + reversePeriodStr + "ZZZZZX";
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, isSerial).startsWith(reversePrefix));
		
		// dewey 
		// suffix year
		callnum = "322.45 .R513 1957";     
		assertEquals("322.45000000 R513 001957",  getVolumeSortCallnum(callnum, callnum, !isSerial));
		assertEquals("322.45000000 R513 001957",  getVolumeSortCallnum(callnum, callnum, isSerial));
       // suffix volume		
		callnum = "323.09 .K43 V.1";
		lopped = "323.09 .K43";
		assertEquals("323.09000000 K43 V.000001", getVolumeSortCallnum(callnum, lopped, !isSerial));
		reversePrefix = "323.09000000 K43 4" + reversePeriodStr + "ZZZZZY";
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, isSerial).startsWith(reversePrefix));
		// suffix - volume and year
		callnum = "322.44 .F816 V.1 1974";  
		lopped = "322.44 .F816"; 
		assertEquals("322.44000000 F816 V.000001 001974", getVolumeSortCallnum(callnum, lopped, !isSerial));
		reversePrefix = "322.44000000 F816 4" + reversePeriodStr + "ZZZZZY" + reverseSpaceStr + "ZZYQSV";
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, isSerial).startsWith(reversePrefix));
		// suffix no.
		callnum = "323 .A512RE NO.23-28";   
		lopped = "323 .A512RE";  
		assertEquals("323.00000000 A512RE NO.000023-000028", getVolumeSortCallnum(callnum, lopped, !isSerial));
		reversePrefix = "323.00000000 A512RE CB" + reversePeriodStr + "ZZZZXW" + reverseHyphenStr + "ZZZZXR";
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, isSerial).startsWith(reversePrefix));
	}


// NOTE:  Dewey is like LC, except part before cutter is numeric.  Given
// how the code works, there is no need to test Dewey in addition to LC.

// TODO:  call numbers that are neither LC nor Dewey ...

	// list of raw call numbers NOT in order to check sorting
	List<String> lcVolumeUnsortedCallnumList = new ArrayList<String>(25);
	{
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.4");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.3 1947");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.1");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.3");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.2");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.2 1959");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.1 Suppl");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.2 1947");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.2 1953");
	}
	
	// list of raw call numbers in "proper" order for show view of non-serial
	List<String> sortedLCVolCallnumList = new ArrayList<String>(25);
	{
		sortedLCVolCallnumList.add("B8.14 L3 V.1");
		sortedLCVolCallnumList.add("B8.14 L3 V.1 Suppl");
		sortedLCVolCallnumList.add("B8.14 L3 V.2");
		sortedLCVolCallnumList.add("B8.14 L3 V.2 1947");
		sortedLCVolCallnumList.add("B8.14 L3 V.2 1953");
		sortedLCVolCallnumList.add("B8.14 L3 V.2 1959");
		sortedLCVolCallnumList.add("B8.14 L3 V.3");
		sortedLCVolCallnumList.add("B8.14 L3 V.3 1947");
		sortedLCVolCallnumList.add("B8.14 L3 V.4");
	}

	
	// list of raw call numbers in "proper" order for show view of serial
	List<String> serialSortedLCVolCallnumList = new ArrayList<String>(25);
	{
		serialSortedLCVolCallnumList.add("B8.14 L3 V.4");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.3 1947");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.3");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.2 1959");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.2 1953");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.2 1947");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.2");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.1 Suppl");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.1");
	}
	boolean isSerial = true;
	String lopped = "B8.14 L3";

	/**
	 * test the sort of call numbers (for non-serials) with volume portion
	 */
@Test
	public void testLCVolumeSorting() 
	{
		// compute list: non-serial volume sorting
		Map<String,String> volSortString2callnum = new HashMap<String,String>(75);
		for (String callnum : lcVolumeUnsortedCallnumList) {
			volSortString2callnum.put(getVolumeSortCallnum(callnum, lopped, !isSerial), callnum);
		}
		List<String> ordered = new ArrayList<String>(volSortString2callnum.keySet());		
		Collections.sort(ordered);

		for (int i = 0; i < ordered.size(); i++) {
			assertEquals("At position " + i + " in list: ", sortedLCVolCallnumList.get(i), volSortString2callnum.get(ordered.get(i)));
		}
	}

	/**
	 * test the sort of call numbers (for serials) with volume portion
	 */
@Test
	public void testLCSerialVolumeSorting() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		// compute list: non-serial volume sorting
		Map<String,String> volSortString2callnum = new HashMap<String,String>(75);
		for (String callnum : lcVolumeUnsortedCallnumList) {
			volSortString2callnum.put(getVolumeSortCallnum(callnum, lopped, isSerial), callnum);
		}
		List<String> ordered = new ArrayList<String>(volSortString2callnum.keySet());		
		Collections.sort(ordered);

		for (int i = 0; i < ordered.size(); i++) {
			assertEquals("At position " + i + " in list: ", serialSortedLCVolCallnumList.get(i), volSortString2callnum.get(ordered.get(i)));
		}
	}
	
}
