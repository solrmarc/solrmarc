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

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.*;

import org.marc4j.ErrorHandler;

import com.solrmarc.icu.lang.UCharacter;

/**
 * Call number utility functions for solrmarc
 * 
 * @author Naomi Dushay, Stanford University
 */

public final class CallNumUtils {
    

// TODO:  should have LCcallnum and DeweyCallnum classes, with the call number
//   pieces as fields.  Then parsing would happen once per call number, not
//   all over the place and some parsing repeated.
    
    /**
     * Default Constructor: private, so it can't be instantiated by other objects
     */ 
    private CallNumUtils(){ }
    
    public static final Pattern DEWEY_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d+)?.*");
    /**
     * regular expression string for the required portion of the LC classification
     *  LC classification is 
     *    1-3 capital letters followed by  float number (may be an integer)
     *    optionally followed by a space and then a year or other number, 
     *      e.g. "1987" "15th"
     * LC call numbers can't begin with I, O, W, X, or Y 
     */
    public static final String LC_CLASS_REQ_REGEX = "[A-Z&&[^IOWXY]]{1}[A-Z]{0,2} *\\d+(\\.\\d+)?";

    /**
     * non-cutter text that can appear before or after cutters
     */
    public static final String NOT_CUTTER = "([\\da-z]\\w*)|([A-Z]\\D+[\\w]*)";
    
    /**
     * the full LC classification string (can have an optional suffix after LC class)
     */
    public static final String LC_CLASS_W_SUFFIX = "(" + LC_CLASS_REQ_REGEX + "( +" + NOT_CUTTER + ")?)";
    
    /**
     * regular expression string for the cutter, without preceding characters 
     * (such as the "required" period, which is sometimes missing, or spaces).
     * A Cutter is a single letter followed by digits.  
     */
    public static final String CUTTER_REGEX = "[A-Z]\\d+";
    
    /**
     * the full LC classification string, followed by the first cutter
     */
    public static final String LC_CLASS_N_CUTTER = LC_CLASS_W_SUFFIX + " *\\.?" + CUTTER_REGEX;
    public static final Pattern LC_CLASS_N_CUTTER_PATTERN = Pattern.compile(LC_CLASS_N_CUTTER + ".*");
        
    /**
     * regular expression for Dewey classification.
     *  Dewey classification is a three digit number (possibly missing leading
     *   zeros) with an optional fraction portion.
     */
    public static final String DEWEY_CLASS_REGEX = "\\d{1,3}(\\.\\d+)?";

    /**
     * Dewey cutters start with a letter, followed by a one to three digit 
     * number. The number may be followed immediately (i.e. without space) by 
     * letters, or followed first by a space and then letters. 
     */
    public static final String DEWEY_MIN_CUTTER_REGEX = "[A-Z]\\d{1,3}";
    public static final String DEWEY_CUTTER_TRAILING_LETTERS_REGEX = DEWEY_MIN_CUTTER_REGEX + "[A-Z]+";
    public static final String DEWEY_CUTTER_SPACE_TRAILING_LETTERS_REGEX = DEWEY_MIN_CUTTER_REGEX + " +[A-Z]+";
    public static final String DEWEY_FULL_CUTTER_REGEX = DEWEY_MIN_CUTTER_REGEX + " *[A-Z]*+";

    /**
     * the full Dewey classification string, followed by the first cutter
     */
    public static final String DEWEY_CLASS_N_CUTTER_REGEX = DEWEY_CLASS_REGEX + " *\\.?" + DEWEY_FULL_CUTTER_REGEX;
    public static final Pattern DEWEY_CLASS_N_CUTTER_PATTERN = Pattern.compile(DEWEY_CLASS_N_CUTTER_REGEX + ".*");
    
    
    private static Map<Character, Character> alphanumReverseMap = new HashMap<Character, Character>();
    static {
        alphanumReverseMap.put('0', 'Z');
        alphanumReverseMap.put('1', 'Y');
        alphanumReverseMap.put('2', 'X');
        alphanumReverseMap.put('3', 'W');
        alphanumReverseMap.put('4', 'V');
        alphanumReverseMap.put('5', 'U');
        alphanumReverseMap.put('6', 'T');
        alphanumReverseMap.put('7', 'S');
        alphanumReverseMap.put('8', 'R');
        alphanumReverseMap.put('9', 'Q');
        alphanumReverseMap.put('A', 'P');
        alphanumReverseMap.put('B', 'O');
        alphanumReverseMap.put('C', 'N');
        alphanumReverseMap.put('D', 'M');
        alphanumReverseMap.put('E', 'L');
        alphanumReverseMap.put('F', 'K');
        alphanumReverseMap.put('G', 'J');
        alphanumReverseMap.put('H', 'I');
        alphanumReverseMap.put('I', 'H');
        alphanumReverseMap.put('J', 'G');
        alphanumReverseMap.put('K', 'F');
        alphanumReverseMap.put('L', 'E');
        alphanumReverseMap.put('M', 'D');
        alphanumReverseMap.put('N', 'C');
        alphanumReverseMap.put('O', 'B');
        alphanumReverseMap.put('P', 'A');
        alphanumReverseMap.put('Q', '9');
        alphanumReverseMap.put('R', '8');
        alphanumReverseMap.put('S', '7');
        alphanumReverseMap.put('T', '6');
        alphanumReverseMap.put('U', '5');
        alphanumReverseMap.put('V', '4');
        alphanumReverseMap.put('W', '3');
        alphanumReverseMap.put('X', '2');
        alphanumReverseMap.put('Y', '1');
        alphanumReverseMap.put('Z', '0');
    }
    
    
    /** this character will sort first */
    public static char SORT_FIRST_CHAR = Character.MIN_VALUE;
    public static StringBuilder reverseDefault = new StringBuilder(75);
    static {
        for (int i = 0; i < 50; i++) 
// N.B.:  this char is tough to deal with in a variety of contexts.  
// Hopefully diacritics and non-latin won't bite us in the butt.
//          reverseDefault.append(Character.toChars(Character.MAX_CODE_POINT));
            reverseDefault.append(Character.toChars('~'));
    }

