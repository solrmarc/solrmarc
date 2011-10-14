package edu.stanford;

import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

import org.solrmarc.testUtils.IndexTest;


/**
 * junit4 tests for Stanford University's handling of vernacular fields (880s)
 * 
 * @author Naomi Dushay
 */
public class VernacularTests extends AbstractStanfordVufindTest {

	private final String testDataFname = "vernacularTests.mrc";
	
@Before
	public final void initVars() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}

	/**
	 * basic test of the 880 fields needed for display
	 * 
	 * Fields currently used in search results or record display, making them
	 *      the candidates for vernacular fields.
	 *  
	 *  author = custom, removeTrailingPunct(100abcdq, [\\\\,/;:], ([A-Za-z]{4}|[0-9]{3}|\\)|\\,) )
	 *  creator_display = 100abcdefghijklmnopqrstuvwxyz
	 *  corp_author_display = 110abcdefghijklmnopqrstuvwxyz
	 *  meeting_display = 111abcdefghijklmnopqrstuvwxyz
	 *  
	 *  full_title_display = 245abcdefghijklmnopqrstuvwxyz
	 *  brief_title_display = custom, removeTrailingPunct(245abdefghijklmnopqrstuvwxyz)
	 *  uniform_title_display = 130abcdefghijklmnopqrstuvwxyz:240abcdefghijklmnopqrstuvwxyz, first
	 *  variant_title_display = 246abcdefghijklmnopqrstuvwxyz
	 *  
	 *  edition = 250ab
	 *  publication_display = 260abcefg
	 *     would be publishDate if we took it from the 260?
	 *  physical = 300abcefg
	 *  
	 *  series_title_display = 440anpv
	 *  series_display = 490av
	 */
@Test
	public final void testBasicMapping() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author";
		assertDocHasFieldValue("allVern", fldName, "personal name author");
		fldName = "vern_author";
		assertDocHasFieldValue("allVern", fldName, "vernacular personal name author");
		fldName = "creator_display";
		assertDocHasFieldValue("allVern", fldName, "personal name author miscellaneous author info");
		fldName = "vern_creator_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular personal name author vernacular miscellaneous author info");
		fldName = "corp_author_display";
		assertDocHasFieldValue("allVern", fldName, "corp name author");
		fldName = "vern_corp_author_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular corp name author");
		fldName = "meeting_display";
		assertDocHasFieldValue("allVern", fldName, "mtg name author");
		fldName = "vern_meeting_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular mtg name author");
	
		fldName = "brief_title_display";
		assertDocHasFieldValue("allVern", fldName, "title 245");
		fldName = "vern_brief_title_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular title 245");
		fldName = "full_title_display";
		assertDocHasFieldValue("allVern", fldName, "title 245 [subfield c].");
		fldName = "vern_full_title_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular title 245 [vernacular subfield c].");
		fldName = "variant_title_display";
		assertDocHasFieldValue("allVern", fldName, "variant title");
		fldName = "vern_variant_title_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular variant title");

		fldName = "edition";
		assertDocHasFieldValue("allVern", fldName, "edition");
		fldName = "vern_edition";
		assertDocHasFieldValue("allVern", fldName, "vernacular edition");
		fldName = "publication_display";
		assertDocHasFieldValue("allVern", fldName, "publication");
		fldName = "vern_publication_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular publication");
		fldName = "physical";
		assertDocHasFieldValue("allVern", fldName, "Let's get physical, physical!");
		fldName = "vern_physical";
		assertDocHasFieldValue("allVern", fldName, "vernacular Let's get physical, physical!");
		fldName = "series_title_display";
		assertDocHasFieldValue("allVern", fldName, "series title");
		fldName = "vern_series_title_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular series title");
		fldName = "series_display";
		assertDocHasFieldValue("allVern", fldName, "just the series, ma'am");
		fldName = "vern_series_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular just the series, ma'am");
	}


	/**
	 * Test 880 field that's not used for display
	 */
@Test
	public final void testIgnored880() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "contents";
		assertDocHasFieldValue("allVern", fldName, "contents");
		assertZeroResults(fldName, "vernacular contents");
	}

	/**
	 * Test multiple occurences of same field
	 *  uniform_title_display = 130abcdefghijklmnopqrstuvwxyz:240abcdefghijklmnopqrstuvwxyz, first
	 */
@Test
	public final void testUniformTitle() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "uniform_title_display";
		assertDocHasFieldValue("130only", fldName, "main entry uniform title");
		fldName = "vern_uniform_title_display";
		assertDocHasFieldValue("130only", fldName, "vernacular main entry uniform title");		

		// 240 is no longer in uniform title (due to title_sort being 130 245)
		fldName = "uniform_title_display";
		assertDocHasNoFieldValue("240only", fldName, "uniform title");
		fldName = "vern_uniform_title_display";
		assertDocHasNoFieldValue("240only", fldName, "vernacular uniform title");		
	}
	
	/**
	 * Test multiple occurences of same field
	 */
