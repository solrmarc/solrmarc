package org.test;


import org.marc4j.marc.Record;
import playground.solrmarc.index.extractor.impl.custom.Mixin;


public class TestMixin implements Mixin {
    public String testMethod(final Record record) {
        return Utils.toString(record);
    }
}
