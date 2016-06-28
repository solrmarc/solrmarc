package org.solrmarc.solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.RequestWriter;

public class SolrCoreLoader
{
    public static SolrProxy loadRemoteSolrServer(String solrHostUpdateURL, boolean useBinaryRequestHandler) 
    {
        CommonsHttpSolrServer httpsolrserver;
        SolrProxy solrProxy = null;
        // if it has /update on the end, remove it
        String urlString = solrHostUpdateURL.replaceAll("[/\\\\]update$", "");
        URL pingURL; 
        try
        {
            pingURL = new URL(urlString + "/admin/ping");
        }
        catch (MalformedURLException e2)
        {
            throw new SolrRuntimeException("Malformed URL for solr server " + urlString, e2);
        }
        boolean statusOK = false;
        try
        {
            BufferedReader pingStream = new BufferedReader(new InputStreamReader(pingURL.openStream()));
            String line;
            while ((line = pingStream.readLine()) != null)
            {
                if (line.matches(".*status.>OK<.*"))
                    statusOK = true;
            }
        }
        catch (IOException e1)
        {
            throw new SolrRuntimeException("Error connecting to solr server for ping " + urlString, e1);
        }
        if (!statusOK)
        {
            throw new SolrRuntimeException("Solr reports not OK " + urlString);
        }
        try {
            Class<?> clazz = Class.forName("org.apache.solr.client.solrj.impl.ResponseParserFactory");
//            if (useStreamingServer)
//            {
//                httpsolrserver = new StreamingUpdateSolrServer(urlString, 100, 2); 
//            }
//            else
            {
                httpsolrserver = new CommonsHttpSolrServer(urlString);
            }
            if (!useBinaryRequestHandler)
            {
                httpsolrserver.setRequestWriter(new RequestWriter());
                httpsolrserver.setParser( new XMLResponseParser());
            }
            solrProxy = new SolrServerProxy(httpsolrserver); 
            return(solrProxy);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return(null);
    }

}
