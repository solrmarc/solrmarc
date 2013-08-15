package edu.stanford;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's vernacular title searching fields
 * @author Naomi Dushay
 */
public class TitleSearchVernTests extends AbstractStanfordBlacklightTest {
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("vernacularSearchTests.mrc");
	}


	/**
	 * vern_title_245a_search:  the 800a matching 245a
	 */
@Test
	public final void testVernTitle245aSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_245a_search";
//		assertSearchFldOneValProps(fldName);
		
		assertSingleResult("2xxVernSearch", fldName, "vern245a");

		assertZeroResults(fldName, "vern245b"); 
		assertZeroResults(fldName, "vern245p"); 
	}

	/**
	 * vern_title_245_search field:  880 matching 245 subfields a,b,f,g,k,n,p,s
	 */
@Test
	public final void testVernTitle245Search() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_245_search";
//		assertSearchFldOneValProps(fldName);

		assertSingleResult("2xxVernSearch", fldName, "vern245a");
		assertSingleResult("2xxVernSearch", fldName, "vern245b");
		assertSingleResult("2xxVernSearch", fldName, "vern245f");
		assertSingleResult("2xxVernSearch", fldName, "vern245g");
		assertSingleResult("2xxVernSearch", fldName, "vern245k");
		assertSingleResult("2xxVernSearch", fldName, "vern245n");
		assertSingleResult("2xxVernSearch", fldName, "vern245p");
		assertSingleResult("2xxVernSearch", fldName, "vern245s"); 
		
		assertZeroResults(fldName, "nope");
	}


	/**
	 * vern_uniform_title_search:  130 adfgklmnoprst and 240 adfgklmnoprs
	 */
@Test
	public final void testVernTitleUniformSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_uniform_search";
//		assertSearchFldOneValProps(fldName);
		
		assertSingleResult("2xxVernSearch", fldName, "vern130a");
		assertSingleResult("2xxVernSearch", fldName, "vern130d");
		assertSingleResult("2xxVernSearch", fldName, "vern130f");
		assertSingleResult("2xxVernSearch", fldName, "vern130g");
		assertSingleResult("2xxVernSearch", fldName, "vern130k");
		assertSingleResult("2xxVernSearch", fldName, "vern130l");
		assertSingleResult("2xxVernSearch", fldName, "vern130m");
		assertSingleResult("2xxVernSearch", fldName, "vern130n");
		assertSingleResult("2xxVernSearch", fldName, "vern130o");
		assertSingleResult("2xxVernSearch", fldName, "vern130p");
		assertSingleResult("2xxVernSearch", fldName, "vern130r");
		assertSingleResult("2xxVernSearch", fldName, "vern130s");
		assertSingleResult("2xxVernSearch", fldName, "vern130t");
		
		assertSingleResult("240VernSearch", fldName, "vern240a");
		assertSingleResult("240VernSearch", fldName, "vern240d");
		assertSingleResult("240VernSearch", fldName, "vern240f");
		assertSingleResult("240VernSearch", fldName, "vern240g");
		assertSingleResult("240VernSearch", fldName, "vern240k");
		assertSingleResult("240VernSearch", fldName, "vern240l");
		assertSingleResult("240VernSearch", fldName, "vern240m");
		assertSingleResult("240VernSearch", fldName, "vern240n");
		assertSingleResult("240VernSearch", fldName, "vern240o");
		assertSingleResult("240VernSearch", fldName, "vern240p");
		assertSingleResult("240VernSearch", fldName, "vern240r");
		assertSingleResult("240VernSearch", fldName, "vern240s");

		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_title_variant_search: 2xx (except 245) test 210, 222, 242
	 */
@Test
	public final void testVernVariantTitle210_222_242Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_variant_search";
//		assertSearchFldMultValProps(fldName);

		assertSingleResult("2xxVernSearch", fldName, "vern210a");
		assertSingleResult("2xxVernSearch", fldName, "vern210b");
		assertSingleResult("2xxVernSearch", fldName, "vern222a");
		assertSingleResult("2xxVernSearch", fldName, "vern222b");
		assertSingleResult("2xxVernSearch", fldName, "vern242a");
		assertSingleResult("2xxVernSearch", fldName, "vern242b");
		assertSingleResult("2xxVernSearch", fldName, "vern242n");
		assertSingleResult("2xxVernSearch", fldName, "vern242p");
		
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_title_variant_search: checking 243
	 */
