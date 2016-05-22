package playground.solrmarc.index.indexer;

import playground.solrmarc.index.collector.MultiValueCollector;
import playground.solrmarc.index.extractor.AbstractMultiValueExtractor;
import playground.solrmarc.index.extractor.AbstractSingleValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractor;
import playground.solrmarc.index.extractor.AbstractValueExtractorFactory;
import playground.solrmarc.index.extractor.formatter.FieldFormatterMapped;
import playground.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import playground.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import playground.solrmarc.index.extractor.impl.direct.DirectMultiValueExtractor;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;
import playground.solrmarc.index.mapping.AbstractValueMappingFactory;
import playground.solrmarc.index.utils.ReflectionUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class ValueIndexerFactory
{
    private final static Logger logger = Logger.getLogger(ValueIndexerFactory.class);
    private final List<AbstractValueExtractorFactory> extractorFactories;
    private final List<AbstractValueMappingFactory> mappingFactories;
    private List<IndexerSpecException> validationExceptions;
    private FullConditionalParser parser = null;    
    private Properties localMappingProperties = null;

    /**
     *  The next three functions make the ValueIndexerFactory implement the Singleton pattern
     *  To create of use a Factory  do:
     *  ValueIndexerFactory.instance()
     */
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

    /**
     * Return ALL of the exceptions encountered while processing indexing specification
     * @return
     */
    public List<IndexerSpecException> getValidationExceptions()
    {
        return validationExceptions;
    }

    public Properties getLocalMappingProperties()
    {
        return localMappingProperties;
    }
    
    static boolean debug_parse = true;
    
    public List<AbstractValueIndexer<?>> createValueIndexers(String configSpecs[])
            throws IllegalAccessException, InstantiationException
    {
        final List<AbstractValueIndexer<?>> valueIndexers = new ArrayList<>();
        validationExceptions.clear();
        if (parser == null) parser = new FullConditionalParser(debug_parse);
        parser.setFactories(this, this.extractorFactories, this.mappingFactories);
        localMappingProperties = new Properties();

        for (final String singleSpec : configSpecs)
        {
            if (singleSpec.startsWith("map.") || singleSpec.startsWith("pattern_map.")) 
            {
                final String[] specParts = singleSpec.split("[ ]?[:=][ ]?", 2);
                localMappingProperties.put(specParts[0].trim(), specParts[1].trim());
            }
        }
        for (final String singleSpec : configSpecs)
        {
            if (singleSpec.startsWith("#") || (!singleSpec.contains(":") && !singleSpec.contains("="))) continue;
            if (singleSpec.startsWith("map.") || singleSpec.startsWith("pattern_map.")) continue;
            if (singleSpec.startsWith("marc.") || singleSpec.startsWith("solrmarc.")) continue;
            
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
     * Creates an indexer representing the indexer process for one (or more) solr field
     * (given by fieldnames), Where the extractor has already been created in the parser handling code, and the
     * maps definitions following the extractor have been pulled out as a list of lists of strings.
     *
     * @param origSpec
     *            the original index specification (for debugging purposes).
     * @param solrFieldName
     *            the name (or names) of the solr field(s) this spec should generate.
     * @param extractor
     *            the extractor object created by the parser handling code (or perhaps one manually created)
     * @param mapSpecs
     *            the mapping/collector specifications definition for this indexer.
     * @return an indexer representing the indexer process for one solr field.
     */
    public AbstractValueIndexer<?> makeMultiValueIndexer(String origSpec, List<String> fieldnames, AbstractValueExtractor<?> extractor, List<List<String>> mapSpecs)
    {
        if (extractor instanceof DirectMultiValueExtractor)
        {
            final DirectMultiValueExtractor multiValueExtractor = (DirectMultiValueExtractor) extractor;
            int indexOfJoin = decorateMultiValueExtractor(origSpec, fieldnames, multiValueExtractor, mapSpecs);
            final List<AbstractMultiValueMapping> mappings;
            if (indexOfJoin != -1)
                mappings = createMultiValueMappings(origSpec, mapSpecs, indexOfJoin);
            else 
                mappings = new ArrayList<AbstractMultiValueMapping>();
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs);

            return new MultiValueIndexer(fieldnames, multiValueExtractor, mappings, collector);
        }
        else if (extractor instanceof AbstractMultiValueExtractor)
        {
            final AbstractMultiValueExtractor multiValueExtractor = (AbstractMultiValueExtractor) extractor;
            final List<AbstractMultiValueMapping> mappings = createMultiValueMappings(origSpec, mapSpecs);
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs);
            return new MultiValueIndexer(fieldnames, multiValueExtractor, mappings, collector);
        }
        else if (extractor instanceof AbstractSingleValueExtractor)
        {
            final AbstractSingleValueExtractor singleValueExtractor = (AbstractSingleValueExtractor) extractor;
            final List<AbstractMultiValueMapping> mappings = createMultiValueMappings(origSpec, mapSpecs);
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs);
            return new MultiValueIndexer(fieldnames, singleValueExtractor, mappings, collector);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Only subclasses of AbstractMultiValueExtractor or AbstractSingleValueExtractor are allowed, but not "
                            + extractor.getClass().getName());
        } 
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
      
    private int decorateMultiValueExtractor(String origSpec, List<String> fieldnames, DirectMultiValueExtractor multiValueExtractor, List<List<String>> mapSpecs)
    {
        if (mapSpecs.size() == 0)
        {
            return -1;
        }
        
        int currentIndex = 0;
        int joinIndex = -1;
        for (List<String> mapSpec : mapSpecs)
        {
            String mapParts[] = mapSpec.toArray(new String[0]);
            if (isACollectorConfiguration(mapParts[0]))
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
                joinIndex = currentIndex;
            }
            else if (mapParts[0].equals("separate"))
            {
                multiValueExtractor.setJoinVal(eJoinVal.SEPARATE);
            }
            else if (mapParts[0].equals("format"))
            {
                if (mapParts.length > 1)
                {
                 //  not yet handled
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
                    ise.setSolrFieldAndSpec(origSpec);
                    validationExceptions.add(ise);
                }
            }
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
            else if (isAValueMappingConfiguration(mapParts[0]) && joinIndex == -1)
            {
                AbstractMultiValueMapping valueMapping = createMultiValueMapping(mapParts);
                multiValueExtractor.addFormatter(new FieldFormatterMapped(valueMapping));
            }
            else if (isAValueMappingConfiguration(mapParts[0]) && joinIndex != -1)
            {
                // post join map specification
                //    mapping specs before "join" are applied before the join operation
                //    mapping spec that are after "join" are applied to the joined output. 
                //    and are handled elsewhere.
            }
            else
            {
                validationExceptions.add(new IndexerSpecException(origSpec, "Illegal format specification: " + toDelimitedString(mapParts, " ")));
            }
            currentIndex ++;
        }
        return(joinIndex);

    }

    
    private boolean isAValueMappingConfiguration(final String configuration)
    {
        if (configuration.matches("[A-Z0-9a-z_]+[.]properties([(][A-Za-z0-9]*[)])?") || 
                configuration.matches("[(]this[)][.]properties([(][A-Za-z0-9]*[)])?") || 
                configuration.startsWith("map") || 
                configuration.startsWith("filter") || 
                configuration.startsWith("custom_map"))
        {
            return (true);
        }
        return (false);
    }

    private List<AbstractMultiValueMapping> createMultiValueMappings(String origSpec, List<List<String>> mapSpecs)
    {
        return(createMultiValueMappings(origSpec, mapSpecs, -1));
    }

    /**
     * Creates the multivalue mappers as specified to be appended to a extractor.
     * (given by fieldnames), Where the extractor has already been created in the parser handling code, and the
     * maps definitions following the extractor have been pulled out as a list of lists of strings.
     *
     * @param origSpec
     *            the original index specification (for debugging purposes).
     * @param mapSpecs
     *            the mapping/collector specifications definition for this indexer.
     * @param indexOfJoin 
     *            index within mapSpecs where the "join" specification occurs.
     *            mapping specs before "join" are applied before the join operation
     *            mapping spec that are after "join" are applied to the joined output.           
     * @return an list of multi value mappings
     */
    private List<AbstractMultiValueMapping> createMultiValueMappings(String origSpec, List<List<String>> mapSpecs, int indexOfJoin)
    {
        List<AbstractMultiValueMapping> maps = new ArrayList<AbstractMultiValueMapping>(mapSpecs.size());
        if (mapSpecs.size() == 0)
        {
            return maps;
        }
        int currentIndex = 0;
        for (List<String> mapSpec : mapSpecs)
        {
            if (currentIndex > indexOfJoin)
            {
                String mapParts[] = mapSpec.toArray(new String[0]);
                if (isACollectorConfiguration(mapParts[0]))
                {
                    /* ignore */
                }
                else if (isADecoratorConfiguration(mapParts[0]))
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
                    validationExceptions.add(new IndexerSpecException(origSpec, "Illegal format specification: " + toDelimitedString(mapParts, " ")));
                }
            }
            currentIndex++;
        }
        return maps;
    }

    
    
    private boolean isACollectorConfiguration(String string)
    {
        if (string.equals("unique") || string.equals("first") || string.equals("sort"))
            return(true);
        return(false);
    }

    private MultiValueCollector createMultiValueCollector(List<List<String>> mapSpecs)
    {
        MultiValueCollector collector = new MultiValueCollector();
        for (List<String> mapSpec : mapSpecs)
        {
            String mapParts[] = mapSpec.toArray(new String[0]);
            if (isACollectorConfiguration(mapParts[0]))
            {    
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
        }
        return collector;
    }

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

}
