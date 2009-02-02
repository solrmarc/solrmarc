package edu.stanford;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.index.IndexReader;
import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's title fields
 * @author Naomi Dushay
 *
 */
public class SubjectTests extends BibIndexTest {
	
	private final String testDataFname = "subjectTests.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
	}


	/**
	 * Test population and properties of topic field
	 */
@Test
	public final void testTopic() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "topic";
		assertTextFieldProps(fldName);
		assertFieldStored(fldName, solrCore);

		// topic = 600abcdq:600t:610ab:610t:630a:630t:650a:655a

		// first one has period after two chars, so period left in.
		assertDocHasFieldValue("1261173", fldName, "Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it.", sis);  // 600t
		assertDocHasFieldValue("115472", fldName, "European Economic Community", sis);  // 610a
		assertDocHasFieldValue("1261173", fldName, "Magna Carta", sis);  // 630a
		assertDocHasFieldValue("4698973", fldName, "Multiculturalism", sis);  // 650a
		assertDocHasFieldValue("6552", fldName, "Dictionaries", sis);  // 655a

		assertSingleResult("3743949", fldName, "Federico", sis); // 600a
		assertSingleResult("3743949", fldName, "1936", sis);  // 600d
		assertSingleResult("919006", fldName, "Emesa", sis);  // 600c
		assertSingleResult("1261173", fldName, "peace", sis);  // 600t
		assertSingleResult("115472", fldName, "Economic", sis);  // 610a
		assertSingleResult("1261173", fldName, "army", sis);  // 610b
		assertSingleResult("1261173", fldName, "magna", sis);  // 630a
		assertSingleResult("4698973", fldName, "Multiculturalism", sis);  // 650a
		assertSingleResult("6552", fldName, "dictionaries", sis);  // 655a
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
		String fldName = "topicStr";
		assertStringFieldProperties(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		
		// topicStr is a copy field from topic, but it is a string
		// topic = 600abcdq:600t:610ab:610t:630a:630t:650a:655a
		
		// 600a, trailing period removed
		assertSingleResult("345228", fldName, "\"Zemnukhov, Ivan\"", sis); 
		assertZeroResults(fldName, "\"Zemnukhov, Ivan.\"", sis);
		// 600acd, trailing period removed
		assertSingleResult("1261173", fldName, "\"Somers, John Somers, Baron, 1651-1716\"", sis);  
		assertZeroResults(fldName, "\"Somers, John Somers, Baron, 1651-1716.\"", sis); 
		// 600ad, trailing comma removed
		assertSingleResult("600trailingComma", fldName, "\"Monroe, Marilyn, 1926-1962\"", sis); 
		assertZeroResults(fldName, "\"Monroe, Marilyn, 1926-1962,\"", sis);
		// 600q now bundled with abcdq
		assertSingleResult("600aqdx", fldName, "\"Kennedy, John F. (John Fitzgerald), 1917-1963\"", sis); 
		assertZeroResults(fldName, "\"(John Fitzgerald),\"", sis);
		assertZeroResults(fldName, "\"(John Fitzgerald)\"", sis);
		assertZeroResults(fldName, "\"Kennedy, John F. 1917-1963\"", sis);
		// 600t, too few letters at end to remove trailing period
		assertSingleResult("1261173", fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it.\"", sis);  // 630
		assertZeroResults(fldName, "\"Letter ballancing the necessity of keeping a land-force in times of peace, with the dangers that may follow on it\"", sis);  // 630
		// 600p no longer included
		assertZeroResults(fldName, "\"Meditation;\"", sis);
		assertZeroResults(fldName, "\"Meditation\"", sis);
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963. Meditation\"", sis);
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963. Meditation;\"", sis);
		// 600ad 
		assertSingleResult("600adtpof", fldName, "\"Hindemith, Paul, 1895-1963\"", sis);
		assertZeroResults(fldName, "\"Hindemith, Paul, 1895-1963.\"", sis);
		// 600t separate
		assertSingleResult("600adtpof", fldName, "\"Nobilissima visione\"", sis); 
		assertZeroResults(fldName, "\"Nobilissima visione.\"", sis);
		
		// 610ab, trailing period removed
		assertSingleResult("1261173", fldName, "\"England and Wales. Army\"", sis);
		assertZeroResults(fldName, "\"England and Wales. Army.\"", sis);
		assertSingleResult("610trailing", fldName, "\"Augusta (Ga.)\"", sis); 
		assertZeroResults(fldName, "\"Augusta (Ga.).\"", sis);  
		// 610t separate
		assertSingleResult("610atpv", fldName, "\"Reports\"", sis); 
		assertZeroResults(fldName, "\"Reports.\"", sis);
		assertSingleResult("610atpv", fldName, "\"United States Strategic Bombing Survey\"", sis); 
		assertZeroResults(fldName, "\"United States Strategic Bombing Survey.\"", sis);
		// 610p no longer included
		assertZeroResults(fldName, "\"Pacific war\"", sis);
		assertZeroResults(fldName, "\"United States Strategic Bombing Survey Pacific war\"", sis);
		
		// 630a, trailing period
		assertSingleResult("1261173", fldName, "\"Magna Carta\"", sis);  
		assertZeroResults(fldName, "\"Magna Carta.\"", sis);  
		// 630p - no longer included
		assertZeroResults(fldName, "\"N.T.\"", sis); 
		assertZeroResults(fldName, "\"N.T\"", sis);  
		// 630s no longer included
		assertZeroResults(fldName, "\"Vulgate\"", sis);  
		assertZeroResults(fldName, "\"Vulgate.\"", sis);  
		Set<String> docIds = new HashSet<String>();
		docIds.add("630alsf");
		docIds.add("630trailing");
		assertSearchResults(fldName, "\"Bible\"", docIds, sis);
		assertZeroResults(fldName, "\"Bible.\"", sis);  

		// 650a, trailing period
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"", sis);
		assertZeroResults(fldName, "\"Literature, Comparative.\"", sis);  
		// 650a, trailing comma
		assertSingleResult("650trailingComma", fldName, "\"Seabiscuit (Race horse)\"", sis); 
		assertZeroResults(fldName, "\"Seabiscuit (Race horse),\"", sis);  
		assertZeroResults(fldName, "\"Seabiscuit (Race horse\"", sis);  
		// 650a, trailing paren left in
		assertSingleResult("650trailing", fldName, "\"BASIC (Computer program language)\"", sis); 
		assertZeroResults(fldName, "\"BASIC (Computer program language\"", sis);
		
		// 655a, trailing period
		assertSingleResult("6551", fldName, "\"bust\"", sis); 
		assertZeroResults(fldName, "\"bust.\"", sis);
		// 655b no longer used
		assertZeroResults(fldName, "\"Laminated marblewood bust\"", sis);
		assertZeroResults(fldName, "\"Laminated marblewood bust.\"", sis);
		assertZeroResults(fldName, "\"Laminated marblewood\"", sis);

