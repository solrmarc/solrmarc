package org.solrmarc.index.extractor.impl.patternMapping;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.solrmarc.index.indexer.IndexerSpecException;


public class PatternMapping
{
    /**
     * This is highly optimized! Using Matcher#replaceAll(String) instead of
     * String#replaceAll(String, String) is 50% faster. Using one instance of
     * Matcher and using Matcher#reset(String) is 10% faster than using
     * Pattern#matcher(String).
     */
    private final Pattern inputPattern;
//    private final String inputString;
    private final String outputPattern;
    private final int orderIndex;
    
    public PatternMapping(final String inputPatternStr, final String outputPattern, final int orderIndex)
    {
        this.inputPattern = Pattern.compile(inputPatternStr);
        this.outputPattern = outputPattern;
        this.orderIndex = orderIndex;
        // Test replacement at creation time to make sure group references match.
        int index = outputPattern.indexOf('$');
        int groupMax = 0;
        Matcher inputMatcher = inputPattern.matcher("");
        inputMatcher.replaceAll(outputPattern);
        int groupCnt = inputMatcher.groupCount();
        while (index >= 0)
        {
            String num = outputPattern.substring(index + 1).replaceFirst("[^0-9].*", "");
            int groupNum = Integer.parseInt(num);
            if (groupNum > groupMax) groupMax = groupNum;
            index = outputPattern.indexOf('$', index + 1 + num.length());
        }
        if (groupCnt < groupMax)
        {
            throw new IndexerSpecException(
                    "Unknown group $" + groupMax + " in pattern map replacement string : " + outputPattern + "\n");
        }
    }

    public static String mapSingleValue(final List<PatternMapping> patternMappings, String value)
    {
        for (PatternMapping patternMapping : patternMappings)
        {
            Matcher matcher = patternMapping.inputPattern.matcher(value);
            if (matcher.find())
            {
                value = patternMapping.map(matcher);
            }
        }
        return value;
    }

    public static void mapValues(final List<PatternMapping> patternMappings, String value, Collection<String> values)
    {
        for (PatternMapping patternMapping : patternMappings)
        {
            Matcher matcher = patternMapping.inputPattern.matcher(value);
            if (matcher.find())
            {
                final String mappedValue = patternMapping.map(matcher);
                if (mappedValue.length() != 0) values.add(mappedValue);
            }
        }
    }

    public int getOrderIndex()
    {
        return orderIndex;
    }

//    public boolean canHandle(final String value)
//    {
//        final boolean result = inputMatcher.reset(value).find();
//        return (result);
//    }

    /**
     * PatternMapping#canHandle(String) has to be called before. Otherwise the
     * result will not be correct!
     *
     * @param value
     *            the value to be mapped.
     * @return the mapped value.
     */
    public String map(final Matcher inputMatcher)
    {
        String result = outputPattern;
        if (outputPattern.contains("$"))
        {
            for (int group = inputMatcher.groupCount(); group > 0 ; group--)
            {
                final String pattern = java.util.regex.Matcher.quoteReplacement("$"+group);
                result = result.replaceAll(pattern, inputMatcher.group(group));
            }
        }
        return(result);
    }
}
