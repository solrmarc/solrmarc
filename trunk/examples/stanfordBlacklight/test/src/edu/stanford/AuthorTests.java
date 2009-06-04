package edu.stanford;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University author fields for blacklight index
 * @author Naomi Dushay
 */
public class AuthorTests extends BibIndexTest {
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("authorTests.mrc");
	}
	
// TODO:  problem with eclipse and encoding for non latin  :-(

	/**
	 * Personal name display field tests.
	 */
@Test
	public final void testPersonalNameDisplay() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_person_display";
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);

		// 100a
		// trailing period removed
		assertDocHasFieldValue("345228", fldName, "Bashkov, Vladimir", sis); 
		// 100ad
		// trailing hyphen retained
		assertDocHasFieldValue("919006", fldName, "Oeftering, Michael, 1872-", sis); 
		// 100ae  (e not indexed)
		// trailing comma should be removed
		assertDocHasFieldValue("7651581", fldName, "Coutinho, Frederico dos Reys", sis); 
		// 100aqd 
		// trailing period removed
		assertDocHasFieldValue("690002", fldName, "Wallin, J. E. Wallace (John Edward Wallace), b. 1876", sis);
		// 100aqd 
		assertDocHasFieldValue("1261173", fldName, "Johnson, Samuel, 1649-1703", sis);
		// 'nother sort of trailing period - not removed
		assertDocHasFieldValue("8634", fldName, "Sallust, 86-34 B.C.", sis);
		// 100 with numeric subfield
		assertDocHasFieldValue("1006", fldName, "Sox on Fox", sis);
		// 100 6a x 2  123456 - non latin - not sure how to express here
	}

	/**
	 * Full personal name display field tests.
	 */
@Test
	public final void testFullPersonalNameDisplay() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_person_full_display";
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);

		// 100ae 
		assertDocHasFieldValue("7651581", fldName, "Coutinho, Frederico dos Reys, ed.", sis); 
		
		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		assertDocHasFieldValue("1001", fldName, "Seuss, Dr.", sis); 
		assertDocHasFieldValue("1002", fldName, "Fowler, T. M. (Thaddeus Mortimer) 1842-1922.", sis); 
		assertDocHasFieldValue("1003", fldName, "Bach, Johann Sebastian.", sis); 
	}

	/**
	 * Corporate name display field tests.
	 */
@Test
	public final void testCorpNameDisplay() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_corp_display";
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);
		
		// 110 
		assertDocHasFieldValue("NYPL", fldName, "New York Public Library.", sis); 
		assertDocHasFieldValue("5511738", fldName, "United States. Congress. House. Committee on Agriculture. Subcommittee on Department Operations, Oversight, Nutrition, and Forestry.", sis); 
		assertDocHasFieldValue("4578538", fldName, "Stanford University. Stanford Electronics Laboratories. SEL-69-048.", sis); 

		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		assertDocHasFieldValue("110", fldName, "United States. Congress (97th, 2nd session : 1982). House.", sis); 
	}

	/**
	 * Meeting name display field tests.
	 */
@Test
	public final void testMeetingNameDisplay() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_meeting_display";
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);
		
		// 111a
		assertDocHasFieldValue("111faim", fldName, "FAIM (Forum).", sis);
		// 111 andc
		assertDocHasFieldValue("5666387", fldName, "International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)", sis);
	}

	/**
	 * author_addl_display field - no longer used
	 */
//@Test
	public final void testAuthorAddlDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
	
		String fldName = "author_addl_display";
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("711", fldName, "Kat, Bucky, 1995-2008", sis); 
		assertDocHasFieldValue("711", fldName, "Rees, Graham L.", sis); 
		assertDocHasFieldValue("711", fldName, "Frog, Kermit, 1960-", sis); 
		assertDocHasFieldValue("722", fldName, "Nypsus, Marcus Iunius. 1993.", sis); 
		assertDocHasFieldValue("733", fldName, "Mendelssohn-Bartholdy, Felix, 1809-1847.", sis); 
		assertDocHasFieldValue("733", fldName, "Rumpole, Horace, 1954-1998", sis); 
	}


	/**
	 * Personal name facet field tests, especially removal of trailing 
	 * punctuation
	 */
