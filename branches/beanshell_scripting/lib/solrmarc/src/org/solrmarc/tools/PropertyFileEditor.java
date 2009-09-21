package org.solrmarc.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PropertyFileEditor {
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
        String parms[][] = new String[args.length][];
        boolean found[] = new boolean[args.length];
 		int count = 0;
 		for (int i = 0; i < args.length; i++)
		{
			found[count] = false;
			parms[count] = args[i].split(" *= *", 2);
			if (parms[count].length > 1) count++;
		}
		try {
			while ((line = reader.readLine()) != null)
			{
				for (int i = 0; i < count; i++)
				{
                    if (line.matches(parms[i][0] + " = .*")) 
                    {
                        found[i] = true;
                        line = parms[i][0] + " = " + parms[i][1].replace('\\', '/');
                    }
                    else if (line.matches(parms[i][0] + "=.*")) 
                    {
                        found[i] = true;
                        line = parms[i][0] + "=" + parms[i][1].replace('\\', '/');
                    }
				}
                System.out.println(line);
			}
			reader.close();
            for (int i = 0; i < count; i++)
            {
                if (!found[i]) 
                {
                    line = parms[i][0] + " = " + parms[i][1].replace('\\', '/');
                    System.out.println(line);
                }
            }
		} 
		catch (IOException e) 
		{
		}
	}

}
