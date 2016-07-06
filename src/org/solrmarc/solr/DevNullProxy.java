package org.solrmarc.solr;

import java.io.IOException;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

public class DevNullProxy extends SolrProxy
{
    
    public DevNullProxy()
    {
    }
    
    @Override
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

    @Override
    public void close()
    {
    }

    public SolrServer getSolrServer()
    {
        return(null);
    }
    
    @Override
    public void commit(boolean optimize) throws IOException
    {
    }

    @Override
    public void delete(String id, boolean fromCommitted, boolean fromPending) throws IOException
    {
    }

    @Override
    public void deleteAllDocs() throws IOException
    {
    }
}
