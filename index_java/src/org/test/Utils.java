package org.test;


import org.marc4j.marc.Record;
import playground.solrmarc.index.extractor.impl.custom.Mixin;


public class Utils implements Mixin {
    public static String toString(final Record record) {
        return (record == null) ? "<null>" : record.toString();
    }
}
