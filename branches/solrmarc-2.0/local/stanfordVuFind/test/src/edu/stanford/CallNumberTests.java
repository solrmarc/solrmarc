package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

public class CallNumberTests extends BibIndexTest {

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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);

		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
		// confirm records that should not have the field
	    	
		//   bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertDocHasNoField("7370014", fldName, sis); 
		// LC 999 starts with X and 852s and 946s to ignore
		assertDocHasNoField("7233951", fldName, sis); 
		// 999 withdrawn to ignore and LC 050 "IN PROCESS"
		assertDocHasNoField("3277173", fldName, sis); 
		// 999 ASIS and LC 090 numeric (looks like barcode)
		assertDocHasNoField("7117119", fldName, sis); 		
		// ALPHANUM 999 "SUSEL" 
		assertDocHasNoField("4578538", fldName, sis); 
		// ALPHANUM 999 "MFILM ...": No call number (format checked in format tests)
		assertDocHasNoField("1261173", fldName, sis); 
		// ALPHANUM 999 "MCD ...": No call number  (format checked in format tests)
		assertDocHasNoField("1234673", fldName, sis); 		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertDocHasNoField("6280316", fldName, sis); 
		// ASIS 999 "INTERNET RESOURCE" and 050 LC and 082 Dewey values: 
		assertDocHasNoField("7531910", fldName, sis);
	
		// bad values should not be found
		// bad start chars for LC
		assertZeroResults(fldName, "I", sis); // IN PROCESS and illegal LC
		assertZeroResults(fldName, "X", sis); // illegal LC
		assertZeroResults(fldName, "W", sis); // illegal LC
		// only N call number in test data is "NO CALL NUMBER"
		assertZeroResults(fldName, "N", sis);
	
		// search for LC classification values
		// single char LC classification
		assertSingleResult("6661112", fldName, "Z", sis);
		assertSingleResult("1033119", fldName, "B", sis);
		// two char LC classification
		assertSingleResult("999LC22", fldName, "C", sis);	
		// mixed one char and two char classification values
		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC1dec");
		docIds.add("2913114");
		docIds.add("3400092");
		assertSearchResults(fldName, "D", docIds, sis);
		// mixed 2 and 3 three char LC classification
		docIds.clear();
		docIds.add("999LC3NoDec");
		docIds.add("999LC3Dec");
		docIds.add("999LC3DecSpace");
		assertSearchResults(fldName, "K", docIds, sis);

		// LCPER
		assertSingleResult("460947", fldName, "E", sis);
	
		// SUDOC
		assertResultSize(fldName, "\"" + govDocStr + "\"", 3, sis);
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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);

		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
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

		// SUDOC
		assertResultSize(fldName, "\"" + govDocStr + "\"", 3, sis);
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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
		// bad values should not be found
		assertZeroResults(fldName, "NO", sis);  // NO CALL NUMBER
		assertZeroResults(fldName, "IN", sis);  // IN PROCESS
		assertZeroResults(fldName, "I*", sis);  // illegal LC char
		assertZeroResults(fldName, "X*", sis);  // illegal LC char
		assertZeroResults(fldName, "WITHDRAWN", sis);
		assertZeroResults(fldName, "W*", sis);  // illegal LC char
		//   bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertDocHasNoField("7370014", fldName, sis); 
		// LC 999 starts with X and 852s and 946s to ignore
		assertDocHasNoField("7233951", fldName, sis); 
		// 999 withdrawn to ignore and LC 050 "IN PROCESS"
		assertDocHasNoField("3277173", fldName, sis); 
		// 999 ASIS and LC 090 numeric (looks like barcode)
		assertDocHasNoField("7117119", fldName, sis); 		
		// ALPHANUM 999 "SUSEL" 
		assertDocHasNoField("4578538", fldName, sis); 
		// ALPHANUM 999 "MFILM ...": No call number, but format Microfilm
		assertDocHasNoField("1261173", fldName, sis); 
		// ALPHANUM 999 "MCD ...": No call number, but format Music - Audio
		assertDocHasNoField("1234673", fldName, sis); 
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertDocHasNoField("6280316", fldName, sis); 
		// ASIS 999 "INTERNET RESOURCE" and 050 LC and 082 Dewey values: 
		assertDocHasNoField("7531910", fldName, sis);

		// single char LC classification
		assertSingleResult("6661112", fldName, "Z", sis);
		// LC 999 one letter, space before Cutter
		assertSingleResult("7772223", fldName, "F", sis);
		assertSingleResult("999LC1dec", fldName, "D", sis);

		// two char LC classification
		assertZeroResults(fldName, "B", sis);
		assertSingleResult("1033119", fldName, "BX", sis);
		// LC 999 two letters, space before Cutter
		assertSingleResult("999LC2", fldName, "HG", sis);
		assertZeroResults(fldName, "C", sis);
		assertSingleResult("999LC22", fldName, "CB", sis);		
		assertSingleResult("2913114", fldName, "DH", sis);
		assertSingleResult("1732616", fldName, "QA", sis); 
		assertSingleResult("115472", fldName, "HC", sis); 
		// mult values for a single doc
		assertSingleResult("3400092", fldName, "DC", sis);

		// three char LC classification
		assertZeroResults(fldName, "K", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("999LC3NoDec");
		docIds.add("999LC3Dec");
		docIds.add("999LC3DecSpace");
		assertSearchResults(fldName, "KJH", docIds, sis);
		
		// LCPER
		assertSingleResult("460947", fldName, "E", sis);

		// SUDOC
// TODO: not sure if there should be a callnum-lc-alpha value for SUDOCs
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
	 * test lc-alpha-desc, a human readable description of the topic indicated
	 *  by the first alpha portion of the local LC call number (see lc-alpha)
	 */
@Test
	public final void testLCAlphaDesc() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "lc-alpha-desc";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
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
		
		// LCPER
		assertSingleResult("460947", fldName, "\"E - History of the Americas (General)\"", sis);

		// SUDOC
// TODO: not sure if there should be a callnum-lc-alpha value for SUDOCs
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
	 * lc-b4cutter contains the portion of local lc call numbers before 
	 * the Cutter.  Stored because it's used in GetMoreLikeThis queries.
	 */
@Test
	public final void testLCb4CutterField() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		// first alpha chars  used in GetMoreLikeThis queries
		String fldName = "lc-b4cutter";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		
		assertFieldStored(fldName, solrCore);  // used in getMoreLikeThis queries
		assertFieldIndexed(fldName, solrCore);
	
		// lc-b4cutter field is stored - retrieve values from specific documents
	
		// LC 999 one letter
		assertDocHasFieldValue("6661112", fldName, "Z3871", sis);
		// LC 999 one letter, space before Cutter
		assertDocHasFieldValue("7772223", fldName, "F1356", sis);
		// LC 999 one letter, decimal digits and space before Cutter
		assertDocHasFieldValue("999LC1dec", fldName, "D764.7", sis);
		// LC 999 two letters, space before Cutter
		assertDocHasFieldValue("999LC2", fldName, "HG6046", sis);
		assertDocHasFieldValue("999LC22", fldName, "CB3", sis);
		// LC 999 two letters, no space before Cutter
		assertDocHasFieldValue("999LC2NoDec", fldName, "PQ2678", sis);
		// LC 999 three letters, no space before Cutter
		assertDocHasFieldValue("999LC3NoDec", fldName, "KJH2678", sis);
		// LC 999 three letters, decimal digit, no space before Cutter
		assertDocHasFieldValue("999LC3Dec", fldName, "KJH666.4", sis);
		// LC 999 three letters, decimal digit, space before Cutter
		assertDocHasFieldValue("999LC3DecSpace", fldName, "KJH66.6", sis);
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertDocHasNoFieldValue("2913114", fldName, "D810", sis);
		assertDocHasFieldValue("2913114", fldName, "DH135", sis);
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertDocHasFieldValue("3400092", fldName, "DC34.5", sis);
		assertDocHasNoFieldValue("3400092", fldName, "BL1844", sis); // 050
	
		// LC 999, LC 050, tough cutter
		assertDocHasFieldValue("115472", fldName, "HC241.25", sis);
		assertDocHasFieldValue("1033119", fldName, "BX4659", sis);
	
		//  bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertDocHasNoField("7370014", fldName, sis); 
		// LC 999 starts with X and 852s and 946s to ignore
		assertDocHasNoField("7233951", fldName, sis); 
		// LC 050 "IN PROCESS" and 999 withdrawn to ignore
		assertDocHasNoField("3277173", fldName, sis); 
		// LC 090 numeric (looks like barcode) and 999 ASIS
		assertDocHasNoField("7117119", fldName, sis); 
	
		// LCPER 999
		assertDocHasFieldValue("460947", fldName, "E184", sis); 
		
		// 082 Dewey, LC 999, 050 (same value)
		assertDocHasFieldValue("1732616", fldName, "QA273", sis); 
		// 082 Dewey, LC 999, 050 (same value)  	
		assertDocHasFieldValue("115472", fldName, "HC241.25", sis); 
	
	
		// SUDOC 999 and 086 (same values, as it happens)
		assertDocHasNoFieldValue("2557826", fldName, govDocStr, sis);
		// SUDOC 999 and 086, two 088 to ignore
		assertDocHasNoFieldValue("2678655", fldName, govDocStr, sis);
		// SUDOC 999 and 086 (same values, as it happens), LC 050
		assertDocHasNoFieldValue("5511738", fldName, govDocStr, sis);
		assertDocHasNoFieldValue("5511738", fldName, "KJ27", sis);
	
		// ALPHANUM 999 "SUSEL" 
		assertDocHasNoField("4578538", fldName, sis); 
		// ALPHANUM 999 "MFILM ...": No call number, but format Microfilm
		assertDocHasNoField("1261173", fldName, sis); 
		// ALPHANUM 999 "MCD ...": No call number, but format Music - Audio
		assertDocHasNoField("1234673", fldName, sis); 
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertDocHasNoField("6280316", fldName, sis); 
		// ASIS 999 "INTERNET RESOURCE" and 090 with bad LC value: No call number but access Online
		assertDocHasNoField("7117119", fldName, sis); 
		// ASIS 999 "INTERNET RESOURCE" and 050 LC and 082 Dewey values: 
		assertDocHasNoField("7531910", fldName, sis);
	
		// lc-b4cutter field is INDEXED - SEARCH for values
	
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
	 * lc-callnum contains local lc call numbers.  It is used for display
	 */
@Test
	public final void testLCCallNumsStored() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		// first alpha chars  used in GetMoreLikeThis queries
		String fldName = "lc-callnum";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		
		assertFieldStored(fldName, solrCore);  
		assertFieldIndexed(fldName, solrCore); // will be used in shelflist browse
	
		// lc-b4cutter field is stored - retrieve values from specific documents
	
		// LC 999 one letter
		assertDocHasFieldValue("6661112", fldName, "Z3871.Z8", sis);
		// LC 999 one letter, space before Cutter
		assertDocHasFieldValue("7772223", fldName, "F1356 .M464 2005", sis);
		// LC 999 one letter, decimal digits and space before Cutter
		assertDocHasFieldValue("999LC1dec", fldName, "D764.7 .K72 1990", sis);
		// LC 999 two letters, space before Cutter
		assertDocHasFieldValue("999LC2", fldName, "HG6046 .V28 1986", sis);
		assertDocHasFieldValue("999LC22", fldName, "CB3 .A6 SUPPL. V.31", sis);
		// LC 999 two letters, no space before Cutter
		assertDocHasFieldValue("999LC2NoDec", fldName, "PQ2678.I26 P54 1992", sis);
		// LC 999 three letters, no space before Cutter
		assertDocHasFieldValue("999LC3NoDec", fldName, "KJH2678.I26 P54 1992", sis);
		// LC 999 three letters, decimal digit, no space before Cutter
		assertDocHasFieldValue("999LC3Dec", fldName, "KJH666.4.I26 P54 1992", sis);
		// LC 999 three letters, decimal digit, space before Cutter
		assertDocHasFieldValue("999LC3DecSpace", fldName, "KJH66.6 .I26 P54 1992", sis);
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertDocHasFieldValue("2913114", fldName, "DH135 .P6 I65", sis);
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		assertDocHasFieldValue("3400092", fldName, "DC34.5 .A78 L4 1996", sis);
	
		// LC 999, LC 050, tough cutter
		assertDocHasFieldValue("115472", fldName, "HC241.25 .I4 D47", sis);
		assertDocHasFieldValue("1033119", fldName, "BX4659.E85 W44", sis);
		assertDocHasFieldValue("1033119", fldName, "BX4659 .E85 W44 1982", sis);
		// 082 Dewey, LC 999, 050 (same value)
		assertDocHasFieldValue("1732616", fldName, "QA273 .C83 1962", sis); 
	
		//  bad LC values
		// LC 999 "NO CALL NUMBER" and 852 to ignore
		assertDocHasNoField("7370014", fldName, sis); 
		// LC 999 starts with X and 852s and 946s to ignore
		assertDocHasNoField("7233951", fldName, sis); 
		// LC 050 "IN PROCESS" and 999 withdrawn to ignore
		assertDocHasNoField("3277173", fldName, sis); 
		// LC 090 numeric (looks like barcode) and 999 ASIS
		assertDocHasNoField("7117119", fldName, sis); 
	
		// LCPER 999
		assertDocHasFieldValue("460947", fldName, "E184.S75 R47A V.1 1980", sis); 
		
	
		// SUDOC 999 and 086 (same values, as it happens)
		assertDocHasNoFieldValue("2557826", fldName, govDocStr, sis);
		// SUDOC 999 and 086, two 088 to ignore
		assertDocHasNoFieldValue("2678655", fldName, govDocStr, sis);
		// SUDOC 999 and 086 (same values, as it happens), LC 050
		assertDocHasNoFieldValue("5511738", fldName, govDocStr, sis);
		assertDocHasNoFieldValue("5511738", fldName, "KJ27", sis);
	
		// ALPHANUM 999 "SUSEL" 
		assertDocHasNoField("4578538", fldName, sis); 
		// ALPHANUM 999 "MFILM ...": No call number, but format Microfilm
		assertDocHasNoField("1261173", fldName, sis); 
		// ALPHANUM 999 "MCD ...": No call number, but format Music - Audio
		assertDocHasNoField("1234673", fldName, sis); 
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		assertDocHasNoField("6280316", fldName, sis); 
		assertDocHasNoField("7117119", fldName, sis); 
		assertDocHasNoField("7531910", fldName, sis);
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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
	
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
		// bad values should not be found
		// should be 000s
		assertZeroResults(fldName, "1", sis);
		
		assertSingleResult("690002", fldName, "100s", sis);
		assertSingleResult("2328381", fldName, "800s", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("2214009");
		docIds.add("1849258");
		assertSearchResults(fldName, "300s", docIds, sis);
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "000s", docIds, sis);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "900s", docIds, sis);
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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
	
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
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
	 * dewey-2digits is a facet containing the hundred and tens digits of a
	 *  local Dewey call numbers (e.g 710s).
	 */
