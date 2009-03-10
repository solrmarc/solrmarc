package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's title fields
 * @author Naomi Dushay
 */
public class TitleTests extends BibIndexTest {
	
	/**
	 * Test title_245a_search field
	 */
@Test
	public final void testTitle245aSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_245a_search";
		createIxInitVars("titleTests.mrc");
		assertSingleValFieldProps(fldName);
		assertFieldNotStored(fldName, solrCore);
		
		assertZeroResults(fldName, "electronic", sis);  // subfield h
		assertZeroResults(fldName, "john", sis);  // subfield c
		assertZeroResults(fldName, "one", sis);  // subfield n
		assertZeroResults(fldName, "handbook", sis);  // in subfield p
	}

	/**
	 * Test population of title_search field.
	 */
@Test
	public final void testTitleSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_search";
		createIxInitVars("titleTests.mrc");
		assertSingleValFieldProps(fldName);
		assertFieldNotStored(fldName, solrCore);

// not sure what is up with diacritics
//		assertDocHasFieldValue("245pThenn", fldName, "245 p then n W�chentliches Verzeichnis Reihe B", sis); 

		Set<String> docIds = new HashSet<String>();
		docIds.add("245NoNorP");
		docIds.add("245nNotp");
		docIds.add("245pNotn");
		docIds.add("245nAndp");
		docIds.add("245pThenn");
		docIds.add("245multpn");
		// default operator is OR, since dismax will make it effectively AND
		docIds.add("2458");
		assertSearchResults(fldName, "245 n", docIds, sis);
		assertSingleResult("245nNotp", fldName, "one", sis);
		assertSingleResult("245pNotn", fldName, "handbook", sis);
		assertSingleResult("245pThenn", fldName, "Verzeichnis", sis);
		assertSingleResult("245nAndp", fldName, "humanities", sis);
	}


	/**
	 * Test population of title_addl_search field
	 */
@Test
	public final void testTitleAddlSearch() throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_addl_search";
		createIxInitVars("titleTests.mrc");
		assertMultiValFieldProps(fldName);
		assertFieldNotStored(fldName, solrCore);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("246aNo740");
		docIds.add("246aAnd740");
		assertSearchResults(fldName, "740 subfield a", docIds, sis);
		docIds.clear();
		docIds.add("246aNo740");
		docIds.add("246aAnd740");
		assertSearchResults(fldName, "field 246 subfield a", docIds, sis);
	}


	/**
	 * Test population of title_old_search field
	 */
@Test
	public final void testOldTitleSearch() throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_old_search";
		createIxInitVars("titleTests.mrc");
		assertMultiValFieldProps(fldName);
		assertFieldNotStored(fldName, solrCore);
	
		assertZeroResults(fldName, "780aNott", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("780tNota");
		docIds.add("780aAndt");
		docIds.add("780tNota");
		assertSearchResults(fldName, "780 subfield t", docIds, sis);
	}
	
	/**
	 * Test population of title_new_search field
	 */
@Test
	public final void testNewTitleSearch() throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_new_search";
		createIxInitVars("titleTests.mrc");
		assertMultiValFieldProps(fldName);
		assertFieldNotStored(fldName, solrCore);
	
		assertZeroResults(fldName, "785aNott", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("785tNota");
		docIds.add("785aAndt");
		assertSearchResults(fldName, "785 subfield t", docIds, sis);
		assertSingleResult("785tNota", fldName, "only", sis);
	}


	/**
	 * Test title_245a_display field;  trailing punctuation is removed
	 */
