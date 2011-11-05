package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's title fields
 * @author Naomi Dushay
 */
public class TitleTests extends AbstractStanfordBlacklightTest {
	
	/**
	 * Test title_245a_display field;  trailing punctuation is removed
	 */
@Test
	public final void testTitle245aDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "title_245a_display";
		createIxInitVars("titleTests.mrc");
//		assertDisplayFieldProperties(fldName);
//		assertFieldNotMultiValued(fldName);

		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p"); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p"); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n"); 

		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		// trailing punctuation removed
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives"); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia"); 
	}

	/**
	 * Test title_display field
	 */
@Test
	public final void testTitleDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "title_display";
		createIxInitVars("titleTests.mrc");
//		assertDisplayFieldProperties(fldName);
//		assertFieldNotMultiValued(fldName);

		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p [electronic resource]"); 
		assertDocHasFieldValue("245nNotp", fldName, "245 n but no p Part one."); 
		assertDocHasFieldValue("245pNotn", fldName, "245 p but no n. subfield b Student handbook"); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p: A, The humanities and social sciences"); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield"); 
		
		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource]"); 
		// trailing slash removed
		assertDocHasNoFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] /"); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema"); 
		assertDocHasNoFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema /"); 
		assertDocHasFieldValue("2453", fldName, "Proceedings"); 
		assertDocHasNoFieldValue("2453", fldName, "Proceedings /"); 
	}

	/**
	 * Test that 245 display field contains non-filing characters and copes with
	 *  trailing punctuation correctly
	 */
@Test
	public final void testTitleDisplayNonFiling() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "title_display";
		createIxInitVars("titleTests.mrc");
//		assertDisplayFieldProperties(fldName);
//		assertFieldNotMultiValued(fldName);
	
		// also check for trailing punctuation handling
		assertDocHasFieldValue("115472", fldName, "India and the European Economic Community"); 
		assertDocHasNoFieldValue("115472", fldName, "India and the European Economic Community."); 
		assertDocHasFieldValue("7117119", fldName, "HOUSING CARE AND SUPPORT PUTTING GOOD IDEAS INTO PRACTICE"); 
		// non-filing characters and trailing punctuation
		assertDocHasFieldValue("1962398", fldName, "A guide to resources in United States libraries"); 
		assertDocHasNoFieldValue("1962398", fldName, "A guide to resources in United States libraries /"); 
		assertDocHasNoFieldValue("1962398", fldName, "guide to resources in United States libraries"); 
		assertDocHasNoFieldValue("1962398", fldName, "guide to resources in United States libraries /"); 
		assertDocHasFieldValue("4428936", fldName, "Il cinema della transizione"); 
		assertDocHasNoFieldValue("4428936", fldName, "cinema della transizione"); 
		assertDocHasFieldValue("1261173", fldName, "The second part of the Confutation of the Ballancing letter"); 
		assertDocHasNoFieldValue("1261173", fldName, "second part of the Confutation of the Ballancing letter"); 
		assertDocHasFieldValue("575946", fldName, "Der Ruckzug der biblischen Prophetie von der neueren Geschichte"); 
		assertDocHasNoFieldValue("575946", fldName, "Der Ruckzug der biblischen Prophetie von der neueren Geschichte."); 
		assertDocHasNoFieldValue("575946", fldName, "Ruckzug der biblischen Prophetie von der neueren Geschichte"); 
		assertDocHasNoFieldValue("575946", fldName, "Ruckzug der biblischen Prophetie von der neueren Geschichte."); 
		assertDocHasFieldValue("666", fldName, "ZZZZ");
	
		// 245 only even though 130 or 240 present.
		assertDocHasFieldValue("2400", fldName, "240 0 non-filing"); 
		assertDocHasFieldValue("2402", fldName, "240 2 non-filing"); 
		assertDocHasFieldValue("2407", fldName, "240 7 non-filing"); 
		assertDocHasFieldValue("130", fldName, "130 4 non-filing"); 
		assertDocHasFieldValue("130240", fldName, "130 and 240"); 
		
		// numeric subfields
		assertDocHasFieldValue("2458", fldName, "245 has sub 8");
		assertDocHasNoFieldValue("2458", fldName, "1.5\\a 245 has sub 8");
	}

	/**
	 * Test title_full_display field 
	 */
