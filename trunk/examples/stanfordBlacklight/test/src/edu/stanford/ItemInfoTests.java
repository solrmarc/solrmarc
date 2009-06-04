package edu.stanford;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import edu.stanford.StanfordIndexer.Access;

/**
 * junit4 tests for Stanford University's fields derived from item info in 
 * 999 other than call number (building_facet, access_facet, location, barcode,
 * etc.)
 * @author Naomi Dushay
 */
public class ItemInfoTests extends BibIndexTest {
	
	/**
	 * test that item records are NOT ignored appropriately
	 */
@Test
	public final void testValidLocations() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("locationTests.mrc");
		String fldVal = "\"Green (Humanities & Social Sciences)\"";
		Set<String> docIds = new HashSet<String>();
		docIds.add("115472");
		docIds.add("1261173");
		docIds.add("2557826");
		assertSearchResults(fldName, fldVal, docIds, sis);
				
		fldVal = "\"Hoover Institution\"";
		docIds.clear();
		docIds.add("229800");
		docIds.add("460947");
		docIds.add("690002");
		assertSearchResults(fldName, fldVal, docIds, sis);
		
		assertSingleResult("7651581", fldName, "\"SAL 3 (Off-campus)\"", sis);
	}		
	

	/**
	 * Test population of building field (a facet)
	 */
@Test
	public final void testBuildingFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("buildingTests.mrc");
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
//	    assertSingleResult("115472", fldName, "\"Applied Physics Department\"", sis);
	    assertZeroResults(fldName, "\"Applied Physics Department\"", sis);
	    assertSingleResult("229800", fldName, "\"Archive of Recorded Sound\"", sis);
	    assertSingleResult("345228", fldName, "\"Art & Architecture\"", sis);
	    assertSingleResult("460947", fldName, "\"Falconer (Biology)\"", sis);
	    assertSingleResult("804724", fldName, "\"SAL Newark (Off-campus)\"", sis);
	    assertSingleResult("919006", fldName, "\"Swain (Chemistry & Chem. Engineering)\"", sis);
	    assertSingleResult("1147269", fldName, "\"Classics Dept.\"", sis);
//	    assertSingleResult("1391080", fldName, "\"Green - Current Periodicals & Microtext\"", sis);
	    assertZeroResults(fldName, "\"Green - Current Periodicals & Microtext\"", sis);
	    assertSingleResult("1505065", fldName, "\"Branner (Earth Sciences & Maps)\"", sis);
	    assertSingleResult("1618836", fldName, "\"Cubberley (Education)\"", sis);
	    assertSingleResult("1732616", fldName, "\"Math & Computer Science\"", sis);
	    assertSingleResult("1849258", fldName, "Engineering", sis);
	    assertSingleResult("2099904", fldName, "\"Jonsson (Government Documents)\"", sis);
	    assertSingleResult("2678655", fldName, "\"Jackson (Business)\"", sis);
//	    assertSingleResult("2797607", fldName, "Meyer", sis);
	    assertZeroResults(fldName, "Meyer", sis);
	    assertSingleResult("3027805", fldName, "\"Miller (Hopkins Marine Station)\"", sis);
	    assertSingleResult("3142611", fldName, "Physics", sis);
	    assertSingleResult("4258089", fldName, "\"Special Collections & Archives\"", sis);
	    assertSingleResult("4428936", fldName, "\"Tanner (Philosophy Dept.)\"", sis);
	    assertSingleResult("4823592", fldName, "\"Crown (Law)\"", sis);
	    assertSingleResult("5666387", fldName, "Music", sis);
	    assertSingleResult("6676531", fldName, "\"East Asia\"", sis);
	
	    Set<String> docIds = new HashSet<String>();
	    docIds.add("3400092");
	    docIds.add("3743949");
	    assertSearchResults(fldName, "\"Hoover Institution\"", docIds, sis);
	    
	    docIds.clear();
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
	    assertSearchResults(fldName, "\"SAL 1&2 (On-campus)\"", docIds, sis);
	
	    docIds.clear();
	    docIds.add("690002");
	    docIds.add("2328381");
	    docIds.add("3941911");
	    docIds.add("7651581");
	    // education - withdrawn;  SAL3 stacks
	    docIds.add("2214009");
	    assertSearchResults(fldName, "\"SAL 3 (Off-campus)\"", docIds, sis);
	
	    docIds.clear();
	    docIds.add("7370014");
	    // ask@lane
	    docIds.add("7233951");
	    assertSearchResults(fldName, "\"Lane (Medical)\"", docIds, sis);
	
	    assertZeroResults(fldName, "\"Stanford University Libraries\"", sis);
