package org.solrmarc.mixin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.solrmarc.index.extractor.impl.custom.Mixin;

public class ComparisonMixin implements Mixin
{
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    static String curDateStr = formatter.format(new Date());

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

    public Collection<String> mapDateComparisonNow(Collection<String> keys, String ifBefore, String ifEqual, String ifAfter)
    {
        return  mapDateComparison(keys, curDateStr, ifBefore, ifEqual, ifAfter);
    }

}
