package org.solrmarc.index;

import static org.junit.Assert.*;

import org.junit.Test;
import org.solrmarc.tools.Utils;

public class UtilsTests
{

    @Test
    public void testCleanData()
    {
        assertEquals(Utils.cleanData("[ microfilm] :"), "microfilm");
        assertEquals(Utils.cleanData("Ray Parker Jr. :"), "Ray Parker Jr.");
    }

}
