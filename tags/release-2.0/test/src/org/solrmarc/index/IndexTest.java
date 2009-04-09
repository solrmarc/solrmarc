package org.solrmarc.index;

import static org.junit.Assert.*;
import org.junit.After;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.*;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.search.*;
//import org.apache.solr.core.*;
//import org.apache.solr.schema.*;
//import org.apache.solr.search.*;
import org.solrmarc.marc.MarcHandler;
import org.solrmarc.marc.MarcImporter;
import org.solrmarc.solr.DocumentProxy;
import org.solrmarc.solr.SolrCoreLoader;
import org.solrmarc.solr.SolrCoreProxy;
import org.solrmarc.solr.SolrSearcherProxy;
import org.solrmarc.tools.Utils;
import org.xml.sax.SAXException;

public abstract class IndexTest {
	
	protected MarcImporter importer;
    protected SolrCoreProxy solrCoreProxy;
	protected SolrSearcherProxy searcherProxy;

	protected static String docIDfname = "id";

    static Logger logger = Logger.getLogger(MarcImporter.class.getName());
	
    /**
     * Given the paths to a marc file to be indexed, the solr directory, and
     *  the path for the solr index, create the index from the marc file.
     * @param confPropFilename - name of config.properties file
     * @param testDataParentPath
     * @param testDataFname
     * @param solrPath - the directory holding the solr instance (think conf files)
     * @param solrDataDir - the data directory to hold the index
     */
	public void createIxInitVars(String configPropFilename, String solrPath, String solrDataDir, 
	                             String testDataParentPath, String testDataFname) 
			                     throws ParserConfigurationException, IOException, SAXException 
	{
        //System.err.println("test.solr.verbose = " + System.getProperty("test.solr.verbose"));
        if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
        {
            java.util.logging.Logger.getLogger("org.apache.solr").setLevel(java.util.logging.Level.SEVERE);
            Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
        }
        if (solrPath != null)  
        {
            System.setProperty("solr.path", solrPath);
            if (solrDataDir == null)
            {
                solrDataDir = solrPath + File.separator + "data";
            }
            System.setProperty("solr.data.dir", solrDataDir);
        }
        deleteDirContents(solrDataDir);
        if (configPropFilename != null)
        {
            importer = new MarcImporter(new String[]{configPropFilename, testDataParentPath + File.separator + testDataFname});
        }
        else 
        {
            importer = new MarcImporter(new String[]{testDataParentPath + File.separator + testDataFname});
        }
      //  importer.getSolrCoreProxy().deleteAllDocs();
        
        int numImported = importer.importRecords();       
        importer.finish();
        
        solrCoreProxy = importer.getSolrCoreProxy();
	}
	
//	private void deleteAllRecordsFromSolrIndex(String configPropFilename) throws IOException
//    {
//        if (configPropFilename != null)
//        {
//            importer = new MarcImporter(new String[]{configPropFilename, "NONE"});
//        }
//        else 
//        {
//            importer = new MarcImporter(new String[]{"NONE"});
//        }
//
//        importer = null;
//    }
//
    private SolrSearcherProxy getSearcherProxy()
	{
	    while (searcherProxy == null)
	    {
	        searcherProxy = new SolrSearcherProxy(solrCoreProxy);
	    }
	    return(searcherProxy);
	}
		
