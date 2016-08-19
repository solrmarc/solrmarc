package org.solrmarc.index.indexer;

/**
 * Exception handler for Solrmarc
 * 
 * @author Robert Haschart
 * @version $Id: SolrMarcIndexerException.java $
 *
 */
public class SolrMarcIndexerException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private transient Throwable cause;

    public final static int IGNORE = 1; // STOP_INDEXING_RECORD_AND_IGNORE
    public final static int DELETE = 2; // STOP_INDEXING_RECORD_AND_DELETE
    public final static int EXIT = 3; // STOP_PROCESSING_INPUT_AND_TERMINATE
    private int level;

    /**
     * Default constructor
     */
    public SolrMarcIndexerException(int level)
    {
        super();
        this.setLevel(level);
    }

    /**
     * Constructs with message.
     * 
     * @param message Message to pass
     */
    public SolrMarcIndexerException(int level, final String message)
    {
        super(message);
        this.setLevel(level);
    }

    /**
     * Constructs with chained exception
     * 
     * @param cause Chained exception
     */
    public SolrMarcIndexerException(int level, final Throwable cause)
    {
        super(cause.toString());
        this.cause = cause;
        this.setLevel(level);
    }

    /**
     * Constructs with message and exception
     * 
     * @param message Message
     * @param cause  Exception
     */
    public SolrMarcIndexerException(int level, final String message, final Throwable cause)
    {
        super(message, cause);
        this.cause = cause;
        this.setLevel(level);
    }

    /**
     * Get the current exception
     * 
     * @return Throwable cause of the exception
     */
    public Throwable getException()
    {
        return cause;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getLevel()
    {
        return level;
    }

}
