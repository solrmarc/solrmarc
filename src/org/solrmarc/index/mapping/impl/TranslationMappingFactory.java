package org.solrmarc.index.mapping.impl;

import org.apache.log4j.Logger;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.AbstractValueMappingFactory;
import org.solrmarc.tools.PropertyUtils;

import java.io.File;
import java.util.HashMap;
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
        if (translationMappingFileName.equals("(this).properties"))
        {
            Properties props = (ValueIndexerFactory.instance().getLocalMappingProperties());
            props = getSubTranslationMapping(props, subMappingName);
            return props;
        }
        Properties properties = translationMappingFiles.get(translationMappingFileName + "(" + subMappingName + ")");
        if (properties != null)
        {
            return properties;
        }
        properties = translationMappingFiles.get(translationMappingFileName + "(null)");
        if (properties != null)
        {
            properties = getSubTranslationMapping(properties, subMappingName);
            translationMappingFiles.put(translationMappingFileName + "(" + subMappingName + ")", properties);
            return properties;
        }
        String[] filenameOpened = new String[]{null};
        properties = PropertyUtils.loadProperties(ValueIndexerFactory.instance().getHomeDirs(), "translation_maps" + File.separator + translationMappingFileName, filenameOpened);
        logger.debug("Load translation map: "+ translationMappingFileName+ " from file : " +filenameOpened[0]);
        translationMappingFiles.put(translationMappingFileName + "(null)", properties);
        if (subMappingName != null)
        {
            properties = getSubTranslationMapping(properties, subMappingName);
            translationMappingFiles.put(translationMappingFileName + "(" + subMappingName + ")", properties);
        }
        return properties;
    }
    
    private Properties getSubTranslationMapping(Properties translationMapping, String mappingPrefix)
    {
        Properties mappings = new Properties();
        for (String key : translationMapping.stringPropertyNames())
        {
            if (key.startsWith(mappingPrefix+"."))
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

    private String getMappingConfigurationName(String[] mapParts)
    {
        String configuration = mapParts[0] + (mapParts.length > 1 ? "(" + mapParts[1] + ")" : "");
        return configuration;
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
            return PatternMappingFactory.patternMappingsFromPatternProperties(mappingConfiguration, translationMapping);
        }
        else
        {
            return new MultiValueTranslationMapping(mappingConfiguration, translationMapping);
        }
    }

    @Override
    public AbstractMultiValueMapping createMultiValueMapping(String[] mapParts)
    {
        String translationMappingFileName = getTranslationMappingFileName(mapParts[0]);
        final String subMappingName = mapParts.length > 1 ? mapParts[1] : null;
        Properties translationMapping = loadTranslationMappingFile(translationMappingFileName, subMappingName);
        String mappingConfiguration = getMappingConfigurationName(mapParts);
        if (translationMapping.containsKey("pattern_0"))
        {
            return PatternMappingFactory.patternMappingsFromPatternProperties(mappingConfiguration, translationMapping);
        }
        else
        {
            return new MultiValueTranslationMapping(mappingConfiguration, translationMapping);
        }
    }
}
