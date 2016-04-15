package playground.solrmarc.index.indexer;

import java.util.ArrayList;
import java.util.List;

import playground.solrmarc.index.specification.ErrorSpecification;

public class IndexerSpecException extends RuntimeException 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ErrorSpecification errMsgs;
	String solrField;
	String spec;
	String message;
	
	public IndexerSpecException(String solrField, ErrorSpecification errMsgs)
	{
		this.errMsgs = errMsgs;
		this.solrField = solrField;
		this.spec = null;
		this.message = null;
	}
	
	public IndexerSpecException(String solrField, String spec, String message)
	{
		this.errMsgs = null;
		this.solrField = solrField;
		this.spec = spec;
		this.message = message;

	}
	
	public IndexerSpecException(ErrorSpecification errMsgs)
	{
		this.errMsgs = errMsgs;
		this.solrField = null;
		this.spec = null;
		this.message = null;
	}
	
	public IndexerSpecException(String message)
	{
		this.errMsgs = null;
		this.solrField = null;
		this.spec = null;
		this.message = message;

	}
	
	public void setSolrFieldAndSpec(String solrField, String spec)
	{
		this.solrField = solrField;
		this.spec = spec;		
	}
	
	@Override
	public String getMessage()
	{
		StringBuilder build = new StringBuilder();
		build.append(solrField).append(" = ").append(spec).append("\n");
		if(errMsgs != null)
		{
			List<String> msgs = new ArrayList<String>();
			errMsgs.addFieldValues(msgs,  null);
			for (String msg : msgs)
			{
				build.append(solrField).append(" : ").append(msg).append("\n");
			}
		}
		if (message != null)
		{
			build.append(solrField).append(" : ").append(message).append("\n");
		}
		return(build.toString());
	}
}
