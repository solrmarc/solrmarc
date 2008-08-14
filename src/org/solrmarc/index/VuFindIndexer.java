package org.solrmarc.index;
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
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class VuFindIndexer extends SolrIndexer
{

	// Initialize logging category
    static Logger logger = Logger.getLogger(VuFindIndexer.class.getName());
	
	/**
	 * Default constructor
	 * @param propertiesMapFile
	 * @throws Exception
	 */
    /*
    public VuFindIndexer(final String propertiesMapFile) throws FileNotFoundException, IOException, ParseException 
    {
        super(propertiesMapFile);
    }
    */
	public VuFindIndexer(final String propertiesMapFile, final String solrMarcDir)
			throws FileNotFoundException, IOException, ParseException {
		super(propertiesMapFile, solrMarcDir);
	}
    
    /**
	 * Determine Record Main Format
	 *
	 * @param  Record  record
	 * @return String  Main format of record
	 */
	public String getFormat(final Record record){
		String leader = record.getLeader().toString();
		char leaderBit;
		ControlField formatField = (ControlField) record.getVariableField("007");
		ControlField fixedField = (ControlField) record.getVariableField("008");
		DataField title = (DataField) record.getVariableField("245");
		char formatCode = ' ';

		// check if there's an h in the 245
		if (title != null) {
		    if (title.getSubfield('h') != null){
		        if (title.getSubfield('h').getData().toLowerCase().contains("[electronic resource]")) {
		    		return "Electronic";
		        }
        	}
        }

        // check the 007
        if(formatField != null){
            formatCode = formatField.getData().toUpperCase().charAt(0);
        	switch (formatCode) {
                case 'A':
                    return "Map";
                /*
                Removed for inaccuracies - turns journals with 856 to electronic
                case 'C':
                    return "Electronic";
                */
                case 'D':
                    return "Globe";
                case 'F':
                    return "Braille";
                case 'G':
                    return "Slide";
                case 'H':
                    return "Microfilm";
                case 'K':
        			return "Photo";
                case 'M':
                case 'V':
                    return "Video";
                case 'O':
                    return "Kit";
                case 'Q':
                    return "Musical Score";
                case 'R':
                    return "Sensor Image";
                case 'S':
                    return "Audio";
        	}
        }

        // check the Leader
        leaderBit = leader.charAt(6);
        switch (Character.toUpperCase(leaderBit)) {
            case 'C':
            case 'D':
                return "Musical Score";
            case 'E':
            case 'F':
                return "Map";
            case 'G':
                return "Slide";
            case 'I':
            case 'J':
                return "Audio";
            case 'K':
                return "Photo";
            case 'M':
                return "Electronic";
            case 'O':
            case 'P':
                return "Kit";
            case 'R':
                return "Physical Object";
            case 'T':
                return "Manuscript";
        }

        // check the Leader
        leaderBit = leader.charAt(7);
        switch (Character.toUpperCase(leaderBit)) {
            // Monograph
            case 'M':
                if (formatCode == 'C') {
                    return "eBook";
                } else {
                    return "Book";
                }
            // Serial
            case 'S':
                // Look in 008 to determine what type of Continuing Resource
                formatCode = fixedField.getData().toUpperCase().charAt(21);
                switch (formatCode) {
                    case 'N':
                        return "Newspaper";
                    case 'P':
                        return "Journal";
                }
                return "Serial";
        }

        return "Unknown";
	}
    
    /**
     * Extract the call number label from a record
     * @param record
     * @return Call number label
     */
    public String getCallNumberLabel(final Record record) {
		
		String val = getFirstFieldVal(record, "090a:050a");
		
		if (val != null) {
            int dotPos = val.indexOf(".");
            if (dotPos > 0) {
                val = val.substring(0, dotPos);
            }
            return val;
		} else {
			return val;
		}
	}
	
	/**
     * Extract the subject component of the call number
     *
     * Can return null
     *
     * @param record
     * @return Call number label
     */
    public String getCallNumberSubject(final Record record) {

        String val = getFirstFieldVal(record, "090a:050a");

        if (val != null) {
            String [] callNumberSubject = val.split("[^A-Z]+");
        	return callNumberSubject[0];
        } else {
            return val;
        }
    }

    /**
     * Extract all topics from a record
     *
     * @param record
     * @return
     */
    public Set<String> getFullTopic(final Record record) {
        Set<String> result = new LinkedHashSet<String>();

        result.addAll(getAllSubfields(record, "600"));
        result.addAll(getAllSubfields(record, "610"));
        result.addAll(getAllSubfields(record, "630"));
        result.addAll(getAllSubfields(record, "650"));
        return result;
    }

    /**
     * Extract all subject geographic regions from a record
     *
     * @param record
     * @return
     */
    public Set<String> getFullGeographic(final Record record) {
    	Set<String> result = new LinkedHashSet<String>();

    	result.addAll(getAllSubfields(record, "651"));
    	return result;
    }

    /**
     * Extract all genres from a record
     *
     * @param record
     * @return
     */
    public Set<String> getFullGenre(final Record record) {
    	Set<String> result = new LinkedHashSet<String>();

    	result.addAll(getAllSubfields(record, "655"));
    	return result;
    }

    /**
     * Create a string from the entire 245 field
     *
     * @param record
     * @return
     */
    public Set<String> getFullTitle(final Record record) {
    	return getAllSubfields(record, "245");
    }

    /**
     * extract all the subfields in a given marc field
     * @param record
     * @param marcFieldNum - the marc field number as a string (e.g. "245")
     * @return
     */
    public Set<String> getAllSubfields(final Record record, String marcFieldNum)
    {
        Set<String> result = new LinkedHashSet<String>();

        StringBuffer buffer = new StringBuffer("");

        DataField marcField = (DataField) record.getVariableField(marcFieldNum);
        if (marcField != null) {
            List<Subfield> subfields = marcField.getSubfields();
            Iterator<Subfield> iter = subfields.iterator();

            Subfield subfield;

            while (iter.hasNext()) {
               subfield = iter.next();
                if (buffer.length() > 0) {
                    buffer.append(" " + subfield.getData());
                } else {
                    buffer.append(subfield.getData());
                }
            }
            result.add(buffer.toString());
        }

        return result;
    }

	/**
	 * Loops through all datafields and creates a field for "all fields"
	 * searching
	 *
	 * @param record Marc record to extract data from
	 */
    public String getAllFields(final Record record)
    {
        StringBuffer buffer = new StringBuffer("");

        List<DataField> fields = record.getDataFields();
        Iterator<DataField> fieldsIter = fields.iterator();
        DataField field;

        List<DataField> subfields;
        Iterator<DataField> subfieldsIter;
        Subfield subfield;

        // Loop through fields
        while(fieldsIter.hasNext()) {
            field = (DataField) fieldsIter.next();

            // Get all fields starting with the 100 and ending with the 839
            // This will ignore any "code" fields and only use textual fields
            int tag = Integer.parseInt(field.getTag());
            if ((tag >= 100) && (tag < 840)) {
                // Loop through subfields
                subfields = field.getSubfields();
                subfieldsIter = subfields.iterator();
                while (subfieldsIter.hasNext()) {
                    subfield = (Subfield) subfieldsIter.next();
                    if (buffer.length() > 0) {
                        buffer.append(" " + subfield.getData());
                    } else {
                        buffer.append(subfield.getData());
                    }
                }
            }
        }

        return buffer.toString();
    }


}