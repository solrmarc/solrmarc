package org.solrmarc.callnum;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;

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
public class DeweyCallNumberTests
{
    /**
     * array of call numbers for use as test data.
     */
    ArrayList<TestData> callNumsData;
    class TestData 
    {
        String callnum;
        boolean valid;
        public TestData(String callnum, boolean valid)
        {
            this.callnum = callnum;
            this.valid = valid;
        }
    };
    
    @Before
    public void setup()
    {
        initCallNums();
    }

    @Test
    public void exercisePatterns() 
    {
        for (TestData td : callNumsData) 
        {
            String callnum = td.callnum;
            Matcher m = DeweyCallNumber.classPattern.matcher(callnum);
            assertTrue(td.valid == m.matches()); 
        }
    }

    @Test
    public void exerciseClass() 
    {
        banner("Exercise DeweyCallNumber");
        for (TestData td : callNumsData) 
        {
            String callnum = td.callnum;
            System.out.printf("call:\t%s", callnum);
            StringBuilder result = new StringBuilder("\nclass:");
            DeweyCallNumber call = new DeweyCallNumber(callnum);
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
   
    @Test
    public void exerciseShelfKey() 
    {
        banner("Exercise DeweyShelfKey");
        String prevShelfKey = "";
        for (TestData td : callNumsData) 
        {
            String callnum = td.callnum;
            DeweyCallNumber call = new DeweyCallNumber(callnum);
            String shelfKey = call.getShelfKey();
            System.out.println(call.getShelfKey());
            assertTrue(shelfKey.compareTo(prevShelfKey) >= 0);
            prevShelfKey = shelfKey;
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
    
    private void initCallNums() 
    {
        callNumsData = new ArrayList<TestData>();
        callNumsData.add(new TestData("", false));
        callNumsData.add(new TestData("1 .I39",  true));         // one digit no fraction
        callNumsData.add(new TestData("1.23 .I39",  true));  // one digit fraction
        callNumsData.add(new TestData("11 .I39",  true));  // two digits no fraction
        callNumsData.add(new TestData("11.34 .I39",  true));  // two digits fraction
        callNumsData.add(new TestData("11.34567 .I39",  true));  // two digits fraction
        callNumsData.add(new TestData("111 .I39",  true));  // no fraction in class
        callNumsData.add(new TestData("111 I39",  true));  // no fraction no period before cutter
        callNumsData.add(new TestData("111Q39",  true));  // no fraction, no period or space before cutter
        callNumsData.add(new TestData("111.12 .I39",  true));  // fraction in class, space period
        callNumsData.add(new TestData("111.123 I39",  true));  // space but no period before cutter
        callNumsData.add(new TestData("111.134Q39",  true));  // no period or space before cutter
        callNumsData.add(new TestData("322.44 .F816 V.1 1974",  true));  // cutterSuffix - volume and year
        callNumsData.add(new TestData("322.45 .R513 1957",  true));  // cutterSuffix year
        callNumsData.add(new TestData("323 .A512RE NO.23-28",  true));  // cutterSuffix no.
        callNumsData.add(new TestData("323 .A778 ED.2",  true));  // cutterSuffix ed
        callNumsData.add(new TestData("323.09 .K43 V.1",  true));  // cutterSuffix volume
        callNumsData.add(new TestData("324.54 .I39 F",  true));  // letter with space
        callNumsData.add(new TestData("324.548 .C425R",  true));  // letter without space
        callNumsData.add(new TestData("324.6 .A75CUA",  true));  // letters without space
        callNumsData.add(new TestData("MC1 259", false));
        callNumsData.add(new TestData("T1 105", false));
    }
}
