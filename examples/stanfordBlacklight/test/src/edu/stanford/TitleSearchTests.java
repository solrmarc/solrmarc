package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's title fields
 * @author Naomi Dushay
 */
public class TitleSearchTests extends BibIndexTest {
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("titleTests.mrc");
	}


	/**
	 * Test title_245a_search field
	 */
@Test
	public final void testTitle245aSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_245a_search";
		assertSearchFldOneValProps(fldName, solrCore, sis);
		
		assertSingleResult("245allSubs", fldName, "245a", sis);
		assertSingleResult("2xx", fldName, "2xx", sis);

		assertZeroResults(fldName, "electronic", sis);  // subfield h
		assertZeroResults(fldName, "john", sis);  // subfield c
		assertZeroResults(fldName, "handbook", sis);  // in subfield p
	}

	/**
	 * Test title_245_search field:  245 subfields a,b,f,g,k,n,p,s
	 */
@Test
	public final void testTitle24x5Search() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_245_search";
		assertSearchFldOneValProps(fldName, solrCore, sis);

		assertSingleResult("245allSubs", fldName, "245a", sis);
		assertSingleResult("245allSubs", fldName, "245b", sis);
		assertSingleResult("245allSubs", fldName, "245p1", sis);
		assertSingleResult("245allSubs", fldName, "245k1", sis);
		assertSingleResult("245allSubs", fldName, "245f", sis);
		assertSingleResult("245allSubs", fldName, "245n1", sis);
		assertSingleResult("245allSubs", fldName, "245g", sis);
		assertSingleResult("245allSubs", fldName, "245p2", sis);
		assertSingleResult("245allSubs", fldName, "245k2", sis);
		assertSingleResult("245allSubs", fldName, "245n2", sis);
		assertSingleResult("245allSubs", fldName, "245s", sis); 	
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("245NoNorP");
		docIds.add("245nNotp");
		docIds.add("245pNotn");
		docIds.add("245nAndp");
		docIds.add("245pThenn");
		docIds.add("245multpn");
		docIds.add("245allSubs");
		assertSearchResults(fldName, "245 n", docIds, sis);
		assertSingleResult("245pNotn", fldName, "handbook", sis);
		assertSingleResult("245pThenn", fldName, "Verzeichnis", sis);
		assertSingleResult("245nAndp", fldName, "humanities", sis);
	}

	/**
	 * uniform_title_search:  130 adfgklmnoprst and 240 adfgklmnoprs
	 */
@Test
	public final void testTitleUniformSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_uniform_search";
		assertSearchFldOneValProps(fldName, solrCore, sis);
		
		assertSingleResult("130240", fldName, "Hoos", sis);
		assertSingleResult("130240", fldName, "Foos", sis);
		assertZeroResults(fldName, "balloon", sis);
		assertZeroResults(fldName, "130", sis);
		
		assertSingleResult("130", fldName, "snimm", sis);
		assertSingleResult("130", fldName, "snimm", sis);
	}

	/**
	 * title_variant_search: 2xx (except 245) test 210, 222, 242
	 */
@Test
	public final void testVariantTitle210_222_242Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_variant_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);

		assertSingleResult("2xx", fldName, "210a", sis);
		assertSingleResult("2xx", fldName, "210b", sis);
		assertSingleResult("2xx", fldName, "222a", sis);
		assertSingleResult("2xx", fldName, "222b", sis);
		assertSingleResult("2xx", fldName, "242a", sis);
		assertSingleResult("2xx", fldName, "242b", sis);
		assertSingleResult("2xx", fldName, "242n", sis);
		assertSingleResult("2xx", fldName, "242p", sis);
		
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * title_variant_search: checking 243
	 */
@Test
	public final void testVariantTitle243Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_variant_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("2xx", fldName, "243a", sis);
		assertSingleResult("2xx", fldName, "243d", sis);
		assertSingleResult("2xx", fldName, "243f", sis);
		assertSingleResult("2xx", fldName, "243g", sis);
		assertSingleResult("2xx", fldName, "243k", sis);
		assertSingleResult("2xx", fldName, "243l", sis);
		assertSingleResult("2xx", fldName, "243m", sis);
		assertSingleResult("2xx", fldName, "243n", sis);
		assertSingleResult("2xx", fldName, "243o", sis);
		assertSingleResult("2xx", fldName, "243p", sis);
		assertSingleResult("2xx", fldName, "243r", sis);
		assertSingleResult("2xx", fldName, "243s", sis);
		
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * title_variant_search: 2xx (except 245) - checking 246 and 247
	 */
