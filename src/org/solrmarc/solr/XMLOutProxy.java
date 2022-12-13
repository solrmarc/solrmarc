package org.solrmarc.solr;

import java.io.PrintStream;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.solrmarc.driver.RecordAndDoc;

public class XMLOutProxy extends SolrProxy
{
    PrintStream output;

    public XMLOutProxy(PrintStream out)
    {
        this.output = out;
    }

    public int addDoc(RecordAndDoc recdoc)
    {
        synchronized (output)
        {
            SolrInputDocument inputDoc = recdoc.getDoc();
            String xml = ClientUtils.toXML(inputDoc);
            output.print(xml + "\n");
            return(1);
        }
    }

    @Override
    public int addDocs(Collection<RecordAndDoc> recdocQ)
    {
        int num = 0;
        for (RecordAndDoc recdoc : recdocQ)
        {
            num += this.addDoc(recdoc);
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
