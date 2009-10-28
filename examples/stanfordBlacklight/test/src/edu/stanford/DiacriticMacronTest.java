package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's handling of diacritics
 * 
 * searching should not be sensitive to diacritics:  searches should return
 *  the same results with and without the diacritics (and with common expansions
 *  like ö (o umlaut) --> oe  .
 * 
 * @author Naomi Dushay
 */
public class DiacriticMacronTest extends AbstractStanfordBlacklightTest {

	// contains the diacritics in our test data
	private String fldName = "author_person_search";
	
@Before
	public final void initVars() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("diacriticTests.mrc");
	}

	/**
	 * Test searching of text that has ring above: query exact match
	 */
@Test
	public final void testMacronExact() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2099904", fldName, "istorīi");
	}
	
	/**
	 * Test searching of text that has ring above: query without diacritics
	 */
@Test
	public final void testMacronText() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2099904", fldName, "istorii");
	}
	
}
