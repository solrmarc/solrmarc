package org.solrmarc.callnum;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for <code>UChicagoUChicagoDeweyCallNumber</code>.
 *
 * Initially copied from on Naomi Dushay's unit tests in <code>CallNumberUnitTests</code>.
 *
 * @author Naomi Dushay, Stanford University
 * @author Tod Olson, University of Chicago
 *
 */
public class UChicagoDeweyCallNumberUnitTests {

    /**
     * light sanity test for parse, check different fields
     */
    @Test
    public void testParse()
    {
        String callnum;
        UChicagoDeweyCallNumber dewey = new UChicagoDeweyCallNumber();

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
        assertEquals("1", new UChicagoDeweyCallNumber(callnum).getClassification());
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("1.23", new UChicagoDeweyCallNumber(callnum).getClassification());
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("11", new UChicagoDeweyCallNumber(callnum).getClassification());
        callnum = "11.34567 .I39"; // two digits fraction
        assertEquals("11.34567", new UChicagoDeweyCallNumber(callnum).getClassification());

        callnum = "111 .I39"; // no fraction in class
        assertEquals("111", new UChicagoDeweyCallNumber(callnum).getClassification());
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("111", new UChicagoDeweyCallNumber(callnum).getClassification());
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("111", new UChicagoDeweyCallNumber(callnum).getClassification());

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("111.12", new UChicagoDeweyCallNumber(callnum).getClassification());
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("111.123", new UChicagoDeweyCallNumber(callnum).getClassification());
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("111.134", new UChicagoDeweyCallNumber(callnum).getClassification());
    }