@Test
	public final void testPersonalNameFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_person_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);

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
		
		assertSingleResult("1261173", fldName, "\"Johnson, Samuel, 1649-1703\"", sis);
	}


	/**
	 * Corporate name facet field tests, especially removal of trailing 
	 * punctuation
	 */
@Test
	public final void testCorporateNameFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_corp_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
	
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
	 * Meeting name facet tests, especially removal of trailing punctuation
	 */
@Test
	public final void testMeetingNameFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_meeting_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
	
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
	 * Other (meeting and corporate, not person) name facet tests, including 
	 *  removal of trailing punctuation
	 */
@Test
	public final void testAuthorOtherFacet()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_other_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
	
		// 110 - trailing period to remove
		assertSingleResult("110foo", fldName, "\"SAFE Association (U.S.). Symposium\"", sis);
		assertZeroResults(fldName, "\"SAFE Association (U.S.). Symposium.\"", sis);
		assertSingleResult("NYPL", fldName, "\"New York Public Library\"", sis);
		assertZeroResults(fldName, "\"New York Public Library.\"", sis);
		// 111
		assertSingleResult("111faim", fldName, "\"FAIM (Forum)\"", sis);
		assertZeroResults(fldName, "\"FAIM (Forum).\"", sis);
		assertZeroResults(fldName, "\"FAIM (Forum\"", sis);
		// 111 sub a n d c  - last char paren
		assertSingleResult("5666387", fldName, "\"International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)\"", sis);
		assertZeroResults(fldName, "\"International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland\"", sis);
		
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
		// 711
		assertSingleResult("711", fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria)\"", sis);
		assertZeroResults(fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria\"", sis);
		
		// 110 and 710
		assertSingleResult("110710corpname", fldName, "\"Thelma\"", sis);
		assertZeroResults(fldName, "\"Thelma.\"", sis);
		assertSingleResult("110710corpname", fldName, "\"Roaring Woman, Louise. 2000-2001\"", sis);
		assertZeroResults(fldName, "\"Roaring Woman, Louise\"", sis);
		assertZeroResults(fldName, "\"Roaring Woman, Louise. 2000-2001.\"", sis);

		// 810 not included
		assertZeroResults(fldName, "\"American Academy in Rome\"", sis);
		// 811 not included
		assertZeroResults(fldName, "\"Delaware Symposium on Language Studies\"", sis);
	}




	/**
	 * Combined author facet (contains personal name, corporate name and 
	 *  meeting name facet values) tests.  Removal of trailing punctuation.
	 */
// no longer in index 2009-05-14
//@Test
	public final void testAuthorCombinedFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_combined_facet";
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);

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
	 * author_1xx_search:  check all search subfields for 100, 110, 111
	 */
@Test
	public final void test1xxSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_1xx_search";
		assertSearchFldOneValProps(fldName, solrCore, sis);
		assertSingleResult("100search", fldName, "100a", sis);
		assertSingleResult("100search", fldName, "100b", sis);
		assertSingleResult("100search", fldName, "100c", sis);
		assertSingleResult("100search", fldName, "100d", sis);
		assertSingleResult("100search", fldName, "100e", sis);
		assertSingleResult("100search", fldName, "100g", sis);
		assertSingleResult("100search", fldName, "100j", sis);
		assertSingleResult("100search", fldName, "100q", sis);
		assertSingleResult("100search", fldName, "100u", sis);

		assertSingleResult("110search", fldName, "110a", sis);
		assertSingleResult("110search", fldName, "110b", sis);
		assertSingleResult("110search", fldName, "110c", sis);
		assertSingleResult("110search", fldName, "110d", sis);
		assertSingleResult("110search", fldName, "110e", sis);
		assertSingleResult("110search", fldName, "110g", sis);
		assertSingleResult("110search", fldName, "110n", sis);
		assertSingleResult("110search", fldName, "110u", sis);

		assertSingleResult("111search", fldName, "111a", sis);
		assertSingleResult("111search", fldName, "111c", sis);
		assertSingleResult("111search", fldName, "111d", sis);
		assertSingleResult("111search", fldName, "111e", sis);
		assertSingleResult("111search", fldName, "111g", sis);
		assertSingleResult("111search", fldName, "111j", sis);
		assertSingleResult("111search", fldName, "111n", sis);
		assertSingleResult("111search", fldName, "111q", sis);
		assertSingleResult("111search", fldName, "111u", sis);
		
		assertZeroResults(fldName, "110f", sis);
		assertZeroResults(fldName, "110k", sis);
		assertZeroResults(fldName, "none", sis);
	}

	/**
	 * vern_author_1xx_search:  check all search subfields for 100, 110, 111
	 */
