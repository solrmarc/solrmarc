package org.solrmarc.index.extractor.impl.fullrecord;

import java.util.Collection;
import java.util.List;

import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.extractor.AbstractMultiValueExtractor;

public class FullRecordAsFieldsValuesExtractor extends  AbstractMultiValueExtractor
{
    final String fieldSpec;
    public FullRecordAsFieldsValuesExtractor(String fieldSpec)
    {
        this.fieldSpec = fieldSpec;
    }

    @Override
    protected void extract(Collection<String> result, Record record) throws Exception
    {
        List<VariableField> fields = (( org.marc4j.marc.impl.RecordImpl) record).getVariableFieldsWithLeader();
        for (VariableField vf : fields) 
        {
            if (fieldSpec.contains(vf.getTag()) || vf.getTag().matches(fieldSpec))
            {
                result.add(vf.toString());
            }
        }
    }

}
