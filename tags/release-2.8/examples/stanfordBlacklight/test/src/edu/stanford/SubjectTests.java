package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's subject fields
 * @author Naomi Dushay
 */
public class SubjectTests extends AbstractStanfordBlacklightTest {
	
	private final String testDataFname = "subjectTests.mrc";
	private final String searchTestDataFname = "subjectSearchTests.mrc";

	/**
	 * Test population and properties of topic_search field
	 */
@Test
	public final void testTopicSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(searchTestDataFname);
		String fldName = "topic_search";
////		assertSearchFldMultValProps(fldName);

		// all subfields except v, x, y and z from  650, 690, 653, 654
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("650search");
		docIds.add("Vern650search");
		assertSearchResults(fldName, "650a", docIds);
		assertSingleResult("650search", fldName, "650b");
		assertSingleResult("650search", fldName, "650c");
		assertSingleResult("650search", fldName, "650d");
		assertSingleResult("650search", fldName, "650e");		
		assertZeroResults(fldName, "650v");
		assertZeroResults(fldName, "650x");
		assertZeroResults(fldName, "650y");
		assertZeroResults(fldName, "650z");

		docIds.clear();
		docIds.add("690search");
		docIds.add("Vern690search");
		assertSearchResults(fldName, "690a", docIds);
		assertSingleResult("690search", fldName, "690b");
		assertSingleResult("690search", fldName, "690c");
		assertSingleResult("690search", fldName, "690d");
		assertSingleResult("690search", fldName, "690e");		
		assertZeroResults(fldName, "690v");
		assertZeroResults(fldName, "690x");
		assertZeroResults(fldName, "690y");
		assertZeroResults(fldName, "690z");

		docIds.clear();
		docIds.add("653search");
		docIds.add("Vern653search");
		assertSearchResults(fldName, "653a", docIds);
		assertZeroResults(fldName, "653v");
		assertZeroResults(fldName, "653x");
		assertZeroResults(fldName, "653y");
		assertZeroResults(fldName, "653z");

		docIds.clear();
		docIds.add("654search");
		docIds.add("Vern654search");
		assertSearchResults(fldName, "654a", docIds);
		assertSingleResult("654search", fldName, "654b");
		assertSingleResult("654search", fldName, "654c");
		assertSingleResult("654search", fldName, "654e");		
		assertZeroResults(fldName, "654v");
		assertZeroResults(fldName, "654x");
		assertZeroResults(fldName, "654y");
		assertZeroResults(fldName, "654z");

        createIxInitVars(testDataFname);
		assertSingleResult("1261173", fldName, "army");  // 650a
		assertSingleResult("4698973", fldName, "Multiculturalism");  // 650a
		assertSingleResult("4698973", fldName, "\"Flyby missions\"");  // 650-2
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"");  // 650a
		assertSingleResult("4698973", fldName, "Multiculturalism");  // 650a
		// multiple occurrences when there are multiple MARC fields with the same tag
		assertSingleResult("229800", fldName, "Commodity exchanges."); 
		assertSingleResult("229800", fldName, "Foreign exchange."); 
		assertZeroResults(fldName, "nasat"); // 650-2
	}

	/**
	 * Test population and properties of vern_topic_search field
	 */
@Test
	public final void testVernTopicSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_topic_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y and z from  650, 690, 653, 654
		
		assertSingleResult("Vern650search", fldName, "vern650a");
		assertSingleResult("Vern650search", fldName, "vern650b");
		assertSingleResult("Vern650search", fldName, "vern650c");
		assertSingleResult("Vern650search", fldName, "vern650d");
		assertSingleResult("Vern650search", fldName, "vern650e");		
		assertZeroResults(fldName, "vern650v");
		assertZeroResults(fldName, "vern650x");
		assertZeroResults(fldName, "vern650y");
		assertZeroResults(fldName, "vern650z");
	
		assertSingleResult("Vern690search", fldName, "vern690a");
		assertSingleResult("Vern690search", fldName, "vern690b");
		assertSingleResult("Vern690search", fldName, "vern690c");
		assertSingleResult("Vern690search", fldName, "vern690d");
		assertSingleResult("Vern690search", fldName, "vern690e");		
		assertZeroResults(fldName, "vern690v");
		assertZeroResults(fldName, "vern690x");
		assertZeroResults(fldName, "vern690y");
		assertZeroResults(fldName, "vern690z");
	
		assertSingleResult("Vern653search", fldName, "vern653a");
		assertZeroResults(fldName, "vern653v");
		assertZeroResults(fldName, "vern653x");
		assertZeroResults(fldName, "vern653y");
		assertZeroResults(fldName, "vern653z");
	
		assertSingleResult("Vern654search", fldName, "654a");
		assertSingleResult("Vern654search", fldName, "654b");
		assertSingleResult("Vern654search", fldName, "654c");
		assertSingleResult("Vern654search", fldName, "654e");		
		assertZeroResults(fldName, "654v");
		assertZeroResults(fldName, "654x");
		assertZeroResults(fldName, "654y");
		assertZeroResults(fldName, "654z");
	}


	/**
	 * Test population and properties of topic_subx_search field
	 */
@Test
	public final void testTopicSubdivisionSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "topic_subx_search";
//		assertSearchFldMultValProps(fldName);
	
		// subfield x from all subject fields
		assertSingleResult("600search", fldName, "600x");
		assertSingleResult("610search", fldName, "610x");
		assertSingleResult("611search", fldName, "611x");
		assertSingleResult("630search", fldName, "630x");
		assertSingleResult("650search", fldName, "650x");
		assertSingleResult("651search", fldName, "651x");
		// no sub x in 653, 654
		assertSingleResult("655search", fldName, "655x");
		assertSingleResult("656search", fldName, "656x");
		assertSingleResult("657search", fldName, "657x");
		// no sub x in 658
		assertSingleResult("690search", fldName, "690x");
		assertSingleResult("691search", fldName, "691x");
		assertSingleResult("696search", fldName, "696x");
		assertSingleResult("697search", fldName, "697x");
		assertSingleResult("698search", fldName, "698x");
		assertSingleResult("699search", fldName, "699x");

		assertZeroResults(fldName, "600a");
		assertZeroResults(fldName, "610a");
		assertZeroResults(fldName, "611a");
		assertZeroResults(fldName, "630a");
		assertZeroResults(fldName, "650a");
		assertZeroResults(fldName, "651a");
		assertZeroResults(fldName, "653a");
		assertZeroResults(fldName, "654a");
		assertZeroResults(fldName, "655a");
		assertZeroResults(fldName, "656a");
		assertZeroResults(fldName, "657a");
		assertZeroResults(fldName, "658a");
		assertZeroResults(fldName, "690a");
		assertZeroResults(fldName, "691a");
		assertZeroResults(fldName, "696a");
		assertZeroResults(fldName, "697a");
		assertZeroResults(fldName, "698a");
		assertZeroResults(fldName, "699a");
	}

	/**
	 * Test population and properties of vern_topic_subx_search field
	 */
@Test
	public final void testVernTopicSubdivisionSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_topic_subx_search";
