package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 */
public class LanguageTests extends AbstractStanfordBlacklightTest {
	
	String fldName = "language";

	
	/**
	 * Test population of language field
	 */
@Test
	public final void testLanguages() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("langTests.mrc");
        assertStringFieldProperties(fldName);
        assertFieldIndexed(fldName);
        assertFieldStored(fldName);
		assertFieldMultiValued(fldName);

		assertSingleResult("008mul041atha", fldName, "Thai"); 
		assertSingleResult("008eng3041a", fldName, "German"); 
		assertSingleResult("008eng3041a", fldName, "Russian");  // not 041h: id 008eng2041a041h 
		assertSingleResult("008eng2041a041h", fldName, "\"Greek, Ancient (to 1453)\""); 
		assertSingleResult("008fre041d", fldName, "French"); 
		assertSingleResult("008nor041ad", fldName, "Norwegian"); 
		assertSingleResult("008nor041ad", fldName, "Swedish"); 

		assertZeroResults(fldName, "Italian");  // not 041k:  id 008mis041ak

		Set<String> docIds = new HashSet<String>();
		docIds.add("008eng3041a");
		docIds.add("008eng2041a041h");
		assertSearchResults(fldName, "English", docIds);
		docIds.clear();
		docIds.add("008spa");
		docIds.add("008fre041d");
		assertSearchResults(fldName, "Spanish", docIds);
	}

	/**
	 * Test that there is no field created when the map is missing
	 *  the value to be mapped and when the map has value set to null
	 */
@Test
	public final void testMapMissingValue() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("langTests.mrc");
	
		assertZeroResults(fldName, "null");
		assertZeroResults(fldName, "\\?\\?\\?");
		assertZeroResults(fldName, "mis");     // 008mis041ak
		assertZeroResults(fldName, "Miscellaneous languages");
		assertZeroResults(fldName, "mul");     // 008mul041atha
		assertZeroResults(fldName, "Multiple languages"); 
		assertZeroResults(fldName, "und");
		assertZeroResults(fldName, "zxx");
	}

}
