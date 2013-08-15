package org.solrmarc.marcoverride;

import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.RecordImpl;
import org.marc4j.marc.impl.Verifier;

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class NoSortRecordImpl extends RecordImpl
{
    /**
     * 
     */
    private static final long serialVersionUID = -5870251915056214892L;


	public NoSortRecordImpl()
    {
        super();
    }
    
    public void addVariableField(VariableField field) {
        if (!(field instanceof VariableField))
            throw new IllegalAddException("Expected VariableField instance");

        String tag = field.getTag();
        if (Verifier.isControlNumberField(tag)) {
            if (Verifier.hasControlNumberField(getControlFields()))
                getControlFields().set(0, (ControlField)field);
            else
                getControlFields().add(0, (ControlField)field);
        } else if (Verifier.isControlField(tag)) {
            getControlFields().add((ControlField)field);
        } else {
            getDataFields().add((DataField)field);
        }

    }


}
