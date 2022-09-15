package org.solrmarc.mixin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.solrmarc.index.extractor.impl.custom.Mixin;

public class ComparisonMixin implements Mixin
{
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    static String curDateStr = formatter.format(new Date());
    static Pattern dateSwap = Pattern.compile("\\[([^ ]*) TO ([^\\]]*)\\]");
    static Pattern dateCheck = Pattern.compile("(....)-(..)-(..)");
    static Pattern yearCheck = Pattern.compile("(20|1\\d)\\d\\d");
    static Pattern monthCheck = Pattern.compile("(0[1-9]|1[0-2])");
    static Pattern longMonthCheck = Pattern.compile("(0[13578]|1[02])");
    static Pattern dayCheck = Pattern.compile("(0[1-9]|[12][0-9]|30)");
    static Pattern dayCheckFeb = Pattern.compile("(0[1-9]|1[0-9]|2[0-8])");

    public Collection<String> mapDateComparison(Collection<String> keys, String dateStrToCompare, String ifBefore, String ifEqual, String ifAfter)
    {
        Collection<String> result = new ArrayList<String>(keys.size());

        for (String key : keys)
        {
            int compare = key.compareTo(dateStrToCompare);
            if (compare < 0)
            {
                if (ifBefore.length() > 0) result.add(ifBefore);
            }
            else if (compare > 0)
            {
                if (ifAfter.length() > 0) result.add(ifAfter);
            }
            else if (compare == 0)
            {
                if (ifEqual.length() > 0) result.add(ifEqual);
            }
        }
        return (result);
    }

    public Collection<String> mapSwapOrderOfRange(Collection<String> keys)
    {
        Collection<String> result = new ArrayList<String>(keys.size());
        
        for (String key : keys)
        {
            Matcher matcher = dateSwap.matcher(key);
            if (matcher.matches())
            {
                if (matcher.group(1).compareTo(matcher.group(2)) > 0 )
                {
                    key = "[" + matcher.group(2) + " TO " + matcher.group(1) + "]";
                }
            }
            result.add(key);            
        }
        return(result);
    }

    public Collection<String> mapValidateDate(Collection<String> keys, String defVal)
    {
        Collection<String> result = new ArrayList<String>(keys.size());
        
        for (String key : keys)
        {
            Matcher dateMatcher = dateCheck.matcher(key);
            if (dateMatcher.matches())
            {
                String year = dateMatcher.group(1);
                String month = dateMatcher.group(2);
                String day = dateMatcher.group(3);
                String value = "";
                if (yearCheck.matcher(year).matches())
                {
                    if (monthCheck.matcher(month).matches())
                    {
                        if (!month.equals("02") && dayCheck.matcher(day).matches() || 
                                ( longMonthCheck.matcher(month).matches() && day.equals("31")) || 
                                ( month.matches("02") && dayCheckFeb.matcher(day).matches()))
                        {
                            value = key;
                        }
                        else
                        {
                            value = year + "-" + month;
                        }
                    }
                    else
                    {
                        value = year;
                    }
                }
                if (value.length() > 0) 
                {
                    value = value + defVal.substring(Math.min(defVal.length(), value.length()));
                    result.add(value);
                }
            }                       
        }
        return(result);
    }

    public Collection<String> mapDateComparisonNow(Collection<String> keys, String ifBefore, String ifEqual, String ifAfter)
    {
        return  mapDateComparison(keys, curDateStr, ifBefore, ifEqual, ifAfter);
    }

}
