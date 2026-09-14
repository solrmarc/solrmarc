package org.solrmarc.index.extractor.impl.java;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * End-to-end regression test for the actual capability this class exists
 * to provide: writing a fresh, previously-uncompiled .java source file to
 * disk, compiling it at runtime via compileSources(), then loading and
 * successfully invoking a method on the resulting compiled class via
 * getClasses()/getClassLoader().
 *
 * Before this test existed, nothing in the automated suite compiled and
 * loaded a genuinely new .java file at runtime at all - other tests only
 * exercise already-compiled code. This specifically exercises the two
 * places that have historically broken across Java versions:
 *
 *   - ClasspathUtils.getClassPath() (used to build the compiler's
 *     classpath), which depends on Boot.getURLClassLoaderToUse() for
 *     cross-version-safe access to the system classloader.
 *   - getClassLoader()'s own child URLClassLoader wrapping
 *     Boot.getURLClassLoaderToUse() as parent (used to load the
 *     resulting compiled class).
 *
 * Running this test across a Java 8 / 11 / 17 CI matrix is what actually
 * proves "the jar can compile and run locally-provided code" holds
 * across all three - a green build without this test only proves the
 * pre-existing, narrower test suite passes on each JDK, not that this
 * specific dynamic-compilation feature does.
 */
public class JavaValueExtractorUtilsCompileTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void compilesAndLoadsAFreshCustomMixinClassAtRuntime() throws Exception
    {
        File tempHome = createHomeDirectoryWithMixinSource();

        JavaValueExtractorUtils utils = new JavaValueExtractorUtils(
            new String[] { tempHome.getAbsolutePath() });

        boolean compiledSomething = utils.compileSources(true);
        assertTrue("compileSources() should report it compiled the new source file", compiledSomething);

        Class<?>[] classes = utils.getClasses();
        assertEquals("exactly one custom mixin class should have been loaded", 1, classes.length);

        Class<?> mixinClass = classes[0];
        assertEquals("org.solrmarc.test.generated.RuntimeCompiledTestMixin", mixinClass.getName());

        Object instance = mixinClass.getDeclaredConstructor().newInstance();
        Method method = mixinClass.getMethod("greeting");
        Object result = method.invoke(instance);

        assertEquals("hello from runtime-compiled code", result);
    }

    /**
     * Companion to the test above, exercising the OTHER class-name-
     * resolution path in JavaValueExtractorUtils: when a .java source
     * file sits flatly in the src directory (no package-matching
     * subdirectories at all), getClassNameForSourceFile() falls back to
     * parsing the "package ...;" declaration out of the file's own
     * content via getPackageName(), rather than deriving the class name
     * from the directory structure. Both paths need to keep working
     * across Java versions, since either is a legitimate way for a
     * SolrMarc user to lay out a custom mixin source file.
     */
    @Test
    public void compilesAndLoadsAFlatlyPlacedMixinWithPackageDeclaredInSource() throws Exception
    {
        File tempHome = createFlatHomeDirectoryWithMixinSource();

        JavaValueExtractorUtils utils = new JavaValueExtractorUtils(
            new String[] { tempHome.getAbsolutePath() });

        boolean compiledSomething = utils.compileSources(true);
        assertTrue("compileSources() should report it compiled the new source file", compiledSomething);

        Class<?>[] classes = utils.getClasses();
        assertEquals("exactly one custom mixin class should have been loaded", 1, classes.length);

        Class<?> mixinClass = classes[0];
        assertEquals("org.solrmarc.test.generated.FlatPackageDeclaredMixin", mixinClass.getName());

        Object instance = mixinClass.getDeclaredConstructor().newInstance();
        Method method = mixinClass.getMethod("greeting");
        Object result = method.invoke(instance);

        assertEquals("hello from a flatly-placed mixin", result);
    }

    private File createHomeDirectoryWithMixinSource() throws IOException
    {
        File tempHome = tempFolder.newFolder("solrmarc-compile-test-home");

        File srcDir = new File(tempHome, "index_java" + File.separator + "src"
            + File.separator + "org" + File.separator + "solrmarc"
            + File.separator + "test" + File.separator + "generated");
        assertTrue("failed to create test source directory", srcDir.mkdirs());

        File sourceFile = new File(srcDir, "RuntimeCompiledTestMixin.java");
        try (FileWriter writer = new FileWriter(sourceFile))
        {
            writer.write(
                "package org.solrmarc.test.generated;\n" +
                "\n" +
                "public class RuntimeCompiledTestMixin\n" +
                "{\n" +
                "    public String greeting()\n" +
                "    {\n" +
                "        return \"hello from runtime-compiled code\";\n" +
                "    }\n" +
                "}\n");
        }

        return tempHome;
    }

    private File createFlatHomeDirectoryWithMixinSource() throws IOException
    {
        File tempHome = tempFolder.newFolder("solrmarc-compile-test-home-flat");

        // Deliberately NOT nested under org/solrmarc/test/generated/ -
        // sits directly in index_java/src, with no matching
        // subdirectories at all.
        File srcDir = new File(tempHome, "index_java" + File.separator + "src");
        assertTrue("failed to create test source directory", srcDir.mkdirs());

        File sourceFile = new File(srcDir, "FlatPackageDeclaredMixin.java");
        try (FileWriter writer = new FileWriter(sourceFile))
        {
            writer.write(
                "package org.solrmarc.test.generated;\n" +
                "\n" +
                "public class FlatPackageDeclaredMixin\n" +
                "{\n" +
                "    public String greeting()\n" +
                "    {\n" +
                "        return \"hello from a flatly-placed mixin\";\n" +
                "    }\n" +
                "}\n");
        }

        return tempHome;
    }
}