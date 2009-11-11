package edu.stanford;
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

import java.util.regex.*;

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
		
    private static final String PUNCT_PREFIX = "([\\.:\\/])?";
	private static final String NS_PREFIX = "(n\\.s\\.?\\,? ?)?";
	private static final String MONTHS = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec";
	private static final String VOL_LETTERS = "[\\:\\/]?(bd|jahrg|new ser|no|pts?|ser|t|v|vols?|vyp" + "|" + MONTHS + ")";
	private static final String VOL_NUMBERS = "\\d+([\\/-]\\d+)?( \\d{4}([\\/-]\\d{4})?)?( ?suppl\\.?)?";
	private static final String VOL_NUMBERS_LOOSER = "\\d+.*";
	private static final String VOL_NUM_AS_LETTERS = "[A-Z]([\\/-]\\[A-Z]+)?.*";
	
	private static final Pattern volPattern = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "\\.? ?" + VOL_NUMBERS, Pattern.CASE_INSENSITIVE);
	private static final Pattern volPatternLoose = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "\\.? ?" + VOL_NUMBERS_LOOSER, Pattern.CASE_INSENSITIVE);
	private static final Pattern volPatLetters = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "[\\/\\. ]" + VOL_NUM_AS_LETTERS , Pattern.CASE_INSENSITIVE);

	private static final String MORE_VOL = "[\\:\\/]?(box|carton|flat box|grade|half box|half carton|index|large folder|large map folder|map folder|mfilm|reel|os box|os folder|small folder|small map folder|suppl|tube|series)";
	private static final Pattern moreVolPattern = Pattern.compile(MORE_VOL + ".*", Pattern.CASE_INSENSITIVE);

	private static final String FOUR_DIGIT_YEAR = " \\d{4}\\D";
	private static final Pattern fourDigitYearPattern = Pattern.compile(FOUR_DIGIT_YEAR + ".*", Pattern.CASE_INSENSITIVE);

	/**
	 * remove volume suffix from LC call number if it is present 
	 * @param rawLCcallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	public static String removeLCVolSuffix(String rawLCcallnum)
	{
		// get suffix to last occurring cutter, if there is one
		String cut2suffix = org.solrmarc.tools.CallNumUtils.getSecondLCcutterSuffix(rawLCcallnum);
		String lastSuffix = cut2suffix;
		if (lastSuffix == null) {
			String cut1suffix = org.solrmarc.tools.CallNumUtils.getFirstLCcutterSuffix(rawLCcallnum);
			if (cut1suffix != null) {
				// first cutter suffix may contain second cutter
				String cut2 = org.solrmarc.tools.CallNumUtils.getSecondLCcutter(rawLCcallnum);
				if (cut2 != null) {
					int ix = cut1suffix.indexOf(cut2);
					if (ix != -1)
						lastSuffix = cut1suffix.substring(0, ix);
					else
						lastSuffix = cut1suffix;
				}
				else
					lastSuffix = cut1suffix;
			}
		}

		// could put last ditch effort with tightest pattern, but don't want to take out too much		

		if (lastSuffix != null) {
			Matcher matcher = volPattern.matcher(lastSuffix);
			if (!matcher.find()) {
				matcher = volPatternLoose.matcher(lastSuffix);
				if (!matcher.find()) {
					matcher = volPatLetters.matcher(lastSuffix);
					if (!matcher.find()) {
						matcher = moreVolPattern.matcher(lastSuffix);
					}
				}
			}
// look for first / last match, not any match (subroutine?)?
			if (matcher.find(0)) {
				// return orig call number with matcher part lopped off.
				int ix = rawLCcallnum.indexOf(lastSuffix) + matcher.start();
				if (ix != -1 && ix < rawLCcallnum.length()) {
					return rawLCcallnum.substring(0, ix).trim();
				}
			}				
		}
		else {
			return removeMoreVolSuffix(rawLCcallnum);
		}

		return rawLCcallnum;
	}

	/**
	 * remove volume suffix from LC call number, if it is present. Call number 
	 *  is for a serial, so if the suffix starts with 4 digits, it can be removed.
	 * @param rawLCcallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	public static String removeLCSerialVolSuffix(String rawLCcallnum)
	{
		String try1 = removeLCVolSuffix(rawLCcallnum);
		if (!try1.equals(rawLCcallnum))
			return try1;
		else
			return removeYearSuffix(rawLCcallnum);
	}
	
	/**
	 * remove suffix that begins with a space followed by four digits followed
	 *  by a non-digit.  (4 digits usually mean a year)
	 * @param rawCallnum
	 * @return call number without the year suffix, or full call number if no 
	 *  year suffix is present.
	 */
	public static String removeYearSuffix(String rawCallnum)
	{
		Matcher matcher = fourDigitYearPattern.matcher(rawCallnum);
		if (matcher.find(0)) {
			// return orig call number with matcher part lopped off.
			int ix = matcher.start();
			if (ix != -1 && ix < rawCallnum.length()) {
				return rawCallnum.substring(0, ix).trim();
			}
		}				

		return rawCallnum;
	}

	
	/**
	 * remove volume suffix from Dewey call number if it is present
	 * @param rawDeweyCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information was present.
	 */
	public static String removeDeweyVolSuffix(String rawDeweyCallnum)
	{
		String cutSuffix = org.solrmarc.tools.CallNumUtils.getDeweyCutterSuffix(rawDeweyCallnum);
		if (cutSuffix == null)
			return rawDeweyCallnum;
		
		Matcher matcher = volPattern.matcher(cutSuffix);
		if (!matcher.find()) {
			matcher = volPatternLoose.matcher(cutSuffix);
			if (!matcher.find()) {
				matcher = volPatLetters.matcher(cutSuffix);
				if (!matcher.find()) {
					matcher = moreVolPattern.matcher(cutSuffix);
				}
			}
		}
		
		if (matcher.find(0)) {
			// return orig call number with matcher part lopped off.
			int ix = rawDeweyCallnum.indexOf(cutSuffix) + matcher.start();
			if (ix != -1 && ix < rawDeweyCallnum.length()) {
				return rawDeweyCallnum.substring(0, ix).trim();
			}
		}
		return removeMoreVolSuffix(rawDeweyCallnum);
	}

	/**
	 * remove volume suffix from Dewey call number if it is present.  Call 
	 *  number is for a serial, so if the suffix starts with a year, it can be removed.
	 * @param rawDeweyCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	public static String removeDeweySerialVolSuffix(String rawDeweyCallnum)
	{
		String try1 = removeDeweyVolSuffix(rawDeweyCallnum);
		if (!try1.equals(rawDeweyCallnum))
			return try1;
		else
			return removeYearSuffix(rawDeweyCallnum);
	}
	
	
	/**
	 * try to remove volume suffix from call number of unknown type.  It first
	 *  tries it as an LC call number, then as a Dewey call number, then
	 *  just goes for it
	 * @param rawCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	public static String removeVolSuffix(String rawCallnum) 
	{
		String lopped = removeLCVolSuffix(rawCallnum);
		if (lopped.equals(rawCallnum))
			lopped = removeDeweyVolSuffix(rawCallnum);
		// might be non-LC, non-Dewey
		if (lopped.equals(rawCallnum)) 
		{
			Matcher matcher = volPattern.matcher(rawCallnum);
			if (!matcher.find()) {
				matcher = volPatternLoose.matcher(rawCallnum);
				if (!matcher.find()) {
					matcher = volPatLetters.matcher(rawCallnum);
				}
			}
	// look for first / last match, not any match (subroutine?)?
			if (matcher.find(0)) {
				// return orig call number with matcher part lopped off.
				int ix = matcher.start();
				if (ix != -1 && ix < rawCallnum.length()) {
					lopped= rawCallnum.substring(0, ix).trim();
				}
			}
			else
				lopped = removeMoreVolSuffix(rawCallnum);
		}
		return lopped;
	}
	
	
	/**
	 * remove volume suffix from call number if it is present.  Call number is
	 *  for a serial, so if the suffix starts with a year, it can be removed.
	 * @param rawCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	public static String removeSerialVolSuffix(String rawCallnum)
	{
		String try1 = removeVolSuffix(rawCallnum);
		if (!try1.equals(rawCallnum))
			return try1;
		else
			return removeYearSuffix(rawCallnum);
	}

	
	/**
	 * go after more localized call number suffixes, such as "box" "carton"
	 *  "series" "index"
	 * @param rawCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	public static String removeMoreVolSuffix(String rawCallnum) 
	{
		Matcher matcher = moreVolPattern.matcher(rawCallnum);
		if (matcher.find()) {
			// return orig call number with matcher part lopped off.
			int ix = matcher.start();
			if (ix != -1 && ix < rawCallnum.length()) {
				return rawCallnum.substring(0, ix).trim();
			}
		}
		return rawCallnum;
	}
	
	
	/**
	 * returns true if the entire call number is a volume suffix
	 * @param rawCallnum
	 */
	public static boolean callNumIsVolSuffix(String rawCallnum) {
		if (rawCallnum != null) {
			Matcher matcher = volPattern.matcher(rawCallnum);
			if (!matcher.find()) {
				matcher = volPatternLoose.matcher(rawCallnum);
				if (!matcher.find()) {
					matcher = volPatLetters.matcher(rawCallnum);
					if (!matcher.find()) {
						matcher = moreVolPattern.matcher(rawCallnum);
					}
				}
			}
			if (matcher.find(0)) 
				return true;
		}
		return false;
	}
	
	/**
	 * return a sortable shelving key for the call number
	 * @param rawCallnum - the call number for which a shelfkey is desired
	 */
	public static String getShelfKey(String rawCallnum) {
		if (rawCallnum == null)
			return null;
		return getShelfKey(rawCallnum, null);
	}

	/**
	 * return a sortable shelving key for the call number
	 * @param rawCallnum - the call number for which a shelfkey is desired
	 * @param recId - record id, for error messages
	 */
	public static String getShelfKey(String rawCallnum, String recId) {
		String result = null;
		try {
			if (org.solrmarc.tools.CallNumUtils.isValidLC(rawCallnum))
				result = org.solrmarc.tools.CallNumUtils.getLCShelfkey(rawCallnum, recId);
			if ( (result == null || result.equals(rawCallnum)) 
					&& org.solrmarc.tools.CallNumUtils.isValidDewey(rawCallnum) )
				result = org.solrmarc.tools.CallNumUtils.getDeweyShelfKey(rawCallnum);
		}
		catch (Exception e) {
		}
	
		if (result == null || result.equals(rawCallnum)) 
			result = org.solrmarc.tools.CallNumUtils.normalizeSuffix(rawCallnum);
		return result;
	}

	/**
	 * return a sortable shelving key for the call number
	 * @param rawCallnum - the call number for which a shelfkey is desired
	 * @param callnumTypeGuess - what kind of call number is it likely to be?
	 * @param recId - record id, for error messages
	 */
	public static String getShelfKey(String rawCallnum, String callnumTypeGuess, String recId) {
		if (rawCallnum == null)
			return null;
		String result = null;
		try {
			if (callnumTypeGuess.equals("LC") || callnumTypeGuess.equals("LCPER"))
				result = org.solrmarc.tools.CallNumUtils.getLCShelfkey(rawCallnum, recId);
			else if (callnumTypeGuess.equals("DEWEY") || callnumTypeGuess.equals("DEWEYPER"))
				result = org.solrmarc.tools.CallNumUtils.getDeweyShelfKey(rawCallnum);
		}
		catch (Exception e) {
		}
		
		if (result == null) 
			return getShelfKey(rawCallnum, recId);

		return result;
	}

	/**
	 * returns a sortable call number.  If it is the call number for a serial,
	 *  the lexical sort will be in ascending order, but will have the most 
	 *  recent volumes first.  If it's not the call number for a serial, the
	 *  sort will be strictly in ascending order.
	 *  
	 * @param rawCallnum
	 * @param loppedCallnum - the call number with volume/part information lopped off
	 * @param isSerial - true if the call number is for a serial 
	 * @return
	 */
	public static String getVolumeSortCallnum(String rawCallnum, String loppedCallnum, boolean isSerial) 
	{
		return getVolumeSortCallnum(rawCallnum, loppedCallnum, isSerial, null);
	}

	/**
	 * returns a sortable call number.  If it is the call number for a serial,
	 *  the lexical sort will be in ascending order, but will have the most 
	 *  recent volumes first.  If it's not the call number for a serial, the
	 *  sort will be strictly in ascending order.
	 *  
	 * @param rawCallnum
	 * @param loppedCallnum - the call number with volume/part information lopped off
	 * @param isSerial - true if the call number is for a serial 
	 * @param recId - record id, for error messages
	 * @return
	 */
	public static String getVolumeSortCallnum(String rawCallnum, String loppedCallnum, boolean isSerial, String recId) 
	{
		if (rawCallnum == null)
			return null;

		if (isSerial && !rawCallnum.equals(loppedCallnum)) 
		{  
			// it's a serial and call number has a part/volume suffix
			//   basic call num sorts as shelfkey, volume suffix sorts as reverse key
			String loppedShelfkey = getShelfKey(loppedCallnum, recId);
			String volSuffix = rawCallnum.substring(loppedCallnum.length()).trim();
			String volSortString = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(org.solrmarc.tools.CallNumUtils.normalizeSuffix(volSuffix));
			return loppedShelfkey + " " + volSortString;
		}
		else
			// regular shelfkey is correct for sort
			return getShelfKey(rawCallnum);
	}

}
