package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

/**
 * junit4 tests for Stanford University physical fields for blacklight index
 * @author Naomi Dushay
 */
public class PhysicalTests extends BibIndexTest {
	
	/**
	 * physical:  test population of field for search and display
	 */
@Test
	public final void testPhysical() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "physical";
		createIxInitVars("physicalTests.mrc");
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);

		// 300abc
	    assertDocHasFieldValue("300111", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.", sis); 
	    // 300abce
	    assertDocHasFieldValue("300222", fldName, "271 p. : ill. ; 21 cm. + answer book.", sis); 
	    // 300 3afg
	    assertDocHasFieldValue("300333", fldName, "1 box 2 x 4 x 3 1/2 ft.", sis); 
	    // 300aafafc - in order ...
	    assertDocHasFieldValue("300444", fldName, "diary 1 volume (463 pages) ; 17 cm. x 34.5 cm.", sis); 
	    
		assertSingleResult("300111", fldName, "sound disc", sis);
		assertSingleResult("300111", fldName, "\"1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.\"", sis);
		assertSingleResult("300222", fldName, "answer", sis);
		assertSingleResult("300222", fldName, "\"271 p. : ill. ; 21 cm. + answer book.\"", sis);
		assertSingleResult("300333", fldName, "\"1 box 2 x 4 x 3 1/2 ft.\"", sis);
		assertSingleResult("300444", fldName, "\"diary 1 volume (463 pages) ; 17 cm. x 34.5 cm.\"", sis);	    

	    tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		assertDocHasFieldValue("3001", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.", sis); 
		assertDocHasFieldValue("3002", fldName, "1 box 2 x 4 x 3 1/2 ft.", sis); 
		assertDocHasFieldValue("3003", fldName, "17 boxes (7 linear ft.)", sis); 
		assertDocHasFieldValue("3004", fldName, "1 page ; 108 cm. x 34.5 cm.", sis); 
		assertDocHasFieldValue("3005", fldName, "65 prints : relief process ; 29 x 22 cm.", sis); 
		assertDocHasFieldValue("3005", fldName, "8 albums (550 photoprints) ; 51 x 46 cm. or smaller.", sis); 
	}

	/**
	 * vern_physical:  test population of field for search and display
	 */
@Test
	public final void testVernPhysical()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "vern_physical";
		createIxInitVars("vernacularSearchTests.mrc");
		assertTextFieldProperties(fldName, solrCore, sis);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("300VernSearch", fldName, "vern300a vern300b vern300c vern300e vern300f vern300g", sis);		
		assertDocHasNoFieldValue("300VernSearch", fldName, "none", sis);
		
		assertSingleResult("300VernSearch", fldName, "vern300a", sis);
		assertSingleResult("300VernSearch", fldName, "vern300b", sis);
		assertSingleResult("300VernSearch", fldName, "vern300c", sis);
		assertSingleResult("300VernSearch", fldName, "vern300e", sis);
		assertSingleResult("300VernSearch", fldName, "vern300f", sis);
		assertSingleResult("300VernSearch", fldName, "vern300g", sis);		
		assertZeroResults(fldName, "none", sis);
	}

}
