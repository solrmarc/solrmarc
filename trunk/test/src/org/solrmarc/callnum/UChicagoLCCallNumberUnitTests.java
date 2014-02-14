package org.solrmarc.callnum;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests for <code>UChicagoLCCallNumber</code>.
 *
 * Initially copied from on Naomi Dushay's unit tests for <code>CallNumberUnitTests</code>.
 *
 * @author Naomi Dushay, Stanford University
 * @author Tod Olson, University of Chicago
 *
 */
public class UChicagoLCCallNumberUnitTests {


    // list of raw call numbers NOT in order to check sorting
    List<String> diabolicalCallnumList = new ArrayList<String>(80);
    {
        diabolicalCallnumList.add("B8.14 L3");
        diabolicalCallnumList.add("B9 L3");
        diabolicalCallnumList.add("B9 2000 L3");
        diabolicalCallnumList.add("B9 SVAR .L3");
        diabolicalCallnumList.add("B9 20th L3");
        diabolicalCallnumList.add("B9 2000 35TH");
        diabolicalCallnumList.add("B9 2000");
        diabolicalCallnumList.add("B9.2 L3");
        diabolicalCallnumList.add("B9.2 2000 L3");
        diabolicalCallnumList.add("B9.22 L3");

        diabolicalCallnumList.add("B82 L3");
        diabolicalCallnumList.add("B82 2000 L3");
        diabolicalCallnumList.add("B82.2 L3");
        diabolicalCallnumList.add("B82.2 1990 L3");

        diabolicalCallnumList.add("B822 L3");
        diabolicalCallnumList.add("B820 2000 L3");
        diabolicalCallnumList.add("B820.2 L3");
        diabolicalCallnumList.add("B820.2 2000 L3");
        diabolicalCallnumList.add("B8220 L3");
        diabolicalCallnumList.add("B8200 L3");

        diabolicalCallnumList.add("AB9 L3");
        diabolicalCallnumList.add("AB9.22 L3");
        diabolicalCallnumList.add("ABR92.L3");

        diabolicalCallnumList.add("XXKD671.G53");
        diabolicalCallnumList.add("XXKD671.G53 2012");
        diabolicalCallnumList.add("XX KDE671.G53");
        diabolicalCallnumList.add("XXKFA 30 1867 .A22");
        diabolicalCallnumList.add("XXKFA 207.A940");

        diabolicalCallnumList.add("M5 M2");
        diabolicalCallnumList.add("M5 K4");
        diabolicalCallnumList.add("M5 L299");
        diabolicalCallnumList.add("M5 .L");
        diabolicalCallnumList.add("M5 L31");
        diabolicalCallnumList.add("M5 L31902");
        diabolicalCallnumList.add("M5 L3 1902");
        diabolicalCallnumList.add("M5 L3 1902 V2");
        diabolicalCallnumList.add("M5 .L3 1902 V2 TANEYTOWN");
        diabolicalCallnumList.add("M5 L3 1902V");
        diabolicalCallnumList.add("M5 L3 1902 V.2");
        diabolicalCallnumList.add("M5 L3 K.240 1900");
        diabolicalCallnumList.add("M5 L3 V.188");
        diabolicalCallnumList.add("M5 L3 K.240 1900Z");
        diabolicalCallnumList.add("M5 L3 K.240 1900Z M");
        diabolicalCallnumList.add("M5 L3 K.240D B");
        diabolicalCallnumList.add("M5 L3 K.3");
        diabolicalCallnumList.add("M5 L3 K.25");
        diabolicalCallnumList.add("M5 L3 K240 DB");
        diabolicalCallnumList.add("M5 L3 K240 1900");

        diabolicalCallnumList.add("M5 L3 .K240");
        diabolicalCallnumList.add("M5 L3 V188");
        diabolicalCallnumList.add("M5 .L3 K.2,13 2001");

        diabolicalCallnumList.add("M5 .L3 K2 1880");
        diabolicalCallnumList.add("M5 .L3 K2 1882");
        diabolicalCallnumList.add("M5 .L3 K2 Q2 1880");
        diabolicalCallnumList.add("M5 .L3 K2 Q2 .A1");
        diabolicalCallnumList.add("M5 .L3 K2 .Q2 MD:CRAPO*DMA 1981");
        diabolicalCallnumList.add("M5 .L3 K2 1880 .Q2 1777");
        diabolicalCallnumList.add("M5 .L3 2000 .K2 1880");
        diabolicalCallnumList.add("M5 .L3 K2 OP.79");
        diabolicalCallnumList.add("M5 .L3 K2 OP.8");
        diabolicalCallnumList.add("M5 .L3 K2 OP.789");
        diabolicalCallnumList.add("M5 .L3 K240B M");
        diabolicalCallnumList.add("M5 .L3 K2 D MAJ 1880");
        diabolicalCallnumList.add("M5 .L3 K240A");
        diabolicalCallnumList.add("M5 .L3 K2 M V.1");
        diabolicalCallnumList.add("M5 .L3 K2 OP.7:NO.6 1880");
        diabolicalCallnumList.add("M5 .L3 K2 OP.7:NO.6 1882");
        diabolicalCallnumList.add("M5 .L3 K2 OP.7:NO.51 1880");
        diabolicalCallnumList.add("M5 .L3 K2 1880 M");
        diabolicalCallnumList.add("M5 .L3 K2 NO.1 1880");
        diabolicalCallnumList.add("M5.L3.K2");
        diabolicalCallnumList.add("M5 .L3 K2 1880 M");
        diabolicalCallnumList.add("M5 .L3 K2 VOL.1");
        diabolicalCallnumList.add("M5 .L3 K2 K.240");
        diabolicalCallnumList.add("M5 .L3 K2 K.240 1880 F");
    }

