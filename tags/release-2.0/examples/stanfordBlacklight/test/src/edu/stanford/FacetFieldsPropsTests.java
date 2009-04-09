package edu.stanford;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

import edu.stanford.StanfordIndexer.*;

/**
 * tests for existence and properties of facet fields.  Does NOT test for 
 *  values within facet fields - that is done in separate test classes
 * TODO:  get facet field names from solrconfig.xml request handlers and/or
 *   firstSearcher / newSearcher warm up queries
 *   
 * @author Naomi Dushay
 */
public class FacetFieldsPropsTests extends BibIndexTest {
	
@Before
    public final void setup() 
            throws ParserConfigurationException, IOException, SAXException 
    {
        createIxInitVars("allfieldsTests.mrc");
    }

    
    /**
     * access_facet field should have properties of facet field 
     *  it is also multivalued and should have expected fixed values
     */
@Test
    public final void testAccessFacet() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "access_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        assertEquals("access_facet value string incorrect: ", "Online", Access.ONLINE.toString());
        assertEquals("access_facet value string incorrect: ", "At the Library", Access.AT_LIBRARY.toString());
    }

    /**
     * building_facet field should have properties of facet field and is multivalued 
     */
@Test
    public final void testBuildingFacet() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "building_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * format_facet field should have properties of facet field and is 
     *  multivalued.  It also has explicitly specified values.
     */
@Test
    public final void testFormatFacet() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "format_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        assertEquals("format_facet value string incorrect: ", "Book", Format.BOOK.toString());
        assertEquals("format_facet value string incorrect: ", "Computer File", Format.COMPUTER_FILE.toString());
        assertEquals("format_facet value string incorrect: ", "Conference Proceedings", Format.CONFERENCE_PROCEEDINGS.toString());
        assertEquals("format_facet value string incorrect: ", "Image", Format.IMAGE.toString());
        assertEquals("format_facet value string incorrect: ", "Journal", Format.JOURNAL.toString()); // no longer used
        assertEquals("format_facet value string incorrect: ", "Journal/Periodical", Format.JOURNAL_PERIODICAL.toString());
        assertEquals("format_facet value string incorrect: ", "Manuscript/Archive", Format.MANUSCRIPT_ARCHIVE.toString());
        assertEquals("format_facet value string incorrect: ", "Map/Globe", Format.MAP_GLOBE.toString());
        assertEquals("format_facet value string incorrect: ", "Microformat", Format.MICROFORMAT.toString());
        assertEquals("format_facet value string incorrect: ", "Music - Recording", Format.MUSIC_RECORDING.toString());
        assertEquals("format_facet value string incorrect: ", "Music - Score", Format.MUSIC_SCORE.toString());
        assertEquals("format_facet value string incorrect: ", "Newspaper", Format.NEWSPAPER.toString());
        assertEquals("format_facet value string incorrect: ", "Serial Publication", Format.SERIAL_PUBLICATION.toString()); // no longer used
        assertEquals("format_facet value string incorrect: ", "Sound Recording", Format.SOUND_RECORDING.toString());
        assertEquals("format_facet value string incorrect: ", "Thesis", Format.THESIS.toString());
        assertEquals("format_facet value string incorrect: ", "Video", Format.VIDEO.toString());
        assertEquals("format_facet value string incorrect: ", "Other", Format.OTHER.toString());
    }

    /**
     * call number facet field should have properties of facet fields and are 
     *  multivalued 
     */
@Test
    public final void testCallNumberFacets() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "lc_1letter_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "lc_alpha_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "lc_b4cutter_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "dewey_1digit_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "dewey_2digit_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "dewey_b4cutter_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * language_facet field should have properties of facet field and is
     *  multivalued
     */
@Test
    public final void testLanguageFacet() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "language_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * author facet field sshould have properties of facet fields and are 
     *  multivalued
     */
@Test
    public final void testAuthorFacet() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "author_person_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "author_corp_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "author_meeting_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "author_combined_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }

    /**
     * subject facet fields should have properties of facet fields and are
     *  multivalued
     */
@Test
    public final void testSubjectFacets() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "topic_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "era_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
        fldName = "geographic_facet";
        assertFacetFldProps(fldName, solrCore, sis);
        assertFieldMultiValued(fldName, solrCore);
    }
}
