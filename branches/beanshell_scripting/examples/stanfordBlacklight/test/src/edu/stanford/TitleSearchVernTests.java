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
public class TitleSearchVernTests extends BibIndexTest {
	
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
		assertSearchFldOneValProps(fldName, solrCore, sis);
		
		assertSingleResult("2xxVernSearch", fldName, "vern245a", sis);

		assertZeroResults(fldName, "vern245b", sis); 
		assertZeroResults(fldName, "vern245p", sis); 
	}

	/**
	 * vern_title_245_search field:  880 matching 245 subfields a,b,f,g,k,n,p,s
	 */
@Test
	public final void testVernTitle245Search() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_245_search";
		assertSearchFldOneValProps(fldName, solrCore, sis);

		assertSingleResult("2xxVernSearch", fldName, "vern245a", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern245b", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern245f", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern245g", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern245k", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern245n", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern245p", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern245s", sis); 
		
		assertZeroResults(fldName, "nope", sis);
	}


	/**
	 * vern_uniform_title_search:  130 adfgklmnoprst and 240 adfgklmnoprs
	 */
@Test
	public final void testVernTitleUniformSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_uniform_search";
		assertSearchFldOneValProps(fldName, solrCore, sis);
		
		assertSingleResult("2xxVernSearch", fldName, "vern130a", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130d", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130f", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130g", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130k", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130l", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130m", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130n", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130o", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130p", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130r", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130s", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern130t", sis);
		
		assertSingleResult("240VernSearch", fldName, "vern240a", sis);
		assertSingleResult("240VernSearch", fldName, "vern240d", sis);
		assertSingleResult("240VernSearch", fldName, "vern240f", sis);
		assertSingleResult("240VernSearch", fldName, "vern240g", sis);
		assertSingleResult("240VernSearch", fldName, "vern240k", sis);
		assertSingleResult("240VernSearch", fldName, "vern240l", sis);
		assertSingleResult("240VernSearch", fldName, "vern240m", sis);
		assertSingleResult("240VernSearch", fldName, "vern240n", sis);
		assertSingleResult("240VernSearch", fldName, "vern240o", sis);
		assertSingleResult("240VernSearch", fldName, "vern240p", sis);
		assertSingleResult("240VernSearch", fldName, "vern240r", sis);
		assertSingleResult("240VernSearch", fldName, "vern240s", sis);

		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_title_variant_search: 2xx (except 245) test 210, 222, 242
	 */
@Test
	public final void testVernVariantTitle210_222_242Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_variant_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);

		assertSingleResult("2xxVernSearch", fldName, "vern210a", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern210b", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern222a", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern222b", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern242a", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern242b", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern242n", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern242p", sis);
		
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_title_variant_search: checking 243
	 */
@Test
	public final void testVernVariantTitle243Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_variant_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("2xxVernSearch", fldName, "vern243a", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243d", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243f", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243g", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243k", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243l", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243m", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243n", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243o", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243p", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243r", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern243s", sis);
		
		assertDocHasNoFieldValue("2xxVernSearch", fldName, "vern243", sis);
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_title_variant_search: 2xx (except 245) - checking 246 and 247
	 */
@Test
	public final void testVernVariantTitle246_247Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_variant_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("2xxVernSearch", fldName, "vern246a", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern246b", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern246f", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern246g", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern246n", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern246p", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern247a", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern247b", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern247f", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern247g", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern247n", sis);
		assertSingleResult("2xxVernSearch", fldName, "vern247p", sis);
		
		assertDocHasNoFieldValue("2xxVernSearch", fldName, "vern246", sis);
		assertDocHasNoFieldValue("2xxVernSearch", fldName, "vern247", sis);
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_related_title_search: 505t
	 */
@Test
	public final void testVernRelatedTitle505Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
		assertSingleResult("4xxVernSearch", fldName, "vern505t", sis);
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_related_title_search: 700 field
	 */
@Test
	public final void testVernRelatedTitle700Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern700f", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700k", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700l", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700m", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700n", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700o", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700p", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700r", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700s", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern700t", sis);

		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernPersonSearch");
		// overlap author
		assertSearchResults(fldName, "vern700g", docIds, sis); 
		
		assertZeroResults(fldName, "vern700j", sis);
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_related_title_search: 710 field
	 */
@Test
	public final void testVernRelatedTitle710Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern710l", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern710m", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern710o", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern710p", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern710r", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern710s", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern710t", sis);
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernCorpSearch");
		// overlap author
		assertSearchResults(fldName, "vern710d", docIds, sis); 
		assertSearchResults(fldName, "vern710g", docIds, sis); 
		assertSearchResults(fldName, "vern710n", docIds, sis);
		// used to be in author
		assertSearchResults(fldName, "vern710f", docIds, sis);
		assertSearchResults(fldName, "vern710k", docIds, sis);

		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_related_title_search: 711 field
	 */
@Test
	public final void testVernRelatedTitle711Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern711f", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern711k", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern711l", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern711p", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern711s", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern711t", sis);

		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernMeetingSearch");
		// overlap author
		assertSearchResults(fldName, "vern711g", docIds, sis); 
		assertSearchResults(fldName, "vern711n", docIds, sis);
		
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_related_title_search: 730 field
	 */
