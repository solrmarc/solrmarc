package org.solrmarc.driver;

import java.io.File;
import java.io.IOException;
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
import java.util.Set;

import org.apache.log4j.Logger;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.utils.FastClasspathUtils;
//import org.solrmarc.index.utils.ReflectionUtils;

/**
 * @author rh9ec
 *
 */
public class Boot
{
    private static Logger logger;

    static
    {
        addLibDirJarstoClassPath();

        logger = Logger.getLogger(Boot.class);
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.apache.log4j.BasicConfigurator.configure();
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
            logger.fatal("ERROR: Unable to invoke main method in specified class: " + classname);
            logger.fatal(e);
            System.exit(2);
        }
        catch (InvocationTargetException e)
        {
            Throwable t = e.getTargetException();
            logger.fatal("ERROR: Error while invoking main method in specified class: " + classname);
            logger.fatal(t);
            System.exit(2);
        }
        catch (ClassNotFoundException e)
        {
            logger.fatal("ERROR: Unable to find specified main class: " + classname);
            findExecutables();
            System.exit(3);
        }
        catch (NoSuchMethodException e)
        {
            logger.fatal("ERROR: Unable to find main method in specified class: " + classname);
            System.exit(4);
        }
        catch (SecurityException e)
        {
            logger.fatal("ERROR: Unable to access main method in specified class: " + classname);
            System.exit(5);
        }
    }

    private static String classnamefromArg(String string) throws ClassNotFoundException
    {
        Set<Class<? extends BootableMain>> mainClasses = FastClasspathUtils.getBootableMainClasses();
        for (Class<?> clazz : mainClasses)
        {
            if (string.equals(clazz.getName()))  return(clazz.getName());
            if (clazz.getName().endsWith("."+string))  return(clazz.getName());
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
        Set<Class<? extends BootableMain>> mainClasses = FastClasspathUtils.getBootableMainClasses();

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
                    logger.info("    " + mainClass.getName() + ((shortNameStr != null) ? "  ("+shortNameStr+")" : ""));
                }
            }
            catch (NoSuchMethodException e)
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
            // TODO Auto-generated catch block
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
        String homePath = getDefaultHomeDir();
        File homeDir = new File(homePath);

        // Now find the sub-directory "lib" as a sibling of the execution location.
        File libPath = new File(homeDir, "lib");
        try
        {
            extendClasspathWithLibJarDir(libPath, "marc4j.*[.]jar");
        }
        catch (RuntimeException ise)
        {
            logger.fatal("Fatal error: Failure while loading jars from lib directory" + ise.getMessage());
            System.exit(10);
        }

        try
        {
            Class.forName("org.marc4j.marc.Record");
        }
        catch (ClassNotFoundException e)
        {
            try {
                logger = Logger.getLogger(Boot.class);
                org.apache.log4j.BasicConfigurator.configure();
                logger.fatal("Fatal error: Unable to find marc4j Record class, probably missing many others as well.  " + e.getMessage());
            }
            catch (Exception e1)
            {
                System.err.println("Fatal error: Unable to find marc4j Record class, probably missing many others as well.");
            }
            System.exit(11);
        }
    }

    private static void extendClasspathWithJar(URLClassLoader sysLoader, File jarfile)
    {
        URL urls[] = sysLoader.getURLs();
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
            throw new RuntimeException("Unable to find any Jars in the provided directory: " + dirpath + "  define location of solrj to use with the option -solrj <dir>");
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
        Method method;
        try
        {
            method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[]{u.toURL()});
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MalformedURLException e)
        {
            // TODO Auto-generated catch block
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
            foundSolrj = extendClasspathWithJarDir(sysLoader, parentDir, ".*solrj.*[.]jar", ".*solrj.*[.]jar");
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
        for (int i = 0 ; i < homeDirStrs.length; i++)
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
        for (String libdirname : addnlLibDirStrs)
        {
            boolean found = false;
            File libDir = new File(libdirname);
            if (!libDir.isAbsolute())
            {
                for (String homeDir : homeDirStrs)
                {
                    libDir = new File(homeDir, libdirname);
                    if (libDir.exists() && libDir.isDirectory() && libDir.listFiles().length > 0)
                    {
                        found = true;
                        break;
                    }
                }
            }
            if (found)
            {
                extendClasspathWithLibJarDir(libDir, null);
                extendClasspathWithDirOfClasses(libDir);
            }
        }
    }
}
