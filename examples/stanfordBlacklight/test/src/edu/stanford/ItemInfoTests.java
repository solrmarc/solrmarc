package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's fields derived from item info in 
 * 999 other than call number (building_facet, access_facet, location, barcode,
 * etc.)
 * @author Naomi Dushay
 */
public class ItemInfoTests extends AbstractStanfordBlacklightTest {
	
	
	/**
	 * Test building facet values.  Skipped building values are in a separate test
	 */
@Test
	public final void testBuildingFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("buildingTests.mrc");
		assertFacetFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		
	    assertSingleResult("229800", fldName, "\"Archive of Recorded Sound\"");
	    assertSingleResult("345228", fldName, "\"Art & Architecture\"");
	    assertSingleResult("460947", fldName, "\"Falconer (Biology)\"");
	    assertSingleResult("804724", fldName, "\"SAL Newark (Off-campus)\"");
	    assertSingleResult("919006", fldName, "\"Swain (Chemistry & Chem. Engineering)\"");
	    assertSingleResult("1147269", fldName, "\"Classics\"");
	    assertSingleResult("1505065", fldName, "\"Branner (Earth Sciences & Maps)\"");
	    assertSingleResult("1618836", fldName, "\"Cubberley (Education)\"");
	    assertSingleResult("1732616", fldName, "\"Math & Computer Science\"");
	    assertSingleResult("1849258", fldName, "Engineering");
	    assertSingleResult("2099904", fldName, "\"Jonsson (Government Documents)\"");
	    assertSingleResult("2678655", fldName, "\"Jackson (Business)\"");
	    assertSingleResult("3027805", fldName, "\"Miller (Hopkins Marine Station)\"");
	    assertSingleResult("3142611", fldName, "Physics");
	    assertSingleResult("4258089", fldName, "\"Special Collections & Archives\"");
	    assertSingleResult("4428936", fldName, "\"Tanner (Philosophy Dept.)\"");
	    assertSingleResult("4823592", fldName, "\"Crown (Law)\"");
	    assertSingleResult("5666387", fldName, "Music");
	    assertSingleResult("6676531", fldName, "\"East Asia\"");
	    assertSingleResult("2797607", fldName, "Meyer");
	
	    // hoover tests are a separate method below
	    
	    Set<String> docIds = new HashSet<String>();
	    docIds.add("1033119");
	    docIds.add("1261173");
	    docIds.add("2557826");
	    docIds.add("3941911");
	    docIds.add("4114632");
	    // checked out
	    docIds.add("575946");
	    // NOT  3277173  (withdrawn)
	    assertSearchResults(fldName, "\"Green (Humanities & Social Sciences)\"", docIds);
	
	    docIds.clear();
	    docIds.add("1033119");
	    docIds.add("1962398");
	    docIds.add("2328381");
	    docIds.add("2913114");
	    assertSearchResults(fldName, "\"Stanford Auxiliary Library (On-campus)\"", docIds);
	
	    docIds.clear();
	    docIds.add("690002");
	    docIds.add("2328381");
	    docIds.add("3941911");
	    docIds.add("7651581");
	    // education - withdrawn;  SAL3 stacks
	    docIds.add("2214009");
	    assertSearchResults(fldName, "\"SAL3 (Off-campus)\"", docIds);
	
	    docIds.clear();
	    docIds.add("7370014");
	    // ask@lane
	    docIds.add("7233951");
	    assertSearchResults(fldName, "\"Lane (Medical)\"", docIds);
	}

	/**
	 * ensure that there are no building facet values for items that are in
	 *  buildings without translations in the library_map 
	 */
@Test
	public void testSkipBuildingFacet()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("buildingTests.mrc");
		
		// APPLIEDPHY (Applied Physics Department is no longer a valid building)
//	    assertSingleResult("115472", fldName, "\"Applied Physics Department\"");  
	    assertZeroResults(fldName, "\"Applied Physics Department\"");
	    assertZeroResults(fldName, "\"APPLIEDPHY\"");

	    // CPM not a valid building
//	    assertSingleResult("1391080", fldName, "\"Green - Current Periodicals & Microtext\""); 
	    assertZeroResults(fldName, "\"Green - Current Periodicals & Microtext\"");
	    assertZeroResults(fldName, "\"CPM\"");

	    // GRN-REF Green - Reference - Obsolete
