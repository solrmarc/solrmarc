package playground.solrmarc.index.utils;


import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;
import playground.solrmarc.index.mapping.AbstractValueMappingFactory;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;


public class ReflectionUtils
{
    private final static Reflections reflections = new Reflections(getConfigurationBuilder());

    private static ConfigurationBuilder getConfigurationBuilder()
    {
        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addClassLoader(ReflectionUtils.class.getClassLoader());

        final String packageName = ReflectionUtils.class.getPackage().getName();
        builder.addUrls(ClasspathHelper.forPackage(packageName.substring(0, packageName.indexOf('.'))));
        builder.addScanners(new SubTypesScanner(false));
        return builder;
    }

    public static <T> Set<Class<? extends T>> getSubclasses(final Class<T> type)
    {
        return reflections.getSubTypesOf(type);
    }

    public static Set<Class<? extends AbstractValueExtractorFactory>> getExtractorFactoryClasses()
    {
        return getSubclasses(AbstractValueExtractorFactory.class);
    }

    public static Set<Class<? extends AbstractValueMappingFactory>> getMappingFactoryClasses()
    {
        return getSubclasses(AbstractValueMappingFactory.class);
    }
}