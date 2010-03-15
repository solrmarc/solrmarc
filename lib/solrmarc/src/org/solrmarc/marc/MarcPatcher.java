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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import org.marc4j.MarcException;
import org.marc4j.MarcPermissiveStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.SubfieldImpl;

import org.solrmarc.tools.StringNaturalCompare;
import org.solrmarc.tools.Utils;

/**
 * A Utility class that writes to the PrintWriter passed in.
 *   print - prints out the record (?)
 *   index - given a solr field name, will output a line containing
 *     the solr record id, solr field name and solr field value
 *       for each value of that solr field for each marc record in the file.
 *   to_xml - prints out the record as xml
 *   translate - ??
 *   
 * @author Robert Haschart
 * @version $Id: MarcPrinter.java 1112 2010-03-09 16:33:32Z rh9ec@virginia.edu $
 *
 */
public class MarcPatcher extends MarcHandler
{

     // Initialize logging category
    static Logger logger = Logger.getLogger(MarcPatcher.class.getName());
    private String changedRecordFileName = null;
    private String locationFileName = null;
    private MarcWriter writerAll = null;
    private MarcWriter writerChanged = null;
    private PrintStream out;
    private String locationRecordIDMapper = null;
    private String mapPattern = null;
    private String mapReplace = null;
    private String locationFileLine[] = null;
//    private String libraryLocationMap = null;
//    private Properties libraries = null;
    private StringNaturalCompare compare = null;

    public MarcPatcher(String locationFile, String changedFile, PrintStream out)
    {
        super();
        this.out = out;
        locationFileName = locationFile;
        changedRecordFileName = changedFile;
//        this.libraryLocationMap = libraryLocationMap;
    }
    
    @Override
    protected void initLocal()
    {
        configToUse = null;
        configProps = new Properties();
        configProps.setProperty("marc.to_utf_8", "false");
        configProps.setProperty("marc.permissive", "true");
        permissiveReader = true;
        String fName = Utils.getProperty(configProps, "marc.path");
        String source = Utils.getProperty(configProps, "marc.source", "STDIN").trim();
        loadReader(source, fName);
        mapPattern = "u?([0-9]*).*";
        mapReplace = "u$1";
//        libraries = new Properties();
//        try
//        {
//            libraries.load(new FileInputStream(libraryLocationMap));
//        }
//        catch (FileNotFoundException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        compare = new StringNaturalCompare();
    }
    
    @Override
    protected void processAdditionalArgs() 
    {
    }

    @Override
    protected void loadLocalProperties() 
    {
        // Specification of how to modify the entries in the delete record file
        // before passing the id onto Solr.   Based on syntax of String.replaceAll
        //  To prepend a 'u' specify the following:  "(.*)->u$1"
        locationRecordIDMapper = Utils.getProperty(configProps, "marc.delete_record_id_mapper");
        if (locationRecordIDMapper != null)
        {
            String parts[] = locationRecordIDMapper.split("->");
            if (parts.length == 2)
            {
                String mapPattern = parts[0];
                String mapReplace = parts[1];
                try {
                    String testID = "12345";
                    String tested = testID.replaceFirst(mapPattern, mapReplace);
                    logger.info("Valid Regex pattern specified in property: marc.delete_record_id_mapper");
                }
                catch (PatternSyntaxException pse)                
                {
                    locationRecordIDMapper = null;
                    logger.warn("Invalid Regex pattern specified in property: marc.delete_record_id_mapper");
                }
            }
            else
            {
                locationRecordIDMapper = null;
                logger.warn("Invalid Regex pattern specified in property: marc.delete_record_id_mapper");
            }
        }
        if (locationRecordIDMapper != null)
        {
            String parts[] = locationRecordIDMapper.split("->");
            if (parts.length == 2)
            {
                mapPattern = parts[0];
                mapReplace = parts[1];
            }
        }
    }

