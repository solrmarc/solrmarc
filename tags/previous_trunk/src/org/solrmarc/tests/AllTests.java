package org.solrmarc.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.solrmarc");
		//$JUnit-BEGIN$
		suite.addTest(BlacklightIndexerTest.suite());

		//$JUnit-END$
		return suite;
	}
	
	public static void main(String args[]){
		TestRunner.run(suite());
	}

}
