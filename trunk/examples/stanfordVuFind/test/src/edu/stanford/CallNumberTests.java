package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

public class CallNumberTests extends AbstractStanfordVufindTest {

	private final String testDataFname = "callNumberTests.mrc";
	private final String govDocStr = "Government Document";

	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}

	
	/**
	 * test lc-first-letter, a facet containing the first character of a local
	 *  LC call number
	 */
@Test
	public final void testLCFirstLetter() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "lc-first-letter";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);

		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		// confirm records that should not have the field
	    	
		//   bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertDocHasNoField("7370014", fldName); 
		// LC 999 starts with X and 852s and 946s to ignore
		assertDocHasNoField("7233951", fldName); 
		// 999 withdrawn to ignore and LC 050 "IN PROCESS"
		assertDocHasNoField("3277173", fldName); 
		// 999 ASIS and LC 090 numeric (looks like barcode)
		assertDocHasNoField("7117119", fldName); 		
		// ALPHANUM 999 "SUSEL" 
		assertDocHasNoField("4578538", fldName); 
		// ALPHANUM 999 "MFILM ...": No call number (format checked in format tests)
		assertDocHasNoField("1261173", fldName); 
		// ALPHANUM 999 "MCD ...": No call number  (format checked in format tests)
		assertDocHasNoField("1234673", fldName); 		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertDocHasNoField("6280316", fldName); 
		// ASIS 999 "INTERNET RESOURCE" and 050 LC and 082 Dewey values: 
		assertDocHasNoField("7531910", fldName);
	
		// bad values should not be found
		// bad start chars for LC
		assertZeroResults(fldName, "I"); // IN PROCESS and illegal LC
		assertZeroResults(fldName, "X"); // illegal LC
		assertZeroResults(fldName, "W"); // illegal LC
		// only N call number in test data is "NO CALL NUMBER"
		assertZeroResults(fldName, "N");
	
		// search for LC classification values
		// single char LC classification
		assertSingleResult("6661112", fldName, "Z");
		assertSingleResult("1033119", fldName, "B");
		// two char LC classification
		assertSingleResult("999LC22", fldName, "C");	
		// mixed one char and two char classification values
		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC1dec");
		docIds.add("2913114");
		docIds.add("3400092");
		assertSearchResults(fldName, "D", docIds);
		// mixed 2 and 3 three char LC classification
		docIds.clear();
		docIds.add("999LC3NoDec");
		docIds.add("999LC3Dec");
		docIds.add("999LC3DecSpace");
		assertSearchResults(fldName, "K", docIds);

		// LCPER
		assertSingleResult("460947", fldName, "E");
	
		// SUDOC
		assertResultSize(fldName, "\"" + govDocStr + "\"", 3);
	}

	/**
	 * test lc-first-desc field, which is lc-first-letter translated to a human 
	 *  friendly description of the broad category defined by LC call number.
	 */
@Test
	public final void testLCFirstDesc() 
			throws IOException, ParserConfigurationException, SAXException 
	{
// TODO: this field should be eliminated and this functionality provided in the UI
		String fldName = "lc-first-desc";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);

		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		// LC classification values
		// single char LC classification
		assertSingleResult("6661112", fldName, "\"Z - Bibliography, Library Science, Information Resources\"");
		// two char LC classification
		assertSingleResult("999LC22", fldName, "\"C - Historical Sciences (Archaeology, Genealogy)\"");
		assertSingleResult("1033119", fldName, "\"B - Philosophy, Psychology, Religion\"");
		// mixed one char and two char classification values
		String val = "\"D - World History\"";
		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC1dec");
		docIds.add("2913114");
		docIds.add("3400092");
		assertSearchResults(fldName, val, docIds);
		// mixed 2 and 3 three char LC classification
		val = "\"K - Law\"";
		docIds.clear();
		docIds.add("999LC3NoDec");
		docIds.add("999LC3Dec");
		docIds.add("999LC3DecSpace");
		assertSearchResults(fldName, val, docIds);
						
		// LCPER
		assertSingleResult("460947", fldName, "\"E - History of the Americas (General)\"");

		// SUDOC
		assertResultSize(fldName, "\"" + govDocStr + "\"", 3);
	}

	/**
	 * lc-alpha is a facet containing the first alpha characters of a local LC 
	 *  call number (1-3 chars) 
	 */