@Test
	public final void testVernVariantTitle243Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_variant_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("2xxVernSearch", fldName, "vern243a");
		assertSingleResult("2xxVernSearch", fldName, "vern243d");
		assertSingleResult("2xxVernSearch", fldName, "vern243f");
		assertSingleResult("2xxVernSearch", fldName, "vern243g");
		assertSingleResult("2xxVernSearch", fldName, "vern243k");
		assertSingleResult("2xxVernSearch", fldName, "vern243l");
		assertSingleResult("2xxVernSearch", fldName, "vern243m");
		assertSingleResult("2xxVernSearch", fldName, "vern243n");
		assertSingleResult("2xxVernSearch", fldName, "vern243o");
		assertSingleResult("2xxVernSearch", fldName, "vern243p");
		assertSingleResult("2xxVernSearch", fldName, "vern243r");
		assertSingleResult("2xxVernSearch", fldName, "vern243s");
		
		assertDocHasNoFieldValue("2xxVernSearch", fldName, "vern243");
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_title_variant_search: 2xx (except 245) - checking 246 and 247
	 */
@Test
	public final void testVernVariantTitle246_247Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_variant_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("2xxVernSearch", fldName, "vern246a");
		assertSingleResult("2xxVernSearch", fldName, "vern246b");
		assertSingleResult("2xxVernSearch", fldName, "vern246f");
		assertSingleResult("2xxVernSearch", fldName, "vern246g");
		assertSingleResult("2xxVernSearch", fldName, "vern246n");
		assertSingleResult("2xxVernSearch", fldName, "vern246p");
		assertSingleResult("2xxVernSearch", fldName, "vern247a");
		assertSingleResult("2xxVernSearch", fldName, "vern247b");
		assertSingleResult("2xxVernSearch", fldName, "vern247f");
		assertSingleResult("2xxVernSearch", fldName, "vern247g");
		assertSingleResult("2xxVernSearch", fldName, "vern247n");
		assertSingleResult("2xxVernSearch", fldName, "vern247p");
		
		assertDocHasNoFieldValue("2xxVernSearch", fldName, "vern246");
		assertDocHasNoFieldValue("2xxVernSearch", fldName, "vern247");
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_related_title_search: 505t
	 */
@Test
	public final void testVernRelatedTitle505Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		createIxInitVars("summaryTests.mrc");
//		assertSearchFldMultValProps(fldName);
		assertSingleResult("505", fldName, "vern505t");
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_related_title_search: 700 field
	 */
@Test
	public final void testVernRelatedTitle700Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern700f");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700k");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700l");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700m");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700n");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700o");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700p");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700r");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700s");
		assertSingleResult("7xxLowVernSearch", fldName, "vern700t");

		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernPersonSearch");
		// overlap author
		assertSearchResults(fldName, "vern700g", docIds); 
		
		assertZeroResults(fldName, "vern700j");
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_related_title_search: 710 field
	 */
@Test
	public final void testVernRelatedTitle710Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern710l");
		assertSingleResult("7xxLowVernSearch", fldName, "vern710m");
		assertSingleResult("7xxLowVernSearch", fldName, "vern710o");
		assertSingleResult("7xxLowVernSearch", fldName, "vern710p");
		assertSingleResult("7xxLowVernSearch", fldName, "vern710r");
		assertSingleResult("7xxLowVernSearch", fldName, "vern710s");
		assertSingleResult("7xxLowVernSearch", fldName, "vern710t");
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernCorpSearch");
		// overlap author
		assertSearchResults(fldName, "vern710d", docIds); 
		assertSearchResults(fldName, "vern710g", docIds); 
		assertSearchResults(fldName, "vern710n", docIds);
		// used to be in author
		assertSearchResults(fldName, "vern710f", docIds);
		assertSearchResults(fldName, "vern710k", docIds);

		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_related_title_search: 711 field
	 */
@Test
	public final void testVernRelatedTitle711Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern711f");
		assertSingleResult("7xxLowVernSearch", fldName, "vern711k");
		assertSingleResult("7xxLowVernSearch", fldName, "vern711l");
		assertSingleResult("7xxLowVernSearch", fldName, "vern711p");
		assertSingleResult("7xxLowVernSearch", fldName, "vern711s");
		assertSingleResult("7xxLowVernSearch", fldName, "vern711t");

		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernMeetingSearch");
		// overlap author
		assertSearchResults(fldName, "vern711g", docIds); 
		assertSearchResults(fldName, "vern711n", docIds);
		
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_related_title_search: 730 field
	 */
