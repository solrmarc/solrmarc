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
import org.solrmarc.tools.CallNumUtils;

/**
 * @author Tod Olson, University of Chicago
 *
 */
public class LCCallNumber extends AbstractCallNumber {

    protected String classification;
    protected String classLetters;
    protected String classDigits;
    protected String classSuffix;
    protected String cutter1;
    protected String cutter1Suffix;
    protected String cutter2;
    protected String cutter2Suffix;

    protected boolean valid;

    public LCCallNumber() {
        this.init();
    }

    public LCCallNumber(String callNumber) {
        parse(callNumber);
    }

    public String getClassification() {
        return this.classification;
    }

    public String getClassLetters() {
        return classLetters;
    }

    public void setClassLetters(String classLetters) {
        this.classLetters = classLetters;
    }

    public String getClassDigits() {
        return classDigits;
    }

    public void setClassDigits(String classDigits) {
        this.classDigits = classDigits;
    }

    public String getClassSuffix() {
        return classSuffix;
    }

    public void setClassSuffix(String classSuffix) {
        this.classSuffix = classSuffix;
    }

    public String getFirstCutter() {
        return cutter1;
    }

    public void setFirstCutter(String cutter1) {
        this.cutter1 = cutter1;
    }

    public String getFirstCutterSuffix() {
        return cutter1Suffix;
    }

    public void setFirstCutterSuffix(String cutter1Suffix) {
        this.cutter1Suffix = cutter1Suffix;
    }

    public String getSecondCutter() {
        return cutter2;
    }

    public void setSecondCutter(String cutter2) {
        this.cutter2 = cutter2;
    }

    public String getSecondCutterSuffix() {
        return cutter2Suffix;
    }

    public void setSecondCutterSuffix(String cutter2Suffix) {
        this.cutter2Suffix = cutter2Suffix;
    }

    public void init() {
        raw = null;
        valid = true;

        classification = null;
        classLetters = null;
        classDigits = null;
        classSuffix = null;
        cutter1 = null;
        cutter1Suffix = null;
        cutter2 = null;
        cutter2Suffix = null;
    }

    @Override
    public void parse(String callNumber) {
        raw = callNumber;
        //TODO remove try once we settle behavior if callNumber is not valid LC (starts with numbers)
        try {
            classification = CallNumUtils.getLCB4FirstCutter(callNumber);
            classLetters = CallNumUtils.getLCstartLetters(callNumber);
            classDigits = CallNumUtils.getLCClassDigits(callNumber);
            classSuffix = CallNumUtils.getLCClassSuffix(callNumber);
            cutter1 = CallNumUtils.getFirstLCcutter(callNumber);
            cutter1Suffix = CallNumUtils.getFirstLCcutterSuffix(callNumber);
            cutter2 = CallNumUtils.getSecondLCcutter(callNumber);
            cutter2Suffix = CallNumUtils.getSecondLCcutterSuffix(callNumber);
        } catch (NullPointerException e) {
            valid = false;
        }
    }

    public boolean isValid() {
        return this.valid;
    }

    @Override
    public String getShelfKey() {
        return getShelfKey(null, null);
    }

    public String getShelfKey(String record) {
        return getShelfKey(record, null);
    }

    public String getShelfKey(String record, ErrorHandler errors) {
        return CallNumUtils.getLCShelfkey(raw, record, errors);
    }

}