    /**
     * unit test for getting the cutter for Dewey call numbers
     */
    @Test
    public void testDeweyCutter()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "11.34 .I39"; // two digits fraction
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());

        callnum = "111 .I39"; // no fraction in class
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("Q39", new UChicagoDeweyCallNumber(callnum).getCutter());

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("Q39", new UChicagoDeweyCallNumber(callnum).getCutter());

        // cutter has following letters
        callnum = "324.54 .I39 F"; // letter with space
        // assertEquals("I39 F", new UChicagoDeweyCallNumber(callnum).getCutter());
        assertEquals("I39", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("C425R", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "324.6 .A75CUA"; // letters without space
        assertEquals("A75CUA", new UChicagoDeweyCallNumber(callnum).getCutter());

        // suffixes
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("K43", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("F816", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("R513", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("A512RE", new UChicagoDeweyCallNumber(callnum).getCutter());
        callnum = "323 .A778 ED.2"; // suffix ed
        assertEquals("A778", new UChicagoDeweyCallNumber(callnum).getCutter());
    }

    /**
     * unit test for getting the cutter suffix for Dewey call numbers
     */
    @Test
    public void testDeweyCutterSuffix()
    {
        // cutter has following letters
        String callnum = "324.54 .I39 F"; // letter with space
        UChicagoDeweyCallNumber dewey = new UChicagoDeweyCallNumber(callnum);
        assertEquals("I39", dewey.cutter);
        assertEquals("I39", dewey.getCutter());
        assertEquals("F", dewey.getSuffix());

        callnum = "324.548 .C425R"; // letter without space
        dewey = new UChicagoDeweyCallNumber(callnum);
        assertEquals("C425R", dewey.getCutter());
        assertNull(dewey.getSuffix());

        callnum = "324.6 .A75CUA"; // letters without space
        dewey = new UChicagoDeweyCallNumber(callnum);
        assertEquals("A75CUA", dewey.getCutter());
        assertNull(dewey.getSuffix());

        // suffixes
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("V.1", new UChicagoDeweyCallNumber(callnum).getSuffix());
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("V.1 1974", new UChicagoDeweyCallNumber(callnum).getSuffix());
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("1957", new UChicagoDeweyCallNumber(callnum).getSuffix());
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("NO.23-28", new UChicagoDeweyCallNumber(callnum).getSuffix());
        callnum = "323 .A778 ED.2"; // suffix ed
        assertEquals("ED.2", new UChicagoDeweyCallNumber(callnum).getSuffix());
    }

    /**
     * unit test for getting Dewey shelf key
     */
    @Test
    public void testDeweyShelfKey()
    {
        // missing leading zeros in class
        String callnum = "1 .I39"; // one digit no fraction
        assertEquals("001.00000000 I39", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "1.23 .I39"; // one digit fraction
        assertEquals("001.23000000 I39", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "11 .I39"; // two digits no fraction
        assertEquals("011.00000000 I39", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "11.34 .I39"; // two digits fraction
        assertEquals("011.34000000 I39", new UChicagoDeweyCallNumber(callnum).getShelfKey());

        callnum = "111 .I39"; // no fraction in class
        assertEquals("111.00000000 I39", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "111 I39"; // no fraction no period before cutter
        assertEquals("111.00000000 I39", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "111Q39"; // no fraction, no period or space before cutter
        assertEquals("111.00000000 Q39", new UChicagoDeweyCallNumber(callnum).getShelfKey());

        callnum = "111.12 .I39"; // fraction in class, space period
        assertEquals("111.12000000 I39", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "111.123 I39"; // space but no period before cutter
        assertEquals("111.12300000 I39", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "111.134Q39"; // no period or space before cutter
        assertEquals("111.13400000 Q39", new UChicagoDeweyCallNumber(callnum).getShelfKey());

        // cutter has following letters
        callnum = "324.54 .I39 F"; // letter with space
        assertEquals("324.54000000 I39 F", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "324.548 .C425R"; // letter without space
        assertEquals("324.54800000 C425R", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "324.6 .A75CUA"; // letters without space
        // TODO: this is broken. presumably not to many dewey call numbers with
        // multiple letters at end of cutter.
        // assertEquals("324.60000000 A75CUA", new UChicagoDeweyCallNumber(callnum).getShelfKey());

        // suffixes
        callnum = "323.09 .K43 V.1"; // suffix volume
        assertEquals("323.09000000 K43 V.000001", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "322.44 .F816 V.1 1974"; // suffix - volume and year
        assertEquals("322.44000000 F816 V.000001 001974", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "322.45 .R513 1957"; // suffix year
        assertEquals("322.45000000 R513 001957", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "323 .A512RE NO.23-28"; // suffix no.
        assertEquals("323.00000000 A512RE NO.000023-000028", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = "323 .A778 ED.2"; // suffix ed
        assertEquals("323.00000000 A778 ED.000002", new UChicagoDeweyCallNumber(callnum).getShelfKey());
    }

    /**
     * unit test for getting non-Dewey shelf key
     */
    @Test
    public void testNonDeweyShelfKey()
    {
        // Non-Dewey call number
        String callnum = "MC1 259";
        UChicagoDeweyCallNumber dewey = new UChicagoDeweyCallNumber(callnum);
        assertEquals("MC1  0259", dewey.getShelfKey());

        // real edge cases
        callnum = "";
        assertEquals("", new UChicagoDeweyCallNumber(callnum).getShelfKey());
        callnum = null;
        assertNull(new UChicagoDeweyCallNumber(callnum).getShelfKey());
    }

    /**
     * unit test for Crerar accession numbers
     *
     */
    @Test
    public void testCrearAccession()
    {
        String callnum = "MC1 259";
        UChicagoDeweyCallNumber dewey = new UChicagoDeweyCallNumber(callnum);
        assertFalse(dewey.isValid());
        assertTrue(dewey.isAccession());
        assertEquals(callnum, dewey.classification);
        assertEquals("MC1", dewey.getAccessionSeries());
        assertEquals("259", dewey.getAccessionNumber());
        assertNull(dewey.getSuffix());
        assertEquals("MC1  0259", dewey.getShelfKey());

        callnum = "T3 628 no.75-021";
        dewey = new UChicagoDeweyCallNumber(callnum);
        assertFalse(dewey.isValid());
        assertTrue(dewey.isAccession());
        assertEquals(callnum, dewey.classification);
        assertEquals("T3", dewey.getAccessionSeries());
        assertEquals("628", dewey.getAccessionNumber());
        assertEquals("no.75-021", dewey.getSuffix());
        assertEquals("T3   0628 no.75-021", dewey.getShelfKey());

        callnum = "T1751";
        dewey = new UChicagoDeweyCallNumber(callnum);
        assertFalse(dewey.isValid());
        assertTrue(dewey.isAccession());
        assertEquals(callnum, dewey.classification);
        assertEquals("T1751", dewey.getAccessionSeries());
        assertNull(dewey.getAccessionNumber());
        assertNull(dewey.getSuffix());
        assertEquals("T1751", dewey.getShelfKey());

        callnum = "T1584 cong.86 sess.1-2 1959-60";
        dewey = new UChicagoDeweyCallNumber(callnum);
        assertFalse(dewey.isValid());
        assertTrue(dewey.isAccession());
        assertEquals(callnum, dewey.classification);
        assertEquals("T1584", dewey.getAccessionSeries());
        assertNull(dewey.getAccessionNumber());
        assertEquals("cong.86 sess.1-2 1959-60", dewey.getSuffix());
        assertEquals("T1584 cong.86 sess.1-2 1959-60", dewey.getShelfKey());
    }

}
