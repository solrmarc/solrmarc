package org.solrmarc.driver;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marc4j.MarcError;
import org.marc4j.MarcException;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcReader;
import org.marc4j.MarcReaderConfig;
import org.marc4j.MarcReaderFactory;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.ValueIndexerFactory;
//import org.solrmarc.marc.MarcReaderFactory;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.tools.PropertyUtils;


public class RecordFixer extends BootableMain
{
    public final static Logger logger = LogManager.getLogger(IndexDriver.class);

    Properties readerProps;
    MarcReaderConfig readerConfig;
    
    List<AbstractValueIndexer<?>> indexers;
    Indexer indexer;
    MarcReader reader;
    SolrProxy solrProxy;
    boolean verbose;
    int numIndexed[];
    String[] args;
    long startTime;
    Thread shutdownSimulator = null;

    private MarcWriter writer;

    public static void main(String[] args)
    {
        RecordFixer driver = new RecordFixer(args);
        driver.execute();
    }

    public RecordFixer(String[] args)
    {
        this.args = args;
    }

    public void execute()
    {
        processArgs(args, true);
        ValueIndexerFactory.initialize(homeDirStrs);
        initializeFromOptions();

        List<String> inputFiles = options.valuesOf(files);
        logger.info("Opening input files: " + Arrays.toString(inputFiles.toArray()));
        this.configureReader(inputFiles);

        this.processInput();
    }

    public void initializeFromOptions()
    {
        String inputSource[] = new String[1];
        String propertyFileAsURLStr = PropertyUtils.getPropertyFileAbsoluteURL(homeDirStrs, options.valueOf(readOpts), true, inputSource);

        // File f1 = new File(options.valueOf(readOpts));
        try
        {
            configureReaderProps(propertyFileAsURLStr);
        }
        catch (IOException e1)
        {
            logger.fatal("Fatal error: Exception opening reader properties input stream: " + inputSource[0]);
            logger.error("Exiting...");
            System.exit(1);
        }
        String outputType = options.has("solrURL") ? options.valueOf("solrURL").toString() : options.has("null") ? "devnull" : "stdout";
        this.configureOutput(outputType);
    }

    public void configureReaderProps(String propertyFileURLStr) throws FileNotFoundException, IOException
    {
        readerProps = new Properties();
        if (propertyFileURLStr != null)
        {
            readerProps.load(PropertyUtils.getPropertyFileInputStream(propertyFileURLStr));
        }
        readerConfig = new MarcReaderConfig(readerProps);
    }

    public void configureReader(List<String> inputFilenames)
    {
        try {
            reader = MarcReaderFactory.makeReader(readerConfig, ValueIndexerFactory.instance().getHomeDirs(), inputFilenames);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public void configureOutput(String mode)
    {
        PrintStream marcOut = null;
        try
        {
            String sysoutRedir = System.getProperty("solrmarc.output.redirect", null);
            if (sysoutRedir != null)
            {
                marcOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(sysoutRedir)), true);
            }
            else
            {
                marcOut = System.out;
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (mode.equals("to_xml"))
        {
            writer = new MarcXmlWriter(marcOut, "UTF-8", true);
        }
        else if (mode.equals("to_json"))
        {
            writer = new MarcJsonWriter(marcOut, MarcJsonWriter.MARC_IN_JSON);
        }
        else if (mode.equals("to_utf8"))
        {
            writer = new MarcStreamWriter(marcOut, "UTF-8", true);
        }
        else if (mode.equals("to_marc8"))
        {
            writer = new MarcStreamWriter(marcOut, "ISO8859_1", true);
            writer.setConverter(new UnicodeToAnsel());
        }
        else if (mode.equals("to_ncr"))
        {
            writer = new MarcStreamWriter(marcOut, "ISO8859_1", true);
            writer.setConverter(new UnicodeToAnsel(true));
        }
//        else if (mode.equals("untranslateNCRifneeded"))
//        {
//            if (writer == null)
//            {
//                baos = new ByteArrayOutputStream();
//                conv = new UnicodeToAnsel();
//                convNCR = new UnicodeToAnsel(true);
//                writer = new MarcStreamWriter(baos, "ISO8859_1", true);
//                writer.setConverter(conv);
//            }
//            baos.reset();
//            record.getLeader().setCharCodingScheme(' ');
//            writer.setConverter(conv);
//            writer.write(record);
//            baos.flush();
//            byte[] bytes = baos.toByteArray();
//            if (byteArrayContains(bytes, "|".getBytes()))
//            {
//                baos.reset();
//                writer.setConverter(convNCR);
//                writer.write(record);
//            }
//            baos.flush();
//            System.out.write(baos.toByteArray());
//        }
    }

    public void processInput()
    {
        while (true)
        {
            Record record = null;
            try {
                if (reader.hasNext())
                    record = reader.next();
                else 
                    break;
            }
            catch (MarcException me)
            {
                logger.error("Unrecoverable Error in MARC record data", me);
                if (Boolean.parseBoolean(System.getProperty("solrmarc.terminate.on.marc.exception", "true")))
                    break;
                else 
                {
                    logger.warn("Trying to continue after MARC record data error");
                    continue;
                }
            }
            if (record.hasErrors())
            {
                reportMarcErrors(record, record.getErrors());
            }

            writer.write(record);
        }

        writer.close();
    }

    private void reportMarcErrors(Record record, List<MarcError> errors)
    {
        String id = record.getControlNumber();
        if (id == null) id = "No_001_Field";
        for (Object err : errors)
        {
            logger.info(id + " : " + err.toString());
        }
    }

}
