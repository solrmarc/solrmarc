package playground.solrmarc.solr;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.RequestWriter;

public class SolrCoreLoader
{
    public static SolrProxy loadRemoteSolrServer(String solrHostUpdateURL, boolean useBinaryRequestHandler, boolean useStreamingServer)
    {
        CommonsHttpSolrServer httpsolrserver;
        SolrProxy solrProxy = null;
        String urlString = solrHostUpdateURL.replaceAll("[/\\\\]update$", "");
        try {
            Class<?> clazz = Class.forName("org.apache.solr.client.solrj.impl.ResponseParserFactory");
            if (useStreamingServer)
            {
                httpsolrserver = new StreamingUpdateSolrServer(urlString, 100, 2); 
            }
            else
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
