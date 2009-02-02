package edu.stanford;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
		assertTextFieldProperties(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);

		// 100a
		assertDocHasFieldValue("345228", fldName, "Bashkov, Vladimir", sis); 
		// 100ad
		assertDocHasFieldValue("919006", fldName, "Oeftering, Michael, 1872-", sis); 
		// 100ae  (e not indexed)
		assertDocHasFieldValue("7651581", fldName, "Coutinho, Frederico dos Reys", sis); 
		// 100aqd 
		assertDocHasFieldValue("690002", fldName, "Wallin, J. E. Wallace (John Edward Wallace), b. 1876", sis);
		// 100aqd 
		assertDocHasFieldValue("1261173", fldName, "Johnson, Samuel, 1649-1703", sis);
		// 100 6a x 2  123456 - non latin - not sure how to express here

		assertSingleResult("345228", fldName, "Bashkov", sis);
		assertSingleResult("919006", fldName, "oeftering", sis);
		assertSingleResult("7651581", fldName, "coutinho", sis);
		assertSingleResult("690002", fldName, "wallace", sis);
		assertSingleResult("1261173", fldName, "johnson", sis);
// TODO:  it indexes the OTHER 100 field ... which is all diacritics
//		assertSingleDocWithValue("123456", fldName, "ibn", sis);
	}


	/**
	 * test population of authorStr field
	 */
@Test
	public final void testAuthorStr()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "authorStr";
		assertStringFieldProperties(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// string field requires exact match
		assertSingleResult("345228", fldName, "\"Bashkov, Vladimir\"", sis);
		assertSingleResult("919006", fldName, "\"Oeftering, Michael, 1872-\"", sis);
		assertSingleResult("7651581", fldName, "\"Coutinho, Frederico dos Reys\"", sis);
		assertSingleResult("690002", fldName, "\"Wallin, J. E. Wallace (John Edward Wallace), b. 1876\"", sis);
		assertSingleResult("1261173", fldName, "\"Johnson, Samuel, 1649-1703\"", sis);
		
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// 100a
		assertDocHasFieldValue("345228", fldName, "Bashkov, Vladimir", sis); 
		// 100ad
		assertDocHasFieldValue("919006", fldName, "Oeftering, Michael", sis); 
		// 100ae  (e not indexed)
		assertDocHasFieldValue("7651581", fldName, "Coutinho, Frederico dos Reys", sis); 
		// 100aqd 
		assertDocHasFieldValue("690002", fldName, "Wallin, J. E. Wallace", sis);
		// 100aqd 
		assertDocHasFieldValue("1261173", fldName, "Johnson, Samuel", sis);
		// 100 6a x 2  123456 - non latin
	
		assertSingleResult("345228", fldName, "\"Bashkov, Vladimir\"", sis);
		assertSingleResult("919006", fldName, "\"Oeftering, Michael\"", sis);
		assertSingleResult("7651581", fldName, "\"Coutinho, Frederico dos Reys\"", sis);
		assertSingleResult("690002", fldName, "\"Wallin, J. E. Wallace\"", sis);
		assertSingleResult("1261173", fldName, "\"Johnson, Samuel\"", sis);
	}

	/**
	 * test population of author2 field
	 */
