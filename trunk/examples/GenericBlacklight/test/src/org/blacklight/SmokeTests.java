package org.blacklight;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

import static org.junit.Assert.*;


/**
 * junit4 tests for blacklight demo use of solrmarc
 * @author Naomi Dushay
 */
public class SmokeTests extends DemoIxTest {
	
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("test_data.utf8.mrc");
	}
	
	/**
	 * Test populate of id field
	 */
@Test 
	public final void testId() 
		throws ParserConfigurationException, SAXException, IOException
	{
		String fldName = "id";
		assert
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);		
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
 //       int numDocs = sis.getReader().numDocs();
 //       assertEquals("Number of documents in index incorrect: ", 3, numDocs);
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
	 * Hokey horrible way to create a test index.
	 */
//@Test
    public final void makeIndex() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("unicornWHoldings.mrc");
	}

}
