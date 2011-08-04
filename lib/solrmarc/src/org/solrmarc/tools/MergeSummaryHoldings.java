package org.solrmarc.tools;

import java.io.*;
import java.util.*;

import org.marc4j.*;
import org.marc4j.marc.*;
import org.solrmarc.marc.*;
import org.solrmarc.marcoverride.MarcSplitStreamWriter;


/**
 * Given a file of MARC bib records and another file of MARC (MHLD) records,
 *  read through the bib file and look for matching MHLD records.  If found,
 *  merge the desired fields from the MHLD record into the bib record, first
 *  removing any existing fields in the bib rec matching a desired field tag.
 *  
 * Note that the MHLD file must have records in StringNaturalCompare ascending
 *  order.
 *  
 * @author naomi
 *
 */
public class MergeSummaryHoldings implements MarcReader
{
    /** default list of MHLD fields to be merged into the bib record, separated by '|' char */
    public static String mhldFldsToMergeDefault = "852|866|867|863|853|868";

    static boolean verbose = false;
    static boolean veryverbose = false;

    /** for the file of MARC bib records */
    private RawRecordReader bibRecsRawRecRdr = null;
    
    /** can be used as an alternative reader for the file of MARC bib records*/
    private MarcReader bibRecsMarcReader = null;

    /** the name of the file containing MHLD records.  It must be a class variable
     * because the file may need to be read multiple times to match bib records */
    private String mhldRecsFileName;
    
    /** for the file of MARC MHLD records */
    private RawRecordReader mhldRawRecRdr = null;

    /**
     * used to find an MHLD record matching the current bib record
     * set to null if:
     *   - we are at the beginning of the MHLD record file
     *   - if we found an MHLD record to match current the bib record 
     * otherwise set to:
     *   - the last mhld record read -- the mhld record with an id greater than the bib id, or the last mhld record in the file
     * if we were able to find a matching record, this is set to null
     */
    private RawRecord unmatchedPrevMhldRec = null;
    
    /**
     *  used to find an MHLD record matching the current bib record - particular
     *   this tells us if we need to start over at the beginning of the MHLD file
     *   to look for matches
     * if we have a matching MHLD record, this is the id of that MHLD record
     * if we don't have a matching MHLD record, this is set to the id of the MHLD record previous to the last mhld record read (unmatchedPrevMhldRec)
     * */
    private String prevMhldRecID = null;

    /** true if we want to attempt to use marc records if they are invalid in ways we can safely ignore 
     * used when reading bib and mhld records */
    private boolean permissive;

    /** true if records should be converted to UTF-8 when they become Record objects
    * used when reading bib and mhld records */
    private boolean toUtf8;
    
    /** the encoding to use as a default for reading the records; usually MARC8
    * used when reading bib and mhld records */
    private String defaultEncoding = null;

    /** list of MHLD fields to be merged into the bib record, separated by '|' char */
    private String mhldFldsToMerge = null;


    
    public MergeSummaryHoldings(RawRecordReader bibRecsRawRecRdr, boolean permissive, boolean toUtf8, String defaultEncoding, 
                                String mhldRecsFileName, String mhldFldsToMerge)
    {
        this.bibRecsRawRecRdr = bibRecsRawRecRdr;
        bibRecsMarcReader = null;
        this.mhldRecsFileName = mhldRecsFileName;
        this.permissive = permissive;
        this.toUtf8 = toUtf8;
        this.defaultEncoding = defaultEncoding;
        this.mhldFldsToMerge = mhldFldsToMerge;
        readMhldFileFromBeginning(mhldRecsFileName);
    }
    
    public MergeSummaryHoldings(RawRecordReader bibRecsRawRecRdr, String mhldRecsFileName, String mhldFldsToMerge)
    {
        this (bibRecsRawRecRdr, true, false, "MARC8", mhldRecsFileName, mhldFldsToMerge);
    }
    
    public MergeSummaryHoldings(MarcReader bibRecsRawRecRdr, String mhldRecsFileName, String mhldFldsToMerge)
    {
        this.bibRecsRawRecRdr = null;
        bibRecsMarcReader = bibRecsRawRecRdr;
        this.mhldRecsFileName = mhldRecsFileName;
        this.mhldFldsToMerge = mhldFldsToMerge;
        readMhldFileFromBeginning(mhldRecsFileName);
    }
    
    
    /**
     * create a new RawRecordReader for the MHLD records file, and reset
     *  prevMhldRecID and unmatchedPrevMhldRec to null
     * @param mhldRecsFileName
     */
    private void readMhldFileFromBeginning(String mhldRecsFileName)
    {
        try
        {
        	mhldRawRecRdr = new RawRecordReader(new FileInputStream(new File(mhldRecsFileName)));
        }
        catch (FileNotFoundException e)
        {
        	mhldRawRecRdr = null;           
        }
        prevMhldRecID = null;
        unmatchedPrevMhldRec = null;
    }

