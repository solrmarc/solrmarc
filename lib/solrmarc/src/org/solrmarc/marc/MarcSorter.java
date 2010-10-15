package org.solrmarc.marc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;

import org.solrmarc.tools.RawRecord;
import org.solrmarc.tools.StringNaturalCompare;

/**
 * Read a binary marc file
 * @author Robert Haschart
 * @version $Id: RawRecordReader.java 700 2009-05-21 19:42:48Z rh9ec@virginia.edu $
 *
 */
public class MarcSorter
{
    static TreeMap<String, byte[]> recordMap = null;
    static boolean verbose = false;
	 // Initialize logging category
	/**
	 * 
	 * @param args
	 */
    public static void main(String[] args)
    {
    //    try {
        InputStream input;
        recordMap = new TreeMap<String, byte[]>(new StringNaturalCompare());
        int offset = 0;
        if (args[0].equals("-v")) { verbose = true; offset = 1; }
        try
        {
            if (args[offset].equals("-"))
            {
                input = System.in;
                if (verbose)  System.err.println("reading Stdin");
            }
            else
            {    
                input = new FileInputStream(new File(args[offset]));
                if (verbose)  System.err.println("reading file "+ args[offset]);
            }            
            processInput(input);
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e)
        {
            System.err.println("Exception: "+e.getMessage());
            e.printStackTrace();
        }

    }

    static void processInput(InputStream input) 
    {
        RawRecordReader rawReader = new RawRecordReader(input);
        RawRecord rec = rawReader.hasNext() ? rawReader.next() : null;
        while (rec != null)
        {
            String field001 = "Undefined";
            field001 = rec.getRecordId();
            byte newRec[] = rec.getRecordBytes();
            if (recordMap.containsKey(field001))
            {
                byte existingRec[] = recordMap.get(field001);
                byte combinedRec[] = new byte[existingRec.length + newRec.length];
                System.arraycopy(existingRec, 0, combinedRec, 0, existingRec.length);
                System.arraycopy(newRec, 0, combinedRec, existingRec.length, newRec.length);
                recordMap.put(field001, combinedRec);
            }
            else
            {
                recordMap.put(field001, newRec);
            }
            if (verbose) System.err.println("Record read : "+ field001);
            rec = rawReader.hasNext() ? rawReader.next() : null;
        }

        try {
            while (recordMap.size() > 0)
            {
                String firstKey = recordMap.firstKey();
                byte recValue[] = recordMap.remove(firstKey);
                System.out.write(recValue);
                System.out.flush();
                if (verbose) System.err.println("Record written : "+ firstKey);
            }
        }
        catch (IOException e)
        {
            //  e.printStackTrace();
            System.err.println(e.getMessage());
        }
       
    }
    
}
