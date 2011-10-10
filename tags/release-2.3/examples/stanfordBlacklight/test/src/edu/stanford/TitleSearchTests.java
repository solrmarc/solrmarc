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
public class TitleSearchTests extends AbstractStanfordBlacklightTest {
	
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
		assertSearchFldOneValProps(fldName);
		
		assertSingleResult("245allSubs", fldName, "245a");
		assertSingleResult("2xx", fldName, "2xx");

		assertZeroResults(fldName, "electronic");  // subfield h
		assertZeroResults(fldName, "john");  // subfield c
		assertZeroResults(fldName, "handbook");  // in subfield p
	}

	/**
	 * Test title_245_search field:  245 subfields a,b,f,g,k,n,p,s
	 */
@Test
	public final void testTitle24x5Search() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_245_search";
		assertSearchFldOneValProps(fldName);

		assertSingleResult("245allSubs", fldName, "245a");
		assertSingleResult("245allSubs", fldName, "245b");
		assertSingleResult("245allSubs", fldName, "245p1");
		assertSingleResult("245allSubs", fldName, "245k1");
		assertSingleResult("245allSubs", fldName, "245f");
		assertSingleResult("245allSubs", fldName, "245n1");
		assertSingleResult("245allSubs", fldName, "245g");
		assertSingleResult("245allSubs", fldName, "245p2");
		assertSingleResult("245allSubs", fldName, "245k2");
		assertSingleResult("245allSubs", fldName, "245n2");
		assertSingleResult("245allSubs", fldName, "245s"); 	
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("245NoNorP");
		docIds.add("245nNotp");
		docIds.add("245pNotn");
		docIds.add("245nAndp");
		docIds.add("245pThenn");
		docIds.add("245multpn");
		docIds.add("245allSubs");
		assertSearchResults(fldName, "245 n", docIds);
		assertSingleResult("245pNotn", fldName, "handbook");
		assertSingleResult("245pThenn", fldName, "Verzeichnis");
		assertSingleResult("245nAndp", fldName, "humanities");
	}

	/**
	 * uniform_title_search:  130 adfgklmnoprst and 240 adfgklmnoprs
	 */
@Test
	public final void testTitleUniformSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_uniform_search";
		assertSearchFldOneValProps(fldName);
		
		assertSingleResult("130240", fldName, "Hoos");
		assertSingleResult("130240", fldName, "Foos");
		assertZeroResults(fldName, "balloon");
		assertZeroResults(fldName, "130");
		
		assertSingleResult("130", fldName, "snimm");
		assertSingleResult("130", fldName, "snimm");
	}

	/**
	 * title_variant_search: 2xx (except 245) test 210, 222, 242
	 */
@Test
	public final void testVariantTitle210_222_242Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_variant_search";
		assertSearchFldMultValProps(fldName);

		assertSingleResult("2xx", fldName, "210a");
		assertSingleResult("2xx", fldName, "210b");
		assertSingleResult("2xx", fldName, "222a");
		assertSingleResult("2xx", fldName, "222b");
		assertSingleResult("2xx", fldName, "242a");
		assertSingleResult("2xx", fldName, "242b");
		assertSingleResult("2xx", fldName, "242n");
		assertSingleResult("2xx", fldName, "242p");
		
		assertZeroResults(fldName, "nope");
	}

	/**
	 * title_variant_search: checking 243
	 */
@Test
	public final void testVariantTitle243Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_variant_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("2xx", fldName, "243a");
		assertSingleResult("2xx", fldName, "243d");
		assertSingleResult("2xx", fldName, "243f");
		assertSingleResult("2xx", fldName, "243g");
		assertSingleResult("2xx", fldName, "243k");
		assertSingleResult("2xx", fldName, "243l");
		assertSingleResult("2xx", fldName, "243m");
		assertSingleResult("2xx", fldName, "243n");
		assertSingleResult("2xx", fldName, "243o");
		assertSingleResult("2xx", fldName, "243p");
		assertSingleResult("2xx", fldName, "243r");
		assertSingleResult("2xx", fldName, "243s");
		
		assertZeroResults(fldName, "nope");
	}

	/**
	 * title_variant_search: 2xx (except 245) - checking 246 and 247
	 */