    /**
     * return true if there is another record in the bib records file
     */
    public boolean hasNext()
    {
        if (bibRecsRawRecRdr != null) 
        	return(bibRecsRawRecRdr.hasNext());
        else if (bibRecsMarcReader != null) 
        	return(bibRecsMarcReader.hasNext());
        return(false);
    }
    
    /**
     * Since this class is a MarcReader, it must implement the next() method.
     * Get the next bib record from the file of MARC bib records, then look 
     *  for a matching MARC MHLD record in the MHLD recs file, and if found, 
     *  merge the MHLD fields specified in mhldFldsToMerge into the bib 
     *  record and then return the bib record.
     */
    public Record next()
    {
        Record bibRec = null;
        if (bibRecsRawRecRdr != null) 
        {
            RawRecord rawrec = bibRecsRawRecRdr.next();
            bibRec = rawrec.getAsRecord(permissive, toUtf8, "999", defaultEncoding);
        }
        else if (bibRecsMarcReader != null)
        {
            bibRec = bibRecsMarcReader.next();
        }
        RawRecord matchingRawMhldRec = getMatchingMhldRawRec(bibRec.getControlNumber());
        bibRec = addMhldFieldsToBibRec(bibRec, matchingRawMhldRec);
        return(bibRec);
    }

    
    
    /**
     * given a bib record ID, find the next MHLD record with a matching id.  
     *  Also sets  unmatchPrevMhldRec and prevMhldRecID and sometimes mhldRawRecRdr
     *  
     * Note that this method appears to find the next matchingMHLD even if the 
     *  bibRecId is the same as the id for the previously matchingMHLD
     * @param bibRecID
     * @return
     */
    private RawRecord getMatchingMhldRawRec(String bibRecID)
    {
    	Comparator<String> comparator = new StringNaturalCompare();
    	
    	// if the id before the last read MHLD id is bigger than the bib id to be 
    	//   matched, then start over in the mhld file
        if (prevMhldRecID != null && comparator.compare(prevMhldRecID, bibRecID) > 0)
        {
        	readMhldFileFromBeginning(mhldRecsFileName);
        }
        
        // if the most recent MHLD record read was a match, or we have started MHLD file from beginning
        //    (we have no unmatching last retrieved mhld record)
        // then get the next record in the MHLD file before entering loop
        if (unmatchedPrevMhldRec == null && mhldRawRecRdr != null && mhldRawRecRdr.hasNext() )
        {
            unmatchedPrevMhldRec = mhldRawRecRdr.next();
        }
        
        // look for an MHLD record that matches the bib rec id, up until the MHLD record id comes after the bib record id;  
        // also keep track of the prior MHLD rec id while searching 
        while (mhldRawRecRdr != null && mhldRawRecRdr.hasNext() && comparator.compare(unmatchedPrevMhldRec.getRecordId(), bibRecID) < 0)
        {
        	// keep the previous MHLD id before we get the new MHLD record
            prevMhldRecID = unmatchedPrevMhldRec.getRecordId();
            unmatchedPrevMhldRec = mhldRawRecRdr.next(); // well, it might match, but we'll address that in the next lines
        }
        
        // if we have a matching mhld, then set prevMhldRecID to the matching record and set unmatchedPrevMhldRec to null
        //  before returning the matching MHLD record
        if (unmatchedPrevMhldRec != null && comparator.compare(unmatchedPrevMhldRec.getRecordId(), bibRecID) == 0)
        {
            RawRecord matchingMhldRec = unmatchedPrevMhldRec; 
            unmatchedPrevMhldRec = null;
            prevMhldRecID = matchingMhldRec.getRecordId();   
            return(matchingMhldRec);
        }
        
        // booby prize
        return(null);
    }
    
    /**
     * given a MARC bib record as a Record object, and a MARC MHLD record as
     *  a RawRecord object, merge the MHLD fields indicated in class var
     *  mhldFldsToMerge into the bib record, first removing any of those fields
     *  already existing in the bib record.
     * @param bibRecord
     * @param rawMhldRecord
     * @return the bib record with the MHLD fields merged in
     */
    private Record addMhldFieldsToBibRec(Record bibRecord, RawRecord rawMhldRecord)
    {
        Record mhldRecord = rawMhldRecord.getAsRecord(permissive, toUtf8, null, defaultEncoding);
        List<VariableField> lvf = (List<VariableField>) bibRecord.getVariableFields(mhldFldsToMerge.split("[|]"));
        for (VariableField vf : lvf)
        {
            bibRecord.removeVariableField(vf);
        }
        bibRecord = MarcCombiningReader.combineRecords(bibRecord, mhldRecord, mhldFldsToMerge);
        return(bibRecord);
    }

