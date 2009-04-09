package edu.stanford;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

public class AuthorTests extends BibIndexTest {
	
	private final String testDataFname = "authorTests.mrc";
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}
	

// TODO:  problem with eclipse and encoding for non latin  :-(

	/**
	 * test population of author field
	 */
@Test
	public final void testAuthor()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author";
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldNotMultiValued(fldName);
		assertFieldStored(fldName);
		assertFieldIndexed(fldName);

		// 100a
		assertDocHasFieldValue("345228", fldName, "Bashkov, Vladimir"); 
		// 100ad
		assertDocHasFieldValue("919006", fldName, "Oeftering, Michael, 1872-"); 
		// 100ae  (e not indexed)
		assertDocHasFieldValue("7651581", fldName, "Coutinho, Frederico dos Reys"); 
		// 100aqd 
		assertDocHasFieldValue("690002", fldName, "Wallin, J. E. Wallace (John Edward Wallace), b. 1876");
		// 100aqd 
		assertDocHasFieldValue("1261173", fldName, "Johnson, Samuel, 1649-1703");
		// 100 6a x 2  123456 - non latin - not sure how to express here

		assertSingleResult("345228", fldName, "Bashkov");
		assertSingleResult("919006", fldName, "oeftering");
		assertSingleResult("7651581", fldName, "coutinho");
		assertSingleResult("690002", fldName, "wallace");
		assertSingleResult("1261173", fldName, "johnson");
// TODO:  it indexes the OTHER 100 field ... which is all diacritics
//		assertSingleDocWithValue("123456", fldName, "ibn");
	}


	/**
	 * test population of authorStr field
	 */
@Test
	public final void testAuthorStr()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "authorStr";
		assertStringFieldProperties(fldName);
		assertFieldNotMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
		// string field requires exact match
		assertSingleResult("345228", fldName, "\"Bashkov, Vladimir\"");
		assertSingleResult("919006", fldName, "\"Oeftering, Michael, 1872-\"");
		assertSingleResult("7651581", fldName, "\"Coutinho, Frederico dos Reys\"");
		assertSingleResult("690002", fldName, "\"Wallin, J. E. Wallace (John Edward Wallace), b. 1876\"");
		assertSingleResult("1261173", fldName, "\"Johnson, Samuel, 1649-1703\"");
		
		// TODO: ensure authorStr (author facet) contains both 100 and 700 fields
	}


// author-letter is no longer used ...
	/**
	 * test population of author-letter field
	 *  only used for VuFind browse
	 * is author-letter a badly misnamed field?  it's the whole subfield a, not just the first letter
	 */
	public final void testAuthorLetter()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author-letter";
		assertStringFieldProperties(fldName);
		assertFieldOmitsNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldStored(fldName);
		assertFieldIndexed(fldName);
	
		// 100a
		assertDocHasFieldValue("345228", fldName, "Bashkov, Vladimir"); 
		// 100ad
		assertDocHasFieldValue("919006", fldName, "Oeftering, Michael"); 
		// 100ae  (e not indexed)
		assertDocHasFieldValue("7651581", fldName, "Coutinho, Frederico dos Reys"); 
		// 100aqd 
		assertDocHasFieldValue("690002", fldName, "Wallin, J. E. Wallace");
		// 100aqd 
		assertDocHasFieldValue("1261173", fldName, "Johnson, Samuel");
		// 100 6a x 2  123456 - non latin
	
		assertSingleResult("345228", fldName, "\"Bashkov, Vladimir\"");
		assertSingleResult("919006", fldName, "\"Oeftering, Michael\"");
		assertSingleResult("7651581", fldName, "\"Coutinho, Frederico dos Reys\"");
		assertSingleResult("690002", fldName, "\"Wallin, J. E. Wallace\"");
		assertSingleResult("1261173", fldName, "\"Johnson, Samuel\"");
	}

	/**
	 * test population of author2 field
	 */
