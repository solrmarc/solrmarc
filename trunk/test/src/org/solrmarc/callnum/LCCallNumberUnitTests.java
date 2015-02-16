package org.solrmarc.callnum;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialStruct;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests for <code>LCCallNumber</code>.
 *
 * Initially copied from on Naomi Dushay's unit tests for <code>CallNumberUnitTests</code>.
 *
 * @author Naomi Dushay, Stanford University
 * @author Tod Olson, University of Chicago
 *
 */
public class LCCallNumberUnitTests {

    // list of raw call numbers NOT in order to check sorting
    String[] diabolicalCallnumList = {
        "B8.14 L3",
        "B9 L3",
        "B9 2000 L3",
        "B9 SVAR .L3",
        "B9 20th L3",
        "B9 2000 35TH",
        "B9 2000",
        "B9.2 L3",
        "B9.2 2000 L3",
        "B9.22 L3",

        "B82 L3",
        "B82 2000 L3",
        "B82.2 L3",
        "B82.2 1990 L3",

        "B822 L3",
        "B820 2000 L3",
        "B820.2 L3",
        "B820.2 2000 L3",
        "B8220 L3",
        "B8200 L3",

        "AB9 L3",
        "AB9.22 L3",
        "ABR92.L3",

        "M5 M2",
        "M5 K4",
        "M5 L299",
        "M5 .L",
        "M5 L31",
        "M5 L31902",
        "M5 L3 1902",
        "M5 L3 1902 V2",
        "M5 .L3 1902 V2 TANEYTOWN",
        "M5 L3 1902V",
        "M5 L3 1902 V.2",
        "M5 L3 K.240 1900",
        "M5 L3 V.188",
        "M5 L3 K.240 1900Z",
        "M5 L3 K.240 1900Z M",
        "M5 L3 K.240D B",
        "M5 L3 K.3",
        "M5 L3 K.25",
        "M5 L3 K240 DB",
        "M5 L3 K240 1900",

        "M5 L3 .K240",
        "M5 L3 V188",
        "M5 .L3 K.2,13 2001",

        /*
         * Not considering cutter suffix that sorts numerically but has not decimal
         */
        /*
        "M5 .L3 K2 1880",
        "M5 .L3 K2 1882",
        "M5 .L3 K2 Q2 1880",
        "M5 .L3 K2 Q2 .A1",
        "M5 .L3 K2 .Q2 MD:CRAPO*DMA 1981",
        "M5 .L3 K2 1880 .Q2 1777",
        "M5 .L3 K2 OP.79",
        "M5 .L3 K2 OP.8",
        "M5 .L3 K2 OP.789",
        "M5 .L3 K2 D MAJ 1880",
        "M5 .L3 K2 M V.1",
        "M5 .L3 K2 OP.7:NO.6 1880",
        "M5 .L3 K2 OP.7:NO.6 1882",
        "M5 .L3 K2 OP.7:NO.51 1880",
        "M5 .L3 K2 1880 M",
        "M5 .L3 K2 NO.1 1880",
        "M5 .L3 K2 1880 M",
        "M5 .L3 K2 VOL.1",
        "M5 .L3 K2 K.240",
        "M5 .L3 K2 K.240 1880 F",
        */

        "M5 .L3 K240A",
        "M5.L3.K2",
        "M5 .L3 K240B M",
        "M5 .L3 2000 .K2 1880",
    };

    // list of diabolical call numbers in "proper" ascending order
    String[] properOrderList = {
        "AB9 L3",
        "AB9.22 L3",
        "ABR92.L3",
        "B8.14 L3",
        "B9 20th L3",
        "B9 2000",
        "B9 2000 35TH",
        "B9 2000 L3",
        "B9 L3",
        "B9 SVAR .L3",
        "B9.2 2000 L3",
        "B9.2 L3",
        "B9.22 L3",

        "B82 2000 L3",
        "B82 L3",
        "B82.2 1990 L3",
        "B82.2 L3",

        "B820 2000 L3",
        "B820.2 2000 L3",
        "B820.2 L3",
        "B822 L3",

        "B8200 L3",
        "B8220 L3",

        "M5 K4",
        "M5 .L",
        "M5 L299",

        // first cutter L3 vol/part info 1902
        "M5 L3 1902",
        "M5 L3 1902 V.2",
        "M5 L3 1902 V2",
        "M5 .L3 1902 V2 TANEYTOWN",
        "M5 L3 1902V",
        // first cutter L3 vol/part info 2000
        "M5 .L3 2000 .K2 1880",

        // first cutter L3 vol/part info: K.2,13 2001
        "M5 .L3 K.2,13 2001",

        // first cutter L3 second cutter K2
        "M5.L3.K2",
        "M5 .L3 K2 1880",
        "M5 .L3 K2 1880 M",
        // first cutter L3 K2 1880
        "M5 .L3 K2 1880 .Q2 1777",
        "M5 .L3 K2 1882",
        "M5 .L3 K2 D MAJ 1880",
        "M5 .L3 K2 K.240", // vol info K.240
        "M5 .L3 K2 K.240 1880 F", // vol info K.240 1880 F
        "M5 .L3 K2 M V.1", // vol info M V.1
        "M5 .L3 K2 NO.1 1880", // vol info NO.1
        "M5 .L3 K2 OP.7:NO.6 1880",
        "M5 .L3 K2 OP.7:NO.6 1882",
        "M5 .L3 K2 OP.7:NO.51 1880",
        "M5 .L3 K2 OP.8",
        "M5 .L3 K2 OP.79",
        "M5 .L3 K2 OP.789",
        "M5 .L3 K2 Q2 1880", // suffix Q2
        // first cutter L3 K2 Q2
        "M5 .L3 K2 Q2 .A1",
        // first cutter L3 K2
        "M5 .L3 K2 .Q2 MD:CRAPO*DMA 1981",
        "M5 .L3 K2 VOL.1", // vol info VOL.1

        // first cutter L3 vol/part info K.240
        "M5 L3 K.3",
        "M5 L3 K.25",
        "M5 L3 K.240 1900",
        "M5 L3 K.240 1900Z",
        "M5 L3 K.240 1900Z M",
        "M5 L3 K.240D B",
        // first cutter L3, second cutter
        "M5 L3 .K240",

        "M5 L3 K240 1900", // ??
        "M5 L3 K240 DB", // ??
        // first cutter L3 second cutter K240x
        "M5 .L3 K240A",
        "M5 .L3 K240B M",
        "M5 L3 V.188", // vol info V.188
        // second cutter V188
        "M5 L3 V188",

        "M5 L31",
        "M5 L31902",
        "M5 M2",

        // XXKs
        "XXKD671.G53",
        "XXKD671.G53 2012",
        "XXKDE671.G53",
        "XXKFA 30 1867 .A22",
        "XXKFA 207.A940",

    };