@Test
	public final void testVariantTitle246_247Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_variant_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("2xx", fldName, "246a", sis);
		assertSingleResult("2xx", fldName, "246b", sis);
		assertSingleResult("2xx", fldName, "246f", sis);
		assertSingleResult("2xx", fldName, "246g", sis);
		assertSingleResult("2xx", fldName, "246n", sis);
		assertSingleResult("2xx", fldName, "246p", sis);
		assertSingleResult("2xx", fldName, "247a", sis);
		assertSingleResult("2xx", fldName, "247b", sis);
		assertSingleResult("2xx", fldName, "247f", sis);
		assertSingleResult("2xx", fldName, "247g", sis);
		assertSingleResult("2xx", fldName, "247n", sis);
		assertSingleResult("2xx", fldName, "247p", sis);
		
		assertZeroResults(fldName, "nope", sis);
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("246aNo740");
		docIds.add("246aAnd740");
		assertSearchResults(fldName, "field 246 subfield a", docIds, sis);
	}

	/**
	 * related_title_search: 505t
	 */
@Test
	public final void testRelatedTitle505Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
		assertSingleResult("505", fldName, "505t", sis);
		assertZeroResults(fldName, "nope", sis);
	}


	/**
	 * related_title_search: 700 field
	 */
@Test
	public final void testRelatedTitle700Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("700", fldName, "700f", sis);
		assertSingleResult("700", fldName, "700g", sis);
		assertSingleResult("700", fldName, "700k", sis);
		assertSingleResult("700", fldName, "700l", sis);
		assertSingleResult("700", fldName, "700m", sis);
		assertSingleResult("700", fldName, "700n", sis);
		assertSingleResult("700", fldName, "700o", sis);
		assertSingleResult("700", fldName, "700p", sis);
		assertSingleResult("700", fldName, "700r", sis);
		assertSingleResult("700", fldName, "700s", sis);
		assertSingleResult("700", fldName, "700t", sis);

		assertZeroResults(fldName, "700d", sis);
		assertZeroResults(fldName, "700j", sis);
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 710 field
	 */