@Test
	public final void testDewey2Digits() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-2digits";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
		assertSingleResult("690002", fldName, "150s", sis);
		assertSingleResult("2328381", fldName, "820s", sis);
		assertSingleResult("1849258", fldName, "350s", sis);	
		assertSingleResult("2214009", fldName, "370s", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("11");
		assertSearchResults(fldName, "000s", docIds, sis);
		docIds.clear();
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "020s", docIds, sis);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "990s", docIds, sis);
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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
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
	 * dewey-b4cutter contains the portion of local dewey call numbers 
	 * before the Cutter.  Stored because it's used in GetMoreLikeThis queries.
	 */
@Test
	public final void testDeweyB4CutterField() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-b4cutter";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		
		assertFieldStored(fldName, solrCore); 
		assertFieldIndexed(fldName, solrCore);
		
		assertDocHasFieldValue("690002", fldName, "159.32", sis); 
		assertDocHasFieldValue("2328381", fldName, "827.5", sis); 
		assertDocHasFieldValue("1849258", fldName, "352.042", sis); 
		assertDocHasFieldValue("2214009", fldName, "370.1", sis); 
		assertDocHasNoFieldValue("2214009", fldName, "WITHDRAWN", sis);
		assertDocHasFieldValue("1", fldName, "001", sis); 
		assertDocHasFieldValue("11", fldName, "001.123", sis); 
		assertDocHasFieldValue("2", fldName, "022", sis); 
		assertDocHasFieldValue("22", fldName, "022.456", sis); 
		assertDocHasFieldValue("3", fldName, "999", sis); 
		assertDocHasFieldValue("31", fldName, "999.85", sis); 
		
		// search for values
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
	 * dewey-callnum contains local dewey call numbers.  It is used for display.
	 */
