package org.solrmarc.solr;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

public class StdOutProxy extends SolrProxy
{
    PrintStream output;
    
    public StdOutProxy(PrintStream out)
    {
        this.output = out;
    }
    
    public int addDoc(SolrInputDocument inputDoc)
    {
        ArrayList<String> fNames = new ArrayList<String>();
        fNames.addAll(inputDoc.getFieldNames());
        Collections.sort(fNames);
        String id = inputDoc.getFieldValue("id").toString();
        for (String fieldName : fNames)
        {
            Collection<Object> values = inputDoc.getFieldValues(fieldName);
            if (values != null) 
            {
                for (Object val : values)
                {
                    output.print(id + " : " + fieldName + " = " + val.toString() + "\n");
                }
            }
        }
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

    public SolrServer getSolrServer()
    {
        return(null);
    }
    
    public void commit(boolean optimize)
    {
        output.flush();
    }

    public void delete(String id)
    {
    }

    public void deleteAllDocs()
    {
    }

}
