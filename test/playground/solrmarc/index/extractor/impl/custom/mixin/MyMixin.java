package playground.solrmarc.index.extractor.impl.custom.mixin;


import playground.solrmarc.index.extractor.impl.custom.Mixin;
import org.marc4j.marc.Record;


public class MyMixin implements Mixin
{
    public String testMixinMethod(final Record record) {
        return (record == null) ? "<null>" : record.toString();
    }
}
