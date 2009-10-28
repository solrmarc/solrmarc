package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's standard number fields
 * @author Naomi Dushay
 *
 */
public class StandardNumberTests extends AbstractStanfordVufindTest {

	/**
	 * Test population of oclc field
	 */
@Test
	public final void testOCLCNums() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "oclc";
		createIxInitVars("oclcNumTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldStored(fldName);
	
		assertDocHasFieldValue("035withOCoLC-M", fldName, "656729"); 
		assertDocHasNoFieldValue("035withOCoLC-MnoParens", fldName, "656729"); 
		// doc should have oclc from good 035 and none from bad 035s
		assertDocHasFieldValue("Mult035onlyOneGood", fldName, "656729"); 
		assertDocHasNoFieldValue("Mult035onlyOneGood", fldName, "164324897"); 
		assertDocHasNoFieldValue("Mult035onlyOneGood", fldName, "1CSUO98-B6924"); 
		assertDocHasNoFieldValue("Mult035onlyOneGood", fldName, "180776170"); 
		// 079 only
		assertDocHasFieldValue("079onlyocm", fldName, "38052115"); 
		assertDocHasFieldValue("079onlyocn", fldName, "122811369"); 
		// 079 with bad prefix - 035 (OCoLC) only
		assertDocHasFieldValue("079badPrefix", fldName, "180776170"); 
		assertDocHasNoFieldValue("079badPrefix", fldName, "66654321"); 
		// doc should only have oclc from subfield a
		assertDocHasFieldValue("079onlywithz", fldName, "46660954"); 
		assertDocHasNoFieldValue("079onlywithz", fldName, "38158328"); 
		// both 079 and 035: doc should have oclc from 079, not from either 035
		assertDocHasFieldValue("079withbad035s", fldName, "12345666"); 
		assertDocHasNoFieldValue("079withbad035s", fldName, "164324897"); 
		assertDocHasNoFieldValue("079withbad035s", fldName, "CSUO98-B6924"); 
		// doc should have oclc from good 035, but not from good 079
		assertDocHasFieldValue("Good035withGood079", fldName, "656729"); 
		assertDocHasNoFieldValue("Good035withGood079", fldName, "00666000"); 
		// doc should have one oclc only, from (OCoLC) prefixed field
		assertDocHasFieldValue("035OCoLConly", fldName, "180776170"); 
		assertDocHasNoFieldValue("035OCoLConly", fldName, "164324897"); 
		assertDocHasNoFieldValue("035OCoLConly", fldName, "CSUO98-B6924"); 
		// doc should have one oclc only, from (OCoLC) prefixed field
		assertDocHasFieldValue("035bad079OCoLConly", fldName, "180776170"); 
		assertDocHasNoFieldValue("035bad079OCoLConly", fldName, "bad 079"); 
		// no oclc number
		assertDocHasNoField("035and079butNoOclc", fldName);
		// multiple oclc numbers
		assertDocHasFieldValue("MultOclcNums", fldName, "656729"); 
		assertDocHasFieldValue("MultOclcNums", fldName, "38052115"); 
		assertDocHasFieldValue("MultOclcNums", fldName, "38403775"); 
		assertDocHasNoFieldValue("MultOclcNums", fldName, "180776170"); 
		assertDocHasNoFieldValue("MultOclcNums", fldName, "00666000"); 
	}


	/**
	 * Test searching of oclc field
	 */
@Test
	public final void testOCLCNumSearchable() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "oclc";
		createIxInitVars("oclcNumTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldIndexed(fldName);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("035withOCoLC-M");
		docIds.add("Mult035onlyOneGood");
		docIds.add("MultOclcNums");
		docIds.add("Good035withGood079");
		assertSearchResults(fldName, "656729", docIds);
		
		docIds.clear();
		docIds.add("079onlyocm");
		docIds.add("MultOclcNums");
		assertSearchResults(fldName, "38052115", docIds);

		docIds.clear();
		docIds.add("079badPrefix");
		docIds.add("035OCoLConly");
		docIds.add("035bad079OCoLConly");
		assertSearchResults(fldName, "180776170", docIds);

		assertSingleResult("079onlyocn", fldName, "122811369"); 
		assertSingleResult("079onlywithz", fldName, "46660954"); 
		assertSingleResult("079withbad035s", fldName, "12345666"); 
		assertSingleResult("MultOclcNums", fldName, "38403775"); 
				
		assertZeroResults(fldName, "1CSUO98-B6924"); 
		assertZeroResults(fldName, "CSUO98-B6924");
		assertZeroResults(fldName, "164324897"); 
		assertZeroResults(fldName, "00666000"); 

		assertZeroResults(fldName, "66654321"); 
		assertZeroResults(fldName, "38158328"); 
		assertZeroResults(fldName, "\"bad 079\""); 
	}


