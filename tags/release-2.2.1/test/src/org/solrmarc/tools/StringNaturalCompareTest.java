package org.solrmarc.tools;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;

import org.junit.Test;


public class StringNaturalCompareTest
{
    @Test
    public void testStringNaturalComparison()
    {
        Comparator<String> comp = new StringNaturalCompare();
        String str1 = "1";
        String str2 = "2";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "u1";
        str2 = "u2";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "   1";
        str2 = " 1";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 0, comp.compare(str1, str2));
        str1 = "   ";
        str2 = " 1";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "   1";
        str2 = "  ";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 1, comp.compare(str1, str2));
        str1 = "1.0566";
        str2 = "1.057";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "1.057";
        str2 = "1.0566";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 1, comp.compare(str1, str2));
        str1 = "1.0566stuff";
        str2 = "1.057stuff";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "1.057stuff";
        str2 = "1.0566stuff";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 1, comp.compare(str1, str2));
        str1 = "u100";
        str2 = "u2";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 1, comp.compare(str1, str2));
        str1 = "u2";
        str2 = "u100";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "u100.45367";
        str2 = "u100.45467";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "U100";
        str2 = "u2";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 1, comp.compare(str1, str2));
        str1 = "U2";
        str2 = "u2";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 0, comp.compare(str1, str2));
        str1 = "200times";
        str2 = "200titles";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "200titles";
        str2 = "200times";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 1, comp.compare(str1, str2));
        str1 = "201times";
        str2 = "2000rew";
        assertEquals("string natural comparison error for " + str1 + " " + str2, -1, comp.compare(str1, str2));
        str1 = "2000times";
        str2 = "201ree";
        assertEquals("string natural comparison error for " + str1 + " " + str2, 1, comp.compare(str1, str2));
    }

}