@Test
	public final void testLCAlpha() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "lc-alpha";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		// bad values should not be found
		assertZeroResults(fldName, "NO");  // NO CALL NUMBER
		assertZeroResults(fldName, "IN");  // IN PROCESS
		assertZeroResults(fldName, "I*");  // illegal LC char
		assertZeroResults(fldName, "X*");  // illegal LC char
		assertZeroResults(fldName, "WITHDRAWN");
		assertZeroResults(fldName, "W*");  // illegal LC char
		//   bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertDocHasNoField("7370014", fldName); 
		// LC 999 starts with X and 852s and 946s to ignore
		assertDocHasNoField("7233951", fldName); 
		// 999 withdrawn to ignore and LC 050 "IN PROCESS"
		assertDocHasNoField("3277173", fldName); 
		// 999 ASIS and LC 090 numeric (looks like barcode)
		assertDocHasNoField("7117119", fldName); 		
		// ALPHANUM 999 "SUSEL" 
		assertDocHasNoField("4578538", fldName); 
		// ALPHANUM 999 "MFILM ...": No call number, but format Microfilm
		assertDocHasNoField("1261173", fldName); 
		// ALPHANUM 999 "MCD ...": No call number, but format Music - Audio
		assertDocHasNoField("1234673", fldName); 
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertDocHasNoField("6280316", fldName); 
		// ASIS 999 "INTERNET RESOURCE" and 050 LC and 082 Dewey values: 
		assertDocHasNoField("7531910", fldName);

		// single char LC classification
		assertSingleResult("6661112", fldName, "Z");
		// LC 999 one letter, space before Cutter
		assertSingleResult("7772223", fldName, "F");
		assertSingleResult("999LC1dec", fldName, "D");

		// two char LC classification
		assertZeroResults(fldName, "B");
		assertSingleResult("1033119", fldName, "BX");
		// LC 999 two letters, space before Cutter
		assertSingleResult("999LC2", fldName, "HG");
		assertZeroResults(fldName, "C");
		assertSingleResult("999LC22", fldName, "CB");		
		assertSingleResult("2913114", fldName, "DH");
		assertSingleResult("1732616", fldName, "QA"); 
		assertSingleResult("115472", fldName, "HC"); 
		// mult values for a single doc
		assertSingleResult("3400092", fldName, "DC");

		// three char LC classification
		assertZeroResults(fldName, "K");
		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC3NoDec");
		docIds.add("999LC3Dec");
		docIds.add("999LC3DecSpace");
		assertSearchResults(fldName, "KJH", docIds);
		
		// LCPER
		assertSingleResult("460947", fldName, "E");

		// SUDOC
// TODO: not sure if there should be a callnum-lc-alpha value for SUDOCs
		assertResultSize(fldName, "\"" + govDocStr + "\"", 0);
/*
		docIds.clear();
		docIds.add("2557826");
		docIds.add("2678655");
		docIds.add("5511738");
		assertSearchResults(fldName, govDocStr, docIds);
*/
	}

	/**
	 * test lc-alpha-desc, a human readable description of the topic indicated
	 *  by the first alpha portion of the local LC call number (see lc-alpha)
	 */
