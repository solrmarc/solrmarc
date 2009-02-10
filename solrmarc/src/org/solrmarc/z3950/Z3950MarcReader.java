package org.solrmarc.z3950;
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class Z3950MarcReader implements MarcReader
{
    private ZClient newclient;
    private BufferedReader is = null;
    private String curLine = null;
    private boolean opened = false;
    
    public Z3950MarcReader(String hostPort, String recIdsFilename)
    {
        newclient = new ZClient();
        Package ir_package = Package.getPackage("com.k_int.IR");
        Package a2j_runtime_package = Package.getPackage("com.k_int.codec.runtime");         
       
//        System.out.println("JZKit command line z39.50 client $Revision: 1.53 $");    
       
        String parms[] = hostPort.split(":");
        opened = newclient.openConnection(parms[0], parms[1]);
        if (parms.length >= 3)
        {
            newclient.cmdBase(parms[3]);
        }
        newclient.cmdElements("F");
        newclient.cmdFormat("usmarc");

        File recFile = new File(recIdsFilename);
        try
        {
            is = new BufferedReader(new FileReader(recFile));
            curLine = readRecId();              
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error: unable to find and open record-id-list: "+ recFile);
        }
        catch (IOException e)
        {
            System.err.println("Error: reading from record-id-list: "+ recFile);
        }
    }

    private String readRecId()
    {
        String line;
        do {
            try
            {
                line = is.readLine();
            }
            catch (IOException e)
            {
                line = null;
            }
            if (line == null) return(line);
            line = line.trim();
        } while (line.length() == 0 || line.startsWith("#") || !line.matches("[Uu]?[0-9]+"));
        return(line);        
    }

    public boolean hasNext()
    {
        return (opened && curLine != null);
    }

    public Record next()
    {
        int recNo;
        if (curLine.toLowerCase().startsWith("u"))
        {
            recNo = Integer.parseInt(curLine.substring(1));
        }
        else
        {
            recNo = Integer.parseInt(curLine);
        }
        Record rec = newclient.getRecordByIDNum(recNo);
        
        curLine = readRecId();
        if (curLine == null)
        {
            newclient.disconnect();
        }
        return(rec);
    }
    
    public static void main(String args[])
    {
        MarcReader reader = new Z3950MarcReader("virgo.lib.virginia.edu:2200", "delete_ids.txt");
        while (reader.hasNext())
        {
            Record rec = reader.next();
            System.out.println(rec.toString());
        }
        System.exit(0);
    }      
    
    public boolean hasErrors()
    {
        return(false);
    }

    public List<Object> getErrors()
    {
        return(null);
    }

}