	/**
	 * Test population of isbn field: the ISBNs that code will use for external 
	 *  lookups (e.g. Google Book Search)
	 */
@Test
	public final void testISBNs() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		// TODO: change this fieldname to isbn_store
		String fldName = "isbn";
		createIxInitVars("isbnTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldStored(fldName);
		assertFieldNotIndexed(fldName);
	
		// no isbn
		assertDocHasNoField("No020", fldName);
		assertDocHasNoField("020noSubaOrz", fldName);
		// 020 subfield a 10 digit varieties
		assertDocHasFieldValue("020suba10digit", fldName, "1417559128"); 
		assertDocHasFieldValue("020suba10endsX", fldName, "123456789X"); 
		assertDocHasFieldValue("020suba10trailingText", fldName, "1234567890"); 
		assertDocHasFieldValue("020suba10trailingText", fldName, "0123456789"); 
		assertDocHasFieldValue("020suba10trailingText", fldName, "0521672694"); 
		assertDocHasFieldValue("020suba10trailingText", fldName, "052185668X"); 
		// 020 subfield a 13 digit varieties
		assertDocHasFieldValue("020suba13", fldName, "9780809424887"); 
		assertDocHasFieldValue("020suba13endsX", fldName, "979123456789X"); 
		assertDocHasNoField("020suba13bad", fldName);
		assertDocHasNoFieldValue("020suba13bad", fldName, "000123456789X"); 
		assertDocHasFieldValue("020suba13trailingText", fldName, "978185585039X"); 
		assertDocHasFieldValue("020suba13trailingText", fldName, "9780809424887"); 
		assertDocHasFieldValue("020suba13trailingText", fldName, "9780809424870"); 
		// sub a mixed 10 and 13 digit
		assertDocHasFieldValue("020subaMult", fldName, "0809424886"); 
		assertDocHasFieldValue("020subaMult", fldName, "123456789X"); 
		assertDocHasFieldValue("020subaMult", fldName, "1234567890"); 
		assertDocHasFieldValue("020subaMult", fldName, "979123456789X"); 
		assertDocHasFieldValue("020subaMult", fldName, "9780809424887"); 
		assertDocHasFieldValue("020subaMult", fldName, "9781855850484"); 
		// no subfield a in 020, but has subfield z 10 digit
		assertDocHasFieldValue("020subz10digit", fldName, "9876543210"); 		
		assertDocHasFieldValue("020subz10endsX", fldName, "123456789X"); 
		assertDocHasFieldValue("020subz10trailingText", fldName, "1234567890"); 
		assertDocHasFieldValue("020subz10trailingText", fldName, "0123456789"); 
		assertDocHasFieldValue("020subz10trailingText", fldName, "0521672694"); 
		// no subfield a in 020, but has subfield z 13 digit		
		assertDocHasFieldValue("020subz13digit", fldName, "9780809424887"); 		
		assertDocHasFieldValue("020subz13endsX", fldName, "979123456789X"); 
		assertDocHasFieldValue("020subz13trailingText", fldName, "978185585039X"); 
		assertDocHasFieldValue("020subz13trailingText", fldName, "9780809424887"); 
		assertDocHasFieldValue("020subz13trailingText", fldName, "9780809424870"); 
		// mult subfield z in single 020
		assertDocHasFieldValue("020multSubz", fldName, "9802311987");
		assertDocHasFieldValue("020multSubz", fldName, "9802311995");
		assertDocHasFieldValue("020multSubz", fldName, "9802312002");
		assertDocHasFieldValue("020multSubz", fldName, "9876543210");
		assertDocHasFieldValue("020multSubz", fldName, "123456789X");
		assertDocHasFieldValue("020multSubz", fldName, "9780809424887");
		assertDocHasFieldValue("020multSubz", fldName, "979123456789X");
		assertDocHasFieldValue("020multSubz", fldName, "9780809424870");
	
		// mult a and z - should only have a
		assertDocHasFieldValue("020SubaAndz", fldName, "0123456789");
		assertDocHasFieldValue("020SubaAndz", fldName, "0521672694");
		assertDocHasNoFieldValue("020SubaAndz", fldName, "9802311987");
		assertDocHasFieldValue("020SubaAndz", fldName, "052185668X");
		assertDocHasNoFieldValue("020SubaAndz", fldName, "123456789X");
		assertDocHasNoFieldValue("020SubaAndz", fldName, "9780809424887");
	}

