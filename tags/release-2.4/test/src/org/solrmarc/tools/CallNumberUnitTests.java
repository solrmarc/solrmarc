package org.solrmarc.tools;

import static org.solrmarc.tools.CallNumUtils.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

public class CallNumberUnitTests
{

    /**
     * unit test for Utils.normalizeFloat()
     */
    @Test
    public void testNormalizeFloat()
    {
        String normed = normalizeFloat("1", 5, 3);
        assertEquals("normalized incorrect", "00001.000", normed);
        normed = normalizeFloat("22", 3, -1);
        assertEquals("normalized incorrect", "022", normed);
        normed = normalizeFloat("33.3", 1, -88);
        assertEquals("normalized incorrect", "33.3", normed);
        normed = normalizeFloat("4040.040", 4, -4);
        assertEquals("normalized incorrect", "4040.04", normed);
        normed = normalizeFloat(".55", 2, 3);
        assertEquals("normalized incorrect", "00.550", normed);
        normed = normalizeFloat("666666.333", 6, -1);
        assertEquals("normalized incorrect", "666666.333", normed);
        normed = normalizeFloat("20.999999999", 2, -1);
        assertEquals("normalized incorrect", "20.999999999", normed);
        normed = normalizeFloat("20.999999999", 2, 4);
        assertEquals("normalized incorrect", "21.0000", normed);
        normed = normalizeFloat("20.999999999", 2, 0);
        assertEquals("normalized incorrect", "21", normed);
        normed = normalizeFloat("0.6666", -1, 3);
        assertEquals("normalized incorrect", ".667", normed);
    }

    /**
     * unit test for Utils.getLCStringB4FirstCutter()
     */
    @Test
    public void testLCStringB4FirstCutter()
    {
        String callnum = "M1 L33";
        assertEquals("M1", getLCB4FirstCutter(callnum));
        callnum = "M211 .M93 K.240 1988"; // first cutter has period
        assertEquals("M211", getLCB4FirstCutter(callnum));
        callnum = "PQ2678.K26 P54 1992"; // no space b4 cutter with period
        assertEquals("PQ2678", getLCB4FirstCutter(callnum));
        callnum = "PR9199.4 .B3"; // class has float, first cutter has period
        assertEquals("PR9199.4", getLCB4FirstCutter(callnum));
        callnum = "PR9199.3.L33 B6"; // decimal call no space before cutter
        assertEquals("PR9199.3", getLCB4FirstCutter(callnum));
        callnum = "HC241.25F4 .D47";
        assertEquals("HC241.25", getLCB4FirstCutter(callnum));

        // suffix before first cutter
        callnum = "PR92 1990 L33";
        assertEquals("PR92 1990", getLCB4FirstCutter(callnum));
        callnum = "PR92 1844 .L33 1990"; // first cutter has period
        assertEquals("PR92 1844", getLCB4FirstCutter(callnum));
        callnum = "PR92 1844.L33 1990"; // no space before cutter w period
        assertEquals("PR92 1844", getLCB4FirstCutter(callnum));
        callnum = "PR92 1844L33 1990"; // no space before cutter w no period
        assertEquals("PR92 1844", getLCB4FirstCutter(callnum));
        // period before cutter
        callnum = "M234.8 1827 .F666";
        assertEquals("M234.8 1827", getLCB4FirstCutter(callnum));
        callnum = "PS3538 1974.L33";
        assertEquals("PS3538 1974", getLCB4FirstCutter(callnum));
        // two cutters
        callnum = "PR9199.3 1920 L33 A6 1982";
        assertEquals("PR9199.3 1920", getLCB4FirstCutter(callnum));
        callnum = "PR9199.3 1920 .L33 1475 .A6";
        assertEquals("PR9199.3 1920", getLCB4FirstCutter(callnum));
        // decimal and period before cutter
        callnum = "HD38.25.F8 R87 1989";
        assertEquals("HD38.25", getLCB4FirstCutter(callnum));
        callnum = "HF5549.5.T7 B294 1992";
        assertEquals("HF5549.5", getLCB4FirstCutter(callnum));

        // suffix with letters
        callnum = "L666 15th A8";
        assertEquals("L666 15th", getLCB4FirstCutter(callnum));

        // non-compliant cutter
        callnum = "M5 .L";
        assertEquals("M5", getLCB4FirstCutter(callnum));

        // no cutter
        callnum = "B9 2000";
        assertEquals("B9 2000", getLCB4FirstCutter(callnum));
        callnum = "B9 2000 35TH";
        assertEquals("B9 2000 35TH", getLCB4FirstCutter(callnum));

        // wacko lc class suffixes
        callnum = "G3840 SVAR .H5"; // suffix letters only
        assertEquals("G3840 SVAR", getLCB4FirstCutter(callnum));

        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002";
        assertEquals("G3824", getLCB4FirstCutter(callnum));
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("G3841", getLCB4FirstCutter(callnum));

        // space between LC class letters and numbers
        callnum = "PQ 8550.21.R57 V5 1992";
        // assertEquals("PQ 8550.21", getLCB4FirstCutter(callnum));
        callnum = "HD 38.25.F8 R87 1989";
        // assertEquals("HD 38.25", getLCB4FirstCutter(callnum));
    }

    /**
     * unit test to make sure the numeric portion of the LC classification is
     * parsed properly (before it's normalized)
     */
    @Test
    public void testLCClassNum()
    {
        String callnum = "M211 .M93 K.240 1988";
        assertEquals("211", getLCClassDigits(callnum));
        callnum = "M1 L33";
        assertEquals("1", getLCClassDigits(callnum));
        callnum = "PR92 1990 L33";
        assertEquals("92", getLCClassDigits(callnum));
        callnum = "PR92 .L33 1990";
        assertEquals("92", getLCClassDigits(callnum));
        callnum = "PR919 .L33 1990";
        assertEquals("919", getLCClassDigits(callnum));
        callnum = "PR9199 .A39";
        assertEquals("9199", getLCClassDigits(callnum));
        callnum = "PR9199.3.L33 B6";
        assertEquals("9199.3", getLCClassDigits(callnum));
        callnum = "PR9199.4 .B3";
        assertEquals("9199.4", getLCClassDigits(callnum));
        callnum = "PQ2678.I26 P54 1992";
        assertEquals("2678", getLCClassDigits(callnum));
        callnum = "HC241.25 .I4 D47";
        assertEquals("241.25", getLCClassDigits(callnum));

        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002";
        assertEquals("3824", getLCClassDigits(callnum));
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("3841", getLCClassDigits(callnum));
    }

