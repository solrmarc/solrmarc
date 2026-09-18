package org.solrmarc.solr;

/**
 * Test fixture standing in for a real solrj Builder class (e.g.
 * Http2SolrClient.Builder) that supports withBasicAuthCredentials().
 * SolrCoreLoader resolves and drives this purely reflectively, exactly as
 * it would a real solrj class - the class name deliberately ends in
 * "Builder" so SolrCoreLoader routes it down the Builder-handling branch.
 *
 * build() always throws immediately, rather than trying to fake the rest
 * of the SolrClient/SolrServer wrapping machinery - tests only need to
 * confirm withBasicAuthCredentials was (or wasn't) called correctly
 * before that point, not exercise the unrelated downstream proxy wrapping.
 */
public class FakeAuthCapableBuilder
{
    public static String lastUsername;
    public static String lastPassword;
    public static boolean buildWasCalled;

    public static void reset()
    {
        lastUsername = null;
        lastPassword = null;
        buildWasCalled = false;
    }

    public FakeAuthCapableBuilder(String url)
    {
    }

    public FakeAuthCapableBuilder withBasicAuthCredentials(String username, String password)
    {
        lastUsername = username;
        lastPassword = password;
        return this;
    }

    public FakeAuthCapableBuilder build()
    {
        buildWasCalled = true;
        throw new RuntimeException("TEST_MARKER_BUILD_REACHED");
    }
}