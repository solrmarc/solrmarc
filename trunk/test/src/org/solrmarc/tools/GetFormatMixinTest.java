package org.solrmarc.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.marc4j.ErrorHandler;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.marc.MarcCombiningReader;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.marcoverride.MarcSplitStreamWriter;


public class GetFormatMixinTest
{
    /**
     * unit test for org.solrmarc.marc.RawRecordReader and org.solrmarc.tools.RawRecord
     */
    @Test
    public void testGetFormatMixin()
    {
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        String testDataFile = System.getProperty("test.data.file");
        if (testDataFile == null || testDataFile.equals("${test.data.file}"))
            testDataFile = "formatRecs.mrc";
        System.err.println("test DataFile = "+ testDataFile);
        MarcReader reader = null;
        Properties indexingProps = new Properties();
        indexingProps.setProperty("getformatmixin", "custom(org.solrmarc.index.GetFormatMixin), getContentTypesAndMediaTypes");
        indexingProps.setProperty("getformatmixinmapped", "custom(org.solrmarc.index.GetFormatMixin), getContentTypesAndMediaTypes, getformat_mixin_map.properties");
        indexingProps.setProperty("getformatmixinunmapped", "custom(org.solrmarc.index.GetFormatMixin), getContentTypesAndMediaTypes, getformat_mixin_unmap_map.properties");
        indexingProps.setProperty("getformatmixinprimary", "custom(org.solrmarc.index.GetFormatMixin), getPrimaryContentType, getformat_mixin_map.properties");
        String verboseStr = System.getProperty("marc.verbose");
        boolean verbose = (verboseStr != null && verboseStr.equalsIgnoreCase("true"));
        ErrorHandler errors = new ErrorHandler();
        PrintStream out = null;
        if (verbose)
        {
            try
            {
                out = new PrintStream(System.out, true, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
            } 
        }
        SolrIndexer testIndexer = SolrIndexer.indexerFromProperties(indexingProps, new String[]{"translation_maps"});
        try
        {
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, testDataFile)), true, true, "MARC8");
            while (reader.hasNext())
            {
                Record record = reader.next();
                Map<String,Object> indexedRecord = testIndexer.map(record, errors);
                String id = record.getControlNumber();
                Object resultraw = indexedRecord.get("getformatmixin");
                Object resultmapped = indexedRecord.get("getformatmixinunmapped");
                Object resultmerged = mergeresults(resultraw, resultmapped);
                showResults(resultmerged, "raw   ", verbose, out, id);

                resultmapped = indexedRecord.get("getformatmixinmapped");
                showResults(resultmapped, "mapped", verbose, out, id);
                if (verbose) 
                {
                    if (errors.hasErrors())
                    {
                        Object prevError = "";
                        for (Object error : errors.getErrors())
                        {
                            if (!error.toString().equals(prevError.toString()))
                                out.println(error.toString());
                            prevError = error;
                        }
                    }
                    out.println(record.toString());
                }
                indexedRecord = testIndexer.map(record);
                errors.reset();
            }
        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test recordfile "+testDataFile);
        }
        System.out.println("Test testGetFormatMixin is successful");
    }

    private Object mergeresults(Object resultraw, Object resultmapped)
    {
        if (resultraw instanceof Set && resultmapped instanceof Set)
        {
            String[] resraw = ((Set<String>)resultraw).toArray(new String[0]);
            String[] resmapped = ((Set<String>)resultmapped).toArray(new String[0]);
            if (resraw.length != resmapped.length)
            {
                fail("merging failure, results sets different size");
            }
            
            Set<String> resmerged = new LinkedHashSet<String>();
            for (int i = 0; i < resraw.length; i++)
            {
                resmerged.add(resraw[i] + "\t--\t" + resmapped[i]);
            }
            return(resmerged);
        }
        else if (resultraw instanceof String && resultmapped instanceof String)
        {
            String resmerged = resultraw + "\t--\t" + resultmapped;
            return(resmerged);
        }
        else
        {
            fail("merging failure, results not both sets");
        }
        return(null);
    }

    private void showResults(Object result, String label, boolean verbose, PrintStream out, String id)
    {
        if (result instanceof String)
        {
            String format = result.toString();
            if (verbose) out.println(id + "("+label+") = " + format);
        }
        else if (result instanceof Set)
        {
            Set<String> formats = (Set<String>)result;
            for (String format : formats)
            {
                if (verbose) out.println(id + "("+label+") = " + format);
            }
        }
        
    }
}
