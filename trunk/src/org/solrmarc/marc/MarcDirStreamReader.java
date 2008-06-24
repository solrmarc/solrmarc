package org.solrmarc.marc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

/**
 * 
 * @author Robert Haschart
 * @version $Id: MarcDirStreamReader.java 19 2008-06-20 14:58:26Z wayne.graham $
 *
 */
public class MarcDirStreamReader implements MarcReader
{
    File list[];
    MarcReader curFileReader;
    int curFileNum;
    boolean permissive;
    
    public MarcDirStreamReader(String dirName)
    {
        File dir = new File(dirName);
        init(dir, false);
    }
    
    public MarcDirStreamReader(File dir)
    {
        init(dir, false);
    }

    public MarcDirStreamReader(String dirName, boolean permissive)
    {
        File dir = new File(dirName);
        init(dir, permissive);
    }
    
    public MarcDirStreamReader(File dir, boolean permissive)
    {
        init(dir, permissive);
    }

    private void init(File dir, boolean permissive)
    {
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return(name.endsWith("mrc"));
            }
        };
        this.permissive = permissive; 
        list = dir.listFiles(filter);
        java.util.Arrays.sort(list);
        curFileNum = 0;
        curFileReader = null;
    }
    
    public boolean hasNext()
    {
        if (curFileReader == null || curFileReader.hasNext() == false)
        {
            nextFile();
        }
        return (curFileReader == null ? false : curFileReader.hasNext());
    }

    private void nextFile()
    {
        if (curFileNum != list.length)
        {
            try
            {
                System.err.println("Switching to input file: "+ list[curFileNum]);
                curFileReader = new MarcPermissiveStreamReader(new FileInputStream(list[curFileNum++]), permissive);
            }
            catch (FileNotFoundException e)
            {
                nextFile();
            }
        }
        else 
        {
            curFileReader = null;
        }
    }

    public Record next()
    {
        if (curFileReader == null || curFileReader.hasNext() == false)
        {
            nextFile();
        }
        return (curFileReader == null ? null : curFileReader.next());
    }
    
    

}
