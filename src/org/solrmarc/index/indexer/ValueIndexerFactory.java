package org.solrmarc.index.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.marc4j.marc.Record;
import org.solrmarc.driver.Boot;
import org.solrmarc.index.collector.MultiValueCollector;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;
import org.solrmarc.index.extractor.AbstractSingleValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractor;
import org.solrmarc.index.extractor.AbstractValueExtractorFactory;
import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.extractor.impl.direct.ModifyableMultiValueExtractor;
import org.solrmarc.index.extractor.impl.java.JavaValueExtractorUtils;
import org.solrmarc.index.extractor.methodcall.MethodCallManager;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.AbstractValueMappingFactory;
import org.solrmarc.index.utils.ClasspathUtils;
import org.solrmarc.tools.SolrMarcIndexerException;
import org.solrmarc.tools.Utils;

public class ValueIndexerFactory
{
    private final static Logger logger = Logger.getLogger(ValueIndexerFactory.class);
    private List<AbstractValueExtractorFactory> extractorFactories;
    private List<AbstractValueMappingFactory> mappingFactories;
    private List<IndexerSpecException> validationExceptions;
    private ThreadLocal<Set<IndexerSpecException>> perRecordExceptions;

    private FullConditionalParser parser = null;
    private Properties localMappingProperties = null;
    private JavaValueExtractorUtils compileTool = null;
    private String[] homeDirStrs = null;
    private final Pattern specPattern = Pattern.compile("([-A-Za-z_0-9, \\t]*)([:=]|([+?|]=))(.*)");
    boolean debug_parse = true;
    private boolean defaultUniqueVal = true;
    private String defaultCustomClassname = null;
    private final Pattern defaultUniquePattern = Pattern.compile("default.unique[ ]*[;=][ ]*[\"]?(true|false)[\"]?");
    private final Pattern defaultCustomClassPattern = Pattern.compile("default.customClass[ ]*[;=][ ]*[\"]?([a-z][A-Za-z0-9.]+[a-zA-Z0-9])[\"]?");
    /**
     * The next three functions make the ValueIndexerFactory implement the
     * Singleton pattern To create of use a Factory do:
     * ValueIndexerFactory.instance()
     */
    private static ValueIndexerFactory theFactory = null;

    public static ValueIndexerFactory initialize(String[] homeDirStrs)
    {
        if (homeDirStrs == null)
        {
            homeDirStrs = new String[] { "." };
        }
        if ((theFactory != null) && (Arrays.equals(homeDirStrs, theFactory.homeDirStrs)))
        {
            return theFactory;
        }
        theFactory = new ValueIndexerFactory(homeDirStrs);
        try
        {
            theFactory.extractorFactories = theFactory.createExtractorFactories(ClasspathUtils.instance().getExtractorFactoryClasses());
            theFactory.mappingFactories = theFactory.createMappingFactories(ClasspathUtils.instance().getMappingFactoryClasses());
        }
        catch (IllegalAccessException | InstantiationException e)
        {
            throw new IndexerSpecException(e, "Error creating extractor or mapping factories");
        }
        return (theFactory);
    }

    public static ValueIndexerFactory instance()
    {
        return (theFactory);
    }

    public ValueIndexerFactory(String[] homeDirStrs)
    {
        this.homeDirStrs = homeDirStrs;
//        validationExceptions = new ArrayList<IndexerSpecException>();
        // perRecordExceptions list changed to be a ThreadLocal list so that each thread can save
        // the exceptions found while indexing a given record without interfering with other indexing threads.
        perRecordExceptions = new ThreadLocal<Set<IndexerSpecException>>()
        {
            @Override
            protected Set<IndexerSpecException> initialValue()
            {
                return new LinkedHashSet<>();
            }
        };
        List<String> dirsJavaSourceList = new ArrayList<String>();
        String[] dirsJavaSource;
        for (String dirStr : homeDirStrs)
        {
            File dir = new File(dirStr);
            File dirIndexJava = new File(dirStr, "index_java");
            File dirIndexJavaSrc = new File(dirIndexJava, "src");
            if (dirIndexJava.exists() && dirIndexJavaSrc.exists())
            {
                logger.info("Using directory: " + dirIndexJava.getAbsolutePath() + " as location of java sources");
                dirsJavaSourceList.add(dir.getAbsolutePath());
            }
        }
        dirsJavaSource =  dirsJavaSourceList.toArray(new String[0]);
        compileTool = new JavaValueExtractorUtils(dirsJavaSource);
        try { 
            compileTool.compileSources(false);
            compileTool.getClasses();
        }
        catch (java.lang.UnsupportedClassVersionError ucve)
        {
            compileTool.compileSources(true);
        }
    }

