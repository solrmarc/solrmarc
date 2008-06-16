/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
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
