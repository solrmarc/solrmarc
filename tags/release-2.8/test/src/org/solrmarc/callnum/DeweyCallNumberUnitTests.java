package org.solrmarc.callnum;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for <code>DeweyCallNumber</code>.
 *
 * Initially copied from on Naomi Dushay's unit tests in <code>CallNumberUnitTests</code>.
 *
 * @author Naomi Dushay, Stanford University
 * @author Tod Olson, University of Chicago
 *
 */
public class DeweyCallNumberUnitTests {

    /**
     * light sanity test for parse, check different fields
     */
    @Test
    public void testParse()
    {
        String callnum;
        DeweyCallNumber dewey;

        callnum = "1 .I39";
        dewey = new DeweyCallNumber(callnum);
        assertEquals("1", dewey.classification);
        assertEquals("I39", dewey.cutter);
        assertEquals(null, dewey.cutterSuffix);

        callnum = "324.6 .A75CUA"; // letters without space
        dewey = new DeweyCallNumber(callnum);
        assertEquals("324.6", dewey.classification);
        assertEquals("A75CUA", dewey.cutter);
        assertEquals(null, dewey.cutterSuffix);
    }

    /**
     * unit test for getting classification from Dewey call number
     */
    @Test
    public void testDeweyGetClassification()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("1", new DeweyCallNumber(callnum).getClassification());
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("1.23", new DeweyCallNumber(callnum).getClassification());
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("11", new DeweyCallNumber(callnum).getClassification());
        callnum = "11.34567 .I39"; // two digits fraction
        assertEquals("11.34567", new DeweyCallNumber(callnum).getClassification());