@Test
	public final void testDeweyCallnumsStored() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "dewey-callnum";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		
		assertFieldStored(fldName, solrCore);  
		assertFieldIndexed(fldName, solrCore);  // will be used in shelflist browse
		
		assertDocHasFieldValue("690002", fldName, "159.32 .W211", sis); 
		assertDocHasFieldValue("2328381", fldName, "827.5 .S97TG", sis); 
		assertDocHasFieldValue("1849258", fldName, "352.042 .C594 ED.2", sis); 
		assertDocHasFieldValue("2214009", fldName, "370.1 .S655", sis); 
		assertDocHasFieldValue("1", fldName, "001 .N44", sis); 
		assertDocHasFieldValue("11", fldName, "001.123 .N44", sis); 
		assertDocHasFieldValue("2", fldName, "022 .N47", sis); 
		assertDocHasFieldValue("22", fldName, "022.456 .S655", sis); 
		assertDocHasFieldValue("3", fldName, "999 .F67", sis); 
		assertDocHasFieldValue("31", fldName, "999.85 .P84", sis); 
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
		assertSearchResults(fldName, "000s", docIds, sis);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "900s", docIds, sis);

		fldName = "dewey-first-desc";
		// not stored: search for values
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

		fldName = "dewey-2digits";
		// not stored: search for values
		docIds.clear();
		docIds.add("1");
		docIds.add("11");
		assertSearchResults(fldName, "000s", docIds, sis);
		docIds.clear();
		docIds.add("2");
		docIds.add("22");
		assertSearchResults(fldName, "020s", docIds, sis);
		docIds.clear();
		docIds.add("3");
		docIds.add("31");
		assertSearchResults(fldName, "990s", docIds, sis);

		fldName = "dewey-2digit-desc";
		// not stored: search for values
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

		fldName = "dewey-b4cutter";
		// stored: retrieve values from specific docs
		assertDocHasFieldValue("1", fldName, "001", sis); 
		assertDocHasFieldValue("11", fldName, "001.123", sis); 
		assertDocHasFieldValue("2", fldName, "022", sis); 
		assertDocHasFieldValue("22", fldName, "022.456", sis); 
		assertDocHasFieldValue("3", fldName, "999", sis); 
		assertDocHasFieldValue("31", fldName, "999.85", sis); 
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
		Set<String> docIds = new HashSet<String>();
		docIds.add("1111");
		docIds.add("2211");
		docIds.add("3311");
		
		for (String docId : docIds) {
			assertDocHasNoField(docId, "lc-first-letter", sis);
			assertDocHasNoField(docId, "lc-first-desc", sis);
			assertDocHasNoField(docId, "lc-alpha", sis);
			assertDocHasNoField(docId, "lc-alpha-desc", sis);
			assertDocHasNoField(docId, "lc-b4cutter", sis);
			assertDocHasNoField(docId, "dewey-first-digit", sis);
			assertDocHasNoField(docId, "dewey-first-desc", sis);
			assertDocHasNoField(docId, "dewey-2digits", sis);
			assertDocHasNoField(docId, "dewey-2digit-desc", sis);
			assertDocHasNoField(docId, "dewey-b4cutter", sis);
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
		
		assertSearchResults(fldName, "\"" + fldVal + "\"", docIds, sis);
	}

}
