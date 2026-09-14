package org.solrmarc.mixin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import org.solrmarc.index.extractor.impl.custom.Mixin;

public class ComparisonMixin implements Mixin
{
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    static Pattern dateSwap = Pattern.compile("\\[([^ ]*) TO ([^\\]]*)\\]");
    static Pattern dateCheck = Pattern.compile("(....)-(..)-(..)");
    static Pattern yearCheck = Pattern.compile("(20|1\\d)\\d\\d");
    static Pattern uncertainYearCheck = Pattern.compile("(20|1\\d)(\\d\\d|\\du|uu)");
    static Pattern monthCheck = Pattern.compile("(0[1-9]|1[0-2])");
    static Pattern longMonthCheck = Pattern.compile("(0[13578]|1[02])");
    static Pattern dayCheck = Pattern.compile("(0[1-9]|[12][0-9]|30)");
    static Pattern dayCheckFeb = Pattern.compile("(0[1-9]|1[0-9]|2[0-8])");
    static Pattern contains4Digits = Pattern.compile(".*?(\\D|\\b)(\\d\\d\\d\\d)(\\D|\\b).*");

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
            String value = mapValidateSingleDate(key, defVal);
            if (value != null && value.length() != 0)
            {
            	result.add(value);
            }
        }
        return(result);
    }

    private String mapValidateSingleDate(String key, String defVal) 
    {
    	Matcher dateMatcher = dateCheck.matcher(key);
        if (dateMatcher.matches())
        {
            String year = dateMatcher.group(1);
            String month = dateMatcher.group(2);
            String day = dateMatcher.group(3);
            String value = null;
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
            value = mapPadShortDate(value, defVal);
            return(value);
        }
        return(null);
	}

	public String mapPadShortDate(String value, String defVal) 
	{
        if (value != null && value.length() > 0 && defVal != null) 
        {
            value = value + defVal.substring(Math.min(defVal.length(), value.length()));
            return(value);
        }
		return value;
	}

	public Collection<String> mapDateComparisonNow(Collection<String> keys, String ifBefore, String ifEqual, String ifAfter)
    {
	    SimpleDateFormat sformatter = new SimpleDateFormat("yyyyMMdd");
	    String curDateStr = sformatter.format(new Date());
	    return  mapDateComparison(keys, curDateStr, ifBefore, ifEqual, ifAfter);
    }

	//   published_daterange = 008[7-14]?((008[6] == 'i' |008[6] == 'd' | 008[6] == 'm' | 008[6] == 'k' | 008[6] == 'q')&& 008[11-14] != "9999"),
	//            map("(\\d\\d\\d\\d)(\\d\\d\\d\\d)=>[$1 TO $2]","(\\d\\d)uu(\\d\\d\\d\\d)=>$2","(\\d\\d\\d)u(\\d\\d\\d\\d)=>$2"), mapSwapOrderOfRange,
	//            sort(str, asc), first
	//   published_daterange ?= 008[7-14]?((008[6] == 'e')), untrimmed,
	//            map("(\\d\\d\\d\\d)(..)(..)=>$1-$2-$3"), , mapValidateDate(""), sort(str, asc), first
	//   published_daterange ?= {008[07-10]:008[11-14]}?((008[6] == 'c')&& 008[11-14] != "9999"),
	//            map("(\\d\\d\\d\\d)(\\d\\d\\d\\d)=>[$2 TO $1]","(\\d\\d)uu(\\d\\d\\d\\d)=>$2","(\\d\\d\\d)u(\\d\\d\\d\\d)=>$2"),
	//            sort(str, asc), first
	//   published_daterange ?= 008[7-10]?((008[6] == 'i' | 008[6] == 'c' | 008[6] == 'd' | 008[6] == 'm' | 008[6] == 'k' | 008[6] == 'q' | 008[6] == 'u')&& 008[11-14] == "9999"),
	//            map("(\\d\\d\\d\\d)=>[$1 TO *]","(\\d\\d)uu=>[$100 TO $199]", "(\\d\\d\\d)u=>[$10 TO $19]"),
	//            sort(str, asc), first
	//   published_daterange ?= {008[07-10]:008[11-14]}?(008[6] == 'r' | 008[6] == 'p' | 008[6] == 'u'), map("(\\d\\d\\d\\d)=>$1"), sort(str, asc), first
	//   published_daterange ?= 008[7-10]?(008[6] == 's'), map(".*?(\\d\\d\\d\\d).*=>$1"), map("(.*)=>$1"), sort(str, asc), first
	//   published_daterange ?= 260c:264c?(ind1 = '1' || ind1 = ' '), map(".*?(\\b\\d\\d\\d\\d\\b).*=>$1"), map("(.*)=>$1"), sort(str, asc), first
    public String getDateRange(Record record, String rangeFormat, String openRangeFormat)
    {
		VariableField vf;
    	String field_008 = ((vf = record.getVariableField("008")) != null) ? ((ControlField)vf).getData() : null;
    	String date_008 = (field_008 != null) ? field_008.substring(7, 15) : "        ";
    	String date_008_1 = (field_008 != null) ? field_008.substring(7, 11) : "    ";
    	String date_008_2 = (field_008 != null) ? field_008.substring(11, 15) : "    ";
    	String result = null;
    	char dType = (field_008 != null && field_008.length() > 14) ? field_008.charAt(6) : ' ';
    	switch (dType) {
    	case 'e':  // extended Date 
    		result = mapValidateSingleDate(date_008.substring(0,4)+"-"+date_008.substring(4,6)+"-"+date_008.substring(6,8), "");
    		break;
    	case 'i':
    	case 'd': 
    	case 'm':
    	case 'k':
    	case 'q':
    	case 'c':
    		if (!date_008_2.equals("9999"))
    		{  
    			if (yearCheck.matcher(date_008_1).matches() && yearCheck.matcher(date_008_2).matches())
				{
					if (date_008_1.compareTo(date_008_2) > 0 )
	                {
	                    result = String.format(rangeFormat, date_008_2, date_008_1);
	                }
					else
					{
		                result = String.format(rangeFormat, date_008_1, date_008_2);
					}
				}
				else if (uncertainYearCheck.matcher(date_008_1).matches() )
				{
					result = date_008_1.replace('u', '0');
				}
				else if (uncertainYearCheck.matcher(date_008_2).matches() )
				{
					result = date_008_2.replace('u', '0');
				}
    		}
    		else if (yearCheck.matcher(date_008_1).matches() )
			{
                result = String.format(openRangeFormat, date_008_1);
    		}
    		break;
    	case 'r':  // for 'r' reprint use the more recent date.
    	case 'p':  // for 'p' performed / published use the more recent date
    	case 't':
    	case 'u':
    		if (yearCheck.matcher(date_008_1).matches() && yearCheck.matcher(date_008_2).matches())
			{
				if (date_008_1.compareTo(date_008_2) > 0 )
		        {
		            result = (dType == 'r' || dType == 'p' ) ? date_008_1 : date_008_2;
		        }
				else
				{
		            result = (dType == 'r' || dType == 'p' ) ? date_008_2 : date_008_1;
				}
				break;
			} // note intentional falling through case here.
    	case 's':
    		if (uncertainYearCheck.matcher(date_008_1).matches() )
			{
				result = date_008_1.replace('u', '0');
			}
			else if (uncertainYearCheck.matcher(date_008_2).matches() )
			{
				result = date_008_2.replace('u', '0');
			}
			break;
    	}
    	if (result == null)
    	{
    		String f260c = null;
    		String f264c = null;
    		if ((vf = record.getVariableField("260")) != null)
    		{
    			f260c = ((DataField)vf).getSubfieldsAsString("c");
    		}
    		if ((vf = record.getVariableField("264")) != null && (((DataField)vf).getIndicator1() == ' ' || ((DataField)vf).getIndicator1() == '1' ))
    		{
    			f264c = ((DataField)vf).getSubfieldsAsString("c");
    		}
    		Matcher m;
    		if (f260c != null && (m = contains4Digits.matcher(f260c)).matches() )
    		{
    			f260c = m.group(2);
    		}
    		if (f264c != null && (m = contains4Digits.matcher(f264c)).matches() )
    		{
    			f264c = m.group(2);
    		}
    		if (f260c != null && uncertainYearCheck.matcher(f260c).matches() )
			{
				result = f260c.replace('u', '0');
			}
			else if (f264c != null && uncertainYearCheck.matcher(f264c).matches() )
			{
				result = f264c.replace('u', '0');
			}
    	}
    	return(result);
    }
    
    public String getSingleDate(Record record, String rangeFormat, String defVal)
    {
    	String result = getDateRange(record, rangeFormat, rangeFormat);
    	if (result != null && result.length() != 0 && defVal != null && defVal.length() > 0)
    	{
    		result = mapPadShortDate(result, defVal);
    		//result = mapValidateSingleDate(result, defVal);
    	}
    	return(result);
    }
}
