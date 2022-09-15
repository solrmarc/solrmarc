package org.solrmarc.index.indexer;

public class IndexerSpecException extends RuntimeException
{
    public enum eErrorSeverity
    {
        NONE, INFO, WARN, ERROR, FATAL;

        public static eErrorSeverity max(eErrorSeverity errLvl1, eErrorSeverity errLvl2)
        {
            if (errLvl1.compareTo(errLvl2) > 0)
                return(errLvl1);
            else
                return (errLvl2);
        }
    };

    private static final long serialVersionUID = 1L;

//    ErrorSpecification errMsgs;
    //Throwable cause;
    String solrField;
    String spec;
    eErrorSeverity errLvl;
    String message;

    public IndexerSpecException(Throwable cause, String solrField, String spec, eErrorSeverity errLvl, String message)
    {
        init(cause, solrField, spec, errLvl, message);
    }

    private void init(Throwable cause, String solrField, String spec, eErrorSeverity errLvl, String message)
    {
        this.initCause(cause);
        this.solrField = solrField;
        this.spec = spec;
        this.message = message;
        this.errLvl = errLvl;
    }

    public IndexerSpecException(String solrField, String spec, eErrorSeverity errLvl, String message)
    {
        init(null, solrField, spec, errLvl, message);
    }

    public IndexerSpecException(String solrField, String spec, String message)
    {
        init(null, solrField, spec, eErrorSeverity.ERROR, message);
    }

    public IndexerSpecException(String solrFieldAndSpec, eErrorSeverity errLvl, String message)
    {
        final String[] tmp = solrFieldAndSpec.split("[ ]*=[ ]*", 2);
        init(null, (tmp.length >= 1) ? tmp[0] : null, (tmp.length == 2) ? tmp[1] : null, errLvl, message);
    }

    public IndexerSpecException(String solrFieldAndSpec, String message)
    {
        final String[] tmp = solrFieldAndSpec.split("[ ]*=[ ]*", 2);
        init(null, (tmp.length >= 1) ? tmp[0] : null, (tmp.length == 2) ? tmp[1] : null, eErrorSeverity.ERROR, message);
    }

    public IndexerSpecException(Throwable cause, eErrorSeverity errLvl, String message)
    {
        init(cause, null, null, errLvl, message);
    }

    public IndexerSpecException(Throwable cause, String message)
    {
        init(cause, null, null, eErrorSeverity.ERROR, message);
    }

    public IndexerSpecException(eErrorSeverity errLvl, String message)
    {
        init(null, null, null, errLvl, message);
    }

    public IndexerSpecException(String message)
    {
        init(null, null, null, eErrorSeverity.ERROR, message);
    }

    public void setSolrFieldAndSpec(String solrField, String spec)
    {
        this.solrField = solrField;
        this.spec = spec;
    }

    public String message()
    {
        return message;
    }

    public String getSolrField()
    {
        return solrField;
    }

    public String getSpecMessage()
    {
        StringBuilder build = new StringBuilder();
        if (solrField != null && spec != null)
        {
            build.append(solrField).append(" = ").append(spec);
        }
        return(build.toString());
    }

    @Override
    public String getMessage()
    {
        StringBuilder build = new StringBuilder();
        if (message != null)
        {
            if (solrField != null)
                build.append(solrField).append(" : ").append(message);
            else
                build.append(message);
        }
        return (build.toString());
    }

    public void setSolrFieldAndSpec(final String solrFieldAndSpec)
    {
        String[] tmp = solrFieldAndSpec.split("[ ]*=[ ]*", 2);
        this.solrField = tmp[0];
        this.spec = tmp[1];
    }

    public eErrorSeverity getErrLvl()
    {
        return errLvl;
    }

    public void setErrLvl(eErrorSeverity errLvl)
    {
        this.errLvl = errLvl;
    }
}
