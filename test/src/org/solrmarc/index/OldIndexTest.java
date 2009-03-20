package org.solrmarc.index;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.solrmarc.marc.MarcImporter;
import org.solrmarc.solr.DocumentProxy;
//import org.solrmarc.solr.SolrCoreLoader;
//import org.solrmarc.solr.SolrCoreProxy;
import org.xml.sax.SAXException;

public abstract class OldIndexTest extends IndexTest{

    protected Object solrCore = null;
    protected Object sis = null;
    
    static Logger logger = Logger.getLogger(MarcImporter.class.getName());
    
//    /**
//     * Given the paths to a marc file to be indexed, the solr directory, and
//     *  the path for the solr index, create the index from the marc file.
//     * @param marc21FilePath
//     * @param confPropFilePath - path to a config.properties file
//     * @param solrPath - the directory holding the solr instance (think conf files)
//     * @param solrDataDir - the data directory to hold the index
//     * @param solrmarcPath - solrmarc top level directory
//     * @param siteSpecPath - site specific directory holding the _index.properties
//     */
//    public static final void createNewTestIndex(String marc21FilePath, String confPropFilePath,
//            String solrPath, String solrDataDir, String solrmarcPath, String siteSpecPath) throws IOException 
//    {
//        // set system properties used by importer
//        setImportSystemProps(marc21FilePath, solrPath, solrDataDir, solrmarcPath, siteSpecPath);
//        // ensure we start with an empty index
//        String ixDir = System.getProperty("solr.data.dir") + File.separator + "index";
//        deleteDir(ixDir);
//
//        String[] args = new String[1];
//        args[0] = confPropFilePath;
//        java.util.logging.Logger.getLogger("org.apache.solr").setLevel(java.util.logging.Level.SEVERE);
//        setLog4jLogLevel(org.apache.log4j.Level.WARN);
//        
//        MarcImporter importer = new MarcImporter(args);
//        int numImported = importer.importRecords();
//        int numDeleted = importer.deleteRecords();
//        importer.finish();
//    }
//

//    @SuppressWarnings("unchecked")
//    private static void setLog4jLogLevel(org.apache.log4j.Level newLevel)
//    {
//        Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
//        Enumeration<Logger> enLogger = rootLogger.getLoggerRepository().getCurrentLoggers();
//        Logger tmpLogger = null;
//        /* If logger is root, then need to loop through all loggers under root
//        * and change their logging levels too.  Also, skip sql loggers so
//        they
//        * do not get effected.
//        */
//        while(enLogger.hasMoreElements())
//        {
//            tmpLogger = (Logger)(enLogger.nextElement());
//            tmpLogger.setLevel(newLevel);
//        }
//        Enumeration<Appender> enAppenders = rootLogger.getAllAppenders();
//        Appender appender;
//        while(enAppenders.hasMoreElements())
//        {
//            appender = (Appender)enAppenders.nextElement();
//            
//            if(appender instanceof AsyncAppender)
//            {
//                AsyncAppender asyncAppender = (AsyncAppender)appender;
//                asyncAppender.activateOptions();
////                rfa = (RollingFileAppender)asyncAppender.getAppender("R");
////                rfa.activateOptions();
////                ca = (ConsoleAppender)asyncAppender.getAppender("STDOUT");
////                ca.activateOptions();
//            }
//        }
//
//    }
//    
//    /**
//     * ensure IndexSearcher and SolrCore are reset for next test
//     */
//    @After
//    public void tearDown()
//    {
//        // avoid "already closed" exception
//        logger.info("Calling teardown to close Solr");
//        try
//        {
//            Thread.sleep(2000);
//        }
//        catch (InterruptedException e1)
//        {
//            e1.printStackTrace();
//        }
//        
//        if (sis != null) 
//        {
//            try {
//                logger.info("Closing searcher");
//                sis.close();
//                sis = null;
//            }
//            catch (IOException e)
//            {
//            }
//        }
//        if (solrCore != null)
//        {
//            logger.info("Closing solr");
//            solrCore.close();
//            solrCore = null;
//        }
//    }
//    
//    /**
//     * The import code expects to find these system properties populated.
//     * 
//     * set the properties used by Solr indexer.  If they are not already set
//     *  as system properties, then use the passed parameters.  (This allows 
//     *  testing within eclipse as well as testing on linux boxes using ant)
//     */
//    protected static final void setImportSystemProps(String marc21FilePath, String solrPath,
//            String solrDataDir, String solrmarcPath, String siteSpecificPath) 
//    {
//        // crucial to set solr.path and marc.path properties
//        System.setProperty("marc.path", marc21FilePath);
//        System.setProperty("marc.source", "FILE");
//        System.setProperty("solr.path", solrPath);
//        System.setProperty("solr.data.dir", solrDataDir);
//        System.setProperty("solrmarc.path", solrmarcPath);
//        System.setProperty("solrmarc.site.path", siteSpecificPath);
//        
//        System.setProperty("marc.to_utf_8", "true");
//        System.setProperty("marc.default_encoding", "MARC8");
////      System.setProperty("marc.permissive", "true");
//    }
//    
//    public static void copyFile(File sourceFile, File destFile) 
//            throws IOException {
//        if (!destFile.exists())
//          destFile.createNewFile();
//         
//        FileChannel source = null;
//        FileChannel destination = null;
//        try {
//            source = new FileInputStream(sourceFile).getChannel();
//            destination = new FileOutputStream(destFile).getChannel();
//            destination.transferFrom(source, 0, source.size());
//        }
//        finally {
//            if(source != null)
//                source.close();
//            if (destination != null)
//                destination.close();
//        }
//    }
//
//    
//    /**
//     * delete the directory indicated by the argument.
//     * @param dirPath - path of directory to be deleted.
//     */
//    public static final void deleteDirContents(String dirPath) {
//        File d = new File(dirPath);
//        File[] files = d.listFiles();
//        if (files != null)  
//            for (File file: files)
//            {   // recursively remove files and directories
//                deleteDir(file.getAbsolutePath());
//            }
//    }
//    
//    /**
//     * delete the directory indicated by the argument.
//     * @param dirPath - path of directory to be deleted.
//     */
//    public static final void deleteDir(String dirPath) {
//        File d = new File(dirPath);
//        File[] files = d.listFiles();
//        if (files != null)  
//            for (File file: files)
//            {   // recursively remove files and directories
//                deleteDir(file.getAbsolutePath());
//            }
//        d.delete();
//    }
//
    /**
     * assert there is a single doc in the index with the value indicated
     * @param docId - the identifier of the SOLR/Lucene document
     * @param fldname - the field to be searched
     * @param fldVal - field value to be found
     * @param sis
     */
    public final void assertSingleResult(String docId, String fldName, String fldVal, Object sis) 
            throws ParserConfigurationException, SAXException, IOException {
        super.assertSingleResult(docId, fldName, fldVal);
    }

