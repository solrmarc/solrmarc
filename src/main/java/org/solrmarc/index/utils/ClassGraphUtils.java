package org.solrmarc.index.utils;

import org.solrmarc.driver.Boot;
import org.solrmarc.driver.BootableMain;
import org.solrmarc.driver.LoggerDelegator;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.extractor.impl.custom.Mixin;
import org.solrmarc.index.mapping.AbstractValueMappingFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassGraphUtils extends ClasspathUtils
{
    public static final LoggerDelegator logger = new LoggerDelegator(ClassGraphUtils.class);

    @SuppressWarnings("unchecked")
	private void getMatchingClasses()
    {
        extractors = new LinkedHashSet<>();
        mappers = new LinkedHashSet<>();
        mixins = new LinkedHashSet<>();
        try
        {
            Boot.classForName("bsh.Interpreter");
        }
        catch (ClassNotFoundException e)
        {
            logger.warn("Cannot find BeanShell Interpreter class:  any index specification that uses BeanShell scripts will cause an error:" + e.getMessage());
        }
        try ( ScanResult scanResult = ( new ClassGraph()
//                    .verbose()             // Enable verbose logging
                    .enableClassInfo())       // Scan classes only
                    .scan()) 
        {
            ClassInfoList extractorClassInfo = scanResult.getSubclasses("org.solrmarc.index.extractor.AbstractValueExtractorFactory");
            for (ClassInfo classInfo : extractorClassInfo)
            {
                Class<? extends AbstractValueExtractorFactory> clazz = (Class<? extends AbstractValueExtractorFactory>)  Boot.classForName(classInfo.getName());
                extractors.add(clazz);
            }
            
            ClassInfoList mapperClassInfo = scanResult.getSubclasses("org.solrmarc.index.mapping.AbstractValueMappingFactory");
            for (ClassInfo classInfo : mapperClassInfo)
            {
                Class<? extends AbstractValueMappingFactory> clazz = (Class<? extends AbstractValueMappingFactory>) Boot.classForName(classInfo.getName());
                mappers.add(clazz);
            }
            
            ClassInfoList mixinClassInfo = scanResult.getClassesImplementing("org.solrmarc.index.extractor.impl.custom.Mixin");
            for (ClassInfo classInfo : mixinClassInfo)
            {
                Class<? extends Mixin> clazz = (Class<? extends Mixin>)  Boot.classForName(classInfo.getName());
                mixins.add(clazz);
            }
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Set<Class<? extends AbstractValueExtractorFactory>> getExtractorFactoryClasses()
    {
        if (extractors == null)
        {
            getMatchingClasses();
        }
        if (extractors.size() == 0)
        {
            logger.warn("Classpath scanning failed, using default extractors only");
            extractors = super.getExtractorFactoryClasses();
        }
        return extractors;
    }

    @Override
    public Set<Class<? extends AbstractValueMappingFactory>> getMappingFactoryClasses()
    {
        if (mappers == null)
        {
            getMatchingClasses();
        }
        if (mappers.size() == 0)
        {
            logger.warn("Classpath scanning failed, using default mapping classes only");
            mappers = super.getMappingFactoryClasses();
        }
        return mappers;
    }

    @Override
    public Set<Class<? extends Mixin>> getMixinClasses()
    {
        if (mixins == null)
        {
            getMatchingClasses();
        }
        if (mixins.size() == 0)
        {
            logger.warn("Classpath scanning failed, using default mixin classes only");
            super.getMixinClasses();
        }
        return mixins;
    }

    @SuppressWarnings("unchecked")
	private void getMatchingBootableClasses()
    {
        bootables = new LinkedHashSet<>();
        try ( ScanResult scanResult = ( new ClassGraph()
//                .verbose()             // Enable verbose logging
                .enableClassInfo())       // Scan classes only
                .scan() )
        {
            ClassInfoList bootableClassInfo = scanResult.getSubclasses("org.solrmarc.driver.BootableMain");
            for (ClassInfo classInfo : bootableClassInfo)
            {
                Class<? extends BootableMain> clazz = (Class<? extends BootableMain>) Boot.classForName(classInfo.getName());
                bootables.add(clazz);

            }
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Set<Class<? extends BootableMain>> getBootableMainClasses()
    {
        if (bootables == null)
        {
            getMatchingBootableClasses();
        }
        if (bootables.size() == 0)
        {
            logger.warn("Classpath scanning failed, using default bootable classes only");
            super.getBootableMainClasses();
        }
        return bootables;
    }

    private void getClassPathForCompiling()
    {
        classpathForCompiling = new ArrayList<File>();
        try ( ScanResult scanResult = ( new ClassGraph()
//                .verbose()             // Enable verbose logging
                .enableClassInfo())       // Scan classes, methods, fields, annotations
                .scan()) 
        {
            List<URL> classpathURLForCompiling = scanResult.getClasspathURLs();
            for (URL url : classpathURLForCompiling) 
            {
                classpathForCompiling.add(new File(url.getFile()));
            }
        }
    }

    public List<File> getClassPath()
    {
        if (this.classpathForCompiling == null || this.classpathForCompiling.size() == 0)
        {
            getClassPathForCompiling();
        }
        return this.classpathForCompiling;
    }


}
