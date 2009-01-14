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
    
    Object getCore()
    {
        return(solrCore);
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
        return((solrExceptionClass != null) ? solrExceptionClass.isInstance(e): false);
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
            e.printStackTrace();
            System.exit(1);
        }            
        
        
        String docStr = null;
        try
        {
            documentBuilder.getClass().getMethod("startDoc").invoke(documentBuilder);
            Iterator<String> keys = map.keySet().iterator();
            while (keys.hasNext())
            {
                String key = keys.next();
                Object value = map.get(key);
                if (value instanceof String)
                {
                    documentBuilder.getClass().getMethod("addField", String.class, String.class).invoke(documentBuilder, key, (String)value);
                }
                else if (value instanceof Collection)
                {
                    Iterator<?> valIter = ((Collection)value).iterator();
                    while (valIter.hasNext())
                    {
                        String collVal = valIter.next().toString();
                        documentBuilder.getClass().getMethod("addField", String.class, String.class).invoke(documentBuilder, key, collVal);
                    }
                }
            }
            documentBuilder.getClass().getMethod("endDoc").invoke(documentBuilder);
            
            // finish up
            Object doc = documentBuilder.getClass().getMethod("getDoc").invoke(documentBuilder);
            setValue(addUpdateCommand, "doc", doc);
            setValue(addUpdateCommand, "allowDups", false);
            setValue(addUpdateCommand, "overwriteCommitted", true);
            setValue(addUpdateCommand, "overwritePending", true);
            
            if (verbose)
            {
                //System.out.println(record.toString());
                docStr = doc.toString().replaceAll("> ", "> \n");
            }
        }
        catch (Exception e1)
        {
            System.err.println("Error: Problem adding document via SolrCoreProxy");               
//          logger.error("Error: Problem instantiating SolrCore");
            e1.printStackTrace();
            System.exit(1);
        }
       
        Method addMethod;
        try
        {
            addMethod = updateHandler.getClass().getMethod("addDoc", addUpdateCommand.getClass());
            addMethod.invoke(updateHandler, addUpdateCommand);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            System.err.println("Error: Problem adding document via SolrCoreProxy");               
//          logger.error("Error: Problem instantiating SolrCore");
            e.printStackTrace();
            System.exit(1);
        } 
        return(docStr);
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
            e.printStackTrace();
            System.exit(1);
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
            System.err.println("Error: Problem deleting document via SolrCoreProxy");               
//          logger.error("Error: Problem instantiating SolrCore");
            e.printStackTrace();
            System.exit(1);
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
            e.printStackTrace();
            System.exit(1);
        }

        setValue(commitUpdateCommand, "optimize", optimize);
        Method commitMethod;
        try
        {
            commitMethod = updateHandler.getClass().getMethod("commit", commitUpdateCommand.getClass());
            commitMethod.invoke(updateHandler, commitUpdateCommand);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            System.err.println("Error: Problem committing via SolrCoreProxy");               
//          logger.error("Error: Problem instantiating SolrCore");
            e.printStackTrace();
            System.exit(1);
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
            System.err.println("Error: Problem closing via SolrCoreProxy");               
//          logger.error("Error: Problem instantiating SolrCore");
            e.printStackTrace();
            System.exit(1);
        }
    }


}