@Test
	public final void testLCAlphaDesc() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "lc-alpha-desc";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		// single char LC classification
		assertSingleResult("6661112", fldName, "\"Z - Bibliography, Library Science, Information Resources\"");
		// LC 999 one letter, space before Cutter
		assertSingleResult("7772223", fldName, "\"F - History of the Americas (Local)\"");
		assertSingleResult("999LC1dec", fldName, "\"D - World History\"");

		// two char LC classification
		assertZeroResults(fldName, "\"B - Philosophy, Psychology, Religion\"");
		assertSingleResult("1033119", fldName, "\"BX - Christian Denominations\"");
		// LC 999 two letters, space before Cutter
		assertSingleResult("999LC2", fldName, "\"HG - Finance\"");
		assertZeroResults(fldName, "\"C - Auxiliary Sciences of History (General)\"");
		assertSingleResult("999LC22", fldName, "\"CB - History of Civilization\"");		
		assertSingleResult("2913114", fldName, "\"DH - Low Countries (History)\"");
		assertSingleResult("1732616", fldName, "\"QA - Mathematics\""); 
		assertSingleResult("115472", fldName, "\"HC - Economic History & Conditions\""); 
		// mult values for a single doc
		assertSingleResult("3400092", fldName, "\"DC - France (History)\"");

		// three char LC classification
		assertZeroResults(fldName, "\"K - Law\"");
		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC3NoDec");
		docIds.add("999LC3Dec");
		docIds.add("999LC3DecSpace");
		assertSearchResults(fldName, "\"KJH - Law of Andorra\"", docIds);
		
		// LCPER
		assertSingleResult("460947", fldName, "\"E - History of the Americas (General)\"");

		// SUDOC
// TODO: not sure if there should be a callnum-lc-alpha value for SUDOCs
		assertResultSize(fldName, "\"" + govDocStr + "\"", 0);
/*
		docIds.clear();
		docIds.add("2557826");
		docIds.add("2678655");
		docIds.add("5511738");
		assertSearchResults(fldName, govDocStr, docIds);
*/
	}


	/**
	 * lc-b4cutter contains the portion of local lc call numbers before 
	 * the Cutter.  Stored because it's used in GetMoreLikeThis queries.
	 */