@Test
	public final void testAuthor2()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author2";
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldStored(fldName);
		assertFieldIndexed(fldName);
	
		// 700a
		assertDocHasFieldValue("4428936", fldName, "Zagarrio, Vito."); 
		// 700 6a
		assertDocHasFieldValue("6808639", fldName, "Chen, Junhong."); 
		// 700 a4
		assertDocHasFieldValue("5666387", fldName, "Huttunen, Matti."); 
		// 700aqd
		assertDocHasFieldValue("6280316", fldName, "Cowles, Calvin D. (Calvin Duvall), 1849-"); 
		// 700adt
		assertDocHasFieldValue("1261173", fldName, "Johnson, Samuel, 1649-1703."); 
		// 700a x 3
		assertDocHasFieldValue("4578538", fldName, "Hewitt, H. S.");
		assertDocHasFieldValue("4578538", fldName, "Kamen, E.");
		assertDocHasFieldValue("4578538", fldName, "Mikulsky, J.");
		// 710a
		assertDocHasNoField("115472", fldName);
		// 110a
		assertDocHasNoField("666", fldName);
	
		assertSingleResult("4428936", fldName, "vito");
		assertSingleResult("6808639", fldName, "junhong");
		assertSingleResult("5666387", fldName, "matti");
		assertSingleResult("6280316", fldName, "cowles");
		assertSingleResult("1261173", fldName, "johnson");
		assertSingleResult("4578538", fldName, "hewitt");
		assertSingleResult("4578538", fldName, "kamen");
		assertSingleResult("4578538", fldName, "mikulsky");
		assertZeroResults(fldName, "Commodities");
		assertZeroResults(fldName, "\"New York Public Library\"");
	}



	/**
	 * Test population of author2Str field
	 */
@Test
	public final void testAuthor2Str() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author2Str";
		// assert field has correct properties
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);

		// string field requires exact match
		assertSingleResult("4428936", fldName, "\"Zagarrio, Vito.\"");
		assertSingleResult("6808639", fldName, "\"Chen, Junhong.\"");
		assertSingleResult("5666387", fldName, "\"Huttunen, Matti.\"");
		assertSingleResult("6280316", fldName, "\"Cowles, Calvin D. (Calvin Duvall), 1849-\"");
		assertSingleResult("1261173", fldName, "\"Johnson, Samuel, 1649-1703.\"");
		assertSingleResult("4578538", fldName, "\"Hewitt, H. S.\"");
		assertSingleResult("4578538", fldName, "\"Kamen, E.\"");
		assertSingleResult("4578538", fldName, "\"Mikulsky, J.\"");
		assertZeroResults(fldName, "\"Landell Mills Commodities Studies Limited\"");
		assertZeroResults(fldName, "\"New York Public Library\"");
	}


	/**
	 * Test that author field isn't stemmed
	 */
@Test
	public final void testAuthorNotStemmed() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author";
	
		assertSingleResult("919006", fldName, "oeftering");
		assertZeroResults(fldName, "oefter");
		assertSingleResult("100240", fldName, "foos");
		assertZeroResults(fldName, "foo");
		assertSingleResult("690002", fldName, "wallace");
		assertZeroResults(fldName, "wallac");
	}


	/**
	 * Test that author2 field isn't stemmed
	 */
@Test
	public final void testAuthor2NotStemmed() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author2";
	
		assertSingleResult("6280316", fldName, "cowles");
		assertZeroResults(fldName, "cowl");
		assertSingleResult("4578538", fldName, "mikulsky");
		assertZeroResults(fldName, "mikulski");
	}


	/**
	 * Test facet value for personal name field, especially removal of trailing 
	 * punctuation
	 */
