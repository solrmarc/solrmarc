package org.solrmarc.index.driver;

import java.util.EnumSet;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.marc4j.MarcError;
import org.marc4j.marc.Record;
import org.solrmarc.index.driver.RecordAndDocError.eErrorLocationVal;

public class RecordAndDocError
{
    final Record rec;
    SolrInputDocument doc;
    eErrorSeverity errLvl;
    public EnumSet<eErrorLocationVal> errLocs = EnumSet.noneOf(eErrorLocationVal.class);
    
    public enum eErrorLocationVal
    {
        MARC_ERROR, INDEXING_ERROR, SOLR_ERROR;
    };

    public enum eErrorSeverity
    {
        NONE, INFO, WARN, ERROR, FATAL
    };

    public RecordAndDocError(Record rec, SolrInputDocument doc, EnumSet<eErrorLocationVal> errLocs, eErrorSeverity errLvl)
    {
        this.rec = rec;
        this.doc = doc;
        this.errLocs = errLocs;
        this.errLvl = errLvl;        
    }
    
    public RecordAndDocError(Record rec, SolrInputDocument doc, eErrorLocationVal errLoc, eErrorSeverity errLvl)
    {
        this.rec = rec;
        this.doc = doc;
        this.errLocs = EnumSet.of(errLoc);
        this.errLvl = errLvl;        
    }

    public RecordAndDocError(Record record)
    {
        this.rec = record;
        this.doc = null;
        this.errLocs = rec.hasErrors() ? EnumSet.of(eErrorLocationVal.MARC_ERROR) : EnumSet.noneOf(eErrorLocationVal.class);
        this.errLvl = rec.hasErrors() ? errLvlForMarcError(rec.getErrors()) : eErrorSeverity.NONE;
    }

    private eErrorSeverity errLvlForMarcError(List<MarcError> errors)
    {
        int maxSeverity = -1;
        for (MarcError err : errors)
        {
            if (err.severity > maxSeverity) maxSeverity = err.severity;
        }
        if (maxSeverity == -1) return(eErrorSeverity.NONE);
        switch (maxSeverity) {
            case MarcError.INFO:        return eErrorSeverity.INFO;
            case MarcError.ERROR_TYPO:  return eErrorSeverity.WARN;
            case MarcError.MINOR_ERROR: return eErrorSeverity.WARN;
            case MarcError.MAJOR_ERROR: return eErrorSeverity.ERROR;
            case MarcError.FATAL:       return eErrorSeverity.FATAL;
            default:                    return eErrorSeverity.NONE;
        }
    }

    public EnumSet<eErrorLocationVal> getErrLocs()
    {
        return errLocs;
    }

    public void setErrLocs(EnumSet<eErrorLocationVal> errLocs)
    {
        this.errLocs = errLocs;
    }

    public Record getRec()
    {
        return rec;
    }

    public SolrInputDocument getDoc()
    {
        return doc;
    }

    public eErrorSeverity getErrLvl()
    {
        return errLvl;
    }

    public void addErrLoc(eErrorLocationVal solrError)
    {
        errLocs.add(solrError);
    }
    
    
}