// TODO: this should NOT be true once we have bad Lane topics out of the topic field
		docIds.clear();
		docIds.add("7233951");
		docIds.add("1132");
		docIds.add("1133");
		assertSearchResults(fldName, "\"Internet Resource\"", docIds, sis);
	}

	/**
	 * Test population and properties of subtopic field
	 */
@Test
	public final void testSubtopic() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "subtopic";
		assertTextFieldProps(fldName);
		assertFieldNotStored(fldName, solrCore);
		
		// subtopic = 600v:600x:600y:600z:610v:610x:610y:610z:630v:630x:630y:630z:650v:650x:650y:650z:655v:655x:655y:655z

		assertSingleResult("5666387", fldName, "congress", sis); // 600v
		assertSingleResult("3027805", fldName, "interview", sis); // 600x
		assertSingleResult("115472", fldName, "india", sis); // 610z
		assertSingleResult("1261173", fldName, "early", sis); // 650x
		assertSingleResult("111", fldName, "1955", sis); // 655y
		assertSingleResult("6553", fldName, "panama", sis); // 655z
	}

	/**
	 * Test population and properties of fulltopic field
	 */
@Test
	public final void testFullTopic() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "fulltopic";
		assertTextFieldProps(fldName);
		assertFieldStored(fldName, solrCore);
		
		//  remember to look for subfields absent from other topic fields
		// fulltopic -  all 600, 610, 630, 650, 655
		// topic = 600abcd:600p:600q:600t:610ab:610p:610t:630a:630p:630s:630t:650a:655ab
		// subtopic = 600v:600x:600y:600z:610v:610x:610y:610z:630v:630x:630y:630z:650v:650x:650y:650z:655v:655x:655y:655z

		// field is stored - retrieve topic values from specific documents
		assertDocHasFieldValue("6808639", fldName, "880-04 Zhongguo gong chan dang Party work.", sis);  // 610-6
		assertDocHasFieldValue("4698973", fldName, "Flyby missions. nasat", sis);  // 650-2
		assertDocHasFieldValue("6553", fldName, "Municipal Fire Station records Fire Reports Atlanta, Georgia 1978 [thesaurus code]", sis);  // 655-3
		// TODO:   note: trailing periods not accepted here.  Not sure why.
		assertSingleResult("345228", fldName, "\"Zemnukhov, Ivan\"", sis); // 600a
		assertSingleResult("1261173", fldName, "\"Magna Carta\"", sis);  // 630a
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"", sis);  // 650a
		assertSingleResult("4698973", fldName, "Multiculturalism", sis);  // 650a
		assertSingleResult("7233951", fldName, "\"Lectures\"", sis);  // 655a

		// field is indexed and tokenized - search for values
		assertSingleResult("4698973", fldName, "nasat", sis); // 650-2
		assertSingleResult("6553", fldName, "fire", sis); // 655-3
		assertSingleResult("3743949", fldName, "Federico", sis); // 600a
		assertSingleResult("3743949", fldName, "1936", sis);  // 600d
		assertSingleResult("919006", fldName, "Emesa", sis);  // 600c
		assertSingleResult("1261173", fldName, "peace", sis);  // 600t
		assertSingleResult("115472", fldName, "Economic", sis);  // 610a
		assertSingleResult("1261173", fldName, "army", sis);  // 610b
		assertSingleResult("1261173", fldName, "magna", sis);  // 630a
		assertSingleResult("6552", fldName, "dictionaries", sis);  // 655a
	}


	/**
	 * Test absence of genre fields
	 */
