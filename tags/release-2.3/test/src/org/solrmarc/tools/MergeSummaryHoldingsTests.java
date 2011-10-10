package org.solrmarc.tools;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;
import org.marc4j.marc.*;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.testUtils.CommandLineUtils;
import org.solrmarc.testUtils.RecordTestingUtils;

/**
 * Note that actual use of MergeSummaryHoldings is a call to main() from a 
 *  shell script, so these tests must use the CommandLineUtils
 * @author Naomi Dushay
 *
 */
public class MergeSummaryHoldingsTests
{
    static String testDir = "test";

    static String testDataParentPath = System.getProperty("test.data.path");
    static String testConfigFname = System.getProperty("test.config.file");
    {
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
//      static String testDataParentPath =  testDir + File.separator + "data";
        if (testConfigFname == null)
            fail("property test.config.file must be defined for the tests to run");
//      static String testConfigFile = smokeTestDir + File.separator + "test_config.properties";
    }

    static String smokeTestDir = testDataParentPath + File.separator + "smoketest";

    
    
    static String MERGE_MHLD_CLASS_NAME = "org.solrmarc.tools.MergeSummaryHoldings";
    static String MARC_PRINTER_CLASS_NAME = "org.solrmarc.marc.MarcPrinter";
    static String MAIN_METHOD_NAME = "main";
    
    // for vetting results - no point in loading these constants for each test
    static Map<String, Record> ALL_MERGED_BIB_RESULTS = new HashMap<String, Record>();
    static Map<String, Record> ALL_UNMERGED_BIBS = new HashMap<String, Record>();
    static
    {
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs1346.mrc";
        try {
            RawRecordReader rawRecRdr = new RawRecordReader(new FileInputStream(new File(bibFilePath)));
            while (rawRecRdr.hasNext())
            {
                RawRecord rawRec = rawRecRdr.next();
                Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
                String id = rec.getControlNumber();
                // String id = RecordTestingUtils.getRecordIdFrom001(rec);
                ALL_UNMERGED_BIBS.put(id, rec);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        bibFilePath = testDataParentPath + File.separator + "mhldMerged1346.mrc";
        try {
            RawRecordReader rawRecRdr = new RawRecordReader(new FileInputStream(new File(bibFilePath)));
            while (rawRecRdr.hasNext())
            {
                RawRecord rawRec = rawRecRdr.next();
                Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
                String id = rec.getControlNumber();
                // String id = RecordTestingUtils.getRecordIdFrom001(rec);
                ALL_MERGED_BIB_RESULTS.put(id, rec);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    

@Before
    public void setUp()
    {
        if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
        {
            java.util.logging.Logger.getLogger("org.apache.solr").setLevel(java.util.logging.Level.SEVERE);
            Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
        }
        testDataParentPath = System.getProperty("test.data.path");
        testConfigFname = System.getProperty("test.config.file");
        //System.out.println("-----testDataParentPath = "+testDataParentPath);
    }
        
    /**
     * code should output the unchanged bib records if no mhlds match
     */
@Test
    public void testNoMatches() 
            throws IOException 
    {
        // bib46, mhld235
        System.out.println("Starting testNoMatches()");
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs46.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds235.mrc";
        System.out.println("processing testNoMatches()");
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibFilePath, mhldFilePath);
        Set<String> mergedRecIds = mergedRecs.keySet();
        System.out.println("mergedRecIds.size() = "+mergedRecIds.size());
        assertEquals(2, mergedRecIds.size());
        System.out.println("processing2 testNoMatches()");

        // result bibs should match the bib input because there was no merge
        String id = "a4";
        RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
        System.out.println("processing2 testNoMatches()");
        id = "a6";
        RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
        System.out.println("Finishing testNoMatches()");

    }
    
    /**
     * code should end smoothly if it encounters no matches between bib and mhld
     */
@Test
    public void testNoOutputMessagesWhenNoMatches() 
            throws IOException 
    {
        // bib46, mhld235
        System.out.println("Starting testNoOutputMessagesWhenNoMatches()");
     //   String commandLinePathPrefix = ".." + File.separator + ".." + File.separator;
        String bibFilePath =  testDataParentPath + File.separator + "mhldMergeBibs46.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds235.mrc";
    
        // ensure no error message was printed
        ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
    //    PrintStream sysMsgs = new PrintStream(sysBAOS);
    //    System.setErr(sysMsgs);
    //    System.setOut(sysMsgs);
    
        mergeBibAndMhldFiles(bibFilePath, mhldFilePath, sysBAOS);
    
        // ensure no error message was printed
        assertTrue("Output messages unexpectedly written: " + sysBAOS.toString(),  sysBAOS.size() == 0);
    //  System.setOut(System.out);
    //  System.setErr(System.err);
        System.out.println("Finishing testNoOutputMessagesWhenNoMatches()");
    }


// first record in file tests ----------    
    /**
     * code should find a match when first bib matches first mhld
     */
@Test
    public void testBothFirstRecsMatch() 
            throws IOException 
    {
        // bib346, mhld34
        System.out.println("Starting testBothFirstRecsMatch()");
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs346.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds34.mrc";
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibFilePath, mhldFilePath);
        
        // there should be 3 results
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());

        // result bibs 3, 4 should have the mhld fields
        String id = "a3";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
        id = "a4";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
        // result bib 6 should not be changed
        id = "a6";
        RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
        System.out.println("Finishing testBothFirstRecsMatch()");
    }

    /**
     * code should find a match when first bib matches non-first mhld
     */
@Test
    public void testFirstBibMatchesNonFirstMhld() 
            throws IOException
    {
        //bib346, mhld235
        System.out.println("Starting testFirstBibMatchesNonFirstMhld()");
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs346.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds235.mrc";
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibFilePath, mhldFilePath);
        
        // there should be 3 results
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result bib 3 only should have the mhld fields
        String id = "a3";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
           // result bibs 4 and 6 should not be changed
        id = "a4";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
        id = "a6";
        RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
        System.out.println("Finishing testFirstBibMatchesNonFirstMhld()");
    }

    /**
     * code should find a match when non-first bib matches first mhld
     */
@Test
    public void testNonFirstBibMatchesFirstMhld() 
            throws IOException
    {
        //bib134, mhld345
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs134.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds345.mrc";
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibFilePath, mhldFilePath);
        
        // there should be 3 results
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result bibs 3 and 4 only should have the mhld fields
        String id = "a1";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
        id = "a3";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
        id = "a4";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
    }

// last record in file tests ------------
    /**
     * code should find a match when last bib matches last mhld
     */
@Test
    public void testBothLastRecsMatch() 
            throws IOException
    {
        //bib46, mhld236
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs46.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds236.mrc";
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibFilePath, mhldFilePath);
        
