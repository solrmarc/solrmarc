import org.marc4j.marc.Record;


public class ChildMixin extends org.test.TestMixin {
    public String testMethod(final Record record) {
        return "Overwritten in ChildMixin";
    }

    public String childTestMethod(final Record record) {
        return (record == null) ? "<null>" : record.toString();
    }
}