    public final void assertZeroResults(String fldName, String fldVal, Object sis) 
            throws ParserConfigurationException, SAXException, IOException
    {
        super.assertZeroResults(fldName, fldVal);
    }
    
    /**
     * Get the Lucene document with the given id from the solr index at the
     *  solrDataDir
     * @param doc_id - the unique id of the lucene document in the index
     * @param sis - Object for the relevant index
     * @return the Lucene document matching the given id
     */
    public final DocumentProxy getDocument(String doc_id, Object sis)
        throws ParserConfigurationException, SAXException, IOException {
        return super.getDocument(doc_id);     
    }

    /**
     * asserts that the document is present in the index
     */
    public final void assertDocPresent(String doc_id, Object sis)
        throws ParserConfigurationException, SAXException, IOException {
        super.assertDocPresent(doc_id);
    }

    /**
     * asserts that the document is NOT present in the index
     */
    public final void assertDocNotPresent(String doc_id, Object sis)
            throws ParserConfigurationException, SAXException, IOException {
        super.assertDocNotPresent(doc_id);
    }

//    /**
//     * asserts that the given field is NOT present in the index
//     * @param fldName - name of the field that shouldn't be in index
//     * @param ir - an IndexReader for the relevant index
//     */
//    @SuppressWarnings("unchecked")
//    public final void assertFieldNotPresent(String fldName, Object ir)
//            throws ParserConfigurationException, IOException, SAXException {
//        // currently umimplemented
//    }

