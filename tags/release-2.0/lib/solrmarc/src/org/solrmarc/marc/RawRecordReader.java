package org.solrmarc.marc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;

/**
 * Read a binary marc file
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class RawRecordReader
{

	 // Initialize logging category
    static Logger logger = Logger.getLogger(MarcFilteredReader.class.getName());
	
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
        if (args.length < 2)
        {
            System.err.println("Error: No records specified for extraction");
        }
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
            if (!args[1].equals(".txt"))
            {
                String idRegex = args[1].trim();
                processInput(input, idRegex, null);
            }
            else 
            {
                File idList = new File(args[1]);
                BufferedReader idStream = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(idList))));
                String line;
                String findReplace[] = null;
                if (args.length > 2) findReplace = args[2].split("->");
                LinkedHashSet<String> idsLookedFor = new LinkedHashSet<String>();
                while ((line = idStream.readLine()) != null)
                {
                    if (findReplace != null)
                    {
                        line = line.replaceFirst(findReplace[0], findReplace[1]);
                    }
                    idsLookedFor.add(line);
                }
                processInput(input, null, idsLookedFor);

            }
        }
        catch (EOFException e)
        {
            //  Done Reading input,   Be happy
        }
        catch (IOException e)
        {
            //  e.printStackTrace();
            logger.error(e.getMessage());
        }

    }

    static void processInput(DataInputStream input, String idRegex, HashSet<String>idsLookedFor) throws IOException
    {
        byte[] byteArray = new byte[24];
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
                logger.error(e.getMessage());
            }
            if (recordStr.startsWith("001"))
            {
                String leader = new String(byteArray);
                int offset = Integer.parseInt(leader.substring(12,17)) - 24;
                int length = Integer.parseInt(recordStr.substring(3,7));
                int offset2 = Integer.parseInt(recordStr.substring(7,12));
                String id = recordStr.substring(offset+offset2, offset+offset2+length-1).trim();
                if ( (idsLookedFor == null && id.matches(idRegex)) ||
                     (idsLookedFor != null && idsLookedFor.contains(id) ) )
                { 
                    System.out.write(byteArray);
                    System.out.write(recordBuf);
                    System.out.flush();
                }
            }
        }
    }

}