@Test
	public final void testPersonalNameFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_pers_name_facet";
		assertStringFieldProperties(fldName);
		assertFieldOmitsNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);

		// 100
		// trailing period that isn't an initial should be removed
		assertSingleResult("345228", fldName, "\"Bashkov, Vladimir\"");
		assertZeroResults(fldName, "\"Bashkov, Vladimir.\"");
		assertSingleResult("690002", fldName, "\"Wallin, J. E. Wallace (John Edward Wallace), b. 1876\"");
		assertZeroResults(fldName, "\"Wallin, J. E. Wallace (John Edward Wallace), b. 1876.\"");
		// this trailing period should be left in
		assertSingleResult("8634", fldName, "\"Sallust, 86-34 B.C.\"");
		assertZeroResults(fldName, "\"Sallust, 86-34 B.C\"");
		// trailing hyphen should be left in
		assertSingleResult("919006", fldName, "\"Oeftering, Michael, 1872-\"");
		assertZeroResults(fldName, "\"Oeftering, Michael, 1872\"");
		// trailing comma should be removed
		assertSingleResult("7651581", fldName, "\"Coutinho, Frederico dos Reys\"");
		assertZeroResults(fldName, "\"Coutinho, Frederico dos Reys,\"");
		// 700
		// remove trailing period
		assertSingleResult("4428936", fldName, "\"Zagarrio, Vito\"");
		assertZeroResults(fldName, "\"Zagarrio, Vito.\"");
		// jackpot: comma, period, hyphen
		assertSingleResult("700friedman", fldName, "\"Friedman, Eli A., 1933-\"");
		assertZeroResults(fldName, "\"Friedman, Eli A., 1933-,.\"");
		// two 700s keep one trailing period, remove one
		assertSingleResult("harrypotter", fldName, "\"Heyman, David\"");
		assertSingleResult("harrypotter", fldName, "\"Rowling, J. K.\"");
		assertZeroResults(fldName, "\"Heyman, David.\"");
		assertZeroResults(fldName, "\"Rowling, J. K\"");
		// 100 and 700
		assertSingleResult("700sayers", fldName, "\"Whimsey, Peter\"");
		assertSingleResult("700sayers", fldName, "\"Sayers, Dorothy L. (Dorothy Leigh), 1893-1957\"");
		assertZeroResults(fldName, "\"Whimsey, Peter,\"");
		assertZeroResults(fldName, "\"Sayers, Dorothy L. (Dorothy Leigh), 1893-1957.\"");
		
		// no 800
		assertZeroResults(fldName, "\"Darnell, Jack\"");
	}


	/**
	 * Test facet value for corporate name field, especially removal of trailing 
	 * punctuation
	 */
@Test
	public final void testCorporateNameFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_corp_name_facet";
		assertStringFieldProperties(fldName);
		assertFieldOmitsNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
		// 110 - trailing period to remove
		assertSingleResult("110foo", fldName, "\"SAFE Association (U.S.). Symposium\"");
		assertZeroResults(fldName, "\"SAFE Association (U.S.). Symposium.\"");
		assertSingleResult("NYPL", fldName, "\"New York Public Library\"");
		assertZeroResults(fldName, "\"New York Public Library.\"");
    	// 710 - trailing period to leave in
		assertSingleResult("6280316", fldName, "\"Julius Bien & Co.\"");
		assertZeroResults(fldName, "\"Julius Bien & Co\"");
		assertSingleResult("57136914", fldName, "\"NetLibrary, Inc.\"");
		assertZeroResults(fldName, "\"NetLibrary, Inc\"");
    	// 710 - last char paren
		assertSingleResult("987666", fldName, "\"(this was a value in a non-latin script)\"");
		assertZeroResults(fldName, "\"(this was a value in a non-latin script\"");
		assertSingleResult("710corpname", fldName, "\"Warner Bros. Pictures (1969- )\"");
		assertZeroResults(fldName, "\"Warner Bros. Pictures (1969- \"");
		assertZeroResults(fldName, "\"Warner Bros. Pictures (1969-\"");
		// 710 - leading space
		assertSingleResult("710corpname", fldName, "\"Heyday Films\"");
		assertZeroResults(fldName, "\" Heyday Films.\"");
		assertZeroResults(fldName, "\"Heyday Films.\"");
		
		// 110 and 710
		assertSingleResult("110710corpname", fldName, "\"Thelma\"");
		assertZeroResults(fldName, "\"Thelma.\"");
		assertSingleResult("110710corpname", fldName, "\"Roaring Woman, Louise. 2000-2001\"");
		assertZeroResults(fldName, "\"Roaring Woman, Louise\"");
		assertZeroResults(fldName, "\"Roaring Woman, Louise. 2000-2001.\"");

		// 810 not included
		assertZeroResults(fldName, "\"American Academy in Rome\"");
	}

	/**
	 * Test facet value for meeting name field, especially removal of trailing 
	 * punctuation
	 */
