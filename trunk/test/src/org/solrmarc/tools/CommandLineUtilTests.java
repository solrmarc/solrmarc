package org.solrmarc.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.solrmarc.testUtils.CommandLineUtils;


public class CommandLineUtilTests
{
    /**
     * unit test for MergeSummaryHoldings and PrintWriter
     */
    @Test
    public void testMergeSummaryHoldings()
    {
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
            "852   $bALDERMAN$cALD-STKS$xpat#169090$x2x$xbind 4N=2 or 3yrs$xex.:  Nr. 15-18 1988-93$xindex ?$xuse copyright year for dating$zCURRENT ISSUES HELD IN THE PERIODICALS ROOM $=5071",
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

        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFile = System.getProperty("test.config.file");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.MergeSummaryHoldings", "main", null, out1, new String[]{"-s", testDataParentPath+"/summaryHld_1-1000.mrc", testDataParentPath+"/u335.mrc" });

        ByteArrayInputStream in2 = new ByteArrayInputStream(out1.toByteArray());
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in2, out2, new String[]{testConfigFile, "print"}); 

        CommandLineUtils.compareUtilOutput(new ByteArrayInputStream(out2.toByteArray()), mergedSummaryHoldingsOutput); 

        // Now merge record again to test the deleting of existing summary holdings info

