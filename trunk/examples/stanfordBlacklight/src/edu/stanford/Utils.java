package edu.stanford;

//could import static, but this seems clearer
import org.solrmarc.tools.CallNumUtils;

public class Utils {

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
		if (isSerial && !rawCallnum.equals(loppedCallnum)) 
		{  
			// it's a serial and call number has a part/volume suffix
			//   basic call num sorts as shelfkey, volume suffix sorts as reverse key
			String loppedShelfkey = getShelfKey(loppedCallnum, recId);
			String volSuffix = rawCallnum.substring(loppedCallnum.length()).trim();
			String volSortString = CallNumUtils.getReverseShelfKey(CallNumUtils.normalizeSuffix(volSuffix));
			return loppedShelfkey + " " + volSortString;
		}
		else
			// regular shelfkey is correct for sort
			return getShelfKey(rawCallnum);
	}


	/**
	 * return a sortable shelving key for the call number
	 * @param rawCallnum - the call number for which a shelfkey is desired
	 */
	public static String getShelfKey(String rawCallnum) {
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
			if (CallNumUtils.isValidLC(rawCallnum))
				result = CallNumUtils.getLCShelfkey(rawCallnum, recId);
			if ( (result == null || result.equals(rawCallnum)) 
					&& CallNumUtils.isValidDewey(rawCallnum) )
				result = CallNumUtils.getDeweyShelfKey(rawCallnum);
		}
		catch (Exception e) {
		}

		if (result == null || result.equals(rawCallnum)) 
			result = CallNumUtils.normalizeSuffix(rawCallnum);
		return result;
	}

	/**
	 * return a sortable shelving key for the call number
	 * @param rawCallnum - the call number for which a shelfkey is desired
	 * @param callnumTypeGuess - what kind of call number is it likely to be?
	 * @param recId - record id, for error messages
	 */
	public static String getShelfKey(String rawCallnum, String callnumTypeGuess, String recId) {
		String result = null;
		try {
			if (callnumTypeGuess.equals("LC") || callnumTypeGuess.equals("LCPER"))
				result = CallNumUtils.getLCShelfkey(rawCallnum, recId);
			else if (callnumTypeGuess.equals("DEWEY") || callnumTypeGuess.equals("DEWEYPER"))
				result = CallNumUtils.getDeweyShelfKey(rawCallnum);
			else 
				result = CallNumUtils.normalizeSuffix(rawCallnum);
		}
		catch (Exception e) {
		}
		
		if (result == null) {
			try {
				if (CallNumUtils.isValidLC(rawCallnum))
					result = CallNumUtils.getLCShelfkey(rawCallnum, recId);
				if ( (result == null || result.equals(rawCallnum)) 
						&& CallNumUtils.isValidDewey(rawCallnum) )
					result = CallNumUtils.getDeweyShelfKey(rawCallnum);
			}
			catch (Exception e) {
			}
		}
		if (result == null || result.equals(rawCallnum)) 
			result = CallNumUtils.normalizeSuffix(rawCallnum);
		return result;
	}
}
