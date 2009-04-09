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
public class LanguageTests extends BibIndexTest {
		
	/**
	 * Test population of language field
	 */
@Test
	public final void testLanguages() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "language_facet";
		createIxInitVars("langTests.mrc");
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);

		assertSingleResult("008mul041atha", fldName, "Thai", sis); 
		assertSingleResult("008eng3041a", fldName, "German", sis); 
		assertSingleResult("008eng3041a", fldName, "Russian", sis);  // not 041h: id 008eng2041a041h 
		assertSingleResult("008eng2041a041h", fldName, "\"Greek, Ancient (to 1453)\"", sis); 
		assertSingleResult("008fre041d", fldName, "French", sis); 
		assertSingleResult("008nor041ad", fldName, "Norwegian", sis); 
		assertSingleResult("008nor041ad", fldName, "Swedish", sis); 

		assertZeroResults(fldName, "Italian", sis);  // not 041k:  id 008mis041ak

		Set<String> docIds = new HashSet<String>();
		docIds.add("008eng3041a");
		docIds.add("008eng2041a041h");
		assertSearchResults(fldName, "English", docIds, sis);
		docIds.clear();
		docIds.add("008spa");
		docIds.add("008fre041d");
		assertSearchResults(fldName, "Spanish", docIds, sis);
	}

	/**
	 * Test that there is no field created when the map is missing
	 *  the value to be mapped and when the map has value set to null
	 */
@Test
	public final void testMapMissingValue() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "language_facet";
		createIxInitVars("langTests.mrc");
	
		assertZeroResults(fldName, "null", sis);
		assertZeroResults(fldName, "\\?\\?\\?", sis);
		assertZeroResults(fldName, "mis", sis);     // 008mis041ak
		assertZeroResults(fldName, "Miscellaneous languages", sis);
		assertZeroResults(fldName, "mul", sis);     // 008mul041atha
		assertZeroResults(fldName, "Multiple languages", sis); 
		assertZeroResults(fldName, "und", sis);
		assertZeroResults(fldName, "zxx", sis);
	}
	
}