    /**
     * unit test to make sure the optional suffix portion of the LC
     * classification is parsed properly (before it's normalized)
     */
    @Test
    public void testLCClassSuffix()
    {

        String callnum = "M1 L33";
        assertNull(getLCClassSuffix(callnum));
        // decimal class before cutter
        callnum = "PR9199.48 .B3";
        assertNull(getLCClassSuffix(callnum));
        callnum = "PR9199.3.L33 B6";
        assertNull(getLCClassSuffix(callnum));
        // suffix after second cutter
        callnum = "PQ2678.I26 P54 1992";
        assertNull(getLCClassSuffix(callnum));
        // suffix after first cutter, before second cutter
        callnum = "PR9199.3.L33 2007 B6";
        assertNull(getLCClassSuffix(callnum));
        // two suffixes after second cutter
        callnum = "M211 .M93 K.240 1988";
        assertNull(getLCClassSuffix(callnum));

        // suffixes no period before cutter
        callnum = "PR92 1990 L33";
        assertEquals("1990", getLCClassSuffix(callnum));
        callnum = "MT37 2003M384";
        assertEquals("2003", getLCClassSuffix(callnum));
        // period before cutter
        callnum = "M234.8 1827 .F666";
        assertEquals("1827", getLCClassSuffix(callnum));
        callnum = "PS3538 1974.L33";
        assertEquals("1974", getLCClassSuffix(callnum));
        // two cutters
        callnum = "PR9199.3 1920 L33 A6 1982";
        assertEquals("1920", getLCClassSuffix(callnum));
        callnum = "PR9199.3 1920 .L33 1475 .A6";
        assertEquals("1920", getLCClassSuffix(callnum));
        callnum = "L666 15th A8"; // suffix with letters
        assertEquals("15th", getLCClassSuffix(callnum));
        // non-compliant cutter
        callnum = "M5 .L";
        assertNull("expected null LC class suffix for " + callnum, getLCClassSuffix(callnum));

        // no cutter
        callnum = "B9 2000";
        assertEquals("2000", getLCClassSuffix(callnum));
        callnum = "B9 2000 35TH";
        assertEquals("2000 35TH", getLCClassSuffix(callnum));

        // wacko lc class suffixes
        callnum = "G3840 SVAR .H5"; // suffix letters only
        assertEquals("SVAR", getLCClassSuffix(callnum));
    }

    /**
     * unit test for finding the first cutter for LC call numbers
     */
    @Test
    public void testFirstLCcutter()
    {
        String callnum = "M1 L33";
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "MT37 2003M384"; // no period before cutter
        assertEquals("M384", getFirstLCcutter(callnum));
        callnum = "M234.8 1827 .F666"; // space period before cutter
        assertEquals("F666", getFirstLCcutter(callnum));
        callnum = "PS3538 1974.L33"; // period before cutter
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "PQ2678.K26 P54 1992"; // no space b4 cutter with period
        assertEquals("K26", getFirstLCcutter(callnum));
        callnum = "M211 .M93 K.240 1988"; // first cutter has period
        assertEquals("M93", getFirstLCcutter(callnum));
        callnum = "PR9199.3 1920 L33 A6 1982"; // two cutters
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // two cutter
        assertEquals("L33", getFirstLCcutter(callnum));

        callnum = "PR9199.4 .B3"; // class has fraction, first cutter has period
        assertEquals("B3", getFirstLCcutter(callnum));
        callnum = "PR9199.3.L33 B6"; // decimal call no space before cutter
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "PR9199.3.L33 2007 B6"; // suffix after 1st cutter, b4 2nd cutter
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "HC241.25F4 .D47";
        assertEquals("F4", getFirstLCcutter(callnum));
        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002"; // suffix after second cutter
        assertEquals("G3", getFirstLCcutter(callnum));
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals("G3", getFirstLCcutter(callnum));
        callnum = "G3824 .G3 G4 S9 M2";
        assertEquals("G3", getFirstLCcutter(callnum));
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("G3", getFirstLCcutter(callnum));
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("C2", getFirstLCcutter(callnum));

        // LC classification suffixes
        callnum = "PR92 1990 L33"; // suffix before first cutter
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "PR92 1844 .L33 1990"; // first cutter has period
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "PR92 1844.L33 1990"; // no space before cutter w period
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "PR92 1844L33 1990"; // no space before cutter w no period
        assertEquals("L33", getFirstLCcutter(callnum));
        callnum = "L666 15th A8"; // suffix with letters
        assertEquals("A8", getFirstLCcutter(callnum));

        // non-compliant LC cutters
        callnum = "M5 .L";
        assertEquals("L", getFirstLCcutter(callnum));

        // wacko lc class suffixes
        callnum = "G3840 SVAR .H5"; // class suffix letters only
        assertEquals("H5", getFirstLCcutter(callnum));

        // no cutter
        callnum = "B9 2000";
        assertNull("expected null LC cutter for " + callnum, getFirstLCcutter(callnum));
        callnum = "B9 2000 35TH";
        assertNull("expected null LC cutter for " + callnum, getFirstLCcutter(callnum));

        // decimal in class and period before cutter
        callnum = "PQ8550.21.R57 V5 1992";
        assertEquals("R57", getFirstLCcutter(callnum));
        callnum = "HF5549.5.T7 B294 1992";
        assertEquals("T7", getFirstLCcutter(callnum));
        // space between LC class letters and numbers
        callnum = "PQ 8550.21.R57 V5 1992";
        // assertEquals("R57", getFirstLCcutter(callnum));
        callnum = "HF 5549.5.T7 B294 1992";
        // assertEquals("T7", getFirstLCcutter(callnum));
    }

    /**
     * unit test for finding optional suffix after LC first cutter
     */
    @Test
    public void testFirstLCcutterSuffix()
    {
        String callnum = "M1 L33";
        assertNull(getFirstLCcutterSuffix(callnum));
        callnum = "HC241.25F4 .D47"; // two cutters
        assertNull(getFirstLCcutterSuffix(callnum));
        callnum = "PQ2678.K26 P54 1992"; // second cutter
        assertNull(getFirstLCcutterSuffix(callnum));
        callnum = "PR9199.3 1920 L33 A6 1982"; // suffix after class
        assertNull(getFirstLCcutterSuffix(callnum));

        // first cutter suffix present
        callnum = "M5 L3 1902";
        assertEquals("1902", getFirstLCcutterSuffix(callnum));
        callnum = "M5 L3 1902V";
        assertEquals("1902V", getFirstLCcutterSuffix(callnum));
        callnum = "PR9199.3.L33 2007 B6"; // suffix after 1st cutter
        assertEquals("2007 B6", getFirstLCcutterSuffix(callnum));
        callnum = "PR9199.3.L33 2007 .B6"; // suffix after 1st cutter, w 2nd cutter
        assertEquals("2007", getFirstLCcutterSuffix(callnum));
        callnum = "PR92 1844 .L33 1990"; // class has suffix
        assertEquals("1990", getFirstLCcutterSuffix(callnum));
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // 2 cutters, suffix after class, 1st cutter
        assertEquals("1475", getFirstLCcutterSuffix(callnum));
        callnum = "M211 .M93 K.240"; // weird suffix
        assertEquals("K.240", getFirstLCcutterSuffix(callnum));
        callnum = "M211 .M93 BMW240"; // weird suffix
        assertEquals("BMW240", getFirstLCcutterSuffix(callnum));

        // double suffix
        callnum = "P5 L33 1475 vol.1"; // double suffix
        assertEquals("1475 vol.1", getFirstLCcutterSuffix(callnum));
        callnum = "P5 L33 1475 vol 1"; // double suffix
        assertEquals("1475 vol 1", getFirstLCcutterSuffix(callnum));
        callnum = "M211 .M93 K.240 1988"; // weird suffix
        assertEquals("K.240 1988", getFirstLCcutterSuffix(callnum));
        callnum = "M5 L3 1902 V.2";
        assertEquals("1902 V.2", getFirstLCcutterSuffix(callnum));
        callnum = "M5 L3 1902 V2";
        assertEquals("1902 V2", getFirstLCcutterSuffix(callnum));
        callnum = "M5 .L3 1902 V2 TANEYTOWN";
        assertEquals("1902 V2 TANEYTOWN", getFirstLCcutterSuffix(callnum));

        // extra suffixes with and without 2nd cutter
        callnum = "P5 L33 1475 vol.1 A1"; // triple suffix
        assertEquals("1475 vol.1 A1", getFirstLCcutterSuffix(callnum));
        callnum = "P5 L33 1475 vol.1 .A1"; // double suffix
        assertEquals("1475 vol.1", getFirstLCcutterSuffix(callnum));
        callnum = "P5 L33 1475 vol 1 A1"; // triple suffix
        assertEquals("1475 vol 1 A1", getFirstLCcutterSuffix(callnum));
        callnum = "P5 L33 1475 vol 1 .A1"; // double suffix w cutter
        assertEquals("1475 vol 1", getFirstLCcutterSuffix(callnum));
        callnum = "M211 .M93 K.240 1988 A1"; // weird suffix
        assertEquals("K.240 1988 A1", getFirstLCcutterSuffix(callnum));
        callnum = "M211 .M93 K.240 1988 .A1"; // weird suffix w cutter
        assertEquals("K.240 1988", getFirstLCcutterSuffix(callnum));

        // suffix after first cutter looks like a cutter
        callnum = "MT130 .M93 K96 W83 1988";
        assertNull(getFirstLCcutterSuffix(callnum));
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("K96", getFirstLCcutterSuffix(callnum));
        callnum = "ML410 .M8 L25 M95 1995";
        assertNull(getFirstLCcutterSuffix(callnum));
        callnum = "ML410 .M8 L25 .M95 1995";
        assertEquals("L25", getFirstLCcutterSuffix(callnum));
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("G4 S9", getFirstLCcutterSuffix(callnum));
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("S24", getFirstLCcutterSuffix(callnum));
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals("S5 1863", getFirstLCcutterSuffix(callnum));

        // suffix starts with a slash
        callnum = "HE5.215 .N9/PT.A"; // slash
        // assertEquals("PT.A", getFirstLCcutterSuffix(callnum));
        
        // test for wacky endless recursion bug
        callnum = "D400.H23 A35 Hamilton Frederick Spencer Lord 1856"; 
        assertEquals(null, getFirstLCcutterSuffix(callnum));

    }