//	    docIds.clear();
//	    docIds.add("6493823");
		// INTERNET
//	    docIds.add("7117119");
//	    assertSearchResults(fldName, "\"Stanford University Libraries\"", docIds, sis);
	}
	
	/**
	 * test that item records are ignored when the locations so indicate
	 */
@Test
	public final void testShadowLocations() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("locationTests.mrc");
		// confirm buildings with only shadow locations don't get a value
		assertZeroResults(fldName, "\"Crown (Law)\"", sis);
		assertZeroResults(fldName, "\"Mathematical & Computer Sciences\"", sis);
		assertZeroResults(fldName, "\"Cubberley (Education)\"", sis);
		assertZeroResults(fldName, "\"Meyer\"", sis);
		assertZeroResults(fldName, "\"Stanford Auxiliary Library (SAL 1&2)\"", sis);
		assertZeroResults(fldName, "\"Stanford University Libraries\"", sis);
		
		// there are 3 green locations that aren't shadowed (confirmed in
		//  testValidLocations()
		assertResultSize(fldName, "\"Green (Humanities & Social Sciences)\"", 3, sis);
		
		// there is 1 SAL3 location that isn't shadowed (it's online)
		assertSingleResult("7651581", fldName, "\"SAL 3 (Off-campus)\"", sis);
	}


	/**
	 * test locations to ignore that aren't shadowed, nor internet 
	 */
@Test
	public final void testOtherIgnoredLocations() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("locationTests.mrc");
		// there are 3 green locations that aren't ignored for any reason
		//  (confirmed in testValidLocations()
		//   the docs that should be ignored are 345228, 575946, 919006
		assertResultSize(fldName, "\"Green (Humanities & Social Sciences)\"", 3, sis);
	}
	

	/**
	 * test for locations that are various flavors of online
	 */
@Test
	public final void testOnlineLocations() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "building_facet";
		createIxInitVars("locationTests.mrc");
		//   three of these locations are in hoover library
	 	assertDocHasNoFieldValue("115472", fldName, "Green", sis); 
		String fldVal = "\"Hoover Institution\"";
		Set<String> docIds = new HashSet<String>();
		docIds.add("229800");
		docIds.add("460947");
		docIds.add("690002");
		assertSearchResults(fldName, fldVal, docIds, sis);
	 	
	 	// access_facet is indexed, not stored, so also test by searching
	 	fldName = "access_facet";
	 	fldVal = Access.ONLINE.toString();
	 	docIds.clear();
	 	docIds.add("115472");
	 	docIds.add("229800");
	 	docIds.add("460947");
	 	docIds.add("690002");
	 	assertSearchResults(fldName, fldVal, docIds, sis);
	}


	/**
	 * test if barcode_search field is populated correctly
	 */
