package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * tests for existence and properties of sort fields.  Does NOT test for 
 *  values within sort fields - that is done in separate test classes
 * TODO:  get sort field names from solrconfig.xml request handlers
 *   
 * @author Naomi Dushay
 */
public class SortFieldsPropsTests extends BibIndexTest {
	
@Before
    public final void setup() 
            throws ParserConfigurationException, IOException, SAXException 
    {
        createIxInitVars("allfieldsTests.mrc");
    }

    
    /**
     * title sort field should have appropriate properties
     */
@Test
    public final void testTitleSort() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "title_sort";
        assertSortFldProps(fldName);
    }

	/**
	 * author sort field should have appropriate properties
	 */
@Test
	public final void testAuthorSort() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "author_sort";
	    assertSortFldProps(fldName);
	}
	
	/**
	 * sort fields are indexed and not stored nor multivalued
	 */
	private void assertSortFldProps(String sortFldName) 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    assertFieldIndexed(sortFldName, solrCore);
	    assertFieldNotStored(sortFldName, solrCore);
        assertFieldNotMultiValued(sortFldName, solrCore);
        assertFieldOmitsNorms(sortFldName, solrCore);
        assertFieldHasNoTermVectors(sortFldName, solrCore);
	}

}
