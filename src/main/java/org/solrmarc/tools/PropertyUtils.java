package org.solrmarc.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;

public class PropertyUtils
{
    protected static final Logger logger = LogManager.getLogger(PropertyUtils.class);
//    private final static String TRANS_MAP_DIR = "translation_maps";
//    private final static String SCRIPTS_DIR = "index_scripts";

    /**
     * Default Constructor It's private, so it can't be instantiated by other
     * objects
     * 
     */
    private PropertyUtils()
    {
    }

    /**
     * Check first for a particular property in the System Properties, so that
     * the -Dprop="value" command line arg mechanism can be used to override
     * values defined in the passed in property file. This is especially useful
     * for defining the marc.source property to define which file to operate on,
     * in a shell script loop.
     * 
     * @param props
     *            property set in which to look.
     * @param propname
     *            name of the property to lookup.
     * @return value stored for that property (or null if it doesn't exist)
     */
    public static String getProperty(Properties props, String propname)
    {
        return getProperty(props, propname, null);
    }

    /**
     * Check first for a particular property in the System Properties, so that
     * the -Dprop="value" command line arg mechanism can be used to override
     * values defined in the passed in property file. This is especially useful
     * for defining the marc.source property to define which file to operate on,
     * in a shell script loop.
     * 
     * @param props
     *            property set in which to look.
     * @param propname
     *            name of the property to lookup.
     * @param defVal
     *            the default value to use if property is not defined
     * @return value stored for that property (or the if it doesn't exist)
     */
    public static String getProperty(Properties props, String propname, String defVal)
    {
        String prop;
        if ((prop = System.getProperty(propname)) != null)
        {
            return (prop);
        }
        if (props != null && (prop = props.getProperty(propname)) != null)
        {
            return (prop);
        }
        return defVal;
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param propertyPaths
     *            the directories to search for the properties file
     * @param propertyFileName
     *            name of the sought properties file
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName)
    {
        return (loadProperties(propertyPaths, propertyFileName, false, null, null));
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param propertyPaths
     *            the directories to search for the properties file
     * @param propertyFileName
     *            name of the sought properties file
     * @param showName
     *            whether the name of the file/resource being read should be
     *            shown.
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName, boolean showName)
    {
        return (loadProperties(propertyPaths, propertyFileName, showName, null, null));
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param propertyPaths
     *            the directories to search for the properties file
     * @param propertyFileName
     *            name of the sought properties file
     * @param filenameReturn
     *            array where first element will be set to the full path of the
     *            loaded file
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName, String filenameReturn[])
    {
        return (loadProperties(propertyPaths, propertyFileName, false, null, filenameReturn));
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param fullFilenameURLStr
     *            String representation of url to properties file whether it is
     *            in a local file or a resource
     * @return Properties object
     */
    public static Properties loadProperties(String fullFilenameURLStr)
    {
        InputStream in = getPropertyFileInputStream(fullFilenameURLStr);
        String errmsg = "Fatal error: Unable to find specified properties file: " + fullFilenameURLStr;

        // load the properties
        Properties props = new Properties();
        try
        {
            if (fullFilenameURLStr.endsWith(".xml") || fullFilenameURLStr.endsWith(".XML"))
            {
                props.loadFromXML(in);
            }
            else
            {
                props.load(in);
            }
            in.close();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(errmsg);
        }
        return props;
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param propertyPaths
     *            the directories to search for the properties file
     * @param propertyFileName
     *            name of the sought properties file
     * @param showName
     *            whether the name of the file/resource being read should be
     *            shown.
     * @param filenameProperty
     *            the name of a property in which to store the name of the loaded
     *            property file
     * @param inputSourceReturn
     *            array where first element will be set to the full path of the
     *            loaded file
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName, boolean showName,
            String filenameProperty, String inputSourceReturn[])
    {
        String inputStreamSource[] = new String[] { null };
        InputStream in = getPropertyFileInputStream(propertyPaths, propertyFileName, showName, inputStreamSource);
        String errmsg = "Fatal error: Unable to find specified properties file: " + propertyFileName;

        // load the properties
        Properties props = new Properties();
        try
        {
            if (propertyFileName.endsWith(".xml") || propertyFileName.endsWith(".XML"))
            {
                props.loadFromXML(in);
            }
            else
            {
                props.load(in);
            }
            in.close();
            if (filenameProperty != null && inputStreamSource[0] != null)
            {
                File tmpFile = new File(inputStreamSource[0]);

                props.setProperty(filenameProperty, tmpFile.getParent());
            }
            if (inputSourceReturn != null && inputStreamSource[0] != null)
            {
                inputSourceReturn[0] = inputStreamSource[0];
            }
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(errmsg);
        }
        return props;
    }

    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName)
    {
        return (getPropertyFileInputStream(propertyPaths, propertyFileName, false));
    }

    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName, boolean showName)
    {
        return (getPropertyFileInputStream(propertyPaths, propertyFileName, false, null));
    }

    public static InputStream getPropertyFileInputStream(String propertyFileURLStr)
    {
        InputStream in = null;
        String errmsg = "Fatal error: Unable to open specified properties file: " + propertyFileURLStr;
        try
        {
            URL url = new URL(propertyFileURLStr);
            in = url.openStream();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(errmsg);
        }

        return (in);
    }

    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName, boolean showName,
            String inputSource[])
    {
        // InputStream in = null;
        String fullPropertyFileURLStr = getPropertyFileAbsoluteURL(propertyPaths, propertyFileName, showName, inputSource);
        return (getPropertyFileInputStream(fullPropertyFileURLStr));
    }

    public static String getPropertyFileAbsoluteURL(String[] propertyPaths, String propertyFileName, boolean showName,
            String inputSource[])
    {
        File propertyFileToReturn = null;
        String fullPathNameToReturn = null;
        int numFound = 0;
        // Check for Absolute path
        File propertyFile = new File(propertyFileName);
        if (propertyFile.isAbsolute() && propertyFile.exists() && propertyFile.isFile() && propertyFile.canRead())
        {
            numFound = 1;
            propertyFileToReturn = propertyFile;
            try
            {
                fullPathNameToReturn = propertyFile.toURI().toURL().toExternalForm();
            }
            catch (MalformedURLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (propertyPaths != null && propertyPaths.length != 0)
        {
            for (String pathPrefix : propertyPaths) 
            {
                propertyFile = new File(pathPrefix, propertyFileName);
                if (propertyFile.exists() && propertyFile.isFile() && propertyFile.canRead())
                {
                    if (inputSource != null && inputSource.length >= 1)
                    {
                        inputSource[0] = propertyFile.getAbsolutePath();
                    }
                    if (propertyFileToReturn == null) 
                    {
                        propertyFileToReturn = propertyFile;
                    }
                    else
                    {
                        logger.debug("Skipped opening file: " + propertyFile.getAbsolutePath());
                    }
                    numFound++;
                }
                else
                {
                    logger.debug("looked for file: " + propertyFile.getAbsolutePath());
                }
            }
        }
        if (propertyFileToReturn != null)
        {
            try
            {
                fullPathNameToReturn = propertyFileToReturn.toURI().toURL().toExternalForm();
            }
            catch (MalformedURLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (numFound == 0)
        {
            String errmsg = "Fatal error: Unable to find specified properties file: " + propertyFileName;
            logger.error(errmsg);
            throw new IndexerSpecException(eErrorSeverity.FATAL, errmsg);
        }
        else if (numFound == 1)
        {
            logger.debug("Opening file: " + propertyFileToReturn.getAbsolutePath());
        }
        else if (numFound > 1)
        {
            logger.info("Opening file (instead of "+(numFound-1)+ " other options): " + propertyFileToReturn.getAbsolutePath());
        }
        return (fullPathNameToReturn);
    }

    /**
     * Takes an InputStream, reads the entire contents into a String
     * 
     * @param stream
     *            the stream to read in.
     * @return String containing entire contents of stream.
     * @throws IOException  if there is an error reading the input stream
     */
    public static String readStreamIntoString(InputStream stream) throws IOException
    {
        Reader in = new BufferedReader(new InputStreamReader(stream));

        StringBuilder sb = new StringBuilder();
        char[] chars = new char[4096];
        int length;

        while ((length = in.read(chars)) > 0)
        {
            sb.append(chars, 0, length);
        }

        return sb.toString();
    }

    public static File findFirstExistingFile(String[] homeDirStrs, String indexSpec)
    {
        String inputSource[] = new String[1];
        URL fileURL = null;
        try
        {
            fileURL = new URL(getPropertyFileAbsoluteURL(homeDirStrs, indexSpec, true, inputSource));
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        File firstExisting = new File(fileURL.getFile()); 
        return(firstExisting);
    }
}
