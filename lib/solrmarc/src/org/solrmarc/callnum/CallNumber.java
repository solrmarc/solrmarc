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
 * Provides a generic interface for building call number objects.
 * <h4>Constructors</h4>
 * Implementing classes are encourages to provide two constructors:
 * <ol>
 * <li>a constructor with no parameters which will just initialize the object, and</li>
 * <li>a constructor with a <code>String</code> parameter, which will <code>init</code>
 * the object and <code>parse</code> the parameter.</li>
 * </ol>
 * <h4>Parsing and fields</h4>
 * <code>parse</code> will set internal fields to represent logical parts of the call number.
 * Use <code>null</code> when some part of the call number is absent.
 * For example, if there is an internal field to represent a cutter but the parser finds no
 * cutter, set the field to <code>null</code>.
 *
 * @author Tod Olson, University of Chicago
 *
 */
public interface CallNumber {
    /**
     * Reset any internal fields so the object is ready to <code>parse</code> a new
     * call number string, or to construct a new call number via the setter methods.
     */
    public void init();

    /**
     * Parse call number and populate any fields.
     */
    public void parse(String callNumber);

    /**
     * Reports whether the string given to <code>parse</code> matched the pattern for a call number.
     * Behavior is unspecified if call number was built from setters or if object has been initialized
     * since the last <code>parse</code>.
     */
    public boolean isValid();

    /**
     * Compute and return a sort key for the call number.
     *
     * @return sort key
     */
    public String getShelfKey();
}
