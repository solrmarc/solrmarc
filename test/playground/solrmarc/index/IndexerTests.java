package playground.solrmarc.index;


import playground.solrmarc.index.collector.SingleValueCollector;
import playground.solrmarc.index.extractor.impl.constant.ConstantSingleValueExtractor;
import playground.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import playground.solrmarc.index.indexer.AbstractValueIndexer;
import playground.solrmarc.index.indexer.SingleValueIndexer;
import playground.solrmarc.index.mapping.AbstractSingleValueMapping;
import playground.solrmarc.index.mapping.impl.SingleValuePatternMapping;
import playground.solrmarc.index.mapping.impl.SingleValueTranslationMapping;
import playground.solrmarc.solr.SolrProxy;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.RecordImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;


public class IndexerTests
{
    private Record testRecord;
    private Properties translationMappingProperties = new Properties();
    private final AbstractSingleValueMapping[] translationMapping = new AbstractSingleValueMapping[]{
            new SingleValueTranslationMapping(translationMappingProperties)
    };
    private final AbstractSingleValueMapping[] patternMapping = new AbstractSingleValueMapping[]{
            new SingleValuePatternMapping(Collections.singletonList(new PatternMapping("[A-Z]", "X", 0)))
    };
    private final AbstractSingleValueMapping[] translationAndPatternMapping = new AbstractSingleValueMapping[]{
            new SingleValueTranslationMapping(translationMappingProperties),
            new SingleValuePatternMapping(Collections.singletonList(new PatternMapping("[A-Z]", "X", 0)))
    };
    private final AbstractSingleValueMapping[] noMappings = new AbstractSingleValueMapping[0];
    private final SingleValueCollector singleCollector = new SingleValueCollector();
    private final ConstantSingleValueExtractor constantExtractor = new ConstantSingleValueExtractor("Foo Bar");

    static
    {
        PropertyConfigurator.configure(new File("log4j.properties").getAbsolutePath());
    }

    @Before
    public void setup()
    {
        testRecord = new RecordImpl();
        testRecord.setId(1L);

        translationMappingProperties.clear();
        translationMappingProperties.put("Foo Bar", "BAR FOO");
    }

    @Test
    public void testNoMappings() throws Exception
    {
        final SingleValueIndexer valueIndexer = new SingleValueIndexer("testField", constantExtractor, noMappings, singleCollector);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(Collections.<AbstractValueIndexer>singletonList(valueIndexer), proxy);

        indexer.index(new TestReader(Collections.singletonList(testRecord)));

        List<Map> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertEquals(Collections.singletonMap("testField", "Foo Bar"), documents.get(0));
    }

    @Test
    public void testWithTranslationMap() throws Exception
    {
        final SingleValueIndexer valueIndexer = new SingleValueIndexer("testField", constantExtractor, translationMapping, singleCollector);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(Collections.<AbstractValueIndexer>singletonList(valueIndexer), proxy);

        indexer.index(new TestReader(Collections.singletonList(testRecord)));

        List<Map> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertEquals(Collections.singletonMap("testField", "BAR FOO"), documents.get(0));
    }

    @Test
    public void testWithPatternMap() throws Exception
    {
        final SingleValueIndexer valueIndexer = new SingleValueIndexer("testField", constantExtractor, patternMapping, singleCollector);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(Collections.<AbstractValueIndexer>singletonList(valueIndexer), proxy);

        indexer.index(new TestReader(Collections.singletonList(testRecord)));

        List<Map> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertEquals(Collections.singletonMap("testField", "Xoo Xar"), documents.get(0));
    }

    @Test
    public void testWithTranslationAndPatternMap() throws Exception
    {
        final SingleValueIndexer valueIndexer = new SingleValueIndexer("testField", constantExtractor, translationAndPatternMapping, singleCollector);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(Collections.<AbstractValueIndexer>singletonList(valueIndexer), proxy);

        indexer.index(new TestReader(Collections.singletonList(testRecord)));

        List<Map> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertEquals(Collections.singletonMap("testField", "XXX XXX"), documents.get(0));
    }

    @Test
    public void testNoValueIndexer() throws Exception
    {
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(new ArrayList<AbstractValueIndexer>(), proxy);
        indexer.index(new TestReader(Collections.singletonList(testRecord)));

        List<Map> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertTrue(documents.get(0).isEmpty());
    }

    private List<Map> extractDocuments(final SolrProxy proxy) throws IOException
    {
        final ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(proxy).addDoc(mapArgumentCaptor.capture(), anyBoolean(), anyBoolean());
        return mapArgumentCaptor.getAllValues();
    }

    class TestReader implements MarcReader
    {
        final Iterator<Record> iterator;

        TestReader(final Iterator<Record> iterator)
        {
            this.iterator = iterator;
        }

        TestReader(final List<Record> list)
        {
            this(list.iterator());
        }

        @Override
        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        @Override
        public Record next()
        {
            return iterator.next();
        }
    }
}
