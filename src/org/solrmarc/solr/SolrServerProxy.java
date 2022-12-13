package org.solrmarc.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.solrmarc.driver.RecordAndDoc;

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

    public int addDoc(RecordAndDoc recdoc)
    {
        int num = 0;
        try
        {
            SolrInputDocument inputDoc = recdoc.getDoc();
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
    public int addDocs(Collection<RecordAndDoc> recdocQ)
    {
        int num = 0;
        try
        {
        	List<SolrInputDocument> docQ = new ArrayList<SolrInputDocument>(recdocQ.size());
        	for (RecordAndDoc recdoc : recdocQ)
        	{
        		docQ.add(recdoc.getDoc());
        	}
            UpdateResponse resp = solrserver.add(docQ);
            NamedList<Object> respresp = resp.getResponse();
            @SuppressWarnings("unused")
            int size = respresp.size();
            num += docQ.size();
            return(num);
        }
        catch (SolrException e)
        {
            throw(new SolrRuntimeException("SolrException", e));
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrServerException", e));
        }
        catch (IOException e)
        {
            throw(new SolrRuntimeException("IOException", e));
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
        catch (SolrException e)
        {
            throw(new SolrRuntimeException("SolrException", e));
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
        catch (SolrException e)
        {
            throw(new SolrRuntimeException("SolrException", e));
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
