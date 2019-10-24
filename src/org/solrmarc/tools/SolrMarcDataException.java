package org.solrmarc.tools;

/**
 * Exception handler for Solrmarc
 * 
 * @author Robert Haschart
 * @version $Id: SolrMarcDataException.java $
 *
 */
public class SolrMarcDataException extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;
    private transient Throwable cause;
    eDataErrorLevel level;

    public enum eDataErrorLevel  {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL;

        public static eDataErrorLevel max(eDataErrorLevel errLvl1, eDataErrorLevel errLvl2)
        {
            if (errLvl1.compareTo(errLvl2) > 0)
                return(errLvl1);
            else
                return (errLvl2);
        }
    };

    /**
     * Constructs with message.
     * 
     * @param message Message to pass
     */
    public SolrMarcDataException(eDataErrorLevel level, final String message)
    {
        super(message);
        this.setLevel(level);
    }

    /**
     * Constructs with chained exception
     * 
     * @param cause Chained exception
     */
    public SolrMarcDataException(eDataErrorLevel level, final Throwable cause)
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
    public SolrMarcDataException(eDataErrorLevel level, final String message, final Throwable cause)
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

    public void setLevel(eDataErrorLevel level)
    {
        this.level = level;
    }

    public eDataErrorLevel getLevel()
    {
        return level;
    }

}
