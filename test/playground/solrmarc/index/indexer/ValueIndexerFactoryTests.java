package playground.solrmarc.index.indexer;


import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
        final Properties configs = new Properties();
        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(0, valueIndexers.size());
    }

    @Test
    public void testConstantIndexer() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("constant", "\"Test constant\"");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("constant", indexer.getSolrFieldNames().iterator().next());
        assertEquals("Test constant", indexer.getFieldData(null).iterator().next());
    }

    @Test
    public void testFullRecordIndexer() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("fullRecord", "text");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("fullRecord", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        String xml = indexer.getFieldData(testRecord).iterator().next().toString();
        
    }

    @Test
    public void testFieldSpecDataFieldIndexer() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("title_fields", "245abnp");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("title_fields", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        Object title = indexer.getFieldData(testRecord);
        
    }
    
    @Test
    public void testFieldSpecControlFieldIndexer() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("id", "001");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("id", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        Object id = indexer.getFieldData(testRecord);
        
    }
    
    
    //  "008[35-37]:041a:041d"
    @Test
    public void testFieldSpecMultiFieldIndexer() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("language_facet", "008[35-37]:041a:041d, language_map.properties, unique");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("language_facet", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        Object id = indexer.getFieldData(testRecord);
        
    }

    @Test
    public void testMixinIndexer() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("mixin", "custom, testMixinMethod");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("mixin", indexer.getSolrFieldNames().iterator().next());
        assertEquals("<null>", indexer.getFieldData(null).iterator().next());
    }

    
    @Test(expected = NullPointerException.class)
    public void testManyIndexers() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("mixin", "custom, testMixinMethod");
        configs.put("constant", "\"Test constant\"");
        configs.put("fullRecord", "xml");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
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
        final Properties configs = new Properties();
        configs.put("constant", "java, testMethod");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("constant", indexer.getSolrFieldNames().iterator().next());
        assertEquals("<null>", indexer.getFieldData(null).iterator().next());
    }

    @Test
    public void testJavaIndexerInheritanceChild() throws Exception
    {
        final Properties configs = new Properties();
        configs.put("constant", "java(ChildMixin), testMethod");

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("constant", indexer.getSolrFieldNames().iterator().next());
        assertEquals("Overwritten in ChildMixin", indexer.getFieldData(null).iterator().next());
    }

    private List<AbstractValueIndexer<?>> createIndexers(Properties configs) throws IllegalAccessException, InstantiationException
    {
        final ValueIndexerFactory factory = new ValueIndexerFactory();
        return factory.createValueIndexers(configs);
    }
}
