package org.solrmarc.index.extractor.impl.java;

import org.apache.log4j.Logger;
import org.solrmarc.index.extractor.methodcall.AbstractMethodCallFactory;
import org.solrmarc.index.indexer.ValueIndexerFactory;

import java.util.Arrays;

public class JavaValueExtractorFactory extends AbstractMethodCallFactory
{
    private final static Logger logger = Logger.getLogger(JavaValueExtractorFactory.class);

    public JavaValueExtractorFactory()
    {
        Class<?>[] classes = ValueIndexerFactory.instance().getCompiledClasses();
        addMethodsFromClasses(Arrays.asList(classes));
        logger.trace("Java extractor methods:\n" + methodCallManager.loadedExtractorMixinsToString());
        logger.trace("Java mapping methods:\n" + methodCallManager.loadedMappingMixinsToString());
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        return mappingConfiguration.startsWith("java");
    }
}
