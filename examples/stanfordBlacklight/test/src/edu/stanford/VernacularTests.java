package edu.stanford;

import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's handling of vernacular fields (880s)
 * 
 * @author Naomi Dushay
 */
public class VernacularTests extends BibIndexTest {

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("vernacularTests.mrc");
	}


	/**
	 * vernacular display fields should have appropriate properties.
	 */
@Test
	public final void testVernacularDisplayFields() 
	    throws ParserConfigurationException, IOException, SAXException
	{
	    String fldName = "vern_author_person_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);	
	    fldName = "vern_author_person_full_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	    fldName = "vern_author_corp_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	    fldName = "vern_author_meeting_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	    fldName = "vern_edition_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	    fldName = "vern_physical_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldMultiValued(fldName, solrCore);
	    fldName = "vern_publication_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldMultiValued(fldName, solrCore);
	    fldName = "vern_title_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	    fldName = "vern_title_full_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	    fldName = "vern_title_uniform_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldNotMultiValued(fldName, solrCore);
	    fldName = "vern_title_variant_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldMultiValued(fldName, solrCore);
	    fldName = "vern_series_title_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldMultiValued(fldName, solrCore);
	    fldName = "vern_series_display";
	    assertDisplayFldProps(fldName, solrCore, sis);
	    assertFieldMultiValued(fldName, solrCore);
	}


	/**
	 * basic test of the 880 fields needed for display
	 */
@Test
	public final void testBasicMapping() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "author_person_display";
		assertDocHasFieldValue("allVern", fldName, "personal name author", sis);
		fldName = "vern_author_person_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular personal name author", sis);
		fldName = "author_person_full_display";
		assertDocHasFieldValue("allVern", fldName, "personal name author miscellaneous author info", sis);
		fldName = "vern_author_person_full_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular personal name author vernacular miscellaneous author info", sis);
		fldName = "author_corp_display";
		assertDocHasFieldValue("allVern", fldName, "corp name author", sis);
		fldName = "vern_author_corp_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular corp name author", sis);
		fldName = "author_meeting_display";
		assertDocHasFieldValue("allVern", fldName, "mtg name author", sis);
		fldName = "vern_author_meeting_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular mtg name author", sis);
	
		fldName = "title_display";
		assertDocHasFieldValue("allVern", fldName, "title 245", sis);
		fldName = "vern_title_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular title 245", sis);
		fldName = "title_full_display";
		assertDocHasFieldValue("allVern", fldName, "title 245 [subfield c].", sis);
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular title 245 [vernacular subfield c].", sis);
		fldName = "title_variant_display";
		assertDocHasFieldValue("allVern", fldName, "variant title", sis);
		fldName = "vern_title_variant_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular variant title", sis);

		fldName = "edition_display";
		assertDocHasFieldValue("allVern", fldName, "edition", sis);
		fldName = "vern_edition_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular edition", sis);
		fldName = "publication_display";
		assertDocHasFieldValue("allVern", fldName, "publication", sis);
		fldName = "vern_publication_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular publication", sis);
		fldName = "physical_display";
		assertDocHasFieldValue("allVern", fldName, "Let's get physical, physical!", sis);
		fldName = "vern_physical_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular Let's get physical, physical!", sis);
		fldName = "series_title_display";
		assertDocHasFieldValue("allVern", fldName, "series title", sis);
		fldName = "vern_series_title_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular series title", sis);
		fldName = "series_display";
		assertDocHasFieldValue("allVern", fldName, "just the series, ma'am", sis);
		fldName = "vern_series_display";
		assertDocHasFieldValue("allVern", fldName, "vernacular just the series, ma'am", sis);
	}


	/**
	 * Test 880 field that's not used for display
	 */
@Test
	public final void testIgnored880() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "toc_search";
		assertSingleResult("allVern", fldName, "contents", sis);
		assertDocHasNoField("allVern", "vern_toc_search", sis);
		assertDocHasNoField("allVern", "vern_toc_display", sis);
	}

	/**
	 * Test multiple occurences of same field uniform_title_display =
	 * 130abcdefghijklmnopqrstuvwxyz:240abcdefghijklmnopqrstuvwxyz, first
	 */
@Test
	public final void testUniformTitle() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_uniform_display";
		assertDocHasFieldValue("130only", fldName, "main entry uniform title", sis);
		fldName = "vern_title_uniform_display";
		assertDocHasFieldValue("130only", fldName, "vernacular main entry uniform title", sis);		

		// 240 is no longer in uniform title (due to title_sort being 130 245)
		fldName = "title_uniform_display";
		assertDocHasNoFieldValue("240only", fldName, "uniform title", sis);
		fldName = "vern_title_uniform_display";
		assertDocHasNoFieldValue("240only", fldName, "vernacular uniform title", sis);		
	}
	
	/**
	 * Test multiple occurences of same field
	 */
@Test
	public final void testFieldDups() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_variant_display";
		assertDocHasFieldValue("two246", fldName, "first 246", sis);
		assertDocHasFieldValue("two246", fldName, "second 246", sis);
		fldName = "vern_title_variant_display";
		assertDocHasFieldValue("two246", fldName, "vernacular first 246", sis);
		assertDocHasFieldValue("two246", fldName, "vernacular second 246", sis);
	}
	
	/**
	 * Test multiple occurences of same subfield
	 */
@Test
	public final void testSubFieldDups() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "publication_display";
		assertDocHasFieldValue("DupSubflds", fldName, "Wellington, New Zealand", sis);
		fldName = "vern_publication_display";
		assertDocHasFieldValue("DupSubflds", fldName, "Naomi in Wellington, in New Zealand", sis);
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
