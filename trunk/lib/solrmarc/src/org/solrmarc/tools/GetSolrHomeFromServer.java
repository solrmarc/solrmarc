package org.solrmarc.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class GetSolrHomeFromServer {
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
        	System.err.println("Error: You must provide the URL of the Solr server to which to connect");
        	System.exit(1);
        }
        if (solrServerURL.endsWith("/update"))
        {
        	solrServerURL = solrServerURL.substring(0, solrServerURL.length() - 7);
        }
        
        solrServerURL = solrServerURL + "/admin";
        
        int exitCode = 1;
        try
        {
        	String solrHome = getSolrHome(solrServerURL);
        	System.out.println(solrHome);
        	exitCode = 0;
        }
        catch (MalformedURLException me)
        {
        	System.err.println("Error: You must provide a valid URL of the Solr server to which to connect");
        }
        catch (IOException ioe)
        {
            System.err.println("Error: no Solr server currently running at URL: " + solrServerURL);
        }
    	System.exit(exitCode);
    }
    
    public static String getSolrHome(String solrHostURL) throws IOException
    {
        if (solrHostURL == null || solrHostURL.length() == 0) 
        	throw( new MalformedURLException());
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
        urlConn.setDoOutput (false);

        // No caching, we want the real thing.
        urlConn.setUseCaches (false);

        // Specify the content type.
        urlConn.setRequestProperty("Content-Type", "text/xml");
        urlConn.setRequestProperty("charset", "utf-8");

         // Get response data.
        input = new BufferedReader(new InputStreamReader(urlConn.getInputStream ()));

        String str;
        String solrHomeLine = null;
        while (null != ((str = input.readLine())))
        {
            if (str.indexOf("SolrHome") != -1)  solrHomeLine = str;
        }

        input.close ();
        if (solrHomeLine == null)  
        	throw new IOException("Error: Can't read string containing SolrHome from URL: "+ solrHostURL);

        String solrHome = solrHomeLine.replaceFirst(".*SolrHome=", "");
        String solrHomePre = solrHomeLine.replaceFirst("cwd=", "").replaceFirst(" *SolrHome=.*", "");
        
        File solrHomePath = new File(solrHome);
        if (! solrHomePath.isAbsolute())
        {
        	solrHomePath = new File(solrHomePre, solrHome);
        }
        solrHome = solrHomePath.getAbsolutePath();
        
        solrHome = solrHome.replaceFirst("/$", "");
        solrHome = solrHome.replaceAll("\\\\", "/");
        return(solrHome);
     }


}