//		assertSearchFldMultValProps(fldName);
	
		// subfield x from all subject fields
		assertSingleResult("Vern600search", fldName, "vern600x");
		assertSingleResult("Vern610search", fldName, "vern610x");
		assertSingleResult("Vern611search", fldName, "vern611x");
		assertSingleResult("Vern630search", fldName, "vern630x");
		assertSingleResult("Vern650search", fldName, "vern650x");
		assertSingleResult("Vern651search", fldName, "vern651x");
		// no sub x in 653, 654
		assertSingleResult("Vern655search", fldName, "vern655x");
		assertSingleResult("Vern656search", fldName, "vern656x");
		assertSingleResult("Vern657search", fldName, "vern657x");
		// no sub x in 658
		assertSingleResult("Vern690search", fldName, "vern690x");
		assertSingleResult("Vern691search", fldName, "vern691x");
		assertSingleResult("Vern696search", fldName, "vern696x");
		assertSingleResult("Vern697search", fldName, "vern697x");
		assertSingleResult("Vern698search", fldName, "vern698x");
		assertSingleResult("Vern699search", fldName, "vern699x");
	
		assertZeroResults(fldName, "vern600a");
		assertZeroResults(fldName, "vern610a");
		assertZeroResults(fldName, "vern611a");
		assertZeroResults(fldName, "vern630a");
		assertZeroResults(fldName, "vern650a");
		assertZeroResults(fldName, "vern651a");
		assertZeroResults(fldName, "vern653a");
		assertZeroResults(fldName, "vern654a");
		assertZeroResults(fldName, "vern655a");
		assertZeroResults(fldName, "vern656a");
		assertZeroResults(fldName, "vern657a");
		assertZeroResults(fldName, "vern658a");
		assertZeroResults(fldName, "vern690a");
		assertZeroResults(fldName, "vern691a");
		assertZeroResults(fldName, "vern696a");
		assertZeroResults(fldName, "vern697a");
		assertZeroResults(fldName, "vern698a");
		assertZeroResults(fldName, "vern699a");
	}


	/**
	 * Test population and properties of geographic_search field
	 */
@Test
	public final void testGeographicSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "geographic_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from  651, 691
		Set<String> docIds = new HashSet<String>();
		docIds.add("651search");
		docIds.add("Vern651search");
		assertSearchResults(fldName, "651a", docIds);
		assertSingleResult("651search", fldName, "651e");		
		assertZeroResults(fldName, "651v");
		assertZeroResults(fldName, "651x");
		assertZeroResults(fldName, "651y");
		assertZeroResults(fldName, "651z");

		docIds.clear();
		docIds.add("691search");
		docIds.add("Vern691search");
		assertSearchResults(fldName, "691a", docIds);
		assertSingleResult("691search", fldName, "691e");		
		assertZeroResults(fldName, "691v");
		assertZeroResults(fldName, "691x");
		assertZeroResults(fldName, "691y");
		assertZeroResults(fldName, "691z");
		
	    createIxInitVars(testDataFname);
	    docIds.clear();
		docIds.add("651a");
		docIds.add("651again");
		assertSearchResults(fldName, "muppets", docIds);
		// geographic punctuation shouldn't matter
		assertSingleResult("651numPeriod", fldName, "7.150");
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y.)\"");
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y.\"");
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y\"");
		assertSingleResult("651siberia", fldName, "\"Siberia (Russia)\"");
		assertSingleResult("651siberia", fldName, "\"Siberia (Russia).\"");
		// 651a
		assertSingleResult("6280316", fldName, "Tennessee"); 
		assertSingleResult("6280316", fldName, "Arkansas"); 
	}

	/**
	 * Test population and properties of vern_geographic_search field
	 */
@Test
	public final void testVernGeographicSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_geographic_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from  651, 691
		assertSingleResult("Vern651search", fldName, "vern651a");		
		assertSingleResult("Vern651search", fldName, "vern651e");		
		assertZeroResults(fldName, "vern651v");
		assertZeroResults(fldName, "vern651x");
		assertZeroResults(fldName, "vern651y");
		assertZeroResults(fldName, "vern651z");
	
		assertSingleResult("Vern691search", fldName, "vern691a");		
		assertSingleResult("Vern691search", fldName, "vern691e");		
		assertZeroResults(fldName, "vern691v");
		assertZeroResults(fldName, "vern691x");
		assertZeroResults(fldName, "vern691y");
		assertZeroResults(fldName, "vern691z");
	}

	/**
	 * Test population and properties of geo_subz_search field
	 */
@Test
	public final void testGeographicSubdivisionSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "geographic_subz_search";
//		assertSearchFldMultValProps(fldName);
	
		// subfield z from all subject fields
		assertSingleResult("600search", fldName, "600z");
		assertSingleResult("610search", fldName, "610z");
		// no sub z in 611
		assertSingleResult("630search", fldName, "630z");
		assertSingleResult("650search", fldName, "650z");
		assertSingleResult("651search", fldName, "651z");
		// no sub z in 653
		assertSingleResult("654search", fldName, "654z");
		assertSingleResult("655search", fldName, "655z");
		assertSingleResult("656search", fldName, "656z");
		assertSingleResult("657search", fldName, "657z");
		// no sub z in 658
		assertSingleResult("690search", fldName, "690z");
		assertSingleResult("691search", fldName, "691z");
		assertSingleResult("696search", fldName, "696z");
		assertSingleResult("697search", fldName, "697z");
		assertSingleResult("698search", fldName, "698z");
		assertSingleResult("699search", fldName, "699z");

		assertZeroResults(fldName, "600a");
		assertZeroResults(fldName, "610a");
		assertZeroResults(fldName, "611a");
		assertZeroResults(fldName, "630a");
		assertZeroResults(fldName, "650a");
		assertZeroResults(fldName, "651a");
		assertZeroResults(fldName, "653a");
		assertZeroResults(fldName, "654a");
		assertZeroResults(fldName, "655a");
		assertZeroResults(fldName, "656a");
		assertZeroResults(fldName, "657a");
		assertZeroResults(fldName, "658a");
		assertZeroResults(fldName, "690a");
		assertZeroResults(fldName, "691a");
		assertZeroResults(fldName, "696a");
		assertZeroResults(fldName, "697a");
		assertZeroResults(fldName, "698a");
		assertZeroResults(fldName, "699a");
	}

	/**
	 * Test population and properties of vern_geo_subz_search field
	 */
@Test
	public final void testVernGeographicSubdivisionSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_geographic_subz_search";
//		assertSearchFldMultValProps(fldName);
	
		// subfield z from all subject fields
		assertSingleResult("Vern600search", fldName, "vern600z");
		assertSingleResult("Vern610search", fldName, "vern610z");
		// no sub z in 611
		assertSingleResult("Vern630search", fldName, "vern630z");
		assertSingleResult("Vern650search", fldName, "vern650z");
		assertSingleResult("Vern651search", fldName, "vern651z");
		// no sub z in 653
		assertSingleResult("Vern654search", fldName, "vern654z");
		assertSingleResult("Vern655search", fldName, "vern655z");
		assertSingleResult("Vern656search", fldName, "vern656z");
		assertSingleResult("Vern657search", fldName, "vern657z");
		// no sub z in 658
		assertSingleResult("Vern690search", fldName, "vern690z");
		assertSingleResult("Vern691search", fldName, "vern691z");
		assertSingleResult("Vern696search", fldName, "vern696z");
		assertSingleResult("Vern697search", fldName, "vern697z");
		assertSingleResult("Vern698search", fldName, "vern698z");
		assertSingleResult("Vern699search", fldName, "vern699z");
	
		assertZeroResults(fldName, "vern600a");
		assertZeroResults(fldName, "vern610a");
		assertZeroResults(fldName, "vern611a");
		assertZeroResults(fldName, "vern630a");
		assertZeroResults(fldName, "vern650a");
		assertZeroResults(fldName, "vern651a");
		assertZeroResults(fldName, "vern653a");
		assertZeroResults(fldName, "vern654a");
		assertZeroResults(fldName, "vern655a");
		assertZeroResults(fldName, "vern656a");
		assertZeroResults(fldName, "vern657a");
		assertZeroResults(fldName, "vern658a");
		assertZeroResults(fldName, "vern690a");
		assertZeroResults(fldName, "vern691a");
		assertZeroResults(fldName, "vern696a");
		assertZeroResults(fldName, "vern697a");
		assertZeroResults(fldName, "vern698a");
		assertZeroResults(fldName, "vern699a");
	}


	/**
	 * Test population and properties of subject_other_search field
	 */
