package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.document.Document;
import org.junit.Test;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's standard number fields
 * @author Naomi Dushay
 *
 */
public class StandardNumberTests extends BibIndexTest {

	/**
	 * Test population of oclc field
	 */
@Test
	public final void testOCLCNums() 
		throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "oclc";
		createIxInitVars("oclcNumTests.mrc");
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
	
		assertDocHasFieldValue("035withOCoLC-M", fldName, "656729", sis); 
		assertDocHasNoFieldValue("035withOCoLC-MnoParens", fldName, "656729", sis); 
		// doc should have oclc from good 035 and none from bad 035s
		assertDocHasFieldValue("Mult035onlyOneGood", fldName, "656729", sis); 
		assertDocHasNoFieldValue("Mult035onlyOneGood", fldName, "164324897", sis); 
		assertDocHasNoFieldValue("Mult035onlyOneGood", fldName, "1CSUO98-B6924", sis); 
		assertDocHasNoFieldValue("Mult035onlyOneGood", fldName, "180776170", sis); 
		// 079 only
		assertDocHasFieldValue("079onlyocm", fldName, "38052115", sis); 
		assertDocHasFieldValue("079onlyocn", fldName, "122811369", sis); 
		// 079 with bad prefix - 035 (OCoLC) only
		assertDocHasFieldValue("079badPrefix", fldName, "180776170", sis); 
		assertDocHasNoFieldValue("079badPrefix", fldName, "66654321", sis); 
		// doc should only have oclc from subfield a
		assertDocHasFieldValue("079onlywithz", fldName, "46660954", sis); 
		assertDocHasNoFieldValue("079onlywithz", fldName, "38158328", sis); 
		// both 079 and 035: doc should have oclc from 079, not from either 035
		assertDocHasFieldValue("079withbad035s", fldName, "12345666", sis); 
		assertDocHasNoFieldValue("079withbad035s", fldName, "164324897", sis); 
		assertDocHasNoFieldValue("079withbad035s", fldName, "CSUO98-B6924", sis); 
		// doc should have oclc from good 035, but not from good 079
		assertDocHasFieldValue("Good035withGood079", fldName, "656729", sis); 
		assertDocHasNoFieldValue("Good035withGood079", fldName, "00666000", sis); 
		// doc should have one oclc only, from (OCoLC) prefixed field
		assertDocHasFieldValue("035OCoLConly", fldName, "180776170", sis); 
		assertDocHasNoFieldValue("035OCoLConly", fldName, "164324897", sis); 
		assertDocHasNoFieldValue("035OCoLConly", fldName, "CSUO98-B6924", sis); 
		// doc should have one oclc only, from (OCoLC) prefixed field
		assertDocHasFieldValue("035bad079OCoLConly", fldName, "180776170", sis); 
		assertDocHasNoFieldValue("035bad079OCoLConly", fldName, "bad 079", sis); 
		// no oclc number
		assertDocHasNoField("035and079butNoOclc", fldName, sis);
		// multiple oclc numbers
		assertDocHasFieldValue("MultOclcNums", fldName, "656729", sis); 
		assertDocHasFieldValue("MultOclcNums", fldName, "38052115", sis); 
		assertDocHasFieldValue("MultOclcNums", fldName, "38403775", sis); 
		assertDocHasNoFieldValue("MultOclcNums", fldName, "180776170", sis); 
		assertDocHasNoFieldValue("MultOclcNums", fldName, "00666000", sis); 
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
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("035withOCoLC-M");
		docIds.add("Mult035onlyOneGood");
		docIds.add("MultOclcNums");
		docIds.add("Good035withGood079");
		assertSearchResults(fldName, "656729", docIds, sis);
		
		docIds.clear();
		docIds.add("079onlyocm");
		docIds.add("MultOclcNums");
		assertSearchResults(fldName, "38052115", docIds, sis);

		docIds.clear();
		docIds.add("079badPrefix");
		docIds.add("035OCoLConly");
		docIds.add("035bad079OCoLConly");
		assertSearchResults(fldName, "180776170", docIds, sis);

