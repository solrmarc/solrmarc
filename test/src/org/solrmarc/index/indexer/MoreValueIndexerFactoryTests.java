package org.solrmarc.index.indexer;


import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.MultiValueIndexer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;


public class MoreValueIndexerFactoryTests
{
    private Record testRecord;
    private final static String inputfilename="./test/data/records/u5278992.mrc";
    private static ValueIndexerFactory factory;
    static
    {
        factory = ValueIndexerFactory.initialize(new String[]{System.getProperty("test.data.dir", "test/data")});
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

        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("linked_title_facet", "LNK245ab, join( : ), cleanEach");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("linked_title_facet", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        Object linked_title_facet = indexer.getFieldData(testRecord);
        
    }
}
