package org.solrmarc.tools;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.solrmarc.testUtils.CommandLineUtils;
import org.solrmarc.testUtils.JavaInvoke;


public class RemoteServerTest
{
    JavaInvoke vmspawner;
    String testDataParentPath;
    String testConfigFile;
    String solrPath;
    Process p = null;
    String jettyTestPort;

    /**
     * Start a Jetty driven solr server running in a separate JVM at port jetty.test.port
     */
    @Before
    public void setUp() throws Exception
    {
        testDataParentPath = System.getProperty("test.data.path");
        testConfigFile = System.getProperty("test.config.file");
        solrPath = System.getProperty("solr.path");
        jettyTestPort = System.getProperty("jetty.test.port");
        // Specify port 0 to select any available port 
        if (jettyTestPort == null)
            jettyTestPort = "0";
        if (solrPath == null)
            fail("property solr.path must be defined for the tests to run");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
        {
            java.util.logging.Logger.getLogger("org.apache.solr").setLevel(java.util.logging.Level.SEVERE);
            Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
        }
        Map<String, String> javaProps = new LinkedHashMap<String, String>();
        javaProps.put("solr.solr.home", new File(solrPath).getCanonicalPath());
        javaProps.put("jetty.port", jettyTestPort);
        List<String> addnlClassPath = new ArrayList<String>();
        addnlClassPath.add(new File(testDataParentPath, "../jetty/start.jar").getCanonicalPath());
        System.out.println("Properties read, starting server");

        ByteArrayOutputStream serverOut = new ByteArrayOutputStream();
        ByteArrayOutputStream serverErr = new ByteArrayOutputStream();
        
        vmspawner = new JavaInvoke("org.mortbay.start.Main",
                                   new File(new File(testDataParentPath, "../jetty").getCanonicalPath()), 
                                   javaProps, 
                                   null,
                                   addnlClassPath,
                                   null, false);
        p = vmspawner.startStdinStderrInstance("JETTY", serverOut, serverErr);
//        p = vmspawner.start();
        boolean serverIsUp = false;
        if (getServerPort() == 0)
        {
            jettyTestPort = waitServerIsUp(25000, 100, serverErr, "INFO:  Started SocketConnector@0.0.0.0:", "INFO:  Started SocketConnector @ 0.0.0.0:");
            serverIsUp = checkServerIsUp(5000, 100, getServerAddress(), getServerPort());
        }
        else
        {
            serverIsUp = checkServerIsUp(25000, 100, getServerAddress(), getServerPort());
        }
        assertTrue("Server did not become available",serverIsUp);
        System.out.println("Server is up and running at port "+ getServerPort());
    }

    /**
     * unit test for index a number of records via the REMOTE http access methods.
     * then search for those records using the RemoteSolrSearcher class.
     */
    @Test
    public void testRemoteIndexRecord()
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");