    /**
     * asserts that the given field is present in the index
     * @param fldName - name of the field that shouldn't be in index
     */
    @SuppressWarnings("unchecked")
    public final void assertFieldPresent(String fldName, Object sis) 
            throws ParserConfigurationException, IOException, SAXException {
        // currently umimplemented
    }

//    /**
//     * asserts that the given field is present in the index
//     * @param fldName - name of the field that shouldn't be in index
//     * @param ir - IndexReader
//     */
//    @SuppressWarnings("unchecked")
//    public final void assertFieldPresent(String fldName, IndexReader ir)
//            throws ParserConfigurationException, IOException, SAXException {
//        Collection<String> fieldNames = ir.getFieldNames(IndexReader.FieldOption.ALL);
//        if (!fieldNames.contains(fldName))
//            fail("Field " + fldName + " not found in index");
//    }

    public final void assertFieldStored(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldStored(fldName);
    }

    public final void assertFieldNotStored(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldNotStored(fldName);
    }

    public final void assertFieldIndexed(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldIndexed(fldName);
    }

    public final void assertFieldNotIndexed(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException,   SAXException {
        super.assertFieldNotIndexed(fldName);
    }

    public final void assertFieldTokenized(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException,   SAXException {
        super.assertFieldTokenized(fldName);
    }

    public final void assertFieldNotTokenized(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldNotTokenized(fldName);
    }

    public final void assertFieldHasTermVectors(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldHasTermVectors(fldName);
    }

    public final void assertFieldHasNoTermVectors(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldHasNoTermVectors(fldName);
    }

    public final void assertFieldOmitsNorms(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldOmitsNorms(fldName);
    }

    public final void assertFieldHasNorms(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldHasNorms(fldName);
    }

    public final void assertFieldMultiValued(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldMultiValued(fldName);
    }

    public final void assertFieldNotMultiValued(String fldName, Object solrCore) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFieldNotMultiValued(fldName);
    }

//    public final SchemaField getSchemaField(String fldName, Object solrCore)
//            throws ParserConfigurationException, IOException, SAXException {
//        return solrCore.getSchema().getField(fldName);
//    }

//    private static final FieldType getFieldType(String fldName, Object solrCore) {
//        return solrCore.getSchema().getFieldType(fldName);
//    }

    public final void assertDocHasFieldValue(String doc_id, String fldName, String fldVal, Object sis)
            throws ParserConfigurationException, IOException, SAXException {
        // TODO: repeatable field vs. not ...
        //  TODO: check for single occurrence of field value, even for repeatable field
        super.assertDocHasFieldValue(doc_id, fldName, fldVal);
    }

    public final void assertDocHasNoFieldValue(String doc_id, String fldName, String fldVal, Object sis)
            throws ParserConfigurationException, IOException, SAXException {
        // TODO: repeatable field vs. not ...
        // TODO: check for single occurrence of field value, even for repeatable field
        super.assertDocHasNoFieldValue(doc_id, fldName, fldVal);
    }

//    public static final Document getSingleDoc(String fldName, String fldVal, Object sis)
//            throws ParserConfigurationException, SAXException, IOException {
//        List<Document> docList = getAllMatchingDocs(fldName, fldVal, sis);
//        if (docList.size() != 1)
//            fail("The index does not have a single document containing field " 
//                    + fldName + " with value of \""+ fldVal +"\"");
//        return docList.get(0);
//    }

    @SuppressWarnings("unchecked")
    public final void assertDocHasNoField(String doc_id, String fldName, Object sis) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertDocHasNoField(doc_id, fldName);
    }

    public final void assertSearchResults(String fldName, String fldVal, 
                                    Set<String> docIds, Object sis) 
            throws ParserConfigurationException, SAXException, IOException
    {
        super.assertSearchResults(fldName, fldVal, docIds);
    }

