package edu.stanford;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's title fields
 * @author Naomi Dushay
 *
 */
public class SubjectTests extends BibIndexTest {
	
	private final String testDataFname = "subjectTests.mrc";


	/**
	 * Test population and properties of topic field
	 */
@Test
	public final void testTopic() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(testDataFname);
		String fldName = "topic";
		assertTextFieldProps(fldName);
		assertFieldStored(fldName);

		// topic = 600abcdq:600t:610ab:610t:630a:630t:650a:655a

		// first one has period after two chars, so period left in.
		assertDocHasFieldValue("1261173", fldName, "Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it.");  // 600t
		assertDocHasFieldValue("115472", fldName, "European Economic Community");  // 610a
		assertDocHasFieldValue("1261173", fldName, "Magna Carta");  // 630a
		assertDocHasFieldValue("4698973", fldName, "Multiculturalism");  // 650a
		assertDocHasFieldValue("6552", fldName, "Dictionaries");  // 655a

		assertSingleResult("3743949", fldName, "Federico"); // 600a
		assertSingleResult("3743949", fldName, "1936");  // 600d
		assertSingleResult("919006", fldName, "Emesa");  // 600c
		assertSingleResult("1261173", fldName, "peace");  // 600t
		assertSingleResult("115472", fldName, "Economic");  // 610a
		assertSingleResult("1261173", fldName, "army");  // 610b
		assertSingleResult("1261173", fldName, "magna");  // 630a
		assertSingleResult("4698973", fldName, "Multiculturalism");  // 650a
		assertSingleResult("6552", fldName, "dictionaries");  // 655a
	}


	/**
	 * Test population and properties of topicStr field
	 *  topicStr is a copy field from topic ... an untokenized string stripped
	 *  of trailing punctuation 
	 */
@Test
	public final void testTopicStr() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(testDataFname);
        String fldName = "topicStr";
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
		
		// topicStr is a copy field from topic, but it is a string
		// topic = 600abcdq:600t:610ab:610t:630a:630t:650a:655a
		
		// 600a, trailing period removed
		assertSingleResult("345228", fldName, "\"Zemnukhov, Ivan\""); 
		assertZeroResults(fldName, "\"Zemnukhov, Ivan.\"");
		// 600acd, trailing period removed
		assertSingleResult("1261173", fldName, "\"Somers, John Somers, Baron, 1651-1716\"");  
		assertZeroResults(fldName, "\"Somers, John Somers, Baron, 1651-1716.\""); 
		// 600ad, trailing comma removed
		assertSingleResult("600trailingComma", fldName, "\"Monroe, Marilyn, 1926-1962\""); 
		assertZeroResults(fldName, "\"Monroe, Marilyn, 1926-1962,\"");
		// 600q now bundled with abcdq
		assertSingleResult("600aqdx", fldName, "\"Kennedy, John F. (John Fitzgerald), 1917-1963\""); 
		assertZeroResults(fldName, "\"(John Fitzgerald),\"");
		assertZeroResults(fldName, "\"(John Fitzgerald)\"");
		assertZeroResults(fldName, "\"Kennedy, John F. 1917-1963\"");
		// 600t, too few letters at end to remove trailing period
		assertSingleResult("1261173", fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it.\"");  // 630
		assertZeroResults(fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it\"");  // 630
		// 600p no longer included
		assertZeroResults(fldName, "\"Meditation;\"");
		assertZeroResults(fldName, "\"Meditation\"");
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963. Meditation\"");
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963. Meditation;\"");
		// 600ad 
		assertSingleResult("600adtpof", fldName, "\"Hindemith, Paul, 1895-1963\"");
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963.\"");
		// 600t separate
		assertSingleResult("600adtpof", fldName, "\"Nobilissima visione\""); 
		assertZeroResults(fldName, "\"Nobilissima visione.\"");
		
		// 610ab, trailing period removed
		assertSingleResult("1261173", fldName, "\"England and Wales. Army\"");
		assertZeroResults(fldName, "\"England and Wales. Army.\"");
		assertSingleResult("610trailing", fldName, "\"Augusta (Ga.)\""); 
		assertZeroResults(fldName, "\"Augusta (Ga.).\"");  
		// 610t separate
		assertSingleResult("610atpv", fldName, "\"Reports\""); 
		assertZeroResults(fldName, "\"Reports.\"");
		assertSingleResult("610atpv", fldName, "\"United States Strategic Bombing Survey\""); 
		assertZeroResults(fldName, "\"United States Strategic Bombing Survey.\"");
		// 610p no longer included
		assertZeroResults(fldName, "\"Pacific war\"");
		assertZeroResults(fldName, "\"United States Strategic Bombing Survey Pacific war\"");
		
		// 630a, trailing period
		assertSingleResult("1261173", fldName, "\"Magna Carta\"");  
		assertZeroResults(fldName, "\"Magna Carta.\"");  
		// 630p - no longer included
		assertZeroResults(fldName, "\"N.T.\""); 
		assertZeroResults(fldName, "\"N.T\"");  
		// 630s no longer included
		assertZeroResults(fldName, "\"Vulgate\"");  
		assertZeroResults(fldName, "\"Vulgate.\"");  
		Set<String> docIds = new HashSet<String>();
		docIds.add("630alsf");
		docIds.add("630trailing");
		assertSearchResults(fldName, "\"Bible\"", docIds);
		assertZeroResults(fldName, "\"Bible.\"");  

		// 650a, trailing period
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"");
		assertZeroResults(fldName, "\"Literature, Comparative.\"");  
		// 650a, trailing comma
		assertSingleResult("650trailingComma", fldName, "\"Seabiscuit (Race horse)\""); 
		assertZeroResults(fldName, "\"Seabiscuit (Race horse),\"");  
		assertZeroResults(fldName, "\"Seabiscuit (Race horse\"");  
		// 650a, trailing paren left in
		assertSingleResult("650trailing", fldName, "\"BASIC (Computer program language)\""); 
		assertZeroResults(fldName, "\"BASIC (Computer program language\"");
		
		// 655a, trailing period
		assertSingleResult("6551", fldName, "\"bust\""); 
		assertZeroResults(fldName, "\"bust.\"");
		// 655b no longer used
		assertZeroResults(fldName, "\"Laminated marblewood bust\"");
		assertZeroResults(fldName, "\"Laminated marblewood bust.\"");
		assertZeroResults(fldName, "\"Laminated marblewood\"");

