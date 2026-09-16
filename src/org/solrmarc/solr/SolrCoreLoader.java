package org.solrmarc.solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.solrmarc.driver.Boot;

public class SolrCoreLoader
{
    public static Logger logger = Logger.getLogger(SolrCoreLoader.class);

    public final static String[] defaultSolrJClassnames = {
            "org.apache.solr.client.solrj.impl.HttpJdkSolrClient$Builder",
            "org.apache.solr.client.solrj.impl.HttpSolrClient$Builder",
            "org.apache.solr.client.solrj.impl.HttpSolrClient",
            "org.apache.solr.client.solrj.impl.HttpSolrServer",
            "org.apache.solr.client.solrj.impl.CommonsHttpSolrServer" };

    /**
     * Backward-compatible overload for any external caller still using the
     * original 3-argument signature. Delegates to the 5-argument version
     * with no basic auth credentials.
     */
    public static SolrProxy loadRemoteSolrServer(String solrHostUpdateURL, String fullClassName, boolean useBinaryRequestHandler)
    {
        return loadRemoteSolrServer(solrHostUpdateURL, fullClassName, useBinaryRequestHandler, null, null);
    }

    public static SolrProxy loadRemoteSolrServer(String solrHostUpdateURL, String fullClassName, boolean useBinaryRequestHandler, String username, String password)
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
                //  Newer solrj clients (such as the HttpJdkSolrClient of Solr 10) create
                //  a pool of non-daemon threads by default.  Since SolrMarc never closes
                //  the client explicitly, those threads would keep the JVM alive long
                //  after the import is finished.  Ask the builder to use a pool of
                //  daemon threads instead so the process can exit normally.  Builders
                //  of older solrj versions have no such method, so skip this if it
                //  is not available.
                try
                {
                    Method withexecutor = httpsolrserverClass.getMethod("withExecutor", ExecutorService.class);
                    withexecutor.invoke(httpsolrserver, daemonExecutor());
                }
                catch (Exception e)
                {
                    // no usable withExecutor() method; nothing to do
                }
                //  If Solr basic authentication credentials were provided, wire
                //  them into the builder. Unlike withExecutor above (a nice-to-have
                //  that's silently skipped on older builders), a missing method
                //  here is a real configuration problem worth surfacing loudly:
                //  silently proceeding without auth against a Solr instance that
                //  requires it would just fail later with a confusing 401, rather
                //  than a clear message about what actually went wrong.
                if (username != null && !username.isEmpty() && password != null)
                {
                    try
                    {
                        Method withBasicAuth = httpsolrserverClass.getMethod("withBasicAuthCredentials", String.class, String.class);
                        withBasicAuth.invoke(httpsolrserver, username, password);
                        logger.debug("Configured Solr basic authentication for user " + username);
                    }
                    catch (NoSuchMethodException e)
                    {
                        throw new SolrRuntimeException("Solr username/password were provided, but the solrj client class "
                            + httpsolrserverClass.getName() + " does not support withBasicAuthCredentials(String, String). "
                            + "Try specifying a newer client via -solrjClassName, e.g. "
                            + "org.apache.solr.client.solrj.impl.Http2SolrClient$Builder", e);
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        throw new SolrRuntimeException("Error invoking withBasicAuthCredentials on solrj client class "
                            + httpsolrserverClass.getName(), e);
                    }
                }
                Method buildsolrserver = httpsolrserverClass.getMethod("build");
                httpsolrserver = buildsolrserver.invoke(httpsolrserver);
            }
            else if (username != null && !username.isEmpty() && password != null)
            {
                //  The resolved solrj client isn't a Builder-style class at all
                //  (e.g. the older HttpSolrServer/CommonsHttpSolrServer path), so
                //  there's no withBasicAuthCredentials to call. Fail loudly rather
                //  than silently proceeding unauthenticated against a Solr
                //  instance that requires credentials.
                throw new SolrRuntimeException("Solr username/password were provided, but the solrj client class "
                    + httpsolrserverClass.getName() + " predates the Builder-based API and has no way to accept "
                    + "basic auth credentials. Try specifying a newer client via -solrjClassName, e.g. "
                    + "org.apache.solr.client.solrj.impl.Http2SolrClient$Builder");
            }
            Class<?> superclass = httpsolrserver.getClass().getSuperclass();
            if (superclass.getName().endsWith(".SolrServer"))
            {
                solrProxy = new SolrServerProxy(httpsolrserver);
                return (solrProxy);
            }
            if (superclass.getName().endsWith(".SolrClient")
                || superclass.getName().endsWith(".BaseHttpSolrClient")
                //  Solr 10 and later use HttpSolrClientBase as the parent of the client classes
                || superclass.getName().endsWith(".HttpSolrClientBase")
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

    /**
     * Builds an {@link ExecutorService} whose threads are daemon threads.
     * This is handed to solrj client builders so that the threads they start
     * do not prevent the JVM from exiting once the import is finished.
     */
    private static ExecutorService daemonExecutor()
    {
        return Executors.newCachedThreadPool(new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable r)
            {
                Thread thread = new Thread(r, "solrmarc-solrj");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

}