@Test
	public final void testAuthor2()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author2";
		assertTextFieldProperties(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// 700a
		assertDocHasFieldValue("4428936", fldName, "Zagarrio, Vito.", sis); 
		// 700 6a
		assertDocHasFieldValue("6808639", fldName, "Chen, Junhong.", sis); 
		// 700 a4
		assertDocHasFieldValue("5666387", fldName, "Huttunen, Matti.", sis); 
		// 700aqd
		assertDocHasFieldValue("6280316", fldName, "Cowles, Calvin D. (Calvin Duvall), 1849-", sis); 
		// 700adt
		assertDocHasFieldValue("1261173", fldName, "Johnson, Samuel, 1649-1703.", sis); 
		// 700a x 3
		assertDocHasFieldValue("4578538", fldName, "Hewitt, H. S.", sis);
		assertDocHasFieldValue("4578538", fldName, "Kamen, E.", sis);
		assertDocHasFieldValue("4578538", fldName, "Mikulsky, J.", sis);
		// 710a
		assertDocHasNoField("115472", fldName, sis);
		// 110a
		assertDocHasNoField("666", fldName, sis);
	
		assertSingleResult("4428936", fldName, "vito", sis);
		assertSingleResult("6808639", fldName, "junhong", sis);
		assertSingleResult("5666387", fldName, "matti", sis);
		assertSingleResult("6280316", fldName, "cowles", sis);
		assertSingleResult("1261173", fldName, "johnson", sis);
		assertSingleResult("4578538", fldName, "hewitt", sis);
		assertSingleResult("4578538", fldName, "kamen", sis);
		assertSingleResult("4578538", fldName, "mikulsky", sis);
		assertZeroResults(fldName, "Commodities", sis);
		assertZeroResults(fldName, "\"New York Public Library\"", sis);
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);

		// string field requires exact match
		assertSingleResult("4428936", fldName, "\"Zagarrio, Vito.\"", sis);
		assertSingleResult("6808639", fldName, "\"Chen, Junhong.\"", sis);
		assertSingleResult("5666387", fldName, "\"Huttunen, Matti.\"", sis);
		assertSingleResult("6280316", fldName, "\"Cowles, Calvin D. (Calvin Duvall), 1849-\"", sis);
		assertSingleResult("1261173", fldName, "\"Johnson, Samuel, 1649-1703.\"", sis);
		assertSingleResult("4578538", fldName, "\"Hewitt, H. S.\"", sis);
		assertSingleResult("4578538", fldName, "\"Kamen, E.\"", sis);
		assertSingleResult("4578538", fldName, "\"Mikulsky, J.\"", sis);
		assertZeroResults(fldName, "\"Landell Mills Commodities Studies Limited\"", sis);
		assertZeroResults(fldName, "\"New York Public Library\"", sis);
	}


	/**
	 * Test that author field isn't stemmed
	 */
@Test
	public final void testAuthorNotStemmed() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author";
	
		assertSingleResult("919006", fldName, "oeftering", sis);
		assertZeroResults(fldName, "oefter", sis);
		assertSingleResult("100240", fldName, "foos", sis);
		assertZeroResults(fldName, "foo", sis);
		assertSingleResult("690002", fldName, "wallace", sis);
		assertZeroResults(fldName, "wallac", sis);
	}


	/**
	 * Test that author2 field isn't stemmed
	 */
