package org.solrmarc.mixin;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.solrmarc.index.extractor.impl.custom.Mixin;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.tools.SolrMarcDataException;
import org.solrmarc.tools.SolrMarcDataException.eDataErrorLevel;

/**
 * Created with IntelliJ IDEA.
 * User: dueberb
 * Date: 1/30/15
 * Time: 10:30 AM
 * To change this template use File | Settings | File Templates.
 */

public class ISBNNormalizer implements Mixin {

    private static String ISBNDelimiterPattern = "\\-";

    public static final Pattern ISBN10Pat =
            Pattern.compile("^.*?(\\d[\\d\\-]{8,}[Xx]?)(?:\\D|\\Z).*$");

    public static final Pattern ISBN13Pat =
            Pattern.compile("^.*?(97[89][\\d\\-]{10,})(?:\\D|\\Z).*$");

    public static Collection<String> filterISBN(Collection <String> isbnList)
    {
        return(filterISBN(isbnList, "13"));
    }

    public static Collection<String> filterISBN(Collection <String> isbnList, String output)
    {
        Collection<String> result = Set.class.isAssignableFrom(isbnList.getClass()) ? new LinkedHashSet<String>() : new ArrayList<String>();
        boolean get13 = (output.equals("13") || output.equals("both"));
        boolean get10 = (output.equals("10") || output.equals("both"));
        if (!get13 && !get10) 
            throw new IndexerSpecException(null, null, IndexerSpecException.eErrorSeverity.ERROR, "Warning: method only accepts values \"10\" \"13\"  or \"both\"");
        for (String isbn : isbnList)
        {
            if (get13)
            {
                try {
                    String isbn13 = normalize_13(isbn);
                    if (isbn13 != null) result.add(isbn13);
                }
                catch (IllegalArgumentException e) {
                    throw (e);
                }
            }
            if (get10)
            {
                try {
                    String isbn10 = normalize_10(isbn);
                    if (isbn10 != null) result.add(isbn10);
                }
                catch (IllegalArgumentException e) {
                }
            }
        }
        return(result);
    }


    /**
     * Try to extract an ISBN from the string. 13s are returned as-is,
     * 10s are turned into an isbn13 and returned. Otherwise throw IllegalArgumentException
     *
     * @param isbnstring The string that may contain an ISBN
     * @return an ISBN13
     * @throws IllegalArgumentException
     */
    public static String normalize_13(String isbnstring) throws IllegalArgumentException {
        // First look for a 13,then a 10
        try {
            return extract_isbn13(isbnstring);
        } catch (IllegalArgumentException e) {
            return isbn10_to_13(extract_isbn10(isbnstring));
        }
    }

    /**
     * Try to extract an ISBN from the string. 13s are returned as-is,
     * 10s are turned into an isbn13 and returned. Otherwise throw IllegalArgumentException
     *
     * @param isbnstring The string that may contain an ISBN
     * @return an ISBN13
     * @throws IllegalArgumentException
     */
    public static String normalize_10(String isbnstring) throws IllegalArgumentException {
        // First look for a 10,then a 13
        try {
            return extract_isbn10(isbnstring);
        } catch (IllegalArgumentException e) {
            return isbn13_to_10(extract_isbn13(isbnstring));
        }
    }

    /**
     * @param isbnstring a String that might contain an ISBN
     * @param pat        The pattern to match against
     * @param len        The length of the ISBN you're looking for (10 or 13)
     * @return the extracted ISBN
     * @throws IllegalArgumentException if an ISBN isn't found
     */

    public static String extract_isbn_by_pat(String isbnstring, Pattern pat, Integer len) throws IllegalArgumentException {
        Matcher m = pat.matcher(isbnstring);
        if (!m.matches()) {
            throw new SolrMarcDataException(eDataErrorLevel.INFO, "'" + isbnstring + "' doesn't contain an ISBN" + len.toString());
        }

        String extracted_string = m.group(1);
        String normalized_string = extracted_string.replaceAll(ISBNDelimiterPattern, "");

        if (normalized_string.length() != len) {
            throw new SolrMarcDataException(eDataErrorLevel.INFO, "'" + normalized_string + "' doesn't contain an ISBN" + len.toString() + "; it's length is " + normalized_string.length());
        }
        return normalized_string;

    }

    public static String extract_isbn10(String isbnstring) throws IllegalArgumentException {
        String result = extract_isbn_by_pat(isbnstring, ISBN10Pat, 10);
        char checkDigit = getisbn10_check_digit(result);
        if (checkDigit != result.charAt(9))
            throw (new SolrMarcDataException(eDataErrorLevel.INFO, "Bad Check Digit for ISBN10"));
        return(result);
    }

    public static String extract_isbn13(String isbnstring) throws IllegalArgumentException {
        String result = extract_isbn_by_pat(isbnstring, ISBN13Pat, 13);
        char checkDigit = getisbn13_check_digit(result);
        if (checkDigit != result.charAt(12))
            throw (new SolrMarcDataException(eDataErrorLevel.INFO, "Bad Check Digit for ISBN13"));
        return(result);
    }

    /**
     * Turn an already-extracted ISBN10 into an ISBN13
     *
     * @param isbn10 -- just the raw digits (plus possible 'X') of an ISBN10
     * @return the equivalent ISBN13
     */

    public static String isbn10_to_13(String isbn10) {
        String longisbn = "978" + isbn10.substring(0, 9);
        char checkDigit = getisbn13_check_digit(longisbn);
        return longisbn + checkDigit;
    }

    public static char getisbn13_check_digit(String isbn13) {

        int[] digits = new int[12];
        for (int i = 0; i < 12; i++) {
            digits[i] = new Integer(isbn13.substring(i, i + 1));
        }

        Integer sum = 0;
        for (int i = 0; i < 12; i++) {
            sum = sum + digits[i] + (2 * digits[i] * (i % 2));
        }

        // Get the smallest multiple of ten > sum
        Integer top = sum + (10 - (sum % 10));
        Integer check = top - sum;
        if (check == 10) {
            return '0';
        } else {
            return check.toString().charAt(0);
        }

    }
    /**
     * Turn an already-extracted ISBN13 into an ISBN10
     *
     * @param isbn13 -- just the raw digits of an ISBN13
     * @return the equivalent ISBN10 (if possible)
     */

    public static String isbn13_to_10(String isbn13) {
        if (!isbn13.substring(0,3).equals("978"))
            throw new IllegalArgumentException("13-digit ISBN '" + isbn13 + "' doesn't start with 978, cannot make a valid 10-digit ISBN for it.");
        String shortisbn = isbn13.substring(3, 12);

        char checkDigit = getisbn10_check_digit(shortisbn);

        String result = shortisbn + checkDigit;
        return(result);

    }

    public static char getisbn10_check_digit(String isbn10) {
        int[] multVect = { 10, 9, 8, 7, 6, 5, 4, 3, 2 };
        int sum = 0; 

        for (int i = 0; i < 9; i++) {
            int digit = new Integer(isbn10.substring(i, i + 1));
            sum += multVect[i] * digit;
        }
        int val = 11 - (sum % 11);
        char checkDigit = (char)((val == 11) ? '0' : (val == 10) ? 'X' : (char)val + '0');
        return (checkDigit);
    }
}