@Test
	public final void testSubjectOther61xSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "subject_other_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from  600, 610-11, 630, 655-58, 696-699
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("600search");
		docIds.add("Vern600search");
		assertSearchResults(fldName, "600a", docIds);
		assertSingleResult("600search", fldName, "600b");
		assertSingleResult("600search", fldName, "600c");
		assertSingleResult("600search", fldName, "600d");
		assertSingleResult("600search", fldName, "600e");		
		assertSingleResult("600search", fldName, "600f");		
		assertSingleResult("600search", fldName, "600g");		
		assertSingleResult("600search", fldName, "600h");		
		assertSingleResult("600search", fldName, "600j");		
		assertSingleResult("600search", fldName, "600k");		
		assertSingleResult("600search", fldName, "600l");		
		assertSingleResult("600search", fldName, "600m");		
		assertSingleResult("600search", fldName, "600n");		
		assertSingleResult("600search", fldName, "600o");		
		assertSingleResult("600search", fldName, "600p");		
		assertSingleResult("600search", fldName, "600q");		
		assertSingleResult("600search", fldName, "600r");		
		assertSingleResult("600search", fldName, "600s");		
		assertSingleResult("600search", fldName, "600t");		
		assertSingleResult("600search", fldName, "600u");		
		assertZeroResults(fldName, "600v");
		assertZeroResults(fldName, "600x");
		assertZeroResults(fldName, "600y");
		assertZeroResults(fldName, "600z");

		docIds.clear();
		docIds.add("610search");
		docIds.add("Vern610search");
		assertSearchResults(fldName, "610a", docIds);
		assertSingleResult("610search", fldName, "610b");
		assertSingleResult("610search", fldName, "610c");
		assertSingleResult("610search", fldName, "610d");
		assertSingleResult("610search", fldName, "610e");	
		assertSingleResult("610search", fldName, "610f");		
		assertSingleResult("610search", fldName, "610g");		
		assertSingleResult("610search", fldName, "610h");		
		assertSingleResult("610search", fldName, "610k");		
		assertSingleResult("610search", fldName, "610l");		
		assertSingleResult("610search", fldName, "610m");		
		assertSingleResult("610search", fldName, "610n");		
		assertSingleResult("610search", fldName, "610o");		
		assertSingleResult("610search", fldName, "610p");		
		assertSingleResult("610search", fldName, "610r");		
		assertSingleResult("610search", fldName, "610s");		
		assertSingleResult("610search", fldName, "610t");		
		assertSingleResult("610search", fldName, "610u");		
		assertZeroResults(fldName, "610v");
		assertZeroResults(fldName, "610x");
		assertZeroResults(fldName, "610y");
		assertZeroResults(fldName, "610z");

		docIds.clear();
		docIds.add("611search");
		docIds.add("Vern611search");
		assertSearchResults(fldName, "611a", docIds);
		assertSingleResult("611search", fldName, "611c");
		assertSingleResult("611search", fldName, "611d");
		assertSingleResult("611search", fldName, "611e");		
		assertSingleResult("611search", fldName, "611f");		
		assertSingleResult("611search", fldName, "611g");		
		assertSingleResult("611search", fldName, "611h");		
		assertSingleResult("611search", fldName, "611j");		
		assertSingleResult("611search", fldName, "611k");		
		assertSingleResult("611search", fldName, "611l");		
		assertSingleResult("611search", fldName, "611n");		
		assertSingleResult("611search", fldName, "611p");		
		assertSingleResult("611search", fldName, "611q");		
		assertSingleResult("611search", fldName, "611s");		
		assertSingleResult("611search", fldName, "611t");		
		assertSingleResult("611search", fldName, "611u");		
		assertZeroResults(fldName, "611v");
		assertZeroResults(fldName, "611x");
		assertZeroResults(fldName, "611y");
		assertZeroResults(fldName, "611z");
		
        createIxInitVars(testDataFname);
		assertSingleResult("3743949", fldName, "Federico"); // 600a
		assertSingleResult("3743949", fldName, "1936");  // 600d
		assertSingleResult("919006", fldName, "Emesa");  // 600c
		assertSingleResult("1261173", fldName, "peace");  // 600t
		assertSingleResult("1261173", fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it\"");  // 600t
		assertSingleResult("115472", fldName, "Economic");  // 610a
		assertSingleResult("115472", fldName, "\"European Economic Community\"");  // 610a
		assertSingleResult("1261173", fldName, "army");  // 610b
		
		assertZeroResults(fldName, "\"Zhongguo gong chan dang Party work.\"");  // 610-6
		assertSingleResult("345228", fldName, "\"Zemnukhov, Ivan\""); // 600a
		assertSingleResult("3743949", fldName, "Federico"); // 600a
		assertSingleResult("3743949", fldName, "1936");  // 600d
		assertSingleResult("919006", fldName, "Emesa");  // 600c
		assertSingleResult("1261173", fldName, "peace");  // 600t
		assertSingleResult("115472", fldName, "Economic");  // 610a
		assertSingleResult("1261173", fldName, "army");  // 610b
		
		// default operator is OR, since dismax will make it effectively AND
		docIds.clear();
		docIds.add("6553");
		docIds.add("610atpv");
		assertSearchResults(fldName, "report", docIds);		
	}

	/**
	 * Test population and properties of vern_subject_other_search field
	 */