        ByteArrayInputStream in3 = new ByteArrayInputStream(out1.toByteArray());
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.MergeSummaryHoldings", "main", in3, out3, new String[]{"-s", testDataParentPath+"/summaryHld_1-1000.mrc" } );
        
        
        ByteArrayInputStream in4 = new ByteArrayInputStream(out3.toByteArray());
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in4, out4, new String[]{testConfigFile, "print"}); 

        CommandLineUtils.compareUtilOutput(new ByteArrayInputStream(out4.toByteArray()), mergedSummaryHoldingsOutput); 
        System.out.println("Test testMergeSummaryHoldings is successful");
    }
    
    /**
     * unit test for MarcDiff
     */
    @Test
    public void testMarcDiff()
    {
        String marcDiffOutput[] ={
            "record with id: u2103 different in file1 and file2",
            " < LEADER 01584cam a2200397 a 4500",
            " > LEADER 01660cam a2200397 a 4500",
            " < 610 20$aForbidden City (Beijing, China)",
            " > 650  0$aPalaces$zChina$zBeijing.",
            " < 650  0$aPalaces$zChina$zBeijing.",
            " > 610 20$aForbidden City (Beijing, China)",
            " < 999   $aNA1547 .P6 T9 1982$wLC$c1$iX000815679$d5/19/2008$e4/9/2008$lFA-OVERSZE$mFINE-ARTS$n16$q1$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            " > 999   $aNA1547 .P6 T9 1982$wLC$c1$iX000815679$d5/19/2008$e4/9/2008$lFA-OVERSZE$mFINE-ARTS$n16$q1$rY$sY$tBOOK$u6/28/1996$xH-NOTIS$o.STAFF.  *arts"
        };
                                     
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFile = System.getProperty("test.config.file");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.MarcDiff", "main", null, out1, new String[]{testDataParentPath+"/u2103.mrc", testDataParentPath+"/u2103_raw.mrc" });

        CommandLineUtils.compareUtilOutput(new ByteArrayInputStream(out1.toByteArray()), marcDiffOutput);
        System.out.println("Test testMarcDiff is successful");
    }

    /**
     * unit test for MarcMerger
     */
    @Test
    public void testMarcMerger()
    {
        String marcMergerOutput1[] ={
            "001 u1",
            "001 u2",
            "001 u3",
            "001 u6",
            "001 u7",
            "001 u8",
            "001 u11",
            "001 u13"
        };
        String marcMergerOutput2[] ={
            "001 u1",
            "001 u2",
            "001 u3",
            "001 u6",
            "001 u7",
            "001 u8",
            "001 u11",
            "001 u13",
            "001 u15"
        };
        String marcMergerOutput3[] ={
            "u4", "u5", "u10", "u13", "u15" };
        
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFile = System.getProperty("test.config.file");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcMerger", "main", null, out1, new String[]{"-min", "u0", "-max", "u14", testDataParentPath+"/mergeInput.mrc", testDataParentPath+"/mergeMod.mrc" });
        
        ByteArrayInputStream in2 = new ByteArrayInputStream(out1.toByteArray());
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in2, out2, new String[]{testConfigFile, "print", "001"}); 
        CommandLineUtils.compareUtilOutput(new ByteArrayInputStream(out2.toByteArray()), marcMergerOutput1); 
        
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcMerger", "main", null, out3, new String[]{testDataParentPath+"/mergeInput.mrc", testDataParentPath+"/mergeMod.mrc" });
        
        ByteArrayInputStream in4 = new ByteArrayInputStream(out3.toByteArray());
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in4, out4, new String[]{testConfigFile, "print", "001"}); 
        CommandLineUtils.compareUtilOutput(new ByteArrayInputStream(out4.toByteArray()), marcMergerOutput2); 

        ByteArrayOutputStream out5 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcMerger", "main", null, out5, new String[]{testDataParentPath+"/mergeMod.del", testDataParentPath+"/mergeMod.mrc" });
        
        CommandLineUtils.compareUtilOutput(new ByteArrayInputStream(out5.toByteArray()), marcMergerOutput3); 
        System.out.println("Test testMarcMerger is successful");
    }

    /**
     * unit test for MarcSort and PrintWriter
     */
    @Test
    public void testMarcSort()
    {
        String marcSorterOutput[] = {
           "001 u18488",
           "001 u740162",
           "001 u1563789",
           "001 u2020731"
        };
        
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFile = System.getProperty("test.config.file");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcSorter", "main", null, out1, new String[]{testDataParentPath+"/url_test_recs.mrc" });
        
        ByteArrayInputStream in2 = new ByteArrayInputStream(out1.toByteArray());
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in2, out2, new String[]{testConfigFile, "print", "001"}); 

        CommandLineUtils.compareUtilOutput(new ByteArrayInputStream(out2.toByteArray()), marcSorterOutput); 
        System.out.println("Test testMarcSort is successful");
    }
    
    /**
     * unit test for MarcSort and PrintWriter
     */
    @Test
    public void testMarcPatcher()
    {
        String marcPatcherOutput[] ={
            "001 u13",
            "999   $aHX44 .S5887 1981$wLC$iX000968615$lCLEM-STKS$mCLEMONS$tBOOK",
            "001 u55",
            "999   $aPR4231 .A43 1984 v.1$wMONO-SER$c1$iX000786425$d4/25/2006$e4/13/2006$lALD-STKS$mALDERMAN$n4$q1$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.2$wMONO-SER$c1$iX000786426$d4/25/2006$e4/13/2006$lALD-STKS$mALDERMAN$n3$q1$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR 4231 .A43 1984 v.11$wMONO-SER$c1$iX002443781$d6/4/2002$lALD-STKS$mALDERMAN$q2$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.3$wMONO-SER$c1$iX002451975$d5/29/2002$e5/24/2002$lALD-STKS$mALDERMAN$n3$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.4$wMONO-SER$c1$iX002452094$d12/6/2008$e10/13/2008$lALD-STKS$mALDERMAN$n4$q1$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.6$wMONO-SER$c1$iX002452034$d11/30/2005$e11/16/2005$lALD-STKS$mALDERMAN$n3$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.7$wMONO-SER$c1$iX002452026$d12/13/2005$e12/6/2005$lALD-STKS$mALDERMAN$n2$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.8$wMONO-SER$c1$iX002452024$d2/10/1997$lALD-STKS$mALDERMAN$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.9$wMONO-SER$c1$iX002452042$d2/10/1997$lALD-STKS$mALDERMAN$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.10$wMONO-SER$c1$iX002452009$d4/15/2004$e4/15/2004$lALD-STKS$mALDERMAN$n2$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.12$wMONO-SER$c1$iX002523300$d6/4/2002$e4/13/2000$lALD-STKS$mALDERMAN$n1$q1$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.1$wMONO-SER$c1$iX000907389$d8/20/2001$e8/24/1996$lCLEM-STKS$mCLEMONS$n3$rM$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.1$wMONO-SER$c2$iX004415427$d11/12/2002$lWITHDRAWN$mCLEMONS$q1$rY$sY$tBOOK$u2/16/1998$xADD",
            "999   $aPR4231 .A43 1984 v.2$wMONO-SER$c1$iX000907388$d2/10/1997$lWITHDRAWN$mCLEMONS$n1$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "999   $aPR4231 .A43 1984 v.13$wMONO-SER$c1$iX004045629$d4/15/2004$e4/15/2004$lALD-STKS$mALDERMAN$n2$q1$rY$sY$tBOOK$u11/18/1996",
            "999   $aPR4231 .A43 1984 v.5$wMONO-SER$iX002451978$lALD-STKS$mALDERMAN$tBOOK",
            "001 u144",
            "999   $aHD6665 .A73 A37 1984$wLC$c1$iX000870129$d6/28/1996$lDARD-STKS$mDARDEN$p$50.00$rY$sY$tBOOK$u6/28/1996$xH-NOTIS",
            "001 u377",
            "999   $aBX1644 .H682 1981$wLC$c1$iX000842719$d6/28/1996$kCHECKEDOUT$lALD-STKS$mALDERMAN$rY$sY$tBOOK$u6/28/1996$xH-NOTIS"
        };
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFile = System.getProperty("test.config.file");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPatcher", "main", null, out1, new String[]{testDataParentPath+"/selectedRecs.mrc", testDataParentPath+"/location_sample.txt", "handleAllLocs" });
        
        ByteArrayInputStream in2 = new ByteArrayInputStream(out1.toByteArray());
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in2, out2, new String[]{testConfigFile, "print", "001|999"}); 

        CommandLineUtils.compareUtilOutput(new ByteArrayInputStream(out2.toByteArray()), marcPatcherOutput); 
        System.out.println("Test testMarcPatcher is successful");
    }
   
    /**
     * unit test for other modes of MarcPrinter
     */
    @Test
    public void testMarcPrinter()
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFile = System.getProperty("test.config.file");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", null, out1, new String[]{testConfigFile, testDataParentPath+"/u4.mrc", "to_xml" });
        CommandLineUtils.compareUtilOutputLine(new ByteArrayInputStream(out1.toByteArray()), "    <leader>01218cam a2200313 a 4500</leader>", 2); 
        CommandLineUtils.compareUtilOutputLine(new ByteArrayInputStream(out1.toByteArray()), "      <subfield code=\"a\">The princes of Hà-tiên (1682-1867) /</subfield>", 39); 
