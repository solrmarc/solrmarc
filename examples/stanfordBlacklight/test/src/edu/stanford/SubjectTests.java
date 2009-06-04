package edu.stanford;

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
public class SubjectTests extends BibIndexTest {
	
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
		assertSearchFldMultValProps(fldName, solrCore, sis);

		// all subfields except v, x, y and z from  650, 690, 653, 654
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("650search");
		docIds.add("Vern650search");
		assertSearchResults(fldName, "650a", docIds, sis);
		assertSingleResult("650search", fldName, "650b", sis);
		assertSingleResult("650search", fldName, "650c", sis);
		assertSingleResult("650search", fldName, "650d", sis);
		assertSingleResult("650search", fldName, "650e", sis);		
		assertZeroResults(fldName, "650v", sis);
		assertZeroResults(fldName, "650x", sis);
		assertZeroResults(fldName, "650y", sis);
		assertZeroResults(fldName, "650z", sis);

		docIds.clear();
		docIds.add("690search");
		docIds.add("Vern690search");
		assertSearchResults(fldName, "690a", docIds, sis);
		assertSingleResult("690search", fldName, "690b", sis);
		assertSingleResult("690search", fldName, "690c", sis);
		assertSingleResult("690search", fldName, "690d", sis);
		assertSingleResult("690search", fldName, "690e", sis);		
		assertZeroResults(fldName, "690v", sis);
		assertZeroResults(fldName, "690x", sis);
		assertZeroResults(fldName, "690y", sis);
		assertZeroResults(fldName, "690z", sis);

		docIds.clear();
		docIds.add("653search");
		docIds.add("Vern653search");
		assertSearchResults(fldName, "653a", docIds, sis);
		assertZeroResults(fldName, "653v", sis);
		assertZeroResults(fldName, "653x", sis);
		assertZeroResults(fldName, "653y", sis);
		assertZeroResults(fldName, "653z", sis);

		docIds.clear();
		docIds.add("654search");
		docIds.add("Vern654search");
		assertSearchResults(fldName, "654a", docIds, sis);
		assertSingleResult("654search", fldName, "654b", sis);
		assertSingleResult("654search", fldName, "654c", sis);
		assertSingleResult("654search", fldName, "654e", sis);		
		assertZeroResults(fldName, "654v", sis);
		assertZeroResults(fldName, "654x", sis);
		assertZeroResults(fldName, "654y", sis);
		assertZeroResults(fldName, "654z", sis);
		