@Test
	public final void vern1xxSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_author_1xx_search";
		createIxInitVars("vernacularSearchTests.mrc");
		assertSearchFldOneValProps(fldName, solrCore, sis);
		
		assertSingleResult("100VernSearch", fldName, "vern100a", sis);
		assertSingleResult("100VernSearch", fldName, "vern100b", sis);
		assertSingleResult("100VernSearch", fldName, "vern100c", sis);
		assertSingleResult("100VernSearch", fldName, "vern100d", sis);
		assertSingleResult("100VernSearch", fldName, "vern100e", sis);
		assertSingleResult("100VernSearch", fldName, "vern100g", sis);
		assertSingleResult("100VernSearch", fldName, "vern100j", sis);
		assertSingleResult("100VernSearch", fldName, "vern100q", sis);
		assertSingleResult("100VernSearch", fldName, "vern100u", sis);
	
		assertSingleResult("110VernSearch", fldName, "vern110a", sis);
		assertSingleResult("110VernSearch", fldName, "vern110b", sis);
		assertSingleResult("110VernSearch", fldName, "vern110c", sis);
		assertSingleResult("110VernSearch", fldName, "vern110d", sis);
		assertSingleResult("110VernSearch", fldName, "vern110e", sis);
		assertSingleResult("110VernSearch", fldName, "vern110g", sis);
		assertSingleResult("110VernSearch", fldName, "vern110n", sis);
		assertSingleResult("110VernSearch", fldName, "vern110u", sis);
	
		assertSingleResult("111VernSearch", fldName, "vern111a", sis);
		assertSingleResult("111VernSearch", fldName, "vern111c", sis);
		assertSingleResult("111VernSearch", fldName, "vern111d", sis);
		assertSingleResult("111VernSearch", fldName, "vern111e", sis);
		assertSingleResult("111VernSearch", fldName, "vern111g", sis);
		assertSingleResult("111VernSearch", fldName, "vern111j", sis);
		assertSingleResult("111VernSearch", fldName, "vern111n", sis);
		assertSingleResult("111VernSearch", fldName, "vern111q", sis);
		assertSingleResult("111VernSearch", fldName, "vern111u", sis);
	
		assertZeroResults(fldName, "vern110f", sis);
		assertZeroResults(fldName, "vern110k", sis);
		assertZeroResults(fldName, "none", sis);
	}

	/**
	 * author_7xx_search: personal name fields 
	 *  check all search subfields for 700, 720, 796
	 */
@Test
	public final void test7xxPersonSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_7xx_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
		assertSingleResult("7xxPersonSearch", fldName, "700a", sis);
		assertSingleResult("7xxPersonSearch", fldName, "700b", sis);
		assertSingleResult("7xxPersonSearch", fldName, "700c", sis);
		assertSingleResult("7xxPersonSearch", fldName, "700d", sis);
		assertSingleResult("7xxPersonSearch", fldName, "700e", sis);
		assertSingleResult("7xxPersonSearch", fldName, "700g", sis);
		assertSingleResult("7xxPersonSearch", fldName, "700j", sis);
		assertSingleResult("7xxPersonSearch", fldName, "700q", sis);
		assertSingleResult("7xxPersonSearch", fldName, "700u", sis);
			
		assertSingleResult("7xxPersonSearch", fldName, "720a", sis);
		assertSingleResult("7xxPersonSearch", fldName, "720e", sis);

		assertSingleResult("7xxPersonSearch", fldName, "796a", sis);
		assertSingleResult("7xxPersonSearch", fldName, "796b", sis);
		assertSingleResult("7xxPersonSearch", fldName, "796c", sis);
		assertSingleResult("7xxPersonSearch", fldName, "796d", sis);
		assertSingleResult("7xxPersonSearch", fldName, "796e", sis);
		assertSingleResult("7xxPersonSearch", fldName, "796g", sis);
		assertSingleResult("7xxPersonSearch", fldName, "796j", sis);
		assertSingleResult("7xxPersonSearch", fldName, "796q", sis);
		assertSingleResult("7xxPersonSearch", fldName, "796u", sis);

		assertZeroResults(fldName, "none", sis);
	}

	/**
	 * vern_author_7xx_search: personal name fields 
	 *  check all search subfields for 700, 720, 796
	 */
