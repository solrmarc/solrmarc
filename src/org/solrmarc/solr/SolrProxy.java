package org.solrmarc.solr;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

public abstract class SolrProxy
{
    /**
     * return true if exception is a SolrException
     *
     * @param e  exception to check
     * @return   true if exception is a SolrException
     */
    public final boolean isSolrException(Exception e)
    {
        if (e.getCause() instanceof SolrServerException)
            return(true);
        return false;
    }

    /**
     * given a SolrInputDocument add it to the index
     * 
     * @param document  document to add to Solr index
     * @return          a string representation of the document
     */

    public abstract int addDoc(SolrInputDocument document);

    public abstract int addDocs(Collection<SolrInputDocument> docQ);

    /**
     * delete doc from the index
     * 
     * @param id
     *            the unique identifier of the document to be deleted
     */
    public abstract void delete(String id);

    /**
     * commit changes to the index
     *
     * @param optimize  Solr run database post-commit
     */
    public abstract void commit(boolean optimize);

    public abstract QueryResponse query(SolrQuery params);

}