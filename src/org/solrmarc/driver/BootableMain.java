package org.solrmarc.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.solrmarc.index.indexer.IndexerSpecException;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class BootableMain 
{
    public final static Logger logger = Logger.getLogger(IndexDriver.class);

    protected String homeDirStrs[];
    protected String addnlLibDirStrs[];
    OptionSpec<String> readOpts;
    OptionSpec<String> configSpecs;
    OptionSpec<String> homeDirs;
    OptionSpec<String> addnlLibDirs;
    OptionSpec<File> solrjDir;
    OptionSpec<String> solrjClass;
    OptionSpec<File> errorMarcErrOutFile;
    OptionSpec<File> errorIndexErrOutFile;
    OptionSpec<File> errorSolrErrOutFile;
    OptionSpec<String> files;
    OptionSet options = null;

    public void processArgs(String args[], boolean failOnZeroArgs)
    {
        OptionParser parser = new OptionParser(  );
        readOpts = parser.acceptsAll(Arrays.asList( "r", "reader_opts"), "file containing MARC Reader options").withRequiredArg().defaultsTo("marcreader.properties");
        configSpecs = parser.acceptsAll(Arrays.asList( "c", "config"), "index specification file to use").withRequiredArg();
        homeDirs = parser.accepts("dir", "directory to look in for scripts, mixins, and translation maps").withRequiredArg().ofType( String.class );
        addnlLibDirs = parser.accepts("lib_local", "directory to look in for additional jars and libraries").withRequiredArg().defaultsTo("lib_local");
        solrjDir = parser.accepts("solrj", "directory to look in for jars required for SolrJ").withRequiredArg().ofType( File.class );
        solrjClass = parser.accepts("solrjClassName", "Classname of class to use for talking to solr").withRequiredArg();
        errorMarcErrOutFile = parser.accepts("marcerr", "File to write records with errors.(not yet implemented)").withRequiredArg().ofType( File.class );
        errorIndexErrOutFile = parser.accepts("indexerr", "File to write the solr documents for records with errors.(not yet implemented)").withRequiredArg().ofType( File.class );
        errorSolrErrOutFile = parser.accepts("solrerr", "File to write the solr documents for records with errors.(not yet implemented)").withRequiredArg().ofType( File.class );
        parser.accepts("debug", "non-multithreaded debug mode");
        parser.acceptsAll(Arrays.asList( "solrURL", "u"), "URL of Remote Solr to use").withRequiredArg();
        parser.acceptsAll(Arrays.asList("print", "stdout"), "write output to stdout in user readable format");//.availableUnless("sorlURL");
        parser.acceptsAll(Arrays.asList("null"), "discard all output, and merely show errors and warnings");//.availableUnless("sorlURL");
        parser.acceptsAll(Arrays.asList("?", "help"), "show this usage information").forHelp();
        //parser.mutuallyExclusive("stdout", "solrURL");
        files = parser.nonOptions().ofType( String.class );

        options = null;
        try {
            options = parser.parse(args );
        }
        catch (OptionException uoe)
        {
            try
            {
                System.err.println(uoe.getMessage());
                parser.printHelpOn(System.err);
            }
            catch (IOException e)
            {
            }
            System.exit(1);
        }
        if ((failOnZeroArgs && args.length == 0) || options.has("help")) 
        {
            try
            {
                parser.printHelpOn(System.err);
            }
            catch (IOException e)
            {
            }
            System.exit(0);
        }
        if (options.has("dir"))
        {
            File defDir = new File(Boot.getDefaultHomeDir());
            List<String> homeDirList = new ArrayList<>();
            boolean hasDefDir = false;
            for (String dir :  (options.valueOf(homeDirs).replaceAll("[,;]", "|").split("[|]")))
            {
                File dirAsFile = new File(dir);
                if (dirAsFile.getAbsolutePath().equals(defDir.getAbsolutePath()))
                {
                    hasDefDir = true;
                }
                homeDirList.add(dirAsFile.getAbsolutePath());
            }
            if (!hasDefDir)  homeDirList.add(defDir.getAbsolutePath());
            homeDirStrs = homeDirList.toArray(new String[0]);
        }
        else 
        {
            homeDirStrs = new String[]{ Boot.getDefaultHomeDir() };
        }
        
        File solrJPath = ((options.has(solrjDir)) ? options.valueOf(solrjDir) : new File("lib-solrj"));
        
        try { 
            if (solrJPath.isAbsolute()) 
            {
                Boot.extendClasspathWithSolJJarDir(null, solrJPath);
            }
            else
            {
                Boot.extendClasspathWithSolJJarDir(homeDirStrs, solrJPath);
            }
        }
        catch (IndexerSpecException ise)
        {
            logger.fatal("Fatal error: Failure to load SolrJ", ise);
            logger.error("Exiting...");
            System.exit(10);
        }
        
        // Now add local lib directories
        try { 
            if (addnlLibDirs.value(options)!= null)
            {
                addnlLibDirStrs = addnlLibDirs.value(options).split("[,;|]");
                Boot.extendClasspathWithLocalJarDirs(homeDirStrs, addnlLibDirStrs);
            }
        }
        catch (IndexerSpecException ise)
        {
            logger.fatal("Fatal error: Failure to load SolrJ", ise);
            logger.error("Exiting...");
            System.exit(10);
        }

        initLogging(homeDirStrs);

    }

    private void initLogging(String[] homeDirs)
    {
        for (String dir : homeDirs)
        {
            File log4jProps = new File(dir, "log4j.properties");
            if (log4jProps.exists())
            {
                LogManager.resetConfiguration();
                PropertyConfigurator.configure(log4jProps.getAbsolutePath());
                return;
            }
        }
    }

//    /**
//     * Returns true if it appears that log4j have been previously configured. This code
//     * checks to see if there are any appenders defined for log4j which is the
//     * definitive way to tell if log4j is already initialized
//     */
//    private static boolean isLog4jConfigured()
//    {
//        Enumeration<?> appenders = LogManager.getRootLogger().getAllAppenders();
//        if (appenders.hasMoreElements())
//        {
//            return true;
//        }
//        else
//        {
//            Enumeration<?> loggers = LogManager.getCurrentLoggers();
//            while (loggers.hasMoreElements())
//            {
//                Logger c = (Logger) loggers.nextElement();
//                if (c.getAllAppenders().hasMoreElements()) return true;
//            }
//        }
//        return false;
//    }
    
}
