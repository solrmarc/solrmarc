package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

/**
 * junit4 tests for Stanford University series fields for blacklight index
 * @author Naomi Dushay
 */
public class SeriesTests extends BibIndexTest {
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("displayFieldsTests.mrc");
	}

	/**
	 * Test series_title_display field 
	 */
@Test
	public final void testSeriesTitleDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "series_title_display";
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("4401", fldName, "This American life", sis); 
		assertDocHasFieldValue("4402", fldName, "The Rare book tapes. Series 1 ; 5", sis); 
		assertDocHasFieldValue("4403", fldName, "Janua linguarum. Series maior, 100", sis); 
	}

	/**
	 * Test series_display field 
	 */
@Test
	public final void testSeriesDisplay() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "series_display";
		assertDisplayFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
		assertDocHasFieldValue("4901", fldName, "Education for living series.", sis); 
		assertDocHasFieldValue("4902", fldName, "Policy series / CES ; 1", sis); 
		assertDocHasFieldValue("4903", fldName, "Department of State publication ; 7846. Department and Foreign Service series ; 128", sis); 
		assertDocHasFieldValue("4904", fldName, "Memoire du BRGM, no 123", sis); 
		assertDocHasFieldValue("4905", fldName, "Annual census of manufactures = Recensement des manufactures,", sis); 
		assertDocHasFieldValue("4906", fldName, "Bulletin / Engineering Experiment Station ; no. 50", sis); 
		assertDocHasFieldValue("4907", fldName, "first 490 a first 490 v", sis); 
		assertDocHasFieldValue("4907", fldName, "second 490 a only", sis); 
		assertDocHasFieldValue("4907", fldName, "third 490 a third 490 v", sis); 
	}
}
