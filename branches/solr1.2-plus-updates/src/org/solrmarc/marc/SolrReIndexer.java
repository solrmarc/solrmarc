package org.solrmarc.marc;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DocumentBuilder;
import org.apache.solr.update.UpdateHandler;
import org.apache.solr.util.RefCounted;
import org.marc4j.MarcException;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;
import org.xml.sax.InputSource;

import org.apache.log4j.Logger;


/**
 * Reindex marc records stored in an index
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class SolrReIndexer
{
    private String solrMarcDir;
    private String solrCoreDir;
    private String solrDataDir;
    private SolrCore solrCore;
    private SolrConfig solrConfig;
    private UpdateHandler updateHandler;
    private boolean verbose = false;
    private SolrIndexer indexer;
    private String queryForRecordsToUpdate;
    protected String solrFieldContainingEncodedMarcRecord;
    protected boolean doUpdate = true;
    private RefCounted<SolrIndexSearcher> refedSolrSearcher;
    private SolrIndexSearcher solrSearcher;
    
    // Initialize logging category
    static Logger logger = Logger.getLogger(SolrReIndexer.class.getName());
    
    /**
     * Constructor
     * @param properties path to properties files
     * @param args additional arguments
     * @throws IOException
     */
    public SolrReIndexer(String properties, String[] args) throws IOException
    {
        Properties props = new Properties();
        InputStream in = new FileInputStream(properties);
        // load the properties
        props.load(in);
        in.close();
        
        solrMarcDir = getProperty(props, "solrmarc.path");
        solrCoreDir = getProperty(props, "solr.path");
        solrDataDir = getProperty(props, "solr.data.dir");
        solrFieldContainingEncodedMarcRecord = getProperty(props, "solr.fieldname");
        queryForRecordsToUpdate = getProperty(props, "solr.query");
        String up = getProperty(props, "solr.do_update");
        doUpdate = (up == null) ? true : Boolean.parseBoolean(up);
        if (queryForRecordsToUpdate == null && args.length > 0)
        {
            queryForRecordsToUpdate = args[0];
        }
        if (solrFieldContainingEncodedMarcRecord == null && args.length > 1)
        {
            solrFieldContainingEncodedMarcRecord = args[1];
        }
        if (solrDataDir == null) solrDataDir = solrCoreDir + "/data";
        // Set up Solr core
        try{
            System.setProperty("solr.data.dir", solrDataDir);
            solrConfig = new SolrConfig(solrCoreDir, "solrconfig.xml", null);
            solrCore = new SolrCore("Solr", solrDataDir, solrConfig, null);
            refedSolrSearcher = solrCore.getSearcher();
            solrSearcher = refedSolrSearcher.get();
        }
        catch (Exception e)
        {
            logger.error("Couldn't set the instance directory");
            logger.error(e.getMessage());
        	//System.err.println("Couldn't set the instance directory");
            //e.printStackTrace();
            System.exit(1);
        }
        verbose = Boolean.parseBoolean(getProperty(props, "marc.verbose"));
        
        updateHandler = solrCore.getUpdateHandler();
        String indexerName = getProperty(props, "solr.indexer");
        String indexerProps = getProperty(props, "solr.indexer.properties");
        
        try
        {
            Class indexerClass;
            try {
                indexerClass = Class.forName(indexerName);
            }
            catch (ClassNotFoundException e)
            {
                Class baseIndexerClass = SolrIndexer.class;
                String baseName = baseIndexerClass.getPackage().getName();
                String fullName = baseName + "." + indexerName;
                indexerClass = Class.forName(fullName);
            }
            Constructor constructor = indexerClass.getConstructor(new Class[]{String.class, String.class});
            Object instance = constructor.newInstance(indexerProps, solrMarcDir);
            if (instance instanceof SolrIndexer)
            {
                indexer = (SolrIndexer)instance;
            }
            else
            {
                //System.err.println("Error: Custom Indexer "+ indexerName +" must be subclass of SolrIndexer .  Exiting...");
            	logger.error("Error: Custom Indexer "+ indexerName +" must be subclass of SolrIndexer .  Exiting...");
                System.exit(1);
            }
        }
        catch (Exception e)
        {
            if (e instanceof ParseException)
            {
                //System.err.println("Error configuring Indexer from properties file.  Exiting...");
            	logger.error("Error configuring Indexer from properties file.  Exiting...");
                System.exit(1);
            }            
            
//            System.err.println("Unable to find Custom indexer: "+ indexerName);
//            System.err.println("Using default SolrIndexer with properties file: " + indexerProps);
            logger.error("Unable to find Custom indexer: "+ indexerName);
            logger.error("Using default SolrIndexer with properties file: " + indexerProps);
            
            try {
                indexer = new SolrIndexer(indexerProps, solrMarcDir);
            }
            catch (Exception e1)
            {
                //System.err.println("Error configuring Indexer from properties file.  Exiting...");
            	logger.error("Error configuring Indexer from properties file.  Exiting...");
                System.exit(1);
            }
        }
        
    }

     /*
      * Check first for a particular property in the System Properties, so that the -Dprop="value" command line arg 
      * mechanism can be used to override values defined in the passed in property file.  This is especially useful
      * for defining the marc.source property to define which file to operate on, in a shell script loop.
      */
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
     * Read matching records from the index
     * @param queryForRecordsToUpdate
     */
    public void readAllMatchingDocs(String queryForRecordsToUpdate)
    {
        String queryparts[] = queryForRecordsToUpdate.split(":");
        if (queryparts.length != 2) 
        {
            //System.err.println("Error query must be of the form    field:term");
        	logger.warn("Error query must be of the form    field:term");
            return;
        }
        Map<String, Object> docMap = readAndIndexDoc(queryparts[0], queryparts[1], doUpdate);  
    }
    
    /**
     * Read and index a Solr document
     * @param field Solr field
     * @param term Term string to index
     * @param update flag to update the record 
     * @return Map of the fields
     */
    public Map<String, Object> readAndIndexDoc(String field, String term, boolean update)
    {
        try
        {
            Query query = new TermQuery(new Term(field, term));
            System. out.println("Searching for :" + field +" : "+ term);
            DocSet ds;
            ds = solrSearcher.getDocSet(query);
            int totalSize = ds.size();
            System. out.println("Num found = " + totalSize);
            int count = 0;
            DocIterator iter = ds.iterator();
            while (iter.hasNext())
            {
                int docNo = iter.nextDoc();
                count ++;
                if (count == 100 || count == 1000 || count == 10000 || count % 10000 == 0)
                {
                    System. out.println("Done handling "+ count +" record out of "+ totalSize);
                }
                
                Document doc = getDocument(solrSearcher, docNo);
                Record record = getRecordFromDocument(doc);
                
                if (record != null)
                {
                    Map<String, Object> docMap = indexer.map(record);
                    addExtraInfoFromDocToMap(doc, docMap);
                    if (update && docMap != null && docMap.size() != 0)
                    {
                        update(docMap);
                    }
                    else
                    {
                        return(docMap);
                    }
                }
            }
        }
        catch (IOException e)
        {
            logger.error(e.getMessage());
            //e.printStackTrace();
        }
        return(null);
    }
    
    /**
     * Add information from a document to a map.
     * Overriden in subclass
     * @param doc
     * @param map
     */
    protected void addExtraInfoFromDocToMap(Document doc, Map<String, Object> docMap)
    {
        addExtraInfoFromDocToMap(doc, docMap, "fund_code_facet");
        addExtraInfoFromDocToMap(doc, docMap, "date_received_facet");   
    }

    /**
     * Add extra information from a Solr Document to a map
     * @param doc Solr Document to pull information from
     * @param map Map to add information to
     * @param keyVal Value to add
     */
    protected void addExtraInfoFromDocToMap(Document doc, Map<String, Object> map, String keyVal)
    {
        String fieldVals[] = doc.getValues(keyVal);
        if (fieldVals != null && fieldVals.length > 0)
        {
            for (int i = 0; i < fieldVals.length; i++)
            {
                String fieldVal = fieldVals[i];
                addToMap(map, keyVal, fieldVal);
            }
        }           
    }

    /**
     * Return a Solr document from the index
     * @param s SolrIndexSearcher to search
     * @param SolrDocumentNum Number of documents to return
     * @return SolrDocument 
     * @throws IOException
     */
    public Document getDocument(SolrIndexSearcher s, int SolrDocumentNum) throws IOException
    {
        Document doc = s.doc(SolrDocumentNum);
        return(doc);
    }
    
    /**
     * Retrieve the marc information from the Solr document
     * @param doc SolrDocument from the index
     * @return marc4j Record
     * @throws IOException
     */
    public Record getRecordFromDocument(Document doc) throws IOException
    {
        Field field = doc.getField(solrFieldContainingEncodedMarcRecord);
        if (field == null)
        {
            //System.err.println("field: "+ solrFieldContainingEncodedMarcRecord + " not found in solr document");
        	logger.warn("field: "+ solrFieldContainingEncodedMarcRecord + " not found in solr document");
        }
        String marcRecordStr = field.stringValue();
        if (marcRecordStr.startsWith("<?xml version"))
        {
            return (getRecordFromXMLString(marcRecordStr));            
        }
        else
        {
            return (getRecordFromRawMarc(marcRecordStr));
        }
    }
        
    /**
     * Extract the marc record from binary marc
     * @param marcRecordStr
     * @return
     */
    private Record getRecordFromRawMarc(String marcRecordStr)
    {
        MarcStreamReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcStreamReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next(); 
                    if (verbose)
                    {
                        System.out.println(record.toString());
                    }
                    return(record);
                }
            }
            catch( MarcException me)
            {
                me.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        } while (tryAgain);
        return(null);
    }
    
    // error output
    static BufferedWriter errOut = null;
    
    /**
     * Extract marc record from MarcXML
     * @param marcRecordStr MarcXML string
     * @return marc4j Record
     */
    public Record getRecordFromXMLString(String marcRecordStr)
    {
        MarcXmlReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcXmlReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next(); 
                    if (verbose)
                    {
                        System.out.println(record.toString());
                        System.out.flush();
                    }
                    return(record);
                }
            }
            catch( MarcException me)
            {
                if (doUpdate == false && errOut == null)
                {
                    try
                    {
                        errOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("badRecs.xml"))));
                        errOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
                    }
                    catch (FileNotFoundException e)
                    {
                        // e.printStackTrace();
                    	logger.error(e.getMessage());
                    }
                    catch (IOException e)
                    {
                        // e.printStackTrace();
                    	logger.error(e.getMessage());
                    }
                }
                if (doUpdate == false && errOut != null)
                {
                    String trimmed = marcRecordStr.substring(marcRecordStr.indexOf("<record>"));
                    trimmed = trimmed.replaceFirst("</collection>", "");
                    trimmed = trimmed.replaceAll("><", ">\n<");
                    try
                    {
                        errOut.write(trimmed);
                    }
                    catch (IOException e)
                    {
                        // e.printStackTrace();
                    	logger.error(e.getMessage());
                    }
                }
                if (marcRecordStr.contains("<subfield code=\"&#31;\">"))
                {
                    // rewrite input string and try again.
                    marcRecordStr = marcRecordStr.replaceAll("<subfield code=\"&#31;\">(.)", "<subfield code=\"$1\">");
                    tryAgain = true;
                }
                else if (extractLeader(marcRecordStr).contains("&#")) //.("<leader>[^<>&]*&#[0-9]+;[^<>&]*</leader>"))
                {
                    // rewrite input string and try again.
                    // 07585nam a2200301 a 4500
                    String leader = extractLeader(marcRecordStr).replaceAll("&#[0-9]+;", "0");
                    marcRecordStr = marcRecordStr.replaceAll("<leader>[^<]*</leader>", leader);
                    tryAgain = true;
                }
                else
                {
                    me.printStackTrace();
                    if (verbose) {
                    	//System.out.println("The bad record is: "+ marcRecordStr);
                    	logger.info("The bad record is: "+ marcRecordStr);
                    	logger.error("The bad record is: "+ marcRecordStr);
                    }
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // e.printStackTrace();
            	logger.error(e.getMessage());
            }
        } while (tryAgain);
        return(null);

    }
        
 
    /**
     * Extract the leader from the marc record string
     * @param marcRecordStr marc record as a String
     * @return Leader leader string for the marc record
     */
    private String extractLeader(String marcRecordStr)
    {
        final String leadertag1 = "<leader>";
        final String leadertag2 = "</leader>";
        String leader = null;
        try {
            leader = marcRecordStr.substring(marcRecordStr.indexOf(leadertag1), marcRecordStr.indexOf(leadertag2)+leadertag2.length() );
        }
        catch (IndexOutOfBoundsException e)
        {}
        return leader;
    }

