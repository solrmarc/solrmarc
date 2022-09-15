package org.solrmarc.index.extractor.impl.custom;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.solrmarc.index.extractor.methodcall.AbstractMethodCallFactory;
import org.solrmarc.index.utils.ClasspathUtils;

public class CustomValueExtractorFactory extends AbstractMethodCallFactory
{
    private final static Logger logger = LogManager.getLogger(CustomValueExtractorFactory.class);

    public CustomValueExtractorFactory()
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<Class<?>> classes = (Collection)ClasspathUtils.instance().getMixinClasses();
        addMethodsFromClasses(classes);
        logger.trace("Custom methods:\n" + methodCallManager.loadedExtractorMixinsToString());
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        return mappingConfiguration.startsWith("custom");
    }
}
