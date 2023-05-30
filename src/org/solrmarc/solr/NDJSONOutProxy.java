package org.solrmarc.solr;

import java.io.PrintStream;
import java.util.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

import com.google.gson.Gson;
public class NDJSONOutProxy extends SolrProxy
{
    PrintStream output;

    public NDJSONOutProxy(PrintStream out)
    {
        this.output = out;
    }

    public int addDoc(SolrInputDocument inputDoc)
    {
        synchronized (output)
        {
            Map<String, List<String>> record = new HashMap<String, List<String>>();
            for (String name : inputDoc.getFieldNames()) {
                ArrayList<String> valList = new ArrayList<String>();

                Iterator values = inputDoc.get(name).iterator();

                while (values.hasNext()) {
                    valList.add(values.next().toString());
                }

                record.put(name, valList);
            }

            Gson gson = new Gson();

            String jsonOut = gson.toJson(record);

            output.print(jsonOut + "\n");

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

