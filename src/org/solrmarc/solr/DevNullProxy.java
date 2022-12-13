package org.solrmarc.solr;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.solrmarc.driver.RecordAndDoc;

public class DevNullProxy extends SolrProxy
{
    public DevNullProxy()
    {
    }

    @Override
    public int addDoc(RecordAndDoc inputDoc)
    {
        return(1);
    }

    @Override
    public int addDocs(Collection<RecordAndDoc> docQ)
    {
        int num = 0;
        for (RecordAndDoc recdoc : docQ)
        {
            num += this.addDoc(recdoc);
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
