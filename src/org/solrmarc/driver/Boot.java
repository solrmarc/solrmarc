package org.solrmarc.driver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Set;

import org.solrmarc.index.utils.ReflectionUtils;

public class Boot
{
    static {
        addLibDirJarstoClassPath();
    }
    
    public static void main(String[] args)
    {
        if (args.length == 0) 
        {
            findExecutables();
        }
        else
        {
            String[] otherArgs = new String[args.length-1];
            System.arraycopy(args, 1,  otherArgs,  0,  args.length-1);
            try
            {
                Class<?> mainClass = Class.forName(args[0]);
                Method mainMethod = mainClass.getMethod("main", String[].class);
                if (!Modifier.isStatic(mainMethod.getModifiers()))
                {
                    System.err.println("Main method is not static in class: "+ args[0]);
                    System.exit(1);
                }
                mainMethod.invoke(null, (Object)otherArgs);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
                System.err.println("Unable to invoke main method in specified main: "+ args[0]);
                System.err.println(e.getMessage());
                System.exit(2);
            }
            catch (ClassNotFoundException e)
            {
                System.err.println("Unable to find specified main class: "+ args[0]);
                System.exit(3);
            }
            catch (NoSuchMethodException e)
            {
                System.err.println("Unable to find main method in specified class: "+ args[0]);
                System.exit(4);
            }
            catch (SecurityException e)
            {
                System.err.println("Unable to access main method in specified class: "+ args[0]);
                System.exit(5);
            }
        }
    }
        
    private static void findExecutables()
    {
        Set<Class<? extends Boot>> mainClasses = ReflectionUtils.getBootableMainClasses();
        
        System.out.println("This class  org.solrmarc.driver.Boot  dynamically loads all of the jars in the lib directory");
        System.out.println("and then calls the main method of a class that requires those jars");
        System.out.println("The first argument provided to this class specifies the name of the class to load and execute");
        System.out.println("all of the subsequent arguments are then passed to that class's main method");

        System.out.println("");
        System.out.println("Known classes that can be bootstrapped in tha manner are:");
        for (Class<?> mainClass : mainClasses)
        {
           try { 
                Method mainMethod = mainClass.getMethod("main", String[].class);
                if (Modifier.isStatic(mainMethod.getModifiers()))
                {
                    System.out.println("    "+mainClass.getName());
                }
            }
            catch (NoSuchMethodException e)
            {
                // no prob
            }
        }
        
        
    }

    /**
     * @param args
     */
    public static void addLibDirJarstoClassPath()
    {
        CodeSource codeSource = Boot.class.getProtectionDomain().getCodeSource();
        String jarDir;
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
        if (jarFile.getName().endsWith(".jar"))
        {
            jarDir = jarFile.getParentFile().getPath();        
        }
        else
        {
            // Not running from a jar.  Probably running from eclipse or other IDE
            jarDir = new File(".").getAbsoluteFile().getParentFile().getAbsolutePath();
        }
        File libPath = new File(jarDir, "lib");
        try
        {
            extendClasspath(libPath);
        }
        catch (RuntimeException ise)
        {
            System.err.println("Fatal error: Failure while loading jars from lib directory"+ ise.getMessage());
            System.exit(10);
        }
        
        try
        {
            Class.forName("org.marc4j.marc.Record");
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("Fatal error: Unable to find marc4j Record class, probably missing many others as well."+ e.getMessage());
            System.exit(11);
        }
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
            throw new RuntimeException("Unable to find Jars for SolrJ in the provided directory: "+dirpath );
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