//------ public methods --------    
    
    /**
     * given a possible Library of Congress call number value, determine if it
     *  matches the pattern of an LC call number
     */
    public static final boolean isValidLC(String possLCval)
    {
        if (possLCval != null && LC_CLASS_N_CUTTER_PATTERN.matcher(possLCval.trim()).matches())
            return true;
        return false;
    }

    /**
     * given a possible Dewey call number value, determine if it
     *  matches the pattern of an Dewey call number
     */
    public static final boolean isValidDeweyWithCutter(String possDeweyVal)
    {
        if (possDeweyVal != null && DEWEY_CLASS_N_CUTTER_PATTERN.matcher(possDeweyVal.trim()).matches())
            return true;
        return false;
    }
   
   /**
     * given a possible Dewey call number value, determine if it
     *  matches the pattern of an Dewey call number
     */
    public static final boolean isValidDewey(String possDeweyVal)
    {
        if (possDeweyVal != null && DEWEY_PATTERN.matcher(possDeweyVal.trim()).matches())
            return true;
        return false;
    }

    /**
     * return the portion of the call number string that occurs before the 
     *  Cutter, NOT including any class suffixes occuring before the cutter
     */
    public static final String getPortionBeforeCutter(String callnum) {
    
        // cutter is a single letter followed by digits.
        // there may be a space before a cutter
        // there should be a period, which is followed by a single letter
        //   the period is sometimes missing
        // For Dewey callnumber, there may be a slash instead of a cutter, 
        //  or there might be NO cutter
        String beginCutterRegex = "( +|(\\.[A-Z])| */)";  
        
        String[] pieces = callnum.split(beginCutterRegex);
        if (pieces.length == 0 || pieces[0] == null || pieces[0].length() == 0)
            return null;
        else
            return pieces[0].trim();
    }

    /**
     * return the portion of the LC call number string that occurs before the 
     *  Cutter.
     */
    public static final String getLCB4FirstCutter(String callnum) {
        String result = null;
        
        String cutter = getFirstLCcutter(callnum);
        if (cutter != null && cutter.length() > 0) {
            // lc class can start with same chars as first cutter: (G384 G3)
            int ix = callnum.indexOf(cutter);
            String lets = getLCstartLetters(callnum);
            if (ix < lets.length())
                ix = callnum.indexOf(cutter, lets.length());
    
            if (ix > 0) {
                result = callnum.substring(0, ix).trim();               
                if (result.endsWith("."))
                    result = result.substring(0, result.length() - 1).trim();
            }
            else
                result = callnum;
        }
        else // no cutter 
            result = callnum;
    
        return result;
    }

    /** 
     * Given a raw LC call number, return the initial letters (before any
     *  numbers)
     */
    public static String getLCstartLetters(String rawLCcallnum) {
        String result = null;
        if (rawLCcallnum != null && rawLCcallnum.length() > 0) {
            String [] lcClass = rawLCcallnum.split("[^A-Z]+");
            if (lcClass.length > 0)
                result = lcClass[0];
        }
        return result;
    }

    /**
     * return the numeric portion of the required portion of the LC classification.
     *  LC classification requires
     *    1-3 capital letters followed by  float number (may be an integer)
     * @param rawLCcallnum
     */
    public static String getLCClassDigits(String rawLCcallnum) {
        String result = null;
    
        String rawClass = getLCB4FirstCutter(rawLCcallnum);
        if (rawClass != null && rawClass.length() > 0) {
            String [] pieces = rawClass.split("[A-Z ]+");
            if (pieces.length > 1)
                result = pieces[1].trim();
        }
        return result;
    }

    /**
     * return the string between the LC class number and the cutter, if it
     *  starts with a digit, null otherwise
     * @param rawLCcallnum - the entire LC call number, as a string
     */
    public static String getLCClassSuffix(String rawLCcallnum) {
        String result = null;
        
        String b4cutter = getLCB4FirstCutter(rawLCcallnum);
        if (b4cutter == null || b4cutter.length() == 0)
            return null;
        
        String classDigits = getLCClassDigits(rawLCcallnum);
        
        if (classDigits != null && classDigits.length() > 0) {
            int reqClassLen = b4cutter.indexOf(classDigits) + classDigits.length();
    
            if (b4cutter.length() > reqClassLen)
                result = b4cutter.substring(reqClassLen).trim();
        }
        
        return result;
    }

    /**
     * return the first cutter in the LC call number, without the preceding 
     * characters (such as the "required" period, which is sometimes missing, 
     * or spaces), or any suffixes
     * @param rawCallnum - the entire call number, as a string
     */
    public static String getFirstLCcutter(String rawCallnum) {
        String result = null;
    
        String regex = LC_CLASS_W_SUFFIX + " *\\.?(" + CUTTER_REGEX + ")";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawCallnum);
    
        if (matcher.find())
            result = matcher.group(6).trim();
    
        // if no well formed cutter, take the chunk after last period or space 
        //  if it begins with a letter
        if (result == null) {
            int i = rawCallnum.trim().lastIndexOf('.');  // period
            if (i == -1)
                i = rawCallnum.trim().lastIndexOf(' ');  // space
            if (rawCallnum.trim().length() > i+1) {
                String possible = rawCallnum.trim().substring(i+1).trim();
                if (Character.isLetter(possible.charAt(0)))
                    result = possible;
            }
        }
        
        return result;
    }

    /**
     * return the suffix after the first cutter, if there is one.  This occurs
     *  before the second cutter, if there is one.
     * @param rawLCcallnum - the entire LC call number, as a string
     */
    public static String getFirstLCcutterSuffix(String rawLCcallnum) {
        String result = null;
    
        String regex = LC_CLASS_N_CUTTER + " *(" + NOT_CUTTER + ")*"; 
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawLCcallnum);
    
        // non cutter string optionally followed by cutter preceded by a period
        if (matcher.find() && matcher.groupCount() > 5 
                && matcher.group(6) != null && matcher.group(6).length() > 0) {
    
            // this only grabs the FIRST non-cutter string it encounters after
            //   the first cutter
            result = matcher.group(6).trim();
            
            // this is to cope with additional non-cutter strings after the
            //  first cutter  (e.g. M211 .M93 K.240 1988)
            int endLastIx = matcher.end(6); // end of previous match
            if (endLastIx < rawLCcallnum.length()) {
                // if there is a suffix, there must be a period before second cutter
                Pattern cutterPat = Pattern.compile(" *\\." + CUTTER_REGEX);
                matcher.usePattern(cutterPat);
                if (matcher.find(endLastIx)) {
                    if (endLastIx < matcher.start())
                        result = result.trim() + " " + rawLCcallnum.substring(endLastIx, matcher.start()).trim();
                }
                else
                    result = result + rawLCcallnum.substring(endLastIx);
            }
        }
        else {
            // string after first cutter looks like a second cutter, but is
            //  not because further on there is a second cutter preceded by
            //  a period.
            // look for period before second cutter
            String afterLCclassNCutter = rawLCcallnum.replaceFirst(LC_CLASS_N_CUTTER + " *", "");
            String cutterRegex = LC_CLASS_N_CUTTER + " *(.*)\\." + CUTTER_REGEX; 
            
            pattern = Pattern.compile(cutterRegex);
            matcher = pattern.matcher(rawLCcallnum);
    
            if (matcher.find() && matcher.groupCount() > 5 
                    && matcher.group(6) != null && matcher.group(6).length() > 0) 
                // there is a second cutter preceded by a period
                result = matcher.group(6).trim();
            else {
                regex = LC_CLASS_N_CUTTER + " \\.\\.\\.$"; 
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(rawLCcallnum);
                if (matcher.find())
                    result = " ...";
            }
        }
        return result;
    }

    /**
     * return the second cutter in the call number, without the preceding 
     * characters (such as the "required" period, which is sometimes missing, 
     * or spaces), or any suffixes
     * @param rawLCcallnum - the entire call number, as a string
     */
    public static String getSecondLCcutter(String rawLCcallnum) {
        String result = null;
        
        String firstCutSuffix = getFirstLCcutterSuffix(rawLCcallnum);
        if (firstCutSuffix == null || firstCutSuffix.length() == 0) {
            // look for second cutter 
            String regex = LC_CLASS_N_CUTTER + " *\\.?(" + CUTTER_REGEX + ")";  
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(rawLCcallnum);
            if (matcher.find() && matcher.groupCount() > 5 
                    && matcher.group(6) != null && matcher.group(6).length() > 0) {
                result = matcher.group(6).trim();
            }
        }
        else {
            // get the text AFTER the first cutter suffix, then parse out
            //   cutter text from any potential following text.
            int ix = rawLCcallnum.indexOf(firstCutSuffix) + firstCutSuffix.length();
            if (ix < rawLCcallnum.length()) {
                String remaining = rawLCcallnum.substring(ix).trim();
                Pattern pattern = Pattern.compile("(" + CUTTER_REGEX + ")");
                Matcher matcher = pattern.matcher(remaining);
                if (matcher.find() && matcher.group(1) != null && matcher.group(1).length() > 0) {
                    result = matcher.group(1).trim();
                }
            }
            // if we still have nothing, look for 2nd cutter in first cutter suffix
            if (result == null) {
                Pattern pattern = Pattern.compile("\\.(" + CUTTER_REGEX + ")");
                Matcher matcher = pattern.matcher(firstCutSuffix);
                if (matcher.find() && matcher.group(1) != null && matcher.group(1).length() > 0) {
                    result = matcher.group(1).trim();
                }
            }
        }
        return result;
    }

    /**
     * return the suffix after the first cutter, if there is one.  This occurs
     *  before the second cutter, if there is one.
     * @param rawLCcallnum - the entire LC call number, as a string
     */
    public static String getSecondLCcutterSuffix(String rawLCcallnum) {
        String result = null;
        
        String secondCutter = getSecondLCcutter(rawLCcallnum);
        if (secondCutter != null && secondCutter.length() > 0) {
            // get the call number after the 2nd cutter
            int ix = rawLCcallnum.indexOf(secondCutter) + secondCutter.length();
            if (ix < rawLCcallnum.length())
                result = rawLCcallnum.substring(ix).trim();
        }
    
        return result;
    }

    /**
     * return the suffix after the first cutter, if there is one.  This occurs
     *  before the second cutter, if there is one.
     * @param rawLCcallnum - the entire LC call number, as a string
     * @deprecated
     */