@Test
	public final void testAuthor2NotStemmed() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author2";
	
		assertSingleResult("6280316", fldName, "cowles", sis);
		assertZeroResults(fldName, "cowl", sis);
		assertSingleResult("4578538", fldName, "mikulsky", sis);
		assertZeroResults(fldName, "mikulski", sis);
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);

		// 100
		// trailing period that isn't an initial should be removed
		assertSingleResult("345228", fldName, "\"Bashkov, Vladimir\"", sis);
		assertZeroResults(fldName, "\"Bashkov, Vladimir.\"", sis);
		assertSingleResult("690002", fldName, "\"Wallin, J. E. Wallace (John Edward Wallace), b. 1876\"", sis);
		assertZeroResults(fldName, "\"Wallin, J. E. Wallace (John Edward Wallace), b. 1876.\"", sis);
		// this trailing period should be left in
		assertSingleResult("8634", fldName, "\"Sallust, 86-34 B.C.\"", sis);
		assertZeroResults(fldName, "\"Sallust, 86-34 B.C\"", sis);
		// trailing hyphen should be left in
		assertSingleResult("919006", fldName, "\"Oeftering, Michael, 1872-\"", sis);
		assertZeroResults(fldName, "\"Oeftering, Michael, 1872\"", sis);
		// trailing comma should be removed
		assertSingleResult("7651581", fldName, "\"Coutinho, Frederico dos Reys\"", sis);
		assertZeroResults(fldName, "\"Coutinho, Frederico dos Reys,\"", sis);
		// 700
		// remove trailing period
		assertSingleResult("4428936", fldName, "\"Zagarrio, Vito\"", sis);
		assertZeroResults(fldName, "\"Zagarrio, Vito.\"", sis);
		// jackpot: comma, period, hyphen
		assertSingleResult("700friedman", fldName, "\"Friedman, Eli A., 1933-\"", sis);
		assertZeroResults(fldName, "\"Friedman, Eli A., 1933-,.\"", sis);
		// two 700s keep one trailing period, remove one
		assertSingleResult("harrypotter", fldName, "\"Heyman, David\"", sis);
		assertSingleResult("harrypotter", fldName, "\"Rowling, J. K.\"", sis);
		assertZeroResults(fldName, "\"Heyman, David.\"", sis);
		assertZeroResults(fldName, "\"Rowling, J. K\"", sis);
		// 100 and 700
		assertSingleResult("700sayers", fldName, "\"Whimsey, Peter\"", sis);
		assertSingleResult("700sayers", fldName, "\"Sayers, Dorothy L. (Dorothy Leigh), 1893-1957\"", sis);
		assertZeroResults(fldName, "\"Whimsey, Peter,\"", sis);
		assertZeroResults(fldName, "\"Sayers, Dorothy L. (Dorothy Leigh), 1893-1957.\"", sis);
		
		// no 800
		assertZeroResults(fldName, "\"Darnell, Jack\"", sis);
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// 110 - trailing period to remove
		assertSingleResult("110foo", fldName, "\"SAFE Association (U.S.). Symposium\"", sis);
		assertZeroResults(fldName, "\"SAFE Association (U.S.). Symposium.\"", sis);
		assertSingleResult("NYPL", fldName, "\"New York Public Library\"", sis);
		assertZeroResults(fldName, "\"New York Public Library.\"", sis);
    	// 710 - trailing period to leave in
		assertSingleResult("6280316", fldName, "\"Julius Bien & Co.\"", sis);
		assertZeroResults(fldName, "\"Julius Bien & Co\"", sis);
		assertSingleResult("57136914", fldName, "\"NetLibrary, Inc.\"", sis);
		assertZeroResults(fldName, "\"NetLibrary, Inc\"", sis);
    	// 710 - last char paren
		assertSingleResult("987666", fldName, "\"(this was a value in a non-latin script)\"", sis);
		assertZeroResults(fldName, "\"(this was a value in a non-latin script\"", sis);
		assertSingleResult("710corpname", fldName, "\"Warner Bros. Pictures (1969- )\"", sis);
		assertZeroResults(fldName, "\"Warner Bros. Pictures (1969- \"", sis);
		assertZeroResults(fldName, "\"Warner Bros. Pictures (1969-\"", sis);
		// 710 - leading space
		assertSingleResult("710corpname", fldName, "\"Heyday Films\"", sis);
		assertZeroResults(fldName, "\" Heyday Films.\"", sis);
		assertZeroResults(fldName, "\"Heyday Films.\"", sis);
		
		// 110 and 710
		assertSingleResult("110710corpname", fldName, "\"Thelma\"", sis);
		assertZeroResults(fldName, "\"Thelma.\"", sis);
		assertSingleResult("110710corpname", fldName, "\"Roaring Woman, Louise. 2000-2001\"", sis);
		assertZeroResults(fldName, "\"Roaring Woman, Louise\"", sis);
		assertZeroResults(fldName, "\"Roaring Woman, Louise. 2000-2001.\"", sis);

		// 810 not included
		assertZeroResults(fldName, "\"American Academy in Rome\"", sis);
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// 111
		assertSingleResult("111faim", fldName, "\"FAIM (Forum)\"", sis);
		assertZeroResults(fldName, "\"FAIM (Forum).\"", sis);
		assertZeroResults(fldName, "\"FAIM (Forum\"", sis);
		// 111 sub a n d c  - last char paren
		assertSingleResult("5666387", fldName, "\"International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)\"", sis);
		assertZeroResults(fldName, "\"International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland\"", sis);
		
		// 711
		assertSingleResult("711", fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria)\"", sis);
		assertZeroResults(fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria\"", sis);
		
		// 811 not included
		assertZeroResults(fldName, "\"Delaware Symposium on Language Studies\"", sis);
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
		assertStringFieldProperties(fldName, solrCore);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);

		// 100 and 700
		assertSingleResult("700sayers", fldName, "\"Whimsey, Peter\"", sis);
		assertSingleResult("700sayers", fldName, "\"Sayers, Dorothy L. (Dorothy Leigh), 1893-1957\"", sis);
		assertZeroResults(fldName, "\"Whimsey, Peter,\"", sis);
		assertZeroResults(fldName, "\"Sayers, Dorothy L. (Dorothy Leigh), 1893-1957.\"", sis);
		// 110 and 710
		assertSingleResult("110710corpname", fldName, "\"Thelma\"", sis);
		assertZeroResults(fldName, "\"Thelma.\"", sis);
		assertSingleResult("110710corpname", fldName, "\"Roaring Woman, Louise. 2000-2001\"", sis);
		assertZeroResults(fldName, "\"Roaring Woman, Louise\"", sis);
		assertZeroResults(fldName, "\"Roaring Woman, Louise. 2000-2001.\"", sis);
		// 111
		assertSingleResult("111faim", fldName, "\"FAIM (Forum)\"", sis);
		assertZeroResults(fldName, "\"FAIM (Forum).\"", sis);
		// 711
		assertSingleResult("711", fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria)\"", sis);
		assertZeroResults(fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria\"", sis);
	}

	/**
	 * Test personal name search field
	 */