@Test
	public final void testLCb4CutterField() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		// first alpha chars  used in GetMoreLikeThis queries
		String fldName = "lc-b4cutter";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		
		assertFieldStored(fldName);  // used in getMoreLikeThis queries
		assertFieldIndexed(fldName);
	
		// lc-b4cutter field is stored - retrieve values from specific documents
	
		// LC 999 one letter
		assertDocHasFieldValue("6661112", fldName, "Z3871");
		// LC 999 one letter, space before Cutter
		assertDocHasFieldValue("7772223", fldName, "F1356");
		// LC 999 one letter, decimal digits and space before Cutter
		assertDocHasFieldValue("999LC1dec", fldName, "D764.7");
		// LC 999 two letters, space before Cutter
		assertDocHasFieldValue("999LC2", fldName, "HG6046");
		assertDocHasFieldValue("999LC22", fldName, "CB3");
		// LC 999 two letters, no space before Cutter
		assertDocHasFieldValue("999LC2NoDec", fldName, "PQ2678");
		// LC 999 three letters, no space before Cutter
		assertDocHasFieldValue("999LC3NoDec", fldName, "KJH2678");
		// LC 999 three letters, decimal digit, no space before Cutter
		assertDocHasFieldValue("999LC3Dec", fldName, "KJH666.4");
		// LC 999 three letters, decimal digit, space before Cutter
		assertDocHasFieldValue("999LC3DecSpace", fldName, "KJH66.6");
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertDocHasNoFieldValue("2913114", fldName, "D810");
		assertDocHasFieldValue("2913114", fldName, "DH135");
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertDocHasFieldValue("3400092", fldName, "DC34.5");
		assertDocHasNoFieldValue("3400092", fldName, "BL1844"); // 050
	
		// LC 999, LC 050, tough cutter
		assertDocHasFieldValue("115472", fldName, "HC241.25");
		assertDocHasFieldValue("1033119", fldName, "BX4659");
	
		//  bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertDocHasNoField("7370014", fldName); 
		// LC 999 starts with X and 852s and 946s to ignore
		assertDocHasNoField("7233951", fldName); 
		// LC 050 "IN PROCESS" and 999 withdrawn to ignore
		assertDocHasNoField("3277173", fldName); 
		// LC 090 numeric (looks like barcode) and 999 ASIS
		assertDocHasNoField("7117119", fldName); 
	
		// LCPER 999
		assertDocHasFieldValue("460947", fldName, "E184"); 
		
		// 082 Dewey, LC 999, 050 (same value)
		assertDocHasFieldValue("1732616", fldName, "QA273"); 
		// 082 Dewey, LC 999, 050 (same value)  	
		assertDocHasFieldValue("115472", fldName, "HC241.25"); 
	
	
		// SUDOC 999 and 086 (same values, as it happens)
		assertDocHasNoFieldValue("2557826", fldName, govDocStr);
		// SUDOC 999 and 086, two 088 to ignore
		assertDocHasNoFieldValue("2678655", fldName, govDocStr);
		// SUDOC 999 and 086 (same values, as it happens), LC 050
		assertDocHasNoFieldValue("5511738", fldName, govDocStr);
		assertDocHasNoFieldValue("5511738", fldName, "KJ27");
	
		// ALPHANUM 999 "SUSEL" 
		assertDocHasNoField("4578538", fldName); 
		// ALPHANUM 999 "MFILM ...": No call number, but format Microfilm
		assertDocHasNoField("1261173", fldName); 
		// ALPHANUM 999 "MCD ...": No call number, but format Music - Audio
		assertDocHasNoField("1234673", fldName); 
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertDocHasNoField("6280316", fldName); 
		// ASIS 999 "INTERNET RESOURCE" and 090 with bad LC value: No call number but access Online
		assertDocHasNoField("7117119", fldName); 
		// ASIS 999 "INTERNET RESOURCE" and 050 LC and 082 Dewey values: 
		assertDocHasNoField("7531910", fldName);
	
		// lc-b4cutter field is INDEXED - SEARCH for values
	
		// bad values should not be found
		assertZeroResults(fldName, "NO CALL NUMBER");
		assertZeroResults(fldName, "IN PROCESS");
		assertZeroResults(fldName, "I*");
		assertZeroResults(fldName, "X*");
		assertZeroResults(fldName, "WITHDRAWN");
		assertZeroResults(fldName, "W*");
		assertZeroResults(fldName, "110978984448763");
			
		// search for LC values
		assertZeroResults(fldName, "Z");
		assertSingleResult("6661112", fldName, "Z3871");
		assertSingleResult("999LC1dec", fldName, "D764.7");
		assertZeroResults(fldName, "C");
		assertZeroResults(fldName, "CB");
		assertSingleResult("999LC22", fldName, "CB3");		
		assertZeroResults(fldName, "D810");
		assertSingleResult("2913114", fldName, "DH135");
		assertZeroResults(fldName, "K");
		assertZeroResults(fldName, "KJ");
		assertZeroResults(fldName, "KJH");
		assertSingleResult("999LC3NoDec", fldName, "KJH2678");
		assertSingleResult("999LC3DecSpace", fldName, "KJH66.6");
		assertSingleResult("1033119", fldName, "BX4659");
		// tricky cutter
		assertZeroResults(fldName, "HC241");
		assertSingleResult("115472", fldName, "HC241.25");
		assertSingleResult("3400092", fldName, "DC34.5");
				
		// LCPER
		assertSingleResult("460947", fldName, "E184");
	
		// SUDOC
		assertResultSize(fldName, "\"" + govDocStr + "\"", 0);
	}


	/**
	 * lc-callnum contains local lc call numbers.  It is used for display
	 */
