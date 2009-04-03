package org.solrmarc.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigDisplayer {
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String configProperties = null;
		if (args.length > 0 && args[0].endsWith("config.properties")) 
		{
			configProperties = args[0];
			String tmpArgs[] = new String[args.length - 1];
			for (int i = 0; i < args.length - 1; i++) 
			{
				tmpArgs[i] = args[i + 1];
			}
			args = tmpArgs;
		}
		if (configProperties == null) 
		{
			configProperties = GetDefaultConfig.getConfigName("");
		}
		InputStream in = Utils.getPropertyFileInputStream(new String[0],
				configProperties);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		String parms[][] = new String[args.length][];
		for (int i = 0; i < args.length; i++) {
			parms[i] = args[i].split(" *= *", 2);
		}
		try {
			while ((line = reader.readLine()) != null) 
			{

				for (int i = 0; i < args.length; i++) 
				{
					if (line.matches(parms[i][0] + " *= *.*")) 
					{
						line = parms[i][0] + " = " + parms[i][1].replace('\\', '/');
					}
				}
				System.out.println(line);
			}
			reader.close();
		} 
		catch (IOException e) 
		{
		}
	}

}