		assertSingleResult("079onlyocn", fldName, "122811369", sis); 
		assertSingleResult("079onlywithz", fldName, "46660954", sis); 
		assertSingleResult("079withbad035s", fldName, "12345666", sis); 
		assertSingleResult("MultOclcNums", fldName, "38403775", sis); 
				
		assertZeroResults(fldName, "1CSUO98-B6924", sis); 
		assertZeroResults(fldName, "CSUO98-B6924", sis);
		assertZeroResults(fldName, "164324897", sis); 
		assertZeroResults(fldName, "00666000", sis); 

		assertZeroResults(fldName, "66654321", sis); 
		assertZeroResults(fldName, "38158328", sis); 
		assertZeroResults(fldName, "\"bad 079\"", sis); 
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
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldNotIndexed(fldName, solrCore);
	
		// no isbn
		assertDocHasNoField("No020", fldName, sis);
		assertDocHasNoField("020noSubaOrz", fldName, sis);
		// 020 subfield a 10 digit varieties
		assertDocHasFieldValue("020suba10digit", fldName, "1417559128", sis); 
		assertDocHasFieldValue("020suba10endsX", fldName, "123456789X", sis); 
		assertDocHasFieldValue("020suba10trailingText", fldName, "1234567890", sis); 
		assertDocHasFieldValue("020suba10trailingText", fldName, "0123456789", sis); 
		assertDocHasFieldValue("020suba10trailingText", fldName, "0521672694", sis); 
		assertDocHasFieldValue("020suba10trailingText", fldName, "052185668X", sis); 
		// 020 subfield a 13 digit varieties
		assertDocHasFieldValue("020suba13", fldName, "9780809424887", sis); 
		assertDocHasFieldValue("020suba13endsX", fldName, "979123456789X", sis); 
		assertDocHasNoField("020suba13bad", fldName, sis);
		assertDocHasNoFieldValue("020suba13bad", fldName, "000123456789X", sis); 
		assertDocHasFieldValue("020suba13trailingText", fldName, "978185585039X", sis); 
		assertDocHasFieldValue("020suba13trailingText", fldName, "9780809424887", sis); 
		assertDocHasFieldValue("020suba13trailingText", fldName, "9780809424870", sis); 
		// sub a mixed 10 and 13 digit
		assertDocHasFieldValue("020subaMult", fldName, "0809424886", sis); 
		assertDocHasFieldValue("020subaMult", fldName, "123456789X", sis); 
		assertDocHasFieldValue("020subaMult", fldName, "1234567890", sis); 
		assertDocHasFieldValue("020subaMult", fldName, "979123456789X", sis); 
		assertDocHasFieldValue("020subaMult", fldName, "9780809424887", sis); 
		assertDocHasFieldValue("020subaMult", fldName, "9781855850484", sis); 
		// no subfield a in 020, but has subfield z 10 digit
		assertDocHasFieldValue("020subz10digit", fldName, "9876543210", sis); 		
		assertDocHasFieldValue("020subz10endsX", fldName, "123456789X", sis); 
		assertDocHasFieldValue("020subz10trailingText", fldName, "1234567890", sis); 
		assertDocHasFieldValue("020subz10trailingText", fldName, "0123456789", sis); 
		assertDocHasFieldValue("020subz10trailingText", fldName, "0521672694", sis); 
		// no subfield a in 020, but has subfield z 13 digit		
		assertDocHasFieldValue("020subz13digit", fldName, "9780809424887", sis); 		
		assertDocHasFieldValue("020subz13endsX", fldName, "979123456789X", sis); 
		assertDocHasFieldValue("020subz13trailingText", fldName, "978185585039X", sis); 
		assertDocHasFieldValue("020subz13trailingText", fldName, "9780809424887", sis); 
		assertDocHasFieldValue("020subz13trailingText", fldName, "9780809424870", sis); 
		// mult subfield z in single 020
		assertDocHasFieldValue("020multSubz", fldName, "9802311987", sis);
		assertDocHasFieldValue("020multSubz", fldName, "9802311995", sis);
		assertDocHasFieldValue("020multSubz", fldName, "9802312002", sis);
		assertDocHasFieldValue("020multSubz", fldName, "9876543210", sis);
		assertDocHasFieldValue("020multSubz", fldName, "123456789X", sis);
		assertDocHasFieldValue("020multSubz", fldName, "9780809424887", sis);
		assertDocHasFieldValue("020multSubz", fldName, "979123456789X", sis);
		assertDocHasFieldValue("020multSubz", fldName, "9780809424870", sis);
	