@Test
	public final void vern7xxPersonSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_author_7xx_search";
		createIxInitVars("vernacularSearchTests.mrc");
		assertSearchFldMultValProps(fldName, solrCore, sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700a", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700b", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700c", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700d", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700e", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700q", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700u", sis);
			
		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernPersonSearch");
		// overlap title
		assertSearchResults(fldName, "vern700g", docIds, sis); 
		// used to be in title
		assertSearchResults(fldName, "vern700j", docIds, sis);
	
		assertSingleResult("7xxVernPersonSearch", fldName, "vern720a", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern720e", sis);
	
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796a", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796b", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796c", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796d", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796e", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796q", sis);
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796u", sis);
		
		docIds.remove("7xxLowVernSearch");
		docIds.add("79xVernSearch");
		// overlap title
		assertSearchResults(fldName, "vern796g", docIds, sis); 
		// used to be in title
		assertSearchResults(fldName, "vern796j", docIds, sis);
		
		assertZeroResults(fldName, "none", sis);
	}

	/**
	 * author_7xx_search: corporate name fields 
	 *  check all search subfields for 710, 797
	 */
@Test
	public final void test7xxCorpSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_7xx_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxCorpSearch", fldName, "710a", sis);
		assertSingleResult("7xxCorpSearch", fldName, "710b", sis);
		assertSingleResult("7xxCorpSearch", fldName, "710c", sis);
		assertSingleResult("7xxCorpSearch", fldName, "710d", sis);
		assertSingleResult("7xxCorpSearch", fldName, "710e", sis);
		assertSingleResult("7xxCorpSearch", fldName, "710g", sis);
		assertSingleResult("7xxCorpSearch", fldName, "710n", sis);
		assertSingleResult("7xxCorpSearch", fldName, "710u", sis);

		assertSingleResult("7xxCorpSearch", fldName, "797a", sis);
		assertSingleResult("7xxCorpSearch", fldName, "797b", sis);
		assertSingleResult("7xxCorpSearch", fldName, "797c", sis);
		assertSingleResult("7xxCorpSearch", fldName, "797d", sis);
		assertSingleResult("7xxCorpSearch", fldName, "797e", sis);
		assertSingleResult("7xxCorpSearch", fldName, "797g", sis);
		assertSingleResult("7xxCorpSearch", fldName, "797n", sis);
		assertSingleResult("7xxCorpSearch", fldName, "797u", sis);
		
		assertZeroResults(fldName, "710f", sis);
		assertZeroResults(fldName, "710k", sis);
		assertZeroResults(fldName, "797f", sis);
		assertZeroResults(fldName, "797k", sis);
		assertZeroResults(fldName, "none", sis);
	}

	/**
	 * vern_author_7xx_search: corporate name fields 
	 *  check all search subfields for 710, 797
	 */