    // TODO: make current ascending order "correct"
    // list of diabolical call numbers in current ascending order
    String[] currentOrderList = {
        "AB9 L3",
        "AB9.22 L3",
        "ABR92.L3",

        "B8.14 L3",
        "B9 20th L3",
        "B9 2000",
        "B9 2000 35TH",
        "B9 2000 L3",
        "B9 L3",
        "B9 SVAR .L3",
        "B9.2 2000 L3",
        "B9.2 L3",
        "B9.22 L3",

        "B82 2000 L3",
        "B82 L3",
        "B82.2 1990 L3",
        "B82.2 L3",

        "B820 2000 L3",
        "B820.2 2000 L3",
        "B820.2 L3",
        "B822 L3",

        "B8200 L3",
        "B8220 L3",

        "M5 K4",
        "M5 .L",

        "M5 L299",
        "M5 L3 1902",
        "M5 L3 1902 V.2",
        "M5 L3 1902 V2",
        "M5 .L3 1902 V2 TANEYTOWN",
        "M5 L3 1902V",
        "M5 .L3 2000 .K2 1880",
        "M5 .L3 K.2,13 2001",

        // diverges from "proper" order here
        "M5 L3 K.3",
        "M5 L3 K.25",
        "M5 L3 K.240 1900",
        "M5 L3 K.240 1900Z",
        "M5 L3 K.240 1900Z M",
        "M5 L3 K.240D B",

        // second cutter K2 with suffix
        /*
         * Not considering this kind of cutter suffix,
         * require decimal or space to trigger numeric sort.
         */
//        "M5 .L3 K2 1880",
//        "M5 .L3 K2 1880 M",
//        "M5 .L3 K2 1882",
//        "M5 .L3 K2 D MAJ 1880",
//
//        "M5 .L3 K2 K.240",
//        "M5 .L3 K2 K.240 1880 F",
//        "M5 .L3 K2 M V.1",
//        "M5 .L3 K2 NO.1 1880",
//        "M5 .L3 K2 OP.7:NO.6 1880",
//        "M5 .L3 K2 OP.7:NO.6 1882",
//        "M5 .L3 K2 OP.7:NO.51 1880",
//        "M5 .L3 K2 OP.8",
//        "M5 .L3 K2 OP.79",
//        "M5 .L3 K2 OP.789");
//
//        "M5 .L3 K2 Q2 1880",
//        "M5 .L3 K2 VOL.1",

        "M5.L3.K2",

        // first cutter no suffix, simple second cutters
        "M5 L3 .K240",
        "M5 L3 K240 1900",
        "M5 .L3 K240A",
        "M5 .L3 K240B M",
        "M5 L3 K240 DB",

        // first cutter suffix
        //"M5 .L3 K2 1880 .Q2 1777", // TODO: Wrong - suffix vs. cutter norm
        //"M5 .L3 K2 .Q2 MD:CRAPO*DMA 1981",
        //"M5 .L3 K2 Q2 .A1",

        "M5 L3 V.188", // title/part suffix
        "M5 L3 V188", // second cutter

        // back to solid territory
        "M5 L31",
        "M5 L31902",
        "M5 M2",
        
        // XXKs
        "XXKD671.G53",
        "XXKD671.G53 2012",
        "XXKDE671.G53",
        "XXKFA 30 1867 .A22",
        "XXKFA 207.A940",
    };

    // CallNumUtils gets this wrong, side effect of allowances for music call numbers.
    String[] mapsImproperOrder = {
        "G3804.N4E73 1935 .N4",
        "G3804.N4E73 1935 .N41",
        "G3804.N4E73 1935 .N42",
        "G3804.N4E73 1990 .U5",
        "G3804.N4E74 1930z .G4",
        "G3804.N4G4 1995 .N4",
        "G3804.N4G4 1998 .N4",
        "G3804.N4G4 2009 .N493",
        "G3804.N4G8 1963 .T7",      // ERROR
        "G3804.N4G44 1947 .N4",
        "G3804.N4G45 1928 .R4",
        "G3804.N4G45 1973 .T7",
        "G3804.N4G52 1930 .M3",
        "G3804.N4G52 1969 .N4",
        "G3804.N4G455 1969 .N40",   // ERROR
        "G3804.N4G475 1929 .N3",    // ERROR
    };

