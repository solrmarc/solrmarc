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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import marcoverride.MarcDirStreamReader;
import marcoverride.MarcPermissiveStreamReader;

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class MarcPrinter
{

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "marcoverride.UVAMarcFactoryImpl");
        String mode = args[0];
        String fileStr = args[1];
        File file = new File(fileStr);
        MarcReader reader;
        boolean permissiveReader = Boolean.parseBoolean(System.getProperty("marc.permissive"));
        
        if (file.isDirectory())
        {
            reader = new MarcDirStreamReader(file, permissiveReader);            
        }
        else
        {       
            InputStream in = new FileInputStream(file);
            reader = new MarcPermissiveStreamReader(in, permissiveReader);
        }
            
        String marcIncludeIfPresent = System.getProperty("marc.include_if_present");
        String marcIncludeIfMissing = System.getProperty("marc.include_if_missing");
        boolean verbose = Boolean.parseBoolean(System.getProperty("marc.verbose"));
        if (reader != null && (marcIncludeIfPresent != null || marcIncludeIfMissing != null))
        {
            reader = new MarcFilteredReader(reader, marcIncludeIfPresent, marcIncludeIfMissing);
        }
        boolean to_utf_8 = Boolean.parseBoolean(System.getProperty("marc.to_utf_8"));
        if (reader != null && to_utf_8)
        {
            reader = new MarcTranslatedReader(reader);
        }
        
        BlacklightIndexer indexer = null;
        try
        {
            indexer = new BlacklightIndexer("blacklight.properties");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
   //     reader.parse(in);
        int recnum = 0;
  //      int matchCount = 0;
        MarcWriter writer = null;
        String fieldname = args.length > 2 ? args[2] : null;
        String fieldVals[] = null;
        Map<String, String> map = null;
//        if (fieldname != null)
//        {
//            Properties prop = new Properties();
//            InputStream in = new FileInputStream("blacklight.properties");
//            try
//            {
//                prop.load(in);
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//                System.exit(1);
//            }
//            String fieldVal = prop.getProperty(fieldname);
//            fieldVals = fieldVal.split(", ", 4);
//            map = indexer.findMap(fieldVals[3]);
//        }

        if (mode.equals("count"))
        {
            int count = 0;
            while (reader.hasNext()) 
            {
                try {
                    Record rec = reader.next();
                    count++;
                    if (count % 1000 == 0) System.err.println(count);
                    if (verbose) System.out.println(rec.toString());
                }
                catch (MarcException me)
                {
                    System.err.println("Error reading Record "+ me.getMessage());
                }
            }
            System.out.println("Total records= "+ count);
        }
        else if (mode.equals("translate"))
        {
            to_utf_8 = true;
            writer = new MarcStreamWriter(System.out, "UTF-8");
            while (reader.hasNext()) 
            {
                try {
                    Record rec = reader.next();
                    writer.write(rec);
                }
                catch (MarcException me)
                {
                    System.err.println("Error reading Record "+ me.getMessage());
                }

            }
            writer.close();
        }
        else if (mode.equals("print"))
        {
            while (reader.hasNext()) 
            {
                try {
                    Record rec = reader.next();
//                    Leader ldr = rec.getLeader();
//                    if (ldr.getBaseAddressOfData() != 0) continue;
                    String recStr = rec.toString();
                    if (verbose) System.out.println(recStr);
                }
                catch (MarcException me)
                {
                    System.err.println("Error reading Marc Record: "+ me.getMessage());                                   
                }
            }
        }

        else if (mode.equals("era"))
        {
            while (reader.hasNext()) 
            {
                Record rec = reader.next();
                if (verbose) System.out.println(rec.toString());
                if (map != null)
                {
                    String orig = SolrIndexer.getFirstFieldVal(rec, "045a");
                    Set<String> res = SolrIndexer.getEra(rec);
                    Iterator<String> iter = res.iterator();
                    if (orig != null)
                    {
                        System.out.println("\noriginal 045a field: "+orig);
                    }
                    while (iter.hasNext())
                    {
                        String val = iter.next();
                        String valMap = Utils.remap(val, map, false);
                        if (valMap != null)
                        {
                            System.out.println("value: "+ val + " maps to: "+ valMap);       
                        }
                        else
                        {
                            System.out.println("value: "+ val + " maps to: nothing");       
                        }
                    }
                }
            }
        }

        else if (mode.equals("map"))
        {
  //      Set<String> deletedRecords = getDeletedRecordIDs();
  //      writer.setConverter(new AnselToUnicode());
            Map<String, Integer> usecount = new LinkedHashMap<String, Integer>();
            Iterator<String> iterkey = map.keySet().iterator();
            while (iterkey.hasNext())
            { 
                String key = iterkey.next();
                usecount.put(key, new Integer(0));
            }
            while (reader.hasNext()) 
            {
                 if (++recnum % 10000 == 0)  System.err.println("Record "+recnum);
                 Record record = reader.next();
                 String id = ((ControlField)record.getVariableField("001")).getData();
    //             if ((((List)record.getVariableFields("048")).size() > 0))
    //             {
    //                 Leader leader = record.getLeader();
    //                 leader.setCharCodingScheme('a');
    //                 record.setLeader(leader);
              //       writer.write(record);
              //       System.out.println(record.toString());
    //             }
                 Set<String> res = SolrIndexer.getFieldList(record, fieldVals[2]);
                 Iterator<String> iter = res.iterator();
                 boolean showRec = false;
                 while (iter.hasNext())
                 {
                     String val = iter.next();
                     String valMap = Utils.remap(val, map, false);
                     if (valMap == null && !map.containsKey(val))
                     {
                         showRec = true;
                         if (usecount.containsKey(val))
                         {
                             int cnt = usecount.get(val);
                             usecount.put(val, new Integer(cnt-1));                        
                         }
                         else
                         {
                             usecount.put(val, -1);
                             System.out.println("Map missing value: "+ val);
                         }
    
                     }
                     else if (usecount.containsKey(val))
                     {
                         int cnt = usecount.get(val);
                         usecount.put(val, new Integer(cnt+1));
                     }
                 }
                 if (showRec && verbose)
                 {
                     System.out.println(record.toString());                 
    
                 }
                 
            //     String fld1 = indexer.getFirstFieldVal(record, "music_catagory_facet", "999a[0,2]:999a[0,1]");
    //             Set fld1 = Utils.remap(indexer.getFieldList(record, "999t"), indexer.findMap("recording_format_facet"), false);
    //             Set fld2 = Utils.remap(indexer.getRecordingFormat(record), indexer.findMap("recording_format_facet"), false);
    //             if (fld1 == null && fld2 == null) continue;
    //             if ((fld1 == null && fld2 != null) || !fld1.equals(fld2))
    //             {                 
    //                 System.out.println("generic = "+fld1);
    //                 System.out.println("custom = "+fld2);
    //                 System.out.println(record.toString());
    //                 indexer.getRecordingFormat(record);
    //             }
    //             if (record.getVariableField("245") != null &&
    //                     ((DataField)record.getVariableField("245")).getSubfield('h')  != null  &&
    //                     ((DataField)record.getVariableField("245")).getSubfield('h').getData().contains("video") )
    //             {                 
    //                 System.out.println(record.toString());
    //             }
    //             if (record.getVariableField("245") != null &&
    //                     ((DataField)record.getVariableField("245")).getSubfield('h')  != null  &&
    //                     ((DataField)record.getVariableField("245")).getSubfield('h').getData().contains("video") &&
    //                     ((ControlField)record.getVariableField("007")) != null &&
    //                     ((ControlField)record.getVariableField("007")).getData().startsWith("v"))
    //             {                 
    //                System.out.println("leader char 7= "+ record.getLeader().toString().substring(7,8));
    //                matchCount++;
    //             }
            }
            Iterator<String> iter1 = usecount.keySet().iterator();
            while (iter1.hasNext())
            {
                String key = iter1.next();
                Integer count = usecount.get(key);
                System.out.println((count >= 0 ? "Existing key ": "Missing key ")+ key + " occurs "+ Math.abs(count) + " times");
            }
        }
//        System.out.println("Number matches = "+matchCount);

    }
    public static Set<String> getDeletedRecordIDs()
    {
        File delFile = new File("select_ids.txt");
        Set<String> result = new LinkedHashSet<String>(); 
        try
        {
            BufferedReader is = new BufferedReader(new FileReader(delFile));
            String line;
            while ((line = is.readLine()) != null)
            {
                line = line.trim();
                if (line.startsWith("#")) continue;
                result.add(line);
                
             }            
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error: unable to find and open delete-record-id-list: "+ delFile);
        }
        catch (IOException e)
        {
            System.err.println("Error: reading from delete-record-id-list: "+ delFile);
        }
        return(result);
    }

}
