package org.solrmarc.tools;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.solrmarc.testUtils.CommandLineUtils;
import org.solrmarc.testUtils.SolrJettyProcess;


public class RemoteServerTest
{
    static SolrJettyProcess solrJettyProcess = null; 
    static int jettyProcessPort; 
    static String testDataParentPath;
    static String testConfigFile;
    static String solrPath;
    static String solrDataDir;

    /**
     * Start a Jetty driven solr server running in a separate JVM at port jetty.test.port
     */
    @BeforeClass
    public static void setUp() 
    {
        String jettyTestPortStr;

        testDataParentPath = System.getProperty("test.data.path");
        testConfigFile = System.getProperty("test.config.file");
        solrPath = System.getProperty("solr.path");
        solrDataDir = System.getProperty("solr.data.dir");
        jettyTestPortStr = System.getProperty("jetty.test.port");
        // Specify port 0 to select any available port 
        if (jettyTestPortStr == null)
            jettyTestPortStr = "0";
        if (solrPath == null)
            fail("property solr.path must be defined for the tests to run");
        if (solrDataDir == null)
            fail("property solr.data.dir must be defined for the tests to run");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");

        solrJettyProcess = new SolrJettyProcess(solrPath, solrDataDir, testDataParentPath, testConfigFile, jettyTestPortStr);
        boolean serverIsUp = false;
        try
        {
            serverIsUp = solrJettyProcess.startProcessWaitUntilSolrIsReady();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        assertTrueMsg("Server did not become available", serverIsUp);
        // If you need to see the output of the solr server after the server is up and running, call 
        // solrJettyProcess.outputReset() here to empty the buffer so the later output is visible in the Eclipse variable viewer
        //solrJettyProcess.outputReset();
        System.out.println("solr.path= "+ solrPath);
        System.out.println("test.config.file= "+ testConfigFile);
        System.out.println("test.data.path= "+ testDataParentPath);
        System.out.println("Server is up and running at port "+ solrJettyProcess.getJettyPort());
    }
    
    
    @AfterClass
    public static void tearDown() throws Exception
    {
        if (solrJettyProcess != null && solrJettyProcess.isServerIsUp())
        {
            solrJettyProcess.stopServer();
        }
    }
    

    /**
     * unit test for index a number of records via the REMOTE http access methods.
     * then search for those records using the RemoteSolrSearcher class.
     */
    @Test
    public void testRemoteIndexRecord()
    {
        int status;
        // index a small set of records
        URL serverURL =  null;
        try
        {
            serverURL = new URL("http", "localhost", solrJettyProcess.getJettyPort(), "/solr");
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        String urlStr = serverURL.toString();
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream err1 = new ByteArrayOutputStream();
        Map<String,String> addnlProps1 = new LinkedHashMap<String,String>();
        addnlProps1.put("solr.hosturl", urlStr);
        addnlProps1.put("solr.path", "REMOTE");
       // addnlProps1.put("marc.verbose", "true");
        addnlProps1.put("solrmarc.use_binary_request_handler", "true");
        addnlProps1.put("solrmarc.use_solr_server_proxy", "true");
        addnlProps1.put("solrmarc.use_streaming_proxy", "true");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
        if (out1.toByteArray().length > 0) System.out.println("Importer results: "+ new String (out1.toByteArray()));
        if (err1.toByteArray().length > 0) System.out.println("Importer results: "+ new String (err1.toByteArray()));
        
        ByteArrayOutputStream out1a = new ByteArrayOutputStream();
        ByteArrayOutputStream err1a = new ByteArrayOutputStream();
        addnlProps1.put("solrmarc.use_binary_request_handler", "false");
        addnlProps1.put("solrmarc.use_streaming_proxy", "false");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1a, err1a, new String[]{testConfigFile, testDataParentPath+"/u2103.mrc"  }, addnlProps1);
        if (out1a.toByteArray().length > 0) System.out.println("Importer results: "+ new String (out1a.toByteArray()));
        if (err1a.toByteArray().length > 0) System.out.println("Importer results: "+ new String (err1a.toByteArray()));

        // retrieve record u3 from the index
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayOutputStream err2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out2, err2, new String[]{urlStr, "id:u3", "marc_display"});
        
        // retrieve record u3 from the original input file
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.RawRecordReader", "main", null, out3, new String[]{testDataParentPath+"/mergeInput.mrc", "u3" });
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(raw), and record via GetRecord ", out2.toByteArray(), out3.toByteArray());
        
        // retrieve record u3 from the index as XML
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        ByteArrayOutputStream err4 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out4, err4, new String[]{urlStr, "id:u3", "marc_xml_display"});
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(XML), and record via GetRecord ", out3.toByteArray(), out4.toByteArray());
        
