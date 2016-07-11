package org.solrmarc.solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

public class SolrCoreLoader
{
    public static SolrProxy loadRemoteSolrServer(String solrHostUpdateURL, String fullClassName, boolean useBinaryRequestHandler) 
    {
        Object httpsolrserver;
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
        
        // Check that a Solr server is running and available at the provided URL
        boolean statusOK = false;
        BufferedReader pingStream = null;
        try
        {
            pingStream = new BufferedReader(new InputStreamReader(pingURL.openStream()));
            String line;
            while ((line = pingStream.readLine()) != null)
            {
                if (line.matches(".*status.>OK<.*"))
                {
                    statusOK = true;
                    break;
                }
            }
        }
        catch (IOException e1)
        {
            throw new SolrRuntimeException("Error connecting to solr server for ping " + urlString, e1);
        }
        finally {
            if (pingStream != null) try { pingStream.close(); } catch (IOException e){}
        }
        if (!statusOK)
        {
            throw new SolrRuntimeException("Solr reports not OK " + urlString);
        }
        try {
            Class<?> httpsolrserverClass = Class.forName(fullClassName);
            Constructor<?> httpsolrserverConst = httpsolrserverClass.getDeclaredConstructor(String.class);
            httpsolrserver = httpsolrserverConst.newInstance(urlString);
            Class<?> superclass = httpsolrserver.getClass().getSuperclass();
            if (superclass.getName().endsWith(".SolrServer"))
            {
                solrProxy = new SolrServerProxy(httpsolrserver); 
                return(solrProxy);
            }
            if (superclass.getName().endsWith(".SolrClient"))
            {
                solrProxy = new SolrClientProxy(httpsolrserver); 
                return(solrProxy);
            }

        }
        catch (ClassNotFoundException e)
        {
            throw new SolrRuntimeException("Error finding class while dynamically loading solrj", e);
        }
        catch (NoClassDefFoundError e)
        {
            throw new SolrRuntimeException("Error finding class while dynamically loading solrj", e);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            throw new SolrRuntimeException("Error finding solrj constructor with one String parameter", e);
        }
        catch (InstantiationException | IllegalAccessException  | IllegalArgumentException | InvocationTargetException e)
        {
            throw new SolrRuntimeException("Error invoking solrj constructor with one String parameter", e);
        }
        throw new SolrRuntimeException("Error Specified solrj class name, found, but it isnt a SolrServer or a SolrClient");
    }

}
