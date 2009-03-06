package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's title fields
 * @author Naomi Dushay
 *
 */
public class TitleTests extends BibIndexTest {
	
	private final String testDataFname = "titleTests.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}


	/**
	 * Test properties of title_short field
	 */
@Test
	public final void testTitle_shortProps() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_short";
		assertSingleValFieldProps(fldName);
		assertFieldStored(fldName);
	}

	/**
	 * Test population of title field.
	 */
@Test
	public final void testTitle() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// TODO: change title field name to title_search
		String fldName = "title";
		assertSingleValFieldProps(fldName);
// TODO: not sure that title should be stored, since it duplicates titleStr
		assertFieldStored(fldName);

		// field is stored - retrieve title values from specific documents
		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p"); 
		assertDocHasFieldValue("245nNotp", fldName, "245 n but no p Part one."); 
		assertDocHasFieldValue("245pNotn", fldName, "245 p but no n. subfield b Student handbook."); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p: A, The humanities and social sciences."); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield"); 
// not sure what is up with diacritics
//		assertDocHasFieldValue("245pThenn", fldName, "245 p then n W�chentliches Verzeichnis Reihe B"); 

		// field is indexed and tokenized - search for values
		Set<String> docIds = new HashSet<String>();
		docIds.add("245NoNorP");
		docIds.add("245nNotp");
		docIds.add("245pNotn");
		docIds.add("245nAndp");
		docIds.add("245pThenn");
		docIds.add("245multpn");
		assertSearchResults(fldName, "245 n", docIds);
	}

	/**
	 * Test population of titleStr field 
	 */
@Test
	public final void testTitleStr() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "titleStr";
		assertFieldNotMultiValued(fldName);
		assertStringFieldProperties(fldName);
// TODO: not positive titleStr needs to be indexed, since title is indexed -- b/c sort by title is this field???
		assertFieldIndexed(fldName);
		assertFieldStored(fldName);

		// field is stored - retrieve titleStr values from specific documents
		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p"); 
		assertDocHasFieldValue("245nNotp", fldName, "245 n but no p Part one."); 
		assertDocHasFieldValue("245pNotn", fldName, "245 p but no n. subfield b Student handbook."); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p: A, The humanities and social sciences."); 
//		assertDocHasFieldValue("245pThenn", fldName, "245 p then n : W�chentliches Verzeichnis. : Reihe B,"); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield"); 
		
		// field is indexed but not tokenized - search for values
		// string field requires exact match
		assertZeroResults(fldName, "245 n");
		assertSingleResult("245NoNorP", fldName, "\"245 no subfield n or p\"");
	}

	/**
	 * Test population of title2 field
	 */
@Test
	public final void testTitle2() throws ParserConfigurationException, IOException, SAXException 
	{
		// TODO: change this field name to title2_search
		String fldName = "title2";
		assertMultiValFieldProps(fldName);
		assertFieldStored(fldName);
	
		// field is stored - retrieve values from specific documents
		assertDocHasFieldValue("246aNo740", fldName, "field 246 subfield a"); 
		assertDocHasFieldValue("246aAnd740", fldName, "field 246 subfield a"); 
		assertDocHasFieldValue("246aAnd740", fldName, "740 subfield a"); 
		
		// field is indexed - search for values
		assertSingleResult("246aAnd740", fldName, "740 subfield a");
		Set<String> docIds = new HashSet<String>();
		docIds.add("246aNo740");
		docIds.add("246aAnd740");
		assertSearchResults(fldName, "field 246 subfield a", docIds);
	}

	/**
	 * Test population of oldTitle field
	 */
@Test
	public final void testOldTitle() throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "oldTitle";
		assertMultiValFieldProps(fldName);
		assertFieldNotStored(fldName);
	
		// field is not stored - can't retrieve values from specific documents
	
		// field is indexed - search for values
		assertZeroResults(fldName, "780aNott");
		Set<String> docIds = new HashSet<String>();
		docIds.add("780tNota");
		docIds.add("780aAndt");
		docIds.add("780tNota");
		assertSearchResults(fldName, "780 subfield t", docIds);
	}

	/**
	 * Test population of newTitle field
	 */