// do we want to separate out year suffixes?  for all or just here? - unused
    public static String getSecondLCcutterYearSuffix(String rawLCcallnum) {
        String result = null;
        
        String regex = LC_CLASS_N_CUTTER + " *(" + NOT_CUTTER + ")*"; 
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawLCcallnum);

        if (matcher.find() && matcher.groupCount() > 5 
                && matcher.group(6) != null && matcher.group(6).length() > 0) {

            // this only grabs the FIRST non-cutter string it encounters after
            //   the first cutter
            result = matcher.group(6);
            
            // this is to cope with additional non-cutter strings after the
            //  first cutter  (e.g. M211 .M93 K.240 1988)
            int endLastIx = matcher.end(6); // end of previous match
            if (endLastIx < rawLCcallnum.length()) {
                Pattern cutterPat = Pattern.compile(" *\\.?" + CUTTER_REGEX + ".*");
                matcher.usePattern(cutterPat);
                if (matcher.find(endLastIx)) {
                    if (endLastIx < matcher.start())
                        result = result.trim() + " " + rawLCcallnum.substring(endLastIx, matcher.start()).trim();
                }
                else
                    result = result.trim() + rawLCcallnum.substring(endLastIx);
            }
        }

        return result;
    }

    /**
     * return the portion of the Dewey call number string that occurs before the 
     *  Cutter.
     */
    public static final String getDeweyB4Cutter(String callnum) {
        String result = null;
        
        String entireCallNumRegex = "(" + DEWEY_CLASS_REGEX + ").*";
        Pattern pattern = Pattern.compile(entireCallNumRegex);
        Matcher matcher = pattern.matcher(callnum);
        if (matcher.find())
            result = matcher.group(1).trim();
        
        return result;
    }

    /**
     * return the first cutter in the call number, without the preceding 
     * characters (such as the "required" period, which is sometimes missing, 
     * or spaces).
     * @param rawCallnum - the entire call number, as a string
     */
    public static String getDeweyCutter(String rawCallnum) {
        String result = null;

        // dewey cutters can have trailing letters, preceded by a space or not
        String regex1 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_CUTTER_TRAILING_LETTERS_REGEX + ")( +" + NOT_CUTTER + ".*)";
        String regex2 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_MIN_CUTTER_REGEX + ")( +" + NOT_CUTTER + ".*)";
        String regex3 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_CUTTER_SPACE_TRAILING_LETTERS_REGEX + ")( +" + NOT_CUTTER + ".*)";
        String regex4 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_CUTTER_TRAILING_LETTERS_REGEX + ")(.*)";
        String regex5 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_MIN_CUTTER_REGEX + ")(.*)";
        String regex6 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_CUTTER_SPACE_TRAILING_LETTERS_REGEX + ")(.*)";
        Pattern pat1 = Pattern.compile(regex1);
        Pattern pat2 = Pattern.compile(regex2);
        Pattern pat3 = Pattern.compile(regex3);
        Pattern pat4 = Pattern.compile(regex4);
        Pattern pat5 = Pattern.compile(regex5);
        Pattern pat6 = Pattern.compile(regex6);

        Matcher matcher = pat1.matcher(rawCallnum);
        if (!matcher.find()) {
            matcher = pat2.matcher(rawCallnum);
            if (!matcher.find()) {
                matcher = pat3.matcher(rawCallnum);
            }
        }

        if (matcher.find()) {
            String cutter = matcher.group(2);
            String suffix = matcher.group(3);
            if (suffix.length() == 0)
                result = cutter.trim();
            else {
                // check if there are letters in the cutter that should be assigned
                //  to the suffix
                if (suffix.startsWith(" ") || cutter.endsWith(" "))
                    result = cutter.trim();
                else {
                    int ix = cutter.lastIndexOf(' ');
                    if (ix != -1)
                        result = cutter.substring(0, ix);
                    else
                        result = cutter.trim();
                }
            }
        }
        else {
            matcher = pat4.matcher(rawCallnum);
            if (matcher.find())
                result = matcher.group(2);
            else {
                matcher = pat5.matcher(rawCallnum);
                if (matcher.find())
                    result = matcher.group(2);
                else {
                    matcher = pat6.matcher(rawCallnum);
                    if (matcher.find())
                        result = matcher.group(2);
                }
            }
        }
        if (result != null)
            return result.trim();
        return result;
    }

    /**
     * return suffix to the first cutter in the dewey call number
     * @param rawCallnum - the entire call number, as a string
     */
    public static String getDeweyCutterSuffix(String rawCallnum) {
        if (rawCallnum == null || rawCallnum.length() == 0)
            return null;
        String result = null;

        String cutter = getDeweyCutter(rawCallnum);
        if (cutter != null) {
            int ix = rawCallnum.indexOf(cutter) + cutter.length();
            result = rawCallnum.substring(ix).trim();
        }
        
        if (result == null || result.length() == 0) 
        {
            // dewey cutters can have trailing letters, preceded by a space or not
            String regex1 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_CUTTER_TRAILING_LETTERS_REGEX + ")( +" + NOT_CUTTER + ".*)";
            String regex2 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_MIN_CUTTER_REGEX + ")( +" + NOT_CUTTER + ".*)";
            String regex3 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_CUTTER_SPACE_TRAILING_LETTERS_REGEX + ")( +" + NOT_CUTTER + ".*)";
            String regex4 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_CUTTER_TRAILING_LETTERS_REGEX + ")(.*)";
            String regex5 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_MIN_CUTTER_REGEX + ")(.*)";
            String regex6 = DEWEY_CLASS_REGEX +  " *\\.?(" + DEWEY_CUTTER_SPACE_TRAILING_LETTERS_REGEX + ")(.*)";
            Pattern pat1 = Pattern.compile(regex1);
            Pattern pat2 = Pattern.compile(regex2);
            Pattern pat3 = Pattern.compile(regex3);
            Pattern pat4 = Pattern.compile(regex4);
            Pattern pat5 = Pattern.compile(regex5);
            Pattern pat6 = Pattern.compile(regex6);
    
            Matcher matcher = pat1.matcher(rawCallnum);
            if (!matcher.find()) {
                matcher = pat2.matcher(rawCallnum);
                if (!matcher.find()) {
                    matcher = pat3.matcher(rawCallnum);
                    if (!matcher.find()) {
                        matcher = pat4.matcher(rawCallnum);
                        if (!matcher.find()) {
                            matcher = pat5.matcher(rawCallnum);
                            if (!matcher.find()) {
                                matcher = pat6.matcher(rawCallnum);
                            }
                        }
                    }
                }
            }
    
            if (matcher.find(0)) {
                cutter = matcher.group(2);
                String suffix = matcher.group(3);
                if (suffix.trim().length() > 0) {
                    // check if there are letters in the cutter that should be assigned
                    //  to the suffix
                    if (suffix.startsWith(" ") || cutter.endsWith(" "))
                        result = suffix;
                    else {
                        int ix = cutter.lastIndexOf(' ');
                        if (ix != -1)
                            result = cutter.substring(ix) + suffix;
                        else
                            result = suffix;
                    }
                }
            }
        }
        if (result != null)
            result = result.trim();
        if (result == null || result.trim().length() == 0)
            return null;
        else
            return result;
    }

    
    /**
     * Used to improve call num sorting and volume lopping.
     * Remove leading and trailing whitespace, ensure whitespace is always a 
     *  single space, remove spaces after periods, remove trailing periods
     *   
     * @param rawCallnum - a non-null String containing a Dewey call number
     * @return normalized form of a call number
     */
    public static String normalizeCallnum(String rawCallnum) {

        // reduce multiple whitespace chars to a single space
        String normalizedCallnum = rawCallnum.trim().replaceAll("\\s\\s+", " ");
        // reduce double periods to a single period
        normalizedCallnum = normalizedCallnum.replaceAll("\\. \\.", " .");
        // remove space after a period if period is after digits and before letters
        normalizedCallnum = normalizedCallnum.replaceAll("(\\d+\\.) ([A-Z])", "$1$2");
        // remove trailing period and any spaces before it
        if (normalizedCallnum.endsWith("."))
            normalizedCallnum = normalizedCallnum.substring(0, normalizedCallnum.length() - 1).trim();

        // cutter could be missing preceding period, but we are leaving that as is

        // there should be a single space before the cutter - the above should
        //  ensure this in nearly all cases
        return normalizedCallnum;
    }

    /**
     * reduce multiple whitespace to single, remove spaces before or after 
     *   periods, remove spaces between letters and class digits
     */
    static String normalizeLCcallnum(String rawLCcallnum) 
    {
        String normCallnum = normalizeCallnum(rawLCcallnum);
        // remove space between class letters and digits
        return normCallnum.replaceAll("^([A-Z][A-Z]?[A-Z]?) ([0-9])", "$1$2");
    }


