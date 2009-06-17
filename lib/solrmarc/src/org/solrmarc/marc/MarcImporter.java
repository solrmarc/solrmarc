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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.*;
import org.marc4j.ErrorHandler;
import org.marc4j.marc.Record;
import org.solrmarc.solr.SolrCoreProxy;
import org.solrmarc.solr.SolrCoreLoader;
import org.solrmarc.solr.SolrRuntimeException;
import org.solrmarc.tools.SolrUpdate;
import org.solrmarc.tools.Utils;


/**
 * @author Wayne Graham (wsgrah@wm.edu)
 * @version $Id$
 *
 */
public class MarcImporter extends MarcHandler 
{	
	/** needs to be visible to StanfordItemMarcImporter ... */
    protected SolrCoreProxy solrCoreProxy;

    protected String solrCoreDir;
    protected String solrDataDir;
    protected String solrCoreName;
    private String deleteRecordListFilename;
    private String deleteRecordIDMapper = null;
    private String SolrHostURL;
    protected boolean optimizeAtEnd = false;
    protected boolean shuttingDown = false;
    protected boolean isShutDown = false;
    protected boolean justIndexDontAdd = false;
    private int recsReadCounter = 0;
    private int recsIndexedCounter = 0;
    private int idsToDeleteCounter = 0;
    private int recsDeletedCounter = 0;
    
    // Initialize logging category
    protected static Logger logger = Logger.getLogger(MarcImporter.class.getName());
    

    /**
	 * Constructs an instance with properties files
	 */
    public MarcImporter(String args[])
    {
        // Process Properties in super class MarcHandler
    	super(args);
    	
    	loadLocalProperties(configProps);
        // Set up Solr core
        solrCoreProxy = getSolrCoreProxy();
	}
    	
    /**
	 * Load the properties file
	 * @param properties
	 */
	private void loadLocalProperties(Properties props) 
	{
        // The solr.home directory
        solrCoreDir = Utils.getProperty(props, "solr.path");

        // The solr data diretory to use
        solrDataDir = Utils.getProperty(props, "solr.data.dir");

        // The name of the solr core to use, in a solr multicore environment
        solrCoreName = Utils.getProperty(props, "solr.core.name");
        
        // Ths URL of the currently running Solr server
        SolrHostURL = Utils.getProperty(props, "solr.hosturl");
        
        String solrLogLevel = Utils.getProperty(props, "solr.log.level");
        
        Level level = Level.WARNING;
        if (solrLogLevel != null)
        {
            if (solrLogLevel.equals("OFF"))     level = Level.OFF;
            if (solrLogLevel.equals("SEVERE"))  level = Level.SEVERE;
            if (solrLogLevel.equals("WARNING")) level = Level.WARNING;
            if (solrLogLevel.equals("INFO"))    level = Level.INFO;
            if (solrLogLevel.equals("FINE"))    level = Level.FINE;
            if (solrLogLevel.equals("FINER"))   level = Level.FINER;
            if (solrLogLevel.equals("FINEST"))  level = Level.FINEST;
            if (solrLogLevel.equals("ALL"))     level = Level.ALL;
        }
        
        java.util.logging.Logger.getLogger("org.apache.solr").setLevel(level);

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
        
        justIndexDontAdd = Boolean.parseBoolean(Utils.getProperty(props, "marc.just_index_dont_add"));
        if (justIndexDontAdd)
            Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
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
            
            Record record = null;
            try {
                record = reader.next();
                recsReadCounter++;
            }
            catch (Exception e) 
            {
                logger.error("Error reading record: " + e.getMessage(), e);
                continue;
            }
                
            try {
                boolean added = addToIndex(record);
                if (added)
                {
                    recsIndexedCounter++;
                    logger.info("Added record " + recsReadCounter + " read from file: " + record.getControlNumber());
                }
                else
                {
                    logger.info("Deleted record " + recsReadCounter + " read from file: " + record.getControlNumber());                        
                }
            }
            catch (Exception e)
            {
                Throwable cause = null;
                if (e instanceof SolrRuntimeException) 
                {
                    cause = e.getCause();
                }
                if (cause != null && cause instanceof InvocationTargetException)
                {
                    cause = ((InvocationTargetException)cause).getTargetException();
                }
                if (cause instanceof Exception && solrCoreProxy.isSolrException((Exception)cause) &&
                        cause.getMessage().contains("missing required fields"))
                {
                   // this is caused by a bad record - one missing required fields (duh)
                   logger.error(cause.getMessage() +  " at record count = " + recsReadCounter);
                   logger.error("Control Number " + record.getControlNumber());
                }
                else if (cause instanceof Exception && solrCoreProxy.isSolrException((Exception)cause) &&
                        cause.getMessage().contains("multiple values encountered for non multiValued field"))
                {
                   logger.error(cause.getMessage() +  " at record count = " + recsReadCounter);
                   logger.error("Control Number " + record.getControlNumber());
                }
                else if (cause instanceof Exception && solrCoreProxy.isSolrException((Exception)cause) )
                {
                    logger.error("Error indexing record: " + record.getControlNumber() + " -- " + cause.getMessage());
                    if (e instanceof SolrRuntimeException) throw (new SolrRuntimeException(cause.getMessage(), (Exception)cause));
                }
                else
                {
            	    logger.error("Error indexing record: " + record.getControlNumber() + " -- " + e.getMessage(), e);
            	    // this error should (might?) only be thrown if we can't write to the index
            	    //   therefore, continuing to index would be pointless.
            	    if (e instanceof SolrRuntimeException) throw ((SolrRuntimeException)e);

                }
            }
        }
        
