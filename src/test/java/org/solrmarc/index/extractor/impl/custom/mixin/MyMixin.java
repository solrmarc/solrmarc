package org.solrmarc.index.extractor.impl.custom.mixin;


import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.impl.custom.Mixin;


public class MyMixin implements Mixin
{
    public String testMixinMethod(final Record record) {
        return (record == null) ? "<null>" : record.toString();
    }
}