//        System.out.println("Test1 testMarcPrinter is successful");
        
        ByteArrayInputStream in2 = new ByteArrayInputStream(out1.toByteArray());
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in2, out2, new String[]{testConfigFile, "translate"}); 

        ByteArrayInputStream in3 = new ByteArrayInputStream(out2.toByteArray());
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in3, out3, new String[]{testConfigFile, "print"}); 

        CommandLineUtils.compareUtilOutputLine(new ByteArrayInputStream(out3.toByteArray()), "LEADER 01222cam a2200313 a 4500", 0); 
        CommandLineUtils.compareUtilOutputLine(new ByteArrayInputStream(out3.toByteArray()), "245 14$aThe princes of Hà-tiên (1682-1867) /$cNicholas Sellers.", 13); 
//        System.out.println("Test2 testMarcPrinter is successful");

        ByteArrayInputStream in4 = new ByteArrayInputStream(out2.toByteArray());
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in4, out4, new String[]{testConfigFile, "untranslate"}); 

        File origfile = new File(testDataParentPath+"/u4.mrc");
        ByteArrayOutputStream outOrig = null;
        try
        {
            BufferedInputStream origFileStream  = new BufferedInputStream(new FileInputStream(origfile));
            outOrig = new ByteArrayOutputStream();
            int byteread;
            while ((byteread = origFileStream.read()) != -1)
            {
                outOrig.write(byteread);
            }
        }
        catch (FileNotFoundException e)
        {
            fail("Error opening file: "+ testDataParentPath+"/u4.mrc");
        }
        catch (IOException e)
        {
            fail("Error reading from file: "+ testDataParentPath+"/u4.mrc");
        }
        CommandLineUtils.assertArrayEquals("original MARC8 record, and roundtripped MARC8 record ", out4.toByteArray(), outOrig.toByteArray()); 
//        System.out.println("Test3 testMarcPrinter is successful");

        ByteArrayInputStream in6 = new ByteArrayInputStream(out4.toByteArray());
        ByteArrayOutputStream out6 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in6, out6, new String[]{testConfigFile, "index", "title_display"}); 
