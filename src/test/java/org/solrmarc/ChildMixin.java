package org.solrmarc;

import org.marc4j.marc.Record;

import org.test.TestMixin;


public class ChildMixin extends TestMixin {
    public String testMethod(final Record record) {
        return "Overwritten in ChildMixin";
    }

    public String childTestMethod(final Record record) {
        return (record == null) ? "<null>" : record.toString();
    }
}
