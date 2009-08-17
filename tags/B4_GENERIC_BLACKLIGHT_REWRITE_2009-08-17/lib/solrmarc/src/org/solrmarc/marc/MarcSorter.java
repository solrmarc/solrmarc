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
	 // Initialize logging category
//    static Logger logger = Logger.getLogger(MarcFilteredReader.class.getName());
	
    private static int parseRecordLength(byte[] leaderData) throws IOException {
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(
                leaderData));
        int length = -1;
        char[] tmp = new char[5];
        isr.read(tmp);
        try {
            length = Integer.parseInt(new String(tmp));
        } catch (NumberFormatException e) {
            throw new IOException("unable to parse record length");
        }
        return(length);
    }
    
	/**
	 * 
	 * @param args
	 */
    public static void main(String[] args)
    {
    //    try {
        DataInputStream input;
        recordMap = new TreeMap<String, byte[]>(new StringNaturalCompare());
        try
        {
            if (args[0].equals("-"))
            {
                input = new DataInputStream(new BufferedInputStream(System.in));
            }
            else
            {    
                input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(args[0]))));
            }            
            processInput(input);
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    static void processInput(DataInputStream input) 
    {
        byte[] byteArray = new byte[24];
        try {
            while (true)
            {
                input.readFully(byteArray);
                int recordLength = parseRecordLength(byteArray);
                byte[] recordBuf = new byte[recordLength - 24];
                input.readFully(recordBuf);
                String recordStr = null;
                try
                {
                    recordStr = new String(recordBuf, "ISO-8859-1");
                }
                catch (UnsupportedEncodingException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                String leader = new String(byteArray);
                int offset = Integer.parseInt(leader.substring(12,17)) - 24;
                int dirOffset = 0;
                String fieldNum;
                String field001 = "Undefined";
                for (fieldNum = recordStr.substring(dirOffset, dirOffset+3); dirOffset < offset;  
                      dirOffset += 12, fieldNum = recordStr.substring(dirOffset, dirOffset+3))
                {
                    if ( fieldNum.equals("001"))
                    {
                        int length = Integer.parseInt(recordStr.substring(dirOffset + 3, dirOffset + 7));
                        int offset2 = Integer.parseInt(recordStr.substring(dirOffset + 7, dirOffset + 12));
                        field001 = recordStr.substring(offset+offset2, offset+offset2+length-1).trim();
                        break;
                    }
                }
                byte[] fullBuf = new byte[recordLength];
                System.arraycopy(byteArray, 0, fullBuf, 0, byteArray.length);
                System.arraycopy(recordBuf, 0, fullBuf, byteArray.length, recordBuf.length);
                if (recordMap.containsKey(field001))
                {
                    byte existingRec[] = recordMap.get(field001);
                    byte newRec[] = new byte[existingRec.length + fullBuf.length];
                    System.arraycopy(existingRec, 0, newRec, 0, existingRec.length);
                    System.arraycopy(fullBuf, 0, newRec, existingRec.length, fullBuf.length);
                    recordMap.put(field001, newRec);
                }
                else
                {
                    recordMap.put(field001, fullBuf);
                }
            }
        }
        catch (EOFException e)
        {
            //  Done Reading input,   Be happy
        }
        catch (IOException e)
        {
            //  e.printStackTrace();
            System.err.println(e.getMessage());
        }

        try {
            while (recordMap.size() > 0)
            {
                String firstKey = recordMap.firstKey();
                byte recValue[] = recordMap.remove(firstKey);
                System.out.write(recValue);
                System.out.flush();
            }
        }
        catch (IOException e)
        {
            //  e.printStackTrace();
            System.err.println(e.getMessage());
        }
       
    }
    
}
