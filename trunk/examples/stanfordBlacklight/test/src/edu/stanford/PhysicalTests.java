package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

/**
 * junit4 tests for Stanford University physical fields for blacklight index
 * @author Naomi Dushay
 */
public class PhysicalTests extends AbstractStanfordBlacklightTest {
	
	/**
	 * physical:  test population of field for search and display
	 */
@Test
	public final void testPhysical() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "physical";
		createIxInitVars("physicalTests.mrc");
//		assertTextFieldProperties(fldName);
//		assertFieldHasNorms(fldName);
//		assertFieldStored(fldName);
//		assertFieldIndexed(fldName);
//		assertFieldMultiValued(fldName);

		// 300abc
	    assertDocHasFieldValue("300111", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in."); 
	    // 300abce
	    assertDocHasFieldValue("300222", fldName, "271 p. : ill. ; 21 cm. + answer book."); 
	    // 300 3afg
	    assertDocHasFieldValue("300333", fldName, "1 box 2 x 4 x 3 1/2 ft."); 
	    // 300aafafc - in order ...
	    assertDocHasFieldValue("300444", fldName, "diary 1 volume (463 pages) ; 17 cm. x 34.5 cm."); 
	    
		assertSingleResult("300111", fldName, "sound disc");
		assertSingleResult("300111", fldName, "\"1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.\"");
		assertSingleResult("300222", fldName, "answer");
		assertSingleResult("300222", fldName, "\"271 p. : ill. ; 21 cm. + answer book.\"");
		assertSingleResult("300333", fldName, "\"1 box 2 x 4 x 3 1/2 ft.\"");
		assertSingleResult("300444", fldName, "\"diary 1 volume (463 pages) ; 17 cm. x 34.5 cm.\"");	    

	    tearDown();
		createIxInitVars("displayFieldsTests.mrc");
		assertDocHasFieldValue("3001", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in."); 
		assertDocHasFieldValue("3002", fldName, "1 box 2 x 4 x 3 1/2 ft."); 
		assertDocHasFieldValue("3003", fldName, "17 boxes (7 linear ft.)"); 
		assertDocHasFieldValue("3004", fldName, "1 page ; 108 cm. x 34.5 cm."); 
		assertDocHasFieldValue("3005", fldName, "65 prints : relief process ; 29 x 22 cm."); 
		assertDocHasFieldValue("3005", fldName, "8 albums (550 photoprints) ; 51 x 46 cm. or smaller."); 
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
//		assertTextFieldProperties(fldName);
//		assertFieldHasNorms(fldName);
//		assertFieldStored(fldName);
//		assertFieldIndexed(fldName);
//		assertFieldMultiValued(fldName);
		
		assertDocHasFieldValue("300VernSearch", fldName, "vern300a vern300b vern300c vern300e vern300f vern300g");		
		assertDocHasNoFieldValue("300VernSearch", fldName, "none");
		
		assertSingleResult("300VernSearch", fldName, "vern300a");
		assertSingleResult("300VernSearch", fldName, "vern300b");
		assertSingleResult("300VernSearch", fldName, "vern300c");
		assertSingleResult("300VernSearch", fldName, "vern300e");
		assertSingleResult("300VernSearch", fldName, "vern300f");
		assertSingleResult("300VernSearch", fldName, "vern300g");		
		assertZeroResults(fldName, "none");
	}

}
