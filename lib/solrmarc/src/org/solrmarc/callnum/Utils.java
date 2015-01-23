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

/**
 * Provides utility functions to support call number manipulation.
 *
 * @author Tod Olson, University of Chicago
 *
 */

public class Utils {
    
    public enum State { START, WORD, GAP, NUM }
    public enum InType { LETTER, SPACE, PERIOD, PUNCT, DIGIT, OTHER, END }

    /**
     * Writes a numerically-sortable version of the input to the buffer.
     * 
     * The rules for production the numerically sortable sequence are:
     * <ul>
     * <li>Letters are translated to upper case</li>
     * <li>numeric sequences are prepended with the length of the sequence</li> 
     * <li>any other character is a word separator</li> 
     * <li>sequences of word separators are reduced to a single space</li> 
     * </ul>
     * 
     * <p>Prepending a sequence of digits with the number of digits ensures that they will easily sort numerically:
     * sort keys for 2-digits nubmers start with 2, sort keys for 3-digit numbers start with 3, etc. 
     * Suggested by John Craig at Code4Lib in Ashville, NC.
     * 
     * <p>Implemented as a finite state machine, modeled by <code>switch</code> statements.
     * 
     * @param buf       buffer for appending sortable version of the input
     * @param input     source character sequence
     */
    public static void appendNumericallySortable(StringBuilder buf, CharSequence input) {
        StringBuilder numBuf = new StringBuilder();
        State state = State.START;
        char c;
        InType inType = InType.END;
        
        for (int i = 0; i < input.length(); i++) {
            c = input.charAt(i);
            // Ugly C-style comparisons to avoid Unicode table lookups, 
            // should be fine for call numbers
            // TODO: remove unneeded input types
            if (c >= 'A' && c <= 'Z') {
                inType = InType.LETTER;
            } else if (c >= 'a' && c <= 'z') {
                inType = InType.LETTER;
                c = Character.toUpperCase(c);
            } else if (c >= '0' && c <= '9') {
                inType = InType.DIGIT;
            } else if (c == '.') {
                inType = InType.PERIOD;
            } else if (Character.isWhitespace(c)) {
                inType = InType.SPACE;
            } else if (c >= '!' && c <= '~') { 
                // Only consider ASCII-style punctuation,
                // have already eliminated digits and letters
                inType = InType.PUNCT;
            } else {
                inType = InType.OTHER;
            }
            
            switch (state) {
                case START:
                    switch (inType) {
                        case LETTER:
                            state = State.WORD;
                            buf.append(c);
                            break;
                        case DIGIT:
                            state = State.NUM;
                            numBuf.append(c);
                            break;
                        default:
                            // Consume anything else, remain in START state
                            break;
                    }
                    break;
                case WORD:      // Write word characters directly to buffer
                    switch (inType) {
                    case LETTER:
                        state = State.WORD;
                        buf.append(c);
                        break;
                    case DIGIT:
                        state = State.NUM;
                        buf.append(' ');
                        numBuf.append(c);
                        break;
                    default:
                        state = State.GAP;
                        buf.append(' ');
                        break;
                    }
                    break;
                case GAP:       // If we are in a gap, only letters or digits will take us out
                    switch (inType) {
                    case LETTER:
                        state = State.WORD;
                        buf.append(c);
                        break;
                    case DIGIT:
                        state = State.NUM;
                        numBuf.append(c);
                        break;
                    default:
                        // Consume anything else, remain in GAP state
                        break;
                    }
                    break;
                case NUM:       // accumulate number in special buffer, write sort version on state change
                    switch (inType) {
                    case DIGIT: // Stay in NUM state and accumulate another digit
                    case PERIOD:
                        numBuf.append(c);
                        break;
                    case LETTER:
                        state = State.WORD;
                        appendSortableNumber(buf, numBuf);
                        numBuf.setLength(0);
                        buf.append(c);
                        break;
                    default:
                        state = State.GAP;
                        appendSortableNumber(buf, numBuf);
                        numBuf.setLength(0);
                        buf.append(' ');
                        break;
                    }
                    break;
                default:
                    //TODO: Dryrot error?
                    break;
            }
        }
        // Remember any lingering number data
        if (state == State.NUM) {
            appendSortableNumber(buf, numBuf);
            numBuf.setLength(0);
        }
    }
    
    /**
     * Appends to a buffer a lexicographically sortable version of the number. 
     * The number may be an integer or may include a decimal.
     * 
     * @param buf       buffer to insert the sortable token
     * @param num       sequence of digits, possibly with decimal
     */
    public static void appendSortableNumber(StringBuilder buf, CharSequence num) {
        /*
        if (num.charAt(0) == '0') {
            int i = 0;
            while (i < num.length() && num.charAt(i) == '0') {
                i++;
            }
            buf.append(num.length() - i);
            buf.append(num.subSequence(i, num.length()));
        } else {
            buf.append(num.length());
            buf.append(num);
        }
        */
        // identify integer part
        int intStart = 0;
        int intEnd = 0;
        while (intStart < num.length() && num.charAt(intStart) == '0') {
            intStart++;
        }
        while (intEnd < num.length() && num.charAt(intEnd) >= '0' && num.charAt(intEnd) <= '9') {
            intEnd++;
        }
        // append length of integer part
        buf.append(intEnd - intStart);
        // append number without leading 0s
        buf.append(num.subSequence(intStart, num.length()));
    }

}