        // retrieve record u3 from the index as JSON
        ByteArrayOutputStream out4a = new ByteArrayOutputStream();
        ByteArrayOutputStream err4a = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out4a, err4a, new String[]{urlStr, "id:u3", "marc_json_display"});
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(JSON), and record via GetRecord ", out3.toByteArray(), out4a.toByteArray());
        //System.out.println("Test testRemoteIndexRecord is successful");
        
        //   now test SolrUpdate  to commit the changes
        ByteArrayOutputStream out5 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.SolrUpdate", "main", null, out5, new String[]{"-v", urlStr+"/update"});
        
        // now delete all of the records in the index to make test order not matter
        //    first get the entire contents of index (don't try this at home)
        ByteArrayOutputStream out6 = new ByteArrayOutputStream();
        ByteArrayOutputStream err6 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out6, err6, new String[]{urlStr, "id:u*", "marc_xml_display"});
//        if (out6.toByteArray().length > 0) System.out.println("RemoteSolrSearcher results: "+ new String (out6.toByteArray()));
//        if (err6.toByteArray().length > 0) System.out.println("RemoteSolrSearcher results: "+ new String (err6.toByteArray()));
        
        //    next extract the ids from the returned records 
        ByteArrayInputStream in7 = new ByteArrayInputStream(out6.toByteArray());
        ByteArrayOutputStream out7 = new ByteArrayOutputStream();
        ByteArrayOutputStream err7 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in7, out7, err7, new String[]{testConfigFile, "print", "001"}, addnlProps1);
        String printidsResult = new String(out7.toByteArray());
        if (!printidsResult.matches("001 u1[\\r]?[\\n]001 u3[\\r]?[\\n]001 u4[\\r]?[\\n]001 u7[\\r]?[\\n]001 u8[\\r]?[\\n]001 u10[\\r]?[\\n]001 u2103[\\r]?[\\n]"))
        {
            System.out.println("Index should contain records u1, u3, u4, u7, u8, u10 and u2103, instead it has "+printidsResult.replaceAll("[\\r]?[\\n]", "").replaceFirst("001 ", "").replaceAll("001", ","));
        }
        assertTrueMsg("Index should contain records u1, u3, u4, u7, u8, u10 and u2103, instead it has "+printidsResult.replaceAll("[\\r]?[\\n]", "").replaceFirst("001 ", "").replaceAll("001", ","),
                   (printidsResult.matches("001 u1[\\r]?[\\n]001 u3[\\r]?[\\n]001 u4[\\r]?[\\n]001 u7[\\r]?[\\n]001 u8[\\r]?[\\n]001 u10[\\r]?[\\n]001 u2103[\\r]?[\\n]")));                                                           
//        if (out7.toByteArray().length > 0) System.out.println("IDs to delete: "+ new String (out7.toByteArray()));
//        if (err7.toByteArray().length > 0) System.out.println("IDs to delete: "+ new String (err7.toByteArray()));

        //    now delete all of the records (but don't commit)
