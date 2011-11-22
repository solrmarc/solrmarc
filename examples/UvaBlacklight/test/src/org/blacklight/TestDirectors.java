package org.blacklight;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
        int numberToFind = 0;
        int numberFoundCorrect = 0;
        int numberNotFound = 0;
        int numberExtra = 0;
        int numberIffy = 0;
        int numLines = 0;
        int numLinesPerfect = 0;
        int numLinesImperfect = 0;
        
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("video_director_test.txt")),"UTF8"));
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
        PrintWriter out1 = null;
        PrintWriter out2 = null;
        try
        {
            out1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("video_director_test_out1.txt")),"UTF8"));
            out2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("video_director_test_out2.txt")),"UTF8"));
        }
        catch (UnsupportedEncodingException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (FileNotFoundException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String line;
        String prevline = "";
        try {
            while ((line = in.readLine()) != null)
            {
                if (!line.contains("\t\t")) 
                    continue;
                numLines++;
                String lineparts[] = line.split("\t\t");
                boolean expectPerfection = true;
                boolean expectToFix = false;
                String answerStr = lineparts[0];
                if (lineparts[0].startsWith("!"))
                {
                    expectPerfection = false;
                    answerStr = lineparts[0].substring(1);
                }
                if (answerStr.startsWith("~"))
                {
                    expectToFix = false;
                    answerStr = lineparts[0].substring(1);
                }
                String answers[] = answerStr.split("[|]");
                Set<String> directors = VideoInfoMixin.getVideoDirectorsFromTextField(lineparts[1]);
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
                    }
                }
                for (String answer : answerSet)
                {
                    if (answer.length() > 0 && !directors.contains(answer))
                    {
                        numberNotFound++;
                        linePerfect = false;
                    }
                }
                if (linePerfect) 
                {
                    numLinesPerfect++;
                    out1.println(line);
                }
                else
                {
                    numLinesImperfect++;
                    out2.println((expectPerfection?"!":"")+line);
                }
                if (expectPerfection && !linePerfect)
                {
                    System.err.println("Failure on expected perfection"+ line);
                }
            }
        }
        catch (IOException e)
        {
        }
        out1.flush();
        out1.close();
        out2.flush();
        out2.close();
        System.out.println("Out of a total of "+numLines+ " Marc record fields, containing an expected "+ numberToFind+ " directors");
        System.out.println("                  "+numLinesPerfect+ " had the correct answer ");
        System.out.println(numberFoundCorrect+" directors, were correctly extracted");
        System.out.println(numberNotFound +" directors, were not found");
        System.out.println(numberExtra +" extra additional directors, were found");
        System.out.println(numberIffy +" may have been due to data errors");
        
        
    }

}