    public Class<?>[] getCompiledClasses()
    {
        return compileTool.getClasses();
    }

    /**
     * Return ALL of the exceptions encountered while processing indexing specification
     *
     * @return list of validation exceptions
     */
    public List<IndexerSpecException> getValidationExceptions()
    {
        return validationExceptions;
    }

    /**
     * Add an error to list of exceptions for the current record
     *
     * @param error  error to add
     */
    public void addPerRecordError(IndexerSpecException error)
    {
        perRecordExceptions.get().add(error);
    }

//    /**
//     * Return the mapping factories loaded above for use in the CUP parser
//     *
//     * @return
//     */
//    final private List<AbstractValueMappingFactory> getMappingFactories()
//    {
//        return mappingFactories;
//    }

    /**
     * Return the value defined as the default class name to use when resolving multiple matches for the same custom method
     *
     * @return default class name
     */
    public String getDefaultCustomClassname()
    {
        return defaultCustomClassname;
    }

    /**
     * Return the extractor factories loaded above for use in the CUP parser
     *
     * @return list of extractor factories
     */
    final List<AbstractValueExtractorFactory> getExtractorFactories()
    {
        return extractorFactories;
    }

    /**
     * Return ALL of the exceptions encountered while processing indexing specification
     *
     * @return exceptions
     */
    public Set<IndexerSpecException> getPerRecordErrors()
    {
        return perRecordExceptions.get();
    }

    /**
     * Clear any stored exceptions encountered while processing indexing specification
     */
    public void clearPerRecordErrors()
    {
        perRecordExceptions.get().clear();
    }

    public Properties getLocalMappingProperties()
    {
        return localMappingProperties;
    }

//    private boolean isDebugParse()
//    {
//        return debug_parse;
//    }
//
//    private void setDebugParse(boolean debug_parse)
//    {
//        this.debug_parse = debug_parse;
//    }
//
    public List<AbstractValueIndexer<?>> createValueIndexers(File[] indexSpecFiles) throws IllegalAccessException, InstantiationException, IOException
    {
        localMappingProperties = new Properties();

        Map<String, List<AbstractValueIndexer<?>>> valueIndexerMap = new LinkedHashMap<>();
        Map<String, List<IndexerSpecException>>    valueIndexerExceptions = new LinkedHashMap<>();

        createValueIndexers(indexSpecFiles, valueIndexerMap, valueIndexerExceptions);

        List<AbstractValueIndexer<?>> valueIndexers = collapseMapToList(valueIndexerMap);
        validationExceptions = collapseExceptionsMaptoList(valueIndexerExceptions);
        return valueIndexers;
    }

//    private List<AbstractValueIndexer<?>> createValueIndexers(File indexSpecFile) throws IllegalAccessException, InstantiationException, IOException
//    {
//        validationExceptions.clear();
//        localMappingProperties = new Properties();
//
//        Map<String, List<AbstractValueIndexer<?>>> valueIndexerMap = new LinkedHashMap<>();
//
//        createValueIndexers(indexSpecFile, valueIndexerMap);
//
//        List<AbstractValueIndexer<?>> valueIndexers = collapseMapToList(valueIndexerMap);
//
//        return valueIndexers;
//    }

