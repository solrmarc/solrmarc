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
import org.solrmarc.marcoverride.MarcUnprettyXmlReader;
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
    protected boolean inputTypeJSON = false;
	protected boolean permissiveReader;
	protected String defaultEncoding;
    protected boolean to_utf_8;
    protected String combineConsecutiveRecordsFields = null;
	protected String configToUse = null;
	protected boolean showConfig = false;
	protected boolean showInputFile = false;
	protected String unicodeNormalize = null;
	
	private String solrmarcPath;
	private String siteSpecificPath;
    protected String homeDir = ".";

    /** The name of the _index.properties file */
	private String indexerProps;
    private final static String TRANS_MAP_DIR = "translation_maps";
    private final static String SCRIPTS_DIR = "index_scripts";
	
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcHandler.class.getName());
	    
	public MarcHandler()
	{
	}
	
	public void init(String args[])
	{
        String configProperties = GetDefaultConfig.getConfigName("config.properties");

        List<String> addnlArgList = new ArrayList<String>();
        if(args.length > 0)
        {
            for (String arg : args)
            {
                String lc_arg = arg.toLowerCase();
                if (arg.endsWith(".properties"))
                {
                    configProperties = arg;
                }
                else if (lc_arg.endsWith(".mrc") || lc_arg.endsWith(".marc"))
                {
                    System.setProperty("marc.path", arg);
                    System.setProperty("marc.source", "FILE");
                }
                else if (lc_arg.endsWith(".json") )
                {
                    System.setProperty("marc.path", arg);
                    System.setProperty("marc.source", "FILE");
                }
                else if (arg.equals("NONE"))
                {
                    System.setProperty("marc.source", "NONE");
                }
                else if (lc_arg.endsWith(".xml"))
                {
                    System.setProperty("marc.path", arg);
                    System.setProperty("marc.source", "FILE");
                }
                else if (lc_arg.endsWith(".del"))
                {
                    System.setProperty("marc.ids_to_delete", arg);
                }
                else if (arg.equals("DELETE_ONLY"))
                {
                    System.setProperty("marc.source", "NONE");
                    System.setProperty("marc.ids_to_delete", "stdin");
                }
                else if (lc_arg.equals("-nocommit"))
                {
                    System.setProperty("solr.commit_at_end", "false");
                }
                else
                {
                    addnlArgList.add(arg);
                }
            }
        }
        addnlArgs = addnlArgList.toArray(new String[0]);
        configToUse = configProperties;
        
        initLocal();
        processAdditionalArgs();
        if (configToUse != null) 
        {
            // System.out.println("Loading properties from " + properties);
            logger.debug("Loading config properties from " + configToUse);
            // Process Properties
            loadProperties(configToUse);
        }

        //  Load the custom Indexer (or the standard one)
        //  note the values indexerName and indexerProps are initialized
        //  by the above call to loadProperties
        if (indexerName != null)
        {
            loadIndexer(indexerName, indexerProps); 
        }
	}
		
    /** 
     * initLocal - local init for subclasses of MarcHandler
     */
	protected void initLocal()
    {
        // do nothing here, override in subclasses.
    }
    
    /** 
     * loadLocalProperties - local init for subclasses of MarcHandler
     */
    protected void loadLocalProperties()
    {
        // do nothing here, override in subclasses.
    }
    
    /** 
     * processAdditionalArgs - local init for subclasses of MarcHandler
     */
    protected void processAdditionalArgs()
    {
        // do nothing here, override in subclasses.
    }
    
	/**
	 * Load the properties file and initialize class variables
	 * @param configProperties _config.properties file
	 */
	public void loadProperties(String configProperties)
	{
        homeDir = getHomeDir();
        logger.debug("Current Directory = "+ (new File(".").getAbsolutePath()));
        if (configProperties.equals("null.properties"))
        {
            configProps = new Properties();
        }
        else
        {
            configProps = Utils.loadProperties(new String[]{homeDir}, configProperties, showConfig, "config.file.dir");
        }
        loadLocalProperties();
        
        setMarc4JProperties(configProps);
        
        solrmarcPath = Utils.getProperty(configProps, "solrmarc.path");
        solrmarcPath = normalizePathsProperty(homeDir, solrmarcPath);

        siteSpecificPath = Utils.getProperty(configProps, "solrmarc.site.path");
        siteSpecificPath = normalizePathsProperty(homeDir, siteSpecificPath);
 
        // class name of SolrIndexer or the subclass to be used
        indexerName = Utils.getProperty(configProps, "solr.indexer");
        if (indexerName == null) indexerName = SolrIndexer.class.getName();
        
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
        unicodeNormalize = Utils.getProperty(configProps, "marc.unicode_normalize");
        if (unicodeNormalize != null) 
        {
            unicodeNormalize = handleUnicodeNormalizeParm(unicodeNormalize);
        }
        String source = Utils.getProperty(configProps, "marc.source", "STDIN").trim();
        if (Utils.getProperty(configProps, "marc.override")!= null)
        {
            System.setProperty("org.marc4j.marc.MarcFactory", Utils.getProperty(configProps, "marc.override").trim());
        }
        else  // no override, tell solrmarc to use the NoSortMarcFactory by default.
        {
            System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        }

        reader = null;
        String fName = Utils.getProperty(configProps, "marc.path");
        if (fName != null)  fName = fName.trim();

        loadReader(source, fName);
	}
	
	private void setMarc4JProperties(Properties configProps2)
    {
        for (String prop : configProps2.stringPropertyNames())
        {
            if (prop.startsWith("org.marc4j."))
            {
                String value = configProps2.getProperty(prop);
                System.setProperty(prop, value);
            }
        }
        
    }

    // We only get here if the parm (unicodeNormalize2) is not null compare it against 
	// the valid values and return the correct value to use as the parm
	private String handleUnicodeNormalizeParm(String parm)
    {
	    if (parm == null) return(null);
        if (parm.equalsIgnoreCase("KC") || parm.equalsIgnoreCase("CompatibilityCompose"))
        {
            parm = "KC";
        }
        else if (parm.equalsIgnoreCase("C") || parm.equalsIgnoreCase("Compose") || parm.equalsIgnoreCase("true"))
        {
            parm = "C";
        }
        else if (parm.equalsIgnoreCase("D") || parm.equalsIgnoreCase("Decompose"))
        {
            parm = "D";
        }
        else if (parm.equalsIgnoreCase("KD") || parm.equalsIgnoreCase("CompatibiltyDecompose"))
        {
            parm = "KD";
        }
        else 
        {
            parm = null;
        }
        return(parm);
    }

    private String getHomeDir()
    {
	    String result = GetDefaultConfig.getJarFileName();       
        if (result == null)
        {
            result = new File(".").getAbsolutePath();
            logger.debug("Setting homeDir to \".\"");
            
        }
        if (result != null) result = new File(result).getParent();
        logger.debug("Setting homeDir to: "+ result);
        return(result);
    }
	
    /**
     * normalizePathsProperty - normalize one or more : separated paths using the normalizePathProperty
     *     method
     *     
     * @param homeDir - directory where the OneJar jar file containing all of the program is found
     * @param path - one or more : separated paths to be normalized. 
     * @return  normalized form of  "homeDir/path"
     */
    private String normalizePathsProperty(String homeDir, String path)
    {
        if (path == null) return(null);
        String paths[] = path.split("[|]");
        StringBuffer result = new StringBuffer();
        for (String part : paths)
        {
            String resolved = normalizePathProperty(homeDir, part);
            if (result.length() > 0) result.append("|");
            result.append(resolved);
        }
        return(result.toString());
    }
    
    /**
	 * normalizePathProperty - if the passed in path is a relative path, make it be relative 
	 *     to the "home" directory (which is where the OneJar jar file containing all of the program
	 *     is found) rather than being relative to the "current" directory (which is whatever directory 
	 *     the user happens to be in when the the JVM was started. Also resolve the property strings
	 *     ${config.file.dir} as the directory containing the config file  and
	 *     ${solrmarc.jar.dir} as the directory containing the OneJar jar file.
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
            if (path.contains("${config.file.dir}") && configProps.getProperty("config.file.dir") != null)
            {
                path = path.replace("${config.file.dir}", configProps.getProperty("config.file.dir"));
            }
            if (path.contains("${solrmarc.jar.dir}") && homeDir != null)
            {
                path = path.replace("${solrmarc.jar.dir}", homeDir);
            }
            while (path.matches(".*$\\{[a-z.]+\\}.*"))
            {
                String pattern = path.replaceFirst("$\\{([a-z.]+)\\}", "$1"); 
                String replace = Utils.getProperty(configProps, pattern);
                if (pattern != null && replace != null)
                {
                    path.replace("${"+pattern+"}", replace);
                }
                else
                {
                    break;
                }
            }
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
                else if (fName != null && fName.toLowerCase().endsWith(".json")) 
                    inputTypeJSON = true;
        		try {
                    if (showInputFile)
                        logger.info("Attempting to open data file: "+ new File(fName).getAbsolutePath());
                    else 
                        logger.debug("Attempting to open data file: "+ new File(fName).getAbsolutePath());
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
                if (showInputFile)
                    logger.info("Attempting to read data from stdin ");
                else
                    logger.debug("Attempting to read data from stdin ");
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
        		else if (b == '{') inputTypeJSON = true;              
        	}
            if (inputTypeXML)
            {
                reader = new MarcUnprettyXmlReader(is);
            }
            else if (inputTypeJSON)
            {
                reader = new MarcJsonReader(is);
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
            String combineLeftField = Utils.getProperty(configProps, "marc.combine_records.left_field");
            String combineRightField = Utils.getProperty(configProps, "marc.combine_records.right_field");
            if (errors == null)
            {
                reader = new MarcCombiningReader(reader, combineConsecutiveRecordsFields, combineLeftField, combineRightField);
            }
            else
            {
                ErrorHandler errors2 = errors;
                errors = new ErrorHandler();
                reader = new MarcCombiningReader(reader, errors, errors2, combineConsecutiveRecordsFields, combineLeftField, combineRightField);
            }
        }
        String marcIncludeIfPresent = Utils.getProperty(configProps, "marc.include_if_present");
        String marcIncludeIfMissing = Utils.getProperty(configProps, "marc.include_if_missing");
        String marcDeleteSubfields = Utils.getProperty(configProps, "marc.delete_subfields");
        if (marcDeleteSubfields != null && marcDeleteSubfields.equals("nomap")) marcDeleteSubfields = null;
        String marcRemapRecord = Utils.getProperty(configProps, "marc.reader.remap");
        if (marcRemapRecord != null && marcRemapRecord.equals("nomap")) marcRemapRecord = null;
        if (marcDeleteSubfields != null)  marcDeleteSubfields = marcDeleteSubfields.trim();
        if (reader != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null || marcDeleteSubfields != null || marcRemapRecord != null))
        {
            if (marcRemapRecord != null)
            {
                String remapFilename =  marcRemapRecord.trim();
                String configFilePath = Utils.getProperty(configProps, "config.file.dir");
                String propertySearchPath[] = makePropertySearchPath(solrmarcPath, siteSpecificPath, configFilePath, homeDir);
                String remapURL = Utils.getPropertyFileAbsoluteURL(propertySearchPath, remapFilename, false, null);
                reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing, marcDeleteSubfields, remapURL);
            }
            else
            {
                reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing, marcDeleteSubfields);
            }
        }
        // Do translating last so that if we are Filtering as well as translating, we don't expend the 
        // effort to translate records, which may then be filtered out and discarded.
        if (reader != null && to_utf_8 && unicodeNormalize != null)
        {
            reader = new MarcTranslatedReader(reader, unicodeNormalize);
        }	    
        return;
	}

    static protected void addToPropertySearchPath(String pathToAdd, ArrayList<String> propertySearchPath, Set<String> propertySearchSet)
    {
        if (!propertySearchSet.contains(pathToAdd))
        {
            propertySearchPath.add(pathToAdd);
            propertySearchPath.add(pathToAdd + File.separator + TRANS_MAP_DIR);
            propertySearchPath.add(pathToAdd + File.separator + SCRIPTS_DIR);
            propertySearchSet.add(pathToAdd);
        }
    }
    
    protected String[] makePropertySearchPath(String solrmarcPath, String siteSpecificPath, String configFilePath, String homeDir)
    {
        ArrayList<String> propertySearchPath = new ArrayList<String>();
        Set<String> propertySearchSet = new HashSet<String>();
        if (siteSpecificPath != null)
        {
            String sitePaths[] = siteSpecificPath.split("[|]");
            for (String site : sitePaths)
            {
                addToPropertySearchPath(site, propertySearchPath, propertySearchSet);
            }
            
        }
        if (solrmarcPath != null)
        {
            String smPaths[] = solrmarcPath.split("[|]");
            for (String path : smPaths)
            {
                addToPropertySearchPath(path, propertySearchPath, propertySearchSet);
            }
            
        }
        if (configFilePath != null)
        {
            addToPropertySearchPath(configFilePath, propertySearchPath, propertySearchSet);
        }
        if (homeDir != null)
        {
            addToPropertySearchPath(homeDir, propertySearchPath, propertySearchSet);
        }
        return(propertySearchPath.toArray(new String[0]));
    }
                    
	/**
	 * Load the Custom Indexer routine
	 * @param properties
	 * @throws IOException
	 */
	public void loadIndexer(String indexerName, String indexerProps) 
	{
	    // Setup the SolrMarc Indexer
        Class<?> indexerClass = null;

        try {
            indexerClass = Class.forName(indexerName);
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Cannot load class: " + indexerName);
            Class<?> baseIndexerClass = SolrIndexer.class;
            String baseName = baseIndexerClass.getPackage().getName();
            String fullName = baseName + "." + indexerName;
            try {
                indexerClass = Class.forName(fullName);
            }
            catch (ClassNotFoundException e1)
            {
                logger.error("Cannot find custom indexer class named: "+ indexerName);
                logger.error("Jar file containing that class MUST be referenced via the property:  solrmarc.custom.jar.path");
                logger.error("Please define this property in your config.properties file");	
                throw new IllegalArgumentException("Error configuring Indexer from properties file.  Exiting...");
            }
        }
	    try
	    {
	        Constructor<?> constructor = indexerClass.getConstructor(new Class[]{String.class, String[].class});
	        String configFilePath = Utils.getProperty(configProps, "config.file.dir");
	        String propertySearchPath[] = makePropertySearchPath(solrmarcPath, siteSpecificPath, configFilePath, homeDir);
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
