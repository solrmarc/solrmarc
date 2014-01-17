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

public class GetLinkedFieldValueUnitTests {

	private MarcFactory marcFactory;

	public GetLinkedFieldValueUnitTests() {
		this.marcFactory = new MarcFactoryImpl();
	}

	/*
	 * getLinkedFieldValue tests
	 */
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
	public void testGetLinkedFieldValueRepeatedSubFieldValues() {
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
		df900.addSubfield(marcFactory.newSubfield('c', "subC"));
		df900.addSubfield(marcFactory.newSubfield('c', "subC"));
		df900.addSubfield(marcFactory.newSubfield('c', "subC"));
		df900.addSubfield(marcFactory.newSubfield('e', "subE"));
		df900.addSubfield(marcFactory.newSubfield('f', "subF"));
		testRec.addVariableField(df900);
		DataField df880_900 = marcFactory.newDataField("880", ' ', ' ');
		df880_900.addSubfield(marcFactory.newSubfield('6', "900-02"));
		df880_900.addSubfield(marcFactory.newSubfield('a', "VsubA"));
		df880_900.addSubfield(marcFactory.newSubfield('c', "VsubC"));
		df880_900.addSubfield(marcFactory.newSubfield('c', "VsubC"));
		df880_900.addSubfield(marcFactory.newSubfield('c', "VsubC"));
		df880_900.addSubfield(marcFactory.newSubfield('e', "VsubE"));
		df880_900.addSubfield(marcFactory.newSubfield('f', "VsubF"));
		testRec.addVariableField(df880_900);

		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("VsubC VsubC VsubC");
		
		Set<String> result = SolrIndexer.getLinkedFieldValue(testRec, "900", "c", null);
		
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
	
	/*
	 * Tests for getLinkedFieldValueCollector
	 * 
	 * Focus on behaviors that are different when the collector is not a Set
	 */

	@Test
	public void testGetLinkedFieldValueCollectorRepeatedFieldValues() {
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
		df900.addSubfield(marcFactory.newSubfield('c', "subC"));
		df900.addSubfield(marcFactory.newSubfield('e', "subE"));
		df900.addSubfield(marcFactory.newSubfield('f', "subF"));
		testRec.addVariableField(df900);
		DataField df880_900 = marcFactory.newDataField("880", ' ', ' ');
		df880_900.addSubfield(marcFactory.newSubfield('6', "900-02"));
		df880_900.addSubfield(marcFactory.newSubfield('a', "VsubA"));
		df880_900.addSubfield(marcFactory.newSubfield('c', "VsubC"));
		df880_900.addSubfield(marcFactory.newSubfield('e', "VsubE"));
		df880_900.addSubfield(marcFactory.newSubfield('f', "VsubF"));
		testRec.addVariableField(df880_900);
		DataField df900_2 = marcFactory.newDataField("900", ' ', ' ');
		df900_2.addSubfield(marcFactory.newSubfield('6', "880-03"));
		df900_2.addSubfield(marcFactory.newSubfield('a', "arf!"));
		df900_2.addSubfield(marcFactory.newSubfield('c', "subC"));
		df900_2.addSubfield(marcFactory.newSubfield('e', "subE"));
		df900_2.addSubfield(marcFactory.newSubfield('f', "subF"));
		testRec.addVariableField(df900_2);
		DataField df880_900_2 = marcFactory.newDataField("880", ' ', ' ');
		df880_900_2.addSubfield(marcFactory.newSubfield('6', "900-03"));
		df880_900_2.addSubfield(marcFactory.newSubfield('a', "Varf!"));
		df880_900_2.addSubfield(marcFactory.newSubfield('c', "VsubC"));
		df880_900_2.addSubfield(marcFactory.newSubfield('e', "VsubE"));
		df880_900_2.addSubfield(marcFactory.newSubfield('f', "VsubF"));
		testRec.addVariableField(df880_900_2);

		List<String> expected = new ArrayList<String>();
		expected.add("VsubC VsubE");
		expected.add("VsubC VsubE");
		
		List<String> result = new ArrayList<String>();
		SolrIndexer.getLinkedFieldValueCollector(testRec, "900", "ce", null, result);
		
		assertEquals(expected, result);
	}

}


