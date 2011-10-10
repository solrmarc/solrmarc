package org.solrmarc.tools; 
import java.io.IOException; 
import java.util.Enumeration; 
import java.util.jar.JarFile; 
import java.util.jar.Manifest; 
import java.util.zip.ZipEntry; 

public class GetDefaultConfig 
{ 

    /** 
     * @param args 
     */ 
    public static void main(String[] args) 
    {
        String configProperties = GetDefaultConfig.getConfigName("");
        System.out.println(configProperties); 
    }

    /** 
     * Extract the manifest attribute Default-Config-File from the top level jar file 
     * @throws IOException 
     */

    public static String getJarFileName()
    { 
        String jarFilename = null; 
        Class<?> bootClass;
        try
        {
            bootClass = Class.forName("com.simontuffs.onejar.Boot");
            jarFilename = bootClass.getMethod("getMyJarPath").invoke(null).toString();
        }
        catch (Exception e)
        {
            // Program not running from within a OneJar 
            jarFilename = null;
        }
        return(jarFilename);
    }
    
    /** 
     * Extract the manifest attribute Default-Config-File from the top level jar file 
     * @throws IOException 
     */

    public static String getConfigName(String defaultValue)
    { 
        String configProperties = null; 
        String jar = getJarFileName();
        if (jar != null)
        {
            try { 
                JarFile jarFile = new JarFile(jar); 
                Manifest manifest = jarFile.getManifest(); 
                String defConfig = manifest.getMainAttributes().getValue("Default-Config-File"); 
                if (defConfig != null && defConfig.length() > 0) 
                { 
                    configProperties = defConfig; 
                } 
                else 
                { 
                    Enumeration entries = jarFile.entries(); 
                    while (entries.hasMoreElements()) 
                    { 
                        ZipEntry entry = (ZipEntry)entries.nextElement(); 
                        if (entry.getName().contains("config.properties")) 
                        { 
                            configProperties = entry.getName(); 
                        } 
                    } 
                } 
            } 
            catch (Exception e) 
            { 
                // no manifest property defining the config 
            }
        }
        if (configProperties == null)
        {
            configProperties = defaultValue;
        }
        return(configProperties);
    }
}
