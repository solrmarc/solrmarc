package org.solrmarc.index;

import static org.junit.Assert.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.solr.core.*;
import org.apache.solr.schema.*;
import org.apache.solr.search.*;
import org.solrmarc.marc.MarcImporter;
import org.solrmarc.solr.SolrCoreLoader;
import org.solrmarc.solr.SolrCoreProxy;
import org.xml.sax.SAXException;

public abstract class IndexTest {

	// Note:  the hardcodings below are only used when the tests are
	//  invoked without the properties set
	//   the properties ARE set when the tests are invoke via ant.
	
	protected String solrmarcPath = System.getProperty("solrmarc.path");
	{
		if (solrmarcPath == null)
			solrmarcPath = "solrmarcProjectDir"; 
	}
	protected String siteSpecificPath = System.getProperty("solrmarc.site.path");
	{
		if (siteSpecificPath == null)
			siteSpecificPath = "yourSiteSpecificDir"; 
	}
	
	protected String configPropFile = siteSpecificPath + File.separator +"your_config.properties";
	
	protected String solrPath = System.getProperty("solr.path");
	{
		if (solrPath == null)
			solrPath = "yourSolrDir";
	}

	protected String testDir = siteSpecificPath + File.separator + "test";
	protected String testDataParentPath = testDir + File.separator + "data";
	protected String testDataPath = testDataParentPath + File.separator + "allfieldsTests.mrc";
	protected String solrDataDir = System.getProperty("solr.data.dir");
	{
		if (solrDataDir == null)
			solrDataDir = testDir + File.separator + "solr" + File.separator + "data";
	}
	protected SolrCore solrCore;
	protected SolrIndexSearcher sis;

	protected static String docIDfname = "id";

    static Logger logger = Logger.getLogger(MarcImporter.class.getName());
	