    public List<AbstractValueIndexer<?>> createValueIndexers(String[] configSpecs) throws IllegalAccessException, InstantiationException
    {
        localMappingProperties = new Properties();

        Map<String, List<AbstractValueIndexer<?>>> valueIndexerMap = new LinkedHashMap<>();
        Map<String, List<IndexerSpecException>>    valueIndexerExceptions = new LinkedHashMap<>();

        createValueIndexers(configSpecs, valueIndexerMap, valueIndexerExceptions);

        List<AbstractValueIndexer<?>> valueIndexers = collapseMapToList(valueIndexerMap);
        validationExceptions = collapseExceptionsMaptoList(valueIndexerExceptions);

        return valueIndexers;
    }

    private List<AbstractValueIndexer<?>> collapseMapToList(Map<String, List<AbstractValueIndexer<?>>> valueIndexerMap)
    {
        List<AbstractValueIndexer<?>> valueIndexers = new ArrayList<AbstractValueIndexer<?>>();
        for (List<AbstractValueIndexer<?>> indexer : valueIndexerMap.values())
        {
            valueIndexers.addAll(indexer);
        }
        return valueIndexers;
    }

    private List<IndexerSpecException> collapseExceptionsMaptoList(Map<String, List<IndexerSpecException>> valueIndexerExceptions)
    {
        List<IndexerSpecException> valueIndexers = new ArrayList<IndexerSpecException>();
        for (List<IndexerSpecException> indexer : valueIndexerExceptions.values())
        {
            valueIndexers.addAll(indexer);
        }
        return valueIndexers;
    }

    private void createValueIndexers(File[] indexSpecFiles, Map<String, List<AbstractValueIndexer<?>>> valueIndexerMap, 
                                     Map<String, List<IndexerSpecException>> valueIndexerExceptions) throws IllegalAccessException, InstantiationException, IOException
    {
        for (File indexSpecFile : indexSpecFiles)
        {
            createValueIndexers(indexSpecFile, valueIndexerMap, valueIndexerExceptions);
        }
    }

    private void createValueIndexers(File indexSpecFile, Map<String, List<AbstractValueIndexer<?>>> valueIndexerMap, 
                                    Map<String, List<IndexerSpecException>>valueIndexerExceptions) throws IllegalAccessException, InstantiationException, IOException
    {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(indexSpecFile));
        String line;
        String saveLine = "";

