package org.solrmarc.index;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.MarcFactoryImpl;

/** 
 * Tests the <code>getField*</code> and <code>getLinkedField*</code> methods 
 * of <code>SolrIndexer</code>.
 * 
 *  @author Tod Olson <tod@uchicago.edu>
 */

public class GetFieldListUnitTests {

	private MarcFactory marcFactory;

	public GetFieldListUnitTests() {
		this.marcFactory = new MarcFactoryImpl();
	}

	/*
	 * Tests for getFieldListCollector
	 * 
	 * Most code paths will be tested through the tests for getFieldList and getFieldListAsList.
	 * 
	 * Focus on tests that do not vary by type of collector.
	 */

	/*
	 * getFieldListCollector: subfield not specified
	 */
	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testGetFieldListCollectorNoSubfield() { 
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "single field"));
		
		SolrIndexer.getFieldListCollector(testRec, "990", new LinkedHashSet<String>());
	}
	
	/*
	 * Tests for getFieldList
	 * 
	 * Will test most of the code paths for getFieldListCollector
	 */

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

	/*
	 * Tests for getFieldListAsList
	 * 
	 * Focus on areas where results will be different from getFieldList 
	 */


	@Test
	public void testGetFieldListAsListRepeatedFieldDupValues() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "second"));

		List<String> expected = new ArrayList<String>();
		expected.add("first");
		expected.add("first");
		expected.add("second");
		
		List<String> result = SolrIndexer.getFieldListAsList(testRec, "990a");
		
		assertEquals(4, testRec.getDataFields().size());
		assertEquals(expected, result);
	}

	@Test
	public void testGetFieldListAsListDupValues() {
 		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law",  "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("990", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "first"));
		testRec.addVariableField(marcFactory.newDataField("991", ' ', ' ', "a", "second"));

		List<String> expected = new ArrayList<String>();
		expected.add("first");
		expected.add("first");
		expected.add("second");
		
		List<String> result = SolrIndexer.getFieldListAsList(testRec, "990a:991a");
		
		assertEquals(expected, result);
	}

}


