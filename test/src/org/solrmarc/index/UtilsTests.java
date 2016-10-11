package org.solrmarc.index;

import static org.junit.Assert.*;

import org.junit.Test;
import org.solrmarc.tools.DataUtil;

public class UtilsTests
{

    @Test
    public void testCleanData()
    {
        assertEquals(DataUtil.cleanData("[ microfilm] :"), "microfilm");
        assertEquals(DataUtil.cleanData("Ray Parker Jr. :"), "Ray Parker Jr.");
    }

}