@Test
	public final void testBarcodeSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "barcode_search";
		createIxInitVars("locationTests.mrc");
		assertStringFieldProperties(fldName, solrCore, sis);
		// assertFieldOmitsNorms(fldName, solrCore); - in String field check
		assertFieldIndexed(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);

		// single barcode in the record
		assertSingleResult("115472", fldName, "36105033811451", sis);
		// multiple barcodes in the record
		assertSingleResult("1033119", fldName, "36105037439663", sis);
		assertSingleResult("1033119", fldName, "36105001623284", sis);
	}


	/**
	 * test if item_display field is populated correctly, focusing on building/library
	 *  barcode - library(short version) - location(categories) - callnumber (no volume/part info)
	 * separated by " -|- " (with the spaces)
	 */
 @Test
	public final void testItemDisplayBuildings()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);

		// APPLIEDPHY ignored
		assertDocHasNoField("115472", fldName, sis); 
		// ARS
		assertDocHasFieldValue("229800", fldName, "36105034181003 -|- ARS -|- Stacks -|- HG6046 .V28 1986", sis);
		// ART
		assertDocHasFieldValue("345228", fldName, "36105043140537 -|- Art -|- Stacks -|- D764.7 .K72 1990", sis);
		// BIOLOGY  -  volume in callnum:   E184.S75 R47A V.1 1980
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Falconer -|- Stacks -|- E184.S75 R47A", sis);
		// CHEMCHMENG 
		assertDocHasFieldValue("919006", fldName, "36105036688153 -|- Swain -|- Stacks -|- PA3998 .H2 O5 1977", sis);
		// CLASSICS
		assertDocHasFieldValue("1147269", fldName, "36105037871261 -|- Classics -|- Stacks -|- PR9184.6 .M3", sis);
		// CPM 
		assertDocHasFieldValue("1391080", fldName, "36105038701285 -|- Green -|- Stacks -|- PQ6653.A646.V5", sis); 
		// EARTH-SCI
		assertDocHasFieldValue("1505065", fldName, "36105039395095 -|- Branner -|- Stacks -|- TD811.5 .G76 1983", sis); 
		// EAST-ASIA
		assertDocHasFieldValue("6676531", fldName, "36105095758004 -|- East Asia -|- Japanese Collection -|- RD35 .H34 1982", sis); 
		// EDUCATION
		assertDocHasFieldValue("1618836", fldName, "36105040261765 -|- Cubberley -|- Stacks -|- PQ6666.E7484 B4 1983", sis); 
		// ENG
		assertDocHasFieldValue("1849258", fldName, "36105047516096 -|- Engineering -|- Stacks -|- 352.042 .C594 ED.2", sis); 
		// GOV-DOCS
		assertDocHasFieldValue("2099904", fldName, "36105041442281 -|- Gov't Docs -|- Stacks -|- DK43 .B63", sis);
		// GREEN 
// TODO:  vol call number for microfilm
//		assertDocHasFieldValue("1261173", fldName, "001AFX2969 -|- Green -|- Media Microtext -|- MFILM N.S. 1350 REEL 230 NO. 3741", sis);
		// volume in callnum:   CB3 .A6 SUPPL. V.31
		assertDocHasFieldValue("575946", fldName, "36105035087092 -|- Green -|- Stacks -|- CB3 .A6 SUPPL.", sis);
// TODO:  vol call number info for other call numbers
//		assertDocHasFieldValue("2557826", fldName, "001AMR5851 -|- Green -|- US Federal Documents -|- E 1.28:COO-4274-1", sis); 
//		assertDocHasFieldValue("4114632", fldName, "4114632-1001 -|- Green -|- US Federal Documents -|- ITC 1.15/3:", sis); 
		assertDocHasFieldValue("3941911", fldName, "36105025373064 -|- Green -|- Bender Reading Room (Non-circulating) -|- PS3557 .O5829 K3 1998", sis); 
		// GRN-REF
		assertDocHasFieldValue("2442876", fldName, "36105043436257 -|- Green -|- Stacks -|- PQ2678.I26 P54 1992", sis);
		// HOOVER
		assertDocHasFieldValue("3743949", fldName, "3743949-1001 -|- Hoover -|- Stacks -|- PQ6613 .A73 G44", sis); 
		// HOPKINS
		assertDocHasFieldValue("3027805", fldName, "36105016935392 -|- Hopkins -|- Stacks -|- DG579 .A5 A5 1995", sis); 
		// HV-ARCHIVE
		assertDocHasFieldValue("3400092", fldName, "36105020376997 -|- Hoover -|- Stacks -|- DC34.5 .A78 L4 1996", sis); 
		// ILB
		assertDocHasFieldValue("1111", fldName, "36105129694373 -|- Green -|- Stacks -|- Z666 .P31 C6 1946", sis); 
		// JACKSON
//		assertDocHasFieldValue("2678655", fldName, "001ANE5736 -|- Jackson =|- Stacks -|- GA 1.13:RCED-85-88", sis);
		// LANE-MED
// TODO: need lane examples with actual values
		assertDocHasNoFieldValue("7233951", fldName, "LL124341 -|- Lane -|- Inquire -|- X578 .S64 1851", sis); 
		assertDocHasNoFieldValue("7370014", fldName, "LL94379 -|- Lane -|- Stacks -|- NO CALL NUMBER", sis); 
		// LAW
