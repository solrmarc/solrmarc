package org.solrmarc.marc;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.xpath.*;

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


public class SolrReIndexer
{
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
    
    public SolrReIndexer(String properties, String[] args) throws IOException
    {
        Properties props = new Properties();
        InputStream in = new FileInputStream(properties);
        // load the properties
        props.load(in);
        in.close();
        
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
            System.err.println("Couldn't set the instance directory");
            e.printStackTrace();
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
    
    public void readAllMatchingDocs(String queryForRecordsToUpdate)
    {
        String queryparts[] = queryForRecordsToUpdate.split(":");
        if (queryparts.length != 2) 
        {
            System.err.println("Error query must be of the form    field:term");
            return;
        }
        Map<String, Object> docMap = readAndIndexDoc(queryparts[0], queryparts[1], doUpdate);  
    }
    
    public Map<String, Object> readAndIndexDoc(String field, String term, boolean update)
    {
        try
        {
            Query query = new TermQuery(new Term(field, term));
            DocSet ds;
            ds = solrSearcher.getDocSet(query);
            int totalSize = ds.size();
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
                    return(docMap);
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(null);
    }
    
    protected void addExtraInfoFromDocToMap(Document doc, Map<String, Object> map)
    {
        // does nothing here, overridden in subclass
    }

    public Document getDocument(SolrIndexSearcher s, int SolrDocumentNum) throws IOException
    {
        Document doc = s.doc(SolrDocumentNum);
        return(doc);
    }
    
    public Record getRecordFromDocument(Document doc) throws IOException
    {
        Field field = doc.getField(solrFieldContainingEncodedMarcRecord);
        if (field == null)
        {
            System.err.println("field: "+ solrFieldContainingEncodedMarcRecord + " not found in solr document");
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
    
    static BufferedWriter errOut = null;
    
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
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
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
                        // TODO Auto-generated catch block
                        e.printStackTrace();
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
                    if (verbose) System.out.println("The bad record is: "+ marcRecordStr);
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while (tryAgain);
        return(null);

    }
        
 
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (XPathExpressionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(rec);
    }

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
            commit(false);
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
        System.out.println("Loading properties from " + properties);
        
        SolrReIndexer reader = null;
        try
        {
            reader = new SolrReIndexer(properties, args);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
