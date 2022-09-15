package org.solrmarc.driver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.solrmarc.index.utils.ClasspathUtils;

/**
 * @author rh9ec
 *
 */
public class Boot extends URLClassLoader

{
    private static LoggerDelegator logger = new LoggerDelegator(ClasspathUtils.class);;
    private static URLClassLoader classLoaderToUse = null;

    static
    {
        getURLClassLoaderToUse();
        //addLibDirJarstoClassPath();
    }

    public static void main(String[] args)
    {
        logger.error("Starting SolrMarc boot loader shim");
        logger.info_multi("Command line: \n" + getCommandLine());
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
     * Classloader Methods - Implements a child-first delegation model to load classes needed by SolrMarc
     *
     * @param urls    the URLs from which to load classes and resources
     * @param parent  the parent class loader for delegation
     */
    public Boot(URL[] urls, ClassLoader parent)
    {
        super(urls, parent);
    }

    private static String getCommandLine()
    {
        String sep = ""+ File.pathSeparatorChar;
        StringBuilder sb = new StringBuilder().append("java ");
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();

        for (int i = 0; i < jvmArgs.size(); i++)
        {
            sb.append( jvmArgs.get( i ) ). append("\n    ");
        }
        sb.append("-classpath " + System.getProperty("java.class.path").replaceAll(sep,  sep+"\n               "));
        // print the non-JVM command line arguments
        // print name of the main class with its arguments, like org.ClassName param1 param2
        sb.append("\n    " + System.getProperty("sun.java.command").replaceFirst(" ", "\n        ").replaceAll(" -", "\n        -"));
        return(sb.toString().replaceAll("\n[ ]*\n", "\n"));
    }
    
    private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>(); //used to cache already defined classes
    @Override
    public void addURL(URL url)
    {
        super.addURL(url);
    }
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException
    {
        System.out.println("Trying to find"+ name);
        throw new ClassNotFoundException();
    }
    @Override
    protected synchronized Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException
    {
        System.out.println("Trying to load: "+className);
        try
        {
            System.out.println("Loading class in Child : " + className);
            byte classByte[];
            Class<?> result = null;

            if (className.endsWith(".Boot") || className.endsWith(".LoggerDelegator"))
            {
                System.out.println("Delegating to parent : " + className);
                // didn't find it, try the parent
                return super.loadClass(className, resolve);
            }

            //checks in cached classes
            result = (Class<?>) classes.get(className);
            if (result != null) {
                return result;
            }

            URL[] urls = super.getURLs();
            if (urls != null)
            {
                int i =0;
                for(URL jarFileURL: urls)
                {
                    System.out.println(i + "test" + jarFileURL);
                    i++;
                    String jarFile = jarFileURL.toURI().getPath();
                    JarFile jar = null;
                    try {
                        jar = new JarFile(jarFile);
                        JarEntry entry = jar.getJarEntry(className.replace(".","/") + ".class");
                        InputStream is = jar.getInputStream(entry);
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        int nextValue = is.read();
                        while (nextValue != -1)
                        {
                            byteStream.write(nextValue);
                            nextValue = is.read();
                        }

                        classByte = byteStream.toByteArray();
                        result = defineClass(className, classByte, 0, classByte.length);
                        classes.put(className, result);
                        jar.close();
                    } 
                    catch (Exception e) {
                        if (jar != null) 
                        {
                            try
                            {
                                jar.close();
                            }
                            catch (IOException e1)
                            {
                            }
                        }
                        continue;
                    }
                }
            }

            result = (Class<?>) classes.get(className);
            if (result != null) {
                return result;
            }
            else{
                throw new ClassNotFoundException("Not found "+ className);
            }
        }
        catch( ClassNotFoundException e ){

            //System.out.println("Delegating to parent : " + className);
            // didn't find it, try the parent
            return super.loadClass(className, resolve);
        }
        catch (URISyntaxException e1)
        {
            //System.out.println("Delegating to parent : " + className);
            // didn't find it, try the parent
            return super.loadClass(className, resolve);
        }
    }
   /*
    * end of classloader methods
    */

    /**
     * @param classname  name of class to invoke
     * @param otherArgs  args for main method
     */
    public static void invokeMain(String classname, String[] otherArgs)
    {
        logger.info_multi("Invoking main program: \n"+ getEffectiveCommandLine(classname, otherArgs));
        try
        {
            Class<?> mainClass = Boot.classForName(classname);
            Method mainMethod = mainClass.getMethod("main", String[].class);
            if (!Modifier.isStatic(mainMethod.getModifiers()))
            {
                logger.fatal("ERROR: Main method is not static in class: " + classname);
                LoggerDelegator.flushToLog();
                System.exit(1);
            }
            mainMethod.invoke(null, (Object) otherArgs);
        }
        catch (IllegalAccessException | IllegalArgumentException e)
        {
            logger.fatal("ERROR: Unable to invoke main method in specified class: " + classname, e);
            LoggerDelegator.flushToLog();
            System.exit(2);
        }
        catch (InvocationTargetException e)
        {
            Throwable t = e.getTargetException();
            logger.fatal("ERROR: Error while invoking main method in specified class: " + classname, t);
            LoggerDelegator.flushToLog();
            System.exit(2);
        }
        catch (ClassNotFoundException e)
        {
            logger.fatal("ERROR: Unable to find specified main class: " + classname, e);
            findExecutables();
            LoggerDelegator.flushToLog();
            System.exit(3);
        }
        catch (NoSuchMethodException e)
        {
            logger.fatal("ERROR: Unable to find main method in specified class: " + classname, e);
            LoggerDelegator.flushToLog();
            System.exit(4);
        }
        catch (SecurityException e)
        {
            logger.fatal("ERROR: Unable to access main method in specified class: " + classname, e);
            LoggerDelegator.flushToLog();
            System.exit(5);
        }
    }

    private static String getEffectiveCommandLine(String classname, String[] otherargs)
    {
        StringBuilder sb = new StringBuilder().append("java ").append("-classpath <CLASSPATH>").append("\n        ").append(classname).append("\n");
        boolean wasDash = false;
        for (String arg : otherargs)
        {
            if (wasDash) 
            {
                sb.append(" ").append(arg);
            }
            else 
            {
                sb.append("\n        ").append(arg);
            }
            wasDash = arg.startsWith("-");
        }
        return(sb.toString().replaceAll("\n[ ]*\n", "\n"));
    }
    
    @SuppressWarnings("unchecked")
    private static String classnamefromArg(String string) throws ClassNotFoundException
    {
        Class<?> clazzUtil = Boot.classForName("org.solrmarc.index.utils.ClasspathUtils");
        Object instance;
        Set<Class<? extends BootableMain>> mainClasses = new LinkedHashSet<Class<? extends BootableMain>>();
        try
        {
            instance = clazzUtil.getMethod("instance", new Class<?>[0]).invoke(null,  new Object[0]);
            Method getBootableMain = instance.getClass().getMethod("getBootableMainClasses", new Class<?>[0]);
            mainClasses = (Set<Class<? extends BootableMain>>) getBootableMain.invoke(instance, new Object[0]);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
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
     *   When run normally this would be the main org.solrmarc jar
     *   when run from classdirs in eclipse, is is the project location
     *
     *   @return  String - location of where this class is running from.  Used
     *                      as default search location for local configuration
     *                      files (As a side effect, sets System Property
     *                      org.solrmarc.jar.dir to this same value so it can be
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
        System.setProperty("org.solrmarc.jar.dir", jarDir);
        return(jarDir);
    }

    /**
     * Finds directory "lib" relative to the defaultHomeDir and loads all of
     * the jar files in that directory dynamically.  If it doesn't find a jar
     * named {@code marc4j*.jar} it will log a fatal error and exit the program.
     */
    private static void addLibDirJarstoClassPath()
    {
        try
        {
            Class.forName("org.solrmarc.driver.LoggerDelegator");
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger = new LoggerDelegator(Boot.class);
        String homePath = getDefaultHomeDir();
        File homeDir = new File(homePath);
        boolean alreadyHasRequiredClasses = true;
//        alreadyHasRequiredClasses &= hasRequired("org.marc4j.marc.Record");
//        alreadyHasRequiredClasses &= hasRequired("org.apache.log4j.Logger");
//        alreadyHasRequiredClasses &= hasRequired("java_cup.runtime.Symbol");
//        alreadyHasRequiredClasses &= hasRequired("joptsimple.OptionSet");
        if (alreadyHasRequiredClasses)
        {
            logger.info("Required classes provided statically.  Proceeding and hoping for the best");

            return;
        }

        // If using a custom Classloader use that to load all of the classes in the SolrMarc core jar
        CodeSource codeSource = Boot.class.getProtectionDomain().getCodeSource();
        File jarFile = null;
        try
        {
            jarFile = new File(codeSource.getLocation().toURI().getPath());
            URLClassLoader loader = getURLClassLoaderToUse();
            if (loader != ClassLoader.getSystemClassLoader())
                extendClasspathWithJar(loader, jarFile);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
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
//        hasRequiredClasses &= require("org.marc4j.marc.Record", "Fatal error: Unable to find required marc4j Record class.");
//        hasRequiredClasses &= require("org.apache.log4j.Logger", "Fatal error: Unable to find required log4j Logging class.");
//        hasRequiredClasses &= require("java_cup.runtime.Symbol", "Fatal error: Unable to find required parser runtime library:  java-cup-runtime.jar ");
//        hasRequiredClasses &= require("joptsimple.OptionSet", "Fatal error: Unable to find required JoptionSimple class library.");
        if (!hasRequiredClasses)
        {
            LoggerDelegator.flushToLog();
            System.exit(11);
        }
    }

    /**
     * If using a custom classloader, use that to lookup and load the requested class by name
     *
     * @param classname                name of class to look up
     * @return                         class that was named
     * @throws ClassNotFoundException  if class was not found by loader
     */
    public static Class<?>classForName(String classname) throws ClassNotFoundException
    {
        if (classLoaderToUse == null) getURLClassLoaderToUse();
        return(Class.forName(classname, true, classLoaderToUse));
    }

    private static boolean hasRequired(String requiredClass)
    {
        try {
            classForName(requiredClass);
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
            classForName(requiredClass);
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

    private static void extendClasspathWithJar(URLClassLoader classLoader, File jarfile)
    {
        URL[] urls = classLoader.getURLs();
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
        try
        {
            if (classLoader instanceof Boot)
            {
                ((Boot)classLoader).addURL(ujar);
            }
            else
            {
                Class<?> urlClassLoaderClazz = URLClassLoader.class;
                Method method = urlClassLoaderClazz.getDeclaredMethod("addURL", new Class[] { URL.class });
                method.setAccessible(true);
                method.invoke(classLoader, new Object[] { ujar });
                logger.debug("Adding Jar file: " + jarfile.getAbsolutePath());
            }
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private static boolean extendClasspathWithJarDir(URLClassLoader sysLoader, File dir, String patternToLoad, String specialMatch)
    {
        boolean foundSpecial = false;
        if (dir == null || ! dir.isDirectory() || dir.listFiles().length == 0)
        {
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

    private static void extendClasspathWithDirOfClasses(URLClassLoader classLoader, File dir)
    {
        URI u = dir.toURI();
        try
        {
            if (classLoader instanceof Boot)
            {
                ((Boot)classLoader).addURL(u.toURL());
            }
            else
            {
                Class<?> urlClassLoaderClazz = URLClassLoader.class;
                Method method = urlClassLoaderClazz.getDeclaredMethod("addURL", new Class[] { URL.class });
                method.setAccessible(true);
                method.invoke(classLoader, new Object[] { u.toURL() });
            }
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    private static void extendClasspathWithDirOfClasses(File dir)
    {
        URLClassLoader sysLoader = getURLClassLoaderToUse();
        extendClasspathWithDirOfClasses(sysLoader, dir);
    }

    private static boolean extendClasspathWithLibJarDir(File dir, String patternForSpecial)
    {
        URLClassLoader sysLoader = getURLClassLoaderToUse();
        boolean foundIt = extendClasspathWithJarDir(sysLoader, dir, ".*[.]jar", patternForSpecial);
        return(foundIt);
    }

    /**
     *  Given a directory, add all of the jars there to the classpath.
     *  If none of those jars has a name containing "solrj"  then look
     *  in the parent directory of the provided directory for a jar with
     *  a name containing "solrj"
     *
     * @param homeDirStrs  home directory strings
     * @param dir          directory
     */
    public static void extendClasspathWithSolJJarDir(String[] homeDirStrs, File dir)
    {
        URLClassLoader sysLoader = getURLClassLoaderToUse();
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
            throw new RuntimeException("Unable to find a solrj jar file in directory: "+ dir.getAbsolutePath() + ((parentDir != null) ? "( or "+parentDir.getAbsolutePath()+ ")": ""));
        }
    }

    public static URLClassLoader getURLClassLoaderToUse()
    {
        if (classLoaderToUse != null) return(classLoaderToUse);
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        String forceCustomClassLoaderProperty = System.getProperty("org.solrmarc.force.custom.classloader", "false");
        if (systemClassLoader instanceof URLClassLoader && ! forceCustomClassLoaderProperty.equalsIgnoreCase("true"))
        {
            classLoaderToUse = (URLClassLoader)systemClassLoader;
        }
        else
        {
            classLoaderToUse = new Boot(new URL[0], systemClassLoader);
            Thread.currentThread().setContextClassLoader(classLoaderToUse);
        }
        return classLoaderToUse;
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

    public static void extendClasspathWithLocalJarDirs(String[] homeDirStrs, String[] addnlLibDirStrs)
    {
        FilenameFilter jarsOrClassesOnly = new FilenameFilter() {
            public boolean accept(File dir, String name) 
            {
                return (name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith("*.class"));
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
                    if (libDir.exists() && libDir.isDirectory() && libDir.listFiles(jarsOrClassesOnly).length > 0)
                    {
                        logger.debug("Adding jars files in directory: " + libDir.getAbsolutePath());
                        extendClasspathWithLibJarDir(libDir, null);
                        extendClasspathWithDirOfClasses(libDir);
                    }
                }
            }
            else if (libDir.exists() && libDir.isDirectory() && libDir.listFiles(jarsOrClassesOnly).length > 0)
            {
                logger.debug("Adding jars files in directory: " + libDir.getAbsolutePath());
                extendClasspathWithLibJarDir(libDir, null);
                extendClasspathWithDirOfClasses(libDir);
            }
        }
    }
}