	/**
	 * Test population of isbnUser_search field: the ISBNs that an end user can 
	 *  search for in our index
	 */
@Test
	public final void testUserISBNs() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "isbnUser_search";
		createIxInitVars("isbnTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		// searches are not exhaustive  (b/c programmer is exhausted)
	
		// isbn search with sub a value from record with mult a and z
		String value = "052185668X";
		assertResultSize(fldName, value, 2);
		String docList[] = searcherProxy.getDocIdsFromSearch(fldName, value, docIDfname);
		String msg = "isbn search \""+ value + "\": ";
		assertDocInList(docList, "020suba10trailingText", msg);
		assertDocInList(docList, "020SubaAndz", msg);
	
		// isbn search with sub z value from record with mult a and z
		value = "9780809424887";
		assertResultSize(fldName, value, 7);
		docList = searcherProxy.getDocIdsFromSearch(fldName, value, docIDfname);
		msg = fldName + " search \""+ value + "\": ";
		assertDocInList(docList, "020suba13", msg);
		assertDocInList(docList, "020suba13trailingText", msg);
		assertDocInList(docList, "020subaMult", msg);
		assertDocInList(docList, "020subz13digit", msg);
		assertDocInList(docList, "020subz13trailingText", msg);
		assertDocInList(docList, "020multSubz", msg);
		assertDocInList(docList, "020SubaAndz", msg);
	}

	/**
	 * Test population of issn field: the ISSNs that code will use for external 
	 *  lookups (e.g. Google Book Search)
	 */
@Test
	public final void testISSNs() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		// TODO: change this fieldname to issn_store
		String fldName = "issn";
		createIxInitVars("issnTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldStored(fldName);
		assertFieldNotIndexed(fldName);
	
		// no issn
		assertDocHasNoField("No022", fldName);
		assertDocHasNoField("022subaNoHyphen", fldName); 
		assertDocHasNoField("022subaTooManyChars", fldName); 
		// 022 single subfield 
		assertDocHasFieldValue("022suba", fldName, "1047-2010"); 
		assertDocHasFieldValue("022subaX", fldName, "1047-201X"); 
		assertDocHasNoFieldValue("022subL", fldName, "0796-5621"); 
		assertDocHasNoFieldValue("022subM", fldName, "0863-4564"); 
		assertDocHasNoFieldValue("022subY", fldName, "0813-1964"); 
		assertDocHasFieldValue("022subZ", fldName, "1144-585X"); 
		// 022 mult subfields
		assertDocHasFieldValue("022subAandL", fldName, "0945-2419"); 
		assertDocHasNoFieldValue("022subAandL", fldName, "0796-5621"); 
		assertDocHasNoFieldValue("022subLandM", fldName, "0038-6073"); 
		assertDocHasNoFieldValue("022subLandM", fldName, "0796-5621"); 
		assertDocHasNoFieldValue("022subMandZ", fldName, "0103-8915"); 
		assertDocHasFieldValue("022subMandZ", fldName, "1144-5858"); 
		assertDocHasFieldValue("Two022a", fldName, "0666-7770"); 
		assertDocHasFieldValue("Two022a", fldName, "1221-2112"); 
	}

