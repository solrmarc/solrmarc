package org.solrmarc.driver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;

import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;

public class Boot
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        CodeSource codeSource = Boot.class.getProtectionDomain().getCodeSource();
        File jarFile = null;
        try
        {
            jarFile = new File(codeSource.getLocation().toURI().getPath());
        }
        catch (URISyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String jarDir = jarFile.getParentFile().getPath();        
        File libPath = new File(jarDir, "lib");
        try
        {
            extendClasspath(libPath);
        }
        catch (IndexerSpecException ise)
        {
            System.err.println("Fatal error: Failure to load SolrJ"+ ise.getMessage());
            System.exit(10);
        }
        
        try
        {
            Class.forName("org.marc4j.marc.Record");
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        IndexDriver driver = new IndexDriver(args);
        driver.execute();

    }
    
    public static void extendClasspathWithJar(URLClassLoader sysLoader, File jarfile)
    {
        URL urls[] = sysLoader.getURLs();
        URL ujar;
        try
        {
            ujar = jarfile.toURI().toURL();
        }
        catch (MalformedURLException e)
        {
            // This shouldn't happen since the jarfile is passed in as a file from a directory
            e.printStackTrace();
            return;
        }

        String ujars = ujar.toString();
        for (int i = 0; i < urls.length; i++)
        {
            if (urls[i].toString().equalsIgnoreCase(ujars)) return;
        }
        Class<URLClassLoader> sysClass = URLClassLoader.class;
        try {
            Method method = sysClass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[] {ujar});
        } 
        catch (Throwable t) 
        {
            t.printStackTrace();
        }
    }

    public static void extendClasspath(File dir)
    {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        if (dir == null || !dir.isDirectory() || dir.listFiles().length == 0)
        {
            String dirpath;
            try
            {
                dirpath = dir.getCanonicalPath();
            }
            catch (IOException e)
            {
                dirpath = dir.getAbsolutePath();
            }
            throw new IndexerSpecException(eErrorSeverity.FATAL, "Unable to find Jars for SolrJ in the provided directory: "+dirpath );
        }
        for (File file : dir.listFiles())
        {
            if (file.getName().endsWith(".jar"))
            {
                extendClasspathWithJar(sysLoader, file);
            }
        } 
    }

}
