package org.solrmarc.tools;

import static org.solrmarc.tools.Utils.*;
import static org.junit.Assert.*;

import org.junit.*;

/**
 * Unit tests for methods in org.solrmarc.tools.Utils
 * @author naomi
 *
 */
public class UtilUnitTests {

	/**
	 * unit test for org.solrmarc.tools.Utils.getIxUnescapedOpenParen
	 */
@Test
	public void testGetIxUnescapedOpenParen()
	{
		// unescaped
		String str = "(blah";
		assertEquals("open paren index incorrect for " + str, 0, getIxUnescapedOpenParen(str));
		str = "blah(";
		assertEquals("open paren index incorrect for " + str, 4, getIxUnescapedOpenParen(str));
		str = "blah(blah";
		assertEquals("open paren index incorrect for " + str, 4, getIxUnescapedOpenParen(str));

		// escaped
		str = "\\(blah";
		assertEquals("open paren index incorrect for " + str, -1, getIxUnescapedOpenParen(str));
		str = "blah\\(";
		assertEquals("open paren index incorrect for " + str, -1, getIxUnescapedOpenParen(str));
		str = "blah\\(blah";
		assertEquals("open paren index incorrect for " + str, -1, getIxUnescapedOpenParen(str));
	}

	/**
	 * unit test for org.solrmarc.tools.Utils.getIxUnescapedComma
	 */
@Test
	public void testGetIxUnescapedComma()
	{
		// unescaped
		String str = ",blah";
		assertEquals("open paren index incorrect for " + str, 0, getIxUnescapedComma(str));
		str = "blah,";
		assertEquals("open paren index incorrect for " + str, 4, getIxUnescapedComma(str));
		str = "blah,blah";
		assertEquals("open paren index incorrect for " + str, 4, getIxUnescapedComma(str));
	
		// escaped
		str = "\\,blah";
		assertEquals("open paren index incorrect for " + str, -1, getIxUnescapedComma(str));
		str = "blah\\,";
		assertEquals("open paren index incorrect for " + str, -1, getIxUnescapedComma(str));
		str = "blah\\,blah";
		assertEquals("open paren index incorrect for " + str, -1, getIxUnescapedComma(str));
	}


	/**
	 * unit test for org.solrmarc.tools.Utils.removeTrailingPeriod
	 */
@Test
	public void testRemoveTrailingPeriod()
	{
		// chars that must be present before period for removal
		String regex = "\\w{3,}";  
		String str = "123.";
		String errmsg = "period removal incorrect for regex ";
		
		// at least three word chars
		assertEquals(errmsg + regex + " : " + str, "123", removeTrailingPeriod(str, regex));
		str = "4."; // too few chars
		assertEquals(errmsg + regex + " : " + str, "4.", removeTrailingPeriod(str, regex));
		str = "i like oz."; // too few chars
		assertEquals(errmsg + regex + " : " + str, "i like oz.", removeTrailingPeriod(str, regex));
		
		// at least three letters
		regex = "[A-Za-z]{3,}";  
		str = "123."; // chars not in regex
		assertEquals(errmsg + regex + " : " + str, "123.", removeTrailingPeriod(str, regex));
		str = "Boo."; // 
		assertEquals(errmsg + regex + " : " + str, "Boo", removeTrailingPeriod(str, regex));
		// following space
		str = "Boo. "; // 
		assertEquals(errmsg + regex + " : " + str, "Boo", removeTrailingPeriod(str, regex));
	}
	
	
	/**
	 * unit test for org.solrmarc.tools.Utils.removeOuterBrackets
	 */
@Test
	public void testRemoveOuterBrackets()
	{
		String str = "[blah]";
		String errmsg = "outer bracket removal incorrect for ";
		assertEquals(errmsg + str + " : ", "blah", removeOuterBrackets(str));
		str = "blah";
		assertEquals(errmsg + str + " : ", "blah", removeOuterBrackets(str));
		str = "[blah";
		assertEquals(errmsg + str + " : ", "blah", removeOuterBrackets(str));
		str = "blah]";
		assertEquals(errmsg + str + " : ", "blah", removeOuterBrackets(str));
		// spaces
		str = "[ blah]";
		assertEquals(errmsg + str + " : ", "blah", removeOuterBrackets(str));
		str = "blah] ";
		assertEquals(errmsg + str + " : ", "blah", removeOuterBrackets(str));
		str = " [ blah ] ";
		assertEquals(errmsg + str + " : ", "blah", removeOuterBrackets(str));
	}
	
	/**
	 * unit test for org.solrmarc.tools.Utils.removeTrailingChars
	 */
@Test
	public void testRemoveTrailingChars()
	{
		String regex = " *([,/;:])";  // chars to remove
		String str = "blah /";
		String errmsg = "trailing char removal incorrect for ";
		assertEquals(errmsg + str + " : ", "blah", removeTrailingChar(str, regex));
		str = "blah;";
		assertEquals(errmsg + str + " : ", "blah", removeTrailingChar(str, regex));
		str = "blah,";
		assertEquals(errmsg + str + " : ", "blah", removeTrailingChar(str, regex));
		str = "blah;,";
		assertEquals(errmsg + str + " : ", "blah;", removeTrailingChar(str, regex));
	}
	
	/**
	 * unit test for org.solrmarc.tools.Utils.removeTrailingCharsAndPerod
	 */
@Test
	public void testRemoveTrailingCharsAndPeriod()
	{
		// unescaped
		String periodRegex = "[A-Za-z]{3,}";
		String trailCharsRegex = "([,/;:])";
		String str = "blah.;";
		String errmsg = "trailing char and period removal incorrect for ";
		assertEquals(errmsg + str + " : ", "blah", removeTrailingCharAndPeriod(str, trailCharsRegex, periodRegex));
		str = "blah. /";
		assertEquals(errmsg + str + " : ", "blah", removeTrailingCharAndPeriod(str, trailCharsRegex, periodRegex));
		str = "ah. /";
		assertEquals(errmsg + str + " : ", "ah.", removeTrailingCharAndPeriod(str, trailCharsRegex, periodRegex));
		// multiple iterations of trailing periods and trailing chars
		str = "blah.;/";
		trailCharsRegex = "([,/;:])*";
		assertEquals(errmsg + str + " : ", "blah", removeTrailingCharAndPeriod(str, trailCharsRegex, periodRegex));
		trailCharsRegex = "[,/;:]";
		assertEquals(errmsg + str + " : ", "blah", removeAllTrailingCharAndPeriod(str, trailCharsRegex, periodRegex));
	}


}
