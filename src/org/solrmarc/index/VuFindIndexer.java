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
	 * Determine Record Format(s)
	 *
	 * @param  Record          record
	 * @return Set<String>     format of record
	 */
	public Set<String> getFormat(final Record record){
        Set<String> result = new LinkedHashSet<String>();
		String leader = record.getLeader().toString();
		char leaderBit;
		ControlField fixedField = (ControlField) record.getVariableField("008");
		DataField title = (DataField) record.getVariableField("245");
		char formatCode = ' ';

		// check if there's an h in the 245
		if (title != null) {
		    if (title.getSubfield('h') != null){
		        if (title.getSubfield('h').getData().toLowerCase().contains("[electronic resource]")) {
		    		result.add("Electronic");
		    		return result;
		        }
        	}
        }

        // check the 007 - this is a repeating field
        List<ControlField> fields = record.getVariableFields("007");
        Iterator<ControlField> fieldsIter = fields.iterator();
        if (fields != null) {
            ControlField formatField;
            while(fieldsIter.hasNext()) {
                formatField = (ControlField) fieldsIter.next();
                formatCode = formatField.getData().toUpperCase().charAt(0);
        	    switch (formatCode) {
                    case 'A':
                        switch(formatField.getData().toUpperCase().charAt(1)) {
                            case 'D':
                                result.add("Atlas");
                                break;
                            default:
                                result.add("Map");
                                break;
                        }
                        break;
                    case 'C':
                        switch(formatField.getData().toUpperCase().charAt(1)) {
                            case 'A':
                                result.add("TapeCartridge");
                                break;
                            case 'B':
                                result.add("ChipCartridge");
                                break;
                            case 'C':
                                result.add("DiscCartridge");
                                break;
                            case 'F':
                                result.add("TapeCassette");
                                break;
                            case 'H':
                                result.add("TapeReel");
                                break;
                            case 'J':
                                result.add("FloppyDisk");
                                break;
                            case 'M':
                            case 'O':
                                result.add("CDROM");
                                break;
                            case 'R':
                                // Do not return - this will cause anything with an
                                // 856 field to be labeled as "Electronic"
                                break;
                            default:
                                result.add("Software");
                                break;
                        }
                        break;
                    case 'D':
                        result.add("Globe");
                        break;
                    case 'F':
                        result.add("Braille");
                        break;
                    case 'G':
                        switch(formatField.getData().toUpperCase().charAt(1)) {
                            case 'C':
                            case 'D':
                                result.add("Filmstrip");
                                break;
                            case 'T':
                                result.add("Transparency");
                                break;
                            default:
                                result.add("Slide");
                                break;
                        }
                        break;
                    case 'H':
                        result.add("Microfilm");
                        break;
                    case 'K':
                        switch(formatField.getData().toUpperCase().charAt(1)) {
                            case 'C':
                                result.add("Collage");
                                break;
                            case 'D':
                                result.add("Drawing");
                                break;
                            case 'E':
                                result.add("Painting");
                                break;
                            case 'F':
                                result.add("Print");
                                break;
                            case 'G':
                                result.add("Photonegative");
                                break;
                            case 'J':
                                result.add("Print");
                                break;
                            case 'L':
                                result.add("Drawing");
                                break;
                            case 'O':
                                result.add("FlashCard");
                                break;
                            case 'N':
                                result.add("Chart");
                                break;
                            default:
                                result.add("Photo");
                                break;
                        }
                        break;
                    case 'M':
                        switch(formatField.getData().toUpperCase().charAt(1)) {
                            case 'F':
                                result.add("VideoCassette");
                                break;
                            case 'R':
                                result.add("Filmstrip");
                                break;
                            default:
                                result.add("MotionPicture");
                                break;
                        }
                        break;
                    case 'O':
                        result.add("Kit");
                        break;
                    case 'Q':
                        result.add("MusicalScore");
                        break;
                    case 'R':
                        result.add("SensorImage");
                        break;
                    case 'S':
                        switch(formatField.getData().toUpperCase().charAt(1)) {
                            case 'D':
                                result.add("SoundDisc");
                                break;
                            case 'S':
                                result.add("SoundCassette");
                                break;
                            default:
                                result.add("SoundRecording");
                                break;
                        }
                        break;
                    case 'V':
                        switch(formatField.getData().toUpperCase().charAt(1)) {
                            case 'C':
                                result.add("VideoCartridge");
                                break;
                            case 'D':
                                result.add("VideoDisc");
                                break;
                            case 'F':
                                result.add("VideoCassette");
                                break;
                            case 'R':
                                result.add("VideoReel");
                                break;
                            default:
                                result.add("Video");
                                break;
                        }
                        break;
            	}
            }
        	if (!result.isEmpty()) {
                return result;
            }
        }

        // check the Leader at position 6
        leaderBit = leader.charAt(6);
        switch (Character.toUpperCase(leaderBit)) {
            case 'C':
            case 'D':
                result.add("MusicalScore");
                break;
            case 'E':
            case 'F':
                result.add("Map");
                break;
            case 'G':
                result.add("Slide");
                break;
            case 'I':
                result.add("SoundRecording");
                break;
            case 'J':
                result.add("MusicRecording");
                break;
            case 'K':
                result.add("Photo");
                break;
            case 'M':
                result.add("Electronic");
                break;
            case 'O':
            case 'P':
                result.add("Kit");
                break;
            case 'R':
                result.add("PhysicalObject");
                break;
            case 'T':
                result.add("Manuscript");
                break;
        }
    	if (!result.isEmpty()) {
            return result;
        }

        // check the Leader at position 7
        leaderBit = leader.charAt(7);
        switch (Character.toUpperCase(leaderBit)) {
            // Monograph
            case 'M':
                if (formatCode == 'C') {
                    result.add("eBook");
                } else {
                    result.add("Book");
                }
                break;
            // Serial
            case 'S':
                // Look in 008 to determine what type of Continuing Resource
                formatCode = fixedField.getData().toUpperCase().charAt(21);
                switch (formatCode) {
                    case 'N':
                        result.add("Newspaper");
                        break;
                    case 'P':
                        result.add("Journal");
                        break;
                    default:
                        result.add("Serial");
                        break;
                }
        }

        // Nothing worked!
    	if (result.isEmpty()) {
            result.add("Unknown");
        }
        
        return result;
	}

    /**
     * Extract the call number label from a record
     * @param record
     * @return Call number label
     */
    public String getFullCallNumber(final Record record) {

		String val = getFirstFieldVal(record, "099ab:090ab:050ab");

		if (val != null) {
            return val.toUpperCase().replaceAll(" ", "");
		} else {
			return val;
		}
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
            return val.toUpperCase();
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
            String [] callNumberSubject = val.toUpperCase().split("[^A-Z]+");
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
     * Creates individual strings for each iteration of the MARC field designated
     * @param   Record  record          The Record object to pull the data from
     * @param   String  marcFieldNum    The marc field number as a string (e.g. "245")
     * @return
     */
    public Set<String> getAllSubfields(final Record record, String marcFieldNum)
    {
        Set<String> result = new LinkedHashSet<String>();
        
        List<DataField> fields = record.getVariableFields(marcFieldNum);
        Iterator<DataField> fieldsIter = fields.iterator();
        DataField field;

        List<DataField> subfields;
        Iterator<DataField> subfieldsIter;
        Subfield subfield;

        // Loop through fields
        while(fieldsIter.hasNext()) {
            field = (DataField) fieldsIter.next();

            StringBuffer buffer = new StringBuffer("");

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
            if ((tag >= 100) && (tag < 900)) {
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