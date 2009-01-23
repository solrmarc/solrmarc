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
public class DiacriticAcuteTests extends BibIndexTest {

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
	 * Test searching of text that has acute: query has acute
	 */
@Test
	public final void testAcuteExact() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2442876", fldName, "piétons", sis);

		assertSingleResult("1391080", fldName, "Gijón", sis);
		assertSingleResult("1391080", fldName, "jesús.", sis);
		assertSingleResult("1391080", fldName, "contemporánea", sis);
		assertSingleResult("1391080", fldName, "José María", sis);

//		assertSingleDocWithValue("2", fldName, "économique", sis);
	}
	
	/**
	 * Test searching of text that has acute: query without diacritics
	 */
@Test
	public final void testPlainText() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("2442876", fldName, "pietons", sis);

		assertSingleResult("1391080", fldName, "gijon", sis);	
		assertSingleResult("1391080", fldName, "jesus", sis);	
		assertSingleResult("1391080", fldName, "contemporanea", sis);
		assertSingleResult("1391080", fldName, "jose maria", sis);

//		assertSingleDocWithValue("2", fldName, "economique", sis);
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
		assertSingleResult("2442876", fldName, "piÃ©tons", sis);

		assertSingleResult("1391080", fldName, "GijÃ³n", sis);
		assertSingleResult("1391080", fldName, "JesÃºs", sis);
		assertSingleResult("1391080", fldName, "contemporÃ¡nea", sis);
		assertSingleResult("1391080", fldName, "JosÃ© MarÃ­a", sis);
	}

	/**
	 * Test searching of text with multiple acute - different combinations
	 */
@Test
	public final void testMultiple() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("1391080", fldName, "José María", sis);
		assertSingleResult("1391080", fldName, "José Maria", sis);
		assertSingleResult("1391080", fldName, "Jose María", sis);
		assertSingleResult("1391080", fldName, "jose maria", sis);

		assertSingleResult("1391080", fldName, "Gijón jesús", sis);
		assertSingleResult("1391080", fldName, "Gijón jesus", sis);
		assertSingleResult("1391080", fldName, "Gijon jesús", sis);
		assertSingleResult("1391080", fldName, "Gijon jesus", sis);
	}
}
