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

import org.solrmarc.tools.CallNumUtils;

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

        //TODO break into helper methods that can be overidden

        //TODO add back in once initial checkin is accepted
        /*
        // group 1 is Dewey class, group 2 is cutter plus suffix
        Pattern deweyClassPat = Pattern.compile("(" + DEWEY_CLASS_REGEX + ") *\\.?(.*)");
        Matcher deweyClassMatch = deweyClassPat.matcher(call);
        if (!deweyClassMatch.matches()) {
            this.matchesDewey = false;
            return;
        }
        this.classification = deweyClassMatch.group(1).trim();
        String cutterAndSuffix = deweyClassMatch.group(2);
        if (cutterAndSuffix == null) {
            return;
        }
        */

        this.classification = CallNumUtils.getDeweyB4Cutter(call);
        if (this.classification == null) {
            this.valid = false;
        }
        this.cutter = CallNumUtils.getDeweyCutter(call);
        this.suffix = CallNumUtils.getDeweyCutterSuffix(call);

    }

    public String getClassification() {
        return classification;
    }
    public void setClassification(String classification) {
        this.classification = classification;
    }
    public String getCutter() {
        return cutter;
    }
    public void setCutter(String cutter) {
        this.cutter = cutter;
    }
    public String getSuffix() {
        return suffix;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    @Override
    public String getShelfKey() {
        return CallNumUtils.getDeweyShelfKey(this.raw);
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