//	    assertSingleResult("2442876", fldName, "\"Green - Reference\""); 
	    assertZeroResults(fldName, "\"Green - Reference\"");
	    assertZeroResults(fldName, "\"GRN-REF\"");

	    // ILB Inter-Library Borrowing - Obsolete
//	    assertSingleResult("1111", fldName, "\"Inter-Library Borrowing\""); 
	    assertZeroResults(fldName, "\"Inter-Library Borrowing\"");
	    assertZeroResults(fldName, "\"ILB\"");
	    
	    // SPEC-DESK   Green (Humanities & Social Sciences)   not a valid building
//	    assertSingleResult("2222", fldName, "Green (Humanities & Social Sciences)");
	    assertZeroResults(fldName, "Green (Humanities & Social Sciences)");
	    assertZeroResults(fldName, "SPEC-DESK");

	    // SUL  Stanford University Libraries   not a valid building
//	    assertSingleResult("6493823", fldName, "Stanford University Libraries");
//	    assertSingleResult("7117119", fldName, "Stanford University Libraries");
	    assertZeroResults(fldName, "\"Stanford University Libraries\"");
	    assertZeroResults(fldName, "\"SUL\"");
	}

	/**
	 * ensure that the two hoover library codes have separate values for the 
	 *  building facet
	 */