        callnum = "111 .I39"; // no fraction in class
        assertEquals("111", new DeweyCallNumber(callnum).getClassification());
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("111", new DeweyCallNumber(callnum).getClassification());
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("111", new DeweyCallNumber(callnum).getClassification());

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("111.12", new DeweyCallNumber(callnum).getClassification());
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("111.123", new DeweyCallNumber(callnum).getClassification());
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("111.134", new DeweyCallNumber(callnum).getClassification());
    }

    /**
     * unit test for getting normalized classification from Dewey call number
     */
    @Test
    public void testDeweyGetClassificationNormalized()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("001", new DeweyCallNumber(callnum).getClassificationNormalized());
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("001.23", new DeweyCallNumber(callnum).getClassificationNormalized());
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("011", new DeweyCallNumber(callnum).getClassificationNormalized());
        callnum = "11.34567 .I39"; // two digits fraction
        assertEquals("011.34567", new DeweyCallNumber(callnum).getClassificationNormalized());

        callnum = "111 .I39"; // no fraction in class
        assertEquals("111", new DeweyCallNumber(callnum).getClassificationNormalized());
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("111", new DeweyCallNumber(callnum).getClassificationNormalized());
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("111", new DeweyCallNumber(callnum).getClassificationNormalized());
    }

    /**
     * unit test for getting the cutter for Dewey call numbers
     */
    @Test
    public void testDeweyCutter()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "11.34 .I39"; // two digits fraction
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());

        callnum = "111 .I39"; // no fraction in class
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("Q39", new DeweyCallNumber(callnum).getCutter());

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("Q39", new DeweyCallNumber(callnum).getCutter());

        // cutter has following letters
        callnum = "324.54 .I39 F"; // letter with space (DEFERRED: messes up volume suffixes)
        //assertEquals("I39 F", new DeweyCallNumber(callnum).getCutter());
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        //assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("C425R", new DeweyCallNumber(callnum).getCutter());
        callnum = "324.6 .A75CUA"; // letters without space
        assertEquals("A75CUA", new DeweyCallNumber(callnum).getCutter());

        // suffixes
        callnum = "323.09 .K43 V.1"; // cutterSuffix volume
        assertEquals("K43", new DeweyCallNumber(callnum).getCutter());
        callnum = "322.44 .F816 V.1 1974"; // cutterSuffix - volume and year
        assertEquals("F816", new DeweyCallNumber(callnum).getCutter());
        callnum = "322.45 .R513 1957"; // cutterSuffix year
        assertEquals("R513", new DeweyCallNumber(callnum).getCutter());
        callnum = "323 .A512RE NO.23-28"; // cutterSuffix no.
        assertEquals("A512RE", new DeweyCallNumber(callnum).getCutter());
        callnum = "323 .A778 ED.2"; // cutterSuffix ed
        assertEquals("A778", new DeweyCallNumber(callnum).getCutter());
    }

    /**
     * unit test for getting the cutter cutterSuffix for Dewey call numbers
     */
    @Test
    public void testDeweyCutterSuffix()
    {
        // cutter has following letters
        String callnum = "324.54 .I39 F"; // letter with space
        DeweyCallNumber dewey = new DeweyCallNumber(callnum);
        assertEquals("I39", dewey.cutter);
        assertEquals("I39", dewey.getCutter());
        assertEquals("F", dewey.getSuffix());

        callnum = "324.548 .C425R"; // letter without space
        dewey = new DeweyCallNumber(callnum);
        assertEquals("C425R", dewey.getCutter());
        assertNull(dewey.getSuffix());

        callnum = "324.6 .A75CUA"; // letters without space
        dewey = new DeweyCallNumber(callnum);
        assertEquals("A75CUA", dewey.getCutter());
        assertNull(dewey.getSuffix());

        // suffixes
        callnum = "323.09 .K43 V.1"; // cutterSuffix volume
        assertEquals("V.1", new DeweyCallNumber(callnum).getSuffix());
        callnum = "322.44 .F816 V.1 1974"; // cutterSuffix - volume and year
        assertEquals("V.1 1974", new DeweyCallNumber(callnum).getSuffix());
        callnum = "322.45 .R513 1957"; // cutterSuffix year
        assertEquals("1957", new DeweyCallNumber(callnum).getSuffix());
        callnum = "323 .A512RE NO.23-28"; // cutterSuffix no.
        assertEquals("NO.23-28", new DeweyCallNumber(callnum).getSuffix());
        callnum = "323 .A778 ED.2"; // cutterSuffix ed
        assertEquals("ED.2", new DeweyCallNumber(callnum).getSuffix());
    }

    /**
     * unit test for getting Dewey shelf key
     */
    @Test
    public void testDeweyShelfKey()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("11 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("11.23 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("211 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "11.34 .I39"; // two digits fraction
        assertEquals("211.34 I39", new DeweyCallNumber(callnum).getShelfKey());

        callnum = "111 .I39"; // no fraction in class
        assertEquals("3111 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("3111 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("3111 Q39", new DeweyCallNumber(callnum).getShelfKey());

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("3111.12 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("3111.123 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("3111.134 Q39", new DeweyCallNumber(callnum).getShelfKey());

        // cutter has following letters
        callnum = "324.54 .I39 F"; // letter with space
        assertEquals("3324.54 I39 F", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("3324.548 C425R", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "324.6 .A75CUA"; // letters without space
        // TODO: this is broken. presumably not to many dewey call numbers with
        // multiple letters at end of cutter.
        assertEquals("3324.6 A75CUA", new DeweyCallNumber(callnum).getShelfKey());

        // suffixes
        callnum = "323.09 .K43 V.1"; // cutterSuffix volume
        assertEquals("3323.09 K43 V 11", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "322.44 .F816 V.1 1974"; // cutterSuffix - volume and year
        assertEquals("3322.44 F816 V 11 41974", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "322.45 .R513 1957"; // cutterSuffix year
        assertEquals("3322.45 R513 41957", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "323 .A512RE NO.23-28"; // cutterSuffix no.
        assertEquals("3323 A512RE NO 223 228", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "323 .A778 ED.2"; // cutterSuffix ed
        assertEquals("3323 A778 ED 12", new DeweyCallNumber(callnum).getShelfKey());
    }

    /**
     * unit test for getting non-Dewey shelf key
     */
    @Test
    public void testNonDeweyShelfKey()
    {
        // Non-Dewey call number
        String callnum = "MC1 259";
        assertEquals("MC 11 3259", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "T1 105";
        assertEquals("T 11 3105", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "T3 628 no.75-021";
        assertEquals("T 13 3628 NO 275 221", new DeweyCallNumber(callnum).getShelfKey());
        
        // real edge cases
        assertEquals("", new DeweyCallNumber("").getShelfKey());
        assertNull(new DeweyCallNumber(null).getShelfKey());
    }
    
    /**
     * unit test for getShelfKey: check empty string call number
     */
    @Test
    public void testGetShelfKey_emptyCallNumber()
    {
        String callnum = "";
        assertEquals("", new DeweyCallNumber(callnum).getShelfKey());
    }

    /**
     * unit test for getShelfKey: check whitespace call number
     */
    @Test
    public void testGetShelfKey_whitespaceCallNumber()
    {
        String callnum = " ";
        assertEquals("", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "    "; // suffix with letters
        assertEquals("", new DeweyCallNumber(callnum).getShelfKey());
    }
    
    /**
     * unit test for getShelfKey: check null call number
     */
    @Test
    public void testGetShelfKey_nullCallNumber()
    {
        assertNull(new DeweyCallNumber(null).getShelfKey());
    }

    /**
     * unit test for getShelfKey: parse has not been called
     */
    @Test
    public void testGetShelfKey_notParsed()
    {
        assertNull(new LCCallNumber().getShelfKey());
    }

    @Test
    public void testDeweyIsValid()
    {
        String callnum;
        
        callnum = "1 .I39";
        assertTrue(new DeweyCallNumber(callnum).isValid());
        callnum = "323 .A512RE NO.23-28";
        assertTrue(new DeweyCallNumber(callnum).isValid());
    }
}
