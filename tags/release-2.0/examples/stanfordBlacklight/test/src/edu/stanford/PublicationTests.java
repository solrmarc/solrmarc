package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

/**
 * junit4 tests for Stanford University publication fields for blacklight index
 * @author Naomi Dushay
 */
public class PublicationTests extends BibIndexTest 
{
	
	/**
	 * test publish_date_display field population.
	 */
@Test
	public final void testPublishDateDisplay() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "publish_date_display";
		createIxInitVars("pubDateTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		// may become multivalued eventually 
		assertFieldNotMultiValued(fldName, solrCore);		

		assertDocHasFieldValue("firstDateOnly008", fldName, "2000", sis); 
		assertDocHasFieldValue("bothDates008", fldName, "1964", sis); 
		assertDocHasFieldValue("contRes", fldName, "1984", sis); 
		assertDocHasFieldValue("pubDate195u", fldName, "1950s", sis);
		assertDocHasFieldValue("pubDate00uu", fldName, "1st century", sis); 
		assertDocHasFieldValue("pubDate01uu", fldName, "2nd century", sis); 
		assertDocHasFieldValue("pubDate02uu", fldName, "3rd century", sis); 
		assertDocHasFieldValue("pubDate03uu", fldName, "4th century", sis);
		assertDocHasFieldValue("pubDate08uu", fldName, "9th century", sis);
		assertDocHasFieldValue("pubDate09uu", fldName, "10th century", sis);
		assertDocHasFieldValue("pubDate10uu", fldName, "11th century", sis); 
		assertDocHasFieldValue("pubDate11uu", fldName, "12th century", sis); 
		assertDocHasFieldValue("pubDate12uu", fldName, "13th century", sis); 
		assertDocHasFieldValue("pubDate13uu", fldName, "14th century", sis); 
		assertDocHasFieldValue("pubDate16uu", fldName, "17th century", sis); 
		assertDocHasFieldValue("pubDate19uu", fldName, "20th century", sis); 
		assertDocHasFieldValue("pubDate20uu", fldName, "21st century", sis); 
		assertDocHasFieldValue("pubDate21uu", fldName, "22nd century", sis); 
		assertDocHasFieldValue("pubDate22uu", fldName, "23rd century", sis); 
		assertDocHasFieldValue("pubDate23uu", fldName, "24th century", sis); 
// TODO:  No pub date when unknown?  or "unknown"?
		assertDocHasNoField("bothDatesBlank", fldName, sis); 
		assertDocHasNoField("pubDateuuuu", fldName, sis); 
		// decided to make xuuu also unassigned
		assertDocHasNoFieldValue("pubDate1uuu", fldName, "after 1000", sis); 
		assertDocHasNoField("pubDate1uuu", fldName, sis); 
	}

	/**
	 * test publish_date_search field population.
	 */
@Test
	public final void testPublishDateSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "publish_date_search";
		createIxInitVars("pubDateTests.mrc");
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldIndexed(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		// may become multivalued eventually 
		assertFieldNotMultiValued(fldName, solrCore);		
	
		// field is indexed - search for values
		assertSingleResult("bothDates008", fldName, "\"1964\"", sis);
		assertSingleResult("pubDate195u", fldName, "\"1950s\"", sis);
		assertSingleResult("pubDate01uu", fldName, "\"2nd century\"", sis);
		assertSingleResult("pubDate19uu", fldName, "\"20th century\"", sis);
		assertZeroResults(fldName, "\"after 1000\"", sis);
	}


	/**
	 * Test publication_display field 
	 */
@Test
	public final void testPublicationDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "publication_display";
		createIxInitVars("displayFieldsTests.mrc");
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("2601", fldName, "Paris : Impr. Vincent, 1798 [i.e. Bruxelles : Moens, 1883]", sis); 
	}
}
