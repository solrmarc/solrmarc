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
public class DisplayFieldTests extends AbstractStanfordVufindTest {
	
	private final String testDataFname = "displayFieldsTests.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}


	private final void assertDisplayOnlyFldProperties(String fldName) 
			throws ParserConfigurationException, IOException, SAXException {
		assertStringFieldProperties(fldName);
		assertFieldNotIndexed(fldName);
		assertFieldStored(fldName);
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
		assertFieldNotMultiValued(fldName);
		
		assertDocHasFieldValue("1001", fldName, "Seuss, Dr."); 
		assertDocHasFieldValue("1002", fldName, "Fowler, T. M. (Thaddeus Mortimer) 1842-1922."); 
		assertDocHasFieldValue("1003", fldName, "Bach, Johann Sebastian."); 
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
		assertFieldNotMultiValued(fldName);
		
		assertDocHasFieldValue("110", fldName, "United States. Congress (97th, 2nd session : 1982). House."); 
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
		assertFieldNotMultiValued(fldName);
		
		assertDocHasFieldValue("111", fldName, "International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)"); 
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
		assertFieldNotMultiValued(fldName);
		
		assertDocHasNoFieldValue("2401", fldName, "Variations, piano, 4 hands, K. 501, G major"); 
		assertDocHasNoFieldValue("2402", fldName, "Treaties, etc. Poland, 1948 Mar. 2. Protocols, etc., 1951 Mar. 6"); 
		assertDocHasFieldValue("130", fldName, "Bible. O.T. Five Scrolls. Hebrew. Biblioteca apostolica vaticana. Manuscript. Urbiniti Hebraicus 1. 1980."); 
		assertDocHasFieldValue("11332244", fldName, "Bodkin Van Horn"); 
	}

	/**
	 * Test properties of uniform_title_short field - which is no more!
	 */
@Test
	public final void testUniformTitleShort() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "uniform_title_short_display";
//		assertFieldNotPresent(fldName.getReader());
/*
		assertDisplayOnlyFldProperties(fldName);
		assertFieldNotMultiValued(fldName);
		
		assertDocHasFieldValue("2401", fldName, "Variations,"); 
		assertDocHasFieldValue("2402", fldName, "Treaties, etc."); 
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
		assertFieldNotMultiValued(fldName);
		
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource]"); 
		// trailing slash removed
		assertDocHasNoFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] /"); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema"); 
		assertDocHasNoFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema /"); 
		assertDocHasFieldValue("2453", fldName, "Proceedings"); 
		assertDocHasNoFieldValue("2453", fldName, "Proceedings /"); 
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
		assertFieldNotMultiValued(fldName);
		
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] / Laverne Galeener-Moore."); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema / Spyrou Gkrintzou."); 
		assertDocHasFieldValue("2453", fldName, "Proceedings / ..."); 
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
		assertFieldMultiValued(fldName);
		
		assertDocHasFieldValue("2461", fldName, "Latino Institute research digest"); 
		assertDocHasFieldValue("2462", fldName, "At head of title: Science and public affairs Jan. 1970-Apr. 1974"); 
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
		assertFieldNotMultiValued(fldName);
		
		assertDocHasFieldValue("2501", fldName, "1. ed."); 
		assertDocHasFieldValue("2502", fldName, "3rd draft / edited by Paul Watson."); 
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
		assertFieldMultiValued(fldName);
		
		assertDocHasFieldValue("2601", fldName, "Paris : Impr. Vincent, 1798 [i.e. Bruxelles : Moens, 1883]"); 
	}

	/**
	 * Test physical field, which is NOT a display only field
	 */
@Test
	public final void testPhysical() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "physical";
		assertStringFieldProperties(fldName);
		assertFieldIndexed(fldName);
		assertFieldStored(fldName);
		assertFieldMultiValued(fldName);
		
		assertDocHasFieldValue("3001", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in."); 
		assertDocHasFieldValue("3002", fldName, "1 box 2 x 4 x 3 1/2 ft."); 
		assertDocHasFieldValue("3003", fldName, "17 boxes (7 linear ft.)"); 
		assertDocHasFieldValue("3004", fldName, "1 page ; 108 cm. x 34.5 cm."); 
		assertDocHasFieldValue("3005", fldName, "65 prints : relief process ; 29 x 22 cm."); 
		assertDocHasFieldValue("3005", fldName, "8 albums (550 photoprints) ; 51 x 46 cm. or smaller."); 
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
		assertFieldMultiValued(fldName);
		
		assertDocHasFieldValue("4401", fldName, "This American life"); 
		assertDocHasFieldValue("4402", fldName, "The Rare book tapes. Series 1 ; 5"); 
		assertDocHasFieldValue("4403", fldName, "Janua linguarum. Series maior, 100"); 
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
		assertFieldMultiValued(fldName);
		
		assertDocHasFieldValue("4901", fldName, "Education for living series."); 
		assertDocHasFieldValue("4902", fldName, "Policy series / CES ; 1"); 
		assertDocHasFieldValue("4903", fldName, "Department of State publication ; 7846. Department and Foreign Service series ; 128"); 
		assertDocHasFieldValue("4904", fldName, "Memoire du BRGM, no 123"); 
		assertDocHasFieldValue("4905", fldName, "Annual census of manufactures = Recensement des manufactures,"); 
		assertDocHasFieldValue("4906", fldName, "Bulletin / Engineering Experiment Station ; no. 50"); 
		assertDocHasFieldValue("4907", fldName, "first 490 a first 490 v"); 
		assertDocHasFieldValue("4907", fldName, "second 490 a only"); 
		assertDocHasFieldValue("4907", fldName, "third 490 a third 490 v"); 
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
		assertFieldMultiValued(fldName);
		
		assertDocHasFieldValue("711", fldName, "Kat, Bucky, 1995-2008"); 
		assertDocHasFieldValue("711", fldName, "Rees, Graham L."); 
		assertDocHasFieldValue("711", fldName, "Frog, Kermit, 1960-"); 
		assertDocHasFieldValue("722", fldName, "Nypsus, Marcus Iunius. 1993."); 
		assertDocHasFieldValue("733", fldName, "Mendelssohn-Bartholdy, Felix, 1809-1847."); 
		assertDocHasFieldValue("733", fldName, "Rumpole, Horace, 1954-1998"); 
	}

}
