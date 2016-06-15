package org.solrmarc.marc;


import java.io.*;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.marc4j.*;
import org.solrmarc.marc.MarcUnprettyXmlReader;
import org.solrmarc.tools.PropertyUtils;

public class MarcReaderFactory {

	protected boolean verbose = false;
//	protected ErrorHandler errors = null;
 
	/** The full class name of SolrIndexer or the subclass to be used */
	//protected Properties configProps;
    protected boolean inputTypeXML = false;
    protected boolean inputTypeBinary = false;
    protected boolean inputTypeJSON = false;
	protected boolean permissiveReader;
	protected String defaultEncoding;
    protected boolean to_utf_8;
    protected String combineConsecutiveRecordsFields = null;
	protected String unicodeNormalize = null;

    private String solrmarcPath;
    private String siteSpecificPath;
    protected String homeDir = ".";

    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcReaderFactory.class.getName());
	    
	private MarcReaderFactory()
	{
	}
	
	static MarcReaderFactory theFactory = new MarcReaderFactory();
	
	public static MarcReaderFactory instance()
	{
	    return(theFactory);
	}
	
//	public MarcReader makeReader(InputStream input,  Map<String, String> config)
//	{
//	    Properties properties Map<String, String> map = new HashMap<String, String>();
//	    for (String key : properties.stringPropertyNames())
//	    {
//	        map.put(key, properties.getProperty(key));
//	    }
//	    return(makeReader(input, map));
//	}
	
	public MarcReader makeReader(InputStream input, Properties config)
	{
        MarcReader reader; 
        setMarc4JProperties(config);
        solrmarcPath = PropertyUtils.getProperty(config, "solrmarc.path");
        solrmarcPath = normalizePathsProperty(homeDir, solrmarcPath, config);

        siteSpecificPath = PropertyUtils.getProperty(config, "solrmarc.site.path");
        siteSpecificPath = normalizePathsProperty(homeDir, siteSpecificPath, config);
        
        combineConsecutiveRecordsFields = PropertyUtils.getProperty(config, "marc.combine_records");
        if (combineConsecutiveRecordsFields != null && combineConsecutiveRecordsFields.length() == 0) 
            combineConsecutiveRecordsFields = null;
        
        permissiveReader = Boolean.parseBoolean(PropertyUtils.getProperty(config, "marc.permissive"));
        if (PropertyUtils.getProperty(config, "marc.default_encoding") != null)
        {
            defaultEncoding = PropertyUtils.getProperty(config, "marc.default_encoding").trim();    
        }
        else
        {
            defaultEncoding = "BESTGUESS";
        }
//        verbose = Boolean.parseBoolean(PropertyUtils.getProperty(configProps, "marc.verbose"));
//        includeErrors = Boolean.parseBoolean(PropertyUtils.getProperty(configProps, "marc.include_errors"));
        to_utf_8 = Boolean.parseBoolean(PropertyUtils.getProperty(config, "marc.to_utf_8"));
        unicodeNormalize = PropertyUtils.getProperty(config, "marc.unicode_normalize");
        if (unicodeNormalize != null) 
        {
            unicodeNormalize = handleUnicodeNormalizeParm(unicodeNormalize);
        }
        
        logger.info("Attempting to read data from stdin ");
//        else
//            logger.debug("Attempting to read data from stdin ");
        
        BufferedInputStream is = new BufferedInputStream(input);
        is.mark(20);
        byte[] buffer = new byte[5];
        int numRead;
        try { 
            numRead = is.read(buffer);
            is.reset();
        }
        catch (IOException e)
        {
            logger.error("Fatal error: Exception reading from InputStream");
            throw new IllegalArgumentException("Fatal error: Exception reading from InputStream");
        }
        String filestart = new String(buffer);
        if (filestart.equalsIgnoreCase("<?xml"))         inputTypeXML = true;
        else if (filestart.startsWith("{"))             inputTypeJSON = true;              
        else if (filestart.matches("\\d\\d\\d\\d\\d"))  inputTypeBinary = true;              
        
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
      //      errors = new ErrorHandler();
            reader = new MarcPermissiveStreamReader(is, true, to_utf_8, defaultEncoding);
        }
        else
        {
            reader = new MarcPermissiveStreamReader(is, false, to_utf_8, defaultEncoding);
        }
        
        // Add Combine Record reader if requested
        
        if (reader != null && combineConsecutiveRecordsFields != null)
        {
            String combineLeftField = PropertyUtils.getProperty(config, "marc.combine_records.left_field");
            String combineRightField = PropertyUtils.getProperty(config, "marc.combine_records.right_field");
//            if (errors == null)
//            {
                reader = new MarcCombiningReader(reader, combineConsecutiveRecordsFields, combineLeftField, combineRightField);
//            }
//            else
//            {
//                ErrorHandler errors2 = errors;
//                errors = new ErrorHandler();
//                reader = new MarcCombiningReader(reader, errors, errors2, combineConsecutiveRecordsFields, combineLeftField, combineRightField);
//            }
        }
        
        // Add FilteredReader if requested

        String marcIncludeIfPresent = PropertyUtils.getProperty(config, "marc.include_if_present");
        String marcIncludeIfMissing = PropertyUtils.getProperty(config, "marc.include_if_missing");
        String marcDeleteSubfields = PropertyUtils.getProperty(config, "marc.delete_subfields");
        if (marcDeleteSubfields != null && marcDeleteSubfields.equals("nomap")) marcDeleteSubfields = null;
        String marcRemapRecord = PropertyUtils.getProperty(config, "marc.reader.remap");
        if (marcRemapRecord != null && marcRemapRecord.equals("nomap")) marcRemapRecord = null;
        if (marcDeleteSubfields != null)  marcDeleteSubfields = marcDeleteSubfields.trim();
        if (reader != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null || marcDeleteSubfields != null || marcRemapRecord != null))
        {
            if (marcRemapRecord != null)
            {
                String remapFilename =  marcRemapRecord.trim();
                String configFilePath = PropertyUtils.getProperty(config, "config.file.dir");
                String propertySearchPath[] = PropertyUtils.makePropertySearchPath(solrmarcPath, siteSpecificPath, configFilePath, homeDir);
                String remapURL = PropertyUtils.getPropertyFileAbsoluteURL(propertySearchPath, remapFilename, false, null);
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

        return reader;

	}
	
	private void setMarc4JProperties(Properties configProps)
    {
        for (String prop : configProps.stringPropertyNames())
        {
            if (prop.startsWith("org.marc4j."))
            {
                String value = configProps.getProperty(prop);
                System.setProperty(prop, value);
            }
            else if (PropertyUtils.getProperty(configProps, "marc.override")!= null)
            {
                System.setProperty("org.marc4j.marc.MarcFactory", PropertyUtils.getProperty(configProps, "marc.override").trim());
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

//    private String getHomeDir()
//    {
//	    String result = GetDefaultConfig.getJarFileName();       
//        if (result == null)
//        {
//            result = new File(".").getAbsolutePath();
//            logger.debug("Setting homeDir to \".\"");
//            
//        }
//        if (result != null) result = new File(result).getParent();
//        logger.debug("Setting homeDir to: "+ result);
//        return(result);
//    }
	
    /**
     * normalizePathsProperty - normalize one or more : separated paths using
     * the normalizePathProperty method
     * 
     * @param homeDir
     *            - directory where the OneJar jar file containing all of the
     *            program is found
     * @param path
     *            - one or more : separated paths to be normalized.
     * @param config 
     * @return normalized form of "homeDir/path"
     */
    private String normalizePathsProperty(String homeDir, String path, Properties config)
    {
        if (path == null) return (null);
        String paths[] = path.split("[|]");
        StringBuffer result = new StringBuffer();
        for (String part : paths)
        {
            String resolved = normalizePathProperty(homeDir, part, config);
            if (result.length() > 0) result.append("|");
            result.append(resolved);
        }
        return (result.toString());
    }

    /**
     * normalizePathProperty - if the passed in path is a relative path, make it
     * be relative to the "home" directory (which is where the OneJar jar file
     * containing all of the program is found) rather than being relative to the
     * "current" directory (which is whatever directory the user happens to be
     * in when the the JVM was started. Also resolve the property strings
     * ${config.file.dir} as the directory containing the config file and
     * ${solrmarc.jar.dir} as the directory containing the OneJar jar file. Note
     * if the passed-in path is null, or is already an absolute path, simply
     * return that value.
     * 
     * @param homeDir
     *            - directory where the OneJar jar file containing all of the
     *            program is found
     * @param path
     *            - a possibly relative file path to be normalized.
     * @param config 
     * @return normalized form of "homeDir/path"
     */
    private String normalizePathProperty(String homeDir, String path, Properties config)
    {
        if (path != null)
        {
            if (path.contains("${config.file.dir}") && config.getProperty("config.file.dir") != null)
            {
                path = path.replace("${config.file.dir}", config.getProperty("config.file.dir"));
            }
            if (path.contains("${solrmarc.jar.dir}") && homeDir != null)
            {
                path = path.replace("${solrmarc.jar.dir}", homeDir);
            }
            while (path.matches(".*$\\{[a-z.]+\\}.*"))
            {
                String pattern = path.replaceFirst("$\\{([a-z.]+)\\}", "$1");
                String replace = PropertyUtils.getProperty(config, pattern);
                if (pattern != null && replace != null)
                {
                    path.replace("${" + pattern + "}", replace);
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
        return (path);
    }

//    public void loadReader(String source, String fName)
//	{       
//        if (source.equals("FILE") || source.equals("STDIN"))
//        {
//        	InputStream is = null;
//        	if (source.equals("FILE")) 
//        	{
//                if (fName != null && fName.toLowerCase().endsWith(".xml")) 
//                    inputTypeXML = true;
//                else if (fName != null && fName.toLowerCase().endsWith(".json")) 
//                    inputTypeJSON = true;
//        		try {
//                    if (showInputFile)
//                        logger.info("Attempting to open data file: "+ new File(fName).getAbsolutePath());
//                    else 
//                        logger.debug("Attempting to open data file: "+ new File(fName).getAbsolutePath());
//					is = new FileInputStream(fName);
//				} 
//        		catch (FileNotFoundException e) 
//        		{
//		        	logger.error("Fatal error: Unable to open specified MARC data file: " + fName);
//		        	throw new IllegalArgumentException("Fatal error: Unable to open specified MARC data file: " + fName);
//				}
//        	}
//        	else
//        	{
//                if (showInputFile)
//                    logger.info("Attempting to read data from stdin ");
//                else
//                    logger.debug("Attempting to read data from stdin ");
//        	    is = new BufferedInputStream(System.in);
//        		is.mark(10);
//        		int b = -1;
//        		try { 
//        		    b = is.read();
//                    is.reset();
//        		}
//        		catch (IOException e)
//        		{
//                    logger.error("Fatal error: Exception reading from stdin");
//                    throw new IllegalArgumentException("Fatal error: Exception reading from stdin");
//        		}
//        		if (b == '<') inputTypeXML = true;
//        		else if (b == '{') inputTypeJSON = true;              
//        	}
//            if (inputTypeXML)
//            {
//                reader = new MarcUnprettyXmlReader(is);
//            }
//            else if (inputTypeJSON)
//            {
//                reader = new MarcJsonReader(is);
//            }
//            else if (permissiveReader)
//            {
//                errors = new ErrorHandler();
//                reader = new MarcPermissiveStreamReader(is, errors, to_utf_8, defaultEncoding);
//            }
//            else
//            {
//                reader = new MarcPermissiveStreamReader(is, false, to_utf_8, defaultEncoding);
//            }
//        }
//        else if (source.equals("DIR"))
//        {
//            reader = new MarcDirStreamReader(PropertyUtils.getProperty(configProps, "marc.path").trim(), permissiveReader, to_utf_8);
//        }
//        else if (source.equals("Z3950"))
//        {
//        	logger.warn("Error: Z3950 not yet implemented");
//            reader = null;
//        }
//        if (reader != null && combineConsecutiveRecordsFields != null)
//        {
//            String combineLeftField = PropertyUtils.getProperty(configProps, "marc.combine_records.left_field");
//            String combineRightField = PropertyUtils.getProperty(configProps, "marc.combine_records.right_field");
//            if (errors == null)
//            {
//                reader = new MarcCombiningReader(reader, combineConsecutiveRecordsFields, combineLeftField, combineRightField);
//            }
//            else
//            {
//                ErrorHandler errors2 = errors;
//                errors = new ErrorHandler();
//                reader = new MarcCombiningReader(reader, errors, errors2, combineConsecutiveRecordsFields, combineLeftField, combineRightField);
//            }
//        }
//        String marcIncludeIfPresent = PropertyUtils.getProperty(configProps, "marc.include_if_present");
//        String marcIncludeIfMissing = PropertyUtils.getProperty(configProps, "marc.include_if_missing");
//        String marcDeleteSubfields = PropertyUtils.getProperty(configProps, "marc.delete_subfields");
//        if (marcDeleteSubfields != null && marcDeleteSubfields.equals("nomap")) marcDeleteSubfields = null;
//        String marcRemapRecord = PropertyUtils.getProperty(configProps, "marc.reader.remap");
//        if (marcRemapRecord != null && marcRemapRecord.equals("nomap")) marcRemapRecord = null;
//        if (marcDeleteSubfields != null)  marcDeleteSubfields = marcDeleteSubfields.trim();
//        if (reader != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null || marcDeleteSubfields != null || marcRemapRecord != null))
//        {
//            if (marcRemapRecord != null)
//            {
//                String remapFilename =  marcRemapRecord.trim();
//                String configFilePath = PropertyUtils.getProperty(configProps, "config.file.dir");
//                String propertySearchPath[] = makePropertySearchPath(solrmarcPath, siteSpecificPath, configFilePath, homeDir);
//                String remapURL = PropertyUtils.getPropertyFileAbsoluteURL(propertySearchPath, remapFilename, false, null);
//                reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing, marcDeleteSubfields, remapURL);
//            }
//            else
//            {
//                reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing, marcDeleteSubfields);
//            }
//        }
//        // Do translating last so that if we are Filtering as well as translating, we don't expend the 
//        // effort to translate records, which may then be filtered out and discarded.
//        if (reader != null && to_utf_8 && unicodeNormalize != null)
//        {
//            reader = new MarcTranslatedReader(reader, unicodeNormalize);
//        }	    
//        return;
//	}
//
//                    
//	/**
//	 * Load the Custom Indexer routine
//	 * @param indexerName  class name of the indexer
//	 * @param indexerProps name of properties file for the indexer
//	 * @throws IOException
//	 */
//	public void loadIndexer(String indexerName, String indexerProps) 
//	{
//	    // Setup the SolrMarc Indexer
//        Class<?> indexerClass = null;
//
//        try {
//            indexerClass = Class.forName(indexerName);
//        }
//        catch (ClassNotFoundException e)
//        {
//            logger.error("Cannot load class: " + indexerName);
//            Class<?> baseIndexerClass = SolrIndexer.class;
//            String baseName = baseIndexerClass.getPackage().getName();
//            String fullName = baseName + "." + indexerName;
//            try {
//                indexerClass = Class.forName(fullName);
//            }
//            catch (ClassNotFoundException e1)
//            {
//                logger.error("Cannot find custom indexer class named: "+ indexerName);
//                logger.error("Jar file containing that class MUST be referenced via the property:  solrmarc.custom.jar.path");
//                logger.error("Please define this property in your config.properties file");	
//                throw new IllegalArgumentException("Error configuring Indexer from properties file.  Exiting...");
//            }
//        }
//	    try
//	    {
//	        Constructor<?> constructor = indexerClass.getConstructor(new Class[]{String.class, String[].class});
//	        String configFilePath = PropertyUtils.getProperty(configProps, "config.file.dir");
//	        String propertySearchPath[] = makePropertySearchPath(solrmarcPath, siteSpecificPath, configFilePath, homeDir);
//	        Object instance = constructor.newInstance(indexerProps, propertySearchPath);
//	
//	        if (instance instanceof SolrIndexer)
//	        {
//	            indexer = (SolrIndexer)instance;
//	        }
//	        else
//	        {
//	            logger.error("Error: Custom Indexer " + indexerName + " must be subclass of SolrIndexer. ");
//	            throw new IllegalArgumentException("Error: Custom Indexer " + indexerName + " must be subclass of SolrIndexer. ");
//	        }
//	    }
//	    catch (Exception e)
//	    {
//	        e.printStackTrace();
//	
//	        if (e instanceof IllegalArgumentException)
//	        {
//	            logger.error("Error configuring Indexer from properties file.  Exiting...");
//	            throw ((IllegalArgumentException) e);
//	        }            
//	
//	        logger.error("Unable to load Custom indexer: " + indexerName);
//	        throw new IllegalArgumentException("Error configuring Indexer from properties file.  Exiting...");
//	    }
//	}
}
