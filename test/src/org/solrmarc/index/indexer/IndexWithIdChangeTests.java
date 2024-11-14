package org.solrmarc.index.indexer;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.marc4j.MarcXmlReader;
import org.solrmarc.driver.Indexer;
import org.solrmarc.driver.RecordAndDoc;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.index.indexer.MultiValueIndexer;
import org.solrmarc.solr.SolrProxy;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;


public class IndexWithIdChangeTests
{
    private final static String inputfilename="./test/data/records/jones_recs.xml";
    private static ValueIndexerFactory factory;
    static
    {
        factory = ValueIndexerFactory.initialize(new String[]{System.getProperty("test.data.dir", "test/data")});
    }

    private List<RecordAndDoc> extractDocuments(final SolrProxy proxy) throws IOException
    {
        final ArgumentCaptor<RecordAndDoc> mapArgumentCaptor = ArgumentCaptor.forClass(RecordAndDoc.class);
        verify(proxy).addDoc(mapArgumentCaptor.capture());
        return mapArgumentCaptor.getAllValues();
    }

    @Test
    public void testIndexDeleteUnmodifiedId() throws Exception
    {
        final String[] configSpecs = {
            "id = 001, first",
            "suppressed = 999t?($t == 1), DeleteRecordIfFieldNotEmpty"
        };
        final List<AbstractValueIndexer<?>> valueIndexers = factory.createValueIndexers(configSpecs);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(valueIndexers, proxy);

        InputStream input = new BufferedInputStream(new FileInputStream(inputfilename));
        indexer.indexToSolr(new MarcXmlReader(input));

        List<RecordAndDoc> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertEquals("in00005570501", documents.get(0).getRec().getControlNumber());
        assertEquals("in00005570501", documents.get(0).getDoc().getFieldValue("id"));

        Field delQField = Indexer.class.getDeclaredField("delQ");
        delQField.setAccessible(true);
        BlockingQueue<String> delQ = (BlockingQueue<String>) delQField.get(indexer);
        assertEquals(1, delQ.size());
        assertEquals("in00006878693", delQ.peek());
    }

    @Test
    public void testIndexDeleteModifiedId() throws Exception
    {
        final String[] configSpecs = {
            "id = 001, (pattern_map.id_prefix), first",
            "pattern_map.id_prefix.pattern_0 = (.+)=>folio.$1",
            "suppressed = 999t?($t == 1), DeleteRecordIfFieldNotEmpty"
        };
        final List<AbstractValueIndexer<?>> valueIndexers = factory.createValueIndexers(configSpecs);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(valueIndexers, proxy);

        InputStream input = new BufferedInputStream(new FileInputStream(inputfilename));
        indexer.indexToSolr(new MarcXmlReader(input));

        List<RecordAndDoc> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertEquals("folio.in00005570501", documents.get(0).getDoc().getFieldValue("id"));

        Field delQField = Indexer.class.getDeclaredField("delQ");
        delQField.setAccessible(true);
        BlockingQueue<String> delQ = (BlockingQueue<String>) delQField.get(indexer);
        assertEquals(1, delQ.size());
        assertEquals("folio.in00006878693", delQ.peek());
    }

    @Test
    public void testIndexDeleteUndefinedId() throws Exception
    {
        final String[] configSpecs = {
            "suppressed = 999t?($t == 1), DeleteRecordIfFieldNotEmpty"
        };
        final List<AbstractValueIndexer<?>> valueIndexers = factory.createValueIndexers(configSpecs);
        final SolrProxy proxy = Mockito.mock(SolrProxy.class);
        final Indexer indexer = new Indexer(valueIndexers, proxy);

        InputStream input = new BufferedInputStream(new FileInputStream(inputfilename));
        indexer.indexToSolr(new MarcXmlReader(input));

        List<RecordAndDoc> documents = extractDocuments(proxy);
        assertEquals(1, documents.size());
        assertEquals("in00005570501", documents.get(0).getRec().getControlNumber());
        assertNull(documents.get(0).getDoc().getFieldValue("id"));

        Field delQField = Indexer.class.getDeclaredField("delQ");
        delQField.setAccessible(true);
        BlockingQueue<String> delQ = (BlockingQueue<String>) delQField.get(indexer);
        assertEquals(1, delQ.size());
        assertEquals("in00006878693", delQ.peek());
    }
}
