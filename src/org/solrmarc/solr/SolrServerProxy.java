package org.solrmarc.solr;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

public class SolrServerProxy extends SolrProxy
{
    SolrServer solrserver;
    
    public SolrServerProxy(SolrServer solrserver)
    {
        this.solrserver = solrserver;
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


    public void close()
    {
    }

    public void commit(boolean optimize) throws IOException
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
    }

    public void delete(String id, boolean fromCommitted, boolean fromPending) throws IOException
    {
        try
        {
            solrserver.deleteById(id);
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
    }

    public void deleteAllDocs() throws IOException
    {
        try
        {
            solrserver.deleteByQuery("*:*");
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
    }
}
