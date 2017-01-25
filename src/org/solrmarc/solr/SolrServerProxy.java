package org.solrmarc.solr;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

public class SolrServerProxy extends SolrProxy
{
    final SolrServer solrserver;

    public SolrServerProxy(SolrServer solrserver)
    {
        this.solrserver = solrserver;
    }

    public SolrServerProxy(Object httpsolrserver)
    {
        this.solrserver = (SolrServer)httpsolrserver;
    }

    public int addDoc(SolrInputDocument inputDoc)
    {
        int num = 0;
        try
        {
            UpdateResponse resp = solrserver.add(inputDoc);
            @SuppressWarnings("unused")
            int status = resp.getStatus();
            return(++num);
        }
        catch (SolrException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IOException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
    }

    @Override
    public int addDocs(Collection<SolrInputDocument> docQ)
    {
        int num = 0;
        try
        {
            UpdateResponse resp = solrserver.add(docQ);
            NamedList<Object> respresp = resp.getResponse();
            @SuppressWarnings("unused")
            int size = respresp.size();
            num += docQ.size();
            return(num);
        }
        catch (SolrException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IOException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
    }

    public void commit(boolean optimize)
    {
        try
        {
            if (optimize)
                solrserver.optimize();
            else
                solrserver.commit();
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IOException e)
        {
            throw(new SolrRuntimeException("IOException", e));
        }
    }

    public void delete(String id)
    {
        try
        {
            solrserver.deleteById(id);
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IOException e)
        {
            throw(new SolrRuntimeException("IOException", e));
        }
    }

    @Override
    public QueryResponse query(SolrQuery params)
    {
        try
        {
            return solrserver.query(params);
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
    }
}