        return recsIndexedCounter;
    }
    
    /**
     * Add a record to the index
     * @param record marc record to add
     */
    private boolean addToIndex(Record record)
    	throws IOException
    {
        Map<String, Object> fieldsMap = indexer.map(record, errors); 
        // test whether some indexing specification determined that this record should be omitted entirely
        if (fieldsMap.size() == 0) 
        {
            // if so not only don't index it, but try to delete the record if its already there.
            String id = record.getControlNumber();
            if (id != null)
            {
                solrCoreProxy.delete(id, true, true);
            }
            return false;
        }
        
        String docStr = addToIndex(fieldsMap);

        if (verbose || justIndexDontAdd)
        {
            if (verbose) 
            {
                System.out.println(record.toString());
                logger.info(record.toString());
            }
            System.out.println(docStr);
            logger.info(docStr);
        }
        return(true);
    }
    
    /**
     * Add a document to the index according to the fields map
     * @param record marc record to add
     * @return the document added, as a String
     */
    protected String addToIndex(Map<String, Object> fieldsMap)
        throws IOException
    {
        if (fieldsMap.size() == 0) 
            return null;
        if (errors != null && includeErrors)
        {
            if (errors.hasErrors())
            {
                addErrorsToMap(fieldsMap, errors);
            }
        }

        // NOTE: exceptions are dealt with by calling class
        return solrCoreProxy.addDoc(fieldsMap, verbose, !justIndexDontAdd);
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
        solrCoreProxy = null;
        logger.info("Setting Solr closed flag");
        isShutDown = true;
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
    
    protected void signalServer()
    {
        if (shuttingDown) return;
        if (SolrHostURL == null || SolrHostURL.length() == 0) return;
        try {
            SolrUpdate.signalServer(SolrHostURL);
        }
        catch (MalformedURLException me)
        {
            //System.err.println("MalformedURLException: " + me);
        	logger.error("Specified URL is malformed: " + SolrHostURL);
        }
        catch (IOException ioe)
        {
            //System.err.println("IOException: " + ioe.getMessage());
        	logger.warn("Unable to establish connection to solr server at URL: " + SolrHostURL);
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
                logger.info("Stopping main loop");
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
            logger.info("Setting Solr closed flag");
            isShutDown = true;
        }
        
        logger.info(" Adding " + recsIndexedCounter + " of " + recsReadCounter + " documents to index");
        logger.info(" Deleting " + recsDeletedCounter + " documents from index");

        if (!isShutDown) finish();

        if (!justIndexDontAdd) signalServer();
        
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

    public SolrCoreProxy getSolrCoreProxy()
    {
        if (solrCoreProxy == null)
        {
            if (solrCoreDir.equals("@SOLR_PATH@") )
            {
                System.err.println("Error: Solr home directory not initialized, please run setsolrhome") ;
                logger.error("Error: Solr home directory not initialized, please run setsolrhome") ;
                System.exit(1);               
            }
            File solrcoretest = new File(solrCoreDir);
            if (!solrcoretest.exists() || !solrcoretest.isDirectory() )
            {
                System.err.println("Error: Supplied Solr home directory does not exist: "+ solrCoreDir) ;
                logger.error("Error: Supplied Solr home directory does not exist: "+ solrCoreDir) ;
                System.exit(1);               
            }
            File solrcoretest1 = new File(solrCoreDir, "solr.xml");
            File solrcoretest2 = new File(solrCoreDir, "conf");
            if (!solrcoretest1.exists() &&  !solrcoretest2.exists() )
            {
                System.err.println("Error: Supplied Solr home directory does not contain proper solr configuration: "+ solrCoreDir) ;
                logger.error("Error: Supplied Solr home directory does not contain proper solr configuration: "+ solrCoreDir) ;
                System.exit(1);               
            }
            solrCoreProxy = SolrCoreLoader.loadCore(solrCoreDir, solrDataDir, solrCoreName, logger);
        }
        return(solrCoreProxy);
    }
    
    /**
     * Main program instantiation for doing the indexing
     * @param args
     */
    public static void main(String[] args) 
    {
        logger.info("Starting SolrMarc indexing.");
        
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