@Test
	public final void testMeetingNameFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_mtg_name_facet";
		assertStringFieldProperties(fldName);
		assertFieldOmitsNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
		// 111
		assertSingleResult("111faim", fldName, "\"FAIM (Forum)\"");
		assertZeroResults(fldName, "\"FAIM (Forum).\"");
		assertZeroResults(fldName, "\"FAIM (Forum\"");
		// 111 sub a n d c  - last char paren
		assertSingleResult("5666387", fldName, "\"International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)\"");
		assertZeroResults(fldName, "\"International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland\"");
		
		// 711
		assertSingleResult("711", fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria)\"");
		assertZeroResults(fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria\"");
		
		// 811 not included
		assertZeroResults(fldName, "\"Delaware Symposium on Language Studies\"");
	}


	/**
	 * Test combined author facet (contains personal name, corporate name and 
	 *  meeting name facet values).  Should have removal of trailing punctuation
	 */
@Test
	public final void testAuthorCombinedFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_combined_facet";
		assertStringFieldProperties(fldName);
		assertFieldOmitsNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);

		// 100 and 700
		assertSingleResult("700sayers", fldName, "\"Whimsey, Peter\"");
		assertSingleResult("700sayers", fldName, "\"Sayers, Dorothy L. (Dorothy Leigh), 1893-1957\"");
		assertZeroResults(fldName, "\"Whimsey, Peter,\"");
		assertZeroResults(fldName, "\"Sayers, Dorothy L. (Dorothy Leigh), 1893-1957.\"");
		// 110 and 710
		assertSingleResult("110710corpname", fldName, "\"Thelma\"");
		assertZeroResults(fldName, "\"Thelma.\"");
		assertSingleResult("110710corpname", fldName, "\"Roaring Woman, Louise. 2000-2001\"");
		assertZeroResults(fldName, "\"Roaring Woman, Louise\"");
		assertZeroResults(fldName, "\"Roaring Woman, Louise. 2000-2001.\"");
		// 111
		assertSingleResult("111faim", fldName, "\"FAIM (Forum)\"");
		assertZeroResults(fldName, "\"FAIM (Forum).\"");
		// 711
		assertSingleResult("711", fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria)\"");
		assertZeroResults(fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria\"");
	}

	/**
	 * Test personal name search field
	 */
@Test
	public final void testPersonalNameSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_pers_name_search";
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
		// 100
		assertSingleResult("690002", fldName, "wallace");
		assertSingleResult("690002", fldName, "Wallace");
		// assert not stemmed
		assertZeroResults(fldName, "wallac");
		assertSingleResult("919006", fldName, "oeftering");
		assertSingleResult("919006", fldName, "Oeftering");
		// assert not stemmed
		assertZeroResults(fldName, "oefter");
		
		// 700
		assertSingleResult("6280316", fldName, "cowles");
		assertSingleResult("6280316", fldName, "Cowles");
		// assert not stemmed
		assertZeroResults(fldName, "cowl");

		// 100 and 700
		assertSingleResult("700sayers", fldName, "whimsey");
		assertSingleResult("700sayers", fldName, "sayers");
		// assert not stemmed
		assertZeroResults(fldName, "sayer");

		// 800
		assertSingleResult("800", fldName, "darnell");
	}
	
	
	/**
	 * Test corporate name search field
	 */
@Test
	public final void testCorporateNameSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_corp_name_search";
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
		// 110
		assertSingleResult("110710corpname", fldName, "thelma");
		
		// 710
		assertSingleResult("6280316", fldName, "julius");
		assertSingleResult("987666", fldName, "latin");
		
		// 810
		assertSingleResult("810", fldName, "rome");
		
		// not stemmed, and more than one (see 110 thelma above)
		assertSingleResult("110710corpname", fldName, "roaring");
		assertZeroResults(fldName, "roar");
	}
	
	/**
	 * Test meeting name search field
	 */
@Test
	public final void testMeetingNameSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_mtg_name_search";
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
		// 111
		assertSingleResult("111faim", fldName, "Forum");
		assertSingleResult("111faim", fldName, "forum");
		// 111 not sub a
		assertSingleResult("5666387", fldName, "Helsinki");
		
		// 711
		assertSingleResult("711", fldName, "computer");
		
		// 811
		assertSingleResult("811", fldName, "delaware");
		// not stemmed
		assertZeroResults(fldName, "delawar");
	}
	
	
	/**
	 * Test combined author search (contains personal name, corporate name and 
	 *  meeting name search)
	 */
