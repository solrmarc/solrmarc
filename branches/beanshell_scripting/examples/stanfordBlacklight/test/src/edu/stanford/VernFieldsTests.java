package edu.stanford;

import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's handling of vernacular fields (880s)
 *  (non-search vernacular fields, that is)
 * 
 * @author Naomi Dushay
 */
public class VernFieldsTests extends BibIndexTest {

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("vernacularNonSearchTests.mrc");
	}


	/**
	 * Test 880 field that's not used for display
	 */
@Test
	public final void testIgnored880() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("allVern", "toc_search", "contents", sis);
		assertDocHasNoField("allVern", "vern_toc_search", sis);
		assertDocHasNoField("allVern", "vern_toc_display", sis);
	}

	/**
	 * Test multiple occurrences of same field
	 */
@Test
	public final void testFieldDups() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_7xx_search";
		assertSingleResult("two700", fldName, "\"first 700\"", sis);

		fldName = "vern_author_7xx_search";
		assertSingleResult("two700", fldName, "\"vernacular first 700\"", sis);
		assertSingleResult("two700", fldName, "\"vernacular second 700\"", sis);
	}
	
	/**
	 * Test multiple occurrences of same subfield
	 */
@Test
	public final void testSubFieldDups() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_8xx_search";
		assertSingleResult("DupSubflds", fldName, "\"Wellington, New Zealand\"", sis);
		fldName = "vern_author_8xx_search";
		assertSingleResult("DupSubflds", fldName, "\"Naomi in Wellington, in New Zealand\"", sis);
	}

/**
	 * Test trailing punctuation removal
	 */
@Test
	public final void testTrailingPunct() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_person_display";
		assertDocHasFieldValue("trailingPunct", fldName, "internal colon : ending period", sis);
		assertDocHasNoFieldValue("trailingPunct", fldName, "internal colon : ending period.", sis);
		fldName = "vern_author_person_display";
		assertDocHasFieldValue("trailingPunct", fldName, "vernacular internal colon : vernacular ending period", sis);
		assertDocHasNoFieldValue("trailingPunct", fldName, "vernacular internal colon : vernacular ending period.", sis);

		fldName = "title_display";
		assertDocHasFieldValue("trailingPunct", fldName, "ends in slash", sis);
		assertDocHasNoFieldValue("trailingPunct", fldName, "ends in slash /", sis);
		fldName = "vern_title_display";
		assertDocHasFieldValue("trailingPunct", fldName, "vernacular ends in slash", sis);
		assertDocHasNoFieldValue("trailingPunct", fldName, "vernacular ends in slash /", sis);
	}
	
	/**
	 * Test record with chinese vernacular
	 */
// @Test
	public final void testChinese() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_person_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao, Qian, 1910-", sis);
		fldName = "vern_author_person_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾, 1910-", sis);
		fldName = "author_person_full_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao, Qian, 1910-", sis);
		fldName = "vern_author_person_full_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾, 1910-", sis);
	
		fldName = "title_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao Qian shu xin ji", sis);
		fldName = "vern_title_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾书信集 / ", sis);
		fldName = "title_full_display";
		assertDocHasFieldValue("4160530", fldName, "Xiao Qian shu xin ji / [Bian zhe Fu Guangming ; ze ren bian ji Zhang Yulin].", sis);
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("4160530", fldName, "萧乾书信集 / [编者傅光明;责任编辑张玉林].", sis);

		fldName = "edition_display";
		assertDocHasFieldValue("4160530", fldName, "Di 1 ban.", sis);
		fldName = "vern_edition_display";
		assertDocHasFieldValue("4160530", fldName, "第1版.", sis);

		fldName = "publication_display";
		assertDocHasFieldValue("4160530", fldName, "[Zhengzhou] : Henan jiao yu chu ban she : Henan sheng xin hua shu dian fa xing, 1991.", sis);
		fldName = "vern_publication_display";
		assertDocHasFieldValue("4160530", fldName, "[郑州]: 河南教育出版社: 河南省新华书店发行, 1991.", sis);
	}

	/**
	 * Test right to left concatenation of subfields for right to left languages
	 */
// @Test
	public final void testR2LConcat() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_full_display";
		assertDocHasFieldValue("RtoL", fldName, "a is for alligator / c is for crocodile, 1980", sis);
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("RtoL", fldName, "1980 ,crocodile for is c / alligator for is a", sis);
		
		
		fldName = "author_person_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "LTR a : LTR b, LTR c", sis);
		fldName = "vern_author_person_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "vern (RTL?) c (third) ,vern (RTL?) b (second) : vern (RTL?) a (first)", sis);
		fldName = "title_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "a first / c second, 1980", sis);
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "1980 ,vern (RTL?) c followed by number / vern (RTL?) a", sis);
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
		assertDocHasFieldValue("2099904", fldName, "", sis);
	}

	/**
	 * Test right to left concatenation of subfields for hebrew
	 */
// @Test
	public final void testHebrew() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_full_display";
		assertDocHasFieldValue("hebrew1", fldName, "Alef bet shel Yahadut.", sis);
		fldName = "vern_title_full_display";
		
PrintStream ps = new PrintStream(System.out, true, "UTF-16");		
ps.println("DEBUG:  vern_title_full_display contains: " + getDocument("hebrew1", sis).getValues("vern_full_title_display")[0]);		
								assertDocHasFieldValue("hebrew1", fldName, "אל״ף בי״ת של יהדות הלל צייטלין ; תירגם וערך מנחם ברש־רועי /", sis);
								assertDocHasFieldValue("hebrew1", fldName, "אל״ף בי״ת של יהדות / הלל צייטלין ; תירגם וערך מנחם ברש־רועי", sis);
		
		fldName = "publication_display";
		assertDocHasFieldValue("hebrew1", fldName, "Yerushalayim : Mosad ha-Rav Ḳuḳ, c1983", sis);
		fldName = "vern_publication_display";
								assertDocHasFieldValue("hebrew1", fldName, "c1983 ,ירושלים : מוסד הרב קוק", sis);
	}

}
