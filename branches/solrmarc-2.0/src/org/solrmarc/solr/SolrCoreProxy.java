package org.solrmarc.solr;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


public class SolrCoreProxy
{
    Object solrCore = null;
    Object updateHandler = null;
    Object deleteUpdateCommand = null;
    Object commitUpdateCommand = null;
    Object addUpdateCommand = null;
    Object documentBuilder = null;
    Class<?> solrExceptionClass = null;
    
    public SolrCoreProxy(Object solrCore)
    {
        this.solrCore = solrCore;
    }
    
    /** return the solrCore as an Object.  Public b/c it's used by test code */
    public Object getCore()
    {
        return solrCore;
    }
    
    public boolean isSolrException(Exception e)
    {
        if (solrExceptionClass == null)
        {
            try
            {
                solrExceptionClass = Class.forName("org.apache.solr.common.SolrException");
            }
            catch (ClassNotFoundException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return ( (solrExceptionClass != null) ? solrExceptionClass.isInstance(e): false);
    }
        
    private void setValue(Object object, String fieldName, Object value)
    {
        try
        {
            Field field = object.getClass().getField(fieldName);
            field.set(object, value);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String addDoc(Map<String, Object> map, boolean verbose) throws IOException
    {
        try
        {
            if (addUpdateCommand == null)
            {            
                Class<?> addUpdateCommandClass = Class.forName("org.apache.solr.update.AddUpdateCommand");            
                addUpdateCommand = addUpdateCommandClass.getConstructor().newInstance();
            }
            else
                ((org.apache.solr.update.AddUpdateCommand) addUpdateCommand).clear();
                
            if (documentBuilder == null)
            {
                Class<?> indexSchemaClass = Class.forName("org.apache.solr.schema.IndexSchema");
                Object indexSchema = solrCore.getClass().getMethod("getSchema").invoke(solrCore);
                
                Class<?> documentBuilderClass = Class.forName("org.apache.solr.update.DocumentBuilder");            
                documentBuilder = documentBuilderClass.getConstructor(indexSchemaClass).newInstance(indexSchema);
            }
            if (updateHandler == null)
            {
                Method updateHandlerMethod = solrCore.getClass().getMethod("getUpdateHandler");
                updateHandler = updateHandlerMethod.invoke(solrCore);
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: Problem creating AddUpdateCommand in SolrCoreProxy"); 
            throw new RuntimeException("Error: Problem creating AddUpdateCommand in SolrCoreProxy", e);
        }            
        
        
        try
        {
            documentBuilder.getClass().getMethod("startDoc").invoke(documentBuilder);
        }
        catch (Exception e1)
        {
            System.err.println("Error: Problem invoking startDoc in SolrCoreProxy");         
            throw new RuntimeException("Error: Problem invoking startDoc in SolrCoreProxy", e1);
        }
        Iterator<String> keys = map.keySet().iterator();
        while (keys.hasNext())
        {
            String key = keys.next();
            Object value = map.get(key);
            if (value instanceof String)
            {
                try
                {
                    documentBuilder.getClass().getMethod("addField", String.class, String.class).invoke(documentBuilder, key, (String)value);
                }
                catch (Exception e)
                {
                    System.err.println("Error: Problem invoking addField in SolrCoreProxy");  
                    e.printStackTrace();
                    throw new RuntimeException("Error: Problem invoking addField in SolrCoreProxy", e);
                }
            }
            else if (value instanceof Collection)
            {
                Iterator<?> valIter = ((Collection)value).iterator();
                while (valIter.hasNext())
                {
                    Object nextItem = valIter.next();
                    if (nextItem != null)
                    {
                        String collVal = nextItem.toString();
                        try
                        {
                            documentBuilder.getClass().getMethod("addField", String.class, String.class).invoke(documentBuilder, key, collVal);
                        }
                        catch (Exception e)
                        {
                            System.err.println("Error: Problem invoking addField in SolrCoreProxy");               
                            throw new RuntimeException("Error: Problem invoking addField in SolrCoreProxy", e);
                        }
                    }
                }
            }
        }
        try 
        { 
            documentBuilder.getClass().getMethod("endDoc").invoke(documentBuilder);
        }
        catch (Exception e1)
        {
            System.err.println("Error: Problem invoking startDoc in SolrCoreProxy");               
            throw new RuntimeException("Error: Problem invoking startDoc in SolrCoreProxy", e1);
        }
        
        // finish up
        Object doc;
        try
        {
            doc = documentBuilder.getClass().getMethod("getDoc").invoke(documentBuilder);
        }
        catch (Exception e1)
        {
            System.err.println("Error: Problem invoking getDoc in SolrCoreProxy");               
            throw new RuntimeException("Error: Problem invoking getDoc in SolrCoreProxy", e1);
        }
        setValue(addUpdateCommand, "doc", doc);
        setValue(addUpdateCommand, "allowDups", false);
        setValue(addUpdateCommand, "overwriteCommitted", true);
        setValue(addUpdateCommand, "overwritePending", true);
        
        String docStr = doc.toString().replaceAll("> ", "> \n");
       
        try
        {
            Method addMethod = updateHandler.getClass().getMethod("addDoc", addUpdateCommand.getClass());
            addMethod.invoke(updateHandler, addUpdateCommand);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) throw (IOException)cause;                
            System.err.println("Error: Problem adding document via SolrCoreProxy");               
            throw new RuntimeException("Error: Problem adding document via SolrCoreProxy: can't call addDoc", e);
        }
        catch (Exception e)
        {
            System.err.println("Error: Problem adding document via SolrCoreProxy");               
            throw new RuntimeException("Error: Problem adding document via SolrCoreProxy: can't call addDoc", e);
        } 
        return docStr;
    }

    public void delete(String id, boolean fromCommitted, boolean fromPending)
    {
        try
        {
            if (deleteUpdateCommand == null)
            {            
                Class<?> deleteUpdateCommandClass = Class.forName("org.apache.solr.update.DeleteUpdateCommand");
                deleteUpdateCommand = deleteUpdateCommandClass.getConstructor().newInstance();
            }
            if (updateHandler == null)
            {
                Method updateHandlerMethod = solrCore.getClass().getMethod("getUpdateHandler");
                updateHandler = updateHandlerMethod.invoke(solrCore);
            }

        }
        catch (Exception e)
        {
            System.err.println("Error: Problem creating DeleteUpdateCommand in SolrCoreProxy");               
            throw new RuntimeException("Error: Problem  creating DeleteUpdateCommand in SolrCoreProxy", e);
        }            
        
        setValue(deleteUpdateCommand, "id", id);
        setValue(deleteUpdateCommand, "fromCommitted", fromCommitted);
        setValue(deleteUpdateCommand, "fromPending", fromPending);
        Method deleteMethod;
        try
        {
            deleteMethod = updateHandler.getClass().getMethod("delete", deleteUpdateCommand.getClass());
            deleteMethod.invoke(updateHandler, deleteUpdateCommand);
        }
        catch (Exception e)
        {
            System.err.println("Error: Problem creating DeleteUpdateCommand in SolrCoreProxy");               
            throw new RuntimeException("Error: Problem  creating DeleteUpdateCommand in SolrCoreProxy", e);
        }
    }

    public void commit(boolean optimize) throws IOException
    {
        try
        {
            if (commitUpdateCommand == null)
            {            
                Class<?> commitUpdateCommandClass = Class.forName("org.apache.solr.update.CommitUpdateCommand");            
                commitUpdateCommand = commitUpdateCommandClass.getConstructor(boolean.class).newInstance(false);
            }
            if (updateHandler == null)
            {
                Method updateHandlerMethod = solrCore.getClass().getMethod("getUpdateHandler");
                updateHandler = updateHandlerMethod.invoke(solrCore);
            }

        }
        catch (Exception e)
        {
            System.err.println("Error: Problem creating CommitUpdateCommand in SolrCoreProxy");               
            throw new RuntimeException("Error: Problem  creating CommitUpdateCommand in SolrCoreProxy", e);
        }

        setValue(commitUpdateCommand, "optimize", optimize);
        try
        {
            Method commitMethod = updateHandler.getClass().getMethod("commit", commitUpdateCommand.getClass());
            commitMethod.invoke(updateHandler, commitUpdateCommand);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) throw (IOException)cause;                
            
            System.err.println("Error: Problem invoking commit in SolrCoreProxy");               
            throw new RuntimeException("Error:  Problem invoking commit in SolrCoreProxy", e);
        }        
        catch (Exception e)
        {
            System.err.println("Error:  Problem invoking commit in SolrCoreProxy");               
            throw new RuntimeException("Error:  Problem invoking commit in SolrCoreProxy", e);
        }            
    }
    
    public void close()
    {
        Method closeMethod;
        try
        {
            closeMethod = solrCore.getClass().getMethod("close");
            closeMethod.invoke(solrCore);
        }
        catch (Exception e)
        {
            System.err.println("Error: Problem invoking close in SolrCoreProxy");             
            throw new RuntimeException("Error: Problem invoking close  in SolrCoreProxy", e);
        }
    }


}
