package org.solrmarc.index.extractor.impl.java;

import org.apache.log4j.Logger;
import org.solrmarc.index.indexer.ValueIndexerFactory;


import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class JavaValueExtractorUtils
{
    private final static Logger logger = Logger.getLogger(JavaValueExtractorUtils.class);
    private final static List<File> sourceFiles = new ArrayList<>();

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
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(StandardLocation.SOURCE_PATH, Collections.singleton(new File(getSrcDirectory())));
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(new File(getBinDirectory())));

        final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector();
        final Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
        final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, null, null,
                units);

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
        return (ValueIndexerFactory.getHomeDir() + "/index_java/bin");
    }

    private static String getSrcDirectory()
    {
        return (ValueIndexerFactory.getHomeDir() + "/index_java/src");
    }

    protected static Class<?>[] getClasses()
    {
        try
        {
            final List<String> classNames = getClassNames();
            final Class[] classes = new Class[classNames.size()];
            final ClassLoader classLoader = getClassLoader();
            for (int i = 0; i < classes.length; i++)
            {
                final String className = classNames.get(i);
                classes[i] = classLoader.loadClass(className);
            }
            return classes;
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
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
            final String sourcePath = sourceFile.getPath();
            final int pathOffset = getSrcDirectory().length() + (getSrcDirectory().endsWith("/") ? 0 : 1);
            final String classPath = sourcePath.substring(pathOffset, sourcePath.length() - 5);
            final String className = classPath.replace(File.separator, ".");
            classNames.add(className);
        }
        return classNames;
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
        final String targetPath = getBinDirectory() + sourcePath.substring(getSrcDirectory().length(), sourcePath.length() - 5)
                + ".class";
        final File targetFile = new File(targetPath);
        return !targetFile.exists() || targetFile.lastModified() < sourceFile.lastModified();
    }
}
