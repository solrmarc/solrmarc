package org.solrmarc.callnum;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author Tod Olson, University of Chicago
 *
 */
public class UtilsUnitTests {

    // column 1 = number, column 2 = sort key
    String[][] sortableNumberArray = {
            {"1", "11"},
            {"89", "289"},
            {"0", "0"},
            {"0002", "12"},
            {"00", "0"},
            {"4725.2", "44725.2"},
    };
    
    @Test
    public void testAppendSortableNumber() {
        for (int i = 0; i < sortableNumberArray.length; i++) {
            String in = sortableNumberArray[i][0];
            String out = sortableNumberArray[i][1];
            StringBuilder outBuf = new StringBuilder();
            Utils.appendSortableNumber(outBuf, in);
            assertEquals(out, outBuf.toString());
        }
    }
    
    String[][] numSortArray = {
            {"1960", "41960"},
            {"100TH", "3100TH"},
            {"100TH AND 17", "3100TH AND 217"},
            {"reel 6038, no. 08", "REEL 46038 NO 18"},
    };
    
    @Test
    public void testNumSortHelper() {
        for (String[] subArray:numSortArray) {
            String in = subArray[0];
            String out = subArray[1];
            StringBuilder outBuf = new StringBuilder();
            Utils.appendNumericallySortable(outBuf, in);
            assertEquals(out, outBuf.toString());
        }
    }

}