// TODO:  method to normalize year and immediate following chars (no space)?   <-- stupid?
    /**
     * given a raw LC call number, return the shelf key - a sortable version
     *  of the call number
     */
    public static String getLCShelfkey(String rawLCcallnum, String recid) {
        return(getLCShelfkey(rawLCcallnum, recid, null));
    }

    /**
     * given a raw LC call number, return the shelf key - a sortable version
     *  of the call number
     */
    public static String getLCShelfkey(String rawLCcallnum, String recid, ErrorHandler errors) {
        StringBuilder resultBuf = new StringBuilder();
        String upcaseLCcallnum = rawLCcallnum.toUpperCase();
        
// TODO: don't repeat same parsing -- some of these methods could take the
//   portion of the callnumber before the cutter as the input string.       
        
        // pad initial letters with trailing blanks to be 4 chars long
        StringBuilder initLetBuf = new StringBuilder("    ");
        String lets = getLCstartLetters(upcaseLCcallnum);
        initLetBuf.replace(0, lets.length(), lets);
        resultBuf.append(initLetBuf);
        
        try {
            // normalize first numeric portion to a constant length:
            //  four digits before decimal, 6 digits after
            String digitStr = getLCClassDigits(upcaseLCcallnum);            
            if (digitStr != null) 
                resultBuf.append(normalizeFloat(digitStr, 4, 6));
            else
                resultBuf.append(normalizeFloat("0", 4, 6));
            
            // optional string b/t class and first cutter
            String classSuffix = getLCClassSuffix(upcaseLCcallnum);
            if (classSuffix != null)
                resultBuf.append(" " + normalizeSuffix(classSuffix));
            
            // normalize first cutter  - treat number as a fraction
            String firstCutter = getFirstLCcutter(upcaseLCcallnum);
            if (firstCutter != null) {
                resultBuf.append(" " + normalizeCutter(firstCutter, 6));

                // normalize optional first cutter suffix
                String firstCutterSuffix = getFirstLCcutterSuffix(upcaseLCcallnum);
                if (firstCutterSuffix != null)
                    resultBuf.append(" " + normalizeSuffix(firstCutterSuffix));
                
                // optional second cutter - normalize
                String secondCutter = getSecondLCcutter(upcaseLCcallnum);
                if (secondCutter != null) {
                    resultBuf.append(" " + normalizeCutter(secondCutter, 6));
                    
                    String secondCutterSuffix = getSecondLCcutterSuffix(upcaseLCcallnum);
                    if (secondCutterSuffix != null)
                        resultBuf.append(" " + normalizeSuffix(secondCutterSuffix));
                }
            }
        } catch (NumberFormatException e) {
//              if (recid != null)
            if ( (recid != null) && (!rawLCcallnum.startsWith("XX")) ) // Stanford mod
            {
                if (errors == null)
                {
                    System.err.println("Problem creating shelfkey for record " + recid + "; call number: " + rawLCcallnum);
                }
                else
                {
                    errors.addError(ErrorHandler.ERROR_TYPO, "Problem creating shelfkey for record " + recid + "; call number: " + rawLCcallnum);
                }
            }
            //e.printStackTrace();
            resultBuf = new StringBuilder();
        }
        
        if (resultBuf.length() == 0)
            resultBuf.append(upcaseLCcallnum);

        return resultBuf.toString().trim();
    }

    /**
     * normalize the cutter string for shelf list sorting - make number into  
     *  decimal of the number of digits indicated by param
     */
    private static String normalizeCutter(String cutter, int numDigits) {
        String result = null;
        if (cutter != null && cutter.length() > 0) {
            String cutLets = getLCstartLetters(cutter);
            String cutDigs = cutter.substring(cutLets.length());
            String norm = null;
            if (cutDigs != null && cutDigs.length() > 0) {
                try {
                    // make sure part after letters is an integer
                    Integer.parseInt(cutDigs);
                    norm = normalizeFloat("." + cutDigs, 1, numDigits); 
                } catch (NumberFormatException e) {
                    norm = cutDigs;
                }
            } 
            else if (cutDigs.length() == 0 && cutLets.length() == 1)
                // if no digits in cutter, want it to sort first
                norm = normalizeFloat("0", 1, numDigits);
    
            result = cutLets + norm;        
        }
        return result;
    }

    /**
     * normalize a suffix for shelf list sorting by changing all digit 
     *  substrings to a constant length (left padding with zeros).
     */
    public static String normalizeSuffix(String suffix) {
        if (suffix != null && suffix.length() > 0) {
            StringBuilder resultBuf = new StringBuilder(suffix.length());
            // get digit substrings
            String[] digitStrs = suffix.split("[\\D]+");
            int len = digitStrs.length;
            if (digitStrs != null && len != 0) {
                int s = 0;
                for (int d = 0; d < len; d++) {
                    String digitStr = digitStrs[d];
                    int ix = suffix.indexOf(digitStr, s);
                    // add the non-digit chars before, if they exist
                    if (s < ix) {
                        String text = suffix.substring(s, ix);
                        resultBuf.append(text);
                    }
                    if (digitStr != null && digitStr.length() != 0) {
                        // add the normalized digit chars, if they exist
                        resultBuf.append(normalizeFloat(digitStr, 6, 0));
                        s = ix + digitStr.length();
                    }
                        
                }
                // add any chars after the last digStr
                resultBuf.append(suffix.substring(s));
                return resultBuf.toString();
            }
        }
        
        return suffix;
    }

    /**
     * given a shelfkey (a lexicaly sortable call number), return the reverse 
     * shelf key - a sortable version of the call number that will give the 
     * reverse order (for getting "previous" call numbers in a list)
     */
    public static String getReverseShelfKey(String shelfkey) {
        StringBuilder resultBuf = new StringBuilder(reverseDefault);
        if (shelfkey != null && shelfkey.length() > 0)
            resultBuf.replace(0, shelfkey.length(), reverseAlphanum(shelfkey));
        return resultBuf.toString();
    }

    /**
     * return the reverse String value, mapping A --> 9, B --> 8, ...
     *   9 --> A and also non-alphanum to sort properly (before or after alphanum)
     */
    private static String reverseAlphanum(String orig) {

/*      
        char[] origArray = orig.toCharArray();

        char[] reverse = new char[origArray.length];
        for (int i = 0; i < origArray.length; i++) {
            Character ch = origArray[i];
            if (ch != null) {
                if (Character.isLetterOrDigit(ch))
                    reverse[i] = alphanumReverseMap.get(ch);
                else 
                    reverse[i] = reverseNonAlphanum(ch);
            }
        }
*/          
        StringBuilder reverse = new StringBuilder();
        for (int ix = 0; ix < orig.length(); ) {
            int codePoint = Character.toUpperCase(orig.codePointAt(ix));
            char[] chs = Character.toChars(codePoint);
            
            if (Character.isLetterOrDigit(codePoint)) {
                if (chs.length == 1) {
                    char c = chs[0];
                    if (alphanumReverseMap.containsKey(c))
                        reverse.append(alphanumReverseMap.get(c));
                    else {
                        // not an ASCII letter or digit
                        
                        // map latin chars with diacritic to char without
                        char foldC;
                        
                        if (UCharacter.UnicodeBlock.of(c) != UCharacter.UnicodeBlock.COMBINING_DIACRITICAL_MARKS &&  
                            UCharacter.UnicodeBlock.of(c) != UCharacter.UnicodeBlock.SPACING_MODIFIER_LETTERS &&
                             (foldC = Utils.foldDiacriticLatinChar(c)) != 0x00)
                            // we mapped a latin char w diacritic to plain ascii 
                            reverse.append(alphanumReverseMap.get(foldC));
                        else
                            // single char, but non-latin, non-digit
                            // ... view it as after Z in regular alphabet, for now
                            reverse.append(SORT_FIRST_CHAR);
                    }
                }
                else  {
                    // multiple 16 bit character unicode letter
                    // ... view it as after Z in regular alphabet, for now
                    reverse.append(SORT_FIRST_CHAR);
                }
            }
            else // not a letter or a digit 
                reverse.append(reverseNonAlphanum(chs[0]));

            ix += chs.length;
        }

        return new String(reverse);     
    }

    /**
     * for non alpha numeric characters, return a character that will sort
     *  first or last, whichever is the opposite of the original character. 
     */
    public static char[] reverseNonAlphanum(char ch) {
        // use punctuation before or after alphanum as appropriate
        switch (ch) {
            case '.':
                return Character.toChars('}');
            case '{':
            case '|':
            case '}':
            case '~':
// N.B.:  these are tough to deal with in a variety of contexts.  
// Hopefully diacritics and non-latin won't bite us in the butt.
//              return Character.toChars(Character.MIN_CODE_POINT);
                return Character.toChars(' ');
            default:
//              return Character.toChars(Character.MAX_CODE_POINT);
                return Character.toChars('~');
        }   
    }

    /**
     * given a raw Dewey call number, return the shelf key - a sortable 
     *  version of the call number
     */
    public static String getDeweyShelfKey(String rawDeweyCallnum) {
        StringBuilder resultBuf = new StringBuilder();

        // class 
        // float number, normalized to have 3 leading zeros
        //   and trailing zeros if blank doesn't sort before digits
        String classNum = normalizeFloat(getDeweyB4Cutter(rawDeweyCallnum), 3, 8);
        resultBuf.append(classNum);
        
        // cutter   1-3 digits
        // optional cutter letters suffix
        //   letters preceded by space or not.

        // normalize cutter  - treat number as a fraction.
        String cutter = getDeweyCutter(rawDeweyCallnum);
        if (cutter != null)
            resultBuf.append(" " + cutter);

        // optional suffix (year, part, volume, edition) ...
        String cutterSuffix = getDeweyCutterSuffix(rawDeweyCallnum);
        if (cutterSuffix != null)
            resultBuf.append(" " + normalizeSuffix(cutterSuffix));
        
        
        if (resultBuf.length() == 0)
            resultBuf.append(rawDeweyCallnum);

        return resultBuf.toString().trim();
    }   

        
    /**
     * normalizes numbers (can have decimal portion) to (digitsB4) before
     *  the decimal (adding leading zeroes as necessary) and (digitsAfter 
     *  after the decimal.  In the case of a whole number, there will be no
     *  decimal point.
     * @param floatStr, the number, as a String
     * @param digitsB4 - the number of characters the result should have before the
     *   decimal point (leading zeroes will be added as necessary). A negative 
     *   number means leave whatever digits encountered as is; don't pad with leading zeroes.
     * @param digitsAfter - the number of characters the result should have after
     *   the decimal point.  A negative number means leave whatever fraction
     *   encountered as is; don't pad with trailing zeroes (trailing zeroes in
     *   this case will be removed)
     * @throws NumberFormatException if string can't be parsed as a number
     */
    public static String normalizeFloat(String floatStr, int digitsB4, int digitsAfter)
    {
        double value = Double.valueOf(floatStr).doubleValue();
        
        String formatStr = getFormatString(digitsB4) + '.' + getFormatString(digitsAfter);
        
        DecimalFormat normFormat = new DecimalFormat(formatStr);
        String norm = normFormat.format(value);
        if (norm.endsWith("."))
            norm = norm.substring(0, norm.length() - 1);
        return norm;
    }
        
    private static String PUNCT_PREFIX = "([\\.:\\/])?";
    private static String NS_PREFIX = "(n\\.s\\.?\\,? ?)?";
    private static String MONTHS = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec";
    private static String VOL_LETTERS = "[\\:\\/]?(bd|iss|jahrg|new ser|no|part|pts?|ser|t|v|vols?|vyp" + "|" + MONTHS + ")";
    private static String VOL_NUMBERS = "\\d+([\\/-]\\d+)?( \\d{4}([\\/-]\\d{4})?)?( ?suppl\\.?)?";
    private static String VOL_NUMBERS_LOOSER = "\\d+.*";
    private static String VOL_NUM_AS_LETTERS = "[A-Z]([\\/-]\\[A-Z]+)?.*";
    
    private static Pattern VOL_PATTERN = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "\\.? ?" + VOL_NUMBERS, Pattern.CASE_INSENSITIVE);
    private static Pattern VOL_PATTERN_LOOSER = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "\\.? ?" + VOL_NUMBERS_LOOSER, Pattern.CASE_INSENSITIVE);
    private static Pattern VOL_PATTERN_LETTERS = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "[\\/\\. ]" + VOL_NUM_AS_LETTERS , Pattern.CASE_INSENSITIVE);

    /**
     * remove volume information from LC call number if it is present as a 
     *   suffix
     * @param rawLCcallnum
     * @return call number without the volume information, or full call number
     *   if no volume information was present.
     */
    public static String removeLCVolSuffix(String rawLCcallnum)
    {
        // get suffix to last occurring cutter, if there is one
        String suffix = getSecondLCcutterSuffix(rawLCcallnum);
        if (suffix == null || suffix.length() == 0) {
            String cut1suffix = getFirstLCcutterSuffix(rawLCcallnum);
            if (cut1suffix != null) {
                // first cutter suffix may contain second cutter
                String cut2 = getSecondLCcutter(rawLCcallnum);
                if (cut2 != null) {
                    int ix = cut1suffix.indexOf(cut2);
                    if (ix != -1)
                        suffix = cut1suffix.substring(0, ix);
                    else
                        suffix = cut1suffix;
                }
                else
                    suffix = cut1suffix;
            }
        }

        // could put last ditch effort with tightest pattern, but don't want to take out too much       
        if (suffix != null && suffix.length() > 0) {
            Matcher matcher = VOL_PATTERN.matcher(suffix);
            if (!matcher.find()) {
                matcher = VOL_PATTERN_LOOSER.matcher(suffix);
                if (!matcher.find()) {
                    matcher = VOL_PATTERN_LETTERS.matcher(suffix);
                }
            }
// look for first / last match, not any match (subroutine?)?
            if (matcher.find(0)) {
                // return orig call number with matcher part lopped off.
                int ix = rawLCcallnum.indexOf(suffix) + matcher.start();
                if (ix != -1 && ix < rawLCcallnum.length()) {
                    return rawLCcallnum.substring(0, ix).trim();
                }
            }               
        }
        return rawLCcallnum;
    }

    
    /**
     * remove volume information from Dewey call number if it is present as a 
     *   suffix
     * @param rawDeweyCallnum
     * @return call number without the volume information, or full call number
     *   if no volume information was present.
     */
    public static String removeDeweyVolSuffix(String rawDeweyCallnum)
    {
        String cutSuffix = getDeweyCutterSuffix(rawDeweyCallnum);

        if (cutSuffix == null || cutSuffix.length() == 0)
            return rawDeweyCallnum;
        
        Matcher matcher = VOL_PATTERN.matcher(cutSuffix);
        if (!matcher.find()) {
            matcher = VOL_PATTERN_LOOSER.matcher(cutSuffix);
            if (!matcher.find()) {
                matcher = VOL_PATTERN_LETTERS.matcher(cutSuffix);
            }
        }
        
        if (matcher.find(0)) {
            // return orig call number with matcher part lopped off.
            int ix = rawDeweyCallnum.indexOf(cutSuffix) + matcher.start();
            if (ix != -1 && ix < rawDeweyCallnum.length()) {
                return rawDeweyCallnum.substring(0, ix).trim();
            }
        }
        return rawDeweyCallnum;
    }
    
    
    /**
     * adds leading zeros to a dewey call number, when they're missing.
     * @param deweyCallNum
     * @return the dewey call number with leading zeros
     */
    public static String addLeadingZeros(String deweyCallNum) 
    {
        String result = deweyCallNum;
        String b4Cutter = org.solrmarc.tools.CallNumUtils.getPortionBeforeCutter(deweyCallNum);

        // TODO: could call Utils.normalizeFloat(b4Cutter.trim(), 3, -1);
        // but still need to add back part after cutter

        String b4dec = null;
        int decIx = b4Cutter.indexOf(".");
        if (decIx >= 0)
            b4dec = deweyCallNum.substring(0, decIx).trim();
        else
            b4dec = b4Cutter.trim();

        if (b4dec != null) {
            switch (b4dec.length()) 
            {
                case 1:
                    result = "00" + deweyCallNum;
                    break;
                case 2:
                    result = "0" + deweyCallNum;
            }
        }

        return result;
    }

    /**
     * return a format string corresponding to the number of digits specified
     * @param numDigits - the number of characters the result should have (to be padded
     *  with zeroes as necessary). A negative number means leave whatever digits
     *   encountered as is; don't pad with zeroes -- up to 12 characters.
     */
    private static String getFormatString(int numDigits) {
        StringBuilder b4 = new StringBuilder();
        if (numDigits < 0)
            b4.append("############");
        else if (numDigits > 0) {
            for (int i = 0; i < numDigits; i++) {
                b4.append('0');
            }
        }
        return b4.toString();   
    }
            
}
