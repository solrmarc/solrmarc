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
            solrPath = "home" + File.separator + "genericBlacklight" + File.separator
                    + "jetty" + File.separator + "solr";
            System.setProperty("solr.path", solrPath);
        }

        String solrmarcPath = System.getProperty("solrmarc.path");
        if (solrmarcPath == null) {
            solrmarcPath = new File("lib" + File.separator + "solrmarc").getAbsolutePath();
            System.setProperty("solrmarc.path", solrmarcPath);
        }

        String siteSpecificPath = System.getProperty("solrmarc.site.path");
        if (siteSpecificPath == null) {
            siteSpecificPath = new File("examples" + File.separator
                    + "GenericBlacklight").getAbsolutePath();
            System.setProperty("solrmarc.site.path", siteSpecificPath);
        }

        String configPropDir = System.getProperty("test.config.dir");
        if (configPropDir == null)
            configPropDir = siteSpecificPath;

        String configPropFile = System.getProperty("test.config.file");
        if (configPropFile == null)
        {
            configPropFile = configPropDir + File.separator
                    + "demo_config.properties";
            System.setProperty("test.config.file", configPropFile);
        }

        String testDataPath = System.getProperty("test.data.path");
        if (testDataPath == null)
        {
            String testDataParentPath = System.getProperty("test.data.parent.path");
            if (testDataParentPath == null)
                testDataParentPath = "examples" + File.separator
                        + "GenericBlacklight" + File.separator + "test"
                        + File.separator + "data";

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
        createIxInitVars(testConfigFname, solrPath, null, testDataParentPath,
                testDataFname);
    }

}
