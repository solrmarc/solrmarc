package org.solrmarc.index;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.MarcFactoryImpl;

/** 
 * Tests the static <code>getAllSubfields*</code> methods of <code>SolrIndexer</code>.
 * 
 *  @author Tod Olson <tod@uchicago.edu>
 */


public class GetAllSubfieldsUnitTests {
	
	private MarcFactory marcFactory;

	public GetAllSubfieldsUnitTests() {
		this.marcFactory = new MarcFactoryImpl();
	}

	/*
	 * Tests for getAllSubfields
	 */

	/* Can't expect to get subfields from a control field! */
	@Test (expected=ClassCastException.class)
	public void testGetAllSubfieldsControlField() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newControlField("008", "741203d19669999wb mr p   b   0   b0ger d"));
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("260", '0', ' ', "a", "Boston :", "b", "Little, Brown,", "c", "1881."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		SolrIndexer.getAllSubfields(testRec, "008:260", null);
		fail("Expected an error when asking getAllSubfields for a control field");
	}
	
	@Test
	public void testGetAllSubfieldsSep() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("900", '0', '4', "a", "Bob", "c", "Hope"));
		testRec.addVariableField(marcFactory.newDataField("901", '0', ' ', "a", "Bing", "b", "Crosby", "c", "1942"));
		testRec.addVariableField(marcFactory.newDataField("902", ' ', ' ', "a", "Jack", "e", "Benny"));
		
		String sep = "+";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("Bob" + sep + "Hope");
		expected.add("Bing" + sep + "Crosby" + sep + "1942");
		Set<String> result = SolrIndexer.getAllSubfields(testRec, "900:901", sep);
		assertEquals(expected, result);
	}

	@Test
	public void testGetAllSubfieldsNullSep() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("900", '0', '4', "a", "Bob", "c", "Hope"));
		testRec.addVariableField(marcFactory.newDataField("901", '0', ' ', "a", "Bing", "b", "Crosby", "c", "1942"));
		testRec.addVariableField(marcFactory.newDataField("902", ' ', ' ', "a", "Jack", "e", "Benny"));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("Bob Hope");
		expected.add("Bing Crosby 1942");
		Set<String> result = SolrIndexer.getAllSubfields(testRec, "900:901", null);
		assertEquals(expected, result);
	}

	/**
	 * tests the property that <code>getAllSubfields</code> calls <code>cleanData</code> 
	 * on each matching result.
	 */
	@Test
	public void testGetAllSubfieldsCleanData() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("260", '0', ' ', "a", "Boston :", "b", "Little, Brown,", "c", "1881."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		String sep = "+";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Common Law" + sep + "by O. W. Holmes");
		expected.add("Boston :" + sep + "Little, Brown," + sep + "1881");
		Set<String> result = SolrIndexer.getAllSubfields(testRec, "245:260", sep);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetAllSubfieldsSubfieldList() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("900", '0', '4', "a", "Bob", "c", "Hope"));
		testRec.addVariableField(marcFactory.newDataField("901", '0', ' ', "a", "Bing", "b", "Crosby", "c", "1942"));
		testRec.addVariableField(marcFactory.newDataField("902", ' ', ' ', "a", "Jack", "e", "Benny"));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("Bob Hope");
		expected.add("Bing 1942");
		Set<String> result = SolrIndexer.getAllSubfields(testRec, "900[ac]:901[ac]", null);
		assertEquals(expected, result);
	}

	@Test
	public void testGetAllSubfieldsSubfieldRange() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("900", '0', '4', "a", "Bob", "c", "Hope"));
		testRec.addVariableField(marcFactory.newDataField("900", '0', ' ', "a", "Bing", "b", "Crosby", "c", "1942"));
		testRec.addVariableField(marcFactory.newDataField("900", ' ', ' ', "a", "Jack", "e", "Benny"));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("Hope");
		expected.add("Crosby 1942");
		Set<String> result = SolrIndexer.getAllSubfields(testRec, "900[b-c]", null);
		assertEquals(expected, result);
	}

	/*
	 * Tests for getAllSubfieldsCollector
	 * 
	 * Above tests exercise code with the collector as a Set.
	 * Focus on behaviors that are different if collector is not a Set
	 */
	
	@Test
	public void testGetAllSubfieldsCollectorListRepeatedFields() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("900", '0', '4', "a", "Bob", "c", "Hope"));
		testRec.addVariableField(marcFactory.newDataField("900", '0', '4', "a", "Bob", "c", "Hope"));
		testRec.addVariableField(marcFactory.newDataField("901", '0', ' ', "a", "Bing", "b", "Crosby", "c", "1942"));
		testRec.addVariableField(marcFactory.newDataField("902", ' ', ' ', "a", "Jack", "e", "Benny"));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("Bob Hope");
		expected.add("Bob Hope");
		expected.add("Bing 1942");
		Set<String> result = SolrIndexer.getAllSubfields(testRec, "900[ac]:901[ac]", null);
		assertEquals(expected, result);
	}

	@Test
	public void testGetAllSubfieldsCollectorListRepeatedValues() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("900", '0', '4', "a", "Bob", "c", "Hope"));
		testRec.addVariableField(marcFactory.newDataField("901", '0', ' ', "a", "Bing", "b", "Crosby", "c", "1942"));
		testRec.addVariableField(marcFactory.newDataField("902", ' ', ' ', "a", "Jack", "e", "Benny"));
		testRec.addVariableField(marcFactory.newDataField("903", '0', '4', "a", "Bob", "c", "Hope"));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("Bob Hope");
		expected.add("Jack");
		expected.add("Bob Hope");
		Set<String> result = SolrIndexer.getAllSubfields(testRec, "900[ac]:902[a]:903[ac]", null);
		assertEquals(expected, result);
	}

}
