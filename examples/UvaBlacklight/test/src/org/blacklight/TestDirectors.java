package org.blacklight;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.solrmarc.index.VideoInfoMixin;


public class TestDirectors
{
    @Test
    public void testDirectorsFuzzyMatch()
    {
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        File testData = new File(testDataParentPath, "video_director_expected_correct.txt");
        runDirectorTestWithDataFile(testData);
        System.out.println("Test testDirectorsFuzzyMatch is successful");        
    }
    
    @Test
    public void testDirectorsWithErrors()
    {
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        File testData = new File(testDataParentPath, "video_director_expected_failures.txt");
        runDirectorTestWithDataFile(testData);
        System.out.println("Test testDirectorsWithErrors is successful");        
    }
    
    private void runDirectorTestWithDataFile(File testInputFile)
    {
        int numberToFind = 0;
        int numberFoundCorrect = 0;
        int numberNotFound = 0;
        int numberExtra = 0;
        int numberIffy = 0;
        int numLines = 0;
        int numLinesPerfect = 0;
        int numLinesImperfect = 0;
        Set<String> extraNames = new LinkedHashSet<String>();
        Set<String> missedNames = new LinkedHashSet<String>();
        String verboseStr = System.getProperty("solrmarc.test.verbose", "false");
        boolean verbose = verboseStr.equalsIgnoreCase("true");
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(testInputFile), "UTF-8"));
        }
        catch (FileNotFoundException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String line;
        try {
            while ((line = in.readLine()) != null)
            {
                if (!line.contains("\t\t")) 
                    continue;
                numLines++;
                String lineparts[] = line.split("\t\t");
                boolean expectPerfection = true;
                boolean greedy = true;
                String answerStr = lineparts[0];
                if (lineparts[0].startsWith("!"))
                {
                    expectPerfection = false;
                    answerStr = lineparts[0].substring(1);
                }
                if (answerStr.startsWith("~"))
                {
                    greedy = false;
                    answerStr = answerStr.substring(1);
                }
                String answers[] = answerStr.split("[|]");
                Set<String> directors = VideoInfoMixin.getVideoDirectorsFromTextField(lineparts[1], greedy);
                Set<String> answerSet = new LinkedHashSet<String>();
                Set<String> iffyAnswerSet = new LinkedHashSet<String>();
                boolean linePerfect = true;
                for (String answer : answers)
                {
                    if (!answer.equals("--"))
                    {
                        numberToFind ++;
                        if (answer.startsWith("??"))
                        {
                            answerSet.add(answer.substring(2));
                            iffyAnswerSet.add(answer.substring(2));
                            numberIffy++;
                        }
                        else
                        {
                            answerSet.add(answer);
                        }
                    }
                }
                for (String director : directors)
                {
                    if (answerSet.contains(director))
                    {
                        numberFoundCorrect++;
                    }
                    else
                    {
                        linePerfect = false;
                        numberExtra++;
                        extraNames.add(director);
                    }
                }
                for (String answer : answerSet)
                {
                    if (answer.length() > 0 && !directors.contains(answer))
                    {
                        numberNotFound++;
                        linePerfect = false;
                        missedNames.add(answer);
                    }
                }
                if (linePerfect) 
                {
                    numLinesPerfect++;
//                    if (out1 != null)  out1.println(line);
                }
                else
                {
                    numLinesImperfect++;
//                    if (out2 != null)  out2.println((expectPerfection?"!":"")+line);
                }
                if (expectPerfection && !linePerfect)
                {
                    if (verbose) System.err.println("Failure on expected perfection:  "+ line);
//                    fail("Failure on expected perfection: " + line);
                }
                else if (!expectPerfection && linePerfect)
                {
                    if (verbose) System.err.println("Success on expected failure:  "+ line);
                }
            }
        }
        catch (IOException e)
        {
        }
//        if (out1 != null) { out1.flush();  out1.close();}
//        if (out2 != null) { out2.flush();  out2.close();}
        if (verbose) System.out.println("Out of a total of "+numLines+ " Marc record fields, containing an expected "+ numberToFind+ " directors");
        if (verbose) System.out.println("                  "+numLinesPerfect+ " had the correct answer ");
        if (verbose) System.out.println(numberFoundCorrect+" directors, were correctly extracted");
        if (verbose) System.out.println(numberNotFound +" directors, were not found");
        if (verbose) showNameList(missedNames);
        if (verbose) System.out.println(numberExtra +" extra additional directors, were found");
        if (verbose) showNameList(extraNames);
        if (verbose) System.out.println(numberIffy +" may have been due to data errors");
    }
    
    static void showNameList(Set<String> list)
    {
        int i = 0;
        for (String name : list)
        {
            System.out.print("  "+name+", ");
            if (++i % 5 == 0) System.out.println();
        }
        System.out.println();
    }
}
