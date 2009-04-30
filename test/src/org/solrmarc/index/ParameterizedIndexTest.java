package org.solrmarc.index;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.solrmarc.marc.MarcPrinter;

@RunWith(Parameterized.class)
public class ParameterizedIndexTest
{
    String config;
    String recordFilename;
    String fieldToCheck;
    String expectedValue;
    static String dataDirectory;
    static String dataFile;
    
    public ParameterizedIndexTest(String config, String recordFilename, String fieldToCheck, String expectedValue)
    {
        this.config = config;
        this.recordFilename = recordFilename;
        this.fieldToCheck = fieldToCheck;
        this.expectedValue = expectedValue;
    }
    
    @Test
    public void verifyIndexingResults() throws Exception 
    {
        StringWriter strOut = new StringWriter();
        PrintWriter pOut = new PrintWriter(strOut);
        String fullRecordFilename = dataDirectory + File.separator + recordFilename;
        MarcPrinter indexTest = new MarcPrinter(new String[]{config, fullRecordFilename, "index", fieldToCheck}, pOut );
        indexTest.handleAll();
        StringBuffer buffer = strOut.getBuffer();
        //System.out.println(buffer.toString());
        String resultLines[] = buffer.toString().split("\r?\n");
        
        String results[] = new String[resultLines.length];
        for (int i = 0; i < resultLines.length; i++)
        {
            results[i] = resultLines[i].replaceAll(".* : .* = ", "");
            //System.out.println("" + i + ":  " + resultLines[i] + "   -->  " + results[i] );
        }
        String expected[] = expectedValue.split("[|]");
        assertEquals("Array lengths should be equal", expected.length, results.length);
        for (int i = 0; i < results.length; i++)
        {
            assertEquals("Array entries should be equal", expected[i], results[i]);
        }
        System.out.println(config + " : " + recordFilename + " : " + fieldToCheck + " --> " + expectedValue);
    }
    
    @Parameters
    public static Collection indexValues() throws Exception
    {
        dataDirectory = System.getProperty("test.data.path");
        dataFile = System.getProperty("test.data.file");
        String fullIndexTestFilename = dataDirectory + File.separator + dataFile;
        File file = new File(fullIndexTestFilename);
        BufferedReader rIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        List result = new LinkedList();
        while (( line = rIn.readLine()) != null)
        {
            if (line.startsWith("#") || line.trim().length() == 0) continue;
            String split[] = line.split(", ", 4);
            if (split.length == 4) 
                result.add(split);
        }
        return(result);
    
    }
}
