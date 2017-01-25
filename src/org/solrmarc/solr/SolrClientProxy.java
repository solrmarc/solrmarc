package org.solrmarc.solr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

public class SolrClientProxy extends SolrProxy
{
    Object solrclient;
    Method addDoc;
    Method addDocs;
    Method commit;
    Method optimize;
    Method delete;
    Method query;

    public SolrClientProxy(Object httpsolrclient)
    {
        this.solrclient = httpsolrclient;
        try
        {
            this.addDoc = getMethod(solrclient, "add", org.apache.solr.common.SolrInputDocument.class);
            this.addDocs = getMethod(solrclient, "add", Collection.class );
            this.commit = getMethod(solrclient, "commit");
            this.optimize = getMethod(solrclient, "optimize");
            this.delete = getMethod(solrclient, "deleteById", String.class);
            this.query = getMethod(solrclient, "query", SolrQuery.class);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
            //throw new IndexerSpecException(e, eErrorSeverity.FATAL, "Unable to find needed methods in solrj jars.")
        }
    }

    private static Method getMethod(Object solrclient, String methodName, Class<?> ... parmClasses) throws NoSuchMethodException, SecurityException
    {
        Class<?> theClazz = solrclient.getClass();
        Method result = null;
        do { 
            try { 
                result = theClazz.getDeclaredMethod(methodName, parmClasses);
            }
            catch (NoSuchMethodException e)
            {
                if (theClazz == Object.class)
                {
                    throw e;
                }
                theClazz = theClazz.getSuperclass();
            }            
        } while (result == null);
        return result;
    }

    public int addDoc(SolrInputDocument inputDoc)
    {
        int num = 0;
        try
        {
            UpdateResponse resp = (UpdateResponse) addDoc.invoke(solrclient, inputDoc);
            @SuppressWarnings("unused")
            int status = resp.getStatus();
            return(++num);
        }
        catch (SolrException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IllegalAccessException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IllegalArgumentException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (InvocationTargetException e)
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
            UpdateResponse resp = (UpdateResponse) addDocs.invoke(solrclient, docQ);
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
        catch (IllegalAccessException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IllegalArgumentException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (InvocationTargetException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
    }

    @Override
    public void commit(boolean doOptimize) throws IOException
    {
        try
        {  
            if (doOptimize)
                optimize.invoke(solrclient);
            else
                commit.invoke(solrclient);
        }
        catch (IllegalAccessException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IllegalArgumentException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (InvocationTargetException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
    }

    @Override
    public void delete(String id) throws IOException
    {
        try
        {
            delete.invoke(solrclient, id);
        }
        catch (IllegalAccessException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (IllegalArgumentException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        catch (InvocationTargetException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
    }

    @Override
    public QueryResponse query(SolrQuery params) throws IOException
    {
        QueryResponse result = null;
        try
        {
            result = (QueryResponse)query.invoke(solrclient, params);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw(new SolrRuntimeException("SolrserverException", e));
        }
        return result;
    }

}