@Test
	public final void testVernRelatedTitle730Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern730a");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730d");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730f");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730g");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730k");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730l");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730m");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730n");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730o");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730p");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730r");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730s");
		assertSingleResult("7xxLowVernSearch", fldName, "vern730t");
	
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_related_title_search: 740 field
	 */
@Test
	public final void testVernRelatedTitle740Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern740a");
		assertSingleResult("7xxLowVernSearch", fldName, "vern740n");
		assertSingleResult("7xxLowVernSearch", fldName, "vern740p");
	
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_related_title_search: 760, 762, 765, 767
	 */
@Test
	public final void testVernRelatedTitle76xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("76xVernSearch", fldName, "vern760s");
		assertSingleResult("76xVernSearch", fldName, "vern760t");
		assertSingleResult("76xVernSearch", fldName, "vern762s");
		assertSingleResult("76xVernSearch", fldName, "vern762t");
		assertSingleResult("76xVernSearch", fldName, "vern765s");
		assertSingleResult("76xVernSearch", fldName, "vern765t");
		assertSingleResult("76xVernSearch", fldName, "vern767s");
		assertSingleResult("76xVernSearch", fldName, "vern767t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_related_title_search: 770, 772, 773, 774, 775, 776, 777
	 */
@Test
	public final void testVernRelatedTitle77xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("77xVernSearch", fldName, "vern770s");
		assertSingleResult("77xVernSearch", fldName, "vern770t");
		assertSingleResult("77xVernSearch", fldName, "vern772s");
		assertSingleResult("77xVernSearch", fldName, "vern772t");
		assertSingleResult("77xVernSearch", fldName, "vern773s");
		assertSingleResult("77xVernSearch", fldName, "vern773t");
		assertSingleResult("77xVernSearch", fldName, "vern774s");
		assertSingleResult("77xVernSearch", fldName, "vern774t");
		assertSingleResult("77xVernSearch", fldName, "vern775s");
		assertSingleResult("77xVernSearch", fldName, "vern775t");
		assertSingleResult("77xVernSearch", fldName, "vern776s");
		assertSingleResult("77xVernSearch", fldName, "vern776t");
		assertSingleResult("77xVernSearch", fldName, "vern777s");
		assertSingleResult("77xVernSearch", fldName, "vern777t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_related_title_search: 780, 785, 786, 787
	 */
@Test
	public final void testVernRelatedTitle78xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("78xVernSearch", fldName, "vern780s");
		assertSingleResult("78xVernSearch", fldName, "vern780t");
		assertSingleResult("78xVernSearch", fldName, "vern785s");
		assertSingleResult("78xVernSearch", fldName, "vern785t");
		assertSingleResult("78xVernSearch", fldName, "vern786s");
		assertSingleResult("78xVernSearch", fldName, "vern786t");
		assertSingleResult("78xVernSearch", fldName, "vern787s");
		assertSingleResult("78xVernSearch", fldName, "vern787t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_related_title_search: 796
	 */
@Test
	public final void testVernRelatedTitle796Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("79xVernSearch", fldName, "vern796f");
		assertSingleResult("79xVernSearch", fldName, "vern796k");
		assertSingleResult("79xVernSearch", fldName, "vern796l");
		assertSingleResult("79xVernSearch", fldName, "vern796m");
		assertSingleResult("79xVernSearch", fldName, "vern796n");
		assertSingleResult("79xVernSearch", fldName, "vern796o");
		assertSingleResult("79xVernSearch", fldName, "vern796p");
		assertSingleResult("79xVernSearch", fldName, "vern796r");
		assertSingleResult("79xVernSearch", fldName, "vern796s");
		assertSingleResult("79xVernSearch", fldName, "vern796t");
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("79xVernSearch");
		docIds.add("7xxVernPersonSearch");
		// overlap author
		assertSearchResults(fldName, "vern796g", docIds); 
		
		assertZeroResults(fldName, "vern796j");
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_related_title_search: 797 field
	 */
@Test
	public final void testVernRelatedTitle797Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
//		assertSingleResult("79xVernSearch", fldName, "vern797d");
//		assertSingleResult("79xVernSearch", fldName, "vern797f");
//		assertSingleResult("79xVernSearch", fldName, "vern797g");
//		assertSingleResult("79xVernSearch", fldName, "vern797k");
		assertSingleResult("79xVernSearch", fldName, "vern797l");
		assertSingleResult("79xVernSearch", fldName, "vern797m");
//		assertSingleResult("79xVernSearch", fldName, "vern797n");
		assertSingleResult("79xVernSearch", fldName, "vern797o");
		assertSingleResult("79xVernSearch", fldName, "vern797p");
		assertSingleResult("79xVernSearch", fldName, "vern797r");
		assertSingleResult("79xVernSearch", fldName, "vern797s");
		assertSingleResult("79xVernSearch", fldName, "vern797t");
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("79xVernSearch");
		docIds.add("7xxVernCorpSearch");
		// overlap author
		assertSearchResults(fldName, "vern797d", docIds); 
		assertSearchResults(fldName, "vern797g", docIds); 
		assertSearchResults(fldName, "vern797n", docIds);
		// used to be in author
		assertSearchResults(fldName, "vern797f", docIds);
		assertSearchResults(fldName, "vern797k", docIds);
		
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_related_title_search: 798 field
	 */
@Test
	public final void testVernRelatedTitle798Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("79xVernSearch", fldName, "vern798f");
//		assertSingleResult("79xVernSearch", fldName, "vern798g");
		assertSingleResult("79xVernSearch", fldName, "vern798k");
		assertSingleResult("79xVernSearch", fldName, "vern798l");
//		assertSingleResult("79xVernSearch", fldName, "vern798n");
		assertSingleResult("79xVernSearch", fldName, "vern798p");
		assertSingleResult("79xVernSearch", fldName, "vern798s");
		assertSingleResult("79xVernSearch", fldName, "vern798t");
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("79xVernSearch");
		docIds.add("7xxVernMeetingSearch");
		// overlap author
		assertSearchResults(fldName, "vern798g", docIds); 
		assertSearchResults(fldName, "vern798n", docIds);
		
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_related_title_search: 799 field
	 */
@Test
	public final void testVernRelatedTitle799Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("79xVernSearch", fldName, "vern799a");
		assertSingleResult("79xVernSearch", fldName, "vern799d");
		assertSingleResult("79xVernSearch", fldName, "vern799f");
		assertSingleResult("79xVernSearch", fldName, "vern799g");
		assertSingleResult("79xVernSearch", fldName, "vern799k");
		assertSingleResult("79xVernSearch", fldName, "vern799l");
		assertSingleResult("79xVernSearch", fldName, "vern799m");
		assertSingleResult("79xVernSearch", fldName, "vern799n");
		assertSingleResult("79xVernSearch", fldName, "vern799o");
		assertSingleResult("79xVernSearch", fldName, "vern799p");
		assertSingleResult("79xVernSearch", fldName, "vern799r");
		assertSingleResult("79xVernSearch", fldName, "vern799s");
		assertSingleResult("79xVernSearch", fldName, "vern799t");
	
		assertZeroResults(fldName, "nope");
	}

	/**
	 * vern_title_series_search: 440, 490
	 */
	@Test
	public final void testVernSeriesTitle440_490Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("4xxVernSearch", fldName, "vern440a");
		assertSingleResult("4xxVernSearch", fldName, "vern440n");
		assertSingleResult("4xxVernSearch", fldName, "vern440p");
		assertSingleResult("4xxVernSearch", fldName, "vern440v");
		assertSingleResult("4xxVernSearch", fldName, "vern490a");
		assertSingleResult("4xxVernSearch", fldName, "vern490v");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_title_series_search: 800
	 */
	@Test
	public final void testVernSeriesTitle800Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("8xxVernSearch", fldName, "vern800f");
		assertSingleResult("8xxVernSearch", fldName, "vern800k");
		assertSingleResult("8xxVernSearch", fldName, "vern800l");
		assertSingleResult("8xxVernSearch", fldName, "vern800m");
		assertSingleResult("8xxVernSearch", fldName, "vern800n");
		assertSingleResult("8xxVernSearch", fldName, "vern800o");
		assertSingleResult("8xxVernSearch", fldName, "vern800p");
		assertSingleResult("8xxVernSearch", fldName, "vern800r");
		assertSingleResult("8xxVernSearch", fldName, "vern800s");
		assertSingleResult("8xxVernSearch", fldName, "vern800t");
		assertSingleResult("8xxVernSearch", fldName, "vern800v");
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("8xxVernSearch");
		docIds.add("800VernSearch");
		// overlap author
		assertSearchResults(fldName, "vern800g", docIds); 
		
		assertZeroResults(fldName, "vern800j");
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_title_series_search: 810
	 */
	@Test
	public final void testVernSeriesTitle810Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
//		assertSearchFldMultValProps(fldName);
	
//		assertSingleResult("8xxVernSearch", fldName, "vern810d");
//		assertSingleResult("8xxVernSearch", fldName, "vern810f");
//		assertSingleResult("8xxVernSearch", fldName, "vern810g");
//		assertSingleResult("8xxVernSearch", fldName, "vern810k");
		assertSingleResult("8xxVernSearch", fldName, "vern810l");
		assertSingleResult("8xxVernSearch", fldName, "vern810m");
//		assertSingleResult("8xxVernSearch", fldName, "vern810n");
		assertSingleResult("8xxVernSearch", fldName, "vern810o");
		assertSingleResult("8xxVernSearch", fldName, "vern810p");
		assertSingleResult("8xxVernSearch", fldName, "vern810r");
		assertSingleResult("8xxVernSearch", fldName, "vern810s");
		assertSingleResult("8xxVernSearch", fldName, "vern810t");
		assertSingleResult("8xxVernSearch", fldName, "vern810v");
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("8xxVernSearch");
		docIds.add("810VernSearch");
		// overlap author
		assertSearchResults(fldName, "vern810d", docIds); 
		assertSearchResults(fldName, "vern810g", docIds); 
		assertSearchResults(fldName, "vern810n", docIds);
		// used to be in author
		assertSearchResults(fldName, "vern810f", docIds);
		assertSearchResults(fldName, "vern810k", docIds);
		
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_title_series_search: 811
	 */
	@Test
	public final void testVernSeriesTitle811Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("8xxVernSearch", fldName, "vern811f");
//		assertSingleResult("8xxVernSearch", fldName, "vern811g");
		assertSingleResult("8xxVernSearch", fldName, "vern811k");
		assertSingleResult("8xxVernSearch", fldName, "vern811l");
//		assertSingleResult("8xxVernSearch", fldName, "vern811n");
		assertSingleResult("8xxVernSearch", fldName, "vern811p");
		assertSingleResult("8xxVernSearch", fldName, "vern811s");
		assertSingleResult("8xxVernSearch", fldName, "vern811t");
		assertSingleResult("8xxVernSearch", fldName, "vern811v");
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("8xxVernSearch");
		docIds.add("811VernSearch");
		// overlap author
		assertSearchResults(fldName, "vern811g", docIds); 
		assertSearchResults(fldName, "vern811n", docIds);
		
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_title_series_search: 830
	 */
	@Test
	public final void testVernSeriesTitle830Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
//		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("8xxVernSearch", fldName, "vern830a");
		assertSingleResult("8xxVernSearch", fldName, "vern830d");
		assertSingleResult("8xxVernSearch", fldName, "vern830f");
		assertSingleResult("8xxVernSearch", fldName, "vern830g");
		assertSingleResult("8xxVernSearch", fldName, "vern830k");
		assertSingleResult("8xxVernSearch", fldName, "vern830l");
		assertSingleResult("8xxVernSearch", fldName, "vern830m");
		assertSingleResult("8xxVernSearch", fldName, "vern830n");
		assertSingleResult("8xxVernSearch", fldName, "vern830o");
		assertSingleResult("8xxVernSearch", fldName, "vern830p");
		assertSingleResult("8xxVernSearch", fldName, "vern830r");
		assertSingleResult("8xxVernSearch", fldName, "vern830s");
		assertSingleResult("8xxVernSearch", fldName, "vern830t");
		assertSingleResult("8xxVernSearch", fldName, "vern830v");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * vern_title_series_search: 840
	 */
	@Test
	public final void testVernSeriesTitle840Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
//		assertSearchFldMultValProps(fldName);
		assertSingleResult("8xxVernSearch", fldName, "vern840a");
		assertSingleResult("8xxVernSearch", fldName, "vern840v");
		assertZeroResults(fldName, "nope");
	}	

}