@Test
	public final void testGenreFields() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		IndexReader ir = sis.getReader();
		assertFieldNotPresent("fullgenre", ir);
		assertFieldNotPresent("genre", ir);
		assertFieldNotPresent("genreStr", ir);
		assertFieldNotPresent("subgenre", ir);
	}

	/**
	 * Test multiple occurences of fullTopic fields when there
	 *  are multiple marc topic fields with the same tag.
	 */
@Test
	public final void testMultFullTopicFields() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "fulltopic";
		assertFieldStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);

		assertDocHasFieldValue("229800", fldName, "Commodity exchanges.", sis);  
		assertDocHasFieldValue("229800", fldName, "Foreign exchange.", sis); 
	
		assertSingleResult("229800", fldName, "Commodity exchanges.", sis); 
		assertSingleResult("229800", fldName, "Foreign exchange.", sis); 
	}	

	/**
	 * Test population of geographicStr field.  No trailing periods or 
	 *  commas.
	 */
@Test
	public final void testGeographicStr()
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "geographicStr";
		assertStringFieldProperties(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
	    // trailing period should be stripped
		Set<String> docIds = new HashSet<String>();
		docIds.add("651a");
		docIds.add("651again");
		assertSearchResults(fldName, "Muppets", docIds, sis);
		assertSingleResult("651numPeriod", fldName, "7.150", sis);
		assertSingleResult("651parens", fldName, "\"Syracuse (N.Y.)\"", sis);
		assertZeroResults(fldName, "\"Syracuse (N.Y.\"", sis);
		assertZeroResults(fldName, "\"Syracuse (N.Y\"", sis);
		assertSingleResult("651siberia", fldName, "\"Siberia (Russia)\"", sis);
		assertZeroResults(fldName, "\"Siberia (Russia).\"", sis);
	}

	/**
	 * Test multiple occurrences of fullGeographic fields when there
	 *  are multiple 651 fields
	 */
@Test
	public final void testMultFullGeoFields() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "fullgeographic";
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);

		assertSingleResult("6280316", fldName, "Tennessee", sis); 
		assertSingleResult("6280316", fldName, "Arkansas", sis); 
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
		assertFieldMultiValued(fldName, solrCore);
		assertStringFieldProperties(fldName, solrCore);
		assertFieldNotStored(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
	
	    // trailing period should be stripped
		Set<String> docIds = new HashSet<String>();
		docIds.add("650y");
		docIds.add("666");
		assertSearchResults(fldName, "\"20th century\"", docIds, sis);
	    
	    // trailing period stripped after 3 digit year
		docIds.clear();
		docIds.add("888");
		docIds.add("999");
		assertSearchResults(fldName, "\"To 449\"", docIds, sis);
	    assertZeroResults(fldName, "\"To 449.\"", sis);
	    
	    // trailing period stripped after 4 digit year
	    assertSingleResult("111", fldName, "\"449-1066\"", sis);
	    
	    // trailing dash - period NOT stripped
	    assertSingleResult("222", fldName, "\"1921-\"", sis);
	    
	    // trailing period NOT stripped
	    assertSingleResult("777", fldName, "\"Roman period, 55 B.C.-449 A.D.\"", sis);
	    
		// no longer assigning "other" when unknown (just leaving value out)
	    assertZeroResults(fldName, "other", sis);
/*
	    docIds.clear();
	    docIds.add("no650or045a");
	    docIds.add("no650but045a");
	    assertSearchResults(fldName, "other", docIds, sis);
*/
	}


	/**
	 * assert field is indexed, tokenized, has norms, does not have term vectors
	 *   and is multivalued.  (says nothing about stored)
	 */
	private final void assertTextFieldProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException
	{
		assertFieldPresent(fldName, solrCore);
		assertFieldIndexed(fldName, solrCore);
		assertFieldTokenized(fldName, solrCore);
		assertFieldHasNorms(fldName, solrCore);
		assertFieldHasNoTermVectors(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
	}

}