@Test
	public final void testRelatedTitle710Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("710", fldName, "710d", sis);
		assertSingleResult("710", fldName, "710f", sis);
		assertSingleResult("710", fldName, "710g", sis);
		assertSingleResult("710", fldName, "710k", sis);
		assertSingleResult("710", fldName, "710l", sis);
		assertSingleResult("710", fldName, "710m", sis);
		assertSingleResult("710", fldName, "710n", sis);
		assertSingleResult("710", fldName, "710o", sis);
		assertSingleResult("710", fldName, "710p", sis);
		assertSingleResult("710", fldName, "710r", sis);
		assertSingleResult("710", fldName, "710s", sis);
		assertSingleResult("710", fldName, "710t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 711 field
	 */
@Test
	public final void testRelatedTitle711Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("711", fldName, "711f", sis);
		assertSingleResult("711", fldName, "711g", sis);
		assertSingleResult("711", fldName, "711k", sis);
		assertSingleResult("711", fldName, "711l", sis);
		assertSingleResult("711", fldName, "711n", sis);
		assertSingleResult("711", fldName, "711p", sis);
		assertSingleResult("711", fldName, "711s", sis);
		assertSingleResult("711", fldName, "711t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 730 field
	 */
@Test
	public final void testRelatedTitle730Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("730", fldName, "730a", sis);
		assertSingleResult("730", fldName, "730d", sis);
		assertSingleResult("730", fldName, "730f", sis);
		assertSingleResult("730", fldName, "730g", sis);
		assertSingleResult("730", fldName, "730k", sis);
		assertSingleResult("730", fldName, "730l", sis);
		assertSingleResult("730", fldName, "730m", sis);
		assertSingleResult("730", fldName, "730n", sis);
		assertSingleResult("730", fldName, "730o", sis);
		assertSingleResult("730", fldName, "730p", sis);
		assertSingleResult("730", fldName, "730r", sis);
		assertSingleResult("730", fldName, "730s", sis);
		assertSingleResult("730", fldName, "730t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 740 field
	 */
@Test
	public final void testRelatedTitle740Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("740", fldName, "740a", sis);
		assertSingleResult("740", fldName, "740n", sis);
		assertSingleResult("740", fldName, "740p", sis);
		assertSingleResult("246aAnd740", fldName, "740 subfield a", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * related_title_search: 760, 762, 765, 767
	 */
@Test
	public final void testRelatedTitle76xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("76x", fldName, "760s", sis);
		assertSingleResult("76x", fldName, "760t", sis);
		assertSingleResult("76x", fldName, "762s", sis);
		assertSingleResult("76x", fldName, "762t", sis);
		assertSingleResult("76x", fldName, "765s", sis);
		assertSingleResult("76x", fldName, "765t", sis);
		assertSingleResult("76x", fldName, "767s", sis);
		assertSingleResult("76x", fldName, "767t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 770, 772, 773, 774, 775, 776, 777
	 */
@Test
	public final void testRelatedTitle77xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("77x", fldName, "770s", sis);
		assertSingleResult("77x", fldName, "770t", sis);
		assertSingleResult("77x", fldName, "772s", sis);
		assertSingleResult("77x", fldName, "772t", sis);
		assertSingleResult("77x", fldName, "773s", sis);
		assertSingleResult("77x", fldName, "773t", sis);
		assertSingleResult("77x", fldName, "774s", sis);
		assertSingleResult("77x", fldName, "774t", sis);
		assertSingleResult("77x", fldName, "775s", sis);
		assertSingleResult("77x", fldName, "775t", sis);
		assertSingleResult("77x", fldName, "776s", sis);
		assertSingleResult("77x", fldName, "776t", sis);
		assertSingleResult("77x", fldName, "777s", sis);
		assertSingleResult("77x", fldName, "777t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 780, 785, 786, 787
	 */
@Test
	public final void testRelatedTitle78xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("78x", fldName, "780s", sis);
		assertSingleResult("78x", fldName, "780t", sis);
		assertSingleResult("78x", fldName, "785s", sis);
		assertSingleResult("78x", fldName, "785t", sis);
		assertSingleResult("78x", fldName, "786s", sis);
		assertSingleResult("78x", fldName, "786t", sis);
		assertSingleResult("78x", fldName, "787s", sis);
		assertSingleResult("78x", fldName, "787t", sis);
	
		assertZeroResults(fldName, "nope", sis);
		
		assertZeroResults(fldName, "780aNott", sis);
		Set<String> docIds = new HashSet<String>();
		docIds.add("780tNota");
		docIds.add("780aAndt");
		docIds.add("780tNota");
		assertSearchResults(fldName, "780 subfield t", docIds, sis);
		
		assertZeroResults(fldName, "785aNott", sis);
		docIds.clear();
		docIds.add("785tNota");
		docIds.add("785aAndt");
		assertSearchResults(fldName, "785 subfield t", docIds, sis);
		docIds.clear();
		docIds.add("780tNota");
		docIds.add("785tNota");
		assertSearchResults(fldName, "only", docIds, sis);
	}

	/**
	 * related_title_search: 796
	 */
@Test
	public final void testRelatedTitle796Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("796", fldName, "796f", sis);
		assertSingleResult("796", fldName, "796g", sis);
		assertSingleResult("796", fldName, "796k", sis);
		assertSingleResult("796", fldName, "796l", sis);
		assertSingleResult("796", fldName, "796m", sis);
		assertSingleResult("796", fldName, "796n", sis);
		assertSingleResult("796", fldName, "796o", sis);
		assertSingleResult("796", fldName, "796p", sis);
		assertSingleResult("796", fldName, "796r", sis);
		assertSingleResult("796", fldName, "796s", sis);
		assertSingleResult("796", fldName, "796t", sis);
	
		assertZeroResults(fldName, "796d", sis);
		assertZeroResults(fldName, "796j", sis);
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 797 field
	 */
@Test
	public final void testRelatedTitle797Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("797", fldName, "797d", sis);
		assertSingleResult("797", fldName, "797f", sis);
		assertSingleResult("797", fldName, "797g", sis);
		assertSingleResult("797", fldName, "797k", sis);
		assertSingleResult("797", fldName, "797l", sis);
		assertSingleResult("797", fldName, "797m", sis);
		assertSingleResult("797", fldName, "797n", sis);
		assertSingleResult("797", fldName, "797o", sis);
		assertSingleResult("797", fldName, "797p", sis);
		assertSingleResult("797", fldName, "797r", sis);
		assertSingleResult("797", fldName, "797s", sis);
		assertSingleResult("797", fldName, "797t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 798 field
	 */
@Test
	public final void testRelatedTitle798Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("798", fldName, "798f", sis);
		assertSingleResult("798", fldName, "798g", sis);
		assertSingleResult("798", fldName, "798k", sis);
		assertSingleResult("798", fldName, "798l", sis);
		assertSingleResult("798", fldName, "798n", sis);
		assertSingleResult("798", fldName, "798p", sis);
		assertSingleResult("798", fldName, "798s", sis);
		assertSingleResult("798", fldName, "798t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * related_title_search: 799 field
	 */
@Test
	public final void testRelatedTitle799Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("799", fldName, "799a", sis);
		assertSingleResult("799", fldName, "799d", sis);
		assertSingleResult("799", fldName, "799f", sis);
		assertSingleResult("799", fldName, "799g", sis);
		assertSingleResult("799", fldName, "799k", sis);
		assertSingleResult("799", fldName, "799l", sis);
		assertSingleResult("799", fldName, "799m", sis);
		assertSingleResult("799", fldName, "799n", sis);
		assertSingleResult("799", fldName, "799o", sis);
		assertSingleResult("799", fldName, "799p", sis);
		assertSingleResult("799", fldName, "799r", sis);
		assertSingleResult("799", fldName, "799s", sis);
		assertSingleResult("799", fldName, "799t", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}

	/**
	 * Series Title Search: 440, 490
	 */
	@Test
	public final void testSeriesTitle440_490Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("4x0", fldName, "440a", sis);
		assertSingleResult("4x0", fldName, "440n", sis);
		assertSingleResult("4x0", fldName, "440p", sis);
		assertSingleResult("4x0", fldName, "440v", sis);
		assertSingleResult("4x0", fldName, "490a", sis);
		assertSingleResult("4x0", fldName, "490v", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * Series Title Search: 800
	 */
	@Test
	public final void testSeriesTitle800Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("800", fldName, "800f", sis);
		assertSingleResult("800", fldName, "800g", sis);
		assertSingleResult("800", fldName, "800k", sis);
		assertSingleResult("800", fldName, "800l", sis);
		assertSingleResult("800", fldName, "800m", sis);
		assertSingleResult("800", fldName, "800n", sis);
		assertSingleResult("800", fldName, "800o", sis);
		assertSingleResult("800", fldName, "800p", sis);
		assertSingleResult("800", fldName, "800r", sis);
		assertSingleResult("800", fldName, "800s", sis);
		assertSingleResult("800", fldName, "800t", sis);
		assertSingleResult("800", fldName, "800v", sis);
	
		assertZeroResults(fldName, "800d", sis);
		assertZeroResults(fldName, "800j", sis);
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * Series Title Search: 810
	 */
	@Test
	public final void testSeriesTitle810Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("810", fldName, "810d", sis);
		assertSingleResult("810", fldName, "810f", sis);
		assertSingleResult("810", fldName, "810g", sis);
		assertSingleResult("810", fldName, "810k", sis);
		assertSingleResult("810", fldName, "810l", sis);
		assertSingleResult("810", fldName, "810m", sis);
		assertSingleResult("810", fldName, "810n", sis);
		assertSingleResult("810", fldName, "810o", sis);
		assertSingleResult("810", fldName, "810p", sis);
		assertSingleResult("810", fldName, "810r", sis);
		assertSingleResult("810", fldName, "810s", sis);
		assertSingleResult("810", fldName, "810t", sis);
		assertSingleResult("810", fldName, "810v", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * Series Title Search: 811
	 */
	@Test
	public final void testSeriesTitle811Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("811", fldName, "811f", sis);
		assertSingleResult("811", fldName, "811g", sis);
		assertSingleResult("811", fldName, "811k", sis);
		assertSingleResult("811", fldName, "811l", sis);
		assertSingleResult("811", fldName, "811n", sis);
		assertSingleResult("811", fldName, "811p", sis);
		assertSingleResult("811", fldName, "811s", sis);
		assertSingleResult("811", fldName, "811t", sis);
		assertSingleResult("811", fldName, "811v", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * Series Title Search: 830
	 */
	@Test
	public final void testSeriesTitle830Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("830", fldName, "830a", sis);
		assertSingleResult("830", fldName, "830d", sis);
		assertSingleResult("830", fldName, "830f", sis);
		assertSingleResult("830", fldName, "830g", sis);
		assertSingleResult("830", fldName, "830k", sis);
		assertSingleResult("830", fldName, "830l", sis);
		assertSingleResult("830", fldName, "830m", sis);
		assertSingleResult("830", fldName, "830n", sis);
		assertSingleResult("830", fldName, "830o", sis);
		assertSingleResult("830", fldName, "830p", sis);
		assertSingleResult("830", fldName, "830r", sis);
		assertSingleResult("830", fldName, "830s", sis);
		assertSingleResult("830", fldName, "830t", sis);
		assertSingleResult("830", fldName, "830v", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}
	/**
	 * Series Title Search: 840
	 */
	@Test
	public final void testSeriesTitle840Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName, solrCore, sis);
	
		assertSingleResult("840", fldName, "840a", sis);
		assertSingleResult("840", fldName, "840v", sis);
	
		assertZeroResults(fldName, "nope", sis);
	}

}