    /**
     * unit test for finding the second cutter for LC call numbers
     */
    @Test
    public void testSecondLCcutter()
    {
        // no second cutter
        String callnum = "M1 L33"; // no second cutter
        assertNull(getSecondLCcutter(callnum));
        callnum = "PR92 1844 .L33 1990"; // class and 1st cutter have suffix
        assertNull(getSecondLCcutter(callnum));
        callnum = "M211 .M93 K.240"; // weird 1st cutter suffix
        assertNull(getSecondLCcutter(callnum));
        callnum = "M211 .M93 BMW240"; // weird 1st cutter suffix
        assertNull(getSecondLCcutter(callnum));
        // double suffix
        callnum = "P5 L33 1475 vol.1"; // double 1st cutter suffix
        assertNull(getSecondLCcutter(callnum));
        callnum = "P5 L33 1475 vol 1"; // double 1st cutter suffix
        assertNull(getSecondLCcutter(callnum));
        callnum = "M211 .M93 K.240 1988"; // weird double 1st cutter suffix
        assertNull(getSecondLCcutter(callnum));

        // two cutters
        callnum = "A1 B2 C33"; // space no period
        assertEquals("C33", getSecondLCcutter(callnum));
        callnum = "A1 B2 .C33"; // space period
        assertEquals("C33", getSecondLCcutter(callnum));
        callnum = "A1 B2.C33"; // no space period
        assertEquals("C33", getSecondLCcutter(callnum));
        callnum = "A1 B2C33"; // no space or period
        assertEquals("C33", getSecondLCcutter(callnum));
        callnum = "HC241.25F4 .D47"; // more parsing fun
        assertEquals("D47", getSecondLCcutter(callnum));

        // first cutter suffix present
        callnum = "PR9199.3.L33 2007 B6"; // suffix b/t cutters no period
        assertNull(getSecondLCcutter(callnum));
        callnum = "PR9199.3.L33 2007 .B6"; // suffix b/t cutters
        assertEquals("B6", getSecondLCcutter(callnum));
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // 2 cutters, suffix after class, 1st cutter
        assertEquals("A6", getSecondLCcutter(callnum));

        // suffix after second cutter
        callnum = "PR9199.3 1920 L33 A6 1982";
        assertEquals("A6", getSecondLCcutter(callnum));
        callnum = "PQ2678.K26 P54 1992";
        assertEquals("P54", getSecondLCcutter(callnum));

        // double suffix after first cutter
        callnum = "P5 L33 1475 vol.1 A1"; // double suffix, but no period
        assertNull(getSecondLCcutter(callnum));
        callnum = "P5 L33 1475 vol.1 .A1"; // double suffix
        assertEquals("A1", getSecondLCcutter(callnum));
        callnum = "P5 L33 1475 vol 1 A1"; // double suffix, but no period
        assertNull(getSecondLCcutter(callnum));
        callnum = "P5 L33 1475 vol 1 .A1"; // double suffix
        assertEquals("A1", getSecondLCcutter(callnum));
        callnum = "M211 .M93 K.240 1988 A1"; // weird suffix, but no period
        assertNull(getSecondLCcutter(callnum));
        callnum = "M211 .M93 K.240 1988 .A1"; // weird suffix
        assertEquals("A1", getSecondLCcutter(callnum));

        // first cutter suffix looks like cutter
        callnum = "MT130 .M93 K96 W83 1988";
        assertEquals("K96", getSecondLCcutter(callnum));
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("W83", getSecondLCcutter(callnum));
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("M2", getSecondLCcutter(callnum));
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("U5", getSecondLCcutter(callnum));
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals("W5", getSecondLCcutter(callnum));
        callnum = "D400.H23 A35 Hamilton Frederick Spencer Lord 1856"; 
        assertEquals("A35", getSecondLCcutter(callnum));

    }

    /**
     * unit test for finding optional suffix after LC first cutter
     */
    @Test
    public void testSecondLCcutterSuffix()
    {
        String callnum = "M1 L33";
        assertNull(getSecondLCcutterSuffix(callnum));
        callnum = "HC241.25F4 .D47"; // two cutters
        assertNull(getSecondLCcutterSuffix(callnum));
        callnum = "PR9199.3.L33 2007 B6"; // suffix after 1st cutter, b4 2nd cutter
        assertNull(getSecondLCcutterSuffix(callnum));
        callnum = "PR92 1844 .L33 1990"; // class has suffix
        assertNull(getSecondLCcutterSuffix(callnum));
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // 2 cutters, suffix after class, 1st cutter
        assertNull(getSecondLCcutterSuffix(callnum));
        callnum = "M211 .M93 K.240 1988"; // weird suffix after first cutter
        assertNull(getSecondLCcutterSuffix(callnum));

        // second cutter suffix present
        callnum = "PR9199.3 1920 L33 A6 1982"; // suffix after second cutter
        assertEquals("1982", getSecondLCcutterSuffix(callnum));
        callnum = "PQ2678.K26 P54 1992"; // no second cutter
        assertEquals("1992", getSecondLCcutterSuffix(callnum));
        callnum = "M453 .Z29 Q1 L V.2";
        assertEquals("L V.2", getSecondLCcutterSuffix(callnum));
        callnum = "M1001 .A13 S9 OP.7:NO.6 1944";
        assertEquals("OP.7:NO.6 1944", getSecondLCcutterSuffix(callnum));
        callnum = "ML410 .M9 P29 1941 M";
        assertEquals("1941 M", getSecondLCcutterSuffix(callnum));
        callnum = "M857 .K93 H2 OP.79";
        assertEquals("OP.79", getSecondLCcutterSuffix(callnum));
        callnum = "G3824 .P5 1776 .E2 1976"; // first cutter suffix too
        assertEquals("1976", getSecondLCcutterSuffix(callnum));
        callnum = "G3822 .G384 1925 .U5 TANEYTOWN"; // first cutter suffix too
        assertEquals("TANEYTOWN", getSecondLCcutterSuffix(callnum));
        callnum = "M857 .R348 P2 D MAJ 1989";
        assertEquals("D MAJ 1989", getSecondLCcutterSuffix(callnum));
        callnum = "LD4881 .S16588 A936A";
        assertEquals("A", getSecondLCcutterSuffix(callnum));
        callnum = "M857 .M93 S412B M";
        assertEquals("B M", getSecondLCcutterSuffix(callnum));

        // cutter like suffixes
        callnum = "MT130 .M93 K96 W83 1988";
        assertEquals("W83 1988", getSecondLCcutterSuffix(callnum));
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("1988", getSecondLCcutterSuffix(callnum));
        callnum = "ML410 .M8 L25 M95 1995";
        assertEquals("M95 1995", getSecondLCcutterSuffix(callnum));
        callnum = "G3824 .G3 G4 S9 .M2";
        assertNull(getSecondLCcutterSuffix(callnum));
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals("MD:CRAPO*DMA 1981", getSecondLCcutterSuffix(callnum));
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals("2002", getSecondLCcutterSuffix(callnum));
    }

