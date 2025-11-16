package org.solrmarc.solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.solrmarc.driver.Boot;

public class SolrCoreLoader
{
    public static Logger logger = Logger.getLogger(SolrCoreLoader.class);

    public final static String[] defaultSolrJClassnames = {
            "org.apache.solr.client.solrj.impl.HttpSolrClient$Builder",
            "org.apache.solr.client.solrj.impl.HttpSolrClient",
            "org.apache.solr.client.solrj.impl.HttpSolrServer",
            "org.apache.solr.client.solrj.impl.CommonsHttpSolrServer" };

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
            logger.debug("Pinging Solr at URL:  " +pingURL);
            boolean dotsShown = false;
            while ((line = pingStream.readLine()) != null)
            {
                if (line.matches(".*\"status\">OK<.*") || line.matches(".*\"status\":\"OK\".*"))
                {
                    logger.debug("    "+ line);
                    statusOK = true;
                    dotsShown = false;
                    break;
                }
                else if (logger.isTraceEnabled())
                {
                    logger.trace("    "+ line);
                }
                else if (logger.isDebugEnabled() && !dotsShown)
                {
                    logger.debug("    ...");
                    dotsShown = true;
                }
            }
        }
        catch (IOException e1)
        {
            throw new SolrRuntimeException("Error connecting to solr server for ping " + urlString, e1);
        }
        finally
        {
            if (pingStream != null) { try { pingStream.close(); } catch (IOException e){} }
        }
        if (!statusOK)
        {
            throw new SolrRuntimeException("Solr reports not OK " + urlString);
        }
        try
        {
            Class<?> httpsolrserverClass = null;
            if (fullClassName != null && fullClassName.length() > 0)
            {
                httpsolrserverClass = Boot.classForName(fullClassName);
            }
            else
            {
                for (String classname : defaultSolrJClassnames)
                {
                    try
                    {
                        httpsolrserverClass = Boot.classForName(classname);
                        logger.debug("Found Solrj class " + classname);
                        break;
                    }
                    catch (ClassNotFoundException e)
                    {
                        logger.debug("Didn't find class " + classname);
                    }
                }
                if (httpsolrserverClass == null)
                {
                    throw new SolrRuntimeException("Error finding class solrj client while dynamically loading solrj");
                }
            }
            Constructor<?> httpsolrserverConst = httpsolrserverClass.getDeclaredConstructor(String.class);
            httpsolrserver = httpsolrserverConst.newInstance(urlString);
            //  Starting in version 7.x of Solr there no longer is a callable constructor for a HttpSolrClient
            //  you now must create a HttpSolrClient.Builder object and call the build() method on it.
            //  This next if block handles that special case.
            if (httpsolrserverClass.getName().endsWith("Builder"))
            {
                Method buildsolrserver = httpsolrserverClass.getMethod("build");
                httpsolrserver = buildsolrserver.invoke(httpsolrserver);
            }
            Class<?> superclass = httpsolrserver.getClass().getSuperclass();
            if (superclass.getName().endsWith(".SolrServer"))
            {
                solrProxy = new SolrServerProxy(httpsolrserver);
                return (solrProxy);
            }
            if (superclass.getName().endsWith(".SolrClient")
                || superclass.getName().endsWith(".BaseHttpSolrClient")
            ) {
                solrProxy = new SolrClientProxy(httpsolrserver);
                return (solrProxy);
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
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw new SolrRuntimeException("Error invoking solrj constructor with one String parameter", e);
        }
        throw new SolrRuntimeException("Error Specified solrj class name, found, but it isn't a SolrServer or a SolrClient");
    }

}
