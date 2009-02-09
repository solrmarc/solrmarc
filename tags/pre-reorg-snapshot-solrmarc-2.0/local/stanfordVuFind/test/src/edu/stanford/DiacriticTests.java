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
public class DiacriticTests extends BibIndexTest {

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
		assertSingleResult("2797607", fldName, "Über", sis);
		assertSingleResult("2797607", fldName, "über", sis);
		assertSingleResult("2797607", fldName, "uber", sis);
		// u - lower case
		assertSingleResult("2797607", fldName, "Fragwürdigkeit", sis);
		assertSingleResult("2797607", fldName, "Fragwurdigkeit", sis);
		// o - lower case
		assertSingleResult("2797607", fldName, "Murtomäki", sis);
		assertSingleResult("2797607", fldName, "Murtomaki", sis);
	}

	/**
	 * Test searching of chars with acute
	 */
@Test
	public final void testAcute() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// all vowels - lower case  (with acute)
		assertSingleResult("1391080", fldName, "contemporánea", sis);
		assertSingleResult("2442876", fldName, "piétons", sis);
		assertSingleResult("1391080", fldName, "José María", sis);
		assertSingleResult("1391080", fldName, "Gijón", sis);
		assertSingleResult("1391080", fldName, "jesús.", sis);
		// plain text
		assertSingleResult("1391080", fldName, "contemporanea", sis);
		assertSingleResult("2442876", fldName, "pietons", sis);
		assertSingleResult("1391080", fldName, "Jose Maria", sis);
		assertSingleResult("1391080", fldName, "Gijon", sis);
		assertSingleResult("1391080", fldName, "jesus.", sis);
		// test multiple variations
		assertSingleResult("1391080", fldName, "José Maria", sis);
		assertSingleResult("1391080", fldName, "Jose María", sis);

		assertSingleResult("1391080", fldName, "Gijón jesús", sis);
		assertSingleResult("1391080", fldName, "Gijón jesus", sis);
		assertSingleResult("1391080", fldName, "Gijon jesús", sis);
		assertSingleResult("1391080", fldName, "Gijon jesus", sis);
	}

	/**
	 * Test searching of chars with grave
	 */
@Test
	public final void testGrave() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// a - lower case
		assertSingleResult("3027805", fldName, "verità", sis);
		assertSingleResult("3027805", fldName, "verita", sis);
		// e - lower case
		assertSingleResult("3027805", fldName, "è", sis);
		assertSingleResult("3027805", fldName, "e", sis);
	}

	/**
	 * Test searching of chars with circumflex
	 */
@Test
	public final void testCircumflex() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("3400092", fldName, "ancêtres", sis);
		assertSingleResult("3400092", fldName, "ancetres", sis);
	}

	/**
	 * Test searching of chars with tilde
	 */
@Test
	public final void testTilde() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("6493823", fldName, "Muñoz", sis);
		assertSingleResult("6493823", fldName, "munoz", sis);

		assertSingleResult("6493823", fldName, "españa", sis);
		assertSingleResult("6493823", fldName, "espana", sis);
	}

	/**
	 * Test searching of chars with cedilla (and tilde)
	 */
@Test
	public final void testCedilla() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("7651581", fldName, "exaltação", sis);
		assertSingleResult("7651581", fldName, "exaltacão", sis);
		assertSingleResult("7651581", fldName, "exaltaçao", sis);
		assertSingleResult("7651581", fldName, "exaltacao", sis);
	}

	/**
	 * Test searching of chars with ligature (russian)
	 */
@Test
	public final void testLigature() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("5", fldName, "tysi͡acha", sis);
		assertSingleResult("5", fldName, "tysi͡acha", sis);
		assertSingleResult("5", fldName, "tysiacha", sis);
	}

	/**
	 * Test searching of chars with soft sign - miagkii znak - (russian)
	 */
@Test
	public final void testSoftZnak() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("66", fldName, "vosem'sot", sis);
		assertSingleResult("66", fldName, "vosemsot", sis);
		// i have no idea if these should work, or how it should look here
//		assertSingleDocWithValue("66", fldName, "vosemьsot", sis);
//		assertSingleDocWithValue("66", fldName, "vosemьot", sis);
//		assertSingleDocWithValue("66", fldName, "voseьsot", sis);
	}

	/**
	 * More test searching of chars with soft sign - miagkii znak - (russian)
	 */