//        System.out.println("Test3 testConfigFile is "+testConfigFile);
//        try
//        {
//            System.out.println("output is "+new String(out6.toByteArray(), "UTF-8"));
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        CommandLineUtils.compareUtilOutputLine(new ByteArrayInputStream(out6.toByteArray()), "u4 : title_display = The princes of Hà-tiên (1682-1867)", 0); 
        System.out.println("Test testMarcPrinter is successful");
    }
    
    /**
     * unit test for GetRecord and FilterRecord
     */
    @Test
    public void testGetRecord()
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFile = System.getProperty("test.config.file");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.RawRecordReader", "main", null, out1, new String[]{testDataParentPath+"/selectedRecs.mrc", "u8" });

        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.RawRecordReader", "main", null, out2, new String[]{testDataParentPath+"/selectedRecs.mrc", testDataParentPath+"/getrecord_select_u8.txt" });

        CommandLineUtils.assertArrayEquals("record via GetRecord with parm, and record via GetRecord with selection file ", out1.toByteArray(), out2.toByteArray()); 

        System.setProperty("marc.include_if_present", "500a/Tamil");
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", null, out3, new String[]{testConfigFile, testDataParentPath+"/selectedRecs.mrc", "untranslate" });
        System.clearProperty("marc.include_if_present");

        CommandLineUtils.assertArrayEquals("record via GetRecord, and record via FilterRecord ", out1.toByteArray(), out3.toByteArray()); 
        System.out.println("Test testGetRecord is successful");
    }
    
    /**
     * unit test for MarcImporter and SolrReindexer
     */
    @Test
    public void testIndexRecord()
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFile = System.getProperty("test.config.file");
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
            fail("property solr.path must be defined for the tests to run");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        if (testConfigFile == null)
            fail("property test.config.file be defined for this test to run");

        // index a small set of records
        System.setProperty("solrmarc.use_solr_server_proxy", "true");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr1 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.marc.MarcImporter", "main", null, out1, outErr1, new String[]{testConfigFile, testDataParentPath+"/mergeInput.mrc"  });
        System.clearProperty("solrmarc.use_solr_server_proxy");

        // retrieve record u3 from the index
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.marc.SolrReIndexer", "main", null, out2, outErr2, new String[]{testConfigFile, "id:u3", "marc_display"});
        
        // retrieve record u3 from the original input file
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.RawRecordReader", "main", null, out3, new String[]{testDataParentPath+"/mergeInput.mrc", "u3" });
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(raw), and record via GetRecord ", out2.toByteArray(), out3.toByteArray()); 
        
        // retrieve record u3 from the index (from marc_xml_display field) 
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr4 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.marc.SolrReIndexer", "main", null, out4, outErr4, new String[]{testConfigFile, "id:u3", "marc_xml_display"});
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(xml) through filter, and record via GetRecord ", out4.toByteArray(), out3.toByteArray()); 

        // index some more records as well as delete some
        ByteArrayOutputStream out6 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr6 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.marc.MarcImporter", "main", null, out6, outErr6, new String[]{testConfigFile, testDataParentPath+"/mergeMod.mrc", testDataParentPath+"/mergeMod.del"  });

        // now check that record u4 isn't present
        ByteArrayOutputStream out7 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr7 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.marc.SolrReIndexer", "main", null, out7, outErr7, new String[]{testConfigFile, "id:u4", "marc_display"});

        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr, and empty record ", out7.toByteArray(), new byte[0]); 
        
        // lastly check that the entire contents of index (don't try this at home)
        ByteArrayOutputStream out8 = new ByteArrayOutputStream();
        ByteArrayOutputStream outErr8 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil2("org.solrmarc.marc.SolrReIndexer", "main", null, out8, outErr8, new String[]{testConfigFile, "id:u*", "marc_display"});
        
        // sort the records returned
        ByteArrayInputStream in9 = new ByteArrayInputStream(out8.toByteArray());
        ByteArrayOutputStream out9 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcSorter", "main", in9, out9, new String[]{"-" });

        ByteArrayOutputStream out10 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcMerger", "main", null, out10, new String[]{testDataParentPath+"/mergeInput.mrc", testDataParentPath+"/mergeMod.mrc" });

        // compare the results
        CommandLineUtils.assertArrayEquals("all records via GetFromSolr, all record via MarcMerger ", out9.toByteArray(), out10.toByteArray()); 

        System.out.println("Test testIndexRecord is successful");
    }
    

                                   
}
