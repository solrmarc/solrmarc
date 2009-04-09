package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * tests for existence and properties of display fields.  Does NOT test for
 *  vernacular display fields - that is done in the vernacular tests.
 * 
 * Does NOT test for values within display fields - that is done in separate 
 * test classes.
 * 
 * TODO:  get display field names from solrconfig.xml request handlers
 * 
 * @author Naomi Dushay
 */
public class DisplayFieldsPropsTests extends BibIndexTest {
	
@Before
    public final void setup() 
            throws ParserConfigurationException, IOException, SAXException 
    {
        createIxInitVars("allfieldsTests.mrc");
    }

    /**
     * display fields containing title information should have appropriate
     *  properties
     */
@Test
    public final void testTitleDisplayFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
	    String fldName = "title_245a_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
        fldName = "title_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
        fldName = "title_full_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
        fldName = "title_uniform_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
        fldName = "title_variant_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * display fields containing series information should have appropriate 
     *  properties.
     */
@Test
    public final void testSeriesTitleDisplayFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "series_title_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "series_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * display fields containing author information should have appropriate
     *  properties.
     */
@Test
    public final void testAuthorDisplayFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "author_person_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
        fldName = "author_person_full_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
        fldName = "author_corp_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
        fldName = "author_meeting_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
    }

    /**
     * display fields containing standard numbers should have appropriate 
     * properties.
     */
@Test
    public final void testStandardNumberDisplayFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "isbn_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "issn_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "lccn_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
        fldName = "oclc_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * format display field should have appropriate properties.
     */
@Test
    public final void testFormatDisplayField() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "format_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * publication display fields should have appropriate properties.
     */
@Test
    public final void testPublicationDisplayFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "publish_date_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
        fldName = "publisher_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "publication_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

	/**
	 * url display fields should have appropriate properties.
	 */
@Test
	public final void testUrlDisplayFields() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "url_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldMultiValued(fldName, solrCore);
	    fldName = "url_sfx_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldMultiValued(fldName, solrCore);
	    fldName = "url_suppl_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldMultiValued(fldName, solrCore);
	}

    /**
     * physical display field should have appropriate properties.
     */
@Test
    public final void testPhysicalDisplayField() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "physical_display";
        assertDisplayFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

	/**
	 * edition display field should have appropriate properties.
	 */
@Test
	public final void testEditionDisplayField() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "edition_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	}

	/**
	 * raw marc display field should have appropriate properties.
	 */
@Test
	public final void testMarcRawDisplayField() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "marc_raw_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	}
}
