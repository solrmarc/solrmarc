package org.blacklight;

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
    
    @Test
    public void testGetFormatVsMixin()
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
        indexingProps.setProperty("format_orig_facet", "custom, getCombinedFormatNew2");
        indexingProps.setProperty("format_diff", "custom,  getFormatDiff");
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
        BlacklightIndexer testIndexer = new BlacklightIndexer(null, new String[]{"translation_maps"});
        testIndexer.reinitFromProperties(indexingProps);
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
                Object format_orig_facet = indexedRecord.get("format_orig_facet");
                Object resultmerged = mergeresults(resultraw, resultmapped);
                Object resultdiff = indexedRecord.get("format_diff");
                resultmapped = indexedRecord.get("getformatmixinmapped");
                if (resultdiff != null)
                {
                    if (!setsEqual(resultmapped, format_orig_facet) )// || errors.hasErrors())
                    {
                        boolean breakOut = false;
                        if (errors.hasErrors())
                        {
                            List<?> errorMsgs = errors.getErrors();
                            for (Object errorMsg : errorMsgs)
                            {
                                String msgStr = errorMsg.toString();
//                                if (msgStr.contains("Record contains minimal metadata"))
//                                {
//                                    breakOut = true;
//                                }
                            }
                        }
                        if (breakOut) continue;
                        showResults(resultdiff,   "diff  ", verbose, out, id);
                        showResults(resultmerged, "raw   ", verbose, out, id);
                        showResults(resultmapped, "new   ", verbose, out, id);                    
                        showResults(format_orig_facet, "old   ", verbose, out, id);                    
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
                            errors.reset();
                        }
                    }
                    else
                    {
                        showResults(resultmapped, "a-ok  ", verbose, out, id); 
                        out.println();
                    }
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

    private boolean setsEqual(Object resultmapped, Object format_orig_facet)
    {
        if (format_orig_facet instanceof Set && resultmapped instanceof Set)
        {
            Set<String> resold = (Set<String>)format_orig_facet;
            Set<String> resmapped = (Set<String>)resultmapped;
            if (!resold.containsAll(resmapped)) 
            {
                return(false);
            }
            if (!resmapped.containsAll(resold)) 
            {
                return(false);
            }
            return(true);
        }
        else if (format_orig_facet instanceof String && resultmapped instanceof String)
        {
            String resold = (String)format_orig_facet;
            String resmapped = (String)resultmapped;
            if (resold.equals(resmapped))
            {
                return(true);
            }
        }
        else
        {
            return(false);           
        }
        return false;
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
        else if (resultraw == null && resultmapped == null)
        {
            return(null);
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
