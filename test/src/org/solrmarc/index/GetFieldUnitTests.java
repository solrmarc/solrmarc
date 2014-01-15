package org.solrmarc.index;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.MarcFactoryImpl;

/** 
 * Tests some of the <code>getField*</code> methods of <code>SolrIndexer</code>.
 * 
 *  @author Tod Olson <tod@uchicago.edu>
 */

public class GetFieldUnitTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private MarcFactory marcFactory;
	private SolrIndexer indexer;
	private Record testRecCommonLaw;

	public GetFieldUnitTests() {
		this.marcFactory = new MarcFactoryImpl();
		// Don't really need any properties set for these methods.
		this.indexer = SolrIndexer.indexerFromProperties(new Properties(), new String[] {"."});
		
		this.testRecCommonLaw = this.marcFactory.newRecord();
		this.testRecCommonLaw.addVariableField(marcFactory.newDataField("240", '0', '4', "a", "The Common Law"));
		this.testRecCommonLaw.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		this.testRecCommonLaw.addVariableField(marcFactory.newDataField("246", '0', '4', "a", "Common Law"));
		this.testRecCommonLaw.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		this.testRecCommonLaw.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "repeat field"));
		this.testRecCommonLaw.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "repeat field"));
		this.testRecCommonLaw.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "repeat subfield", "a", "repeat subfield"));
		
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/*
	 * getFieldList: Test for fixed field ranges
	 */

	@Test 
	public void testGetFieldListIndex() {
		String ctrlStr = "741203d19669999wb mr p   b   0   b0ger d";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("d");
		
		Record testRec = marcFactory.newRecord("01725cam  2200469 i 4500");
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr));
		Set<String> result = SolrIndexer.getFieldList(testRec, "008[6]");
		
		assertEquals(expected, result);
	}

	@Test 
	public void testGetFieldListIndexRange() {
		String ctrlStr = "741203d19669999wb mr p   b   0   b0ger d";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("1966");
		
		Record testRec = marcFactory.newRecord("01725cam  2200469 i 4500");
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr));
		Set<String> result = SolrIndexer.getFieldList(testRec, "008[7-10]");
		
		assertEquals(expected, result);
	}

	@Test 
	public void testGetFieldListIndexRangeRepeatedFields() {
		String ctrlStr1 = "741203d19669999wb mr p   b   0   b0ger d";
		String ctrlStr2 = "741203d19760000wb mr p   b   0   b0eng d";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("1966");
		expected.add("1976");
		
		Record testRec = marcFactory.newRecord("01725cam  2200469 i 4500");
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr2));
		Set<String> result = SolrIndexer.getFieldList(testRec, "008[7-10]");
		
		assertEquals(expected, result);
	}

	/*
	 * getFieldList: subfield not specified
	 */
	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testGetFieldListNoSubfield() { 
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "single field"));
		
		SolrIndexer.getFieldList(testRec, "990");
	}

	/*
	 * getFieldList: Basic tests for subfields
	 */
	@Test
	public void testGetFieldListSingleField() { 
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "single field"));

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("single field");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "990a");
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListRepeatedField() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "second"));

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("first");
		expected.add("second");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "990a");
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListRepeatedFieldDupValues() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "second"));

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("first");
		expected.add("second");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "990a");
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListDupValues() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "second"));

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("first");
		expected.add("second");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "990a:991a");
		
		assertEquals(expected, result);
	}

	/*
	 * getFieldList: tests for subfield sets and ranges
	 */
	@Test
	public void testGetFieldListSubfieldList() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		DataField df = marcFactory.newDataField("900", ' ', ' ');
		df.addSubfield(marcFactory.newSubfield('a', "subA"));
		df.addSubfield(marcFactory.newSubfield('a', "subA2"));
		df.addSubfield(marcFactory.newSubfield('b', "subB"));
		df.addSubfield(marcFactory.newSubfield('c', "subC"));
		testRec.addVariableField(df);
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "second"));

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("subA subA2 subC");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "900ac");
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListSubfieldBracket() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		DataField df = marcFactory.newDataField("900", ' ', ' ');
		df.addSubfield(marcFactory.newSubfield('a', "subA"));
		df.addSubfield(marcFactory.newSubfield('a', "subA2"));
		df.addSubfield(marcFactory.newSubfield('b', "subB"));
		df.addSubfield(marcFactory.newSubfield('c', "subC"));
		testRec.addVariableField(df);
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "second"));

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("subA subA2");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "900[a]");
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListSubfieldBracket2() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		DataField df = marcFactory.newDataField("900", ' ', ' ');
		df.addSubfield(marcFactory.newSubfield('a', "subA"));
		df.addSubfield(marcFactory.newSubfield('a', "subA2"));
		df.addSubfield(marcFactory.newSubfield('b', "subB"));
		df.addSubfield(marcFactory.newSubfield('c', "subC"));
		testRec.addVariableField(df);

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("subB");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "900[b]");
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListSubfieldBracketList() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		DataField df = marcFactory.newDataField("900", ' ', ' ');
		df.addSubfield(marcFactory.newSubfield('a', "subA"));
		df.addSubfield(marcFactory.newSubfield('a', "subA2"));
		df.addSubfield(marcFactory.newSubfield('b', "subB"));
		df.addSubfield(marcFactory.newSubfield('c', "subC"));
		testRec.addVariableField(df);

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("subA subA2 subC");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "900[ac]");
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListSubfieldBracketRange() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		DataField df = marcFactory.newDataField("900", ' ', ' ');
		df.addSubfield(marcFactory.newSubfield('a', "subA"));
		df.addSubfield(marcFactory.newSubfield('a', "subA2"));
		df.addSubfield(marcFactory.newSubfield('b', "subB"));
		df.addSubfield(marcFactory.newSubfield('c', "subC"));
		df.addSubfield(marcFactory.newSubfield('e', "subE"));
		df.addSubfield(marcFactory.newSubfield('f', "subF"));
		df.addSubfield(marcFactory.newSubfield('h', "subH"));
		testRec.addVariableField(df);

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("subB subC subE");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "900[b-e]");
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListSubfieldBracketRangeFunnyOrder() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		DataField df = marcFactory.newDataField("900", ' ', ' ');
		df.addSubfield(marcFactory.newSubfield('a', "subA"));
		df.addSubfield(marcFactory.newSubfield('b', "subB"));
		df.addSubfield(marcFactory.newSubfield('g', "subG"));
		df.addSubfield(marcFactory.newSubfield('c', "subC"));
		df.addSubfield(marcFactory.newSubfield('a', "subA2"));
		df.addSubfield(marcFactory.newSubfield('e', "subE"));
		df.addSubfield(marcFactory.newSubfield('f', "subF"));
		df.addSubfield(marcFactory.newSubfield('h', "subH"));
		testRec.addVariableField(df);

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("subB subC subE");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "900[b-e]");
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetFieldListSubfieldBracketRanges() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		DataField df = marcFactory.newDataField("900", ' ', ' ');
		df.addSubfield(marcFactory.newSubfield('a', "subA"));
		df.addSubfield(marcFactory.newSubfield('b', "subB"));
		df.addSubfield(marcFactory.newSubfield('c', "subC"));
		df.addSubfield(marcFactory.newSubfield('e', "subE"));
		df.addSubfield(marcFactory.newSubfield('f', "subF"));
		df.addSubfield(marcFactory.newSubfield('g', "subG"));
		df.addSubfield(marcFactory.newSubfield('h', "subH"));
		df.addSubfield(marcFactory.newSubfield('s', "subS"));
		df.addSubfield(marcFactory.newSubfield('t', "subT"));
		df.addSubfield(marcFactory.newSubfield('w', "subW"));
		df.addSubfield(marcFactory.newSubfield('y', "subHY"));
		testRec.addVariableField(df);

		Set<String> expected = new LinkedHashSet<String>();
		expected.add("subB subC subE subS subT");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "900[b-er-t]");
		
		assertEquals(expected, result);
	}
	
	/*
	 * getFieldList: Test linked field (LNK tagStr syntax)
	 */
	@Test
	public void testGetFieldListLinkedField() {
 		Record testRec = this.marcFactory.newRecord();
		DataField df245 = marcFactory.newDataField("245", ' ', ' ');
		df245.addSubfield(marcFactory.newSubfield('6', "880-01"));
		df245.addSubfield(marcFactory.newSubfield('a', "The Common Law"));
		df245.addSubfield(marcFactory.newSubfield('c', "by O. W. Holmes."));
		testRec.addVariableField(df245);
		DataField df880 = marcFactory.newDataField("880", ' ', ' ');
		df880.addSubfield(marcFactory.newSubfield('6', "245-01"));
		df880.addSubfield(marcFactory.newSubfield('a', "The Vernacular Common Law"));
		df880.addSubfield(marcFactory.newSubfield('c', "vernacularAuthor"));
		testRec.addVariableField(df880);
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "second"));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Vernacular Common Law");
		
		Set<String> result = SolrIndexer.getFieldList(testRec, "LNK245a");
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetLinkedFieldValueSubFieldList() {
 		Record testRec = this.marcFactory.newRecord();
		DataField df245 = marcFactory.newDataField("245", ' ', ' ');
		df245.addSubfield(marcFactory.newSubfield('6', "880-01"));
		df245.addSubfield(marcFactory.newSubfield('a', "The Common Law"));
		df245.addSubfield(marcFactory.newSubfield('c', "by O. W. Holmes."));
		testRec.addVariableField(df245);
		DataField df880 = marcFactory.newDataField("880", ' ', ' ');
		df880.addSubfield(marcFactory.newSubfield('6', "245-01"));
		df880.addSubfield(marcFactory.newSubfield('a', "The Vernacular Common Law"));
		df880.addSubfield(marcFactory.newSubfield('c', "vernacularAuthor"));
		testRec.addVariableField(df880);
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Vernacular Common Law");
		
		Set<String> result = SolrIndexer.getLinkedFieldValue(testRec, "245", "a", null);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetLinkedFieldValueSubFieldList2() {
 		Record testRec = this.marcFactory.newRecord();
		DataField df245 = marcFactory.newDataField("245", ' ', ' ');
		df245.addSubfield(marcFactory.newSubfield('6', "880-01"));
		df245.addSubfield(marcFactory.newSubfield('a', "The Common Law"));
		df245.addSubfield(marcFactory.newSubfield('b', "or how to stop worrying..."));
		df245.addSubfield(marcFactory.newSubfield('c', "by O. W. Holmes."));
		testRec.addVariableField(df245);
		DataField df880_245 = marcFactory.newDataField("880", ' ', ' ');
		df880_245.addSubfield(marcFactory.newSubfield('6', "245-01"));
		df880_245.addSubfield(marcFactory.newSubfield('a', "The Vernacular Common Law"));
		df880_245.addSubfield(marcFactory.newSubfield('b', "vernacular sub-title"));
		df880_245.addSubfield(marcFactory.newSubfield('c', "vernacularAuthor"));
		testRec.addVariableField(df880_245);
		DataField df900 = marcFactory.newDataField("900", ' ', ' ');
		df900.addSubfield(marcFactory.newSubfield('6', "880-02"));
		df900.addSubfield(marcFactory.newSubfield('a', "subA"));
		df900.addSubfield(marcFactory.newSubfield('b', "subB"));
		df900.addSubfield(marcFactory.newSubfield('c', "subC"));
		df900.addSubfield(marcFactory.newSubfield('e', "subE"));
		df900.addSubfield(marcFactory.newSubfield('f', "subF"));
		df900.addSubfield(marcFactory.newSubfield('g', "subG"));
		df900.addSubfield(marcFactory.newSubfield('h', "subH"));
		df900.addSubfield(marcFactory.newSubfield('s', "subS"));
		df900.addSubfield(marcFactory.newSubfield('t', "subT"));
		df900.addSubfield(marcFactory.newSubfield('w', "subW"));
		df900.addSubfield(marcFactory.newSubfield('y', "subHY"));
		testRec.addVariableField(df900);
		DataField df880_900 = marcFactory.newDataField("880", ' ', ' ');
		df880_900.addSubfield(marcFactory.newSubfield('6', "900-02"));
		df880_900.addSubfield(marcFactory.newSubfield('a', "VsubA"));
		df880_900.addSubfield(marcFactory.newSubfield('b', "VsubB"));
		df880_900.addSubfield(marcFactory.newSubfield('c', "VsubC"));
		df880_900.addSubfield(marcFactory.newSubfield('e', "VsubE"));
		df880_900.addSubfield(marcFactory.newSubfield('f', "VsubF"));
		df880_900.addSubfield(marcFactory.newSubfield('g', "VsubG"));
		df880_900.addSubfield(marcFactory.newSubfield('h', "VsubH"));
		df880_900.addSubfield(marcFactory.newSubfield('s', "VsubS"));
		df880_900.addSubfield(marcFactory.newSubfield('t', "VsubT"));
		df880_900.addSubfield(marcFactory.newSubfield('w', "VsubW"));
		df880_900.addSubfield(marcFactory.newSubfield('y', "VsubHY"));
		testRec.addVariableField(df880_900);

		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("VsubC VsubE VsubF VsubS VsubT");
		
		Set<String> result = SolrIndexer.getLinkedFieldValue(testRec, "900", "[c-fr-t]", null);
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetLinkedFieldValueBracketExpressionTrivial() {
 		Record testRec = this.marcFactory.newRecord();
		DataField df245 = marcFactory.newDataField("245", ' ', ' ');
		df245.addSubfield(marcFactory.newSubfield('6', "880-01"));
		df245.addSubfield(marcFactory.newSubfield('a', "The Common Law"));
		df245.addSubfield(marcFactory.newSubfield('c', "by O. W. Holmes."));
		testRec.addVariableField(df245);
		DataField df880 = marcFactory.newDataField("880", ' ', ' ');
		df880.addSubfield(marcFactory.newSubfield('6', "245-01"));
		df880.addSubfield(marcFactory.newSubfield('a', "The Vernacular Common Law"));
		df880.addSubfield(marcFactory.newSubfield('c', "vernacularAuthor"));
		testRec.addVariableField(df880);		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Vernacular Common Law");
		
		Set<String> result = SolrIndexer.getLinkedFieldValue(testRec, "245", "[a]", null);
		
		assertEquals(expected, result);
	}

	@Test
	public void testGetLinkedFieldValueBracketExpressionRange() {
 		Record testRec = this.marcFactory.newRecord();
		DataField df245 = marcFactory.newDataField("245", ' ', ' ');
		df245.addSubfield(marcFactory.newSubfield('6', "880-01"));
		df245.addSubfield(marcFactory.newSubfield('a', "The Common Law"));
		df245.addSubfield(marcFactory.newSubfield('b', "or how to stop worrying..."));
		df245.addSubfield(marcFactory.newSubfield('c', "by O. W. Holmes."));
		testRec.addVariableField(df245);
		DataField df880 = marcFactory.newDataField("880", ' ', ' ');
		df880.addSubfield(marcFactory.newSubfield('6', "245-01"));
		df880.addSubfield(marcFactory.newSubfield('a', "The Vernacular Common Law"));
		df880.addSubfield(marcFactory.newSubfield('b', "vernacular sub-title"));
		df880.addSubfield(marcFactory.newSubfield('c', "vernacularAuthor"));
		testRec.addVariableField(df880);
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Vernacular Common Law");
		
		Set<String> result = SolrIndexer.getLinkedFieldValue(testRec, "245", "[a]", null);
		
		assertEquals(expected, result);
	}

}