    String[] mapsProperOrder = {
        "G3804.N4E73 1935 .N4",
        "G3804.N4E73 1935 .N41",
        "G3804.N4E73 1935 .N42",
        "G3804.N4E73 1990 .U5",
        "G3804.N4E74 1930z .G4",
        "G3804.N4G4 1995 .N4",
        "G3804.N4G4 1998 .N4",
        "G3804.N4G4 2009 .N493",
        "G3804.N4G44 1947 .N4",
        "G3804.N4G45 1928 .R4",
        "G3804.N4G45 1973 .T7",
        "G3804.N4G455 1969 .N40",
        "G3804.N4G475 1929 .N3",
        "G3804.N4G52 1930 .M3",
        "G3804.N4G52 1969 .N4",
        "G3804.N4G8 1963 .T7",
    };

    String[] depressionImproperOrder = {
        "HB3717 1929.B673 1998",
        "HB3717 1929.A3",
        "HB3717 1873.S36 1972",
        "HB3717 1929.C47",
        "HB3717 1919.S73 1995",
        "HB3717 1929.B365 2000",
        "HB3717 1929.B37 1987",
        "HB3717 1907.B78 2007",
    };

    String[] depressionProperOrder = {
        "HB3717 1873.S36 1972",
        "HB3717 1907.B78 2007",
        "HB3717 1919.S73 1995",
        "HB3717 1929.A3",
        "HB3717 1929.B365 2000",
        "HB3717 1929.B37 1987",
        "HB3717 1929.B673 1998",
        "HB3717 1929.C47",
    };

    String[] wwiDivisionsImproperOrder = {
        "D570.33 369TH.L5",
        "D570.3 28TH.M3",
        "D570.33 369TH.L7",
        "D570.3 90TH.S2413 2014",
        "D570.32 135TH.G5 1920",
        "D570.348 5TH.T4",
        "D570.32 306TH.H5",
        "D570.33 369TH.S58 2005",
        "D570.34 5TH.W5",
        "D570.3 28TH.P7",
        "D570.33 369TH.N456 2009",
        "D570.33 369TH.S25 2014",
        "D570.3 42D.C66 1994",
        "D570.3 28TH.M3",
    };

    String[] wwiDivisionsProperOrder = {
        "D570.3 28TH.M3",
        "D570.3 28TH.P7",
        "D570.3 42D.C66 1994",
        "D570.3 90TH.S2413 2014",
        "D570.32 135TH.G5 1920",
        "D570.32 306TH.H5",
        "D570.33 369TH.L5",
        "D570.33 369TH.L7",
        "D570.33 369TH.N456 2009",
        "D570.33 369TH.S25 2014",
        "D570.33 369TH.S58 2005",
        "D570.34 5TH.W5",
        "D570.348 5TH.T4",
    };

    // Köchel numbers, Burghauser numbers, etc should properly use a 
    // period indicating the fact that it's an abbreviation and not a cutter
    String[] dvorakBurghauserImproperOrder = {
        "M452.D98 B.120 1951",
        "M452.D98 B.121 1955",
        "M452.D98 B.18",
        "M452.D98 B.193 1955B",
        "M452.D98 B.37 1989",
        "M452.D98 B.179 1955",
        "M452.D98 B.17",
        "M452.D98 B.92 1955B",
        "M452.D98 B.57 1956B",
        "M452.D98 B.121, 1900",
        "M452.D98 B.192 1955B",
        "M452.D98 B.45 1958B",
        "M452.D98 B.75 1955",
    };

    String[] dvorakBurghauserProperOrder = {
        "M452.D98 B.17",
        "M452.D98 B.18",
        "M452.D98 B.37 1989",
        "M452.D98 B.45 1958B",
        "M452.D98 B.57 1956B",
        "M452.D98 B.75 1955",
        "M452.D98 B.92 1955B",
        "M452.D98 B.120 1951",
        "M452.D98 B.121, 1900",
        "M452.D98 B.121 1955",
        "M452.D98 B.179 1955",
        "M452.D98 B.192 1955B",
        "M452.D98 B.193 1955B",
    };
    
    // weird local thing; justification lost to time (TriColleges)
    String[] class0ImproperOrder = { 
        "KQH2506.H86 2009", 
        "KQE10.A47 1966", 
        "KQH0.S34", 
    }; 
    
    String[] class0ProperOrder = { 
        "KQE10.A47 1966", 
        "KQH0.S34", 
        "KQH2506.H86 2009", 
    };
    
    // cutter ordering
    String[] cuttersImproperOrder = { 
        "PN1993.5.F7A4672 1996", 
        "PN1993.5.F7A4762 2003", 
        "PN1993.5.F7A46866 1999", 
    };
    
    String[] cuttersProperOrder = { 
        "PN1993.5.F7A4672 1996", 
        "PN1993.5.F7A46866 1999", 
        "PN1993.5.F7A4762 2003",
    };
    
    // suffix/cutter sorting
    String[] suffixCutterImproperOrder = {
            "BF 1999 1973",
            "BF1999 .A63880 1978",
            "BF1999 Aarons",
            "BF1999 .B34",
            "BF1999 C78",
    };

    String[] suffixCutterProperOrder = {
            "BF 1999 1973",
            "BF1999 .A63880 1978",
            "BF1999 .B34",
            "BF1999 C78",
            "BF1999 Aarons",
    };
    
    // cutter suffixes with another cutter
    String[] cutterSuffixes = {
            "",
    };
    
    // serials or series if the volume is present
    String[] serialWithVolumeImproperOrder = {
            "B23.L4 v.1",
            "B23.L4 v.2",
            "B23.L4",
    };