// TODO: this should NOT be true once we have bad Lane topics out of the topic field
		docIds.clear();
		docIds.add("7233951");
		docIds.add("1132");
		docIds.add("1133");
		assertSearchResults(fldName, "\"Internet Resource\"", docIds);
	}

	/**
	 * Test population and properties of subtopic field
	 */
@Test
	public final void testSubtopic() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(testDataFname);
		String fldName = "subtopic";
		assertTextFieldProps(fldName);
		assertFieldNotStored(fldName);
		
		// subtopic = 600v:600x:600y:600z:610v:610x:610y:610z:630v:630x:630y:630z:650v:650x:650y:650z:655v:655x:655y:655z

		assertSingleResult("5666387", fldName, "congress"); // 600v
		assertSingleResult("3027805", fldName, "interview"); // 600x
		assertSingleResult("115472", fldName, "india"); // 610z
		assertSingleResult("1261173", fldName, "early"); // 650x
		assertSingleResult("111", fldName, "1955"); // 655y
		assertSingleResult("6553", fldName, "panama"); // 655z
	}

	/**
	 * Test population and properties of fulltopic field
	 */
@Test
	public final void testFullTopic() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(testDataFname);
		String fldName = "fulltopic";
		assertTextFieldProps(fldName);
		assertFieldStored(fldName);
		
		//  remember to look for subfields absent from other topic fields
		// fulltopic -  all 600, 610, 630, 650, 655
		// topic = 600abcd:600p:600q:600t:610ab:610p:610t:630a:630p:630s:630t:650a:655ab
		// subtopic = 600v:600x:600y:600z:610v:610x:610y:610z:630v:630x:630y:630z:650v:650x:650y:650z:655v:655x:655y:655z

		// field is stored - retrieve topic values from specific documents
		assertDocHasFieldValue("6808639", fldName, "880-04 Zhongguo gong chan dang Party work.");  // 610-6
		assertDocHasFieldValue("4698973", fldName, "Flyby missions. nasat");  // 650-2
		assertDocHasFieldValue("6553", fldName, "Municipal Fire Station records Fire Reports Atlanta, Georgia 1978 [thesaurus code]");  // 655-3
		// TODO:   note: trailing periods not accepted here.  Not sure why.
		assertSingleResult("345228", fldName, "\"Zemnukhov, Ivan\""); // 600a
		assertSingleResult("1261173", fldName, "\"Magna Carta\"");  // 630a
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"");  // 650a
		assertSingleResult("4698973", fldName, "Multiculturalism");  // 650a
		assertSingleResult("7233951", fldName, "\"Lectures\"");  // 655a

		// field is indexed and tokenized - search for values
		assertSingleResult("4698973", fldName, "nasat"); // 650-2
		assertSingleResult("6553", fldName, "fire"); // 655-3
		assertSingleResult("3743949", fldName, "Federico"); // 600a
		assertSingleResult("3743949", fldName, "1936");  // 600d
		assertSingleResult("919006", fldName, "Emesa");  // 600c
		assertSingleResult("1261173", fldName, "peace");  // 600t
		assertSingleResult("115472", fldName, "Economic");  // 610a
		assertSingleResult("1261173", fldName, "army");  // 610b
		assertSingleResult("1261173", fldName, "magna");  // 630a
		assertSingleResult("6552", fldName, "dictionaries");  // 655a
	}


	/**
	 * Test absence of genre fields
	 */