        // there should be 2 results
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(2, mergedRecIds.size());
    
        // result bib 6 only should have the mhld fields
        String id = "a4";
           RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
        id = "a6";
           RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
    }

    /**
     * code should find a match when last bib matches non-last mhld
     */
@Test
    public void testLastBibMatchesNonLastMhld() 
        throws IOException
    {
        //bib134, mhld345
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs134.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds345.mrc";
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibFilePath, mhldFilePath);
        
        // there should be 3 results
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result bibs 3 and 4 only should have the mhld fields
        String id = "a1";
           RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
        id = "a3";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
        id = "a4";
        RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
    }

    /**
     * code should find a match when non-last bib matches last mhld
     */
@Test
    public void testNonLastBibMatchesLastMhld() 
            throws IOException
    {
        //bib46, mhld34
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs46.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds34.mrc";
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibFilePath, mhldFilePath);
        
        // there should be 2 results
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(2, mergedRecIds.size());
    
        // result bib 6 only should have the mhld fields
        String id = "a4";
           RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
        id = "a6";
           RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
    }

    
    /**
     * need to ensure all the MHLD data is included, not just the first record
     */
// FIXME: fails!  needs MarcCombiningReader for mhld or at least a diff version of RawRecordReader
@Test
    public void testMultMHLDsWithSameID()
            throws IOException
    {
        //bib134, multMhlds1        
        String bibFilePath = testDataParentPath + File.separator + "mhldMergeBibs134.mrc";
        String mhldFilePath = testDataParentPath + File.separator + "mhldMergeMhlds1Mult.mrc";
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibFilePath, mhldFilePath);
        
        Record mergedRec = mergedRecs.get("a1");
        assertEquals("Expected three 852", 3, mergedRec.getVariableFields("852").size());
        Set<String> expectedVals = new HashSet<String>();
        expectedVals.add("Location1");
        expectedVals.add("Location2");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "852", 'b', expectedVals);

        expectedVals.clear();
        expectedVals.add("(month)");
        expectedVals.add("(season)");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "853", 'b', expectedVals);
        
        assertEquals("Expected one 863", 2, mergedRec.getVariableFields("863").size());
        assertEquals("Expected one 866", 1, mergedRec.getVariableFields("866").size());
        //fail("Implement me");
    }
    
    /**
     * the MHLD fields should only be merged into ONE of the bibs, if the bibs will be combined?
     * Or it's probably ok if they are in each bib, as they should be removed from the bib after processing?
     */