    /**
     * Given a file of MARC MHLD records and a file of MARC Bibliographic records,
     *  merge selected fields from the MHLD records into matching MARC Bib records.  
     *  Ignores MHLD records with no matching bib record.
     *  Selected fields are defined in class constant mhldFldsToMerge.
     * Note that the MHLD file must have records in StringNaturalCompare ascending order.
     * @param args - command line arguments
     */
    public static void main(String[] args)
    {
    	String mhldRecsFileName = null;
        RawRecordReader bibsRawRecRdr = null;
        boolean outputAllBibs = false;
        
        int argoffset = 0;
        if (args.length == 0)
        {
            System.err.println("Usage: MergeSummaryHoldings [-v] [-a] -s marcMhldFile.mrc  marcBibsFile.mrc");
            System.err.println("   or: cat marcBibsFile.mrc | MergeSummaryHoldings [-v] [-a] -s marcMhldFile.mrc ");
        }
        while (argoffset < args.length && args[argoffset].startsWith("-"))
        {
            if (args[argoffset].equals("-v"))
            {
                verbose = true;
                argoffset++;
            }
            if (args[argoffset].equals("-vv"))
            {
                verbose = true;
                veryverbose = true;
                argoffset++;
            }
            if (args[argoffset].equals("-a"))
            {
            	outputAllBibs = true;
                argoffset++;
            }
            if (args[argoffset].equals("-s"))
            {
                mhldRecsFileName = args[1+argoffset];
                argoffset += 2;
            }
        }

        // last argument should be the name of a file containing marc bib records
        if (args.length > argoffset && (args[argoffset].endsWith(".mrc") || args[argoffset].endsWith(".marc") || args[argoffset].endsWith(".xml")))
        {
            try
            {
                bibsRawRecRdr = new RawRecordReader(new FileInputStream(new File(args[argoffset])));
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(1);
            }
        }
        else // or the marc bib records should be read from std in
        {
            bibsRawRecRdr = new RawRecordReader(System.in);
        }
        
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        mergeMhldRecsIntoBibRecs(bibsRawRecRdr, mhldRecsFileName, outputAllBibs);
        System.exit(0);
    }
    
    
    /**
     * called from main()
     * 
     * for each bib record in the "file" that has a corresponding mhld record
     *  1) remove any existing fields in the bib record that duplicate the mhld fields to be merged into the bib record
     *  2) merge the mhld fields into the bib record
     *  3) write the resulting record to stdout (always if allRecords = true;  only if bib record changed if allRecords = false)
     * 
     * @param bibsRawRecRdr - a RawRecordReader instantiated for a file of MARC bibliographic records
     * @param mhldRecsFileName - the name of the file containing MARC MHLD records
     * @param outputAllBibs - write the bib record to stdout even if it wasn't changed
     */
    private static void mergeMhldRecsIntoBibRecs(RawRecordReader bibsRawRecRdr, String mhldRecsFileName, boolean outputAllBibs)
    {
        MergeSummaryHoldings merger = new MergeSummaryHoldings(bibsRawRecRdr, true, false, "MARC8", 
                                                               mhldRecsFileName, mhldFldsToMergeDefault);
        RawRecord rawBibRecCurrent = null;
        RawRecord matchingRawMhldRec = null;
        MarcWriter writer = new MarcSplitStreamWriter(System.out, "ISO-8859-1", 70000, "999");
        while (bibsRawRecRdr.hasNext())
        {
// FIXME:  we may need a loop within this loop in case there is more than one MHLD record matching the same bib record

        	rawBibRecCurrent = bibsRawRecRdr.next();
            matchingRawMhldRec = merger.getMatchingMhldRawRec(rawBibRecCurrent.getRecordId());
            try
            {
                if (matchingRawMhldRec != null)
                {
                	// remove any existing fields in the bib record that duplicate mhld fields to be merged into the bib record
                    Record bibRecWithChanges = rawBibRecCurrent.getAsRecord(true, false, "999", "MARC8");
                    Record bibRecWithoutChanges = null;                    
                    boolean removedField = false;
                    List<VariableField> lvf = (List<VariableField>) bibRecWithChanges.getVariableFields(mhldFldsToMergeDefault.split("[|]"));
                    for (VariableField vf : lvf)
                    {
                        bibRecWithChanges.removeVariableField(vf);
                        removedField = true;
                    }
                    
                    // we will ensure that there is a difference between the orig record and the rec with removed field(s)
                    if (removedField) 
                    	bibRecWithoutChanges = rawBibRecCurrent.getAsRecord(true, false, "999", "MARC8");

                    Record matchMhldRec = matchingRawMhldRec.getAsRecord(true, false, mhldFldsToMergeDefault, "MARC8");
                    
                    // prepare the merged record
                    bibRecWithChanges = MarcCombiningReader.combineRecords(bibRecWithChanges, matchMhldRec, mhldFldsToMergeDefault, "999");
                    
                    // only keep the merged record if it is different from the original record, or if we are retaining all bibs
                    if (outputAllBibs == true || !removedField || !bibRecWithoutChanges.toString().equals(bibRecWithChanges.toString()))
                    {
                        writer.write(bibRecWithChanges);
                        System.out.flush();
                    }
                }
                else if (outputAllBibs == true)
                {
                    System.out.write(rawBibRecCurrent.getRecordBytes());
                    System.out.flush();
                }
            }
            catch (IOException e) 
            {
                System.err.println("Error writing record " + rawBibRecCurrent.getRecordId());
            }
        }
    }
}
