package org.solrmarc.solr;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.NamedList;

public class DevNullProxy extends SolrProxy
{
    
    public DevNullProxy()
    {
    }
    
    public int addDoc(SolrInputDocument inputDoc)
    {
        return(1);
    }
    
    @Override
    public int addDocs(Collection<SolrInputDocument> docQ)
    {
        int num = 0;
        for (SolrInputDocument doc : docQ)
        {
            num += this.addDoc(doc);
        }
        return(num);
    }

    public void close()
    {
    }

    public SolrServer getSolrServer()
    {
        return(null);
    }
    
    public void commit(boolean optimize) throws IOException
    {
    }

    public void delete(String id, boolean fromCommitted, boolean fromPending) throws IOException
    {
    }

    public void deleteAllDocs() throws IOException
    {
    }
}