@Test
	public final void testVariantTitle246_247Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_variant_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("2xx", fldName, "246a");
		assertSingleResult("2xx", fldName, "246b");
		assertSingleResult("2xx", fldName, "246f");
		assertSingleResult("2xx", fldName, "246g");
		assertSingleResult("2xx", fldName, "246n");
		assertSingleResult("2xx", fldName, "246p");
		assertSingleResult("2xx", fldName, "247a");
		assertSingleResult("2xx", fldName, "247b");
		assertSingleResult("2xx", fldName, "247f");
		assertSingleResult("2xx", fldName, "247g");
		assertSingleResult("2xx", fldName, "247n");
		assertSingleResult("2xx", fldName, "247p");
		
		assertZeroResults(fldName, "nope");
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("246aNo740");
		docIds.add("246aAnd740");
		assertSearchResults(fldName, "field 246 subfield a", docIds);
	}

	/**
	 * related_title_search: 505t
	 */
@Test
	public final void testRelatedTitle505Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
		assertSingleResult("505", fldName, "505t");
		assertZeroResults(fldName, "nope");
	}


	/**
	 * related_title_search: 700 field
	 */
@Test
	public final void testRelatedTitle700Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("700", fldName, "700f");
		assertSingleResult("700", fldName, "700g");
		assertSingleResult("700", fldName, "700k");
		assertSingleResult("700", fldName, "700l");
		assertSingleResult("700", fldName, "700m");
		assertSingleResult("700", fldName, "700n");
		assertSingleResult("700", fldName, "700o");
		assertSingleResult("700", fldName, "700p");
		assertSingleResult("700", fldName, "700r");
		assertSingleResult("700", fldName, "700s");
		assertSingleResult("700", fldName, "700t");

		assertZeroResults(fldName, "700d");
		assertZeroResults(fldName, "700j");
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 710 field
	 */
