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

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.Verifier;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.tools.SolrMarcException;
import org.solrmarc.tools.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class MarcFilteredReader implements MarcReader
{
    String includeRecordIfFieldPresent = null;
    String includeRecordIfFieldContains = null;
    String includeRecordIfFieldMissing = null;
    String includeRecordIfFieldDoesntContain = null;
    String deleteSubfieldsSpec = null;
    Record currentRecord = null;
    MarcReader reader;
    SolrMarcException exception;
//    private String remapPropertiesFilename;
    private Properties remapProperties = null;
    
    // Initialize logging category
    static Logger logger = Logger.getLogger(MarcFilteredReader.class.getName());
    
    /**
     * 
     * @param r
     * @param ifFieldPresent
     * @param ifFieldMissing
     */
    public MarcFilteredReader(MarcReader r, String ifFieldPresent, String ifFieldMissing, String deleteSubfields)
    {
        deleteSubfieldsSpec = deleteSubfields;
        if (ifFieldPresent != null)
        {
            String present[] = ifFieldPresent.split("/", 2);
            includeRecordIfFieldPresent = present[0];
            if (present.length > 1)
            {
                includeRecordIfFieldContains = present[1];
            }
        }
        if (ifFieldMissing != null)
        {
            String missing[] = ifFieldMissing.split("/", 2);
            includeRecordIfFieldMissing = missing[0];
            if (missing.length > 1)
            {
                includeRecordIfFieldDoesntContain = missing[1];
            }
        }
        reader = r;
    }

    public MarcFilteredReader(MarcReader r, String ifFieldPresent, String ifFieldMissing, String deleteSubfields, String remapURL)
    {
        this(r, ifFieldPresent, ifFieldMissing, deleteSubfields);
      //  remapPropertiesFilename = System.getProperty("marc.override.reader.remapURL", null);
        remapProperties = Utils.loadProperties(remapURL);      
    }

    /**
     * Implemented through interface
     * @return Returns true if the iteration has more records, false otherwise
     */
    public boolean hasNext()
    {
        if (currentRecord == null) 
        { 
            currentRecord = next(); 
        }
        return(currentRecord != null);
    }
    
    /**
     * Returns the next marc file in the iteration
     */
    public Record next()
    {
        
    	if (currentRecord != null) 
        { 
            Record tmp = currentRecord; 
            currentRecord = null; 
            return(tmp);
        }
        
        while (currentRecord == null)
        {
            if (!reader.hasNext()) return(null);
            Record rec = null;
            
            try {
                rec = reader.next();
            }
            catch (MarcException me)
            {
                //System.err.println("Error reading Marc Record: "+ me.getMessage());  
//            	exception = new SolrMarcException(me.getMessage(), me.getCause());
//            	exception.printMessage("Error reading Marc record:");
//            	exception.printStackTrace();
            	logger.error("Error reading Marc Record.");
            	logger.error(me.getMessage());
            }
            if (deleteSubfieldsSpec!= null) 
            {
                deleteSubfields(rec);
            }
            if (remapProperties != null)
            {
                remapRecord(rec);
            }
            if (rec != null && includeRecordIfFieldPresent != null)
            {
                Set<String> fields = SolrIndexer.getFieldList(rec, includeRecordIfFieldPresent);
                if (fields.size() != 0)
                {
                    if (includeRecordIfFieldContains == null || Utils.setItemContains(fields, includeRecordIfFieldContains))
                    {
                        currentRecord = rec;
                    }
                }
            }
           
            if (rec != null && includeRecordIfFieldMissing != null)
            {
                Set<String> fields = SolrIndexer.getFieldList(rec, includeRecordIfFieldMissing);
                if ((includeRecordIfFieldDoesntContain == null && fields.size() == 0) ||
                    (includeRecordIfFieldDoesntContain != null && !Utils.setItemContains(fields, includeRecordIfFieldDoesntContain)))
                {
                    currentRecord = rec;
                }
            }
            if (rec != null && includeRecordIfFieldPresent == null && includeRecordIfFieldMissing == null)
            {
                currentRecord = rec;
            }
        }
        return currentRecord ;
    }

    void deleteSubfields(Record rec)
    {
        String fieldSpecs[] = deleteSubfieldsSpec.split(":");
        for (String fieldSpec : fieldSpecs)
        {
            String tag = fieldSpec.substring(0,3);
            String subfield = null;
            if (fieldSpec.length() > 3)  subfield = fieldSpec.substring(3);                    
            List<VariableField> list = (List<VariableField>)rec.getVariableFields(tag);
            for (VariableField field : list)
            {
                if (field instanceof DataField)
                {
                    DataField df = ((DataField)field);
                    if (subfield != null) 
                    {
                        List<Subfield> sfs = (List<Subfield>)df.getSubfields(subfield.charAt(0));
                        if (sfs != null && sfs.size() != 0)
                        {
                            rec.removeVariableField(df);
                            for (Subfield sf : sfs)
                            {
                                df.removeSubfield(sf);
                            }
                            rec.addVariableField(df);
                        }
                    }
                    else
                    {
                        rec.removeVariableField(df);
                    }
                }
            }
        }
    }
    
    void remapRecord(Record rec)
    {
        List<VariableField> fields = rec.getVariableFields();
        List<VariableField> fToDelete = new ArrayList<VariableField>();
        for (VariableField field : fields)
        {
            String tag = field.getTag();
            if (remapProperties.containsKey(tag))
            {
                if (Verifier.isControlNumberField(tag)) 
                {
                    
                }
                else
                {
                    List<Subfield> subfields = ((DataField)field).getSubfields();
                    List<Subfield> sfToDelete = new ArrayList<Subfield>();
                    for (int i = 0; remapProperties.containsKey(tag+"_"+i); i++)
                    {
                        String remapString = remapProperties.getProperty(tag+"_"+i);
                        String mapParts[] = remapString.split("=>");
                        if (eval(mapParts[0], (DataField)field))
                        {
                            process(mapParts[1], (DataField)field, sfToDelete, fToDelete);
                        }
                    }

                    if (sfToDelete.size() != 0)
                    {
                        for (Subfield sf : sfToDelete)
                        {
                            ((DataField)field).removeSubfield(sf);
                        }
                    }
                }
            }
        }
        if (fToDelete.size() != 0)
        {
            for (VariableField field : fields)
            {
                rec.removeVariableField(field);
            }
        }
    }
    
    private boolean eval(String conditional, DataField field)
    {
        List<Subfield> subfields;
        if (conditional.startsWith("true()"))
        {
            return(true);
        }
        else if (conditional.startsWith("not("))
        {
            String arg = getOneArg(conditional);
            if (arg.length() == 1)
            {
                return(!eval(conditional, field));
            }
        }
        else if (conditional.startsWith("subfieldmatches("))
        {
            String args[] = getTwoArgs(conditional);
            if (args.length == 2 && args[0].length() == 1)
            {
                subfields = field.getSubfields(args[0].charAt(0));
                for (Subfield sf : subfields)
                {
                    if (sf.getData().matches(args[1]))
                        return(true);
                }
            }
        }
        else if (conditional.startsWith("subfieldcontains("))
        {
            String args[] = getTwoArgs(conditional);
            if (args.length == 2 && args[0].length() == 1)
            {
                subfields = field.getSubfields(args[0].charAt(0));
                for (Subfield sf : subfields)
                {
                    if (sf.getData().contains(args[1]))
                        return(true);
                }
            }
        }
        else if (conditional.startsWith("subfieldexists("))
        {
            String arg = getOneArg(conditional);
            if (arg.length() == 1)
            {
                subfields = field.getSubfields(arg.charAt(0));
                if (subfields.size() > 0) return(true);
            }
        }
        else if (conditional.startsWith("and("))
        {
            String args[] = getTwoConditionals(conditional);
            if (args.length == 2)
            {
                return(eval(args[0], field) && eval(args[1], field));
            }
        }
        else if (conditional.startsWith("or("))
        {
            String args[] = getTwoConditionals(conditional);
            if (args.length == 2)
            {
                return(eval(args[0], field) || eval(args[1], field));
            }
        }
        return false;
    }
    
    private void process(String command, DataField field, List<Subfield> sfToDelete, List<VariableField> fToDelete)
    {
        List<Subfield> subfields;
        if (command.startsWith("replace("))
        {
            String args[] = getThreeArgs(command);
            if (args.length == 3 && args[0].length() == 1)
            {
                subfields = field.getSubfields(args[0].charAt(0));
                for (Subfield sf : subfields)
                {
                    String newData = sf.getData().replaceAll(args[1], args[2]);
                    if (!newData.equals(sf.getData()))
                    {
                        sf.setData(newData);
                    }
                }
            }
        }
        else if (command.startsWith("append("))
        {
            String args[] = getTwoArgs(command);
            if (args.length == 2 && args[0].length() == 1)
            {
                subfields = field.getSubfields(args[0].charAt(0));
                for (Subfield sf : subfields)
                {
                    String newData = sf.getData() + args[1];
                    if (!newData.equals(sf.getData()))
                    {
                        sf.setData(newData);
                    }
                }
            }
        }
        else if (command.startsWith("prepend("))
        {
            String args[] = getTwoArgs(command);
            if (args.length == 2 && args[0].length() == 1)
            {
                subfields = field.getSubfields(args[0].charAt(0));
                for (Subfield sf : subfields)
                {
                    String newData = args[1] + sf.getData();
                    if (!newData.equals(sf.getData()))
                    {
                        sf.setData(newData);
                    }
                }
            }
        }
        else if (command.startsWith("deletesubfield("))
        {
            String arg = getOneArg(command);
            if (arg.length() == 1)
            {
                subfields = field.getSubfields(arg.charAt(0));
                for (Subfield sf : subfields)
                {
                    sfToDelete.add(sf);
                }
            }
        }
        else if (command.startsWith("both("))
        {
            String args[] = getTwoConditionals(command);
            if (args.length == 2)
            {
                process(args[0], field, sfToDelete, fToDelete);
                process(args[1], field, sfToDelete, fToDelete);
            }
        }
        else if (command.startsWith("deletefield("))
        {
            fToDelete.add(field);
        }        
    }
    
    static Pattern oneArg = Pattern.compile("[a-z]*[(]\"((\\\"|[^\"])*)\"[ ]*[)]");
    private String getOneArg(String conditional)
    {
        Matcher m = oneArg.matcher(conditional.trim());
        if (m.matches())
        {
            return(m.group(1).replaceAll("\\\"", "\""));
        }
        return null;
    }
    
    static Pattern twoArgs = Pattern.compile("[a-z]*[(]\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\"[)]");
    private String[] getTwoArgs(String conditional)
    {
        Matcher m = twoArgs.matcher(conditional.trim());
        if (m.matches())
        {
            String result[] = new String[]{m.group(1).replaceAll("\\\"", "\""), m.group(3).replaceAll("\\\"", "\"")};
            return(result);
        }
        return null;
    }
    
    static Pattern threeArgs = Pattern.compile("[a-z]*[(][ ]*\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\"[)]");
    private String[] getThreeArgs(String conditional)
    {
        Matcher m = threeArgs.matcher(conditional.trim());
        if (m.matches())
        {
            String result[] = new String[]{m.group(1).replaceAll("\\\"", "\""), m.group(3).replaceAll("\\\"", "\""), m.group(5).replaceAll("\\\"", "\"")};
            return(result);
        }
        return null;
    }
    
    static Pattern twoConditionals = Pattern.compile("[a-z]*[(]([a-z]*[(].*[)]),[ ]*([a-z]*[(].*[)])[)]");
    private String[] getTwoConditionals(String conditional)
    {
        Matcher m = twoConditionals.matcher(conditional.trim());
        if (m.matches())
        {
            String result[] = new String[]{m.group(1), m.group(2)};
            return(result);
        }
        return null;
    }

}