//@Test
	public final void testSoftZnakLots() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("66", fldName, "vosem'sot", sis);
		assertSingleResult("66", fldName, "vosemsot", sis);
		assertSingleResult("66", fldName, "semьdesiatykhtysiacha", sis);
		assertSingleResult("66", fldName, "sem'desiatykhtysiacha", sis);
		assertSingleResult("66", fldName, "semdesiatykhtysiacha", sis);
		// test multiples
		assertSingleResult("66", fldName, "vosemьsot semьdesiatykhtysiacha", sis);
		assertSingleResult("66", fldName, "vosemьsot semdesiatykhtysiacha", sis);
		assertSingleResult("66", fldName, "vosemsot semьdesiatykhtysiacha", sis);
		assertSingleResult("66", fldName, "vosemьsot sem'desiatykhtysiacha", sis);
		assertSingleResult("66", fldName, "vosemsot sem'desiatykhtysiacha", sis);
		assertSingleResult("66", fldName, "vosem'sot semdesiatykhtysiacha", sis);
		assertSingleResult("66", fldName, "vosemsot semdesiatykhtysiacha", sis);
	}

	/**
	 * Test searching of chars with hard sign - tverdyi znak - (russian)
	 */
@Test
	public final void testHardZnak() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("6", fldName, "Obʺedinenie", sis);
		assertSingleResult("6", fldName, "Obedinenie", sis);
		// i have no idea if these should work, or how it should look here
//		assertSingleDocWithValue("6", fldName, "Oъedinenie", sis);
//		assertSingleDocWithValue("6", fldName, "Obъedinenie", sis);
	}

	/**
	 * Test searching of chars with caron - (russian)
	 */
@Test
	public final void testCaron() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("345228", fldName, "povesť", sis);
		assertSingleResult("345228", fldName, "povest'", sis);
		assertSingleResult("345228", fldName, "povest", sis);
	
		// TODO: don't know why it doesn't like caron after the l ...
//		assertSingleDocWithValue("345228", fldName, "dokumentaľnai*", sis);
//		assertSingleDocWithValue("345228", fldName, "dokumental'nai*", sis);
		assertSingleResult("345228", fldName, "dokumentalnai*", sis);
	}

	/**
	 * Test searching of lower case i with macron - (russian)
	 */
@Test
	public final void testRussianMacronOverI() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2099904", fldName, "istorīi", sis);
		assertSingleResult("2099904", fldName, "istorii", sis);
		// i have no idea if these should work, or how it should look here
//		assertSingleDocWithValue("2099904", fldName, "istorī", sis);
//		assertSingleDocWithValue("2099904", fldName, "istori", sis);
	}

	/**
	 * Test searching of chars with macron - (Japanese)
	 */
@Test
	public final void testMacron() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("6676531", fldName, "kaisō", sis);
		assertSingleResult("6676531", fldName, "kaiso", sis);
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
		assertSingleResult("7", fldName, "Śpiewy", sis);
		assertSingleResult("7", fldName, "spiewy", sis);
	}
	
	/**
	 * Test searching of chars with slash - (Scandanavian)
	 */
// incorrect raw data
//@Test
	public final void testSlash() 
			throws ParserConfigurationException, IOException, SAXException 
	{
//		assertSingleDocWithValue("8", fldName, "nnðýþnn", sis);
//		assertSingleDocWithValue("8", fldName, "nnønn", sis);
		assertSingleResult("8", fldName, "nnunn", sis);
	}

	/**
	 * Test searching of chars with caron - (Latvian)
	 */
// incorrect raw data
//@Test
	public final void testCaron2() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("9", fldName, "Latviesǔ", sis);
		assertSingleResult("9", fldName, "Latviesu", sis);
	}

	/**
	 * Test searching of chars with ogonek - (Lithuanian)
	 */
// incorrect raw data
//@Test
	public final void testOgonek() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("10", fldName, "lokalių", sis);
		assertSingleResult("10", fldName, "lokaliu", sis);
		assertZeroResults(fldName, "lokaliy", sis);
	}

	/**
	 * Test searching of chars with overdot - (Lithuanian)
	 */
// incorrect raw data
//@Test
	public final void testOverdot() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("10", fldName, "pridėtos", sis);
		assertSingleResult("10", fldName, "pridetos", sis);
	}

	/**
	 * Test searching of unknown vietnamese char
	 */
// incorrect raw data
//@Test
	public final void testVietnamese() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("6676531", fldName, "Lập", sis);
		assertSingleResult("6676531", fldName, "lap", sis);
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
								assertSingleResult("13", fldName, "עברית", sis);
		assertSingleResult("13", fldName, "ivrit", sis);
	}
}