@Test
	public final void testTitle245aDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "title_245a_display";
		createIxInitVars("titleTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);

		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p", sis); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p", sis); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n", sis); 

		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		// trailing punctuation removed
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives", sis); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia", sis); 
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
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);

		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p [electronic resource]", sis); 
		assertDocHasFieldValue("245nNotp", fldName, "245 n but no p Part one.", sis); 
		assertDocHasFieldValue("245pNotn", fldName, "245 p but no n. subfield b Student handbook", sis); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p: A, The humanities and social sciences", sis); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield", sis); 
		
		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource]", sis); 
		// trailing slash removed
		assertDocHasNoFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] /", sis); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema", sis); 
		assertDocHasNoFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema /", sis); 
		assertDocHasFieldValue("2453", fldName, "Proceedings", sis); 
		assertDocHasNoFieldValue("2453", fldName, "Proceedings /", sis); 
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
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);
	
		// also check for trailing punctuation handling
		assertDocHasFieldValue("115472", fldName, "India and the European Economic Community", sis); 
		assertDocHasNoFieldValue("115472", fldName, "India and the European Economic Community.", sis); 
		assertDocHasFieldValue("7117119", fldName, "HOUSING CARE AND SUPPORT PUTTING GOOD IDEAS INTO PRACTICE", sis); 
		// non-filing characters and trailing punctuation
		assertDocHasFieldValue("1962398", fldName, "A guide to resources in United States libraries", sis); 
		assertDocHasNoFieldValue("1962398", fldName, "A guide to resources in United States libraries /", sis); 
		assertDocHasNoFieldValue("1962398", fldName, "guide to resources in United States libraries", sis); 
		assertDocHasNoFieldValue("1962398", fldName, "guide to resources in United States libraries /", sis); 
		assertDocHasFieldValue("4428936", fldName, "Il cinema della transizione", sis); 
		assertDocHasNoFieldValue("4428936", fldName, "cinema della transizione", sis); 
		assertDocHasFieldValue("1261173", fldName, "The second part of the Confutation of the Ballancing letter", sis); 
		assertDocHasNoFieldValue("1261173", fldName, "second part of the Confutation of the Ballancing letter", sis); 
		assertDocHasFieldValue("575946", fldName, "Der Ruckzug der biblischen Prophetie von der neueren Geschichte", sis); 
		assertDocHasNoFieldValue("575946", fldName, "Der Ruckzug der biblischen Prophetie von der neueren Geschichte.", sis); 
		assertDocHasNoFieldValue("575946", fldName, "Ruckzug der biblischen Prophetie von der neueren Geschichte", sis); 
		assertDocHasNoFieldValue("575946", fldName, "Ruckzug der biblischen Prophetie von der neueren Geschichte.", sis); 
		assertDocHasFieldValue("666", fldName, "ZZZZ", sis);
	
		// 245 only even though 130 or 240 present.
		assertDocHasFieldValue("2400", fldName, "240 0 non-filing", sis); 
		assertDocHasFieldValue("2402", fldName, "240 2 non-filing", sis); 
		assertDocHasFieldValue("2407", fldName, "240 7 non-filing", sis); 
		assertDocHasFieldValue("130", fldName, "130 4 non-filing", sis); 
		assertDocHasFieldValue("130240", fldName, "130 and 240", sis); 
		
		// numeric subfields
		assertDocHasFieldValue("2458", fldName, "245 has sub 8", sis);
		assertDocHasNoFieldValue("2458", fldName, "1.5\\a 245 has sub 8", sis);
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
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p [electronic resource] / by John Sandford.", sis); 
		assertDocHasFieldValue("245nNotp", fldName, "245 n but no p Part one.", sis); 
		assertDocHasFieldValue("245pNotn", fldName, "245 p but no n. subfield b Student handbook.", sis); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p: A, The humanities and social sciences.", sis); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield", sis); 
		
		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		
		assertDocHasFieldValue("2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] / Laverne Galeener-Moore.", sis); 
		assertDocHasFieldValue("2452", fldName, "Ton meionoteton eunoia : mythistorema / Spyrou Gkrintzou.", sis); 
		assertDocHasFieldValue("2453", fldName, "Proceedings / ...", sis); 
	}

	/**
	 * Test title_variant_display field 
	 */
	@Test
	public final void testVariantTitleDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "title_variant_display";
		createIxInitVars("displayFieldsTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2461", fldName, "Latino Institute research digest", sis); 
		assertDocHasFieldValue("2462", fldName, "At head of title: Science and public affairs Jan. 1970-Apr. 1974", sis); 
	}

	/**
	 * Test uniform title display - it uses 130 when there is one.
	 *   as of 2008-12-10  only uses 130, not 240 (to mirror title_sort field)
	 *  Non-filing characters are included.
	 */
