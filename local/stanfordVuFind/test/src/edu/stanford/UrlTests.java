package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


public class UrlTests extends BibIndexTest {

	private final String testDataFname = "onlineFormat.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}

	/**
	 * Test method for {@link edu.stanford.StanfordIndexer#getFullTextUrl(org.marc4j.marc.Record)}.
	 */
@Test
	public final void testFullTextUrls() throws IOException, ParserConfigurationException, SAXException 
	{
// TODO: rename field to urlFullText_store
		String fldName = "url";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
	
		// field is stored - retrieve values from specific documents
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
	}
	
	/**
	 * test urlSfx_store field
	 */
@Test
	public final void testSFXUrls() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "urlSfx_store";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
			
		// 956 SFX fields
		// field is stored - retrieve format values from specific documents
		assertDocHasFieldValue("mult856and956", fldName, "http://caslon.stanford.edu:3210/sfxlcl3?superLongURL", sis); 
		assertDocHasFieldValue("7117119", fldName, "http://caslon.stanford.edu:3210/sfxlcl3?url_ver=Z39.88-2004&ctx_ver=Z39.88-2004&ctx_enc=info:ofi/enc:UTF-8&rfr_id=info:sid/sfxit.com:opac_856&url_ctx_fmt=info:ofi/fmt:kev:mtx:ctx&sfx.ignore_date_threshold=1&rft.object_id=110978984448763&svc_val_fmt=info:ofi/fmt:kev:mtx:sch_svc&", sis); 
		
		// 956 non-SFX fields (a representative few of them)
		assertDocHasNoField("956BlankIndicators", fldName, sis);
		assertDocHasNoField("956ind2is0", fldName, sis);
	}
	
	
	/**
	 * Test urlSuppl_store field contents
	 * Test method for {@link edu.stanford.StanfordIndexer#getSupplUrls(org.marc4j.marc.Record)}.
	 */
@Test
	public final void testSupplmentaryUrls() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "urlSuppl_store";
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore, sis);
		assertFieldNotIndexed(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
			
		// field is stored - retrieve format values from specific documents
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

}