@Test
	public final void testPersonalNameSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_pers_name_search";
		assertTextFieldProperties(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// 100
		assertSingleResult("690002", fldName, "wallace", sis);
		assertSingleResult("690002", fldName, "Wallace", sis);
		// assert not stemmed
		assertZeroResults(fldName, "wallac", sis);
		assertSingleResult("919006", fldName, "oeftering", sis);
		assertSingleResult("919006", fldName, "Oeftering", sis);
		// assert not stemmed
		assertZeroResults(fldName, "oefter", sis);
		
		// 700
		assertSingleResult("6280316", fldName, "cowles", sis);
		assertSingleResult("6280316", fldName, "Cowles", sis);
		// assert not stemmed
		assertZeroResults(fldName, "cowl", sis);

		// 100 and 700
		assertSingleResult("700sayers", fldName, "whimsey", sis);
		assertSingleResult("700sayers", fldName, "sayers", sis);
		// assert not stemmed
		assertZeroResults(fldName, "sayer", sis);

		// 800
		assertSingleResult("800", fldName, "darnell", sis);
	}
	
	
	/**
	 * Test corporate name search field
	 */
@Test
	public final void testCorporateNameSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_corp_name_search";
		assertTextFieldProperties(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// 110
		assertSingleResult("110710corpname", fldName, "thelma", sis);
		
		// 710
		assertSingleResult("6280316", fldName, "julius", sis);
		assertSingleResult("987666", fldName, "latin", sis);
		
		// 810
		assertSingleResult("810", fldName, "rome", sis);
		
		// not stemmed, and more than one (see 110 thelma above)
		assertSingleResult("110710corpname", fldName, "roaring", sis);
		assertZeroResults(fldName, "roar", sis);
	}
	
	/**
	 * Test meeting name search field
	 */
