package org.solrmarc.index.indexer;


import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.MultiValueIndexer;
import org.solrmarc.index.indexer.ValueIndexerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;


public class MoreValueIndexerFactoryTests
{
    private Record testRecord;
    private final static String inputfilename="./records/u5278992.mrc";

    static
    {
        PropertyConfigurator.configure(new File("log4j.properties").getAbsolutePath());
    }

    @Before
    public void setup() throws FileNotFoundException
    {
        
        InputStream input = new BufferedInputStream(new FileInputStream(inputfilename));
        MarcReader reader = new MarcPermissiveStreamReader(input, true, true);
        testRecord = reader.next();
        testRecord.setId(1L);

    }    
    
    //  "LNK245ab"
    @Test
    public void testFieldSpecMultiFieldIndexer() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("linked_title_facet", "LNK245ab, join( : ), cleanEach");

        final AbstractValueIndexer<?> valueIndexer = createIndexer("linked_title_facet", "LNK245ab, join( : ), cleanEach");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("linked_title_facet", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        Object linked_title_facet = indexer.getFieldData(testRecord);
        
    }


    private AbstractValueIndexer<?> createIndexer(String fieldNames, String indexSpec) throws IllegalAccessException, InstantiationException
    {
        final ValueIndexerFactory factory = ValueIndexerFactory.initialize(new String[]{System.getProperty("test.data.dir")});
        return factory.createValueIndexer(fieldNames, indexSpec);
    }
}
