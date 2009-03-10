package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

/**
 * junit4 tests for Stanford University edition field for blacklight index
 * @author Naomi Dushay
 */
public class EditionTests extends BibIndexTest {
	
	/**
	 * Test edition_display field
	 */
@Test
	public final void testEditionDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "edition_display";

		createIxInitVars("displayFieldsTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2501", fldName, "1. ed.", sis); 
		assertDocHasFieldValue("2502", fldName, "3rd draft / edited by Paul Watson.", sis); 
		
//TODO: indexer skips over records with multiple vals for single val fields
/*
		tearDown();
		createIxInitVars("editionTests.mrc");
	    assertDocHasFieldValue("editionMV", fldName, "Special education version", sis); 
	    assertDocHasFieldValue("editionMV", fldName, "Medium-high voice ed.", sis); 
*/
	}
}
