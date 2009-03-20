package edu.stanford;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;

import edu.stanford.StanfordIndexer.Access;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's location fields (building_facet, 
 *  access_facet)
 * @author Naomi Dushay
 */
public class LocationTests extends BibIndexTest {

	private final String testDataFname = "locationTests.mrc";
	private final String fldName = "building_facet";
	
	/**
	 * set up the index for the tests
	 */
@Before
	@SuppressWarnings("unused")
	public final void setup() 
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
	 * Test population of building field (a facet)
	 */
@Test
	public final void testBuilding() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		tearDown();
		createIxInitVars("buildingTests.mrc");
		assertFacetFldProps(fldName, solrCore, sis);
		assertFieldMultiValued(fldName, solrCore);
		
	    assertSingleResult("115472", fldName, "\"Applied Physics Department\"", sis);
	    assertSingleResult("229800", fldName, "\"Archive of Recorded Sound\"", sis);
	    assertSingleResult("345228", fldName, "\"Art & Architecture\"", sis);
	    assertSingleResult("460947", fldName, "\"Falconer (Biology)\"", sis);
	    assertSingleResult("804724", fldName, "\"Off-campus (Newark)\"", sis);
	    assertSingleResult("919006", fldName, "\"Swain (Chemistry & Chem. Engineering)\"", sis);
	    assertSingleResult("1147269", fldName, "Classics", sis);
	    assertSingleResult("1391080", fldName, "\"Green - Current Periodicals & Microtext\"", sis);
	    assertSingleResult("1505065", fldName, "\"Branner (Earth Sciences & Maps)\"", sis);
	    assertSingleResult("1618836", fldName, "\"Cubberley (Education)\"", sis);
	    assertSingleResult("1732616", fldName, "\"Mathematical & Computer Sciences\"", sis);
	    assertSingleResult("1849258", fldName, "Engineering", sis);
	    assertZeroResults(fldName, "\"Government Documents\"", sis);
	    assertSingleResult("2678655", fldName, "\"Jackson (Business)\"", sis);
	    assertSingleResult("2797607", fldName, "Meyer", sis);
	    assertSingleResult("3027805", fldName, "\"Miller (Hopkins Marine Station)\"", sis);
	    assertSingleResult("3142611", fldName, "Physics", sis);
	    assertSingleResult("3400092", fldName, "\"Hoover Institution Archives\"", sis);
	    assertSingleResult("3743949", fldName, "\"Hoover Institution\"", sis);
	    assertSingleResult("4258089", fldName, "\"Special Collections & University Archives\"", sis);
	    assertSingleResult("4428936", fldName, "\"Tanner (Philosophy)\"", sis);
	    assertSingleResult("4823592", fldName, "\"Crown (Law)\"", sis);
	    assertSingleResult("5666387", fldName, "Music", sis);
	    assertSingleResult("6676531", fldName, "\"East Asia\"", sis);
	
	    Set<String> docIds = new HashSet<String>();
	    docIds.add("1033119");
	    docIds.add("1261173");
	    docIds.add("2557826");
	    docIds.add("3941911");
	    docIds.add("4114632");
	    docIds.add("2442876");  //GRN-REF Green - Reference
	    docIds.add("1111");  // ILB = Inter-Library Borrowing
	    docIds.add("2222");  // SPEC-DESK = Special Collections Loan Desk
	    // checked out
	    docIds.add("575946");
	    // NOT  3277173  (withdrawn)
	    assertSearchResults(fldName, "\"Green (Humanities & Social Sciences)\"", docIds, sis);
	
	    docIds.clear();
	    docIds.add("1033119");
	    docIds.add("1962398");
	    docIds.add("2328381");
	    docIds.add("2913114");
	    assertSearchResults(fldName, "\"Stanford Auxiliary Library (SAL 1&2)\"", docIds, sis);
	
	    docIds.clear();
	    docIds.add("690002");
	    docIds.add("2328381");
	    docIds.add("3941911");
	    docIds.add("7651581");
	    // education - withdrawn;  SAL3 stacks
	    docIds.add("2214009");
	    assertSearchResults(fldName, "\"Off-campus (SAL3)\"", docIds, sis);
	
	    docIds.clear();
	    docIds.add("7370014");
	    // ask@lane
	    docIds.add("7233951");
	    assertSearchResults(fldName, "\"Lane (Medical)\"", docIds, sis);
	
	    docIds.clear();
	    docIds.add("6493823");
		// INTERNET
	    docIds.add("7117119");
	    assertSearchResults(fldName, "\"Stanford University Libraries\"", docIds, sis);
	}
	
	/**
	 * test that item records are ignored when the locations so indicate
	 */
@Test
	public final void testShadowLocations() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		// confirm buildings with only shadow locations don't get a value
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
		//   three of these locations are in hoover library
	 	assertDocHasNoFieldValue("115472", fldName, "Green", sis); 
		String fldVal = "\"Hoover Institution\"";
		Set<String> docIds = new HashSet<String>();
		docIds.add("229800");
		docIds.add("460947");
		docIds.add("690002");
		assertSearchResults(fldName, fldVal, docIds, sis);
	 	
	 	// access_facet is indexed, not stored, so also test by searching
	 	String fldName = "access_facet";
	 	fldVal = Access.ONLINE.toString();
	 	docIds.clear();
	 	docIds.add("115472");
	 	docIds.add("229800");
	 	docIds.add("460947");
	 	docIds.add("690002");
	 	assertSearchResults(fldName, fldVal, docIds, sis);
	}

}
