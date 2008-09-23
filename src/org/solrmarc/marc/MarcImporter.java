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


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.*;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.DocumentBuilder;
import org.apache.solr.update.UpdateHandler;
import org.marc4j.ErrorHandler;
import org.marc4j.MarcDirStreamReader;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.marc.MarcFilteredReader;
import org.solrmarc.tools.Utils;

/**
 * @author Wayne Graham (wsgrah@wm.edu)
 * @version $Id$
 *
 */
public class MarcImporter {
	
    private String solrMarcDir;
    private String solrCoreName;
    private String solrCoreDir;
    private String solrDataDir;
    private String deleteRecordListFilename;
    private SolrIndexer indexer;
    private MarcReader reader;
    private SolrCore solrCore;
    private SolrConfig solrConfig;
    private UpdateHandler updateHandler;
    private boolean optimizeAtEnd = true;
    private boolean verbose = false;
    private boolean shuttingDown = false;
    private boolean isShutDown = false;
    private boolean to_utf_8 = false;
    private boolean unicodeNormalize = false;
    private ErrorHandler errors = null;
    private boolean includeErrors = false;
   
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcImporter.class.getName());
    
    private String SolrHostURL;
	/**
	 * Constructs an instance with a properties file
	 * @param properties
	 * @throws IOException 
	 */
    public MarcImporter(String properties) throws IOException
    {
        // Process Properties
        loadProperties(properties);

        // Set up Solr core
        try{
            System.setProperty("solr.data.dir", solrDataDir);
            logger.info("Using the data directory of: " + solrDataDir);
            
            File configFile = new File(solrCoreDir + "/solr.xml");
            logger.info("Using the multicore schema file at : " + configFile.getAbsolutePath());
            logger.info("Using the " + solrCoreName + " core");
            
            CoreContainer cc = new CoreContainer(solrCoreDir, configFile);
            
            solrCore = cc.getCore(solrCoreName);
           
        }
        catch (Exception e)
        {
            logger.error("Couldn't load the solr core directory");
            e.printStackTrace();
            System.exit(1);
        }

        // Setup UpdateHandler
        updateHandler = solrCore.getUpdateHandler();

	}

    /**
     * Load the properties file
     * @param properties
     * @throws IOException
     */
    public void loadProperties(String properties) throws IOException
    {
        Properties props = new Properties();

        InputStream in = new FileInputStream(properties);

        // load the properties
        props.load(in);
        in.close();

        // The location of where the .properties files are located
        solrMarcDir = getProperty(props, "solrmarc.path");

        // The solr.home directory
        solrCoreDir = getProperty(props, "solr.path");

        // The solr core to be used
        solrCoreName = getProperty(props, "solr.core.name");

        // The solr data diretory to use
        solrDataDir = getProperty(props, "solr.data.dir");
        if (solrDataDir == null) {
            solrDataDir = solrCoreDir + "/" + solrCoreName;
        }

        // The SolrMarc indexer
        String indexerName = getProperty(props, "solr.indexer");

        // The SolrMarc indexer properties file
        String indexerProps = getProperty(props, "solr.indexer.properties");



        // Setup the SolrMarc Indexer
        try
        {
            Class indexerClass;
            
            try {
                indexerClass = Class.forName(indexerName);
            }
            catch (ClassNotFoundException e)
            {
                logger.error("Cannot load class: " + indexerName);
                Class baseIndexerClass = SolrIndexer.class;
                String baseName = baseIndexerClass.getPackage().getName();
                String fullName = baseName + "." + indexerName;
                indexerClass = Class.forName(fullName);
                logger.error(e.getCause());
            }
            
            Constructor constructor = indexerClass.getConstructor(new Class[]{String.class, String.class});
            Object instance = constructor.newInstance(indexerProps, solrMarcDir);

            if (instance instanceof SolrIndexer)
            {
                indexer = (SolrIndexer)instance;
            }
            else
            {
            	logger.error("Error: Custom Indexer " + indexerName + " must be subclass of SolrIndexer .  Exiting...");
                System.exit(1);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        
            if (e instanceof ParseException)
            {
            	logger.error("Error configuring Indexer from properties file.  Exiting...");
                System.exit(1);
            }            
            
            logger.warn("Unable to load Custom indexer: " + indexerName);
            logger.warn("Using default SolrIndexer with properties file: " + indexerProps); 
            
            try {
                indexer = new SolrIndexer(indexerProps, solrMarcDir);
            }
            catch (Exception e1)
            {
            	logger.error("Error configuring Indexer from properties file.  Exiting...");
                System.exit(1);
            }
        }
        SolrHostURL = getProperty(props, "solr.hosturl");

        boolean permissiveReader = Boolean.parseBoolean(getProperty(props, "marc.permissive"));
        String defaultEncoding;
        if (getProperty(props, "marc.default_encoding") != null)
        {
            defaultEncoding = getProperty(props, "marc.default_encoding").trim();    
        }
        else
        {
            defaultEncoding = "BESTGUESS";
        }
        verbose = Boolean.parseBoolean(getProperty(props, "marc.verbose"));
        includeErrors = Boolean.parseBoolean(getProperty(props, "marc.include_errors"));
        to_utf_8 = Boolean.parseBoolean(getProperty(props, "marc.to_utf_8"));
        unicodeNormalize = Boolean.parseBoolean(getProperty(props, "marc.unicode_normalize"));
        deleteRecordListFilename = getProperty(props, "marc.ids_to_delete");
        String source = getProperty(props, "marc.source").trim();
        optimizeAtEnd = Boolean.parseBoolean(getProperty(props, "solr.optimize_at_end"));
        if (getProperty(props, "marc.override")!= null)
        {
            System.setProperty("org.marc4j.marc.MarcFactory", getProperty(props, "marc.override").trim());
        }
        reader = null;
        if (source.equals("FILE"))
        {
            if (permissiveReader)
            {
                errors = new ErrorHandler();
                reader = new MarcPermissiveStreamReader(new FileInputStream(getProperty(props, "marc.path").trim()), errors, to_utf_8, defaultEncoding);
            }
            else
            {
                reader = new MarcPermissiveStreamReader(new FileInputStream(getProperty(props, "marc.path").trim()), false, to_utf_8, defaultEncoding);
            }
        }
        else if (source.equals("DIR"))
        {
            reader = new MarcDirStreamReader(getProperty(props, "marc.path").trim(), permissiveReader, to_utf_8);
        }
        else if (source.equals("Z3950"))
        {
        	logger.warn("Error: Z3950 not yet implemented");
            reader = null;
        }
        String marcIncludeIfPresent = getProperty(props, "marc.include_if_present");
        String marcIncludeIfMissing = getProperty(props, "marc.include_if_missing");
        if (reader != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null))
        {
            reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing);
        }
