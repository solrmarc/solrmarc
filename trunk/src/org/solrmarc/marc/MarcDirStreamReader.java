package org.solrmarc.marc;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import org.apache.log4j.Logger;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.tools.SolrMarcException;

/**
 * Read the marc records in a directory.
 * 
 * Implements the MarcReader interface
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
    SolrMarcException exception = new SolrMarcException();
    
 // Initialize logging category
    static Logger logger = Logger.getLogger(MarcImporter.class.getName());
    
    /**
     * Constructor
     * @param dirName Path to the directory containing the marc files
     */
    public MarcDirStreamReader(String dirName)
    {
        File dir = new File(dirName);
        init(dir, false);
    }
    
    /**
     * Constructor
     * @param dir File to index
     */
    public MarcDirStreamReader(File dir)
    {
        init(dir, false);
    }

    /**
     * Read a directory of marc files to index
     * @param dirName Path to the directory to be indexed
     * @param permissive If the permissive reader should be used.
     */
    public MarcDirStreamReader(String dirName, boolean permissive)
    {
        File dir = new File(dirName);
        init(dir, permissive);
    }
    
    /**
     * Read a directory of marc files to index using permissive reader
     * @param dir File to be indexed
     * @param permissive If the permissive reader should be used.
     */
    public MarcDirStreamReader(File dir, boolean permissive)
    {
        init(dir, permissive);
    }

    /**
     * Initialize variables
     * @param dir File object to instantiate
     * @param permissive If the permissive reader should be used
     */
    private void init(File dir, boolean permissive)
    {
        // filter marc records
    	FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith("mrc");
            }
        };
        
        this.permissive = permissive; 
        list = dir.listFiles(filter);
        java.util.Arrays.sort(list);
        curFileNum = 0;
        curFileReader = null;
    }
    
    /**
     * Implemented through interface
     * @return Returns true if the iteration has more records, false otherwise
     */
    public boolean hasNext()
    {
        if (curFileReader == null || curFileReader.hasNext() == false)
        {
            nextFile();
        }
        return (curFileReader == null ? false : curFileReader.hasNext());
    }

    /**
     * Returns the next marc file in the iteration
     */
    private void nextFile()
    {
        if (curFileNum != list.length)
        {
            try
            {
                //System.err.println("Switching to input file: "+ list[curFileNum]);
            	logger.info("Switching to input file: "+ list[curFileNum]);
            	
            	//exception.printMessage("Switching to input file: "+ list[curFileNum]);
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

    /**
     * Returns the next record in the iteration
     * @return Record the record object
     */
    public Record next()
    {
        if (curFileReader == null || curFileReader.hasNext() == false)
        {
            nextFile();
        }
        return (curFileReader == null ? null : curFileReader.next());
    }
   

}
