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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;

import org.solrmarc.tools.StringNaturalCompare;

/**
 * Read a binary marc file
 * @author Robert Haschart
 * @version $Id: RawRecordReader.java 700 2009-05-21 19:42:48Z rh9ec@virginia.edu $
 *
 */
public class MarcMerger
{
    public final static String minRecordID = "u0";
    public final static String maxRecordID = "u99999999999";
    public static boolean verbose = false;

    static class SimpleRecord
    {
        String id;
        byte fullrecord[];
        DataInputStream input;
        SimpleRecord(String id, byte fullrecord[])
        {
            this.id = id;
            this.fullrecord = fullrecord;
        }
        
        SimpleRecord(DataInputStream input) 
        {
            this.input = input;
            next();
        }
        
        private static int parseRecordLength(byte[] leaderData) throws IOException 
        {
            InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(
                    leaderData));
            int length = -1;
            char[] tmp = new char[5];
            isr.read(tmp);
            try {
                length = Integer.parseInt(new String(tmp));
            } 
            catch (NumberFormatException e) 
            {
                throw new IOException("unable to parse record length");
            }
            return(length);
        }
        
        public void next()
        {
            try
            {
                byte[] byteArray = new byte[24];
                input.readFully(byteArray);
                int recordLength = parseRecordLength(byteArray);
                byte[] recordBuf = new byte[recordLength];
                System.arraycopy(byteArray, 0, recordBuf, 0, 24);
                input.readFully(recordBuf, 24, recordLength - 24);
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
                int offset = Integer.parseInt(leader.substring(12,17));
                int dirOffset = 24;
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
                id = field001;
                fullrecord = recordBuf;
            }
            catch (NumberFormatException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (EOFException e)
            {
                id = maxRecordID;
                fullrecord = null;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
	 // Initialize logging category
//    static Logger logger = Logger.getLogger(MarcFilteredReader.class.getName());
	
    
	/**
	 * 
	 * @param args
	 */
    public static void main(String[] args)
    {
    //    try {
        DataInputStream input0;
        DataInputStream input1;
        DataInputStream input2;
        DataInputStream input3 = null;
        String segmentMaxRecordID = maxRecordID;
        int argoffset = 0;
        boolean mergeRecords = true;
        if (args[0].equals("-v"))
        {
            verbose = true;
            argoffset = 1;
        }
        if (args[0+argoffset].endsWith(".del"))
        {
            // merging deletes, not merging records.
            mergeRecords = false;
        }
        try
        {
            input0 = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(args[0+argoffset]))));
            String nextFile = getNextFile(args[0+argoffset]);
            if (verbose)  System.err.println("Name of \"next\" file to get MaxRecordID = " + nextFile);

            if (nextFile != null)
            {
                try {
                    input1 = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(nextFile))));
                    segmentMaxRecordID = new SimpleRecord(input1).id;
                    if (verbose)  System.err.println("value for MaxRecordID = " + segmentMaxRecordID);
                }
                catch (FileNotFoundException e)
                {
                    // no next file,  ignore it be happy
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
            }
            String modfile = args[1+argoffset];
            String delfile = null;
            if (modfile.endsWith(".mrc"))
            {
                delfile = modfile.substring(0, modfile.length()-4) + ".del";
            }
            else if (!modfile.substring(Math.max(modfile.lastIndexOf('\\'), modfile.lastIndexOf('/'))).contains("."))
            {
                delfile = modfile + ".del";
                modfile = modfile + ".mrc";
            }
            input2 = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(modfile))));
            try {
                input3 = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(delfile))));
            }
            catch (FileNotFoundException e)
            {
                // no del file,  ignore it be happy
            }
            if (mergeRecords) 
            {
                processMergeRecords(input0, segmentMaxRecordID, input2, input3, System.out);
            }
            else
            {
                processMergeDeletes(input0, input2, input3, System.out);
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
 
    private static String getNextFile(String filename)
    {
        File filepath = new File(filename);
        String filePrefix = filepath.getParent();
        String name = filepath.getName();
        String name1 = name.replaceAll("([^0-9]*)[0-9]*.*", "$1");
        String name2 = name.replaceAll("[^0-9]*([0-9]*).*", "$1");
        String name3 = name.replaceAll("[^0-9]*[0-9]*(.*)", "$1");
        int nameVal = 100000000 + Integer.parseInt(name2);
        String newName2 = String.valueOf(nameVal + 1);
        newName2 = newName2.substring(newName2.length() - name2.length());
        String newName = ((filePrefix == null) ? "" : filePrefix + File.separator) + name1 + newName2 + name3;
        return(newName);
    }

    static void processMergeRecords(DataInputStream mainFile, String maxID, DataInputStream newOrModified, DataInputStream deleted, OutputStream out) 
    {
        Comparator<String> compare = new StringNaturalCompare();
        try
        {
            SimpleRecord mainrec = new SimpleRecord(mainFile);
            String segmentMinRecordID = mainrec.id;
            SimpleRecord newOrModrec = new SimpleRecord(newOrModified);
            String deletedId = maxRecordID;
            BufferedReader delReader = null;
            if (deleted != null)
            {
                delReader = new BufferedReader(new InputStreamReader(deleted));
                deletedId = getNextDelId(delReader);
            }
            while (compare.compare(newOrModrec.id, segmentMinRecordID) < 0)
            {
                newOrModrec.next();
            }
            while (compare.compare(deletedId, segmentMinRecordID) < 0)
            {
                deletedId = getNextDelId(delReader);
            }
            
            while (compare.compare(mainrec.id, maxRecordID)< 0)
            {
                if (compare.compare(mainrec.id, newOrModrec.id)< 0  && compare.compare(mainrec.id, deletedId) < 0)
                {
                    // mainrec unchanged, just write it out.
                    //if (verbose) System.err.println("Writing original record "+ mainrec.id + " from input file");
                    out.write(mainrec.fullrecord);
                    out.flush();
                    mainrec.next();
                }
                else if (compare.compare(mainrec.id, newOrModrec.id)== 0  && compare.compare(mainrec.id, deletedId)== 0)
                {   
                    // mainrec equals deleteID  AND it equals modifiedRecId,  Delete record.  Although this should not happen.
                    if (verbose) System.err.println("Deleting record "+ deletedId);
                    deletedId = getNextDelId(delReader);
                    newOrModrec.next();
                    mainrec.next();
                }
                else if (compare.compare(mainrec.id, newOrModrec.id)< 0  && compare.compare(mainrec.id, deletedId)== 0)
                {    
                    // mainrec equals deleteID,   Delete record.  
                    if (verbose) System.err.println("Deleting record "+ deletedId);
                    deletedId = getNextDelId(delReader);
                    mainrec.next();
                }
                else if (compare.compare(mainrec.id, newOrModrec.id)== 0  && compare.compare(mainrec.id, deletedId)< 0)
                {    
                    // mainrec equals modifiedRecId,  Write out modified record.
                    if (verbose) System.err.println("Writing changed record "+ newOrModrec.id + " from Mod file");
                    out.write(newOrModrec.fullrecord);
                    out.flush();
                    newOrModrec.next();
                    mainrec.next();
                }
                else // mainrec.id is greater than either newOrModrec.id or deletedId
                {
                    if (compare.compare(mainrec.id, newOrModrec.id)> 0)
                    {    
                        // newOrModrec is a new record,  Write out new record.
                        if (verbose) System.err.println("Writing new record "+ newOrModrec.id + " from mod file");
                        out.write(newOrModrec.fullrecord);
                        out.flush();
                        newOrModrec.next();
                    }
                    if (compare.compare(mainrec.id, deletedId)> 0)
                    {    
                        // Trying to delete a record that's already not there.  Be Happy.
                        deletedId = getNextDelId(delReader);
                    }
                }
            }
            while (compare.compare(newOrModrec.id, maxRecordID)< 0 && compare.compare(newOrModrec.id, maxID)< 0)
            {
                // newOrModrec is a new record,  Write out new record.
                if (verbose) System.err.println("Writing record "+ newOrModrec.id + " from mod file");
                out.write(newOrModrec.fullrecord);
                out.flush();
                newOrModrec.next();
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    static void processMergeDeletes(DataInputStream mainFile, DataInputStream newOrModified, DataInputStream deleted, PrintStream out) 
    {
        Comparator<String> compare = new StringNaturalCompare();
        BufferedReader mainReader = new BufferedReader(new InputStreamReader(mainFile));
        String mainDelete = getNextDelId(mainReader);
        
        SimpleRecord newOrModrec = new SimpleRecord(newOrModified);
        String deletedId = maxRecordID;
        BufferedReader delReader = null;
        if (deleted != null)
        {
            delReader = new BufferedReader(new InputStreamReader(deleted));
            deletedId = getNextDelId(delReader);
        }
        while (compare.compare(mainDelete, maxRecordID)< 0)
        {
            if (compare.compare(mainDelete, newOrModrec.id)< 0  && compare.compare(mainDelete, deletedId) < 0)
            {
                // mainDeleted rec ID unchanged, just write it out to delete file.
                //if (verbose) System.err.println("Writing original record "+ mainrec.id + " from input file");
                out.println(mainDelete);
                mainDelete = getNextDelId(mainReader);
            }
            else if (compare.compare(mainDelete, newOrModrec.id)== 0  && compare.compare(mainDelete, deletedId)== 0)
            {   
                // mainrec equals deleteID  AND it equals modifiedRecId,  Delete record.  Although this should not happen.
                if (verbose) System.err.println("Deleting record "+ deletedId);
                deletedId = getNextDelId(delReader);
                newOrModrec.next();
                out.println(mainDelete);
                mainDelete = getNextDelId(mainReader);
            }
            else if (compare.compare(mainDelete, newOrModrec.id)< 0  && compare.compare(mainDelete, deletedId)== 0)
            {    
                // mainrec equals deleteID,   Delete record.  
                if (verbose) System.err.println("Deleting record "+ deletedId);
                deletedId = getNextDelId(delReader);
                out.println(mainDelete);
                mainDelete = getNextDelId(mainReader);
            }
            else if (compare.compare(mainDelete, newOrModrec.id)== 0  && compare.compare(mainDelete, deletedId)< 0)
            {    
                // mainrec equals modifiedRecId,  Write out modified record.
                if (verbose) System.err.println("Record added, removing id from  "+ newOrModrec.id + " from Mod file");
                newOrModrec.next();
                mainDelete = getNextDelId(mainReader);
            }
            else // mainrec.id is greater than either newOrModrec.id or deletedId
            {
                if (compare.compare(mainDelete, newOrModrec.id)> 0 && compare.compare(newOrModrec.id, deletedId)== 0)
                {    
                    //  Update contains a new 
                    out.println(mainDelete);
                }
                else
                {
                    if (compare.compare(mainDelete, newOrModrec.id)> 0)
                    {    
                        // newOrModrec is a new record,  Write out new record.
                        if (verbose) System.err.println("New record in mod file "+ newOrModrec.id + " skipping it.");
                        newOrModrec.next();
                    }
                    if (compare.compare(mainDelete, deletedId)> 0)
                    {    
                        // Trying to delete a record that's already not there.  Be Happy.
                        out.println(mainDelete);
                        deletedId = getNextDelId(delReader);
                    }
                }
            }
        }
        while (compare.compare(deletedId, maxRecordID)< 0 )
        {
            // deletedId is the id of a newly deleted record,  Write out that record id.
            if (verbose) System.err.println("Writing record "+ newOrModrec.id + " from mod file");
            out.println(mainDelete);
            deletedId = getNextDelId(delReader);
        }
    }


    private static String getNextDelId(BufferedReader delReader)
    {
        if (delReader == null) return(maxRecordID);
        String id = maxRecordID;
        try {
            String line = delReader.readLine();
            if (line != null) 
            {
                id = line.replaceFirst("u?([0-9]*).*", "u$1");
            }
        }
        catch (IOException e)
        {
            // end of file, be Happy.
        }
        return(id);
    }
    
}