@Test
	public final void testTitleFullDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "title_full_display";
		createIxInitVars("titleTests.mrc");
//				assertDisplayFieldProperties(fldName);
//		assertFieldNotMultiValued(fldName);
		
		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p [electronic resource] / by John Sandford."); 
		assertDocHasFieldValue("245nNotp", fldName, "245 n but no p Part one."); 
		assertDocHasFieldValue("245pNotn", fldName, "245 p but no n. subfield b Student handbook."); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p: A, The humanities and social sciences."); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield"); 
		
		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] / Laverne Galeener-Moore."); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema / Spyrou Gkrintzou."); 
		assertDocHasFieldValue("2453", fldName, "Proceedings / ..."); 
	}

	/**
	 * Test uniform title display - it uses 130 when there is one.
	 *   as of 2009-03-26  first of 130, 240 
	 *   as of 2008-12-10  only uses 130, not 240 (to mirror title_sort field)
	 *  Non-filing characters are included.
	 */
@Test
	public final void testUniformTitleDisplay() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "title_uniform_display";
		createIxInitVars("titleTests.mrc");
//				assertDisplayFieldProperties(fldName);
//		assertFieldNotMultiValued(fldName);
	
		// no 240 or 130
		assertDocHasNoField("115472", fldName);
		assertDocHasNoField("7117119", fldName);
		assertDocHasNoField("1962398", fldName);
		assertDocHasNoField("4428936", fldName);
		assertDocHasNoField("1261173", fldName);
		
		// 240 only
		String s240 = "De incertitudine et vanitate scientiarum. German";
		assertDocHasFieldValue("575946", fldName, s240);
		assertDocHasFieldValue("666", fldName, s240); 
		assertDocHasFieldValue("2400", fldName, "Wacky"); 
		assertDocHasFieldValue("2402", fldName, "A Wacky"); 
		assertDocHasNoFieldValue("2402", fldName, "Wacky"); 
		assertDocHasFieldValue("2407", fldName, "A Wacky Tacky"); 
		assertDocHasNoFieldValue("2407", fldName, "Tacky"); 

		// uniform title 130 if exists, 240 if not.
		assertDocHasFieldValue("130", fldName, "The Snimm."); 
		assertDocHasNoFieldValue("130", fldName, "Snimm."); 
		assertDocHasFieldValue("130240", fldName, "Hoos Foos"); 
		assertDocHasNoFieldValue("130240", fldName, "Marvin O'Gravel Balloon Face"); 
		assertDocHasNoFieldValue("130240", fldName, "Hoos Foos Marvin O'Gravel Balloon Face"); 
		
		// numeric subfields
		assertDocHasFieldValue("1306", fldName, "Sox on Fox");
		assertDocHasNoFieldValue("1306", fldName, "880-01 Sox on Fox");
		assertDocHasFieldValue("0240", fldName, "sleep little fishies");
		assertDocHasNoFieldValue("0240", fldName, "(DE-101c)310008891 sleep little fishies");
		assertDocHasFieldValue("24025", fldName, "la di dah");
		assertDocHasNoFieldValue("24025", fldName, "ignore me la di dah");
		
		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		assertDocHasFieldValue("2401", fldName, "Variations, piano, 4 hands, K. 501, G major"); 
		assertDocHasFieldValue("2402", fldName, "Treaties, etc. Poland, 1948 Mar. 2. Protocols, etc., 1951 Mar. 6"); 
		assertDocHasFieldValue("130", fldName, "Bible. O.T. Five Scrolls. Hebrew. Biblioteca apostolica vaticana. Manuscript. Urbiniti Hebraicus 1. 1980."); 
		assertDocHasFieldValue("11332244", fldName, "Bodkin Van Horn"); 
	}

	/**
	 * Test multiple occurrences of same field uniform_title_display =
	 * 130abcdefghijklmnopqrstuvwxyz:240abcdefghijklmnopqrstuvwxyz, first
	 */
