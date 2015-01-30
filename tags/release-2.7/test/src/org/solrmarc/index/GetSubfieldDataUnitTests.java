package org.solrmarc.index;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.LeaderImpl;
import org.marc4j.marc.impl.MarcFactoryImpl;

/** 
 * Tests the static <code>getSubfieldData*</code> methods of <code>SolrIndexer</code>.
 * 
 *  @author Tod Olson <tod@uchicago.edu>
 */


public class GetSubfieldDataUnitTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	private MarcFactory marcFactory;

	public GetSubfieldDataUnitTests() {
		this.marcFactory = new MarcFactoryImpl();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/*
	 * Tests for getSubfieldDataCollector 
	 * 
	 * Most code paths will be tested through the tests for 
	 * getSubfieldDataAsSet and getSubfieldDataAsList.
	 * 
	 * Focus on tests that do not vary by type of collector.
	 */

	/*
	 * getSubfieldDataCollector: tests for bad subfldStr argument.
	 */
	@Test(expected=ClassCastException.class)
	public void testGetSubfieldDataCollectorStringArgsDataFldNullSubfld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		SolrIndexer.getSubfieldDataCollector(testRec, "245", null, null, new ArrayList<String>());
		fail("should have thrown exception");
	}
	
	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testGetSubfieldDataCollectorStringArgsDataFldEmptySubfld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		SolrIndexer.getSubfieldDataCollector(testRec, "245", "", null, new ArrayList<String>());
		fail("should have thrown exception");
	}

	
	/*
	 * Tests for getSubfieldDataAsSet
	 */
	
	/*
	 * getSubfieldDataAsSet: tests for Leader
	 */

	@Test 
	public void testGetSubfieldDataAsSetStringArgsLdr() {
		String ldrStr = "01725cam  2200469 i 4500";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add(ldrStr);
		
		Leader testLdr = new LeaderImpl(ldrStr);
		Record testRec = marcFactory.newRecord(testLdr);
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "000", null, null);
		
		assertEquals(expected, result);
	}

	@Test 
	public void testGetSubfieldDataAsSetIntArgsLdr() {
		int beginIdx = 3;
		int endIdx = 11;
		String ldrStr = "01725cam  2200469 i 4500";
		String ldrSubStr = ldrStr.substring(beginIdx, endIdx);
		Set<String> expected = new LinkedHashSet<String>();
		expected.add(ldrSubStr);
		
		Leader testLdr = new LeaderImpl(ldrStr);
		Record testRec = marcFactory.newRecord(testLdr);
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "000", null, beginIdx, endIdx);
		
		assertEquals(expected, result);
	}

	/*
	 * getSubfieldDataAsSet: tests for Control Fields
	 */
	
	@Test 
	public void testGetSubfieldDataAsSetStringArgsCtrlFld() {
		String ctrlStr = "741203d19669999wb mr p   b   0   b0ger d";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add(ctrlStr);
		
		ControlField testCtrl = marcFactory.newControlField("008", ctrlStr);
		Record testRec = marcFactory.newRecord();
		testRec.addVariableField(testCtrl);
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "008", null, null);
		
		assertEquals(expected, result);
	}

	public void testGetSubfieldDataAsSetStringArgsCtrlFld2() {
		String ctrlStr1 = "741203d19669999wb mr p   b   0   b0ger d";
		String ctrlStr2 = "741203d19760000wb mr p   b   0   b0eng d";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add(ctrlStr1);
		expected.add(ctrlStr2);
		
		Record testRec = marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr2));
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "008", null, null);
		
		assertEquals(expected, result);
	}

	@Test 
	public void testGetSubfieldDataAsSetIntArgsCtrlFld() {
		int beginIdx = 7;
		int endIdx = 11;
		String ctrlStr1 = "741203d19669999wb mr p   b   0   b0ger d";
		String ctrlStr2 = "741203d19760000wb mr p   b   0   b0eng d";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("1966");
		expected.add("1976");
		
		Record testRec = marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr2));
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "008", null, beginIdx, endIdx);
		
		assertEquals(expected, result);
	}

	/*
	 * Tests for Data Fields: Integer index arguments
	 */
	
	@Test
	public void testGetSubfieldDataAsSetIntArgsDataFld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law / ", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("Common");
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "245", "a", 4, 10);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetSubfieldDataAsSetIntArgsDataFldRepeatedData() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("999", ' ', ' ', "a", "foo bar baz"));
		testRec.addVariableField(marcFactory.newDataField("999", ' ', ' ', "a", "foo bar baz"));
		testRec.addVariableField(marcFactory.newDataField("999", ' ', ' ', "a", "feh bar quzz"));
		testRec.addVariableField(marcFactory.newDataField("999", ' ', ' ', "a", "foo mux bat"));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("bar");
		expected.add("mux");
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "999", "a", 4, 7);
		assertEquals(expected, result);
	}

	@Test(expected=ClassCastException.class)
	public void testGetSubfieldDataAsSetIntArgsDataFldNullSubfield() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law / ", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		SolrIndexer.getSubfieldDataAsSet(testRec, "245", null, 4, 11);
		fail("Call where tag is a DataField and subfield is null should throw a ClassCastException");
	}
	
	@Test
	public void testGetSubfieldDataAsSetIntArgsDataFldTwoSubfld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("041", '0', '4', "a", "gerfre", "b", "engspa"));
		testRec.addVariableField(marcFactory.newDataField("041", '0', '4', "a", "pal", "c", "foo"));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("ger eng");
		expected.add("pal");
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "041", "ab", 0, 3);
		assertEquals(expected, result);
	}

	/*
	 * Tests for Data Fields: String separator arguments
	 */
	
	@Test
	public void testGetSubfieldDataAsSetStringArgsDataFld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "b", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Common Law");
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "245", "a", null);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetSubfieldDataAsSetStringArgsDataFldNullSep() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Common Law by O. W. Holmes.");
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "245", "ac", null);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetSubfieldDataAsSetStringArgsDataFldStringSep() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		String sep = "+";
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Common Law" + sep + "by O. W. Holmes.");
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "245", "ac", sep);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetSubfieldDataAsSetStringArgsDataFldRepeats() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		Set<String> expected = new LinkedHashSet<String>();
		expected.add("The Common Law by O. W. Holmes.");
		Set<String> result = SolrIndexer.getSubfieldDataAsSet(testRec, "245", "ac", null);
		assertEquals(expected, result);
	}

	@Test(expected=ClassCastException.class)
	public void testGetSubfieldDataAsSetStringArgsDataFldNullSubfld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		SolrIndexer.getSubfieldDataAsSet(testRec, "245", null, null);
		fail("should have thrown exception");
	}
	
	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testGetSubfieldDataAsSetStringArgsDataFldEmptySubfld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		SolrIndexer.getSubfieldDataAsSet(testRec, "245", "", null);
		fail("should have thrown exception");
	}

	/*
	 * getSubfieldDataAsList: tests for Control Fields
	 */
	
	@Test 
	public void testGetSubfieldDataAsListStringArgsCtrlFld() {
		String ctrlStr = "741203d19669999wb mr p   b   0   b0ger d";
		List<String> expected = new ArrayList<String>();
		expected.add(ctrlStr);
		
		ControlField testCtrl = marcFactory.newControlField("008", ctrlStr);
		Record testRec = marcFactory.newRecord();
		testRec.addVariableField(testCtrl);
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "008", null, null);
		
		assertEquals(expected, result);
	}

	public void testGetSubfieldDataAsListStringArgsCtrlFld2() {
		String ctrlStr1 = "741203d19669999wb mr p   b   0   b0ger d";
		String ctrlStr2 = "741203d19760000wb mr p   b   0   b0eng d";
		List<String> expected = new ArrayList<String>();
		expected.add(ctrlStr1);
		expected.add(ctrlStr2);
		
		Record testRec = marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr2));
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "008", null, null);
		
		assertEquals(expected, result);
	}

	public void testGetSubfieldDataAsListStringArgsCtrlFldRepeatedValue() {
		String ctrlStr1 = "741203d19669999wb mr p   b   0   b0ger d";
		String ctrlStr2 = "741203d19760000wb mr p   b   0   b0eng d";
		List<String> expected = new ArrayList<String>();
		expected.add(ctrlStr1);
		expected.add(ctrlStr2);
		expected.add(ctrlStr1);
		
		Record testRec = marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newControlField("005", "20011201100500.0"));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr2));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "008", null, null);
		
		assertEquals(expected, result);
	}

	@Test 
	public void testGetSubfieldDataAsListIntArgsCtrlFld() {
		int beginIdx = 7;
		int endIdx = 11;
		String ctrlStr1 = "741203d19669999wb mr p   b   0   b0ger d";
		String ctrlStr2 = "741203d19760000wb mr p   b   0   b0eng d";
		List<String> expected = new ArrayList<String>();
		expected.add("1966");
		expected.add("1976");
		
		Record testRec = marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr2));
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "008", null, beginIdx, endIdx);
		
		assertEquals(expected, result);
	}

	@Test 
	public void testGetSubfieldDataAsListIntArgsCtrlFldRepeatedValues() {
		int beginIdx = 7;
		int endIdx = 11;
		String ctrlStr1 = "741203d19669999wb mr p   b   0   b0ger d";
		String ctrlStr2 = "741203d19760000wb mr p   b   0   b0eng d";
		List<String> expected = new ArrayList<String>();
		expected.add("1966");
		expected.add("1976");
		expected.add("1966");
		
		Record testRec = marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newControlField("005", "20011201100500.0"));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr2));
		testRec.addVariableField(marcFactory.newControlField("008", ctrlStr1));
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "008", null, beginIdx, endIdx);
		
		assertEquals(expected, result);
	}

	/*
	 * Tests for Data Fields: Integer index arguments
	 */
	
	@Test
	public void testGetSubfieldDataAsListIntArgsDataFld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law / ", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		List<String> expected = new ArrayList<String>();
		expected.add("Common");
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "245", "a", 4, 10);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetSubfieldDataAsListIntArgsDataFldRepeatedData() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("999", ' ', ' ', "a", "foo bar baz"));
		testRec.addVariableField(marcFactory.newDataField("999", ' ', ' ', "a", "foo bar baz"));
		testRec.addVariableField(marcFactory.newDataField("999", ' ', ' ', "a", "feh bar quzz"));
		testRec.addVariableField(marcFactory.newDataField("999", ' ', ' ', "a", "foo mux bat"));
		
		List<String> expected = new ArrayList<String>();
		expected.add("bar");
		expected.add("bar");
		expected.add("bar");
		expected.add("mux");
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "999", "a", 4, 7);
		assertEquals(expected, result);
	}

	@Test(expected=ClassCastException.class)
	public void testGetSubfieldDataAsListIntArgsDataFldNullSubfield() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law / ", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		SolrIndexer.getSubfieldDataAsList(testRec, "245", null, 4, 11);
		fail("Call where tag is a DataField and subfield is null should throw a ClassCastException");
	}
	
	@Test
	public void testGetSubfieldDataAsListIntArgsDataFldTwoSubfld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("041", '0', '4', "a", "gerfre", "b", "engspa"));
		testRec.addVariableField(marcFactory.newDataField("041", '0', '4', "a", "pal", "c", "foo"));
		
		List<String> expected = new ArrayList<String>();
		expected.add("ger eng");
		expected.add("pal");
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "041", "ab", 0, 3);
		assertEquals(expected, result);
	}

	/*
	 * Tests for Data Fields: String separator arguments
	 */
	
	@Test
	public void testGetSubfieldDataAsListStringArgsDataFld() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "b", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		List<String> expected = new ArrayList<String>();
		expected.add("The Common Law");
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "245", "a", null);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetSubfieldDataAsListStringArgsDataFldNullSep() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		List<String> expected = new ArrayList<String>();
		expected.add("The Common Law by O. W. Holmes.");
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "245", "ac", null);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetSubfieldDataAsListStringArgsDataFldStringSep() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		String sep = "+";
		List<String> expected = new ArrayList<String>();
		expected.add("The Common Law" + sep + "by O. W. Holmes.");
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "245", "ac", sep);
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetSubfieldDataAsListStringArgsDataFldRepeats() {
		Record testRec = this.marcFactory.newRecord();
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("245", '0', '4', "a", "The Common Law", "c", "by O. W. Holmes."));
		testRec.addVariableField(marcFactory.newDataField("300", ' ', ' ', "a", "xvi, 422 p. ;", "c", "22 cm."));
		
		List<String> expected = new ArrayList<String>();
		expected.add("The Common Law by O. W. Holmes.");
		expected.add("The Common Law by O. W. Holmes.");
		List<String> result = SolrIndexer.getSubfieldDataAsList(testRec, "245", "ac", null);
		assertEquals(expected, result);
	}

}