@Test
	public final void testAuthorCombinedSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_combined_search";
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
		// 100
		assertSingleResult("919006", fldName, "oeftering");
		// assert not stemmed
		assertZeroResults(fldName, "oefter");
		
		// 700
		assertSingleResult("6280316", fldName, "cowles");

		// 800
		assertSingleResult("800", fldName, "darnell");

		// 110
		assertSingleResult("110710corpname", fldName, "thelma");
		
		// 710
		assertSingleResult("6280316", fldName, "julius");
		
		// 810
		assertSingleResult("810", fldName, "rome");

		// 111
		assertSingleResult("111faim", fldName, "forum");
		// 111 not sub a
		assertSingleResult("5666387", fldName, "Helsinki");
		
		// 711
		assertSingleResult("711", fldName, "computer");
		
		// 811
		assertSingleResult("811", fldName, "delaware");
		// not stemmed
		assertZeroResults(fldName, "delawar");
	}


	String sortLastPrefixStr = String.valueOf(Character.toChars(Character.MAX_CODE_POINT)) + " ";


	/**
	 * Test that author sort field uses the correct fields in the correct order
	 */
@Test
	public final void testAuthorSortIncludedFields() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
	
		// 100 (then 240) then 245
		assertSingleResult("345228", fldName, "\"Bashkov Vladimir 100a only\""); 
		assertZeroResults(fldName, "\"Bashkov Vladimir\"");  // needs 245
		assertZeroResults(fldName, "\"100a only\"");  // needs 100
		
		// 110 (then 240) then 245
		assertSingleResult("110710corpname", fldName, "\"Thelma facets from 110 and 710\""); 
		assertZeroResults(fldName, "\"Thelma\"");  // needs 245
		assertZeroResults(fldName, "\"Thelma.\"");  // needs 245
		assertZeroResults(fldName, "\"facets from 110 and 710\"");  // needs 110
		
		// 111 (then 240) then 245
		assertSingleResult("111faim", fldName, "\"FAIM Forum mtg name facet from 111 should be FAIM Forum\"");
		assertZeroResults(fldName, "\"FAIM Forum\""); // needs 245
		assertZeroResults(fldName, "\"FAIM (Forum)\""); // needs 245
		assertZeroResults(fldName, "\"FAIM (Forum).\""); // needs 245
		assertZeroResults(fldName, "\"mtg name facet from 111 should be: FAIM Forum\""); // needs 111
		assertZeroResults(fldName, "\"mtg name facet from 111 should be: FAIM (Forum)\""); // needs 111
		assertZeroResults(fldName, "\"FAIM (Forum). mtg name facet from 111 should be: FAIM (Forum)\""); // needs puncuation stripped
		
		// no 100 but 240 (then 245)
		String s240 = "De incertitudine et vanitate scientiarum German ";
		assertZeroResults(fldName, s240);  // needs 245
		assertSingleResult("666", fldName, "\"" + sortLastPrefixStr + s240 + "ZZZZ\"");
		assertZeroResults(fldName, "ZZZZ"); // needs 240

		// 100 and 240
		assertSingleResult("100240", fldName, "\"Hoos Foos Marvin OGravel Balloon Face 100 and 240\""); 
		assertZeroResults(fldName, "\"Hoos Foos 100 and 240\""); 
		assertZeroResults(fldName, "\"Marvin OGravel Balloon Face 100 and 240\""); 

		// no 100 no 240 (then 245)
		assertSingleResult("245only", fldName, "\"" + sortLastPrefixStr + "245 no 100 or 240\""); 
	}


	/**
	 * Test that author sort field ignores non-filing characters in 240 and 245 fields
	 */
	@Test
	public final void testAuthorSortNonFilingChars() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
		
		// NOTE: 100 does not allow non-filing chars
		
		// no 100 but 240 w non-filing
		assertSingleResult("2400", fldName, "\"" + sortLastPrefixStr + "Wacky 240 0 nonfiling\""); 
		
		assertSingleResult("2402", fldName, "\"" + sortLastPrefixStr + "Wacky 240 2 nonfiling\""); 
		assertZeroResults(fldName, "\"A Wacky 240 2 nonfiling\""); 
		
		assertSingleResult("2407", fldName, "\"" + sortLastPrefixStr + "Tacky 240 7 nonfiling\""); 
		assertZeroResults(fldName, "\"A Wacky Tacky 240 7 nonfiling\""); 
		
		// no 100 but 240 (no non-filing), 245 with non-filing
		String s240 = sortLastPrefixStr + "De incertitudine et vanitate scientiarum German ";
		assertSingleResult("575946", fldName, "\"" + s240 + "Ruckzug der biblischen Prophetie von der neueren Geschichte\"");
		assertZeroResults(fldName, "\"" + s240 + "Der Ruckzug der biblischen Prophetie von der neueren Geschichte\"");	
		assertZeroResults(fldName, "\"Ruckzug der biblischen Prophetie von der neueren Geschichte\""); // needs 240
		
		// no 100 no 240, 245 with non-filing
		assertSingleResult("1261174", fldName, "\"" + sortLastPrefixStr + "second part of the Confutation of the Ballancing letter\"");
		assertZeroResults(fldName, "\"The second part of the Confutation of the Ballancing letter\"");

		// no 100, but 240, 245 both with non-filing
		assertSingleResult("892452", fldName, "\"" + sortLastPrefixStr + "Wacky 240 245 nonfiling\""); 
		assertZeroResults(fldName, "\"A Wacky In 240 245 nonfiling\""); 
		assertZeroResults(fldName, "\"Wacky In 240 245 nonfiling\""); 
		assertZeroResults(fldName, "\"A Wacky 240 245 nonfiling\""); 
	}


	/**
	 * Test that author sort field deals properly with numeric subfields
	 */