@Test
	public final void testUniformTitle() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("vernacularNonSearchTests.mrc");
		String fldName = "title_uniform_display";
		assertDocHasFieldValue("130only", fldName, "main entry uniform title");
		fldName = "vern_title_uniform_display";
		assertDocHasFieldValue("130only", fldName, "vernacular main entry uniform title");		
	
		// 240 is back in uniform title (despite title_sort being 130 245)
		fldName = "title_uniform_display";
		assertDocHasFieldValue("240only", fldName, "uniform title");
		fldName = "vern_title_uniform_display";
		assertDocHasFieldValue("240only", fldName, "vernacular uniform title");		
	}

	/**
	 * Test that title sort field uses the correct fields in the correct order
	 */
@Test
	public final void testTitleSortIncludedFields() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "title_sort";
		createIxInitVars("titleTests.mrc");
//        assertSortFldProps(fldName);
		
		// 130 (with non-filing)
		assertSingleResult("130", fldName, "\"Snimm 130 4 nonfiling\""); 
		assertSingleResult("1306", fldName, "\"Sox on Fox 130 has sub 6\"");
		assertSingleResult("888", fldName, "\"interspersed punctuation here\"");
		
		// 240
		assertZeroResults(fldName, "\"sleep little fishies 240 has sub 0\"");
		assertSingleResult("0240", fldName, "\"240 has sub 0\"");

		assertZeroResults(fldName, "\"la di dah 240 has sub 2 and 5\"");
		assertSingleResult("24025", fldName, "\"240 has sub 2 and 5\"");

		// 130 and 240
		assertSingleResult("130240", fldName, "\"Hoos Foos 130 and 240\""); 
		assertZeroResults(fldName, "\"Hoos Foos Marvin OGravel Balloon Face 130 and 240\""); 
		assertZeroResults(fldName, "\"Marvin OGravel Balloon Face 130 and 240\""); 
	}

	/**
	 * Test that title sort field ignores non-filing characters in 245 
	 *  and uniform title fields
	 */
@Test
	public final void testTitleSortNonFiling() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "title_sort";
		createIxInitVars("titleTests.mrc");
//        assertSortFldProps(fldName);
		
		// field is not string; rather tokenized with single term
//		assertTextFieldProperties(fldName);
//		assertFieldOmitsNorms(fldName);
//		assertFieldIndexed(fldName);
//		assertFieldNotStored(fldName);
//		assertFieldNotMultiValued(fldName);
		
		// sort field is indexed (but not tokenized) - search for documents		
		assertSingleResult("115472", fldName, "\"India and the European Economic Community\"");
		assertSingleResult("115472", fldName, "\"india and the european economic community\"");
		assertSingleResult("7117119", fldName, "\"HOUSING CARE AND SUPPORT PUTTING GOOD IDEAS INTO PRACTICE\"");
		assertSingleResult("7117119", fldName, "\"housing care and support putting good ideas into practice\"");
		assertSingleResult("1962398", fldName, "\"guide to resources in United States libraries\"");
		assertZeroResults(fldName, "\"a guide to resources in United States libraries\"");
		assertSingleResult("4428936", fldName, "\"cinema della transizione\"");
		assertZeroResults(fldName, "\"Il cinema della transizione\"");
		assertSingleResult("1261173", fldName, "\"second part of the Confutation of the Ballancing letter\"");
		assertZeroResults(fldName, "\"The second part of the Confutation of the Ballancing letter\"");
		
		// 130 (with non-filing)
		assertSingleResult("130", fldName, "\"Snimm 130 4 nonfiling\""); 
		assertZeroResults(fldName, "\"The Snimm 130 4 nonfiling\""); 
		// 130 and 240
		assertSingleResult("130240", fldName, "\"Hoos Foos 130 and 240\""); 
		assertZeroResults(fldName, "\"Hoos Foos Marvin OGravel Balloon Face 130 and 240\""); 
		assertZeroResults(fldName, "\"Marvin OGravel Balloon Face 130 and 240\""); 

		// NOTE: 240 is no longer in title_sort field
		//  search for 240
		String s240 = "De incertitudine et vanitate scientiarum German ";
		assertZeroResults(fldName, s240);  // needs 245
		// search for 240 and 245 
		assertSingleResult("666", fldName, "ZZZZ");
		assertZeroResults(fldName, "\"" + s240 + "ZZZZ\""); 
		
		// non filing chars in 245
		assertSingleResult("575946", fldName, "\"Ruckzug der biblischen Prophetie von der neueren Geschichte\"");
		assertZeroResults(fldName, "\"Der Ruckzug der biblischen Prophetie von der neueren Geschichte\"");	
		assertZeroResults(fldName, "\"" + s240 + "Ruckzug der biblischen Prophetie von der neueren Geschichte\"");
		assertZeroResults(fldName, "\"" + s240 + "Der Ruckzug der biblischen Prophetie von der neueren Geschichte\"");	
		
		// 240 has non-filing
		assertSingleResult("2400", fldName, "\"240 0 nonfiling\""); 
		assertZeroResults(fldName, "\"Wacky 240 0 nonfiling\""); 
		
		assertSingleResult("2402", fldName, "\"240 2 nonfiling\""); 
		assertZeroResults(fldName, "\"Wacky 240 2 nonfiling\""); 
		assertZeroResults(fldName, "\"A Wacky 240 2 nonfiling\""); 
		
		assertSingleResult("2407", fldName, "\"240 7 nonfiling\""); 
		assertZeroResults(fldName, "\"Tacky 240 7 nonfiling\""); 
		assertZeroResults(fldName, "\"A Wacky Tacky 240 7 nonfiling\""); 
		
		//TODO:  is there a way to test the sorting??
	}


	/**
	 * Test that title sort field deals properly with numeric subfields
	 */
