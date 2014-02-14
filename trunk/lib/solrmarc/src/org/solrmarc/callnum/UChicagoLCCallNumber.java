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

import org.marc4j.ErrorHandler;

/**
 * Extends <code>LCCallNumber</code> to accommodate University of Chicago local practices.
 *
 * <code>XXK</code>: <code>K</code> call numbers are reall7 Benyon K, which looks like but pre-dates the
 * LC K classification.
 * Actual LC K call numbers have a prefix of <code>XX</code>, such as <code>XXKF2371.A75I77 1990</code>.
 *
 * @author Tod Olson, University of Chicago
 *
 */
public class UChicagoLCCallNumber extends LCCallNumber {

    /* should be private, but easier to test */
    protected boolean xxkFlag = false;

    public UChicagoLCCallNumber() {
        this.init();
    }

    public UChicagoLCCallNumber(String callNumber) {
        this.init();
        this.parse(callNumber);
    }

    /**
     * Is this a <code>XXK</code> call number, i.e. is this really an LC K call number
     * with <code>XX</code> prepended?
     *
     * @return <code>true</code> if this is a <code>XXK</code> call number.
     */
    public boolean isXXK() {
        return xxkFlag;
    }

    public void init() {
        super.init();
        this.xxkFlag = false;
    }

/*	public void parse(String callNumber) {
        // Java regexps must match entire string not just quick match
        boolean xxkFlag = callNumber.matches("^XX *K.*");
        if (xxkFlag) {
            // UChicago K's are Benyon K, XXKs are LC K
            // Deal with XX prefix"
            super.parse(callNumber.substring(2).trim());
            // Close up space after XX, simplify any later parsing for now
            this.raw = "XX" + this.raw;
            this.classification = "XX" + this.classification;
            this.classLetters = "XX" + this.classLetters;
        } else {
            super.parse(callNumber);
        }
    }
*/
    // TODO: better to increase the number of characters available in for callClassLetters in shelf key and leave the XX in the class?
    public void parse(String callNumber) {
        // Java regexps must match entire string, not just quick match
        this.xxkFlag = callNumber.matches("^XX *K.*");
        if (this.xxkFlag) {
            // UChicago K's are Benyon K, XXKs are LC K
            // Deal with XX prefix"
            this.raw = callNumber.substring(2).trim();
        } else {
            this.raw = callNumber;
        }
        super.parse(this.raw);
    }

    public String getShelfKey(String record, ErrorHandler errors) {
        String shelfKey = super.getShelfKey(record, errors);
        if (this.xxkFlag) {
            // Sould only need one X to make sorting correct, but gets odd in handler
            shelfKey = "XX" + shelfKey;
        }
        return shelfKey;
    }


}