@Test
	public final void testUniformTitleDisplay() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "title_uniform_display";
		createIxInitVars("titleTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);
	
		// no 240 or 130
		assertDocHasNoField("115472", fldName, sis);
		assertDocHasNoField("7117119", fldName, sis);
		assertDocHasNoField("1962398", fldName, sis);
		assertDocHasNoField("4428936", fldName, sis);
		assertDocHasNoField("1261173", fldName, sis);
		
		// 240 only
		String s240 = "De incertitudine et vanitate scientiarum. German";
		assertDocHasNoFieldValue("575946", fldName, s240, sis);
		assertDocHasNoFieldValue("666", fldName, s240, sis); 
		assertDocHasNoFieldValue("2400", fldName, "Wacky", sis); 
		assertDocHasNoFieldValue("2402", fldName, "A Wacky", sis); 
		assertDocHasNoFieldValue("2402", fldName, "Wacky", sis); 
		assertDocHasNoFieldValue("2407", fldName, "A Wacky Tacky", sis); 
		assertDocHasNoFieldValue("2407", fldName, "Tacky", sis); 

		// uniform title 130 if exists, 240 if not.
		assertDocHasFieldValue("130", fldName, "The Snimm.", sis); 
		assertDocHasNoFieldValue("130", fldName, "Snimm.", sis); 
		assertDocHasFieldValue("130240", fldName, "Hoos Foos", sis); 
		assertDocHasNoFieldValue("130240", fldName, "Marvin O'Gravel Balloon Face", sis); 
		assertDocHasNoFieldValue("130240", fldName, "Hoos Foos Marvin O'Gravel Balloon Face", sis); 
		
		// numeric subfields
		assertDocHasFieldValue("1306", fldName, "Sox on Fox", sis);
		assertDocHasNoFieldValue("1306", fldName, "880-01 Sox on Fox", sis);
		assertDocHasNoFieldValue("0240", fldName, "sleep little fishies", sis);
		assertDocHasNoFieldValue("0240", fldName, "(DE-101c)310008891 sleep little fishies", sis);
		assertDocHasNoFieldValue("24025", fldName, "la di dah", sis);
		assertDocHasNoFieldValue("24025", fldName, "ignore me la di dah", sis);
		
		tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		assertDocHasNoFieldValue("2401", fldName, "Variations, piano, 4 hands, K. 501, G major", sis); 
		assertDocHasNoFieldValue("2402", fldName, "Treaties, etc. Poland, 1948 Mar. 2. Protocols, etc., 1951 Mar. 6", sis); 
		assertDocHasFieldValue("130", fldName, "Bible. O.T. Five Scrolls. Hebrew. Biblioteca apostolica vaticana. Manuscript. Urbiniti Hebraicus 1. 1980.", sis); 
		assertDocHasFieldValue("11332244", fldName, "Bodkin Van Horn", sis); 
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
		
		// 130 (with non-filing)
		assertSingleResult("130", fldName, "\"Snimm 130 4 nonfiling\"", sis); 
		assertSingleResult("1306", fldName, "\"Sox on Fox 130 has sub 6\"", sis);
		assertSingleResult("888", fldName, "\"interspersed punctuation here\"", sis);
		
		// 240
		assertZeroResults(fldName, "\"sleep little fishies 240 has sub 0\"", sis);
		assertSingleResult("0240", fldName, "\"240 has sub 0\"", sis);

		assertZeroResults(fldName, "\"la di dah 240 has sub 2 and 5\"", sis);
		assertSingleResult("24025", fldName, "\"240 has sub 2 and 5\"", sis);

		// 130 and 240
		assertSingleResult("130240", fldName, "\"Hoos Foos 130 and 240\"", sis); 
		assertZeroResults(fldName, "\"Hoos Foos Marvin OGravel Balloon Face 130 and 240\"", sis); 
		assertZeroResults(fldName, "\"Marvin OGravel Balloon Face 130 and 240\"", sis); 
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
		
		// field is not string; rather tokenized with single term
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldOmitsNorms(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);
		
		// sort field is indexed (but not tokenized) - search for documents		
		assertSingleResult("115472", fldName, "\"India and the European Economic Community\"", sis);
		assertSingleResult("115472", fldName, "\"india and the european economic community\"", sis);
		assertSingleResult("7117119", fldName, "\"HOUSING CARE AND SUPPORT PUTTING GOOD IDEAS INTO PRACTICE\"", sis);
		assertSingleResult("7117119", fldName, "\"housing care and support putting good ideas into practice\"", sis);
		assertSingleResult("1962398", fldName, "\"guide to resources in United States libraries\"", sis);
		assertZeroResults(fldName, "\"a guide to resources in United States libraries\"", sis);
		assertSingleResult("4428936", fldName, "\"cinema della transizione\"", sis);
		assertZeroResults(fldName, "\"Il cinema della transizione\"", sis);
		assertSingleResult("1261173", fldName, "\"second part of the Confutation of the Ballancing letter\"", sis);
		assertZeroResults(fldName, "\"The second part of the Confutation of the Ballancing letter\"", sis);
		
		// 130 (with non-filing)
		assertSingleResult("130", fldName, "\"Snimm 130 4 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"The Snimm 130 4 nonfiling\"", sis); 
		// 130 and 240
		assertSingleResult("130240", fldName, "\"Hoos Foos 130 and 240\"", sis); 
		assertZeroResults(fldName, "\"Hoos Foos Marvin OGravel Balloon Face 130 and 240\"", sis); 
		assertZeroResults(fldName, "\"Marvin OGravel Balloon Face 130 and 240\"", sis); 

		// NOTE: 240 is no longer in title_sort field
		//  search for 240
		String s240 = "De incertitudine et vanitate scientiarum German ";
		assertZeroResults(fldName, s240, sis);  // needs 245
		// search for 240 and 245 
		assertSingleResult("666", fldName, "ZZZZ", sis);
		assertZeroResults(fldName, "\"" + s240 + "ZZZZ\"", sis); 
		
		// non filing chars in 245
		assertSingleResult("575946", fldName, "\"Ruckzug der biblischen Prophetie von der neueren Geschichte\"", sis);
		assertZeroResults(fldName, "\"Der Ruckzug der biblischen Prophetie von der neueren Geschichte\"", sis);	
		assertZeroResults(fldName, "\"" + s240 + "Ruckzug der biblischen Prophetie von der neueren Geschichte\"", sis);
		assertZeroResults(fldName, "\"" + s240 + "Der Ruckzug der biblischen Prophetie von der neueren Geschichte\"", sis);	
		
		// 240 has non-filing
		assertSingleResult("2400", fldName, "\"240 0 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"Wacky 240 0 nonfiling\"", sis); 
		
		assertSingleResult("2402", fldName, "\"240 2 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"Wacky 240 2 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"A Wacky 240 2 nonfiling\"", sis); 
		
		assertSingleResult("2407", fldName, "\"240 7 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"Tacky 240 7 nonfiling\"", sis); 
		assertZeroResults(fldName, "\"A Wacky Tacky 240 7 nonfiling\"", sis); 
		
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

		assertSingleResult("2458", fldName, "\"245 has sub 8\"", sis);
		assertZeroResults(fldName, "\"1.5\\a 245 has sub 8\"", sis);	
		
		assertSingleResult("1306", fldName, "\"Sox on Fox 130 has sub 6\"", sis);
		assertZeroResults(fldName, "\"880\\-01 Sox on Fox 130 has sub 6\"", sis);

		// 240 no longer in title_sort
		assertSingleResult("0240", fldName, "\"240 has sub 0\"", sis);
		assertZeroResults(fldName, "\"sleep little fishies 240 has sub 0\"", sis);
		assertZeroResults(fldName, "\"(DE-101c)310008891 sleep little fishies 240 has sub 0\"", sis);

		assertSingleResult("24025", fldName, "\"240 has sub 2 and 5\"", sis);
		assertZeroResults(fldName, "\"la di dah 240 has sub 2 and 5\"", sis);
		assertZeroResults(fldName, "\"ignore me la di dah NjP 240 has sub 2 and 5\"", sis);
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
	
		assertSingleResult("111", fldName, "\"ind 0 leading quotes\"", sis);
		assertZeroResults(fldName, "\"\"ind 0 leading quotes\"\"", sis);
		assertZeroResults(fldName, "\"ind 0 leading quotes\\\"\"", sis);
		assertSingleResult("222", fldName, "\"required field\"", sis);
		assertZeroResults(fldName, "\"**required field**\"", sis);
		assertZeroResults(fldName, "\"required field**\"", sis);
		assertSingleResult("333", fldName, "\"ind 0 leading hyphens\"", sis);
		assertZeroResults(fldName, "\"--ind 0 leading hyphens\"", sis);
		assertSingleResult("444", fldName, "\"ind 0 leading elipsis\"", sis);
		assertZeroResults(fldName, "\"...ind 0 leading elipsis\"", sis);
		assertSingleResult("555", fldName, "\"ind 0 leading quote elipsis\"", sis);
		assertZeroResults(fldName, "\"\\\"...ind 0 leading quote elipsis\"", sis);
		assertSingleResult("777", fldName, "\"ind 4 leading quote elipsis\"", sis);
		assertZeroResults(fldName, "\"\\\"...ind 4 leading quote elipsis\"", sis);
		assertSingleResult("888", fldName, "\"interspersed punctuation here\"", sis);
		assertZeroResults(fldName, "\"interspersed *(punctua@#$@#$tion \"here--", sis);
		assertZeroResults(fldName, "\"Boo! interspersed *(punctua@#$@#$tion \"here--", sis);
		assertSingleResult("999", fldName, "everything", sis);
		// lucene special chars:  + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		assertZeroResults(fldName, "every!\\\"#$%\\&'\\(\\)\\*\\+,\\-./\\:;<=>\\?@\\[\\\\\\]\\^_`\\{|\\}\\~thing", sis);
	}


	/**
	 * assert field is indexed, tokenized, has norms, does not have term vectors
	 *   and is multivalued.  (says nothing about stored)
	 */
	private final void assertSingleValFieldProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException
	{
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldIndexed(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);
	}

	/**
	 * assert field is indexed, tokenized, has norms, does not have term vectors
	 *   and is multivalued.  (says nothing about stored)
	 */
	private final void assertMultiValFieldProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException
	{
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldIndexed(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
	}

}