@Test
	public final void testVernSubjectOther61xSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_subject_other_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from  600, 610-11, 630, 655-58, 696-699
	
		assertSingleResult("Vern600search", fldName, "vern600a");
		assertSingleResult("Vern600search", fldName, "vern600b");
		assertSingleResult("Vern600search", fldName, "vern600c");
		assertSingleResult("Vern600search", fldName, "vern600d");
		assertSingleResult("Vern600search", fldName, "vern600e");		
		assertSingleResult("Vern600search", fldName, "vern600f");		
		assertSingleResult("Vern600search", fldName, "vern600g");		
		assertSingleResult("Vern600search", fldName, "vern600h");		
		assertSingleResult("Vern600search", fldName, "vern600j");		
		assertSingleResult("Vern600search", fldName, "vern600k");		
		assertSingleResult("Vern600search", fldName, "vern600l");		
		assertSingleResult("Vern600search", fldName, "vern600m");		
		assertSingleResult("Vern600search", fldName, "vern600n");		
		assertSingleResult("Vern600search", fldName, "vern600o");		
		assertSingleResult("Vern600search", fldName, "vern600p");		
		assertSingleResult("Vern600search", fldName, "vern600q");		
		assertSingleResult("Vern600search", fldName, "vern600r");		
		assertSingleResult("Vern600search", fldName, "vern600s");		
		assertSingleResult("Vern600search", fldName, "vern600t");		
		assertSingleResult("Vern600search", fldName, "vern600u");		
		assertZeroResults(fldName, "vern600v");
		assertZeroResults(fldName, "vern600x");
		assertZeroResults(fldName, "vern600y");
		assertZeroResults(fldName, "vern600z");
	
		assertSingleResult("Vern610search", fldName, "vern610a");
		assertSingleResult("Vern610search", fldName, "vern610b");
		assertSingleResult("Vern610search", fldName, "vern610c");
		assertSingleResult("Vern610search", fldName, "vern610d");
		assertSingleResult("Vern610search", fldName, "vern610e");	
		assertSingleResult("Vern610search", fldName, "vern610f");		
		assertSingleResult("Vern610search", fldName, "vern610g");		
		assertSingleResult("Vern610search", fldName, "vern610h");		
		assertSingleResult("Vern610search", fldName, "vern610k");		
		assertSingleResult("Vern610search", fldName, "vern610l");		
		assertSingleResult("Vern610search", fldName, "vern610m");		
		assertSingleResult("Vern610search", fldName, "vern610n");		
		assertSingleResult("Vern610search", fldName, "vern610o");		
		assertSingleResult("Vern610search", fldName, "vern610p");		
		assertSingleResult("Vern610search", fldName, "vern610r");		
		assertSingleResult("Vern610search", fldName, "vern610s");		
		assertSingleResult("Vern610search", fldName, "vern610t");		
		assertSingleResult("Vern610search", fldName, "vern610u");		
		assertZeroResults(fldName, "vern610v");
		assertZeroResults(fldName, "vern610x");
		assertZeroResults(fldName, "vern610y");
		assertZeroResults(fldName, "vern610z");
	
		assertSingleResult("Vern611search", fldName, "vern611a");
		assertSingleResult("Vern611search", fldName, "vern611c");
		assertSingleResult("Vern611search", fldName, "vern611d");
		assertSingleResult("Vern611search", fldName, "vern611e");		
		assertSingleResult("Vern611search", fldName, "vern611f");		
		assertSingleResult("Vern611search", fldName, "vern611g");		
		assertSingleResult("Vern611search", fldName, "vern611h");		
		assertSingleResult("Vern611search", fldName, "vern611j");		
		assertSingleResult("Vern611search", fldName, "vern611k");		
		assertSingleResult("Vern611search", fldName, "vern611l");		
		assertSingleResult("Vern611search", fldName, "vern611n");		
		assertSingleResult("Vern611search", fldName, "vern611p");		
		assertSingleResult("Vern611search", fldName, "vern611q");		
		assertSingleResult("Vern611search", fldName, "vern611s");		
		assertSingleResult("Vern611search", fldName, "vern611t");		
		assertSingleResult("Vern611search", fldName, "vern611u");		
		assertZeroResults(fldName, "vern611v");
		assertZeroResults(fldName, "vern611x");
		assertZeroResults(fldName, "vern611y");
		assertZeroResults(fldName, "vern611z");
	}


	/**
	 * Test population and properties of subject_other_search field
	 */
	@Test
	public final void testSubjectOther630Search() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "subject_other_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from 630
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("630search");
		docIds.add("Vern630search");
		assertSearchResults(fldName, "630a", docIds);
		assertSingleResult("630search", fldName, "630d");
		assertSingleResult("630search", fldName, "630e");		
		assertSingleResult("630search", fldName, "630f");		
		assertSingleResult("630search", fldName, "630g");		
		assertSingleResult("630search", fldName, "630h");		
		assertSingleResult("630search", fldName, "630k");		
		assertSingleResult("630search", fldName, "630l");		
		assertSingleResult("630search", fldName, "630m");		
		assertSingleResult("630search", fldName, "630n");		
		assertSingleResult("630search", fldName, "630o");		
		assertSingleResult("630search", fldName, "630p");		
		assertSingleResult("630search", fldName, "630r");		
		assertSingleResult("630search", fldName, "630s");		
		assertSingleResult("630search", fldName, "630t");		
		assertZeroResults(fldName, "630v");
		assertZeroResults(fldName, "630x");
		assertZeroResults(fldName, "630y");
		assertZeroResults(fldName, "630z");
		
        createIxInitVars(testDataFname);
		assertSingleResult("1261173", fldName, "magna carta");  // 630a
	}

	/**
	 * Test population and properties of vern_subject_other_search field
	 */
@Test
	public final void testVernSubjectOther630Search() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_subject_other_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from 630
	
		assertSingleResult("Vern630search", fldName, "vern630a");
		assertSingleResult("Vern630search", fldName, "vern630d");
		assertSingleResult("Vern630search", fldName, "vern630e");		
		assertSingleResult("Vern630search", fldName, "vern630f");		
		assertSingleResult("Vern630search", fldName, "vern630g");		
		assertSingleResult("Vern630search", fldName, "vern630h");		
		assertSingleResult("Vern630search", fldName, "vern630k");		
		assertSingleResult("Vern630search", fldName, "vern630l");		
		assertSingleResult("Vern630search", fldName, "vern630m");		
		assertSingleResult("Vern630search", fldName, "vern630n");		
		assertSingleResult("Vern630search", fldName, "vern630o");		
		assertSingleResult("Vern630search", fldName, "vern630p");		
		assertSingleResult("Vern630search", fldName, "vern630r");		
		assertSingleResult("Vern630search", fldName, "vern630s");		
		assertSingleResult("Vern630search", fldName, "vern630t");		
		assertZeroResults(fldName, "vern630v");
		assertZeroResults(fldName, "vern630x");
		assertZeroResults(fldName, "vern630y");
		assertZeroResults(fldName, "vern630z");
	}

	/**
	 * Test population and properties of subject_other_search field
	 */
@Test
	public final void testSubjectOther65xSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "subject_other_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from 655-58
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("655search");
		docIds.add("Vern655search");
		assertSearchResults(fldName, "655a", docIds);
		assertSingleResult("655search", fldName, "655b");
		assertSingleResult("655search", fldName, "655c");
		assertZeroResults(fldName, "655v");
		assertZeroResults(fldName, "655x");
		assertZeroResults(fldName, "655y");
		assertZeroResults(fldName, "655z");
		
		docIds.clear();
		docIds.add("656search");
		docIds.add("Vern656search");
		assertSearchResults(fldName, "656a", docIds);
		assertSingleResult("656search", fldName, "656k");		
		assertZeroResults(fldName, "656v");
		assertZeroResults(fldName, "656x");
		assertZeroResults(fldName, "656y");
		assertZeroResults(fldName, "656z");
	
		docIds.clear();
		docIds.add("657search");
		docIds.add("Vern657search");
		assertSearchResults(fldName, "657a", docIds);
		assertZeroResults(fldName, "657v");
		assertZeroResults(fldName, "657x");
		assertZeroResults(fldName, "657y");
		assertZeroResults(fldName, "657z");
	
		docIds.clear();
		docIds.add("658search");
		docIds.add("Vern658search");
		assertSearchResults(fldName, "658a", docIds);
		assertSingleResult("658search", fldName, "658b");
		assertSingleResult("658search", fldName, "658c");
		assertSingleResult("658search", fldName, "658d");
		// no sub v,x,y,z in 658
		
        createIxInitVars(testDataFname);
		assertSingleResult("6552", fldName, "dictionaries");  // 655a
		
		assertSingleResult("6553", fldName, "\"Fire Reports\"");  // 655-a
		assertZeroResults(fldName, "atlanta georgia 1978");  // 655z
		assertSingleResult("7233951", fldName, "\"Lectures\"");  // 655a
		assertZeroResults(fldName, "municipal"); // 655-3
		assertSingleResult("6552", fldName, "dictionaries");  // 655a
		
		// default operator is OR, since dismax will make it effectively AND
		docIds.clear();
		docIds.add("6553");
		docIds.add("610atpv");
		assertSearchResults(fldName, "report", docIds);		
	}

	/**
	 * Test population and properties of vern_subject_other_search field
	 */