        while ((line = reader.readLine()) != null)
        {
            if (saveLine.length() > 0 && line.matches("^[ \t].*"))
            {
                line = saveLine + line;
                saveLine = "";
            }
            if (line.matches(".*,[ \t]*$"))
            {
                saveLine = line;
            }
            else
            {
                lines.add(line);
            }
        }
        if (saveLine.length() > 0)
        {
            lines.add(saveLine);
        }
        createValueIndexers(lines.toArray(new String[0]), valueIndexerMap, valueIndexerExceptions);
        reader.close();
    }

    private void createValueIndexers(String[] configSpecs, Map<String, List<AbstractValueIndexer<?>>> valueIndexerMap,  
                                    Map<String, List<IndexerSpecException>> valueIndexerExceptions) throws IllegalAccessException, InstantiationException
    {
        this.defaultCustomClassname = null;
        for (final String singleSpec : configSpecs)
        {
            if (singleSpec.startsWith("map.") || singleSpec.startsWith("pattern_map."))
            {
                final String[] specParts = singleSpec.split("[ ]?[:=][ ]?", 2);
                specParts[1] = specParts[1].replaceAll("\\\\(.)", "$1");
                localMappingProperties.put(specParts[0].trim(), specParts[1].trim());
            }
        }
        for (String singleSpec : configSpecs)
        {
            singleSpec = singleSpec.trim();
            if (singleSpec.startsWith("#") || (!singleSpec.contains(":") && !singleSpec.contains("="))) continue;
            if (singleSpec.startsWith("map.") || singleSpec.startsWith("pattern_map.")) continue;
            if (singleSpec.startsWith("default"))
            {
                if (singleSpec.startsWith("default.unique"))
                {
                    Matcher defUnique = defaultUniquePattern.matcher(singleSpec);
                    if (defUnique.matches())
                    {
                        this.defaultUniqueVal = Boolean.parseBoolean(defUnique.group(1));
                    }
                }
                else if (singleSpec.startsWith("default.customClass"))
                {
                    Matcher defClass = defaultCustomClassPattern.matcher(singleSpec);
                    if (defClass.matches())
                    {
                        this.defaultCustomClassname = defClass.group(1);
                    }
                }
            }

            Matcher match = specPattern.matcher(singleSpec);
            if (match.matches())
            {
                final String solrFieldName = match.group(1).replaceAll("[ \\t]", "");
                final String delimiter = match.group(2);
                final String mappingDefinition = match.group(4);
                try
                {
                    MultiValueIndexer valueIndexer = createValueIndexer(solrFieldName, mappingDefinition);
                    List<IndexerSpecException> exceptions;
                    if (delimiter.startsWith("+") && valueIndexerExceptions.containsKey(solrFieldName))
                    {
                        exceptions = valueIndexerExceptions.get(solrFieldName);
                    }
                    else
                    {
                        exceptions = new ArrayList<>();
                    }
                    if (valueIndexer != null)
                    {
                        List<AbstractValueIndexer<?>> indexerList;
                        if ((delimiter.startsWith("+") || delimiter.startsWith("|") || delimiter.startsWith("?")) && valueIndexerMap.containsKey(solrFieldName))
                        {
                            indexerList = valueIndexerMap.get(solrFieldName);
                        }
                        else
                        {
                            indexerList = new ArrayList<>();
                        }
                        if (delimiter.startsWith("?")) 
                        {
                            valueIndexer.setIfEmpty();
                        }
                        if (delimiter.startsWith("|")) 
                        {
                            valueIndexer.setIfUnique();
                        }
                        indexerList.add(valueIndexer);
                        valueIndexerMap.put(solrFieldName, indexerList);
                    }
                    for (IndexerSpecException ise : FullConditionalParser.getErrors())
                    {
                        ise.setSolrFieldAndSpec(solrFieldName, mappingDefinition);
                        exceptions.add(ise);
                    }
                    valueIndexerExceptions.put(solrFieldName, exceptions);
                }
                catch (IndexerSpecException ise)
                {
                    ise.setSolrFieldAndSpec(solrFieldName, mappingDefinition);
                    valueIndexerExceptions.put(solrFieldName, Collections.singletonList(ise));
                }
            }
        }
    }

    public MultiValueIndexer createValueIndexer(String fieldNames, String indexSpec)
    {
        if (parser == null)
        {
            try
            {
                parser = new FullConditionalParser(debug_parse);
                parser.setFactories(this);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InstantiationException e)
            {
                e.printStackTrace();
            }
        }
        logger.trace("Processing spec: " + indexSpec);
        MultiValueIndexer valueIndexer = parser.parse(fieldNames, indexSpec);
        // Test fire the indexer to catch obvious error such as missing property
        // files
        if (valueIndexer != null)
        {
            boolean testFileMethod = Boolean.parseBoolean(System.getProperty("solrmarc.indexer.test.fire.method", "false"));
            if (testFileMethod)
            {
                try
                {
                    logger.trace("Test firing spec: " + indexSpec);
                    valueIndexer.getFieldData(org.solrmarc.index.extractor.methodcall.StaticMarcTestRecords.testRecord[0]);
                }
                catch (SolrMarcIndexerException smie)
                {
                    // user generated exception, ignore it
                }
                catch (InvocationTargetException ite)
                {
                    throw new IndexerSpecException(ite.getTargetException(), "Error on test invocation of custom method: " + indexSpec);
                }
                catch (Exception e)
                {
                    try
                    {
                        Class<?> targetErrorClazz = Boot.classForName("bsh.TargetError");
                        if (targetErrorClazz.isInstance(e))
                        {
                            throw new IndexerSpecException(e, "Error on test invocation of custom script method: " + indexSpec);
                        }
                    }
                    catch (ClassNotFoundException localClassNotFoundException)
                    {
                    }
                    throw new IndexerSpecException(e, "Error on test invocation of custom method: " + indexSpec);
                }
            }
        }
        return (valueIndexer);
    }

    public static AbstractValueIndexer<?> makeThreadSafeCopy(AbstractValueIndexer<?> toClone)
    {
        String solrFieldNamesStr = toClone.getSolrFieldNamesStr();
        String specLabel = toClone.getSpecLabel();
        AbstractMultiValueExtractor extractor;
        MultiValueCollector collector = toClone.collector;
        AtomicLong totalElapsedTime = toClone.totalElapsedTime;
        AbstractMultiValueMapping[] mappings = new AbstractMultiValueMapping[toClone.mappings.length];
        for (int i = 0; i < toClone.mappings.length; i++)
        {
            if (((toClone.mappings[i] instanceof ExternalMethod)) && (!((ExternalMethod) toClone.mappings[i]).isThreadSafe()))
            {
                mappings[i] = ((AbstractMultiValueMapping) ((ExternalMethod) toClone.mappings[i]).makeThreadSafeCopy());
            }
            else
            {
                mappings[i] = ((AbstractMultiValueMapping) toClone.mappings[i]);
            }
        }
        if (((toClone.extractor instanceof ExternalMethod)) && (!((ExternalMethod) toClone.extractor).isThreadSafe()))
        {
            extractor = (AbstractMultiValueExtractor) ((ExternalMethod) toClone.extractor).makeThreadSafeCopy();
        }
        else
        {
            extractor = (AbstractMultiValueExtractor) toClone.extractor;
        }
        MultiValueIndexer result = new MultiValueIndexer(solrFieldNamesStr, extractor, mappings, collector, specLabel, totalElapsedTime);
        if (toClone.getOnlyIfEmpty())  result.setIfEmpty();
        if (toClone.getOnlyIfUnique())  result.setIfUnique();
        return result;
    }

    private List<AbstractValueExtractorFactory> createExtractorFactories(final Set<Class<? extends AbstractValueExtractorFactory>> factoryClasses) throws IllegalAccessException, InstantiationException
    {
        final List<AbstractValueExtractorFactory> factories = new ArrayList<>(factoryClasses.size());
        for (final Class<? extends AbstractValueExtractorFactory> extractorFactoryClass : factoryClasses)
        {
            if (!Modifier.isAbstract(extractorFactoryClass.getModifiers()))
            {
                logger.trace("Create value extractor factory for " + extractorFactoryClass);
                try {
                    AbstractValueExtractorFactory factory = (AbstractValueExtractorFactory) extractorFactoryClass.newInstance();
                    factories.add(factory);
                }
                catch (ClassCastException cce) {
                }
            }
        }
        return factories;
    }

    private List<AbstractValueMappingFactory> createMappingFactories(final Set<Class<? extends AbstractValueMappingFactory>> factoryClasses) throws IllegalAccessException, InstantiationException
    {
        final List<AbstractValueMappingFactory> factories = new ArrayList<>(factoryClasses.size());
        for (final Class<? extends AbstractValueMappingFactory> extractorFactoryClass : factoryClasses)
        {
            logger.trace("Create value mapping factory for  s " + extractorFactoryClass);
            try {
                AbstractValueMappingFactory factory = (AbstractValueMappingFactory) extractorFactoryClass.newInstance();
                factories.add(factory);
            }
            catch (ClassCastException cce) {
            }

        }
        return factories;
    }

    /**
     * Creates an indexer representing the indexer process for one (or more)
     * solr field (given by fieldnames), Where the extractor has already been
     * created in the parser handling code, and the maps definitions following
     * the extractor have been pulled out as a list of lists of strings.
     *
     * @param origSpec
     *            the original index specification (for debugging purposes).
     * @param solrFieldName
     *            the name (or names) of the solr field(s) this spec should
     *            generate.
     * @param extractor
     *            the extractor object created by the parser handling code (or
     *            perhaps one manually created)
     * @param mapSpecs
     *            the mapping/collector specifications definition for this
     *            indexer.
     * @return an indexer representing the indexer process for one solr field.
     */
    AbstractValueIndexer<?> makeMultiValueIndexer(String origSpec, String fieldnames, AbstractValueExtractor<?> extractor, List<List<String>> mapSpecs, List<IndexerSpecException> currentExceptions)
    {
        if (mapSpecs == null)
        {
            mapSpecs = new ArrayList<List<String>>();
        }
        if (extractor instanceof ModifyableMultiValueExtractor)
        {
            int indexOfJoin = decorateMultiValueExtractor(origSpec, fieldnames, (ModifyableMultiValueExtractor)extractor, mapSpecs, currentExceptions);
            final List<AbstractMultiValueMapping> mappings;
            if (indexOfJoin != -1)
            {
                mappings = createMultiValueMappings(origSpec, mapSpecs, indexOfJoin, currentExceptions);
            }
            else
            {
                mappings = new ArrayList<AbstractMultiValueMapping>();
            }
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs, true);

            return new MultiValueIndexer(fieldnames, (AbstractMultiValueExtractor)extractor, mappings, collector);
        }
        else if (extractor instanceof AbstractMultiValueExtractor)
        {
            final AbstractMultiValueExtractor multiValueExtractor = (AbstractMultiValueExtractor) extractor;
            final List<AbstractMultiValueMapping> mappings = createMultiValueMappings(origSpec, mapSpecs, currentExceptions);
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs);
            return new MultiValueIndexer(fieldnames, multiValueExtractor, mappings, collector);
        }
        else if (extractor instanceof AbstractSingleValueExtractor)
        {
            final AbstractSingleValueExtractor singleValueExtractor = (AbstractSingleValueExtractor) extractor;
            final List<AbstractMultiValueMapping> mappings = createMultiValueMappings(origSpec, mapSpecs, currentExceptions);
            final MultiValueCollector collector = createMultiValueCollector(mapSpecs);
            return new MultiValueIndexer(fieldnames, singleValueExtractor, mappings, collector);
        }
        else if (extractor == null)
        {
            throw new IllegalArgumentException("Extractor is null, most likely there was an error parsing the index specification: " + origSpec);
        }
        throw new IllegalArgumentException("Only subclasses of AbstractMultiValueExtractor or AbstractSingleValueExtractor are allowed, but not " + extractor.getClass().getName());
    }

    boolean isADecoratorConfiguration(String str)
    {
        if (str.equals("join") || str.equals("separate") || str.equals("format") || str.equals("substring") ||
            str.equals("cleanEach") || str.equals("cleanEnd") || str.equals("clean") || str.equals("stripAccent") ||
            str.equals("stripPunct") || str.equals("stripInd1") ||str.equals("stripInd2") || str.equals("stripInd") || 
            str.equals("toUpper") || str.equals("toLower") || str.equals("titleSortUpper") || str.equals("titleSortLower") ||
            str.equals("untrimmed") || str.equals("toTitleCase")) return (true);
        return (false);
    }

    private int decorateMultiValueExtractor(String origSpec, String fieldnames, ModifyableMultiValueExtractor multiValueExtractor, List<List<String>> mapSpecs, List<IndexerSpecException> currentExceptions)
    {
        if (mapSpecs.size() == 0)
        {
            return -1;
        }

        int currentIndex = 0;
        int joinIndex = -1;
        for (List<String> mapSpec : mapSpecs)
        {
            String[] mapParts = (String[]) mapSpec.toArray(new String[0]);
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
                    multiValueExtractor.setFormatPatterns(mapParts);
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
                        multiValueExtractor.setSubstring(mapParts[1], "toEnd");
                    }
                }
                catch (IndexerSpecException ise)
                {
                    ise.setSolrFieldAndSpec(fieldnames, origSpec);
                    currentExceptions.add(ise);
                }
            }
            else if (mapParts[0].equals("cleanEach"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.CLEAN_EACH);
            }
            else if (mapParts[0].equals("untrimmed"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.UNTRIMMED);
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
            else if (mapParts[0].equals("stripInd1"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.STRIP_INDICATOR_1);
            }
            else if (mapParts[0].equals("stripInd2"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.STRIP_INDICATOR_2);
            }
            else if (mapParts[0].equals("stripInd"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.STRIP_INDICATOR);
            }
            else if (mapParts[0].equals("toUpper"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.TO_UPPER);
            }
            else if (mapParts[0].equals("toLower"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.TO_LOWER);
            }
            else if (mapParts[0].equals("toTitleCase"))
            {
                multiValueExtractor.addCleanVal(eCleanVal.TO_TITLECASE);
            }
            else if (mapParts[0].equals("titleSortUpper"))
            {
                multiValueExtractor.setCleanVal(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.CLEAN_END, eCleanVal.STRIP_ACCCENTS, eCleanVal.STRIP_ALL_PUNCT, eCleanVal.STRIP_INDICATOR));
                multiValueExtractor.addCleanVal(eCleanVal.TO_UPPER);
            }
            else if (mapParts[0].equals("titleSortLower"))
            {
                multiValueExtractor.setCleanVal(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.CLEAN_END, eCleanVal.STRIP_ACCCENTS, eCleanVal.STRIP_ALL_PUNCT, eCleanVal.STRIP_INDICATOR));
                multiValueExtractor.addCleanVal(eCleanVal.TO_LOWER);
            }
            else if (isAValueMappingConfiguration(mapParts[0]) && joinIndex == -1)
            {
                AbstractMultiValueMapping valueMapping = createMultiValueMapping(mapParts);
                multiValueExtractor.addMap(valueMapping);
            }
            else if (isAValueMappingConfiguration(mapParts[0]) && joinIndex != -1)
            {
                // post join map specification
                //     mapping specs before "join" are applied before the join operation
                //     mapping spec that are after "join" are applied to the joined output.
                //     and are handled elsewhere.
            }
            else
            {
                currentExceptions.add(new IndexerSpecException(fieldnames, origSpec, "Illegal format specification: " + Utils.join(mapParts, " ")));
            }
            currentIndex++;
        }
        return (joinIndex);
    }

    private boolean isAValueMappingConfiguration(final String configuration)
    {
        for (final AbstractValueMappingFactory mappingFactory : mappingFactories)
        {
            if (mappingFactory.canHandle(configuration))
            {
                return true;
            }
        }
        return (false);
    }

    private List<AbstractMultiValueMapping> createMultiValueMappings(String origSpec, List<List<String>> mapSpecs, List<IndexerSpecException> currentExceptions)
    {
        return (createMultiValueMappings(origSpec, mapSpecs, -1, currentExceptions));
    }

    /**
     * Creates the multivalue mappers as specified to be appended to a
     * extractor. (given by fieldnames), Where the extractor has already been
     * created in the parser handling code, and the maps definitions following
     * the extractor have been pulled out as a list of lists of strings.
     *
     * @param origSpec
     *            the original index specification (for debugging purposes).
     * @param mapSpecs
     *            the mapping/collector specifications definition for this
     *            indexer.
     * @param indexOfJoin
     *            index within mapSpecs where the "join" specification occurs.
     *            mapping specs before "join" are applied before the join
     *            operation mapping spec that are after "join" are applied to
     *            the joined output.
     * @return an list of multi value mappings
     */
    private List<AbstractMultiValueMapping> createMultiValueMappings(String origSpec, List<List<String>> mapSpecs, int indexOfJoin, List<IndexerSpecException> currentExceptions)
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
                String[] mapParts = mapSpec.toArray(new String[0]);
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
                    currentExceptions.add(new IndexerSpecException(origSpec, "Illegal format specification: " + Utils.join(mapParts, " ")));
                }
            }
            currentIndex++;
        }
        return maps;
    }

    private boolean isACollectorConfiguration(String string)
    {
        if (string.equals("unique") || string.equals("first") || string.equals("sort") || string.equals("notunique") ||
            string.equals("notfirst") || string.equals("all") || string.equals("DeleteRecordIfFieldEmpty") ||
            string.equals("DeleteRecordIfFieldNotEmpty") || string.equals("SkipRecordIfFieldEmpty") || string.equals("SkipRecordIfFieldNotEmpty") ||
            string.equals("normalize") || string.equals("unnormalize"))
            return (true);
        return (false);
    }

