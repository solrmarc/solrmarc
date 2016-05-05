package playground.solrmarc.index.indexer;

import playground.solrmarc.index.collector.MultiValueCollector;
//import playground.solrmarc.index.collector.impl.FirstObjectMultiValueCollector;
import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;
import playground.solrmarc.index.extractor.impl.direct.DirectMultiValueExtractor;
import playground.solrmarc.index.fieldmatch.FieldFormatter.eCleanVal;
import playground.solrmarc.index.fieldmatch.FieldFormatter.eJoinVal;
//import playground.solrmarc.index.fieldmatch.FieldFormatterJoin;
import playground.solrmarc.index.fieldmatch.FieldFormatterMapped;
//import playground.solrmarc.index.fieldmatch.FieldFormatterSubstring;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;
import playground.solrmarc.index.mapping.AbstractValueMappingFactory;
//import playground.solrmarc.index.specification.ErrorSpecification;
import playground.solrmarc.index.utils.ReflectionUtils;
import playground.solrmarc.index.utils.StringReader;
import org.apache.log4j.Logger;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class ValueIndexerFactory
{
    private final static Pattern COMMA_SPLIT_PATTERN = Pattern.compile(",");
//    private final static FirstObjectMultiValueCollector FIRST_OBJECT_COLLECTOR = new FirstObjectMultiValueCollector();
//    private final static MultiValueCollector MULTI_VALUE_COLLECTOR = new MultiValueCollector();
    // private final static FieldMatchCollector MULTI_VALUE_FM_COLLECTOR = new
    // FieldMatchCollector();
    // private final static FieldMatchCollector MULTI_VALUE_UNIQUE_FM_COLLECTOR
    // = new FieldMatchCollector(true);
    // private final static JoiningMultiValueCollector
    // JOINING_MULTI_VALUE_COLLECTOR = new JoiningMultiValueCollector();
    // private final static SingleValueCollector SINGLE_VALUE_COLLECTOR = new
    // SingleValueCollector();

    private final static Logger logger = Logger.getLogger(ValueIndexerFactory.class);
    private final List<AbstractValueExtractorFactory> extractorFactories;
    private final List<AbstractValueMappingFactory> mappingFactories;
    private List<IndexerSpecException> validationExceptions;
    private static FullConditionalParser parser = null;

    public List<IndexerSpecException> getValidationExceptions()
    {
        return validationExceptions;
    }

    private static ValueIndexerFactory theFactory = new ValueIndexerFactory();
    
    public static ValueIndexerFactory instance()
    {
        return(theFactory);
    }
    
    private ValueIndexerFactory()
    {
        validationExceptions = new ArrayList<IndexerSpecException>();
        try
        {
            this.extractorFactories = createExtractorFactories(ReflectionUtils.getExtractorFactoryClasses());
            this.mappingFactories = createMappingFactories(ReflectionUtils.getMappingFactoryClasses());
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

    public List<AbstractValueIndexer<?>> createValueIndexers(String configSpecs[])
            throws IllegalAccessException, InstantiationException
    {
        final List<AbstractValueIndexer<?>> valueIndexers = new ArrayList<>();
        validationExceptions.clear();
        if (parser == null) parser = new FullConditionalParser(true);
        parser.setFactories(this, this.extractorFactories, this.mappingFactories);
        for (final String singleSpec : configSpecs)
        {
            if (singleSpec.startsWith("#") || (!singleSpec.contains(":") && !singleSpec.contains("="))) continue;
            final String[] specParts = singleSpec.split("[ ]?[:=][ ]?", 2);
            final String solrFieldName = specParts[0].trim();
            final String mappingDefinition = specParts[1].trim();
            try
            {
                MultiValueIndexer valueIndexer = parser.parse(singleSpec);
                if (valueIndexer != null)
                {
                    valueIndexers.add(valueIndexer);
                }
                boolean first = true;
                for (String err  : parser.getErrors())
                {
                    if (first) 
                    {
                        first = false;
                        validationExceptions.add(new IndexerSpecException(solrFieldName, mappingDefinition, err));
                    }
                    else 
                    {
                        validationExceptions.add(new IndexerSpecException(solrFieldName, null, err));
                    }
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
    // public List<AbstractValueIndexer<?>> createValueIndexers(String
    // configSpecs[]) throws IllegalAccessException, InstantiationException
    // {
    // final List<AbstractValueIndexer<?>> valueIndexers = new ArrayList<>();
    // validationExceptions.clear();
    //
    // for (final String singleSpec : configSpecs)
    // {
    // if (singleSpec.startsWith("#") || (!singleSpec.contains(":") &&
    // !singleSpec.contains("="))) continue;
    // final String[] specParts = singleSpec.split("[ ]?[:=][ ]?", 2);
    // final String solrFieldName = specParts[0].trim();
    // final String mappingDefinition = specParts[1].trim();
    // try {
    // final AbstractValueIndexer<?> valueIndexer =
    // createValueIndexer(solrFieldName, mappingDefinition);
    // if (valueIndexer != null)
    // {
    // valueIndexers.add(valueIndexer);
    // }
    // }
    // catch (IndexerSpecException ise)
    // {
    // ise.setSolrFieldAndSpec(solrFieldName,mappingDefinition);
    // validationExceptions.add(ise);
    // }
    // }
    // return valueIndexers;
    // }

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

    public AbstractValueIndexer<?> makeMultiValueIndexer(List<String> fieldnames, AbstractValueExtractor<?> extractor, List<List<String>> mapSpecs)
    {
        if (extractor instanceof DirectMultiValueExtractor)
        {
            final DirectMultiValueExtractor multiValueExtractor = (DirectMultiValueExtractor) extractor;
//            if (multiValueExtractor.getFieldsAndSubfieldSpec() instanceof ErrorSpecification)
//            {
//  //              throw new IndexerSpecException((ErrorSpecification) multiValueExtractor.getFieldsAndSubfieldSpec());
//            }
            decorateMultiValueExtractor(fieldnames, multiValueExtractor, mapSpecs);
            final AbstractMultiValueMapping[] mappings = new AbstractMultiValueMapping[0];
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs);

            return new MultiValueIndexer(fieldnames, multiValueExtractor, mappings, collector);
        }
        else if (extractor instanceof AbstractMultiValueExtractor)
        {
            final AbstractMultiValueExtractor multiValueExtractor = (AbstractMultiValueExtractor) extractor;
            final List<AbstractMultiValueMapping> mappings = createMultiValueMappings(multiValueExtractor, mapSpecs);
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs);
            return new MultiValueIndexer(fieldnames, multiValueExtractor, mappings, collector);
        }
        else if (extractor instanceof AbstractSingleValueExtractor)
        {
            final AbstractSingleValueExtractor singleValueExtractor = (AbstractSingleValueExtractor) extractor;
            final List<AbstractMultiValueMapping> mappings = createMultiValueMappings(singleValueExtractor, mapSpecs);
            // final AbstractSingleValueMapping[] mappings =
            // createSingleValueMappings(configurationReader);
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs);
            // final SingleValueCollector collector =
            // createSingleValueCollector(configurationReader);
            return new MultiValueIndexer(fieldnames, singleValueExtractor, mappings, collector);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Only subclasses of AbstractMultiValueExtractor or AbstractSingleValueExtractor are allowed, but not "
                            + extractor.getClass().getName());
        } 
    }
    
    private List<AbstractMultiValueMapping> createMultiValueMappings(AbstractValueExtractor<?> extractor, List<List<String>> mapSpecs)
    {
        List<AbstractMultiValueMapping> maps = new ArrayList<AbstractMultiValueMapping>(mapSpecs.size());
        if (mapSpecs.size() == 0)
        {
            return maps;
        }
        for (List<String> spec : mapSpecs)
        {

            for (List<String> mapSpec : mapSpecs)
            {
                String mapParts[] = mapSpec.toArray(new String[0]);
                if (mapParts[0].equals("unique"))
                {
                    /* ignore */ //extractor.setUnique(true);
                }
                else if (mapParts[0].equals("first"))
                {
                    /* ignore */
                }
                else if (mapParts[0].equals("sort"))
                {
                    /* ignore */
                }
                else if (isAValueMappingConfiguration(mapParts[0]))
                {
                    AbstractMultiValueMapping valueMapping = createMultiValueMapping(mapParts);
                    if (valueMapping != null) maps.add(valueMapping);
                }
                else
                {
                    validationExceptions.add(new IndexerSpecException("Illegal format specification: " + toDelimitedString(mapParts, " ")));
                }
            }
            
        }
        return maps;
    }

    private MultiValueCollector createMultiValueCollector(List<List<String>> mapSpecs)
    {
        MultiValueCollector collector = new MultiValueCollector();
        for (List<String> mapSpec : mapSpecs)
        {
            String mapParts[] = mapSpec.toArray(new String[0]);
            if (mapParts[0].equals("unique"))
            {
                collector.setUnique(true);
            }
            else if (mapParts[0].equals("first"))
            {
                collector.setFirst(true);
            }
            else if (mapParts[0].equals("sort"))
            {
                collector.setSortComparator(mapParts[1], mapParts[2]);
            }
        }
        return collector;
    }

    private void decorateMultiValueExtractor(List<String> fieldnames, DirectMultiValueExtractor multiValueExtractor, List<List<String>> mapSpecs)
    {
        // List<AbstractMultiValueMapping> mappings = new ArrayList<>();

        if (mapSpecs.size() == 0)
        {
            return;
        }

        for (List<String> mapSpec : mapSpecs)
        {
            String mapParts[] = mapSpec.toArray(new String[0]);
            if (mapParts[0].equals("sort") || mapParts[0].equals("unique") || mapParts[0].equals("first"))
            {
                /* ignore, handle it elsewhere */
            }
            else if (mapParts[0].equals("join"))
            {
                multiValueExtractor.setJoinVal(eJoinVal.JOIN);
                if (mapParts.length > 1)
                {
                    multiValueExtractor.setSeparator(mapParts[1]);
                }
            }
            else if (mapParts[0].equals("separate"))
            {
                multiValueExtractor.setJoinVal(eJoinVal.SEPARATE);
            }
            else if (mapParts[0].equals("format"))
            {
                if (mapParts.length > 1)
                {
                 //   multiValueExtractor.setSeparator(mapParts[1]);
                }
            }
            else if (mapParts[0].equals("substring"))
            {
                try {
                    if (mapParts.length > 2)
                    {
                        multiValueExtractor.setSubstring(mapParts[1], mapParts[2]);
                    }
                    else
                    {
                        multiValueExtractor.setSubstring(mapParts[1], mapParts[1]);
                    }
                }
                catch (IndexerSpecException ise)
                {
                    validationExceptions.add(ise);
                }
            }
//            else if (mapParts[0].equals("unique"))
//            {
//                multiValueExtractor.setUnique(true);
//            }
//            else if (mapParts[0].equals("first"))
//            {
//               /* ignore */
//            }
            else if (mapParts[0].equals("cleanEach"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_EACH);
            }
            else if (mapParts[0].equals("cleanEnd"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_END);
            }
            else if (mapParts[0].equals("clean"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_EACH);
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_END);
            }
            else if (mapParts[0].equals("stripAccent"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.STRIP_ACCCENTS);
            }
            else if (mapParts[0].equals("stripPunct"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.STRIP_ALL_PUNCT);
            }
            else if (mapParts[0].equals("stripInd2"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.STRIP_INDICATOR_2);
            }
            else if (mapParts[0].equals("toUpper"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.TO_UPPER);
            }
            else if (mapParts[0].equals("toLower"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.TO_LOWER);
            }
            else if (mapParts[0].equals("toUpper"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.TO_UPPER);
            }
            else if (mapParts[0].equals("toLower"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.TO_LOWER);
            }
            else if (mapParts[0].equals("titleSortUpper"))
            {
                multiValueExtractor.setCleanVal(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.CLEAN_END, eCleanVal.STRIP_ACCCENTS,
                        eCleanVal.STRIP_ALL_PUNCT, eCleanVal.STRIP_INDICATOR_2));
                multiValueExtractor.addCleanVal(eCleanVal.TO_UPPER);
            }
            else if (mapParts[0].equals("titleSortLower"))
            {
                multiValueExtractor.setCleanVal(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.CLEAN_END, eCleanVal.STRIP_ACCCENTS,
                        eCleanVal.STRIP_ALL_PUNCT, eCleanVal.STRIP_INDICATOR_2));
                multiValueExtractor.addCleanVal(eCleanVal.TO_LOWER);
            }
            else if (isAValueMappingConfiguration(mapParts[0]))
            {
                AbstractMultiValueMapping valueMapping = createMultiValueMapping(mapParts);
                multiValueExtractor.addFormatter(new FieldFormatterMapped(valueMapping));
            }
            else
            {
                validationExceptions.add(new IndexerSpecException("Illegal format specification: " + toDelimitedString(mapParts, " ")));
            }
        }

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
            if (mappingConfig.startsWith("join"))
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

//            else if (mapParts[0].equals("substring"))
//            {
//                if (mapParts.length > 2)
//                {
//                    fmt = new FieldFormatterSubstring(fmt, mapParts[1], mapParts[2]);
//                }
//                else
//                {
//                    fmt = new FieldFormatterSubstring(fmt, mapParts[1]);
//                }
//            }
            else if (mappingConfig.equals("unique"))
            {
                multiValueExtractor.setUnique(true);
            }
            else if (mappingConfig.equals("first"))
            {
                multiValueExtractor.setFirstOnly(true);
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

    /**
     * @see ValueIndexerFactory#createMultiValueMappings(StringReader)
     */
    // private AbstractSingleValueMapping[] createSingleValueMappings(final
    // StringReader mappingConfiguration)
    // {
    // List<AbstractSingleValueMapping> mappings = new ArrayList<>();
    // mappingConfiguration.skipUntilAfter(',');
    //
    // mappingConfiguration.mark();
    // final String config = mappingConfiguration.readAll().trim();
    // mappingConfiguration.reset();
    //
    // if (config.isEmpty())
    // {
    // return new AbstractSingleValueMapping[0];
    // }
    //
    // for (final String mappingConfig : COMMA_SPLIT_PATTERN.split(config))
    // {
    // if (!isAValueMappingConfiguration(mappingConfig))
    // {
    // break;
    // }
    // AbstractSingleValueMapping valueMapping =
    // createSingleValueMapping(mappingConfig.trim());
    // if (valueMapping != null)
    // {
    // mappings.add(valueMapping);
    // }
    // mappingConfiguration.skip(mappingConfig.length() + 1);
    // }
    // return mappings.toArray(new AbstractSingleValueMapping[mappings.size()]);
    // }

    private boolean isAValueMappingConfiguration(final String configuration)
    {
        if (configuration.matches("[A-Z0-9a-z_]+[.]properties([(][A-Za-z0-9]*[)])?") || configuration.startsWith("map")
                || configuration.startsWith("filter") || configuration.startsWith("custom_map"))
        {
            return (true);
        }
        return (false);
    }

    // private AbstractSingleValueMapping createSingleValueMapping(final String
    // mappingConfig)
    // {
    // for (final AbstractValueMappingFactory mappingFactory : mappingFactories)
    // {
    // if (mappingFactory.canHandle(mappingConfig))
    // {
    // return mappingFactory.createSingleValueMapping(mappingConfig);
    // }
    // }
    // throw new IndexerSpecException("Could not handle impl: " + mappingConfig
    // + "\nLoaded impl factories:\n" +
    // mappingFactories.toString().replaceAll(",", ",\n"));
    // }
    private static String toDelimitedString(String[] strs, String delimiter)
    {
        StringBuilder strb = new StringBuilder();
        for (String str : strs)
        {
          strb.append(str).append(delimiter);
        }
        return strb.substring(0, strb.length() - delimiter.length());
     }

    private AbstractMultiValueMapping createMultiValueMapping(String[] mapParts)
    {
        for (final AbstractValueMappingFactory mappingFactory : mappingFactories)
        {
            if (mappingFactory.canHandle(mapParts[0]))
            {
                return mappingFactory.createMultiValueMapping(mapParts);
            }
        }
        throw new IndexerSpecException("Could not handle map descriptor: " + toDelimitedString(mapParts, " ")); 
        // + "\nLoaded impl factories:\n" + mappingFactories.toString().replaceAll(",", ",\n"));
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
//    {
//        // TODO: Factory!
//        configurationReader.skipUntilAfter(',');
//        configurationReader.mark();
//        String collectorIdentifier = configurationReader.readAll().trim();
//        configurationReader.reset();
//
//        if (collectorIdentifier.isEmpty())
//        {
//            return MULTI_VALUE_COLLECTOR;
//        }
//        else if (collectorIdentifier.startsWith("first"))
//        {
//            return FIRST_OBJECT_COLLECTOR;
//        }
//        throw new IndexerSpecException(
//                "The named collector implementation be identified. " + configurationReader.getLookahead());
//    }

    // private FieldMatchCollector createCollector(DirectMultiValueExtractor
    // multiValueExtractor, StringReader configurationReader)
    // {
    // configurationReader.skipUntilAfter(',');
    // configurationReader.mark();
    // String collectorIdentifier = configurationReader.readAll().trim();
    // configurationReader.reset();
    // if (collectorIdentifier.startsWith(DirectMultiValueExtractor.UNIQUE))
    // {
    // multiValueExtractor.setUnique(true);
    // }
    // throw new IndexerSpecException("The impl couldn't be identified. " +
    // configurationReader);
    // }

    // private SingleValueCollector createSingleValueCollector(StringReader
    // configurationReader)
    // {
    // // TODO: Factory!
    // return SINGLE_VALUE_COLLECTOR;
    // }
}
