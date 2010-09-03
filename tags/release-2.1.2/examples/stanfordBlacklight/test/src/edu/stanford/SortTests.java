package edu.stanford;

import static org.junit.Assert.fail;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University access_facet field
 * @author Naomi Dushay
 */
public class SortTests extends AbstractStanfordBlacklightTest {
	
	private final String testDataFname = "sortTests.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}

	/**
	 * Spaces should be significant in sorting
	 */
@Test
	public final void testSpaceSignificance() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Case / Capitalization should have no effect on sorting
	 *   (not just latin chars???)
	 */
@Test
	public final void testCaseSignificance() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Diacritics should have no effect on sorting
	 *   (not just latin chars, test first chars and subsequent chars)
	 */
@Test
	public final void testDiacriticSorting() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Non-filing indicators should be ignored for sorting.
	 *  TODO: maybe someday autodetect non-filing chars that aren't accommodated in the marc record
	 */
@Test
	public final void testNonFiling() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Combination of non-filing characters and diacritics in first character should sort properly
	 */
@Test
	public final void testNonFilingAndDiacriticsCombined() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Punctuation should not affect sorting
	 */
@Test
	public final void testPunctuation() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 *  Hebrew alif and ayn should be ignored for sorting
TODO:  as first character only, or as any character?
TODO:  transliteration vs. hebrew script ... 
	 */
@Test
	public final void testHebrewAlifAyn() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Znaks, hard and soft, should be ignored for sorting
TODO: More information needed about znaks: is this a character?  a diacritic?  Should any occurrence be ignored?
	 */
@Test
	public final void testZnaks() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Chinese - traditional and simplified characters should be sorted together
TODO: More details needed about Chinese scripts
	 */
@Test
	public final void testChinese() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Japanese - old and new characters should sort together?
TODO: More details needed about Japanese scripts
	 */
@Test
	public final void testJapanese() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Korean - something about spaces vs. no space (?)
TODO: More details needed about Korean spaces
	 */
@Test
	public final void testKorean() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Polish L should sort properly
TODO: More details needed about Polish L
	 */
@Test
	public final void testPolishL() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}
	
	/**
	 * Subscripts should be sorted properly
	 */
@Test
	public final void testSubscripts() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}

// TODO:  superscripts

	/**
	 * Oe and Ae ligatures should be sorted like oe and ae
	 */
@Test
	public final void testAeOeLigature() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		fail();
	}

// TODO:  what else? currency symbols, mathematical symbols, ...


}