// TODO: gov docs call numbers
//		assertDocHasFieldValue("4823592", fldName, "36105063104488 -|- Crown -|- Basement -|- Y 4.G 74/7:G 21/10", sis); 
		// MATH-CS
		assertDocHasFieldValue("1732616", fldName, "36105033142303 -|- Math/CS -|- Stacks -|- QA273 .C83 1962", sis); 
		// MEYER - ignored
		assertDocHasNoField("2797607", fldName, sis);
		// MUSIC
		assertDocHasFieldValue("5666387", fldName, "36105114964369 -|- Music -|- Stacks -|- ML410 .S54 I58 2000", sis); 
		// PHYSICS
		assertDocHasFieldValue("3142611", fldName, "36105017175519 -|- Physics -|- Stacks -|- PS3553 .L337 F76 1978", sis); 
		// SAL
		assertDocHasFieldValue("1962398", fldName, "36105082101390 -|- SAL 1&2 -|- In Transit -|- Z3871.Z8", sis); 
		assertDocHasFieldValue("2913114", fldName, "36105082973251 -|- SAL 1&2 -|- In Transit -|- DS135 .P6 I65", sis); 
		// SAL3
		assertDocHasFieldValue("690002", fldName, "36105046693508 -|- SAL 3 -|- Stacks -|- 159.32 .W211", sis);
		//    in process
		assertDocHasFieldValue("7651581", fldName, "36105129694373 -|- SAL 3 -|- In Process -|- PQ9661 .P31 C6 1946", sis); 
		// SAL-NEWARK
		assertDocHasFieldValue("804724", fldName, "36105035887392 -|- SAL Newark -|- Stacks -|- Z7164.T23.W45", sis);
		// SPEC-COLL
// TODO:  gov doc callnum
//		assertDocHasFieldValue("4258089", fldName, "4258089-1001 -|- Special Coll. -|- Stacks -|- NAS 1.26:205100", sis); 
		// SPEC-DESK
		assertDocHasFieldValue("2222", fldName, "36105129694373 -|- Green -|- Stacks -|- S666 .P31 C6 1946", sis);
		// TANNER
		assertDocHasFieldValue("4428936", fldName, "36105021909747 -|- Tanner -|- Stacks -|- PN1993.5 .I88 C5618 2000", sis); 
		// SUL
		assertDocHasFieldValue("6493823", fldName, "36105122224160 -|- SUL -|- Stacks -|- F1356 .M464 2005", sis); 
		//    internet resource
		assertDocHasNoField("7117119", fldName, sis); 
				
		// multiple items in single record, diff buildings
		assertDocHasFieldValue("1033119", fldName, "36105037439663 -|- Green -|- Stacks -|- BX4659.E85 W44", sis);
		assertDocHasFieldValue("1033119", fldName, "36105001623284 -|- SAL 1&2 -|- Stacks -|- BX4659 .E85 W44 1982", sis);
		
// TODO:  mult items same build, diff loc
		
		//   same build, same loc, same callnum, one in another building
		assertDocHasFieldValue("2328381", fldName, "36105003934432 -|- SAL 1&2 -|- Stacks -|- PR3724.T3", sis);
		assertDocHasFieldValue("2328381", fldName, "36105003934424 -|- SAL 1&2 -|- Stacks -|- PR3724.T3", sis);
		assertDocHasFieldValue("2328381", fldName, "36105048104132 -|- SAL 3 -|- Stacks -|- 827.5 .S97TG", sis);
	}


	/**
	 * test if item_display field is populated correctly, focusing on locations
	 *  barcode - library(short version) - location(categories) - callnumber (no volume/part info)
	 * separated by " -|- " (with the spaces)
	 */
 @Test
	public final void testItemDisplayLocations()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("229800", fldName, "36105034181003 -|- ARS -|- Stacks -|- HG6046 .V28 1986", sis);
		assertDocHasFieldValue("6676531", fldName, "36105095758004 -|- East Asia -|- Japanese Collection -|- RD35 .H34 1982", sis); 
		assertDocHasNoFieldValue("7233951", fldName, "LL124341 -|- Lane -|- Inquire -|- X578 .S64 1851", sis); 
		assertDocHasFieldValue("1962398", fldName, "36105082101390 -|- SAL 1&2 -|- In Transit -|- Z3871.Z8", sis); 
		assertDocHasFieldValue("2913114", fldName, "36105082973251 -|- SAL 1&2 -|- In Transit -|- DS135 .P6 I65", sis); 
		assertDocHasFieldValue("7651581", fldName, "36105129694373 -|- SAL 3 -|- In Process -|- PQ9661 .P31 C6 1946", sis); 

		// TODO: on order locations
		// TODO: reserve locations
		
		// TODO:  vol call number for microfilm
