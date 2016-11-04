package org.solrmarc.mixin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.impl.custom.Mixin;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.tools.PropertyUtils;

public class FileLookupMixin implements Mixin
{
    Map<String, Map<String, String>> textfileMaps = new LinkedHashMap<>();

    public String getFromFileBy001(Record record, String filename, String defaultValue)
    {
        Map<String, String> lookupMap = getLookupMap(filename);
        
        String id = record.getControlNumber();
        String result = resultMap.containsKey(id) ? resultMap.get(id) : defaultValue;
        return (result);
    }

    public Collection<String> getFromFileByKey(Collection<String> values, String filename, String defaultValue)
    {
        Map<String, String> lookupMap = getLookupMap(filename);
        Collection<String> result = new ArrayList<String>(values.size());

        for (String value : values)
        {
            if (lookupMap.containsKey(id))
            {
                result.add(lookupMap.get(id));
            }
        }
        if (result.isEmpty() && defaultValue.length() > 0)
        {
            result.add(defaultValue);
        }
        return (result);
    }

    private Map<String, String> getLookupMap(String filename)
    {
        if (!textfileMaps.containsKey(filename))
        {
            resultMap = loadTextFileIntoMap(filename);
        }
        else
        {
            resultMap = textfileMaps.get(filename);
            if (resultMap == null) throw new IndexerSpecException("Map not loaded, lookup fails " + filename);
        }
    }

    private Map<String, String> loadTextFileIntoMap(String filename)
    {
        Map<String, String> resultMap;
        try
        {
            resultMap = new LinkedHashMap<>();
            InputStream dateFirstAddedStream = PropertyUtils.getPropertyFileInputStream(ValueIndexerFactory.instance().getHomeDirs(), filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(dateFirstAddedStream));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String parts[] = line.split("[\t ]+", 2);
                resultMap.put(parts[0], parts[1]);
            }
            textfileMaps.put(filename, resultMap);
        }
        catch (FileNotFoundException e)
        {
            textfileMaps.put(filename, null);
            throw new IndexerSpecException("Unable to open lookup file " + filename);
        }
        catch (IOException e)
        {
            textfileMaps.put(filename, null);
            throw new IndexerSpecException("Unable to read lookup file " + filename);
        }
        return(resultMap);
    }
}