@Test
	public final void testVernSubjectOther65xSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_subject_other_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from 655-58
	
		assertSingleResult("Vern655search", fldName, "vern655a");
		assertSingleResult("Vern655search", fldName, "vern655b");
		assertSingleResult("Vern655search", fldName, "vern655c");
		assertZeroResults(fldName, "vern655v");
		assertZeroResults(fldName, "vern655x");
		assertZeroResults(fldName, "vern655y");
		assertZeroResults(fldName, "vern655z");
	
		assertSingleResult("Vern656search", fldName, "vern656a");
		assertSingleResult("Vern656search", fldName, "vern656k");		
		assertZeroResults(fldName, "vern656v");
		assertZeroResults(fldName, "vern656x");
		assertZeroResults(fldName, "vern656y");
		assertZeroResults(fldName, "vern656z");
	
		assertSingleResult("Vern657search", fldName, "vern657a");
		assertZeroResults(fldName, "vern657v");
		assertZeroResults(fldName, "vern657x");
		assertZeroResults(fldName, "vern657y");
		assertZeroResults(fldName, "vern657z");
	
		assertSingleResult("Vern658search", fldName, "vern658a");
		assertSingleResult("Vern658search", fldName, "vern658b");
		assertSingleResult("Vern658search", fldName, "vern658c");
		assertSingleResult("Vern658search", fldName, "vern658d");
		// no sub v,x,y,z in 658
	}

	/**
	 * Test population and properties of subject_other_search field
	 */
@Test
	public final void testSubjectOther69xSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "subject_other_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from 696-699
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("696search");
		docIds.add("Vern696search");
		assertSearchResults(fldName, "696a", docIds);
		assertSingleResult("696search", fldName, "696b");
		assertSingleResult("696search", fldName, "696c");
		assertSingleResult("696search", fldName, "696d");
		assertSingleResult("696search", fldName, "696e");		
		assertSingleResult("696search", fldName, "696f");		
		assertSingleResult("696search", fldName, "696g");		
		assertSingleResult("696search", fldName, "696h");		
		assertSingleResult("696search", fldName, "696j");		
		assertSingleResult("696search", fldName, "696k");		
		assertSingleResult("696search", fldName, "696l");		
		assertSingleResult("696search", fldName, "696m");		
		assertSingleResult("696search", fldName, "696n");		
		assertSingleResult("696search", fldName, "696o");		
		assertSingleResult("696search", fldName, "696p");		
		assertSingleResult("696search", fldName, "696q");		
		assertSingleResult("696search", fldName, "696r");		
		assertSingleResult("696search", fldName, "696s");		
		assertSingleResult("696search", fldName, "696t");		
		assertSingleResult("696search", fldName, "696u");		
		assertZeroResults(fldName, "696v");
		assertZeroResults(fldName, "696x");
		assertZeroResults(fldName, "696y");
		assertZeroResults(fldName, "696z");
	
		docIds.clear();
		docIds.add("697search");
		docIds.add("Vern697search");
		assertSearchResults(fldName, "697a", docIds);
		assertSingleResult("697search", fldName, "697b");
		assertSingleResult("697search", fldName, "697c");
		assertSingleResult("697search", fldName, "697d");
		assertSingleResult("697search", fldName, "697e");		
		assertSingleResult("697search", fldName, "697f");		
		assertSingleResult("697search", fldName, "697g");		
		assertSingleResult("697search", fldName, "697h");		
		assertSingleResult("697search", fldName, "697j");		
		assertSingleResult("697search", fldName, "697k");		
		assertSingleResult("697search", fldName, "697l");		
		assertSingleResult("697search", fldName, "697m");		
		assertSingleResult("697search", fldName, "697n");		
		assertSingleResult("697search", fldName, "697o");		
		assertSingleResult("697search", fldName, "697p");		
		assertSingleResult("697search", fldName, "697q");		
		assertSingleResult("697search", fldName, "697r");		
		assertSingleResult("697search", fldName, "697s");		
		assertSingleResult("697search", fldName, "697t");		
		assertSingleResult("697search", fldName, "697u");		
		assertZeroResults(fldName, "697v");
		assertZeroResults(fldName, "697x");
		assertZeroResults(fldName, "697y");
		assertZeroResults(fldName, "697z");
	
		docIds.clear();
		docIds.add("698search");
		docIds.add("Vern698search");
		assertSearchResults(fldName, "698a", docIds);
		assertSingleResult("698search", fldName, "698b");
		assertSingleResult("698search", fldName, "698c");
		assertSingleResult("698search", fldName, "698d");
		assertSingleResult("698search", fldName, "698e");		
		assertSingleResult("698search", fldName, "698f");		
		assertSingleResult("698search", fldName, "698g");		
		assertSingleResult("698search", fldName, "698h");		
		assertSingleResult("698search", fldName, "698j");		
		assertSingleResult("698search", fldName, "698k");		
		assertSingleResult("698search", fldName, "698l");		
		assertSingleResult("698search", fldName, "698m");		
		assertSingleResult("698search", fldName, "698n");		
		assertSingleResult("698search", fldName, "698o");		
		assertSingleResult("698search", fldName, "698p");		
		assertSingleResult("698search", fldName, "698q");		
		assertSingleResult("698search", fldName, "698r");		
		assertSingleResult("698search", fldName, "698s");		
		assertSingleResult("698search", fldName, "698t");		
		assertSingleResult("698search", fldName, "698u");		
		assertZeroResults(fldName, "698v");
		assertZeroResults(fldName, "698x");
		assertZeroResults(fldName, "698y");
		assertZeroResults(fldName, "698z");
	
		docIds.clear();
		docIds.add("699search");
		docIds.add("Vern699search");
		assertSearchResults(fldName, "699a", docIds);
		assertSingleResult("699search", fldName, "699b");
		assertSingleResult("699search", fldName, "699c");
		assertSingleResult("699search", fldName, "699d");
		assertSingleResult("699search", fldName, "699e");		
		assertSingleResult("699search", fldName, "699f");		
		assertSingleResult("699search", fldName, "699g");		
		assertSingleResult("699search", fldName, "699h");		
		assertSingleResult("699search", fldName, "699j");		
		assertSingleResult("699search", fldName, "699k");		
		assertSingleResult("699search", fldName, "699l");		
		assertSingleResult("699search", fldName, "699m");		
		assertSingleResult("699search", fldName, "699n");		
		assertSingleResult("699search", fldName, "699o");		
		assertSingleResult("699search", fldName, "699p");		
		assertSingleResult("699search", fldName, "699q");		
		assertSingleResult("699search", fldName, "699r");		
		assertSingleResult("699search", fldName, "699s");		
		assertSingleResult("699search", fldName, "699t");		
		assertSingleResult("699search", fldName, "699u");		
		assertZeroResults(fldName, "699v");
		assertZeroResults(fldName, "699x");
		assertZeroResults(fldName, "699y");
		assertZeroResults(fldName, "699z");
	}

	/**
	 * Test population and properties of vern_subject_other_search field
	 */
@Test
	public final void testVernSubjectOther69xSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_subject_other_search";