    // list of diabolical call numbers in "proper" ascending order
    List<String> properOrderList = new ArrayList<String>(80);
    {
        properOrderList.add("AB9 L3");
        properOrderList.add("AB9.22 L3");
        properOrderList.add("ABR92.L3");
        properOrderList.add("B8.14 L3");
        properOrderList.add("B9 20th L3");
        properOrderList.add("B9 2000");
        properOrderList.add("B9 2000 35TH");
        properOrderList.add("B9 2000 L3");
        properOrderList.add("B9 L3");
        properOrderList.add("B9 SVAR .L3");
        properOrderList.add("B9.2 2000 L3");
        properOrderList.add("B9.2 L3");
        properOrderList.add("B9.22 L3");

        properOrderList.add("B82 2000 L3");
        properOrderList.add("B82 L3");
        properOrderList.add("B82.2 1990 L3");
        properOrderList.add("B82.2 L3");

        properOrderList.add("B820 2000 L3");
        properOrderList.add("B820.2 2000 L3");
        properOrderList.add("B820.2 L3");
        properOrderList.add("B822 L3");

        properOrderList.add("B8200 L3");
        properOrderList.add("B8220 L3");

        properOrderList.add("M5 K4");
        properOrderList.add("M5 .L");
        properOrderList.add("M5 L299");

        // first cutter L3 vol/part info 1902
        properOrderList.add("M5 L3 1902");
        properOrderList.add("M5 L3 1902 V.2");
        properOrderList.add("M5 L3 1902 V2");
        properOrderList.add("M5 .L3 1902 V2 TANEYTOWN");
        properOrderList.add("M5 L3 1902V");
        // first cutter L3 vol/part info 2000
        properOrderList.add("M5 .L3 2000 .K2 1880");

        // first cutter L3 vol/part info: K.2,13 2001
        properOrderList.add("M5 .L3 K.2,13 2001");

        // first cutter L3 second cutter K2
        properOrderList.add("M5.L3.K2");
        properOrderList.add("M5 .L3 K2 1880");
        properOrderList.add("M5 .L3 K2 1880 M"); // vol info 1880 M
        // first cutter L3 K2 1880
        properOrderList.add("M5 .L3 K2 1880 .Q2 1777");
        properOrderList.add("M5 .L3 K2 1882");
        properOrderList.add("M5 .L3 K2 D MAJ 1880");
        properOrderList.add("M5 .L3 K2 K.240"); // vol info K.240
        properOrderList.add("M5 .L3 K2 K.240 1880 F"); // vol info K.240 1880 F
        properOrderList.add("M5 .L3 K2 M V.1"); // vol info M V.1
        properOrderList.add("M5 .L3 K2 NO.1 1880"); // vol info NO.1
        properOrderList.add("M5 .L3 K2 OP.7:NO.6 1880");
        properOrderList.add("M5 .L3 K2 OP.7:NO.6 1882");
        properOrderList.add("M5 .L3 K2 OP.7:NO.51 1880");
        properOrderList.add("M5 .L3 K2 OP.8");
        properOrderList.add("M5 .L3 K2 OP.79");
        properOrderList.add("M5 .L3 K2 OP.789");
        properOrderList.add("M5 .L3 K2 Q2 1880"); // suffix Q2
        // first cutter L3 K2 Q2
        properOrderList.add("M5 .L3 K2 Q2 .A1");
        // first cutter L3 K2
        properOrderList.add("M5 .L3 K2 .Q2 MD:CRAPO*DMA 1981");
        properOrderList.add("M5 .L3 K2 VOL.1"); // vol info VOL.1

        // first cutter L3 vol/part info K.240
        properOrderList.add("M5 L3 K.3");
        properOrderList.add("M5 L3 K.25");
        properOrderList.add("M5 L3 K.240 1900");
        properOrderList.add("M5 L3 K.240 1900Z");
        properOrderList.add("M5 L3 K.240 1900Z M");
        properOrderList.add("M5 L3 K.240D B");
        // first cutter L3, second cutter
        properOrderList.add("M5 L3 .K240");

        properOrderList.add("M5 L3 K240 1900"); // ??
        properOrderList.add("M5 L3 K240 DB"); // ??
        // first cutter L3 second cutter K240x
        properOrderList.add("M5 .L3 K240A");
        properOrderList.add("M5 .L3 K240B M");
        properOrderList.add("M5 L3 V.188"); // vol info V.188
        // second cutter V188
        properOrderList.add("M5 L3 V188");

        properOrderList.add("M5 L31");
        properOrderList.add("M5 L31902");
        properOrderList.add("M5 M2");

        properOrderList.add("XXKD671.G53");
        properOrderList.add("XXKD671.G53 2012");
        properOrderList.add("XX KDE671.G53");
        properOrderList.add("XXKFA 30 1867 .A22");
        properOrderList.add("XXKFA 207.A940");
    }