//    private void lookupAndUpdate(String doc_id, String[] fields)
//    {
//        Record record = lookup(doc_id);
//        if (verbose)
//        {
//            System.out.println(record.toString());
//        }
//    }
    
    /**
     * Add a key value pair to a map
     */
    protected void addToMap(Map<String, Object> map, String key, String value)
    {
        if (map.containsKey(key))
        {
            Object prevValue = map.get(key);
            if (prevValue instanceof String)
            {
                if (!prevValue.equals(value))
                {
                    Set<String> result = new LinkedHashSet<String>();
                    result.add((String)prevValue);
                    result.add((String)value);
                    map.put(key, result);
                }
            }
            else if (prevValue instanceof Collection)
            {
                Iterator<String> valIter = ((Collection)prevValue).iterator();
                boolean addit = true;
                while (valIter.hasNext())
                {
                    String collVal = valIter.next();
                    if (collVal.equals(value)) addit = false;
                }
                if (addit) 
                {
                    ((Collection)prevValue).add(value);
                    map.put(key, prevValue);
                }
            }
        }
        else 
        {
            map.put(key, value);
        }
    }

    /**
     * find a specific marc record (using its id) in the solr index
     * @param doc_id ID of the marc record to find
     * @return if the item is in the index
     */
    private Record lookup(String doc_id)
    {
        RefCounted<SolrIndexSearcher> rs = solrCore.getSearcher();
        SolrIndexSearcher s = rs.get();
        Term t = new Term("id", doc_id);
        int docNo;
        Record rec = null;
        try
        {
            docNo = s.getFirstMatch(t);
            if (docNo > 0)
            {
                Document doc = getDocument(s, docNo);
                rec = getRecordFromDocument(doc);
            }
            else
            {
            	//TODO: construct this from the properties
                URL url = new URL("http://solrpowr.lib.virginia.edu:8080/solr/select/?q=id%3A"+doc_id+"&start=0&rows=1");
                InputStream stream = url.openStream();
                //The evaluate methods in the XPath and XPathExpression interfaces are used to parse an XML document with XPath expressions. The XPathFactory class is used to create an XPath object. Create an XPathFactory object with the static newInstance method of the XPathFactory class.

                XPathFactory  factory = XPathFactory.newInstance();

                // Create an XPath object from the XPathFactory object with the newXPath method.

                XPath xPath = factory.newXPath();

                // Create and compile an XPath expression with the compile method of the XPath object. As an example, select the title of the article with its date attribute set to January-2004. An attribute in an XPath expression is specified with an @ symbol. For further reference on XPath expressions, see the XPath specification for examples on creating an XPath expression.

                XPathExpression  xPathExpression=
                    xPath.compile("/response/result/doc/arr[@name='marc_display']/str");
                
                InputSource inputSource = new InputSource(stream);
                String marcRecordStr = xPathExpression.evaluate(inputSource);
                rec = getRecordFromXMLString(marcRecordStr);
            }           
        }
        catch (IOException e)
        {
            // e.printStackTrace();
        	logger.error(e.getMessage());
        }
        catch (XPathExpressionException e)
        {
            // e.printStackTrace();
        	logger.error(e.getMessage());
        }
        return(rec);
    }

    /**
     * Update a document in the Solr index
     * @param map Values of the "new" marc record
     */
    public void update(Map<String, Object> map)
    { 
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
//            System.out.println(record.toString());
            String doc = addcmd.doc.toString().replaceAll("> ", "> \n");
            //System.out.println(doc);
            logger.info(doc);
        }
        addcmd.allowDups = false;
        addcmd.overwriteCommitted = true;
        addcmd.overwritePending = true;
       
        try {
            updateHandler.addDoc(addcmd);
        } 
        catch (IOException e) 
        {
            //System.err.println("Couldn't add document");
            //e.printStackTrace();
        	logger.error("Couldn't add marc file.");
        	logger.error(e.getMessage());
        }                
    }
    
    /**
     * finish reindexing
     */
    public void finish()
    {
        try {
            //System.out.println("Calling commit");
        	logger.debug("Callling commit");
            commit(false);
        } 
        catch (IOException e) {
//            System.err.println("Final commit and optmization failed");
//            e.printStackTrace();
        	logger.error("Final commit and optimization failed");
        	logger.error(e.getMessage());
        }
        
       // System.out.println("Done with commit, closing Solr");
        logger.info("Done with commit, closing Solr");
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
     * @param args
     */
    public static void main(String[] args)
    {
        String properties = "import.properties";
        if(args.length > 0 && args[0].endsWith(".properties"))
        {
            properties = args[0];
            String newArgs[] = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            args = newArgs;
        }
       // System.out.println("Loading properties from " + properties);
        logger.info("Loading properties from " + properties);
        
        SolrReIndexer reader = null;
        try
        {
            reader = new SolrReIndexer(properties, args);
        }
        catch (IOException e)
        {
            //  e.printStackTrace();
        	logger.error(e.getMessage());
            System.exit(1);
        }
        
        reader.readAllMatchingDocs(reader.queryForRecordsToUpdate);
        
        reader.finish();
        if (errOut != null)
        {
            try
            {
                errOut.write("\n</collection>");
                errOut.flush();

            }
            catch (IOException e)
            {
                // e.printStackTrace();
            	logger.error(e.getMessage());
            }
        }

    }

}
