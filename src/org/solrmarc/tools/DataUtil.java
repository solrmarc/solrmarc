package org.solrmarc.tools;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.text.Normalizer.Form;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;

public class DataUtil
{
    private final static String TWO_DIGIT_PREFIXES = "(20|19|18|17|16|15|14|13|12|11|10)";
    private final static Pattern FOUR_DIGIT_PATTERN_BRACES = Pattern.compile("\\[[12]\\d{3,3}\\]");
    private final static Pattern FOUR_DIGIT_PATTERN_ONE_BRACE = Pattern.compile("\\[[12]\\d{3,3}");
    private final static Pattern FOUR_DIGIT_PATTERN_STARTING_WITH_1_2 = Pattern
            .compile(TWO_DIGIT_PREFIXES + "[0-9][0-9]");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_1 = Pattern.compile("l\\d{3,3}");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_2 = Pattern.compile("\\[19\\]\\d{2,2}");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_3 = Pattern.compile(TWO_DIGIT_PREFIXES + "[0-9][-?0-9]");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_4 = Pattern.compile("i.e. " + TWO_DIGIT_PREFIXES+ "[0-9][0-9]");
    private final static Pattern BC_DATE_PATTERN = Pattern.compile("[0-9]+ [Bb][.]?[Cc][.]?");
    private final static Pattern FOUR_DIGIT_PATTERN = Pattern.compile("\\d{4,4}");
    protected static Logger logger = Logger.getLogger(DataUtil.class.getName());

