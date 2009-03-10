package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * tests for existence and properties of search fields, NOT including facet
 *  fields that are used for searches in request handlers.  Does NOT test for 
 *  values within search fields - that is done in separate test classes
 * TODO:  get search field names from solrconfig.xml request handlers
 * 
 * @author Naomi Dushay
 */
public class SearchFieldsPropsTests extends BibIndexTest {
	
@Before
    public final void setup() 
            throws ParserConfigurationException, IOException, SAXException 
    {
        createIxInitVars("allfieldsTests.mrc");
    }

    /**
     * title search fields should have appropriate properties
     */
@Test
    public final void testTitleSearchFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "title_245a_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);

        fldName = "title_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
    
        fldName = "title_addl_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);

        fldName = "title_old_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    
        fldName = "title_new_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);

//TODO:  uniform title factored in for searching?
    }

    /**
     * series (title) search fields should have appropriate properties.
     */
@Test
    public final void testSeriesTitleSearchFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "series_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    
        fldName = "series_addl_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * author search fields should have appropriate properties.
     */
@Test
    public final void testAuthorSearchFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "author_person_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    
        fldName = "author_corp_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);

        fldName = "author_meeting_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);

        fldName = "author_combined_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * subject ssearch fields should have appropriate properties.
     */
@Test
    public final void testSubjectSearchFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "topic_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);    

        fldName = "topic_full_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    
        fldName = "geographic_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);

        fldName = "geographic_full_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);

        fldName = "era_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * standard number search fields should have appropriate properties.
     *  These are string fields b/c there are no spaces in standard numbers.
     */
@Test
    public final void testStandardNumberSearchFields() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "isbn_search";
        assertFieldIndexed(fldName, solrCore);
        assertFieldNotStored(fldName, solrCore);
        assertStringFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);

        fldName = "issn_search";
        assertFieldIndexed(fldName, solrCore);
        assertFieldNotStored(fldName, solrCore);
        assertStringFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * table of contents search field should have appropriate properties.
     */
@Test
    public final void testTOCSearchField() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "toc_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * publish date search field should have appropriate properties.
     */
@Test
    public final void testPublishDateSearchField() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "publish_date_search";
        assertSearchFldProps(fldName);
// TODO: text for date?  date field?  string field??        
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
    }

    /**
     * physical search field should have appropriate properties.
     */
@Test
    public final void testPhysicalSearchField() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "physical_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * catch-all search field should have appropriate properties.
     */
@Test
    public final void testCatchAllSearchField() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "all_search";
        assertSearchFldProps(fldName);
        assertTextFieldProperties(fldName, solrCore, sis);
        assertFieldNotMultiValued(fldName, solrCore);
    }


    /**
     * search fields are indexed and not stored
     */
    private void assertSearchFldProps(String searchFldName) 
        throws ParserConfigurationException, IOException, SAXException
    {
        assertFieldIndexed(searchFldName, solrCore);
        assertFieldNotStored(searchFldName, solrCore);
        assertFieldHasNorms(searchFldName, solrCore);
        // only need term vectors for "MoreLikeThis" handler
    }
}