    // TODO: make current ascending order "correct"
    // list of diabolical call numbers in current ascending order
    List<String> currentOrderList = new ArrayList<String>(80);
    {
        currentOrderList.add("AB9 L3");
        currentOrderList.add("AB9.22 L3");
        currentOrderList.add("ABR92.L3");

        currentOrderList.add("B8.14 L3");
        currentOrderList.add("B9 20th L3");
        currentOrderList.add("B9 2000");
        currentOrderList.add("B9 2000 35TH");
        currentOrderList.add("B9 2000 L3");
        currentOrderList.add("B9 L3");
        currentOrderList.add("B9 SVAR .L3");
        currentOrderList.add("B9.2 2000 L3");
        currentOrderList.add("B9.2 L3");
        currentOrderList.add("B9.22 L3");

        currentOrderList.add("B82 2000 L3");
        currentOrderList.add("B82 L3");
        currentOrderList.add("B82.2 1990 L3");
        currentOrderList.add("B82.2 L3");

        currentOrderList.add("B820 2000 L3");
        currentOrderList.add("B820.2 2000 L3");
        currentOrderList.add("B820.2 L3");
        currentOrderList.add("B822 L3");

        currentOrderList.add("B8200 L3");
        currentOrderList.add("B8220 L3");

        currentOrderList.add("M5 K4");
        currentOrderList.add("M5 .L");

        currentOrderList.add("M5 L299");
        currentOrderList.add("M5 L3 1902");
        currentOrderList.add("M5 L3 1902 V.2");
        currentOrderList.add("M5 L3 1902 V2");
        currentOrderList.add("M5 .L3 1902 V2 TANEYTOWN");
        currentOrderList.add("M5 L3 1902V");
        currentOrderList.add("M5 .L3 2000 .K2 1880");
        currentOrderList.add("M5 .L3 K.2,13 2001");

        // diverges from "proper" order here
        currentOrderList.add("M5 L3 K.3");
        currentOrderList.add("M5 L3 K.25");
        currentOrderList.add("M5 L3 K.240 1900");
        currentOrderList.add("M5 L3 K.240 1900Z");
        currentOrderList.add("M5 L3 K.240 1900Z M");
        currentOrderList.add("M5 L3 K.240D B");

        // second cutter K2 with suffix
        currentOrderList.add("M5.L3.K2");
        currentOrderList.add("M5 .L3 K2 1880");
        currentOrderList.add("M5 .L3 K2 1880 M");
        currentOrderList.add("M5 .L3 K2 1882");
        currentOrderList.add("M5 .L3 K2 D MAJ 1880");

        currentOrderList.add("M5 .L3 K2 K.240");
        currentOrderList.add("M5 .L3 K2 K.240 1880 F");
        currentOrderList.add("M5 .L3 K2 M V.1");
        currentOrderList.add("M5 .L3 K2 NO.1 1880");
        currentOrderList.add("M5 .L3 K2 OP.7:NO.6 1880");
        currentOrderList.add("M5 .L3 K2 OP.7:NO.6 1882");
        currentOrderList.add("M5 .L3 K2 OP.7:NO.51 1880");
        currentOrderList.add("M5 .L3 K2 OP.8");
        currentOrderList.add("M5 .L3 K2 OP.79");
        currentOrderList.add("M5 .L3 K2 OP.789");

        currentOrderList.add("M5 .L3 K2 Q2 1880");
        currentOrderList.add("M5 .L3 K2 VOL.1");

        // first cutter no suffix, simple second cutters
        currentOrderList.add("M5 L3 .K240");
        currentOrderList.add("M5 L3 K240 1900");
        currentOrderList.add("M5 .L3 K240A");
        currentOrderList.add("M5 .L3 K240B M");
        currentOrderList.add("M5 L3 K240 DB");

        // first cutter suffix
        currentOrderList.add("M5 .L3 K2 1880 .Q2 1777"); // TODO: Wrong - suffix vs. cutter norm
        currentOrderList.add("M5 .L3 K2 .Q2 MD:CRAPO*DMA 1981");
        currentOrderList.add("M5 .L3 K2 Q2 .A1");

        currentOrderList.add("M5 L3 V.188"); // title/part suffix
        currentOrderList.add("M5 L3 V188"); // second cutter

        // back to solid territory
        currentOrderList.add("M5 L31");
        currentOrderList.add("M5 L31902");
        currentOrderList.add("M5 M2");

        // XXKs
        currentOrderList.add("XXKD671.G53");
        currentOrderList.add("XXKD671.G53 2012");
        currentOrderList.add("XX KDE671.G53");
        currentOrderList.add("XXKFA 30 1867 .A22");
        currentOrderList.add("XXKFA 207.A940");
    }

    /**
     * test the sort of LC call numbers (via the shelf key) - diabolical data
//TODO: improve sort and check results against "correct" sort
     */
    @Test
    public final void testLCcallnumsSorted() throws IOException,
            ParserConfigurationException, SAXException
    {
        // compute list of sorted shelfkeys
        Map<String, String> shelfkey2callnum = new HashMap<String, String>(75);
        for (String callnum : diabolicalCallnumList)
        {
            UChicagoLCCallNumber lcCall = new UChicagoLCCallNumber(callnum);
            shelfkey2callnum.put(lcCall.getShelfKey(), callnum);
        }
        List<String> orderedKeys = new ArrayList<String>(shelfkey2callnum.keySet());
        Collections.sort(orderedKeys);

        for (int i = 0; i < orderedKeys.size(); i++)
        {
            UChicagoLCCallNumber currentOrderListCall = new UChicagoLCCallNumber(currentOrderList.get(i));
            // System.out.println(ordered.get(i));
            assertEquals("At position " + i + " in list:",
                    currentOrderListCall.getShelfKey(), orderedKeys.get(i));
            // assertEquals("At position " + i + " in list: ", properOrderList.get(i), shelfkey2callnum.get(ordered.get(i)));
        }
    }


