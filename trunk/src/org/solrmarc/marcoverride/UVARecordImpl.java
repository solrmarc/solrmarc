package org.solrmarc.marcoverride;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.RecordImpl;
import org.marc4j.marc.impl.Verifier;

public class UVARecordImpl extends RecordImpl
{

    public UVARecordImpl()
    {
        super();
    }
    
    public void addVariableField(VariableField field) 
    {
        if (!(field instanceof VariableField))
            throw new IllegalAddException("Expected VariableField instance");

        String tag = field.getTag();
        if (Verifier.isControlNumberField(tag)) 
        {
            ControlField cfield = getControlNumberField();
            if (cfield != null)
            {
                if (!((ControlField)field).getData().startsWith("u") && 
                       cfield.getData().startsWith("u") )
                {
                    // ditch it!
                    return;
                }
            }
        }
        super.addVariableField(field);
    }


}
