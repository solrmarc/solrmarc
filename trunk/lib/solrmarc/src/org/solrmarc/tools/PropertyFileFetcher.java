package org.solrmarc.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PropertyFileFetcher {
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		
        if (args.length > 0 && args[0].endsWith("jar")) 
        {
            byte[] buffer = new byte[1024];
            int    bytesRead;
            String jarFile = args[0];
            String directory = ".";
            if (args.length > 1)
            {
                directory = args[1]; 
            }
            InputStream in = Utils.getPropertyFileInputStream(null, jarFile);
            File outputFile = new File(directory, jarFile);
            try {
                FileOutputStream out = new FileOutputStream(outputFile);
                while ((bytesRead = in.read(buffer)) != -1) 
                {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                in.close();
            }
            catch (IOException ioe)
            {
                System.err.println("Error: extracting jar file");
                System.exit(1);
            }
            System.exit(0);
        }
        
        String propertyFile = null;
        if (args.length > 0 && args[0].endsWith("properties")) 
        {
            propertyFile = args[0];
        }
        if (propertyFile == null) 
        {
            propertyFile = GetDefaultConfig.getConfigName("");
        }
        InputStream in = Utils.getPropertyFileInputStream(null, propertyFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = reader.readLine()) != null) 
            {
                System.out.println(line);
            }
            reader.close();
        } 
        catch (IOException e) 
        {
            System.err.println("Error: Exception occurred reading specified file "+ propertyFile+ " from jar");
        }
	}
}
