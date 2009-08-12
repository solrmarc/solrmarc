package edu.stanford;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's url fields
 * @author Naomi Dushay
 */
public class UrlTests extends BibIndexTest {

	private final String testDataFname = "onlineFormat.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}

	/**
	 * test url_sfx_display field
	 */
@Test
	public final void testSFXUrls() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "url_sfx";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
			
		// 956 SFX fields
		assertDocHasFieldValue("mult856and956", fldName, "http://caslon.stanford.edu:3210/sfxlcl3?superLongURL", sis); 
		assertDocHasFieldValue("7117119", fldName, "http://caslon.stanford.edu:3210/sfxlcl3?url_ver=Z39.88-2004&ctx_ver=Z39.88-2004&ctx_enc=info:ofi/enc:UTF-8&rfr_id=info:sid/sfxit.com:opac_856&url_ctx_fmt=info:ofi/fmt:kev:mtx:ctx&sfx.ignore_date_threshold=1&rft.object_id=110978984448763&svc_val_fmt=info:ofi/fmt:kev:mtx:sch_svc&", sis); 
		assertDocHasFieldValue("newSfx", fldName, "http://library.stanford.edu/sfx?reallyLongLotsOfArgs", sis); 
		
		// 956 non-SFX fields (a representative few of them)
		assertDocHasNoField("956BlankIndicators", fldName, sis);
		assertDocHasNoField("956ind2is0", fldName, sis);
	}
	
	
	
	/**
	 * Test method for {@link edu.stanford.StanfordIndexer#getFullTextUrl(org.marc4j.marc.Record)}.
	 */
@Test
	public final void testFullTextUrls() throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "url_fulltext";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
	
		// fulltext url(s) in docs 
		assertDocHasFieldValue("856ind2is0", fldName, "http://www.netLibrary.com/urlapi.asp?action=summary&v=1&bookid=122436", sis); 
		assertDocHasFieldValue("856ind2is0Again", fldName, "http://www.url856.com/fulltext/ind2_0", sis); 
		assertDocHasFieldValue("856ind2is1NotToc", fldName, "http://www.url856.com/fulltext/ind2_1/not_toc", sis); 
		assertDocHasFieldValue("856ind2isBlankFulltext", fldName, "http://www.url856.com/fulltext/ind2_blank/not_toc", sis); 
		assertDocHasFieldValue("956BlankIndicators", fldName, "http://www.url956.com/fulltext/blankIndicators", sis); 
		assertDocHasFieldValue("956ind2is0", fldName, "http://www.url956.com/fulltext/ind2_is_0", sis); 
		assertDocHasFieldValue("956and856TOC", fldName, "http://www.url956.com/fulltext/ind2_is_blank", sis); 
		assertDocHasFieldValue("mult856and956", fldName, "http://www.sciencemag.org/", sis); 
		assertDocHasFieldValue("mult856and956", fldName, "http://www.jstor.org/journals/00368075.html", sis); 
		assertDocHasFieldValue("mult856and956", fldName, "http://www.sciencemag.org/archive/", sis); 
		assertDocHasFieldValue("956and856TOCand856suppl", fldName, "http://www.url956.com/fulltext/ind2_is_blank", sis); 
		
		// SFX url
		assertDocHasNoFieldValue("mult856and956", fldName, "http://caslon.stanford.edu:3210/sfxlcl3?superLongURL", sis); 
	
		// docs with no fulltext url in bib rec
		assertDocHasNoField("856ind2is1TocSubz", fldName, sis); 
		assertDocHasNoField("856ind2is1TocSub3", fldName, sis); 
		assertDocHasNoField("856ind2is2suppl", fldName, sis); 
		assertDocHasNoField("856ind2isBlankTocSubZ", fldName, sis); 
		assertDocHasNoField("856ind2isBlankTocSub3", fldName, sis); 
		assertDocHasNoField("856tocAnd856SupplNoFulltext", fldName, sis);
		
		// don't get jackson forms for off-site paging requests
		assertDocHasNoField("123http", fldName, sis);
		assertDocHasNoField("124http", fldName, sis);
	}
	

	/**
	 * Test url_suppl_display field contents
	 * Test method for {@link edu.stanford.StanfordIndexer#getSupplUrls(org.marc4j.marc.Record)}.
	 */
@Test
	public final void testSupplmentaryUrls() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "url_suppl";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
			
		// book
		assertDocHasFieldValue("856ind2is1TocSubz", fldName, "http://www.url856.com/ind2_1/toc_subz", sis); 
		assertDocHasFieldValue("856ind2is1TocSub3", fldName, "http://www.url856.com/ind2_1/toc_sub3", sis); 
		assertDocHasFieldValue("856ind2is2suppl", fldName, "http://www.url856.com/ind2_2/supplementaryMaterial", sis); 
		assertDocHasFieldValue("856ind2isBlankTocSubZ", fldName, "http://www.url856.com/ind2_blank/toc_subz", sis); 
		assertDocHasFieldValue("856ind2isBlankTocSub3", fldName, "http://www.url856.com/ind2_blank/toc_sub3", sis); 
		assertDocHasFieldValue("956and856TOC", fldName, "http://www.url856.com/toc", sis); 
		assertDocHasFieldValue("956and856TOCand856suppl", fldName, "http://www.url856.com/ind2_2/supplMaterial", sis); 
		assertDocHasFieldValue("956and856TOCand856suppl", fldName, "http://www.url856.com/toc", sis); 
		assertDocHasFieldValue("856tocAnd856SupplNoFulltext", fldName, "http://www.url856.com/toc", sis); 
		assertDocHasFieldValue("856tocAnd856SupplNoFulltext", fldName, "http://www.url856.com/ind2_2/supplMaterial", sis); 
		assertDocHasFieldValue("7423084", fldName, "http://www.loc.gov/catdir/samples/prin031/2001032103.html", sis); 
		assertDocHasFieldValue("7423084", fldName, "http://www.loc.gov/catdir/toc/prin031/2001032103.html", sis); 
		assertDocHasFieldValue("7423084", fldName, "http://www.loc.gov/catdir/description/prin022/2001032103.html", sis); 
	
		// docs with no urlSuppl_store in bib rec
		assertDocHasNoField("856ind2is0", fldName, sis); 
		assertDocHasNoField("856ind2is0Again", fldName, sis); 
		assertDocHasNoField("856ind2is1NotToc", fldName, sis); 
		assertDocHasNoField("856ind2isBlankFulltext", fldName, sis); 
		assertDocHasNoField("956BlankIndicators", fldName, sis); 
		assertDocHasNoField("956ind2is0", fldName, sis); 
		assertDocHasNoField("mult856and956", fldName, sis); 
	}

	/**
	 * url display fields should have appropriate properties.
	 */
@Test
	public final void testUrlDisplayFields() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		Set<String> urlFields = new HashSet<String>(3);
		urlFields.add("url_fulltext"); 
		urlFields.add("url_suppl"); 
		urlFields.add("url_sfx"); 
		for (String fldName : urlFields) 
		{
		    assertDisplayFldProps(fldName, solrCore, sis);
		    assertFieldMultiValued(fldName, solrCore);
		}
	}

}
