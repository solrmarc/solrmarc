package org.solrmarc.driver;

import java.util.EnumSet;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.marc4j.MarcError;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.tools.SolrMarcIndexerException;

/**
 * @author rh9ec
 *
 */
public class RecordAndDoc
{
    final Record rec;
    SolrInputDocument doc;
    eErrorSeverity errLvl;
    SolrMarcIndexerException smie;
    IndexerSpecException ise;
    EnumSet<eErrorLocationVal> errLocs = EnumSet.noneOf(eErrorLocationVal.class);

    public enum eErrorLocationVal
    {
        MARC_ERROR, INDEXING_ERROR, SOLR_ERROR;
    };

    /**
     * Constructor for a RecordAndDoc object for which the Doc hasn't yet been created.
     *
     * @param record - The MARC record being processed.
     */
    RecordAndDoc(Record record)
    {
        this.rec = record;
        this.doc = null;
        this.errLocs = rec.hasErrors() ? EnumSet.of(eErrorLocationVal.MARC_ERROR) : EnumSet.noneOf(eErrorLocationVal.class);
        this.errLvl = rec.hasErrors() ? errLvlForMarcError(rec.getErrors()) : eErrorSeverity.NONE;
    }

    /**
     * Used in conjunction with the above constructor to set the Doc member once it has been created.
     *
     * @param doc - The created SolrInputDocument
     */
    void setDoc(SolrInputDocument doc)
    {
        this.doc = doc;
    }

    SolrMarcIndexerException getSolrMarcIndexerException()
    {
        return smie;
    }

    void setSolrMarcIndexerException(SolrMarcIndexerException smie)
    {
        if (this.smie == null || this.smie.getLevel() < smie.getLevel())
        {
            this.smie = smie;
        }
    }

    void setIndexerSpecException(IndexerSpecException ise)
    {
        if (this.ise == null || this.ise.getErrLvl().compareTo(ise.getErrLvl()) < 0)
        {
            this.ise = ise;
        }
    }

    private static eErrorSeverity errLvlForMarcError(List<MarcError> errors)
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

    Record getRec()
    {
        return rec;
    }

    SolrInputDocument getDoc()
    {
        return doc;
    }

    void setMaxErrLvl(eErrorSeverity errLvl)
    {
        this.errLvl = eErrorSeverity.max(this.errLvl, errLvl);
    }

    eErrorSeverity getErrLvl()
    {
        return errLvl;
    }

    void addErrLoc(eErrorLocationVal solrError)
    {
        errLocs.add(solrError);
    }
}
