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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
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
        MarcReader reader = null;
        Properties indexingProps = new Properties();
        indexingProps.setProperty("getformatmixin", "custom(org.solrmarc.index.GetFormatMixin), getContentTypesAndMediaTypes");
        boolean verbose = false;
        SolrIndexer testIndexer = SolrIndexer.indexerFromProperties(indexingProps, new String[0]);
        try
        {
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "formatRecs.mrc")), true, true, "MARC8");
            while (reader.hasNext())
            {
                Record record = reader.next();
                Map<String,Object> indexedRecord = testIndexer.map(record);
                Object result = indexedRecord.get("getformatmixin");
                if (result instanceof String)
                {
                    String format = result.toString();
                    if (verbose) System.out.println(format);
                }
                else if (result instanceof Set)
                {
                    Set<String> formats = (Set<String>)result;
                    for (String format : formats)
                    {
                        if (verbose) System.out.println(format);
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test recordfile  formatTests.mrc");
        }
        System.out.println("Test testRemoteIndexRecord is successful");
    }
}
