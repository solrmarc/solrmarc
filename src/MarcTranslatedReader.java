import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class MarcTranslatedReader implements MarcReader
{
    MarcReader reader;
    CharConverter convert;
    MarcWriter errorWriter;
    
    public MarcTranslatedReader(MarcReader r)
    {
        reader = r;
        convert = new AnselToUnicode();
        errorWriter = null;
    }
    
    public MarcTranslatedReader(MarcReader r, String filenameForRecordsWithError)
    {
        reader = r;
        convert = new AnselToUnicode();
        try
        {
            errorWriter = new MarcStreamWriter(new FileOutputStream(filenameForRecordsWithError));
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Unable to open error output file: "+ filenameForRecordsWithError);
            errorWriter = null;
        }
    }

    protected void finalize() throws Throwable 
    {
        try {
            if (errorWriter != null)
            {
                errorWriter.close();
            }
        } 
        finally 
        {
            super.finalize();
        }
    }
    
    public boolean hasNext()
    {
        return reader.hasNext();
    }

    public Record next()
    {
        Record rec = reader.next();
        Leader l = rec.getLeader();
        if (l.getCharCodingScheme() == 'a') return(rec);
        List fields = rec.getVariableFields();
        Iterator f_iter = fields.iterator();
        while (f_iter.hasNext())
        {
            VariableField f = (VariableField)f_iter.next();
            if (!(f instanceof DataField)) continue;
            DataField field = (DataField)f;
            List subfields = field.getSubfields();
            Iterator s_iter = subfields.iterator();
            while (s_iter.hasNext())
            {
                Subfield sf = (Subfield)s_iter.next();
                String oldData = sf.getData();
                try {
                    String newData = convert.convert(oldData);
                    if (!oldData.equals(newData))
                    {
                        sf.setData(newData);
                    }
                }
                catch (Exception e)
                {
                    if (errorWriter != null) errorWriter.write(rec);
//                    String newData = convert.convert(oldData);
                }
            }
        }
        l.setCharCodingScheme('a');
        rec.setLeader(l);
        return rec;
    }
}
