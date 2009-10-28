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
 *  like Ã¶ (o umlaut) --> oe  .
 * 
 * @author Naomi Dushay
 */
public class DiacriticTests extends AbstractStanfordVufindTest {

	private final String testDataFname = "diacriticTests.mrc";
	
	// contains the diacritics in our test data
	private String fldName = "author";
	
	@SuppressWarnings("unused")
@Before
	public final void initVars() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}

	/**
	 * Test searching of chars with umlaut
	 */
@Test
	public final void testUmlaut() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// U - upper case
		assertSingleResult("2797607", fldName, "Ãœber");
		assertSingleResult("2797607", fldName, "Ã¼ber");
		assertSingleResult("2797607", fldName, "uber");
		// u - lower case
		assertSingleResult("2797607", fldName, "FragwÃ¼rdigkeit");
		assertSingleResult("2797607", fldName, "Fragwurdigkeit");
		// o - lower case
		assertSingleResult("2797607", fldName, "MurtomÃ¤ki");
		assertSingleResult("2797607", fldName, "Murtomaki");
	}

	/**
	 * Test searching of chars with acute
	 */
@Test
	public final void testAcute() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// all vowels - lower case  (with acute)
		assertSingleResult("1391080", fldName, "contemporÃ¡nea");
		assertSingleResult("2442876", fldName, "piÃ©tons");
		assertSingleResult("1391080", fldName, "JosÃ© MarÃ­a");
		assertSingleResult("1391080", fldName, "GijÃ³n");
		assertSingleResult("1391080", fldName, "jesÃºs.");
		// plain text
		assertSingleResult("1391080", fldName, "contemporanea");
		assertSingleResult("2442876", fldName, "pietons");
		assertSingleResult("1391080", fldName, "Jose Maria");
		assertSingleResult("1391080", fldName, "Gijon");
		assertSingleResult("1391080", fldName, "jesus.");
		// test multiple variations
		assertSingleResult("1391080", fldName, "JosÃ© Maria");
		assertSingleResult("1391080", fldName, "Jose MarÃ­a");

		assertSingleResult("1391080", fldName, "GijÃ³n jesÃºs");
		assertSingleResult("1391080", fldName, "GijÃ³n jesus");
		assertSingleResult("1391080", fldName, "Gijon jesÃºs");
		assertSingleResult("1391080", fldName, "Gijon jesus");
	}

	/**
	 * Test searching of chars with grave
	 */
@Test
	public final void testGrave() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// a - lower case
		assertSingleResult("3027805", fldName, "veritÃ ");
		assertSingleResult("3027805", fldName, "verita");
		// e - lower case
		assertSingleResult("3027805", fldName, "Ã¨");
		assertSingleResult("3027805", fldName, "e");
	}

	/**
	 * Test searching of chars with circumflex
	 */
@Test
	public final void testCircumflex() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("3400092", fldName, "ancÃªtres");
		assertSingleResult("3400092", fldName, "ancetres");
	}

	/**
	 * Test searching of chars with tilde
	 */
@Test
	public final void testTilde() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("6493823", fldName, "MuÃ±oz");
		assertSingleResult("6493823", fldName, "munoz");

		assertSingleResult("6493823", fldName, "espaÃ±a");
		assertSingleResult("6493823", fldName, "espana");
	}

	/**
	 * Test searching of chars with cedilla (and tilde)
	 */
@Test
	public final void testCedilla() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltaÃ§Ã£o");
		assertSingleResult("7651581", fldName, "exaltacÃ£o");
		assertSingleResult("7651581", fldName, "exaltaÃ§ao");
		assertSingleResult("7651581", fldName, "exaltacao");
	}

	/**
	 * Test searching of chars with ligature (russian)
	 */
@Test
	public final void testLigature() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("5", fldName, "tysiÍ¡acha");
		assertSingleResult("5", fldName, "tysiÍ¡acha");
		assertSingleResult("5", fldName, "tysiacha");
	}

	/**
	 * Test searching of chars with soft sign - miagkii znak - (russian)
	 */
@Test
	public final void testSoftZnak() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("66", fldName, "vosem'sot");
		assertSingleResult("66", fldName, "vosemsot");
		// i have no idea if these should work, or how it should look here
//		assertSingleDocWithValue("66", fldName, "vosemÑŒsot");
//		assertSingleDocWithValue("66", fldName, "vosemÑŒot");
//		assertSingleDocWithValue("66", fldName, "voseÑŒsot");
	}

	/**
	 * More test searching of chars with soft sign - miagkii znak - (russian)
	 */
