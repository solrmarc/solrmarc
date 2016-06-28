package org.solrmarc.index.mapping.impl;

import org.apache.log4j.Logger;
import org.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.AbstractValueMappingFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TranslationMappingFactory extends AbstractValueMappingFactory
{
    private final static Logger logger = Logger.getLogger(TranslationMappingFactory.class);
    private final static Map<String, Properties> translationMappingFiles = new HashMap<>();

    @Override
    public boolean canHandle(String mappingConfiguration)
    {
        return mappingConfiguration.endsWith(".properties")
                || (mappingConfiguration.contains(".properties(") && mappingConfiguration.endsWith(")"));
    }

    private Properties loadTranslationMappingFile(String translationMappingFileName, String subMappingName)
    {
        Properties properties = translationMappingFiles.get(translationMappingFileName + "(" + subMappingName + ")");
        if (properties != null)
        {
            return properties;
        }
        if (translationMappingFileName.equals("(this).properties"))
        {
            properties = ValueIndexerFactory.instance().getLocalMappingProperties();
        }
        else
        {
            properties = translationMappingFiles.get(translationMappingFileName + "(null)");
        }
        if (properties != null)
        {
            properties = getSubTranslationMapping(properties, subMappingName);
            translationMappingFiles.put(translationMappingFileName + "(" + subMappingName + ")", properties);
            return properties;
        }
        try
        {
            properties = new Properties();
            logger.debug("Load translation map: ./translation_maps/" + translationMappingFileName);
         //   File file = new File(".");
         //   logger.debug("current Directory = " + file.getAbsolutePath());
            String filename = ValueIndexerFactory.instance().getHomeDir() + "/translation_maps/" + translationMappingFileName;
            FileInputStream inputStream = new FileInputStream(filename);
            properties.load(inputStream);
            translationMappingFiles.put(translationMappingFileName + "(null)", properties);
            if (subMappingName != null)
            {
                properties = getSubTranslationMapping(properties, subMappingName);
                translationMappingFiles.put(translationMappingFileName + "(" + subMappingName + ")", properties);
            }
            return properties;
        }
        catch (IOException e)
        {
            throw new IndexerSpecException(e, "Cant find translation map: " +translationMappingFileName);
        }
    }
    
    private Properties getSubTranslationMapping(Properties translationMapping, String mappingPrefix)
    {
        Properties mappings = new Properties();
        for (String key : translationMapping.stringPropertyNames())
        {
            if (key.startsWith(mappingPrefix))
            {
                String value = translationMapping.getProperty(key);
//                if (value.equals("null"))
//                {
//                    value = null;
//                }
                if (key.length() == mappingPrefix.length())
                {
                    // remove prefix. There is no period.
                    mappings.setProperty("", value);
                }
                else
                {
                    // remove prefix and period.
                    mappings.setProperty(key.substring(mappingPrefix.length() + 1), value);
                }
            }
        }
        return mappings;
    }

    private String getTranslationMappingFileName(String mappingConfiguration)
    {
        if (mappingConfiguration.equals("(this).properties"))
            return(mappingConfiguration);
        int index = mappingConfiguration.indexOf('(');
        if (index != -1)
        {
            return mappingConfiguration.substring(0, index);
        }
        else
        {
            return mappingConfiguration;
        }
    }

    private String getSubMappingName(String mappingConfiguration)
    {
        int index = mappingConfiguration.indexOf('(');
        if (index != -1)
        {
            return mappingConfiguration.substring(index + 1, mappingConfiguration.length() - 1);
        }
        else
        {
            return null;
        }
    }

    // @Override
    // public AbstractSingleValueMapping createSingleValueMapping(String
    // mappingConfiguration) {
    // final String translationMappingFileName =
    // getTranslationMappingFileName(mappingConfiguration);
    // final String subMappingName = getSubMappingName(mappingConfiguration);
    // final Properties translationMapping =
    // loadTranslationMappingFile(translationMappingFileName, subMappingName);
    // return new SingleValueTranslationMapping(translationMapping);
    // }

    @Override
    public AbstractMultiValueMapping createMultiValueMapping(String mappingConfiguration)
    {
        String translationMappingFileName = getTranslationMappingFileName(mappingConfiguration);
        final String subMappingName = getSubMappingName(mappingConfiguration);
        Properties translationMapping = loadTranslationMappingFile(translationMappingFileName, subMappingName);
        if (translationMapping.containsKey("pattern_0"))
        {
            return PatternMappingFactory.pattermMappingsFromPatternProperties(translationMapping);
        }
        else
        {
            return new MultiValueTranslationMapping(translationMapping);
        }
    }

    @Override
    public AbstractMultiValueMapping createMultiValueMapping(String[] mapParts)
    {
        String translationMappingFileName = getTranslationMappingFileName(mapParts[0]);
        final String subMappingName = mapParts.length > 1 ? mapParts[1] : null;
        Properties translationMapping = loadTranslationMappingFile(translationMappingFileName, subMappingName);
        if (translationMapping.containsKey("pattern_0"))
        {
            return PatternMappingFactory.pattermMappingsFromPatternProperties(translationMapping);
        }
        else
        {
            return new MultiValueTranslationMapping(translationMapping);
        }
    }
}