//@Test
    public void testMultBibsWithSameID() 
    {
        // multBibs4, mhld 34
        fail("Implement me");
    }
    
    /**
     * need to ensure all the MHLD data is included, not just the first record
     */
    public void testMultBothWithSameID() 
    {
        
    }
    
    
    
    /**
     * the bib record should only get the fields specified
     */
    public void testFieldsToMerge() 
    {
    }

    /**
     * the bib record should not get any MHLD fields that aren't indicated for the merge
     */
    public void testFieldsNotToMerge() 
    {
    }
    
    /**
     * if the MHLD has more than one instance of a field, all instances should be put in the bib record
     */
    public void testMultOccurFieldsToMerge() 
    {
    }
    
    
    /**
     * if the bib rec has existing MHLD fields (not from another MHLD record?) then it should
     * remove them before adding the MHLD fields
     */
//@Test
    public void testCrashingBibFieldsRemoved() 
    {
        //bibWmhldFlds, completeMhld
        fail("implement me");
    }
    

// Tests for very basic functionality of code, including Bob's original test (with some modifications to run as a more typical junit test)    

String mergedSummaryHoldingsOutput[] = {
        "LEADER 02429nas a2200481 a 4500",
        "001 u335",
        "003 SIRSI",
        "008 840508c19799999gw fu p       0uuub0ger d",
        "035   $a(Sirsi) o10701458",
        "035   $a(OCoLC)10701458",
        "040   $aVA@$cVA@",
        "049   $aVAS@",
        "090   $aAP30$b.T75$mVAS@$qALDERMAN",
        "245 00$aTumult.",
        "246 13$aZeitschrift für Verkehrswissenschaft",
        "260   $aBerlin :$bMerve Verlag,$c1979-",
        "300   $av. :$bill. ;$c24 cm.",
        "310   $aSemiannual",
        "362 0 $a1-",
        "500   $aTitle from cover; imprint varies.",
        "599   $a2$b(YR.) 2008 NO. 34;$b(YR.) 2008 NO. 33;$bNR. 32 2007;",
        "596   $a2",
        "515   $aNone published 1980-1981.",
        "852   $bALDERMAN$cALD-STKS$xpat#169090$x2x$xbind 4N=2 or 3yrs$xex.:  Nr. 15-18 1988-93$xindex ?$xuse copyright year for dating$zCURRENT ISSUES HELD IN THE PERIODICALS ROOM $x5071",
        "853 2 $82$anr.",
        "853 2 $83$anr.$i(year)$j(unit)",
        "853 2 $84$a(yr.)$bno.$u2$vc",
        "866  0$81$aNr.1-28  (1979-2004)$zIn stacks",
        "863  1$83.6$a29$i2005$j.",
        "863  1$83.7$a30$i2005$j.",
        "863  1$83.8$a31$i2006$j.",
        "863  1$83.9$a32$i2007",
        "863  1$84.1$a2008$b33",
        "863  1$84.2$a2008$b34",
        "999   $aAP30 .T75 Nr.7-10 1983-87$wLCPER$c1$iX001614137$d5/9/2008$lALD-STKS$mALDERMAN$n2$q3$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.1-3 1979-82$wLCPER$c1$iX000769605$d4/8/2009$lALD-STKS$mALDERMAN$q2$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.4-6 1982-83$wLCPER$c1$iX000764174$d5/21/2002$lALD-STKS$mALDERMAN$q5$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.11-14 1988-90$wLCPER$c1$iX002128357$d1/27/2010$lALD-STKS$mALDERMAN$n1$q1$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.15-18 1991-93$wLCPER$c1$iX002509913$d11/11/1994$lALD-STKS$mALDERMAN$n1$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Periodical order-001$wLCPER$c1$i335-6001$d1/11/1999$lALD-STKS$mALDERMAN$rY$sY$tBOUND-JRNL$u12/18/1996",
        "999   $aAP30 .T75 Nr.19-22 1994-96$wLCPER$c1$iX006060933$d7/23/1998$e5/26/1998$lALD-STKS$mALDERMAN$n1$rY$sY$tBOUND-JRNL$u5/26/1998$xADD",
        "999   $aAP30 .T75 Nr.25-28 2001-2004$wLCPER$c1$iX030047292$d2/12/2007$e1/23/2007$lALD-STKS$mALDERMAN$q1$rY$sY$tBOUND-JRNL$u1/22/2007$xADD",
        "999   $aAP30 .T75 Nr.23-24 1998-1999$wLCPER$c1$iX006166304$d4/5/2007$e3/13/2007$lALD-STKS$mALDERMAN$rY$sY$tBOUND-JRNL$u3/12/2007$xADD",
        };

