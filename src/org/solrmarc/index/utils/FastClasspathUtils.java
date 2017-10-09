package org.solrmarc.index.utils;

import org.apache.log4j.Logger;
import org.solrmarc.driver.BootableMain;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.extractor.impl.custom.Mixin;
import org.solrmarc.index.mapping.AbstractValueMappingFactory;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.InterfaceMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;

import java.util.LinkedHashSet;
import java.util.Set;

public class FastClasspathUtils
{
    public final static Logger logger =  Logger.getLogger(FastClasspathUtils.class);

    private static Set <Class<? extends AbstractValueExtractorFactory>>  extractors = null;
    private static Set <Class<? extends AbstractValueMappingFactory>>    mappers = null;
    private static Set <Class<? extends BootableMain>>                   bootables = null;
    private static Set <Class<? extends Mixin>>                          mixins = null;

    private static void getMatchingClasses()
    {
        extractors = new LinkedHashSet<>();
        mappers = new LinkedHashSet<>();
        mixins = new LinkedHashSet<>();
        FastClasspathScanner scanner = new FastClasspathScanner()  
            .matchSubclassesOf(AbstractValueExtractorFactory.class, new SubclassMatchProcessor<AbstractValueExtractorFactory>() 
            {
                @Override
                public void processMatch(Class<? extends AbstractValueExtractorFactory> matchingClass) 
                {
                    logger.debug("Subclass of AbstractValueExtractorFactory: " + matchingClass);
                    extractors.add(matchingClass);
                }
            })
            .matchSubclassesOf(AbstractValueMappingFactory.class, new SubclassMatchProcessor<AbstractValueMappingFactory>() 
            {
                @Override
                public void processMatch(Class<? extends AbstractValueMappingFactory> matchingClass) 
                {
                    logger.debug("Subclass of AbstractValueMappingFactory: " + matchingClass);
                    mappers.add(matchingClass);
                }
            })
            .matchClassesImplementing(Mixin.class, new InterfaceMatchProcessor<Mixin>() 
            {
                @Override
                public void processMatch(Class<? extends Mixin> matchingClass) 
                {
                    logger.debug("Subclass of Mixin: " + matchingClass);
                    mixins.add(matchingClass);
                }
            });
        scanner.scan();

    }

    public static Set<Class<? extends AbstractValueExtractorFactory>> getExtractorFactoryClasses()
    {
        if (extractors == null)
        {
            getMatchingClasses();
        }
        return extractors;
    }

    public static Set<Class<? extends AbstractValueMappingFactory>> getMappingFactoryClasses()
    {
        if (mappers == null)
        {
            getMatchingClasses();
        }
        return mappers;
    }

    public static Set<Class<? extends Mixin>> getMixinClasses()
    {
        if (mixins == null)
        {
            getMatchingClasses();
        }
        return mixins;
    }

    private static void getMatchingBootableClasses()
    {
        bootables = new LinkedHashSet<>();
        FastClasspathScanner scanner = new FastClasspathScanner()  
            .matchSubclassesOf(BootableMain.class, new SubclassMatchProcessor<BootableMain>() 
            {
                @Override
                public void processMatch(Class<? extends BootableMain> matchingClass) 
                {
                    bootables.add(matchingClass);
                }
            });
        scanner.scan();
    }

    public static Set<Class<? extends BootableMain>> getBootableMainClasses()
    {
        if (bootables == null)
        {
            getMatchingBootableClasses();
        }
        return bootables;
    }
}