//@Test
	public final void testSoftZnakLots() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("66", fldName, "vosem'sot");
		assertSingleResult("66", fldName, "vosemsot");
		assertSingleResult("66", fldName, "semÑŒdesiatykhtysiacha");
		assertSingleResult("66", fldName, "sem'desiatykhtysiacha");
		assertSingleResult("66", fldName, "semdesiatykhtysiacha");
		// test multiples
		assertSingleResult("66", fldName, "vosemÑŒsot semÑŒdesiatykhtysiacha");
		assertSingleResult("66", fldName, "vosemÑŒsot semdesiatykhtysiacha");
		assertSingleResult("66", fldName, "vosemsot semÑŒdesiatykhtysiacha");
		assertSingleResult("66", fldName, "vosemÑŒsot sem'desiatykhtysiacha");
		assertSingleResult("66", fldName, "vosemsot sem'desiatykhtysiacha");
		assertSingleResult("66", fldName, "vosem'sot semdesiatykhtysiacha");
		assertSingleResult("66", fldName, "vosemsot semdesiatykhtysiacha");
	}

	/**
	 * Test searching of chars with hard sign - tverdyi znak - (russian)
	 */
@Test
	public final void testHardZnak() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("6", fldName, "ObÊºedinenie");
		assertSingleResult("6", fldName, "Obedinenie");
		// i have no idea if these should work, or how it should look here
//		assertSingleDocWithValue("6", fldName, "OÑŠedinenie");
//		assertSingleDocWithValue("6", fldName, "ObÑŠedinenie");
	}

	/**
	 * Test searching of chars with caron - (russian)
	 */
@Test
	public final void testCaron() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("345228", fldName, "povesÅ¥");
		assertSingleResult("345228", fldName, "povest'");
		assertSingleResult("345228", fldName, "povest");
	
		// TODO: don't know why it doesn't like caron after the l ...
//		assertSingleDocWithValue("345228", fldName, "dokumentaÄ¾nai*");
//		assertSingleDocWithValue("345228", fldName, "dokumental'nai*");
		assertSingleResult("345228", fldName, "dokumentalnai*");
	}

	/**
	 * Test searching of lower case i with macron - (russian)
	 */
@Test
	public final void testRussianMacronOverI() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2099904", fldName, "istorÄ«i");
		assertSingleResult("2099904", fldName, "istorii");
		// i have no idea if these should work, or how it should look here
//		assertSingleDocWithValue("2099904", fldName, "istorÄ«");
//		assertSingleDocWithValue("2099904", fldName, "istori");
	}

	/**
	 * Test searching of chars with macron - (Japanese)
	 */
@Test
	public final void testMacron() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("6676531", fldName, "kaisÅ�");
		assertSingleResult("6676531", fldName, "kaiso");
	}

// Tests below this line have incorrect raw data.  The data looks okay here
//   but when it looks like this in oxygen (or in xml editor here) it is not
//   indexed correctly.  Not sure why.  Best guesses: some char set 
//   incompatibility and/or composed vs. decomposed unicode.


	/**
	 * Test searching of chars with kreska - (Polish)
	 */
//incorrect raw data
//@Test
	public final void testKreska() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7", fldName, "Åšpiewy");
		assertSingleResult("7", fldName, "spiewy");
	}
	
	/**
	 * Test searching of chars with slash - (Scandanavian)
	 */
// incorrect raw data
//@Test
	public final void testSlash() 
			throws ParserConfigurationException, IOException, SAXException 
	{
//		assertSingleDocWithValue("8", fldName, "nnÃ°Ã½Ã¾nn");
//		assertSingleDocWithValue("8", fldName, "nnÃ¸nn");
		assertSingleResult("8", fldName, "nnunn");
	}

	/**
	 * Test searching of chars with caron - (Latvian)
	 */
// incorrect raw data
//@Test
	public final void testCaron2() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("9", fldName, "LatviesÇ”");
		assertSingleResult("9", fldName, "Latviesu");
	}

	/**
	 * Test searching of chars with ogonek - (Lithuanian)
	 */
// incorrect raw data
//@Test
	public final void testOgonek() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("10", fldName, "lokaliÅ³");
		assertSingleResult("10", fldName, "lokaliu");
		assertZeroResults(fldName, "lokaliy");
	}

	/**
	 * Test searching of chars with overdot - (Lithuanian)
	 */
// incorrect raw data
//@Test
	public final void testOverdot() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("10", fldName, "pridÄ—tos");
		assertSingleResult("10", fldName, "pridetos");
	}

	/**
	 * Test searching of unknown vietnamese char
	 */
// incorrect raw data
//@Test
	public final void testVietnamese() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("6676531", fldName, "Láº­p");
		assertSingleResult("6676531", fldName, "lap");
	}

// TODO: move hebrew test to other class when indexing non-latin
	/**
	 * (not exactly a diacritic, but unicode ... )
	 * 
	 * Test searching of hebrew alphabet
	 */
//@Test
	public final void testHebrew() 
			throws ParserConfigurationException, IOException, SAXException 
	{
								assertSingleResult("13", fldName, "×¢×‘×¨×™×ª");
		assertSingleResult("13", fldName, "ivrit");
	}
}
