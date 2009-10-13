package org.solrmarc.marc;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;
import org.marc4j.*;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.tools.GetDefaultConfig;
import org.solrmarc.tools.Utils;

public abstract class MarcHandler {

	abstract protected int handleAll();

	protected SolrIndexer indexer;
	protected MarcReader reader;
	protected boolean verbose = false;
	protected ErrorHandler errors = null;
	protected boolean includeErrors = false;
    /** The full class name of SolrIndexer or the subclass to be used */
	protected String indexerName;
	protected String addnlArgs[] = null;
	protected Properties configProps;
	protected boolean inputTypeXML = false;
	protected boolean permissiveReader;
	protected String defaultEncoding;
    protected boolean to_utf_8;
    protected String combineConsecutiveRecordsFields = null;
	
	private String solrmarcPath;
	private String siteSpecificPath;
    protected String homeDir = ".";
    
	/** The name of the _index.properties file */
	private String indexerProps;
    private final String TRANS_MAP_DIR = "translation_maps";
    private final String SCRIPTS_DIR = "scripts";
	
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcHandler.class.getName());
	    
	public MarcHandler(String args[])
	{
        String configProperties = GetDefaultConfig.getConfigName("config.properties");
        List<String> addnlArgList = new ArrayList<String>();
        if(args.length > 0)
        {
            for (String arg : args)
            {
                if (arg.endsWith(".properties"))
                {
                    configProperties = arg;
                }
                else if (arg.endsWith(".mrc") || arg.endsWith("marc"))
                {
                    System.setProperty("marc.path", arg);
                    System.setProperty("marc.source", "FILE");
                }
                else if (arg.equals("NONE"))
                {
                    System.setProperty("marc.source", "NONE");
                }
                else if (arg.endsWith(".xml"))
                {
                    System.setProperty("marc.path", arg);
                    System.setProperty("marc.source", "FILE");
                }
                else if (arg.endsWith(".del"))
                {
                    System.setProperty("marc.ids_to_delete", arg);
                }
                else
                {
                	addnlArgList.add(arg);
                }
            }
        }
        addnlArgs = addnlArgList.toArray(new String[0]);
        
        // System.out.println("Loading properties from " + properties);
        logger.debug("Loading config properties from " + configProperties);
        // Process Properties
        loadProperties(configProperties);

        //  Load the custom Indexer (or the standard one)
        //  note the values indexerName and indexerProps are initialized
        //  by the above call to loadProperties
        loadIndexer(indexerName, indexerProps);
        
	}
		
	/**
	 * Load the properties file and initialize class variables
	 * @param configProperties _config.properties file
	 */
	public void loadProperties(String configProperties)
	{
        homeDir = GetDefaultConfig.getJarFileName();
        if (homeDir != null) homeDir = new File(homeDir).getParent();
        
        configProps = Utils.loadProperties(new String[]{homeDir}, configProperties);
	    
        solrmarcPath = Utils.getProperty(configProps, "solrmarc.path");
        solrmarcPath = normalizePathProperty(homeDir, solrmarcPath);

        siteSpecificPath = Utils.getProperty(configProps, "solrmarc.site.path");
        siteSpecificPath = normalizePathProperty(homeDir, siteSpecificPath);
 
        // class name of SolrIndexer or the subclass to be used
        indexerName = Utils.getProperty(configProps, "solr.indexer");

        // _index.properties file
        indexerProps = Utils.getProperty(configProps, "solr.indexer.properties");

        combineConsecutiveRecordsFields = Utils.getProperty(configProps, "marc.combine_records");
        if (combineConsecutiveRecordsFields != null && combineConsecutiveRecordsFields.length() == 0) 
            combineConsecutiveRecordsFields = null;
        
        permissiveReader = Boolean.parseBoolean(Utils.getProperty(configProps, "marc.permissive"));
        if (Utils.getProperty(configProps, "marc.default_encoding") != null)
        {
            defaultEncoding = Utils.getProperty(configProps, "marc.default_encoding").trim();    
        }
        else
        {
            defaultEncoding = "BESTGUESS";
        }
        verbose = Boolean.parseBoolean(Utils.getProperty(configProps, "marc.verbose"));
        includeErrors = Boolean.parseBoolean(Utils.getProperty(configProps, "marc.include_errors"));
        to_utf_8 = Boolean.parseBoolean(Utils.getProperty(configProps, "marc.to_utf_8"));
        boolean unicodeNormalize = Boolean.parseBoolean(Utils.getProperty(configProps, "marc.unicode_normalize"));
        String source = Utils.getProperty(configProps, "marc.source", "STDIN").trim();
        if (Utils.getProperty(configProps, "marc.override")!= null)
        {
            System.setProperty("org.marc4j.marc.MarcFactory", Utils.getProperty(configProps, "marc.override").trim());
        }
        reader = null;
        String fName = Utils.getProperty(configProps, "marc.path");
        if (fName != null)  fName = fName.trim();
        
        loadReader(source, fName);
	}
	
	/**
	 * normalizePathProperty - if the passed in path is a relative path, make it be relative 
	 *     to the "home" directory (which is where the OneJar jar file containing all of the program
	 *     is found) rather than being relative to the "current" directory (which is whatever directory 
	 *     the user happens to be in when the the JVM was started.
	 *     Note if the passed-in path is null, or is already an absolute path, simply return that value.
	 *     
	 * @param homeDir - directory where the OneJar jar file containing all of the program is found
	 * @param path - a possibly relative file path to be normalized. 
	 * @return  normalized form of  "homeDir/path"
	 */
	private String normalizePathProperty(String homeDir, String path)
    {
        if (path != null)
        {
            File smPath = new File(path);
            if (smPath != null && !smPath.isAbsolute()) 
            {
                smPath = new File(homeDir, path);
                try
                {
                    path = smPath.getCanonicalPath();
                }
                catch (IOException e)
                {
                    path = smPath.getAbsolutePath();
                }
            }
        }
        return(path);
    }

    public void loadReader(String source, String fName)
	{       
        if (source.equals("FILE") || source.equals("STDIN"))
        {
        	InputStream is = null;
        	if (source.equals("FILE")) 
        	{
        		if (fName != null && fName.toLowerCase().endsWith(".xml")) 
        		    inputTypeXML = true;
        		try {
					is = new FileInputStream(fName);
				} 
        		catch (FileNotFoundException e) 
        		{
		        	logger.error("Fatal error: Unable to open specified MARC data file: " + fName);
		        	throw new IllegalArgumentException("Fatal error: Unable to open specified MARC data file: " + fName);
				}
        	}
        	else
        	{
        		is = new BufferedInputStream(System.in);
        		is.mark(10);
        		int b = -1;
        		try { 
        		    b = is.read();
                    is.reset();
        		}
        		catch (IOException e)
        		{
                    logger.error("Fatal error: Exception reading from stdin");
                    throw new IllegalArgumentException("Fatal error: Exception reading from stdin");
        		}
        		if (b == '<') inputTypeXML = true;        		
        	}
            if (inputTypeXML)
            {
                reader = new MarcXmlReader(is);
            }
            else if (permissiveReader)
            {
                errors = new ErrorHandler();
                reader = new MarcPermissiveStreamReader(is, errors, to_utf_8, defaultEncoding);
            }
            else
            {
                reader = new MarcPermissiveStreamReader(is, false, to_utf_8, defaultEncoding);
            }
        }
        else if (source.equals("DIR"))
        {
            reader = new MarcDirStreamReader(Utils.getProperty(configProps, "marc.path").trim(), permissiveReader, to_utf_8);
        }
        else if (source.equals("Z3950"))
        {
        	logger.warn("Error: Z3950 not yet implemented");
            reader = null;
        }
        if (reader != null && combineConsecutiveRecordsFields != null)
        {
            if (errors == null)
            {
                reader = new MarcCombiningReader(reader, combineConsecutiveRecordsFields);
            }
            else
            {
                ErrorHandler errors2 = errors;
                errors = new ErrorHandler();
                reader = new MarcCombiningReader(reader, errors, errors2, combineConsecutiveRecordsFields);
            }
        }
        String marcIncludeIfPresent = Utils.getProperty(configProps, "marc.include_if_present");
        String marcIncludeIfMissing = Utils.getProperty(configProps, "marc.include_if_missing");
        String marcDeleteSubfields = Utils.getProperty(configProps, "marc.delete_subfields");
        if (marcDeleteSubfields != null)  marcDeleteSubfields = marcDeleteSubfields.trim();
        if (reader != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null || marcDeleteSubfields != null))
        {
            reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing, marcDeleteSubfields);
        }
	//        // Do translating last so that if we are Filtering as well as translating, we don't expend the 
	//        // effort to translate records, which may then be filtered out and discarded.
	//        if (reader != null && to_utf_8)
	//        {
	//            reader = new MarcTranslatedReader(reader, unicodeNormalize);
	//        }	    
        return;
	}

	/**
	 * Load the Custom Indexer routine
	 * @param properties
	 * @throws IOException
	 */
	public void loadIndexer(String indexerName, String indexerProps) 
	{
	    // Setup the SolrMarc Indexer
	    try
	    {
	        Class<?> indexerClass;
	
	        try {
	            indexerClass = Class.forName(indexerName);
	        }
	        catch (ClassNotFoundException e)
	        {
	            logger.error("Cannot load class: " + indexerName);
	            Class<?> baseIndexerClass = SolrIndexer.class;
	            String baseName = baseIndexerClass.getPackage().getName();
	            String fullName = baseName + "." + indexerName;
	            indexerClass = Class.forName(fullName);
	            logger.error(e.getCause());
	        }
	
	        Constructor<?> constructor = indexerClass.getConstructor(new Class[]{String.class, String[].class});
	        String propertySearchPath[] = new String[0];
	        if (siteSpecificPath != null && solrmarcPath != null)
	        {
	            propertySearchPath =  new String[] { siteSpecificPath,
	                                                 siteSpecificPath + File.separator + TRANS_MAP_DIR,
	                                                 siteSpecificPath + File.separator + SCRIPTS_DIR,
	                                                 solrmarcPath, 
	                                                 solrmarcPath + File.separator + TRANS_MAP_DIR,
	                                                 solrmarcPath + File.separator + SCRIPTS_DIR };	
	        }
	        else if (siteSpecificPath == null && solrmarcPath != null)
	        {
                propertySearchPath =  new String[] { solrmarcPath, 
                                                     solrmarcPath + File.separator + TRANS_MAP_DIR,
                                                     solrmarcPath + File.separator + SCRIPTS_DIR }; 
	        }
            else if (siteSpecificPath != null && solrmarcPath == null)
            {
                 propertySearchPath =  new String[] { siteSpecificPath,
                                                      siteSpecificPath + File.separator + TRANS_MAP_DIR,
                                                      siteSpecificPath + File.separator + SCRIPTS_DIR };
            }
            else if (siteSpecificPath == null && solrmarcPath == null)
            {
                 propertySearchPath =  new String[] { homeDir,
                                                      homeDir + File.separator + TRANS_MAP_DIR,
                                                      homeDir + File.separator + SCRIPTS_DIR };
            }
	        Object instance = constructor.newInstance(indexerProps, propertySearchPath);
	
	        if (instance instanceof SolrIndexer)
	        {
	            indexer = (SolrIndexer)instance;
	        }
	        else
	        {
	            logger.error("Error: Custom Indexer " + indexerName + " must be subclass of SolrIndexer. ");
	            throw new IllegalArgumentException("Error: Custom Indexer " + indexerName + " must be subclass of SolrIndexer. ");
	        }
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	
	        if (e instanceof IllegalArgumentException)
	        {
	            logger.error("Error configuring Indexer from properties file.  Exiting...");
	            throw ((IllegalArgumentException) e);
	        }            
	
	        logger.error("Unable to load Custom indexer: " + indexerName);
	        throw new IllegalArgumentException("Error configuring Indexer from properties file.  Exiting...");
	    }
	}
}
