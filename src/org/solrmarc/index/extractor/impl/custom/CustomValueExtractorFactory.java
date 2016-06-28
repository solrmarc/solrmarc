package org.solrmarc.index.extractor.impl.custom;

import org.apache.log4j.Logger;
import org.solrmarc.index.extractor.methodcall.AbstractMethodCallFactory;
import org.solrmarc.index.utils.ReflectionUtils;

import java.util.Collection;

public class CustomValueExtractorFactory extends AbstractMethodCallFactory
{
    private final static Logger logger = Logger.getLogger(CustomValueExtractorFactory.class);

    public CustomValueExtractorFactory()
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<Class<?>> classes = (Collection) ReflectionUtils.getSubclasses(Mixin.class);
        addMethodsFromClasses(classes);
        logger.trace("Custom methods:\n" + methodCallManager.loadedExtractorMixinsToString());
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration)
    {
        return mappingConfiguration.startsWith("custom");
    }
}
