package org.solrmarc.callnum;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author Tod Olson, University of Chicago
 *
 */
public class UtilsUnitTests {

    @Test
    public void testNormalizeFloat() {
        // regular floats
        assertEquals("123.456", Utils.normalizeFloat("123.456", 3, 3));
        assertEquals("0123.456000", Utils.normalizeFloat("123.456", 4, 6));
        // decimal only
        assertEquals("123.000", Utils.normalizeFloat("123", 3, 3));
        assertEquals("0123.000000", Utils.normalizeFloat("123", 4, 6));
        // no padding
        assertEquals("123.456", Utils.normalizeFloat("123.456", -1, -1));
        // trim needless zeroes
        assertEquals("123.456", Utils.normalizeFloat("0123.45600", -1, -1));
    }

    // TODO: what should happen if the string is too long for the units or decimals?
    @Test
    public void testNormalizeFloatStringTooLong() {
        //assertEquals("???", Utils.normalizeFloat("123.456", 3, 2));
        //assertEquals("???", Utils.normalizeFloat("123.456", 2, 3));
    }

    @Test (expected = NumberFormatException.class)
    public void testNormalizeFloatNonFloatString() {
        Utils.normalizeFloat("Not a float", 3, 2);
        fail("should have thrown a java.lang.NumberFormatException when formating a non-float");
    }
/*
    @Test
    public void test() {
        fail("Not yet implemented");
    }
*/

}
