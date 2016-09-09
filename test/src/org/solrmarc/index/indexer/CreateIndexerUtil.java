package org.solrmarc.index.indexer;

import java.util.List;

public class CreateIndexerUtil
{
    public static AbstractValueIndexer<?> createIndexer(String fieldNames, String indexSpec) throws IllegalAccessException, InstantiationException
    {
        final ValueIndexerFactory factory = ValueIndexerFactory.initialize(new String[]{System.getProperty("test.data.dir")});
        return factory.createValueIndexer(fieldNames, indexSpec);
    }

    public static List<AbstractValueIndexer<?>> createIndexers(String[] configSpecs) throws IllegalAccessException, InstantiationException
    {
        final ValueIndexerFactory factory = ValueIndexerFactory.initialize(new String[]{System.getProperty("test.data.dir")});
        return factory.createValueIndexers(configSpecs);
    }
}