@Test
	public final void testLCCallNumsStored() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		// first alpha chars  used in GetMoreLikeThis queries
		String fldName = "lc-callnum";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		
		assertFieldStored(fldName);  
		assertFieldIndexed(fldName); // will be used in shelflist browse
	
		// lc-b4cutter field is stored - retrieve values from specific documents
	
		// LC 999 one letter
		assertDocHasFieldValue("6661112", fldName, "Z3871.Z8");
		// LC 999 one letter, space before Cutter
		assertDocHasFieldValue("7772223", fldName, "F1356 .M464 2005");
		// LC 999 one letter, decimal digits and space before Cutter
		assertDocHasFieldValue("999LC1dec", fldName, "D764.7 .K72 1990");
		// LC 999 two letters, space before Cutter
		assertDocHasFieldValue("999LC2", fldName, "HG6046 .V28 1986");
		assertDocHasFieldValue("999LC22", fldName, "CB3 .A6 SUPPL. V.31");
		// LC 999 two letters, no space before Cutter
		assertDocHasFieldValue("999LC2NoDec", fldName, "PQ2678.I26 P54 1992");
		// LC 999 three letters, no space before Cutter
		assertDocHasFieldValue("999LC3NoDec", fldName, "KJH2678.I26 P54 1992");
		// LC 999 three letters, decimal digit, no space before Cutter
		assertDocHasFieldValue("999LC3Dec", fldName, "KJH666.4.I26 P54 1992");
		// LC 999 three letters, decimal digit, space before Cutter
		assertDocHasFieldValue("999LC3DecSpace", fldName, "KJH66.6 .I26 P54 1992");
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertDocHasFieldValue("2913114", fldName, "DH135 .P6 I65");
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertDocHasFieldValue("3400092", fldName, "DC34.5 .A78 L4 1996");
	
		// LC 999, LC 050, tough cutter
		assertDocHasFieldValue("115472", fldName, "HC241.25 .I4 D47");
		assertDocHasFieldValue("1033119", fldName, "BX4659.E85 W44");
		assertDocHasFieldValue("1033119", fldName, "BX4659 .E85 W44 1982");
		// 082 Dewey, LC 999, 050 (same value)
		assertDocHasFieldValue("1732616", fldName, "QA273 .C83 1962"); 
	
		//  bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertDocHasNoField("7370014", fldName); 
		// LC 999 starts with X and 852s and 946s to ignore
		assertDocHasNoField("7233951", fldName); 
		// LC 050 "IN PROCESS" and 999 withdrawn to ignore
		assertDocHasNoField("3277173", fldName); 
		// LC 090 numeric (looks like barcode) and 999 ASIS
		assertDocHasNoField("7117119", fldName); 
	
		// LCPER 999
		assertDocHasFieldValue("460947", fldName, "E184.S75 R47A V.1 1980"); 
		
	
		// SUDOC 999 and 086 (same values, as it happens)
		assertDocHasNoFieldValue("2557826", fldName, govDocStr);
		// SUDOC 999 and 086, two 088 to ignore
		assertDocHasNoFieldValue("2678655", fldName, govDocStr);
		// SUDOC 999 and 086 (same values, as it happens), LC 050
		assertDocHasNoFieldValue("5511738", fldName, govDocStr);
		assertDocHasNoFieldValue("5511738", fldName, "KJ27");
	
		// ALPHANUM 999 "SUSEL" 
		assertDocHasNoField("4578538", fldName); 
		// ALPHANUM 999 "MFILM ...": No call number, but format Microfilm
		assertDocHasNoField("1261173", fldName); 
		// ALPHANUM 999 "MCD ...": No call number, but format Music - Audio
		assertDocHasNoField("1234673", fldName); 
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertDocHasNoField("6280316", fldName); 
		assertDocHasNoField("7117119", fldName); 
		assertDocHasNoField("7531910", fldName);
	}


	/**
	 * dewey-first-digit is a facet of the hundreds digit in local Dewey call 
	 * numbers
	 */
@Test
	public final void testDeweyFirstDigit() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-first-digit";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
	
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		// bad values should not be found
		// should be 000s
		assertZeroResults(fldName, "1");
		
		assertSingleResult("690002", fldName, "100s");
		assertSingleResult("2328381", fldName, "800s");
		Set<String> docIds = new HashSet<String>();
		docIds.add("2214009");
		docIds.add("1849258");
		assertSearchResults(fldName, "300s", docIds);
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "000s", docIds);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "900s", docIds);
	}


	/**
	 * dewey-first-desc is the human friendly description of the broad category
	 *  indicated by the hundreds digit of a local Dewey call number (in
	 *   dewey-first-digit)
	 */