	/**
	 * ensure IndexSearcher and SolrCore are reset for next test
	 */
	@After
	public void tearDown()
	{
	    // avoid "already closed" exception
	    logger.info("Calling teardown to close importer");
        if (searcherProxy != null) 
        {
            logger.info("Closing searcher");
            searcherProxy.close();
            searcherProxy = null;
        }
        if (solrCoreProxy != null)
        {
            logger.info("Closing solr");
            solrCoreProxy.close();
            solrCoreProxy = null;
        }
	    
	//    importer.finish();
	    importer = null;
	}
	
//	/**
//	 * The import code expects to find these system properties populated.
//	 * 
//	 * set the properties used by Solr indexer.  If they are not already set
//	 *  as system properties, then use the passed parameters.  (This allows 
//	 *  testing within eclipse as well as testing on linux boxes using ant)
//	 */
//	protected static final void setImportSystemProps(String marc21FilePath, String solrPath,
//			String solrDataDir, String solrmarcPath, String siteSpecificPath) 
//	{
//		// crucial to set solr.path and marc.path properties
//        System.setProperty("marc.path", marc21FilePath);
//        System.setProperty("marc.source", "FILE");
//		System.setProperty("solr.path", solrPath);
//		System.setProperty("solr.data.dir", solrDataDir);
//		System.setProperty("solrmarc.path", solrmarcPath);
//		System.setProperty("solrmarc.site.path", siteSpecificPath);
//		
//		System.setProperty("marc.to_utf_8", "true");
//		System.setProperty("marc.default_encoding", "MARC8");
////		System.setProperty("marc.permissive", "true");
//	}
	
//	public static void copyFile(File sourceFile, File destFile) 
//			throws IOException {
//		if (!destFile.exists())
//		  destFile.createNewFile();
//		 
//		FileChannel source = null;
//		FileChannel destination = null;
//		try {
//			source = new FileInputStream(sourceFile).getChannel();
//			destination = new FileOutputStream(destFile).getChannel();
//			destination.transferFrom(source, 0, source.size());
//		}
//		finally {
//			if(source != null)
//				source.close();
//			if (destination != null)
//				destination.close();
//		}
//	}

	
	/**
	 * delete the directory indicated by the argument.
	 * @param dirPath - path of directory to be deleted.
	 */
	public static final void deleteDirContents(String dirPath) {
		File d = new File(dirPath);
		File[] files = d.listFiles();
		if (files != null)	
			for (File file: files)
			{	// recursively remove files and directories
				deleteDir(file.getAbsolutePath());
			}
	}
	
	/**
	 * delete the directory indicated by the argument.
	 * @param dirPath - path of directory to be deleted.
	 */
	public static final void deleteDir(String dirPath) {
		File d = new File(dirPath);
		File[] files = d.listFiles();
		if (files != null)	
			for (File file: files)
			{	// recursively remove files and directories
				deleteDir(file.getAbsolutePath());
			}
		d.delete();
	}

    /**
     * assert there is a single doc in the index with the value indicated
     * @param docId - the identifier of the SOLR/Lucene document
     * @param fldname - the field to be searched
     * @param fldVal - field value to be found
     * @param sis
     */
    public final void assertSingleResult(String docId, String fldName, String fldVal) 
            throws ParserConfigurationException, SAXException, IOException 
    {
        int solrDocNum = getSingleDocNum(fldName, fldVal);
        String recordID = getSearcherProxy().getIdStringBySolrDocNum(solrDocNum);
        assertTrue("doc \"" + docId + "\" does not have " + fldName + " of " + fldVal, recordID.equals(docId));
    
    }

    public final void assertZeroResults(String fldName, String fldVal) 
            throws ParserConfigurationException, SAXException, IOException
    {
        assertResultSize(fldName, fldVal, 0);
    }
    
	/**
	 * Get the Lucene document with the given id from the solr index at the
	 *  solrDataDir
	 * @param doc_id - the unique id of the lucene document in the index
	 * @param sis - SolrIndexSearcher for the relevant index
	 * @return the Lucene document matching the given id
	 */
	public final DocumentProxy getDocument(String doc_id)
		throws ParserConfigurationException, SAXException, IOException 
	{
		int solrDocNums[] = getSearcherProxy().getDocSet(docIDfname, doc_id);
		if (solrDocNums.length == 1)
			return getSearcherProxy().getDocumentBySolrDocNum(solrDocNums[0]);
		else
			return null;		
	}

	/**
	 * asserts that the document is present in the index
	 */
	public final void assertDocPresent(String doc_id)
		throws ParserConfigurationException, SAXException, IOException 
	{
	    int solrDocNums[] = getSearcherProxy().getDocSet(docIDfname, doc_id);
		assertTrue("Found no document with id \"" + doc_id + "\"", solrDocNums.length == 1);
	}