@Test
	public final void testGenreFields() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(testDataFname);
//		IndexReader ir = sis.getReader();
//		assertFieldNotPresent("fullgenre", ir);
//		assertFieldNotPresent("genre", ir);
//		assertFieldNotPresent("genreStr", ir);
//		assertFieldNotPresent("subgenre", ir);
	}

	/**
	 * Test multiple occurences of fullTopic fields when there
	 *  are multiple marc topic fields with the same tag.
	 */
@Test
	public final void testMultFullTopicFields() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(testDataFname);
		String fldName = "fulltopic";
		assertFieldStored(fldName);
		assertFieldIndexed(fldName);

		assertDocHasFieldValue("229800", fldName, "Commodity exchanges.");  
		assertDocHasFieldValue("229800", fldName, "Foreign exchange."); 
	
		assertSingleResult("229800", fldName, "Commodity exchanges."); 
		assertSingleResult("229800", fldName, "Foreign exchange."); 
	}	

	/**
	 * Test population of geographicStr field.  No trailing periods or 
	 *  commas.
	 */
@Test
	public final void testGeographicStr()
			throws ParserConfigurationException, IOException, SAXException
	{
        createIxInitVars(testDataFname);
		String fldName = "geographicStr";
		assertStringFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
	    // trailing period should be stripped
		Set<String> docIds = new HashSet<String>();
		docIds.add("651a");
		docIds.add("651again");
		assertSearchResults(fldName, "Muppets", docIds);
		assertSingleResult("651numPeriod", fldName, "7.150");
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y.)\"");
		assertZeroResults(fldName, "\"Syracuse (N.Y.\"");
		assertZeroResults(fldName, "\"Syracuse (N.Y\"");
		assertSingleResult("651siberia", fldName, "\"Siberia (Russia)\"");
		assertZeroResults(fldName, "\"Siberia (Russia).\"");
	}

	/**
	 * Test multiple occurrences of fullGeographic fields when there
	 *  are multiple 651 fields
	 */
@Test
	public final void testMultFullGeoFields() 
			throws ParserConfigurationException, IOException, SAXException 
	{
        createIxInitVars(testDataFname);
		String fldName = "fullgeographic";
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);

		assertSingleResult("6280316", fldName, "Tennessee"); 
		assertSingleResult("6280316", fldName, "Arkansas"); 
	}	

	/**
	 * Test population of era field
	 *  (no longer assigning "other" when unknown)
	 */
@Test
	public final void testEra() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "era";
		createIxInitVars("eraTests.mrc");
		assertFieldMultiValued(fldName);
		assertStringFieldProperties(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
	
	    // trailing period should be stripped
		Set<String> docIds = new HashSet<String>();
		docIds.add("650y");
		docIds.add("666");
		assertSearchResults(fldName, "\"20th century\"", docIds);
	    
	    // trailing period stripped after 3 digit year
		docIds.clear();
		docIds.add("888");
		docIds.add("999");
		assertSearchResults(fldName, "\"To 449\"", docIds);
	    assertZeroResults(fldName, "\"To 449.\"");
	    
	    // trailing period stripped after 4 digit year
	    assertSingleResult("111", fldName, "\"449-1066\"");
	    
	    // trailing dash - period NOT stripped
	    assertSingleResult("222", fldName, "\"1921-\"");
	    
	    // trailing period NOT stripped
	    assertSingleResult("777", fldName, "\"Roman period, 55 B.C.-449 A.D.\"");
	    
		// no longer assigning "other" when unknown (just leaving value out)
	    assertZeroResults(fldName, "other");
/*
	    docIds.clear();
	    docIds.add("no650or045a");
	    docIds.add("no650but045a");
	    assertSearchResults(fldName, "other", docIds);
*/
	}


	/**
	 * assert field is indexed, tokenized, has norms, does not have term vectors
	 *   and is multivalued.  (says nothing about stored)
	 */
	private final void assertTextFieldProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException
	{
//        createIxInitVars(testDataFname);
//		assertFieldPresent(fldName);
		assertFieldIndexed(fldName);
		assertFieldTokenized(fldName);
		assertFieldHasNorms(fldName);
		assertFieldHasNoTermVectors(fldName);
		assertFieldMultiValued(fldName);
	}

}
