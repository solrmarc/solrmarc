package org.solrmarc.driver;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.solrmarc.solr.SolrRuntimeException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Pure unit tests for BootableMain.resolveSolrPassword(). Deliberately
 * builds its own minimal OptionParser/OptionSet rather than going through
 * the full BootableMain.processArgs() machinery, and touches no network
 * or Solr connection at all - this only exercises option/file resolution.
 */
public class BootableMainSolrPasswordTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private OptionSpec<String> solrPassword;
    private OptionSpec<File> solrPasswordFile;
    private OptionParser parser;

    private OptionSet parse(String... args)
    {
        parser = new OptionParser();
        solrPassword = parser.accepts("solrPassword").withRequiredArg();
        solrPasswordFile = parser.accepts("solrPasswordFile").withRequiredArg().ofType(File.class);
        return parser.parse(args);
    }

    @Test
    public void neitherOptionSuppliedReturnsNull()
    {
        OptionSet options = parse();
        assertNull(BootableMain.resolveSolrPassword(options, solrPassword, solrPasswordFile));
    }

    @Test
    public void plainPasswordOptionIsReturnedAsIs()
    {
        OptionSet options = parse("-solrPassword", "hunter2");
        assertEquals("hunter2", BootableMain.resolveSolrPassword(options, solrPassword, solrPasswordFile));
    }

    @Test
    public void passwordFileContentIsReadAndTrimmed()
    {
        File pwFile = writePasswordFile("hunter2\n");
        OptionSet options = parse("-solrPasswordFile", pwFile.getAbsolutePath());
        assertEquals("hunter2", BootableMain.resolveSolrPassword(options, solrPassword, solrPasswordFile));
    }

    @Test
    public void passwordFileWithTrailingWhitespaceIsTrimmed()
    {
        File pwFile = writePasswordFile("  hunter2  \n");
        OptionSet options = parse("-solrPasswordFile", pwFile.getAbsolutePath());
        assertEquals("hunter2", BootableMain.resolveSolrPassword(options, solrPassword, solrPasswordFile));
    }

    @Test
    public void bothOptionsSuppliedThrows()
    {
        File pwFile = writePasswordFile("hunter2\n");
        OptionSet options = parse("-solrPassword", "someOtherPassword", "-solrPasswordFile", pwFile.getAbsolutePath());
        try
        {
            BootableMain.resolveSolrPassword(options, solrPassword, solrPasswordFile);
            fail("expected a SolrRuntimeException for specifying both options");
        }
        catch (SolrRuntimeException e)
        {
            assertTrue("message should explain the conflict: " + e.getMessage(),
                e.getMessage().contains("not both"));
        }
    }

    @Test
    public void missingPasswordFileThrowsWithFileNameInMessage()
    {
        File missingFile = new File(tempFolder.getRoot(), "does-not-exist.txt");
        OptionSet options = parse("-solrPasswordFile", missingFile.getAbsolutePath());
        try
        {
            BootableMain.resolveSolrPassword(options, solrPassword, solrPasswordFile);
            fail("expected a SolrRuntimeException for a missing password file");
        }
        catch (SolrRuntimeException e)
        {
            assertTrue("message should include the file path: " + e.getMessage(),
                e.getMessage().contains(missingFile.getAbsolutePath()));
        }
    }

    @Test
    public void emptyPasswordFileReturnsNull()
    {
        // readLine() on an empty file returns null, matching "no password
        // provided" rather than an empty string - worth pinning down
        // explicitly since it's an easy edge case to get wrong.
        File pwFile = writePasswordFile("");
        OptionSet options = parse("-solrPasswordFile", pwFile.getAbsolutePath());
        assertNull(BootableMain.resolveSolrPassword(options, solrPassword, solrPasswordFile));
    }

    private File writePasswordFile(String content)
    {
        try
        {
            File file = tempFolder.newFile("solr-password.txt");
            try (FileWriter writer = new FileWriter(file))
            {
                writer.write(content);
            }
            return file;
        }
        catch (Exception e)
        {
            throw new RuntimeException("test setup failure", e);
        }
    }
}