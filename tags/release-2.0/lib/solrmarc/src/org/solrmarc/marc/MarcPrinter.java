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


import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import org.marc4j.MarcException;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

import org.solrmarc.marc.MarcFilteredReader;
import org.solrmarc.tools.Utils;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class MarcPrinter extends MarcHandler
{

     // Initialize logging category
    static Logger logger = Logger.getLogger(MarcPrinter.class.getName());
    private String mode;
    private String indexkeyprefix = "";
    private MarcWriter writer = null;
    
    public MarcPrinter(String args[])
    {
        super(args);
        loadLocalProperties(configProps);
        processAdditionalArgs(addnlArgs);
    }
    
    private void processAdditionalArgs(String[] args) 
    {
        for (String arg : args)
        {
            if (arg.equals("print") || arg.equals("index") || arg.equals("to_xml") || arg.equals("translate"))
            {
                mode = arg;
            }
            else if (mode.equals("index"))
            {
                indexkeyprefix = arg;
            }
        }
    }

    private void loadLocalProperties(Properties props) 
    {
        String marcIncludeIfPresent2 = Utils.getProperty(props, "marc.include_if_present2");
        String marcIncludeIfMissing2 = Utils.getProperty(props, "marc.include_if_missing2");

        if (reader != null && (marcIncludeIfPresent2 != null || marcIncludeIfMissing2 != null)) 
        {
            reader = new MarcFilteredReader(reader, marcIncludeIfPresent2, marcIncludeIfMissing2, null);
        }
    }

    @Override
    protected int handleAll() 
    {
           // keep track of record count
        int recordCounter = 0;
        
        while(reader != null && reader.hasNext())
        {
            recordCounter++;
 
            try {
                Record record = reader.next();
                
                if (mode.equals("print"))
                {
                    String recStr = record.toString();
                    
                    System.out.println(recStr);
                }
                else if (mode.equals("to_xml"))
                {
                    if (writer == null)
                    {
                        writer = new MarcXmlWriter(System.out, "UTF-8", true);
                    }
                    writer.write(record);
                }
                else if (mode.equals("translate"))
                {
                    if (writer == null)
                    {
                        writer = new MarcStreamWriter(System.out, "UTF-8");
                    }
                    writer.write(record);
                }
                else if (mode.equals("index"))
                {
                    String recStr = record.toString();
                        
                    if (verbose) System.out.println(recStr);
                    Map<String,Object> indexMap = indexer.map(record);
                    if (errors != null && includeErrors)
                    {
                        if (errors.hasErrors())
                        {
                            indexMap.put("marc_error", errors.getErrors());
                        }
                    }
                    TreeSet<String> sortedKeys = new TreeSet<String>();
                    sortedKeys.addAll(indexMap.keySet());
                    Iterator<String> keys = sortedKeys.iterator();
                    String key = "id";
                    Object recordID = indexMap.get(key);
                    //System.out.println("\nIndexID= "+ key + "  Value = "+ value);
                    while (keys.hasNext())
                    {
                        key = keys.next();
                        Object value = indexMap.get(key);
                        if (key.equals("id")) continue;
                        if (key.startsWith(indexkeyprefix))
                        {
                            if (value instanceof String)
                            {
                                System.out.println(recordID+ " : "+ key + " = "+ value);
                            }
                            else if (value instanceof Collection)
                            {
                                Iterator<?> valIter = ((Collection)value).iterator();
                                while (valIter.hasNext())
                                {
                                    String collVal = valIter.next().toString();
                                    System.out.println(recordID+ " : "+ key + " = "+ collVal);
                                }
                            }
                        }
                    }
                }
            }
            catch (MarcException me)
            {
                System.err.println("Error reading Marc Record: "+ me.getMessage());                                   
                logger.error("Error reading Marc Record: "+ me.getMessage());
                return(1);
            }        
        }
        if (writer != null) { writer.close(); }
        return 0;
    }

    
    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException
    {
        MarcPrinter marcPrinter = null;
        try {
            marcPrinter = new MarcPrinter(args);
        }
        catch (IllegalArgumentException e)
        {
            logger.error(e.getMessage());
            System.err.println(e.getMessage());
            //e.printStackTrace();
            System.exit(1);
        }
        
        int exitCode = marcPrinter.handleAll();
        System.exit(exitCode);
    }


}