@Test
	public void testHoover2Locs()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("buildingTests.mrc");
		
	    assertSingleResult("3743949", fldName, "\"Hoover Library\"");
	    assertSingleResult("3400092", fldName, "\"Hoover Archives\"");
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
		assertTextFieldProperties(fldName);
		assertFieldOmitsNorms(fldName);
		assertFieldIndexed(fldName);
		assertFieldNotStored(fldName);
		assertFieldMultiValued(fldName);

		// single barcode in the record
		assertSingleResult("115472", fldName, "36105033811451");
		// multiple barcodes in the record
		assertSingleResult("1033119", fldName, "36105037439663");
		assertSingleResult("1033119", fldName, "36105001623284");
	}


	String sep = " -|- ";
	boolean isSerial = true;

	
	/**
	 * test if item_display field is populated correctly, focusing on building/library
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     callnum_sort (from lopped call num) -|- 
	 *     callnum_reverse_sort (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
 @Test
	public final void testItemDisplayBuildings()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");
		assertDisplayFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		
		// APPLIEDPHY ignored for building facet, but not here
		String callnum = "HC241.25 .I4 D47";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("115472", fldName, "36105033811451 -|- APPLIEDPHY -|- Stacks -|- " + 
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// ARS
		callnum = "HG6046 .V28 1986";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("229800", fldName, "36105034181003 -|- ARS -|- Stacks -|- " + 
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		// ART
		callnum = "D764.7 .K72 1990";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("345228", fldName, "36105043140537 -|- Art -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// BIOLOGY 
		callnum = "E184.S75 R47A V.1 1980";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Falconer -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// CHEMCHMENG 
		callnum = "PA3998 .H2 O5 1977";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("919006", fldName, "36105036688153 -|- Swain -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// CLASSICS
		callnum = "PR9184.6 .M3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1147269", fldName, "36105037871261 -|- Classics -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// CPM 
		callnum = "PQ6653.A646.V5";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1391080", fldName, "36105038701285 -|- CPM -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// EARTH-SCI
		callnum = "TD811.5 .G76 1983";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1505065", fldName, "36105039395095 -|- Branner -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// EAST-ASIA
		callnum = "RD35 .H34 1982";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("6676531", fldName, "36105095758004 -|- East Asia -|- Japanese Collection -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// EDUCATION
		callnum = "PQ6666.E7484 B4 1983";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1618836", fldName, "36105040261765 -|- Cubberley -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// ENG
		callnum = "352.042 .C594 ED.2";
		lopped = CallNumUtils.removeDeweyVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1849258", fldName, "36105047516096 -|- Engineering -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// GOV-DOCS
		callnum = "DK43 .B63";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2099904", fldName, "36105041442281 -|- Government Docs -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// GREEN 
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1261173", fldName, "001AFX2969 -|- Green -|- Media-Microtext Collection (Lower Level) -|- " +
					lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// GRN-REF
		callnum = "PQ2678.I26 P54 1992";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2442876", fldName, "36105043436257 -|- GRN-REF -|- Stacks -|- " +
					callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// HOOVER
		callnum = "PQ6613 .A73 G44";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("3743949", fldName, "3743949-1001 -|- Hoover Library -|- Stacks -|- " +
					callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// HOPKINS
		callnum = "DG579 .A5 A5 1995";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("3027805", fldName, "36105016935392 -|- Hopkins -|- Stacks -|- " +
					callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// HV-ARCHIVE
		callnum = "DC34.5 .A78 L4 1996";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("3400092", fldName, "36105020376997 -|- Hoover Archives -|- Stacks -|- " +
					callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// ILB
		callnum = "Z666 .P31 C6 1946";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1111", fldName, "36105129694373 -|- ILB -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// JACKSON
		callnum = "GA 1.13:RCED-85-88";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2678655", fldName, "001ANE5736 -|- Jackson -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// LANE-MED
		callnum = "X578 .S64 1851";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, "LC", "7233951");
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("7233951", fldName, "LL124341 -|- Lane -|- Check Lane catalog for status -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// LAW
		callnum = "Y 4.G 74/7:G 21/10";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("4823592", fldName, "36105063104488 -|- Crown -|- Basement -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// MATH-CS
		callnum = "QA273 .C83 1962";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1732616", fldName, "36105033142303 -|- Math/CS -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// MEYER
		callnum = "B781 .A33 I55 1993";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2797607", fldName, "36105004381195 -|- Meyer -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// MUSIC
		callnum = "ML410 .S54 I58 2000";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("5666387", fldName, "36105114964369 -|- Music -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// PHYSICS
		callnum = "PS3553 .L337 F76 1978";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("3142611", fldName, "36105017175519 -|- Physics -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// SAL
		callnum = "DS135 .P6 I65";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2913114", fldName, "36105082973251 -|- SAL -|- Temporary Shelving -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// SAL3
		callnum = "159.32 .W211";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("690002", fldName, "36105046693508 -|- SAL3 -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// SAL-NEWARK
		callnum = "Z7164.T23.W45";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("804724", fldName, "36105035887392 -|- SAL Newark -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// SPEC-COLL
		callnum = "NAS 1.26:205100";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("4258089", fldName, "4258089-1001 -|- Special Collections -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// SPEC-DESK
		callnum = "S666 .P31 C6 1946";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2222", fldName, "36105129694373 -|- SPEC-DESK -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// TANNER
		callnum = "PN1993.5 .I88 C5618 2000";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("4428936", fldName, "36105021909747 -|- Tanner -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// SUL
		callnum = "F1356 .M464 2005";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("6493823", fldName, "36105122224160 -|- SUL -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// multiple items in single record, diff buildings
		callnum = "BX4659.E85 W44";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1033119", fldName, "36105037439663 -|- Green -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		callnum = "BX4659 .E85 W44 1982";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1033119", fldName, "36105001623284 -|- SAL -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		//   same build, same loc, same callnum, one in another building
		callnum = "PR3724.T3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2328381", fldName, "36105003934432 -|- SAL -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		assertDocHasFieldValue("2328381", fldName, "36105003934424 -|- SAL -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		callnum = "827.5 .S97TG";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2328381", fldName, "36105048104132 -|- SAL3 -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		tearDown();
		createIxInitVars("itemDisplayTests.mrc");
		
		// Lane example with actual values
		callnum = "Z3871.Z8 V.22 1945";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("6661112", fldName, "36105082101390 -|- Lane -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// mult items same build, diff loc
		callnum = "PR3724.T3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2328381", fldName, "36105003934432 -|- Green -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		callnum = "PR3724.T3 A2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2328381", fldName, "36105003934424 -|- Green -|- Bindery (Off-Campus) -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		callnum = "827.5 .S97TG";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2328381", fldName, "36105048104132 -|- GRN-REF -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	}


	/**
	 * test if item_display field is populated correctly, focusing on locations
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     callnum_sort (from lopped call num) -|- 
	 *     callnum_reverse_sort (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
 @Test
	public final void testItemDisplayLocations()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");
		
		// stacks
		String callnum = "HG6046 .V28 1986";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("229800", fldName, "36105034181003 -|- ARS -|- Stacks -|- " + 
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "PS3557 .O5829 K3 1998";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("3941911", fldName, "36105019748495 -|- SAL3 -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		callnum = "PS3557 .O5829 K3 1998";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("3941911", fldName, "36105025373064 -|- Green -|- Bender Room: Non-circulating -|- " + 
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		callnum = "RD35 .H34 1982";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("6676531", fldName, "36105095758004 -|- East Asia -|- Japanese Collection -|- " + 
			callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1261173", fldName, "001AFX2969 -|- Green -|- Media-Microtext Collection (Lower Level) -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// inquire
		callnum = "X578 .S64 1851";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, "LC", "7233951");
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("7233951", fldName, "LL124341 -|- Lane -|- Check Lane catalog for status -|- " +
			callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// in transit
		callnum = "Z3871.Z8";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1962398", fldName, "36105082101390 -|- SAL -|- Temporary Shelving -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "DS135 .P6 I65";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2913114", fldName, "36105082973251 -|- SAL -|- Temporary Shelving -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// in process
		callnum = "PQ9661 .P31 C6 1946";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("7651581", fldName, "36105129694373 -|- SAL3 -|- In process -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// gov docs
		callnum = "E 1.28:COO-4274-1";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2557826", fldName, "001AMR5851 -|- Green -|- US Federal Documents -|- " + 
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "ITC 1.15/3:";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("4114632", fldName, "4114632-1001 -|- Green -|- US Federal Documents -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// one withdrawn location, one valid 
		callnum = "370.1 .S655";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasNoFieldValue("2214009", fldName, "36105033336798 -|- Cubberley -|- Withdrawn -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		assertDocHasFieldValue("2214009", fldName, "36105033336780 -|- SAL3 -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// online locations:  ELECTR-LOC  INTERNET  ONLINE-TXT RESV-URL
		assertDocHasNoField("7117119", fldName);  
		
		tearDown();
		createIxInitVars("itemDisplayTests.mrc");
		// on order locations
		callnum = "E184.S75 R47A V.1 1980";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Green -|- On order -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		// reserve locations
		callnum = "159.32 .W211";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("690002", fldName, "36105046693508 -|- Branner -|- Reserves -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		
		// mult items same build, diff loc
		callnum = "PR3724.T3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2328381", fldName, "36105003934432 -|- Green -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "PR3724.T3 A2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2328381", fldName, "36105003934424 -|- Green -|- Bindery (Off-Campus) -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "827.5 .S97TG";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2328381", fldName, "36105048104132 -|- GRN-REF -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		
		// multiple items for single bib with same library / location, diff callnum
		callnum = "PR3724.T3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("666", fldName, "36105003934432 -|- Green -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "PR3724.T3 A2 V.1";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("666", fldName, "36105003934424 -|- Green -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "PR3724.T3 A2 V.2";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("666", fldName, "36105048104132 -|- Green -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	}


	/**
	 * test if item_display field is populated correctly when location is online
	 */
