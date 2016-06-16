package playground.solrmarc.index;

import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.index.indexer.IndexerSpecException;
import playground.solrmarc.index.indexer.ValueIndexerFactory;
import playground.solrmarc.solr.SolrProxy;

import org.apache.solr.common.SolrInputDocument;
import org.marc4j.ErrorHandler;
import org.marc4j.MarcError;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.marc.MarcReaderFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexer
{
    protected final List<AbstractValueIndexer<?>> indexers;
    protected SolrProxy solrProxy;
    public EnumSet<eErrorHandleVal> errHandle = EnumSet.noneOf(eErrorHandleVal.class);
    public enum eErrorHandleVal
    {
        RETURN_ERROR_RECORDS, INDEX_ERROR_RECORDS;
    };
    
    public Indexer(final List<AbstractValueIndexer<?>> indexers, final SolrProxy solrProxy)
    {
        this.indexers = indexers;
        this.solrProxy = solrProxy;
    }

    public boolean isSet(eErrorHandleVal val)
    {
        return(errHandle.contains(val));
    }
    
    public void setErr(eErrorHandleVal val)
    {
        errHandle.add(val);
    }
    
    public int index(final MarcReader reader) throws Exception
    {
        int cnt = 0;
        while (reader.hasNext())
        {
            final Record record = reader.next();
            final Map<String, Object> document = index(record);
            solrProxy.addDoc(document, false, true);
            cnt++;
        }
        return(cnt);
    }
    
    /**
     * indexToSolr  - Reads in a MARC Record, produces SolrInputDocument for it, sends that document to solr
     *                This is the single threaded version that does each of those action sequentially
     * @param reader
     * @return
     * @throws Exception
     */
    public int indexToSolr(final MarcReader reader) throws Exception
    {
        int cnt = 0;
        while (reader.hasNext())
        {
            final Record record = reader.next();
 //           ErrorHandler errors = MarcReaderFactory.instance().getErrors();
            final SolrInputDocument document = indexToSolrDoc(record);
            solrProxy.addDoc(document);
            cnt++;
        }
        return(cnt);
    }
    
    private void addExceptionsToMap(SolrInputDocument document, List<IndexerSpecException> perRecordExceptions)
    {
        if (perRecordExceptions != null)
        {
            for (IndexerSpecException e : perRecordExceptions)
            {
      //          if (e.getSolrField() == null) e.setSolrFieldAndSpec("marc_error", null);
                String specMessage = e.getSpecMessage();
    //            if (!specMessage.equals(lastSpec))
    //            {
    //                text.append(specMessage);
    //            }
    //            lastSpec = specMessage;
                document.addField("marc_error", e.getMessage());
                for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
                {
                    document.addField("marc_error", e.getSolrField()+ " : " + cause.getMessage());
                }
            }
        }
    }

//    if (errors.hasErrors())
//    {
//        addErrorsToMap(fieldsMap, errors);
//    }

    
    private void addErrorsToMap(SolrInputDocument document, List<MarcError> errors)
    {
        for (Object err : errors)
        {
            document.addField("marc_error", err.toString());
        }
    }

    private Map<String, Object> index(final Record record) throws Exception 
    {
        final Map<String, Object> document = new HashMap<>();
        for (final AbstractValueIndexer<?> indexer : indexers)
        {
            try { 
                final Collection<String> data = indexer.getFieldData(record);
                for (String fieldName : indexer.getSolrFieldNames())
                {
                    if (data.size() == 0)
                    {
                        /* do_nothing() */
                    }
                    else if (data.size() == 1)
                    {
                        document.put(fieldName, data.iterator().next());
                    }
                    else
                    {
                        for (String dataVal : data)
                        {
                            document.put(fieldName, dataVal);
                        }
                    }
                }
            }
            catch (InvocationTargetException ioe)
            {
                Throwable wrapped = ioe.getTargetException();
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    document.put("marc_error", indexer.getSolrFieldNames().toString() + wrapped.getMessage());
                else throw(ioe);
            }
            catch (IllegalArgumentException e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
            catch (IndexerSpecException e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
            catch (Exception e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    document.put("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
        }
        return document;
    }
    
    protected SolrInputDocument indexToSolrDoc(final Record record) throws Exception
    {
        SolrInputDocument inputDoc = new SolrInputDocument();
        ValueIndexerFactory.instance().clearPerRecordErrors();
        for (final AbstractValueIndexer<?> indexer : indexers)
        {
            try { 
                final Collection<String> data = indexer.getFieldData(record);

                for (String fieldName : indexer.getSolrFieldNames())
                {
                    if (data.size() == 0)
                    {
                        /* do_nothing() */
                    }
                    else
                    {
                        for (String dataVal : data)
                        {
                            inputDoc.addField(fieldName, dataVal, 1.0f );
                        }
                    }
                }
            }
            catch (IllegalArgumentException e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    inputDoc.addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
            catch (IndexerSpecException e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    inputDoc.addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
            catch (InvocationTargetException ioe)
            {
                Throwable wrapped = ioe.getTargetException();
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    inputDoc.addField("marc_error", indexer.getSolrFieldNames().toString() + wrapped.getMessage());
                else throw(ioe);
            }
            catch (Exception e)
            {
                if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
                    inputDoc.addField("marc_error", indexer.getSolrFieldNames().toString() + e.getMessage());
                else throw(e);
            }
        }
        if (errHandle.contains(eErrorHandleVal.INDEX_ERROR_RECORDS))
        {
            if (record.hasErrors() )
            {
                addErrorsToMap(inputDoc, record.getErrors());
            }
            List<IndexerSpecException> perRecordExceptions = ValueIndexerFactory.instance().getPerRecordErrors();
            addExceptionsToMap(inputDoc, perRecordExceptions);
        }

        return inputDoc;
    }
}
