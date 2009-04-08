package org.solrmarc.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PropertyFileFetcher {
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
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