//		assertDocHasFieldValue("1261173", fldName, "001AFX2969 -|- Green -|- Media Microtext -|- MFILM N.S. 1350 REEL 230 NO. 3741", sis);
// TODO:  vol call number info for other call numbers
//		assertDocHasFieldValue("2557826", fldName, "001AMR5851 -|- Green -|- US Federal Documents -|- E 1.28:COO-4274-1", sis); 
//		assertDocHasFieldValue("4114632", fldName, "4114632-1001 -|- Green -|- US Federal Documents -|- ITC 1.15/3:", sis); 
		
		// first is non-circulating location
		assertDocHasFieldValue("3941911", fldName, "36105025373064 -|- Green -|- Bender Reading Room (Non-circulating) -|- PS3557 .O5829 K3 1998", sis); 
		assertDocHasFieldValue("3941911", fldName, "36105019748495 -|- SAL 3 -|- Stacks -|- PS3557 .O5829 K3 1998", sis);

		// withdrawn - no item_display field
		assertDocHasNoFieldValue("3277173", fldName, "001ALW4497 -|- Green -|- Withdrawn -|- WDRNALW4497", sis); 
		assertDocHasNoField("3277173", fldName, sis);

		// one withdrawn location, one valid 
		assertDocHasNoFieldValue("2214009", fldName, "36105033336798 -|- Cubberley -|- Withdrawn -|- 370.1 .S655", sis); 
		assertDocHasFieldValue("2214009", fldName, "36105033336780 -|- SAL 3 -|- Stacks -|- 370.1 .S655", sis); 

		// online locations:  ELECTR-LOC  INTERNET  ONLINE-TXT RESV-URL
		assertDocHasNoFieldValue("7117119", fldName, "7117119-1001 -|- SUL -|- INTERNET RESOURCE -|- INTERNET", sis);
		assertDocHasNoField("7117119", fldName, sis);  
		
