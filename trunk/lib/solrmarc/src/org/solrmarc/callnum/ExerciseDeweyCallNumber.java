package org.solrmarc.callnum;

import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * Exercise the {@code DeweyCallNumber} class from the command line.
 * Illustrates parsing a shelf key construction.
 * Useful for debugging during development.
 * 
 * <p>From the root of the solrmarc distribution:
 * <p>{@code java -cp lib/solrmarc/build org.solrmarc.callnum.ExerciseDeweyCallNumber}
 * 
 * @author Tod Olson, University of Chicago
 *
 */
public class ExerciseDeweyCallNumber {
    
    /**
     * array of call numbers for use as test data.
     */
    static ArrayList<String> callNumsArray;
    
    static String[] callNums = {
        "1 .I39",
        "324.6 .A75CUA", // letters without space
        "1 .I39", // one digit no fraction
        "1.23 .I39", // one digit fraction
        "11 .I39", // two digits no fraction
        "11.34567 .I39", // two digits fraction
        "111 .I39", // no fraction in class
        "111 I39", // no fraction no period before cutter
        "111Q39", // no fraction, no period or space before cutter
        "111.12 .I39", // fraction in class, space period
        "111.123 I39", // space but no period before cutter
        "111.134Q39", // no period or space before cutter
        "1 .I39", // one digit no fraction
        "1.23 .I39", // one digit fraction
        "11 .I39", // two digits no fraction
        "11.34 .I39", // two digits fraction
        "111 .I39", // no fraction in class
        "111 I39", // no fraction no period before cutter
        "111Q39", // no fraction, no period or space before cutter
        "111.12 .I39", // fraction in class, space period
        "111.123 I39", // space but no period before cutter
        "111.134Q39", // no period or space before cutter
        "324.54 .I39 F", // letter with space
        "324.548 .C425R", // letter without space
        "324.6 .A75CUA", // letters without space
        "323.09 .K43 V.1", // cutterSuffix volume
        "322.44 .F816 V.1 1974", // cutterSuffix - volume and year
        "322.45 .R513 1957", // cutterSuffix year
        "323 .A512RE NO.23-28", // cutterSuffix no.
        "323 .A778 ED.2", // cutterSuffix ed
        "324.54 .I39 F", // letter with space
        "324.548 .C425R", // letter without space
        "324.6 .A75CUA", // letters without space
        "323.09 .K43 V.1", // cutterSuffix volume
        "322.44 .F816 V.1 1974", // cutterSuffix - volume and year
        "322.45 .R513 1957", // cutterSuffix year
        "323 .A512RE NO.23-28", // cutterSuffix no.
        "323 .A778 ED.2", // cutterSuffix ed
        "1 .I39", // one digit no fraction
        "1.23 .I39", // one digit fraction
        "11 .I39", // two digits no fraction
        "11.34 .I39", // two digits fraction
        "111 .I39", // no fraction in class
        "111 I39", // no fraction no period before cutter
        "111Q39", // no fraction, no period or space before cutter
        "111.12 .I39", // fraction in class, space period
        "111.123 I39", // space but no period before cutter
        "111.134Q39", // no period or space before cutter
        "324.54 .I39 F", // letter with space
        "324.548 .C425R", // letter without space
        "324.6 .A75CUA", // letters without space
        "323.09 .K43 V.1", // cutterSuffix volume
        "322.44 .F816 V.1 1974", // cutterSuffix - volume and year
        "322.45 .R513 1957", // cutterSuffix year
        "323 .A512RE NO.23-28", // cutterSuffix no.
        "323 .A778 ED.2", // cutterSuffix ed
        "MC1 259",
        "T1 105",
        "",
    };
    
