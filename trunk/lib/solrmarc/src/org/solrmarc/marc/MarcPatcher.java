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


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import org.marc4j.MarcException;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;
import org.marc4j.marc.impl.VariableFieldImpl;

import org.solrmarc.marcoverride.MarcSplitStreamWriter;
import org.solrmarc.tools.RawRecord;
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
    private String changedLocationFileName = null;
    private String locationFileName = null;
    private MarcWriter writerAll = null;
    private MarcWriter writerChanged = null;
    private RawRecordReader rawReader = null;
    private PrintStream out;
    private String locationRecordIDMapper = null;
    private String mapPattern = null;
    private String mapReplace = null;
    private String locationFileLine[] = null;
    private String currentLocationID = null;
//    private String libraryLocationMap = null;
//    private Properties libraries = null;
    private StringNaturalCompare compare = null;
    private boolean handleAllLocs = false;

//    public MarcPatcher(String locationFile, String changedOutputFile, PrintStream out)
//    {
//        super();
//        this.out = out;
//        locationFileName = locationFile;
//        changedRecordFileName = changedOutputFile;
//    }
   
    public MarcPatcher(String locationFile, String changedLocationFile, String changedOutputFile, PrintStream out, boolean handleAllLocs)
    {
        super();
        this.out = out;
        locationFileName = locationFile;
        changedRecordFileName = changedOutputFile;
        changedLocationFileName = changedLocationFile;
        this.handleAllLocs = handleAllLocs;
    }
    
    @Override
    public void loadReader(String source, String fName)
    {       
        if (source.equals("FILE") || source.equals("STDIN"))
        {
            InputStream is = null;
            if (source.equals("FILE")) 
            {
                try {
                    if (showInputFile)
                        logger.info("Attempting to open data file: "+ new File(fName).getAbsolutePath());
                    else 
                        logger.debug("Attempting to open data file: "+ new File(fName).getAbsolutePath());
                    is = new FileInputStream(fName);
                } 
                catch (FileNotFoundException e) 
                {
                    logger.error("Fatal error: Unable to open specified MARC data file: " + fName);
                    throw new IllegalArgumentException("Fatal error: Unable to open specified MARC data file: " + fName);
                }
            }
            else
            {
                if (showInputFile)
                    logger.info("Attempting to read data from stdin ");
                else
                    logger.debug("Attempting to read data from stdin ");
                is = System.in;
            }
            rawReader = new RawRecordReader(is);
            reader = null;
        }
    }
    
    @Override
    protected void initLocal()
    {
        configToUse = null;
        configProps = new Properties();
        configProps.setProperty("marc.to_utf_8", "false");
        configProps.setProperty("marc.default_encoding", "ISO-8859-1");
        configProps.setProperty("marc.permissive", "true");
        permissiveReader = true;
        defaultEncoding = "ISO8859_1";
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
        BufferedReader changedLocationReader = null;
        if (changedLocationFileName != null)
        {
            try
            {
                changedLocationReader = new BufferedReader(new InputStreamReader( new FileInputStream(new File(changedLocationFileName))));
            }
            catch (FileNotFoundException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        if (writerAll == null && out != null && changedRecordFileName != null)
        {
            writerAll = new MarcSplitStreamWriter(out, "ISO-8859-1", 70000, "999");
        }
        if (writerChanged == null && changedRecordFileName != null)
        {
            FileOutputStream changedRecordStream;
            try
            {
                File changedRecordFile = new File(changedRecordFileName);
                changedRecordStream = new FileOutputStream(changedRecordFile);
                writerChanged = new MarcSplitStreamWriter(changedRecordStream, "ISO-8859-1", 70000, "999");
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
            writerChanged = new MarcSplitStreamWriter(out, "ISO-8859-1", 70000, "999");
        }

        while(rawReader != null && rawReader.hasNext())
        {
            recordCounter++;
 
            try {
                RawRecord record = rawReader.next();
                Record patchedRecord = patchRecord(record, locationReader, changedLocationReader);
                
                if (writerAll != null)
                {
                    if (patchedRecord != null) writerAll.write(patchedRecord);
                    else out.write(record.getRecordBytes());
                }
                if (patchedRecord != null && writerChanged != null) 
                {
                    writerChanged.write(patchedRecord);
                }
                if (out != null) out.flush();
            }
            catch (MarcException me)
            {
                System.err.println("Error reading Marc Record: "+ me.getMessage());                                   
                logger.error("Error reading Marc Record: "+ me.getMessage());
                return(1);
            }        
            catch (IOException me)
            {
                System.err.println("Error Writing Raw Marc Record: "+ me.getMessage());                                   
                logger.error("Error Writing Raw Marc Record: "+ me.getMessage());
                return(1);
            }        
        }
        if (writerAll != null) { writerAll.close(); }
        if (writerChanged != null) { writerChanged.close(); }
        return 0;
    }

    
    private Record patchRecord(RawRecord rawRecord, BufferedReader locationReader, BufferedReader changedlocationReader)
    {
        boolean patched = false;
        Record record = null;
        String recId = rawRecord.getRecordId();
        if (locationFileLine == null) locationFileLine = getNextLocationLine(locationReader, changedlocationReader);
        while (locationFileLine != null && compare.compare(locationFileLine[0], recId) < 0)
        {
            locationFileLine = getNextLocationLine(locationReader, changedlocationReader);
        }
        if (locationFileLine != null && compare.compare(locationFileLine[0], recId) == 0)
        {
            record = rawRecord.getAsRecord(true, false, "999", "MARC8");
            List<VariableField> fields999 = (List<VariableField>)record.getVariableFields("999");
            while (locationFileLine != null && compare.compare(locationFileLine[0], recId) == 0)
            {
                patched |= patchRecordWithLine(record, fields999, locationFileLine);
                locationFileLine = getNextLocationLine(locationReader, changedlocationReader);
            }
            if (handleAllLocs)
            {
                fields999 = (List<VariableField>)record.getVariableFields("999");
                Iterator<VariableField> fieldIter = fields999.iterator();
                while (fieldIter.hasNext())
                {
                    VariableField vf = fieldIter.next();
                    if (vf.getId() == null) // vf.getId().intValue() != 1 && vf.getId().intValue() != 2)
                    {
                        record.removeVariableField(vf);
                        fieldIter.remove();
                        patched = true;
                    }
                }
            }
        }
        return(patched ? record : null);
    }

    private String[] getNextLocationLine(BufferedReader locationReader, BufferedReader changedLocationReader)
    {
        String line = null;
        String result[];
        do { 
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
            result = line.split("\\|");
            if (changedLocationReader == null) break;
            while (currentLocationID == null || compare.compare(currentLocationID, result[0]) < 0)
            {
                try
                {
                    currentLocationID = changedLocationReader.readLine();
                    if (currentLocationID == null) return(null);
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } while (!result[0].equals(currentLocationID));
    
        result[0] =  result[0].replaceFirst(mapPattern, mapReplace);
        result[1] = result[1].trim();
        return result;
    }

    private boolean patchRecordWithLine(Record record, List<VariableField> fields999, String locationFileLine2[])
    {
        boolean changed = false;
        boolean barcodeFound = false;
        DataField closestMatch = null;
        int minEditDistance = 100;
        for (VariableField f999 : fields999)
        {
            DataField df999 = (DataField)f999;
            List multi_i = null;
            if ((multi_i = df999.getSubfields('i')).size() > 1)
            {
                // patch to fix problem with multiple 'i' subfields problem
                // Subfield first_i = (Subfield)multi_i.get(0);
                Subfield second_i = (Subfield)multi_i.get(1);
                Subfield loc_k = df999.getSubfield('k');
                if (loc_k.getData().equals(second_i.getData()))
                {
                    df999.removeSubfield(second_i);
                }
                else
                {
                    second_i.setCode('k');
                }
                loc_k.setCode('l');
                changed = true;
            }
            Subfield barcode = df999.getSubfield('i');
            int curEditDistance;
            if (barcode != null && barcode.getData().equals(locationFileLine2[1]))
            {
                barcodeFound = true;
                df999.setId(new Long(1));
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
            else if (handleAllLocs && locationFileLine2.length >= 8)
            {
            }
            else if (handleAllLocs && !barcodeFound && barcode != null && (curEditDistance = getLevenshteinDistance(barcode.getData(), locationFileLine2[1])) < minEditDistance)
            {
                minEditDistance = curEditDistance;
                closestMatch = df999;
            }
        }
        // didn't find existing 999 corresponding to the location file line in question. create one from scratch.
        if (!barcodeFound && (closestMatch != null || locationFileLine2.length >= 8))
        {
            DataField df = new DataFieldImpl();
            df.addSubfield(new SubfieldImpl('a', closestMatch != null ? closestMatch.getSubfield('a').getData() : locationFileLine2[7]));
            df.addSubfield(new SubfieldImpl('w', closestMatch != null ? closestMatch.getSubfield('w').getData() : locationFileLine2[6]));
            df.addSubfield(new SubfieldImpl('i', locationFileLine2[1]));
            if (!locationFileLine2[2].equals(locationFileLine2[3]))
            {
                df.addSubfield(new SubfieldImpl('k', locationFileLine2[2]));
            }
            df.addSubfield(new SubfieldImpl('l', locationFileLine2[3]));
            df.addSubfield(new SubfieldImpl('m', locationFileLine2[4]));
            df.addSubfield(new SubfieldImpl('t', closestMatch != null ? closestMatch.getSubfield('t').getData() : locationFileLine2[5]));
            df.setId(new Long(2));
            df.setTag(closestMatch != null  ? closestMatch.getTag() : "999");
            df.setIndicator1(closestMatch != null  ? closestMatch.getIndicator1() : ' ');
            df.setIndicator2(closestMatch != null  ? closestMatch.getIndicator2() : ' ');
            record.addVariableField(df);
            changed = true;
        }
        return(changed);
    }

    public static int getLevenshteinDistance (String s, String t) 
    {
        if (s == null || t == null) {
          throw new IllegalArgumentException("Strings must not be null");
        }
              
        /*
          The difference between this impl. and the previous is that, rather 
           than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
           we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
           is the 'current working' distance array that maintains the newest distance cost
           counts as we iterate through the characters of String s.  Each time we increment
           the index of String t we are comparing, d is copied to p, the second int[].  Doing so
           allows us to retain the previous cost counts as required by the algorithm (taking 
           the minimum of the cost count to the left, up one, and diagonally up and to the left
           of the current cost count being calculated).  (Note that the arrays aren't really 
           copied anymore, just switched...this is clearly much better than cloning an array 
           or doing a System.arraycopy() each time  through the outer loop.)

           Effectively, the difference between the two implementations is this one does not 
           cause an out of memory condition when calculating the LD over two very large strings.          
        */        
              
        int n = s.length(); // length of s
        int m = t.length(); // length of t
              
        if (n == 0) {
          return m;
        } else if (m == 0) {
          return n;
        }

        int p[] = new int[n+1]; //'previous' cost array, horizontally
        int d[] = new int[n+1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i<=n; i++) {
           p[i] = i;
        }
              
        for (j = 1; j<=m; j++) {
           t_j = t.charAt(j-1);
           d[0] = j;
              
           for (i=1; i<=n; i++) {
              cost = s.charAt(i-1)==t_j ? 0 : 1;
              // minimum of cell to the left+1, to the top+1, diagonally left and up +cost                
              d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
           }

           // copy current distance counts to 'previous row' distance counts
           _d = p;
           p = d;
           d = _d;
        } 
              
        // our last action in the above loop was to switch d and p, so p now 
        // actually has the most recent cost counts
        return p[n];
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
        String changedLocationFile = null;
        String changedFile = null;
        boolean changesOnly = false;
        boolean handleAllLocs = false;
        String outputFile = null;
        for (int i = 1; i < args.length; i++)
        {
            if (args[i].endsWith(".txt") && locationFile == null) locationFile = args[i];
            else if (args[i].endsWith(".txt") && locationFile != null) changedLocationFile = args[i];
            else if (args[i].equals("handleAllLocs")) handleAllLocs = true;
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
            marcPatcher = new MarcPatcher(locationFile, changedLocationFile, changedFile, pOut, handleAllLocs);
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
//        System.clearProperty("marc.path");
//        System.clearProperty("marc.source");
    }


}
