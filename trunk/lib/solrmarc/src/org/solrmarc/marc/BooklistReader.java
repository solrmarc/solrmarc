package org.solrmarc.marc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
//import org.apache.lucene.document.Document;
import org.solrmarc.marc.MarcImporter.MyShutdownThread;
import org.solrmarc.tools.Utils;

/**
 * 
 * 
 * @author Robert Haschart 
 * @version $Id$
 *
 */
public class BooklistReader extends SolrReIndexer
{
    Map<String, Map<String, Object>> documentCache = null;
    // Initialize logging category
    static Logger logger = Logger.getLogger(BooklistReader.class.getName());
    String booklistFilename = null;
    /**
     * Constructor
     * @param properties Path to properties files
     * @throws IOException
     */
    public BooklistReader(String args[]) 
    {
        super(addArg(args, "NONE"));
        loadLocalProperties(configProps);
        //handle args that weren't grabbed by some super class.
        processAdditionalArgs(this.addnlArgs);
        documentCache = new LinkedHashMap<String, Map<String, Object>>();
    }
    
    static String[] addArg(String args[], String toAdd)
    {
        String result[] = new String[args.length + 1];
        System.arraycopy(args, 0, result, 1, args.length);
        result[0] = toAdd;
        return(result);
    }
    
    private void loadLocalProperties(Properties props)
    {
        if (solrFieldContainingEncodedMarcRecord == null) 
        {
            solrFieldContainingEncodedMarcRecord = "marc_display";
        }
    }
    
    private void processAdditionalArgs(String[] args) 
    {
        booklistFilename = args.length > 0 ? args[0] : "booklists.txt";
    }

    public int handleAll()
    {
        Runtime.getRuntime().addShutdownHook(new MyShutdownThread(this));

        Date start = new Date();
        
        readBooklist(booklistFilename);
        
        finish(); 
        
        signalServer();

        return(0);
    }
        
    /**
     * Read a book list
     * @param filename Path to the book list file
     */
    public void readBooklist(String filename)
    {
        Reader input = null;
        try
        {
            if (filename.startsWith("http:"))
            {
                URL url = new URL(filename);
                URLConnection conn = url.openConnection();
                input = new InputStreamReader(conn.getInputStream());
            }
            else        
            {
                input = new FileReader(new File(filename));
            }
            BufferedReader reader = new BufferedReader(input);
            String line;
            Date today = new Date();
            while ((line = reader.readLine()) != null)
            {
                if (shuttingDown) break;
                
                String fields[] = line.split("\\|");
                Map<String, String> valuesToAdd = new LinkedHashMap<String, String>();
                valuesToAdd.put("fund_code_facet", fields[11]);
                valuesToAdd.put("date_received_facet", fields[0]);
                DateFormat format = new SimpleDateFormat("yyyyMMdd");
                Date dateReceived = format.parse(fields[0], new ParsePosition(0));
                if (dateReceived.after(today)) continue;
                
                String docID = "u"+fields[9];
                Map<String, Object> docMap = getDocumentMap(docID);
                if (docMap != null)
                {
                    addNewDataToRecord( docMap, valuesToAdd );
                    documentCache.put(docID, docMap);
                    if (doUpdate && docMap != null && docMap.size() != 0)
                    {
                        update(docMap);
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            // e.printStackTrace();
        	logger.info(e.getMessage());
        	logger.error(e.getCause());
        }
        catch (IOException e)
        {
            // e.printStackTrace();
        	logger.info(e.getMessage());
        	logger.error(e.getCause());
        }

    }
    
    /**
     * Get the documentMap for the the given marc id
     * @param docID 
     * @return The map of index fields
     */
    private Map<String, Object> getDocumentMap(String docID)
    {
        Map<String, Object> docMap = null;
        if (documentCache.containsKey(docID))
        {
            docMap = documentCache.get(docID);
        }
        else
        {
            docMap = readAndIndexDoc("id", docID, false);
        }
        return docMap;
    }

    private void addNewDataToRecord(Map<String, Object> docMap, Map<String, String> valuesToAdd )
    {
        Iterator<String> keyIter = valuesToAdd.keySet().iterator();
        while (keyIter.hasNext())
        {
            String keyVal = keyIter.next();
            String addnlFieldVal = valuesToAdd.get(keyVal);
            addToMap(docMap, keyVal, addnlFieldVal); 
        }        
    }
    
//    private Record lookup(String doc_id)
//    {
//        RefCounted<SolrIndexSearcher> rs = solrCore.getSearcher();
//        SolrIndexSearcher s = rs.get();
//        Term t = new Term("id", doc_id);
//        int docNo;
//        String marcRecord = null;
//        try
//        {
//            docNo = s.getFirstMatch(t);
//            if (docNo > 0)
//            {
//                Document doc = s.doc(docNo);
//                Field field = doc.getField("marc_display");
//                marcRecord = field.stringValue();
//            }
//            else
//            {
//                URL url = new URL("http://solrpowr.lib.virginia.edu:8080/solr/select/?q=id%3A"+doc_id+"&start=0&rows=1");
//                InputStream stream = url.openStream();
//                //The evaluate methods in the XPath and XPathExpression interfaces are used to parse an XML document with XPath expressions. The XPathFactory class is used to create an XPath object. Create an XPathFactory object with the static newInstance method of the XPathFactory class.
//
//                XPathFactory  factory = XPathFactory.newInstance();
//
//                // Create an XPath object from the XPathFactory object with the newXPath method.
//
//                XPath xPath = factory.newXPath();
//
//                // Create and compile an XPath expression with the compile method of the XPath object. As an example, select the title of the article with its date attribute set to January-2004. An attribute in an XPath expression is specified with an @ symbol. For further reference on XPath expressions, see the XPath specification for examples on creating an XPath expression.
//
//                XPathExpression  xPathExpression=
//                    xPath.compile("/response/result/doc/arr[@name='marc_display']/str");
//                
//                InputSource inputSource = new InputSource(stream);
//                marcRecord = xPathExpression.evaluate(inputSource);
//            }
//            
//            MarcXmlReader reader = new MarcXmlReader(new ByteArrayInputStream(marcRecord.getBytes("UTF8")));
//            if (reader.hasNext())
//            {
//                Record rec = reader.next();
//                return(rec);
//            }
//
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        catch (XPathExpressionException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return(null);
//    }

    /**
     * @param args
     */

    public static void main(String[] args) 
    {
        logger.info("Starting Booklist processing.");
        
        BooklistReader reader = null;
        try
        {
            reader = new BooklistReader(args);
        }
        catch (IllegalArgumentException e)
        {
            logger.error(e.getMessage());
            System.err.println(e.getMessage());
            //e.printStackTrace();
            System.exit(1);
        }
        
        int exitCode = reader.handleAll();
        System.exit(exitCode);
    }

}
