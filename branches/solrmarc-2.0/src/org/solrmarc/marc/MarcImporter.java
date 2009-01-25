package org.solrmarc.marc;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.*;
import org.marc4j.ErrorHandler;
import org.marc4j.marc.Record;
import org.solrmarc.solr.SolrCoreProxy;
import org.solrmarc.solr.SolrCoreLoader;
import org.solrmarc.tools.Utils;


/**
 * @author Wayne Graham (wsgrah@wm.edu)
 * @version $Id$
 *
 */
public class MarcImporter extends MarcHandler 
{	
	/** needs to be visible to StanfordCallnumMarcImporter ... */
    protected SolrCoreProxy solrCoreProxy;

    private String solrCoreDir;
    private String solrDataDir;
    private String deleteRecordListFilename;
    private String deleteRecordIDMapper = null;
    private String SolrHostURL;
    private boolean optimizeAtEnd = true;
    private boolean shuttingDown = false;
    private boolean isShutDown = false;
    private int recsReadCounter = 0;
    private int recsIndexedCounter = 0;
    private int idsToDeleteCounter = 0;
    private int recsDeletedCounter = 0;
    
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcImporter.class.getName());
    

    /**
	 * Constructs an instance with properties files
	 */
    public MarcImporter(String args[])
    {
        // Process Properties in super class MarcHandler
    	super(args);
    	
    	loadLocalProperties(configProps);
        // Set up Solr core
        solrCoreProxy = SolrCoreLoader.loadCore(solrCoreDir, solrDataDir, null, logger);
	}
	
    /**
	 * Load the properties file
	 * @param properties
	 */
	public void loadLocalProperties(Properties props) 
	{
        // The solr.home directory
        solrCoreDir = Utils.getProperty(props, "solr.path");

        // The solr data diretory to use
        solrDataDir = Utils.getProperty(props, "solr.data.dir");

        // Ths URL of the currently running Solr server
        SolrHostURL = Utils.getProperty(props, "solr.hosturl");
        
        // Specification of how to modify the entries in the delete record file
        // before passing the id onto Solr.   Based on syntax of String.replaceAll
        //  To prepend a 'u' specify the following:  "(.*)->u$1"
        deleteRecordIDMapper = Utils.getProperty(props, "marc.delete_record_id_mapper");
        if (deleteRecordIDMapper != null)
        {
            String parts[] = deleteRecordIDMapper.split("->");
            if (parts.length == 2)
            {
                String mapPattern = parts[0];
                String mapReplace = parts[1];
                try {
                    String testID = "12345";
                    String tested = testID.replaceFirst(mapPattern, mapReplace);
                    logger.info("Valid Regex pattern specified in property: marc.delete_record_id_mapper");
                }
                catch (PatternSyntaxException pse)                
                {
                    deleteRecordIDMapper = null;
                    logger.warn("Invalid Regex pattern specified in property: marc.delete_record_id_mapper");
                }
            }
            else
            {
                deleteRecordIDMapper = null;
                logger.warn("Invalid Regex pattern specified in property: marc.delete_record_id_mapper");
            }
        }

        deleteRecordListFilename = Utils.getProperty(props, "marc.ids_to_delete");
        optimizeAtEnd = Boolean.parseBoolean(Utils.getProperty(props, "solr.optimize_at_end"));
        return;
	}

    /**
     * Delete records from the index
     * @return Number of records deleted
     */
    public int deleteRecords()
    {
    	idsToDeleteCounter = 0;
    	recsDeletedCounter = 0;
    	
        if (deleteRecordListFilename == null || 
            deleteRecordListFilename.length() == 0) 
        {
            return recsDeletedCounter;
        }
        String mapPattern = null;
        String mapReplace = null;
        if (deleteRecordIDMapper != null)
        {
            String parts[] = deleteRecordIDMapper.split("->");
            if (parts.length == 2)
            {
                mapPattern = parts[0];
                mapReplace = parts[1];
            }
        }
        File delFile = new File(deleteRecordListFilename);
        try
        {
            BufferedReader is = new BufferedReader(new FileReader(delFile));
            String line;
            boolean fromCommitted = true;
            boolean fromPending = true;
            while ((line = is.readLine()) != null)
            {
                if (shuttingDown) break;
                line = line.trim();
                if (line.startsWith("#")) continue;
                if (deleteRecordIDMapper != null)
                {
                    line = line.replaceFirst(mapPattern, mapReplace);
                }                
                String id = line;
                idsToDeleteCounter++;
                solrCoreProxy.delete(id, fromCommitted, fromPending);
                recsDeletedCounter++;
            }            
        }
        catch (FileNotFoundException fnfe)
        {
        	logger.error("Error: unable to find and open delete-record-id-list: " + delFile, fnfe);
        }
        catch (IOException ioe)
        {
        	logger.error("Error: reading from delete-record-id-list: " + delFile, ioe);
        }
        return recsDeletedCounter;
    }

    /**
     * Iterate over the marc records in the file and add them to the index
     * @return Number of records indexed
     */
    public int importRecords()
    {
        // keep track of record counts
        recsReadCounter = 0;
        recsIndexedCounter = 0;
        
        while(reader != null && reader.hasNext())
        {
            if (shuttingDown) break;
            
            try {
                Record record = reader.next();
                recsReadCounter++;
                
                try {
                    addToIndex(record);
                    recsIndexedCounter++;
                    logger.info("Added record " + recsReadCounter + " read from file: " + record.getControlNumber());
                }
                catch (Exception e)
                {
                    // check for missing fields
                    if (solrCoreProxy.isSolrException(e) &&
                    		e.getMessage().contains("missing required fields"))
                    {
                   	   logger.error(e.getMessage() +  " at record count = " + recsReadCounter);
                   	   logger.error("Control Number " + record.getControlNumber(), e);
                    }
                    else
                    {
                	    logger.error("Error indexing record: " + record.getControlNumber() + " -- " + e.getMessage(), e);
                    }
                }
            } 
            catch (Exception e) {
                logger.error("Error reading record: " + e.getMessage(), e);
            }
        }
        
        return recsIndexedCounter;
    }
    
    /**
     * Add a record to the index
     * @param record marc record to add
     */
    private void addToIndex(Record record)
    	throws IOException
    {
        Map<String, Object> map = indexer.map(record); 
        if (map.size() == 0) return;
        if (errors != null && includeErrors)
        {
            if (errors.hasErrors())
            {
                addErrorsToMap(map, errors);
            }
        }

        // NOTE: exceptions are dealt with by calling class
//        try {
            String docStr = solrCoreProxy.addDoc(map, verbose);
            if (verbose)
            {
                logger.info(record.toString());
                logger.info(docStr);
            }
/*
            
        } 
        catch (Exception e) 
        {
            //System.err.println("Couldn't add document");
        	logger.error("Couldn't add document: " + e.getMessage());
            //e.printStackTrace();
        	logger.error("Control Number " + record.getControlNumber(), e);
        }                
*/
    }

    private void addErrorsToMap(Map<String, Object> map, ErrorHandler errors2)
    {
        map.put("marc_error", errors.getErrors());
    }

    /**
     * 
     */
    public void finish()
    {
        try {
            //System.out.println("Calling commit");
        	logger.info("Calling commit");
        	logger.info(" Adding " + recsIndexedCounter + " of " + recsReadCounter + " documents to index");
        	logger.info(" Deleting " + recsDeletedCounter + " documents from index");
            solrCoreProxy.commit(shuttingDown ? false : optimizeAtEnd);
        } 
        catch (IOException ioe) {
            //System.err.println("Final commit and optmization failed");
        	logger.error("Final commit and optimization failed: " + ioe.getMessage());
        	logger.debug(ioe);
            //e.printStackTrace();
        }
        
        //System.out.println("Done with commit, closing Solr");
        logger.info("Done with the commit, closing Solr");
        solrCoreProxy.close();
    }
    
   
   /**
     * If there is a running Solr server instance looking at the same index
     * that is being updated by this process, this function can be used to signal 
     * that server that the indexes have changed, so that it will find the new data
     * with out having to be restarted.
     * 
     * uses member variable SolrHostURL which contains the URL of the Solr server
     * for example:    http://localhost:8983/solr/update
     * This value is taken from the  solr.hosturl  entry in the properties file. 
     */
    
    private void signalServer()
    {
        if (shuttingDown) return;
        if (SolrHostURL == null || SolrHostURL.length() == 0) return;
        try {
            URL         url;
            URLConnection   urlConn;
            DataOutputStream    printout;
            BufferedReader input;

            // URL of CGI-Bin script.
            url = new URL (SolrHostURL);

            // URL connection channel.
            urlConn = url.openConnection();

            // Let the run-time system (RTS) know that we want input.
            urlConn.setDoInput (true);

            // Let the RTS know that we want to do output.
            urlConn.setDoOutput (true);

            // No caching, we want the real thing.
            urlConn.setUseCaches (false);

            // Specify the content type.
            urlConn.setRequestProperty("Content-Type", "text/xml");
            urlConn.setRequestProperty("charset", "utf-8");

            // Send POST output.
            printout = new DataOutputStream (urlConn.getOutputStream ());

            String content = "<commit/>";
             
            printout.writeBytes (content);
            printout.flush ();
            printout.close ();

            // Get response data.
            input = new BufferedReader(new InputStreamReader(urlConn.getInputStream ()));

            String str;
            while (null != ((str = input.readLine())))
            {
                System.out.println (str);
            }

            input.close ();

            // Display response.
         }
        catch (MalformedURLException me)
        {
            //System.err.println("MalformedURLException: " + me);
        	logger.error("MalformedURLException" + me);
        }
        catch (IOException ioe)
        {
            //System.err.println("IOException: " + ioe.getMessage());
        	logger.error("IOException: " + ioe.getMessage());
        }
    }  

    /**
     * Set the shutdown flag
     */
    public void shutDown()
    {
        shuttingDown = true;
    }
    
    
    class MyShutdownThread extends Thread 
    {
        MarcImporter importer;
        public MyShutdownThread(MarcImporter im)
        {
            importer = im;
        }
        public void run()
        {
            //System.err.println("Starting Shutdown hook");
        	logger.info("Starting Shutdown hook");
            
        	if (!importer.isShutDown) 
            {
                importer.shutDown();
            }
            while (!importer.isShutDown) 
            {
                try
                {
                    sleep(2000);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            logger.info("Finished Shutdown hook");
           // System.err.println("Finished Shutdown hook");
        }
    }
    
    
    /**
     * Main loop in the MarcImporter class the handles all of 
     * importing and deleting of records.
     * @param args
     */
    @Override
	public int handleAll()
    {
        Runtime.getRuntime().addShutdownHook(new MyShutdownThread(this));
        
        //System.out.println("Here we go...");
        
        Date start = new Date();
        
        int numImported = 0;
        int numDeleted = 0;
        try
        {        
            numImported = importRecords();           
            numDeleted = deleteRecords();
        }
        catch (Exception e)
        {
            logger.info("Exception occurred while Indexing: "+ e.getMessage());           
        }
        
        finish();
        
        signalServer();
        
        isShutDown = true;
        
        Date end = new Date();
        
        long totalTime = end.getTime() - start.getTime();
        
        logger.info("Finished indexing in " + Utils.calcTime(totalTime));
        //System.out.println("Finished in " + Utils.calcTime(totalTime) );
        
        // calculate the time taken
        float indexingRate = numImported*1000 / totalTime;
        
        //System.out.println("Indexed " + numImported + " at a rate of about " + indexingRate + "per sec");
        //System.out.println("Deleted " + numDeleted + " records");
        
        logger.info("Indexed " + numImported + " at a rate of about " + indexingRate + " per sec");
        logger.info("Deleted " + numDeleted + " records");
        
        return(shuttingDown ? 1 : 0);
    }
    
    /**
     * Main program instantiation for doing the indexing
     * @param args
     */
    public static void main(String[] args) 
    {
        logger.info("Starting SolrMarc indexing.");
    	// default properties file
        
        MarcImporter importer = null;
        try
        {
            importer = new MarcImporter(args);
        }
        catch (IllegalArgumentException e)
        {
        	logger.error(e.getMessage());
        	System.err.println(e.getMessage());
            //e.printStackTrace();
            System.exit(1);
        }
        
        int exitCode = importer.handleAll();
        System.exit(exitCode);
    }


 		
}