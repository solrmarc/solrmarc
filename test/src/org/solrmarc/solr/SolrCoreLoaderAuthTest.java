package org.solrmarc.solr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Tests SolrCoreLoader.loadRemoteSolrServer()'s basic-auth wiring in
 * isolation from any real Solr instance. Two things make this possible
 * without a network dependency on a real Solr server:
 *
 *   - A tiny embedded HTTP server (built into every JDK, no extra
 *     dependency) stands in just long enough to satisfy the /admin/ping
 *     check loadRemoteSolrServer performs before doing anything else.
 *     Note this ping is unauthenticated in the real code (a plain
 *     URL#openStream() call, no Authorization header) - it exists only
 *     to confirm something is listening, so a fake server responding
 *     with "status":"OK" is a faithful stand-in.
 *
 *   - Fake "Builder" classes (see FakeAuthCapableBuilder,
 *     FakeNoAuthBuilder, FakeLegacyClient) stand in for real solrj client
 *     classes. SolrCoreLoader resolves and drives whatever class name
 *     it's given purely via reflection, so these fakes are driven through
 *     the exact same code path a real solrj Builder would be.
 */
public class SolrCoreLoaderAuthTest
{
    private HttpServer pingServer;
    private String solrUrl;

    @Before
    public void startFakePingServer() throws IOException
    {
        pingServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        pingServer.createContext("/admin/ping", exchange -> respondOk(exchange));
        pingServer.start();
        solrUrl = "http://localhost:" + pingServer.getAddress().getPort();

        FakeAuthCapableBuilder.reset();
        FakeNoAuthBuilder.reset();
    }

    @After
    public void stopFakePingServer()
    {
        if (pingServer != null)
        {
            pingServer.stop(0);
        }
    }

    private static void respondOk(HttpExchange exchange) throws IOException
    {
        byte[] body = "{\"status\":\"OK\"}".getBytes("UTF-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody())
        {
            os.write(body);
        }
    }

    @Test
    public void credentialsAreWiredIntoABuilderThatSupportsThem()
    {
        try
        {
            SolrCoreLoader.loadRemoteSolrServer(solrUrl, FakeAuthCapableBuilder.class.getName(), true, "someuser", "somepass");
            fail("expected the fake build() to throw, since it always does");
        }
        catch (SolrRuntimeException e)
        {
            assertTrue("expected our marker exception to be the underlying cause: " + e,
                causeChainContains(e, "TEST_MARKER_BUILD_REACHED"));
        }
        assertEquals("someuser", FakeAuthCapableBuilder.lastUsername);
        assertEquals("somepass", FakeAuthCapableBuilder.lastPassword);
        assertTrue("build() should have been reached", FakeAuthCapableBuilder.buildWasCalled);
    }

    @Test
    public void noCredentialsMeansWithBasicAuthIsNeverCalled()
    {
        try
        {
            SolrCoreLoader.loadRemoteSolrServer(solrUrl, FakeAuthCapableBuilder.class.getName(), true);
            fail("expected the fake build() to throw, since it always does");
        }
        catch (SolrRuntimeException e)
        {
            assertTrue(causeChainContains(e, "TEST_MARKER_BUILD_REACHED"));
        }
        assertNull("withBasicAuthCredentials should never have been called", FakeAuthCapableBuilder.lastUsername);
        assertNull(FakeAuthCapableBuilder.lastPassword);
        assertTrue("build() should still have been reached normally", FakeAuthCapableBuilder.buildWasCalled);
    }

    @Test
    public void credentialsProvidedButBuilderDoesNotSupportAuthFailsBeforeBuild()
    {
        try
        {
            SolrCoreLoader.loadRemoteSolrServer(solrUrl, FakeNoAuthBuilder.class.getName(), true, "someuser", "somepass");
            fail("expected a SolrRuntimeException since this fake builder has no withBasicAuthCredentials");
        }
        catch (SolrRuntimeException e)
        {
            assertTrue("message should explain the unsupported method: " + e.getMessage(),
                e.getMessage().contains("does not support withBasicAuthCredentials"));
        }
        assertFalse("build() should never have been reached - fail fast, don't proceed unauthenticated",
            FakeNoAuthBuilder.buildWasCalled);
    }

    @Test
    public void credentialsProvidedButClientPredatesBuilderApiFailsClearly()
    {
        try
        {
            SolrCoreLoader.loadRemoteSolrServer(solrUrl, FakeLegacyClient.class.getName(), true, "someuser", "somepass");
            fail("expected a SolrRuntimeException since this fake client predates the Builder API entirely");
        }
        catch (SolrRuntimeException e)
        {
            assertTrue("message should explain the client predates the Builder API: " + e.getMessage(),
                e.getMessage().contains("predates the Builder-based API"));
        }
    }

    /**
     * SolrCoreLoader wraps reflection failures (InvocationTargetException
     * wrapping our thrown marker) inside its own SolrRuntimeException.
     * Walks the full cause chain looking for our marker text, rather than
     * assuming a specific exact wrapping depth.
     */
    private static boolean causeChainContains(Throwable t, String markerText)
    {
        while (t != null)
        {
            if (t.getMessage() != null && t.getMessage().contains(markerText))
            {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}