package edu.stanford;


import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 *
 */
public class DisplayFieldTests extends BibIndexTest {
	
	private final String testDataFname = "displayFieldsTests.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}


	private final void assertDisplayOnlyFldProperties(String fldName) 
			throws ParserConfigurationException, IOException, SAXException {
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
	}


	/**
	 * Test creator_display field 
	 */
@Test
	public final void testCreatorDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "creator_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("1001", fldName, "Seuss, Dr.", sis); 
		assertDocHasFieldValue("1002", fldName, "Fowler, T. M. (Thaddeus Mortimer) 1842-1922.", sis); 
		assertDocHasFieldValue("1003", fldName, "Bach, Johann Sebastian.", sis); 
	}

	/**
	 * Test corp_author_display field 
	 */
@Test
	public final void testCorpAuthorDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "corp_author_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("110", fldName, "United States. Congress (97th, 2nd session : 1982). House.", sis); 
	}

	/**
	 * Test meeting_display field 
	 */
@Test
	public final void testMeetingDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "meeting_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("111", fldName, "International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)", sis); 
	}

	/**
	 * Test uniform_title_display field 
	 *   as of 2008-12-10  only uses 130, not 240 (to mirror title_sort field)
	 */
@Test
	public final void testUniformTitleDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "uniform_title_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasNoFieldValue("2401", fldName, "Variations, piano, 4 hands, K. 501, G major", sis); 
		assertDocHasNoFieldValue("2402", fldName, "Treaties, etc. Poland, 1948 Mar. 2. Protocols, etc., 1951 Mar. 6", sis); 
		assertDocHasFieldValue("130", fldName, "Bible. O.T. Five Scrolls. Hebrew. Biblioteca apostolica vaticana. Manuscript. Urbiniti Hebraicus 1. 1980.", sis); 
		assertDocHasFieldValue("11332244", fldName, "Bodkin Van Horn", sis); 
	}

	/**
	 * Test properties of uniform_title_short field - which is no more!
	 */
@Test
	public final void testUniformTitleShort() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "uniform_title_short_display";
		assertFieldNotPresent(fldName, sis.getReader());
/*
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2401", fldName, "Variations,", sis); 
		assertDocHasFieldValue("2402", fldName, "Treaties, etc.", sis); 
*/
	}

	/**
	 * Test brief_title_display field - which is also used for sorting ...
	 */
@Test
	public final void testBriefTitleDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "brief_title_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource]", sis); 
		// trailing slash removed
		assertDocHasNoFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] /", sis); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema", sis); 
		assertDocHasNoFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema /", sis); 
		assertDocHasFieldValue("2453", fldName, "Proceedings", sis); 
		assertDocHasNoFieldValue("2453", fldName, "Proceedings /", sis); 
	}

	/**
	 * Test full_title_display field 
	 */
@Test
	public final void testFullTitleDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "full_title_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] / Laverne Galeener-Moore.", sis); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema / Spyrou Gkrintzou.", sis); 
		assertDocHasFieldValue("2453", fldName, "Proceedings / ...", sis); 
	}

	/**
	 * Test variant_title_display field 
	 */
@Test
	public final void testVariantTitleDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "variant_title_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2461", fldName, "Latino Institute research digest", sis); 
		assertDocHasFieldValue("2462", fldName, "At head of title: Science and public affairs Jan. 1970-Apr. 1974", sis); 
	}

	/**
	 * Test edition field, which is a display only field
	 */
@Test
	public final void testEdition() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "edition";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2501", fldName, "1. ed.", sis); 
		assertDocHasFieldValue("2502", fldName, "3rd draft / edited by Paul Watson.", sis); 
	}

	/**
	 * Test publication_display field 
	 */
@Test
	public final void testPublicationDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "publication_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2601", fldName, "Paris : Impr. Vincent, 1798 [i.e. Bruxelles : Moens, 1883]", sis); 
	}

	/**
	 * Test physical field, which is NOT a display only field
	 */
@Test
	public final void testPhysical() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "physical";
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("3001", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.", sis); 
		assertDocHasFieldValue("3002", fldName, "1 box 2 x 4 x 3 1/2 ft.", sis); 
		assertDocHasFieldValue("3003", fldName, "17 boxes (7 linear ft.)", sis); 
		assertDocHasFieldValue("3004", fldName, "1 page ; 108 cm. x 34.5 cm.", sis); 
		assertDocHasFieldValue("3005", fldName, "65 prints : relief process ; 29 x 22 cm.", sis); 
		assertDocHasFieldValue("3005", fldName, "8 albums (550 photoprints) ; 51 x 46 cm. or smaller.", sis); 
	}

	/**
	 * Test series_title_display field 
	 */
@Test
	public final void testSeriesTitleDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "series_title_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("4401", fldName, "This American life", sis); 
		assertDocHasFieldValue("4402", fldName, "The Rare book tapes. Series 1 ; 5", sis); 
		assertDocHasFieldValue("4403", fldName, "Janua linguarum. Series maior, 100", sis); 
	}

	/**
	 * Test series_display field 
	 */
@Test
	public final void testSeriesDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "series_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("4901", fldName, "Education for living series.", sis); 
		assertDocHasFieldValue("4902", fldName, "Policy series / CES ; 1", sis); 
		assertDocHasFieldValue("4903", fldName, "Department of State publication ; 7846. Department and Foreign Service series ; 128", sis); 
		assertDocHasFieldValue("4904", fldName, "Memoire du BRGM, no 123", sis); 
		assertDocHasFieldValue("4905", fldName, "Annual census of manufactures = Recensement des manufactures,", sis); 
		assertDocHasFieldValue("4906", fldName, "Bulletin / Engineering Experiment Station ; no. 50", sis); 
		assertDocHasFieldValue("4907", fldName, "first 490 a first 490 v", sis); 
		assertDocHasFieldValue("4907", fldName, "second 490 a only", sis); 
		assertDocHasFieldValue("4907", fldName, "third 490 a third 490 v", sis); 
	}

	/**
	 * Test contributor_display field 
	 */
@Test
	public final void testContributorDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "contributor_display";
		assertDisplayOnlyFldProperties(fldName);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("711", fldName, "Kat, Bucky, 1995-2008", sis); 
		assertDocHasFieldValue("711", fldName, "Rees, Graham L.", sis); 
		assertDocHasFieldValue("711", fldName, "Frog, Kermit, 1960-", sis); 
		assertDocHasFieldValue("722", fldName, "Nypsus, Marcus Iunius. 1993.", sis); 
		assertDocHasFieldValue("733", fldName, "Mendelssohn-Bartholdy, Felix, 1809-1847.", sis); 
		assertDocHasFieldValue("733", fldName, "Rumpole, Horace, 1954-1998", sis); 
	}

}
