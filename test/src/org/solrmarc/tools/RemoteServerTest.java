package org.solrmarc.tools;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
        if (jettyTestPort == null)
            jettyTestPort = "8983";
        if (solrPath == null)
            fail("property solr.path must be defined for the tests to run");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        Map<String, String> javaProps = new LinkedHashMap<String, String>();
        javaProps.put("solr.solr.home", new File(solrPath).getCanonicalPath());
        javaProps.put("jetty.port", jettyTestPort);
        List<String> addnlClassPath = new ArrayList<String>();
        addnlClassPath.add(new File(testDataParentPath, "../jetty/start.jar").getCanonicalPath());
        System.out.println("Properties read, starting server");

        vmspawner = new JavaInvoke("org.mortbay.start.Main",
                                   new File(new File(testDataParentPath, "../jetty").getCanonicalPath()), 
                                   javaProps, 
                                   null,
                                   addnlClassPath,
                                   null, false);
        p = vmspawner.startStdinStderrInstance("JETTY");
//        p = vmspawner.start();
        boolean serverIsUp = checkServerIsUp(10000, 100, getServerAddress(), getServerPort());
        assertTrue("Server did not become available",serverIsUp);
        System.out.println("Server is up and running");
    }

    /**
     * unit test for RemoteIndexRecord
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
        System.setProperty("solr.hosturl", urlStr);
        System.setProperty("solr.path", "REMOTE");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.marc.MarcImporter", "main", null, out1, outErr1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  });

        // retrieve record u3 from the index
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out2, outErr2, new String[]{urlStr, "id:u3", "marc_display"});
        
        // retrieve record u3 from the original input file
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.RawRecordReader", "main", null, out3, new String[]{testDataParentPath+"/mergeInput.mrc", "u3" });
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(raw), and record via GetRecord ", out2.toByteArray(), out3.toByteArray());
        
        // retrieve record u3 from the index as XML
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr4 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out4, outErr4, new String[]{urlStr, "id:u3", "marc_xml_display"});
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(raw), and record via GetRecord ", out4.toByteArray(), out3.toByteArray());
        System.out.println("Test testRemoteIndexRecord is successful");
        
        
        // Now test SolrUpdate  
        ByteArrayOutputStream out5 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.SolrUpdate", "main", null, out5, new String[]{urlStr+"/update"});

        assertTrue("Remote update was unsuccessful ", out5.toByteArray().toString().contains("<int name=\"status\">0</int>"));
        System.out.println("Test4 testRemoteIndexRecord is successful");

        
        
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

