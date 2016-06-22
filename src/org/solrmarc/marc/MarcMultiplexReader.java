package org.solrmarc.marc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

public class MarcMultiplexReader implements MarcReader
{
    final Collection<MarcReader> readers;
    final Iterator<MarcReader> iterator;
    MarcReader curReader = null;
    
    public MarcMultiplexReader(MarcReader ... marcReaders)
    {
        readers = Arrays.asList(marcReaders);
        iterator = readers.iterator();
    }

    public MarcMultiplexReader(final Collection<MarcReader> marcReaders)
    {
        readers = marcReaders;
        iterator = readers.iterator();
    }

    @Override
    public boolean hasNext()
    {
        while (curReader == null || !curReader.hasNext())
        {
            if (iterator.hasNext())
            {
                curReader = iterator.next();
            }
            else
            {
                curReader = null;
                return(false);
            }
        }
        return(curReader.hasNext());
    }

    @Override
    public Record next()
    {
        return(curReader.next());
    }
    
}
