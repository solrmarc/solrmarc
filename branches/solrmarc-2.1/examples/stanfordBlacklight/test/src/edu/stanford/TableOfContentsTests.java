package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * tests for table of contents fields
 * 
 * @author Naomi Dushay
 */
public class TableOfContentsTests extends AbstractStanfordBlacklightTest {
	
    /**
     * test population of table of contents search field
     */
@Test
    public final void testTOCSearchField() 
        throws ParserConfigurationException, IOException, SAXException
    {
        String fldName = "toc_search";
	    createIxInitVars("summaryTests.mrc");
        assertSearchFldMultValProps(fldName);

        assertSingleResult("505", fldName, "505a");
        assertSingleResult("505", fldName, "505r");
        assertSingleResult("505", fldName, "505t");
		
		assertZeroResults(fldName, "nope");
    }

	/**
	 * vern_toc_search:  check all vernacular search subfields for 505
	 */
@Test
	public final void vernTocSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_toc_search";
		createIxInitVars("summaryTests.mrc");
		assertSearchFldMultValProps(fldName);

		assertSingleResult("505", fldName, "vern505a");
        assertSingleResult("505", fldName, "vern505r");
        assertSingleResult("505", fldName, "vern505t");
		
		assertZeroResults(fldName, "nope");
	}


	/**
	 * test population of context search field
	 */
@Test
	public final void testContextSearchField() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "context_search";
	    createIxInitVars("summaryTests.mrc");
	    assertSearchFldMultValProps(fldName);

	    assertSingleResult("518", fldName, "518a");

		assertZeroResults(fldName, "nope");
	}
	
	/**
	 * test population of context search field
	 */
@Test
	public final void testVernContextSearchField() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "vern_context_search";
	    createIxInitVars("summaryTests.mrc");
	    assertSearchFldMultValProps(fldName);

	    assertSingleResult("518", fldName, "vern518a");

		assertZeroResults(fldName, "nope");
	}
	
	
	/**
	 * test population of summary search field
	 */
@Test
	public final void testSummarySearchField() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "summary_search";
	    createIxInitVars("summaryTests.mrc");
	    assertSearchFldMultValProps(fldName);
	    
		assertSingleResult("520", fldName, "520a");
		assertSingleResult("520", fldName, "520b");

		assertZeroResults(fldName, "nope");
	}
	
	/**
	 * test population of vernacular summary search field 
	 */
@Test
	public final void testVernSummarySearchField() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "vern_summary_search";
	    createIxInitVars("summaryTests.mrc");
	    assertSearchFldMultValProps(fldName);

	    assertSingleResult("520", fldName, "vern520a");
		assertSingleResult("520", fldName, "vern520b");

		assertZeroResults(fldName, "nope");
	}

}