    private static Pattern ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static Pattern PUNCT_OR_SPACE = Pattern.compile("[ \\p{Punct}]+", Pattern.UNICODE_CHARACTER_CLASS);

    
    /**
     * Cleans non-digits from a String
     *
     * @param date
     *            String to parse
     * @return Numeric part of date String (or null)
     */
    public static String cleanDate(final String date)
    {
        Matcher matcher_braces = FOUR_DIGIT_PATTERN_BRACES.matcher(date);
        Matcher matcher_one_brace = FOUR_DIGIT_PATTERN_ONE_BRACE.matcher(date);
        Matcher matcher_start_with_1_2 = FOUR_DIGIT_PATTERN_STARTING_WITH_1_2.matcher(date);
        Matcher matcher_l_plus_three_digits = FOUR_DIGIT_PATTERN_OTHER_1.matcher(date);
        Matcher matcher_bracket_19_plus_two_digits = FOUR_DIGIT_PATTERN_OTHER_2.matcher(date);
        Matcher matcher_three_digits_plus_unk = FOUR_DIGIT_PATTERN_OTHER_3.matcher(date);
        Matcher matcher_ie_date = FOUR_DIGIT_PATTERN_OTHER_4.matcher(date);
        Matcher matcher = FOUR_DIGIT_PATTERN.matcher(date);
        Matcher matcher_bc_date = BC_DATE_PATTERN.matcher(date);

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
     *
     * @param origStr  text string with possible enclosing brackets
     * @return         a copy of the text string with the brackets removed
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

    /**
     * Change string to have initial Capital letters on all words and lower case
     * elsewhere.
     * @param s the string to change
     * @return The Title Case Version Of The String
     */
    public static String toTitleCase(String s)
    {
        final String ACTIONABLE_DELIMITERS = " .-/"; // these cause the character following
                                                     // to be capitalized
        final String QUESTIONABLE_DELIMITERS = "'"; // these might cause the character following
                                                    // to be capitalized

        boolean hasNoLowerCase = true;
        for (char c : s.toCharArray())
        {
            if (Character.isLowerCase(c))  hasNoLowerCase = false;
        }

        StringBuilder sb = new StringBuilder();
        int countSinceActionable = 0;

        char prevChar = ' ';
        for (char c : s.toCharArray())
        {
            boolean cIsUpper = Character.isUpperCase(c);
            boolean cIsLower = Character.isLowerCase(c);
            boolean curActionable = (ACTIONABLE_DELIMITERS.indexOf(c) >= 0);
            boolean prevActionable = (ACTIONABLE_DELIMITERS.indexOf(prevChar) >= 0);
            boolean prevQuestionable = (QUESTIONABLE_DELIMITERS.indexOf(prevChar) >= 0);
            boolean toUpper = false;
            boolean toLower = false;
            if (prevActionable && cIsLower)  toUpper = true;
            else if (prevQuestionable && cIsLower && countSinceActionable <= 2) toUpper = true;
            else if (hasNoLowerCase && cIsUpper && countSinceActionable >= 1) toLower = true;
            countSinceActionable = (curActionable) ? 0 : countSinceActionable+1;

            char newC = (toUpper) ? Character.toUpperCase(c) : (toLower) ? Character.toLowerCase(c) : c ;
            sb.append(newC);
            prevChar = c;
        }
        return sb.toString();
    }
    
    public static EnumSet<FieldFormatter.eCleanVal> getCleanValForParam(String params )
    {
        EnumSet<FieldFormatter.eCleanVal> result = EnumSet.noneOf(eCleanVal.class);
        for (FieldFormatter.eCleanVal cleanVal : EnumSet.allOf(FieldFormatter.eCleanVal.class))
        {
            if (params.contains(cleanVal.toString()))  result.add(cleanVal);
        }
        if (params.contains("titleSortUpper"))
        {
            result.addAll(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.STRIP_ACCCENTS,
                    eCleanVal.STRIP_ALL_PUNCT, eCleanVal.TO_UPPER, eCleanVal.STRIP_INDICATOR));
        }
        if (params.contains("titleSortLower"))
        {
            result.addAll(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.STRIP_ACCCENTS,
                    eCleanVal.STRIP_ALL_PUNCT, eCleanVal.TO_LOWER, eCleanVal.STRIP_INDICATOR));
        }
        if (params.matches(".*clean([^E].*|$)"))
        {
            result.addAll(EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.CLEAN_END));
        }
        return(result);
    }
    
    public static String cleanByVal(String input,  EnumSet<FieldFormatter.eCleanVal> cleanVal)
    {        
        String str = (cleanVal.contains(eCleanVal.CLEAN_EACH)) ? DataUtil.cleanData(input) : input;
    
        if (!cleanVal.contains(eCleanVal.STRIP_ACCCENTS) && !cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT)
                && !cleanVal.contains(eCleanVal.TO_LOWER) && !cleanVal.contains(eCleanVal.TO_UPPER)
                && !cleanVal.contains(eCleanVal.TO_TITLECASE) && !cleanVal.contains(eCleanVal.STRIP_INDICATOR_1) 
                && !cleanVal.contains(eCleanVal.STRIP_INDICATOR_2) && !cleanVal.contains(eCleanVal.STRIP_INDICATOR))
        {
            return (str);
        }
        // Do more extensive cleaning of data.
        if (cleanVal.contains(eCleanVal.STRIP_ACCCENTS))
        {
            str = DataUtil.stripAccents(str);
        }
        if (cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT)) 
        {
            str = DataUtil.stripAllPunct(str);
        }
        if (!cleanVal.contains(eCleanVal.UNTRIMMED))  str = str.trim();
    
        if (cleanVal.contains(eCleanVal.TO_LOWER))
        {
            str = str.toLowerCase();
        }
        else if (cleanVal.contains(eCleanVal.TO_UPPER))
        {
            str = str.toUpperCase();
        }
        else if (cleanVal.contains(eCleanVal.TO_TITLECASE))
        {
            str = DataUtil.toTitleCase(str);
        }
        return str;
    }

    
    public static String stripAllPunct(String str)
    {
        String str1 = str.replaceAll("( |\\p{Punct})+", " ");
        String str2 = PUNCT_OR_SPACE.matcher(str).replaceAll(" ");
        if (str1.equals(str2)) 
        {
            str = str1;
        }
        else
        {
            str = str2;
            str = str.replaceAll("( |\\p{Punct})+", " ");
        }
        return(str);
    }
    
    public static String stripAccents(String str)
    {
        str = ACCENTS.matcher(Normalizer.normalize(str, Form.NFD)).replaceAll("");
        StringBuilder folded = new StringBuilder();
        boolean replaced = false;
        for (char c : str.toCharArray())
        {
            char newc = Utils.foldDiacriticLatinChar(c);
            if (newc != 0x00)
            {
                folded.append(newc);
                replaced = true;
            }
            else
            {
                folded.append(c);
            }
        }
        if (replaced) str = folded.toString();
        return(str);
    }
}
