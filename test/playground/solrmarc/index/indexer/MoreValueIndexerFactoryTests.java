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

        final List<AbstractValueIndexer<?>> valueIndexers = createIndexers(configs);
        assertEquals(1, valueIndexers.size());

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexers.get(0);
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("linked_title_facet", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        Object linked_title_facet = indexer.getFieldData(testRecord);
        
    }


    private List<AbstractValueIndexer<?>> createIndexers(Properties configs) throws IllegalAccessException, InstantiationException
    {
        final ValueIndexerFactory factory = new ValueIndexerFactory();
        return factory.createValueIndexers(configs);
    }
}
