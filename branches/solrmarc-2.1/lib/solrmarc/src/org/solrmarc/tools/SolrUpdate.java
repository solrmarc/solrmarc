package org.solrmarc.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.solrmarc.marc.MarcImporter;
import org.solrmarc.tools.GetDefaultConfig;


public class SolrUpdate
{
	// Initialize logging category
	protected static Logger logger = Logger.getLogger(MarcImporter.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String solrServerURL = null;
        Properties configProps;
        
        if(args.length > 0)
        {
            for (String arg : args)
            {
                if (arg.startsWith("http"))
                {
                    solrServerURL = arg;
                }
            }
        }
        if (solrServerURL == null)
        {
            String configProperties = GetDefaultConfig.getConfigName(null);
            if (configProperties != null)
            {
                configProps = Utils.loadProperties(null, configProperties);
                // Ths URL of the currently running Solr server
                solrServerURL = Utils.getProperty(configProps, "solr.hosturl");
            }
        }
        
        try
        {
            signalServer(solrServerURL);
        }
        catch (MalformedURLException me)
        {
            System.err.println("Specified URL is malformed: " + solrServerURL);
        }
        catch (IOException ioe)
        {
            System.err.println("Unable to establish connection to solr server at URL: " + solrServerURL);
        }
   }
    
    /**
     * If there is a running Solr server instance looking at the same index
     * that is being updated by this process, this function can be used to signal 
     * that server that the indexes have changed, so that it will find the new data
     * with out having to be restarted.
     * 
     * uses member variable SolrHostURL which contains the URL of the Solr server
     * for example:    http://localhost:8983/solr/update
     * This value is taken from the  solr.hosturl  entry in the properties file. 
     * @throws IOException 
     */
    
    public static void signalServer(String solrHostURL) throws IOException
    {
        if (solrHostURL == null || solrHostURL.length() == 0) return;
        URL         url;
        URLConnection   urlConn;
        DataOutputStream    printout;
        BufferedReader input;

        // URL of CGI-Bin script.
        url = new URL (solrHostURL);

        // URL connection channel.
        urlConn = url.openConnection();

        // Let the run-time system (RTS) know that we want input.
        urlConn.setDoInput (true);

        // Let the RTS know that we want to do output.
        urlConn.setDoOutput (true);

        // No caching, we want the real thing.
        urlConn.setUseCaches (false);

        // Specify the content type.
        urlConn.setRequestProperty("Content-Type", "text/xml");
        urlConn.setRequestProperty("charset", "utf-8");

        // Send POST output.
        printout = new DataOutputStream (urlConn.getOutputStream ());

        String content = "<commit/>";
         
        printout.writeBytes (content);
        printout.flush ();
        printout.close ();

        // Get response data.
        input = new BufferedReader(new InputStreamReader(urlConn.getInputStream ()));

        String str;
        while (null != ((str = input.readLine())))
        {
        	logger.info(str);
        }

        input.close ();

        // Display response.
     }


}