	/**
	 * asserts that the document is NOT present in the index
	 */
	public final void assertDocNotPresent(String doc_id)
			throws ParserConfigurationException, SAXException, IOException 
	{
        int solrDocNums[] = getSearcherProxy().getDocSet(docIDfname, doc_id);
        assertTrue("Found no document with id \"" + doc_id + "\"", solrDocNums.length == 0);
	}

//	/**
//	 * asserts that the given field is NOT present in the index
//	 * @param fldName - name of the field that shouldn't be in index
//	 * @param ir - an IndexReader for the relevant index
//	 */
//	@SuppressWarnings("unchecked")
//	public static final void assertFieldNotPresent(String fldName, IndexReader ir)
//			throws ParserConfigurationException, IOException, SAXException 
//	{
//	    Collection<String> fieldNames = ir.getFieldNames(IndexReader.FieldOption.ALL);
//	    if (fieldNames.contains(fldName))
//			fail("Field " + fldName + " found in index.");
//	}
//
//	/**
//	 * asserts that the given field is present in the index
//	 * @param fldName - name of the field that shouldn't be in index
//	 */
//	@SuppressWarnings("unchecked")
//	public static final void assertFieldPresent(String fldName, SolrIndexSearcher sis) 
//			throws ParserConfigurationException, IOException, SAXException 
//	{
//	    IndexReader ir = sis.getReader();
//	    assertFieldPresent(fldName, ir);
//	}
//
//	/**
//	 * asserts that the given field is present in the index
//	 * @param fldName - name of the field that shouldn't be in index
//	 * @param ir - IndexReader
//	 */
//	@SuppressWarnings("unchecked")
//	public static final void assertFieldPresent(String fldName, IndexReader ir)
//			throws ParserConfigurationException, IOException, SAXException 
//	{
//	    Collection<String> fieldNames = ir.getFieldNames(IndexReader.FieldOption.ALL);
//	    if (!fieldNames.contains(fldName))
//			fail("Field " + fldName + " not found in index");
//	}
//
	public final void assertFieldStored(String fldName) 
	        throws ParserConfigurationException, IOException, SAXException
    {
        assertTrue(fldName + " is not stored", solrCoreProxy.checkSchemaField(fldName, "field", "stored"));
    }

    public final void assertFieldNotStored(String fldName) 
            throws ParserConfigurationException, IOException, SAXException
    {
        assertTrue(fldName + " is stored", !solrCoreProxy.checkSchemaField(fldName, "field", "stored"));
    }

    public final void assertFieldIndexed(String fldName) 
            throws ParserConfigurationException, IOException, SAXException
    {
        assertTrue(fldName + " is not indexed", solrCoreProxy.checkSchemaField(fldName, "field", "indexed"));
    }

    public final void assertFieldNotIndexed(String fldName) 
            throws ParserConfigurationException, IOException, SAXException
    {
        assertTrue(fldName + " is indexed", !solrCoreProxy.checkSchemaField(fldName, "field", "indexed"));
    }

    public final void assertFieldTokenized(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
		assertTrue(fldName + " is not tokenized", solrCoreProxy.checkSchemaField(fldName, "type", "isTokenized"));
	}

