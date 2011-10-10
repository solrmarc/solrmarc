package org.solrmarc.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

public class SolrServerProxy implements SolrProxy
{
    SolrServer solrserver;
    public SolrServerProxy(SolrServer solrserver)
    {
        this.solrserver = solrserver;
    }
    
    public String addDoc(Map<String, Object> fieldsMap, boolean verbose, boolean addDocToIndex) throws IOException
    {
        SolrInputDocument inputDoc = new SolrInputDocument();
        Iterator<String> keys = fieldsMap.keySet().iterator();
        while (keys.hasNext())
        {
            String fldName = keys.next();
            Object fldValObject = fieldsMap.get(fldName);
            if (fldValObject instanceof Collection<?>)
            {
                Collection<?> collValObject = (Collection<?>)fldValObject;
                for (Object item : collValObject)
                {
                    inputDoc.addField(fldName, item, 1.0f );
                }
            }
            else if (fldValObject instanceof String)
            {
                inputDoc.addField(fldName, fldValObject, 1.0f );
            }
        }
        if (addDocToIndex)
        {
            try
            {
                solrserver.add(inputDoc);
            }
            catch (SolrServerException e)
            {
                throw(new SolrRuntimeException("SolrserverException", e));
            }
        }

        if (verbose || !addDocToIndex)
            return inputDoc.toString().replaceAll("> ", "> \n");
        else
            return(null);
    }

    public void close()
    {
        // do nothing
    }

    public SolrServer getSolrServer()
    {
        return(solrserver);        
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

    public boolean isSolrException(Exception e)
    {
        if (e.getCause() instanceof SolrServerException)
            return(true);
        return false;
    }

}
