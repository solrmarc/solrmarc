package org.solrmarc.index.indexer;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.MultiValueIndexer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ValueIndexerFactoryTests
{
    private Record testRecord;
    private final static String inputfilename="./records/u233.mrc";

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

    @Test
    public void testEmptyConfiguration() throws IllegalAccessException, InstantiationException
    {
        String[] configSpecs = { };
        final List<AbstractValueIndexer<?>> valueIndexers = CreateIndexerUtil.createIndexers(configSpecs);
        assertEquals(0, valueIndexers.size());
    }

    @Test
    public void testConstantIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = CreateIndexerUtil.createIndexer("constant", "\"Test constant\"");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("constant", indexer.getSolrFieldNames().iterator().next());
        assertEquals("Test constant", indexer.getFieldData(null).iterator().next());
    }

    @Test
    public void testFullRecordIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = CreateIndexerUtil.createIndexer("fullRecord", "text");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("fullRecord", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        String xml = indexer.getFieldData(testRecord).iterator().next().toString();
        
    }

    @Test
    public void testFieldSpecDataFieldIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = CreateIndexerUtil.createIndexer("title_fields", "245abnp, clean");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("title_fields", indexer.getSolrFieldNames().iterator().next());
        Collection<String> result = indexer.getFieldData(testRecord);
        assertEquals(1, result.size());
        String value = result.iterator().next();
        assertEquals("Lascaux en Périgord noir : environnement, art pariétal et conservation", value);
        
    }
    
    @Test
    public void testFieldSpecControlFieldIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = CreateIndexerUtil.createIndexer("id", "001");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("id", indexer.getSolrFieldNames().iterator().next());
        Collection<String> result = indexer.getFieldData(testRecord);
        assertEquals(1, result.size());
        assertEquals("u233", result.iterator().next()); 
    }
    
    
    //  "008[35-37]:041a:041d"
    @Test
    public void testFieldSpecMultiFieldIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = CreateIndexerUtil.createIndexer("language_facet", "008[35-37]:041a:041d, language_map.properties, unique");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("language_facet", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        Object id = indexer.getFieldData(testRecord);
        
    }

    @Test
    public void testMixinIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = CreateIndexerUtil.createIndexer("mixin", "custom, testMixinMethod");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("mixin", indexer.getSolrFieldNames().iterator().next());
        assertEquals("<null>", indexer.getFieldData(null).iterator().next());
    }

    
    @Test(expected = NullPointerException.class)
    public void testManyIndexers() throws Exception
    {
        String[] configs = {
        "mixin = custom, testMixinMethod",
        "constant = \"Test constant\"",
        "fullRecord = xml" };

        final List<AbstractValueIndexer<?>> valueIndexers = CreateIndexerUtil.createIndexers(configs);
        assertEquals(3, valueIndexers.size());

        for (final AbstractValueIndexer<?> valueIndexer : valueIndexers)
        {
            // This will fail because the full record indexer dosn't allow NULL as record.
            assertNotNull(valueIndexer.getFieldData(null));
        }
    }

    @Test
    public void testJavaIndexerInheritanceParent() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = CreateIndexerUtil.createIndexer("javacall", "java, testMethod");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("javacall", indexer.getSolrFieldNames().iterator().next());
        assertEquals("<null>", indexer.getFieldData(null).iterator().next());
    }

    @Test
    public void testJavaIndexerInheritanceChild() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = CreateIndexerUtil.createIndexer("javachildcall", "java(ChildMixin), testMethod");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("javachildcall", indexer.getSolrFieldNames().iterator().next());
        assertEquals("Overwritten in ChildMixin", indexer.getFieldData(null).iterator().next());
    }
}
