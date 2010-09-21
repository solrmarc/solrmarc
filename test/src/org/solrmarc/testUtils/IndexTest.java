package org.solrmarc.testUtils;

import static org.junit.Assert.*;
import org.junit.After;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.*;
import org.solrmarc.marc.MarcImporter;
import org.solrmarc.solr.*;
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
        logger.debug("System.getProperty(\"os.name\") : "+System.getProperty("os.name"));
        if (!System.getProperty("os.name").toLowerCase().contains("win"))
        {
            logger.info("Calling Delete Dir Contents");
            deleteDirContents(solrDataDir);
        }
        importer = new MarcImporter();
        if (configPropFilename != null)
        {
            importer.init(new String[]{configPropFilename, testDataParentPath + File.separator + testDataFname});
        }
        else 
        {
            importer.init(new String[]{testDataParentPath + File.separator + testDataFname});
        }
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        {
            logger.info("Calling Delete All Docs");
            importer.getSolrProxy().deleteAllDocs();
        }
        
        int numImported = importer.importRecords();       
        importer.finish();
        
        solrCoreProxy = (SolrCoreProxy)importer.getSolrProxy();
        solrCoreProxy.commit(false);
        searcherProxy = new SolrSearcherProxy(solrCoreProxy);
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
    protected SolrSearcherProxy getSearcherProxy()
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
		logger.debug("Deleting: "+ d.getAbsolutePath());
		d.delete();
	}

    /**
     * assert there is a single doc in the index with the value indicated
     * @param docId - the identifier of the SOLR/Lucene document
     * @param fldname - the field to be searched
     * @param fldVal - field value to be found
     */
    public final void assertSingleResult(String docId, String fldName, String fldVal) 
            throws ParserConfigurationException, SAXException, IOException 
    {
        int solrDocNum = getSingleDocNum(fldName, fldVal);
        String recordID = getSearcherProxy().getDocIdFromSolrDocNum(solrDocNum, docIDfname);
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
	 * @return the Lucene document matching the given id
	 */
	public final DocumentProxy getDocument(String doc_id)
		throws ParserConfigurationException, SAXException, IOException 
	{
		int solrDocNums[] = getSearcherProxy().getDocSet(docIDfname, doc_id);
		if (solrDocNums.length == 1)
			return getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNums[0]);
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
		DocumentProxy doc = getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum);
		if (doc.hasFieldWithValue(fldName, fldVal)) return;
		fail("Field " + fldName + " did not contain value \"" + fldVal + "\" in doc " + doc_id);
	}

	public final void assertDocHasNoFieldValue(String doc_id, String fldName, String fldVal)
			throws ParserConfigurationException, IOException, SAXException 
	{
		// TODO: repeatable field vs. not ...
		// TODO: check for single occurrence of field value, even for repeatable field
        int solrDocNum = getSingleDocNum(docIDfname, doc_id);
        DocumentProxy doc = getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum);
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
	    DocumentProxy doc = getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum);
	    String vals[] = doc.getValuesForField(fldName);
	    if (vals == null || vals.length == 0) 
            return;
        fail("Field " + fldName + " found in doc \"" + doc_id + "\"");
	}

	/**
	 * Do a search for the implied term query and assert the search results
	 *  have docIds that are an exact match of the set of docIds passed in
	 * @param fldName - name of the field to be searched
	 * @param fldVal - value of the field to be searched
	 * @param docIds - Set of doc ids expected to be in the results
	 */
	public final void assertSearchResults(String fldName, String fldVal, Set<String> docIds) 
			throws ParserConfigurationException, SAXException, IOException
	{
        String resultDocIds[] = getSearcherProxy().getDocIdsFromSearch(fldName, fldVal, docIDfname);
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
     * @param fldName - the field to be searched
     * @param fldVal - field value to be found
	 * @param numExp the number of documents expected
	 * @return List of the Documents returned from the search
	 */
	public final void assertResultSize(String fldName, String fldVal, int numExp) 
			throws ParserConfigurationException, SAXException, IOException 
	{
        int num = getSearcherProxy().getNumberOfHits(fldName, fldVal); 
		assertTrue("Expected " + numExp + " documents for " + fldName + " search \"" 
				+ fldVal + "\" but got " + num, num == numExp);
	}

	/**
	 * get the ids of all the documents matching the implied term search
     * @param fldName - the field to be searched
     * @param fldVal - field value to be found
	 */
	public final String[] getDocIDList(String fldName, String fldVal)
	        throws ParserConfigurationException, SAXException, IOException 
	{
	    return searcherProxy.getDocIdsFromSearch(fldName, fldVal, docIDfname);
	}
	
	/**
	 * Given an index field name and value, return a list of Lucene Documents
	 *  that match the term query sent to the index
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @return a list of Lucene Documents
	 */
	public final List<DocumentProxy> getAllMatchingDocs(String fld, String value) 
			throws ParserConfigurationException, SAXException, IOException 
	{
		List<DocumentProxy> docList = new ArrayList<DocumentProxy>();
	    int solrDocNums[] = getSearcherProxy().getDocSet(fld, value);
	    
	    for (int solrDocNum : solrDocNums)	        
	    {
	    	docList.add( getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum) );
	    }
	    return docList;
	}


	/**
	 * return the number of docs that match the implied term query
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 */
	public int getNumMatchingDocs(String fld, String value)
			throws IOException
	{
        return getSearcherProxy().getNumberOfHits(fld, value);
	}

	/**
	 * Given an index field name and value, return a list of Documents
	 *  that match the term query sent to the index, sorted in ascending
	 *  order per the sort fld
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 *   (ascending)
	 * @return a sorted list of DocumentProxy objects
	 */
	public final List<DocumentProxy> getAscSortDocs(String fld, String value, String sortfld) 
			throws IOException, InstantiationException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException 
	{
        int solrDocNums[] = getSearcherProxy().getAscSortDocNums(fld, value, sortfld);
        return getDocProxiesFromDocNums(solrDocNums);
	}
	
	/**
	 * Given an index field name and value, return a list of Documents
	 *  that match the term query sent to the index, sorted in descending
	 *  order per the sort fld
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 *   (descending)
	 * @return a sorted list of DocumentProxy objects
	 */
	public final List<DocumentProxy> getDescSortDocs(String fld, String value, String sortfld) 
            throws IOException, InstantiationException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException 
	{
        int solrDocNums[] = getSearcherProxy().getDescSortDocNums(fld, value, sortfld);
        return getDocProxiesFromDocNums(solrDocNums);
	}
		
	/**
	 * Given an index field name and value, return a list of Lucene Documents
	 *  numbers that match the term query sent to the index, sorted in ascending
	 *  order per the sort fld
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 *   (ascending)
	 * @return an array of int that are sorted (ascending) solr document 
	 * numbers
	 */
	public final int[] getAscSortDocNums(String fld, String value, String sortfld) 
			throws IOException, NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException
	{
        return getSearcherProxy().getAscSortDocNums(fld, value, sortfld);
	}
	
	/**
	 * Given an index field name and value, return a list of Lucene Documents
	 *  numbers that match the term query sent to the index, sorted in descending
	 *  order per the sort fld
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 *   (descending)
	 * @return an array of int that are sorted (descending) solr document 
	 * numbers
	 */
	public final int[] getDescSortDocNums(String fld, String value, String sortfld) 
            throws IOException, InstantiationException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException 
	{
        return getSearcherProxy().getDescSortDocNums(fld, value, sortfld);
	}
	
	
	/**
	 * given an array of Solr document numbers as int, return a List of
	 * DocumentProxy objects corresponding to the Solr doc nums.  Order is
	 * maintained.
	 */
	private List<DocumentProxy> getDocProxiesFromDocNums(int[] solrDocNums) 
		throws IOException 
	{
        List<DocumentProxy> docProxyList = new ArrayList<DocumentProxy>();
        for (int solrDocNum : solrDocNums)
            docProxyList.add( getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum) );
        return docProxyList;
	}
	
	/**
	 * assert field is not tokenized, has no termVector and, if indexed, omitsNorm 
	 */
	public final void assertStringFieldProperties(String fldName) 
			throws ParserConfigurationException, IOException, SAXException 
	{
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
		assertFieldTokenized(fldName);
		assertFieldHasNoTermVectors(fldName);
	}

	public final void assertDisplayFieldProperties(String fldName) 
            throws ParserConfigurationException, IOException, SAXException 
    {
        assertFieldNotTokenized(fldName);
        assertFieldNotIndexed(fldName);
        assertFieldStored(fldName);
	    assertFieldHasNoTermVectors(fldName);
    }

    public final void assertFacetFieldProperties(String fldName) 
        throws ParserConfigurationException, IOException, SAXException 
    {
        assertFieldNotTokenized(fldName);
        assertFieldIndexed(fldName);
  //      assertFieldStored(fldName);
        assertFieldHasNoTermVectors(fldName);
    }

    /**
     * search fields are tokenized, indexed, not stored, and have norms
     */
	public void assertSearchFldOneValProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldNotMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
        // TODO: term vectors used in more like this and highlighting?
	}
	
    /**
     * search fields are tokenized, indexed, not stored, and have norms
     */
	public void assertSearchFldMultValProps(String fldName) 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertTextFieldProperties(fldName);
		assertFieldHasNorms(fldName);
		assertFieldMultiValued(fldName);
		assertFieldNotStored(fldName);
		assertFieldIndexed(fldName);
        // TODO: term vectors used in more like this and highlighting?
	}

	/**
	 * sort fields are indexed and not stored nor multivalued
	 */
	public void assertSortFldProps(String sortFldName) 
	    throws ParserConfigurationException, IOException, SAXException
	{
		assertFieldHasNoTermVectors(sortFldName);
		assertFieldOmitsNorms(sortFldName);
	    assertFieldIndexed(sortFldName);
	    assertFieldNotStored(sortFldName);
        assertFieldNotMultiValued(sortFldName);
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