package org.solrmarc.index;


import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.RecordImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.solrmarc.driver.Indexer;
import org.solrmarc.index.collector.MultiValueCollector;
import org.solrmarc.index.extractor.impl.constant.ConstantMultiValueExtractor;
import org.solrmarc.index.extractor.impl.patternMapping.PatternMapping;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.MultiValueIndexer;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.index.mapping.impl.MultiValuePatternMapping;
import org.solrmarc.index.mapping.impl.MultiValueTranslationMapping;
import org.solrmarc.solr.SolrProxy;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;


public class IndexerTests
{
    private Record testRecord;
    private Properties translationMappingProperties = new Properties();
    private final AbstractMultiValueMapping[] translationMapping = new AbstractMultiValueMapping[]{
            new MultiValueTranslationMapping(translationMappingProperties)
    };
    private final AbstractMultiValueMapping[] patternMapping = new AbstractMultiValueMapping[]{
            new MultiValuePatternMapping(Collections.singletonList(new PatternMapping("[A-Z]", "X", 0)), true, false, false)
    };
    private final AbstractMultiValueMapping[] translationAndPatternMapping = new AbstractMultiValueMapping[]{
            new MultiValueTranslationMapping(translationMappingProperties),
            new MultiValuePatternMapping(Collections.singletonList(new PatternMapping("(FOO)", "Foo $1", 0)))
    };
    private final MultiValueCollector singleCollector = new MultiValueCollector();
    private final ConstantMultiValueExtractor constantExtractor = new ConstantMultiValueExtractor("Foo Bar");
    private static ValueIndexerFactory factory;
    static
    {
        factory = ValueIndexerFactory.initialize(new String[]{System.getProperty("test.data.dir", "test/data")});
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
        final MultiValueIndexer valueIndexer = factory.createValueIndexer("testField", "\"Foo Bar\"");
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(Collections.<AbstractValueIndexer<?>>singletonList(valueIndexer), proxy);

        indexer.indexToSolr(new TestReader(Collections.singletonList(testRecord)));

        List<SolrInputDocument> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        SolrInputDocument document = documents.get(0);
        assertEquals("Foo Bar", document.getField("testField").getValue().toString());
    }

    @Test
    public void testWithTranslationMap() throws Exception
    {
        final MultiValueIndexer valueIndexer = new MultiValueIndexer("testField", constantExtractor, translationMapping, singleCollector);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(Collections.<AbstractValueIndexer<?>>singletonList(valueIndexer), proxy);

        indexer.indexToSolr(new TestReader(Collections.singletonList(testRecord)));

        List<SolrInputDocument> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        SolrInputDocument document = documents.get(0);
        assertEquals("BAR FOO", document.getField("testField").getValue().toString());
    }

    @Test
    public void testWithPatternMap() throws Exception
    {
        final MultiValueIndexer valueIndexer = new MultiValueIndexer("testField", constantExtractor, patternMapping, singleCollector);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(Collections.<AbstractValueIndexer<?>>singletonList(valueIndexer), proxy);

        indexer.indexToSolr(new TestReader(Collections.singletonList(testRecord)));

        List<SolrInputDocument> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        SolrInputDocument document = documents.get(0);
        assertEquals("Xoo Xar", document.getField("testField").getValue().toString());
    }

    @Test
    public void testWithTranslationAndPatternMap() throws Exception
    {
        final MultiValueIndexer valueIndexer = new MultiValueIndexer("testField", constantExtractor, translationAndPatternMapping, singleCollector);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(Collections.<AbstractValueIndexer<?>>singletonList(valueIndexer), proxy);

        indexer.indexToSolr(new TestReader(Collections.singletonList(testRecord)));

        List<SolrInputDocument> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        SolrInputDocument document = documents.get(0);
        assertEquals("Foo FOO", document.getField("testField").getValue().toString());
    }

    @Test
    public void testNoValueIndexer() throws Exception
    {
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(new ArrayList<AbstractValueIndexer<?>>(), proxy);
        indexer.indexToSolr(new TestReader(Collections.singletonList(testRecord)));

        List<SolrInputDocument> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertTrue(documents.get(0).isEmpty());
    }

    private List<SolrInputDocument> extractDocuments(final SolrProxy proxy) throws IOException
    {
        final ArgumentCaptor<SolrInputDocument> mapArgumentCaptor = ArgumentCaptor.forClass(SolrInputDocument.class);
        verify(proxy).addDoc(mapArgumentCaptor.capture());
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
