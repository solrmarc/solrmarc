package org.solrmarc.mixin;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.index.SolrIndexerMixin;
import org.solrmarc.tools.Utils;

public class LanguageMixin extends SolrIndexerMixin
{
    public Collection<String> getCombinedFormat(final Record record) throws Exception
    {    
        // part1_format_facet = 000[6]:007[0], format_maps.properties(broad_format), first
        // part2_format_facet = 999t, format_maps.properties(format)

        String mapName1 = SolrIndexer.instance().loadTranslationMap(null, "format_maps.properties(broad_format)");
        String mapName2 = SolrIndexer.instance().loadTranslationMap(null, "format_maps.properties(format_007)");
        String mapName3 = SolrIndexer.instance().loadTranslationMap(null, "format_maps.properties(format)");

        Collection<String> result = SolrIndexer.instance().getFieldList(record, "999t");
        result = SolrIndexer.instance().remap(result, SolrIndexer.instance().findMap(mapName3), false);

        Set<String> urls = SolrIndexer.instance().getFieldList(record, "856u");
        if (Utils.setItemContains(urls, "serialssolutions"))
        {
            String serialsFormat = SolrIndexer.instance().remap("as", SolrIndexer.instance().findMap(mapName1), true);
            if (serialsFormat != null) result.add(serialsFormat);
        }
        else
        {
            String format_007 = SolrIndexer.instance().getFirstFieldVal(record, mapName2, "007[0]");
            if (format_007 != null) 
            {
                result.add(format_007);
            }
            else 
            {
                String broadFormat = SolrIndexer.instance().getFirstFieldVal(record, mapName1, "000[6-7]:000[6]");
                if (broadFormat != null) 
                {
                    if (broadFormat.contains("|"))
                        broadFormat = broadFormat.substring(0, broadFormat.indexOf('|'));
                    result.add(broadFormat);
                }
            }
        }
        return(result);
    }

    public Set<String> getCustomLanguage(final Record record, String propertiesMap) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        String mapName = SolrIndexer.instance().loadTranslationMap(propertiesMap);
        String primaryLanguage = SolrIndexer.instance().getFirstFieldVal(record, mapName, "008[35-37]");
        Collection<String> otherLanguages = SolrIndexer.instance().getFieldList(record, "041a:041d");
        otherLanguages = SolrIndexer.instance().remap(otherLanguages, SolrIndexer.instance().findMap(mapName), true);
        Collection<String> translatedFrom = SolrIndexer.instance().getFieldList(record, "041h");
        translatedFrom = SolrIndexer.instance().remap(translatedFrom, SolrIndexer.instance().findMap(mapName), true);
        Collection<String> subtitleLanguage = SolrIndexer.instance().getFieldList(record, "041b");
        subtitleLanguage = SolrIndexer.instance().remap(subtitleLanguage, SolrIndexer.instance().findMap(mapName), true);
        Collection<String> format = getCombinedFormat(record);
        boolean isBook = Utils.setItemContains(format, "Book") || Utils.setItemContains(format, "Journal");
        boolean isDVD = Utils.setItemContains(format, "DVD") ;
        Set<String> notesFields = SolrIndexer.instance().getFieldList(record, "500a");
        boolean isTranslated = Utils.setItemContains(notesFields, "[Tt]ranslat((ed)|(ion))");
        if (primaryLanguage != null)  resultSet.add(primaryLanguage);
        if (primaryLanguage != null && Utils.setItemContains(otherLanguages, primaryLanguage))
        {
            otherLanguages.remove(primaryLanguage);
        }
        if (isBook && isTranslated && otherLanguages.size() == 1 && translatedFrom.size() == 0)
        {
            copySetWithSuffix(resultSet, otherLanguages, " (translated from)");
        }
        else 
        {
            if (isDVD)
                copySetWithSuffix(resultSet, otherLanguages, " (dubbed in)");
            else
                copySetWithSuffix(resultSet, otherLanguages, " (also in)");
            
            if (primaryLanguage != null && Utils.setItemContains(translatedFrom, primaryLanguage))
            {
                translatedFrom.remove(primaryLanguage);
            }
            copySetWithSuffix(resultSet, translatedFrom, " (translated from)");
        }
        copySetWithSuffix(resultSet, subtitleLanguage, (isBook ? " (summary in)" : " (subtitles in)") );
        return(resultSet);
    }
    
    private void copySetWithSuffix(Set<String> resultSet, Collection<String> languageList, String suffix)
    {
        for (String language : languageList)
        {
            String toAdd = language + suffix;
            resultSet.add(toAdd);
        }  
    }

}