@Test
	public final void testNewTitle() throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "newTitle";
		assertMultiValFieldProps(fldName);
		assertFieldNotStored(fldName);
	
		// field is not stored - can't retrieve language values from specific documents
	
		// field is indexed - search for values
		assertZeroResults(fldName, "785aNott");
		Set<String> docIds = new HashSet<String>();
		docIds.add("785tNota");
		docIds.add("785aAndt");
		assertSearchResults(fldName, "785 subfield t", docIds);
		assertSingleResult("785tNota", fldName, "only");
	}


	/**
	 * Test that 245 display field contains non-filing characters
	 */
@Test
	public final void testBriefTitleDisplay() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "brief_title_display";
		
		assertStringFieldProperties(fldName);
		assertFieldNotIndexed(fldName);
		assertFieldStored(fldName);
		assertFieldNotMultiValued(fldName);
	
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
	 * Test uniform title display - it uses 130 when there is one.
	 *  Non-filing characters are included.
	 */
@Test
	public final void testUniformTitleDisplay() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "uniform_title_display";
		
		assertStringFieldProperties(fldName);
		assertFieldNotIndexed(fldName);
		assertFieldStored(fldName);
		assertFieldNotMultiValued(fldName);
	
		// no 240 or 130
		assertDocHasNoField("115472", fldName);
		assertDocHasNoField("7117119", fldName);
		assertDocHasNoField("1962398", fldName);
		assertDocHasNoField("4428936", fldName);
		assertDocHasNoField("1261173", fldName);
		
		// 240 only
		String s240 = "De incertitudine et vanitate scientiarum. German";
		assertDocHasNoFieldValue("575946", fldName, s240);
		assertDocHasNoFieldValue("666", fldName, s240); 
		assertDocHasNoFieldValue("2400", fldName, "Wacky"); 
		assertDocHasNoFieldValue("2402", fldName, "A Wacky"); 
		assertDocHasNoFieldValue("2402", fldName, "Wacky"); 
		assertDocHasNoFieldValue("2407", fldName, "A Wacky Tacky"); 
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
		assertDocHasNoFieldValue("0240", fldName, "sleep little fishies");
		assertDocHasNoFieldValue("0240", fldName, "(DE-101c)310008891 sleep little fishies");
		assertDocHasNoFieldValue("24025", fldName, "la di dah");
		assertDocHasNoFieldValue("24025", fldName, "ignore me la di dah");
	}


	/**
	 * Test that title sort field uses the correct fields in the correct order
	 */
@Test
	public final void testTitleSortIncludedFields() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "title_sort";
		
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
		// field is not string; rather tokenized with single term
		assertTextFieldProperties(fldName);
		assertFieldOmitsNorms(fldName);
		assertFieldIndexed(fldName);
		assertFieldNotStored(fldName);
		assertFieldNotMultiValued(fldName);
		
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


	/**
	 * assert field is indexed, tokenized, has norms, does not have term vectors
	 *   and is multivalued.  (says nothing about stored)
	 */
	private final void assertSingleValFieldProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException
	{
//		assertFieldPresent(fldName);
		assertFieldIndexed(fldName);
		assertFieldTokenized(fldName);
		assertFieldHasNorms(fldName);
		assertFieldHasNoTermVectors(fldName);
		assertFieldNotMultiValued(fldName);
	}

	/**
	 * assert field is indexed, tokenized, has norms, does not have term vectors
	 *   and is multivalued.  (says nothing about stored)
	 */
	private final void assertMultiValFieldProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException
	{
//		assertFieldPresent(fldName);
		assertFieldIndexed(fldName);
		assertFieldTokenized(fldName);
		assertFieldHasNorms(fldName);
		assertFieldHasNoTermVectors(fldName);
		assertFieldMultiValued(fldName);
	}

}
