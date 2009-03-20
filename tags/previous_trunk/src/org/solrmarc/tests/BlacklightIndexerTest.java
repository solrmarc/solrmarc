/**
 * 
 */
package org.solrmarc.tests;

import java.io.InputStream;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.BlacklightIndexer;

import junit.framework.*;
import junit.textui.*;

/**
 * @author Wayne Graham
 *
 */
public class BlacklightIndexerTest extends TestCase {
	
	Record record = null;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		InputStream input = getClass().getResourceAsStream("resources/med_bibs-20sample.mrc");
		MarcReader reader = new MarcStreamReader(input);
		
		while(reader.hasNext()){
			record = reader.next();
		}
		
		input.close();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		record = null;
	}

	/**
	 * Test method for {@link org.solrmarc.index.BlacklightIndexer#BlacklightIndexer(java.lang.String)}.
	 */
	public final void testBlacklightIndexer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.solrmarc.index.BlacklightIndexer#getRecordingAndScore(org.marc4j.marc.Record)}.
	 */
	public final void testGetRecordingAndScore() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.solrmarc.index.BlacklightIndexer#getRecordingFormat(org.marc4j.marc.Record)}.
	 */
	public final void testGetRecordingFormat() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.solrmarc.index.BlacklightIndexer#getCallNumberPrefix(org.marc4j.marc.Record)}.
	 */
	public final void testGetCallNumberPrefix() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.solrmarc.index.BlacklightIndexer#getCallNumberCleaned(org.marc4j.marc.Record)}.
	 */
	public final void testGetCallNumberCleaned() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.solrmarc.index.BlacklightIndexer#getOclcNum(org.marc4j.marc.Record)}.
	 */
	public final void testGetOclcNum() {
		fail("Not yet implemented"); // TODO
	}
	
	public static Test suite(){
		return new TestSuite(BlacklightIndexerTest.class);
	}
	
	public static void main(String args[]){
		TestRunner.run(suite());
	}

}
