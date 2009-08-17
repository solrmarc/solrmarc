package org.solrmarc.index;

import java.util.*;

//import org.apache.log4j.Logger;

import org.marc4j.MarcException;
import org.marc4j.marc.Record;

import org.solrmarc.marc.MarcHandler;

/**
 * Reads in marc records and creates mapping of solr field names to solr field
 *  values per configuration files.  Only creates the mapping;  does not
 *  write out to file or to index.
 *  
 * based on org.solrmarc.marc.MarcPrinter by Bob Haschart
 *   
 * @author Naomi Dushay
 * @version $Id$
 */
public class MarcMappingTest extends MarcHandler
{
//    static Logger logger = Logger.getLogger(MarcMappingTest.class.getName());
    
    /** name of unique key field in solr document */
    private String idFldName = null;

    /**
     * Constructor 
     * @param args - array of Strings:
     *    arg[0] - name of xxx_config.properties file
     *    arg[1] - name of unique key field in solr document
     */
    public MarcMappingTest(String args[])
    {
        super(args);
        if (args.length < 2) {
        	throw new RuntimeException("Must have at least 2 arguments for MarcMappingTest: name of xxx_config.properties file and the name of the unique key field in the solr document");                              
        }
        idFldName = args[1];
    }
    
    
    /**
     * read in the file of marc records indicated, looking for the desired 
     *  record, and returning the mapping of solr field names to values.
     * @param desiredRecId - value for solr id field
     * @param mrcFileName - absolute path of file of marc records (name must end in .mrc or .marc or .xml)
     * @return a mapping of solr field names to solr field values (as Objects 
     *  that are Strings or Collections of Strings)
     */
    public Map<String,Object> getIndexMapForRecord(String desiredRecId, String mrcFileName) 
    {
    	loadReader("FILE", mrcFileName);
        while(reader != null && reader.hasNext())
        {
        	try 
        	{
                Record record = reader.next();
                
                Map<String,Object> solrFldName2ValMap = indexer.map(record, errors);
                if (errors != null && includeErrors && errors.hasErrors())
                   solrFldName2ValMap.put("marc_error", errors.getErrors());
                // FIXME:  
                Object thisRecId = solrFldName2ValMap.get(idFldName);
                if (thisRecId.equals(desiredRecId))
                	return solrFldName2ValMap;
        	}
            catch (MarcException me)
            {
                System.err.println("Error reading Marc Record: "+ me.getMessage());                              
            }        
        }
        return null;
    }

    
    @Override
    /**
     * this method is required, though we don't use it here.
     */
    public int handleAll() 
    {
        return 0;
    }

}
