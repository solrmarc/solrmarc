package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import edu.stanford.StanfordIndexer.Access;
import org.xml.sax.SAXException;


public class LocationTests extends BibIndexTest {

	private final String testDataFname = "locationTests.mrc";
	private final String fldName = "building";
	
	/**
	 * set up the index for the tests
	 */
@Before
	@SuppressWarnings("unused")
	public final void createIndex() 
			throws IOException, ParserConfigurationException, SAXException {
		createIxInitVars(testDataFname);
	}



	/**
	 * test that some of the item records are NOT ignored!
	 */
@Test
	public final void testValidLocations() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		// building is indexed, not stored, so search to confirm
		String fldVal = "\"Green (Humanities & Social Sciences)\"";
		Set<String> docIds = new HashSet<String>();
		docIds.add("115472");
		docIds.add("1261173");
		docIds.add("2557826");
		assertSearchResults(fldName, fldVal, docIds, sis);
				
		fldVal = "\"Hoover Institution\"";
		docIds.clear();
		docIds.add("229800");
		docIds.add("460947");
		docIds.add("690002");
		assertSearchResults(fldName, fldVal, docIds, sis);
		
		assertSingleResult("7651581", fldName, "\"Off-campus (SAL3)\"", sis);
	}		
	
	
	/**
	 * test that item records are ignored when the locations so indicate
	 */
@Test
	public final void testShadowLocations() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		// building is indexed, not stored, so search to confirm buildings with
		//  only shadow locations in test data
		assertZeroResults(fldName, "\"Crown (Law)\"", sis);
		assertZeroResults(fldName, "\"Mathematical & Computer Sciences\"", sis);
		assertZeroResults(fldName, "\"Cubberley (Education)\"", sis);
		assertZeroResults(fldName, "\"Meyer\"", sis);
		assertZeroResults(fldName, "\"Stanford Auxiliary Library (SAL 1&2)\"", sis);
		assertZeroResults(fldName, "\"Stanford University Libraries\"", sis);
		
		// there are 3 green locations that aren't shadowed (confirmed in
		//  testValidLocations()
		assertResultSize(fldName, "\"Green (Humanities & Social Sciences)\"", 3, sis);
		
		// there is 1 SAL3 location that isn't shadowed (it's online)
		assertSingleResult("7651581", fldName, "\"Off-campus (SAL3)\"", sis);
	}


	/**
	 * test locations to ignore that aren't shadowed, nor internet 
	 */
@Test
	public final void testOtherIgnoredLocations() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// building is indexed, not stored, so search to confirm buildings with
		//  other ignored locations in test data

		// there are 3 green locations that aren't ignored for any reason
		//  (confirmed in testValidLocations()
		//   the docs that should be ignored are 345228, 575946, 919006
		assertResultSize(fldName, "\"Green (Humanities & Social Sciences)\"", 3, sis);
	}
	

	/**
	 * test for locations that are various flavors of online
	 */
@Test
	public final void testOnlineLocations() 
			throws ParserConfigurationException, IOException, SAXException
	{
		// building is indexed, not stored
		//   three of these locations are in hoover library
	 	assertDocHasNoFieldValue("115472", fldName, "Green", sis); 
		String fldVal = "\"Hoover Institution\"";
		Set<String> docIds = new HashSet<String>();
		docIds.add("229800");
		docIds.add("460947");
		docIds.add("690002");
		assertSearchResults(fldName, fldVal, docIds, sis);
	 	
	 	// accessMethod is indexed, not stored, so also test by searching
	 	String fldName = "accessMethod_facet";
	 	fldVal = Access.ONLINE.toString();
	 	docIds.clear();
	 	docIds.add("115472");
	 	docIds.add("229800");
	 	docIds.add("460947");
	 	docIds.add("690002");
	 	assertSearchResults(fldName, fldVal, docIds, sis);
	}

}
