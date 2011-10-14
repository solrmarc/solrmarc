package edu.stanford;

import static edu.stanford.CallNumUtils.*;
import static org.junit.Assert.*;

import org.junit.*;

public class CallNumberUnitTests {


	/**
	 * remove box suffix, if it exists
	 */
@Test
	public void testRemoveBoxSuffix()
	{
		String callnum = "M1522 BOX 1";
		assertEquals("M1522", removeVolSuffix(callnum));
	}

	/**
	 * remove carton suffix, if it exists
	 */
@Test
	public void testRemoveCartonSuffix()
	{
		String callnum = "M1479 CARTON 1";
		assertEquals("M1479", removeVolSuffix(callnum));
	}

	/**
	 * remove flat box suffix, if it exists
	 */
@Test
	public void testRemoveFlatBoxSuffix()
	{
		String callnum = "M1522 FLAT BOX 17";
		assertEquals("M1522", removeVolSuffix(callnum));
	}

	/**
	 * remove half box suffix, if it exists
	 */
@Test
	public void testRemoveHalfBoxSuffix()
	{
		String callnum = "M1522 HALF BOX 17";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * remove half carton suffix, if it exists
	 */
@Test
	public void testRemoveHalfCartonSuffix()
	{
		String callnum = "M1522 HALF CARTON 17";
		assertEquals("M1522", removeVolSuffix(callnum));
	}

	/**
	 * remove index suffix, if it exists
	 */
@Test
	public void testRemoveIndexSuffix()
	{
		String callnum = "ML1 .I614 INDEX 1969-1986";
		assertEquals("ML1 .I614", removeVolSuffix(callnum));
		callnum = "KD270 .E64 INDEX:A/K";
		assertEquals("KD270 .E64", removeVolSuffix(callnum));
	}
	
	/**
	 * remove large folder suffix, if it exists
	 */
@Test
	public void testLargeFolderSuffix()
	{
		String callnum = "M1522 LARGE FOLDER 26";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * remove large map folder suffix, if it exists
	 */
@Test
	public void testRemoveLargeMapFolderSuffix()
	{
		String callnum = "M1522 LARGE MAP FOLDER 26";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * remove call number grade suffix, if it exists
	 */
@Test
	public void testRemoveLCGradeSuffix()
	{
		String callnum = "TX 519 .L18 GRADE 1";
		assertEquals("TX 519 .L18", removeLCVolSuffix(callnum));
		callnum = "TX 519 .L18ST GRADE 8";
		assertEquals("TX 519 .L18ST", removeLCVolSuffix(callnum));
	}

	/**
	 * remove map folder suffix, if it exists
	 */
@Test
	public void testRemoveMapFolderSuffix()
	{
		String callnum = "M1522 MAP FOLDER 26";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * remove mfilm reel suffix, if it exists
	 */
@Test
	public void testRemoveMfilmReelSuffix()
	{
		//TODO: works if this isn't treated as LC call number, and it should be
		// ALPHANUM
		String callnum = "CD3031 .A35 T-60 MFILM REEL 3";
		assertEquals("CD3031 .A35 T-60", removeVolSuffix(callnum));
		callnum = "Z7164 .S67 M54 MFILM REEL 42";
		assertEquals("Z7164 .S67 M54", removeVolSuffix(callnum));
	}

	/**
	 * remove os box suffix, if it exists
	 */
@Test
	public void testOSBoxSuffix()
	{
		String callnum = "M1522 OS BOX 1";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * remove os folder suffix, if it exists
	 */
@Test
	public void testOSFolderSuffix()
	{
		String callnum = "M1522 OS FOLDER 1";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * remove small folder suffix, if it exists
	 */
@Test
	public void testSmallFolderSuffix()
	{
		String callnum = "M1522 SMALL FOLDER 26";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * remove small map folder suffix, if it exists
	 */
@Test
	public void testRemoveSmallMapFolderSuffix()
	{
		String callnum = "M1522 SMALL MAP FOLDER 26";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * remove series suffix, if it exists
	 */
@Test
	public void testRemoveSeriesSuffix()
	{
		String callnum = "SC 165 SERIES 5 BOX 1";
		assertEquals("SC 165", removeVolSuffix(callnum));
		callnum = "M1090 SERIES 6 HALF BOX 39B";
		assertEquals("M1090", removeVolSuffix(callnum));
		callnum = "M1090 SERIES 16 HALF BOX 1.1";
		assertEquals("M1090", removeVolSuffix(callnum));
		callnum = "M1090 SERIES 16 OS FOLDER 276.3";
		assertEquals("M1090", removeVolSuffix(callnum));
		callnum = "M1090 SERIES 16 SMALL MAP FOLDER 72.02";
		assertEquals("M1090", removeVolSuffix(callnum));
		callnum = "M1090 SERIES 16 SMALL MAP FOLDER 318";
		assertEquals("M1090", removeVolSuffix(callnum));
		callnum = "M1090 SERIES 16 SMALL FOLDER 72.06";
		assertEquals("M1090", removeVolSuffix(callnum));
		callnum = "M1090 SERIES 16 LARGE MAP FOLDER 276.5";
		assertEquals("M1090", removeVolSuffix(callnum));
	}

	/**
	 * remove suppl suffix, if it exists
	 */
@Test
	public void testRemoveSupplSuffix()
	{
		String callnum = "Z7164.C81 N2 SUPPL.";
		assertEquals("Z7164.C81 N2", removeLCVolSuffix(callnum));
	}

	/**
	 * remove tube suffix, if it exists
	 */
@Test
	public void testTubeSuffix()
	{
		String callnum = "M1522 TUBE 1";
		assertEquals("M1522", removeVolSuffix(callnum));
	}
	
	/**
	 * when the whole call number is a suffix
	 */
	@Test
	public void testCallNumIsSuffix()
	{
		String callnum = "V.432: NO.7013-7017-7020 2004";
		assertEquals("", removeVolSuffix(callnum));
		callnum = "V.433: NO.7021-7024 2005";
		assertEquals("", removeVolSuffix(callnum));
// FIXME: is this what we actually want?
		callnum = "MFILM N.S. 10518:1-2";
		assertEquals("", removeVolSuffix(callnum));
	}

	/**
	 * remove suffix from gov doc call number starting "calif"
	 */
@Test
	public void testGovDocLopSuffix()
	{
		String callnum = "CALIF L1080 .J67 V.1-12:NO.1";
		assertEquals("CALIF L1080 .J67", removeVolSuffix(callnum));
		callnum = "CALIF T900 .J6 V.1-2";
		assertEquals("CALIF T900 .J6", removeVolSuffix(callnum));
		callnum = "CALIF C971 .N4 V.1:NO.2";
		assertEquals("CALIF C971 .N4", removeVolSuffix(callnum));
		callnum = "ECE/TRANS/SER.Z/1/2007 V.2";
		assertEquals("ECE/TRANS/SER.Z/1/2007", removeVolSuffix(callnum));
		callnum = "ECE/TRANS/185(VOL.1)";
// NOTE:  good enough
//		assertEquals("ECE/TRANS/185", removeVolSuffix(callnum));
		assertEquals("ECE/TRANS/185(", removeVolSuffix(callnum));
	}

	/**
	 * remove suffix from gov doc call number
	 */
// NOTE: I doubt this should be done like this.
//@Test
	public void testGovDocLopMoreSuffix() 
	{
		String callnum = "Y 4.ED 8/1:R 86/2/2003";
		assertEquals("Y 4.ED 8/1:R 86/2", removeVolSuffix(callnum));
		callnum = "Y 4.ED 8/1:R 86/2007";
		assertEquals("Y 4.ED 8/1:R 86", removeVolSuffix(callnum));
		callnum = "D 213.7:2009 CD-ROM";
		assertEquals("D 213.7", removeVolSuffix(callnum));
		callnum = "PREX 14.10:6/7-12";
		assertEquals("PREX 14.10", removeVolSuffix(callnum));
		callnum = "D 1.13/13:964";
		assertEquals("D 1.13/13", removeVolSuffix(callnum));
		callnum = "D 1.13/13:961-962";
		assertEquals("D 1.13/13", removeVolSuffix(callnum));
	}

	/**
	 * remove LC call number suffix when cutter ends in a letter
	 */
@Test
	public void testLCRemoveSuffixCutEndsLet()
	{
		String callnum = "TX519 .D26S 1954 V.1";
		assertEquals("TX519 .D26S 1954", removeLCVolSuffix(callnum));
		callnum = "TX519 .D26S 1954 V.2";
		assertEquals("TX519 .D26S 1954", removeLCVolSuffix(callnum));
	}
	
	/**
	 * remove LC call number suffix when there is space between LCC letters
	 *  and numbers
	 */
// NOTE: not worth worrying about at this time 2009-08-03
//@Test
	public void testLCRemoveSuffixClassHasSpace()
	{
		String callnum = "TX 519 .D26S 1954 V.1";
		assertEquals("TX 519 .D26S 1954", removeLCVolSuffix(callnum));
		callnum = "PQ 8550.21.R57 V.5 1992";
		assertEquals("PQ 8550.21.R57", removeLCVolSuffix(callnum));
		callnum = "DS 646.29.D5 J39 V.8";
		assertEquals("DS 646.29.D5 J39", removeLCVolSuffix(callnum));
	}

	/**
	 * remove Dewey call number suffix when cutter ends in a letter
	 */
@Test
	public void testDeweyRemoveSuffixCutEndsLet()
	{
		String callnum = "505 .N285B V.241-245 1973";
		assertEquals("505 .N285B", removeDeweyVolSuffix(callnum));
	}
	
	/**
	 * remove Dewey call number suffix when cutter starts with 2 letters
	 */
//NOTE: not worth worrying about at this time 2009-08-03
//@Test
	public void testDeweyRemoveSuffixCutStarts2Letters()
	{
		String callnum = "888.4 .JF78A V.5";
		assertEquals("888.4 .JF78A", removeDeweyVolSuffix(callnum));
	}

	/**
	 * remove LC call number suffix when there's a colon in the call number
	 */
@Test
	public void testRemoveSuffixLCwithColon()
	{
		String callnum = "Q1 .N2 V.434:NO.7031 2005:MAR.17";
		assertEquals("Q1 .N2", removeLCVolSuffix(callnum));
		callnum = "Q1 .N2 V.421-426 2003:INDEX";
		assertEquals("Q1 .N2", removeLCVolSuffix(callnum));
		callnum = "Q1 .N2 V.171 1953:JAN.-MAR.";
		assertEquals("Q1 .N2", removeLCVolSuffix(callnum));
		callnum = "Z286 .D47 J69 1992:MAR.-DEC.";
		assertEquals("Z286 .D47 J69", removeLCSerialVolSuffix(callnum));
		callnum = "QD1 .C59 1973:P.1-1252";
		assertEquals("QD1 .C59", removeLCSerialVolSuffix(callnum));
		callnum = "Q1 .S34 V.209:4452-4460 1980:JUL.-AUG.";
		assertEquals("Q1 .S34", removeVolSuffix(callnum));
		callnum = "Q1 .S34 V.293-294:5536-5543 2001:SEP-OC";
		assertEquals("Q1 .S34", removeVolSuffix(callnum));
	}

	/**
	 * remove Dewey call number suffix when there's a colon in the call number
	 */
@Test
	public void testRemoveSuffixDeweyWithColon()
	{
		String callnum = "505 .N285 V.434:1-680 2005";
		assertEquals("505 .N285", removeDeweyVolSuffix(callnum));
		callnum = "505 .N285 V.458:543--1212 2009";
		assertEquals("505 .N285", removeDeweyVolSuffix(callnum));
		callnum = "505 .N285 V.428:NO.6978-6981 2004";
		assertEquals("505 .N285", removeVolSuffix(callnum));
	}

	/**
	 * remove year suffix, if it exists
	 */
@Test
	public void testRemoveYearSuffix()
	{
		String callnum = "F1386 .A2 A2 1891 ";
		assertEquals("F1386 .A2 A2", removeYearSuffix(callnum));
		callnum = "F1386 .A2 A2 1592-1593";
		assertEquals("F1386 .A2 A2", removeYearSuffix(callnum));
		callnum = "F1386 .A2 A2 1903:JAN.-JUN.";
		assertEquals("F1386 .A2 A2", removeYearSuffix(callnum));
		callnum = "Z7511 .N33 1968-1971";
		assertEquals("Z7511 .N33", removeYearSuffix(callnum));
		callnum = "Z7511 .N33 1956/1957, 1960/1961-1966/196";
		assertEquals("Z7511 .N33", removeYearSuffix(callnum));
		callnum = "QD1 .C59 1975:P.1-742";
		assertEquals("QD1 .C59", removeYearSuffix(callnum));
	}


	/**
	 * remove year suffix, if it exists, as part of LC call number
	 */
@Test
	public void testRemoveLCYearSuffix()
	{
		String callnum = "F1386 .A2 A2 1891 ";
		assertEquals("F1386 .A2 A2", removeLCSerialVolSuffix(callnum));
		callnum = "F1386 .A2 A2 1592-1593";
		assertEquals("F1386 .A2 A2", removeLCSerialVolSuffix(callnum));
		callnum = "F1386 .A2 A2 1903:JAN.-JUN.";
		assertEquals("F1386 .A2 A2", removeLCSerialVolSuffix(callnum));
		callnum = "Z7511 .N33 1968-1971";
		assertEquals("Z7511 .N33", removeLCSerialVolSuffix(callnum));
		callnum = "Z7511 .N33 1956/1957, 1960/1961-1966/196";
		assertEquals("Z7511 .N33", removeLCSerialVolSuffix(callnum));
		callnum = "QD1 .C59 1975:P.1-742";
		assertEquals("QD1 .C59", removeLCSerialVolSuffix(callnum));
	}

}
