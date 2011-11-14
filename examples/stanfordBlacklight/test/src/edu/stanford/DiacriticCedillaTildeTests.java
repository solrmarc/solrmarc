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
public class DiacriticCedillaTildeTests extends AbstractStanfordBlacklightTest {

	// contains the diacritics in our test data
	private String fldName = "author_person_search";
	
//@Before
	public final void initVars() 
			throws ParserConfigurationException, IOException, SAXException 
	{
//		createIxInitVars("diacriticTests.mrc");
	}

	/**
	 * Test searching of text with cedilla and tilde present
	 */
//Test
	public final void testExact() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltação");
	}
		
	/**
	 * Test searching of text in plain text
	 */
//Test
	public final void testPlainText() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltacao");
	}

	/**
	 * Test searching of text with tilde present, cedilla not
	 */
//Test
	public final void testCedillaPlainTildeExact() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltacão");
	}
	
	/**
	 * Test searching of chars with cedilla present, tilde not
	 */
//Test
	public final void testCedillaExactTildePlain() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltaçao");
	}

	/**
	 * Test searching of text with weird chars as they appear in oxygen
	 */
//Test
	public final void testWeirdLooking() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltaÃ§Ão");
		
		// ones that look normal in oxygen, etc. index incorrectly  :-P
		//	assertSingleDocWithValue("7651581", fldName, "Seleção");
		//	assertSingleDocWithValue("7651581", fldName, "Selecão");
		//	assertSingleDocWithValue("7651581", fldName, "Seleçao");
		// assertSingleDocWithValue("7651581", fldName, "SeleÃ§Ão");
		//	assertSingleDocWithValue("7651581", fldName, "Selecao");
	}
}
