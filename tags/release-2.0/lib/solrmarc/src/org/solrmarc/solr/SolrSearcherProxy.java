package org.solrmarc.solr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;
import java.util.concurrent.Future;


public class SolrSearcherProxy
{
    Object solrSearcher = null;
    
    public SolrSearcherProxy(SolrCoreProxy solrCoreProxy)
    {
//      refedSolrSearcher = solrCore.getSearcher();
//      solrSearcher = refedSolrSearcher.get();

        Object solrCore = solrCoreProxy.getCore();
        Object refedSolrSearcher;
        try
        {     
            Future waitSearcher[] = new Future[]{null};
            refedSolrSearcher = solrCore.getClass().getMethod("getSearcher", boolean.class, boolean.class, waitSearcher.getClass()).
                                                    invoke(solrCore, false, true, waitSearcher);
            solrSearcher = refedSolrSearcher.getClass().getMethod("get").invoke(refedSolrSearcher);
            if (waitSearcher[0] != null)
            {
                waitSearcher[0].get();
            }
        }
        catch (Exception e)
        {
            if (e instanceof java.lang.NoSuchMethodException)
            {
                Method methods[] = solrCore.getClass().getMethods();
                for (Method method : methods)
                {
                    if (method.getName().equals("getSearcher"))
                    {
                        System.err.print(method.getName() + "(");
                        Class<?> classes[] = method.getParameterTypes();
                        for (Class clazz : classes)
                        {
                            System.err.print(clazz.getName() + ", ");                            
                        }
                        System.err.println(")");
                    }
                }
            }
            e.printStackTrace();
        }
    }

    public void close()
    {
        try 
        {
            solrSearcher.getClass().getMethod("close").invoke(solrSearcher);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        solrSearcher = null;
   }
    
    public String[] getIdSet(String field, String term) throws IOException
    {
        String resultSet[] = null;
        try
        {
            Object docSet = getSolrDocSet(field, term);
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            resultSet = new String[totalSize];
            //System. out.println("Searching for :" + field +" : "+ term+ "    Num found = " + totalSize);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            int i = 0;
            while (iteratorHasNext(docIterator))
            {
                int solrDocNum = iteratorGetNextSolrId(docIterator);
                resultSet[i++] = getIdStringBySolrDocNum(solrDocNum);
            }
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(resultSet);
    }
    
    public int[] getDocSet(String field, String term) throws IOException
    {
        int resultSet[] = null;
        try
        {
            Object docSet = getSolrDocSet(field, term);
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            resultSet = new int[totalSize];
            //System. out.println("Searching for :" + field +" : "+ term+ "    Num found = " + totalSize);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            int i = 0;
            while (iteratorHasNext(docIterator))
            {
                resultSet[i++] = iteratorGetNextSolrId(docIterator);
            }
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(resultSet);
    }
    
    public Object getDocSetIterator(String field, String term) throws IOException
    {
        try
        {
            Object docSet = getSolrDocSet(field, term);
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            //System. out.println("Searching for :" + field +" : "+ term+ "    Num found = " + totalSize);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            return(docIterator);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(null);
    }
    
    public int getNumberOfHits(String field, String term) throws IOException
    {
        try
        {
            Object docSet = getSolrDocSet(field, term);
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            return(totalSize);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(-1);
    }

    private Object getSolrDocSet(String field, String term) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException 
    {
        Object schema = solrSearcher.getClass().getMethod("getSchema").invoke(solrSearcher);
        Class parser = Class.forName("org.apache.solr.search.QueryParsing");
        Object query = parser.getMethod("parseQuery", String.class, String.class, schema.getClass())
                             .invoke(null, term, field, schema);

        Object docSet = solrSearcher.getClass().getMethod("getDocSet", Class.forName("org.apache.lucene.search.Query")).invoke(solrSearcher, query);
        return(docSet);
    }
    
    public boolean iteratorHasNext(Object docSetIterator)
    {
        boolean ret = false;
        try
        {
            ret = (Boolean)(docSetIterator.getClass().getInterfaces()[0].getMethod("hasNext").invoke(docSetIterator));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(ret);
    }
    
    public int iteratorGetNextSolrId(Object docSetIterator) throws IOException
    {
        int docNo = -1;
        try
        {
            docNo = (Integer)(docSetIterator.getClass().getInterfaces()[0].getMethod("next").invoke(docSetIterator));
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(docNo);
    }
    
    public DocumentProxy iteratorGetNextDoc(Object docSetIterator) throws IOException
    {
        Object document = null;
        try
        {
            int docNo = (Integer)(docSetIterator.getClass().getInterfaces()[0].getMethod("next").invoke(docSetIterator));
            document = solrSearcher.getClass().getMethod("doc", int.class).invoke(solrSearcher, docNo);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(new DocumentProxy(document));
    }
    
//    public Object getDocBySolrDocNum(int docNo) throws IOException
//    {
//        Object document = null;
//        try
//        {
//            document = solrSearcher.getClass().getMethod("doc", int.class).invoke(solrSearcher, docNo);
//        }
//        catch (Exception e)
//        {
//            if (e instanceof InvocationTargetException)
//            {
//                Throwable cause = e.getCause();
//                if (cause instanceof IOException) throw (IOException)cause;                
//            }
//            e.printStackTrace();
//        }
//        return(document);
//    }
    
    public String getIdStringBySolrDocNum(int docNo) throws IOException
    {
        Object document = null;
        String id = null;
        try
        {
            document = solrSearcher.getClass().getMethod("doc", int.class).invoke(solrSearcher, docNo);
            Object field = document.getClass().getMethod("getField", String.class).invoke(document, "id");
            id = field.getClass().getMethod("stringValue").invoke(field).toString();
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(id);
    }
    
    public DocumentProxy getDocumentBySolrDocNum(int docNo) throws IOException
    {
        Object document = null;
        String id = null;
        try
        {
            document = solrSearcher.getClass().getMethod("doc", int.class).invoke(solrSearcher, docNo);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(new DocumentProxy(document));
    }
}
