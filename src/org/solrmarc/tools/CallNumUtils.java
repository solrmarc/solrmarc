package org.solrmarc.tools;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Call number utility functions for solrmarc.
 * This Entire class has been relocated to org.solrmarc.callnum.CallNumUtils call the method there directly.
 * All this class does is delegate all calls to the corresponding static methods in the new location.
 *
 * @author Naomi Dushay, Stanford University
 */

@Deprecated
public final class CallNumUtils
{
    /**
     * Default Constructor: private, so it can't be instantiated by other objects
     */
    private CallNumUtils() { }


//------ public methods --------

    /**
     * given a possible Library of Congress call number value, determine if it
     *  matches the pattern of an LC call number
     *
     * @param possLCval  possible call number to check
     * @return           true if parses as a valid LC call number
     */
    public static final boolean isValidLC(String possLCval)
    {
        return org.solrmarc.callnum.CallNumUtils.isValidLC(possLCval);
    }

    /**
     * given a possible Dewey call number value, determine if it
     *  matches the pattern of an Dewey call number
     *
     * @param possDeweyVal  possible call number to check
     * @return              true if parses as a valid Dewey call number with a cutter
     */
    public static final boolean isValidDeweyWithCutter(String possDeweyVal)
    {
        return org.solrmarc.callnum.CallNumUtils.isValidDeweyWithCutter(possDeweyVal);
    }

    /**
     * given a possible Dewey call number value, determine if it
     *  matches the pattern of an Dewey call number
     *
     * @param possDeweyVal  possible call number to check
     * @return              true if parses as a valid Dewey call number
     */
    public static final boolean isValidDewey(String possDeweyVal)
    {
        return org.solrmarc.callnum.CallNumUtils.isValidDewey(possDeweyVal);
    }

    /**
     * return the portion of the call number string that occurs before the
     *  Cutter, NOT including any class suffixes occuring before the cutter
     *
     * @param callnum  call number to parse
     * @return         the part of the call number before the cutter
     */
    public static final String getPortionBeforeCutter(String callnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getPortionBeforeCutter(callnum);
    }

    /**
     * return the portion of the LC call number string that occurs before the
     *  Cutter.
     *
     * @param callnum  LC call number to parse
     * @return         the part of the LC call number before the first cutter
     */
    public static final String getLCB4FirstCutter(String callnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getLCB4FirstCutter(callnum);
    }

    /**
     * Given a raw LC call number, return the initial letters (before any
     *  numbers)
     *
     * @param rawLCcallnum  LC call number to parse
     * @return              the initial letters of the LC classification
     */
    public static String getLCstartLetters(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getLCstartLetters(rawLCcallnum);
    }

    /**
     * return the numeric portion of the required portion of the LC classification.
     *  LC classification requires
     *    1-3 capital letters followed by  float number (may be an integer)
     *
     * @param rawLCcallnum the entire LC call number, as a string
     * @return             the numeric portion of the classification
     */
    public static String getLCClassDigits(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getLCClassDigits(rawLCcallnum);
    }

    /**
     * return the string between the LC class number and the cutter, if it
     *  starts with a digit, null otherwise
     *
     * @param rawLCcallnum the entire LC call number, as a string
     * @return             the portion of the string between the classification
     *                     number and the cutter, or null if there is none.
     */
    public static String getLCClassSuffix(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getLCClassSuffix(rawLCcallnum);
    }

    /**
     * return the first cutter in the LC call number, without the preceding
     * characters (such as the "required" period, which is sometimes missing,
     * or spaces), or any suffixes
     *
     * @param rawLCcallnum the entire LC call number, as a string
     * @return             the first cutter of the call number without any suffix
     */
    public static String getFirstLCcutter(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getFirstLCcutter(rawLCcallnum);
    }

    /**
     * return the suffix after the first cutter, if there is one.  This occurs
     *  before the second cutter, if there is one.
     *
     * @param rawLCcallnum the entire LC call number, as a string
     * @return             the suffix of the first cutter of the call number
     */
    public static String getFirstLCcutterSuffix(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getFirstLCcutterSuffix(rawLCcallnum);
    }

    /**
     * return the second cutter in the call number, without the preceding
     * characters (such as the "required" period, which is sometimes missing,
     * or spaces), or any suffixes
     *
     * @param rawLCcallnum the entire LC call number, as a string
     * @return             the second cutter of the call number without any suffix
     */
    public static String getSecondLCcutter(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getSecondLCcutter(rawLCcallnum);
    }

    /**
     * return the suffix after the second cutter, if there is one.  This occurs
     *  before the second cutter, if there is one.
     *
     * @param rawLCcallnum the entire LC call number, as a string
     * @return             the suffix of the second cutter of the call number
     */
    public static String getSecondLCcutterSuffix(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getSecondLCcutterSuffix(rawLCcallnum);
    }

    /**
     * return the suffix after the second cutter, if there is one.  This occurs
     *  before the second cutter, if there is one.
     *
     * @param rawLCcallnum the entire LC call number, as a string
     * @return             the year suffix of the second cutter of the call number
     * @deprecated
     */
// do we want to separate out year suffixes?  for all or just here? - unused
    public static String getSecondLCcutterYearSuffix(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getSecondLCcutterYearSuffix(rawLCcallnum);
    }