    /**
     * unit test for UChicagoLCCallNumber.getClassification()
     */
    @Test
    public void testGetClassification()
    {
        String callnum = "M1 L33";
        assertEquals("M1", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "M211 .M93 K.240 1988"; // first cutter has period
        assertEquals("M211", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "PQ2678.K26 P54 1992"; // no space b4 cutter with period
        assertEquals("PQ2678", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "PR9199.4 .B3"; // class has float, first cutter has period
        assertEquals("PR9199.4", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "PR9199.3.L33 B6"; // decimal call no space before cutter
        assertEquals("PR9199.3", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "HC241.25F4 .D47";
        assertEquals("HC241.25", new UChicagoLCCallNumber(callnum).getClassification());

        // suffix before first cutter
        callnum = "PR92 1990 L33";
        assertEquals("PR92 1990", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "PR92 1844 .L33 1990"; // first cutter has period
        assertEquals("PR92 1844", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "PR92 1844.L33 1990"; // no space before cutter w period
        assertEquals("PR92 1844", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "PR92 1844L33 1990"; // no space before cutter w no period
        assertEquals("PR92 1844", new UChicagoLCCallNumber(callnum).getClassification());
        // period before cutter
        callnum = "M234.8 1827 .F666";
        assertEquals("M234.8 1827", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "PS3538 1974.L33";
        assertEquals("PS3538 1974", new UChicagoLCCallNumber(callnum).getClassification());
        // two cutters
        callnum = "PR9199.3 1920 L33 A6 1982";
        assertEquals("PR9199.3 1920", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "PR9199.3 1920 .L33 1475 .A6";
        assertEquals("PR9199.3 1920", new UChicagoLCCallNumber(callnum).getClassification());
        // decimal and period before cutter
        callnum = "HD38.25.F8 R87 1989";
        assertEquals("HD38.25", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "HF5549.5.T7 B294 1992";
        assertEquals("HF5549.5", new UChicagoLCCallNumber(callnum).getClassification());

        // suffix with letters
        callnum = "L666 15th A8";
        assertEquals("L666 15th", new UChicagoLCCallNumber(callnum).getClassification());

        // non-compliant cutter
        callnum = "M5 .L";
        assertEquals("M5", new UChicagoLCCallNumber(callnum).getClassification());

        // no cutter
        callnum = "B9 2000";
        assertEquals("B9 2000", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "B9 2000 35TH";
        assertEquals("B9 2000 35TH", new UChicagoLCCallNumber(callnum).getClassification());

        // wacko lc class suffixes
        callnum = "G3840 SVAR .H5"; // suffix letters only
        assertEquals("G3840 SVAR", new UChicagoLCCallNumber(callnum).getClassification());

        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002";
        assertEquals("G3824", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("G3841", new UChicagoLCCallNumber(callnum).getClassification());

        // space between LC class letters and numbers
        callnum = "PQ 8550.21.R57 V5 1992";
        // assertEquals("PQ 8550.21", new UChicagoLCCallNumber(callnum).getClassification());
        callnum = "HD 38.25.F8 R87 1989";
        // assertEquals("HD 38.25", new UChicagoLCCallNumber(callnum).getClassification());

        // XXK call numbers
        callnum = "XXKD671.G53 2012";
        assertEquals("KD671", new UChicagoLCCallNumber(callnum).getClassification());
    }

    /**
     * unit test to make sure the numeric portion of the LC classification is
     * parsed properly (before it's normalized)
     */
    @Test
    public void testLCClassNum()
    {
        String callnum = "M211 .M93 K.240 1988";
        assertEquals("211", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "M1 L33";
        assertEquals("1", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "PR92 1990 L33";
        assertEquals("92", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "PR92 .L33 1990";
        assertEquals("92", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "PR919 .L33 1990";
        assertEquals("919", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "PR9199 .A39";
        assertEquals("9199", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "PR9199.3.L33 B6";
        assertEquals("9199.3", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "PR9199.4 .B3";
        assertEquals("9199.4", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "PQ2678.I26 P54 1992";
        assertEquals("2678", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "HC241.25 .I4 D47";
        assertEquals("241.25", new UChicagoLCCallNumber(callnum).getClassDigits());

        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002";
        assertEquals("3824", new UChicagoLCCallNumber(callnum).getClassDigits());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("3841", new UChicagoLCCallNumber(callnum).getClassDigits());

        // XXK call numbers
        callnum = "XXKD671.G53 2012";
        assertEquals("671", new UChicagoLCCallNumber(callnum).getClassDigits());
    }

    /**
     * unit test to make sure the optional suffix portion of the LC
     * classification is parsed properly (before it's normalized)
     */
    @Test
    public void testLCClassSuffix()
    {

        String callnum = "M1 L33";
        assertNull(new UChicagoLCCallNumber(callnum).getClassSuffix());
        // decimal class before cutter
        callnum = "PR9199.48 .B3";
        assertNull(new UChicagoLCCallNumber(callnum).getClassSuffix());
        callnum = "PR9199.3.L33 B6";
        assertNull(new UChicagoLCCallNumber(callnum).getClassSuffix());
        // suffix after second cutter
        callnum = "PQ2678.I26 P54 1992";
        assertNull(new UChicagoLCCallNumber(callnum).getClassSuffix());
        // suffix after first cutter, before second cutter
        callnum = "PR9199.3.L33 2007 B6";
        assertNull(new UChicagoLCCallNumber(callnum).getClassSuffix());
        // two suffixes after second cutter
        callnum = "M211 .M93 K.240 1988";
        assertNull(new UChicagoLCCallNumber(callnum).getClassSuffix());

        // suffixes no period before cutter
        callnum = "PR92 1990 L33";
        assertEquals("1990", new UChicagoLCCallNumber(callnum).getClassSuffix());
        callnum = "MT37 2003M384";
        assertEquals("2003", new UChicagoLCCallNumber(callnum).getClassSuffix());
        // period before cutter
        callnum = "M234.8 1827 .F666";
        assertEquals("1827", new UChicagoLCCallNumber(callnum).getClassSuffix());
        callnum = "PS3538 1974.L33";
        assertEquals("1974", new UChicagoLCCallNumber(callnum).getClassSuffix());
        // two cutters
        callnum = "PR9199.3 1920 L33 A6 1982";
        assertEquals("1920", new UChicagoLCCallNumber(callnum).getClassSuffix());
        callnum = "PR9199.3 1920 .L33 1475 .A6";
        assertEquals("1920", new UChicagoLCCallNumber(callnum).getClassSuffix());
        callnum = "L666 15th A8"; // suffix with letters
        assertEquals("15th", new UChicagoLCCallNumber(callnum).getClassSuffix());
        // non-compliant cutter
        callnum = "M5 .L";
        assertNull("expected null LC class suffix for " + callnum, new UChicagoLCCallNumber(callnum).getClassSuffix());

        // no cutter
        callnum = "B9 2000";
        assertEquals("2000", new UChicagoLCCallNumber(callnum).getClassSuffix());
        callnum = "B9 2000 35TH";
        assertEquals("2000 35TH", new UChicagoLCCallNumber(callnum).getClassSuffix());

        // wacko lc class suffixes
        callnum = "G3840 SVAR .H5"; // suffix letters only
        assertEquals("SVAR", new UChicagoLCCallNumber(callnum).getClassSuffix());
    }

    /**
     * unit test for finding the first cutter for LC call numbers
     */
    @Test
    public void testFirstLCcutter()
    {
        String callnum = "M1 L33";
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "MT37 2003M384"; // no period before cutter
        assertEquals("M384", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "M234.8 1827 .F666"; // space period before cutter
        assertEquals("F666", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PS3538 1974.L33"; // period before cutter
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PQ2678.K26 P54 1992"; // no space b4 cutter with period
        assertEquals("K26", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "M211 .M93 K.240 1988"; // first cutter has period
        assertEquals("M93", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PR9199.3 1920 L33 A6 1982"; // two cutters
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // two cutter
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());

        callnum = "PR9199.4 .B3"; // class has fraction, first cutter has period
        assertEquals("B3", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PR9199.3.L33 B6"; // decimal call no space before cutter
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PR9199.3.L33 2007 B6"; // suffix after 1st cutter, b4 2nd cutter
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "HC241.25F4 .D47";
        assertEquals("F4", new UChicagoLCCallNumber(callnum).getFirstCutter());
        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002"; // suffix after second cutter
        assertEquals("G3", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals("G3", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "G3824 .G3 G4 S9 M2";
        assertEquals("G3", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("G3", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("C2", new UChicagoLCCallNumber(callnum).getFirstCutter());

        // LC classification suffixes
        callnum = "PR92 1990 L33"; // suffix before first cutter
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PR92 1844 .L33 1990"; // first cutter has period
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PR92 1844.L33 1990"; // no space before cutter w period
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "PR92 1844L33 1990"; // no space before cutter w no period
        assertEquals("L33", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "L666 15th A8"; // suffix with letters
        assertEquals("A8", new UChicagoLCCallNumber(callnum).getFirstCutter());

        // non-compliant LC cutters
        callnum = "M5 .L";
        assertEquals("L", new UChicagoLCCallNumber(callnum).getFirstCutter());

        // wacko lc class suffixes
        callnum = "G3840 SVAR .H5"; // class suffix letters only
        assertEquals("H5", new UChicagoLCCallNumber(callnum).getFirstCutter());

        // no cutter
        callnum = "B9 2000";
        assertNull("expected null LC cutter for " + callnum, new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "B9 2000 35TH";
        assertNull("expected null LC cutter for " + callnum, new UChicagoLCCallNumber(callnum).getFirstCutter());

        // decimal in class and period before cutter
        callnum = "PQ8550.21.R57 V5 1992";
        assertEquals("R57", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "HF5549.5.T7 B294 1992";
        assertEquals("T7", new UChicagoLCCallNumber(callnum).getFirstCutter());
        // space between LC class letters and numbers
        callnum = "PQ 8550.21.R57 V5 1992";
        // assertEquals("R57", new UChicagoLCCallNumber(callnum).getFirstCutter());
        callnum = "HF 5549.5.T7 B294 1992";
        // assertEquals("T7", new UChicagoLCCallNumber(callnum).getFirstCutter());
    }

    /**
     * unit test for finding optional suffix after LC first cutter
     */
    @Test
    public void testFirstLCcutterSuffix()
    {
        String callnum = "M1 L33";
        assertNull(new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "HC241.25F4 .D47"; // two cutters
        assertNull(new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "PQ2678.K26 P54 1992"; // second cutter
        assertNull(new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "PR9199.3 1920 L33 A6 1982"; // suffix after class
        assertNull(new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());

        // first cutter suffix present
        callnum = "M5 L3 1902";
        assertEquals("1902", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M5 L3 1902V";
        assertEquals("1902V", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "PR9199.3.L33 2007 B6"; // suffix after 1st cutter
        assertEquals("2007 B6", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "PR9199.3.L33 2007 .B6"; // suffix after 1st cutter, w 2nd cutter
        assertEquals("2007", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "PR92 1844 .L33 1990"; // class has suffix
        assertEquals("1990", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // 2 cutters, suffix after class, 1st cutter
        assertEquals("1475", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M211 .M93 K.240"; // weird suffix
        assertEquals("K.240", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M211 .M93 BMW240"; // weird suffix
        assertEquals("BMW240", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());

        // double suffix
        callnum = "P5 L33 1475 vol.1"; // double suffix
        assertEquals("1475 vol.1", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "P5 L33 1475 vol 1"; // double suffix
        assertEquals("1475 vol 1", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M211 .M93 K.240 1988"; // weird suffix
        assertEquals("K.240 1988", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M5 L3 1902 V.2";
        assertEquals("1902 V.2", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M5 L3 1902 V2";
        assertEquals("1902 V2", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M5 .L3 1902 V2 TANEYTOWN";
        assertEquals("1902 V2 TANEYTOWN", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());

        // extra suffixes with and without 2nd cutter
        callnum = "P5 L33 1475 vol.1 A1"; // triple suffix
        assertEquals("1475 vol.1 A1", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "P5 L33 1475 vol.1 .A1"; // double suffix
        assertEquals("1475 vol.1", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "P5 L33 1475 vol 1 A1"; // triple suffix
        assertEquals("1475 vol 1 A1", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "P5 L33 1475 vol 1 .A1"; // double suffix w cutter
        assertEquals("1475 vol 1", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M211 .M93 K.240 1988 A1"; // weird suffix
        assertEquals("K.240 1988 A1", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "M211 .M93 K.240 1988 .A1"; // weird suffix w cutter
        assertEquals("K.240 1988", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());

        // suffix after first cutter looks like a cutter
        callnum = "MT130 .M93 K96 W83 1988";
        assertNull(new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("K96", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "ML410 .M8 L25 M95 1995";
        assertNull(new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "ML410 .M8 L25 .M95 1995";
        assertEquals("L25", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("G4 S9", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("S24", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals("S5 1863", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());

        // suffix starts with a slash
        callnum = "HE5.215 .N9/PT.A"; // slash
        // assertEquals("PT.A", new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());

        // test for wacky endless recursion bug
        callnum = "D400.H23 A35 Hamilton Frederick Spencer Lord 1856";
        assertEquals(null, new UChicagoLCCallNumber(callnum).getFirstCutterSuffix());

    }

    /**
     * unit test for finding the second cutter for LC call numbers
     */
    @Test
    public void testSecondLCcutter()
    {
        // no second cutter
        String callnum = "M1 L33"; // no second cutter
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "PR92 1844 .L33 1990"; // class and 1st cutter have suffix
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "M211 .M93 K.240"; // weird 1st cutter suffix
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "M211 .M93 BMW240"; // weird 1st cutter suffix
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        // double suffix
        callnum = "P5 L33 1475 vol.1"; // double 1st cutter suffix
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "P5 L33 1475 vol 1"; // double 1st cutter suffix
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "M211 .M93 K.240 1988"; // weird double 1st cutter suffix
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());

        // two cutters
        callnum = "A1 B2 C33"; // space no period
        assertEquals("C33", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "A1 B2 .C33"; // space period
        assertEquals("C33", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "A1 B2.C33"; // no space period
        assertEquals("C33", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "A1 B2C33"; // no space or period
        assertEquals("C33", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "HC241.25F4 .D47"; // more parsing fun
        assertEquals("D47", new UChicagoLCCallNumber(callnum).getSecondCutter());

        // first cutter suffix present
        callnum = "PR9199.3.L33 2007 B6"; // suffix b/t cutters no period
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "PR9199.3.L33 2007 .B6"; // suffix b/t cutters
        assertEquals("B6", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // 2 cutters, suffix after class, 1st cutter
        assertEquals("A6", new UChicagoLCCallNumber(callnum).getSecondCutter());

        // suffix after second cutter
        callnum = "PR9199.3 1920 L33 A6 1982";
        assertEquals("A6", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "PQ2678.K26 P54 1992";
        assertEquals("P54", new UChicagoLCCallNumber(callnum).getSecondCutter());

        // double suffix after first cutter
        callnum = "P5 L33 1475 vol.1 A1"; // double suffix, but no period
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "P5 L33 1475 vol.1 .A1"; // double suffix
        assertEquals("A1", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "P5 L33 1475 vol 1 A1"; // double suffix, but no period
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "P5 L33 1475 vol 1 .A1"; // double suffix
        assertEquals("A1", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "M211 .M93 K.240 1988 A1"; // weird suffix, but no period
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "M211 .M93 K.240 1988 .A1"; // weird suffix
        assertEquals("A1", new UChicagoLCCallNumber(callnum).getSecondCutter());

        // first cutter suffix looks like cutter
        callnum = "MT130 .M93 K96 W83 1988";
        assertEquals("K96", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("W83", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("M2", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("U5", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals("W5", new UChicagoLCCallNumber(callnum).getSecondCutter());
        callnum = "D400.H23 A35 Hamilton Frederick Spencer Lord 1856";
        assertEquals("A35", new UChicagoLCCallNumber(callnum).getSecondCutter());

    }

    /**
     * unit test for finding optional suffix after LC first cutter
     */
    @Test
    public void testSecondLCcutterSuffix()
    {
        String callnum = "M1 L33";
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "HC241.25F4 .D47"; // two cutters
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "PR9199.3.L33 2007 B6"; // suffix after 1st cutter, b4 2nd cutter
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "PR92 1844 .L33 1990"; // class has suffix
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // 2 cutters, suffix after class, 1st cutter
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "M211 .M93 K.240 1988"; // weird suffix after first cutter
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());

        // second cutter suffix present
        callnum = "PR9199.3 1920 L33 A6 1982"; // suffix after second cutter
        assertEquals("1982", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "PQ2678.K26 P54 1992"; // no second cutter
        assertEquals("1992", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "M453 .Z29 Q1 L V.2";
        assertEquals("L V.2", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "M1001 .A13 S9 OP.7:NO.6 1944";
        assertEquals("OP.7:NO.6 1944", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "ML410 .M9 P29 1941 M";
        assertEquals("1941 M", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "M857 .K93 H2 OP.79";
        assertEquals("OP.79", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "G3824 .P5 1776 .E2 1976"; // first cutter suffix too
        assertEquals("1976", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "G3822 .G384 1925 .U5 TANEYTOWN"; // first cutter suffix too
        assertEquals("TANEYTOWN", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "M857 .R348 P2 D MAJ 1989";
        assertEquals("D MAJ 1989", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "LD4881 .S16588 A936A";
        assertEquals("A", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "M857 .M93 S412B M";
        assertEquals("B M", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());

        // cutter like suffixes
        callnum = "MT130 .M93 K96 W83 1988";
        assertEquals("W83 1988", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("1988", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "ML410 .M8 L25 M95 1995";
        assertEquals("M95 1995", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "G3824 .G3 G4 S9 .M2";
        assertNull(new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("MD:CRAPO*DMA 1981", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals("2002", new UChicagoLCCallNumber(callnum).getSecondCutterSuffix());
    }


    /**
     * unit test for getLCShelfKey
     */
    @Test
    public void testUChicagoLCShelfKey()
    {
        String callnum = "M1 L33";
        assertEquals("M   0001.000000 L0.330000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.4 .B3"; // class has fraction, first cutter has period
        assertEquals("PR  9199.400000 B0.300000", new UChicagoLCCallNumber(callnum).getShelfKey());

        // LC classification suffixes
        callnum = "PR92 1990 L33"; // suffix before first cutter
        assertEquals("PR  0092.000000 001990 L0.330000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR92 1844 .L33 1990"; // first cutter has period
        assertEquals("PR  0092.000000 001844 L0.330000 001990", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR92 1844.L33 1990"; // no space before cutter w period
        assertEquals("PR  0092.000000 001844 L0.330000 001990", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR92 1844L33 1990"; // no space before cutter w no period
        assertEquals("PR  0092.000000 001844 L0.330000 001990", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "L666 15th A8"; // suffix with letters
        assertEquals("L   0666.000000 000015TH A0.800000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "MT37 2003M384"; // no period before cutter
        assertEquals("MT  0037.000000 002003 M0.384000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M234.8 1827 .F666"; // space period before cutter
        assertEquals("M   0234.800000 001827 F0.666000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PS3538 1974.L33"; // period before cutter
        assertEquals("PS  3538.000000 001974 L0.330000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3 1920 L33 A6 1982"; // two cutters, nothing b/t 'em
        assertEquals("PR  9199.300000 001920 L0.330000 A0.600000 001982", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // two cutters suffix b/t
        assertEquals("PR  9199.300000 001920 L0.330000 001475 A0.600000", new UChicagoLCCallNumber(callnum).getShelfKey());

        // first cutter suffix
        callnum = "M5 L3 1902";
        assertEquals("M   0005.000000 L0.300000 001902", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .M9 1900Z";
        assertEquals("M   1001.000000 M0.900000 001900Z", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .M9 K.551 1900Z M";
        assertEquals("M   1001.000000 M0.900000 K.000551 001900Z M", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .M9 K.173D B";
        assertEquals("M   1001.000000 M0.900000 K.000173D B", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "G3840 SVAR .H5";
        assertEquals("G   3840.000000 SVAR H0.500000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "G3841 .C2 1935 .M3"; // with second cutter
        assertEquals("G   3841.000000 C0.200000 001935 M0.300000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PQ2678.K26 P54 1992"; // no space b4 cutter with period
        assertEquals("PQ  2678.000000 K0.260000 P0.540000 001992", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M211 .M93 K.240 1988"; // first cutter has period
        assertEquals("M   0211.000000 M0.930000 K.000240 001988", new UChicagoLCCallNumber(callnum).getShelfKey());

        // second cutter
        callnum = "PQ2678.K26 P54"; // 2 cutters
        assertEquals("PQ  2678.000000 K0.260000 P0.540000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3.L33 B6"; // decimal call no space before cutter
        assertEquals("PR  9199.300000 L0.330000 B0.600000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3.L33 2007 B6"; // double suffix after 1st cutter
        assertEquals("PR  9199.300000 L0.330000 002007 B000006", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "PR9199.3.L33 2007 .B6"; // suffix after 1st cutter, b4 2nd
                                            // cutter
        assertEquals("PR  9199.300000 L0.330000 002007 B0.600000",
                new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "HC241.25F4 .D47";
        assertEquals("HC  0241.250000 F0.400000 D0.470000", new UChicagoLCCallNumber(callnum).getShelfKey());

        // second cutter suffix
        callnum = "M453 .Z29 Q1 L V.2";
        assertEquals("M   0453.000000 Z0.290000 Q0.100000 L V.000002", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .A13 S9 OP.7:NO.6 1944";
        assertEquals(
                "M   1001.000000 A0.130000 S0.900000 OP.000007:NO.000006 001944", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "ML410 .M9 P29 1941 M";
        assertEquals("ML  0410.000000 M0.900000 P0.290000 001941 M", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M857 .K93 H2 OP.79";
        assertEquals("M   0857.000000 K0.930000 H0.200000 OP.000079", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "G3824 .P5 1776 .E2 1976"; // first cutter suffix too
        assertEquals("G   3824.000000 P0.500000 001776 E0.200000 001976", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "G3822 .G384 1925 .U5 TANEYTOWN"; // first cutter suffix too
        assertEquals("G   3822.000000 G0.384000 001925 U0.500000 TANEYTOWN", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M857 .R348 P2 D MAJ 1989";
        assertEquals("M   0857.000000 R0.348000 P0.200000 D MAJ 001989", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "LD4881 .S16588 A936A";
        assertEquals("LD  4881.000000 S0.165880 A0.936000 A", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M857 .M93 S412B M";
        assertEquals("M   0857.000000 M0.930000 S0.412000 B M", new UChicagoLCCallNumber(callnum).getShelfKey());

        // suffix after first cutter looks like a cutter
        callnum = "MT130 .M93 K96 W83 1988";
        assertEquals("MT  0130.000000 M0.930000 K0.960000 W000083 001988", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("MT  0130.000000 M0.930000 K000096 W0.830000 001988", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "ML410 .M8 L25 M95 1995";
        assertEquals("ML  0410.000000 M0.800000 L0.250000 M000095 001995", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "ML410 .M8 L25 .M95 1995";
        assertEquals("ML  0410.000000 M0.800000 L000025 M0.950000 001995", new UChicagoLCCallNumber(callnum).getShelfKey());
        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002"; // suffix after second cutter
        assertEquals(
                "G   3824.000000 G0.300000 S0.500000 001863 W000005 002002", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals(
                "G   3824.000000 G0.300000 S000005 001863 W0.500000 002002", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "G3824 .G3 G4 S9 M2";
        assertEquals("G   3824.000000 G0.300000 G0.400000 S000009 M000002", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("G   3824.000000 G0.300000 G000004 S000009 M0.200000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals(
                "G   3841.000000 C0.200000 S000024 U0.500000 MD:CRAPO*DMA 001981", new UChicagoLCCallNumber(callnum).getShelfKey());

        // wacko
        callnum = "M1001 .H";
        assertEquals("M   1001.000000 H0.000000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "LD6353 1886";
        assertEquals("LD  6353.000000 001886", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "M1001 .M939 S.3,13 2001";
        assertEquals("M   1001.000000 M0.939000 S.000003,000013 002001", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "LD6329 1903 35TH";
        assertEquals("LD  6329.000000 001903 000035TH", new UChicagoLCCallNumber(callnum).getShelfKey());

        // Classification with no cutter (users search this way)
        // Must define sort keys, belongs in LCCallNumber
        /*
        callnum = "BF199";
        assertEquals("BF  0199.000000 00.000000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "BF199.";
        assertEquals("BF  0199.000000 00.000000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "BF199.A1J7";
        assertEquals("BF  0199.000000 A0.100000 J0.700000", new UChicagoLCCallNumber(callnum).getShelfKey());
        */

        // XXK call numbers
        callnum = "XXKD671.G53";
        UChicagoLCCallNumber callObj = new UChicagoLCCallNumber(callnum);
        //assertTrue(callObj.xxkFlag);
        assertTrue(callObj.isXXK());
        assertEquals("KD671.G53", callObj.raw);
        assertEquals("KD", callObj.classLetters);
        assertEquals("KD", callObj.getClassLetters());
        assertEquals("XXKD  0671.000000 G0.530000", callObj.getShelfKey());
        callnum = "XX KD671.G53";
        assertEquals("XXKD  0671.000000 G0.530000", new UChicagoLCCallNumber(callnum).getShelfKey());
        callnum = "XXKD671.G53 2012";
        assertEquals("XXKD  0671.000000 G0.530000 002012", new UChicagoLCCallNumber(callnum).getShelfKey());

        //XX with 3 LC letters: classification part of key should be 1 character wider than normal
        callnum = "XXKFA 207.A940";
        assertEquals("XXKFA 0207.000000 A0.940000", new UChicagoLCCallNumber(callnum).getShelfKey());
        //XX with 33 LC letters, plus year suffix
        callnum = "XXKFA 30 1867 .A22"; // first cutter has period
        assertEquals("XXKFA 0030.000000 001867 A0.220000", new UChicagoLCCallNumber(callnum).getShelfKey());
    }

    @Test
    public void testUChicagoXXKSimpleConstructor() {
        String callnum = "XXKD671.G53";
        UChicagoLCCallNumber ucCall = new UChicagoLCCallNumber();
        ucCall.parse(callnum);
        assertTrue(ucCall.isXXK());
        assertEquals(ucCall.raw, "KD671.G53");
    }

    @Test
    public void testUChicagoXXKStringConstructor() {
        String callnum = "XXKD671.G53";
        UChicagoLCCallNumber ucCall = new UChicagoLCCallNumber(callnum);
        assertTrue(ucCall.isXXK());
        assertEquals(ucCall.raw, "KD671.G53");
    }

}