@Test
	public final void testDeweyFirstDesc() 
			throws IOException, ParserConfigurationException, SAXException 
	{
// TODO: this field should be eliminated and this functionality provided in the UI
		String fldName = "dewey-first-desc";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
	
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		assertSingleResult("690002", fldName, "\"100s - Philosophy & Psychology\"");
		assertSingleResult("2328381", fldName, "\"800s - Literature\"");
		Set<String> docIds = new HashSet<String>();
		docIds.add("2214009");
		docIds.add("1849258");
		assertSearchResults(fldName, "\"300s - Social Sciences\"", docIds);
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "\"000s - Computer Science, Information & General Works\"", docIds);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "\"900s - History & Geography\"", docIds);
	}


	/**
	 * dewey-2digits is a facet containing the hundred and tens digits of a
	 *  local Dewey call numbers (e.g 710s).
	 */
@Test
	public final void testDewey2Digits() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-2digits";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		assertSingleResult("690002", fldName, "150s");
		assertSingleResult("2328381", fldName, "820s");
		assertSingleResult("1849258", fldName, "350s");	
		assertSingleResult("2214009", fldName, "370s");
		Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("11");
		assertSearchResults(fldName, "000s", docIds);
		docIds.clear();
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "020s", docIds);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "990s", docIds);
	}


	/**
	 * dewey-2digits-desc contains a few word description of the code in 
	 *  dewey-2digits
	 */
@Test
	public final void testDewey2DigitDesc() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-2digit-desc";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		assertSingleResult("690002", fldName, "\"150s - Psychology\"");
		assertSingleResult("2328381", fldName, "\"820s - English & Old English Literatures\"");
		assertSingleResult("1849258", fldName, "\"350s - Public Administration\"");	
		assertSingleResult("2214009", fldName, "\"370s - Education\"");
		Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("11");
		assertSearchResults(fldName, "\"000s - Computer Science, Information & General Works\"", docIds);
		docIds.clear();
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "\"020s - Library & Information Sciences\"", docIds);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "\"990s - General History of Other Areas\"", docIds);
	}

	/**
	 * dewey-b4cutter contains the portion of local dewey call numbers 
	 * before the Cutter.  Stored because it's used in GetMoreLikeThis queries.
	 */
@Test
	public final void testDeweyB4CutterField() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-b4cutter";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		
		assertFieldStored(fldName); 
		assertFieldIndexed(fldName);
		
		assertDocHasFieldValue("690002", fldName, "159.32"); 
		assertDocHasFieldValue("2328381", fldName, "827.5"); 
		assertDocHasFieldValue("1849258", fldName, "352.042"); 
		assertDocHasFieldValue("2214009", fldName, "370.1"); 
		assertDocHasNoFieldValue("2214009", fldName, "WITHDRAWN");
		assertDocHasFieldValue("1", fldName, "001"); 
		assertDocHasFieldValue("11", fldName, "001.123"); 
		assertDocHasFieldValue("2", fldName, "022"); 
		assertDocHasFieldValue("22", fldName, "022.456"); 
		assertDocHasFieldValue("3", fldName, "999"); 
		assertDocHasFieldValue("31", fldName, "999.85"); 
		
		// search for values
		assertZeroResults(fldName, "159");
		assertSingleResult("690002", fldName, "159.32");
		assertZeroResults(fldName, "827");
		assertSingleResult("2328381", fldName, "827.5"); 
		assertZeroResults(fldName, "352");
		assertSingleResult("1849258", fldName, "352.042"); 
		assertZeroResults(fldName, "370");
		assertSingleResult("2214009", fldName, "370.1"); 
		assertZeroResults(fldName, "WITHDRAWN");
		
		assertSingleResult("1", fldName, "001"); 
		assertSingleResult("11", fldName, "001.123"); 
		assertSingleResult("2", fldName, "022"); 
		assertSingleResult("22", fldName, "022.456"); 
		assertSingleResult("3", fldName, "999"); 
		assertSingleResult("31", fldName, "999.85"); 
	}

	/**
	 * dewey-callnum contains local dewey call numbers.  It is used for display.
	 */
