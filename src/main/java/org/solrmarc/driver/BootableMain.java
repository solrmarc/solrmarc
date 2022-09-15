package org.solrmarc.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.solrmarc.index.indexer.IndexerSpecException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * This class is the common parent for top level executable programs included as a part of SolrMarc,
 * most notably IndexDriver, and SolrMarcDebug.  Its purpose is to handle command-line arguments
 * that are passed to any of those programs.  It relies on the external library joptsimple.
 * https://pholser.github.io/jopt-simple/
 *
 * @author rh9ec
 *
 */

public class BootableMain
{
    private static final LoggerDelegator logger = new LoggerDelegator(BootableMain.class);

    protected String[] homeDirStrs;
    protected String[] addnlLibDirStrs;
    protected OptionSpec<String> readOpts;
    protected OptionSpec<String> configSpecs;
    protected OptionSpec<String> homeDirs;
    protected OptionSpec<String> addnlLibDirs;
    protected OptionSpec<File> solrjDir;
    protected OptionSpec<String> solrjClass;
    protected OptionSpec<File> deleteRecordByIdFile;
    protected OptionSpec<File> errorMarcErrOutFile;
    protected OptionSpec<File> errorIndexErrOutFile;
    protected OptionSpec<File> errorSolrErrOutFile;
    protected OptionSpec<String> files;
    protected OptionSet options = null;

    /**
     * Extract command line arguments and store them in various protected variables.
     *
     * @param args - the command line arguments passed to the Boot class, except for the first one
     *                which specifies which main program is to be executed.
     *
     * @param failOnZeroArgs - true will cause the program to exit and print a help message
     *                detailing the valid command-line arguments.  false will simply do nothing
     *                and return.
     */
    protected void processArgs(String[] args, boolean failOnZeroArgs)
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
        deleteRecordByIdFile = parser.accepts("del", "File to read list of document ids that are to be deleted").withRequiredArg().ofType( File.class );
        parser.accepts("debug", "non-multithreaded debug mode");
        parser.acceptsAll(Arrays.asList( "solrURL", "u"), "URL of Remote Solr to use").withRequiredArg();
        parser.acceptsAll(Arrays.asList( "solrCommit", "c"), "Whether to commit, true or false").withRequiredArg();
      //  parser.acceptsAll(Arrays.asList("print", "stdout"), "write output to stdout in user readable format").availableUnless("solrURL");
     //   parser.acceptsAll(Arrays.asList("null"), "discard all output, and merely show errors and warnings").availableUnless("solrURL");
        parser.acceptsAll(Arrays.asList("?", "help"), "show this usage information").forHelp();
        //parser.mutuallyExclusive("stdout", "solrURL");
        processAddnlArgs(parser);
        files = parser.nonOptions().ofType( String.class );

        options = null;
        try {
            options = parser.parse(args );
        }
        catch (Exception uoe)
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
                logger.debug(dir);
                File dirAsFile = new File(dir);
                if (dirAsFile.getAbsolutePath().equals(defDir.getAbsolutePath()))
                {
                    hasDefDir = true;
                }
                if (!homeDirList.contains(dirAsFile.getAbsolutePath())) {
                    homeDirList.add(dirAsFile.getAbsolutePath());
                    logger.debug("Adding directory: " + dirAsFile.getAbsolutePath());
                }
                logger.debug("checking for loop");

            }
            logger.debug("for loop done");

            if (!hasDefDir)
            {
                homeDirList.add(defDir.getAbsolutePath());
            }
            this.homeDirStrs = ((String[]) homeDirList.toArray(new String[0]));
        }
        else
        {
            homeDirStrs = new String[]{ Boot.getDefaultHomeDir() };
        }
        logger.debug("setting property " );

        System.setProperty("org.solrmarc.home.dir", homeDirStrs[0]);
        logger.debug("reinit" );

        LoggerDelegator.reInit(this.homeDirStrs);
        if (needsSolrJ())
        {
            if (!hasSolrJ())
            {
                logger.debug("set solrjpath" );
                File solrJPath = (options.has(this.solrjDir) ? (File) this.options.valueOf(this.solrjDir) : new File("lib-solrj"));
                try
                {
                    if (solrJPath.isAbsolute())
                    {
                        logger.debug("is absolute" );
                        Boot.extendClasspathWithSolJJarDir(null, solrJPath);
                    }
                    else
                    {
                        logger.debug("not absolute " );
                        Boot.extendClasspathWithSolJJarDir(this.homeDirStrs, solrJPath);
                    }
                }
                catch (IndexerSpecException ise)
                {
                    logger.fatal("Fatal error: Failure to load SolrJ", ise);
                    logger.error("Exiting...");
                    System.exit(10);
                }
            }
        }
        // Now add local lib directories
        try {
            if (addnlLibDirs.value(options) != null)
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
    }

    protected void processAddnlArgs(OptionParser parser)
    {
        // TODO Auto-generated method stub

    }

    private boolean hasSolrJ()
    {
        logger.debug("has solr?" );
        try {
            Boot.classForName("org.apache.solr.common.SolrInputDocument");
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
        return true;
    }

    protected boolean needsSolrJ()
    {
        return true;
    }
}
