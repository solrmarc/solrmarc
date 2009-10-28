package edu.stanford;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import org.solrmarc.solr.SolrSearcherProxy;
import org.xml.sax.SAXException;

import org.junit.*;

import static org.junit.Assert.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 */
public class IncrementalUpdateTests extends AbstractStanfordBlacklightTest {
	
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("unicornWHoldings.mrc");
	}
	

	/**
	 * Test deleting record when there is no index
	 */
@Test 
	public final void testDeleteButEmptyIndex() 
		throws ParserConfigurationException, SAXException, IOException
	{
		// when first record in index
	}

	/**
	 * Test when deleted record is the first one in the index
	 */
@Test 
	public final void testDeleteFirstRecord() 
		throws ParserConfigurationException, SAXException, IOException
	{
		// when first record in index
	}

	
	/**
	 * Test when deleted record is the last one in the index
	 */
@Test 
	public final void testDeleteLastRecord() 
		throws ParserConfigurationException, SAXException, IOException
	{
		// when last record in index
	}

	/**
	 * Test when deleted records are in the middle of the index, and not in order
	 */
@Test 
	public final void testDeleteRecords() 
		throws ParserConfigurationException, SAXException, IOException
	{
		// when records to delete are in the middle
		// keys are NOT in ascending order
	}

	/**
	 * Test when deleted records include the first and last 
	 */
@Test 
	public final void testDeletedFirstLastRecords() 
		throws ParserConfigurationException, SAXException, IOException
	{
		// when first few, last few, and middle recs in index
	}
	

/**
	 * Test that deletion of non-existing records smoothly reports error and
	 *  causes no other problems
	 */
@Test
	public final void testDeleteNonExistentRecords() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		// file also has key that exists
		// file is also out of numeric order
	}


	/**
	 * Test that new records are added properly
	 */
@Test
	public final void testNewRecords() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	}

	
	/**
	 * Test that records are updated properly
	 *   when deleted then added
	 *   when just updated record present (not deleted)
	 */
@Test
	public final void testUpdatedRecords() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		// fields in new record that weren't in old record
		// fields in old record that weren't in new record
		// fields that exist in both record, but changed
		// multiple occurrences of fields
	}


}