@Test
	public final void vern7xxCorpSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_author_7xx_search";
		createIxInitVars("vernacularSearchTests.mrc");
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710a", sis);
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710b", sis);
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710c", sis);
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710e", sis);
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710u", sis);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernCorpSearch");
		// overlap title
		assertSearchResults(fldName, "vern710d", docIds, sis); 
		assertSearchResults(fldName, "vern710g", docIds, sis); 
		assertSearchResults(fldName, "vern710n", docIds, sis);
	
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797a", sis);
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797b", sis);
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797c", sis);
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797e", sis);
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797u", sis);
		
		docIds.remove("7xxLowVernSearch");
		docIds.add("79xVernSearch");
		// overlap title
		assertSearchResults(fldName, "vern797d", docIds, sis); 
		assertSearchResults(fldName, "vern797g", docIds, sis); 
		assertSearchResults(fldName, "vern797n", docIds, sis);
		
		assertZeroResults(fldName, "vern710f", sis);
		assertZeroResults(fldName, "vern710k", sis);
		assertZeroResults(fldName, "vern797f", sis);
		assertZeroResults(fldName, "vern797k", sis);
		assertZeroResults(fldName, "none", sis);
	}


	/**
	 * author_7xx_search: meeting name fields 
	 *  check all search subfields for 711, 798
	 */
@Test
	public final void test7xxMeetingSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_7xx_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxMeetingSearch", fldName, "711a", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "711c", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "711d", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "711e", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "711g", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "711j", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "711n", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "711q", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "711u", sis);
		
		assertSingleResult("7xxMeetingSearch", fldName, "798a", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "798c", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "798d", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "798e", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "798g", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "798j", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "798n", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "798q", sis);
		assertSingleResult("7xxMeetingSearch", fldName, "798u", sis);

		assertZeroResults(fldName, "none", sis);
	}

	/**
	 * vern_author_7xx_search: meeting name fields 
	 *  check all search subfields for 711, 798
	 */
@Test
	public final void vern7xxMeetingSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_author_7xx_search";
		createIxInitVars("vernacularSearchTests.mrc");
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711a", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711c", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711d", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711e", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711j", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711q", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711u", sis);
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernMeetingSearch");
		// overlap title
		assertSearchResults(fldName, "vern711g", docIds, sis); 
		assertSearchResults(fldName, "vern711n", docIds, sis);
	
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798a", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798c", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798d", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798e", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798j", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798q", sis);
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798u", sis);
	
		docIds.remove("7xxLowVernSearch");
		docIds.add("79xVernSearch");
		// overlap title
		assertSearchResults(fldName, "vern798g", docIds, sis); 
		assertSearchResults(fldName, "vern798n", docIds, sis);
		
		assertZeroResults(fldName, "none", sis);
	}

	/**
	 * author_8xx_search:  check all search subfields for 800, 810, 811
	 */
@Test
	public final void test8xxSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_8xx_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
		assertSingleResult("800search", fldName, "800a", sis);
		assertSingleResult("800search", fldName, "800b", sis);
		assertSingleResult("800search", fldName, "800c", sis);
		assertSingleResult("800search", fldName, "800d", sis);
		assertSingleResult("800search", fldName, "800e", sis);
		assertSingleResult("800search", fldName, "800g", sis);
		assertSingleResult("800search", fldName, "800j", sis);
		assertSingleResult("800search", fldName, "800q", sis);
		assertSingleResult("800search", fldName, "800u", sis);
	
		assertSingleResult("810search", fldName, "810a", sis);
		assertSingleResult("810search", fldName, "810b", sis);
		assertSingleResult("810search", fldName, "810c", sis);
		assertSingleResult("810search", fldName, "810d", sis);
		assertSingleResult("810search", fldName, "810e", sis);
		assertSingleResult("810search", fldName, "810g", sis);
		assertSingleResult("810search", fldName, "810n", sis);
		assertSingleResult("810search", fldName, "810u", sis);
	
		assertSingleResult("811search", fldName, "811a", sis);
		assertSingleResult("811search", fldName, "811c", sis);
		assertSingleResult("811search", fldName, "811d", sis);
		assertSingleResult("811search", fldName, "811e", sis);
		assertSingleResult("811search", fldName, "811g", sis);
		assertSingleResult("811search", fldName, "811j", sis);
		assertSingleResult("811search", fldName, "811n", sis);
		assertSingleResult("811search", fldName, "811q", sis);
		assertSingleResult("811search", fldName, "811u", sis);
		
		assertZeroResults(fldName, "810f", sis);
		assertZeroResults(fldName, "810k", sis);
		assertZeroResults(fldName, "none", sis);
	}

	/**
	 * vern_author_8xx_search:  check all search subfields for 800, 810, 811
	 */
