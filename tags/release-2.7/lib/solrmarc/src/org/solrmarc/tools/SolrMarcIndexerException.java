package org.solrmarc.tools;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Exception handler for Solrmarc
 * @author Robert Haschart
 * @version $Id: SolrMarcIndexerException.java $
 *
 */
public class SolrMarcIndexerException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private transient Throwable cause;

	public final static int IGNORE = 1; // STOP_INDEXING_RECORD_AND_IGNORE
	public final static int DELETE = 2; // STOP_INDEXING_RECORD_AND_DELETE
	public final static int EXIT = 3;   // STOP_PROCESSING_INPUT_AND_TERMINATE
	private int level;
	private Map<String, Object> indexMap = null;
	
    public final static String fieldNameIgnore = "solrmarcignorerecord";
    public final static String fieldNameDelete = "solrmarcdeleterecord";
    public final static String fieldNameExit = "solrmarcexitrecord";
	
    public static String getLevelFieldName(int level)
	{
	   if (level == IGNORE) return(fieldNameIgnore); 
	   else if (level == DELETE) return(fieldNameDelete); 
	   else if (level == EXIT) return(fieldNameExit); 
	   return(null);
	}
    
	/**
	 * Default constructor
	 */
	public SolrMarcIndexerException(int level, Map<String, Object> indexMap) 
	{
		super();
		this.indexMap = indexMap;
		this.setLevel(level);
	}

    /**
     * Constructs with message.
     * @param message Message to pass
     */
    public SolrMarcIndexerException(int level, Map<String, Object> indexMap, final String message) {
        super(message);
        this.indexMap = indexMap;
        this.setLevel(level);
    }
    
    /**
     * Constructs with message.
     * @param message Message to pass
     */
    public SolrMarcIndexerException(int level, final String message) {
        super(message);
        this.indexMap = null;
        this.setLevel(level);
    }

	/**
	 * Constructs with chained exception
	 * @param cause Chained exception
	 */
	public SolrMarcIndexerException(int level, Map<String, Object> indexMap, final Throwable cause) {
		super(cause.toString());
		this.cause = cause;
        this.indexMap = indexMap;
		this.setLevel(level);
	}

	/**
	 * Constructs with message and exception
	 * @param message Message
	 * @param cause Exception
	 */
	public SolrMarcIndexerException(int level, Map<String, Object> indexMap, final String message, final Throwable cause) {
		super(message, cause);
		this.cause = cause;
        this.indexMap = indexMap;
		this.setLevel(level);
	}
	
    /**
     * Get the index map that was generated for thge record where the exception was thrown
     * @return Map<String, Object> the generated index map
     */
    public Map<String, Object> getIndexMap()
    {
        return indexMap;
    }
	
	/**
	 * Get the current exception
	 * @return Throwable cause of the exception
	 */
	public Throwable getException(){
		return cause;
	}
	
	/**
	 * Print a message 
	 * @param message Message to print
	 */
	public void printMessage(final String message){
		System.err.println(message);
	}
	
	/**
	 * Print stack trace for the current exception
	 */
	public void printStackTrace(){
		printStackTrace(System.err);
	}
	
	/**
	 * Print the stack trace for a nested exception
	 * @param printStream PrintStream to print stack trace for
	 */
	public void printStackTrace(final PrintStream printStream){
		synchronized(printStream){
			super.printStackTrace();
			if(cause != null){
				printStream.println("--- Nested Exception ---");
				cause.printStackTrace(printStream);
			}
		}
	}
	
	/**
	 * Print the nested stack trace for nested PrintWriter exceptions
	 * @param printWriter PrintWriter to print stack trace for
	 */
	public void printStrackTrace(final PrintWriter printWriter){
		synchronized(printWriter){
			super.printStackTrace(printWriter);
			if(cause != null){
				printWriter.println("--- Nested Exception ---");
				cause.printStackTrace(printWriter);
			}
		}
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

}
