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
public class DiacriticCedillaTildeTests extends BibIndexTest {

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
	 * Test searching of text with cedilla and tilde present
	 */
	@Test
	public final void testExact() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltação", sis);
	}
		
	/**
	 * Test searching of text in plain text
	 */
@Test
	public final void testPlainText() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltacao", sis);
	}

	/**
	 * Test searching of text with tilde present, cedilla not
	 */
	@Test
	public final void testCedillaPlainTildeExact() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltacão", sis);
	}
	
	/**
	 * Test searching of chars with cedilla present, tilde not
	 */
@Test
	public final void testCedillaExactTildePlain() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltaçao", sis);
	}

	/**
	 * Test searching of text with weird chars as they appear in oxygen
	 */
//@Test
	public final void testWeirdLooking() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltaÃ§Ão", sis);
		
		// ones that look normal in oxygen, etc. index incorrectly  :-P
		//	assertSingleDocWithValue("7651581", fldName, "Seleção", sis);
		//	assertSingleDocWithValue("7651581", fldName, "Selecão", sis);
		//	assertSingleDocWithValue("7651581", fldName, "Seleçao", sis);
		// assertSingleDocWithValue("7651581", fldName, "SeleÃ§Ão", sis);
		//	assertSingleDocWithValue("7651581", fldName, "Selecao", sis);
	}
}