    @Override
    public int handleAll() 
    {
        // keep track of record count
        int recordCounter = 0;
        BufferedReader locationReader = null;
        try
        {
            locationReader = new BufferedReader(new InputStreamReader( new FileInputStream(new File(locationFileName))));
        }
        catch (FileNotFoundException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (writerAll == null && out != null && changedRecordFileName != null)
        {
            writerAll = new MarcPermissiveStreamWriter(out, "UTF-8");
        }
        if (writerChanged == null && changedRecordFileName != null)
        {
            FileOutputStream changedRecordStream;
            try
            {
                File changedRecordFile = new File(changedRecordFileName);
                changedRecordStream = new FileOutputStream(changedRecordFile);
                writerChanged = new MarcPermissiveStreamWriter(changedRecordStream, "UTF-8");
            }
            catch (FileNotFoundException e)
            {
                writerChanged = null;
                changedRecordFileName = null;
                e.printStackTrace();
            }
        }
        else if (writerChanged == null && changedRecordFileName == null && out != null)
        {
            writerChanged = new MarcPermissiveStreamWriter(out, "UTF-8");
        }

        while(reader != null && reader.hasNext())
        {
            recordCounter++;
 
            try {
                Record record = reader.next();
                boolean patched = patchRecord(record, locationReader);
                
                if (writerAll != null)  writerAll.write(record);
                if (patched && writerChanged != null) 
                {
                    writerChanged.write(record);
                }
                if (out != null) out.flush();
            }
            catch (MarcException me)
            {
                System.err.println("Error reading Marc Record: "+ me.getMessage());                                   
                logger.error("Error reading Marc Record: "+ me.getMessage());
                return(1);
            }        
        }
        if (writerAll != null) { writerAll.close(); }
        if (writerChanged != null) { writerChanged.close(); }
        return 0;
    }

    
    private boolean patchRecord(Record record, BufferedReader locationReader)
    {
        boolean patched = false;
        String recId = record.getControlNumber();
        if (locationFileLine == null) locationFileLine = getNextLocationLine(locationReader);
        while (locationFileLine != null && compare.compare(locationFileLine[0], recId) < 0)
        {
            locationFileLine = getNextLocationLine(locationReader);
        }
        while (locationFileLine != null && compare.compare(locationFileLine[0], recId) == 0)
        {
            patched |= patchRecordWithLine(record, locationFileLine);
            locationFileLine = getNextLocationLine(locationReader);
        }
        return(patched);
    }

    private String[] getNextLocationLine(BufferedReader locationReader)
    {
        String line = null;
        try
        {
            line = locationReader.readLine();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (line == null) return(null);
        String result[] = line.split("\\|");
        result[0] =  result[0].replaceFirst(mapPattern, mapReplace);
        result[1] = result[1].trim();
        return result;
    }

    private boolean patchRecordWithLine(Record record, String locationFileLine2[])
    {
        boolean changed = false;
        List<VariableField> fields999 = (List<VariableField>)record.getVariableFields("999");
        for (VariableField f999 : fields999)
        {
            DataField df999 = (DataField)f999;
            Subfield barcode = df999.getSubfield('i');
            if (barcode != null && barcode.getData().equals(locationFileLine2[1]))
            {
                Subfield curLoc = df999.getSubfield('k');
                Subfield homeLoc = df999.getSubfield('l');
                if (curLoc == null)
                {
                    if (!locationFileLine2[2].equals(locationFileLine2[3]))
                    {
                        List<Subfield> subfields = (List<Subfield>)df999.getSubfields(); 
                        int index = 0;
                        for (Subfield sf : subfields)
                        {
                            if (sf.getCode()== 'l') break;
                            index++;
                        }
                        df999.addSubfield(index, new SubfieldImpl('k', locationFileLine2[2]));
                        changed = true;
                    }
                }
                else 
                {
                    if (!locationFileLine2[2].equals(curLoc.getData()))
                    {
                        if (locationFileLine2[2].equals(locationFileLine2[3]))
                        {
                            df999.removeSubfield(curLoc);
                        }
                        else
                        {
                            curLoc.setData(locationFileLine2[2]);
                        }
                        changed = true;
                    }
                }
                if (homeLoc != null && !locationFileLine2[3].equals(homeLoc.getData()))
                {
                    Subfield libraryName = df999.getSubfield('m');
                    String newLibraryName = locationFileLine2[4];                    
                    homeLoc.setData(locationFileLine2[3]);
                    if (newLibraryName != null && !newLibraryName.equals(libraryName.getData()))
                    {
                        libraryName.setData(newLibraryName);
                    }
                    changed = true;
                }
            }
        }
        return(changed);
    }

//    private String getLibraryName(String location)
//    {
//        String result = null;
//        if (libraries != null)
//        {
//            result = libraries.getProperty(location);
//            if (!result.equals(result.toUpperCase()))
//            {
//                result = null;
//            }
//        }
//        return(result);
//    }

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException
    {
        MarcPatcher marcPatcher = null;
        PrintStream pOut = null;
        String tmpArgs[] = new String[1];
        tmpArgs[0] = args[0];
        String locationFile = null;
        String changedFile = null;
        boolean changesOnly = false;
        String outputFile = null;
        for (int i = 1; i < args.length; i++)
        {
            if (args[i].endsWith(".txt")) locationFile = args[i];
            else if (args[i].equals("changesOnly")) changesOnly = true;
            else if (args[i].endsWith(".mrc") && changedFile == null) changedFile = args[i];
            else if (args[i].endsWith(".mrc") && changedFile != null) outputFile = args[i];
        }
        
        try {
            if (changesOnly)
            {
                if (changedFile != null)
                    pOut = new PrintStream(new FileOutputStream(new File(changedFile)));

                else
                    pOut = System.out;
            }
            else if (outputFile != null && outputFile.startsWith("none"))
                pOut = null;
            else if (outputFile != null)
                pOut = new PrintStream(new FileOutputStream(new File(outputFile)));
            else
                pOut = System.out;
            marcPatcher = new MarcPatcher(locationFile, changedFile, pOut);
            marcPatcher.init(tmpArgs);
        }
        catch (IllegalArgumentException e)
        {
            logger.error(e.getMessage());
            System.err.println(e.getMessage());
            //e.printStackTrace();
            System.exit(1);
        }
        
        int exitCode = marcPatcher.handleAll();
        if (pOut != null) pOut.flush();
        System.exit(exitCode);
    }


}
