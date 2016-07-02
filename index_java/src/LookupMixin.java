package org.solrmarc.mixin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.impl.custom.Mixin;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.tools.PropertyUtils;


import java.util.Map;

public class LookupMixin implements Mixin
{
    Map<String, Map<String, String>> textfileMaps = new LinkedHashMap<>();
    
    public String getFromSortedTextFile(Record record, String filename, String defaultValue)
    {
        Map<String, String> resultMap;
        if (!textfileMaps.containsKey(filename))
        {
            try
            {
                resultMap = new LinkedHashMap<>();
                InputStream dateFirstAddedStream = PropertyUtils.getPropertyFileInputStream(new String[]{ValueIndexerFactory.getHomeDir()}, filename);
                BufferedReader reader = new BufferedReader(new InputStreamReader(dateFirstAddedStream));
                String line;
                while ((line = reader.readLine())!= null)
                {
                    String parts[] = line.split("[\t ]");
                    resultMap.put(parts[0], parts[1]);
                }
                textfileMaps.put(filename,  resultMap);
            }
            catch (FileNotFoundException e)
            {
                textfileMaps.put(filename,  null);
                throw new IndexerSpecException("Unable to open lookup file " + filename);
            }
            catch (IOException e)
            {
                textfileMaps.put(filename,  null);
                throw new IndexerSpecException("Unable to read lookup file " + filename);
            }
        }
        else 
        {
            resultMap = textfileMaps.get(filename);
            if (resultMap == null)
                throw new IndexerSpecException("Map not loaded, lookup fails " + filename);
        }
        String id = record.getControlNumber();
        String result = resultMap.containsKey(id) ? resultMap.get(id) : defaultValue;
        return(result);
    }   

}
