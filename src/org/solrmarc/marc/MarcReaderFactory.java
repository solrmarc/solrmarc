package org.solrmarc.marc;


import java.io.*;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.marc4j.MarcReader;
import org.solrmarc.tools.PropertyUtils;

public class MarcReaderFactory {

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

    public MarcReader makeReader(Properties props, String[] searchDirectories, String ... inputFilenames) {
        org.marc4j.MarcReaderConfig config = new org.marc4j.MarcReaderConfig(props);
        setMarc4JProperties(props);
        try {
            MarcReader reader = org.marc4j.MarcReaderFactory.makeReader(config, searchDirectories, inputFilenames);
            return(reader);
        }
        catch(FileNotFoundException fne) {
            throw new IllegalArgumentException("Fatal error: Exception opening InputStream" + fne.getMessage());
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Fatal error: Exception reading InputStream" + e.getMessage());
        }
    }

    public MarcReader makeReader(Properties props, String[] searchDirectories, List<String> inputFilenames) {
        org.marc4j.MarcReaderConfig config = new org.marc4j.MarcReaderConfig(props);
        setMarc4JProperties(props);
        try {
            MarcReader reader = org.marc4j.MarcReaderFactory.makeReader(config, searchDirectories, inputFilenames);
            return(reader);
        }
        catch(FileNotFoundException fne) {
            throw new IllegalArgumentException("Fatal error: Exception opening InputStream" + fne.getMessage());
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Fatal error: Exception reading InputStream" + e.getMessage());
        }
    }

	public MarcReader makeReader(Properties props, String[] searchDirectories, String inputFilename) {
	    org.marc4j.MarcReaderConfig config = new org.marc4j.MarcReaderConfig(props);
        setMarc4JProperties(props);
        try {
            MarcReader reader = org.marc4j.MarcReaderFactory.makeReader(config, searchDirectories, inputFilename);
            return(reader);
        }
        catch(FileNotFoundException fne) {
            throw new IllegalArgumentException("Fatal error: Exception opening InputStream" + fne.getMessage());
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Fatal error: Exception reading InputStream" + e.getMessage());
        }
    }

	public MarcReader makeReader(Properties props, String[] searchDirectories, InputStream input) {
        org.marc4j.MarcReaderConfig config = new org.marc4j.MarcReaderConfig(props);
        setMarc4JProperties(props);
        try {
            MarcReader reader = org.marc4j.MarcReaderFactory.makeReader(config, input);
            return(reader);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Fatal error: Exception reading InputStream" + e.getMessage());
        }
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
}
