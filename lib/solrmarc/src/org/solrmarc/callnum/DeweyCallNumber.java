package org.solrmarc.callnum;
/*
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a call number class for Dewey call numbers.
 *
 * Borrows heavily from Naomi Dushay's <code>CallNumUtils</code>.
 *
 * @author Tod Olson, University of Chicago
 *
 */
public class DeweyCallNumber extends AbstractCallNumber {
    protected String classification;
    protected String cutter;
    protected String suffix;

    /**
     * regular expression for Dewey classification.
     *  Dewey classification is a three digit number (possibly missing leading
     *   zeros) with an optional fraction portion.
     *   Must use non-capturing group internally, as <code>parse</code> needs
     *   reliable group numbers for the matches.
     */
    public static final String DEWEY_CLASS_REGEX = "\\d{1,3}(?:\\.\\d+)?";

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

    /**
     * non-cutter text that can appear before or after cutters
     */
    public static final String NOT_CUTTER = "(:?[\\da-z]\\w*)|(:?[A-Z]\\D+[\\w]*)";

    public DeweyCallNumber() {
        this.init();
    }

    public DeweyCallNumber(String call) {
        parse(call);
    }

    public void init() {
        this.raw = null;
        this.classification = null;
        this.cutter = null;
        this.suffix = null;
    }

    public void parse(String call) {
        this.init();
        this.raw = call;
        // Assume call number is valid Dewey, until we show otherwise
        this.valid = true;

        if (call == null || call.length() == 0) {
            this.valid = false;
            return;
        }
        String cutterAndSuffix = this.parseClassificationHelper(call);
        if (!this.valid)
            return;
        this.parseCutterAndSuffixHelper(cutterAndSuffix);
    }

    /**
     * Parses the call number, sets <code>this.classification</code>, and returns any remaining
     * cutter and suffix. Sets <code>valid</code> to <code>false</code> if the beginning of the
     * call number does not match a Dewey classification pattern
     *
     * @param call call number to parse
     * @return     <code>null</code> if the beginning of the call number does not look like a
     *             Dewey class, empty string if parse is successful but there is no cutter or suffix.
     *
     */
    protected String parseClassificationHelper(String call) {
        // group 1 is Dewey class, group 2 is cutter plus suffix; consume spaces and period in between.
        Pattern deweyClassPat = Pattern.compile("(" + DEWEY_CLASS_REGEX + ")\\s*\\.?(.*)");
        Matcher deweyClassMatch = deweyClassPat.matcher(call);
        if (!deweyClassMatch.matches()) {
            this.valid = false;
            return null;
        }
        this.classification = deweyClassMatch.group(1).trim();
        return deweyClassMatch.group(2);
    }

    /**
     * helper method that parses the what remains after the classification has been identified.
     * Note: group count for all patterns must be 2.
     *
     * @param cutterAndSuffix the part of the call number left over after classification has been trimmed.
     */
    protected void parseCutterAndSuffixHelper(String cutterAndSuffix) {
        // dewey cutters can have trailing letters, preceded by a space or not
        String regex1 = "(" + DEWEY_CUTTER_TRAILING_LETTERS_REGEX + ") +(" + NOT_CUTTER + ".*)";
        String regex2 = "(" + DEWEY_MIN_CUTTER_REGEX + ") +(" + NOT_CUTTER + ".*)";
        String regex3 = "(" + DEWEY_CUTTER_SPACE_TRAILING_LETTERS_REGEX + ") +(" + NOT_CUTTER + ".*)";
        String regex4 = "(" + DEWEY_CUTTER_TRAILING_LETTERS_REGEX + ")(.*)";
        String regex5 = "(" + DEWEY_MIN_CUTTER_REGEX + ")(.*)";
        String regex6 = "(" + DEWEY_CUTTER_SPACE_TRAILING_LETTERS_REGEX + ")(.*)";
        Pattern[] patterns = {
                Pattern.compile(regex1),
                Pattern.compile(regex2),
                Pattern.compile(regex3),
                Pattern.compile(regex4),
                Pattern.compile(regex5),
                Pattern.compile(regex6),
        };
        // try each pattern, take the first match
        for (Pattern pat : patterns) {
            Matcher matcher = pat.matcher(cutterAndSuffix);
            if (matcher.matches()) {
                String cutter = matcher.group(1);
                String suffix = matcher.group(2);
                suffix = suffix.trim();
                // Not certain why we check for non-empty suffix--inherited algorithm
                if (suffix.length() > 0) {
                    // check if there are letters in the cutter that should be assigned
                    //  to the suffix
                    int ix = cutter.lastIndexOf(' ');
                    // may be obsolete, no test cases exercise this code! - TAO 2014-02-21
                    if (ix != -1) {
                        // add to suffix...
                        suffix = cutter.substring(ix) + suffix;
                        // ..._then_ trim from cutter
                        cutter = cutter.substring(0, ix);
                    }
                }
                // enforce policy of null if cutter or suffix are empty
                if (cutter.length()==0)
                    this.cutter = null;
                else
                    this.cutter = cutter;
                if (suffix.length()==0)
                    this.suffix = null;
                else
                    this.suffix = suffix;
                return;
            }
        }
        // If we reach here no patterns have matched, assume it is all suffix
        this.suffix = cutterAndSuffix;
    }

    /**
     * returns the classification of the call number.
     * @return call number classification, or <code>null</code> if not set or found by <code>parse<code>.
     */
    public String getClassification() {
        return classification;
    }
    public void setClassification(String classification) {
        this.classification = classification;
    }
    /**
     * returns the cutter of the call number.
     * @return call number cutter, or <code>null</code> if no cutter was set or found.
     */
    public String getCutter() {
        return cutter;
    }
    public void setCutter(String cutter) {
        this.cutter = cutter;
    }
    /**
     * returns the suffix of the call number.
     * @return call number suffix, or <code>null</code> if no suffix was set or found.
     */
    public String getSuffix() {
        return suffix;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    @Override
    public String getShelfKey() {
        if (!this.valid) {
            return this.raw;
        }
        //return CallNumUtils.getDeweyShelfKey(this.raw);

        StringBuilder resultBuf = new StringBuilder();

        // class
        // float number, normalized to have 3 leading zeros
        //   and trailing zeros if blank doesn't sort before digits
        String classNum = Utils.normalizeFloat(this.classification, 3, 8);
        resultBuf.append(classNum);

        // cutter   1-3 digits
        // optional cutter letters suffix
        //   letters preceded by space or not.

        // normalize cutter  - treat number as a fraction.
        if (this.cutter != null)
            resultBuf.append(" " + this.cutter);

        // optional suffix (year, part, volume, edition)
        if (this.suffix != null)
            resultBuf.append(" " + Utils.normalizeSuffix(this.suffix));


        if (resultBuf.length() == 0)
            resultBuf.append(this.raw);

        return resultBuf.toString().trim();

    }

    /*
    public String debugInfo() {
        String info = "this.raw = " + this.raw
                + "this.classification = " + this.classification
                + "this.cutter = " + this.cutter
                + "this.suffix = " + this.suffix;
        return info;
    }
    */
}
