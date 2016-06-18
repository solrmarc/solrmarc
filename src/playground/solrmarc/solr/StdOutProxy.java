package playground.solrmarc.solr;

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

public class StdOutProxy implements SolrProxy
{
    PrintStream output;
    
    public StdOutProxy(PrintStream out)
    {
        this.output = out;
    }
    
    public void addDoc(SolrInputDocument inputDoc)
    {
        ArrayList<String> fNames = new ArrayList<String>();
        fNames.addAll(inputDoc.getFieldNames());
        Collections.sort(fNames);
        String id = inputDoc.getFieldValue("id").toString();
        for (String fieldName : fNames)
        {
            for (Object val : inputDoc.getFieldValues(fieldName))
            {
                output.print(id + " : " + fieldName + " = " + val.toString() + "\n");
            }
        }
    }
    
    public String addDoc(Map<String, Object> fieldsMap, boolean verbose, boolean addDocToIndex) throws IOException
    {
        SolrInputDocument inputDoc = new SolrInputDocument();
        Iterator<String> keys = fieldsMap.keySet().iterator();
        while (keys.hasNext())
        {
            String fldName = keys.next();
            Object fldValObject = fieldsMap.get(fldName);
            if (fldValObject instanceof Collection<?>)
            {
                Collection<?> collValObject = (Collection<?>)fldValObject;
                for (Object item : collValObject)
                {
                    inputDoc.addField(fldName, item, 1.0f );
                }
            }
            else if (fldValObject instanceof String)
            {
                inputDoc.addField(fldName, fldValObject, 1.0f );
            }
        }
        if (addDocToIndex)
        {
        }

        if (verbose || !addDocToIndex)
            return inputDoc.toString().replaceAll("> ", "> \n");
        else
            return(null);
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

    public boolean isSolrException(Exception e)
    {
        if (e.getCause() instanceof SolrServerException)
            return(true);
        return false;
    }

    @Override
    public void addDocs(Collection<SolrInputDocument> docQ)
    {
        for (SolrInputDocument doc : docQ)
        {
            this.addDoc(doc);
        }
    }

}