    public final void assertFieldValues(String fldName, String fldVal, 
                                    Set<String> docIds, Object sis) 
            throws ParserConfigurationException, SAXException, IOException
    {
        super.assertFieldValues(fldName, fldVal, docIds);
    }

    
    /**
     * get all the documents matching the implied term search and check for
     *  expected number of results
     * @param fld
     * @param text
     * @param numExp the number of documents expected
     * @param sis - Object for relevant index
     * @return List of the Documents returned from the search
     */
    public final void assertResultSize(String fld, String text, int numExp, Object sis) 
            throws ParserConfigurationException, SAXException, IOException {
        super.assertResultSize(fld, text, numExp);
    }

//    /**
//     * Given an index field name and value, return a list of Lucene Documents
//     *  that match the term query sent to the index
//     * @param fld - the name of the field to be searched in the lucene index
//     * @param text - the string to be searched in the given field
//     * @param sis - Object for relevant index
//     * @return a list of Lucene Documents
//     */
//    public final String[] getAllMatchingDocs(String fld, String text, Object sis) 
//            throws ParserConfigurationException, SAXException, IOException {
//        return super.getAllMatchingDocs(fld, text)
//    }

    /**
     * Given an index field name and value, return a list of Lucene Documents
     *  that match the term query sent to the index, sorted as indicated
     * @param fld - the name of the field to be searched in the lucene index
     * @param text - the string to be searched in the given field
     * @param sortfld - name of the field results should be sorted by
     * @param sis - Object for relevant index
     * @return a list of Lucene Documents sorted (ascending) per indicated field
     */
//    public static final List<Document> getSortedDocs(String fld, String text, String sortfld, Object sis) 
//            throws ParserConfigurationException, SAXException, IOException {
//        List<Document> docList = new ArrayList<Document>();
//
//        Query query = QueryParsing.parseQuery(text, fld, sis.getSchema());
//        Hits hits = sis.search(query, new Sort(sortfld));
//        for (int i = 0; i < hits.length(); i++) {
//            docList.add(hits.doc(i));
//        }
//        return docList;
//    }
    
//    public static final Object getObject(String solrPath, String solrDataDir)
//            throws ParserConfigurationException, IOException, SAXException {
//        return getObject(getSolrCore(solrPath, solrDataDir));
//    }
//
//    public static final Object getObject(Object solrCore)
//            throws ParserConfigurationException, IOException, SAXException {
//        return solrCore.getSearcher().get();
//    }

//    public static final Object getSolrCore(String solrPath, String solrDataDir)
//            throws ParserConfigurationException, IOException, SAXException {
//        SolrCoreProxy solrCoreProxy = SolrCoreLoader.loadCore(solrPath, solrDataDir, null, logger);
//        return (Object) solrCoreProxy.getCore();
//    }

//  public static final IndexReader getIndexReader(String solrPath, String solrDataDir)
//          throws ParserConfigurationException, IOException, SAXException {
//      return getObject(solrPath, solrDataDir).getReader();
//  }
//
//  public static final IndexReader getIndexReader(SolrCore solrCore)
//          throws ParserConfigurationException, IOException, SAXException {
//      return getObject(solrCore).getReader();
//  }

    /**
     * assert field is not tokenized, has no termVector and, if indexed, omitsNorm 
     */
    public final void assertStringFieldProperties(String fldName, Object solrCore, Object sis) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertStringFieldProperties(fldName);
    }

    /**
     * assert field is not tokenized, has no termVector and, if indexed, omitsNorm 
     */
    public final void assertDisplayFldProps(String fldName, Object solrCore, Object sis) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertDisplayFieldProperties(fldName);
    }

    /**
     * assert field is not tokenized, has no termVector and, if indexed, omitsNorm 
     */
    public final void assertFacetFldProps(String fldName, Object solrCore, Object sis) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertFacetFieldProperties(fldName);
    }

    /**
     * assert field is present, tokenized, has no termVectors
     */
    public final void assertTextFieldProperties(String fldName, Object solrCore, Object sis) 
            throws ParserConfigurationException, IOException, SAXException {
        super.assertTextFieldProperties(fldName);
    }

    public final void assertDocInList(String docIdList[], String doc_id, String msgPrefix, Object sis) 
            throws ParserConfigurationException, SAXException, IOException {
        super.assertDocInList(docIdList, doc_id, msgPrefix);
    }

}