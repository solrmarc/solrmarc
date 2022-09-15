package org.solrmarc.mixin;

import org.marc4j.marc.Record;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.solrmarc.index.SolrIndexerMixin;

public class ILSConnectorClient extends SolrIndexerMixin implements AutoCloseable 
{
    static int ids[] = { 399, 401, 11000, 203802, 824842, 7000000};
    
    private Object httpclient = null; 
        
    public String getDataFromUrlViaHttpClient(String url)
    {
        String response = null;
        Method getStatusLine;
        Method getEntity;
        Method entityToString;
        try
        {
            getStatusLine = Class.forName("org.apache.http.HttpResponse").getMethod("getStatusLine");
            getEntity = Class.forName("org.apache.http.HttpResponse").getMethod("getEntity");
            entityToString = Class.forName("org.apache.http.util.EntityUtils").getMethod("toString", Class.forName("org.apache.http.HttpEntity")); 
        }
        catch (NoSuchMethodException | SecurityException | ClassNotFoundException e2)
        {
            throw new RuntimeException();
        }
        
        if (httpclient == null)
        {
            try
            {
                httpclient = Class.forName("org.apache.http.impl.client.HttpClients").getMethod("createDefault").invoke(null);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e)
            {
                throw new RuntimeException();
            }
        }
        Object httpGet;
        try
        {
            httpGet = Class.forName("org.apache.http.client.methods.HttpGet").getConstructor(String.class).newInstance(url);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e1)
        {
            throw new RuntimeException();
        }

        Object httpResponse;
        try
        {
            httpResponse = httpclient.getClass().getMethod("execute", Class.forName("org.apache.http.client.methods.HttpUriRequest")).invoke(httpclient, httpGet);
            Object statusLine = getStatusLine.invoke(httpResponse);
            int status = (Integer) (statusLine.getClass().getMethod("getStatusCode").invoke(statusLine));
            if (status >= 200 && status < 300) 
            {
                Object httpEntity = getEntity.invoke(httpResponse);
                if (httpEntity != null)
                {
                    response = entityToString.invoke(null, httpEntity).toString();
                }
            } 
            else 
            {
                throw new RuntimeException("Status "+status+" returned for URL: " + url);
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
        return(response);
    }
    
    public String getDataFromUrlViaCommonsHttpClient(String url)
    {
        String response = null;
        Method getResponseBodyAsString;
        Method releaseConnection;
        try
        {
            getResponseBodyAsString = Class.forName("org.apache.commons.httpclient.HttpMethod").getMethod("getResponseBodyAsString");
            releaseConnection = Class.forName("org.apache.commons.httpclient.HttpMethod").getMethod("releaseConnection");
        }
        catch (NoSuchMethodException | SecurityException | ClassNotFoundException e2)
        {
            throw new RuntimeException();
        }
        if (httpclient == null)
        {
            try
            {
                httpclient = Class.forName("org.apache.commons.httpclient.HttpClient").getConstructor().newInstance();
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException e)
            {
                throw new RuntimeException();
            }
        }

//        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//            new DefaultHttpMethodRetryHandler());
//        client.getParams().setSoTimeout(1000 * timeout);
//        client.getParams().setConnectionManagerTimeout(1000 * timeout);
//        if (userName != null && password != null) {
//          setBasicAuthorization(method, userName, password);
//        }
        Object getmethod = null;
        try 
        {
            getmethod = Class.forName("org.apache.commons.httpclient.methods.GetMethod").getConstructor(String.class).newInstance(url);
            int status = (Integer)(httpclient.getClass().getMethod("executeMethod", Class.forName("org.apache.commons.httpclient.HttpMethod")).invoke(httpclient, getmethod));
            if (status >= 200 && status < 300) 
            {
                response = getResponseBodyAsString.invoke(getmethod).toString();
            }
            else 
            {
                throw new RuntimeException("Status "+status+" returned for URL: " + url);
            }
        } 
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException e) 
        {
            throw new RuntimeException("Failed to get " + url, e);
        } 
        finally 
        {
            try
            {
                if (getmethod != null) 
                    releaseConnection.invoke(getmethod);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
                throw new RuntimeException("Failed to get " + url, e);
            }
        }
        return(response);
    }

    public String getDataFromUrlByID(String urlBase, String idStr)
    {
        String response = null;
        String url = urlBase + idStr;
 //       response = getDataFromUrlViaHttpClient(url);
        response = getDataFromUrlViaCommonsHttpClient(url);
        return(response);
    }
    
    public String getDataForRecordByID(final Record record, String urlBase, String idRegex, String idSelect)
    {
        String response = null;
        String id = record.getControlNumber();
        if (id.matches(idRegex))
        {
            String idToUse = id.replaceFirst(idRegex,  idSelect);
            response = getDataFromUrlByID(urlBase, idToUse);
        }
        return(response);
    }

    @Override
    public void close() throws Exception
    {
        if (httpclient != null && httpclient instanceof Closeable) 
        {
            Class.forName("java.io.Closeable").getMethod("close").invoke(httpclient);
            httpclient = null;
        }
    }
    
    public static void main(String[] args)
    {
        ILSConnectorClient client = new ILSConnectorClient();
        
        String urlBase = "http://firehose2test.lib.virginia.edu:8081/firehose2/items/";
        for (int id : ids) 
        {
            try {
                String response = client.getDataFromUrlByID(urlBase, Integer.toString(id));
                System.out.println(response);
            }
            catch (RuntimeException re)
            {
                System.out.println("Exception for id : "+id);
            }
        }
        try
        {
            client.close();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