@Test
	public final void testTitleSortNumericSubflds() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "title_sort";
		createIxInitVars("titleTests.mrc");
//        assertSortFldProps(fldName);

		assertSingleResult("2458", fldName, "\"245 has sub 8\"");
		assertZeroResults(fldName, "\"1.5\\a 245 has sub 8\"");	
		
		assertSingleResult("1306", fldName, "\"Sox on Fox 130 has sub 6\"");
		assertZeroResults(fldName, "\"880\\-01 Sox on Fox 130 has sub 6\"");

		// 240 no longer in title_sort
		assertSingleResult("0240", fldName, "\"240 has sub 0\"");
		assertZeroResults(fldName, "\"sleep little fishies 240 has sub 0\"");
		assertZeroResults(fldName, "\"(DE-101c)310008891 sleep little fishies 240 has sub 0\"");

		assertSingleResult("24025", fldName, "\"240 has sub 2 and 5\"");
		assertZeroResults(fldName, "\"la di dah 240 has sub 2 and 5\"");
		assertZeroResults(fldName, "\"ignore me la di dah NjP 240 has sub 2 and 5\"");
	}

	/**
	 * Test that search result title sort field ignores punctuation
	 */
@Test
	public final void testTitleSortPunct()
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "title_sort";
		createIxInitVars("titleTests.mrc");
//        assertSortFldProps(fldName);
	
		assertSingleResult("111", fldName, "\"ind 0 leading quotes\"");
		assertZeroResults(fldName, "\"\"ind 0 leading quotes\"\"");
		assertZeroResults(fldName, "\"ind 0 leading quotes\\\"\"");
		assertSingleResult("222", fldName, "\"required field\"");
		assertZeroResults(fldName, "\"**required field**\"");
		assertZeroResults(fldName, "\"required field**\"");
		assertSingleResult("333", fldName, "\"ind 0 leading hyphens\"");
		assertZeroResults(fldName, "\"--ind 0 leading hyphens\"");
		assertSingleResult("444", fldName, "\"ind 0 leading elipsis\"");
		assertZeroResults(fldName, "\"...ind 0 leading elipsis\"");
		assertSingleResult("555", fldName, "\"ind 0 leading quote elipsis\"");
		assertZeroResults(fldName, "\"\\\"...ind 0 leading quote elipsis\"");
		assertSingleResult("777", fldName, "\"ind 4 leading quote elipsis\"");
		assertZeroResults(fldName, "\"\\\"...ind 4 leading quote elipsis\"");
		assertSingleResult("888", fldName, "\"interspersed punctuation here\"");
		assertZeroResults(fldName, "\"interspersed *(punctua@#$@#$tion \"here--");
		assertZeroResults(fldName, "\"Boo! interspersed *(punctua@#$@#$tion \"here--");
		assertSingleResult("999", fldName, "everything");
		// lucene special chars:  + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		assertZeroResults(fldName, "every!\\\"#$%\\&'\\(\\)\\*\\+,\\-./\\:;<=>\\?@\\[\\\\\\]\\^_`\\{|\\}\\~thing");
	}

}