    String[] serialWithVolumeProperOrder = {
            "B23.L4",
            "B23.L4 v.1",
            "B23.L4 v.2",
    };
    
    // Harvard-Yenching: whole numbers
    String[] harvardYenchingImproperOrder = {
            "110 0603",
            "110 1104",
            "110 1121",
            "4725 6339",
            "4725 7233",
            "4725.2 1123",
            "4725.2 1232",
            "4725.2 1321",
            "4725.2 1321.1",
            "4725.2 1321.2",
            "4725.2 1321.3",
            "4725.2 7942",
            "4725.4 4131",
            "4725.4 4175",
            "4726 1913",
            "J4800.3 3204",
            "J3423 1423.1",
            "J3421 5161",
            "J4555 3843",
            "J4777.5 5349",
            "J5810.1 0066",
            "J5810.1 6506",
            "J5880 3065",
            "J5921.95 4342",
            "J5922 6050.5",
            "J5936.2 4231",
            "J5936.2 3333.3",
            "J9214 0447",
            "J9333 5765.2",
            };

    String[] harvardYenchingProperOrder = {
            "110 0603",
            "110 1104",
            "110 1121",
            "4725 6339",
            "4725 7233",
            "4725.2 1123",
            "4725.2 1232",
            "4725.2 1321",
            "4725.2 1321.1",
            "4725.2 1321.2",
            "4725.2 1321.3",
            "4725.2 7942",
            "4725.4 4131",
            "4725.4 4175",
            "4726 1913",
            "J3421 5161",
            "J3423 1423.1",
            "J4555 3843",
            "J4777.5 5349",
            "J4800.3 3204",
            "J5810.1 0066",
            "J5810.1 6506",
            "J5880 3065",
            "J5921.95 4342",
            "J5922 6050.5",
            "J5936.2 3333.3",
            "J5936.2 4231",
            "J9214 0447",
            "J9333 5765.2",
    };

    
    /**
     * test the sort of LC call numbers (via the shelf key) - diabolical data
//TODO: improve sort and check results against "correct" sort
     */
    @Test
    public final void testLCcallnumsSorted_diabolical() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(diabolicalCallnumList, currentOrderList);
    }

    /**
     * a few more sort tests - smaller lists
     */
    @Test
    public final void testLCcallnumsSorted_maps() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(mapsImproperOrder, mapsProperOrder);
    }

    @Test
    public final void testLCcallnumsSorted_depression() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(depressionImproperOrder, depressionProperOrder);
    }

    @Test
    public final void testLCcallnumsSorted_wwi() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(wwiDivisionsImproperOrder, wwiDivisionsProperOrder);
    }

    @Test
    public final void testLCcallnumsSorted_dvorak() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(dvorakBurghauserImproperOrder, dvorakBurghauserProperOrder);
    }

    @Test
    public final void testLCcallnumsSorted_class0() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(class0ImproperOrder, class0ProperOrder);
    }

    @Test
    public final void testLCcallnumsSorted_cutters() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(cuttersImproperOrder, cuttersProperOrder);
    }

    @Test
    public final void testLCcallnumsSorted_suffixAndCutters() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(suffixCutterImproperOrder, suffixCutterProperOrder);
    }

    @Test
    public final void testLCcallnumsSorted_serialsWithVolumes() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(serialWithVolumeImproperOrder, serialWithVolumeProperOrder);
    }

    @Test
    public final void testLCcallnumsSorted_HarvardYenching() throws IOException,
            ParserConfigurationException, SAXException
    {
        compareCallNumbers(harvardYenchingImproperOrder, harvardYenchingProperOrder);
    }
    
    /**
     * Helper for all the sort tests; actually does the sorting and comparing
     */
    private void compareCallNumbers(String[] improper, String[] proper) 
            throws IOException, ParserConfigurationException, SAXException
    {
        // compute list of sorted shelfkeys
        Map<String, String> shelfkey2callnum = new HashMap<String, String>(75);
        for (String callnum : improper)
        {
            LCCallNumber lcCall = new LCCallNumber(callnum);
            shelfkey2callnum.put(lcCall.getShelfKey(), callnum);
        }
        List<String> orderedKeys = new ArrayList<String>(shelfkey2callnum.keySet());
        Collections.sort(orderedKeys);

        // compare against correct order
        for (int i = 0; i < orderedKeys.size(); i++)
        {
            LCCallNumber properCall = new LCCallNumber(proper[i]);
            assertEquals("At position " + i + " in list:",
                    properCall.getShelfKey(), orderedKeys.get(i));
        }
    }

    private List<String> sortCallNumbers(String[] callNums) {
        // compute list of sorted shelfkeys
        Map<String, String> shelfkey2callnum = new HashMap<String, String>(75);
        for (String callnum : callNums)
        {
            LCCallNumber lcCall = new LCCallNumber(callnum);
            shelfkey2callnum.put(lcCall.getShelfKey(), callnum);
        }
        List<String> orderedKeys = new ArrayList<String>(shelfkey2callnum.keySet());
        Collections.sort(orderedKeys);
        return orderedKeys;
    }


    /**
     * unit test for LCCallNumber.getClassification()
     */
    @Test
    public void testGetClassification()
    {
        String callnum; 

        // classification only
        callnum = "QA76";
        assertEquals("QA76", new LCCallNumber(callnum).getClassification());
        // simple example
        callnum = "PS153 .G38 B73 2012";
        assertEquals("PS153", new LCCallNumber(callnum).getClassification());

        callnum = "M1 L33";
        assertEquals("M1", new LCCallNumber(callnum).getClassification());
        callnum = "M211 .M93 K.240 1988"; // first cutter has period
        assertEquals("M211", new LCCallNumber(callnum).getClassification());
        callnum = "PQ2678.K26 P54 1992"; // no space b4 cutter with period
        assertEquals("PQ2678", new LCCallNumber(callnum).getClassification());
        callnum = "PR9199.4 .B3"; // class has float, first cutter has period
        assertEquals("PR9199.4", new LCCallNumber(callnum).getClassification());
        callnum = "PR9199.3.L33 B6"; // decimal call no space before cutter
        assertEquals("PR9199.3", new LCCallNumber(callnum).getClassification());
        callnum = "HC241.25F4 .D47";
        assertEquals("HC241.25", new LCCallNumber(callnum).getClassification());

        // suffix before first cutter
        callnum = "PR92 1990 L33";
        assertEquals("PR92 1990", new LCCallNumber(callnum).getClassification());
        callnum = "PR92 1844 .L33 1990"; // first cutter has period
        assertEquals("PR92 1844", new LCCallNumber(callnum).getClassification());
        callnum = "PR92 1844.L33 1990"; // no space before cutter w period
        assertEquals("PR92 1844", new LCCallNumber(callnum).getClassification());
        callnum = "PR92 1844L33 1990"; // no space before cutter w no period
        assertEquals("PR92 1844", new LCCallNumber(callnum).getClassification());
        // period before cutter
        callnum = "M234.8 1827 .F666";
        assertEquals("M234.8 1827", new LCCallNumber(callnum).getClassification());
        callnum = "PS3538 1974.L33";
        assertEquals("PS3538 1974", new LCCallNumber(callnum).getClassification());
        // two cutters
        callnum = "PR9199.3 1920 L33 A6 1982";
        assertEquals("PR9199.3 1920", new LCCallNumber(callnum).getClassification());
        callnum = "PR9199.3 1920 .L33 1475 .A6";
        assertEquals("PR9199.3 1920", new LCCallNumber(callnum).getClassification());
        // decimal and period before cutter
        callnum = "HD38.25.F8 R87 1989";
        assertEquals("HD38.25", new LCCallNumber(callnum).getClassification());
        callnum = "HF5549.5.T7 B294 1992";
        assertEquals("HF5549.5", new LCCallNumber(callnum).getClassification());

        // suffix with letters
        callnum = "L666 15th A8";
        assertEquals("L666 15th", new LCCallNumber(callnum).getClassification());

        // non-compliant cutter
        callnum = "M5 .L";
        assertEquals("M5", new LCCallNumber(callnum).getClassification());

        // no cutter
        callnum = "B9 2000";
        assertEquals("B9 2000", new LCCallNumber(callnum).getClassification());
        callnum = "B9 2000 35TH";
        assertEquals("B9 2000 35TH", new LCCallNumber(callnum).getClassification());

        // wacko lc class suffixes
        callnum = "G3840 SVAR .H5"; // suffix letters only
        assertEquals("G3840 SVAR", new LCCallNumber(callnum).getClassification());

        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002";
        assertEquals("G3824", new LCCallNumber(callnum).getClassification());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("G3841", new LCCallNumber(callnum).getClassification());

        // space between LC class letters and numbers
        callnum = "PQ 8550.21.R57 V5 1992";
        assertEquals("PQ 8550.21", new LCCallNumber(callnum).getClassification());
        callnum = "HD 38.25.F8 R87 1989";
        assertEquals("HD 38.25", new LCCallNumber(callnum).getClassification());
        
        // XXK call numbers
        callnum = "XXKD671.G53 2012";
        assertEquals("XXKD671", new LCCallNumber(callnum).getClassification());
    }

    /**
     * test for getClassLetters
     */
    @Test
    public void testGetClassLetters()
    {
        String callnum;
        
        callnum = "M1 L33";
        assertEquals("M", new LCCallNumber(callnum).getClassLetters());
        callnum = "PQ 8550.21.R57 V5 1992";
        assertEquals("PQ", new LCCallNumber(callnum).getClassLetters());
        callnum = "M 1 L33";    // space after one letter
        assertEquals("M", new LCCallNumber(callnum).getClassLetters());
        callnum = "PQ 8550.21.R57 V5 1992";     // space after multiple letters
        assertEquals("PQ", new LCCallNumber(callnum).getClassLetters());
        
        // XXK call numbers
        callnum = "XXKFA75.J656 2012";  // non-standard class letters
        assertEquals("XXKFA", new LCCallNumber(callnum).getClassLetters());
        callnum = "Sony MS957"; // circulating equipment
        assertEquals("Sony", new LCCallNumber(callnum).getClassLetters());
    }
    
    /**
     * test for getClassNumber
     */
    @Test
    public void testGetClassNumber()
    {
        String callnum = "M211 .M93 K.240 1988";
        assertEquals("211", new LCCallNumber(callnum).getClassNumber());
        callnum = "M1 L33";
        assertEquals("1", new LCCallNumber(callnum).getClassNumber());
        callnum = "PR92 1990 L33";
        assertEquals("92", new LCCallNumber(callnum).getClassNumber());
        callnum = "PR92 .L33 1990";
        assertEquals("92", new LCCallNumber(callnum).getClassNumber());
        callnum = "PR919 .L33 1990";
        assertEquals("919", new LCCallNumber(callnum).getClassNumber());
        callnum = "PR9199 .A39";
        assertEquals("9199", new LCCallNumber(callnum).getClassNumber());
        callnum = "PR9199.3.L33 B6";
        assertEquals("9199.3", new LCCallNumber(callnum).getClassNumber());
        callnum = "PR9199.4 .B3";
        assertEquals("9199.4", new LCCallNumber(callnum).getClassNumber());
        callnum = "PQ2678.I26 P54 1992";
        assertEquals("2678", new LCCallNumber(callnum).getClassNumber());
        callnum = "HC241.25 .I4 D47";
        assertEquals("241.25", new LCCallNumber(callnum).getClassNumber());

        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002";
        assertEquals("3824", new LCCallNumber(callnum).getClassNumber());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("3841", new LCCallNumber(callnum).getClassNumber());
    }

    /**
     * unit test to make sure the optional suffix portion of the LC
     * classification is parsed properly (before it's normalized)
     */
    @Test
    public void testLCClassSuffix()
    {

        String callnum = "M1 L33";
        assertNull(new LCCallNumber(callnum).getClassSuffix());
        // decimal class before cutter
        callnum = "PR9199.48 .B3";
        assertNull(new LCCallNumber(callnum).getClassSuffix());
        callnum = "PR9199.3.L33 B6";
        assertNull(new LCCallNumber(callnum).getClassSuffix());
        // suffix after second cutter
        callnum = "PQ2678.I26 P54 1992";
        assertNull(new LCCallNumber(callnum).getClassSuffix());
        // suffix after first cutter, before second cutter
        callnum = "PR9199.3.L33 2007 B6";
        assertNull(new LCCallNumber(callnum).getClassSuffix());
        // two suffixes after second cutter
        callnum = "M211 .M93 K.240 1988";
        assertNull(new LCCallNumber(callnum).getClassSuffix());

        // suffixes no period before cutter
        callnum = "PR92 1990 L33";
        assertEquals("1990", new LCCallNumber(callnum).getClassSuffix());
        callnum = "MT37 2003M384";
        assertEquals("2003", new LCCallNumber(callnum).getClassSuffix());
        // period before cutter
        callnum = "M234.8 1827 .F666";
        assertEquals("1827", new LCCallNumber(callnum).getClassSuffix());
        callnum = "PS3538 1974.L33";
        assertEquals("1974", new LCCallNumber(callnum).getClassSuffix());
        // two cutters
        callnum = "PR9199.3 1920 L33 A6 1982";
        assertEquals("1920", new LCCallNumber(callnum).getClassSuffix());
        callnum = "PR9199.3 1920 .L33 1475 .A6";
        assertEquals("1920", new LCCallNumber(callnum).getClassSuffix());
        callnum = "L666 15th A8"; // suffix with letters
        assertEquals("15th", new LCCallNumber(callnum).getClassSuffix());
        // non-compliant cutter
        callnum = "M5 .L";
        assertNull("expected null LC class suffix for " + callnum, new LCCallNumber(callnum).getClassSuffix());

        // no cutter
        callnum = "B9 2000";
        assertEquals("2000", new LCCallNumber(callnum).getClassSuffix());
        callnum = "B9 2000 35TH";
        assertEquals("2000 35TH", new LCCallNumber(callnum).getClassSuffix());

        // wacko lc class suffixes
        callnum = "G3840 SVAR .H5"; // suffix letters only
        assertEquals("SVAR", new LCCallNumber(callnum).getClassSuffix());
    }

    String[][] cutterTestArray = {
            {"L33", "L33"},
            {".B2", "B2"},
            {"W5 2002", "W5 42002"},
            {".G384 1925 .U5 TANEYTOWN", "G384 41925 U5 TANEYTOWN"},
    };

    /**
     * unit test for appendCutterShelfKey
     */
    @Test
    public void testAppendCutterShelfKey()
    {
        for (String[] test:cutterTestArray) {
            String cutter = test[0];
            String expected = test[1];
            StringBuilder buf = new StringBuilder();
            LCCallNumber.appendCutterShelfKey(buf, cutter);
            assertEquals(expected, buf.toString());
        }
    }

    /**
     * unit test for getShelfKey
     */
    @Test
    public void testGetShelfKey()
    {
        String callnum = "M1 L33";
        assertEquals("M 11 L33", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.4 .B3"; // class has fraction, first cutter has period
        assertEquals("PR 49199.4 B3", new LCCallNumber(callnum).getShelfKey());
        callnum = "AB9 L3";
        assertEquals("AB 19 L3", new LCCallNumber(callnum).getShelfKey());

        // LC classification suffixes
        callnum = "PR92 1990 L33"; // suffix before first cutter
        assertEquals("PR 292 41990 L33", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR92 1844 .L33 1990"; // first cutter has period
        assertEquals("PR 292 41844 L33 41990", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR92 1844.L33 1990"; // no space before cutter w period
        assertEquals("PR 292 41844 L33 41990", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR92 1844L33 1990"; // no space before cutter w no period
        assertEquals("PR 292 41844 L33 41990", new LCCallNumber(callnum).getShelfKey());
        callnum = "L666 15th A8"; // suffix with letters
        assertEquals("L 3666 215TH A8", new LCCallNumber(callnum).getShelfKey());
        callnum = "MT37 2003M384"; // no period before cutter
        assertEquals("MT 237 42003 M384", new LCCallNumber(callnum).getShelfKey());
        callnum = "M234.8 1827 .F666"; // space period before cutter
        assertEquals("M 3234.8 41827 F666", new LCCallNumber(callnum).getShelfKey());
        callnum = "PS3538 1974.L33"; // period before cutter
        assertEquals("PS 43538 41974 L33", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3 1920 L33 A6 1982"; // two cutters, nothing b/t 'em
        assertEquals("PR 49199.3 41920 L33 A6 41982", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // two cutters suffix b/t
        assertEquals("PR 49199.3 41920 L33 41475 A6", new LCCallNumber(callnum).getShelfKey());

        // first cutter suffix
        callnum = "M5 L3 1902";
        assertEquals("M 15 L3 41902", new LCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .M9 1900Z";
        assertEquals("M 41001 M9 41900Z", new LCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .M9 K.551 1900Z M";
        assertEquals("M 41001 M9 K 3551 41900Z M", new LCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .M9 K.173D B";
        assertEquals("M 41001 M9 K 3173D B", new LCCallNumber(callnum).getShelfKey());
        callnum = "G3840 SVAR .H5";
        assertEquals("G 43840 _SVAR H5", new LCCallNumber(callnum).getShelfKey());
        callnum = "G3841 .C2 1935 .M3"; // with second cutter
        assertEquals("G 43841 C2 41935 M3", new LCCallNumber(callnum).getShelfKey());
        callnum = "PQ2678.K26 P54 1992"; // no space b4 cutter with period
        assertEquals("PQ 42678 K26 P54 41992", new LCCallNumber(callnum).getShelfKey());
        callnum = "M211 .M93 K.240 1988"; // first cutter has period
        assertEquals("M 3211 M93 K 3240 41988", new LCCallNumber(callnum).getShelfKey());

        // second cutter
        callnum = "PQ2678.K26 P54"; // 2 cutters
        assertEquals("PQ 42678 K26 P54", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3.L33 B6"; // decimal call no space before cutter
        assertEquals("PR 49199.3 L33 B6", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3.L33 2007 B6"; // double suffix after 1st cutter
        assertEquals("PR 49199.3 L33 42007 B6", new LCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3.L33 2007 .B6"; // suffix after 1st cutter, b4 2nd cutter
        assertEquals("PR 49199.3 L33 42007 B6",
                new LCCallNumber(callnum).getShelfKey());
        callnum = "HC241.25F4 .D47";
        assertEquals("HC 3241.25 F4 D47", new LCCallNumber(callnum).getShelfKey());

        // second cutter suffix
        callnum = "M453 .Z29 Q1 L V.2";
        assertEquals("M 3453 Z29 Q1 L V 12", new LCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .A13 S9 OP.7:NO.6 1944";
        assertEquals(
                "M 41001 A13 S9 OP 17 NO 16 41944", new LCCallNumber(callnum).getShelfKey());
        callnum = "ML410 .M9 P29 1941 M";
        assertEquals("ML 3410 M9 P29 41941 M", new LCCallNumber(callnum).getShelfKey());
        callnum = "M857 .K93 H2 OP.79";
        assertEquals("M 3857 K93 H2 OP 279", new LCCallNumber(callnum).getShelfKey());
        callnum = "G3824 .P5 1776 .E2 1976"; // first cutter suffix too
        assertEquals("G 43824 P5 41776 E2 41976", new LCCallNumber(callnum).getShelfKey());
        callnum = "G3822 .G384 1925 .U5 TANEYTOWN"; // first cutter suffix too
        assertEquals("G 43822 G384 41925 U5 TANEYTOWN", new LCCallNumber(callnum).getShelfKey());
        callnum = "M857 .R348 P2 D MAJ 1989";
        assertEquals("M 3857 R348 P2 D MAJ 41989", new LCCallNumber(callnum).getShelfKey());
        callnum = "LD4881 .S16588 A936A";
        assertEquals("LD 44881 S16588 A936 A", new LCCallNumber(callnum).getShelfKey());
        callnum = "M857 .M93 S412B M";
        assertEquals("M 3857 M93 S412 B M", new LCCallNumber(callnum).getShelfKey());

        // suffix after first cutter looks like a cutter
        /*
         * NB: There is a difference here versus original Stanford code
         * If the suffix after the first cutter looks like a cutter, this code will treat it
         * as a cutter, on the assumption that few humans will know that it's not a cutter
         */
        
        callnum = "MT130 .M93 K96 W83 1988";
        assertEquals("MT 3130 M93 K96 W83 41988", new LCCallNumber(callnum).getShelfKey());
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("MT 3130 M93 K96 W83 41988", new LCCallNumber(callnum).getShelfKey());
        callnum = "ML410 .M8 L25 M95 1995";
        assertEquals("ML 3410 M8 L25 M95 41995", new LCCallNumber(callnum).getShelfKey());
        callnum = "ML410 .M8 L25 .M95 1995";
        assertEquals("ML 3410 M8 L25 M95 41995", new LCCallNumber(callnum).getShelfKey());
        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002"; // suffix after second cutter
        assertEquals(
                "G 43824 G3 S5 41863 W5 42002", new LCCallNumber(callnum).getShelfKey());
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals(
                "G 43824 G3 S5 41863 W5 42002", new LCCallNumber(callnum).getShelfKey());
        callnum = "G3824 .G3 G4 S9 M2";
        assertEquals("G 43824 G3 G4 S9 M2", new LCCallNumber(callnum).getShelfKey());
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("G 43824 G3 G4 S9 M2", new LCCallNumber(callnum).getShelfKey());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals(
                "G 43841 C2 S24 U5 MD CRAPO DMA 41981", new LCCallNumber(callnum).getShelfKey());

        // wacko
        callnum = "M1001 .H";
        assertEquals("M 41001 H", new LCCallNumber(callnum).getShelfKey());
        callnum = "LD6353 1886";
        assertEquals("LD 46353 41886", new LCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .M939 S.3,13 2001";
        assertEquals("M 41001 M939 S 13 213 42001", new LCCallNumber(callnum).getShelfKey());
        callnum = "LD6329 1903 35TH";
        assertEquals("LD 46329 41903 235TH", new LCCallNumber(callnum).getShelfKey());
        
        // Local call number variations
        callnum = "XXKF 385.B550 1996";
        assertEquals("XXKF 3385 B550 41996", new LCCallNumber(callnum).getShelfKey());
        callnum = "DR19999 Sanders";
        assertEquals("DR 519999 _SANDERS", new LCCallNumber(callnum).getShelfKey());
        

        // XXK call numbers
        callnum = "XXKD671.G53";
        assertEquals("XXKD 3671 G53", new LCCallNumber(callnum).getShelfKey());
        callnum = "XXKD671.G53";
        assertEquals("XXKD 3671 G53", new LCCallNumber(callnum).getShelfKey());
        callnum = "XXKD671.G53 2012";
        assertEquals("XXKD 3671 G53 42012", new LCCallNumber(callnum).getShelfKey());

        //XX with 3 LC letters: classification part of key should be 1 character wider than normal
        callnum = "XXKFA 207.A940";
        assertEquals("XXKFA 3207 A940", new LCCallNumber(callnum).getShelfKey());
        //XX with 33 LC letters, plus year suffix
        callnum = "XXKFA 30 1867 .A22"; // first cutter has period
        assertEquals("XXKFA 230 41867 A22", new LCCallNumber(callnum).getShelfKey());

    }

    /**
     * unit test for getShelfKey: classification but no cutter
     */
    @Test
    public void testGetShelfKey_classNoCutter()
    {
        String callnum = "BF199";
        assertEquals("BF 3199", new LCCallNumber(callnum).getShelfKey());
        callnum = "BF199.";
        assertEquals("BF 3199", new LCCallNumber(callnum).getShelfKey());
    }

    /**
     * unit test for getShelfKey: check that trailing spaces 
     * produce the same shelfkey
     */
    @Test
    public void testGetShelfKey_trailingSpaces()
    {
        String callnum = "BQ1270";
        assertEquals("BQ 41270", new LCCallNumber(callnum).getShelfKey());
        callnum = "BQ 1270";
        assertEquals("BQ 41270", new LCCallNumber(callnum).getShelfKey());
        callnum = "BQ1270 ";
        assertEquals("BQ 41270", new LCCallNumber(callnum).getShelfKey());
        callnum = "BQ 1270 ";
        assertEquals("BQ 41270", new LCCallNumber(callnum).getShelfKey());
    }

    /**
     * unit test for getShelfKey: check that lower case user input is handled sensibly
     */
    @Test
    public void testGetShelfKey_lowerCase()
    {
        String callnum = "bq1270";
        assertEquals("BQ 41270", new LCCallNumber(callnum).getShelfKey());
        callnum = "l666 15th A8"; // suffix with letters
        assertEquals("L 3666 215TH A8", new LCCallNumber(callnum).getShelfKey());
        
        // Check that upper case and lower case input normalizer the same
        String callnumUpper = "M857 .K93 H2 OP.79";
        String callnumLower = "m857 .k93 h2 op.79";
        String callnumKey = "M 3857 K93 H2 OP 279";
        assertEquals(callnumKey, new LCCallNumber(callnumUpper).getShelfKey());
        assertEquals(callnumKey, new LCCallNumber(callnumLower).getShelfKey());
    }

    /**
     * unit test for isValid
     */
    @Test
    public void testIsValid() {
        String callnum = null;
        
        callnum = "BF199";      // classification only
        assertTrue(new LCCallNumber(callnum).isValid());
        callnum = "ABR92.L3";   // cutter with no space
        assertTrue(new LCCallNumber(callnum).isValid());
        callnum = "ABR92 .L3";  // cutter with space
        assertTrue(new LCCallNumber(callnum).isValid());

        callnum = "395.09 B34";      // no class letters
        assertFalse(new LCCallNumber(callnum).isValid());
        callnum = "XXKD671.G53";     // invalid class letters
        assertFalse(new LCCallNumber(callnum).isValid());
        callnum = "Sony PDX10";      // no class numbers
        assertFalse(new LCCallNumber(callnum).isValid());
    }

    /**
     * unit test for toString
     */
    @Test
    public void testToString() {
        String callnum = null;
        
        callnum = "BF199";      // classification only
        assertEquals("BF199", new LCCallNumber(callnum).toString());
        callnum = "ABR92.L3";   // cutter with no space
        assertEquals("ABR92 .L3", new LCCallNumber(callnum).toString());
        callnum = "ABR92 .L3";  // cutter with space
        assertEquals("ABR92 .L3", new LCCallNumber(callnum).toString());
        callnum = "B8 L3";      // cutter missing period
        assertEquals("B8 .L3", new LCCallNumber(callnum).toString());
        callnum = "B8.14 .L3";  // class with decimal and cutter
        assertEquals("B8.14 .L3", new LCCallNumber(callnum).toString());
        callnum = "B8.14";      // class with decimal and no cutter
        assertEquals("B8.14", new LCCallNumber(callnum).toString());
        callnum = "B82.2 1990"; // suffix and no cutter
        assertEquals("B82.2 1990", new LCCallNumber(callnum).toString());
        callnum = "B82.2 1990 L3";      // suffix with cutter
        assertEquals("B82.2 1990 .L3", new LCCallNumber(callnum).toString());
        
        // non-LC
        callnum = "395.09 B34";
        assertEquals("395.09 B34", new LCCallNumber(callnum).toString());
        callnum = "110 0603";
        assertEquals("110 0603", new LCCallNumber(callnum).toString());
    }
}
