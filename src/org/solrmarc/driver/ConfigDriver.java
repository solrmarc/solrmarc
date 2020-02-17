package org.solrmarc.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.solrmarc.tools.PropertyUtils;
import org.solrmarc.tools.Utils;


public class ConfigDriver extends BootableMain
{
    private static LoggerDelegator logger = new LoggerDelegator(ConfigDriver.class);

    public static void main(String[] args)
    {
        String configProps = args[0];
        logger.debug("Using config " + configProps + " to initialize SolrMarc");
        // Some of the values in this file will be extracted and passed as command line arguments others will be
        // extracted directly by the MarcReader configuration handler.  This is done by passing the same property file
        // to the MarcReader configurations handler, which will extract the fields it wants and ignore the rest.
        String homeDirStr = Boot.getDefaultHomeDir();
        String configDirStr = null;
        File configFile = new File(configProps);
        boolean configEqHomeDir = false;
        if (configFile.exists())
        {
            configDirStr = configFile.getAbsoluteFile().getParentFile().getAbsolutePath();
            configEqHomeDir = configDirStr.equals(homeDirStr);
        }
        else if (!configFile.isAbsolute())
        {
            configFile = new File(homeDirStr, configProps);
            configEqHomeDir = true;
        }
        
        Properties configProperties = new Properties();
        try
        {
            configProperties.load(new FileInputStream(configFile.getAbsolutePath()));
        }
        catch (FileNotFoundException e)
        {
            logger.fatal("Unable to find old-style solrmarc config file named: " + configFile.getAbsolutePath());
            LoggerDelegator.flushToLog();
            System.exit(2);
        }
        catch (IOException e)
        {
            logger.fatal("Unable to read old-style solrmarc config file named: " + configFile.getAbsolutePath());
            LoggerDelegator.flushToLog();
            System.exit(2);
        }

        String marcreaderProperties = configFile.getName();
        String solrHosturl = PropertyUtils.getProperty(configProperties, "solr.hosturl");
        String solrCommit  = PropertyUtils.getProperty(configProperties, "solrmarc.commit.at.end");
        String solrIndexerProperties = PropertyUtils.getProperty(configProperties, "solr.indexer.properties");
        String solrmarcPath = PropertyUtils.getProperty(configProperties, "solrmarc.path");
        if (configDirStr != null)
        {
            File localDir = new File(".");
            String localDirStr;
            try {
                localDirStr = localDir.getCanonicalPath();
            }
            catch (IOException ioe)
            {
                localDirStr = localDir.getAbsolutePath();
            }
            Path pathAbsolute = Paths.get(configDirStr);
            Path pathBase = Paths.get(localDirStr);
            Path pathRelative = pathBase.relativize(pathAbsolute);
            String dirParm = pathRelative.toString(); 
            if (pathRelative.toString().length() == 0 && !configEqHomeDir)
            {
                dirParm = ".";
            }
            if (solrmarcPath == null || solrmarcPath.length() == 0 || (solrmarcPath.equals(".") && !configEqHomeDir ))
            {
                solrmarcPath = dirParm;
            }
            else
            {
                solrmarcPath = solrmarcPath + ";" + dirParm;
            }
        }

        // get the value from the property file AND the value from the command line
        // If they are different, use those two values to replace the corename in the solr URL
        String solrCore = configProperties.getProperty("solr.core.name");
        String systemSolrCore = PropertyUtils.getProperty(configProperties, "solr.core.name");

        if (solrCore != null && systemSolrCore != null && !solrCore.equals(systemSolrCore) && solrHosturl.contains(solrCore))
        {
            logger.debug("Replacing corename " + solrCore + " with corename "+ systemSolrCore);
            solrHosturl = solrHosturl.replace(solrCore, systemSolrCore);
            logger.debug("New Solr URL is "+ solrHosturl);
        }
        String dirArg[] = (solrmarcPath == null || solrmarcPath.length() == 0  || (solrmarcPath.equals(".") && configEqHomeDir ))
                        ? new String[0]
                        : new String[]{"-dir", solrmarcPath};
        if (solrIndexerProperties == null)
        {
            logger.error("The provided old-style SolrMarc config.properties file doesn't define the value \"solr.indexer.properties\"");
            LoggerDelegator.flushToLog();
            System.exit(1);
        }
        String configArg[] = {"-config",  solrIndexerProperties};
        String urlArg[] = (solrHosturl != null) ? new String[]{"-solrURL", solrHosturl}  : new String[0];
        String commitArg[] = (solrCommit  != null) ? new String[]{"-solrCommit", solrCommit}   : new String[0];
        String marcReaderArg[] = { "-reader_opts", marcreaderProperties};
        
        List<String> driverArgs = new ArrayList<>();
        driverArgs.addAll(Arrays.asList(marcReaderArg));
        driverArgs.addAll(Arrays.asList(dirArg));
        driverArgs.addAll(Arrays.asList(configArg));
        driverArgs.addAll(Arrays.asList(urlArg));
        driverArgs.addAll(Arrays.asList(commitArg));
        for (int i = 1; i < args.length; i++)
        {
            driverArgs.add(args[i]);
        }
        
        String effectiveCommandLine = "java -jar solrmarc_core.jar IndexDriver " + Utils.join(quoteIfHasSpace(driverArgs), " ");
        logger.info("Effective Command Line is:");
        logger.info("   " + effectiveCommandLine);

        Boot.invokeMain("org.solrmarc.driver.IndexDriver", driverArgs.toArray(new String[0]));
    }
    
    public ConfigDriver(String[] args, List<String> driverArgs) 
    {
     }

    private static String[] quoteIfHasSpace(List<String> driverArgs)
    {
        String[] result = new String[driverArgs.size()];
        int i = 0;
        for (String arg : driverArgs)
        {
            result[i++] = (arg.contains(" ")) ? "\"" + arg +  "\"" : arg;
        }
        return(result);
    }
}