    public final void assertFieldNotTokenized(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
	{
		assertTrue(fldName + " is tokenized", !solrCoreProxy.checkSchemaField(fldName, "type", "isTokenized"));
	}

    public final void assertFieldHasTermVectors(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
		assertTrue(fldName + " doesn't have termVectors", solrCoreProxy.checkSchemaField(fldName, "field", "storeTermVector"));
	}

    public final void assertFieldHasNoTermVectors(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
        assertTrue(fldName + " has termVectors", !solrCoreProxy.checkSchemaField(fldName, "field", "storeTermVector"));
    }

    public final void assertFieldOmitsNorms(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
        assertTrue(fldName + " has norms", solrCoreProxy.checkSchemaField(fldName, "field", "omitNorms"));
    }

    public final void assertFieldHasNorms(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
        assertTrue(fldName + " omits norms", !solrCoreProxy.checkSchemaField(fldName, "field", "omitNorms"));
	}

    public final void assertFieldMultiValued(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
	    assertTrue(fldName + " is not multiValued", solrCoreProxy.checkSchemaField(fldName, "field", "multiValued"));
    }

    public final void assertFieldNotMultiValued(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
        assertTrue(fldName + " is multiValued", !solrCoreProxy.checkSchemaField(fldName, "field", "multiValued"));
    }

//	public static final SchemaField getSchemaField(String fldName)
//			throws ParserConfigurationException, IOException, SAXException 
//	{
//		return solrCoreProxy.getSchema().getField(fldName);
//	}
//
//	private static final FieldType getFieldType(String fldName, SolrCore solrCore) {
//		return solrCore.getSchema().getFieldType(fldName);
//	}

	public final void assertDocHasFieldValue(String doc_id, String fldName, String fldVal)
			throws ParserConfigurationException, IOException, SAXException 
	{
		// TODO: repeatable field vs. not ...
		//  TODO: check for single occurrence of field value, even for repeatable field
		int solrDocNum = getSingleDocNum(docIDfname, doc_id);
		DocumentProxy doc = getSearcherProxy().getDocumentBySolrDocNum(solrDocNum);
		if (doc.hasFieldWithValue(fldName, fldVal)) return;
		fail("Field " + fldName + " did not contain value \"" + fldVal + "\" in doc " + doc_id);
	}

	public final void assertDocHasNoFieldValue(String doc_id, String fldName, String fldVal)
			throws ParserConfigurationException, IOException, SAXException 
	{
		// TODO: repeatable field vs. not ...
		// TODO: check for single occurrence of field value, even for repeatable field
        int solrDocNum = getSingleDocNum(docIDfname, doc_id);
        DocumentProxy doc = getSearcherProxy().getDocumentBySolrDocNum(solrDocNum);
        if (doc.hasFieldWithValue(fldName, fldVal)) 
            fail("Field " + fldName + " contained value \"" + fldVal + "\" in doc " + doc_id);
	}

	public final int getSingleDocNum(String fldName, String fldVal)
			throws ParserConfigurationException, SAXException, IOException 
	{
		int results[] = getSearcherProxy().getDocSet(fldName, fldVal);
		if (results.length != 1)
			fail("The index does not have a single document containing field " 
					+ fldName + " with value of \""+ fldVal +"\"");
		return results[0];
	}

	@SuppressWarnings("unchecked")
	public final void assertDocHasNoField(String doc_id, String fldName) 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    int solrDocNum = getSingleDocNum(docIDfname, doc_id);
	    DocumentProxy doc = getSearcherProxy().getDocumentBySolrDocNum(solrDocNum);
	    String vals[] = doc.getValuesForField(fldName);
	    if (vals == null || vals.length == 0) 
            return;
        fail("Field " + fldName + " found in doc \"" + doc_id + "\"");
	}

	public final void assertSearchResults(String fldName, String fldVal, Set<String> docIds) 
			throws ParserConfigurationException, SAXException, IOException
	{
        String resultDocIds[] = getSearcherProxy().getIdSet(fldName, fldVal);
        assertTrue("Expected " + docIds.size() + " documents for " + fldName + " search \"" 
                   + fldVal + "\" but got " + resultDocIds.length, docIds.size() == resultDocIds.length);
        
		String msg = fldName + " search \"" + fldVal + "\": ";
		
		for (String docId : docIds)
			assertDocInList(resultDocIds, docId, msg);
	}

	public final void assertFieldValues(String fldName, String fldVal, 
									Set<String> docIds) 
			throws ParserConfigurationException, SAXException, IOException
	{
		for (String docId : docIds)
			assertDocHasFieldValue(docId, fldName, fldVal); 
	}

	
	/**
	 * get all the documents matching the implied term search and check for
	 *  expected number of results
	 * @param fld
	 * @param text
	 * @param numExp the number of documents expected
	 * @param sis - SolrIndexSearcher for relevant index
	 * @return List of the Documents returned from the search
	 */
	public final void assertResultSize(String fld, String text, int numExp) 
			throws ParserConfigurationException, SAXException, IOException 
	{
        int num = getSearcherProxy().getNumberOfHits(fld, text); 
		assertTrue("Expected " + numExp + " documents for " + fld + " search \"" 
				+ text + "\" but got " + num, num == numExp);
	}

	public final String[] getDocIDList(String fld, String text)
	        throws ParserConfigurationException, SAXException, IOException 
	{
	    return searcherProxy.getIdSet(fld, text);
	}
	