@Test
	public final void testAuthorSortNumericSubflds() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
	
		// 100 
		assertSingleResult("1006", fldName, "\"Sox on Fox 100 has sub 6\"");
		assertZeroResults(fldName, "\"880\\-01 Sox on Fox 100 has sub 6\"");
		
		// 240
		assertSingleResult("0240", fldName, "\"" + sortLastPrefixStr + "sleep little fishies 240 has sub 0\"");
		assertZeroResults(fldName, "\"(DE-101c)310008891 sleep little fishies 240 has sub 0\"");
		
		// 240 mult numeric subfields
		assertSingleResult("24025", fldName, "\"" + sortLastPrefixStr + "la di dah 240 has sub 2 and 5\"");
		assertZeroResults(fldName, "\"ignore me la di dah NjP 240 has sub 2 and 5\"");
		assertZeroResults(fldName, "\"la di dah NjP 240 has sub 2 and 5\"");
		assertZeroResults(fldName, "\"ignore me la di dah 240 has sub 2 and 5\"");

		// 245
		assertSingleResult("2458", fldName, "\"" + sortLastPrefixStr + "245 has sub 8\"");
		assertZeroResults(fldName, "\"1.5\\a 245 has sub 8\"");	
	}
	
	/**
	 * Test that author sort field ignores punctuation
	 */
@Test
	public final void testAuthorSortPunct() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
	
		assertSingleResult("111", fldName, "\"ind 0 leading quotes in 100\"");
		assertZeroResults(fldName, "\"\"ind 0 leading quotes\" in 100\"");
		assertZeroResults(fldName, "\"ind 0 leading quotes\\\" in 100\"");
		assertSingleResult("333", fldName, "\"" + sortLastPrefixStr + "ind 0 leading hyphens in 240\"");
		assertZeroResults(fldName, "\"--ind 0 leading hyphens in 240\"");
		assertSingleResult("444", fldName, "\"" + sortLastPrefixStr + "ind 0 leading elipsis in 240\"");
		assertZeroResults(fldName, "\"...ind 0 leading elipsis in 240\"");
		assertSingleResult("555", fldName, "\"ind 0 leading quote elipsis in 100\"");
		assertZeroResults(fldName, "\"\\\"...ind 0 leading quote elipsis in 100\"");
		assertSingleResult("777", fldName, "\"" + sortLastPrefixStr + "ind 4 leading quote elipsis in 240\"");
		assertZeroResults(fldName, "\"\\\"...ind 4 leading quote elipsis in 240\"");
		assertSingleResult("888", fldName, "\"interspersed punctuation here\"");
		assertZeroResults(fldName, "\"interspersed *(punctua@#$@#$tion \"here--");
		assertZeroResults(fldName, "\"Boo! interspersed *(punctua@#$@#$tion \"here--");
		assertSingleResult("999", fldName, "\"everything in 100\"");
		// lucene special chars:  + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		assertZeroResults(fldName, "\"every!\\\"#$%\\&'\\(\\)\\*\\+,\\-./\\:;<=>\\?@\\[\\\\\\]\\^_`\\{|\\}\\~thing in 100\"");
	}


