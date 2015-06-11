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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
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
    private Vector<String> recids = null;
    private int curRecNum = 0;
    private Iterator<Record> curRecIter = null;
    private Record curRec = null;
    
    public Z3950MarcReader(ZClient client, String[] args)
    {
        newclient = client;
        recids = new Vector<String>();
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].matches("u?[0-9]+") || args[i].matches("[0-9]*:.*"))
            {
                recids.add(args[i]);
            }
            else
            {
                String filename = null;
                try
                {
                    if (args[i].equals("-"))
                    {
                        is = new BufferedReader(new InputStreamReader(System.in));
                        filename = "Stdin";
                    }
                    else 
                    {
                        File recFile = new File(args[i]);
                        is = new BufferedReader(new FileReader(recFile));
                        filename = args[i];
                    }
                    curLine = readRecId(); 
                    while (curLine != null)
                    {
                        recids.add(curLine);
                        curLine = readRecId(); 
                    }
                }
                catch (FileNotFoundException e)
                {
                    System.err.println("Error: unable to find and open record-id-list: "+ filename);
                }
//                catch (IOException e)
//                {
//                    System.err.println("Error: reading from record-id-list: "+ filename);
//                }
            }
        }
    }

    private String getIdFromIdString(String idstr)
    {
        String idnum = null;
        if (idstr.matches("u[0-9]+"))
        {
            idnum = idstr.substring(1);
        }
        else if (idstr.matches("[0-9]+"))
        {
            idnum = idstr;
        }
        return(idnum);
    }

    public void close()
    {
        newclient.disconnect();
        newclient = null;
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
        if (curRec == null) 
        {
            curRec = next();
        }
        return(curRec != null);
    }

    private Iterator<Record> nextIter()
    {
        Iterator<Record> recIter = null;
        String nextRecStr = (curRecNum < recids.size()) ? recids.elementAt(curRecNum++) : null;
        if (nextRecStr == null) return(recIter);
        String recNo = getIdFromIdString(nextRecStr);
        if (recNo != null)
            recIter = newclient.getRecordByIDStr(recNo);
        else
        {
            String[] parts = nextRecStr.split(":", 2);
            if (parts.length != 2) 
                return(recIter);
            recIter = newclient.getRecordBySearchStr(parts[0], parts[1]);
         }
         return(recIter);
    }

    public Record next()
    {
        if (curRec != null)
        {
            Record tmprec = curRec;
            curRec = null;
            return(tmprec);
        }
        while (true)
        {
            if (curRecIter != null && curRecIter.hasNext()) 
                return(curRecIter.next());
            curRecIter = nextIter();
            if (curRecIter == null) break;
        }
        return(null);
    }
    
    public static ZClient makeZClient(String hostPortDbUserPass)
    {
        ZClient newclient = new ZClient();
        Package ir_package = Package.getPackage("com.k_int.IR");
        Package a2j_runtime_package = Package.getPackage("com.k_int.codec.runtime");         
       
//        System.out.println("JZKit command line z39.50 client $Revision: 1.53 $");    
       
        String parms[] = hostPortDbUserPass.split(":");
        String host = parms[0];
        String port = parms[1];
        String database = parms.length > 2 ? parms[2] : null;
        String userId = parms.length > 3 ? parms[3] : null;
        String pass = parms.length > 4 ? parms[4] : null;
        boolean opened = newclient.openConnection(host, port, userId, pass);
        if (database != null)
        {
            newclient.cmdBase(database);
        }
        newclient.cmdElements("F");
        newclient.cmdFormat("usmarc");
        if (opened) 
            return(newclient);
        else 
            return(null);
    }
    
    public static void main(String args[])
    {
        String hostPortDbUserPass = "virgo.lib.virginia.edu:2200";
        if (args.length > 0 && args[0].matches("[A-Za-z0-9][A-Za-z0-9.]+[A-Za-z0-9]+:[0-9]+.*"))
        {
            hostPortDbUserPass = args[0];
            String[] tmpArgs = args;
            args = new String[tmpArgs.length - 1];
            System.arraycopy(tmpArgs, 1, args, 0, tmpArgs.length-1);
        }
        ZClient zclient =  makeZClient(hostPortDbUserPass);
        MarcReader reader = new Z3950MarcReader(zclient, args);
        OutputStream marcOutput = null;
        PrintStream output = null;
//        if (args.length >= 2)
//        {
//            try
//            {
//                marcOutput = new FileOutputStream(args[1]);
//                output = System.out;
//            }
//            catch (FileNotFoundException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        else
        {
            marcOutput = System.out;
            output = null;
        }
        MarcStreamWriter writer = new MarcStreamWriter(marcOutput);
        while (reader.hasNext())
        {
            Record rec = reader.next();
            if (rec == null)
                continue; 
            writer.write(rec);
            try
            {
                marcOutput.flush();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
 //           if (output != null) output.write(rec.toString());
        }
        ((Z3950MarcReader)reader).close();
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