	/**
	 * Given an index field name and value, return a list of Lucene Documents
	 *  that match the term query sent to the index
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param text - the string to be searched in the given field
	 * @param sis - SolrIndexSearcher for relevant index
	 * @return a list of Lucene Documents
	 */
	public final List<DocumentProxy> getAllMatchingDocs(String fld, String text) 
			throws ParserConfigurationException, SAXException, IOException 
	{
		List<DocumentProxy> docList = new ArrayList<DocumentProxy>();
	    int solrDocNums[] = getSearcherProxy().getDocSet(fld, text);
	    
	    for (int solrDocNum : solrDocNums)	        
	    {
	    	docList.add( getSearcherProxy().getDocumentBySolrDocNum(solrDocNum) );
	    }
	    return docList;
	}

//	/**
//	 * Given an index field name and value, return a list of Lucene Documents
//	 *  that match the term query sent to the index, sorted as indicated
//	 * @param fld - the name of the field to be searched in the lucene index
//	 * @param text - the string to be searched in the given field
//	 * @param sortfld - name of the field results should be sorted by
//	 * @param sis - SolrIndexSearcher for relevant index
//	 * @return a list of Lucene Documents sorted (ascending) per indicated field
//	 */
//	public static final List<DocumentProxy> getSortedDocs(String fld, String text, String sortfld, SolrIndexSearcher sis) 
//			throws ParserConfigurationException, SAXException, IOException {
//		List<Document> docList = new ArrayList<Document>();
//
//		Query query = QueryParsing.parseQuery(text, fld, sis.getSchema());
//		Hits hits = sis.search(query, new Sort(sortfld));
//		for (int i = 0; i < hits.length(); i++) {
//			docList.add(hits.doc(i));
//		}
//	    return docList;
//	}
	
//	public static final SolrIndexSearcher getSolrIndexSearcher(String solrPath, String solrDataDir)
//			throws ParserConfigurationException, IOException, SAXException {
//		return getSolrIndexSearcher(getSolrCore(solrPath, solrDataDir));
//	}
//
//	public static final SolrIndexSearcher getSolrIndexSearcher(SolrCore solrCore)
//			throws ParserConfigurationException, IOException, SAXException {
//		return solrCore.getSearcher().get();
//	}
//
//	public static final SolrCore getSolrCore(String solrPath, String solrDataDir)
//			throws ParserConfigurationException, IOException, SAXException {
//		SolrCoreProxy solrCoreProxy = SolrCoreLoader.loadCore(solrPath, solrDataDir, null, logger);
//		return (SolrCore) solrCoreProxy.getCore();
//	}

//	public static final IndexReader getIndexReader(String solrPath, String solrDataDir)
//			throws ParserConfigurationException, IOException, SAXException {
//	    return getSolrIndexSearcher(solrPath, solrDataDir).getReader();
//	}
//
//	public static final IndexReader getIndexReader(SolrCore solrCore)
//			throws ParserConfigurationException, IOException, SAXException {
//	    return getSolrIndexSearcher(solrCore).getReader();
//	}

	/**
	 * assert field is not tokenized, has no termVector and, if indexed, omitsNorm 
	 */
	public final void assertStringFieldProperties(String fldName) 
			throws ParserConfigurationException, IOException, SAXException 
	{
//	    assertFieldPresent(fldName);
        assertFieldNotTokenized(fldName);
        assertFieldHasNoTermVectors(fldName);
        // since omitNorms is only relevant if field is indexed,
        // assertFieldOmitsNorms fails if the field is NOT indexed as
        // default boolean value is false.
        if (solrCoreProxy.checkSchemaField(fldName, "field", "indexed")) 
            assertFieldOmitsNorms(fldName);
	}

	/**
	 * assert field is present, tokenized, has no termVectors
	 */
	public final void assertTextFieldProperties(String fldName) 
			throws ParserConfigurationException, IOException, SAXException 
	{
//		assertFieldPresent(fldName);
		assertFieldTokenized(fldName);
		assertFieldHasNoTermVectors(fldName);
	}

	public final void assertDisplayFieldProperties(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
	    // assertFieldPresent(fldName);
        assertFieldNotTokenized(fldName);
        assertFieldNotIndexed(fldName);
        assertFieldStored(fldName);
	    assertFieldHasNoTermVectors(fldName);
    }

    public final void assertFacetFieldProperties(String fldName) 
        throws ParserConfigurationException, IOException, SAXException 
    {
        // assertFieldPresent(fldName);
        assertFieldNotTokenized(fldName);
        assertFieldIndexed(fldName);
  //      assertFieldStored(fldName);
        assertFieldHasNoTermVectors(fldName);
    }

	public final void assertDocInList(String[] docIdList, String doc_id, String msgPrefix) 
			throws ParserConfigurationException, SAXException, IOException 
	{
		for (String id : docIdList)
		{
		    if (id.equals(doc_id))  return;
		}
		fail(msgPrefix + "doc \"" + doc_id + "\" missing from list");
	}

}