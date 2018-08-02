package org.solrmarc.index.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import org.solrmarc.driver.BootableMain;
import org.solrmarc.driver.LoggerDelegator;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.extractor.impl.custom.Mixin;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.mapping.AbstractValueMappingFactory;

public class ClasspathUtils
{
    public static final LoggerDelegator logger = new LoggerDelegator(ClasspathUtils.class);
    protected Set<Class<? extends AbstractValueExtractorFactory>> extractors = null;
    protected Set<Class<? extends AbstractValueMappingFactory>> mappers = null;
    protected Set<Class<? extends BootableMain>> bootables = null;
    protected Set<Class<? extends Mixin>> mixins = null;
    protected static ClasspathUtils theInstance = null;

    public static ClasspathUtils instance()
    {
        if (theInstance == null)
        {
            try
            {
                Class.forName("io.github.lukehutch.fastclasspathscanner.FastClasspathScanner");
                theInstance = new FastClasspathUtils();
            }
            catch (ClassNotFoundException e1)
            {
                logger.error("The FastClasspathScanner class cannot be found.   The associated jar wasn't found or loaded.");
                logger.error("Trying to run without that library, but some dynamic features will not work.");
                theInstance = new ClasspathUtils();
            }
        }
        return theInstance;
    }

    @SuppressWarnings("unchecked")
    private void getDefaultExtractorClasses()
    {
        this.extractors = new LinkedHashSet<>();
        try
        {
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.impl.constant.ConstantValueExtractorFactory"));
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.impl.custom.CustomValueExtractorFactory"));
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.impl.date.DateValueExtractorFactory"));
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.impl.direct.DirectValueExtractorFactory"));
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.impl.fullrecord.FullRecordValueExtractorFactory"));
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.impl.java.JavaValueExtractorFactory"));
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.impl.patternMapping.PatternMappingValueExtractorFactory"));
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.methodcall.AbstractMethodCallFactory"));
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Cannot find a required extractor class:" + e.getMessage());
            throw new IndexerSpecException(e, "Error creating set of default extractor factories");
        }
        try
        {
            Class.forName("bsh.Interpreter");
            this.extractors.add((Class<? extends AbstractValueExtractorFactory>) Class.forName("org.solrmarc.index.extractor.impl.script.ScriptValueExtractorFactory"));
        }
        catch (ClassNotFoundException e)
        {
            logger.warn("Cannot find BeanShell Interpreter class:  any index specification that uses BeanShell scripts will cause an error:" + e.getMessage());
        }
    }

    public Set<Class<? extends AbstractValueExtractorFactory>> getExtractorFactoryClasses()
    {
        if (this.extractors == null || this.extractors.size() == 0)
        {
            getDefaultExtractorClasses();
        }
        return this.extractors;
    }

    @SuppressWarnings("unchecked")
    private void getDefaultMappingClasses()
    {
        this.mappers = new LinkedHashSet<>();
        try
        {
            this.mappers.add((Class<? extends AbstractValueMappingFactory>) Class.forName("org.solrmarc.index.mapping.impl.MethodCallMappingFactory"));
            this.mappers.add((Class<? extends AbstractValueMappingFactory>) Class.forName("org.solrmarc.index.mapping.impl.PatternMappingFactory"));
            this.mappers.add((Class<? extends AbstractValueMappingFactory>) Class.forName("org.solrmarc.index.mapping.impl.TranslationMappingFactory"));
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Cannot find a required mapping class:" + e.getMessage());
            throw new IndexerSpecException(e, "Error creating set of default mapping factories");
        }
    }

    public Set<Class<? extends AbstractValueMappingFactory>> getMappingFactoryClasses()
    {
        if (this.mappers == null || this.mappers.size() == 0)
        {
            getDefaultMappingClasses();
        }
        return this.mappers;
    }

    @SuppressWarnings("unchecked")
    private void getDefaultMixinClasses()
    {
        this.mixins = new LinkedHashSet<>();
        try
        {
            this.mixins.add((Class<? extends Mixin>) Class.forName("org.solrmarc.index.SolrIndexer"));
            this.mixins.add((Class<? extends Mixin>) Class.forName("org.solrmarc.callnum.CallNumberMixin"));
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Cannot find a required mixin class:" + e.getMessage());
            throw new IndexerSpecException(e, "Error creating set of default mixin classes");
        }
    }

    public Set<Class<? extends Mixin>> getMixinClasses()
    {
        if (this.mixins == null || this.mixins.size() == 0)
        {
            getDefaultMixinClasses();
        }
        return this.mixins;
    }

    @SuppressWarnings("unchecked")
    private void getDefaultBootableClasses()
    {
        this.bootables = new LinkedHashSet<>();
        try
        {
            this.bootables.add((Class<? extends BootableMain>) Class.forName("org.solrmarc.driver.IndexDriver"));
            this.bootables.add((Class<? extends BootableMain>) Class.forName("org.solrmarc.driver.ConfigDriver"));
            this.bootables.add((Class<? extends BootableMain>) Class.forName("org.solrmarc.debug.SolrMarcDebug"));
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Cannot find a required bootable class:" + e.getMessage());
            throw new IndexerSpecException(e, "Error creating set of default bootable classes");
        }
    }

    public Set<Class<? extends BootableMain>> getBootableMainClasses()
    {
        if (this.bootables == null || this.bootables.size() == 0)
        {
            getDefaultBootableClasses();
        }
        return this.bootables;
    }
}