@Test
	public final void testFieldDups() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "variant_title_display";
		assertDocHasFieldValue("two246", fldName, "first 246");
		assertDocHasFieldValue("two246", fldName, "second 246");
		fldName = "vern_variant_title_display";
		assertDocHasFieldValue("two246", fldName, "vernacular first 246");
		assertDocHasFieldValue("two246", fldName, "vernacular second 246");
	}
	
	/**
	 * Test multiple occurences of same subfield
	 */
@Test
	public final void testSubFieldDups() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "publication_display";
		assertDocHasFieldValue("DupSubflds", fldName, "Wellington, New Zealand");
		fldName = "vern_publication_display";
		assertDocHasFieldValue("DupSubflds", fldName, "Naomi in Wellington, in New Zealand");
	}

	/**
	 * Test trailing punctuation removal
	 */
@Test
	public final void testTrailingPunct() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author";
		assertDocHasFieldValue("trailingPunct", fldName, "internal colon : ending period");
		assertDocHasNoFieldValue("trailingPunct", fldName, "internal colon : ending period.");
		fldName = "vern_author";
		assertDocHasFieldValue("trailingPunct", fldName, "vernacular internal colon : vernacular ending period");
		assertDocHasNoFieldValue("trailingPunct", fldName, "vernacular internal colon : vernacular ending period.");

		fldName = "brief_title_display";
		assertDocHasFieldValue("trailingPunct", fldName, "ends in slash");
		assertDocHasNoFieldValue("trailingPunct", fldName, "ends in slash /");
		fldName = "vern_brief_title_display";
		assertDocHasFieldValue("trailingPunct", fldName, "vernacular ends in slash");
		assertDocHasNoFieldValue("trailingPunct", fldName, "vernacular ends in slash /");
	}
	
	/**
	 * Test record with chinese vernacular
	 */
//@Test
	public final void testChinese() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author";
		assertDocHasFieldValue("4160530", fldName, "Xiao, Qian, 1910-");
		fldName = "vern_author";
		assertDocHasFieldValue("4160530", fldName, "萧乾, 1910-");
		fldName = "creator_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao, Qian, 1910-");
		fldName = "vern_creator_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾, 1910-");
	
		fldName = "brief_title_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao Qian shu xin ji");
		fldName = "vern_brief_title_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾书信集 / ");
		fldName = "full_title_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao Qian shu xin ji / [Bian zhe Fu Guangming ; ze ren bian ji Zhang Yulin].");
		fldName = "vern_full_title_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾书信集 / [编者傅光明;责任编辑张玉林].");

		fldName = "edition";
		assertDocHasFieldValue("4160530", fldName, "Di 1 ban.");
		fldName = "vern_edition";
		assertDocHasFieldValue("4160530", fldName, "第1版.");

		fldName = "publication_display";
		assertDocHasFieldValue("4160530", fldName, "[Zhengzhou] : Henan jiao yu chu ban she : Henan sheng xin hua shu dian fa xing, 1991.");
		fldName = "vern_publication_display";
		assertDocHasFieldValue("4160530", fldName, "[郑州]: 河南教育出版社: 河南省新华书店发行, 1991.");
	}

	/**
	 * Test right to left concatenation of subfields for right to left languages
	 */
//@Test
	public final void testR2LConcat() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "full_title_display";
		assertDocHasFieldValue("RtoL", fldName, "a is for alligator / c is for crocodile, 1980");
		fldName = "vern_full_title_display";
		assertDocHasFieldValue("RtoL", fldName, "1980 ,crocodile for is c / alligator for is a");
		
		
		fldName = "creator_display";
		assertDocHasFieldValue("RtoL2", fldName, "LTR a : LTR b, LTR c");
		fldName = "vern_creator_display";
		assertDocHasFieldValue("RtoL2", fldName, "vern (RTL?) c (third) ,vern (RTL?) b (second) : vern (RTL?) a (first)");
		fldName = "full_title_display";
		assertDocHasFieldValue("RtoL2", fldName, "a first / c second, 1980");
		fldName = "vern_full_title_display";
		assertDocHasFieldValue("RtoL2", fldName, "1980 ,vern (RTL?) c followed by number / vern (RTL?) a");
	}

	/**
	 * Test punctuation changes for right to left concatenation of subfields for
	 *  right to left languages
	 */
//@Test
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
//@Test
	public final void testHebrew() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "full_title_display";
		assertDocHasFieldValue("hebrew1", fldName, "Alef bet shel Yahadut.");
		fldName = "vern_full_title_display";
		
//PrintStream ps = new PrintStream(System.out, true, "UTF-16");		
//ps.println("DEBUG:  vern_full_title_display contains: " + getDocument("hebrew1").getValues("vern_full_title_display")[0]);		
//								assertDocHasFieldValue("hebrew1", fldName, "אל״ף בי״ת של יהדות הלל צייטלין ; תירגם וערך מנחם ברש־רועי /");
//								assertDocHasFieldValue("hebrew1", fldName, "אל״ף בי״ת של יהדות / הלל צייטלין ; תירגם וערך מנחם ברש־רועי");
//		
		fldName = "publication_display";
		assertDocHasFieldValue("hebrew1", fldName, "Yerushalayim : Mosad ha-Rav Ḳuḳ, c1983");
		fldName = "vern_publication_display";
								assertDocHasFieldValue("hebrew1", fldName, "c1983 ,ירושלים : מוסד הרב קוק");
	}

}
