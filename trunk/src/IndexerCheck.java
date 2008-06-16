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


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.DocumentBuilder;
import org.apache.solr.update.UpdateHandler;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;

/**
 * @author Robert Haschart
 * @version $Id$
 */
public class IndexerCheck {
    
    private String solrCoreDir;
    private String solrDataDir;
    private String deleteRecordListFilename;
    private SolrIndexer indexer;
    private MarcReader reader1;
    private MarcReader reader2;
    private MarcReader reader3;
    private MarcWriter writer1;
    private MarcWriter writer2;
    private MarcWriter writer3;
    private SolrCore solrCore;
    private SolrConfig solrConfig;
    private UpdateHandler updateHandler;
    private boolean optimizeAtEnd = true;
    private boolean verbose = false;
    private boolean shuttingDown = false;
    private boolean isShutDown = false;
    private boolean to_utf_8 = false;
    
    private String SolrHostURL;
    /**
     * Constructs an instance with a properties file
     * @param properties
     * @throws IOException 
     */
    IndexerCheck(String properties) throws IOException
    {
        loadProperties(properties);
        // Set up Solr core
        try{
            System.setProperty("solr.data.dir", solrDataDir);
            solrConfig = new SolrConfig(solrCoreDir, "solrconfig.xml", null);
            solrCore = new SolrCore(solrDataDir, null);
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
        
        verbose = Boolean.parseBoolean(getProperty(props, "marc.verbose"));
        to_utf_8 = Boolean.parseBoolean(getProperty(props, "marc.to_utf_8"));
        deleteRecordListFilename = getProperty(props, "marc.ids_to_delete");
        String source = getProperty(props, "marc.source").trim();
        optimizeAtEnd = Boolean.parseBoolean(getProperty(props, "solr.optimize_at_end"));
        if (getProperty(props, "marc.override")!= null)
        {
            System.setProperty("org.marc4j.marc.MarcFactory", getProperty(props, "marc.override").trim());
        }
        reader1 = null;
        reader1 = new MarcStreamReader(new FileInputStream(getProperty(props, "marc.path1").trim()));
        reader2 = null;
        reader2 = new MarcStreamReader(new FileInputStream(getProperty(props, "marc.path2").trim()));
        reader3 = null;
        reader3 = new MarcStreamReader(new FileInputStream(getProperty(props, "marc.path2").trim()), "ISO-8859-1");
        writer1 = new MarcXmlWriter(new FileOutputStream(getProperty(props, "marc.path1").trim()+"diffs.xml"), "UTF-8");
        writer2 = new MarcXmlWriter(new FileOutputStream(getProperty(props, "marc.path2").trim()+"diffs.xml"), "UTF-8");
        writer3 = new MarcXmlWriter(new FileOutputStream(getProperty(props, "marc.path2").trim()+"translated.diffs.xml"), "UTF-8");
        ((MarcXmlWriter)writer1).setIndent(true);
        ((MarcXmlWriter)writer2).setIndent(true);
        String marcIncludeIfPresent = getProperty(props, "marc.include_if_present");
        String marcIncludeIfMissing = getProperty(props, "marc.include_if_missing");
        if (reader1 != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null))
        {
            reader1 = new MarcFilteredReader(reader1, marcIncludeIfPresent, marcIncludeIfMissing);
        }
        if (reader2 != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null))
        {
            reader2 = new MarcFilteredReader(reader2, marcIncludeIfPresent, marcIncludeIfMissing);
        }
        if (reader3 != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null))
        {
            reader3 = new MarcFilteredReader(reader3, marcIncludeIfPresent, marcIncludeIfMissing);
        }
        // Do translating last so that if we are Filtering as well as translating, we don't expend the 
        // effort to translate records, which may then be filtered out and discarded.
//        reader1 = new MarcTranslatedReader(reader1);
//      reader2 = new MarcTranslatedReader(reader2);
        reader3 = new MarcTranslatedReader(reader3, false);

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

    public int compareRecords()
    {
        // keep track of record
        int recordCounter = 0;
        int outcount = 0;
        while(reader1 != null && reader1.hasNext() && reader2 != null && reader2.hasNext())
        {
            if (shuttingDown) break;
            recordCounter++;
            if (outcount == 10) break;
            try {
                Record record1 = reader1.next();
                Record record2 = reader2.next();
                Leader lead = record2.getLeader();
                lead.setCharCodingScheme('a');
                record2.setLeader(lead);
                Record record3 = reader3.next();
                while (!record1.getControlNumber().equals(record2.getControlNumber()))
                {
                    if (Integer.parseInt(record1.getControlNumber().substring(1)) < 
                            Integer.parseInt(record2.getControlNumber().substring(1)))
                    {
                        System.out.println("Record missing from file 2: "+record1.getControlNumber());
                        record1 = reader1.next();
                    }
                    else
                    {
                        System.out.println("Record missing from file 1: "+record2.getControlNumber());
                        record2 = reader2.next();
                        record3 = reader3.next();
                    }
                }
                DocumentBuilder builder1 = new DocumentBuilder(solrCore.getSchema());
                DocumentBuilder builder2 = new DocumentBuilder(solrCore.getSchema());
                builder1.startDoc();
//                indexer.indexRecord(builder1, record1);        
                builder1.endDoc();
                builder2.startDoc();
//                indexer.indexRecord(builder2, record2);        
                builder2.endDoc();
                
                // finish up
                String doc1 = builder1.getDoc().toString();
                String doc2 = builder2.getDoc().toString();
                doc1 = doc1.replaceAll("> ", "> \n");
                doc2 = doc2.replaceAll("> ", "> \n");
                if (!doc1.equals(doc2))
                {
                    System.out.println("Records differ " + recordCounter + ": " + record1.getControlNumber() + "  "+ record2.getControlNumber());
                    writer1.write(record1);
                    writer2.write(record2);
                    writer3.write(record3);
                    outcount ++;
//                    String doc1split[] = doc1.split("\n");
//                    String doc2split[] = doc2.split("\n");
//                    for (int i = 0; i < doc1split.length; i++)
//                    {
//                        if (! doc1split[i].equals(doc2split[i]))
//                        {
//                            System.out.println("   Lines differ  doc1: " + doc1split[i]);
//                            System.out.println("                 doc2: " + doc2split[i]);
//                        }
//                    }
                    
                }
                else 
                {
        //            System.out.println("Records match " + recordCounter + ": " + record1.getControlNumber() + "  "+ record2.getControlNumber());
                }
                
            }
            catch(Exception e)
            {
                // keep going?
                System.err.println("Error indexing");
                e.printStackTrace();
            }            
        }
        writer1.close();
        writer2.close();
        return (recordCounter);
    }
           
    public void shutDown()
    {
        shuttingDown = true;
    }
    
    class MyShutdownThread extends Thread 
    {
        IndexerCheck importer;
        public MyShutdownThread(IndexerCheck im)
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
        
        IndexerCheck importer = null;
        try
        {
            importer = new IndexerCheck(properties);
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
        
        int numImported = importer.compareRecords();
                       
        importer.isShutDown = true;
        
        Date end = new Date();
        
        long totalTime = end.getTime() - start.getTime();
        
        System.out.println("Finished in " + Utils.calcTime(totalTime) );
        
        // calculate the time taken
        float indexingRate = numImported*1000 / totalTime;
        
        System.out.println("Indexed " + numImported + " at a rate of about " + indexingRate + "per sec");
        
        System.exit(importer.shuttingDown ? 1 : 0);
    }


        
}