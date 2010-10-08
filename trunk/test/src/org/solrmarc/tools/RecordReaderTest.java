package org.solrmarc.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.marc.MarcCombiningReader;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.marcoverride.MarcSplitStreamWriter;


public class RecordReaderTest
{
    /**
     * unit test for org.solrmarc.marc.RawRecordReader and org.solrmarc.tools.RawRecord
     */
    @Test
    public void testRawRecordReader()
    {
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        RawRecordReader rawReader = null;
        MarcReader reader = null;
        try
        {
            rawReader = new RawRecordReader(new FileInputStream(new File(testDataParentPath, "u4.mrc")));
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "u4.mrc")), true, true, "MARC8");
            
            RawRecord rawRec = null;
            if (rawReader.hasNext()) rawRec = rawReader.next();
            Record rec = null;
            if (reader.hasNext())  rec = reader.next();
            Record rec2 = rawRec.getAsRecord(true, true, null, "MARC8");
            assertRecordsEquals("record read via RawReader different from record read via Permissive reader", rec, rec2);
        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test record  u4.mrc");
        }
    }
    /**
     * unit test for org.solrmarc.marcoverride.MarcSplitStreamWriter and org.solrmarc.marc.MarcCombiningReader
     */
    @Test
    public void testCombiningReaderAndSplitStreamWriter()
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        MarcReader reader = null;
        try
        {
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "bad_too_long_plus_2.mrc")), true, true, "MARC8");
            
            Record rec = null;
            if (reader.hasNext())  rec = reader.next();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MarcSplitStreamWriter writer = new MarcSplitStreamWriter(output, "UTF-8", 70000, "991");
            writer.write(rec);
            writer.close();
            MarcReader reader2 = new MarcPermissiveStreamReader(new ByteArrayInputStream(output.toByteArray()), true, true, "MARC8");
            MarcReader reader3 = new MarcCombiningReader(reader2, "991", null, null);

            Record rec2 = null;
            if (reader3.hasNext()) rec2 = reader3.next(); 
            assertRecordsEquals("record read directly is different from record read in written using SplitStreamWriter, and combined again", rec, rec2);
            
            MarcReader reader4 = new MarcPermissiveStreamReader(new ByteArrayInputStream(output.toByteArray()), true, true, "MARC8");
            Record rec4 = null;
            if (reader4.hasNext()) rec4 = reader4.next();
            assertRecordIsSubset("record read directly ought to be different from record read in written using SplitStreamWriter, but not combined reader", rec, rec4);

        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test record  bad_too_long_plus_2.mrc");
        }
    }
    /**
     * unit test for org.solrmarc.marcoverride.MarcSplitStreamWriter and org.solrmarc.marc.RawRecordReader
     */
    @Test
    public void testRawRecordCombiningAndSplitStreamWriter()
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        MarcReader reader = null;
        try
        {
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "bad_too_long_plus_2.mrc")), true, true, "MARC8");
            
            Record rec = null;
            if (reader.hasNext())  rec = reader.next();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MarcSplitStreamWriter writer = new MarcSplitStreamWriter(output, "UTF-8", 70000, "991");
            writer.write(rec);
            writer.close();
            MarcReader reader2 = new MarcPermissiveStreamReader(new ByteArrayInputStream(output.toByteArray()), true, true, "MARC8");
            RawRecordReader reader3 = new RawRecordReader(new ByteArrayInputStream(output.toByteArray()));

            RawRecord rawRec = null;
            if (reader3.hasNext()) rawRec = reader3.next(); 
            Record rec2 = rawRec.getAsRecord(true, true, "991", "MARC8");
            assertRecordsEquals("record read directly is different from record read in written using SplitStreamWriter, and combined again", rec, rec2);
        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test record  bad_too_long_plus_2.mrc");
        }
    }
    
    private void assertRecordsEquals(String message, Record rec1, Record rec2)
    {
        int result = compareRecords(rec1, rec2);
        String messageMore = null;
        if (result == 1) message = "Control Fields are different between rec1 and rec2";
        else if (result == 2) message = "Subfields are different between rec1 and rec2";
        else if (result == 3) message = "One record has a DataField where another has a ControlField";
        else if (result == -1) message = "Done with one record but not the other";
        assertEquals(message+" "+messageMore, 0, result);
    }
    
    private void assertRecordIsSubset(String message, Record rec1, Record rec2)
    {
        int result = compareRecords(rec1, rec2);
        String messageMore= null;
        if (result == 1) message = "Control Fields are different between rec1 and rec2";
        else if (result == 2) message = "Subfields are different between rec1 and rec2";
        else if (result == 3) message = "One record has a DataField where another has a ControlField";
        else if (result == 0) message = "Records are equal when they shouldn't be";
        assertEquals(message+" "+messageMore, -1, result);
    }
    
    private int compareRecords(Record rec1, Record rec2)
    {
        List<VariableField> fields1 = (List<VariableField>)rec1.getVariableFields();
        List<VariableField> fields2 = (List<VariableField>)rec2.getVariableFields();
        Iterator<VariableField> iter1 = fields1.iterator();
        Iterator<VariableField> iter2 = fields2.iterator();
        while (iter1.hasNext() && iter2.hasNext())
        {
            VariableField f1 = iter1.next();
            VariableField f2 = iter2.next();
            if (f1 instanceof ControlField && f2 instanceof ControlField)
            {
                ControlField cf1 = (ControlField)f1;
                ControlField cf2 = (ControlField)f2;
                if (! cf1.getData().equals(cf2.getData()))  return(1);
            }
            else if (f1 instanceof DataField && f2 instanceof DataField)
            {
                DataField df1 = (DataField)f1;
                DataField df2 = (DataField)f2;
                List<Subfield> sfs1 = (List<Subfield>)df1.getSubfields();
                List<Subfield> sfs2 = (List<Subfield>)df2.getSubfields();
                Iterator<Subfield> iter3 = sfs1.iterator();
                Iterator<Subfield> iter4 = sfs2.iterator();
                while (iter3.hasNext() && iter4.hasNext())
                {
                    Subfield sf1 = iter3.next();
                    Subfield sf2 = iter4.next();
                    if (! sf1.getData().equals(sf2.getData()))  
                        return(2);
                }
            }
            else 
            {
                return(3);
            }
        }
        // if done with one record but not the other
        if (iter1.hasNext() || iter2.hasNext())
        {
            return(-1);
        }
        return(0);
    }
}
