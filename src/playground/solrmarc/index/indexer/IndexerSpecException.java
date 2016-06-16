package playground.solrmarc.index.indexer;

import java.util.ArrayList;
import java.util.List;

//import playground.solrmarc.index.specification.ErrorSpecification;

public class IndexerSpecException extends RuntimeException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

//    ErrorSpecification errMsgs;
    //Throwable cause;
    String solrField;
    String spec;
    String message;

//    public IndexerSpecException(String solrField, ErrorSpecification errMsgs)
//    {
//        this.errMsgs = errMsgs;
//        this.solrField = solrField;
//        this.spec = null;
//        this.message = null;
//    }

    public IndexerSpecException(Throwable cause, String solrField, String spec, String message)
    {
//        this.errMsgs = null;
        this.initCause(cause);
        this.solrField = solrField;
        this.spec = spec;
        this.message = message;

    }

    public IndexerSpecException(String solrField, String spec, String message)
    {
//        this.errMsgs = null;
   //     this.cause = null;
        this.solrField = solrField;
        this.spec = spec;
        this.message = message;

    }
    
    public IndexerSpecException(String solrFieldAndSpec, String message)
    {
//        this.errMsgs = null;
 //      this.cause = null;
        String[] tmp = solrFieldAndSpec.split("[ ]*=[ ]*", 2);
        this.solrField = tmp[0];
        this.spec = tmp[1];
        this.message = message;

    }

//    public IndexerSpecException(ErrorSpecification errMsgs)
//    {
//        this.errMsgs = errMsgs;
//        this.solrField = null;
//        this.spec = null;
//        this.message = null;
//    }

    public IndexerSpecException(Throwable cause, String message)
    {
//        this.errMsgs = null;
        this.initCause(cause);
        this.solrField = null;
        this.spec = null;
        this.message = message;

    }
    
    public IndexerSpecException(String message)
    {
//        this.errMsgs = null;
     //   this.cause = null;
        this.solrField = null;
        this.spec = null;
        this.message = message;

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
}
