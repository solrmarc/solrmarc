package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's handling of vernacular fields (880s)
 *  (non-search vernacular fields, that is)
 * 
 * @author Naomi Dushay
 */
public class VernFieldsTests extends AbstractStanfordBlacklightTest {

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("vernacularNonSearchTests.mrc");
		mappingTestInit();
	}


	/**
	 * Test 880 field that's not used for display
	 */
@Test
	public final void testIgnored880() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("allVern", "toc_search", "contents");
		assertDocHasNoField("allVern", "vern_toc_search");
		assertDocHasNoField("allVern", "vern_toc_display");
	}

	/**
	 * Test multiple occurrences of same field
	 */
@Test
	public final void testFieldDups() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_7xx_search";
		assertSingleResult("two700", fldName, "\"first 700\"");

		fldName = "vern_author_7xx_search";
		assertSingleResult("two700", fldName, "\"vernacular first 700\"");
		assertSingleResult("two700", fldName, "\"vernacular second 700\"");
	}
	
	/**
	 * Test multiple occurrences of same subfield
	 */
@Test
	public final void testSubFieldDups() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_8xx_search";
		assertSingleResult("DupSubflds", fldName, "\"Wellington, New Zealand\"");
		fldName = "vern_author_8xx_search";
		assertSingleResult("DupSubflds", fldName, "\"Naomi in Wellington, in New Zealand\"");
	}

	/**
	 * Test trailing punctuation removal
	 */
@Test
	public final void testTrailingPunct() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_person_display";
		assertDocHasFieldValue("trailingPunct", fldName, "internal colon : ending period");
		assertDocHasNoFieldValue("trailingPunct", fldName, "internal colon : ending period.");
		fldName = "vern_author_person_display";

		assertDocHasNoFieldValue("trailingPunct", fldName, "vernacular internal colon : vernacular ending period.");
		assertDocHasFieldValue("trailingPunct", fldName, "vernacular internal colon : vernacular ending period");

		fldName = "title_display";
		assertDocHasFieldValue("trailingPunct", fldName, "ends in slash");
		assertDocHasNoFieldValue("trailingPunct", fldName, "ends in slash /");
		fldName = "vern_title_display";
		assertDocHasFieldValue("trailingPunct", fldName, "vernacular ends in slash");
		assertDocHasNoFieldValue("trailingPunct", fldName, "vernacular ends in slash /");
	}
	
	/**
	 * Test indexing of unmatched 880s with getLinkedField method
	 */
@Test
	public final void testUnmatched880sLinkedField() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_toc_search";
	    createIxInitVars("unmatched880sTests.mrc");

	    Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("2");
		docIds.add("3");
		assertSearchResults(fldName, "vern505a", docIds);

		// subfields to be ignored
        assertZeroResults(fldName, "vern505b");
        assertZeroResults(fldName, "vern505c");
        assertZeroResults(fldName, "vern505g");
	}

	/**
	 * Test indexing of unmatched 880s using Stanford's getVernacular method
	 */
@Test
	public final void testUnmatched880sVernacular() 
	{
		String fldName = "vern_title_uniform_display";
	    String testFilePath = testDataParentPath + File.separator + "unmatched880sTests.mrc";
		solrFldMapTest.assertSolrFldValue(testFilePath, "4", fldName, "vern130a");
		solrFldMapTest.assertSolrFldValue(testFilePath, "5", fldName, "vern240a");
	}
	
	/**
	 * Test indexing of unmatched 880s using Stanford's getVernAllAlphaExcept method
	 */
@Test
	public final void testUnmatched880sVernAllAlphaExcept() 
	{
		String fldName = "vern_topic_search";
	    String testFilePath = testDataParentPath + File.separator + "unmatched880sTests.mrc";
		solrFldMapTest.assertSolrFldValue(testFilePath, "6", fldName, "vern650a");
	}
	
	/**
	 * Test indexing of unmatched 880s using Stanford's vernRemoveTrailingPunct method
	 */
@Test
	public final void testUnmatched880sVernRemovePunct() 
	{
		String fldName = "vern_author_person_display";
	    String testFilePath = testDataParentPath + File.separator + "unmatched880sTests.mrc";
		solrFldMapTest.assertSolrFldValue(testFilePath, "7", fldName, "vern100a");
	}

	/**
	 * Test record with chinese vernacular
	 */
// @Test
	public final void testChinese() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_person_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao, Qian, 1910-");
		fldName = "vern_author_person_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾, 1910-");
		fldName = "author_person_full_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao, Qian, 1910-");
		fldName = "vern_author_person_full_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾, 1910-");
	
		fldName = "title_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao Qian shu xin ji");
		fldName = "vern_title_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾书信集 / ");
		fldName = "title_full_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao Qian shu xin ji / [Bian zhe Fu Guangming ; ze ren bian ji Zhang Yulin].");
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾书信集 / [编者傅光明;责任编辑张玉林].");

		fldName = "edition_display";
		assertDocHasFieldValue("4160530", fldName, "Di 1 ban.");
		fldName = "vern_edition_display";
		assertDocHasFieldValue("4160530", fldName, "第1版.");

		fldName = "publication_display";
		assertDocHasFieldValue("4160530", fldName, "[Zhengzhou] : Henan jiao yu chu ban she : Henan sheng xin hua shu dian fa xing, 1991.");
		fldName = "vern_publication_display";
		assertDocHasFieldValue("4160530", fldName, "[郑州]: 河南教育出版社: 河南省新华书店发行, 1991.");
	}

	/**
	 * Test right to left concatenation of subfields for right to left languages
	 */
// @Test
	public final void testR2LConcat() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_full_display";
		assertDocHasFieldValue("RtoL", fldName, "a is for alligator / c is for crocodile, 1980");
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("RtoL", fldName, "1980 ,crocodile for is c / alligator for is a");
		
		
		fldName = "author_person_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "LTR a : LTR b, LTR c");
		fldName = "vern_author_person_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "vern (RTL?) c (third) ,vern (RTL?) b (second) : vern (RTL?) a (first)");
		fldName = "title_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "a first / c second, 1980");
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "1980 ,vern (RTL?) c followed by number / vern (RTL?) a");
	}

	/**
	 * Test punctuation changes for right to left concatenation of subfields for
	 * right to left languages
	 */
// @Test
	public final void testR2LConcatPunct() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		org.junit.Assert.fail("not yet implemented");
		String fldName = "";
		assertDocHasFieldValue("2099904", fldName, "");
	}

	/**
	 * Test right to left concatenation of subfields for hebrew
	 */
// @Test
	public final void testHebrew() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_full_display";
		assertDocHasFieldValue("hebrew1", fldName, "Alef bet shel Yahadut.");
		fldName = "vern_title_full_display";
		
PrintStream ps = new PrintStream(System.out, true, "UTF-16");		
ps.println("DEBUG:  vern_title_full_display contains: " + getDocument("hebrew1").getFirstValue("vern_full_title_display"));		
								assertDocHasFieldValue("hebrew1", fldName, "אל״ף בי״ת של יהדות הלל צייטלין ; תירגם וערך מנחם ברש־רועי /");
								assertDocHasFieldValue("hebrew1", fldName, "אל״ף בי״ת של יהדות / הלל צייטלין ; תירגם וערך מנחם ברש־רועי");
		
		fldName = "publication_display";
		assertDocHasFieldValue("hebrew1", fldName, "Yerushalayim : Mosad ha-Rav Ḳuḳ, c1983");
		fldName = "vern_publication_display";
								assertDocHasFieldValue("hebrew1", fldName, "c1983 ,ירושלים : מוסד הרב קוק");
	}

}