//		assertSearchFldMultValProps(fldName);
	
		// all subfields except v, x, y, z from 696-699
	
		assertSingleResult("Vern696search", fldName, "vern696a");
		assertSingleResult("Vern696search", fldName, "vern696b");
		assertSingleResult("Vern696search", fldName, "vern696c");
		assertSingleResult("Vern696search", fldName, "vern696d");
		assertSingleResult("Vern696search", fldName, "vern696e");		
		assertSingleResult("Vern696search", fldName, "vern696f");		
		assertSingleResult("Vern696search", fldName, "vern696g");		
		assertSingleResult("Vern696search", fldName, "vern696h");		
		assertSingleResult("Vern696search", fldName, "vern696j");		
		assertSingleResult("Vern696search", fldName, "vern696k");		
		assertSingleResult("Vern696search", fldName, "vern696l");		
		assertSingleResult("Vern696search", fldName, "vern696m");		
		assertSingleResult("Vern696search", fldName, "vern696n");		
		assertSingleResult("Vern696search", fldName, "vern696o");		
		assertSingleResult("Vern696search", fldName, "vern696p");		
		assertSingleResult("Vern696search", fldName, "vern696q");		
		assertSingleResult("Vern696search", fldName, "vern696r");		
		assertSingleResult("Vern696search", fldName, "vern696s");		
		assertSingleResult("Vern696search", fldName, "vern696t");		
		assertSingleResult("Vern696search", fldName, "vern696u");		
		assertZeroResults(fldName, "vern696v");
		assertZeroResults(fldName, "vern696x");
		assertZeroResults(fldName, "vern696y");
		assertZeroResults(fldName, "vern696z");
	
		assertSingleResult("Vern697search", fldName, "vern697a");
		assertSingleResult("Vern697search", fldName, "vern697b");
		assertSingleResult("Vern697search", fldName, "vern697c");
		assertSingleResult("Vern697search", fldName, "vern697d");
		assertSingleResult("Vern697search", fldName, "vern697e");		
		assertSingleResult("Vern697search", fldName, "vern697f");		
		assertSingleResult("Vern697search", fldName, "vern697g");		
		assertSingleResult("Vern697search", fldName, "vern697h");		
		assertSingleResult("Vern697search", fldName, "vern697j");		
		assertSingleResult("Vern697search", fldName, "vern697k");		
		assertSingleResult("Vern697search", fldName, "vern697l");		
		assertSingleResult("Vern697search", fldName, "vern697m");		
		assertSingleResult("Vern697search", fldName, "vern697n");		
		assertSingleResult("Vern697search", fldName, "vern697o");		
		assertSingleResult("Vern697search", fldName, "vern697p");		
		assertSingleResult("Vern697search", fldName, "vern697q");		
		assertSingleResult("Vern697search", fldName, "vern697r");		
		assertSingleResult("Vern697search", fldName, "vern697s");		
		assertSingleResult("Vern697search", fldName, "vern697t");		
		assertSingleResult("Vern697search", fldName, "vern697u");		
		assertZeroResults(fldName, "vern697v");
		assertZeroResults(fldName, "vern697x");
		assertZeroResults(fldName, "vern697y");
		assertZeroResults(fldName, "vern697z");
	
		assertSingleResult("Vern698search", fldName, "vern698a");
		assertSingleResult("Vern698search", fldName, "vern698b");
		assertSingleResult("Vern698search", fldName, "vern698c");
		assertSingleResult("Vern698search", fldName, "vern698d");
		assertSingleResult("Vern698search", fldName, "vern698e");		
		assertSingleResult("Vern698search", fldName, "vern698f");		
		assertSingleResult("Vern698search", fldName, "vern698g");		
		assertSingleResult("Vern698search", fldName, "vern698h");		
		assertSingleResult("Vern698search", fldName, "vern698j");		
		assertSingleResult("Vern698search", fldName, "vern698k");		
		assertSingleResult("Vern698search", fldName, "vern698l");		
		assertSingleResult("Vern698search", fldName, "vern698m");		
		assertSingleResult("Vern698search", fldName, "vern698n");		
		assertSingleResult("Vern698search", fldName, "vern698o");		
		assertSingleResult("Vern698search", fldName, "vern698p");		
		assertSingleResult("Vern698search", fldName, "vern698q");		
		assertSingleResult("Vern698search", fldName, "vern698r");		
		assertSingleResult("Vern698search", fldName, "vern698s");		
		assertSingleResult("Vern698search", fldName, "vern698t");		
		assertSingleResult("Vern698search", fldName, "vern698u");		
		assertZeroResults(fldName, "vern698v");
		assertZeroResults(fldName, "vern698x");
		assertZeroResults(fldName, "vern698y");
		assertZeroResults(fldName, "vern698z");
	
		assertSingleResult("Vern699search", fldName, "vern699a");
		assertSingleResult("Vern699search", fldName, "vern699b");
		assertSingleResult("Vern699search", fldName, "vern699c");
		assertSingleResult("Vern699search", fldName, "vern699d");
		assertSingleResult("Vern699search", fldName, "vern699e");		
		assertSingleResult("Vern699search", fldName, "vern699f");		
		assertSingleResult("Vern699search", fldName, "vern699g");		
		assertSingleResult("Vern699search", fldName, "vern699h");		
		assertSingleResult("Vern699search", fldName, "vern699j");		
		assertSingleResult("Vern699search", fldName, "vern699k");		
		assertSingleResult("Vern699search", fldName, "vern699l");		
		assertSingleResult("Vern699search", fldName, "vern699m");		
		assertSingleResult("Vern699search", fldName, "vern699n");		
		assertSingleResult("Vern699search", fldName, "vern699o");		
		assertSingleResult("Vern699search", fldName, "vern699p");		
		assertSingleResult("Vern699search", fldName, "vern699q");		
		assertSingleResult("Vern699search", fldName, "vern699r");		
		assertSingleResult("Vern699search", fldName, "vern699s");		
		assertSingleResult("Vern699search", fldName, "vern699t");		
		assertSingleResult("Vern699search", fldName, "vern699u");		
		assertZeroResults(fldName, "vern699v");
		assertZeroResults(fldName, "vern699x");
		assertZeroResults(fldName, "vern699y");
		assertZeroResults(fldName, "vern699z");
	}

	/**
	 * Test population and properties of subject_other_subvy_search field
	 */
@Test
	public final void testSubjectOtherSubdivisionSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "subject_other_subvy_search";
