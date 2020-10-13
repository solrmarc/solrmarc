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
    public void testCleanDate()
    {
        assertEquals(DataUtil.cleanDate("197?"), "1970");
        assertEquals(DataUtil.cleanDate("[19]72"), "1972");
        assertEquals(DataUtil.cleanDate("1500"), "1500");
        assertEquals(DataUtil.cleanDate("l500"), "1500"); // "L" instead of "1"
        assertEquals(DataUtil.cleanDate("i.e. 1500"), "1500");
        assertEquals(DataUtil.cleanDate("[2021]"), "2021");
        assertEquals(DataUtil.cleanDate("[2021"), "2021");
        assertNull(DataUtil.cleanDate("2000 B.C.")); // B.C. dates intentionally ignored
        assertEquals(DataUtil.cleanDate("1400"), "1400");
    }

    @Test
    public void testTitleCase()
    {
        assertEquals(DataUtil.toTitleCase("YET ANOTHER STRING"), "Yet Another String");
        assertEquals(DataUtil.toTitleCase("Robert F.W. Allston papers"), "Robert F.W. Allston Papers");
        assertEquals(DataUtil.toTitleCase("The sound of music"), "The Sound Of Music");
        assertEquals(DataUtil.toTitleCase("martin o'malley"), "Martin O'Malley");
        assertEquals(DataUtil.toTitleCase("john wilkes-booth"), "John Wilkes-Booth");
        assertEquals(DataUtil.toTitleCase("Hollywood musicals of the 1960's"), "Hollywood Musicals Of The 1960's");
        assertEquals(DataUtil.toTitleCase("Organized interests and self-regulation"), "Organized Interests And Self-Regulation");
        assertEquals(DataUtil.toTitleCase("U.S. foreign affairs on CD-ROM"), "U.S. Foreign Affairs On CD-ROM");
        assertEquals(DataUtil.toTitleCase("One sings, the other doesn't"), "One Sings, The Other Doesn't");
    }
}