//        System.setProperty("marc.delete_record_id_mapper", "001 u?([0-9]*).*->u$1");
        ByteArrayInputStream in8 = new ByteArrayInputStream(out7.toByteArray());
        ByteArrayOutputStream out8 = new ByteArrayOutputStream();
        ByteArrayOutputStream err8 = new ByteArrayOutputStream();
        Map<String,String> addnlProps8 = new LinkedHashMap<String,String>();
        addnlProps8.put("marc.delete_record_id_mapper", "001 u?([0-9]*).*->u$1");
        addnlProps8.put("solr.hosturl", urlStr);
        addnlProps8.put("solr.path", "REMOTE");
        addnlProps8.put("marc.verbose", "true");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", in8, out8, err8, new String[]{testConfigFile, "DELETE_ONLY", "-nocommit"}, addnlProps8);
//        if (out8.toByteArray().length > 0) System.out.println("Importer results: "+ new String (out8.toByteArray()));
//        if (err8.toByteArray().length > 0) System.out.println("Importer results: "+ new String (err8.toByteArray()));

        //   then check that the index is NOT empty yet (because we didn't commit)
        ByteArrayOutputStream out9 = new ByteArrayOutputStream();
        ByteArrayOutputStream err9 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out9, err9, new String[]{urlStr, "id:u*", "marc_display"});

        CommandLineUtils.assertArrayEquals("record dump before and after delete but no commit ", out9.toByteArray(), out6.toByteArray()); 
        
        //   now test SolrUpdate  to commit the changes
        ByteArrayOutputStream out11 = new ByteArrayOutputStream();
        status = CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.SolrUpdate", "main", null, out11, new String[]{"-v", urlStr+"/update"});

//        if (out11.toByteArray().length > 0) System.out.println("Final record is: "+ new String (out11.toByteArray()));
        assertTrueMsg("Remote update was unsuccessful (status = "+status+" )", status==0);
        assertTrueMsg("Remote update was unsuccessful ", new String(out11.toByteArray()).contains("<int name=\"status\">0</int>"));

        //   lastly check that the index is NOW empty
        ByteArrayOutputStream out10 = new ByteArrayOutputStream();
        ByteArrayOutputStream err10 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out10, err10, new String[]{urlStr, "id:u*", "marc_xml_display"});
//        if (out10.toByteArray().length > 0) System.out.println("RemoteSolrSearcher results: "+ new String (out10.toByteArray()));
//        if (err10.toByteArray().length > 0) System.out.println("RemoteSolrSearcher results: "+ new String (err10.toByteArray()));

        System.out.println("Final check record size is: "+ out10.toByteArray().length);