//    private MultiValueCollector createMultiValueCollector()
//    {
//        MultiValueCollector collector = new MultiValueCollector();
//        collector.setUnique(this.defaultUniqueVal);
//        return (collector);
//    }

    private MultiValueCollector createMultiValueCollector(List<List<String>> mapSpecs, boolean setDefaultValForUnique)
    {
        MultiValueCollector collector = new MultiValueCollector();
        if (setDefaultValForUnique)
        {
            collector.setUnique(this.defaultUniqueVal);
        }
        for (List<String> mapSpec : mapSpecs)
        {
            String[] mapParts = mapSpec.toArray(new String[0]);
            if (isACollectorConfiguration(mapParts[0]))
            {
                if (mapParts[0].equals("unique"))
                {
                    collector.setUnique(true);
                }
                else if (mapParts[0].equals("notunique"))
                {
                    collector.setUnique(false);
                }
                else if (mapParts[0].equals("first"))
                {
                    collector.setFirst(mapParts[0]);
                }
                else if (mapParts[0].equals("notfirst"))
                {
                    collector.setFirst(mapParts[0]);
                }
                else if (mapParts[0].equals("normalize"))
                {
                    collector.setNormalize("C");
                }
                else if (mapParts[0].equals("unnormalize"))
                {
                    collector.setNormalize("D");
                }
                else if (mapParts[0].equals("all"))
                {
                    collector.setFirst(mapParts[0]);
                }
                else if (mapParts[0].equals("sort"))
                {
                    collector.setSortComparator(mapParts[1], mapParts[2]);
                }
                else if (mapParts[0].equals("DeleteRecordIfFieldEmpty"))
                {
                    collector.setIfFieldEmpty(MultiValueCollector.eAction.DELETE);
                }
                else if (mapParts[0].equals("DeleteRecordIfFieldNotEmpty"))
                {
                    collector.setIfFieldNotEmpty(MultiValueCollector.eAction.DELETE);
                }
                else if (mapParts[0].equals("SkipRecordIfFieldEmpty"))
                {
                    collector.setIfFieldEmpty(MultiValueCollector.eAction.SKIP);
                }
                else if (mapParts[0].equals("SkipRecordIfFieldNotEmpty"))
                {
                    collector.setIfFieldNotEmpty(MultiValueCollector.eAction.SKIP);
                }
            }
        }
        return collector;
    }

    private MultiValueCollector createMultiValueCollector(List<List<String>> mapSpecs)
    {
        return (createMultiValueCollector(mapSpecs, false));
    }

    // currently used by SolrIndexer methods that provide backwards-compatibility
    public AbstractMultiValueMapping createMultiValueMapping(final String mappingConfig)
    {
        for (final AbstractValueMappingFactory mappingFactory : mappingFactories)
        {
            if (mappingFactory.canHandle(mappingConfig))
            {
                return mappingFactory.createMultiValueMapping(mappingConfig);
            }
        }
        throw new IndexerSpecException("Could not handle impl: " + mappingConfig + "\nLoaded impl factories:\n" + mappingFactories.toString().replaceAll(",", ",\n"));
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
        throw new IndexerSpecException("Could not handle map descriptor: " + Utils.join(mapParts, " "));
    }

    public String[] getHomeDirs()
    {
        return (homeDirStrs);
    }

    public void doneWithRecord(Record record)
    {
        MethodCallManager.instance().doneWithRecord(record);
    }
}