    /**
     * unit test for getLCShelfKey
     */
    @Test
    public void testLCShelfKey()
    {
        String callnum = "M1 L33";
        assertEquals("M   0001.000000 L0.330000", getLCShelfkey(callnum, null));
        callnum = "PR9199.4 .B3"; // class has fraction, first cutter has period
        assertEquals("PR  9199.400000 B0.300000", getLCShelfkey(callnum, null));

        // LC classification suffixes
        callnum = "PR92 1990 L33"; // suffix before first cutter
        assertEquals("PR  0092.000000 001990 L0.330000", getLCShelfkey(callnum, null));
        callnum = "PR92 1844 .L33 1990"; // first cutter has period
        assertEquals("PR  0092.000000 001844 L0.330000 001990", getLCShelfkey(callnum, null));
        callnum = "PR92 1844.L33 1990"; // no space before cutter w period
        assertEquals("PR  0092.000000 001844 L0.330000 001990", getLCShelfkey(callnum, null));
        callnum = "PR92 1844L33 1990"; // no space before cutter w no period
        assertEquals("PR  0092.000000 001844 L0.330000 001990", getLCShelfkey(callnum, null));
        callnum = "L666 15th A8"; // suffix with letters
        assertEquals("L   0666.000000 000015TH A0.800000", getLCShelfkey(callnum, null));
        callnum = "MT37 2003M384"; // no period before cutter
        assertEquals("MT  0037.000000 002003 M0.384000", getLCShelfkey(callnum, null));
        callnum = "M234.8 1827 .F666"; // space period before cutter
        assertEquals("M   0234.800000 001827 F0.666000", getLCShelfkey(callnum, null));
        callnum = "PS3538 1974.L33"; // period before cutter
        assertEquals("PS  3538.000000 001974 L0.330000", getLCShelfkey(callnum, null));
        callnum = "PR9199.3 1920 L33 A6 1982"; // two cutters, nothing b/t 'em
        assertEquals("PR  9199.300000 001920 L0.330000 A0.600000 001982", getLCShelfkey(callnum, null));
        callnum = "PR9199.3 1920 .L33 1475 .A6"; // two cutters suffix b/t
        assertEquals("PR  9199.300000 001920 L0.330000 001475 A0.600000", getLCShelfkey(callnum, null));

        // first cutter suffix
        callnum = "M5 L3 1902";
        assertEquals("M   0005.000000 L0.300000 001902", getLCShelfkey(callnum, null));
        callnum = "M1001 .M9 1900Z";
        assertEquals("M   1001.000000 M0.900000 001900Z", getLCShelfkey(callnum, null));
        callnum = "M1001 .M9 K.551 1900Z M";
        assertEquals("M   1001.000000 M0.900000 K.000551 001900Z M", getLCShelfkey(callnum, null));
        callnum = "M1001 .M9 K.173D B";
        assertEquals("M   1001.000000 M0.900000 K.000173D B", getLCShelfkey(callnum, null));
        callnum = "G3840 SVAR .H5";
        assertEquals("G   3840.000000 SVAR H0.500000", getLCShelfkey(callnum, null));
        callnum = "G3841 .C2 1935 .M3"; // with second cutter
        assertEquals("G   3841.000000 C0.200000 001935 M0.300000", getLCShelfkey(callnum, null));
        callnum = "PQ2678.K26 P54 1992"; // no space b4 cutter with period
        assertEquals("PQ  2678.000000 K0.260000 P0.540000 001992", getLCShelfkey(callnum, null));
        callnum = "M211 .M93 K.240 1988"; // first cutter has period
        assertEquals("M   0211.000000 M0.930000 K.000240 001988", getLCShelfkey(callnum, null));

        // second cutter
        callnum = "PQ2678.K26 P54"; // 2 cutters
        assertEquals("PQ  2678.000000 K0.260000 P0.540000", getLCShelfkey(callnum, null));
        callnum = "PR9199.3.L33 B6"; // decimal call no space before cutter
        assertEquals("PR  9199.300000 L0.330000 B0.600000", getLCShelfkey(callnum, null));
        callnum = "PR9199.3.L33 2007 B6"; // double suffix after 1st cutter
        assertEquals("PR  9199.300000 L0.330000 002007 B000006", getLCShelfkey(callnum, null));
        callnum = "PR9199.3.L33 2007 .B6"; // suffix after 1st cutter, b4 2nd
                                            // cutter
        assertEquals("PR  9199.300000 L0.330000 002007 B0.600000",
                getLCShelfkey(callnum, null));
        callnum = "HC241.25F4 .D47";
        assertEquals("HC  0241.250000 F0.400000 D0.470000", getLCShelfkey(callnum, null));

        // second cutter suffix
        callnum = "M453 .Z29 Q1 L V.2";
        assertEquals("M   0453.000000 Z0.290000 Q0.100000 L V.000002", getLCShelfkey(callnum, null));
        callnum = "M1001 .A13 S9 OP.7:NO.6 1944";
        assertEquals(
                "M   1001.000000 A0.130000 S0.900000 OP.000007:NO.000006 001944", getLCShelfkey(callnum, null));
        callnum = "ML410 .M9 P29 1941 M";
        assertEquals("ML  0410.000000 M0.900000 P0.290000 001941 M", getLCShelfkey(callnum, null));
        callnum = "M857 .K93 H2 OP.79";
        assertEquals("M   0857.000000 K0.930000 H0.200000 OP.000079", getLCShelfkey(callnum, null));
        callnum = "G3824 .P5 1776 .E2 1976"; // first cutter suffix too
        assertEquals("G   3824.000000 P0.500000 001776 E0.200000 001976", getLCShelfkey(callnum, null));
        callnum = "G3822 .G384 1925 .U5 TANEYTOWN"; // first cutter suffix too
        assertEquals("G   3822.000000 G0.384000 001925 U0.500000 TANEYTOWN", getLCShelfkey(callnum, null));
        callnum = "M857 .R348 P2 D MAJ 1989";
        assertEquals("M   0857.000000 R0.348000 P0.200000 D MAJ 001989", getLCShelfkey(callnum, null));
        callnum = "LD4881 .S16588 A936A";
        assertEquals("LD  4881.000000 S0.165880 A0.936000 A", getLCShelfkey(callnum, null));
        callnum = "M857 .M93 S412B M";
        assertEquals("M   0857.000000 M0.930000 S0.412000 B M", getLCShelfkey(callnum, null));

        // suffix after first cutter looks like a cutter
        callnum = "MT130 .M93 K96 W83 1988";
        assertEquals("MT  0130.000000 M0.930000 K0.960000 W000083 001988", getLCShelfkey(callnum, null));
        callnum = "MT130 .M93 K96 .W83 1988";
        assertEquals("MT  0130.000000 M0.930000 K000096 W0.830000 001988", getLCShelfkey(callnum, null));
        callnum = "ML410 .M8 L25 M95 1995";
        assertEquals("ML  0410.000000 M0.800000 L0.250000 M000095 001995", getLCShelfkey(callnum, null));
        callnum = "ML410 .M8 L25 .M95 1995";
        assertEquals("ML  0410.000000 M0.800000 L000025 M0.950000 001995", getLCShelfkey(callnum, null));
        // first cutter starts with same chars as LC class
        callnum = "G3824 .G3 .S5 1863 W5 2002"; // suffix after second cutter
        assertEquals(
                "G   3824.000000 G0.300000 S0.500000 001863 W000005 002002", getLCShelfkey(callnum, null));
        callnum = "G3824 .G3 S5 1863 .W5 2002"; // suffix after second cutter
        assertEquals(
                "G   3824.000000 G0.300000 S000005 001863 W0.500000 002002", getLCShelfkey(callnum, null));
        callnum = "G3824 .G3 G4 S9 M2";
        assertEquals("G   3824.000000 G0.300000 G0.400000 S000009 M000002", getLCShelfkey(callnum, null));
        callnum = "G3824 .G3 G4 S9 .M2";
        assertEquals("G   3824.000000 G0.300000 G000004 S000009 M0.200000", getLCShelfkey(callnum, null));
        callnum = "G3841.C2 S24 .U5 MD:CRAPO*DMA 1981";
        assertEquals(
                "G   3841.000000 C0.200000 S000024 U0.500000 MD:CRAPO*DMA 001981", getLCShelfkey(callnum, null));

        // wacko
        callnum = "M1001 .H";
        assertEquals("M   1001.000000 H0.000000", getLCShelfkey(callnum, null));
        callnum = "LD6353 1886";
        assertEquals("LD  6353.000000 001886", getLCShelfkey(callnum, null));
        callnum = "M1001 .M939 S.3,13 2001";
        assertEquals("M   1001.000000 M0.939000 S.000003,000013 002001", getLCShelfkey(callnum, null));
        callnum = "LD6329 1903 35TH";
        assertEquals("LD  6329.000000 001903 000035TH", getLCShelfkey(callnum, null));
    }

