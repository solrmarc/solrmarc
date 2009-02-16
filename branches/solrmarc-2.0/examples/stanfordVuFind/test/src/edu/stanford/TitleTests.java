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
		assertFieldStored(fldName, solrCore);
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
		assertFieldStored(fldName, solrCore);

		// field is stored - retrieve title values from specific documents
		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p", sis); 
		assertDocHasFieldValue("245nNotp", fldName, "245 n but no p Part one.", sis); 
		assertDocHasFieldValue("245pNotn", fldName, "245 p but no n. subfield b Student handbook.", sis); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p: A, The humanities and social sciences.", sis); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield", sis); 
// not sure what is up with diacritics
//		assertDocHasFieldValue("245pThenn", fldName, "245 p then n W�chentliches Verzeichnis Reihe B", sis); 

		// field is indexed and tokenized - search for values
		Set<String> docIds = new HashSet<String>();
		docIds.add("245NoNorP");
		docIds.add("245nNotp");
		docIds.add("245pNotn");
		docIds.add("245nAndp");
		docIds.add("245pThenn");
		docIds.add("245multpn");
		assertSearchResults(fldName, "245 n", docIds, sis);
	}

	/**
	 * Test population of titleStr field 
	 */
@Test
	public final void testTitleStr() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "titleStr";
		assertFieldNotMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
// TODO: not positive titleStr needs to be indexed, since title is indexed -- b/c sort by title is this field???
		assertFieldIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);

		// field is stored - retrieve titleStr values from specific documents
		assertDocHasFieldValue("245NoNorP", fldName, "245 no subfield n or p", sis); 
		assertDocHasFieldValue("245nNotp", fldName, "245 n but no p Part one.", sis); 
		assertDocHasFieldValue("245pNotn", fldName, "245 p but no n. subfield b Student handbook.", sis); 
		assertDocHasFieldValue("245nAndp", fldName, "245 n and p: A, The humanities and social sciences.", sis); 
//		assertDocHasFieldValue("245pThenn", fldName, "245 p then n : W�chentliches Verzeichnis. : Reihe B,", sis); 
		assertDocHasFieldValue("245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield", sis); 
		
		// field is indexed but not tokenized - search for values
		// string field requires exact match
		assertZeroResults(fldName, "245 n", sis);
		assertSingleResult("245NoNorP", fldName, "\"245 no subfield n or p\"", sis);
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
		assertFieldStored(fldName, solrCore);
	
		// field is stored - retrieve values from specific documents
		assertDocHasFieldValue("246aNo740", fldName, "field 246 subfield a", sis); 
		assertDocHasFieldValue("246aAnd740", fldName, "field 246 subfield a", sis); 
		assertDocHasFieldValue("246aAnd740", fldName, "740 subfield a", sis); 
		
		// field is indexed - search for values
		assertSingleResult("246aAnd740", fldName, "740 subfield a", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("246aNo740");
		docIds.add("246aAnd740");
		assertSearchResults(fldName, "field 246 subfield a", docIds, sis);
	}

	/**
	 * Test population of oldTitle field
	 */
@Test
	public final void testOldTitle() throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "oldTitle";
		assertMultiValFieldProps(fldName);
		assertFieldNotStored(fldName, solrCore);
	
		// field is not stored - can't retrieve values from specific documents
	
		// field is indexed - search for values
		assertZeroResults(fldName, "780aNott", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("780tNota");
		docIds.add("780aAndt");
		docIds.add("780tNota");
		assertSearchResults(fldName, "780 subfield t", docIds, sis);
	}

	/**
	 * Test population of newTitle field
	 */
@Test
	public final void testNewTitle() throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "newTitle";
		assertMultiValFieldProps(fldName);
		assertFieldNotStored(fldName, solrCore);
	
		// field is not stored - can't retrieve language values from specific documents
	
		// field is indexed - search for values
		assertZeroResults(fldName, "785aNott", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("785tNota");
		docIds.add("785aAndt");
		assertSearchResults(fldName, "785 subfield t", docIds, sis);
		assertSingleResult("785tNota", fldName, "only", sis);
	}


	/**
	 * Test that 245 display field contains non-filing characters
	 */
@Test
	public final void testBriefTitleDisplay() 
		throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "brief_title_display";
		
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
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
	 * Test uniform title display - it uses 130 when there is one.
	 *  Non-filing characters are included.
	 */
@Test
	public final void testUniformTitleDisplay() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "uniform_title_display";
		
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
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
		assertFieldPresent(fldName, sis);
		assertFieldIndexed(fldName, solrCore);
		assertFieldTokenized(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldHasNoTermVectors(fldName, solrCore);
		assertFieldNotMultiValued(fldName, solrCore);
	}

	/**
	 * assert field is indexed, tokenized, has norms, does not have term vectors
	 *   and is multivalued.  (says nothing about stored)
	 */
	private final void assertMultiValFieldProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException
	{
		assertFieldPresent(fldName, sis);
		assertFieldIndexed(fldName, solrCore);
		assertFieldTokenized(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldHasNoTermVectors(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
	}

}
