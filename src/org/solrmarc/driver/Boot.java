package org.solrmarc.driver;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Set;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.utils.ClasspathUtils;

/**
 * @author rh9ec
 *
 */
public class Boot
{
    private static LoggerDelegator logger = null;

    static
    {
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
            String classname = null;
            String[] otherArgs;
            if (args[0].endsWith(".properties"))
            {
                classname = "org.solrmarc.driver.ConfigDriver";
                otherArgs = args;
            }
            else
            {
                otherArgs = new String[args.length - 1];
                System.arraycopy(args, 1, otherArgs, 0, args.length - 1);

                try
                {
                    classname = classnamefromArg(args[0]);
                }
                catch (ClassNotFoundException e1)
                {
                    logger.fatal("ERROR: Unable to find main class for specified short name: " + args[0]);
                    findExecutables();
                    LoggerDelegator.flushToLog();
                    System.exit(3);
                }
            }
            invokeMain(classname, otherArgs);
        }
    }

    /**
     * @param classname
     * @param otherArgs
     */
    protected static void invokeMain(String classname, String[] otherArgs)
    {
        try
        {
            Class<?> mainClass = Class.forName(classname);
            Method mainMethod = mainClass.getMethod("main", String[].class);
            if (!Modifier.isStatic(mainMethod.getModifiers()))
            {
                logger.fatal("ERROR: Main method is not static in class: " + classname);
                System.exit(1);
            }
            mainMethod.invoke(null, (Object) otherArgs);
        }
        catch (IllegalAccessException | IllegalArgumentException e)
        {
            logger.fatal("ERROR: Unable to invoke main method in specified class: " + classname, e);
            System.exit(2);
        }
        catch (InvocationTargetException e)
        {
            Throwable t = e.getTargetException();
            logger.fatal("ERROR: Error while invoking main method in specified class: " + classname, t);
            System.exit(2);
        }
        catch (ClassNotFoundException e)
        {
            logger.fatal("ERROR: Unable to find specified main class: " + classname, e);
            findExecutables();
            System.exit(3);
        }
        catch (NoSuchMethodException e)
        {
            logger.fatal("ERROR: Unable to find main method in specified class: " + classname, e);
            System.exit(4);
        }
        catch (SecurityException e)
        {
            logger.fatal("ERROR: Unable to access main method in specified class: " + classname, e);
            System.exit(5);
        }
    }

    private static String classnamefromArg(String string) throws ClassNotFoundException
    {
        Set<Class<? extends BootableMain>> mainClasses = ClasspathUtils.instance().getBootableMainClasses();
        for (Class<?> clazz : mainClasses)
        {
            if (string.equals(clazz.getName()))          return clazz.getName();
            if (clazz.getName().endsWith("." + string))  return clazz.getName();
        }
 
        for (Class<?> clazz : mainClasses)
        {
            String shortNameStr = null;
            try
            {
                Field shortName = clazz.getDeclaredField("shortName");
                shortNameStr = shortName.get(null).toString();
            }
            catch (NoSuchFieldException  | SecurityException | IllegalArgumentException | IllegalAccessException e)
            {
                // no valid short name don't worry.
            }
            if (shortNameStr != null && string.equals(shortNameStr))  return(clazz.getName());
        }
        throw new ClassNotFoundException("can't find class");
    }

    private static void findExecutables()
    {
        Set<Class<? extends BootableMain>> mainClasses = ClasspathUtils.instance().getBootableMainClasses();
        logger.info("This class  org.solrmarc.driver.Boot  dynamically loads all of the jars in the lib directory");
        logger.info("and then calls the main method of a class that requires those jars");
        logger.info("The first argument provided to this class specifies the name of the class to load and execute");
        logger.info("all of the subsequent arguments are then passed to that class's main method");

        logger.info("");
        logger.info("Known classes that can be bootstrapped in tha manner are:");
        for (Class<?> mainClass : mainClasses)
        {
            try
            {
                Method mainMethod = mainClass.getMethod("main", String[].class);
                String shortNameStr = null;
                try
                {
                    Field shortName = mainClass.getDeclaredField("shortName");
                    shortNameStr = shortName.get(null).toString();
                }
                catch (NoSuchFieldException  | SecurityException | IllegalArgumentException | IllegalAccessException e)
                {
                    // no valid short name don't worry.
                }
                if (Modifier.isStatic(mainMethod.getModifiers()))
                {
                    logger.info("    " + mainClass.getName() + (shortNameStr != null ? "  (" + shortNameStr + ")" : ""));
                }
            }
            catch (NoSuchMethodException localNoSuchMethodException)
            {
                // no prob
            }
        }
    }

    /**
     *   Find the location of where this class is running from
     *   When run normally this would be the main solrmarc jar
     *   when run from classdirs in eclipse, is is the project location
     *
     *   @return  String - location of where this class is running from.  Used
     *                      as default search location for local configuration
     *                      files (As a side effect, sets System Property
     *                      solrmarc.jar.dir to this same value so it can be
     *                      referenced in log4j.properties)
     */
    public static String getDefaultHomeDir()
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
            e.printStackTrace();
        }
        if (jarFile.getName().endsWith(".jar"))
        {
            jarDir = jarFile.getParentFile().getPath();
        }
        else
        {
            // Not running from a jar. Probably running from eclipse or other
            // IDE
            jarDir = new File(".").getAbsoluteFile().getParentFile().getAbsolutePath();
        }
        System.setProperty("solrmarc.jar.dir", jarDir);
        return(jarDir);
    }

    /**
     * Finds directory "lib" relative to the defaultHomeDir and loads all of
     * the jar files in that directory dynamically.  If it doesn't find a jar
     * named marc4j*.jar it will log a fatal error and exit the program.
     */
    private static void addLibDirJarstoClassPath()
    {
        logger = new LoggerDelegator(Boot.class);
        String homePath = getDefaultHomeDir();
        File homeDir = new File(homePath);
        boolean alreadyHasRequiredClasses = true;
        alreadyHasRequiredClasses &= hasRequired("org.marc4j.marc.Record");
        alreadyHasRequiredClasses &= hasRequired("org.apache.log4j.Logger");
        alreadyHasRequiredClasses &= hasRequired("java_cup.runtime.Symbol");
        alreadyHasRequiredClasses &= hasRequired("joptsimple.OptionSet");
        if (alreadyHasRequiredClasses)
        {
            logger.info("Required classes provided statically.  Proceeding and hoping for the best");

            return;
        }

        // Now find the sub-directory "lib" as a sibling of the execution location.
        File libPath = new File(homeDir, "lib");
        try
        {
            extendClasspathWithLibJarDir(libPath, "marc4j.*[.]jar");
        }
        catch (RuntimeException ise)
        {
            logger.fatal("Fatal error: Failure while loading jars from lib directory" + ise.getMessage());
            LoggerDelegator.flushToLog();
            System.exit(10);
        }
        boolean hasRequiredClasses = true;
        hasRequiredClasses &= require("org.marc4j.marc.Record", "Fatal error: Unable to find required marc4j Record class.");
        hasRequiredClasses &= require("org.apache.log4j.Logger", "Fatal error: Unable to find required log4j Logging class.");
        hasRequiredClasses &= require("java_cup.runtime.Symbol", "Fatal error: Unable to find required parser runtime library:  java-cup-runtime.jar ");
        hasRequiredClasses &= require("joptsimple.OptionSet", "Fatal error: Unable to find required JoptionSimple class library.");
        if (!hasRequiredClasses)
        {
            LoggerDelegator.flushToLog();
            System.exit(11);
        }
    }

    private static boolean hasRequired(String requiredClass)
    {
        try {
            Class.forName(requiredClass);
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
        return true;
    }
    
    private static boolean require(String requiredClass, String errMsg)
    {
        try {
            Class.forName(requiredClass);
        }
        catch (ClassNotFoundException e)
        {
            try {
                logger.fatal(errMsg);
            }
            catch (Exception e1)
            {
                System.err.println(errMsg);
            }
            return false;
        }
        return true;
    }

    private static void extendClasspathWithJar(URLClassLoader sysLoader, File jarfile)
    {
        URL[] urls = sysLoader.getURLs();
        URL ujar;
        try
        {
            ujar = jarfile.toURI().toURL();
        }
        catch (MalformedURLException e)
        {
            // This shouldn't happen since the jarfile is passed in as a file
            // from a directory
            e.printStackTrace();
            return;
        }

        String ujars = ujar.toString();
        for (int i = 0; i < urls.length; i++)
        {
            if (urls[i].toString().equalsIgnoreCase(ujars)) return;
        }
        Class<URLClassLoader> sysClass = URLClassLoader.class;
        try
        {
            Method method = sysClass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[] { ujar });
            logger.debug("Adding Jar file: " + jarfile.getAbsolutePath());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private static boolean extendClasspathWithJarDir(URLClassLoader sysLoader, File dir, String patternToLoad, String specialMatch)
    {
        boolean foundSpecial = false;
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
//           throw new RuntimeException("Unable to find any Jars in the provided directory: " + dirpath + "  define location of solrj to use with the option -solrj <dir>");
           return false;
        }
        for (File file : dir.listFiles())
        {
            if (file.getName().matches(patternToLoad))
            {
                extendClasspathWithJar(sysLoader, file);
                if (specialMatch != null && file.getName().matches(specialMatch))
                {
                    foundSpecial = true;
                }
            }
        }
        return(foundSpecial);
    }

    private static void extendClasspathWithDirOfClasses(URLClassLoader sysLoader, File dir)
    {
        URI u = dir.toURI();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> urlClass = URLClassLoader.class;
        try
        {
            Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[] { u.toURL() });
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    private static void extendClasspathWithDirOfClasses(File dir)
    {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        extendClasspathWithDirOfClasses(sysLoader, dir);
    }

    private static boolean extendClasspathWithLibJarDir(File dir, String patternForSpecial)
    {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        boolean foundIt = extendClasspathWithJarDir(sysLoader, dir, ".*[.]jar", patternForSpecial);
        return(foundIt);
    }

    /**
     *  Given a directory, add all of the jars there to the classpath.
     *  If none of those jars has a name containing "solrj"  then look
     *  in the parent directory of the provided directory for a jar with
     *  a name containing "solrj"
     *
     * @param homeDirStrs
     * @param dir
     */
    static void extendClasspathWithSolJJarDir(String[] homeDirStrs, File dir)
    {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        boolean foundSolrj = false;

        File dirWithJars = getDirToStartFrom(homeDirStrs, dir);
        foundSolrj = extendClasspathWithJarDir(sysLoader, dirWithJars, ".*[.]jar", ".*solrj.*[.]jar");
        File parentDir = null;
        if (!foundSolrj)
        {
            parentDir = dirWithJars.getParentFile();
            if (parentDir != null)
            {
                foundSolrj = extendClasspathWithJarDir(sysLoader, parentDir, ".*solrj.*[.]jar", ".*solrj.*[.]jar");
            }
        }
        if (!foundSolrj)
        {
            throw new IndexerSpecException("Unable to find a solrj jar file in directory: "+ dir.getAbsolutePath() + ((parentDir != null) ? "( or "+parentDir.getAbsolutePath()+ ")": ""));
        }
    }

    private static File getDirToStartFrom(String[] homeDirStrs, File dir)
    {
        if (homeDirStrs == null) return(dir);
        // traverse list in stated order so earlier entries will be found and used before later ones.
        for (int i = 0; i < homeDirStrs.length; i++)
        {
            String homeDirStr = homeDirStrs[i];
            File dirSolrJ = new File(homeDirStr, dir.getPath());
            if (dirSolrJ.exists())
            {
                return(dirSolrJ);
            }
        }
        return(dir);
    }

    static void extendClasspathWithLocalJarDirs(String[] homeDirStrs, String[] addnlLibDirStrs)
    {
        FilenameFilter jarsOnly = new FilenameFilter() {
            public boolean accept(File dir, String name) 
            {
                return (name.toLowerCase().endsWith(".jar"));
            }
        };
        
        for (String libdirname : addnlLibDirStrs)
        {
            File libDir = new File(libdirname);
            if (!libDir.isAbsolute())
            {
                logger.debug("Number of homeDirStrs: " + homeDirStrs.length);
                if (homeDirStrs.length >= 1)
                {
                    logger.debug("homeDirStrs[0]: " + homeDirStrs[0]);
                }
                if (homeDirStrs.length >= 2)
                {
                    logger.debug("homeDirStrs[1]: " + homeDirStrs[1]);
                }
                for (int i = homeDirStrs.length - 1; i >= 0; i--)
                {
                    String homeDir = homeDirStrs[i];
                    logger.debug("Checking for jars files in directory: " + homeDir + "/" + libdirname);
                    libDir = new File(homeDir, libdirname);
                    if (libDir.exists() && libDir.isDirectory() && libDir.listFiles(jarsOnly).length > 0)
                    {
                        logger.debug("Adding jars files in directory: " + libDir.getAbsolutePath());
                        extendClasspathWithLibJarDir(libDir, null);
                        extendClasspathWithDirOfClasses(libDir);
                    }
                }
            }
            else if (libDir.exists() && libDir.isDirectory() && libDir.listFiles(jarsOnly).length > 0)
            {
                logger.debug("Adding jars files in directory: " + libDir.getAbsolutePath());
                extendClasspathWithLibJarDir(libDir, null);
                extendClasspathWithDirOfClasses(libDir);
            }
        }
    }
}
