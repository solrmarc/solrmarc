package org.solrmarc.callnum;


import java.util.ArrayList;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;

/**
 * Exercise the {@code LCCallNumber} class from the command line.
 * Illustrates parsing a shelf key construction.
 * Useful for debugging during development.
 * 
 * <p>From the root of the solrmarc distribution:
 * <p>{@code java -cp lib/solrmarc/build org.solrmarc.callnum.ExerciseLCCallNumber}
 * 
 * @author Tod Olson, University of Chicago
 *
 */
public class LCCallNumberTests {
    
    /**
     * array of call numbers for use as test data.
     */
    static ArrayList<String> callNums;
    
    @Before
    public void setup()
    {
        initCallNums();
    }

//    public static void main(String[] args) {
//        initCallNums();
//        exercisePatterns();
//        exerciseClass();
//        examineShelfKeyOrder();
//    }

    static String[][] shelfArray= {
            {"BF 1999 1973", "BF1999 .A63880"},
            {"BF1999 .A63880", "BF1999 Aarons"},
            {"BF1999 Aarons", "BF1999 .B34"},
            {"BF1999 .B34", "BF1999 C78"},
    };

    @Test
    public void examineShelfKeyOrder()
    {
        System.out.println();
        System.out.println("Examine Shelf Key Order");
        for (String[] pair: shelfArray)
        {
            LCCallNumber call1 = new LCCallNumber(pair[0]);
            LCCallNumber call2 = new LCCallNumber(pair[1]);
            String key1 = call1.getShelfKey();
            String key2 = call2.getShelfKey();
            
            System.out.print("\"" + key1 + "\"\tcompareTo \"" + key2 + "\" =\t");
            System.out.print(key1.compareTo(key2));
            System.out.print("\n");
        }
        
    }
    
    @Test
    public void exercisePatterns() 
    {
        banner("Exercise Patterns");
        for (String c : callNums) 
        {
            Matcher m = LCCallNumber.classPattern.matcher(c);
            System.out.printf("callnum:\t%s", c);
            StringBuilder result = new StringBuilder("\tresult:");
            if (m.matches()) 
            {
                for (int i = 1 ; i <= m.groupCount(); i++) 
                {
                    appendHelper(result, m.group(i));
                }
            } 
            else 
            {
                result.append("\t*** no match ***");
            }
            System.out.println(result);
        }
    }

    @Test
    public void exerciseClass() 
    {
        banner("Exercise LCCallNumber");
        for (String c : callNums) 
        {
            System.out.printf("call:\t%s", c);
            StringBuilder result = new StringBuilder("\nclass:");
            LCCallNumber call = new LCCallNumber(c);
            appendHelper(result, call.getClassification());
            appendHelper(result, call.getClassLetters());
            appendHelper(result, call.getClassDigits());
            appendHelper(result, call.getClassSuffix());
            result.append("\ncutter:");
            appendHelper(result, call.getCutter());
            result.append("\nkey:");
            appendHelper(result, call.getShelfKey());
            System.out.println(result);
            System.out.println();
        }
    }

    private static void appendHelper(StringBuilder buf, String str) 
    {
        buf.append('\t');
        buf.append(str);
        buf.append(';');
    }
    
    private static void banner(String msg) 
    {
        System.out.println("###");
        System.out.println("###" + " " + msg);
        System.out.println("###");
    }
    
    private static void initCallNums() {
        callNums = new ArrayList<String>();
        
        callNums.add("A1 B2 .C33");
        callNums.add("A1 B2 C33");
        callNums.add("A1 B2.C33");
        callNums.add("A1 B2C33");
        callNums.add("AB9 L3");
        callNums.add("BF199");
        callNums.add("BF199.");
        callNums.add("BF199.A1J7");
        callNums.add("G3841 .C2 1935 .M3");
        callNums.add("HC241.25 .I4 D47");
        callNums.add("HD 38.25.F8 R87 1989");
        callNums.add("HD38.25.F8 R87 1989");
        callNums.add("HE5.215 .N9/PT.A");
        callNums.add("HF 5549.5.T7 B294 1992");
        callNums.add("LD6329 1903 35TH");
        callNums.add("LD6353 1886");
        callNums.add("M1 .L33");
        callNums.add("M1 L33");
        callNums.add("M5 .L");
        callNums.add("M5 .L3 1902 V2 TANEYTOWN");
        callNums.add("M5 L3 1902 V.2");
        callNums.add("M5 L3 1902 V2");
        callNums.add("M5 L3 1902");
        callNums.add("M5 L3 1902V");
        callNums.add("M211 .M93 BMW240");
        callNums.add("M211 .M93 K.240 1988 .A1");
        callNums.add("M211 .M93 K.240 1988 A1");
        callNums.add("M211 .M93 K.240");
        callNums.add("M453 .Z29 Q1 L V.2");
        callNums.add("M857 .K93 H2 OP.79");
        callNums.add("M857 .M93 S412B M");
        callNums.add("M1001 .H");
        callNums.add("M1001 .M9 1900Z");
        callNums.add("M1001 .M9 K.173D B");
        callNums.add("M1001 .M9 K.551 1900Z M");
        callNums.add("M1001 .M939 S.3,13 2001");
        callNums.add("ML410 .M8 L25 .M95 1995");
        callNums.add("ML410 .M8 L25 M95 1995");
        callNums.add("ML410 .M9 P29 1941 M");
        callNums.add("MT130 .M93 K96 .W83 1988");
        callNums.add("MT130 .M93 K96 W83 1988");
        callNums.add("MT37 2003M384");
        callNums.add("PQ2678.K26 P54");
        callNums.add("PQ8550.21.R57 V5 1992");
        callNums.add("PR919 .L33 1990");
        callNums.add("PR9199 .A39");
        callNums.add("PR9199.48 .B3");
        callNums.add("PR92 .L33 1990");
        callNums.add("PS153 .G38 B73 2012");
        
        // incomplete or local
        callNums.add("QA76");
        callNums.add("M1");
        callNums.add("XXKD671.G53 2012");
        callNums.add("BF1999.A63880 1978");
        callNums.add("BF1999 Aarons");
        
        // Non-LC
        callNums.add("Sony PDX10");
        callNums.add("RCA Jz(1)");
        
        // Lower case input
        callNums.add("bq1270");
        callNums.add("l666 15th A8");

    }
    
}