//	/**
//	 * Test that author sort order is correct
//	 */
//@Test
//	public final void testAuthorSortOrder() 
//		throws ParserConfigurationException, IOException, SAXException
//	{
//		// list of doc ids in correct author sort order
//		List<String> expectedOrderList = new ArrayList<String>(10);
//		expectedOrderList.add("345228");  // Bashkov Vladimir 100a only
//		expectedOrderList.add("999");  // everything in 100
//		expectedOrderList.add("111faim");  // FAIM Forum mtg name facet from 111 should be FAIM Forum
//		expectedOrderList.add("100240");  // Hoos Foos Marvin OGravel Balloon Face 100 and 240
//		expectedOrderList.add("555");  // ind 0 leading quote elipsis in 100
//		expectedOrderList.add("111");  // ind 0 leading quotes in 100
//		expectedOrderList.add("888");  // interspersed punctuation here
//		expectedOrderList.add("1006");  // Sox on Fox 100 has sub 6
//		expectedOrderList.add("110710corpname");  // Thelma facets from 110 and 710
//// 100 / 110 / 111 is missing;  sort last, THEN sort by title		
//		expectedOrderList.add("2458");  // 245 has sub 8
//		expectedOrderList.add("245only");  // 245 no 100 or 240
//		expectedOrderList.add("575946");  // De incertitudine et vanitate scientiarum German Ruckzug der biblischen Prophetie von der neueren Geschichte
//		expectedOrderList.add("666");  // De incertitudine et vanitate scientiarum German ZZZZ
//		expectedOrderList.add("444");  // ind 0 leading elipsis in 240
//		expectedOrderList.add("333");  // ind 0 leading hyphens in 240
//		expectedOrderList.add("777");  // ind 4 leading quote elipsis in 240
//		expectedOrderList.add("24025");  // la di dah 240 has sub 2 and 5
//		expectedOrderList.add("1261174");  // second part of the Confutation of the Ballancing letter
//		expectedOrderList.add("0240");  // sleep little fishies 240 has sub 0
//		expectedOrderList.add("2407");  // Tacky 240 7 nonfiling
//		expectedOrderList.add("2400");  // Wacky 240 0 nonfiling
//		expectedOrderList.add("2402");  // Wacky 240 2 nonfiling
//		expectedOrderList.add("892452");  // Wacky 240 245 nonfiling
//		
//		// get search results sorted by author_sort field
//		List<Document> results = getSortedDocs("collection", "Catalog", "author_sort");
//		
//		// we know we have documents that are not in the expected order list
//		int expDocIx = 0;
//		for (Document doc : results) {
//			if (expDocIx < expectedOrderList.size() - 1) {
//				// we haven't found all docs in the expected list yet
//				Field f = doc.getField("id");  // author_sort isn't stored
//				if (f != null) {
//					String docId = f.stringValue();
//					if (docId.equals(expectedOrderList.get(expDocIx + 1))) {
//						
//						expDocIx++;
//					}
//					
//					
//				}
//			}
//			else break;  // we found all the documents in the expected order list
//		}
//		
//		if (expDocIx != expectedOrderList.size() - 1) {
//			String lastCorrDocId = expectedOrderList.get(expDocIx);
//			fail("Author Sort Order is incorrect.  Last correct document was " + lastCorrDocId);
//		}
//	}


}
