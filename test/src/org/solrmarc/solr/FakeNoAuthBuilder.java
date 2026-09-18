package org.solrmarc.solr;

/**
 * Test fixture standing in for a Builder-style solrj client class that
 * does NOT support withBasicAuthCredentials() (i.e. an older solrj
 * version). Used to confirm SolrCoreLoader fails loudly, before ever
 * calling build(), when credentials are supplied but unsupported.
 */
public class FakeNoAuthBuilder
{
    public static boolean buildWasCalled;

    public static void reset()
    {
        buildWasCalled = false;
    }

    public FakeNoAuthBuilder(String url)
    {
    }

    public FakeNoAuthBuilder build()
    {
        buildWasCalled = true;
        throw new RuntimeException("TEST_MARKER_SHOULD_NOT_BE_REACHED");
    }
}