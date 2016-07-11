package org.solrmarc.index.indexer;

import org.apache.log4j.Logger;
import org.solrmarc.index.collector.MultiValueCollector;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.extractor.formatter.FieldFormatterMapped;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.extractor.impl.direct.DirectMultiValueExtractor;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.AbstractValueMappingFactory;
import org.solrmarc.index.utils.FastClasspathUtils;
//import org.solrmarc.index.utils.ReflectionUtils;
import org.solrmarc.index.utils.StringReader;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class ValueIndexerStringReaderFactory
{
    private final static Pattern COMMA_SPLIT_PATTERN = Pattern.compile(",");

    private final static Logger logger = Logger.getLogger(ValueIndexerStringReaderFactory.class);
    private final List<AbstractValueExtractorFactory> extractorFactories;
    private final List<AbstractValueMappingFactory> mappingFactories;
    private List<IndexerSpecException> validationExceptions;
    private static FullConditionalParser parser = null;

    public List<IndexerSpecException> getValidationExceptions()
    {
        return validationExceptions;
    }

    private static ValueIndexerStringReaderFactory theFactory = new ValueIndexerStringReaderFactory();
    
    public static ValueIndexerStringReaderFactory instance()
    {
        return(theFactory);
    }
    
    private ValueIndexerStringReaderFactory()
    {
        validationExceptions = new ArrayList<IndexerSpecException>();
        try
        {
            this.extractorFactories = createExtractorFactories(FastClasspathUtils.getExtractorFactoryClasses());
            this.mappingFactories = createMappingFactories(FastClasspathUtils.getMappingFactoryClasses());
        }
        catch (IllegalAccessException | InstantiationException e)
        {
            throw new IndexerSpecException(e, "Error creating extractor or mapping factories");
        }
    }

    public List<AbstractValueIndexer<?>> createValueIndexers(Properties indexerProperties)
            throws IllegalAccessException, InstantiationException
    {
        final List<AbstractValueIndexer<?>> valueIndexers = new ArrayList<>();
        validationExceptions.clear();
        for (final String solrFieldName : indexerProperties.stringPropertyNames())
        {
            final String mappingDefinition = indexerProperties.getProperty(solrFieldName);
            try
            {
                final AbstractValueIndexer<?> valueIndexer = createValueIndexer(solrFieldName, mappingDefinition);
                if (valueIndexer != null)
                {
                    valueIndexers.add(valueIndexer);
                }
            }
            catch (IndexerSpecException ise)
            {
                ise.setSolrFieldAndSpec(solrFieldName, mappingDefinition);
                validationExceptions.add(ise);
            }
        }
        return valueIndexers;
    }

    private List<AbstractValueExtractorFactory> createExtractorFactories(
            final Set<Class<? extends AbstractValueExtractorFactory>> factoryClasses)
            throws IllegalAccessException, InstantiationException
    {
        final List<AbstractValueExtractorFactory> factories = new ArrayList<>(factoryClasses.size());
        for (final Class<? extends AbstractValueExtractorFactory> extractorFactoryClass : factoryClasses)
        {
            if (Modifier.isAbstract(extractorFactoryClass.getModifiers()))
            {
                continue;
            }
            logger.trace("Create value extractor factory for " + extractorFactoryClass);
            final AbstractValueExtractorFactory factory = extractorFactoryClass.newInstance();
            factories.add(factory);
        }
        return factories;
    }

    private List<AbstractValueMappingFactory> createMappingFactories(
            final Set<Class<? extends AbstractValueMappingFactory>> factoryClasses)
            throws IllegalAccessException, InstantiationException
    {
        final List<AbstractValueMappingFactory> factories = new ArrayList<>(factoryClasses.size());
        for (final Class<? extends AbstractValueMappingFactory> extractorFactoryClass : factoryClasses)
        {
            logger.trace("Create value mapping factory for  s " + extractorFactoryClass);
            final AbstractValueMappingFactory factory = extractorFactoryClass.newInstance();
            factories.add(factory);
        }
        return factories;
    }

    /**
     * Creates an indexer representing the indexer process for one solr field
     * (given by solrFieldName), defined by the mappingConfiguration.
     *
     * @param solrFieldName
     *            the name of the solr field.
     * @param mappingConfiguration
     *            the configuration definition for this indexer.
     * @return an indexer representing the indexer process for one solr field.
     */
    public AbstractValueIndexer<?> createValueIndexer(final String solrFieldName, final String mappingConfiguration)
    {
        final StringReader configurationReader = new StringReader(mappingConfiguration);
        final AbstractValueExtractor<?> extractor;
        try
        {
            extractor = createExtractor(solrFieldName, configurationReader);
        }
        finally
        {
        }

        if (extractor == null)
        {
            return null;
        }
        else if (extractor instanceof DirectMultiValueExtractor)
        {
            final DirectMultiValueExtractor multiValueExtractor = (DirectMultiValueExtractor) extractor;
//            if (multiValueExtractor.getFieldsAndSubfieldSpec() instanceof ErrorSpecification)
//            {
//  //              throw new IndexerSpecException((ErrorSpecification) multiValueExtractor.getFieldsAndSubfieldSpec());
//            }
            decorateMultiValueExtractor(solrFieldName, multiValueExtractor, configurationReader);
            final AbstractMultiValueMapping[] mappings = new AbstractMultiValueMapping[0];
            final MultiValueCollector collector = createMultiValueCollector(configurationReader);
            return new MultiValueIndexer(solrFieldName, multiValueExtractor, mappings, collector);
        }
        else if (extractor instanceof AbstractMultiValueExtractor)
        {
            final AbstractMultiValueExtractor multiValueExtractor = (AbstractMultiValueExtractor) extractor;
            final AbstractMultiValueMapping[] mappings = createMultiValueMappings(configurationReader);
            final MultiValueCollector collector = createMultiValueCollector(configurationReader);
            return new MultiValueIndexer(solrFieldName, multiValueExtractor, mappings, collector);
        }
        else if (extractor instanceof AbstractSingleValueExtractor)
        {
            final AbstractSingleValueExtractor singleValueExtractor = (AbstractSingleValueExtractor) extractor;
            final AbstractMultiValueMapping[] mappings = createMultiValueMappings(configurationReader);
            // final AbstractSingleValueMapping[] mappings =
            // createSingleValueMappings(configurationReader);
            final MultiValueCollector collector = createMultiValueCollector(configurationReader);
            // final SingleValueCollector collector =
            // createSingleValueCollector(configurationReader);
            return new MultiValueIndexer(solrFieldName, singleValueExtractor, mappings, collector);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Only subclasses of AbstractMultiValueExtractor or AbstractSingleValueExtractor are allowed, but not "
                            + extractor.getClass().getName());
        }
    }

    private boolean isACollectorConfiguration(String string)
    {
        if (string.equals("unique") || string.equals("first") || string.equals("sort"))
            return(true);
        return(false);
    }

    boolean isADecoratorConfiguration(String str)
    {
        if (str.equals("join") || str.equals("separate") || str.equals("format") || str.equals("substring") || 
            str.equals("cleanEach") || str.equals("cleanEnd") || str.equals("clean") || str.equals("stripAccent") || 
            str.equals("stripPunct") || str.equals("stripInd2") || str.equals("toUpper") || str.equals("toLower") || 
            str.equals("toUpper") || str.equals("toLower") || str.equals("titleSortUpper") || str.equals("titleSortLower"))
            return(true);
        return(false);
    }
    
    private void decorateMultiValueExtractor(final String solrFieldName, DirectMultiValueExtractor multiValueExtractor,
            StringReader mappingConfiguration)
    {
        // List<AbstractMultiValueMapping> mappings = new ArrayList<>();
        mappingConfiguration.skipUntilAfter(',');

        mappingConfiguration.mark();
        final String configurationData = mappingConfiguration.readAll().trim();
        mappingConfiguration.reset();

        if (configurationData.isEmpty())
        {
            return;
        }

 //       FieldFormatter fmt = multiValueExtractor.getFormatter();
        for (String mappingConfig : COMMA_SPLIT_PATTERN.split(configurationData))
        {
            mappingConfig = mappingConfig.trim();
            if (mappingConfig.startsWith("sort") || mappingConfig.startsWith("unique") || mappingConfig.startsWith("first"))
            {
                /* ignore, handle it elsewhere */
            }
            else if (mappingConfig.startsWith("join"))
            {
                multiValueExtractor.setJoinVal(eJoinVal.JOIN);
                final int openParanthisis = mappingConfig.indexOf('(');
                final int closeParanthisis = mappingConfig.indexOf(')');
                if (openParanthisis >= 0 && closeParanthisis >= 0)
                {
                    multiValueExtractor.setSeparator(mappingConfig.substring(openParanthisis + 1, closeParanthisis));
                }
            }
            else if (mappingConfig.equals("separate"))
            {
                multiValueExtractor.setJoinVal(eJoinVal.SEPARATE);
            }

            else if (mappingConfig.equals("cleanEach"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_EACH);
            }
            else if (mappingConfig.equals("cleanEnd"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_END);
            }
            else if (mappingConfig.equals("clean"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_EACH);
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_END);
            }
            else if (mappingConfig.equals("stripAccent"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.STRIP_ACCCENTS);
            }
            else if (mappingConfig.equals("titleSortUpper"))
            {
                multiValueExtractor.setCleanVal(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.CLEAN_END, eCleanVal.STRIP_ACCCENTS,
                        eCleanVal.STRIP_ALL_PUNCT, eCleanVal.STRIP_INDICATOR_2));
                multiValueExtractor.addCleanVal(eCleanVal.TO_UPPER);
            }
            else if (mappingConfig.equals("titleSortLower"))
            {
                multiValueExtractor.setCleanVal(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.CLEAN_END, eCleanVal.STRIP_ACCCENTS,
                        eCleanVal.STRIP_ALL_PUNCT, eCleanVal.STRIP_INDICATOR_2));
                multiValueExtractor.addCleanVal(eCleanVal.TO_LOWER);
            }
            else if (isAValueMappingConfiguration(mappingConfig))
            {
                AbstractMultiValueMapping valueMapping = createMultiValueMapping(mappingConfig);
                multiValueExtractor.addFormatter(new FieldFormatterMapped(valueMapping));
            }
            else
            {
                validationExceptions.add(new IndexerSpecException("Illegal format specification: " + mappingConfiguration.getLookahead()));
            }
            mappingConfiguration.skip(mappingConfig.length() + 1);
        }
    }

    private AbstractValueExtractor<?> createExtractor(final String solrFieldName, final StringReader mappingConfiguration)
    {
        for (final AbstractValueExtractorFactory factory : extractorFactories)
        {
            if (factory.canHandle(solrFieldName, mappingConfiguration.getLookahead()))
            {
                return factory.createExtractor(solrFieldName, mappingConfiguration);
            }
        }
        throw new IndexerSpecException("No indexer factory found for: " + mappingConfiguration.getLookahead());
    }

    /**
     * Creates a bunch of value mappings in the same order as they are given in
     * the settings.
     *
     * @param mappingConfiguration
     *            the date of the current configuration line
     * @return an array of mappings.
     */
    private AbstractMultiValueMapping[] createMultiValueMappings(final StringReader mappingConfiguration)
    {
        List<AbstractMultiValueMapping> mappings = new ArrayList<>();
        mappingConfiguration.skipUntilAfter(',');

        mappingConfiguration.mark();
        final String configurationData = mappingConfiguration.readAll().trim();
        mappingConfiguration.reset();

        if (configurationData.isEmpty())
        {
            return new AbstractMultiValueMapping[0];
        }

        for (final String mappingConfig : COMMA_SPLIT_PATTERN.split(configurationData))
        {
            if (!isAValueMappingConfiguration(mappingConfig))
            {
                break;
            }
            AbstractMultiValueMapping valueMapping = createMultiValueMapping(mappingConfig.trim());
            if (valueMapping != null)
            {
                mappings.add(valueMapping);
            }
            mappingConfiguration.skip(mappingConfig.length() + 1);
        }
        return mappings.toArray(new AbstractMultiValueMapping[mappings.size()]);
    }

    private boolean isAValueMappingConfiguration(final String configuration)
    {
        if (configuration.matches("[A-Z0-9a-z_]+[.]properties([(][A-Za-z0-9]*[)])?") || configuration.startsWith("map")
                || configuration.startsWith("filter") || configuration.startsWith("custom_map"))
        {
            return (true);
        }
        return (false);
    }

    public AbstractMultiValueMapping createMultiValueMapping(final String mappingConfig)
    {
        for (final AbstractValueMappingFactory mappingFactory : mappingFactories)
        {
            if (mappingFactory.canHandle(mappingConfig))
            {
                return mappingFactory.createMultiValueMapping(mappingConfig);
            }
        }
        throw new IndexerSpecException("Could not handle impl: " + mappingConfig + "\nLoaded impl factories:\n"
                + mappingFactories.toString().replaceAll(",", ",\n"));
    }

    /**
     *   Note:  this probably doesn't work !!!!!
     *   really.
     *
     * @param configurationReader
     * @return
     */
    private MultiValueCollector createMultiValueCollector(StringReader configurationReader)
    {
        // TODO: Factory!
        configurationReader.skipUntilAfter(',');
        configurationReader.mark();
        MultiValueCollector collector = new MultiValueCollector();
        // Note:  this probably doesn't work !!!!!
        // really.
        do {
            String collectorIdentifier = configurationReader.readStringUntil(',').trim();
            if (collectorIdentifier.equals("unique"))
            {
                collector.setUnique(true);
            }
            else if (collectorIdentifier.equals("first"))
            {
                collector.setFirst(true);
            }
            else if (collectorIdentifier.startsWith("sort"))
            {
                String parms[] = collectorIdentifier.substring(5).split("[,)][ ]*", 3);
                collector.setSortComparator(parms[1], parms[2]);
            }
            configurationReader.skipUntilAfter(',');
        } while (configurationReader.isEmpty());
        configurationReader.reset();

        return collector;
    }
}