@Test
	public final void testVernRelatedTitle730Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern730a", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730d", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730f", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730g", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730k", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730l", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730m", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730n", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730o", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730p", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730r", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730s", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern730t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_related_title_search: 740 field
	 */
@Test
	public final void testVernRelatedTitle740Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("7xxLowVernSearch", fldName, "vern740a", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern740n", sis);
		assertSingleResult("7xxLowVernSearch", fldName, "vern740p", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_related_title_search: 760, 762, 765, 767
	 */
@Test
	public final void testVernRelatedTitle76xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("76xVernSearch", fldName, "vern760s", sis);
		assertSingleResult("76xVernSearch", fldName, "vern760t", sis);
		assertSingleResult("76xVernSearch", fldName, "vern762s", sis);
		assertSingleResult("76xVernSearch", fldName, "vern762t", sis);
		assertSingleResult("76xVernSearch", fldName, "vern765s", sis);
		assertSingleResult("76xVernSearch", fldName, "vern765t", sis);
		assertSingleResult("76xVernSearch", fldName, "vern767s", sis);
		assertSingleResult("76xVernSearch", fldName, "vern767t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_related_title_search: 770, 772, 773, 774, 775, 776, 777
	 */
@Test
	public final void testVernRelatedTitle77xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("77xVernSearch", fldName, "vern770s", sis);
		assertSingleResult("77xVernSearch", fldName, "vern770t", sis);
		assertSingleResult("77xVernSearch", fldName, "vern772s", sis);
		assertSingleResult("77xVernSearch", fldName, "vern772t", sis);
		assertSingleResult("77xVernSearch", fldName, "vern773s", sis);
		assertSingleResult("77xVernSearch", fldName, "vern773t", sis);
		assertSingleResult("77xVernSearch", fldName, "vern774s", sis);
		assertSingleResult("77xVernSearch", fldName, "vern774t", sis);
		assertSingleResult("77xVernSearch", fldName, "vern775s", sis);
		assertSingleResult("77xVernSearch", fldName, "vern775t", sis);
		assertSingleResult("77xVernSearch", fldName, "vern776s", sis);
		assertSingleResult("77xVernSearch", fldName, "vern776t", sis);
		assertSingleResult("77xVernSearch", fldName, "vern777s", sis);
		assertSingleResult("77xVernSearch", fldName, "vern777t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_related_title_search: 780, 785, 786, 787
	 */
@Test
	public final void testVernRelatedTitle78xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("78xVernSearch", fldName, "vern780s", sis);
		assertSingleResult("78xVernSearch", fldName, "vern780t", sis);
		assertSingleResult("78xVernSearch", fldName, "vern785s", sis);
		assertSingleResult("78xVernSearch", fldName, "vern785t", sis);
		assertSingleResult("78xVernSearch", fldName, "vern786s", sis);
		assertSingleResult("78xVernSearch", fldName, "vern786t", sis);
		assertSingleResult("78xVernSearch", fldName, "vern787s", sis);
		assertSingleResult("78xVernSearch", fldName, "vern787t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_related_title_search: 796
	 */
@Test
	public final void testVernRelatedTitle796Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("79xVernSearch", fldName, "vern796f", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796k", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796l", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796m", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796n", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796o", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796p", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796r", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796s", sis);
		assertSingleResult("79xVernSearch", fldName, "vern796t", sis);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("79xVernSearch");
		docIds.add("7xxVernPersonSearch");
		// overlap author
		assertSearchResults(fldName, "vern796g", docIds, sis); 
		
		assertZeroResults(fldName, "vern796j", sis);
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_related_title_search: 797 field
	 */
@Test
	public final void testVernRelatedTitle797Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
//		assertSingleResult("79xVernSearch", fldName, "vern797d", sis);
//		assertSingleResult("79xVernSearch", fldName, "vern797f", sis);
//		assertSingleResult("79xVernSearch", fldName, "vern797g", sis);
//		assertSingleResult("79xVernSearch", fldName, "vern797k", sis);
		assertSingleResult("79xVernSearch", fldName, "vern797l", sis);
		assertSingleResult("79xVernSearch", fldName, "vern797m", sis);
//		assertSingleResult("79xVernSearch", fldName, "vern797n", sis);
		assertSingleResult("79xVernSearch", fldName, "vern797o", sis);
		assertSingleResult("79xVernSearch", fldName, "vern797p", sis);
		assertSingleResult("79xVernSearch", fldName, "vern797r", sis);
		assertSingleResult("79xVernSearch", fldName, "vern797s", sis);
		assertSingleResult("79xVernSearch", fldName, "vern797t", sis);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("79xVernSearch");
		docIds.add("7xxVernCorpSearch");
		// overlap author
		assertSearchResults(fldName, "vern797d", docIds, sis); 
		assertSearchResults(fldName, "vern797g", docIds, sis); 
		assertSearchResults(fldName, "vern797n", docIds, sis);
		// used to be in author
		assertSearchResults(fldName, "vern797f", docIds, sis);
		assertSearchResults(fldName, "vern797k", docIds, sis);
		
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_related_title_search: 798 field
	 */
@Test
	public final void testVernRelatedTitle798Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("79xVernSearch", fldName, "vern798f", sis);
//		assertSingleResult("79xVernSearch", fldName, "vern798g", sis);
		assertSingleResult("79xVernSearch", fldName, "vern798k", sis);
		assertSingleResult("79xVernSearch", fldName, "vern798l", sis);
//		assertSingleResult("79xVernSearch", fldName, "vern798n", sis);
		assertSingleResult("79xVernSearch", fldName, "vern798p", sis);
		assertSingleResult("79xVernSearch", fldName, "vern798s", sis);
		assertSingleResult("79xVernSearch", fldName, "vern798t", sis);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("79xVernSearch");
		docIds.add("7xxVernMeetingSearch");
		// overlap author
		assertSearchResults(fldName, "vern798g", docIds, sis); 
		assertSearchResults(fldName, "vern798n", docIds, sis);
		
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_related_title_search: 799 field
	 */
@Test
	public final void testVernRelatedTitle799Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("79xVernSearch", fldName, "vern799a", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799d", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799f", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799g", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799k", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799l", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799m", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799n", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799o", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799p", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799r", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799s", sis);
		assertSingleResult("79xVernSearch", fldName, "vern799t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * vern_title_series_search: 440, 490
	 */
	@Test
	public final void testVernSeriesTitle440_490Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("4xxVernSearch", fldName, "vern440a", sis);
		assertSingleResult("4xxVernSearch", fldName, "vern440n", sis);
		assertSingleResult("4xxVernSearch", fldName, "vern440p", sis);
		assertSingleResult("4xxVernSearch", fldName, "vern440v", sis);
		assertSingleResult("4xxVernSearch", fldName, "vern490a", sis);
		assertSingleResult("4xxVernSearch", fldName, "vern490v", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_title_series_search: 800
	 */
	@Test
	public final void testVernSeriesTitle800Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("8xxVernSearch", fldName, "vern800f", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800k", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800l", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800m", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800n", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800o", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800p", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800r", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800s", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800t", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern800v", sis);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("8xxVernSearch");
		docIds.add("800VernSearch");
		// overlap author
		assertSearchResults(fldName, "vern800g", docIds, sis); 
		
		assertZeroResults(fldName, "vern800j", sis);
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_title_series_search: 810
	 */
	@Test
	public final void testVernSeriesTitle810Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
//		assertSingleResult("8xxVernSearch", fldName, "vern810d", sis);
//		assertSingleResult("8xxVernSearch", fldName, "vern810f", sis);
//		assertSingleResult("8xxVernSearch", fldName, "vern810g", sis);
//		assertSingleResult("8xxVernSearch", fldName, "vern810k", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern810l", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern810m", sis);
//		assertSingleResult("8xxVernSearch", fldName, "vern810n", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern810o", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern810p", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern810r", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern810s", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern810t", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern810v", sis);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("8xxVernSearch");
		docIds.add("810VernSearch");
		// overlap author
		assertSearchResults(fldName, "vern810d", docIds, sis); 
		assertSearchResults(fldName, "vern810g", docIds, sis); 
		assertSearchResults(fldName, "vern810n", docIds, sis);
		// used to be in author
		assertSearchResults(fldName, "vern810f", docIds, sis);
		assertSearchResults(fldName, "vern810k", docIds, sis);
		
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_title_series_search: 811
	 */
	@Test
	public final void testVernSeriesTitle811Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("8xxVernSearch", fldName, "vern811f", sis);
//		assertSingleResult("8xxVernSearch", fldName, "vern811g", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern811k", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern811l", sis);
//		assertSingleResult("8xxVernSearch", fldName, "vern811n", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern811p", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern811s", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern811t", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern811v", sis);
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("8xxVernSearch");
		docIds.add("811VernSearch");
		// overlap author
		assertSearchResults(fldName, "vern811g", docIds, sis); 
		assertSearchResults(fldName, "vern811n", docIds, sis);
		
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_title_series_search: 830
	 */
	@Test
	public final void testVernSeriesTitle830Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("8xxVernSearch", fldName, "vern830a", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830d", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830f", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830g", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830k", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830l", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830m", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830n", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830o", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830p", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830r", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830s", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830t", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern830v", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * vern_title_series_search: 840
	 */
	@Test
	public final void testVernSeriesTitle840Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
		assertSingleResult("8xxVernSearch", fldName, "vern840a", sis);
		assertSingleResult("8xxVernSearch", fldName, "vern840v", sis);
		assertZeroResults(fldName, "nope", sis);
	}	

}