        // index a small set of records
        URL serverURL =  null;
        try
        {
            serverURL = new URL("http", "localhost", getServerPort(), "/solr");
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
        addnlProps1.put("marc.verbose", "true");
//        addnlProps1.put("solrmarc.use_binary_request_handler", "true");
//        addnlProps1.put("solrmarc.use_solr_server_proxy", "true");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
//        if (out1.toByteArray().length > 0) System.out.println("Importer results: "+ new String (out1.toByteArray()));
//        if (err1.toByteArray().length > 0) System.out.println("Importer results: "+ new String (err1.toByteArray()));

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
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(raw), and record via GetRecord ", out4.toByteArray(), out3.toByteArray());
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
        CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.SolrUpdate", "main", null, out11, new String[]{"-v", urlStr+"/update"});

//        if (out11.toByteArray().length > 0) System.out.println("Final record is: "+ new String (out11.toByteArray()));
        assertTrue("Remote update was unsuccessful ", new String(out11.toByteArray()).contains("<int name=\"status\">0</int>"));

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
    
    @After
    public void tearDown() throws Exception
    {
        if (p != null)
        {
            p.destroy();
            p.waitFor();
        }
    }
    
    InetAddress getServerAddress() throws UnknownHostException 
    {
        return InetAddress.getLocalHost();
    }
    
    int getServerPort() 
    {
        return Integer.parseInt(jettyTestPort);
    }
    
    /**
     * Repeats a TCP connection check every <em>sleepTime</em> milliseconds until it either succeeds
     * or times out after <em>timeout</em> milliseconds.
     * 
     * @see Server#checkServerIsUp(InetAddress, int) An explanation of the TCP checking mechanism.
     * 
     * @param timeout If no check is successful after this many milliseconds has passed, fail the 
     * overall checking process.
     * @param sleepTime How long to wait (in milliseconds) between checks of the service.
     * @param server address of server to check.
     * @param port port to check.
     * @return true if a connection attempt succeeds, false in the case of error or 
     * no connection attempt successful.
     */
    public static String waitServerIsUp(long timeout, long sleepTime, ByteArrayOutputStream out, String patternToWatchFor1, String patternToWatchFor2  ) 
    {
        long start = System.currentTimeMillis();
        String socketStr = "0";
        int lastLineRead = 0;
        while((System.currentTimeMillis() - start) < timeout)
        {
            String outputSoFar = new String(out.toByteArray());
            String lines[] = outputSoFar.split("\r?\n");
            for (int i = lastLineRead; i < lines.length; i++)
            {
//                System.out.println(lines[i]);
                if (lines[i].contains(patternToWatchFor1))
                {
                    socketStr = lines[i].replaceAll(".*"+patternToWatchFor1 + "([0-9]*).*", "$1");
                    return(socketStr);
                }
                else if (lines[i].contains(patternToWatchFor2))
                {
                    socketStr = lines[i].replaceAll(".*"+patternToWatchFor2 + "([0-9]*).*", "$1");
                    return(socketStr);
                }
            }
            lastLineRead = lines.length;
            try {
                Thread.sleep(sleepTime);
            } 
            catch (InterruptedException e) 
            {
                return socketStr;
            }
        }
        return socketStr;
    }
    
    /**
     * Repeats a TCP connection check every <em>sleepTime</em> milliseconds until it either succeeds
     * or times out after <em>timeout</em> milliseconds.
     * 
     * @see Server#checkServerIsUp(InetAddress, int) An explanation of the TCP checking mechanism.
     * 
     * @param timeout If no check is successful after this many milliseconds has passed, fail the 
     * overall checking process.
     * @param sleepTime How long to wait (in milliseconds) between checks of the service.
     * @param server address of server to check.
     * @param port port to check.
     * @return true if a connection attempt succeeds, false in the case of error or 
     * no connection attempt successful.
     */
    public static boolean checkServerIsUp(long timeout, long sleepTime, InetAddress server, int port ) 
    {
        long start = System.currentTimeMillis();
        while((System.currentTimeMillis() - start) < timeout)
        {
            if(!checkServerIsUp(server, port))
            {
                try {
                    Thread.sleep(sleepTime);
                } 
                catch (InterruptedException e) 
                {
                    return false;
                }
            }
            else
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Performs a simple TCP connection check to the specified address and port.
     * 
     * @param server address of the server to contact.
     * @param port TCP port to connect to on the specified server.
     * @return true if that port is accepting connections, 
     * false in all other cases: not listening and/or connection error.
     */
    public static boolean checkServerIsUp(InetAddress server, int port) 
    {
        Socket sock = null;
        try {
            sock = SocketFactory.getDefault().createSocket(server, port);
            sock.setSoLinger(true, 0);
            return true;
        } 
        catch (IOException e) 
        { 
            return false;
        }
        finally
        {
            if(sock != null)
            {
                try {
                    sock.close();
                } 
                catch (IOException e) 
                {
                    // don't care
                }
            }
        }
    }
}

