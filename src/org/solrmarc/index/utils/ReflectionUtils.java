package org.solrmarc.index.utils;

//import org.reflections.Reflections;
//import org.reflections.scanners.SubTypesScanner;
//import org.reflections.util.ClasspathHelper;
//import org.reflections.util.ConfigurationBuilder;
//import org.solrmarc.driver.Boot;
//import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
//import org.solrmarc.index.extractor.impl.custom.Mixin;
//import org.solrmarc.index.mapping.AbstractValueMappingFactory;
//
//import java.util.Set;
//
public class ReflectionUtils
{
//    private final static Reflections reflections = new Reflections(getConfigurationBuilder());
//
//    private static ConfigurationBuilder getConfigurationBuilder()
//    {
//        final ConfigurationBuilder builder = new ConfigurationBuilder();
//        builder.addClassLoader(FastClasspathUtils.class.getClassLoader());
//
//        final String packageName = FastClasspathUtils.class.getPackage().getName();
//        builder.addUrls(ClasspathHelper.forPackage(packageName.substring(0, packageName.indexOf('.'))));
//        builder.addScanners(new SubTypesScanner(false));
//        return builder;
//    }
//
//    private static <T> Set<Class<? extends T>> getSubclasses(final Class<T> type)
//    {
//        return reflections.getSubTypesOf(type);
//    }
// 
//    public static Set<Class<? extends Mixin>> getMixinClasses()
//    {
//        return getSubclasses(Mixin.class);
//    }
//
//    public static Set<Class<? extends AbstractValueExtractorFactory>> getExtractorFactoryClasses()
//    {
//        return getSubclasses(AbstractValueExtractorFactory.class);
//    }
//
//    public static Set<Class<? extends AbstractValueMappingFactory>> getMappingFactoryClasses()
//    {
//        return getSubclasses(AbstractValueMappingFactory.class);
//    }
//    
//    public static Set<Class<? extends Boot>> getBootableMainClasses()
//    {
//        Set<Class<? extends Boot>> objs = getSubclasses(Boot.class);
//        return (objs); 
//    }
//
}