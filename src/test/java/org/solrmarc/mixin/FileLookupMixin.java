package org.solrmarc.mixin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.impl.custom.Mixin;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.tools.PropertyUtils;

public class FileLookupMixin implements Mixin
{
    static ConcurrentHashMap<String, Map<String, String>> textfileMaps = new ConcurrentHashMap<>();

    public String getFromFileBy001(Record record, String filename, String defaultValue)
    {
        return getFromFileBy001(record, filename, "[\t]", defaultValue);
    }

    public String getFromFileBy001(Record record, String filename, String sepPattern, String defaultValue)
    {
        Map<String, String> lookupMap = getLookupMap(filename, sepPattern);

        String id = record.getControlNumber();
        String result = lookupMap.containsKey(id) ? lookupMap.get(id) : defaultValue.length() > 0 ? defaultValue : null;
        return (result);
    }

    public String getFromFileKeyExists(Record record, String filename, String exists, String notExists)
    {
        Map<String, String> lookupMap = getLookupMap(filename, "");

        String id = record.getControlNumber();
        String result = lookupMap.containsKey(id) ? exists.length() > 0 ? exists : null : notExists.length() > 0 ? notExists : null;
        return (result);
    }

    public Collection<String> mapFromFileByKey(Collection<String> keys, String filename, String sepPattern, String defaultValue)
    {
        Map<String, String> lookupMap = getLookupMap(filename, sepPattern);
        Collection<String> result = new ArrayList<String>(keys.size());

        for (String key : keys)
        {
            if (lookupMap.containsKey(key))
            {
                result.add(lookupMap.get(key));
            }
        }
        if (result.isEmpty() && defaultValue.length() > 0)
        {
            result.add(defaultValue);
        }
        return (result);
    }

    public Collection<String> mapFromFileKeyExists(Collection<String> keys, String filename, String exists, String notExists)
    {
        Map<String, String> lookupMap = getLookupMap(filename, "");
        Collection<String> result = new ArrayList<String>(keys.size());

        for (String key : keys)
        {
            if (lookupMap.containsKey(key))
            {
                if (!exists.isEmpty())
                {
                    result.add(exists);
                }
                return (result);
            }
        }
        if (result.isEmpty() && !notExists.isEmpty())
        {
            result.add(notExists);
        }
        return (result);
    }

    public Collection<String> mapLookupSelect(Collection<String> values, String sepPattern, String select)
    {
        Collection<String> result = new ArrayList<String>(values.size());
        int selValue = Integer.parseInt(select);
        for (String value : values)
        {
            String[] split = value.split(sepPattern);
            if (selValue >= 0 && selValue < split.length)
            {
                result.add(split[selValue]);
            }
        }
        return (result);
    }
/*
 *  date_received = custom, getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), map("([^|]*)[|].*=>$1")
    fund_code = custom, getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), map("([^|]*)[|](.*)=>$2")

    date_received = custom, getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), custom_map(org.solrmarc.mixin.FileLookupMixin mapLookupSelect("[|]",0))
    fund_code = custom, getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), map("([^|]*)[|](.*)=>$2")

    date_received = custom, getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), custom_map(mapLookupSelect("[|]",0))
    fund_code = custom, getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), map("([^|]*)[|](.*)=>$2"), custom_map(mapLookupSplit(":"))
*/
    public Collection<String> mapLookupSplit(Collection<String> values, String sepPattern)
    {
        Collection<String> result = new ArrayList<String>(values.size());

        for (String value : values)
        {
            String[] split = value.split(sepPattern);
            for (String splitPart : split)
            {
                result.add(splitPart);
            }
        }
        return (result);
    }

    private Map<String, String> getLookupMap(String filename, String sepPattern)
    {
        Map<String, String> lookupMap;
        if (!textfileMaps.containsKey(filename+sepPattern))
        {
            lookupMap = loadTextFileIntoMap(filename, sepPattern);
        }
        else
        {
            lookupMap = textfileMaps.get(filename+sepPattern);
            if (lookupMap == null) throw new IndexerSpecException("Map not loaded, lookup fails " + filename);
        }
        return(lookupMap);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadTextFileIntoMap(String filename, String sepPattern)
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
                if (sepPattern.isEmpty())
                {
                    if (!line.trim().isEmpty())
                    {
                        resultMap.put(line, "1");
                    }
                }
                else 
                {
                    String parts[] = line.split(sepPattern, 2);
                    if (parts.length == 2)
                    {
                        resultMap.put(parts[0], parts[1]);
                    }
                }
            }
            textfileMaps.putIfAbsent(filename+sepPattern, Collections.unmodifiableMap(resultMap));
        }
        catch (FileNotFoundException e)
        {
            textfileMaps.putIfAbsent(filename+sepPattern, Collections.EMPTY_MAP);
            throw new IndexerSpecException("Unable to open lookup file " + filename);
        }
        catch (IOException e)
        {
            textfileMaps.putIfAbsent(filename+sepPattern, Collections.EMPTY_MAP);
            throw new IndexerSpecException("Unable to read lookup file " + filename);
        }
        return(textfileMaps.get(filename+sepPattern));
    }
}