@Test
	public final void vern8xxSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_author_8xx_search";
		createIxInitVars("vernacularSearchTests.mrc");
		assertSearchFldMultValProps(fldName, solrCore, sis);
		assertSingleResult("800VernSearch", fldName, "vern800a", sis);
		assertSingleResult("800VernSearch", fldName, "vern800b", sis);
		assertSingleResult("800VernSearch", fldName, "vern800c", sis);
		assertSingleResult("800VernSearch", fldName, "vern800d", sis);
		assertSingleResult("800VernSearch", fldName, "vern800e", sis);
		assertSingleResult("800VernSearch", fldName, "vern800q", sis);
		assertSingleResult("800VernSearch", fldName, "vern800u", sis);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("800VernSearch");
		docIds.add("8xxVernSearch");
		// overlap title
		assertSearchResults(fldName, "vern800g", docIds, sis); 
		// used to be in title
		assertSearchResults(fldName, "vern800j", docIds, sis);
	
		assertSingleResult("810VernSearch", fldName, "vern810a", sis);
		assertSingleResult("810VernSearch", fldName, "vern810b", sis);
		assertSingleResult("810VernSearch", fldName, "vern810c", sis);
		assertSingleResult("810VernSearch", fldName, "vern810e", sis);
		assertSingleResult("810VernSearch", fldName, "vern810u", sis);
	
		docIds.remove("800VernSearch");
		docIds.add("810VernSearch");
		// overlap title
		assertSearchResults(fldName, "vern810d", docIds, sis); 
		assertSearchResults(fldName, "vern810g", docIds, sis); 
		assertSearchResults(fldName, "vern810n", docIds, sis); 
		
		assertSingleResult("811VernSearch", fldName, "vern811a", sis);
		assertSingleResult("811VernSearch", fldName, "vern811c", sis);
		assertSingleResult("811VernSearch", fldName, "vern811d", sis);
		assertSingleResult("811VernSearch", fldName, "vern811e", sis);
		assertSingleResult("811VernSearch", fldName, "vern811j", sis);
		assertSingleResult("811VernSearch", fldName, "vern811q", sis);
		assertSingleResult("811VernSearch", fldName, "vern811u", sis);
	
		docIds.remove("810VernSearch");
		docIds.add("811VernSearch");
		// overlap title
		assertSearchResults(fldName, "vern811g", docIds, sis); 
		assertSearchResults(fldName, "vern811n", docIds, sis); 
		
		
		assertZeroResults(fldName, "vern810f", sis);
		assertZeroResults(fldName, "vern810k", sis);
		assertZeroResults(fldName, "none", sis);
	}

	String sortLastPrefixStr = String.valueOf(Character.toChars(Character.MAX_CODE_POINT)) + " ";


	/**
	 * Author sort field must use the correct fields in the correct order
	 */
@Test
	public final void testAuthorSortIncludedFields() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
	    assertSortFldProps(fldName, solrCore, sis);
	
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
	 * Author sort field must ignore non-filing characters in 240 and 245 fields
	 */
	@Test
	public final void testAuthorSortNonFilingChars() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
	    assertSortFldProps(fldName, solrCore, sis);
		
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
	 * Author sort field must properly cope with numeric subfields
	 */
@Test
	public final void testAuthorSortNumericSubflds() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
	    assertSortFldProps(fldName, solrCore, sis);
	
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
	 * Author sort field must ignore punctuation
	 */
@Test
	public final void testAuthorSortPunct() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "author_sort";
	    assertSortFldProps(fldName, solrCore, sis);
	
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
	 * Author sort order must be correct
	 */
@Test
	public final void testAuthorSortOrder() 
		throws ParserConfigurationException, IOException, SAXException
	{
		// list of doc ids in correct author sort order
		List<String> expectedOrderList = new ArrayList<String>(30);
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
