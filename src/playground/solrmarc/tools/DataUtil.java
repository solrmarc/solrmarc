package playground.solrmarc.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class DataUtil
{
    private final static Pattern FOUR_DIGIT_PATTERN_BRACES = Pattern.compile("\\[[12]\\d{3,3}\\]");
    private final static Pattern FOUR_DIGIT_PATTERN_ONE_BRACE = Pattern.compile("\\[[12]\\d{3,3}");
    private final static Pattern FOUR_DIGIT_PATTERN_STARTING_WITH_1_2 = Pattern
            .compile("(20|19|18|17|16|15)[0-9][0-9]");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_1 = Pattern.compile("l\\d{3,3}");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_2 = Pattern.compile("\\[19\\]\\d{2,2}");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_3 = Pattern.compile("(20|19|18|17|16|15)[0-9][-?0-9]");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_4 = Pattern.compile("i.e. (20|19|18|17|16|15)[0-9][0-9]");
    private final static Pattern BC_DATE_PATTERN = Pattern.compile("[0-9]+ [Bb][.]?[Cc][.]?");
    private final static Pattern FOUR_DIGIT_PATTERN = Pattern.compile("\\d{4,4}");
    private static Matcher matcher;
    private static Matcher matcher_braces;
    private static Matcher matcher_one_brace;
    private static Matcher matcher_start_with_1_2;
    private static Matcher matcher_l_plus_three_digits;
    private static Matcher matcher_bracket_19_plus_two_digits;
    private static Matcher matcher_ie_date;
    private static Matcher matcher_bc_date;
    private static Matcher matcher_three_digits_plus_unk;
    protected static Logger logger = Logger.getLogger(DataUtil.class.getName());

    /**
     * Cleans non-digits from a String
     * 
     * @param date
     *            String to parse
     * @return Numeric part of date String (or null)
     */
    public static String cleanDate(final String date)
    {
        matcher_braces = FOUR_DIGIT_PATTERN_BRACES.matcher(date);
        matcher_one_brace = FOUR_DIGIT_PATTERN_ONE_BRACE.matcher(date);
        matcher_start_with_1_2 = FOUR_DIGIT_PATTERN_STARTING_WITH_1_2.matcher(date);
        matcher_l_plus_three_digits = FOUR_DIGIT_PATTERN_OTHER_1.matcher(date);
        matcher_bracket_19_plus_two_digits = FOUR_DIGIT_PATTERN_OTHER_2.matcher(date);
        matcher_three_digits_plus_unk = FOUR_DIGIT_PATTERN_OTHER_3.matcher(date);
        matcher_ie_date = FOUR_DIGIT_PATTERN_OTHER_4.matcher(date);
        matcher = FOUR_DIGIT_PATTERN.matcher(date);
        matcher_bc_date = BC_DATE_PATTERN.matcher(date);

        String cleanDate = null; // raises DD-anomaly

        if (matcher_braces.find())
        {
            cleanDate = matcher_braces.group();
            cleanDate = removeOuterBrackets(cleanDate);
            if (matcher.find())
            {
                String tmp = matcher.group();
                if (!tmp.equals(cleanDate))
                {
                    tmp = "" + tmp;
                }
            }
        }
        else if (matcher_ie_date.find())
        {
            cleanDate = matcher_ie_date.group().replaceAll("i.e. ", "");
        }
        else if (matcher_one_brace.find())
        {
            cleanDate = matcher_one_brace.group();
            cleanDate = removeOuterBrackets(cleanDate);
            if (matcher.find())
            {
                String tmp = matcher.group();
                if (!tmp.equals(cleanDate))
                {
                    tmp = "" + tmp;
                }
            }
        }
        else if (matcher_bc_date.find())
        {
            cleanDate = null;
        }
        else if (matcher_start_with_1_2.find())
        {
            cleanDate = matcher_start_with_1_2.group();
        }
        else if (matcher_l_plus_three_digits.find())
        {
            cleanDate = matcher_l_plus_three_digits.group().replaceAll("l", "1");
        }
        else if (matcher_bracket_19_plus_two_digits.find())
        {
            cleanDate = matcher_bracket_19_plus_two_digits.group().replaceAll("\\[", "").replaceAll("\\]", "");
        }
        else if (matcher_three_digits_plus_unk.find())
        {
            cleanDate = matcher_three_digits_plus_unk.group().replaceAll("[-?]", "0");
        }
        if (cleanDate != null)
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            String thisYear = dateFormat.format(calendar.getTime());
            try
            {
                if (Integer.parseInt(cleanDate) > Integer.parseInt(thisYear) + 1) cleanDate = null;
            }
            catch (NumberFormatException nfe)
            {
                cleanDate = null;
            }
        }
        if (cleanDate != null)
        {
            logger.debug("Date : " + date + " mapped to : " + cleanDate);
        }
        else
        {
            logger.debug("No Date match: " + date);
        }
        return cleanDate;
    }

    /**
     * Removes trailing characters (space, comma, slash, semicolon, colon),
     * trailing period if it is preceded by at least three letters, and single
     * square bracket characters if they are the start and/or end chars of the
     * cleaned string
     *
     * @param origStr
     *            String to clean
     * @return cleaned string
     */
    public static String cleanData(String origStr)
    {
        String currResult = origStr;
        String prevResult;
        do
        {
            prevResult = currResult;
            currResult = currResult.trim();

            currResult = currResult.replaceAll(" *([,/;:])$", "");

            // trailing period removed in certain circumstances
            if (currResult.endsWith("."))
            {
                if (currResult.matches(".*[JS]r\\.$"))
                {
                    // dont strip period off of Jr. or Sr.
                }
                else if (currResult.matches(".*\\w\\w\\.$"))
                {
                    currResult = currResult.substring(0, currResult.length() - 1);
                }
                else if (currResult.matches(".*\\p{L}\\p{L}\\.$"))
                {
                    currResult = currResult.substring(0, currResult.length() - 1);
                }
                else if (currResult
                        .matches(".*\\w\\p{InCombiningDiacriticalMarks}?\\w\\p{InCombiningDiacriticalMarks}?\\.$"))
                {
                    currResult = currResult.substring(0, currResult.length() - 1);
                }
                else if (currResult.matches(".*\\p{Punct}\\.$"))
                {
                    currResult = currResult.substring(0, currResult.length() - 1);
                }
            }

            currResult = removeOuterBrackets(currResult);

            if (currResult.length() == 0) return currResult;

        } while (!currResult.equals(prevResult));

        // if (!currResult.equals(origStr))
        // System.out.println(origStr + " -> "+ currResult);

        return currResult;
    }

    /**
     * Call cleanData on an entire set of Strings has a side effect of deleting
     * entries that are identical when they are cleaned.
     * 
     * @param values
     *            - the set to clean
     * @return Set<String> - the "same" set with all of its entries cleaned.
     */
    public static Set<String> cleanData(Set<String> values)
    {
        Set<String> result = new LinkedHashSet<String>();
        for (String entry : values)
        {
            String cleaned = cleanData(entry);
            result.add(cleaned);
        }
        return (result);
    }

    /**
     * Repeatedly removes trailing characters indicated in regular expression,
     * PLUS trailing period if it is preceded by its regular expression
     *
     * @param origStr
     *            String to clean
     * @param trailingCharsRegEx
     *            a regular expression of trailing chars to be removed (see java
     *            Pattern class). Note that the regular expression should NOT
     *            have '$' at the end. (e.g. " *[,/;:]" replaces any commas,
     *            slashes, semicolons or colons at the end of the string, and
     *            these chars may optionally be preceded by a space)
     * @param charsB4periodRegEx
     *            a regular expression that must immediately precede a trailing
     *            period IN ORDER FOR THE PERIOD TO BE REMOVED. Note that the
     *            regular expression will NOT have the period or '$' at the end.
     *            (e.g. "[a-zA-Z]{3,}" means at least three letters must
     *            immediately precede the period for it to be removed.)
     * @return cleaned string
     */
    public static String removeAllTrailingCharAndPeriod(String origStr, String trailingCharsRegEx,
            String charsB4periodRegEx)
    {
        if (origStr == null) return null;

        String currResult = origStr;
        String prevResult;
        do
        {
            prevResult = currResult;
            currResult = removeTrailingCharAndPeriod(currResult.trim(), trailingCharsRegEx, charsB4periodRegEx);

            if (currResult.length() == 0) return currResult;

        } while (!currResult.equals(prevResult));

        return currResult;
    }

    /**
     * Removes trailing characters indicated in regular expression, PLUS
     * trailing period if it is preceded by its regular expression.
     *
     * @param origStr
     *            String to clean
     * @param trailingCharsRegEx
     *            a regular expression of trailing chars to be removed (see java
     *            Pattern class). Note that the regular expression should NOT
     *            have '$' at the end. (e.g. " *[,/;:]" replaces any commas,
     *            slashes, semicolons or colons at the end of the string, and
     *            these chars may optionally be preceded by a space)
     * @param charsB4periodRegEx
     *            a regular expression that must immediately precede a trailing
     *            period IN ORDER FOR THE PERIOD TO BE REMOVED. Note that the
     *            regular expression will NOT have the period or '$' at the end.
     *            (e.g. "[a-zA-Z]{3,}" means at least three letters must
     *            immediately precede the period for it to be removed.)
     * @return cleaned string
     */
    public static String removeTrailingCharAndPeriod(String origStr, String trailingCharsRegEx,
            String charsB4periodRegEx)
    {
        if (origStr == null) return null;

        String result = removeTrailingChar(origStr, trailingCharsRegEx);

        result = removeTrailingPeriod(result, charsB4periodRegEx);

        return result;
    }

    /**
     * Remove the characters per the regular expression if they are at the end
     * of the string.
     * 
     * @param origStr
     *            string to be cleaned
     * @param charsToReplaceRegEx
     *            - a regular expression of the trailing string/chars to be
     *            removed e.g. " *([,/;:])" meaning last character is a comma,
     *            slash, semicolon, colon, possibly preceded by one or more
     *            spaces.
     * @see Pattern class in java api
     * @return the string with the specified trailing characters removed
     */
    public static String removeTrailingChar(String origStr, String charsToReplaceRegEx)
    {
        if (origStr == null) return origStr;
        // get rid of reg ex specified chars at the end of the string
        return origStr.trim().replaceAll(charsToReplaceRegEx + "$", "");
    }

    /**
     * If there is a period at the end of the string, remove the period if it is
     * immediately preceded by the regular expression
     * 
     * @param origStr
     *            the string to be cleaned
     * @param precedingCharsRegEx
     *            a regular expression that must immediately precede a trailing
     *            period IN ORDER FOR THE PERIOD TO BE REMOVED. Note that the
     *            regular expression will NOT have the period or '$' at the end.
     *            (e.g. "[a-zA-Z]{3,}" means at least three letters must
     *            immediately precede the period for it to be removed.)
     * @return the string without a trailing period iff the regular expression
     *         param was found immediately before the trailing period
     */
    public static String removeTrailingPeriod(String origStr, String precedingCharsRegEx)
    {
        if (origStr == null) return origStr;
        String result = origStr.trim();
        if (result.endsWith(".") && result.matches(".*" + precedingCharsRegEx + "\\.$"))
            result = result.substring(0, result.length() - 1).trim();

        return result;
    }

    /**
     * Remove single square bracket characters if they are the start and/or end
     * chars (matched or unmatched) and are the only square bracket chars in the
     * string.
     */
    public static String removeOuterBrackets(String origStr)
    {
        if (origStr == null || origStr.length() == 0) return origStr;

        String result = origStr.trim();

        if (result.length() > 0)
        {
            boolean openBracketFirst = result.charAt(0) == '[';
            boolean closeBracketLast = result.endsWith("]");
            if (openBracketFirst && closeBracketLast && result.indexOf('[', 1) == -1
                    && result.lastIndexOf(']', result.length() - 2) == -1)
                // only square brackets are at beginning and end
                result = result.substring(1, result.length() - 1);
            else if (openBracketFirst && result.indexOf(']') == -1)
                // starts with '[' but no ']'; remove open bracket
                result = result.substring(1);
            else if (closeBracketLast && result.indexOf('[') == -1)
                // ends with ']' but no '['; remove close bracket
                result = result.substring(0, result.length() - 1);
        }

        return result.trim();
    }

}
