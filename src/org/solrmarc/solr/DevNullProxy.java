package org.solrmarc.solr;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

public class DevNullProxy extends SolrProxy
{
    public DevNullProxy()
    {
    }

    @Override
    public int addDoc(SolrInputDocument inputDoc)
    {
        return(1);
    }

    @Override
    public int addDocs(Collection<SolrInputDocument> docQ)
    {
        int num = 0;
        for (SolrInputDocument doc : docQ)
        {
            num += this.addDoc(doc);
        }
        return(num);
    }

    @Override
    public void commit(boolean optimize)
    {
    }

    @Override
    public void delete(String id)
    {
    }

    @Override
    public QueryResponse query(SolrQuery params)
    {
        return null;
    }
}
