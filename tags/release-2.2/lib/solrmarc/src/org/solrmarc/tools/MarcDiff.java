package org.solrmarc.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Map;

import org.marc4j.ErrorHandler;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;
import org.solrmarc.marc.MarcTranslatedReader;
import org.solrmarc.marc.RawRecordReader;

public class MarcDiff
{
    static boolean verbose = false;
    
    public static void main(String[] args)
    {
        if (args[0].equals("-v")) 
        {
            verbose = true;
            String newArgs[] = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            args = newArgs;
        }
        String fileStr1 = args[0];
        File file1 = new File(fileStr1);
        String fileStr2 = args[1];
        File file2 = new File(fileStr2);
        RawRecordReader reader1 = null;
        try
        {
            reader1 = new RawRecordReader(new FileInputStream(file1));
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RawRecordReader reader2 = null;;
        try
        {
            reader2 = new RawRecordReader(new FileInputStream(file2));
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RawRecord rec1 = null;
        RawRecord rec2 = null;
        Comparator<String> comp = new StringNaturalCompare();
        while (reader1.hasNext() && reader2.hasNext())
        {
            if (rec1 == null) rec1 = reader1.next();
            if (rec2 == null) rec2 = reader2.next();
            int compVal = comp.compare(rec1.getRecordId(), rec2.getRecordId());
            if (compVal == 0)
            {
                byte rec1bytes[] = rec1.getRecordBytes();
                byte rec2bytes[] = rec2.getRecordBytes();
                if (!java.util.Arrays.equals(rec1bytes, rec2bytes))
                {
                    Record r1 = rec1.getAsRecord(true, true, "999", "MARC8");
                    Record r2 = rec2.getAsRecord(true, true, "999", "MARC8");
                    String str1 = r1.toString();
                    String str2 = r2.toString();
                    if (!verbose) System.out.println("record with id: " + rec1.getRecordId() + " different in file1 and file2");
                    if (!str1.equals(str2))
                    {
                        showDiffs(System.out, str1, str2, verbose, null);
                    }
                }
                
                rec1 = reader1.next();
                rec2 = reader2.next();
            }
            else if (compVal < 0)
            {
                System.out.println("record with id: " + rec1.getRecordId() + " found in file1 but not in file2");
                if (verbose) 
                {
                    Record rec = rec1.getAsRecord(true, true, "999", "MARC8");
                    System.out.println(rec.toString());
                }
                rec1 = reader1.next();
            }
            else if (compVal > 0)
            {
                System.out.println("record with id: " + rec2.getRecordId() + " found in file2 but not in file1");
                if (verbose) 
                {
                    Record rec = rec2.getAsRecord(true, true, "999", "MARC8");
                    System.out.println(rec.toString());
                }
                rec2 = reader2.next();
            }
        }
        while (reader1.hasNext())
        {
            System.out.println("record with id: " + rec1.getRecordId() + " found in file1 but not in file2");
            if (verbose) 
            {
                Record rec = rec1.getAsRecord(true, true, "999", "MARC8");
                System.out.println(rec.toString());
            }
            rec1 = reader1.next();
        }
        while (reader2.hasNext())
        {
            System.out.println("record with id: " + rec2.getRecordId() + " found in file2 but not in file1");
            if (verbose) 
            {
                Record rec = rec2.getAsRecord(true, true, "999", "MARC8");
                System.out.println(rec.toString());
            }
            rec2 = reader2.next();
        }
    }
    
    public static void showDiffs(PrintStream out, String strNorm, String strPerm, boolean verbose, Map<Character,String> map)
    {
        if (strNorm != null)
        {
            String normLines[] = strNorm.split("\n");
            String permLines[] = strPerm.split("\n");
            if (normLines.length == permLines.length)
            {
                for (int i = 0; i < normLines.length; i++)
                {
                    if (normLines[i].equals(permLines[i]))
                    {
                        if (verbose) out.println("   " + normLines[i]);
                    }
                    else if (map != null)
                    {
                        int index1 = 0; 
                        int index2 = 0;
                        boolean showLines = false;
                        while (index1 < normLines[i].length() && index2 < permLines[i].length())
                        {
                            while (index1 < normLines[i].length() && index2 < permLines[i].length() &&
                                   normLines[i].charAt(index1) == permLines[i].charAt(index2))
                            {
                                index1++; index2++;
                            }
                            if (index1 < normLines[i].length() && index2 < permLines[i].length())
                            {
                                if (!map.containsKey(permLines[i].charAt(index2)))
                                {
                                    Character key = permLines[i].charAt(index2);
                                    map.put(key, normLines[i] + "@@" +  permLines[i]);
                                    showLines = true; 
                                    out.println(" "+key+" : " + normLines[i]);
                                    out.println(" "+key+" : " + permLines[i]);                    

                                }
                                index2++;
                                index1++;
                                if (index1 < normLines[i].length() && index2 < permLines[i].length())
                                {
                                    while (permLines[i].substring(index2,index2+1).matches("\\p{M}") )
                                    {
                                        index2++;
                                    }                           
                                    while (normLines[i].substring(index1,index1+1).matches("\\p{M}") )
                                    {
                                        index1++;
                                    }    
                                }
                            }
                        }
//                            if (showLines)
//                            {
//                                out.println(" < " + normLines[i]);
//                                out.println(" > " + permLines[i]);                    
//                            }
                    }
                    else
                    {
                        out.println(" < " + normLines[i]);
                        out.println(" > " + permLines[i]);                    
                    }
                }
            }
        }
        else
        {
            String permLines[] = strPerm.split("\n");
            for (int i = 0; i < permLines.length; i++)
            {
                if (verbose) out.println("   " + permLines[i]);
            }
        }

    }


}
