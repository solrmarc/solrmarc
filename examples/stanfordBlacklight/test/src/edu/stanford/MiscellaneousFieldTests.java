package edu.stanford;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

import static org.junit.Assert.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 */
public class MiscellaneousFieldTests extends BibIndexTest {
	
	/**
	 * Test correct document id - the id is from 001 with an a in front
	 */
@Test 
	public final void testId() 
		throws ParserConfigurationException, SAXException, IOException
	{
		String fldName = "id";
		createIxInitVars("idTests.mrc");
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);		
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
        int numDocs = sis.getReader().numDocs();
        assertEquals("Number of documents in index incorrect: ", 3, numDocs);
        assertDocNotPresent("001noSubNo004", sis);
        assertDocPresent("001suba", sis);
        assertDocNotPresent("001and004nosub", sis);
        assertDocNotPresent("004noSuba", sis);
        assertDocPresent("001subaAnd004nosub", sis);
        assertDocNotPresent("004noSuba", sis);
        assertDocPresent("001subaAnd004suba", sis);
        assertDocNotPresent("004suba", sis);
        
        assertSingleResult("001suba", fldName, "\"001suba\"", sis);
        assertSingleResult("001subaAnd004nosub", fldName, "\"001subaAnd004nosub\"", sis);
        assertSingleResult("001subaAnd004suba", fldName, "\"001subaAnd004suba\"", sis);
	}
	

	/**
	 * Test that there is no field created when the map is missing
	 *  the value to be mapped and when the map has value set to null
	 */
@Test
	public final void testMapMissingValue() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "language";
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

	
	/**
	 * Test population of allfields
	 */
@Test
	public final void testAllSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "all_search";
		createIxInitVars("allfieldsTests.mrc");
		assertSearchFldOneValProps(fldName, solrCore, sis);
		
		String docId = "allfields1";

		// 245 just for good measure
        assertSingleResult(docId, fldName, "should", sis); 
        
        // 0xx fields are not included except 024, 027, 028
        assertSingleResult(docId, fldName, "2777802000", sis); // 024
        assertSingleResult(docId, fldName, "90620", sis); // 024
        assertSingleResult(docId, fldName, "technical", sis); // 027
        assertSingleResult(docId, fldName, "vibrations", sis); // 027
        assertZeroResults(fldName, "ocolcm", sis);  // 035
        assertZeroResults(fldName, "orlob", sis);  // 040

        // 3xx fields ARE included
        assertSingleResult(docId, fldName, "sound", sis); // 300
        assertSingleResult(docId, fldName, "annual", sis);  // 310
        
        // 6xx subject fields - we're including them, even though
        // fulltopic is all subfields of all 600, 610, 630, 650, 655
        // fullgeographic is all subfields of all 651
        //   b/c otherwise standard numbers and other things are doubled here,
        //   but topics are not.
        
        // 9xx fields are NOT included
        assertZeroResults(fldName, "EDATA", sis);  // 946
        assertZeroResults(fldName, "pamphlet", sis);  // 947
        assertZeroResults(fldName, "stacks", sis);  // 999
	}


	/**
	 * raw marc display field should have appropriate properties.
	 */
@Test
	public final void testMarc21Field() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("allfieldsTests.mrc");
	    String fldName = "marc21";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	}


	/**
	 * open search field should be stored, not indexed
	 */
@Test
	public final void testOpenSearch() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("allfieldsTests.mrc");
	    String fldName = "open_search";
	    assertDisplayFldProps(fldName, solrCore, sis);
	}

	/**
	 * spell fields should be stored and indexed
	 */
@Test
	public final void testSpellFields() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		createIxInitVars("allfieldsTests.mrc");
		Set<String> fields = new HashSet<String>(3);
		fields.add("spell");
		fields.add("spell_title");
		fields.add("spell_author");
		for (String fldName : fields) {
		    assertTextFieldProperties(fldName, solrCore, sis);
		    assertFieldOmitsNorms(fldName, solrCore);
		    assertFieldIndexed(fldName, solrCore);
		    assertFieldStored(fldName, solrCore);
		}
		assertFieldNotMultiValued("spell", solrCore);
		assertFieldMultiValued("spell_title", solrCore);
		assertFieldMultiValued("spell_author", solrCore);
	}


	/**
	 * Hokey horrible way to create a test index.
	 */
/*
//@Test
    public final void makeBLIndex() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("unicornWHoldings.mrc");
	}
*/

}