@Test
	public final void testItemDisplayOnlineLocs() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "item_display";
		createIxInitVars("locationTests.mrc");
		
		// online locations do not appear as items in the search results, but
		//   they do appear in nearby on shelf

		// ELECTR-LOC
		String callnum = "HC241.25 .I4 D47";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("115472", fldName, "36105033811451 -|- Online -|- Online -|- " + 
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// INTERNET
		callnum = "HG6046 .V28 1986";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("229800", fldName, "36105034181003 -|- Online -|- Online -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// ONLINE-TXT
		callnum = "E184.S75 R47A V.1 1980";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Online -|- Online -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// RESV-URL is skipped
		assertDocHasNoField("690002", fldName);
	}

	
// display home location
	/**
	 * test if item_display field is populated correctly when there is a current
	 *  location that should be ignored in favor of the home location
	 */
@Test
	public final void testItemDisplayIgnoreCurrentLocs() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");
		
		// these locations should only appear as "current" locations, and they
		//   should be ignored in favor of "home" locations.  The status of the
		//   item (e.g. checked out) will be displayed elsewhere.

		// CHECKEDOUT as current location, Stacks as home location
		String callnum = "CB3 .A6 SUPPL. V.31";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("575946", fldName, "36105035087092 -|- Green -|- Stacks -|- " + 
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// WITHDRAWN as current location implies item is skipped
		assertDocHasNoField("3277173", fldName);
	}


	/**
	 * test if item_display field is populated correctly when location implies
	 *  item is shelved by title (SHELBYTITL  SHELBYSER  STORBYTITL)
	 */
@Test
 	public final void testItemDisplayShelbyLocs()
 			throws ParserConfigurationException, IOException, SAXException 
 	{
		String fldName = "item_display";
		createIxInitVars("callNumberLCSortTests.mrc");

		// callnum for all three is  PQ9661 .P31 C6 VOL 1 1946"
		
		// SHELBYTITL
		String callnum = "Shelved by title";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, "LC", "1111");
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String show_view_callnum = callnum + " VOL 1 1946";
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(show_view_callnum, callnum, isSerial);
		assertDocHasFieldValue("1111", fldName, "36105129694373 -|- Swain -|- Serials -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + show_view_callnum + sep + volSort);
		
		// STORBYTITL
		assertDocHasFieldValue("3311", fldName, "36105129694375 -|- Swain -|- Storage area -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + show_view_callnum + sep + volSort);

		// SHELBYSER
		callnum = "Shelved by Series title";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, "LC", "1111");
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		show_view_callnum = callnum + " VOL 1 1946";
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(show_view_callnum, callnum, isSerial);
		assertDocHasFieldValue("2211", fldName, "36105129694374 -|- Swain -|- Serials -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + show_view_callnum + sep + volSort);
 	}
 	
 	
	/**
	 * test if item_display field is missing when the location shouldn't be
	 *  displayed
	 */
@Test
 	public final void testItemDisplaySkipLocs()
 			throws ParserConfigurationException, IOException, SAXException 
 	{
		String fldName = "item_display";
		createIxInitVars("locationTests.mrc");

// TODO:  the record should not be INDEXED if all items are skipped		
		
		// DISCARD-NS
		assertDocHasNoField("345228", fldName);

		// WITHDRAWN
		assertDocHasNoField("575946", fldName);
		
		// FED-DOCS-S  (shadow)
		assertDocHasNoField("804724", fldName);
		
		// CDPSHADOW and TECHSHADOW
		assertDocHasNoField("1033119", fldName);
		
		// LOST
		assertDocHasNoField("1505065", fldName);
		
		// INPROCESS - keep it
		String callnum = "PQ9661 .P31 C6 1946";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("7651581", fldName, "36105129694373 -|- SAL3 -|- In process -|- " + 
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
 	}


	/**
	 * test if item_display field is populated correctly when location is to
	 *  be left "as is"  (no translation in map, but don't skip)
	 */
@Test
	public final void testAsIsLocations()
			throws ParserConfigurationException, IOException, SAXException 
	{
	String fldName = "item_display";
	createIxInitVars("mediaLocTest.mrc");

	String callnum = "G70.212 .A73934 2008";
	String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
	String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
	String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
	assertDocHasFieldValue("7652182", fldName, "36105130436541 -|- Branner -|- Permanent Reserve: Inquire at loan desk -|- " +
			callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	assertDocHasFieldValue("7652182", fldName, "36105130436848 -|- Branner -|- Reference: Non-circulating -|- " +
			callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	assertDocHasFieldValue("7652182", fldName, "36105130437192 -|- Branner -|- MEDIA -|- " +
			callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	}


 	 	
	/**
	 * test if item_display field is populated correctly, focused on lopped callnums
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     callnum_sort (from lopped call num) -|- 
	 *     callnum_reverse_sort (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
 @Test
	public final void testItemDisplayLoppedCallnums()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");

		// LC
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = "E184.S75 R47A";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Falconer -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		callnum = "CB3 .A6 SUPPL. V.31";
		// NOTE:  it finds V.31, so it doesn't look for SUPPL. preceding it.
		lopped = "CB3 .A6 SUPPL.";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("575946", fldName, "36105035087092 -|- Green -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// DEWEY (no vol)
		callnum = "159.32 .W211";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("690002", fldName, "36105046693508 -|- SAL3 -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// SUDOC (no vol)
		callnum = "E 1.28:COO-4274-1";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("2557826", fldName, "001AMR5851 -|- Green -|- US Federal Documents -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
					
		tearDown();
		createIxInitVars("itemDisplayTests.mrc");

		// LCPER 
		callnum = "E184.S75 R47A V.1 1980";
		lopped = "E184.S75 R47A";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Green -|- On order -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		// DEWEYPER (no vol)
		callnum = "666.27 .F22";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("446688", fldName, "36105007402873 -|- Green -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		// ALPHANUM-SUSEL (no vol)
		callnum = "SUSEL-69048";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("4578538", fldName, "36105046377987 -|- SAL3 -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		// ALPHANUM - MFILM
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1261173", fldName, "001AFX2969 -|- Green -|- Media-Microtext Collection (Lower Level) -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		// ALPHANUM - MCD
		callnum = "MCD Brendel Plays Beethoven's Eroica variations";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("1234673", fldName, "001AFX2969 -|- Green -|- Media-Microtext Collection (Lower Level) -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// multiple items with same call number
		callnum = "PS3557 .O5829 K3 1998";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("3941911", fldName, "36105025373064 -|- Green -|- Bender Room: Non-circulating -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		assertDocHasFieldValue("3941911", fldName, "36105019748495 -|- Green -|- Bender Room: Non-circulating -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// multiple items with same call number due to vol lopping
		callnum = "PR3724.T3 A2 V.12";
		lopped = "PR3724.T3 A2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("111", fldName, "36105003934432 -|- Green -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "PR3724.T3 A2 V.1";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("111", fldName, "36105003934424 -|- Green -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		callnum = "PR3724.T3 A2 V.2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("111", fldName, "36105048104132 -|- Green -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		
		// multiple items with same call number due to mult buildings
		callnum = "PR3724.T3 V2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum);
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("222", fldName, "36105003934432 -|- Green -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
		assertDocHasFieldValue("222", fldName, "36105003934424 -|- SAL -|- Stacks -|- " +
				callnum + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);

		// invalid LC call number
		callnum = "Y 4.G 74/7:G 21/10";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, "LC", "4823592");
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, !isSerial);
		assertDocHasFieldValue("4823592", fldName, "36105063104488 -|- Crown -|- Basement -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	}
 
	/**
	 * test if item_display field is populated correctly, focused on forward sorting callnums
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     callnum_sort (from lopped call num) -|- 
	 *     callnum_reverse_sort (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
@Test
	public final void testItemDisplayCallnumSort()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");

		// are we getting the shelfkey for the lopped call number?
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = "E184.S75 R47A";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Falconer -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	}

	/**
	 * test if item_display field is populated correctly, focused on backward sorting callnums
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     callnum_sort (from lopped call num) -|- 
	 *     callnum_reverse_sort (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
@Test
	public final void testItemDisplayCallnumReverseSort()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");

		// are we getting the reverse shelfkey for the lopped call number?
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = "E184.S75 R47A";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Falconer -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	}

	/**
	 * test if item_display field is populated correctly, focused on full call numbers
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     callnum_sort (from lopped call num) -|- 
	 *     callnum_reverse_sort (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
@Test
	public final void testItemDisplayFullCallnum()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");
		
		// are we getting the full call number as expected
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Falconer -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	}

	/**
	 * test if item_display field is populated correctly, focused on sorting call numbers for show view
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     callnum_sort (from lopped call num) -|- 
	 *     callnum_reverse_sort (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
@Test
	public final void testItemDisplayCallnumVolumeSort()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		createIxInitVars("buildingTests.mrc");
		
		// are we getting the volume sortable call number we expect?
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		assertDocHasFieldValue("460947", fldName, "36105007402873 -|- Falconer -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort);
	}


	/**
	 * Assert that multiple copies of an item each have a separate field
	 */
//@Test
	public final void testMultipleCopies() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		String fileName = "multipleCopies.mrc";
		createIxInitVars(fileName);
		mappingTestInit();
	    String testFilePath = testDataParentPath + File.separator + fileName;

	    String callnum = "PR3724.T2";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped);
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, isSerial);
		String rest = " -|- SAL3 -|- Stacks -|- " +
				lopped + sep + shelfkey + sep + reversekey + sep + callnum + sep + volSort;
	    String item1 = "36105003934432" + rest;
	    String item2 = "36105003934424" + rest;

	    solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, item1);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, item2);
	    
	    assertDocHasFieldValue("1", fldName, item1);
	    assertDocHasFieldValue("1", fldName, item2);
	}
}