//		assertSearchFldMultValProps(fldName);
	
		// subfields v from all subject fields
		assertSingleResult("600search", fldName, "600v");
		assertSingleResult("610search", fldName, "610v");
		assertSingleResult("611search", fldName, "611v");
		assertSingleResult("630search", fldName, "630v");
		assertSingleResult("650search", fldName, "650v");
		assertSingleResult("651search", fldName, "651v");
		// no sub v in 653
		assertSingleResult("654search", fldName, "654v");
		assertSingleResult("655search", fldName, "655v");
		assertSingleResult("656search", fldName, "656v");
		assertSingleResult("657search", fldName, "657v");
		// no sub v in 658
		assertSingleResult("690search", fldName, "690v");
		assertSingleResult("691search", fldName, "691v");
		assertSingleResult("696search", fldName, "696v");
		assertSingleResult("697search", fldName, "697v");
		assertSingleResult("698search", fldName, "698v");
		assertSingleResult("699search", fldName, "699v");

		// subfields y from all subject fields
		assertSingleResult("600search", fldName, "600y");
		assertSingleResult("610search", fldName, "610y");
		assertSingleResult("611search", fldName, "611y");
		assertSingleResult("630search", fldName, "630y");
		assertSingleResult("650search", fldName, "650y");
		assertSingleResult("651search", fldName, "651y");
		// no sub y in 653
		assertSingleResult("654search", fldName, "654y");
		assertSingleResult("655search", fldName, "655y");
		assertSingleResult("656search", fldName, "656y");
		assertSingleResult("657search", fldName, "657y");
		// no sub y in 658
		assertSingleResult("690search", fldName, "690y");
		assertSingleResult("691search", fldName, "691y");
		assertSingleResult("696search", fldName, "696y");
		assertSingleResult("697search", fldName, "697y");
		assertSingleResult("698search", fldName, "698y");
		assertSingleResult("699search", fldName, "699y");
		
		assertZeroResults(fldName, "600a");
		assertZeroResults(fldName, "610a");
		assertZeroResults(fldName, "611a");
		assertZeroResults(fldName, "630a");
		assertZeroResults(fldName, "650a");
		assertZeroResults(fldName, "651a");
		assertZeroResults(fldName, "653a");
		assertZeroResults(fldName, "654a");
		assertZeroResults(fldName, "655a");
		assertZeroResults(fldName, "656a");
		assertZeroResults(fldName, "657a");
		assertZeroResults(fldName, "658a");
		assertZeroResults(fldName, "690a");
		assertZeroResults(fldName, "691a");
		assertZeroResults(fldName, "696a");
		assertZeroResults(fldName, "697a");
		assertZeroResults(fldName, "698a");
		assertZeroResults(fldName, "699a");
		
		// 651v
	    createIxInitVars(testDataFname);
	    assertSingleResult("6280316", fldName, "Maps."); 
	    assertSingleResult("6280316", fldName, "map."); 
	    
	    // 650y 651y
		createIxInitVars("eraTests.mrc");
		Set<String> docIds = new HashSet<String>();
		docIds.add("650y");
		docIds.add("666");
		assertSearchResults(fldName, "\"20th century\"", docIds);
		assertSearchResults(fldName, "\"20th century.\"", docIds);
		docIds.clear();
		docIds.add("111");
		docIds.add("777");
		docIds.add("888");
		docIds.add("999");
		assertSearchResults(fldName, "\"To 449\"", docIds);
	    assertSingleResult("111", fldName, "\"449-1066\"");
	    assertSingleResult("222", fldName, "\"1921\"");
	    assertSingleResult("777", fldName, "\"Roman period, 55 B.C.-449 A.D.\"");
	}

	/**
	 * Test population and properties of vern_subject_other_subvy_search field
	 */
@Test
	public final void testVernSubjectOtherSubdivisionSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars(searchTestDataFname);
		String fldName = "vern_subject_other_subvy_search";
//		assertSearchFldMultValProps(fldName);
	
		// subfields v from all subject fields
		assertSingleResult("Vern600search", fldName, "vern600v");
		assertSingleResult("Vern610search", fldName, "vern610v");
		assertSingleResult("Vern611search", fldName, "vern611v");
		assertSingleResult("Vern630search", fldName, "vern630v");
		assertSingleResult("Vern650search", fldName, "vern650v");
		assertSingleResult("Vern651search", fldName, "vern651v");
		// no sub v in 653
		assertSingleResult("Vern654search", fldName, "vern654v");
		assertSingleResult("Vern655search", fldName, "vern655v");
		assertSingleResult("Vern656search", fldName, "vern656v");
		assertSingleResult("Vern657search", fldName, "vern657v");
		// no sub v in 658
		assertSingleResult("Vern690search", fldName, "vern690v");
		assertSingleResult("Vern691search", fldName, "vern691v");
		assertSingleResult("Vern696search", fldName, "vern696v");
		assertSingleResult("Vern697search", fldName, "vern697v");
		assertSingleResult("Vern698search", fldName, "vern698v");
		assertSingleResult("Vern699search", fldName, "vern699v");
	
		// subfields y from all subject fields
		assertSingleResult("Vern600search", fldName, "vern600y");
		assertSingleResult("Vern610search", fldName, "vern610y");
		assertSingleResult("Vern611search", fldName, "vern611y");
		assertSingleResult("Vern630search", fldName, "vern630y");
		assertSingleResult("Vern650search", fldName, "vern650y");
		assertSingleResult("Vern651search", fldName, "vern651y");
		// no sub y in 653
		assertSingleResult("Vern654search", fldName, "vern654y");
		assertSingleResult("Vern655search", fldName, "vern655y");
		assertSingleResult("Vern656search", fldName, "vern656y");
		assertSingleResult("Vern657search", fldName, "vern657y");
		// no sub y in 658
		assertSingleResult("Vern690search", fldName, "vern690y");
		assertSingleResult("Vern691search", fldName, "vern691y");
		assertSingleResult("Vern696search", fldName, "vern696y");
		assertSingleResult("Vern697search", fldName, "vern697y");
		assertSingleResult("Vern698search", fldName, "vern698y");
		assertSingleResult("Vern699search", fldName, "vern699y");
		
		assertZeroResults(fldName, "vern600a");
		assertZeroResults(fldName, "vern610a");
		assertZeroResults(fldName, "vern611a");
		assertZeroResults(fldName, "vern630a");
		assertZeroResults(fldName, "vern650a");
		assertZeroResults(fldName, "vern651a");
		assertZeroResults(fldName, "vern653a");
		assertZeroResults(fldName, "vern654a");
		assertZeroResults(fldName, "vern655a");
		assertZeroResults(fldName, "vern656a");
		assertZeroResults(fldName, "vern657a");
		assertZeroResults(fldName, "vern658a");
		assertZeroResults(fldName, "vern690a");
		assertZeroResults(fldName, "vern691a");
		assertZeroResults(fldName, "vern696a");
		assertZeroResults(fldName, "vern697a");
		assertZeroResults(fldName, "vern698a");
		assertZeroResults(fldName, "vern699a");
	}


	/**
	 * Test removal of 650a nomesh and 655a from Lane records.
	 */
@Test
	public final void testLaneBlacklistedTopics() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    createIxInitVars("subjectLaneBlacklistTests.mrc");

	    // 650a nomesh  (topic_search, topic_facet)
	    String fldName = "topic_search";
	    assertZeroResults(fldName, "nomesh");
	    assertSingleResult("650a", fldName, "rock");
	    fldName = "topic_facet";
	    assertZeroResults(fldName, "nomesh");
	    assertSingleResult("650a", fldName, "\"I am a rock\"");
	    
	    // 655a subject_other_search, topic_facet
	    fldName = "subject_other_search";
	    assertZeroResults(fldName, "\"internet resource\"");
	    assertZeroResults(fldName, "internet");
	    assertZeroResults(fldName, "resource");
	    assertZeroResults(fldName, "fulltext");
	    assertZeroResults(fldName, "noexport");
	    assertSingleResult("655b", fldName, "\"be bee be\"");
	    assertSingleResult("655keepme", fldName, "\"keep me\"");
	    fldName = "topic_facet";
	    assertZeroResults(fldName, "\"Internet Resource\"");
	    assertZeroResults(fldName, "Fulltext");
	    assertZeroResults(fldName, "NoExport");
	    // 655b not in topic_facet
	    // assertSingleResult("655b", fldName, "\"be bee be\"");
	    assertSingleResult("655keepme", fldName, "\"keep me\"");
	}


	/**
	 * Test population and properties of topic_facet field
	 *  topic_facet is a copy field from topic ... an untokenized string without
	 *  of trailing punctuation 
	 */
@Test
	public final void testTopicFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(testDataFname);
        String fldName = "topic_facet";