String mergedSummaryHoldingsOutputNoUmlaut[] = {
        "LEADER 02429nas a2200481 a 4500",
        "001 u335",
        "003 SIRSI",
        "008 840508c19799999gw fu p       0uuub0ger d",
        "035   $a(Sirsi) o10701458",
        "035   $a(OCoLC)10701458",
        "040   $aVA@$cVA@",
        "049   $aVAS@",
        "090   $aAP30$b.T75$mVAS@$qALDERMAN",
        "245 00$aTumult.",
        "246 13$aZeitschrift fèur Verkehrswissenschaft",
        "260   $aBerlin :$bMerve Verlag,$c1979-",
        "300   $av. :$bill. ;$c24 cm.",
        "310   $aSemiannual",
        "362 0 $a1-",
        "500   $aTitle from cover; imprint varies.",
        "599   $a2$b(YR.) 2008 NO. 34;$b(YR.) 2008 NO. 33;$bNR. 32 2007;",
        "596   $a2",
        "515   $aNone published 1980-1981.",
        "852   $bALDERMAN$cALD-STKS$xpat#169090$x2x$xbind 4N=2 or 3yrs$xex.:  Nr. 15-18 1988-93$xindex ?$xuse copyright year for dating$zCURRENT ISSUES HELD IN THE PERIODICALS ROOM $x5071",
        "853 2 $82$anr.",
        "853 2 $83$anr.$i(year)$j(unit)",
        "853 2 $84$a(yr.)$bno.$u2$vc",
        "866  0$81$aNr.1-28  (1979-2004)$zIn stacks",
        "863  1$83.6$a29$i2005$j.",
        "863  1$83.7$a30$i2005$j.",
        "863  1$83.8$a31$i2006$j.",
        "863  1$83.9$a32$i2007",
        "863  1$84.1$a2008$b33",
        "863  1$84.2$a2008$b34",
        "999   $aAP30 .T75 Nr.7-10 1983-87$wLCPER$c1$iX001614137$d5/9/2008$lALD-STKS$mALDERMAN$n2$q3$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.1-3 1979-82$wLCPER$c1$iX000769605$d4/8/2009$lALD-STKS$mALDERMAN$q2$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.4-6 1982-83$wLCPER$c1$iX000764174$d5/21/2002$lALD-STKS$mALDERMAN$q5$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.11-14 1988-90$wLCPER$c1$iX002128357$d1/27/2010$lALD-STKS$mALDERMAN$n1$q1$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.15-18 1991-93$wLCPER$c1$iX002509913$d11/11/1994$lALD-STKS$mALDERMAN$n1$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Periodical order-001$wLCPER$c1$i335-6001$d1/11/1999$lALD-STKS$mALDERMAN$rY$sY$tBOUND-JRNL$u12/18/1996",
        "999   $aAP30 .T75 Nr.19-22 1994-96$wLCPER$c1$iX006060933$d7/23/1998$e5/26/1998$lALD-STKS$mALDERMAN$n1$rY$sY$tBOUND-JRNL$u5/26/1998$xADD",
        "999   $aAP30 .T75 Nr.25-28 2001-2004$wLCPER$c1$iX030047292$d2/12/2007$e1/23/2007$lALD-STKS$mALDERMAN$q1$rY$sY$tBOUND-JRNL$u1/22/2007$xADD",
        "999   $aAP30 .T75 Nr.23-24 1998-1999$wLCPER$c1$iX006166304$d4/5/2007$e3/13/2007$lALD-STKS$mALDERMAN$rY$sY$tBOUND-JRNL$u3/12/2007$xADD",
        };

    /**
     * This is Bob's original test, re-written only to allow it to execute as
     * a normal junit test within Eclipse.
     */