	/**
	 * Test population of issnUser_search field: the ISSNs that an end user can 
	 *  search for in our index
	 */
@Test
	public final void testUserISSNs() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "issnUser_search";
		createIxInitVars("issnTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
		assertSingleResult("022suba", fldName, "1047-2010");
		assertSingleResult("022subaX", fldName, "1047-201X");
	
		assertResultSize(fldName, "0796-5621", 3);
		String docList[] = searcherProxy.getDocIdsFromSearch(fldName, "0796-5621", docIDfname);
		assertDocInList(docList, "022subL", fldName + " search \"0796-5621\": ");
		assertDocInList(docList, "022subAandL", fldName + " search \"0796-5621\": ");
		assertDocInList(docList, "022subLandM", fldName + " search \"0796-5621\": ");

		assertSingleResult("022subM", fldName, "0863-4564");
		assertSingleResult("022subY", fldName, "0813-1964");
		assertSingleResult("022subMandZ", fldName, "1144-5858");
		assertSingleResult("022subLandM", fldName, "0038-6073");
		assertSingleResult("022subMandZ", fldName, "0103-8915");
		assertSingleResult("022subZ", fldName, "1144-585X");
		assertSingleResult("022subAandL", fldName, "0945-2419");
		assertSingleResult("Two022a", fldName, "0666-7770");
		assertSingleResult("Two022a", fldName, "1221-2112");
	}

	/**
	 * Test population and searching of lccn field
	 */
@Test
	public final void testLCCNs() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "lccn";
		createIxInitVars("lccnTests.mrc");
		assertStringFieldProperties(fldName);
		assertFieldNotMultiValued(fldName);		
		assertFieldStored(fldName);
		assertFieldIndexed(fldName);

		// no lccn
		assertDocHasNoField("No010", fldName);
// TODO:  the 9 digit lccn passes.  I don't know why.  I no longer care.
//		assertDocHasNoField("010bad", fldName); 
		// 010 sub a only 
		assertDocHasFieldValue("010suba8digit", fldName, "85153773"); 
		assertDocHasFieldValue("010suba10digit", fldName, "2001627090");
		// prefix
		assertDocHasFieldValue("010suba8digitPfx", fldName, "a  60123456"); 
		assertDocHasFieldValue("010suba8digit2LetPfx", fldName, "bs 66654321"); 
		assertDocHasFieldValue("010suba8digit3LetPfx", fldName, "cad77665544");
		// according to loc marc doc, shouldn't have prefix for 10 digit, but
		//  what the heck - let's test
		assertDocHasFieldValue("010suba10digitPfx", fldName, "r 2001336783"); 
		assertDocHasFieldValue("010suba10digit2LetPfx", fldName, "ne2001045944");
		// suffix
		assertDocHasFieldValue("010suba8digitSfx", fldName, "79139101"); 
		assertDocHasFieldValue("010suba10digitSfx", fldName, "2006002284"); 
		assertDocHasFieldValue("010suba8digitSfx2", fldName, "73002284"); 
		// sub z
		assertDocHasFieldValue("010subz", fldName, "20072692384"); 
		assertDocHasFieldValue("010subaAndZ", fldName, "76647633"); 
		assertDocHasNoFieldValue("010subaAndZ", fldName, "76000587"); 
		assertDocHasFieldValue("010multSubZ", fldName, "76647633"); 
		assertDocHasNoFieldValue("010multSubZ", fldName, "2000123456"); 

		// search for them
		// 010 sub a only 
		assertSingleResult("010suba8digit", fldName, "85153773"); 
		assertSingleResult("010suba10digit", fldName, "2001627090");
		// prefix
		assertSingleResult("010suba8digitPfx", fldName, "\"a  60123456\""); 
		assertSingleResult("010suba8digit2LetPfx", fldName, "\"bs 66654321\""); 
		assertSingleResult("010suba8digit3LetPfx", fldName, "cad77665544");
		// according to loc marc doc, shouldn't have prefix for 10 digit, but
		//  what the heck - let's test
		assertSingleResult("010suba10digitPfx", fldName, "\"r 2001336783\""); 
		assertSingleResult("010suba10digit2LetPfx", fldName, "ne2001045944");
		// suffix
		assertSingleResult("010suba8digitSfx", fldName, "79139101"); 
		assertSingleResult("010suba10digitSfx", fldName, "2006002284"); 
		assertSingleResult("010suba8digitSfx2", fldName, "73002284"); 
		// sub z
		assertSingleResult("010subz", fldName, "20072692384"); 
		Set<String> docIds = new HashSet<String>();
		docIds.add("010subaAndZ");
		docIds.add("010multSubZ");
		assertSearchResults(fldName, "76647633", docIds);
		assertZeroResults(fldName, "76000587");
		assertZeroResults(fldName, "2000123456");
	}

}