        createIxInitVars(testDataFname);
		assertSingleResult("1261173", fldName, "army", sis);  // 650a
		assertSingleResult("4698973", fldName, "Multiculturalism", sis);  // 650a
		assertSingleResult("4698973", fldName, "\"Flyby missions\"", sis);  // 650-2
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"", sis);  // 650a
		assertSingleResult("4698973", fldName, "Multiculturalism", sis);  // 650a
		// multiple occurrences when there are multiple MARC fields with the same tag
		assertSingleResult("229800", fldName, "Commodity exchanges.", sis); 
		assertSingleResult("229800", fldName, "Foreign exchange.", sis); 
		assertZeroResults(fldName, "nasat", sis); // 650-2
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y and z from  650, 690, 653, 654
		
		assertSingleResult("Vern650search", fldName, "vern650a", sis);
		assertSingleResult("Vern650search", fldName, "vern650b", sis);
		assertSingleResult("Vern650search", fldName, "vern650c", sis);
		assertSingleResult("Vern650search", fldName, "vern650d", sis);
		assertSingleResult("Vern650search", fldName, "vern650e", sis);		
		assertZeroResults(fldName, "vern650v", sis);
		assertZeroResults(fldName, "vern650x", sis);
		assertZeroResults(fldName, "vern650y", sis);
		assertZeroResults(fldName, "vern650z", sis);
	
		assertSingleResult("Vern690search", fldName, "vern690a", sis);
		assertSingleResult("Vern690search", fldName, "vern690b", sis);
		assertSingleResult("Vern690search", fldName, "vern690c", sis);
		assertSingleResult("Vern690search", fldName, "vern690d", sis);
		assertSingleResult("Vern690search", fldName, "vern690e", sis);		
		assertZeroResults(fldName, "vern690v", sis);
		assertZeroResults(fldName, "vern690x", sis);
		assertZeroResults(fldName, "vern690y", sis);
		assertZeroResults(fldName, "vern690z", sis);
	
		assertSingleResult("Vern653search", fldName, "vern653a", sis);
		assertZeroResults(fldName, "vern653v", sis);
		assertZeroResults(fldName, "vern653x", sis);
		assertZeroResults(fldName, "vern653y", sis);
		assertZeroResults(fldName, "vern653z", sis);
	
		assertSingleResult("Vern654search", fldName, "654a", sis);
		assertSingleResult("Vern654search", fldName, "654b", sis);
		assertSingleResult("Vern654search", fldName, "654c", sis);
		assertSingleResult("Vern654search", fldName, "654e", sis);		
		assertZeroResults(fldName, "654v", sis);
		assertZeroResults(fldName, "654x", sis);
		assertZeroResults(fldName, "654y", sis);
		assertZeroResults(fldName, "654z", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// subfield x from all subject fields
		assertSingleResult("600search", fldName, "600x", sis);
		assertSingleResult("610search", fldName, "610x", sis);
		assertSingleResult("611search", fldName, "611x", sis);
		assertSingleResult("630search", fldName, "630x", sis);
		assertSingleResult("650search", fldName, "650x", sis);
		assertSingleResult("651search", fldName, "651x", sis);
		// no sub x in 653, 654
		assertSingleResult("655search", fldName, "655x", sis);
		assertSingleResult("656search", fldName, "656x", sis);
		assertSingleResult("657search", fldName, "657x", sis);
		// no sub x in 658
		assertSingleResult("690search", fldName, "690x", sis);
		assertSingleResult("691search", fldName, "691x", sis);
		assertSingleResult("696search", fldName, "696x", sis);
		assertSingleResult("697search", fldName, "697x", sis);
		assertSingleResult("698search", fldName, "698x", sis);
		assertSingleResult("699search", fldName, "699x", sis);

		assertZeroResults(fldName, "600a", sis);
		assertZeroResults(fldName, "610a", sis);
		assertZeroResults(fldName, "611a", sis);
		assertZeroResults(fldName, "630a", sis);
		assertZeroResults(fldName, "650a", sis);
		assertZeroResults(fldName, "651a", sis);
		assertZeroResults(fldName, "653a", sis);
		assertZeroResults(fldName, "654a", sis);
		assertZeroResults(fldName, "655a", sis);
		assertZeroResults(fldName, "656a", sis);
		assertZeroResults(fldName, "657a", sis);
		assertZeroResults(fldName, "658a", sis);
		assertZeroResults(fldName, "690a", sis);
		assertZeroResults(fldName, "691a", sis);
		assertZeroResults(fldName, "696a", sis);
		assertZeroResults(fldName, "697a", sis);
		assertZeroResults(fldName, "698a", sis);
		assertZeroResults(fldName, "699a", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// subfield x from all subject fields
		assertSingleResult("Vern600search", fldName, "vern600x", sis);
		assertSingleResult("Vern610search", fldName, "vern610x", sis);
		assertSingleResult("Vern611search", fldName, "vern611x", sis);
		assertSingleResult("Vern630search", fldName, "vern630x", sis);
		assertSingleResult("Vern650search", fldName, "vern650x", sis);
		assertSingleResult("Vern651search", fldName, "vern651x", sis);
		// no sub x in 653, 654
		assertSingleResult("Vern655search", fldName, "vern655x", sis);
		assertSingleResult("Vern656search", fldName, "vern656x", sis);
		assertSingleResult("Vern657search", fldName, "vern657x", sis);
		// no sub x in 658
		assertSingleResult("Vern690search", fldName, "vern690x", sis);
		assertSingleResult("Vern691search", fldName, "vern691x", sis);
		assertSingleResult("Vern696search", fldName, "vern696x", sis);
		assertSingleResult("Vern697search", fldName, "vern697x", sis);
		assertSingleResult("Vern698search", fldName, "vern698x", sis);
		assertSingleResult("Vern699search", fldName, "vern699x", sis);
	
		assertZeroResults(fldName, "vern600a", sis);
		assertZeroResults(fldName, "vern610a", sis);
		assertZeroResults(fldName, "vern611a", sis);
		assertZeroResults(fldName, "vern630a", sis);
		assertZeroResults(fldName, "vern650a", sis);
		assertZeroResults(fldName, "vern651a", sis);
		assertZeroResults(fldName, "vern653a", sis);
		assertZeroResults(fldName, "vern654a", sis);
		assertZeroResults(fldName, "vern655a", sis);
		assertZeroResults(fldName, "vern656a", sis);
		assertZeroResults(fldName, "vern657a", sis);
		assertZeroResults(fldName, "vern658a", sis);
		assertZeroResults(fldName, "vern690a", sis);
		assertZeroResults(fldName, "vern691a", sis);
		assertZeroResults(fldName, "vern696a", sis);
		assertZeroResults(fldName, "vern697a", sis);
		assertZeroResults(fldName, "vern698a", sis);
		assertZeroResults(fldName, "vern699a", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from  651, 691
		Set<String> docIds = new HashSet<String>();
		docIds.add("651search");
		docIds.add("Vern651search");
		assertSearchResults(fldName, "651a", docIds, sis);
		assertSingleResult("651search", fldName, "651e", sis);		
		assertZeroResults(fldName, "651v", sis);
		assertZeroResults(fldName, "651x", sis);
		assertZeroResults(fldName, "651y", sis);
		assertZeroResults(fldName, "651z", sis);

		docIds.clear();
		docIds.add("691search");
		docIds.add("Vern691search");
		assertSearchResults(fldName, "691a", docIds, sis);
		assertSingleResult("691search", fldName, "691e", sis);		
		assertZeroResults(fldName, "691v", sis);
		assertZeroResults(fldName, "691x", sis);
		assertZeroResults(fldName, "691y", sis);
		assertZeroResults(fldName, "691z", sis);
		
	    createIxInitVars(testDataFname);
	    docIds.clear();
		docIds.add("651a");
		docIds.add("651again");
		assertSearchResults(fldName, "muppets", docIds, sis);
		// geographic punctuation shouldn't matter
		assertSingleResult("651numPeriod", fldName, "7.150", sis);
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y.)\"", sis);
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y.\"", sis);
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y\"", sis);
		assertSingleResult("651siberia", fldName, "\"Siberia (Russia)\"", sis);
		assertSingleResult("651siberia", fldName, "\"Siberia (Russia).\"", sis);
		// 651a
		assertSingleResult("6280316", fldName, "Tennessee", sis); 
		assertSingleResult("6280316", fldName, "Arkansas", sis); 
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from  651, 691
		assertSingleResult("Vern651search", fldName, "vern651a", sis);		
		assertSingleResult("Vern651search", fldName, "vern651e", sis);		
		assertZeroResults(fldName, "vern651v", sis);
		assertZeroResults(fldName, "vern651x", sis);
		assertZeroResults(fldName, "vern651y", sis);
		assertZeroResults(fldName, "vern651z", sis);
	
		assertSingleResult("Vern691search", fldName, "vern691a", sis);		
		assertSingleResult("Vern691search", fldName, "vern691e", sis);		
		assertZeroResults(fldName, "vern691v", sis);
		assertZeroResults(fldName, "vern691x", sis);
		assertZeroResults(fldName, "vern691y", sis);
		assertZeroResults(fldName, "vern691z", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// subfield z from all subject fields
		assertSingleResult("600search", fldName, "600z", sis);
		assertSingleResult("610search", fldName, "610z", sis);
		// no sub z in 611
		assertSingleResult("630search", fldName, "630z", sis);
		assertSingleResult("650search", fldName, "650z", sis);
		assertSingleResult("651search", fldName, "651z", sis);
		// no sub z in 653
		assertSingleResult("654search", fldName, "654z", sis);
		assertSingleResult("655search", fldName, "655z", sis);
		assertSingleResult("656search", fldName, "656z", sis);
		assertSingleResult("657search", fldName, "657z", sis);
		// no sub z in 658
		assertSingleResult("690search", fldName, "690z", sis);
		assertSingleResult("691search", fldName, "691z", sis);
		assertSingleResult("696search", fldName, "696z", sis);
		assertSingleResult("697search", fldName, "697z", sis);
		assertSingleResult("698search", fldName, "698z", sis);
		assertSingleResult("699search", fldName, "699z", sis);

		assertZeroResults(fldName, "600a", sis);
		assertZeroResults(fldName, "610a", sis);
		assertZeroResults(fldName, "611a", sis);
		assertZeroResults(fldName, "630a", sis);
		assertZeroResults(fldName, "650a", sis);
		assertZeroResults(fldName, "651a", sis);
		assertZeroResults(fldName, "653a", sis);
		assertZeroResults(fldName, "654a", sis);
		assertZeroResults(fldName, "655a", sis);
		assertZeroResults(fldName, "656a", sis);
		assertZeroResults(fldName, "657a", sis);
		assertZeroResults(fldName, "658a", sis);
		assertZeroResults(fldName, "690a", sis);
		assertZeroResults(fldName, "691a", sis);
		assertZeroResults(fldName, "696a", sis);
		assertZeroResults(fldName, "697a", sis);
		assertZeroResults(fldName, "698a", sis);
		assertZeroResults(fldName, "699a", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// subfield z from all subject fields
		assertSingleResult("Vern600search", fldName, "vern600z", sis);
		assertSingleResult("Vern610search", fldName, "vern610z", sis);
		// no sub z in 611
		assertSingleResult("Vern630search", fldName, "vern630z", sis);
		assertSingleResult("Vern650search", fldName, "vern650z", sis);
		assertSingleResult("Vern651search", fldName, "vern651z", sis);
		// no sub z in 653
		assertSingleResult("Vern654search", fldName, "vern654z", sis);
		assertSingleResult("Vern655search", fldName, "vern655z", sis);
		assertSingleResult("Vern656search", fldName, "vern656z", sis);
		assertSingleResult("Vern657search", fldName, "vern657z", sis);
		// no sub z in 658
		assertSingleResult("Vern690search", fldName, "vern690z", sis);
		assertSingleResult("Vern691search", fldName, "vern691z", sis);
		assertSingleResult("Vern696search", fldName, "vern696z", sis);
		assertSingleResult("Vern697search", fldName, "vern697z", sis);
		assertSingleResult("Vern698search", fldName, "vern698z", sis);
		assertSingleResult("Vern699search", fldName, "vern699z", sis);
	
		assertZeroResults(fldName, "vern600a", sis);
		assertZeroResults(fldName, "vern610a", sis);
		assertZeroResults(fldName, "vern611a", sis);
		assertZeroResults(fldName, "vern630a", sis);
		assertZeroResults(fldName, "vern650a", sis);
		assertZeroResults(fldName, "vern651a", sis);
		assertZeroResults(fldName, "vern653a", sis);
		assertZeroResults(fldName, "vern654a", sis);
		assertZeroResults(fldName, "vern655a", sis);
		assertZeroResults(fldName, "vern656a", sis);
		assertZeroResults(fldName, "vern657a", sis);
		assertZeroResults(fldName, "vern658a", sis);
		assertZeroResults(fldName, "vern690a", sis);
		assertZeroResults(fldName, "vern691a", sis);
		assertZeroResults(fldName, "vern696a", sis);
		assertZeroResults(fldName, "vern697a", sis);
		assertZeroResults(fldName, "vern698a", sis);
		assertZeroResults(fldName, "vern699a", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from  600, 610-11, 630, 655-58, 696-699
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("600search");
		docIds.add("Vern600search");
		assertSearchResults(fldName, "600a", docIds, sis);
		assertSingleResult("600search", fldName, "600b", sis);
		assertSingleResult("600search", fldName, "600c", sis);
		assertSingleResult("600search", fldName, "600d", sis);
		assertSingleResult("600search", fldName, "600e", sis);		
		assertSingleResult("600search", fldName, "600f", sis);		
		assertSingleResult("600search", fldName, "600g", sis);		
		assertSingleResult("600search", fldName, "600h", sis);		
		assertSingleResult("600search", fldName, "600j", sis);		
		assertSingleResult("600search", fldName, "600k", sis);		
		assertSingleResult("600search", fldName, "600l", sis);		
		assertSingleResult("600search", fldName, "600m", sis);		
		assertSingleResult("600search", fldName, "600n", sis);		
		assertSingleResult("600search", fldName, "600o", sis);		
		assertSingleResult("600search", fldName, "600p", sis);		
		assertSingleResult("600search", fldName, "600q", sis);		
		assertSingleResult("600search", fldName, "600r", sis);		
		assertSingleResult("600search", fldName, "600s", sis);		
		assertSingleResult("600search", fldName, "600t", sis);		
		assertSingleResult("600search", fldName, "600u", sis);		
		assertZeroResults(fldName, "600v", sis);
		assertZeroResults(fldName, "600x", sis);
		assertZeroResults(fldName, "600y", sis);
		assertZeroResults(fldName, "600z", sis);

		docIds.clear();
		docIds.add("610search");
		docIds.add("Vern610search");
		assertSearchResults(fldName, "610a", docIds, sis);
		assertSingleResult("610search", fldName, "610b", sis);
		assertSingleResult("610search", fldName, "610c", sis);
		assertSingleResult("610search", fldName, "610d", sis);
		assertSingleResult("610search", fldName, "610e", sis);	
		assertSingleResult("610search", fldName, "610f", sis);		
		assertSingleResult("610search", fldName, "610g", sis);		
		assertSingleResult("610search", fldName, "610h", sis);		
		assertSingleResult("610search", fldName, "610k", sis);		
		assertSingleResult("610search", fldName, "610l", sis);		
		assertSingleResult("610search", fldName, "610m", sis);		
		assertSingleResult("610search", fldName, "610n", sis);		
		assertSingleResult("610search", fldName, "610o", sis);		
		assertSingleResult("610search", fldName, "610p", sis);		
		assertSingleResult("610search", fldName, "610r", sis);		
		assertSingleResult("610search", fldName, "610s", sis);		
		assertSingleResult("610search", fldName, "610t", sis);		
		assertSingleResult("610search", fldName, "610u", sis);		
		assertZeroResults(fldName, "610v", sis);
		assertZeroResults(fldName, "610x", sis);
		assertZeroResults(fldName, "610y", sis);
		assertZeroResults(fldName, "610z", sis);

		docIds.clear();
		docIds.add("611search");
		docIds.add("Vern611search");
		assertSearchResults(fldName, "611a", docIds, sis);
		assertSingleResult("611search", fldName, "611c", sis);
		assertSingleResult("611search", fldName, "611d", sis);
		assertSingleResult("611search", fldName, "611e", sis);		
		assertSingleResult("611search", fldName, "611f", sis);		
		assertSingleResult("611search", fldName, "611g", sis);		
		assertSingleResult("611search", fldName, "611h", sis);		
		assertSingleResult("611search", fldName, "611j", sis);		
		assertSingleResult("611search", fldName, "611k", sis);		
		assertSingleResult("611search", fldName, "611l", sis);		
		assertSingleResult("611search", fldName, "611n", sis);		
		assertSingleResult("611search", fldName, "611p", sis);		
		assertSingleResult("611search", fldName, "611q", sis);		
		assertSingleResult("611search", fldName, "611s", sis);		
		assertSingleResult("611search", fldName, "611t", sis);		
		assertSingleResult("611search", fldName, "611u", sis);		
		assertZeroResults(fldName, "611v", sis);
		assertZeroResults(fldName, "611x", sis);
		assertZeroResults(fldName, "611y", sis);
		assertZeroResults(fldName, "611z", sis);
		
        createIxInitVars(testDataFname);
		assertSingleResult("3743949", fldName, "Federico", sis); // 600a
		assertSingleResult("3743949", fldName, "1936", sis);  // 600d
		assertSingleResult("919006", fldName, "Emesa", sis);  // 600c
		assertSingleResult("1261173", fldName, "peace", sis);  // 600t
		assertSingleResult("1261173", fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it\"", sis);  // 600t
		assertSingleResult("115472", fldName, "Economic", sis);  // 610a
		assertSingleResult("115472", fldName, "\"European Economic Community\"", sis);  // 610a
		assertSingleResult("1261173", fldName, "army", sis);  // 610b
		
		assertZeroResults(fldName, "\"Zhongguo gong chan dang Party work.\"", sis);  // 610-6
		assertSingleResult("345228", fldName, "\"Zemnukhov, Ivan\"", sis); // 600a
		assertSingleResult("3743949", fldName, "Federico", sis); // 600a
		assertSingleResult("3743949", fldName, "1936", sis);  // 600d
		assertSingleResult("919006", fldName, "Emesa", sis);  // 600c
		assertSingleResult("1261173", fldName, "peace", sis);  // 600t
		assertSingleResult("115472", fldName, "Economic", sis);  // 610a
		assertSingleResult("1261173", fldName, "army", sis);  // 610b
		
		// default operator is OR, since dismax will make it effectively AND
		docIds.clear();
		docIds.add("6553");
		docIds.add("610atpv");
		assertSearchResults(fldName, "report", docIds, sis);		
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from  600, 610-11, 630, 655-58, 696-699
	
		assertSingleResult("Vern600search", fldName, "vern600a", sis);
		assertSingleResult("Vern600search", fldName, "vern600b", sis);
		assertSingleResult("Vern600search", fldName, "vern600c", sis);
		assertSingleResult("Vern600search", fldName, "vern600d", sis);
		assertSingleResult("Vern600search", fldName, "vern600e", sis);		
		assertSingleResult("Vern600search", fldName, "vern600f", sis);		
		assertSingleResult("Vern600search", fldName, "vern600g", sis);		
		assertSingleResult("Vern600search", fldName, "vern600h", sis);		
		assertSingleResult("Vern600search", fldName, "vern600j", sis);		
		assertSingleResult("Vern600search", fldName, "vern600k", sis);		
		assertSingleResult("Vern600search", fldName, "vern600l", sis);		
		assertSingleResult("Vern600search", fldName, "vern600m", sis);		
		assertSingleResult("Vern600search", fldName, "vern600n", sis);		
		assertSingleResult("Vern600search", fldName, "vern600o", sis);		
		assertSingleResult("Vern600search", fldName, "vern600p", sis);		
		assertSingleResult("Vern600search", fldName, "vern600q", sis);		
		assertSingleResult("Vern600search", fldName, "vern600r", sis);		
		assertSingleResult("Vern600search", fldName, "vern600s", sis);		
		assertSingleResult("Vern600search", fldName, "vern600t", sis);		
		assertSingleResult("Vern600search", fldName, "vern600u", sis);		
		assertZeroResults(fldName, "vern600v", sis);
		assertZeroResults(fldName, "vern600x", sis);
		assertZeroResults(fldName, "vern600y", sis);
		assertZeroResults(fldName, "vern600z", sis);
	
		assertSingleResult("Vern610search", fldName, "vern610a", sis);
		assertSingleResult("Vern610search", fldName, "vern610b", sis);
		assertSingleResult("Vern610search", fldName, "vern610c", sis);
		assertSingleResult("Vern610search", fldName, "vern610d", sis);
		assertSingleResult("Vern610search", fldName, "vern610e", sis);	
		assertSingleResult("Vern610search", fldName, "vern610f", sis);		
		assertSingleResult("Vern610search", fldName, "vern610g", sis);		
		assertSingleResult("Vern610search", fldName, "vern610h", sis);		
		assertSingleResult("Vern610search", fldName, "vern610k", sis);		
		assertSingleResult("Vern610search", fldName, "vern610l", sis);		
		assertSingleResult("Vern610search", fldName, "vern610m", sis);		
		assertSingleResult("Vern610search", fldName, "vern610n", sis);		
		assertSingleResult("Vern610search", fldName, "vern610o", sis);		
		assertSingleResult("Vern610search", fldName, "vern610p", sis);		
		assertSingleResult("Vern610search", fldName, "vern610r", sis);		
		assertSingleResult("Vern610search", fldName, "vern610s", sis);		
		assertSingleResult("Vern610search", fldName, "vern610t", sis);		
		assertSingleResult("Vern610search", fldName, "vern610u", sis);		
		assertZeroResults(fldName, "vern610v", sis);
		assertZeroResults(fldName, "vern610x", sis);
		assertZeroResults(fldName, "vern610y", sis);
		assertZeroResults(fldName, "vern610z", sis);
	
		assertSingleResult("Vern611search", fldName, "vern611a", sis);
		assertSingleResult("Vern611search", fldName, "vern611c", sis);
		assertSingleResult("Vern611search", fldName, "vern611d", sis);
		assertSingleResult("Vern611search", fldName, "vern611e", sis);		
		assertSingleResult("Vern611search", fldName, "vern611f", sis);		
		assertSingleResult("Vern611search", fldName, "vern611g", sis);		
		assertSingleResult("Vern611search", fldName, "vern611h", sis);		
		assertSingleResult("Vern611search", fldName, "vern611j", sis);		
		assertSingleResult("Vern611search", fldName, "vern611k", sis);		
		assertSingleResult("Vern611search", fldName, "vern611l", sis);		
		assertSingleResult("Vern611search", fldName, "vern611n", sis);		
		assertSingleResult("Vern611search", fldName, "vern611p", sis);		
		assertSingleResult("Vern611search", fldName, "vern611q", sis);		
		assertSingleResult("Vern611search", fldName, "vern611s", sis);		
		assertSingleResult("Vern611search", fldName, "vern611t", sis);		
		assertSingleResult("Vern611search", fldName, "vern611u", sis);		
		assertZeroResults(fldName, "vern611v", sis);
		assertZeroResults(fldName, "vern611x", sis);
		assertZeroResults(fldName, "vern611y", sis);
		assertZeroResults(fldName, "vern611z", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from 630
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("630search");
		docIds.add("Vern630search");
		assertSearchResults(fldName, "630a", docIds, sis);
		assertSingleResult("630search", fldName, "630d", sis);
		assertSingleResult("630search", fldName, "630e", sis);		
		assertSingleResult("630search", fldName, "630f", sis);		
		assertSingleResult("630search", fldName, "630g", sis);		
		assertSingleResult("630search", fldName, "630h", sis);		
		assertSingleResult("630search", fldName, "630k", sis);		
		assertSingleResult("630search", fldName, "630l", sis);		
		assertSingleResult("630search", fldName, "630m", sis);		
		assertSingleResult("630search", fldName, "630n", sis);		
		assertSingleResult("630search", fldName, "630o", sis);		
		assertSingleResult("630search", fldName, "630p", sis);		
		assertSingleResult("630search", fldName, "630r", sis);		
		assertSingleResult("630search", fldName, "630s", sis);		
		assertSingleResult("630search", fldName, "630t", sis);		
		assertZeroResults(fldName, "630v", sis);
		assertZeroResults(fldName, "630x", sis);
		assertZeroResults(fldName, "630y", sis);
		assertZeroResults(fldName, "630z", sis);
		
        createIxInitVars(testDataFname);
		assertSingleResult("1261173", fldName, "magna carta", sis);  // 630a
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from 630
	
		assertSingleResult("Vern630search", fldName, "vern630a", sis);
		assertSingleResult("Vern630search", fldName, "vern630d", sis);
		assertSingleResult("Vern630search", fldName, "vern630e", sis);		
		assertSingleResult("Vern630search", fldName, "vern630f", sis);		
		assertSingleResult("Vern630search", fldName, "vern630g", sis);		
		assertSingleResult("Vern630search", fldName, "vern630h", sis);		
		assertSingleResult("Vern630search", fldName, "vern630k", sis);		
		assertSingleResult("Vern630search", fldName, "vern630l", sis);		
		assertSingleResult("Vern630search", fldName, "vern630m", sis);		
		assertSingleResult("Vern630search", fldName, "vern630n", sis);		
		assertSingleResult("Vern630search", fldName, "vern630o", sis);		
		assertSingleResult("Vern630search", fldName, "vern630p", sis);		
		assertSingleResult("Vern630search", fldName, "vern630r", sis);		
		assertSingleResult("Vern630search", fldName, "vern630s", sis);		
		assertSingleResult("Vern630search", fldName, "vern630t", sis);		
		assertZeroResults(fldName, "vern630v", sis);
		assertZeroResults(fldName, "vern630x", sis);
		assertZeroResults(fldName, "vern630y", sis);
		assertZeroResults(fldName, "vern630z", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from 655-58
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("655search");
		docIds.add("Vern655search");
		assertSearchResults(fldName, "655a", docIds, sis);
		assertSingleResult("655search", fldName, "655b", sis);
		assertSingleResult("655search", fldName, "655c", sis);
		assertZeroResults(fldName, "655v", sis);
		assertZeroResults(fldName, "655x", sis);
		assertZeroResults(fldName, "655y", sis);
		assertZeroResults(fldName, "655z", sis);
		
		docIds.clear();
		docIds.add("656search");
		docIds.add("Vern656search");
		assertSearchResults(fldName, "656a", docIds, sis);
		assertSingleResult("656search", fldName, "656k", sis);		
		assertZeroResults(fldName, "656v", sis);
		assertZeroResults(fldName, "656x", sis);
		assertZeroResults(fldName, "656y", sis);
		assertZeroResults(fldName, "656z", sis);
	
		docIds.clear();
		docIds.add("657search");
		docIds.add("Vern657search");
		assertSearchResults(fldName, "657a", docIds, sis);
		assertZeroResults(fldName, "657v", sis);
		assertZeroResults(fldName, "657x", sis);
		assertZeroResults(fldName, "657y", sis);
		assertZeroResults(fldName, "657z", sis);
	
		docIds.clear();
		docIds.add("658search");
		docIds.add("Vern658search");
		assertSearchResults(fldName, "658a", docIds, sis);
		assertSingleResult("658search", fldName, "658b", sis);
		assertSingleResult("658search", fldName, "658c", sis);
		assertSingleResult("658search", fldName, "658d", sis);
		// no sub v,x,y,z in 658
		
        createIxInitVars(testDataFname);
		assertSingleResult("6552", fldName, "dictionaries", sis);  // 655a
		
		assertSingleResult("6553", fldName, "\"Fire Reports\"", sis);  // 655-a
		assertZeroResults(fldName, "atlanta georgia 1978", sis);  // 655z
		assertSingleResult("7233951", fldName, "\"Lectures\"", sis);  // 655a
		assertZeroResults(fldName, "municipal", sis); // 655-3
		assertSingleResult("6552", fldName, "dictionaries", sis);  // 655a
		
		// default operator is OR, since dismax will make it effectively AND
		docIds.clear();
		docIds.add("6553");
		docIds.add("610atpv");
		assertSearchResults(fldName, "report", docIds, sis);		
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from 655-58
	
		assertSingleResult("Vern655search", fldName, "vern655a", sis);
		assertSingleResult("Vern655search", fldName, "vern655b", sis);
		assertSingleResult("Vern655search", fldName, "vern655c", sis);
		assertZeroResults(fldName, "vern655v", sis);
		assertZeroResults(fldName, "vern655x", sis);
		assertZeroResults(fldName, "vern655y", sis);
		assertZeroResults(fldName, "vern655z", sis);
	
		assertSingleResult("Vern656search", fldName, "vern656a", sis);
		assertSingleResult("Vern656search", fldName, "vern656k", sis);		
		assertZeroResults(fldName, "vern656v", sis);
		assertZeroResults(fldName, "vern656x", sis);
		assertZeroResults(fldName, "vern656y", sis);
		assertZeroResults(fldName, "vern656z", sis);
	
		assertSingleResult("Vern657search", fldName, "vern657a", sis);
		assertZeroResults(fldName, "vern657v", sis);
		assertZeroResults(fldName, "vern657x", sis);
		assertZeroResults(fldName, "vern657y", sis);
		assertZeroResults(fldName, "vern657z", sis);
	
		assertSingleResult("Vern658search", fldName, "vern658a", sis);
		assertSingleResult("Vern658search", fldName, "vern658b", sis);
		assertSingleResult("Vern658search", fldName, "vern658c", sis);
		assertSingleResult("Vern658search", fldName, "vern658d", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from 696-699
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("696search");
		docIds.add("Vern696search");
		assertSearchResults(fldName, "696a", docIds, sis);
		assertSingleResult("696search", fldName, "696b", sis);
		assertSingleResult("696search", fldName, "696c", sis);
		assertSingleResult("696search", fldName, "696d", sis);
		assertSingleResult("696search", fldName, "696e", sis);		
		assertSingleResult("696search", fldName, "696f", sis);		
		assertSingleResult("696search", fldName, "696g", sis);		
		assertSingleResult("696search", fldName, "696h", sis);		
		assertSingleResult("696search", fldName, "696j", sis);		
		assertSingleResult("696search", fldName, "696k", sis);		
		assertSingleResult("696search", fldName, "696l", sis);		
		assertSingleResult("696search", fldName, "696m", sis);		
		assertSingleResult("696search", fldName, "696n", sis);		
		assertSingleResult("696search", fldName, "696o", sis);		
		assertSingleResult("696search", fldName, "696p", sis);		
		assertSingleResult("696search", fldName, "696q", sis);		
		assertSingleResult("696search", fldName, "696r", sis);		
		assertSingleResult("696search", fldName, "696s", sis);		
		assertSingleResult("696search", fldName, "696t", sis);		
		assertSingleResult("696search", fldName, "696u", sis);		
		assertZeroResults(fldName, "696v", sis);
		assertZeroResults(fldName, "696x", sis);
		assertZeroResults(fldName, "696y", sis);
		assertZeroResults(fldName, "696z", sis);
	
		docIds.clear();
		docIds.add("697search");
		docIds.add("Vern697search");
		assertSearchResults(fldName, "697a", docIds, sis);
		assertSingleResult("697search", fldName, "697b", sis);
		assertSingleResult("697search", fldName, "697c", sis);
		assertSingleResult("697search", fldName, "697d", sis);
		assertSingleResult("697search", fldName, "697e", sis);		
		assertSingleResult("697search", fldName, "697f", sis);		
		assertSingleResult("697search", fldName, "697g", sis);		
		assertSingleResult("697search", fldName, "697h", sis);		
		assertSingleResult("697search", fldName, "697j", sis);		
		assertSingleResult("697search", fldName, "697k", sis);		
		assertSingleResult("697search", fldName, "697l", sis);		
		assertSingleResult("697search", fldName, "697m", sis);		
		assertSingleResult("697search", fldName, "697n", sis);		
		assertSingleResult("697search", fldName, "697o", sis);		
		assertSingleResult("697search", fldName, "697p", sis);		
		assertSingleResult("697search", fldName, "697q", sis);		
		assertSingleResult("697search", fldName, "697r", sis);		
		assertSingleResult("697search", fldName, "697s", sis);		
		assertSingleResult("697search", fldName, "697t", sis);		
		assertSingleResult("697search", fldName, "697u", sis);		
		assertZeroResults(fldName, "697v", sis);
		assertZeroResults(fldName, "697x", sis);
		assertZeroResults(fldName, "697y", sis);
		assertZeroResults(fldName, "697z", sis);
	
		docIds.clear();
		docIds.add("698search");
		docIds.add("Vern698search");
		assertSearchResults(fldName, "698a", docIds, sis);
		assertSingleResult("698search", fldName, "698b", sis);
		assertSingleResult("698search", fldName, "698c", sis);
		assertSingleResult("698search", fldName, "698d", sis);
		assertSingleResult("698search", fldName, "698e", sis);		
		assertSingleResult("698search", fldName, "698f", sis);		
		assertSingleResult("698search", fldName, "698g", sis);		
		assertSingleResult("698search", fldName, "698h", sis);		
		assertSingleResult("698search", fldName, "698j", sis);		
		assertSingleResult("698search", fldName, "698k", sis);		
		assertSingleResult("698search", fldName, "698l", sis);		
		assertSingleResult("698search", fldName, "698m", sis);		
		assertSingleResult("698search", fldName, "698n", sis);		
		assertSingleResult("698search", fldName, "698o", sis);		
		assertSingleResult("698search", fldName, "698p", sis);		
		assertSingleResult("698search", fldName, "698q", sis);		
		assertSingleResult("698search", fldName, "698r", sis);		
		assertSingleResult("698search", fldName, "698s", sis);		
		assertSingleResult("698search", fldName, "698t", sis);		
		assertSingleResult("698search", fldName, "698u", sis);		
		assertZeroResults(fldName, "698v", sis);
		assertZeroResults(fldName, "698x", sis);
		assertZeroResults(fldName, "698y", sis);
		assertZeroResults(fldName, "698z", sis);
	
		docIds.clear();
		docIds.add("699search");
		docIds.add("Vern699search");
		assertSearchResults(fldName, "699a", docIds, sis);
		assertSingleResult("699search", fldName, "699b", sis);
		assertSingleResult("699search", fldName, "699c", sis);
		assertSingleResult("699search", fldName, "699d", sis);
		assertSingleResult("699search", fldName, "699e", sis);		
		assertSingleResult("699search", fldName, "699f", sis);		
		assertSingleResult("699search", fldName, "699g", sis);		
		assertSingleResult("699search", fldName, "699h", sis);		
		assertSingleResult("699search", fldName, "699j", sis);		
		assertSingleResult("699search", fldName, "699k", sis);		
		assertSingleResult("699search", fldName, "699l", sis);		
		assertSingleResult("699search", fldName, "699m", sis);		
		assertSingleResult("699search", fldName, "699n", sis);		
		assertSingleResult("699search", fldName, "699o", sis);		
		assertSingleResult("699search", fldName, "699p", sis);		
		assertSingleResult("699search", fldName, "699q", sis);		
		assertSingleResult("699search", fldName, "699r", sis);		
		assertSingleResult("699search", fldName, "699s", sis);		
		assertSingleResult("699search", fldName, "699t", sis);		
		assertSingleResult("699search", fldName, "699u", sis);		
		assertZeroResults(fldName, "699v", sis);
		assertZeroResults(fldName, "699x", sis);
		assertZeroResults(fldName, "699y", sis);
		assertZeroResults(fldName, "699z", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// all subfields except v, x, y, z from 696-699
	
		assertSingleResult("Vern696search", fldName, "vern696a", sis);
		assertSingleResult("Vern696search", fldName, "vern696b", sis);
		assertSingleResult("Vern696search", fldName, "vern696c", sis);
		assertSingleResult("Vern696search", fldName, "vern696d", sis);
		assertSingleResult("Vern696search", fldName, "vern696e", sis);		
		assertSingleResult("Vern696search", fldName, "vern696f", sis);		
		assertSingleResult("Vern696search", fldName, "vern696g", sis);		
		assertSingleResult("Vern696search", fldName, "vern696h", sis);		
		assertSingleResult("Vern696search", fldName, "vern696j", sis);		
		assertSingleResult("Vern696search", fldName, "vern696k", sis);		
		assertSingleResult("Vern696search", fldName, "vern696l", sis);		
		assertSingleResult("Vern696search", fldName, "vern696m", sis);		
		assertSingleResult("Vern696search", fldName, "vern696n", sis);		
		assertSingleResult("Vern696search", fldName, "vern696o", sis);		
		assertSingleResult("Vern696search", fldName, "vern696p", sis);		
		assertSingleResult("Vern696search", fldName, "vern696q", sis);		
		assertSingleResult("Vern696search", fldName, "vern696r", sis);		
		assertSingleResult("Vern696search", fldName, "vern696s", sis);		
		assertSingleResult("Vern696search", fldName, "vern696t", sis);		
		assertSingleResult("Vern696search", fldName, "vern696u", sis);		
		assertZeroResults(fldName, "vern696v", sis);
		assertZeroResults(fldName, "vern696x", sis);
		assertZeroResults(fldName, "vern696y", sis);
		assertZeroResults(fldName, "vern696z", sis);
	
		assertSingleResult("Vern697search", fldName, "vern697a", sis);
		assertSingleResult("Vern697search", fldName, "vern697b", sis);
		assertSingleResult("Vern697search", fldName, "vern697c", sis);
		assertSingleResult("Vern697search", fldName, "vern697d", sis);
		assertSingleResult("Vern697search", fldName, "vern697e", sis);		
		assertSingleResult("Vern697search", fldName, "vern697f", sis);		
		assertSingleResult("Vern697search", fldName, "vern697g", sis);		
		assertSingleResult("Vern697search", fldName, "vern697h", sis);		
		assertSingleResult("Vern697search", fldName, "vern697j", sis);		
		assertSingleResult("Vern697search", fldName, "vern697k", sis);		
		assertSingleResult("Vern697search", fldName, "vern697l", sis);		
		assertSingleResult("Vern697search", fldName, "vern697m", sis);		
		assertSingleResult("Vern697search", fldName, "vern697n", sis);		
		assertSingleResult("Vern697search", fldName, "vern697o", sis);		
		assertSingleResult("Vern697search", fldName, "vern697p", sis);		
		assertSingleResult("Vern697search", fldName, "vern697q", sis);		
		assertSingleResult("Vern697search", fldName, "vern697r", sis);		
		assertSingleResult("Vern697search", fldName, "vern697s", sis);		
		assertSingleResult("Vern697search", fldName, "vern697t", sis);		
		assertSingleResult("Vern697search", fldName, "vern697u", sis);		
		assertZeroResults(fldName, "vern697v", sis);
		assertZeroResults(fldName, "vern697x", sis);
		assertZeroResults(fldName, "vern697y", sis);
		assertZeroResults(fldName, "vern697z", sis);
	
		assertSingleResult("Vern698search", fldName, "vern698a", sis);
		assertSingleResult("Vern698search", fldName, "vern698b", sis);
		assertSingleResult("Vern698search", fldName, "vern698c", sis);
		assertSingleResult("Vern698search", fldName, "vern698d", sis);
		assertSingleResult("Vern698search", fldName, "vern698e", sis);		
		assertSingleResult("Vern698search", fldName, "vern698f", sis);		
		assertSingleResult("Vern698search", fldName, "vern698g", sis);		
		assertSingleResult("Vern698search", fldName, "vern698h", sis);		
		assertSingleResult("Vern698search", fldName, "vern698j", sis);		
		assertSingleResult("Vern698search", fldName, "vern698k", sis);		
		assertSingleResult("Vern698search", fldName, "vern698l", sis);		
		assertSingleResult("Vern698search", fldName, "vern698m", sis);		
		assertSingleResult("Vern698search", fldName, "vern698n", sis);		
		assertSingleResult("Vern698search", fldName, "vern698o", sis);		
		assertSingleResult("Vern698search", fldName, "vern698p", sis);		
		assertSingleResult("Vern698search", fldName, "vern698q", sis);		
		assertSingleResult("Vern698search", fldName, "vern698r", sis);		
		assertSingleResult("Vern698search", fldName, "vern698s", sis);		
		assertSingleResult("Vern698search", fldName, "vern698t", sis);		
		assertSingleResult("Vern698search", fldName, "vern698u", sis);		
		assertZeroResults(fldName, "vern698v", sis);
		assertZeroResults(fldName, "vern698x", sis);
		assertZeroResults(fldName, "vern698y", sis);
		assertZeroResults(fldName, "vern698z", sis);
	
		assertSingleResult("Vern699search", fldName, "vern699a", sis);
		assertSingleResult("Vern699search", fldName, "vern699b", sis);
		assertSingleResult("Vern699search", fldName, "vern699c", sis);
		assertSingleResult("Vern699search", fldName, "vern699d", sis);
		assertSingleResult("Vern699search", fldName, "vern699e", sis);		
		assertSingleResult("Vern699search", fldName, "vern699f", sis);		
		assertSingleResult("Vern699search", fldName, "vern699g", sis);		
		assertSingleResult("Vern699search", fldName, "vern699h", sis);		
		assertSingleResult("Vern699search", fldName, "vern699j", sis);		
		assertSingleResult("Vern699search", fldName, "vern699k", sis);		
		assertSingleResult("Vern699search", fldName, "vern699l", sis);		
		assertSingleResult("Vern699search", fldName, "vern699m", sis);		
		assertSingleResult("Vern699search", fldName, "vern699n", sis);		
		assertSingleResult("Vern699search", fldName, "vern699o", sis);		
		assertSingleResult("Vern699search", fldName, "vern699p", sis);		
		assertSingleResult("Vern699search", fldName, "vern699q", sis);		
		assertSingleResult("Vern699search", fldName, "vern699r", sis);		
		assertSingleResult("Vern699search", fldName, "vern699s", sis);		
		assertSingleResult("Vern699search", fldName, "vern699t", sis);		
		assertSingleResult("Vern699search", fldName, "vern699u", sis);		
		assertZeroResults(fldName, "vern699v", sis);
		assertZeroResults(fldName, "vern699x", sis);
		assertZeroResults(fldName, "vern699y", sis);
		assertZeroResults(fldName, "vern699z", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// subfields v from all subject fields
		assertSingleResult("600search", fldName, "600v", sis);
		assertSingleResult("610search", fldName, "610v", sis);
		assertSingleResult("611search", fldName, "611v", sis);
		assertSingleResult("630search", fldName, "630v", sis);
		assertSingleResult("650search", fldName, "650v", sis);
		assertSingleResult("651search", fldName, "651v", sis);
		// no sub v in 653
		assertSingleResult("654search", fldName, "654v", sis);
		assertSingleResult("655search", fldName, "655v", sis);
		assertSingleResult("656search", fldName, "656v", sis);
		assertSingleResult("657search", fldName, "657v", sis);
		// no sub v in 658
		assertSingleResult("690search", fldName, "690v", sis);
		assertSingleResult("691search", fldName, "691v", sis);
		assertSingleResult("696search", fldName, "696v", sis);
		assertSingleResult("697search", fldName, "697v", sis);
		assertSingleResult("698search", fldName, "698v", sis);
		assertSingleResult("699search", fldName, "699v", sis);

		// subfields y from all subject fields
		assertSingleResult("600search", fldName, "600y", sis);
		assertSingleResult("610search", fldName, "610y", sis);
		assertSingleResult("611search", fldName, "611y", sis);
		assertSingleResult("630search", fldName, "630y", sis);
		assertSingleResult("650search", fldName, "650y", sis);
		assertSingleResult("651search", fldName, "651y", sis);
		// no sub y in 653
		assertSingleResult("654search", fldName, "654y", sis);
		assertSingleResult("655search", fldName, "655y", sis);
		assertSingleResult("656search", fldName, "656y", sis);
		assertSingleResult("657search", fldName, "657y", sis);
		// no sub y in 658
		assertSingleResult("690search", fldName, "690y", sis);
		assertSingleResult("691search", fldName, "691y", sis);
		assertSingleResult("696search", fldName, "696y", sis);
		assertSingleResult("697search", fldName, "697y", sis);
		assertSingleResult("698search", fldName, "698y", sis);
		assertSingleResult("699search", fldName, "699y", sis);
		
		assertZeroResults(fldName, "600a", sis);
		assertZeroResults(fldName, "610a", sis);
		assertZeroResults(fldName, "611a", sis);
		assertZeroResults(fldName, "630a", sis);
		assertZeroResults(fldName, "650a", sis);
		assertZeroResults(fldName, "651a", sis);
		assertZeroResults(fldName, "653a", sis);
		assertZeroResults(fldName, "654a", sis);
		assertZeroResults(fldName, "655a", sis);
		assertZeroResults(fldName, "656a", sis);
		assertZeroResults(fldName, "657a", sis);
		assertZeroResults(fldName, "658a", sis);
		assertZeroResults(fldName, "690a", sis);
		assertZeroResults(fldName, "691a", sis);
		assertZeroResults(fldName, "696a", sis);
		assertZeroResults(fldName, "697a", sis);
		assertZeroResults(fldName, "698a", sis);
		assertZeroResults(fldName, "699a", sis);
		
		// 651v
	    createIxInitVars(testDataFname);
	    assertSingleResult("6280316", fldName, "Maps.", sis); 
	    assertSingleResult("6280316", fldName, "map.", sis); 
	    
	    // 650y 651y
		createIxInitVars("eraTests.mrc");
		Set<String> docIds = new HashSet<String>();
		docIds.add("650y");
		docIds.add("666");
		assertSearchResults(fldName, "\"20th century\"", docIds, sis);
		assertSearchResults(fldName, "\"20th century.\"", docIds, sis);
		docIds.clear();
		docIds.add("111");
		docIds.add("777");
		docIds.add("888");
		docIds.add("999");
		assertSearchResults(fldName, "\"To 449\"", docIds, sis);
	    assertSingleResult("111", fldName, "\"449-1066\"", sis);
	    assertSingleResult("222", fldName, "\"1921\"", sis);
	    assertSingleResult("777", fldName, "\"Roman period, 55 B.C.-449 A.D.\"", sis);
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
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		// subfields v from all subject fields
		assertSingleResult("Vern600search", fldName, "vern600v", sis);
		assertSingleResult("Vern610search", fldName, "vern610v", sis);
		assertSingleResult("Vern611search", fldName, "vern611v", sis);
		assertSingleResult("Vern630search", fldName, "vern630v", sis);
		assertSingleResult("Vern650search", fldName, "vern650v", sis);
		assertSingleResult("Vern651search", fldName, "vern651v", sis);
		// no sub v in 653
		assertSingleResult("Vern654search", fldName, "vern654v", sis);
		assertSingleResult("Vern655search", fldName, "vern655v", sis);
		assertSingleResult("Vern656search", fldName, "vern656v", sis);
		assertSingleResult("Vern657search", fldName, "vern657v", sis);
		// no sub v in 658
		assertSingleResult("Vern690search", fldName, "vern690v", sis);
		assertSingleResult("Vern691search", fldName, "vern691v", sis);
		assertSingleResult("Vern696search", fldName, "vern696v", sis);
		assertSingleResult("Vern697search", fldName, "vern697v", sis);
		assertSingleResult("Vern698search", fldName, "vern698v", sis);
		assertSingleResult("Vern699search", fldName, "vern699v", sis);
	
		// subfields y from all subject fields
		assertSingleResult("Vern600search", fldName, "vern600y", sis);
		assertSingleResult("Vern610search", fldName, "vern610y", sis);
		assertSingleResult("Vern611search", fldName, "vern611y", sis);
		assertSingleResult("Vern630search", fldName, "vern630y", sis);
		assertSingleResult("Vern650search", fldName, "vern650y", sis);
		assertSingleResult("Vern651search", fldName, "vern651y", sis);
		// no sub y in 653
		assertSingleResult("Vern654search", fldName, "vern654y", sis);
		assertSingleResult("Vern655search", fldName, "vern655y", sis);
		assertSingleResult("Vern656search", fldName, "vern656y", sis);
		assertSingleResult("Vern657search", fldName, "vern657y", sis);
		// no sub y in 658
		assertSingleResult("Vern690search", fldName, "vern690y", sis);
		assertSingleResult("Vern691search", fldName, "vern691y", sis);
		assertSingleResult("Vern696search", fldName, "vern696y", sis);
		assertSingleResult("Vern697search", fldName, "vern697y", sis);
		assertSingleResult("Vern698search", fldName, "vern698y", sis);
		assertSingleResult("Vern699search", fldName, "vern699y", sis);
		
		assertZeroResults(fldName, "vern600a", sis);
		assertZeroResults(fldName, "vern610a", sis);
		assertZeroResults(fldName, "vern611a", sis);
		assertZeroResults(fldName, "vern630a", sis);
		assertZeroResults(fldName, "vern650a", sis);
		assertZeroResults(fldName, "vern651a", sis);
		assertZeroResults(fldName, "vern653a", sis);
		assertZeroResults(fldName, "vern654a", sis);
		assertZeroResults(fldName, "vern655a", sis);
		assertZeroResults(fldName, "vern656a", sis);
		assertZeroResults(fldName, "vern657a", sis);
		assertZeroResults(fldName, "vern658a", sis);
		assertZeroResults(fldName, "vern690a", sis);
		assertZeroResults(fldName, "vern691a", sis);
		assertZeroResults(fldName, "vern696a", sis);
		assertZeroResults(fldName, "vern697a", sis);
		assertZeroResults(fldName, "vern698a", sis);
		assertZeroResults(fldName, "vern699a", sis);
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
	    assertZeroResults(fldName, "nomesh", sis);
	    assertSingleResult("650a", fldName, "rock", sis);
	    fldName = "topic_facet";
	    assertZeroResults(fldName, "nomesh", sis);
	    assertSingleResult("650a", fldName, "\"I am a rock\"", sis);
	    
	    // 655a subject_other_search, topic_facet
	    fldName = "subject_other_search";
	    assertZeroResults(fldName, "\"internet resource\"", sis);
	    assertZeroResults(fldName, "internet", sis);
	    assertZeroResults(fldName, "resource", sis);
	    assertZeroResults(fldName, "fulltext", sis);
	    assertZeroResults(fldName, "noexport", sis);
	    assertSingleResult("655b", fldName, "\"be bee be\"", sis);
	    assertSingleResult("655keepme", fldName, "\"keep me\"", sis);
	    fldName = "topic_facet";
	    assertZeroResults(fldName, "\"Internet Resource\"", sis);
	    assertZeroResults(fldName, "Fulltext", sis);
	    assertZeroResults(fldName, "NoExport", sis);
	    // 655b not in topic_facet
	    // assertSingleResult("655b", fldName, "\"be bee be\"", sis);
	    assertSingleResult("655keepme", fldName, "\"keep me\"", sis);
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
        assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		// topic_facet is a copy field from topic_facet, but it is a string
		// topic = 600abcdq:600t:610ab:610t:630a:630t:650a:655a
		
		// 600a, trailing period removed
		assertSingleResult("345228", fldName, "\"Zemnukhov, Ivan\"", sis); 
		assertZeroResults(fldName, "\"Zemnukhov, Ivan.\"", sis);
		// 600acd, trailing period removed
		assertSingleResult("1261173", fldName, "\"Somers, John Somers, Baron, 1651-1716\"", sis);  
		assertZeroResults(fldName, "\"Somers, John Somers, Baron, 1651-1716.\"", sis); 
		// 600ad, trailing comma removed
		assertSingleResult("600trailingComma", fldName, "\"Monroe, Marilyn, 1926-1962\"", sis); 
		assertZeroResults(fldName, "\"Monroe, Marilyn, 1926-1962,\"", sis);
		// 600q now bundled with abcdq
		assertSingleResult("600aqdx", fldName, "\"Kennedy, John F. (John Fitzgerald), 1917-1963\"", sis); 
		assertZeroResults(fldName, "\"(John Fitzgerald),\"", sis);
		assertZeroResults(fldName, "\"(John Fitzgerald)\"", sis);
		assertZeroResults(fldName, "\"Kennedy, John F. 1917-1963\"", sis);
		// 600t, too few letters at end to remove trailing period
		assertSingleResult("1261173", fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it.\"", sis);  // 630
		assertZeroResults(fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it\"", sis);  // 630
		// 600p no longer included
		assertZeroResults(fldName, "\"Meditation;\"", sis);
		assertZeroResults(fldName, "\"Meditation\"", sis);
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963. Meditation\"", sis);
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963. Meditation;\"", sis);
		// 600ad 
		assertSingleResult("600adtpof", fldName, "\"Hindemith, Paul, 1895-1963\"", sis);
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963.\"", sis);
		// 600t separate
		assertSingleResult("600adtpof", fldName, "\"Nobilissima visione\"", sis); 
		assertZeroResults(fldName, "\"Nobilissima visione.\"", sis);
		
		// 610ab, trailing period removed
		assertSingleResult("1261173", fldName, "\"England and Wales. Army\"", sis);
		assertZeroResults(fldName, "\"England and Wales. Army.\"", sis);
		assertSingleResult("610trailing", fldName, "\"Augusta (Ga.)\"", sis); 
		assertZeroResults(fldName, "\"Augusta (Ga.).\"", sis);  
		// 610t separate
		assertSingleResult("610atpv", fldName, "\"Reports\"", sis); 
		assertZeroResults(fldName, "\"Reports.\"", sis);
		assertSingleResult("610atpv", fldName, "\"United States Strategic Bombing Survey\"", sis); 
		assertZeroResults(fldName, "\"United States Strategic Bombing Survey.\"", sis);
		// 610p no longer included
		assertZeroResults(fldName, "\"Pacific war\"", sis);
		assertZeroResults(fldName, "\"United States Strategic Bombing Survey Pacific war\"", sis);
		
		// 630a, trailing period
		assertSingleResult("1261173", fldName, "\"Magna Carta\"", sis);  
		assertZeroResults(fldName, "\"Magna Carta.\"", sis);  
		// 630p - no longer included
		assertZeroResults(fldName, "\"N.T.\"", sis); 
		assertZeroResults(fldName, "\"N.T\"", sis);  
		// 630s no longer included
		assertZeroResults(fldName, "\"Vulgate\"", sis);  
		assertZeroResults(fldName, "\"Vulgate.\"", sis);  
		Set<String> docIds = new HashSet<String>();
		docIds.add("630alsf");
		docIds.add("630trailing");
		assertSearchResults(fldName, "\"Bible\"", docIds, sis);
		assertZeroResults(fldName, "\"Bible.\"", sis);  

		// 650a, trailing period
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"", sis);
		assertZeroResults(fldName, "\"Literature, Comparative.\"", sis);  
		// 650a, trailing comma
		assertSingleResult("650trailingComma", fldName, "\"Seabiscuit (Race horse)\"", sis); 
		assertZeroResults(fldName, "\"Seabiscuit (Race horse),\"", sis);  
		assertZeroResults(fldName, "\"Seabiscuit (Race horse\"", sis);  
		// 650a, trailing paren left in
		assertSingleResult("650trailing", fldName, "\"BASIC (Computer program language)\"", sis); 
		assertZeroResults(fldName, "\"BASIC (Computer program language\"", sis);
		
		// 655a, trailing period
		assertSingleResult("6551", fldName, "\"bust\"", sis); 
		assertZeroResults(fldName, "\"bust.\"", sis);
		// 655b no longer used
		assertZeroResults(fldName, "\"Laminated marblewood bust\"", sis);
		assertZeroResults(fldName, "\"Laminated marblewood bust.\"", sis);
		assertZeroResults(fldName, "\"Laminated marblewood\"", sis);

// TODO: this should NOT be true once we have bad Lane topics out of the topic field
		docIds.clear();
		docIds.add("7233951");
		docIds.add("1132");
		docIds.add("1133");
		assertSearchResults(fldName, "\"Internet Resource\"", docIds, sis);
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
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
	
	    // trailing period should be stripped
		Set<String> docIds = new HashSet<String>();
		docIds.add("651a");
		docIds.add("651again");
		assertSearchResults(fldName, "Muppets", docIds, sis);
		assertSingleResult("651numPeriod", fldName, "7.150", sis);
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y.)\"", sis);
		assertZeroResults(fldName, "\"Syracuse (N.Y.\"", sis);
		assertZeroResults(fldName, "\"Syracuse (N.Y\"", sis);
		assertSingleResult("651siberia", fldName, "\"Siberia (Russia)\"", sis);
		assertZeroResults(fldName, "\"Siberia (Russia).\"", sis);
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
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
	
	    // trailing period should be stripped
		Set<String> docIds = new HashSet<String>();
		docIds.add("650y");
		docIds.add("666");
		assertSearchResults(fldName, "\"20th century\"", docIds, sis);
	    
	    // trailing period stripped after 3 digit year
		docIds.clear();
		docIds.add("888");
		docIds.add("999");
		assertSearchResults(fldName, "\"To 449\"", docIds, sis);
	    assertZeroResults(fldName, "\"To 449.\"", sis);
	    
	    // trailing period stripped after 4 digit year
	    assertSingleResult("111", fldName, "\"449-1066\"", sis);
	    
	    // trailing dash - period NOT stripped
	    assertSingleResult("222", fldName, "\"1921-\"", sis);
	    
	    // trailing period NOT stripped
	    assertSingleResult("777", fldName, "\"Roman period, 55 B.C.-449 A.D.\"", sis);	    
	}

}