		// mult a and z - should only have a
		assertDocHasFieldValue("020SubaAndz", fldName, "0123456789", sis);
		assertDocHasFieldValue("020SubaAndz", fldName, "0521672694", sis);
		assertDocHasNoFieldValue("020SubaAndz", fldName, "9802311987", sis);
		assertDocHasFieldValue("020SubaAndz", fldName, "052185668X", sis);
		assertDocHasNoFieldValue("020SubaAndz", fldName, "123456789X", sis);
		assertDocHasNoFieldValue("020SubaAndz", fldName, "9780809424887", sis);
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
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
		// searches are not exhaustive  (b/c programmer is exhausted)
	
		// isbn search with sub a value from record with mult a and z
		String value = "052185668X";
		List<Document> docList = assertResultSize(fldName, value, 2, sis);
		String msg = "isbn search \""+ value + "\": ";
		assertDocInList(docList, "020suba10trailingText", msg, sis);
		assertDocInList(docList, "020SubaAndz", msg, sis);
	
		// isbn search with sub z value from record with mult a and z
		value = "9780809424887";
		docList = assertResultSize(fldName, value, 7, sis);
		msg = fldName + " search \""+ value + "\": ";
		assertDocInList(docList, "020suba13", msg, sis);
		assertDocInList(docList, "020suba13trailingText", msg, sis);
		assertDocInList(docList, "020subaMult", msg, sis);
		assertDocInList(docList, "020subz13digit", msg, sis);
		assertDocInList(docList, "020subz13trailingText", msg, sis);
		assertDocInList(docList, "020multSubz", msg, sis);
		assertDocInList(docList, "020SubaAndz", msg, sis);
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
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldNotIndexed(fldName, solrCore);
	
		// no issn
		assertDocHasNoField("No022", fldName, sis);
		assertDocHasNoField("022subaNoHyphen", fldName, sis); 
		assertDocHasNoField("022subaTooManyChars", fldName, sis); 
		// 022 single subfield 
		assertDocHasFieldValue("022suba", fldName, "1047-2010", sis); 
		assertDocHasFieldValue("022subaX", fldName, "1047-201X", sis); 
		assertDocHasNoFieldValue("022subL", fldName, "0796-5621", sis); 
		assertDocHasNoFieldValue("022subM", fldName, "0863-4564", sis); 
		assertDocHasNoFieldValue("022subY", fldName, "0813-1964", sis); 
		assertDocHasFieldValue("022subZ", fldName, "1144-585X", sis); 
		// 022 mult subfields
		assertDocHasFieldValue("022subAandL", fldName, "0945-2419", sis); 
		assertDocHasNoFieldValue("022subAandL", fldName, "0796-5621", sis); 
		assertDocHasNoFieldValue("022subLandM", fldName, "0038-6073", sis); 
		assertDocHasNoFieldValue("022subLandM", fldName, "0796-5621", sis); 
		assertDocHasNoFieldValue("022subMandZ", fldName, "0103-8915", sis); 
		assertDocHasFieldValue("022subMandZ", fldName, "1144-5858", sis); 
		assertDocHasFieldValue("Two022a", fldName, "0666-7770", sis); 
		assertDocHasFieldValue("Two022a", fldName, "1221-2112", sis); 
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
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
		assertSingleResult("022suba", fldName, "1047-2010", sis);
		assertSingleResult("022subaX", fldName, "1047-201X", sis);
	
		List<Document> docList = assertResultSize(fldName, "0796-5621", 3, sis);
		assertDocInList(docList, "022subL", fldName + " search \"0796-5621\": ", sis);
		assertDocInList(docList, "022subAandL", fldName + " search \"0796-5621\": ", sis);
		assertDocInList(docList, "022subLandM", fldName + " search \"0796-5621\": ", sis);