    /**
     * unit test for Utils.getDeweyB4Cutter()
     */
    @Test
    public void testDeweyB4Cutter()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("1", getDeweyB4Cutter(callnum));
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("1.23", getDeweyB4Cutter(callnum));
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("11", getDeweyB4Cutter(callnum));
        callnum = "11.34567 .I39"; // two digits fraction
        assertEquals("11.34567", getDeweyB4Cutter(callnum));

        callnum = "111 .I39"; // no fraction in class
        assertEquals("111", getDeweyB4Cutter(callnum));
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("111", getDeweyB4Cutter(callnum));
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("111", getDeweyB4Cutter(callnum));

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("111.12", getDeweyB4Cutter(callnum));
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("111.123", getDeweyB4Cutter(callnum));
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("111.134", getDeweyB4Cutter(callnum));
    }

    /**
     * unit test for getting the cutter for Dewey call numbers
     */
    @Test
    public void testDeweyCutter()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("I39", getDeweyCutter(callnum));
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("I39", getDeweyCutter(callnum));
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("I39", getDeweyCutter(callnum));
        callnum = "11.34 .I39"; // two digits fraction
        assertEquals("I39", getDeweyCutter(callnum));

        callnum = "111 .I39"; // no fraction in class
        assertEquals("I39", getDeweyCutter(callnum));
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("I39", getDeweyCutter(callnum));
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("Q39", getDeweyCutter(callnum));

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("I39", getDeweyCutter(callnum));
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("I39", getDeweyCutter(callnum));
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("Q39", getDeweyCutter(callnum));

        // cutter has following letters
        callnum = "324.54 .I39 F"; // letter with space
        // assertEquals("I39 F", getDeweyCutter(callnum));
        assertEquals("I39", getDeweyCutter(callnum));
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("C425R", getDeweyCutter(callnum));
        callnum = "324.6 .A75CUA"; // letters without space
        assertEquals("A75CUA", getDeweyCutter(callnum));

        // suffixes
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("K43", getDeweyCutter(callnum));
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("F816", getDeweyCutter(callnum));
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("R513", getDeweyCutter(callnum));
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("A512RE", getDeweyCutter(callnum));
        callnum = "323 .A778 ED.2"; // suffix ed
        assertEquals("A778", getDeweyCutter(callnum));
    }

    /**
     * unit test for getting the cutter for Dewey call numbers
     */
    @Test
    public void testDeweyCutterSuffix()
    {
        // cutter has following letters
        String callnum = "324.54 .I39 F"; // letter with space
        // assertEquals("I39 F", getDeweyCutter(callnum));
        assertEquals("I39", getDeweyCutter(callnum));
        // assertNull(getDeweyCutterSuffix(callnum));
        assertEquals("F", getDeweyCutterSuffix(callnum));
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("C425R", getDeweyCutter(callnum));
        assertNull(getDeweyCutterSuffix(callnum));
        callnum = "324.6 .A75CUA"; // letters without space
        assertEquals("A75CUA", getDeweyCutter(callnum));
// TODO: this is broken.  Presumably not too many call numbers with multiple letters at end of cutter
        // assertNull(getDeweyCutterSuffix(callnum));

        // suffixes
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("V.1", getDeweyCutterSuffix(callnum));
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("V.1 1974", getDeweyCutterSuffix(callnum));
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("1957", getDeweyCutterSuffix(callnum));
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("NO.23-28", getDeweyCutterSuffix(callnum));
        callnum = "323 .A778 ED.2"; // suffix ed
        assertEquals("ED.2", getDeweyCutterSuffix(callnum));
    }

    /**
     * unit test for getting Dewey shelf key
     */
    @Test
    public void testDeweyShelfKey()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("001.00000000 I39", getDeweyShelfKey(callnum));
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("001.23000000 I39", getDeweyShelfKey(callnum));
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("011.00000000 I39", getDeweyShelfKey(callnum));
        callnum = "11.34 .I39"; // two digits fraction
        assertEquals("011.34000000 I39", getDeweyShelfKey(callnum));

        callnum = "111 .I39"; // no fraction in class
        assertEquals("111.00000000 I39", getDeweyShelfKey(callnum));
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("111.00000000 I39", getDeweyShelfKey(callnum));
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("111.00000000 Q39", getDeweyShelfKey(callnum));

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("111.12000000 I39", getDeweyShelfKey(callnum));
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("111.12300000 I39", getDeweyShelfKey(callnum));
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("111.13400000 Q39", getDeweyShelfKey(callnum));

        // cutter has following letters
        callnum = "324.54 .I39 F"; // letter with space
        assertEquals("324.54000000 I39 F", getDeweyShelfKey(callnum));
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("324.54800000 C425R", getDeweyShelfKey(callnum));
        callnum = "324.6 .A75CUA"; // letters without space
        // TODO: this is broken. presumably not to many dewey call numbers with
        // multiple letters at end of cutter.
        // assertEquals("324.60000000 A75CUA", getDeweyShelfKey(callnum));

        // suffixes
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("323.09000000 K43 V.000001", getDeweyShelfKey(callnum));
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("322.44000000 F816 V.000001 001974", getDeweyShelfKey(callnum));
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("322.45000000 R513 001957", getDeweyShelfKey(callnum));
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("323.00000000 A512RE NO.000023-000028", getDeweyShelfKey(callnum));
        callnum = "323 .A778 ED.2"; // suffix ed
        assertEquals("323.00000000 A778 ED.000002", getDeweyShelfKey(callnum));
    }

    // list of raw call numbers NOT in order to check sorting
    List<String> diabolicalCallnumList = new ArrayList<String>(75);
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
    List<String> properOrderList = new ArrayList<String>(75);
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
    }

    // TODO: make current ascending order "correct"
    // list of diabolical call numbers in current ascending order
    List<String> currentOrderList = new ArrayList<String>(75);
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
            shelfkey2callnum.put(getLCShelfkey(callnum, null), callnum);
        }
        List<String> ordered = new ArrayList<String>(shelfkey2callnum.keySet());
        Collections.sort(ordered);

        for (int i = 0; i < ordered.size(); i++)
        {
            // System.out.println(ordered.get(i));
            assertEquals("At position " + i + " in list: ", currentOrderList.get(i), shelfkey2callnum.get(ordered.get(i)));
            // assertEquals("At position " + i + " in list: ", properOrderList.get(i), shelfkey2callnum.get(ordered.get(i)));
        }
    }

    /**
     * test the reverse sort of LC call numbers (via the reverse shelf key) -
     * diabolical data
     */
    @Test
    public final void testLCcallNumsReverseSorted() throws IOException,
            ParserConfigurationException, SAXException
    {
        // compute list of expected ordering by getting the shelf keys for the
        // diabolical call numbers and sorting them in reverse order
        Map<String, String> expShelfkey2callnum = new HashMap<String, String>(75);
        for (String callnum : diabolicalCallnumList)
        {
            expShelfkey2callnum.put(getLCShelfkey(callnum, null), callnum);
        }
        List<String> keysExpectedOrder = new ArrayList<String>(expShelfkey2callnum.keySet());
        Collections.sort(keysExpectedOrder);
        Collections.reverse(keysExpectedOrder);

        // get reverse sorted shelfkeys
        Map<String, String> reverseShelfkey2callnum = new HashMap<String, String>(75);
        for (String callnum : diabolicalCallnumList)
        {
            String reverseKey = getReverseShelfKey(getLCShelfkey(callnum, null));
            reverseShelfkey2callnum.put(reverseKey, callnum);
        }
        List<String> ordered = new ArrayList<String>(reverseShelfkey2callnum.keySet());
        Collections.sort(ordered);

        for (int i = 0; i < ordered.size(); i++) {
            assertEquals("At position " + i + " in List: ", expShelfkey2callnum.get(keysExpectedOrder.get(i)), reverseShelfkey2callnum.get(ordered.get(i)));
        }
    }

    // FIXME: TODO: need tests for dewey and LC shelfkey sorts!!!!!

    /**
     * unit test for removing volume/part info at end of call number, if it
     * exists
     */
    @Test
    public void testRemoveLC_V_VolInfo()
    {
        // call nums to be left as is
        String callnum = "M211 .M93 K.240 1988 A1";
        assertEquals("M211 .M93 K.240 1988 A1", removeLCVolSuffix(callnum));
        callnum = "M5 .L3 K2 Q2 1880";
        assertEquals("M5 .L3 K2 Q2 1880", removeLCVolSuffix(callnum));
        callnum = "M5 L3 1902";
        assertEquals("M5 L3 1902", removeLCVolSuffix(callnum));
        callnum = "M5 L3 1902V";
        assertEquals("M5 L3 1902V", removeLCVolSuffix(callnum));
        callnum = "PR9199.3.L33 2007 B6";
        assertEquals("PR9199.3.L33 2007 B6", removeLCVolSuffix(callnum));
        callnum = "PR92 1844 .L33 1990";
        assertEquals("PR92 1844 .L33 1990", removeLCVolSuffix(callnum));
        callnum = "PR9199.3 1920 .L33 1475 .A6";
        assertEquals("PR9199.3 1920 .L33 1475 .A6", removeLCVolSuffix(callnum));
        callnum = "M211 .M93 K.240";
        assertEquals("M211 .M93 K.240", removeLCVolSuffix(callnum));
        callnum = "M211 .M93 BMW240";
        assertEquals("M211 .M93 BMW240", removeLCVolSuffix(callnum));

        // v
        callnum = "M5 L3 V.188";
        assertEquals("M5 L3", removeLCVolSuffix(callnum));
        // leave alone - it may be a second cutter
        callnum = "M5 L3 V188";
        assertEquals("M5 L3 V188", removeLCVolSuffix(callnum));
        callnum = "ND34 .J78 1982 v.1";
        assertEquals("ND34 .J78 1982", removeLCVolSuffix(callnum));
        callnum = "M5 L3 1902 V.2";
        assertEquals("M5 L3 1902", removeLCVolSuffix(callnum));
        callnum = "M5 L3 1902 V2"; // not a second cutter b/c no period
        assertEquals("M5 L3 1902", removeLCVolSuffix(callnum));
        callnum = "BX4655 .A2 F V.40";
        assertEquals("BX4655 .A2 F", removeLCVolSuffix(callnum));
        callnum = "M5 .L3 K2 M V.1";
        assertEquals("M5 .L3 K2 M", removeLCVolSuffix(callnum));
        // preceded by 2nd cutter with V
        callnum = "PG2750 .K32 V6 V.2 1976";
        assertEquals("PG2750 .K32 V6", removeLCVolSuffix(callnum));

// TODO:  these are neither easy, nor, hopefully, common, so punting for now
// plus, they are gov docs, so shouldn't be LC ...
        // callnum = "ER 1.11:ERDA-116/V.7";
        // assertEquals("ER 1.11:ERDA-116", removeLCVolSuffix(callnum));
        // callnum = "ER 1.11:ERDA-116/V7";
        // assertEquals("ER 1.11:ERDA-116", removeLCVolSuffix(callnum));

        // year suffix
        callnum = "N13 .A25 V.85 2006";
        assertEquals("N13 .A25", removeLCVolSuffix(callnum));
        callnum = "Q46 .P3 V.14 1856";
        assertEquals("Q46 .P3", removeLCVolSuffix(callnum));
        callnum = "TL725.3. T7 E8 V.3 1934";
        assertEquals("TL725.3. T7 E8", removeLCVolSuffix(callnum));
        callnum = "PQ8550.21.R57 V5 1992";
        assertEquals("PQ8550.21.R57 V5 1992", removeLCVolSuffix(callnum));
        callnum = "TL725.3. W7 E8 V.3 1973-1974";
//        assertEquals("TL725.3. W7 E8", removeLCVolSuffix(callnum));
        callnum = "A1 B2 C3 V3";
        assertEquals("A1 B2 C3", removeLCVolSuffix(callnum));
        callnum = "TL725.3 W7 E8 V3 1973-1974";
        assertEquals("TL725.3 W7 E8", removeLCVolSuffix(callnum));

        // suffix
        callnum = "M5 .L3 1902 V2 TANEYTOWN"; // not a second cutter b/c no period
        assertEquals("M5 .L3 1902", removeLCVolSuffix(callnum));
        callnum = "S2.7940 V5 V.55 JUL-DEC 1940";
        assertEquals("S2.7940 V5", removeLCVolSuffix(callnum));
        callnum = "FS2.7940 B3 V.55 JUL-DEC 1940";
        assertEquals("FS2.7940 B3", removeLCVolSuffix(callnum));

        // range
        callnum = "B92.3. A7 E8 V.1-2";
        assertEquals("B92.3. A7 E8", removeLCVolSuffix(callnum));
        callnum = "C25.3 B7 E8 V.1-2 1923";
        assertEquals("C25.3 B7 E8", removeLCVolSuffix(callnum));
        callnum = "D725.3 C7 E8 V26-78";
        assertEquals("D725.3 C7 E8", removeLCVolSuffix(callnum));
        callnum = "F75.3 D7 E8 V26-78 1972";
        assertEquals("F75.3 D7 E8", removeLCVolSuffix(callnum));
        callnum = "G7.3 E7 E8 V.1-2 1965-1972";
        assertEquals("G7.3 E7 E8", removeLCVolSuffix(callnum));
        callnum = "K75.3 F7 E8 V1-2 1965-1972";
        assertEquals("K75.3 F7 E8", removeLCVolSuffix(callnum));

        // commas
        callnum = "JV1801 .C34 V.27,V.40-41";
        assertEquals("JV1801 .C34", removeLCVolSuffix(callnum));
        callnum = "HV71 .P5 v.5,7,9";
        assertEquals("HV71 .P5", removeLCVolSuffix(callnum));

        // slash
        callnum = "BB53 .D424 V.6-8 1910/1911-1913/1915";
        assertEquals("BB53 .D424", removeLCVolSuffix(callnum));
        // parens
        callnum = "F126 .A1 D4 V.49(1) 1993";
        assertEquals("F126 .A1 D4", removeLCVolSuffix(callnum));

        // double
        callnum = "PQ4001 .G55 V.41 NO.3 1991";
        assertEquals("PQ4001 .G55", removeLCVolSuffix(callnum));
        callnum = "DC411 .F73 V.43:NO.323-326 1999";
        assertEquals("DC411 .F73", removeLCVolSuffix(callnum));
        callnum = "PC13 .B5 V.271 SUB V.38 1997";
        assertEquals("PC13 .B5", removeLCVolSuffix(callnum));
        callnum = "DG11 .S79 V.3:NO.4 1990-V.5:NO.1 1992";
        assertEquals("DG11 .S79", removeLCVolSuffix(callnum));
        callnum = "PQ4204 .A9 A57 V.8 1983:NO.23-24";
        assertEquals("PQ4204 .A9 A57", removeLCVolSuffix(callnum));
    }

    /**
     * unit test for removing volume/part info at end of call number, if it
     * exists
     */
    @Test
    public void testRemoveLC_vol_VolInfo()
    {
        // Vol
        String callnum = "P5 L33 1475 vol.1 A1";
        assertEquals("P5 L33 1475", removeLCVolSuffix(callnum));
        callnum = "P5 L33 1475 vol 1 A1";
        assertEquals("P5 L33 1475", removeLCVolSuffix(callnum));
        callnum = "M5 .L3 K2 VOL.1";
        assertEquals("M5 .L3 K2", removeLCVolSuffix(callnum));

        // filed under "ns" ...
        // Q11 .P6 n.s., vol.55, pt.5
    }

    /**
     * unit test for removing volume/part info at end of call number, if it
     * exists
     */
    @Test
    public void testRemoveLC_addl_eng_VolInfo()
    {
        // NO
        String callnum = "HD320.5 W5 NO.10";
        assertEquals("HD320.5 W5", removeLCVolSuffix(callnum));
        // suffix
        callnum = "M5 .L3 K2 NO.1 1880";
        assertEquals("M5 .L3 K2", removeLCVolSuffix(callnum));
        // range
        callnum = "HF3919 .F4 1970:NO.12-22";
        assertEquals("HF3919 .F4 1970", removeLCVolSuffix(callnum));
        callnum = "DC140 .P82 NO.243-271 1793";
        assertEquals("DC140 .P82", removeLCVolSuffix(callnum));
        // colon
        callnum = "HF336 .G35 1968:NO.1-16";
        assertEquals("HF336 .G35 1968", removeLCVolSuffix(callnum));
        callnum = "M5 .L3 K2 OP.7:NO.6 1880";
        assertEquals("M5 .L3 K2 OP.7", removeLCVolSuffix(callnum));
        // suffix
        callnum = "D1056.2. A5 S46 NO.1-3 (1982:JULY)";
        assertEquals("D1056.2. A5 S46", removeLCVolSuffix(callnum));
        callnum = "HM3 .H6 NO.1-4 1966:JUL.-1967:JUN.";
        assertEquals("HM3 .H6", removeLCVolSuffix(callnum));
        callnum = "AP20 .P3 NO.1-4 1961/1962";
        assertEquals("AP20 .P3", removeLCVolSuffix(callnum));
        callnum = "F2646 .A2 F35 FF ANO 1:NO.1 2006:ABR";
        // FIXME: should get this - colon? leftmost? rightmost?
        // ANO
        // assertEquals("F2646 .A2 F35 FF ANO 1", removeLCVolSuffix(callnum));
        callnum = "Z809 .T47 B57 ANNO.7-8:NO.25-31 1990-91";
        // ANNO
        // assertEquals("Z809 .T47 B57 ANNO.7-8", removeLCVolSuffix(callnum));

        // NS
        callnum = "JK1369.43 .P65 1974 n.s., v.2";
        assertEquals("JK1369.43 .P65 1974", removeLCVolSuffix(callnum));
        callnum = "DB361 .Z2 N.S.V.16-17 1935-1936";
        assertEquals("DB361 .Z2", removeLCVolSuffix(callnum));
        callnum = "Q11 .P6 n.s., vol.55, pt.5";
        assertEquals("Q11 .P6", removeLCVolSuffix(callnum));
        callnum = "B2 .E86 N.S.NO.2 1967";
        assertEquals("B2 .E86", removeLCVolSuffix(callnum));
        callnum = "DQ441 .A3 N.S. V.39 1991";
        assertEquals("DQ441 .A3", removeLCVolSuffix(callnum));
        callnum = "D461 .M7 N.S.:V.14 1937:JAN.-JUNE";
        assertEquals("D461 .M7", removeLCVolSuffix(callnum));
        callnum = "DE2 .I7 N.S. V.3:N0.9 1931";
        assertEquals("DE2 .I7", removeLCVolSuffix(callnum));

        // PT
        callnum = "HE5.215 .N9 PT.5";
        assertEquals("HE5.215 .N9", removeLCVolSuffix(callnum));
        callnum = "DT1 .A12 PT.1 1967-1969";
        assertEquals("DT1 .A12", removeLCVolSuffix(callnum));
        callnum = "HE5.215 .N9 PT.A"; // part is letter, not number
        assertEquals("HE5.215 .N9", removeLCVolSuffix(callnum));
        callnum = "HE5.215 .N9/PT.A"; // slash
        // problem finding first cutter suffix (also in test above)
        // assertEquals("HE5.215 .N9", removeLCVolSuffix(callnum));
        callnum = "HE5.215:15023-71/PT.A"; // colon in weird place
        // weird colon - govdoc ?
        // assertEquals("HE5.215:15023-71", removeLCVolSuffix(callnum));

        // SER
        callnum = "M3 .B4488 ser.4 v.18";
        assertEquals("M3 .B4488", removeLCVolSuffix(callnum));
        callnum = "M3 .B4488 ser.4 v.18 1999";
        assertEquals("M3 .B4488", removeLCVolSuffix(callnum));
        callnum = "N2 .G3 SER.1:V.1 1859";
        assertEquals("N2 .G3", removeLCVolSuffix(callnum));
    }

    /**
     * unit test for removing volume/part info at end of call number, if it
     * exists
     */
    @Test
    public void testRemoveLC_NonEng_VolInfo()
    {
        // bd
        String callnum = "PJ5111 .A7 BD.1/2 1955/1964";
        assertEquals("PJ5111 .A7", removeLCVolSuffix(callnum));
        // jahrg
        callnum = "P87 .J34 JAHRG.1-2 1999-200";
        assertEquals("P87 .J34", removeLCVolSuffix(callnum));
        callnum = "DT333 .S34 47. Jahrg.Heft Nr. 8";
        assertEquals("DT333 .S34 47.", removeLCVolSuffix(callnum));
        callnum = "PT3810 .S43 1. JAHRG. 1998";
        // number before jahrg.
        assertEquals("PT3810 .S43 1.", removeLCVolSuffix(callnum));
        // vyp
        callnum = "PL43.5 .U38 VYP.1";
        assertEquals("PL43.5 .U38", removeLCVolSuffix(callnum));
        callnum = "PG3361 .T5 Z68 VYP.1";
        assertEquals("PG3361 .T5 Z68", removeLCVolSuffix(callnum));
        // range
        callnum = "PG3550 .I82 A85 VYP.4-5 2007-2008";
        assertEquals("PG3550 .I82 A85", removeLCVolSuffix(callnum));
        // slash
        callnum = "DK511 .I82 N33 F VYP.2/3";
        assertEquals("DK511 .I82 N33 F", removeLCVolSuffix(callnum));

        // T, with V as second cutter
        callnum = "DK511 .C37 V67 T.10 1976";
        assertEquals("DK511 .C37 V67", removeLCVolSuffix(callnum));
        // T, with only one cutter
        callnum = "Z7164.L1 T.884";
        assertEquals("Z7164.L1", removeLCVolSuffix(callnum));
        // T as second cutter - leave alone
        callnum = "Z7164.L1 T884";
        assertEquals("Z7164.L1 T884", removeLCVolSuffix(callnum));
    }

    /**
     * unit test for removing volume/part info at end of call number, if it
     * exists
     */
    @Test
    public void testRemoveDewey_VolInfo()
    {
        // leave alone
        String callnum = "322.45 .R513 1957";
        assertEquals("322.45 .R513 1957", removeDeweyVolSuffix(callnum));
        callnum = "323 .A778 ED.2";
        assertEquals("323 .A778 ED.2", removeDeweyVolSuffix(callnum));
        callnum = "328.444 .F831 1946:MAR.6-APR.11";
        assertEquals("328.444 .F831 1946", removeDeweyVolSuffix(callnum));

        // bd
        callnum = "870.5 .J25 BD.255-258 1937";
        assertEquals("870.5 .J25", removeDeweyVolSuffix(callnum));

        // no
        callnum = "054 .F332 F NO.30";
        assertEquals("054 .F332 F", removeDeweyVolSuffix(callnum));
        // suffix
        callnum = "054 .F332 F NO.30 1956";
        assertEquals("054 .F332 F", removeDeweyVolSuffix(callnum));
        // range
        callnum = "323 .A512RE NO.23-28";
        assertEquals("323 .A512RE", removeDeweyVolSuffix(callnum));
        callnum = "840.5 .N828 NO.1-4 1929-1930";
        assertEquals("840.5 .N828", removeDeweyVolSuffix(callnum));
        // colon
        callnum = "379.675 .C827U 1975/1976:NO.6";
        assertEquals("379.675 .C827U 1975/1976", removeDeweyVolSuffix(callnum));

        // n.s.
        callnum = "430.5 .D486 N.S. V.2";
        assertEquals("430.5 .D486", removeDeweyVolSuffix(callnum));
        callnum = "053 .P418 N.S.NO.267-269 1989:JAN.-JUN.";
        assertEquals("053 .P418", removeDeweyVolSuffix(callnum));
        callnum = "830.5 .J25 N.S.V.12-14 1932-1934";
        assertEquals("830.5 .J25", removeDeweyVolSuffix(callnum));

        // pt
        callnum = "379.44 .F815S 1985/1986:PT.4.3B";
        assertEquals("379.44 .F815S 1985/1986", removeDeweyVolSuffix(callnum));
        callnum = "540.5 .J257 1885:PT.2";
        assertEquals("540.5 .J257 1885", removeDeweyVolSuffix(callnum));

        // ser
        callnum = "505 .R455 SER.2:V.21";
        assertEquals("505 .R455", removeDeweyVolSuffix(callnum));
        callnum = "510.6 .R766 SER.7:V.1 1981";
        assertEquals("510.6 .R766", removeDeweyVolSuffix(callnum));
        callnum = "064 .I592BA SER.2:V.3-4";
        assertEquals("064 .I592BA", removeDeweyVolSuffix(callnum));
        // letter
        callnum = "064.7 .A31DB SER.B 1924-1927";
        assertEquals("064.7 .A31DB", removeDeweyVolSuffix(callnum));

        // t
        callnum = "620.5 .N934 4TH SERIE T.7 1890";
        assertEquals("620.5 .N934 4TH SERIE", removeDeweyVolSuffix(callnum));

        // v
        callnum = "323.09 .K43 V.1";
        assertEquals("323.09 .K43", removeDeweyVolSuffix(callnum));
        callnum = "064 .I592EA V.1";
        assertEquals("064 .I592EA", removeDeweyVolSuffix(callnum));
        callnum = "301.51 .J25 1993-1997:V.10";
        assertEquals("301.51 .J25 1993-1997", removeDeweyVolSuffix(callnum));
        // preceding colon
        callnum = "053 .E89 1840:V.1";
        assertEquals("053 .E89 1840", removeDeweyVolSuffix(callnum));
        // range
        callnum = "506 .A171 V.9-10";
        assertEquals("506 .A171", removeDeweyVolSuffix(callnum));
        // suffix
        callnum = "322.44 .F816 V.1 1974";
        assertEquals("322.44 .F816", removeDeweyVolSuffix(callnum));
        callnum = "505 .B938 V.144-146 1934-1936";
        assertEquals("505 .B938", removeDeweyVolSuffix(callnum));
        callnum = "905 .H918 V.1-7 1934-1940 INDEX";
        assertEquals("905 .H918", removeDeweyVolSuffix(callnum));
        callnum = "614.0945 .I91 V.30:1 1984";
        assertEquals("614.0945 .I91", removeDeweyVolSuffix(callnum));
        callnum = "305 .E85 V.32 1896:JUL.-DEC.";
        assertEquals("305 .E85", removeDeweyVolSuffix(callnum));
        // doubles
        callnum = "328.444 .F79 V.0 PT.2";
        assertEquals("328.444 .F79", removeDeweyVolSuffix(callnum));
        callnum = "570.5 .N287 V.34:NO.2 1941";
        assertEquals("570.5 .N287", removeDeweyVolSuffix(callnum));
        callnum = "570.5 .N287 V.28:NO.2 1936:AUG.";
        assertEquals("570.5 .N287", removeDeweyVolSuffix(callnum));
        callnum = "570.5 .N287 V.33:NO.2-10 1940";
        assertEquals("570.5 .N287", removeDeweyVolSuffix(callnum));
        callnum = "385.04 .R154 V.14:3RD IN VOL";
        assertEquals("385.04 .R154", removeDeweyVolSuffix(callnum));
        // comma
        callnum = "627.05 .H838 V.38:NO.1-4,7-8 1983";
        assertEquals("627.05 .H838", removeDeweyVolSuffix(callnum));

        // vol
    }

    /**
     * remove LC call number 3 letter month suffix, if it exists
     */
    @Test
    public void testRemoveLC3LetMonths()
    {
        String callnum = "BM198.2 .H85 OCT 2006";
        assertEquals("BM198.2 .H85", removeLCVolSuffix(callnum));
        callnum = "BM198.2 .H85 NOV 2006";
        assertEquals("BM198.2 .H85", removeLCVolSuffix(callnum));
        callnum = "BM198.2 .H85 JAN 2007";
        assertEquals("BM198.2 .H85", removeLCVolSuffix(callnum));
    }

}