	public void createIxInitVars(String testDataFname) 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createNewTestIndex(testDataParentPath + File.separator + testDataFname, configPropFile, solrPath, solrDataDir, solrmarcPath, siteSpecificPath);
		solrCore = getSolrCore(solrPath, solrDataDir);
		sis = getSolrIndexSearcher(solrCore);
	}
	
	/**
	 * sets up the initial loading for tests, and does the field properties
	 *  assertions on an expected string, multivalued field properties
	 */
	protected final void setupMultiValStrFldTests(String fldName, String datafile) 
			throws IOException, ParserConfigurationException, SAXException {
		createNewTestIndex(testDataParentPath + datafile, configPropFile, solrPath, solrDataDir, solrmarcPath, siteSpecificPath);
		SolrCore solrCore = getSolrCore(solrPath, solrDataDir);
		assertStringFieldProperties(fldName, solrCore);
		assertFieldMultiValued(fldName, solrCore);
	}

	/**
	 * assert there is a single doc in the index with the value indicated
	 * @param docId - the identifier of the SOLR/Lucene document
	 * @param fldname - the field to be searched
	 * @param fldVal - field value to be found
	 * @param sis
	 */
	public final void assertSingleResult(String docId, String fldName, String fldVal, SolrIndexSearcher sis) 
			throws ParserConfigurationException, SAXException, IOException {
		Document doc = getSingleDoc(fldName, fldVal, sis);
		assertTrue("doc \"" + docId + "\" does not have " + fldName + " of " + fldVal, doc.getValues(docIDfname)[0].equals(docId));
	
	}

	public final void assertZeroResults(String fldName, String fldVal, SolrIndexSearcher sis) 
			throws ParserConfigurationException, SAXException, IOException
	{
		assertResultSize(fldName, fldVal, 0, sis);
	}
	
	/**
	 * Given the paths to a marc file to be indexed, the solr directory, and
	 *  the path for the solr index, create the index from the marc file.
	 * @param marc21FilePath
	 * @param confPropFilePath - path to a config.properties file
	 * @param solrPath - the directory holding the solr instance (think conf files)
	 * @param solrDataDir - the data directory to hold the index
	 * @param solrmarcPath - solrmarc top level directory
	 * @param siteSpecPath - site specific directory holding the _index.properties
	 */
	public static final void createNewTestIndex(String marc21FilePath, String confPropFilePath,
			String solrPath, String solrDataDir, String solrmarcPath, String siteSpecPath) throws IOException 
	{
		// set system properties used by importer
		setImportSystemProps(marc21FilePath, solrPath, solrDataDir, solrmarcPath, siteSpecPath);
		// ensure we start with an empty index
		String ixDir = System.getProperty("solr.data.dir") + File.separator + "index";
		deleteDir(ixDir);

		String[] args = new String[3];
		args[0] = confPropFilePath;
		String transMapSubDir = File.separator + "translation_maps";
		args[1] = siteSpecPath + transMapSubDir;
		args[2] = solrmarcPath + transMapSubDir;
	    MarcImporter importer = new MarcImporter(args);
	    int numImported = importer.importRecords();
	    int numDeleted = importer.deleteRecords();
	    importer.finish();
	}

	/**
	 * The import code expects to find these system properties populated.
	 * 
	 * set the properties used by Solr indexer.  If they are not already set
	 *  as system properties, then use the passed parameters.  (This allows 
	 *  testing within eclipse as well as testing on linux boxes using ant)
	 */
	protected static final void setImportSystemProps(String marc21FilePath, String solrPath,
			String solrDataDir, String solrmarcPath, String siteSpecificPath) 
	{
		// crucial to set solr.path and marc.path properties
		System.setProperty("marc.path", marc21FilePath);
		System.setProperty("solr.path", solrPath);
		System.setProperty("solr.data.dir", solrDataDir);
		System.setProperty("solrmarc.path", solrmarcPath);
		System.setProperty("solrmarc.site.path", siteSpecificPath);
		
		System.setProperty("marc.to_utf_8", "true");
		System.setProperty("marc.default_encoding", "MARC8");
//		System.setProperty("marc.permissive", "true");
	}
	
	public static void copyFile(File sourceFile, File destFile) 
			throws IOException {
		if (!destFile.exists())
		  destFile.createNewFile();
		 
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally {
			if(source != null)
				source.close();
			if (destination != null)
				destination.close();
		}
	}

	
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
	 * Get the Lucene document with the given id from the solr index at the
	 *  solrDataDir
	 * @param doc_id - the unique id of the lucene document in the index
	 * @param sis - SolrIndexSearcher for the relevant index
	 * @return the Lucene document matching the given id
	 */
	public static final Document getDocument(String doc_id, SolrIndexSearcher sis)
		throws ParserConfigurationException, SAXException, IOException {
		List<Document> docList = getAllMatchingDocs(docIDfname, doc_id, sis);
		if (docList.size() == 1)
			return docList.get(0);
		else
			return null;		
	}

	/**
	 * asserts that the document is present in the index
	 */
	public static final void assertDocPresent(String doc_id, SolrIndexSearcher sis)
		throws ParserConfigurationException, SAXException, IOException {
		Document doc = getDocument(doc_id, sis);
		assertNotNull("Found no document with id \"" + doc_id + "\"", doc);
	}

	/**
	 * asserts that the document is NOT present in the index
	 */
	public static final void assertDocNotPresent(String doc_id, SolrIndexSearcher sis)
			throws ParserConfigurationException, SAXException, IOException {
		Document doc = getDocument(doc_id, sis);
		assertNull("Unexpectedly found document with id \"" + doc_id + "\"", doc);
	}

	/**
	 * asserts that the given field is NOT present in the index
	 * @param fldName - name of the field that shouldn't be in index
	 * @param ir - an IndexReader for the relevant index
	 */
	@SuppressWarnings("unchecked")
	public static final void assertFieldNotPresent(String fldName, IndexReader ir)
			throws ParserConfigurationException, IOException, SAXException {
	    Collection<String> fieldNames = ir.getFieldNames(IndexReader.FieldOption.ALL);
	    if (fieldNames.contains(fldName))
			fail("Field " + fldName + " found in index.");
	}

	/**
	 * asserts that the given field is present in the index
	 * @param fldName - name of the field that shouldn't be in index
	 */
	@SuppressWarnings("unchecked")
	public static final void assertFieldPresent(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
	    assertFieldPresent(fldName, getIndexReader(solrCore));
	}

	/**
	 * asserts that the given field is present in the index
	 * @param fldName - name of the field that shouldn't be in index
	 * @param ir - IndexReader
	 */
	@SuppressWarnings("unchecked")
	public static final void assertFieldPresent(String fldName, IndexReader ir)
			throws ParserConfigurationException, IOException, SAXException {
	    Collection<String> fieldNames = ir.getFieldNames(IndexReader.FieldOption.ALL);
	    if (!fieldNames.contains(fldName))
			fail("Field " + fldName + " not found in index");
	}

	public static final void assertFieldStored(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " is not stored", getSchemaField(fldName, solrCore).stored());
	}

	public static final void assertFieldNotStored(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " is stored", !getSchemaField(fldName, solrCore).stored());
	}

	public static final void assertFieldIndexed(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " is not indexed",	getSchemaField(fldName, solrCore).indexed());
	}

	public static final void assertFieldNotIndexed(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException,	SAXException {
		assertTrue(fldName + " is indexed", !getSchemaField(fldName, solrCore).indexed());
	}

	public static final void assertFieldTokenized(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException,	SAXException {
		assertTrue(fldName + " is not tokenized", getFieldType(fldName, solrCore).isTokenized());
	}

	public static final void assertFieldNotTokenized(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " is tokenized", !getFieldType(fldName, solrCore).isTokenized());
	}

	public static final void assertFieldHasTermVectors(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " doesn't have termVectors", getSchemaField(fldName, solrCore).storeTermVector());
	}

	public static final void assertFieldHasNoTermVectors(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " has termVectors", !getSchemaField(fldName, solrCore).storeTermVector());
	}

	public static final void assertFieldOmitsNorms(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " has norms", getSchemaField(fldName, solrCore).omitNorms());
	}

	public static final void assertFieldHasNorms(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " omits norms", !getSchemaField(fldName, solrCore).omitNorms());
	}

	public static final void assertFieldMultiValued(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " is not multiValued", getSchemaField(fldName, solrCore).multiValued());
	}

	public static final void assertFieldNotMultiValued(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertTrue(fldName + " is multiValued", !getSchemaField(fldName, solrCore).multiValued());
	}

	public static final SchemaField getSchemaField(String fldName, SolrCore solrCore)
			throws ParserConfigurationException, IOException, SAXException {
		return solrCore.getSchema().getField(fldName);
	}

	private static final FieldType getFieldType(String fldName, SolrCore solrCore) {
		return solrCore.getSchema().getFieldType(fldName);
	}

	public static final void assertDocHasFieldValue(String doc_id, String fldName, String fldVal, SolrIndexSearcher sis)
			throws ParserConfigurationException, IOException, SAXException {
		// TODO: repeatable field vs. not ...
		//  TODO: check for single occurrence of field value, even for repeatable field
		String[] values = getSingleDoc(docIDfname, doc_id, sis).getValues(fldName);
		if (values != null)
			for (String val : values)
				if (val.equals(fldVal))
					return;
		fail("Field " + fldName + " did not contain value \"" + fldVal + "\" in doc " + doc_id);
	}

	public static final void assertDocHasNoFieldValue(String doc_id, String fldName, String fldVal, SolrIndexSearcher sis)
			throws ParserConfigurationException, IOException, SAXException {
		// TODO: repeatable field vs. not ...
		// TODO: check for single occurrence of field value, even for repeatable field
		String[] values = getSingleDoc(docIDfname, doc_id, sis).getValues(fldName);
		if (values != null)
			for (String val : values)
				if (val != null && val.equals(fldVal))
					fail("Field " + fldName + " contained value \"" + fldVal + "\" in doc " + doc_id);
	}

	public static final Document getSingleDoc(String fldName, String fldVal, SolrIndexSearcher sis)
			throws ParserConfigurationException, SAXException, IOException {
		List<Document> docList = getAllMatchingDocs(fldName, fldVal, sis);
		if (docList.size() != 1)
			fail("The index does not have a single document containing field " 
					+ fldName + " with value of \""+ fldVal +"\"");
		return docList.get(0);
	}

	@SuppressWarnings("unchecked")
	public static final void assertDocHasNoField(String doc_id, String fldName, SolrIndexSearcher sis) 
			throws ParserConfigurationException, IOException, SAXException {
		Document doc = getSingleDoc(docIDfname, doc_id, sis);
	    if (doc.getField(fldName) != null)
			fail("Field " + fldName + " found in doc \"" + doc_id + "\"");
	}

	public static final void assertSearchResults(String fldName, String fldVal, 
									Set<String> docIds, SolrIndexSearcher sis) 
			throws ParserConfigurationException, SAXException, IOException
	{
		List<Document> docList = assertResultSize(fldName, fldVal, docIds.size(), sis);
		String msg = fldName + " search \"" + fldVal + "\": ";
		
		for (String docId : docIds)
			assertDocInList(docList, docId, msg, sis);
	}

	public static final void assertFieldValues(String fldName, String fldVal, 
									Set<String> docIds, SolrIndexSearcher sis) 
			throws ParserConfigurationException, SAXException, IOException
	{
		for (String docId : docIds)
			assertDocHasFieldValue(docId, fldName, fldVal, sis); 
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
	public static final List<Document> assertResultSize(String fld, String text, int numExp, SolrIndexSearcher sis) 
			throws ParserConfigurationException, SAXException, IOException {
		List<Document> docList = getAllMatchingDocs(fld, text, sis);
		assertTrue("Expected " + numExp + " documents for " + fld + " search \"" 
				+ text + "\" but got " + docList.size(), docList.size() == numExp);
		return docList;
	}

	/**
	 * Given an index field name and value, return a list of Lucene Documents
	 *  that match the term query sent to the index
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param text - the string to be searched in the given field
	 * @param sis - SolrIndexSearcher for relevant index
	 * @return a list of Lucene Documents
	 */
	public static final List<Document> getAllMatchingDocs(String fld, String text, SolrIndexSearcher sis) 
			throws ParserConfigurationException, SAXException, IOException {
		List<Document> docList = new ArrayList<Document>();
		Query query = QueryParsing.parseQuery(text, fld, sis.getSchema());
	    DocSet ds = sis.getDocSet(query);
	    DocIterator iter = ds.iterator();
	    while (iter.hasNext())
	    {
	    	docList.add(sis.doc(iter.nextDoc()));
	    }
	    return docList;
	}

	/**
	 * Given an index field name and value, return a list of Lucene Documents
	 *  that match the term query sent to the index, sorted as indicated
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param text - the string to be searched in the given field
	 * @param sortfld - name of the field results should be sorted by
	 * @param sis - SolrIndexSearcher for relevant index
	 * @return a list of Lucene Documents sorted (ascending) per indicated field
	 */
	public static final List<Document> getSortedDocs(String fld, String text, String sortfld, SolrIndexSearcher sis) 
			throws ParserConfigurationException, SAXException, IOException {
		List<Document> docList = new ArrayList<Document>();

		Query query = QueryParsing.parseQuery(text, fld, sis.getSchema());
		Hits hits = sis.search(query, new Sort(sortfld));
		for (int i = 0; i < hits.length(); i++) {
			docList.add(hits.doc(i));
		}
	    return docList;
	}
	
	public static final SolrIndexSearcher getSolrIndexSearcher(String solrPath, String solrDataDir)
			throws ParserConfigurationException, IOException, SAXException {
		return getSolrIndexSearcher(getSolrCore(solrPath, solrDataDir));
	}

	public static final SolrIndexSearcher getSolrIndexSearcher(SolrCore solrCore)
			throws ParserConfigurationException, IOException, SAXException {
		return solrCore.getSearcher().get();
	}

	public static final SolrCore getSolrCore(String solrPath, String solrDataDir)
			throws ParserConfigurationException, IOException, SAXException {
		SolrCoreProxy solrCoreProxy = SolrCoreLoader.loadCore(solrPath, solrDataDir, null, logger);
		return (SolrCore) solrCoreProxy.getCore();
	}

	public static final IndexReader getIndexReader(String solrPath, String solrDataDir)
			throws ParserConfigurationException, IOException, SAXException {
	    return getSolrIndexSearcher(solrPath, solrDataDir).getReader();
	}

	public static final IndexReader getIndexReader(SolrCore solrCore)
			throws ParserConfigurationException, IOException, SAXException {
	    return getSolrIndexSearcher(solrCore).getReader();
	}

	/**
	 * assert field is not tokenized, has no termVector and, if indexed, omitsNorm 
	 */
	public static final void assertStringFieldProperties(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertFieldPresent(fldName, solrCore);
		assertFieldNotTokenized(fldName, solrCore);
		assertFieldHasNoTermVectors(fldName, solrCore);
		// since omitNorms is only relevant if field is indexed, 
		//  assertFieldOmitsNorms fails if the field is NOT indexed as
		//  default boolean value is false.
		if (getSchemaField(fldName, solrCore).indexed())
			assertFieldOmitsNorms(fldName, solrCore);
	}

	/**
	 * assert field is present, tokenized, has no termVectors
	 */
	public static final void assertTextFieldProperties(String fldName, SolrCore solrCore) 
			throws ParserConfigurationException, IOException, SAXException {
		assertFieldPresent(fldName, solrCore);
		assertFieldTokenized(fldName, solrCore);
		assertFieldHasNoTermVectors(fldName, solrCore);
	}

	public static final void assertDocInList(List<Document> docList, String doc_id,	String msgPrefix, SolrIndexSearcher sis) 
			throws ParserConfigurationException, SAXException, IOException {
		assertTrue(msgPrefix + "doc \"" + doc_id + "\" missing from list", docList.contains(getDocument(doc_id, sis)));
	}

}