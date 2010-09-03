package org.solrmarc.tools;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.solrmarc.marc.MarcCombiningReader;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.marcoverride.MarcSplitStreamWriter;


public class MergeSummaryHoldings implements MarcReader
{
    static boolean verbose = false;
    static boolean veryverbose = false;
    static boolean allRecords = false;
    static String summaryHoldingsMarcFileName = null;
    private RawRecordReader rawinput0 = null;
    private MarcReader cookedinput0 = null;
    private RawRecordReader rawinput1 = null;
    private RawRecord  rawinput1CurrentRecord = null;
    private String rawinput1LastRecordID = null;
    private String input1Filename;
    private boolean permissive;
    private boolean toUtf8;
    private boolean combineRecords;
    private String defaultEncoding = null;
    private String fieldsToCopy = null;
    private static Comparator<String> compare = null;

    public MergeSummaryHoldings(RawRecordReader input0, boolean permissive, boolean toUtf8, boolean combineRecords, String defaultEncoding, 
                                String input1Filename, String fieldsToCopy)
    {
        rawinput0 = input0;
        cookedinput0 = null;
        this.input1Filename = input1Filename;
        this.permissive = permissive;
        this.toUtf8 = toUtf8;
        this.combineRecords = combineRecords;
        this.defaultEncoding = defaultEncoding;
        this.fieldsToCopy = fieldsToCopy;
        ResetInput1(input1Filename);
    }
    
    public MergeSummaryHoldings(RawRecordReader input0, String input1Filename, String fieldsToCopy)
    {
        this (input0, true, false, true, "MARC8", input1Filename, fieldsToCopy);
    }
    
    public MergeSummaryHoldings(MarcReader input0, String input1Filename, String fieldsToCopy)
    {
        rawinput0 = null;
        cookedinput0 = input0;
        this.input1Filename = input1Filename;
        this.fieldsToCopy = fieldsToCopy;
        ResetInput1(input1Filename);
    }
    
    
    private void ResetInput1(String input1Filename)
    {
        try
        {
            rawinput1 = new RawRecordReader(new FileInputStream(new File(input1Filename)));
        }
        catch (FileNotFoundException e)
        {
            rawinput1 = null;           
        }
        rawinput1LastRecordID = null;
        rawinput1CurrentRecord = null;
    }

    public boolean hasNext()
    {
        if (rawinput0 != null) return(rawinput0.hasNext());
        else if (cookedinput0 != null) return(cookedinput0.hasNext());
        return(false);
    }
    
    public Record next()
    {
        Record rec = null;
        if (rawinput0 != null) 
        {
            RawRecord rawrec = rawinput0.next();
            rec = rawrec.getAsRecord(permissive, toUtf8, combineRecords, defaultEncoding);
        }
        else if (cookedinput0 != null)
        {
            rec = cookedinput0.next();
        }
        RawRecord summaryHoldings = getSummaryHoldings(rec.getControlNumber());
        rec = addSummaryHoldings(rec, summaryHoldings);
        return(rec);
    }
    
    private RawRecord getSummaryHoldings(String recID)
    {
        if (compare == null) compare = new StringNaturalCompare();
        if (rawinput1LastRecordID != null && compare.compare(rawinput1LastRecordID, recID) > 0)
        {
            ResetInput1(input1Filename);
        }
        if (rawinput1CurrentRecord == null && rawinput1 != null && rawinput1.hasNext() )
        {
            rawinput1CurrentRecord = rawinput1.next();
        }
        while (rawinput1 != null && rawinput1.hasNext() && compare.compare(rawinput1CurrentRecord.getRecordId(), recID) < 0)
        {
            rawinput1LastRecordID = rawinput1CurrentRecord.getRecordId();
            rawinput1CurrentRecord = rawinput1.next();
        }
        if (rawinput1CurrentRecord != null && compare.compare(rawinput1CurrentRecord.getRecordId(), recID) == 0)
        {
            RawRecord toReturn = rawinput1CurrentRecord; 
            rawinput1LastRecordID = rawinput1CurrentRecord.getRecordId();
            rawinput1CurrentRecord = null;
            return(toReturn);
        }
        return(null);
    }
    
    private Record addSummaryHoldings(Record rec, RawRecord summaryHoldings)
    {
        Record record1 = summaryHoldings.getAsRecord(permissive, toUtf8, false, defaultEncoding);
        List<VariableField> lvf = (List<VariableField>)rec.getVariableFields(fieldsToCopy.split("[|]"));
        for (VariableField vf : lvf)
        {
            rec.removeVariableField(vf);
        }
        rec = MarcCombiningReader.combineRecords(rec, record1, fieldsToCopy);
        return(rec);
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args)
    {
    //    try {
        RawRecordReader input0 = null;
        RawRecordReader input1 = null;
        int argoffset = 0;
        if (args.length == 0)
        {
            System.err.println("Usage: MergeSummaryHoldings [-v] [-a] -s summaryHoldingsFile.mrc  normalMarcFile.mrc");
            System.err.println("   or: cat normalMarcFile.mrc | MergeSummaryHoldings [-v] [-a] -s summaryHoldingsFile.mrc ");
        }
        while (args[argoffset].startsWith("-"))
        {
            if (args[argoffset].equals("-v"))
            {
                verbose = true;
                argoffset++;
            }
            if (args[argoffset].equals("-vv"))
            {
                verbose = true;
                veryverbose = true;
                argoffset++;
            }
            if (args[argoffset].equals("-a"))
            {
                allRecords = true;
                argoffset++;
            }
            if (args[argoffset].equals("-s"))
            {
                summaryHoldingsMarcFileName = args[1+argoffset];
                argoffset += 2;
            }
        }
        if (args.length > argoffset && (args[argoffset].endsWith(".mrc") || args[argoffset].endsWith(".xml")))
        {
            try
            {
                input0 = new RawRecordReader(new FileInputStream(new File(args[argoffset])));
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            input0 = new RawRecordReader(System.in);
        }
        
        processMergeSummaryHoldings(input0, summaryHoldingsMarcFileName);
    }
    
    
    private static void processMergeSummaryHoldings(RawRecordReader input0, String summaryHoldingsMarcFileName)
    {
        String fieldsToMerge = "852|866|867|863|853";
        MergeSummaryHoldings merger = new MergeSummaryHoldings(input0,  true, false, true, "MARC8", 
                                                               summaryHoldingsMarcFileName, fieldsToMerge);
        RawRecord rec0 = null;
        RawRecord rec1 = null;
        MarcWriter writer = new MarcSplitStreamWriter(System.out, "ISO-8859-1", 70000, "999");
        while (input0.hasNext())
        {
            rec0 = input0.next();
            rec1 = merger.getSummaryHoldings(rec0.getRecordId());
            try{
                if (rec1 != null)
                {
                    Record record0 = rec0.getAsRecord(true, false, true, "MARC8");
                    List<VariableField> lvf = (List<VariableField>)record0.getVariableFields(fieldsToMerge.split("[|]"));
                    for (VariableField vf : lvf)
                    {
                        record0.removeVariableField(vf);
                    }
                    Record record1 = rec1.getAsRecord(true, false, false, "MARC8");
                    record0 = MarcCombiningReader.combineRecords(record0, record1, fieldsToMerge);
                    writer.write(record0);
                    System.out.flush();
                }
                else if (allRecords != false)
                {
                    System.out.write(rec0.getRecordBytes());
                    System.out.flush();
                }
            }
            catch (IOException e) 
            {
                System.err.println("Error writing to std output");
            }
        }
        // TODO Auto-generated method stub
        
    }
}
