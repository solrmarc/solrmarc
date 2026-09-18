package org.solrmarc.solr;

/**
 * Test fixture standing in for an old-style solrj client (like the real
 * HttpSolrServer/CommonsHttpSolrServer) that predates the Builder-based
 * API entirely. Deliberately named without a "Builder" suffix so
 * SolrCoreLoader routes it down the non-Builder branch, where it should
 * reject basic auth credentials outright with a clear error rather than
 * silently proceeding unauthenticated.
 */
public class FakeLegacyClient
{
    public FakeLegacyClient(String url)
    {
    }
}