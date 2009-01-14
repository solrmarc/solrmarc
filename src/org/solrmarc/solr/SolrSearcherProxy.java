package org.solrmarc.solr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


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
            refedSolrSearcher = solrCore.getClass().getMethod("getSearcher").invoke(solrCore);
            solrSearcher = refedSolrSearcher.getClass().getMethod("get").invoke(refedSolrSearcher);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Object getDocSetIterator(String field, String term) throws IOException
    {
        try
        {
            Object qterm = Class.forName("org.apache.lucene.index.Term")
                               .getConstructor(String.class, String.class)
                               .newInstance(field, term);
            Object query = Class.forName("org.apache.lucene.search.TermQuery")
                            .getConstructor(qterm.getClass())
                            .newInstance(qterm);
            Object docSet = solrSearcher.getClass().getMethod("getDocSet", query.getClass());
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            System. out.println("Num found = " + totalSize);
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

    public boolean iteratorHasNext(Object docSetIterator)
    {
        boolean ret = false;
        try
        {
            ret = (Boolean)(docSetIterator.getClass().getMethod("hasNext").invoke(docSetIterator));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(ret);
    }
    
    public Object iteratorGetNext(Object docSetIterator) throws IOException
    {
        Object document = null;
        try
        {
            int docNo = (Integer)(docSetIterator.getClass().getMethod("next").invoke(docSetIterator));
            document = solrSearcher.getClass().getMethod("getDocument", int.class).invoke(solrSearcher, docNo);
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
        return(document);
    }

}
