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
public class DiacriticAcuteTests extends AbstractStanfordBlacklightTest {

	// contains the diacritics in our test data
	private String fldName = "author_person_search";
	
@Before
	public final void initVars() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("diacriticTests.mrc");
	}

	/**
	 * Test searching of text that has acute: query has acute
	 */
@Test
	public final void testAcuteExact() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2442876", fldName, "piétons");

		assertSingleResult("1391080", fldName, "Gijón");
		assertSingleResult("1391080", fldName, "jesús.");
		assertSingleResult("1391080", fldName, "contemporánea");
		assertSingleResult("1391080", fldName, "José María");

//		assertSingleDocWithValue("2", fldName, "économique");
	}
	
	/**
	 * Test searching of text that has acute: query without diacritics
	 */
@Test
	public final void testPlainText() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2442876", fldName, "pietons");

		assertSingleResult("1391080", fldName, "gijon");	
		assertSingleResult("1391080", fldName, "jesus");	
		assertSingleResult("1391080", fldName, "contemporanea");
		assertSingleResult("1391080", fldName, "jose maria");

//		assertSingleDocWithValue("2", fldName, "economique");
	}
	
	
	/**
	 * Test searching of text that has acute: query looks like raw xml in oxygen
	 */
//@Test
	public final void testWeirdLooking() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// how it appears in oxygen, terminal, bbedit, textedit
		// wonder if this should work ... this is how it appears in oxygen ...
		assertSingleResult("2442876", fldName, "piÃ©tons");

		assertSingleResult("1391080", fldName, "GijÃ³n");
		assertSingleResult("1391080", fldName, "JesÃºs");
		assertSingleResult("1391080", fldName, "contemporÃ¡nea");
		assertSingleResult("1391080", fldName, "JosÃ© MarÃ­a");
	}

	/**
	 * Test searching of text with multiple acute - different combinations
	 */
@Test
	public final void testMultiple() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("1391080", fldName, "José María");
		assertSingleResult("1391080", fldName, "José Maria");
		assertSingleResult("1391080", fldName, "Jose María");
		assertSingleResult("1391080", fldName, "jose maria");

		assertSingleResult("1391080", fldName, "Gijón jesús");
		assertSingleResult("1391080", fldName, "Gijón jesus");
		assertSingleResult("1391080", fldName, "Gijon jesús");
		assertSingleResult("1391080", fldName, "Gijon jesus");
	}
}