//        assertFacetFieldProperties(fldName);
//		assertFieldMultiValued(fldName);
		
		// topic_facet is a copy field from topic_facet, but it is a string
		// topic = 600abcdq:600t:610ab:610t:630a:630t:650a:655a
		
		// 600a, trailing period removed
		assertSingleResult("345228", fldName, "\"Zemnukhov, Ivan\""); 
		assertZeroResults(fldName, "\"Zemnukhov, Ivan.\"");
		// 600acd, trailing period removed
		assertSingleResult("1261173", fldName, "\"Somers, John Somers, Baron, 1651-1716\"");  
		assertZeroResults(fldName, "\"Somers, John Somers, Baron, 1651-1716.\""); 
		// 600ad, trailing comma removed
		assertSingleResult("600trailingComma", fldName, "\"Monroe, Marilyn, 1926-1962\""); 
		assertZeroResults(fldName, "\"Monroe, Marilyn, 1926-1962,\"");
		// 600q now bundled with abcdq
		assertSingleResult("600aqdx", fldName, "\"Kennedy, John F. (John Fitzgerald), 1917-1963\""); 
		assertZeroResults(fldName, "\"(John Fitzgerald),\"");
		assertZeroResults(fldName, "\"(John Fitzgerald)\"");
		assertZeroResults(fldName, "\"Kennedy, John F. 1917-1963\"");
		// 600t, too few letters at end to remove trailing period
		assertSingleResult("1261173", fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it.\"");  // 630
		assertZeroResults(fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it\"");  // 630
		// 600p no longer included
		assertZeroResults(fldName, "\"Meditation;\"");
		assertZeroResults(fldName, "\"Meditation\"");
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963. Meditation\"");
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963. Meditation;\"");
		// 600ad 
		assertSingleResult("600adtpof", fldName, "\"Hindemith, Paul, 1895-1963\"");
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963.\"");
		// 600t separate
		assertSingleResult("600adtpof", fldName, "\"Nobilissima visione\""); 
		assertZeroResults(fldName, "\"Nobilissima visione.\"");
		
		// 610ab, trailing period removed
		assertSingleResult("1261173", fldName, "\"England and Wales. Army\"");
		assertZeroResults(fldName, "\"England and Wales. Army.\"");
		assertSingleResult("610trailing", fldName, "\"Augusta (Ga.)\""); 
		assertZeroResults(fldName, "\"Augusta (Ga.).\"");  
		// 610t separate
		assertSingleResult("610atpv", fldName, "\"Reports\""); 
		assertZeroResults(fldName, "\"Reports.\"");
		assertSingleResult("610atpv", fldName, "\"United States Strategic Bombing Survey\""); 
		assertZeroResults(fldName, "\"United States Strategic Bombing Survey.\"");
		// 610p no longer included
		assertZeroResults(fldName, "\"Pacific war\"");
		assertZeroResults(fldName, "\"United States Strategic Bombing Survey Pacific war\"");
		
		// 630a, trailing period
		assertSingleResult("1261173", fldName, "\"Magna Carta\"");  
		assertZeroResults(fldName, "\"Magna Carta.\"");  
		// 630p - no longer included
		assertZeroResults(fldName, "\"N.T.\""); 
		assertZeroResults(fldName, "\"N.T\"");  
		// 630s no longer included
		assertZeroResults(fldName, "\"Vulgate\"");  
		assertZeroResults(fldName, "\"Vulgate.\"");  
		Set<String> docIds = new HashSet<String>();
		docIds.add("630alsf");
		docIds.add("630trailing");
		assertSearchResults(fldName, "\"Bible\"", docIds);
		assertZeroResults(fldName, "\"Bible.\"");  

		// 650a, trailing period
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"");
		assertZeroResults(fldName, "\"Literature, Comparative.\"");  
		// 650a, trailing comma
		assertSingleResult("650trailingComma", fldName, "\"Seabiscuit (Race horse)\""); 
		assertZeroResults(fldName, "\"Seabiscuit (Race horse),\"");  
		assertZeroResults(fldName, "\"Seabiscuit (Race horse\"");  
		// 650a, trailing paren left in
		assertSingleResult("650trailing", fldName, "\"BASIC (Computer program language)\""); 
		assertZeroResults(fldName, "\"BASIC (Computer program language\"");
		
		// 655a, trailing period
		assertSingleResult("6551", fldName, "\"bust\""); 
		assertZeroResults(fldName, "\"bust.\"");
		// 655b no longer used
		assertZeroResults(fldName, "\"Laminated marblewood bust\"");
		assertZeroResults(fldName, "\"Laminated marblewood bust.\"");
		assertZeroResults(fldName, "\"Laminated marblewood\"");

// TODO: this should NOT be true once we have bad Lane topics out of the topic field
		docIds.clear();
		docIds.add("7233951");
		docIds.add("1132");
		docIds.add("1133");
		assertSearchResults(fldName, "\"Internet Resource\"", docIds);
	}

	/**
	 * topic_facet field should not be case sensitive (?)
	 */
//@Test
	public final void testTopicFacetCaseSensitive()
			throws ParserConfigurationException, IOException, SAXException
	{
		String fileName = "topicFacetCaseSensitive.mrc";
		createIxInitVars(fileName);
		mappingTestInit();
		String fldName = "topic_facet";
	    String testFilePath = testDataParentPath + File.separator + fileName;

	    solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, "what case am i?");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2", fldName, "WHAT CASE AM I?");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3", fldName, "What case am I?");
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("2");
		docIds.add("3");
		assertSearchResults(fldName, "\"what case am i?", docIds);
		assertSearchResults(fldName, "\"WHAT CASE AM I?\"", docIds);
		assertSearchResults(fldName, "\"What case am I?\"", docIds);
	}


	/**
	 * Test population of geographic_facet field.  No trailing periods or 
	 *  commas.
	 */
@Test
	public final void testGeographicFacet()
			throws ParserConfigurationException, IOException, SAXException
	{
        createIxInitVars(testDataFname);
		String fldName = "geographic_facet";
//		assertFacetFieldProperties(fldName);
//		assertFieldMultiValued(fldName);
	
	    // trailing period should be stripped
		Set<String> docIds = new HashSet<String>();
		docIds.add("651a");
		docIds.add("651again");
		assertSearchResults(fldName, "Muppets", docIds);
		assertSingleResult("651numPeriod", fldName, "7.150");
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y.)\"");
		assertZeroResults(fldName, "\"Syracuse (N.Y.\"");
		assertZeroResults(fldName, "\"Syracuse (N.Y\"");
		assertSingleResult("651siberia", fldName, "\"Siberia (Russia)\"");
		assertZeroResults(fldName, "\"Siberia (Russia).\"");
	}

	/**
	 * Test population of era_facet field.  Trailing punctionation stripped.
	 */
@Test
	public final void testEraFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "era_facet";
		createIxInitVars("eraTests.mrc");
//		assertFacetFieldProperties(fldName);
//		assertFieldMultiValued(fldName);
	
	    // trailing period should be stripped
		Set<String> docIds = new HashSet<String>();
		docIds.add("650y");
		docIds.add("666");
		assertSearchResults(fldName, "\"20th century\"", docIds);
	    
	    // trailing period stripped after 3 digit year
		docIds.clear();
		docIds.add("888");
		docIds.add("999");
		assertSearchResults(fldName, "\"To 449\"", docIds);
	    assertZeroResults(fldName, "\"To 449.\"");
	    
	    // trailing period stripped after 4 digit year
	    assertSingleResult("111", fldName, "\"449-1066\"");
	    
	    // trailing dash - period NOT stripped
	    assertSingleResult("222", fldName, "\"1921-\"");
	    
	    // trailing period NOT stripped
	    assertSingleResult("777", fldName, "\"Roman period, 55 B.C.-449 A.D.\"");	    
	}

}