//        // Do translating last so that if we are Filtering as well as translating, we don't expend the 
//        // effort to translate records, which may then be filtered out and discarded.
//        if (reader != null && to_utf_8)
//        {
//            reader = new MarcTranslatedReader(reader, unicodeNormalize);
//        }
        return;
    }
    
    // Check first for a particular property in the System Properties, so that the -Dprop="value" command line arg 
    // mechanism can be used to override values defined in the passed in property file.  This is especially useful
    // for defining the marc.source property to define which file to operate on, in a shell script loop.
    private String getProperty(Properties props, String propname)
    {
        String prop;
        if ((prop = System.getProperty(propname)) != null)
        {
            return(prop);
        }
        if ((prop = props.getProperty(propname)) != null)
        {
            return(prop);
        }
        return null;
    }

    /**
     * Delete records from the index
     * @return Number of records deleted
     */
    public int deleteRecords()
    {
        int numDeleted = 0;
        if (deleteRecordListFilename == null || 
            deleteRecordListFilename.length() == 0) 
        {
            return(numDeleted);
        }
        File delFile = new File(deleteRecordListFilename);
        try
        {
            BufferedReader is = new BufferedReader(new FileReader(delFile));
            String line;
            DeleteUpdateCommand delCmd = new DeleteUpdateCommand();
            while ((line = is.readLine()) != null)
            {
                if (shuttingDown) break;
                line = line.trim();
                if (line.startsWith("#")) continue;
                if (!line.startsWith("u"))
                {
                    line = "u" + line;
                }                
                delCmd.id = line;
                delCmd.fromCommitted = true;
                delCmd.fromPending = true;
                updateHandler.delete(delCmd);
                numDeleted++;
            }            
        }
        catch (FileNotFoundException fnfe)
        {
        	logger.error("Error: unable to find and open delete-record-id-list: " + delFile, fnfe);
        }
        catch (IOException ioe)
        {
        	logger.error("Error: reading from delete-record-id-list: " + delFile);
        }
        return(numDeleted);
    }

    /**
     * Iterate over the marc records in the file and add them to the index
     * @return Number of records indexed
     */
    public int importRecords()
    {
        // keep track of record count
        int recordCounter = 0;
        
        while(reader != null && reader.hasNext())
        {
            if (shuttingDown) break;
            recordCounter++;
            
            try {
                Record record = reader.next();
                
                try {
                    addToIndex(record);
                    logger.info("Adding record " + recordCounter + ": " + record.getControlNumber());
                }
                catch (org.apache.solr.common.SolrException solrException)
                {
                   //check for missing fields
                	if (solrException.getMessage().contains("missing required fields"))
                   {
                	   logger.error(solrException.getMessage() +  " at record count = " + recordCounter);
                	   logger.error("Control Number " + record.getControlNumber(), solrException);
                   }
                   else
                   {
                	   logger.error("Error indexing: " + solrException.getMessage());
                	   logger.error("Control Number " + record.getControlNumber(), solrException);
                   }
                }
                catch(Exception e)
                {
                    // keep going?
                	logger.error("Error indexing: " + e.getMessage());
                	logger.error("Control Number " + record.getControlNumber(), e);
                }
            } catch (Exception e) {
                logger.error("Error reading record: " + e.getMessage());
            }
        }
        
        return recordCounter;
    }
    
    /**
     * Add a record to the index
     * @param record marc record to add
     */
    public void addToIndex(Record record)
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
        AddUpdateCommand addcmd = new AddUpdateCommand();
        DocumentBuilder builder = new DocumentBuilder(solrCore.getSchema());
        builder.startDoc();
    	Iterator<String> keys = map.keySet().iterator();
        while (keys.hasNext())
        {
        	String key = keys.next();
        	Object value = map.get(key);
        	if (value instanceof String)
        	{
        		builder.addField(key, (String)value);
        	}
        	else if (value instanceof Collection)
        	{
        		Iterator<?> valIter = ((Collection)value).iterator();
        		while (valIter.hasNext())
        		{
        			String collVal = valIter.next().toString();
            		builder.addField(key, collVal);
        		}
        	}
        }
        builder.endDoc();
        
        // finish up
        addcmd.doc = builder.getDoc();
        
        if (verbose)
        {
            //System.out.println(record.toString());
            String doc = addcmd.doc.toString().replaceAll("> ", "> \n");
            //System.out.println(doc);
            logger.info(record.toString());
            logger.info(doc);
        }
        addcmd.allowDups = false;
        addcmd.overwriteCommitted = true;
        addcmd.overwritePending = true;
       
        try {
            updateHandler.addDoc(addcmd);
        } 
        catch (IOException ioe) 
        {
            //System.err.println("Couldn't add document");
        	logger.error("Couldn't add document: " + ioe.getMessage());
            //e.printStackTrace();
        	logger.error("Control Number " + reader.next().getControlNumber(), ioe);
        }                
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
            commit(shuttingDown ? false : optimizeAtEnd);
        } 
        catch (IOException ioe) {
            //System.err.println("Final commit and optmization failed");
        	logger.error("Final commit and optimization failed: " + ioe.getMessage());
        	logger.debug(ioe);
            //e.printStackTrace();
        }
        
        //System.out.println("Done with commit, closing Solr");
        logger.info("Done with the commit, closing Solr");
        solrCore.close();
    }


	/**
	 * Commit the document to the repository and optimize the index
	 * @param optimize
	 * @throws IOException
	 */
	public void commit(boolean optimize) throws IOException 
    {
		CommitUpdateCommand commitcmd = new CommitUpdateCommand(optimize);
		updateHandler.commit(commitcmd);
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
     * Main program instantiation for doing the indexing
     * @param args
     */
    public static void main(String[] args) 
    {
        logger.info("Starting SolrMarc indexing.");
    	// default properties file
        String properties = "import.properties";
        
        if(args.length > 0)
        {
            properties = args[0];
        }
        
        // System.out.println("Loading properties from " + properties);
        logger.info("Loading properties from " + properties);
        
        MarcImporter importer = null;
        try
        {
            importer = new MarcImporter(properties);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
        	logger.error("Couldn't load properties file." + e);
            //e.printStackTrace();
            System.exit(1);
        }
        
        logger.debug("Shutdown hook for Solr");
        Runtime.getRuntime().addShutdownHook(importer.new MyShutdownThread(importer));
        
        //System.out.println("Here we go...");
        
        Date start = new Date();
        
        int numImported = 0;
        int numDeleted = 0;
        try
        {        
            numImported = importer.importRecords();
            
            numDeleted = importer.deleteRecords();
        }
        catch (Exception e)
        {
            logger.info("Exception occurred while Indexing: "+ e.getMessage());
            
        }
        
        importer.finish();
        
        importer.signalServer();
        
        importer.isShutDown = true;
        
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
        
        System.exit(importer.shuttingDown ? 1 : 0);
    }


 		
}