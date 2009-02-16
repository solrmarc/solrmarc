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
public class DiacriticMacronTest extends BibIndexTest {

	private final String testDataFname = "diacriticTests.mrc";
	
	// contains the diacritics in our test data
	private String fldName = "author";
	
@Before
	public final void initVars() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}

	/**
	 * Test searching of text that has ring above: query exact match
	 */
@Test
	public final void testMacronExact() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2099904", fldName, "istorīi", sis);
	}
	
	/**
	 * Test searching of text that has ring above: query without diacritics
	 */
@Test
	public final void testMacronText() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2099904", fldName, "istorii", sis);
	}
	
}