// TODO:  multiple items for single bib with same library / location, diff callnum
//  with same library, diff location

		// use file with SHELBY locations
		tearDown();
		createIxInitVars("callNumberLCSortTests.mrc");
		// SHELBYTITL  SHELBYSER  STORBYTITL
		assertDocHasFieldValue("1111", fldName, "36105129694373 -|- Swain -|- Serials -|- Shelved by title", sis);
		assertDocHasFieldValue("2211", fldName, "36105129694374 -|- Swain -|- Shelved by series title", sis);
		assertDocHasFieldValue("3311", fldName, "36105129694375 -|- Swain -|- Storage Area -|- Shelved by title", sis);
		
		// use file with more invalid locations
		tearDown();
		createIxInitVars("locationTests.mrc");

		// online locations:  ELECTR-LOC  INTERNET  ONLINE-TXT RESV-URL
		assertDocHasNoFieldValue("115472", fldName, "36105033811451 -|- Green -|- ELECTR-LOC -|- 1HC241.25 .I4 D47", sis);
		assertDocHasNoField("115472", fldName, sis);
		assertDocHasNoFieldValue("229800", fldName, "36105034181003 -|- Hoover -|- INTERNET -|- HG6046 .V28 1986", sis);
		assertDocHasNoField("229800", fldName, sis);
		//             call num is E184.S75 R47A V.1 1980
		assertDocHasNoFieldValue("460947", fldName, "36105007402873 -|- Hoover -|- ONLINE-TXT -|- E184.S75 R47A", sis);
		assertDocHasNoField("460947", fldName, sis);
		assertDocHasNoFieldValue("690002", fldName, "36105046693508 -|- Hoover -|- RESV-URL -|- 159.32 .W211", sis);
		assertDocHasNoField("690002", fldName, sis);
		// discard
		assertDocHasNoFieldValue("345228", fldName, "36105043140537 -|- Green -|- DISCARD-NS -|- D764.7 .K72 1990", sis);
		assertDocHasNoField("345228", fldName, sis);
		// withdrawn   callnum is CB3 .A6 SUPPL. V.31
		assertDocHasNoFieldValue("575946", fldName, "36105035087092 -|- Green -|- WITHDRAWN -|- CB3 .A6 SUPPL.", sis);
		assertDocHasNoField("575946", fldName, sis);
		// lost
		assertDocHasNoFieldValue("1505065", fldName, "36105039395095 -|- Green -|- LOST -|- TD811.5 .G76 1983", sis);
		assertDocHasNoField("1505065", fldName, sis);  		
		// shadow locations
		assertDocHasNoFieldValue("804724", fldName, "36105035887392 -|- Green -|- FED-DOCS-S -|- Z7164.T23.W45", sis);
		assertDocHasNoField("804724", fldName, sis);  
		assertDocHasNoFieldValue("919006", fldName, "36105036688153 -|- Green -|- SUPERSEDED -|- PA3998 .H2 O5 1977", sis);
		assertDocHasNoField("919006", fldName, sis);  
		assertDocHasNoFieldValue("1033119", fldName, "36105037439663 -|- Green -|- CDPSHADOW -|- BX4659.E85 W44", sis);
		assertDocHasNoFieldValue("1033119", fldName, "36105001623284 -|- Crown -|- TECHSHADOW -|- BX4659 .E85 W44 1982", sis);
		assertDocHasNoField("1033119", fldName, sis);  
	}


	/**
	 * test if item_display field is populated correctly, focused on callnums
	 *  barcode - library(short version) - location(categories) - callnumber (no volume/part info)
	 * separated by " -|- " (with the spaces)
	 */
 @Test
	public final void testItemDisplayCallnums()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);

		// LC
		// volume in callnum:   E184.S75 R47A V.1 1980
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Falconer -|- Stacks -|- E184.S75 R47A", sis);
		// volume in callnum:   CB3 .A6 SUPPL. V.31
		assertDocHasFieldValue("575946", fldName, "36105035087092 -|- Green -|- Stacks -|- CB3 .A6 SUPPL.", sis);

		// DEWEY
		assertDocHasFieldValue("690002", fldName, "36105046693508 -|- SAL 3 -|- Stacks -|- 159.32 .W211", sis);

		// SUDOC
//		assertDocHasFieldValue("2557826", fldName, "001AMR5851 -|- Green -|- US Federal Documents -|- E 1.28:COO-4274-1", sis); 
//		assertDocHasFieldValue("4114632", fldName, "4114632-1001 -|- Green -|- US Federal Documents -|- ITC 1.15/3:", sis); 
//		assertDocHasFieldValue("2678655", fldName, "001ANE5736 -|- Jackson =|- Stacks -|- GA 1.13:RCED-85-88", sis);
		
		// ALPHANUM
		assertDocHasFieldValue("1261173", fldName, "001AFX2969 -|- Green -|- Media Microtext -|- MFILM N.S. 1350 REEL 230 NO. 3741", sis);
		
		// ASIS
		
		// SUSEL
		
		// (other one)
				
		// TODO:  multiple volumes for single bib with same callnumber
		//   due to vol stripping
		//   due to multiple buildings
		//   due to multiple copies

		// NO CALL NUMBER
		assertDocHasNoFieldValue("7370014", fldName, "LL94379 -|- Lane -|- Stacks -|- NO CALL NUMBER", sis);
		assertDocHasNoField("7370014", fldName, sis);
		
		// TODO: test the ignore callnum locations: SHELBYTITL  SHELBYSER  STORBYTITL
		// TODO: test invalid LC or Dewey call number
		// TODO: other oddities
		
		// two items in one location with same call number, one in another location
		assertDocHasFieldValue("2328381", fldName, "36105003934432 -|- SAL 1&2 -|- Stacks -|- PR3724.T3", sis);
		assertDocHasFieldValue("2328381", fldName, "36105003934424 -|- SAL 1&2 -|- Stacks -|- PR3724.T3", sis);
		assertDocHasFieldValue("2328381", fldName, "36105048104132 -|- SAL 3 -|- Stacks -|- 827.5 .S97TG", sis);
	}

}