    /**
     * return the portion of the Dewey call number string that occurs before the
     *  Cutter.
     *
     * @param callnum  call number to parse
     * @return         the part of the call number before the cutter
     */
    public static final String getDeweyB4Cutter(String callnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getDeweyB4Cutter(callnum);
    }

    /**
     * return the first cutter in the call number, without the preceding
     * characters (such as the "required" period, which is sometimes missing,
     * or spaces).
     *
     * @param rawCallnum  the entire call number, as a string
     * @return            the cutter of the input call number
     */
    public static String getDeweyCutter(String rawCallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getDeweyCutter(rawCallnum);
    }

    /**
     * return suffix to the first cutter in the dewey call number
     *
     * @param rawCallnum  the entire call number, as a string
     * @return            the suffix of the first cutter of the input call number
     */
    public static String getDeweyCutterSuffix(String rawCallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getDeweyCutterSuffix(rawCallnum);
    }


    /**
     * Used to improve call num sorting and volume lopping.
     * Remove leading and trailing whitespace, ensure whitespace is always a
     *  single space, remove spaces after periods, remove trailing periods
     *
     * @param rawCallnum  a non-null String containing a Dewey call number
     * @return            the normalized form of the call number
     */
    public static String normalizeCallnum(String rawCallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.normalizeCallnum(rawCallnum);
    }

    /**
     * given a raw LC call number, return the shelf key - a sortable version
     *  of the call number
     *
     * @param rawLCcallnum  a non-null String containing an LC call number
     * @param recid         record id (unused)
     * @return              the normalized form of the call number
     */
    public static String getLCShelfkey(String rawLCcallnum, String recid)
    {
        return org.solrmarc.callnum.CallNumUtils.getLCShelfkey(rawLCcallnum, recid);
    }

    /**
     * normalize a suffix for shelf list sorting by changing all digit
     *  substrings to a constant length (left padding with zeros).
     *
     * @param suffix  the suffix of a call number
     * @return        the suffix normalized for sorting, left-padded with zeros)
     */
    public static String normalizeSuffix(String suffix)
    {
        return org.solrmarc.callnum.CallNumUtils.normalizeSuffix(suffix);
    }

    /**
     * given a shelfkey (a lexicaly sortable call number), return the reverse
     * shelf key - a sortable version of the call number that will give the
     * reverse order (for getting "previous" call numbers in a list)
     *
     * @param shelfkey  the forward-sortable shelfkey that has been computed for a
     *                  call number
     * @return          a reverse-order version of the shelfkey
     */
    public static String getReverseShelfKey(String shelfkey)
    {
        return org.solrmarc.callnum.CallNumUtils.getReverseShelfKey(shelfkey);
    }


    /**
     * for non alpha numeric characters, return a character that will sort
     *  first or last, whichever is the opposite of the original character.
     *
     * @param ch  original character
     * @return    character that sorts opposite of input
     */
    public static char[] reverseNonAlphanum(char ch)
    {
        return org.solrmarc.callnum.CallNumUtils.reverseNonAlphanum(ch);
    }

    /**
     * given a raw Dewey call number, return the shelf key - a sortable
     *  version of the call number
     *
     * @param rawDeweyCallnum  call number
     * @return                 shelfkey
     */
    public static String getDeweyShelfKey(String rawDeweyCallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.getDeweyShelfKey(rawDeweyCallnum);
    }


    /**
     * normalizes numbers (can have decimal portion) to (digitsB4) before
     *  the decimal (adding leading zeroes as necessary) and (digitsAfter
     *  after the decimal.  In the case of a whole number, there will be no
     *  decimal point.
     *
     * @param floatStr    the number, as a String
     * @param digitsB4    the number of characters the result should have before the
     *                    decimal point (leading zeroes will be added as necessary). A negative
     *                    number means leave whatever digits encountered as is; don't pad with leading zeroes.
     * @param digitsAfter the number of characters the result should have after
     *                    the decimal point.  A negative number means leave whatever fraction
     *                    encountered as is; don't pad with trailing zeroes (trailing zeroes in
     *                    this case will be removed)
     * @return            normalized number
     * @throws NumberFormatException if string can't be parsed as a number
     */
    public static String normalizeFloat(String floatStr, int digitsB4, int digitsAfter)
    {
        return org.solrmarc.callnum.CallNumUtils.normalizeFloat(floatStr, digitsB4, digitsAfter);
    }

    /**
     * remove volume information from LC call number if it is present as a
     *   suffix
     *
     * @param rawLCcallnum call number
     * @return             call number without the volume information, or full call 
     *                     number if no volume information was present.
     */
    public static String removeLCVolSuffix(String rawLCcallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.removeLCVolSuffix(rawLCcallnum);
    }


    /**
     * remove volume information from Dewey call number if it is present as a
     *   suffix
     *
     * @param rawDeweyCallnum  call number
     * @return                 call number without the volume information, or full
     *                         call number if no volume information was present.
     */
    public static String removeDeweyVolSuffix(String rawDeweyCallnum)
    {
        return org.solrmarc.callnum.CallNumUtils.removeDeweyVolSuffix(rawDeweyCallnum);
    }


    /**
     * adds leading zeros to a dewey call number, when they're missing.
     *
     * @param deweyCallNum call number
     * @return             the dewey call number with leading zeros
     */
    public static String addLeadingZeros(String deweyCallNum)
    {
        return org.solrmarc.callnum.CallNumUtils.addLeadingZeros(deweyCallNum);
    }
}
