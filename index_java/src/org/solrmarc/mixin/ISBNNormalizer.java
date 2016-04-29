package org.solrmarc.mixin;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import playground.solrmarc.index.extractor.impl.custom.Mixin;

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
            Pattern.compile("^.*?(978[\\d\\-]{10,})(?:\\D|\\Z).*$");

    public static Collection<String> filterISBN(Collection <String> isbnList, String output)
    {
    	Collection<String> result = Set.class.isAssignableFrom(isbnList.getClass()) ? new LinkedHashSet<String>() : new ArrayList<String>();
		boolean get13 = (output.equals("13") || output.equals("both"));
		boolean get10 = (output.equals("10") || output.equals("both"));
    	for (String isbn : isbnList)
    	{
			if (get13)
			{
	    		String isbn13 = normalize(isbn);
	    		if (isbn13 != null) result.add(isbn13);
	    	}
			if (get10)
			{
		        try {
		        	String isbn10 = extract_isbn10(isbn);
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
    public static String normalize(String isbnstring) throws IllegalArgumentException {
        // First look for a 13,then a 10
        try {
            return extract_isbn13(isbnstring);
        } catch (IllegalArgumentException e) {
            return isbn10_to_13(extract_isbn10(isbnstring));
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
            throw new IllegalArgumentException(isbnstring + " doesn't contain an ISBN" + len.toString());
        }

        String extracted_string = m.group(1);
        String normalized_string = extracted_string.replaceAll(ISBNDelimiterPattern, "");

        if (normalized_string.length() != len) {
            throw new IllegalArgumentException("'" + normalized_string + "' doesn't contain an ISBN" + len.toString() + "; it's length is " + normalized_string.length());
        }
        return normalized_string;

    }

    public static String extract_isbn10(String isbnstring) throws IllegalArgumentException {
        return extract_isbn_by_pat(isbnstring, ISBN10Pat, 10);
    }

    public static String extract_isbn13(String isbnstring) throws IllegalArgumentException {
        return extract_isbn_by_pat(isbnstring, ISBN13Pat, 13);
    }

    /**
     * Turn an already-extracted ISBN10 into an ISBN13
     *
     * @param isbn10 -- just the raw digits (plus possible 'X') of an ISBN10
     * @return the equivalent ISBN13
     */

    public static String isbn10_to_13(String isbn10) {
        String longisbn = "978" + isbn10.substring(0, 9);


        int[] digits = new int[12];
        for (int i = 0; i < 12; i++) {
            digits[i] = new Integer(longisbn.substring(i, i + 1));
        }

        Integer sum = 0;
        for (int i = 0; i < 12; i++) {
            sum = sum + digits[i] + (2 * digits[i] * (i % 2));
        }

        // Get the smallest multiple of ten > sum
        Integer top = sum + (10 - (sum % 10));
        Integer check = top - sum;
        if (check == 10) {
            return longisbn + "0";
        } else {
            return longisbn + check.toString();
        }

    }

}
