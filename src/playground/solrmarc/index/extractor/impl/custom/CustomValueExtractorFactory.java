package playground.solrmarc.index.extractor.impl.custom;


import playground.solrmarc.index.extractor.methodcall.AbstractMethodCallFactory;
import playground.solrmarc.index.utils.ReflectionUtils;
import org.apache.log4j.Logger;

import java.util.Collection;


public class CustomValueExtractorFactory extends AbstractMethodCallFactory {
    private final static Logger logger = Logger.getLogger(CustomValueExtractorFactory.class);

    public CustomValueExtractorFactory() {
        Collection<Class<?>> classes = (Collection) ReflectionUtils.getSubclasses(Mixin.class);
        addMethodsFromClasses(classes);
        logger.trace("Custom methods:\n" + methodCallManager.loadedMixinsToString());
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration) {
        return mappingConfiguration.startsWith("custom");
    }
}
