package playground.solrmarc.index.extractor.impl.java;


import playground.solrmarc.index.extractor.methodcall.AbstractMethodCallFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;


public class JavaValueExtractorFactory extends AbstractMethodCallFactory {
    private final static Logger logger = Logger.getLogger(JavaValueExtractorFactory.class);

    public JavaValueExtractorFactory() {
        try {
            JavaValueExtractorUtils.compileSources();
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        addMethodsFromClasses(Arrays.asList(JavaValueExtractorUtils.getClasses()));
        logger.trace("Java methods:\n" + methodCallManager.loadedMixinsToString());
    }

    @Override
    public boolean canHandle(final String solrFieldName, final String mappingConfiguration) {
        return mappingConfiguration.startsWith("java");
    }
}
