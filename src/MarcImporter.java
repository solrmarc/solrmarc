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
import java.util.Map;
import java.util.Properties;

import marcoverride.MarcDirStreamReader;
import marcoverride.MarcPermissiveStreamReader;

import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.DocumentBuilder;
import org.apache.solr.update.UpdateHandler;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

/**
 * @author Wayne Graham (wsgrah@wm.edu)
 *
 */
public class MarcImporter {
	
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
    
    private String SolrHostURL;
	/**
	 * Constructs an instance with a properties file
	 * @param properties
	 * @throws IOException 
	 */
	MarcImporter(String properties) throws IOException
    {
		loadProperties(properties);
        // Set up Solr core
        try{
            System.setProperty("solr.data.dir", solrDataDir);
            solrConfig = new SolrConfig(solrCoreDir, "solrconfig.xml", null);
            solrCore = new SolrCore("Solr", solrDataDir, solrConfig, null);
        }
        catch (Exception e)
        {
            System.err.println("Couldn't set the instance directory");
            e.printStackTrace();
            System.exit(1);
        }
        
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
        
        solrCoreDir = getProperty(props, "solr.path");
        solrDataDir = getProperty(props, "solr.data.dir");
        if (solrDataDir == null) solrDataDir = solrCoreDir + "/data";
        String indexerName = getProperty(props, "solr.indexer");
        String indexerProps = getProperty(props, "solr.indexer.properties");
        
        try
        {
            Class indexerClass = Class.forName(indexerName);
            Constructor constructor = indexerClass.getConstructor(new Class[]{String.class});
            Object instance = constructor.newInstance(indexerProps);
            if (instance instanceof SolrIndexer)
            {
                indexer = (SolrIndexer)instance;
            }
            else
            {
                System.err.println("Error: Custom Indexer "+ indexerName +" must be subclass of SolrIndexer .  Exiting...");
                System.exit(1);
            }
        }
        catch (Exception e)
        {
            if (e instanceof ParseException)
            {
                System.err.println("Error configuring Indexer from properties file.  Exiting...");
                System.exit(1);
            }            
            System.err.println("Unable to find Custom indexer: "+ indexerName);
            System.err.println("Using default SolrIndexer with properties file: " + indexerProps);
            try {
                indexer = new SolrIndexer(indexerProps);
            }
            catch (Exception e1)
            {
                System.err.println("Error configuring Indexer from properties file.  Exiting...");
                System.exit(1);
            }
        }
        SolrHostURL = getProperty(props, "solr.hosturl");

        boolean permissiveReader = Boolean.parseBoolean(System.getProperty("marc.permissive"));        
        verbose = Boolean.parseBoolean(getProperty(props, "marc.verbose"));
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
            reader = new MarcPermissiveStreamReader(new FileInputStream(getProperty(props, "marc.path").trim()), permissiveReader);
        }
        else if (source.equals("DIR"))
        {
            reader = new MarcDirStreamReader(getProperty(props, "marc.path").trim(), permissiveReader);
        }
        else if (source.equals("Z3950"))
        {
            System.err.println("Error: not yet implemented");
            reader = null;
        }
        String marcIncludeIfPresent = getProperty(props, "marc.include_if_present");
        String marcIncludeIfMissing = getProperty(props, "marc.include_if_missing");
        if (reader != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null))
        {
            reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing);
        }
        // Do translating last so that if we are Filtering as well as translating, we don't expend the 
        // effort to translate records, which may then be filtered out and discarded.
        if (reader != null && to_utf_8)
        {
            reader = new MarcTranslatedReader(reader, unicodeNormalize);
        }
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
        catch (FileNotFoundException e)
        {
            System.err.println("Error: unable to find and open delete-record-id-list: "+ delFile);
        }
        catch (IOException e)
        {
            System.err.println("Error: reading from delete-record-id-list: "+ delFile);
        }
        return(numDeleted);
    }

    public int importRecords()
    {
        // keep track of record
        int recordCounter = 0;
        
        while(reader != null && reader.hasNext())
        {
            if (shuttingDown) break;
            recordCounter++;
            
            try {
                Record record = reader.next();
                
                System.out.println("Adding record " + recordCounter + ": " + record.getControlNumber());
                addToIndex(record);
            }
            catch (org.apache.solr.common.SolrException e)
            {
               if (e.getMessage().contains("missing required fields"))
               {
                   System.err.println("Warning : " + e.getMessage()+  "at record count = "+ recordCounter);
               }
               else
               {
                   System.err.println("Error indexing");
                   e.printStackTrace();
               }
            }
            catch(Exception e)
            {
                // keep going?
                System.err.println("Error indexing");
                e.printStackTrace();
            }            
        }
        
        return (recordCounter);
    }
    
    public void addToIndex(Record record)
    {
        Map<String, Object> map = indexer.map(record); 
        if (map.size() == 0) return;

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
        		Iterator<String> valIter = ((Collection)value).iterator();
        		while (valIter.hasNext())
        		{
        			String collVal = valIter.next();
            		builder.addField(key, collVal);
        		}
        	}
        }
        builder.endDoc();
        
        // finish up
        addcmd.doc = builder.getDoc();
        
        if (verbose)
        {
            System.out.println(record.toString());
            String doc = addcmd.doc.toString().replaceAll("> ", "> \n");
            System.out.println(doc);
        }
        addcmd.allowDups = false;
        addcmd.overwriteCommitted = true;
        addcmd.overwritePending = true;
       
        try {
            updateHandler.addDoc(addcmd);
        } 
        catch (IOException e) 
        {
            System.err.println("Couldn't add document");
            e.printStackTrace();
        }                
    }

    public void finish()
    {
        try {
            System.out.println("Calling commit");
            commit(shuttingDown ? false : optimizeAtEnd);
        } 
        catch (IOException e) {
            System.err.println("Final commit and optmization failed");
            e.printStackTrace();
        }
        
        System.out.println("Done with commit, closing Solr");       
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
     * If there is a running Solr server instance loking at the same index
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
            System.err.println("MalformedURLException: " + me);
        }
        catch (IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }  

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
            System.err.println("Starting Shutdown hook");
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
            System.err.println("Finished Shutdown hook");
        }
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        // default properties file
        String properties = "import.properties";
        if(args.length > 0)
        {
            properties = args[0];
        }
        System.out.println("Loading properties from " + properties);
        
        MarcImporter importer = null;
        try
        {
            importer = new MarcImporter(properties);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        Runtime.getRuntime().addShutdownHook(importer.new MyShutdownThread(importer));
        
        System.out.println("Here we go...");
        
        Date start = new Date();
        
        int numImported = importer.importRecords();

        int numDeleted = importer.deleteRecords();
        
        importer.finish();
        
        importer.signalServer();
        
        importer.isShutDown = true;
        
        Date end = new Date();
        
        long totalTime = end.getTime() - start.getTime();
        
        System.out.println("Finished in " + Utils.calcTime(totalTime) );
        
        // calculate the time taken
        float indexingRate = numImported*1000 / totalTime;
        
        System.out.println("Indexed " + numImported + " at a rate of about " + indexingRate + "per sec");
        System.out.println("Deleted " + numDeleted + " records");
        
        System.exit(importer.shuttingDown ? 1 : 0);
    }


 		
}