package org.solrmarc.index.extractor.impl.java;

import org.apache.log4j.Logger;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.tools.PropertyUtils;


import javax.tools.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaValueExtractorUtils
{
    private final static Logger logger = Logger.getLogger(JavaValueExtractorUtils.class);
    private final static List<File> sourceFiles = new ArrayList<>();
    private final static Map<File, String> packageNamesForFile = new LinkedHashMap<>();
    private final static Pattern packageFinder = Pattern.compile("package[ \t]+(([a-z_][a-z0-9_]*[.])*[a-z_][a-z0-9_]*)[ \t]*;.*");

    protected static void clean()
    {
        logger.debug("Clean...");
        deleteFolder(new File(getBinDirectory()));
    }

    private static void deleteFolder(File folder)
    {
        final File[] files = folder.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    deleteFolder(file);
                }
                else if (!file.delete())
                {
                    throw new RuntimeException("Couldn't delete file " + file.getAbsolutePath());
                }
            }
        }
        if (!folder.delete())
        {
            throw new RuntimeException("Couldn't delete file " + folder.getAbsolutePath());
        }
    }

    /**
     * Compiles java sources if they have changed.
     *
     * @return true if one or more java sources were compiled, else false.
     * @throws IOException
     */
    protected static boolean compileSources() throws IOException
    {
        createDirectories();
        final List<File> sourceFiles = getChangedSourceFiles();
        if (sourceFiles.isEmpty())
        {
            return false;
        }
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
        {
            logger.warn("Java environment at JAVA_HOME = "+System.getProperty("java.home")+ " does not have a Java compiler.");
            logger.warn("Any custom mixin routines in "+getSrcDirectory()+ " will not be compiled and will not be available.");
            return false;
        }
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, Charset.forName("UTF-8"));
        fileManager.setLocation(StandardLocation.SOURCE_PATH, Collections.singleton(new File(getSrcDirectory())));
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(new File(getBinDirectory())));

        final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        final Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
        final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, null, null, units);

        logger.trace("Compile java files:\n" + sourceFiles.toString().replaceAll(",", ",\n"));
        if (!task.call())
        {
            StringBuilder buffer = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics())
            {
                buffer.append(diagnostic.toString()).append('\n');
            }
            throw new RuntimeException('\n' + buffer.toString() + "\nCompiling java sources failed!");
        }
        logger.trace("... done");
        fileManager.close();

        return true;
    }

    private static void createDirectories()
    {
        final File srcDirectory = new File(getSrcDirectory());
        final File binDirectory = new File(getBinDirectory());

        if (!srcDirectory.exists())
        {
            if (!srcDirectory.mkdirs())
            {
                throw new RuntimeException("Couldn't create source directory: " + srcDirectory.getAbsolutePath());
            }
        }
        if (!binDirectory.exists())
        {
            if (!binDirectory.mkdirs())
            {
                throw new RuntimeException("Couldn't create binary directory: " + binDirectory.getAbsolutePath());
            }
        }
    }

    private static String getBinDirectory()
    {
        return (ValueIndexerFactory.getHomeDir() + File.separator + "index_java" + File.separator + "bin");
    }

    private static String getSrcDirectory()
    {
        return (ValueIndexerFactory.getHomeDir() + File.separator + "index_java" + File.separator + "src");
    }

    protected static Class<?>[] getClasses()
    {
        final List<String> classNames = getClassNames();
        final List<Class<?>> classList = new ArrayList<Class<?>>(classNames.size());
        final ClassLoader classLoader = getClassLoader();
        for (String className : classNames)
        {
         //   final String className = classNames.get(i);
            try
            {
                classList.add(classLoader.loadClass(className));
            }
            catch (ClassNotFoundException e)
            {
                logger.warn("Unable to load custom mixin class: "+className);
            }
        }
        final Class<?>[] classes = classList.toArray(new Class<?>[classList.size()]);
        return classes;
    }

    private static ClassLoader getClassLoader()
    {
        try
        {
            final URL url = new File(getBinDirectory()).toURI().toURL();
            return new URLClassLoader(new URL[] { url });
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getClassNames()
    {
        final List<File> sourceFiles = getSourceFiles();
        final List<String> classNames = new ArrayList<>();
        for (final File sourceFile : sourceFiles)
        {
            classNames.add(getClassNameForSourceFile(sourceFile));
        }
        return classNames;
    }

    private static String getClassNameForSourceFile(final File sourceFile)
    {
        final String sourcePath = sourceFile.getPath();
        final int pathOffset = getSrcDirectory().length() + (getSrcDirectory().endsWith("/") ? 0 : 1);
        final String classPath = sourcePath.substring(pathOffset, sourcePath.length() - 5);
        String className = classPath.replace(File.separator, ".");
        if (!className.contains("."))
        {
            // find package in source file
            final String packageName = getPackageName(sourceFile);
            className = packageName + className;
        }
        return(className);
    }
    
    private static String getClassFileForSourceFile(final File sourceFile)
    {
        final String sourcePath = sourceFile.getPath();
        final int pathOffset = getSrcDirectory().length() + (getSrcDirectory().endsWith("/") ? 0 : 1);
        final String classPath = sourcePath.substring(pathOffset, sourcePath.length() - 5);
        String classFile = classPath.replace(File.separator, ".");
        if (!classPath.contains(File.separator))
        {
            // find package in source file
            final String packageName = getPackageName(sourceFile);
            classFile = packageName.replace(".", File.separator) + classFile;
        }
        return(getBinDirectory() + File.separator + classFile + ".class");
    }

    private static String getPackageName(File sourceFile)
    {
        String packageName = "";
        if (!packageNamesForFile.containsKey(sourceFile))
        {
            try
            {
                BufferedReader srcReader = new BufferedReader(new FileReader(sourceFile));
                String line;
                while ((line = srcReader.readLine()) != null)
                {
                    Matcher matcher = packageFinder.matcher(line);
                    if (matcher.matches())
                    {
                        packageName = matcher.group(1) + ".";
                        break;
                    }
                }
                srcReader.close();
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            packageNamesForFile.put(sourceFile, packageName);
        }
        else
        {
            packageName = packageNamesForFile.get(sourceFile);
        }
        return packageName;
    }

    private static List<File> getChangedSourceFiles()
    {
        final List<File> sourceFiles = getSourceFiles();
        final List<File> changedSourceFiles = new ArrayList<>();
        for (final File sourceFile : sourceFiles)
        {
            if (hasChanged(sourceFile))
            {
                changedSourceFiles.add(sourceFile);
            }
        }
        return changedSourceFiles;
    }

    private static List<File> getSourceFiles()
    {
        if (sourceFiles.isEmpty())
        {
            final Queue<File> directories = new LinkedList<>();
            directories.add(new File(getSrcDirectory()));
            while (!directories.isEmpty())
            {
                final File directory = directories.poll();
                for (File file : listFiles(directory))
                {
                    if (file.isDirectory())
                    {
                        directories.add(file);
                    }
                    else if (file.isFile() && file.getName().endsWith(".java"))
                    {
                        sourceFiles.add(file);
                    }
                }
            }
        }
        return sourceFiles;
    }

    private static File[] listFiles(File directory)
    {
        if (directory == null)
        {
            return new File[0];
        }
        final File[] fileList = directory.listFiles();
        return fileList != null ? fileList : new File[0];
    }

    private static boolean hasChanged(File sourceFile)
    {
        final String sourcePath = sourceFile.getPath();
        final String targetPath = getClassFileForSourceFile(new File(sourcePath));
        //getBinDirectory() + sourcePath.substring(getSrcDirectory().length(), sourcePath.length() - 5) + ".class";
        final File targetFile = new File(targetPath);
        return !targetFile.exists() || targetFile.lastModified() < sourceFile.lastModified();
    }
}