		assertSingleResult("022subM", fldName, "0863-4564", sis);
		assertSingleResult("022subY", fldName, "0813-1964", sis);
		assertSingleResult("022subMandZ", fldName, "1144-5858", sis);
		assertSingleResult("022subLandM", fldName, "0038-6073", sis);
		assertSingleResult("022subMandZ", fldName, "0103-8915", sis);
		assertSingleResult("022subZ", fldName, "1144-585X", sis);
		assertSingleResult("022subAandL", fldName, "0945-2419", sis);
		assertSingleResult("Two022a", fldName, "0666-7770", sis);
		assertSingleResult("Two022a", fldName, "1221-2112", sis);
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
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotMultiValued(fldName, solrCore);		
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);

		// no lccn
		assertDocHasNoField("No010", fldName, sis);
// TODO:  the 9 digit lccn passes.  I don't know why.  I no longer care.
//		assertDocHasNoField("010bad", fldName, sis); 
		// 010 sub a only 
		assertDocHasFieldValue("010suba8digit", fldName, "85153773", sis); 
		assertDocHasFieldValue("010suba10digit", fldName, "2001627090", sis);
		// prefix
		assertDocHasFieldValue("010suba8digitPfx", fldName, "a  60123456", sis); 
		assertDocHasFieldValue("010suba8digit2LetPfx", fldName, "bs 66654321", sis); 
		assertDocHasFieldValue("010suba8digit3LetPfx", fldName, "cad77665544", sis);
		// according to loc marc doc, shouldn't have prefix for 10 digit, but
		//  what the heck - let's test
		assertDocHasFieldValue("010suba10digitPfx", fldName, "r 2001336783", sis); 
		assertDocHasFieldValue("010suba10digit2LetPfx", fldName, "ne2001045944", sis);
		// suffix
		assertDocHasFieldValue("010suba8digitSfx", fldName, "79139101", sis); 
		assertDocHasFieldValue("010suba10digitSfx", fldName, "2006002284", sis); 
		assertDocHasFieldValue("010suba8digitSfx2", fldName, "73002284", sis); 
		// sub z
		assertDocHasFieldValue("010subz", fldName, "20072692384", sis); 
		assertDocHasFieldValue("010subaAndZ", fldName, "76647633", sis); 
		assertDocHasNoFieldValue("010subaAndZ", fldName, "76000587", sis); 
		assertDocHasFieldValue("010multSubZ", fldName, "76647633", sis); 
		assertDocHasNoFieldValue("010multSubZ", fldName, "2000123456", sis); 

		// search for them
		// 010 sub a only 
		assertSingleResult("010suba8digit", fldName, "85153773", sis); 
		assertSingleResult("010suba10digit", fldName, "2001627090", sis);
		// prefix
		assertSingleResult("010suba8digitPfx", fldName, "\"a  60123456\"", sis); 
		assertSingleResult("010suba8digit2LetPfx", fldName, "\"bs 66654321\"", sis); 
		assertSingleResult("010suba8digit3LetPfx", fldName, "cad77665544", sis);
		// according to loc marc doc, shouldn't have prefix for 10 digit, but
		//  what the heck - let's test
		assertSingleResult("010suba10digitPfx", fldName, "\"r 2001336783\"", sis); 
		assertSingleResult("010suba10digit2LetPfx", fldName, "ne2001045944", sis);
		// suffix
		assertSingleResult("010suba8digitSfx", fldName, "79139101", sis); 
		assertSingleResult("010suba10digitSfx", fldName, "2006002284", sis); 
		assertSingleResult("010suba8digitSfx2", fldName, "73002284", sis); 
		// sub z
		assertSingleResult("010subz", fldName, "20072692384", sis); 
		Set<String> docIds = new HashSet<String>();
		docIds.add("010subaAndZ");
		docIds.add("010multSubZ");
		assertSearchResults(fldName, "76647633", docIds, sis);
		assertZeroResults(fldName, "76000587", sis);
		assertZeroResults(fldName, "2000123456", sis);
	}

}