@Test
	public final void testMeetingNameSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_mtg_name_search";
		assertTextFieldProperties(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// 111
		assertSingleResult("111faim", fldName, "Forum", sis);
		assertSingleResult("111faim", fldName, "forum", sis);
		// 111 not sub a
		assertSingleResult("5666387", fldName, "Helsinki", sis);
		
		// 711
		assertSingleResult("711", fldName, "computer", sis);
		
		// 811
		assertSingleResult("811", fldName, "delaware", sis);
		// not stemmed
		assertZeroResults(fldName, "delawar", sis);
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
		assertTextFieldProperties(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		// 100
		assertSingleResult("919006", fldName, "oeftering", sis);
		// assert not stemmed
		assertZeroResults(fldName, "oefter", sis);
		
		// 700
		assertSingleResult("6280316", fldName, "cowles", sis);

		// 800
		assertSingleResult("800", fldName, "darnell", sis);

		// 110
		assertSingleResult("110710corpname", fldName, "thelma", sis);
		
		// 710
		assertSingleResult("6280316", fldName, "julius", sis);
		
		// 810
		assertSingleResult("810", fldName, "rome", sis);

		// 111
		assertSingleResult("111faim", fldName, "forum", sis);
		// 111 not sub a
		assertSingleResult("5666387", fldName, "Helsinki", sis);
		
		// 711
		assertSingleResult("711", fldName, "computer", sis);
		
		// 811
		assertSingleResult("811", fldName, "delaware", sis);
		// not stemmed
		assertZeroResults(fldName, "delawar", sis);
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
		assertSingleResult("345228", fldName, "\"Bashkov Vladimir 100a only\"", sis); 
		assertZeroResults(fldName, "\"Bashkov Vladimir\"", sis);  // needs 245
		assertZeroResults(fldName, "\"100a only\"", sis);  // needs 100
		
		// 110 (then 240) then 245
		assertSingleResult("110710corpname", fldName, "\"Thelma facets from 110 and 710\"", sis); 
		assertZeroResults(fldName, "\"Thelma\"", sis);  // needs 245
		assertZeroResults(fldName, "\"Thelma.\"", sis);  // needs 245
		assertZeroResults(fldName, "\"facets from 110 and 710\"", sis);  // needs 110
		
		// 111 (then 240) then 245
		assertSingleResult("111faim", fldName, "\"FAIM Forum mtg name facet from 111 should be FAIM Forum\"", sis);
		assertZeroResults(fldName, "\"FAIM Forum\"", sis); // needs 245
		assertZeroResults(fldName, "\"FAIM (Forum)\"", sis); // needs 245
		assertZeroResults(fldName, "\"FAIM (Forum).\"", sis); // needs 245
		assertZeroResults(fldName, "\"mtg name facet from 111 should be: FAIM Forum\"", sis); // needs 111
		assertZeroResults(fldName, "\"mtg name facet from 111 should be: FAIM (Forum)\"", sis); // needs 111
		assertZeroResults(fldName, "\"FAIM (Forum). mtg name facet from 111 should be: FAIM (Forum)\"", sis); // needs puncuation stripped
		
		// no 100 but 240 (then 245)
		String s240 = "De incertitudine et vanitate scientiarum German ";
		assertZeroResults(fldName, s240, sis);  // needs 245
		assertSingleResult("666", fldName, "\"" + sortLastPrefixStr + s240 + "ZZZZ\"", sis);
		assertZeroResults(fldName, "ZZZZ", sis); // needs 240

		// 100 and 240
		assertSingleResult("100240", fldName, "\"Hoos Foos Marvin OGravel Balloon Face 100 and 240\"", sis); 
		assertZeroResults(fldName, "\"Hoos Foos 100 and 240\"", sis); 
		assertZeroResults(fldName, "\"Marvin OGravel Balloon Face 100 and 240\"", sis); 

		// no 100 no 240 (then 245)
		assertSingleResult("245only", fldName, "\"" + sortLastPrefixStr + "245 no 100 or 240\"", sis); 
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
		assertSingleResult("2400", fldName, "\"" + sortLastPrefixStr + "Wacky 240 0 nonfiling\"", sis); 
		
		assertSingleResult("2402", fldName, "\"" + sortLastPrefixStr + "Wacky 240 2 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"A Wacky 240 2 nonfiling\"", sis); 
		
		assertSingleResult("2407", fldName, "\"" + sortLastPrefixStr + "Tacky 240 7 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"A Wacky Tacky 240 7 nonfiling\"", sis); 
		
		// no 100 but 240 (no non-filing), 245 with non-filing
		String s240 = sortLastPrefixStr + "De incertitudine et vanitate scientiarum German ";
		assertSingleResult("575946", fldName, "\"" + s240 + "Ruckzug der biblischen Prophetie von der neueren Geschichte\"", sis);
		assertZeroResults(fldName, "\"" + s240 + "Der Ruckzug der biblischen Prophetie von der neueren Geschichte\"", sis);	
		assertZeroResults(fldName, "\"Ruckzug der biblischen Prophetie von der neueren Geschichte\"", sis); // needs 240
		
		// no 100 no 240, 245 with non-filing
		assertSingleResult("1261174", fldName, "\"" + sortLastPrefixStr + "second part of the Confutation of the Ballancing letter\"", sis);
		assertZeroResults(fldName, "\"The second part of the Confutation of the Ballancing letter\"", sis);

		// no 100, but 240, 245 both with non-filing
		assertSingleResult("892452", fldName, "\"" + sortLastPrefixStr + "Wacky 240 245 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"A Wacky In 240 245 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"Wacky In 240 245 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"A Wacky 240 245 nonfiling\"", sis); 
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
		assertSingleResult("1006", fldName, "\"Sox on Fox 100 has sub 6\"", sis);
		assertZeroResults(fldName, "\"880\\-01 Sox on Fox 100 has sub 6\"", sis);
		
		// 240
		assertSingleResult("0240", fldName, "\"" + sortLastPrefixStr + "sleep little fishies 240 has sub 0\"", sis);
		assertZeroResults(fldName, "\"(DE-101c)310008891 sleep little fishies 240 has sub 0\"", sis);
		
		// 240 mult numeric subfields
		assertSingleResult("24025", fldName, "\"" + sortLastPrefixStr + "la di dah 240 has sub 2 and 5\"", sis);
		assertZeroResults(fldName, "\"ignore me la di dah NjP 240 has sub 2 and 5\"", sis);
		assertZeroResults(fldName, "\"la di dah NjP 240 has sub 2 and 5\"", sis);
		assertZeroResults(fldName, "\"ignore me la di dah 240 has sub 2 and 5\"", sis);

		// 245
		assertSingleResult("2458", fldName, "\"" + sortLastPrefixStr + "245 has sub 8\"", sis);
		assertZeroResults(fldName, "\"1.5\\a 245 has sub 8\"", sis);	
	}
	
	/**
	 * Test that author sort field ignores punctuation
	 */
@Test
	public final void testAuthorSortPunct() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
	
		assertSingleResult("111", fldName, "\"ind 0 leading quotes in 100\"", sis);
		assertZeroResults(fldName, "\"\"ind 0 leading quotes\" in 100\"", sis);
		assertZeroResults(fldName, "\"ind 0 leading quotes\\\" in 100\"", sis);
		assertSingleResult("333", fldName, "\"" + sortLastPrefixStr + "ind 0 leading hyphens in 240\"", sis);
		assertZeroResults(fldName, "\"--ind 0 leading hyphens in 240\"", sis);
		assertSingleResult("444", fldName, "\"" + sortLastPrefixStr + "ind 0 leading elipsis in 240\"", sis);
		assertZeroResults(fldName, "\"...ind 0 leading elipsis in 240\"", sis);
		assertSingleResult("555", fldName, "\"ind 0 leading quote elipsis in 100\"", sis);
		assertZeroResults(fldName, "\"\\\"...ind 0 leading quote elipsis in 100\"", sis);
		assertSingleResult("777", fldName, "\"" + sortLastPrefixStr + "ind 4 leading quote elipsis in 240\"", sis);
		assertZeroResults(fldName, "\"\\\"...ind 4 leading quote elipsis in 240\"", sis);
		assertSingleResult("888", fldName, "\"interspersed punctuation here\"", sis);
		assertZeroResults(fldName, "\"interspersed *(punctua@#$@#$tion \"here--", sis);
		assertZeroResults(fldName, "\"Boo! interspersed *(punctua@#$@#$tion \"here--", sis);
		assertSingleResult("999", fldName, "\"everything in 100\"", sis);
		// lucene special chars:  + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		assertZeroResults(fldName, "\"every!\\\"#$%\\&'\\(\\)\\*\\+,\\-./\\:;<=>\\?@\\[\\\\\\]\\^_`\\{|\\}\\~thing in 100\"", sis);
	}


	/**
	 * Test that author sort order is correct
	 */
