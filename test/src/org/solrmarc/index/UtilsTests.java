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
    
    @Test
    public void testTitleCase()
    {
        assertEquals(DataUtil.toTitleCase("The sound of music"), "The Sound Of Music");
        assertEquals(DataUtil.toTitleCase("maRTin o'maLLEY"), "Martin O'Malley");
        assertEquals(DataUtil.toTitleCase("john wilkes-booth"), "John Wilkes-Booth");
        assertEquals(DataUtil.toTitleCase("YET ANOTHER STRING"), "Yet Another String");
    }
}
