package org.solrmarc.testUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandLineUtils
{
    public static void compareUtilOutputLines(InputStream stdin, String outputLineExpected[], int numLinesToSkip, int numToCompare)
    {
        BufferedReader reader2 = null;
        try
        {
            reader2 = new BufferedReader(new InputStreamReader(stdin, "UTF-8"));
        }
        catch (UnsupportedEncodingException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        String line = null;
        int lineCnt = 0;
        try
        {
            while ((line = reader2.readLine()) != null)
            {
                if (numLinesToSkip > 0 && lineCnt < numLinesToSkip || line.length() == 0) 
                {
                    // do nothing;
                }
                else if (numToCompare > 0 && lineCnt - numLinesToSkip < numToCompare && outputLineExpected.length > lineCnt - numLinesToSkip) 
                    assertEquals("output line doesn't match expected", line, outputLineExpected[lineCnt - numLinesToSkip] );
                lineCnt++;
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   
    public static void compareUtilOutputLine(InputStream stdin, String outputLineExpected, int numLinesToSkip)
    {
        compareUtilOutputLines(stdin, new String[]{outputLineExpected}, numLinesToSkip, 1);
    }
    
    public static void compareUtilOutput(InputStream stdin, String[] outputLinesExpected)
    {
        compareUtilOutputLines(stdin, outputLinesExpected, 0, outputLinesExpected.length);
    }
    
    public static void runCommandLineUtil2(String className, String methodName, InputStream stdin, OutputStream stdout, OutputStream stderr, String[] args)
    {
        InputStream origIn = System.in;
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        Class clazz;
        Method method;
        try
        {
            clazz = Class.forName(className);
            method = clazz.getMethod(methodName, String[].class);
            if (stdin != null) System.setIn(stdin);
            if (stdout != null) System.setOut(new PrintStream(stdout));
            if (stderr != null) System.setErr(new PrintStream(stderr));
            method.invoke(null, (Object)args);
        }
        catch (ClassNotFoundException e)
        {
            fail("Unable to find specified class "+className+" to invoke");
        }
        catch (SecurityException e)
        {
            fail("Unable to access specified method "+methodName+" to invoke within class "+className+"");
        }
        catch (NoSuchMethodException e)
        {
            fail("Unable to find specified method "+methodName+" to invoke within class "+className+"");
        }
        catch (IllegalArgumentException e)
        {
            fail("Illegal arguments for specified method "+methodName+" to invoke within class "+className+"");
        }
        catch (IllegalAccessException e)
        {
            fail("Unable to access specified method "+methodName+" to invoke within class "+className+"");
        }
        catch (InvocationTargetException e)
        {
            e.getTargetException().printStackTrace();
            fail("Specified method "+methodName+" threw an exception "+e.getTargetException().getClass().getName());
        }
        System.setIn(origIn);
        System.setOut(origOut);
        System.setErr(origErr);
    }

    public static void runCommandLineUtil(String className, String methodName, InputStream stdin, OutputStream stdout, String[] args)
    {
        runCommandLineUtil2(className, methodName, stdin, stdout, null, args);
    }
    
    public static void assertArrayEquals(String message, byte[] byteArray1, byte[] byteArray2)
    {
        assertEquals(message + " lengths not equal", byteArray1.length, byteArray2.length);
        
        for (int i = 0 ; i < byteArray1.length; i++)
        {
            assertEquals(message + "byte at offset "+ i + " not equal", byteArray1[i], byteArray2[i]);
        }        
    }

                                                                
}
