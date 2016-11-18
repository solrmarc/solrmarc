This page will show you some ways to use existing tests and how to write your own tests for your site specific version of Blacklight.

We recommend a Test-First Development approach:

  1. write a test for what you would like to have happen. (e.g. what value you would like a Solr field to get when you index particular MARC data.
  1. run the test - it should fail.  If you will be using any customizations, (e.g. new mappings in your xxx\_index.properties file, or your own custom methods), then the test should fail.
    * If the test doesn't fail, you should make sure it **will** fail if the data/result pairing is incorrect -- you don't want any false positives in your tests!
  1. manipulate configurations and customized methods as needed until the test passes.
  1. passing test - Yay!

For more on test first development ...


# Introduction #

SolrMarc comes with three different testing infrastructures.  Two of them take different approaches to test whether the mappings from a MARC record to the intended contents of a Solr field are correct.  The third builds a Solr index from the test data, which makes it possible to search the index.

## Method I:  Mapping Tests in .txt File (Run from ant Task) ##

This approach may be more convenient when your site's customizations are all done in the xxx\_index.properties configuration file.  This approach may also be more convenient if you intend to have a single MARC record in individual test data files.  (Note: SolrMarc provides scripts to extract individual MARC records from files containing multiple MARC records, as well as from Solr directly.  See the "Batch File / Shell Scripts" section at the bottom of the GettingStarted wiki page.)  This approach will be less useful when there are many values to be assigned to a particular solr field (e.g. a record with a lot of 650 subject fields), or when you want to ensure the absence of a Solr field value, or a Solr field altogether.  Or when you want to test searching (see Method III).

  * When you run these tests with the provided ant task, a full build of your code is always performed before the tests are run.
  * These test do NOT indicate _which_ Solr document will be checked, so it is recommended that you use test files with a single MARC record for each test.
  * These tests require ALL the values for a field to be specified, in order.
  * These tests ONLY check for particular value(s) in a field.  They do not check to ensure a value is NOT present, or that the field is not present, etc.

The tests and expected results are in a file called indextest.txt that lives in your site's test/data folder.  The tests are run via an ant target "test" which looks for the indextest.txt file in your site's test/data directory.  Test data is run through the SolrMarc indexing code up to the point where the Solr document is about to be created.

Steps:

  1. Configure the SolrMarc program for your site.  (See GettingStarted)
  1. Make sure you have known MaRC records in your created test/data folder.
    * you can use any of the test data provided in the SolrMarc project, or provide your own.
  1. Create an indextest.txt file in your test folder.  Each test is a line in the file; for an example, see UvaBlacklight/test/data/indextest.txt.

  * each line looks for a value in a field in a Solr document that would be created by indexing the specified MARC data.
  * format of each line:
    1. name of your site's xxx\_config.properties file
    1. name of the file containing MARC test data (relative to your site's SolrMarc folder).  It is recommended that you use test files with a single MARC record for each test
    1. name of the Solr field to test
    1. value expected for the Solr field
      * example: `demo_config.properties, 00282214.mrc, title_t, Shodede-yam Yehudiyim`
      * for an array, list the values separated by a vertical bar.  Order matters.
        * example: `demo_config.properties, 2009373513.mrc, title_t, Ci an zhou bian /|次按驟變`

> Run the tests with the ant "test" target. Two ways you can do this:
    * From the command line, from the top level directory of the SolrMarc project, type
> > > `ant test`
        * Note: the site.dir.properties file at the top level directory is how this ant task knows where to find YOUR test file.  It must be in your site's test/data directory.
    * From your IDE, run the ant test target in the top level build.xml ant file.

The JUnit code that is used for these tests is org.solrmarc.test.ParameterizedIndexTest, written by Bob Haschart.

## Methods II and III: Tests in Java Code ##

These tests are "closer" to the java indexing code, so if you are writing customized java methods for indexing, this approach may be more convenient.

These approaches may also be more convenient if you intend to have multiple MARC records in your test files.  Additionally, these approaches allow checking for the presence or absence of a single value for a Solr field, so it may be more useful when there are many values to be assigned to a particular Solr field (e.g. a record with a lot of 650 subject fields).  Lastly, you do not need a solr index running to test this way.

It allows tests to be run from Eclipse or other IDEs.  Because it is just java code, it can also be run from an ant task.
  * The tests and expected results are in the java code.
  * When you run these tests, a full build of your code will not happen (unless you configure your IDE for a full build before you run JUnit tests).
  * These tests indicate which Solr document is to be tested, so you can use a test data file with multiple MaRC records.
  * These tests allow any individual value for a field to be specified, without having to specify all the other values.
  * These tests can check for the absence of a value from a Solr field.
  * A Solr index does not need to be running to execute these tests.

## Method II:  Mapping Tests in Java Code ##

This approach tests what values _would_ be written to a Solr document, if one were actually being created.  As with Method I, test data is run through the SolrMarc indexing code up to the point where the Solr document is about to be created.

The tests and expected results will be in java code in your site's test/src directory. You can run these tests from within your IDE;  you could also easily adapt the ant "test" target to run these tests.

Steps:

  1. Configure the SolrMarc program for your site. (See GettingStarted)
  1. Make sure you have known MARC records in your created test/data folder.
    * you can use any of the test data provided in the SolrMarc project, or provide your own.
  1. Create an abstract class that will accommodate configuration settings for running the tests for your site.
    * an example is in the examples/GenericBlacklight/test/src directory, called AbstractMappingTests.java.  The code looks like this:

```
package org.blacklight;

import java.io.File;

import org.junit.*;

import org.solrmarc.index.SolrFieldMappingTest;

/**
 * Abstract class for junit4 tests for generic Blacklight mapping tests
 * @author Naomi Dushay
 */
public class AbstractMappingTests {
	
	/** SolrFieldMappingTest object to be used in specific tests */
	protected SolrFieldMappingTest solrFldMapTest = null;
	
        // your site's directory, per GettingStarted configurations
	private String siteDir = "bldemo";  

        // parent directory for MARC record files
	protected String marcFileDir = siteDir + File.separator + "test" + File.separator + "data" + File.separator; 

        // a default test file.
	protected String marc30recTestFile = marcFileDir + "test_data.utf8.mrc";


@Before
	public void setup() 
	{
	    // these properties must be set or MarcHandler can't initialize properly
            System.setProperty("solrmarc.path", "lib" + File.separator + "solrmarc");
	    System.setProperty("solrmarc.site.path", siteDir); 
    	    System.setProperty("marc.source", "FILE");

    	    // needed to get through initialization; overridden in individual tests
    	    System.setProperty("marc.path", marc30recTestFile);

    	    solrFldMapTest = new SolrFieldMappingTest(siteDir + File.separator + "demo_config.properties", "id");
	}

}
```

> You will need to change the values for `siteDir`, for `marcFileDir`, and for `marc30recTestFile`, and for the `marc.path` property value and the location of your sites `config.properties` file assigned in the setup() method.

Next:
  1. Create instances of the subclass that run actual tests.
    * examples are in the examples/GenericBlacklight/test/src directory, called (blah)MappingTests.java  Code looks like this:

```
package org.blacklight;

import org.junit.*;

/**
 * junit4 tests for generic Blacklight example - call number fields
 * @author Naomi Dushay
 */
public class CallnumMappingTests extends AbstractMappingTests
{
	
	/**
	 * call number display field: lc_callnum_display
	 * lc_callnum_display = 050ab, first
	 */
@Test
	public final void lcDisplayTest()
	{
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "00282214", "lc_callnum_display", "PK2788.9.A9 F55 1998");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "86207417", "lc_callnum_display", "G535 .F54 1984");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92117465", "lc_callnum_display", "KPC13 .K67 1990");
		solrFldMapTest.assertSolrFldValue(marc30recTestFile, "92828023", "lc_callnum_display", "BM520.88.A53 I88 1992b");
	}
}
```

Then:
> Run your instance subclass as a junit test!

### Where's the Beef for Method II? ###

The workhorse class for these tests is org.solrmarc.index.SolrFieldMappingTest, which is in the test/src directory of the SolrMarc project.  It currently contains two methods allowing assertions about field mappings:

  * **assertSolrFldValue(String mrcFileName, String solrDocId, String expectedFldName, String expectedFldVal)**
> assert that when the file of marc records is processed, there will be a Solr document with the given id containing at least one instance of the expected Solr field with the expected value
    * mrcFileName - absolute path of file of marc records (name must end in .mrc or .marc or .xml)
    * solrDocId - value of Solr unique key field for the Solr document to check
    * expectedFldName - the name of the Solr field to be checked
    * expectedFldVal - the value expected to be in at least one instance of the Solr field for the indicated Solr document

  * **assertSolrFldHasNoValue(String mrcFileName, String solrDocId, String expectedFldName, String expectedFldVal)**
> assert that when the file of marc records is processed, the Solr document with the given id does NOT contain an instance of the indicated field with the indicated value
    * mrcFileName - absolute path of file of marc records (name must end in .mrc or .marc or .xml)
    * solrDocId - value of Solr unique key field for the Solr document to check
    * expectedFldName - the name of the Solr field to be checked
    * expectedFldVal - the value that should be in NO instance of the Solr field for the indicated Solr document

You can group all of your mapping tests into a single java class so they can all be run in one shot.  Then you can configure the ant script to run this test.  An example is in the examples/GenericBlacklight/test/src directory, called AllMappingTests.java.


## Method III: Indexing Tests ##

This approach actually creates an index, which allows for additional types of testing:
  * Ensure that all Solr configuration files are consistent (e.g. there isn't a field in solrconfig.xml that is not defined, either explicitly or dynamically, in schema.xml).
  * Smoke test to ensure you can write data to a Solr index with your site's code.
  * Searching tests for Solr indexed fields.
  * More refined assertions about contents and properties of Solr fields.

Running tests with this approach can take longer, since you are building an index, possibly for each test.  This is especially true for large sets of test records.  Note that Solr does NOT need to be running for this type of testing -- the tests use the Solr java API to access the index.

The tests and expected results will be in java code in your site's test/src directory. You can run these tests from within your IDE;  you could also easily adapt the ant "test" target to run these tests.

Steps:

  1. Configure the SolrMarc program for your site. (See GettingStarted)
  1. Make sure you have known MaRC records in your created test/data folder.
    * you can use any of the test data provided in the SolrMarc project, or provide your own.
  1. Create an abstract class that will accommodate configuration settings for running the tests for your site.
    * an example is in the examples/GenericBlacklight/test/src directory, called DemoIxTest.java.  The code looks like this:

```
package org.blacklight;

import static org.junit.Assert.fail;
import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.solrmarc.index.IndexTest;

/**
 * Site Specific code used for testing the Generic Blacklight example
 * 
 * @author Naomi Dushay
 */
public abstract class DemoIxTest extends IndexTest
{

    // Note: the hardcodings below are only used when the tests are
    // invoked without the properties set
    // the properties ARE set when the tests are invoke via ant.
    {
        // !!! NOTE:  this one is local and must be modified for your installation !!!
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
        {
            // solrPath = siteSpecificPath + File.separator + "solr";
            solrPath = (your desired solr path)
            System.setProperty("solr.path", solrPath);
        }

        String solrmarcPath = System.getProperty("solrmarc.path");
        if (solrmarcPath == null) {
            solrmarcPath = new File("lib" + File.separator + "solrmarc").getAbsolutePath();
            System.setProperty("solrmarc.path", solrmarcPath);
        }

        String siteSpecificPath = System.getProperty("solrmarc.site.path");
        if (siteSpecificPath == null) {
            siteSpecificPath = new File("examples" + File.separator + "GenericBlacklight").getAbsolutePath();
            System.setProperty("solrmarc.site.path", siteSpecificPath);
        }

        String configPropDir = System.getProperty("test.config.dir");
        if (configPropDir == null)
            configPropDir = siteSpecificPath;

        String configPropFile = System.getProperty("test.config.file");
        if (configPropFile == null)
        {
            configPropFile = configPropDir + File.separator + "demo_config.properties";
            System.setProperty("test.config.file", configPropFile);
        }

        String testDataPath = System.getProperty("test.data.path");
        if (testDataPath == null)
        {
            String testDataParentPath = System.getProperty("test.data.parent.path");
            if (testDataParentPath == null)
                testDataParentPath = "examples" + File.separator + "GenericBlacklight" + File.separator + "test" + File.separator + "data";

            // testDir = "test";
            // testDataParentPath = testDir + File.separator + "data";
            System.setProperty("test.data.path", testDataParentPath);
        }

        String solrDataDir = System.getProperty("solr.data.dir");
        if (solrDataDir == null)
            solrDataDir = solrPath + File.separator + "data";

    }

    public void createIxInitVars(String testDataFname)
            throws ParserConfigurationException, IOException, SAXException
    {
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
        {
            fail("property solr.path must be defined for the tests to run");
        }

        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
        {
            fail("property test.data.path must be defined for the tests to run");
        }
        String testConfigFname = System.getProperty("test.config.file");
        if (testConfigFname == null)
        {
            fail("property test.config.file must be defined for the tests to run");
        }
        createIxInitVars(testConfigFname, solrPath, null, testDataParentPath, testDataFname);
    }
}
```

You can see from this code that you need to have the following System properties set to run these tests:
  * solr.path
  * test.data.path
  * test.config.file
  * solrmarc.site.path
  * solrmarc.path - the code above may work as is
  * solr.data.dir - the code above may work as is, or you may want to allow Solr to use its defaults for the Solr data directory.

Next:
  1. Create instances of the subclass that run actual tests.
    * examples are in the examples/GenericBlacklight/test/src directory, called (blah)MappingTests.java  Code looks like this:

```
package org.blacklight;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.junit.*;

/**
 * junit4 tests for generic blacklight example
 * 
 * @author Naomi Dushay
 */
public class SmokeTests extends org.blacklight.DemoIxTest
{
    @Before
    public final void setup() throws ParserConfigurationException, IOException, SAXException
    {
        createIxInitVars("test_data.utf8.mrc");
    }

    /**
     * Test id field
     */
    @Test
    public final void testId() throws ParserConfigurationException, SAXException, IOException
    {
        String fldName = "id";
        assertStringFieldProperties(fldName);
        assertFieldNotMultiValued(fldName);
        assertFieldStored(fldName);
        assertFieldIndexed(fldName);

        assertDocPresent("00282214");

        assertSingleResult("00282214", fldName, "00282214");
    }
}
```

Then:
> Run your instance subclass as a junit test!  The !createIxInitVars() method that is called in the setup() method  creates a Solr index from the test file, using the SolrMarc code and Solr instance that you indicated in your configuration properties.

### Where's the Beef for Method III? ###

The workhorse class for these tests is org.solrmarc.index.IndexTest, which is in the test/src directory of the SolrMarc project.  It currently contains many assertion methods about field mappings and field properties, for example:
  * **assertDocHasFieldValue(String doc\_id, String fldName, String fldVal)**
> > assert the specified document has the indicated field with the specified value
  * **assertDocHasNoFieldValue(String doc\_id, String fldName, String fldVal)**
> > assert the specified document does NOT have the indicated field with the specified value
  * **assertFieldValues(String fldName, String fldVal, Set`<String>` docIds)**
> > assert a search for the Solr field/value pair will return exactly the set of documents indicated.
  * **assertDocPresent(String doc\_id)**
> > asserts that the document is present in the index
  * **assertDocNotPresent(String doc\_id)**
> > asserts that the document is _not_ present in the index
  * **assertSingleResult(String docId, String fldName, String fldVal)**
> > assert the identified document is the sole indexed document with the Solr field/value indicated
  * **assertZeroResults(String fldName, String fldVal)**
> > assert there is no document in the index with the Solr field/value pair indicated
  * **assertResultSize(String fld, String text, int numExp)**
> > assert a search for the field/value pair gets the expected number of results

There are also assertion methods regarding properties of fields, such as stored, indexed, tokenized, multiValued, hasNorms, etc.

While the GenericBlacklight example code only has the single test shown above, the StanfordBlacklight example code has a lot more tests using this method.

One note:  the searching is actually done using the Lucene API, so where the Solr searching differs from the Lucene searching of an index, the results are not guaranteed.