//        if (out10.toByteArray().length > 0) System.out.println("Final record is: "+ new String (out10.toByteArray()));
        CommandLineUtils.assertArrayEquals("record dump via RemoteSolrSearcher, and empty record ", out10.toByteArray(), new byte[0]); 

        System.out.println("Test testRemoteIndexRecord is successful");
    }
    
    @Test
    public void testSolrjBinaryAndNonBinary()
    {
        int status;
        // index a small set of records
        URL serverURL =  null;
        try
        {
            serverURL = new URL("http", "localhost", solrJettyProcess.getJettyPort(), "/solr");
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String urlStr = serverURL.toString();
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream err1 = new ByteArrayOutputStream();
        Map<String,String> addnlProps1 = new LinkedHashMap<String,String>();
        addnlProps1.put("solr.hosturl", urlStr);
        addnlProps1.put("solr.path", "REMOTE");
        addnlProps1.put("solrmarc.use_binary_request_handler", "true");
        addnlProps1.put("solrmarc.use_solr_server_proxy", "true");
        addnlProps1.put("solrmarc.use_streaming_proxy", "false");
        
        // Add several records using remote non-streaming binary solrj 
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
        
        // Check whether record was written as binary 
        String results = getRawFieldByID(urlStr, "u3", "marc_display");
        assertTrueMsg("Record added using remote binary request handler doesn't contain \\u001e", results.contains("\\u001e"));
        assertTrueMsg("Record added using remote binary request handler does contain #30;", !results.contains("#30;"));

        out1.reset();
        err1.reset();
        // Add several records using remote non-streaming non-binary solrj 
        addnlProps1.put("solrmarc.use_binary_request_handler", "false");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
        
        // Check whether record was not written as binary 
        results = getRawFieldByID(urlStr, "u3", "marc_display");
        assertTrueMsg("Record added using remote non-binary request handler does contain \\u001e", !results.contains("\\u001e"));
        assertTrueMsg("Record added using remote non-binary request handler doesn't contain #30;", results.contains("#30;"));

        out1.reset();
        err1.reset();
        // Add several records using remote streaming binary solrj 
        addnlProps1.put("solrmarc.use_binary_request_handler", "true");
        addnlProps1.put("solrmarc.use_streaming_proxy", "true");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
        
        results = getRawFieldByID(urlStr, "u3", "marc_display");
        assertTrueMsg("Record added using remote binary request handler doesn't contain \\u001e", results.contains("\\u001e"));
        assertTrueMsg("Record added using remote binary request handler does contain #30;", !results.contains("#30;"));

        out1.reset();
        err1.reset();
        // Add several records using remote streaming non-binary solrj 
        addnlProps1.put("solrmarc.use_binary_request_handler", "false");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
        
        // Check whether record was not written as binary 
        results = getRawFieldByID(urlStr, "u3", "marc_display");
        assertTrueMsg("Record added using remote non-binary request handler does contain \\u001e", !results.contains("\\u001e"));
        assertTrueMsg("Record added using remote non-binary request handler doesn't contain #30;", results.contains("#30;"));

        out1.reset();
        err1.reset();
        // Add several records using local binary solrj 
        addnlProps1.put("solr.path", new File(solrPath).getAbsolutePath());
        addnlProps1.put("solrmarc.use_binary_request_handler", "true");
        addnlProps1.put("solrmarc.use_streaming_proxy", "false");
        addnlProps1.put("solr.data.dir", solrDataDir);
        status = CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
        assertTrueMsg("MarcImporter run returned status="+status, status==0);

        results = getRawFieldByID(urlStr, "u3", "marc_display");
        assertTrueMsg("Record added using local binary request handler doesn't contain \\u001e", results.contains("\\u001e"));
        assertTrueMsg("Record added using local binary request handler does contain #30;", !results.contains("#30;"));

        out1.reset();
        err1.reset();
        // Add several records using local non-binary solrj 
        addnlProps1.put("solrmarc.use_binary_request_handler", "false");
        status = CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
        
        // Check whether record was not written as binary 
        results = getRawFieldByID(urlStr, "u3", "marc_display");
        assertTrueMsg("Record added using local non-binary request handler does contain \\u001e", !results.contains("\\u001e"));
        assertTrueMsg("Record added using local non-binary request handler doesn't contain #30;", results.contains("#30;"));

        System.out.println("Test testSolrjBinaryAndNonBinary is successful");
    }

    private static void assertTrueMsg(String message, boolean condition) 
    {
        if (!condition) System.out.println(message);
        assertTrue(message, condition);
    }


    /**
     *   getRawFieldByID - Talk to solr jetty server at specificed URL, search for record with id, 
     *   and return the raw value of the field in the field "fieldToFetch" 
     *   If the record with that id doesn't exist id or the record doesn't contain that field return null
     */
    public static String getRawFieldByID(String serverURL, String id, String fieldToFetch)
    {
        String fieldValue = null;
        String select = "select/?q=id%3A%ID%&version=2.2&start=0&rows=1&indent=on&fl=%FIELD%&wt=json";
        URL selectURL;
        try
        {
            selectURL = new URL(serverURL + "/" + select.replace("%ID%", id).replace("%FIELD%", fieldToFetch));
            InputStream is = selectURL.openStream();
            String selectInfo = Utils.readStreamIntoString(is);
            String findAtStart = "\""+fieldToFetch+"\":";
            int fieldStart = selectInfo.indexOf(findAtStart);
            int fieldEnd = selectInfo.indexOf("\"}]");
            if (fieldStart != -1 && fieldEnd != -1)
            {
                fieldValue = selectInfo.substring(fieldStart+findAtStart.length(), fieldEnd);
            }
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(fieldValue);
    }
        
}