@Test
	public final void testRelatedTitle710Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("710", fldName, "710d");
		assertSingleResult("710", fldName, "710f");
		assertSingleResult("710", fldName, "710g");
		assertSingleResult("710", fldName, "710k");
		assertSingleResult("710", fldName, "710l");
		assertSingleResult("710", fldName, "710m");
		assertSingleResult("710", fldName, "710n");
		assertSingleResult("710", fldName, "710o");
		assertSingleResult("710", fldName, "710p");
		assertSingleResult("710", fldName, "710r");
		assertSingleResult("710", fldName, "710s");
		assertSingleResult("710", fldName, "710t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 711 field
	 */
@Test
	public final void testRelatedTitle711Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("711", fldName, "711f");
		assertSingleResult("711", fldName, "711g");
		assertSingleResult("711", fldName, "711k");
		assertSingleResult("711", fldName, "711l");
		assertSingleResult("711", fldName, "711n");
		assertSingleResult("711", fldName, "711p");
		assertSingleResult("711", fldName, "711s");
		assertSingleResult("711", fldName, "711t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 730 field
	 */
@Test
	public final void testRelatedTitle730Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("730", fldName, "730a");
		assertSingleResult("730", fldName, "730d");
		assertSingleResult("730", fldName, "730f");
		assertSingleResult("730", fldName, "730g");
		assertSingleResult("730", fldName, "730k");
		assertSingleResult("730", fldName, "730l");
		assertSingleResult("730", fldName, "730m");
		assertSingleResult("730", fldName, "730n");
		assertSingleResult("730", fldName, "730o");
		assertSingleResult("730", fldName, "730p");
		assertSingleResult("730", fldName, "730r");
		assertSingleResult("730", fldName, "730s");
		assertSingleResult("730", fldName, "730t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 740 field
	 */
@Test
	public final void testRelatedTitle740Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("740", fldName, "740a");
		assertSingleResult("740", fldName, "740n");
		assertSingleResult("740", fldName, "740p");
		assertSingleResult("246aAnd740", fldName, "740 subfield a");
	
		assertZeroResults(fldName, "nope");
	}

	/**
	 * related_title_search: 760, 762, 765, 767
	 */
@Test
	public final void testRelatedTitle76xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("76x", fldName, "760s");
		assertSingleResult("76x", fldName, "760t");
		assertSingleResult("76x", fldName, "762s");
		assertSingleResult("76x", fldName, "762t");
		assertSingleResult("76x", fldName, "765s");
		assertSingleResult("76x", fldName, "765t");
		assertSingleResult("76x", fldName, "767s");
		assertSingleResult("76x", fldName, "767t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 770, 772, 773, 774, 775, 776, 777
	 */
@Test
	public final void testRelatedTitle77xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("77x", fldName, "770s");
		assertSingleResult("77x", fldName, "770t");
		assertSingleResult("77x", fldName, "772s");
		assertSingleResult("77x", fldName, "772t");
		assertSingleResult("77x", fldName, "773s");
		assertSingleResult("77x", fldName, "773t");
		assertSingleResult("77x", fldName, "774s");
		assertSingleResult("77x", fldName, "774t");
		assertSingleResult("77x", fldName, "775s");
		assertSingleResult("77x", fldName, "775t");
		assertSingleResult("77x", fldName, "776s");
		assertSingleResult("77x", fldName, "776t");
		assertSingleResult("77x", fldName, "777s");
		assertSingleResult("77x", fldName, "777t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 780, 785, 786, 787
	 */
@Test
	public final void testRelatedTitle78xSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("78x", fldName, "780s");
		assertSingleResult("78x", fldName, "780t");
		assertSingleResult("78x", fldName, "785s");
		assertSingleResult("78x", fldName, "785t");
		assertSingleResult("78x", fldName, "786s");
		assertSingleResult("78x", fldName, "786t");
		assertSingleResult("78x", fldName, "787s");
		assertSingleResult("78x", fldName, "787t");
	
		assertZeroResults(fldName, "nope");
		
		assertZeroResults(fldName, "780aNott");
		Set<String> docIds = new HashSet<String>();
		docIds.add("780tNota");
		docIds.add("780aAndt");
		docIds.add("780tNota");
		assertSearchResults(fldName, "780 subfield t", docIds);
		
		assertZeroResults(fldName, "785aNott");
		docIds.clear();
		docIds.add("785tNota");
		docIds.add("785aAndt");
		assertSearchResults(fldName, "785 subfield t", docIds);
		docIds.clear();
		docIds.add("780tNota");
		docIds.add("785tNota");
		assertSearchResults(fldName, "only", docIds);
	}

	/**
	 * related_title_search: 796
	 */
@Test
	public final void testRelatedTitle796Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("796", fldName, "796f");
		assertSingleResult("796", fldName, "796g");
		assertSingleResult("796", fldName, "796k");
		assertSingleResult("796", fldName, "796l");
		assertSingleResult("796", fldName, "796m");
		assertSingleResult("796", fldName, "796n");
		assertSingleResult("796", fldName, "796o");
		assertSingleResult("796", fldName, "796p");
		assertSingleResult("796", fldName, "796r");
		assertSingleResult("796", fldName, "796s");
		assertSingleResult("796", fldName, "796t");
	
		assertZeroResults(fldName, "796d");
		assertZeroResults(fldName, "796j");
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 797 field
	 */
@Test
	public final void testRelatedTitle797Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("797", fldName, "797d");
		assertSingleResult("797", fldName, "797f");
		assertSingleResult("797", fldName, "797g");
		assertSingleResult("797", fldName, "797k");
		assertSingleResult("797", fldName, "797l");
		assertSingleResult("797", fldName, "797m");
		assertSingleResult("797", fldName, "797n");
		assertSingleResult("797", fldName, "797o");
		assertSingleResult("797", fldName, "797p");
		assertSingleResult("797", fldName, "797r");
		assertSingleResult("797", fldName, "797s");
		assertSingleResult("797", fldName, "797t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 798 field
	 */
@Test
	public final void testRelatedTitle798Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("798", fldName, "798f");
		assertSingleResult("798", fldName, "798g");
		assertSingleResult("798", fldName, "798k");
		assertSingleResult("798", fldName, "798l");
		assertSingleResult("798", fldName, "798n");
		assertSingleResult("798", fldName, "798p");
		assertSingleResult("798", fldName, "798s");
		assertSingleResult("798", fldName, "798t");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * related_title_search: 799 field
	 */
@Test
	public final void testRelatedTitle799Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_related_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("799", fldName, "799a");
		assertSingleResult("799", fldName, "799d");
		assertSingleResult("799", fldName, "799f");
		assertSingleResult("799", fldName, "799g");
		assertSingleResult("799", fldName, "799k");
		assertSingleResult("799", fldName, "799l");
		assertSingleResult("799", fldName, "799m");
		assertSingleResult("799", fldName, "799n");
		assertSingleResult("799", fldName, "799o");
		assertSingleResult("799", fldName, "799p");
		assertSingleResult("799", fldName, "799r");
		assertSingleResult("799", fldName, "799s");
		assertSingleResult("799", fldName, "799t");
	
		assertZeroResults(fldName, "nope");
	}

	/**
	 * Series Title Search: 440, 490
	 */
	@Test
	public final void testSeriesTitle440_490Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("4x0", fldName, "440a");
		assertSingleResult("4x0", fldName, "440n");
		assertSingleResult("4x0", fldName, "440p");
		assertSingleResult("4x0", fldName, "440v");
		assertSingleResult("4x0", fldName, "490a");
		assertSingleResult("4x0", fldName, "490v");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * Series Title Search: 800
	 */
	@Test
	public final void testSeriesTitle800Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("800", fldName, "800f");
		assertSingleResult("800", fldName, "800g");
		assertSingleResult("800", fldName, "800k");
		assertSingleResult("800", fldName, "800l");
		assertSingleResult("800", fldName, "800m");
		assertSingleResult("800", fldName, "800n");
		assertSingleResult("800", fldName, "800o");
		assertSingleResult("800", fldName, "800p");
		assertSingleResult("800", fldName, "800r");
		assertSingleResult("800", fldName, "800s");
		assertSingleResult("800", fldName, "800t");
		assertSingleResult("800", fldName, "800v");
	
		assertZeroResults(fldName, "800d");
		assertZeroResults(fldName, "800j");
		assertZeroResults(fldName, "nope");
	}
	/**
	 * Series Title Search: 810
	 */
	@Test
	public final void testSeriesTitle810Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("810", fldName, "810d");
		assertSingleResult("810", fldName, "810f");
		assertSingleResult("810", fldName, "810g");
		assertSingleResult("810", fldName, "810k");
		assertSingleResult("810", fldName, "810l");
		assertSingleResult("810", fldName, "810m");
		assertSingleResult("810", fldName, "810n");
		assertSingleResult("810", fldName, "810o");
		assertSingleResult("810", fldName, "810p");
		assertSingleResult("810", fldName, "810r");
		assertSingleResult("810", fldName, "810s");
		assertSingleResult("810", fldName, "810t");
		assertSingleResult("810", fldName, "810v");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * Series Title Search: 811
	 */
	@Test
	public final void testSeriesTitle811Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("811", fldName, "811f");
		assertSingleResult("811", fldName, "811g");
		assertSingleResult("811", fldName, "811k");
		assertSingleResult("811", fldName, "811l");
		assertSingleResult("811", fldName, "811n");
		assertSingleResult("811", fldName, "811p");
		assertSingleResult("811", fldName, "811s");
		assertSingleResult("811", fldName, "811t");
		assertSingleResult("811", fldName, "811v");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * Series Title Search: 830
	 */
	@Test
	public final void testSeriesTitle830Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("830", fldName, "830a");
		assertSingleResult("830", fldName, "830d");
		assertSingleResult("830", fldName, "830f");
		assertSingleResult("830", fldName, "830g");
		assertSingleResult("830", fldName, "830k");
		assertSingleResult("830", fldName, "830l");
		assertSingleResult("830", fldName, "830m");
		assertSingleResult("830", fldName, "830n");
		assertSingleResult("830", fldName, "830o");
		assertSingleResult("830", fldName, "830p");
		assertSingleResult("830", fldName, "830r");
		assertSingleResult("830", fldName, "830s");
		assertSingleResult("830", fldName, "830t");
		assertSingleResult("830", fldName, "830v");
	
		assertZeroResults(fldName, "nope");
	}
	/**
	 * Series Title Search: 840
	 */
	@Test
	public final void testSeriesTitle840Search()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "title_series_search";
		assertSearchFldMultValProps(fldName);
	
		assertSingleResult("840", fldName, "840a");
		assertSingleResult("840", fldName, "840v");
	
		assertZeroResults(fldName, "nope");
	}

}
