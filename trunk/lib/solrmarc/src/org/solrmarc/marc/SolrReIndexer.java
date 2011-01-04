package org.solrmarc.marc;

import java.io.*;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.*;
import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;

import org.solrmarc.solr.DocumentProxy;
import org.solrmarc.solr.SolrCoreLoader;
import org.solrmarc.solr.SolrCoreProxy;
import org.solrmarc.solr.SolrSearcherProxy;
import org.solrmarc.tools.Utils;

import org.apache.log4j.Logger;


/**
 * Reindex marc records stored in an index
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class SolrReIndexer extends MarcImporter
{
    protected SolrSearcherProxy solrSearcherProxy;
    private String queryForRecordsToUpdate;
    protected String solrFieldContainingEncodedMarcRecord;
    protected boolean doUpdate = true;
    protected MarcWriter output = null;
 
    // Initialize logging category
    static Logger logger = Logger.getLogger(SolrReIndexer.class.getName());
    
    /**
     * Constructor
     * @param properties path to properties files
     * @param args additional arguments
     * @throws IOException
     */
    public SolrReIndexer()
    {
    }

    @Override
    public int handleAll()
    {
        verbose = false;
        output = new MarcStreamWriter(System.out, "UTF8", true);
        if (solrFieldContainingEncodedMarcRecord == null) solrFieldContainingEncodedMarcRecord = "marc_display";
        readAllMatchingDocs(queryForRecordsToUpdate);
        output.close();
        return 0;
    }

    @Override
    protected void loadLocalProperties()
    {
        super.loadLocalProperties();
        solrFieldContainingEncodedMarcRecord = Utils.getProperty(configProps, "solr.fieldname");
        queryForRecordsToUpdate = Utils.getProperty(configProps, "solr.query");
        String up = Utils.getProperty(configProps, "solr.do_update");
        doUpdate = (up == null) ? true : Boolean.parseBoolean(up);
    }
    
    @Override
    protected void processAdditionalArgs() 
    {
        if (queryForRecordsToUpdate == null && addnlArgs.length > 0)
        {
            queryForRecordsToUpdate = addnlArgs[0];
        }
        if (solrFieldContainingEncodedMarcRecord == null && addnlArgs.length > 1)
        {
            solrFieldContainingEncodedMarcRecord = addnlArgs[1];
        }
        solrSearcherProxy = new SolrSearcherProxy((SolrCoreProxy)solrProxy);
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
            logger.error("Error query must be of the form    field:term");
            System.out.println("Error: query must be of the form    field:term  " + queryForRecordsToUpdate);
            return;
        }
        try
        {
            int solrDocNums[] = solrSearcherProxy.getDocSet(queryparts[0], queryparts[1]);
            for (int docNum : solrDocNums)
            {
                DocumentProxy doc = solrSearcherProxy.getDocumentProxyBySolrDocNum(docNum);
                Record record = getRecordFromDocument(doc);
                if (output != null && record != null) 
                {
                    output.write(record);
                    System.out.flush();
                }
             }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  
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
            int solrDocNums[] = solrSearcherProxy.getDocSet(field, term);
            Object docSetIterator = solrSearcherProxy.getDocSetIterator(field, term);
            int count = 0;
            while (solrSearcherProxy.iteratorHasNext(docSetIterator))
            {
                DocumentProxy doc = solrSearcherProxy.iteratorGetNextDoc(docSetIterator);
    //            count ++;
    //            if (count == 100 || count == 1000 || count == 10000 || count % 10000 == 0)
    //            {
    //                System. out.println("Done handling "+ count +" record out of "+ totalSize);
    //            }
                    
                Record record = getRecordFromDocument(doc);
                    
                if (record != null)
                {
                    Map<String, Object> docMap = indexer.map(record, errors);
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
     * @param doc
     * @param map
     */
    protected void addExtraInfoFromDocToMap(DocumentProxy doc, Map<String, Object> docMap)
    {
        addExtraInfoFromDocToMap(doc, docMap, "fund_code_facet");
        addExtraInfoFromDocToMap(doc, docMap, "date_received_facet");   
        addExtraInfoFromDocToMap(doc, docMap, "marc_error");   
    }

    /**
     * Add extra information from a Solr Document to a map
     * @param doc Solr Document to pull information from
     * @param map Map to add information to
     * @param keyVal Value to add
     */
    protected void addExtraInfoFromDocToMap(DocumentProxy doc, Map<String, Object> map, String keyVal)
    {
        String fieldVals[] = null;
        fieldVals = doc.getValuesForField(keyVal);
        if (fieldVals != null && fieldVals.length > 0)
        {
            for (int i = 0; i < fieldVals.length; i++)
            {
                String fieldVal = fieldVals[i];
                addToMap(map, keyVal, fieldVal);
            }
        }           
    }

//    /**
//     * Return a Solr document from the index
//     * @param s SolrIndexSearcher to search
//     * @param SolrDocumentNum Number of documents to return
//     * @return SolrDocument 
//     * @throws IOException
//     */
//    public Document getDocument(SolrIndexSearcher s, int SolrDocumentNum) throws IOException
//    {
//        Document doc = s.doc(SolrDocumentNum);
//        return(doc);
//    }
    
    /**
     * Retrieve the marc information from the Solr document
     * @param doc SolrDocument from the index
     * @return marc4j Record
     * @throws IOException
     */
    public Record getRecordFromDocument(DocumentProxy doc) throws IOException
    {
        String fields[] = null;
        fields = doc.getValuesForField(solrFieldContainingEncodedMarcRecord);
        if (fields == null || fields.length == 0)
        {
            //System.err.println("field: "+ solrFieldContainingEncodedMarcRecord + " not found in solr document");
            logger.warn("field: "+ solrFieldContainingEncodedMarcRecord + " not found in solr document");
            return(null);
        }
        String marcRecordStr = null;
        try
        {
            if (fields[0] != null) marcRecordStr = fields[0];
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        int tries = 0;
        boolean tryAgain = false;
        do {
            try {
                tries++;
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
                if (tries == 1)
                {
                    tryAgain = true; 
                    marcRecordStr = normalizeUnicode(marcRecordStr);
                }
                else 
                {
                    me.printStackTrace();
                }
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        } while (tryAgain);
        return(null);
    }
    
    private String normalizeUnicode(String string)
    {
        Pattern pattern = Pattern.compile("(\\\\u([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]))|(#(29|30|31);)");
        Matcher matcher = pattern.matcher(string);
        StringBuffer result = new StringBuffer();
        int prevEnd = 0;
        while(matcher.find())
        {
            result.append(string.substring(prevEnd, matcher.start()));
            result.append(getChar(matcher.group()));
            prevEnd = matcher.end();
        }
        result.append(string.substring(prevEnd));
        string = result.toString();
        return(string);
    }
    
    private String getChar(String charCodePoint)
    {
        int charNum;
        if (charCodePoint.startsWith("\\u"))
        {
            charNum = Integer.parseInt(charCodePoint.substring(1), 16);
        }
        else
        {
            charNum = Integer.parseInt(charCodePoint.substring(1, 3));
        }
        String result = ""+((char)charNum);
        return(result);
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

//    /**
//     * find a specific marc record (using its id) in the solr index
//     * @param doc_id ID of the marc record to find
//     * @return if the item is in the index
//     */
//    private Record lookup(String doc_id)
//    {
//        RefCounted<SolrIndexSearcher> rs = solrCore.getSearcher();
//        SolrIndexSearcher s = rs.get();
//        Term t = new Term("id", doc_id);
//        int docNo;
//        Record rec = null;
//        try
//        {
//            docNo = s.getFirstMatch(t);
//            if (docNo > 0)
//            {
//                Document doc = getDocument(s, docNo);
//                rec = getRecordFromDocument(doc);
//            }
//            else
//            {
//                //TODO: construct this from the properties
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
//                String marcRecordStr = xPathExpression.evaluate(inputSource);
//                rec = getRecordFromXMLString(marcRecordStr);
//            }           
//        }
//        catch (IOException e)
//        {
//            // e.printStackTrace();
//            logger.error(e.getMessage());
//        }
//        catch (XPathExpressionException e)
//        {
//            // e.printStackTrace();
//            logger.error(e.getMessage());
//        }
//        return(rec);
//    }

    /**
     * Update a document in the Solr index
     * @param map Values of the "new" marc record
     */
    public void update(Map<String, Object> map)
    { 
        try {
            String docStr = solrProxy.addDoc(map, verbose, true);
            if (verbose)
            {
 //               logger.info(record.toString());
                logger.info(docStr);
            }

        } 
        catch (IOException ioe) 
        {
            //System.err.println("Couldn't add document");
            logger.error("Couldn't add document: " + ioe.getMessage());
            //e.printStackTrace();
 //           logger.error("Control Number " + record.getControlNumber(), ioe);
        }                
    }
    

    /*
     * @param args
     */
    public static void main(String[] args)
    {
        String newArgs[] = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = "NONE";
        
        SolrReIndexer reader = null;
        reader = new SolrReIndexer();
        reader.init(newArgs);
          
        reader.handleAll();
        
        reader.finish();
//        System.clearProperty("marc.path");
//        System.clearProperty("marc.source");
        System.exit(0);

    }

}