@Test
	public final void testAuthorSortOrder() 
		throws ParserConfigurationException, IOException, SAXException
	{
		// list of doc ids in correct author sort order
		List<String> expectedOrderList = new ArrayList<String>(10);
		expectedOrderList.add("345228");  // Bashkov Vladimir 100a only
		expectedOrderList.add("999");  // everything in 100
		expectedOrderList.add("111faim");  // FAIM Forum mtg name facet from 111 should be FAIM Forum
		expectedOrderList.add("100240");  // Hoos Foos Marvin OGravel Balloon Face 100 and 240
		expectedOrderList.add("555");  // ind 0 leading quote elipsis in 100
		expectedOrderList.add("111");  // ind 0 leading quotes in 100
		expectedOrderList.add("888");  // interspersed punctuation here
		expectedOrderList.add("1006");  // Sox on Fox 100 has sub 6
		expectedOrderList.add("110710corpname");  // Thelma facets from 110 and 710
// 100 / 110 / 111 is missing;  sort last, THEN sort by title		
		expectedOrderList.add("2458");  // 245 has sub 8
		expectedOrderList.add("245only");  // 245 no 100 or 240
		expectedOrderList.add("575946");  // De incertitudine et vanitate scientiarum German Ruckzug der biblischen Prophetie von der neueren Geschichte
		expectedOrderList.add("666");  // De incertitudine et vanitate scientiarum German ZZZZ
		expectedOrderList.add("444");  // ind 0 leading elipsis in 240
		expectedOrderList.add("333");  // ind 0 leading hyphens in 240
		expectedOrderList.add("777");  // ind 4 leading quote elipsis in 240
		expectedOrderList.add("24025");  // la di dah 240 has sub 2 and 5
		expectedOrderList.add("1261174");  // second part of the Confutation of the Ballancing letter
		expectedOrderList.add("0240");  // sleep little fishies 240 has sub 0
		expectedOrderList.add("2407");  // Tacky 240 7 nonfiling
		expectedOrderList.add("2400");  // Wacky 240 0 nonfiling
		expectedOrderList.add("2402");  // Wacky 240 2 nonfiling
		expectedOrderList.add("892452");  // Wacky 240 245 nonfiling
		
		// get search results sorted by author_sort field
		List<Document> results = getSortedDocs("collection", "Catalog", "author_sort", sis);
		
		// we know we have documents that are not in the expected order list
		int expDocIx = 0;
		for (Document doc : results) {
			if (expDocIx < expectedOrderList.size() - 1) {
				// we haven't found all docs in the expected list yet
				Field f = doc.getField("id");  // author_sort isn't stored
				if (f != null) {
					String docId = f.stringValue();
					if (docId.equals(expectedOrderList.get(expDocIx + 1))) {
						
						expDocIx++;
					}
					
					
				}
			}
			else break;  // we found all the documents in the expected order list
		}
		
		if (expDocIx != expectedOrderList.size() - 1) {
			String lastCorrDocId = expectedOrderList.get(expDocIx);
			fail("Author Sort Order is incorrect.  Last correct document was " + lastCorrDocId);
		}
	}


}
