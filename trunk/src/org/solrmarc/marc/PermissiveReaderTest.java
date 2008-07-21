package org.solrmarc.marc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.marc4j.Errors;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;

public class PermissiveReaderTest
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "marcoverride.UVAMarcFactoryImpl");
        boolean verbose = Boolean.parseBoolean(System.getProperty("marc.verbose"));
        if (args[0].equals("-v")) 
        {
            verbose = true;
            String newArgs[] = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            args = newArgs;
        }
        String fileStr = args[0];
        File file = new File(fileStr);
        MarcReader readerNormal = null;
        MarcReader readerPermissive = null;
        boolean to_utf_8 = true;
       
        InputStream inNorm;
        InputStream inPerm;
        OutputStream patchedRecStream = null;
        MarcWriter patchedRecs = null;
        try
        {
            inNorm = new FileInputStream(file);
            readerNormal = new MarcPermissiveStreamReader(inNorm, false, to_utf_8);
            inPerm = new FileInputStream(file);
            readerPermissive = new MarcPermissiveStreamReader(inPerm, true, to_utf_8);
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        boolean done = false;
        if (args.length > 1)
        {
            try
            {
                patchedRecStream = new FileOutputStream(new File(args[1]));
                patchedRecs = new MarcStreamWriter(patchedRecStream);
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        while (readerNormal.hasNext() && readerPermissive.hasNext())
        {
            Record recNorm = readerNormal.next();
            Record recPerm = readerPermissive.next();
            String strNorm = recNorm.toString();
            String strPerm = recPerm.toString();
            if (!strNorm.equals(strPerm))
            {
                if (verbose)
                {
                    dumpErrors(readerPermissive);
                    showDiffs(strNorm, strPerm);
                    System.out.println("-------------------------------------------------------------------------------------");
                    
                }
                if (patchedRecs != null)
                {
                    patchedRecs.write(recPerm);
                }
            }
            else if (readerPermissive.hasErrors())
            {
                if (verbose)
                {
                    System.out.println("Results identical, but errors reported");
                    dumpErrors(readerPermissive);
                    showDiffs(strNorm, strPerm);
                    System.out.println("-------------------------------------------------------------------------------------");
                }
                if (patchedRecs != null)
                {
                    patchedRecs.write(recPerm);
                }
            }
        }
    }

    public static void showDiffs(String strNorm, String strPerm)
    {
        String normLines[] = strNorm.split("\n");
        String permLines[] = strPerm.split("\n");
        if (normLines.length == permLines.length)
        {
            for (int i = 0; i < normLines.length; i++)
            {
                if (normLines[i].equals(permLines[i]))
                {
                    System.out.println("   " + normLines[i]);
                }
                else
                {
                    System.out.println(" < " + normLines[i]);
                    System.out.println(" > " + permLines[i]);                    
                }
            }
        }

    }
    
    public static void dumpErrors(MarcReader readerPermissive)
    {
        List<Object> errors = readerPermissive.getErrors();
        if (errors != null) 
        {
            Iterator<Object> iter = errors.iterator();
            while (iter.hasNext())
            {
                Object error = iter.next();
                if (((Errors.Error)(error)).getSeverity() >= Errors.MINOR_ERROR)
                {
                    int i = 10;
                }
                System.out.println(error.toString());
            }
        }
    }
}