@Test
	public final void testDeweyCallnumsStored() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-callnum";
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		
		assertFieldStored(fldName);  
		assertFieldIndexed(fldName);  // will be used in shelflist browse
		
		assertDocHasFieldValue("690002", fldName, "159.32 .W211"); 
		assertDocHasFieldValue("2328381", fldName, "827.5 .S97TG"); 
		assertDocHasFieldValue("1849258", fldName, "352.042 .C594 ED.2"); 
		assertDocHasFieldValue("2214009", fldName, "370.1 .S655"); 
		assertDocHasFieldValue("1", fldName, "001 .N44"); 
		assertDocHasFieldValue("11", fldName, "001.123 .N44"); 
		assertDocHasFieldValue("2", fldName, "022 .N47"); 
		assertDocHasFieldValue("22", fldName, "022.456 .S655"); 
		assertDocHasFieldValue("3", fldName, "999 .F67"); 
		assertDocHasFieldValue("31", fldName, "999.85 .P84"); 
	}


	/**
	 * test addition of leading zeros to Dewey call numbers with fewer than
	 *  three digits before the decimal (or implied decimal)
	 */
@Test
	public final void testDeweyLeadingZeros() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-first-digit";
		// not stored: search for values
		Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "000s", docIds);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "900s", docIds);

		fldName = "dewey-first-desc";
		// not stored: search for values
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "\"000s - Computer Science, Information & General Works\"", docIds);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "\"900s - History & Geography\"", docIds);

		fldName = "dewey-2digits";
		// not stored: search for values
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		assertSearchResults(fldName, "000s", docIds);
		docIds.clear();
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "020s", docIds);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "990s", docIds);

		fldName = "dewey-2digit-desc";
		// not stored: search for values
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		assertSearchResults(fldName, "\"000s - Computer Science, Information & General Works\"", docIds);
		docIds.clear();
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "\"020s - Library & Information Sciences\"", docIds);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "\"990s - General History of Other Areas\"", docIds);

		fldName = "dewey-b4cutter";
		// stored: retrieve values from specific docs
		assertDocHasFieldValue("1", fldName, "001"); 
		assertDocHasFieldValue("11", fldName, "001.123"); 
		assertDocHasFieldValue("2", fldName, "022"); 
		assertDocHasFieldValue("22", fldName, "022.456"); 
		assertDocHasFieldValue("3", fldName, "999"); 
		assertDocHasFieldValue("31", fldName, "999.85"); 
	}


	/**
	 * test access facet online for call number "INTERNET RESOURCE"
	 */
@Test
	public final void testAccessOnlineFrom999() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String afld = "accessMethod_facet";
		String fldVal = StanfordIndexer.Access.ONLINE.toString();
		Set<String> docIds = new HashSet<String>();
		docIds.add("6280316");
		docIds.add("7117119");
		docIds.add("7531910");
		assertSearchResults(afld, fldVal, docIds);
	}


	/**
	 * test that SHELBYTITL, SHELBYSER and STORBYTITL locations cause call 
	 *  numbers to be ignored
	 */
@Test
	public final void testIgnoreShelbyLocations() 
			throws ParserConfigurationException, IOException, SAXException
	{
		Set<String> docIds = new HashSet<String>();
		docIds.add("1111");
		docIds.add("2211");
		docIds.add("3311");
		
		for (String docId : docIds) {
			assertDocHasNoField(docId, "lc-first-letter");
			assertDocHasNoField(docId, "lc-first-desc");
			assertDocHasNoField(docId, "lc-alpha");
			assertDocHasNoField(docId, "lc-alpha-desc");
			assertDocHasNoField(docId, "lc-b4cutter");
			assertDocHasNoField(docId, "dewey-first-digit");
			assertDocHasNoField(docId, "dewey-first-desc");
			assertDocHasNoField(docId, "dewey-2digits");
			assertDocHasNoField(docId, "dewey-2digit-desc");
			assertDocHasNoField(docId, "dewey-b4cutter");
		}
	}

	/**
	 * test the lc-first-desc value for lc call numbers beginning with P
	 */
@Test
	public final void testLCFirstDescP() 
			throws ParserConfigurationException, IOException, SAXException {
	
		String fldName = "lc-first-desc";
		String fldVal = "P - Language & Literature";

		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC2NoDec");
		docIds.add("2328381");
		
		assertSearchResults(fldName, "\"" + fldVal + "\"", docIds);
	}

}
