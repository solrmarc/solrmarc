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
        DeweyCallNumber dewey = new DeweyCallNumber();

        callnum = "1 .I39";
        dewey.parse(callnum);
        assertEquals("1", dewey.classification);
        assertEquals("I39", dewey.cutter);
        assertEquals(null, dewey.suffix);

        callnum = "324.6 .A75CUA"; // letters without space
        dewey.parse(callnum);
        assertEquals("324.6", dewey.classification);
        assertEquals("A75CUA", dewey.cutter);
        assertEquals(null, dewey.suffix);
    }

    /**
     * unit test for getting classification from Dewey call number
     */
    @Test
    public void testDeweyGetClass()
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
        callnum = "324.54 .I39 F"; // letter with space
        // assertEquals("I39 F", new DeweyCallNumber(callnum).getCutter());
        assertEquals("I39", new DeweyCallNumber(callnum).getCutter());
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("C425R", new DeweyCallNumber(callnum).getCutter());
        callnum = "324.6 .A75CUA"; // letters without space
        assertEquals("A75CUA", new DeweyCallNumber(callnum).getCutter());

        // suffixes
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("K43", new DeweyCallNumber(callnum).getCutter());
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("F816", new DeweyCallNumber(callnum).getCutter());
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("R513", new DeweyCallNumber(callnum).getCutter());
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("A512RE", new DeweyCallNumber(callnum).getCutter());
        callnum = "323 .A778 ED.2"; // suffix ed
        assertEquals("A778", new DeweyCallNumber(callnum).getCutter());
    }

    /**
     * unit test for getting the cutter suffix for Dewey call numbers
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
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("V.1", new DeweyCallNumber(callnum).getSuffix());
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("V.1 1974", new DeweyCallNumber(callnum).getSuffix());
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("1957", new DeweyCallNumber(callnum).getSuffix());
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("NO.23-28", new DeweyCallNumber(callnum).getSuffix());
        callnum = "323 .A778 ED.2"; // suffix ed
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
        assertEquals("001.00000000 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("001.23000000 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("011.00000000 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "11.34 .I39"; // two digits fraction
        assertEquals("011.34000000 I39", new DeweyCallNumber(callnum).getShelfKey());

        callnum = "111 .I39"; // no fraction in class
        assertEquals("111.00000000 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("111.00000000 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("111.00000000 Q39", new DeweyCallNumber(callnum).getShelfKey());

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("111.12000000 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("111.12300000 I39", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("111.13400000 Q39", new DeweyCallNumber(callnum).getShelfKey());

        // cutter has following letters
        callnum = "324.54 .I39 F"; // letter with space
        assertEquals("324.54000000 I39 F", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("324.54800000 C425R", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "324.6 .A75CUA"; // letters without space
        // TODO: this is broken. presumably not to many dewey call numbers with
        // multiple letters at end of cutter.
        // assertEquals("324.60000000 A75CUA", new DeweyCallNumber(callnum).getShelfKey());

        // suffixes
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("323.09000000 K43 V.000001", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("322.44000000 F816 V.000001 001974", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("322.45000000 R513 001957", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("323.00000000 A512RE NO.000023-000028", new DeweyCallNumber(callnum).getShelfKey());
        callnum = "323 .A778 ED.2"; // suffix ed
        assertEquals("323.00000000 A778 ED.000002", new DeweyCallNumber(callnum).getShelfKey());
    }

    /**
     * unit test for getting non-Dewey shelf key
     */
    @Test
    public void testNonDeweyShelfKey()
    {
        // Non-Dewey call number
        String callnum = "MC1 259";
        assertEquals(callnum, new DeweyCallNumber(callnum).getShelfKey());

        // real edge cases
        callnum = "";
        assertEquals(callnum, new DeweyCallNumber(callnum).getShelfKey());
        callnum = null;
        assertEquals(callnum, new DeweyCallNumber(callnum).getShelfKey());
    }

}
