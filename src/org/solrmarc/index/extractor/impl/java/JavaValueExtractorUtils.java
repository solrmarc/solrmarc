package org.solrmarc.index.extractor.impl.java;

import org.apache.log4j.Logger;

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
    private final static Map<File, String> packageNamesForFile = new LinkedHashMap<>();
    private final static Pattern packageFinder = Pattern.compile("package[ \t]+(([a-z_][a-z0-9_]*[.])*[a-z_][a-z0-9_]*)[ \t]*;.*");
    private Map<String,List<File>> sourceFilesMap = new LinkedHashMap<String, List<File>>();
    private String[] dirsContainingJavaSource;

    public JavaValueExtractorUtils(String[] dirsContainingJavaSource)
    {
        this.dirsContainingJavaSource = dirsContainingJavaSource;
    }
    
    /**
     * Compiles java sources if they have changed.
     * @param homeDirStrs 
     *
     * @return true if one or more java sources were compiled, else false.
     * @throws IOException
     */
    public boolean compileSources() 
    {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        boolean compiledSome = false;
        if (compiler == null)
        {
            logger.warn("Java environment at JAVA_HOME = "+System.getProperty("java.home")+ " does not have a Java compiler.");
            logger.warn("Any custom mixin routines will not be compiled and will not be available.");
            return compiledSome;
        }
        for (String homeDirectory : dirsContainingJavaSource)
        {
            String srcDirectory = homeDirectory + File.separator + "index_java" + File.separator + "src";
            String binDirectory = homeDirectory + File.separator + "index_java" + File.separator + "bin";
            createBinDirectory(binDirectory);            
            final List<File> sourceFiles = getChangedSourceFiles(srcDirectory, binDirectory);
            if (sourceFiles.isEmpty())
            {
                continue;
            }
            compiledSome = true;
            List<File> classpath = new  ArrayList<>();
            URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            logger.debug("Classpath for compiling java files:");
            for (URL url : sysLoader.getURLs())
            {
                classpath.add(new File(url.getFile()));
                logger.debug("    " + url.getFile());
            }
            try {
                final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, Charset.forName("UTF-8"));
                fileManager.setLocation(StandardLocation.SOURCE_PATH, Collections.singleton(new File(srcDirectory)));
                fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(new File(binDirectory)));
                fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
        
                final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
                final Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
                final Iterable<String> options = Collections.singletonList("-g");
                final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, options, null, units);
        
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
            }
            catch (IOException ioe)
            {
                throw new RuntimeException('\n' + ioe.getMessage() + "\nCompiling java sources failed!");
            }
        }
        return true;
    }

    private void createBinDirectory(String binDirectoryStr)
    {
        final File binDirectory = new File(binDirectoryStr);

        if (!binDirectory.exists())
        {
            if (!binDirectory.mkdirs())
            {
                throw new RuntimeException("Couldn't create binary directory: " + binDirectory.getAbsolutePath());
            }
        }
    }

    public Class<?>[] getClasses()
    {
        final Set<String> classNames = getClassNames();
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

    private ClassLoader getClassLoader()
    {
        ArrayList<URL> listURL = new ArrayList<URL>();
        for (String homeDirectory : dirsContainingJavaSource)
        {
            String binDirectory = homeDirectory + File.separator + "index_java" + File.separator + "bin";
            try {
                URL url = new File(binDirectory).toURI().toURL();
                // Insert each subsequent URL at the back of the list so earlier directories are searched first.
                listURL.add(url);
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException(e);
            }
        }
        URL[] URLs = listURL.toArray(new URL[0]);
        return new URLClassLoader(URLs);
    }

    private Set<String> getClassNames()
    {
        final Set<String> classNames = new LinkedHashSet<>();

        for (String homeDirectory : dirsContainingJavaSource)
        {
            String srcDirectory = homeDirectory + File.separator + "index_java" + File.separator + "src";
            String binDirectory = homeDirectory + File.separator + "index_java" + File.separator + "bin";

            List<File> sourceFiles = getSourceFiles(srcDirectory);
            for (final File sourceFile : sourceFiles)
            {
                classNames.add(getClassNameForSourceFile(sourceFile, srcDirectory, binDirectory));
            }
        }
        return classNames;
    }

    private String getClassNameForSourceFile(final File sourceFile, String srcDirectory, String binDirectory)
    {
        final String sourcePath = sourceFile.getPath();
        final int pathOffset = srcDirectory.length() + (srcDirectory.endsWith("/") ? 0 : 1);
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
    
    private String getClassFileForSourceFile(final File sourceFile, String srcDirectory, String binDirectory)
    {
        final String sourcePath = sourceFile.getPath();
        final int pathOffset = srcDirectory.length() + (srcDirectory.endsWith("/") ? 0 : 1);
        final String classPath = sourcePath.substring(pathOffset, sourcePath.length() - 5);
        String classFile = classPath;
        if (!classPath.contains(File.separator))
        {
            // find package in source file
            final String packageName = getPackageName(sourceFile);
            classFile = packageName.replace(".", File.separator) + classFile;
        }
        return(binDirectory + File.separator + classFile + ".class");
    }

    private String getPackageName(File sourceFile)
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

    private List<File> getChangedSourceFiles(String srcDirectory, String binDirectory)
    {
        final List<File> sourceFiles = getSourceFiles(srcDirectory);
        final List<File> changedSourceFiles = new ArrayList<>();
        for (final File sourceFile : sourceFiles)
        {
            if (hasChanged(sourceFile, srcDirectory, binDirectory))
            {
                changedSourceFiles.add(sourceFile);
            }
        }
        return changedSourceFiles;
    }

    private List<File> getSourceFiles(String srcDirectory)
    {
        List<File> sourceFiles;
        if (sourceFilesMap.containsKey(srcDirectory))
        {
            sourceFiles = sourceFilesMap.get(srcDirectory);
            return sourceFiles;
        }
        sourceFiles = new ArrayList<File>();
        final Queue<File> directories = new LinkedList<>();
        directories.add(new File(srcDirectory));
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
        sourceFilesMap.put(srcDirectory, sourceFiles);
        return sourceFiles;
    }

    private File[] listFiles(File directory)
    {
        if (directory == null)
        {
            return new File[0];
        }
        final File[] fileList = directory.listFiles();
        return fileList != null ? fileList : new File[0];
    }

    private boolean hasChanged(File sourceFile, String srcDirectory, String binDirectory)
    {
        final String sourcePath = sourceFile.getPath();
        final String targetPath = getClassFileForSourceFile(new File(sourcePath), srcDirectory, binDirectory);
        final File targetFile = new File(targetPath);
        return !targetFile.exists() || targetFile.lastModified() < sourceFile.lastModified();
    }
}
