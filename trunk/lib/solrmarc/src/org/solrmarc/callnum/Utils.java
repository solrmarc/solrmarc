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

import java.text.DecimalFormat;

/**
 * Provides utility functions to support call number manipulation.
 * Mostly reused from Naomis Dushay's <code>CallNumUtils</code>.
 *
 * @author Naomi Dushay, Stanford University
 * @author Tod Olson, University of Chicago
 *
 */

public class Utils {

    /**
     * normalizes numbers (can have decimal portion) to (<code>digitsB4</code>) before
     *  the decimal (adding leading zeroes as necessary) and (<code>digitsAfter</code>)
     *  after the decimal.  In the case of a whole number, there will be no
     *  decimal point.
     * @param floatStr    the number, as a String
     * @param digitsB4    the number of characters the result should have before the
     *                    decimal point (leading zeroes will be added as necessary). A negative
     *                    number means leave whatever digits encountered as is; don't pad with leading zeroes.
     * @param digitsAfter the number of characters the result should have after
     *                    the decimal point.  A negative number means leave whatever fraction
     *                    encountered as is; don't pad with trailing zeroes (trailing zeroes in
     *                    this case will be removed)
     * @throws NumberFormatException if string can't be parsed as a number
     */
    public static String normalizeFloat(String floatStr, int digitsB4, int digitsAfter)
    {
        double value = Double.valueOf(floatStr).doubleValue();

        // TODO: what if digitsB4 or digitsAfter are too small for the value?
        String formatStr = getFormatString(digitsB4) + '.' + getFormatString(digitsAfter);

        DecimalFormat normFormat = new DecimalFormat(formatStr);
        String norm = normFormat.format(value);
        if (norm.endsWith("."))
            norm = norm.substring(0, norm.length() - 1);
        return norm;
    }

    /**
     * returns a format string corresponding to the number of digits specified
     * @param numDigits the number of characters the result should have (to be padded
     *                  with zeroes as necessary). A negative number means leave whatever digits
     *                  encountered as is; don't pad with zeroes -- up to 12 characters.
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