@Test
    public void origTestOfRewritingMHLDtoSameBib() 
            throws IOException
    {
        String mhldRecFileName = testDataParentPath + File.separator + "summaryHld_1-1000.mrc";
        String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";
    
        InputStream inStr = null;
        ByteArrayOutputStream resultMrcOutStream = new ByteArrayOutputStream();
        String[] mergeMhldArgs = new String[]{"-s", mhldRecFileName, bibRecFileName };
    
        // call the code for mhldfile summaryHld_1-1000.mrc  and bibfile u335.mrc
        CommandLineUtils.runCommandLineUtil(MERGE_MHLD_CLASS_NAME, MAIN_METHOD_NAME, inStr, resultMrcOutStream, mergeMhldArgs);
    
        RecordTestingUtils.assertMarcRecsEqual(mergedSummaryHoldingsOutput, resultMrcOutStream);
        
        // Now merge record again to test the deleting of existing summary holdings info
        ByteArrayInputStream mergedMarcBibRecAsInStream = new ByteArrayInputStream(resultMrcOutStream.toByteArray());
        resultMrcOutStream.close();
        resultMrcOutStream = new ByteArrayOutputStream();
        //  do the merge by piping the bib record in to the merge class
        CommandLineUtils.runCommandLineUtil(MERGE_MHLD_CLASS_NAME, MAIN_METHOD_NAME, mergedMarcBibRecAsInStream, resultMrcOutStream, new String[]{"-s", mhldRecFileName } );
        
        RecordTestingUtils.assertMarcRecsEqual(mergedSummaryHoldingsOutput, resultMrcOutStream);
    }


    /**
     * test methods that return Map of ids to Records and no sysout stuff
     */
@Test
    public void testGettingOutputAsMapOfRecords() 
            throws IOException
    {
        String mhldRecFileName = testDataParentPath + File.separator + "summaryHld_1-1000.mrc";
        String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";
    
        Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibRecFileName, mhldRecFileName);

        junit.framework.Assert.assertEquals("results should have 1 record", 1, mergedRecs.size());
        String expId = "u335";
        assertTrue("Record with id " + expId + " should be in results", mergedRecs.containsKey(expId));
        
        Record resultRec = mergedRecs.get(expId);
        RecordTestingUtils.assertEqualsIgnoreLeader(mergedSummaryHoldingsOutputNoUmlaut, resultRec);        
    }


    /**
     * Test if using Naomi's approach with next() works as well as weird way of duplicating code
     */
@Test
    public void testMergeToStdOut2() 
            throws IOException
    {
        String mhldRecFileName = testDataParentPath + File.separator + "summaryHld_1-1000.mrc";
        String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";
    
        ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
        PrintStream sysMsgs = new PrintStream(sysBAOS);
        System.setOut(sysMsgs);

        MergeSummaryHoldings.mergeMhldRecsIntoBibRecsAsStdOut2(bibRecFileName, mhldRecFileName);
    
        RecordTestingUtils.assertMarcRecsEqual(mergedSummaryHoldingsOutput, sysBAOS);
    }


// supporting methods for testing ----------------------------------------------

    /**
     * 
     * @param bibRecsFileName name of the file containing Bib records, relative to the testDataParentPath
     * @param mhldRecsFileName name of the file containing MHLD records, relative to the testDataParentPath
     * @param outStream name of the a ByteArrayOutputStream to be used for output from running the command
     * @return the resulting merged bib file as a ByteArrayOutputStream
     */
    private ByteArrayOutputStream mergeBibAndMhldFiles(String bibRecsFileName, String mhldRecsFileName, ByteArrayOutputStream outStream) 
    {
    //    String fullBibRecsFileName = testDataParentPath + File.separator + bibRecsFileName;
    //   String fullMhldRecsFileName = testDataParentPath + File.separator + mhldRecsFileName;

        InputStream inStr = null;
    //    ByteArrayOutputStream resultMrcOutStream = new ByteArrayOutputStream();
        String[] mergeMhldArgs = new String[]{"-s", bibRecsFileName, mhldRecsFileName };

        // call the MergeSummaryHoldings code from the command line
        CommandLineUtils.runCommandLineUtil(MERGE_MHLD_CLASS_NAME, MAIN_METHOD_NAME, inStr, outStream, mergeMhldArgs);
        return outStream;
    }
    
}
