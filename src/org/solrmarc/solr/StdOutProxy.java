package org.solrmarc.solr;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.solrmarc.driver.RecordAndDoc;

public class StdOutProxy extends SolrProxy
{
    PrintStream output;

    public StdOutProxy(PrintStream out)
    {
        this.output = out;
    }

    public int addDoc(RecordAndDoc recdoc)
    {
        synchronized (output)
        {
            SolrInputDocument inputDoc = recdoc.getDoc();
            ArrayList<String> fNames = new ArrayList<String>();
            fNames.addAll(inputDoc.getFieldNames());
            Collections.sort(fNames);
            String id = inputDoc.getFieldValue("id") != null ? inputDoc.getFieldValue("id").toString() : "<no id>";
            for (String fieldName : fNames)
            {
                Collection<Object> values = inputDoc.getFieldValues(fieldName);
                if (values != null) 
                {
                    for (Object val : values)
                    {
                        output.print(id + " : " + fieldName + " = " + ((val != null) ? val.toString() : "[null]") + "\n");
                    }
                }
            }
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