    public ExerciseDeweyCallNumber() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        initCallNums();
        exercisePatterns();
        exerciseClass();
    }

    private static void exercisePatterns() {
        banner("Exercise Patterns");
        for (String c : callNums) {
            Matcher m = DeweyCallNumber.classPattern.matcher(c);
            System.out.printf("callnum:\t%s", c);
            StringBuilder result = new StringBuilder("\tresult:");
            if (m.matches()) {
                for (int i = 1 ; i <= m.groupCount(); i++) {
                    appendHelper(result, m.group(i));
                }
            } else {
                result.append("\t*** no match ***");
            }
            System.out.println(result);
        }
    }

    private static void exerciseClass() {
        banner("Exercise DeweyCallNumber");
        for (String c : callNums) {
            System.out.printf("call:\t%s", c);
            StringBuilder result = new StringBuilder("\nclass:");
            DeweyCallNumber call = new DeweyCallNumber(c);
            appendHelper(result, call.getClassification());
            appendHelper(result, call.getClassDigits());
            appendHelper(result, call.getClassDecimal());
            result.append("\ncutter:");
            appendHelper(result, call.getCutter());
            result.append("\ncutter cutterSuffix:");
            appendHelper(result, call.getSuffix());
            result.append("\nkey:");
            appendHelper(result, call.getShelfKey());
            System.out.println(result);
            System.out.println();
        }
    }

    private static void appendHelper(StringBuilder buf, String str) {
        buf.append('\t');
        buf.append(str);
        buf.append(';');
    }
    
    private static void banner(String msg) {
        System.out.println("###");
        System.out.println("###" + " " + msg);
        System.out.println("###");
    }
    
    private static void initCallNums() {
        callNumsArray = new ArrayList<String>();
        
        callNumsArray.add("M1 .L33");
        callNumsArray.add("M1 L33");
        callNumsArray.add("PS153 .G38 B73 2012");
        callNumsArray.add("M1 L33");
        callNumsArray.add("M211 .M93 K.240 1988");
        callNumsArray.add("PQ2678.K26 P54 1992");
        callNumsArray.add("PR9199.4 .B3");
        callNumsArray.add("PR9199.3.L33 B6");
        callNumsArray.add("HC241.25F4 .D47");
        callNumsArray.add("PR92 1990 L33");
        callNumsArray.add("PR92 1844 .L33 1990");
        callNumsArray.add("PR92 1844.L33 1990");
        callNumsArray.add("PR92 1844L33 1990");
        callNumsArray.add("M234.8 1827 .F666");
        callNumsArray.add("PS3538 1974.L33");
        callNumsArray.add("PR9199.3 1920 L33 A6 1982");
        callNumsArray.add("PR9199.3 1920 .L33 1475 .A6");
        callNumsArray.add("HD38.25.F8 R87 1989");
        callNumsArray.add("HF5549.5.T7 B294 1992");
        callNumsArray.add("L666 15th A8");
        callNumsArray.add("M5 .L");
        callNumsArray.add("B9 2000");
        callNumsArray.add("B9 2000 35TH");
        callNumsArray.add("G3840 SVAR .H5");
        callNumsArray.add("G3824 .G3 .S5 1863 W5 2002");
        callNumsArray.add("G3841.C2 S24 .U5 MD:CRAPO*DMA 1981");
        callNumsArray.add("PQ 8550.21.R57 V5 1992");
        callNumsArray.add("HD 38.25.F8 R87 1989");
        callNumsArray.add("M211 .M93 K.240 1988");
        callNumsArray.add("M1 L33");
        callNumsArray.add("PR92 1990 L33");
        callNumsArray.add("PR92 .L33 1990");
        callNumsArray.add("PR919 .L33 1990");
        callNumsArray.add("PR9199 .A39");
        callNumsArray.add("PR9199.3.L33 B6");
        callNumsArray.add("PR9199.4 .B3");
        callNumsArray.add("PQ2678.I26 P54 1992");
        callNumsArray.add("HC241.25 .I4 D47");
        callNumsArray.add("G3824 .G3 .S5 1863 W5 2002");
        callNumsArray.add("G3841.C2 S24 .U5 MD:CRAPO*DMA 1981");
        callNumsArray.add("M1 L33");
        callNumsArray.add("PR9199.48 .B3");
        callNumsArray.add("PR9199.3.L33 B6");
        callNumsArray.add("PQ2678.I26 P54 1992");
        callNumsArray.add("PR9199.3.L33 2007 B6");
        callNumsArray.add("M211 .M93 K.240 1988");
        callNumsArray.add("PR92 1990 L33");
        callNumsArray.add("MT37 2003M384");
        callNumsArray.add("M234.8 1827 .F666");
        callNumsArray.add("PS3538 1974.L33");
        callNumsArray.add("PR9199.3 1920 L33 A6 1982");
        callNumsArray.add("PR9199.3 1920 .L33 1475 .A6");
        callNumsArray.add("L666 15th A8");
        callNumsArray.add("M5 .L");
        callNumsArray.add("B9 2000");
        callNumsArray.add("B9 2000 35TH");
        callNumsArray.add("G3840 SVAR .H5");
        callNumsArray.add("M1 L33");
        callNumsArray.add("MT37 2003M384");
        callNumsArray.add("M234.8 1827 .F666");
        callNumsArray.add("PS3538 1974.L33");
        callNumsArray.add("PQ2678.K26 P54 1992");
        callNumsArray.add("M211 .M93 K.240 1988");
        callNumsArray.add("PR9199.3 1920 L33 A6 1982");
        callNumsArray.add("PR9199.3 1920 .L33 1475 .A6");
        callNumsArray.add("PR9199.4 .B3");
        callNumsArray.add("PR9199.3.L33 B6");
        callNumsArray.add("PR9199.3.L33 2007 B6");
        callNumsArray.add("HC241.25F4 .D47");
        callNumsArray.add("G3824 .G3 .S5 1863 W5 2002");
        callNumsArray.add("G3824 .G3 S5 1863 .W5 2002");
        callNumsArray.add("G3824 .G3 G4 S9 M2");
        callNumsArray.add("G3824 .G3 G4 S9 .M2");
        callNumsArray.add("G3841.C2 S24 .U5 MD:CRAPO*DMA 1981");
        callNumsArray.add("PR92 1990 L33");
        callNumsArray.add("PR92 1844 .L33 1990");
        callNumsArray.add("PR92 1844.L33 1990");
        callNumsArray.add("PR92 1844L33 1990");
        callNumsArray.add("L666 15th A8");
        callNumsArray.add("M5 .L");
        callNumsArray.add("G3840 SVAR .H5");
        callNumsArray.add("B9 2000");
        callNumsArray.add("B9 2000 35TH");
        callNumsArray.add("PQ8550.21.R57 V5 1992");
        callNumsArray.add("HF5549.5.T7 B294 1992");
        callNumsArray.add("PQ 8550.21.R57 V5 1992");
        callNumsArray.add("HF 5549.5.T7 B294 1992");
        callNumsArray.add("M1 L33");
        callNumsArray.add("HC241.25F4 .D47");
        callNumsArray.add("PQ2678.K26 P54 1992");
        callNumsArray.add("PR9199.3 1920 L33 A6 1982");
        callNumsArray.add("M5 L3 1902");
        callNumsArray.add("M5 L3 1902V");
        callNumsArray.add("PR9199.3.L33 2007 B6");
        callNumsArray.add("PR9199.3.L33 2007 .B6");
        callNumsArray.add("PR92 1844 .L33 1990");
        callNumsArray.add("PR9199.3 1920 .L33 1475 .A6");
        callNumsArray.add("M211 .M93 K.240");
        callNumsArray.add("M211 .M93 BMW240");
        callNumsArray.add("P5 L33 1475 vol.1");
        callNumsArray.add("P5 L33 1475 vol 1");
        callNumsArray.add("M211 .M93 K.240 1988");
        callNumsArray.add("M5 L3 1902 V.2");
        callNumsArray.add("M5 L3 1902 V2");
        callNumsArray.add("M5 .L3 1902 V2 TANEYTOWN");
        callNumsArray.add("P5 L33 1475 vol.1 A1");
        callNumsArray.add("P5 L33 1475 vol.1 .A1");
        callNumsArray.add("P5 L33 1475 vol 1 A1");
        callNumsArray.add("P5 L33 1475 vol 1 .A1");
        callNumsArray.add("M211 .M93 K.240 1988 A1");
        callNumsArray.add("M211 .M93 K.240 1988 .A1");
        callNumsArray.add("MT130 .M93 K96 W83 1988");
        callNumsArray.add("MT130 .M93 K96 .W83 1988");
        callNumsArray.add("ML410 .M8 L25 M95 1995");
        callNumsArray.add("ML410 .M8 L25 .M95 1995");
        callNumsArray.add("G3824 .G3 G4 S9 .M2");
        callNumsArray.add("G3841.C2 S24 .U5 MD:CRAPO*DMA 1981");
        callNumsArray.add("G3824 .G3 S5 1863 .W5 2002");
        callNumsArray.add("HE5.215 .N9/PT.A");
        callNumsArray.add("D400.H23 A35 Hamilton Frederick Spencer Lord 1856");
        callNumsArray.add("M1 L33");
        callNumsArray.add("PR92 1844 .L33 1990");
        callNumsArray.add("M211 .M93 K.240");
        callNumsArray.add("M211 .M93 BMW240");
        callNumsArray.add("P5 L33 1475 vol.1");
        callNumsArray.add("P5 L33 1475 vol 1");
        callNumsArray.add("M211 .M93 K.240 1988");
        callNumsArray.add("A1 B2 C33");
        callNumsArray.add("A1 B2 .C33");
        callNumsArray.add("A1 B2.C33");
        callNumsArray.add("A1 B2C33");
        callNumsArray.add("HC241.25F4 .D47");
        callNumsArray.add("PR9199.3.L33 2007 B6");
        callNumsArray.add("PR9199.3.L33 2007 .B6");
        callNumsArray.add("PR9199.3 1920 .L33 1475 .A6");
        callNumsArray.add("PR9199.3 1920 L33 A6 1982");
        callNumsArray.add("PQ2678.K26 P54 1992");
        callNumsArray.add("P5 L33 1475 vol.1 A1");
        callNumsArray.add("P5 L33 1475 vol.1 .A1");
        callNumsArray.add("P5 L33 1475 vol 1 A1");
        callNumsArray.add("P5 L33 1475 vol 1 .A1");
        callNumsArray.add("M211 .M93 K.240 1988 A1");
        callNumsArray.add("M211 .M93 K.240 1988 .A1");
        callNumsArray.add("MT130 .M93 K96 W83 1988");
        callNumsArray.add("MT130 .M93 K96 .W83 1988");
        callNumsArray.add("G3824 .G3 G4 S9 .M2");
        callNumsArray.add("G3841.C2 S24 .U5 MD:CRAPO*DMA 1981");
        callNumsArray.add("G3824 .G3 S5 1863 .W5 2002");
        callNumsArray.add("D400.H23 A35 Hamilton Frederick Spencer Lord 1856");
        callNumsArray.add("M1 L33");
        callNumsArray.add("HC241.25F4 .D47");
        callNumsArray.add("PR9199.3.L33 2007 B6");
        callNumsArray.add("PR92 1844 .L33 1990");
        callNumsArray.add("PR9199.3 1920 .L33 1475 .A6");
        callNumsArray.add("M211 .M93 K.240 1988");
        callNumsArray.add("PR9199.3 1920 L33 A6 1982");
        callNumsArray.add("PQ2678.K26 P54 1992");
        callNumsArray.add("M453 .Z29 Q1 L V.2");
        callNumsArray.add("M1001 .A13 S9 OP.7:NO.6 1944");
        callNumsArray.add("ML410 .M9 P29 1941 M");
        callNumsArray.add("M857 .K93 H2 OP.79");
        callNumsArray.add("G3824 .P5 1776 .E2 1976");
        callNumsArray.add("G3822 .G384 1925 .U5 TANEYTOWN");
        callNumsArray.add("M857 .R348 P2 D MAJ 1989");
        callNumsArray.add("LD4881 .S16588 A936A");
        callNumsArray.add("M857 .M93 S412B M");
        callNumsArray.add("MT130 .M93 K96 W83 1988");
        callNumsArray.add("MT130 .M93 K96 .W83 1988");
        callNumsArray.add("ML410 .M8 L25 M95 1995");
        callNumsArray.add("G3824 .G3 G4 S9 .M2");
        callNumsArray.add("G3841.C2 S24 .U5 MD:CRAPO*DMA 1981");
        callNumsArray.add("G3824 .G3 S5 1863 .W5 2002");
        callNumsArray.add("M1 L33");
        callNumsArray.add("PR9199.4 .B3");
        callNumsArray.add("AB9 L3");
        callNumsArray.add("PR92 1990 L33");
        callNumsArray.add("PR92 1844 .L33 1990");
        callNumsArray.add("PR92 1844.L33 1990");
        callNumsArray.add("PR92 1844L33 1990");
        callNumsArray.add("L666 15th A8");
        callNumsArray.add("MT37 2003M384");
        callNumsArray.add("M234.8 1827 .F666");
        callNumsArray.add("PS3538 1974.L33");
        callNumsArray.add("PR9199.3 1920 L33 A6 1982");
        callNumsArray.add("PR9199.3 1920 .L33 1475 .A6");
        callNumsArray.add("M5 L3 1902");
        callNumsArray.add("M1001 .M9 1900Z");
        callNumsArray.add("M1001 .M9 K.551 1900Z M");
        callNumsArray.add("M1001 .M9 K.173D B");
        callNumsArray.add("G3840 SVAR .H5");
        callNumsArray.add("G3841 .C2 1935 .M3");
        callNumsArray.add("PQ2678.K26 P54 1992");
        callNumsArray.add("M211 .M93 K.240 1988");
        callNumsArray.add("PQ2678.K26 P54");
        callNumsArray.add("PR9199.3.L33 B6");
        callNumsArray.add("PR9199.3.L33 2007 B6");
        callNumsArray.add("PR9199.3.L33 2007 .B6");
        callNumsArray.add("HC241.25F4 .D47");
        callNumsArray.add("M453 .Z29 Q1 L V.2");
        callNumsArray.add("M1001 .A13 S9 OP.7:NO.6 1944");
        callNumsArray.add("ML410 .M9 P29 1941 M");
        callNumsArray.add("M857 .K93 H2 OP.79");
        callNumsArray.add("G3824 .P5 1776 .E2 1976");
        callNumsArray.add("G3822 .G384 1925 .U5 TANEYTOWN");
        callNumsArray.add("M857 .R348 P2 D MAJ 1989");
        callNumsArray.add("LD4881 .S16588 A936A");
        callNumsArray.add("M857 .M93 S412B M");
        callNumsArray.add("MT130 .M93 K96 W83 1988");
        callNumsArray.add("MT130 .M93 K96 .W83 1988");
        callNumsArray.add("ML410 .M8 L25 M95 1995");
        callNumsArray.add("ML410 .M8 L25 .M95 1995");
        callNumsArray.add("G3824 .G3 .S5 1863 W5 2002");
        callNumsArray.add("G3824 .G3 S5 1863 .W5 2002");
        callNumsArray.add("G3824 .G3 G4 S9 M2");
        callNumsArray.add("G3824 .G3 G4 S9 .M2");
        callNumsArray.add("G3841.C2 S24 .U5 MD:CRAPO*DMA 1981");
        callNumsArray.add("BF199");
        callNumsArray.add("BF199.");
        callNumsArray.add("BF199.A1J7");
        callNumsArray.add("M1001 .H");
        callNumsArray.add("LD6353 1886");
        callNumsArray.add("M1001 .M939 S.3,13 2001");
        callNumsArray.add("LD6329 1903 35TH");
        // Non-LC
        callNumsArray.add("Sony PDX10");
        callNumsArray.add("RCA Jz(1)");
        callNumsArray.add("QA76");
        callNumsArray.add("M1");
    }
    
}
