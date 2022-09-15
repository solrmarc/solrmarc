package org.solrmarc.solr;

import java.io.PrintStream;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;

public class XMLOutProxy extends SolrProxy
{
    PrintStream output;

    public XMLOutProxy(PrintStream out)
    {
        this.output = out;
    }

    public int addDoc(SolrInputDocument inputDoc)
    {
        synchronized (output)
        {
            String xml = ClientUtils.toXML(inputDoc);
            output.print(xml + "\n");
            return(1);
        }
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
    public void commit(boolean optimize)
    {
        output.flush();
    }

    @Override
    public void delete(String id)
    {
    }

    @Override
    public QueryResponse query(SolrQuery params)
    {
        return null;
    }

}
