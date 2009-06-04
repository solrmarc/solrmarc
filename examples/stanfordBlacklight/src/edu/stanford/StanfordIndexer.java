package edu.stanford;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

import org.marc4j.marc.*;
//could import static, but this seems clearer
import org.solrmarc.tools.Utils;  
import org.solrmarc.tools.CallNumUtils;

/**
 * Stanford custom methods
 * @author Naomi Dushay
 */
public class StanfordIndexer extends org.solrmarc.index.SolrIndexer
{
    /** access facet values */
	public static enum Access {
		ONLINE,
		AT_LIBRARY;
		
		/**
		 * need to override for text of multiple words
		 */
		@Override 
		public String toString() {
			switch(this) {
				case AT_LIBRARY:
					return "At the Library";
				case ONLINE:
					return "Online";
			}
			String lc = super.toString().toLowerCase();
			String firstchar = lc.substring(0, 1).toUpperCase();
			return lc.replaceFirst(".{1}", firstchar);
		}

	}

	/** format facet values */
	public static enum Format {
		BOOK,
		COMPUTER_FILE,
		CONFERENCE_PROCEEDINGS,
		IMAGE,
		JOURNAL_PERIODICAL,
		MANUSCRIPT_ARCHIVE,
		MAP_GLOBE,
		MICROFORMAT,
		MUSIC_RECORDING,
		MUSIC_SCORE,
		NEWSPAPER,
		SOUND_RECORDING,
		THESIS,
		VIDEO,
		OTHER;
		
		/**
		 * need to override for text of multiple words
		 */
		@Override 
		public String toString() {
			switch(this) {
				case COMPUTER_FILE:
					return "Computer File";
				case CONFERENCE_PROCEEDINGS:
					return "Conference Proceedings";
				case JOURNAL_PERIODICAL:
					return "Journal/Periodical";
				case MANUSCRIPT_ARCHIVE:
					return "Manuscript/Archive";
				case MAP_GLOBE:
					return "Map/Globe";
				case MUSIC_RECORDING:
					return "Music - Recording";
				case MUSIC_SCORE:
					return "Music - Score";
				case SOUND_RECORDING:
					return "Sound Recording";
			}
			String lc = super.toString().toLowerCase();
			String firstchar = lc.substring(0, 1).toUpperCase();
			return lc.replaceFirst(".{1}", firstchar);
		}
	}
		
	/** call number values */
	public static final String GOV_DOC_FACET_VAL = "Government Document";
	public static final String GOV_DOC_CALLNO_PFX = "Gov't Doc: ";
	
	
	/**
	 * Default constructor
     * @param indexingPropsFile the name of xxx_index.properties file mapping 
     *  solr field names to values in the marc records
     * @param propertyDirs - array of directories holding properties files
	 */
    public StanfordIndexer(String indexingPropsFile, String[] propertyDirs) 
    		throws FileNotFoundException, IOException, ParseException 
    {
        super(indexingPropsFile, propertyDirs);
    }

// Id Method  -------------------- Begin ----------------------------- Id Method
    
     /**
     * We have our ckeys in 001 subfield a.  Marc4j is unhappy with subfields 
     * in a control field so this is a kludge work around.
     */
    public String getId(final Record record)
    {
    	ControlField fld = (ControlField) record.getVariableField("001");
        if (fld != null && fld.getData() != null)
        {
        	String id = fld.getData();
        	if (id.startsWith("a"))
        		return id.substring(1);
        	else 
        		return null;
        }
        return null;
    }
    

// Id Method  --------------------  End  ----------------------------- Id Method
    
// Format Method  --------------- Begin -------------------------- Format Method
    
    /**
     * Returns the formats of the resource as described by a marc bib record
     * @param record
     * @return Set of strings containing format values for the resource
     */
    @SuppressWarnings("unchecked")
	public Set<String> getFormats(final Record record)
    {
        Set<String> resultSet = new HashSet<String>();
    	
    	// As of July 28, 2008, algorithms for formats are currently in email 
    	// message from Vitus Tang to Naomi Dushay, cc Phil Schreur, Margaret 
    	// Hughes, and Jennifer Vine dated July 23, 2008.
    	
        // Note: MARC21 documentation refers to char numbers that are 0 based,
    	//  just like java string indexes, so char "06" is at index 6, and is
    	//  the seventh character of the field

    	// assign formats based on leader chars 06, 07 and chars in 008
    	String leaderStr = record.getLeader().toString();
    	char leaderChar07 = leaderStr.charAt(7);
    	VariableField f008 = record.getVariableField("008");
    	char leaderChar06 = leaderStr.charAt(6);
    	switch (leaderChar06) {
	    	case 'a':
	    		if (leaderChar07 == 'a' || leaderChar07 == 'm') 
	            	resultSet.add(Format.BOOK.toString());
	    		break;
	    	case 'b':
	    	case 'p':
	        	resultSet.add(Format.MANUSCRIPT_ARCHIVE.toString());
	    		break;
	    	case 'c':
	    	case 'd':
	        	resultSet.add(Format.MUSIC_SCORE.toString());
	    		break;
	    	case 'e':
	    	case 'f':
	        	resultSet.add(Format.MAP_GLOBE.toString());
	    		break;
	    	case 'g':
    			// look for m or v in 008 field, char 33 (count starts at 0)
    			if (f008 != null && f008.find("^.{33}[mv]")) 
    	        	resultSet.add(Format.VIDEO.toString());
	    		break;
	    	case 'i':
	        	resultSet.add(Format.SOUND_RECORDING.toString());
	    		break;
	    	case 'j':
	        	resultSet.add(Format.MUSIC_RECORDING.toString());
	    		break;
	    	case 'k': 
    			// look for i, k, p, s or t in 008 field, char 33 (count starts at 0)
    			if (f008 != null && f008.find("^.{33}[ikpst]")) 
    	        	resultSet.add(Format.IMAGE.toString());
	    		break;
	    	case 'm':
    			// look for a in 008 field, char 26 (count starts at 0)
    			if (f008 != null && f008.find("^.*{26}a")) 
    	        	resultSet.add(Format.COMPUTER_FILE.toString());
	    		break;
	    	case 'o':  // instructional kit
	        	resultSet.add(Format.OTHER.toString());
	    		break;
	    	case 'r':  // object
	        	resultSet.add(Format.OTHER.toString());
	    		break;
	    	case 't': 
	    		if (leaderChar07 == 'a' || leaderChar07 == 'm') 
	            	resultSet.add(Format.BOOK.toString());
	    		break;
    	} // end switch

    	if ( resultSet.isEmpty() || resultSet.size() == 0 ) {
    		// look for serial publications - leader/07 s
        	if (leaderChar07 == 's') {
            	if (f008 != null) {
        			char c21 = ((ControlField) f008).getData().charAt(21);
        			switch (c21) {
        				case 'd':   // updating database (ignore)
        					break;
        				case 'l':   // updating looseleaf (ignore)
        					break;
        				case 'm':   // monographic series
        					resultSet.add(Format.BOOK.toString());
        					break;
        				case 'n':
        					resultSet.add(Format.NEWSPAPER.toString());
        					break;
	        			case 'p':
	                		// b4 2008-12-02 was: resultSet.add(Format.JOURNAL.toString());
	                		resultSet.add(Format.JOURNAL_PERIODICAL.toString());
	        				break;
	        			case 'w':   // web site
	        				break;
        			}
        		}
        	}
    	}
    	
    	// look for serial publications 006/00 s
       	if ( resultSet.isEmpty() || resultSet.size() == 0 ) {
        	VariableField f006 = record.getVariableField("006");
        	if (f006 != null && f006.find("^[s]") ) {
        		char c04 = ((ControlField) f006).getData().charAt(4);
        		switch (c04) {
    				case 'd':   // updating database (ignore)
    					break;
    				case 'l':   // updating looseleaf (ignore)
    					break;
    				case 'm':   // monographic series
    					resultSet.add(Format.BOOK.toString());
    					break;
    				case 'n':
    					resultSet.add(Format.NEWSPAPER.toString());
    					break;
        			case 'p':
                		resultSet.add(Format.JOURNAL_PERIODICAL.toString());
        				break;
        			case 'w':   // web site
        				break;
        			case ' ':
                		resultSet.add(Format.JOURNAL_PERIODICAL.toString());
        		}
        	}
        	// if still nothing, see if 007/00s serial publication by default
        	else if ( ( resultSet.isEmpty() || resultSet.size() == 0 ) && leaderChar07 == 's') {
            	if (f008 != null) {
        			char c21 = ((ControlField) f008).getData().charAt(21);
        			switch (c21) {
        				case 'd':
        				case 'l':
        				case 'm':
        				case 'n':
            			case 'p':
            			case 'w': 
            				break;
            			case ' ':
            				// b4 2008-12-02 was: resultSet.add(Format.SERIAL_PUBLICATION.toString());
                    		resultSet.add(Format.JOURNAL_PERIODICAL.toString());
        			}
            	}
        	}
    	}
       	
    	// look for conference proceedings in 6xx
		List<DataField> dfList = record.getDataFields();
		for (DataField df:dfList) {
			if (df.getTag().startsWith("6")) {
				List<String> subList = Utils.getSubfieldStrings(df, 'x');
				subList.addAll(Utils.getSubfieldStrings(df, 'v'));
				for (String s : subList) {
					if (s.toLowerCase().contains("congresses")) {
                		resultSet.remove(Format.JOURNAL_PERIODICAL.toString());
						resultSet.add(Format.CONFERENCE_PROCEEDINGS.toString());
					}
				}
			}
		}
    	    	
    	// thesis is determined by the presence of a 502 field.
        Set<String> dissNote = new LinkedHashSet<String>();
        dissNote.addAll(getSubfieldDataAsSet(record, "502", "a", " "));
        if (!dissNote.isEmpty() || dissNote.size() != 0)
        	resultSet.add(Format.THESIS.toString());
        
        // microfilm is determined by 245 subfield h containing "microform"
        Set<String> titleH = new LinkedHashSet<String>();
        titleH.addAll(getSubfieldDataAsSet(record, "245", "h", " "));       
        // check the h subfield of the 245 field
        if (Utils.setItemContains(titleH, "microform"))
        	resultSet.add(Format.MICROFORMAT.toString());
        
        // check for format information from 999 ALPHANUM call numbers
        List<VariableField> list999 = record.getVariableFields("999");
        for (VariableField vf : list999) {
        	DataField df = (DataField) vf;
        	String subw = Utils.getSubfieldData(df, 'w');
        	if (subw != null && subw.trim().equalsIgnoreCase("ALPHANUM")) {
    			String suba = Utils.getSubfieldData(df, 'a');
    			if (suba != null) {
    				suba = suba.trim();
        			if (suba.startsWith("MFILM"))
        				resultSet.add(Format.MICROFORMAT.toString());
        			else if (suba.startsWith("MCD"))
        				resultSet.add(Format.MUSIC_RECORDING.toString());
        		}
    		}
        }        

    	// if we still don't have a format, it's an "other"
    	if ( resultSet.isEmpty() || resultSet.size() == 0 )
        	resultSet.add(Format.OTHER.toString());
        
        return resultSet;
    }
    
// Format Method  --------------- End   -------------------------- Format Method

    
// Standard Number Methods --------- Begin ------------- Standard Number Methods    
    
    /**
     * returns the ISBN(s) from a record for external lookups (such as Google 
     *  Book Search)  (rather than the potentially larger set of ISBNs for the 
     *  end user to search our index)
     * @param record
     * @return Set of strings containing ISBN numbers
     */
    public Set<String> getISBNs(final Record record)
    {
    	//ISBN algorithm 
		// 1. all 020 subfield a starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
		// 2. if no ISBN from any 020 subfield a "yields a search result", use all 020 subfield z starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
    	// 
    	// NOTE BENE: there is no way to ensure field order in the retrieved lucene document
    	    	
        Set<String> isbnSet = new HashSet<String>();

        Set<String> candidates = getFieldList(record, "020a");
        if (!candidates.isEmpty())
        	isbnSet.addAll(Utils.returnValidISBNs(candidates));
        
        if (isbnSet.isEmpty()) {
            candidates.addAll(getFieldList(record, "020z"));
        	isbnSet.addAll(Utils.returnValidISBNs(candidates));
        }

    	return isbnSet;        	
    }
    
    /**
     * returns the ISBN(s) from a record for the end user to search our index
     *  (not the potentially smaller set of ISBNs for us to use for external
     *  lookups such as Google Book Search)
     * @param record
     * @return Set of strings containing ISBN numbers
     */
    public Set<String> getUserISBNs(final Record record)
    {
    	//ISBN algorithm - more inclusive
    	// 1. all 020 subfield a starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
    	//  AND
		// 2. all 020 subfield z starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
    	    	
        Set<String> isbnSet = new HashSet<String>();

        Set<String> candidates = getFieldList(record, "020a");
        candidates.addAll(getFieldList(record, "020z"));
        isbnSet.addAll(Utils.returnValidISBNs(candidates));
    	return isbnSet;        	
    }

    
    /**
     * returns the ISSN(s) from a record.  As ISSN is rarely multivalued, but
     *  MAY be multivalued, Naomi has decreed
     * This is a custom routine because we want multiple ISSNs only if they are 
     * subfield a.
     * @param record
     * @return Set of strings containing ISSN numbers
     */
    public Set<String> getISSNs(final Record record)
    {
    	// ISSN algorithm - rare but possible to have multiple ISSNs for an item
		// 1. 022 subfield a with ISSN
		// 2. if no ISSN from any 022 subfields a, use 022 subfield z

		// NOTE 1: the ISSN is always an eight digit number divided into two halves by a hyphen.
    	// NOTE 2: the last digit of an ISSN is a check digit and could be an uppercase X.

    	Set<String> issnSet = new HashSet<String>();
    	
        Set<String> set = getFieldList(record, "022a");
        if (set.isEmpty())
        	set.addAll(getFieldList(record, "022z"));

        Pattern p = Pattern.compile("^\\d{4}-\\d{3}[X\\d]$");
        Iterator<String> iter = set.iterator();
        while (iter.hasNext()) {
            String value = (String)iter.next().trim();
            // check we have the right pattern
            if (p.matcher(value).matches()) 
            	issnSet.add(value);
        }
    	return issnSet;        	
    }

    /**
     * returns the OCLC numbers from a record, if they exist.  Note that this
     *  method does NOT pad with leading zeros.  (Who needs 'em?)
     * @param record
     * @return Set of Strings containing OCLC numbers.  There could be none.
     */
    public Set<String> getOCLCNums(final Record record)
    {
    	// OCLC number algorithm
    	// 1. 035 subfield a, value prefixed "(OCoLC-M)"  -  remove prefix
    	// 2. if no 035 subfield a prefixed "(OCoLC-M)", 
    	//      use 079 field subfield a, value prefixed "ocm" or "ocn" - remove prefix
    	//      (If the id is eight digits in length, the prefix is "ocm", if 9 digits, "ocn")
    	//      Id's that are smaller than eight digits are padded with leading zeros.
    	// 3. if no "(OCoLC-M)" 035 subfield a and no "ocm" or "ocn" 079 field subfield a, 
    	//    use 035 subfield a, value prefixed "(OCoLC)"  -  remove prefix

        Set<String> oclcSet = new HashSet<String>();

        Set<String> set035a = getFieldList(record, "035a");
        oclcSet = Utils.getPrefixedVals(set035a, "(OCoLC-M)");
        if (oclcSet.isEmpty()) { 
        	// check for 079 prefixed "ocm" or "ocn"
        	// 079 is not repeatable
        	String val = getFirstFieldVal(record, "079a");
        	if (val != null && val.length() != 0)
        	{
        		String good = null;
        		if (val.startsWith("ocm"))
        			good = Utils.removePrefix(val, "ocm");
        		else if (val.startsWith("ocn"))
        			good = Utils.removePrefix(val, "ocn");
            	if (good != null && good.length() != 0)
            	{
            		oclcSet.add(good.trim());
            		return oclcSet;
            	}
        	}
        	// check for 035a prefixed "(OCoLC)"
            oclcSet = Utils.getPrefixedVals(set035a, "(OCoLC)");
        }
    	return oclcSet;
    }

// Standard Number Methods --------- End --------------- Standard Number Methods    
    
    
// Title Methods ------------------- Begin ----------------------- Title Methods    
    
    /**
     * returns the 245 a + b + h, without the trailing slash before the last 
     *  subfield c
     */
//    @SuppressWarnings("unchecked")
// TODO:  can remove this method when brief title display settles down.
/*
	public Set<String> getBriefTitleDisplay(final Record record)
    {
    	Set<String> resultSet = new HashSet<String>();
    	List<VariableField> list245 = record.getVariableFields("245");
    	for (VariableField vf : list245) {
    		DataField d245 = (DataField) vf;
    		StringBuffer buffer = new StringBuffer();
    		List<Subfield> subList = d245.getSubfields();
    		for (Subfield sub : subList) {
    			char subcode = sub.getCode();
    			if (subcode == 'a' || subcode == 'b' || subcode == 'h') {
    				if (buffer.length() > 0) {
                        buffer.append(" " + sub.getData().trim());
                    } else {
                        buffer.append(sub.getData().trim());
                    }
    			}
    		}
    		// remove trailing slash, if present
    		if (buffer.length() > 0) {
        		int lastCharIx = buffer.length() - 1;
        		if (buffer.substring(lastCharIx).toString().equals("/")) {
        			resultSet.add(buffer.deleteCharAt(lastCharIx).toString().trim());
        		}
        		else
        			resultSet.add(buffer.toString());
    		}
    	}
    	return resultSet;
    }
*/
    
    /**
     * returns string for title sort:  a string containing
     *  1. the uniform title (130), if there is one - not including non-filing chars 
     *      as noted in 2nd indicator
     * followed by
     *  2.  the 245 title, not including non-filing chars as noted in ind 2
     */
    @SuppressWarnings("unchecked")
	public String getSortTitle(final Record record) 
    {
    	StringBuffer resultBuf = new StringBuffer();

    	// uniform title
    	DataField df = (DataField) record.getVariableField("130");
    	if (df != null) 
        	resultBuf.append(getAlphaSubfldsAsSortStr(df, false));
    	
    	// 245 (required) title statement
       	df = (DataField) record.getVariableField("245");
       	if (df != null)
       		resultBuf.append(getAlphaSubfldsAsSortStr(df, true));

    	return resultBuf.toString().trim();
    }
    
// Title Methods -------------------- End ------------------------ Title Methods    
    

// Author Methods ----------------- Begin ----------------------- Author Methods    
    
    /**
     * returns string for author sort:  a string containing
     *  1. the main entry author, if there is one 
     *  2. the main entry uniform title (240), if there is one - not including 
     *    non-filing chars as noted in 2nd indicator
     * followed by
     *  3.  the 245 title, not including non-filing chars as noted in ind 2
     */
    @SuppressWarnings("unchecked")
	public String getSortAuthor(final Record record) 
    {
    	StringBuffer resultBuf = new StringBuffer();

    	DataField df = (DataField) record.getVariableField("100");
    	// main entry personal name
    	if (df != null) 
        	resultBuf.append(getAlphaSubfldsAsSortStr(df, false));

    	df = (DataField) record.getVariableField("110");
    	// main entry corporate name
    	if (df != null) 
        	resultBuf.append(getAlphaSubfldsAsSortStr(df, false));

    	df = (DataField) record.getVariableField("111");
    	// main entry meeting name
    	if (df != null) 
        	resultBuf.append(getAlphaSubfldsAsSortStr(df, false));

    	// need to sort fields missing 100/110/111 last
    	if (resultBuf.length() == 0) {
    		resultBuf.append(Character.toChars(Character.MAX_CODE_POINT)); 
    		resultBuf.append(' '); // for legibility in luke
    	}
    	
    	// uniform title, main entry
      	df = (DataField) record.getVariableField("240");
       	if (df != null)
           	resultBuf.append(getAlphaSubfldsAsSortStr(df, false));
    	
    	// 245 (required) title statement
       	df = (DataField) record.getVariableField("245");
       	if (df != null)
       		resultBuf.append(getAlphaSubfldsAsSortStr(df, true));

    	return resultBuf.toString().trim();
    }
    
// Author Methods ------------------ End ------------------------ Author Methods    

// Subject Methods ----------------- Begin --------------------- Subject Methods    

    /**
     * Gets the value strings, but skips over 655a values when Lane is one of
     *  the locations.  Also ignores 650a with value "nomesh".
     * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
     * @return Set of strings containing values without Lane 655a or 650a nomesh
     */
    public Set<String> getTopicAllAlphaExcept(final Record record, final String fieldSpec) 
    {
    	Set<String> resultSet = getAllAlphaExcept(record, fieldSpec);
    	if (getBuildings(record).contains("LANE-MED"))
    	{
    		Set<String> skipSet = getFieldList(record, "655a");
    		resultSet.removeAll(skipSet);
    	}

		resultSet.remove("nomesh");
    	return resultSet;
    }
        
    /**
     * Gets the value strings, but skips over 655a values when Lane is one of
     *  the locations.  Also ignores 650a with value "nomesh". Removes trailing 
     *  characters indicated in regular expression, PLUS trailing period if it 
     *  is preceded by its regular expression.
     *
     * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
     * @param trailingCharsRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately 
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED. 
     *  Note that the regular expression will NOT have the period or '$' at 
     *  the end. 
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately 
     *   precede the period for it to be removed.) 
     * @return Set of strings containing values without trailing characters and 
     * without Lane 655a or 650a nomesh
     */
    public Set<String> getTopicWithoutTrailingPunct(final Record record, final String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx) 
    {
    	Set<String> resultSet = removeTrailingPunct(record, fieldSpec, charsToReplaceRegEx, charsB4periodRegEx);
    	if (getBuildings(record).contains((String) "LANE-MED"))
    	{
    		Set<String> skipSet = getFieldList(record, "655a");
    		resultSet.removeAll(skipSet);
    	}

		resultSet.remove("nomesh");
    	return resultSet;
    }

    
// Subject Methods ----------------- End ----------------------- Subject Methods    

// Access Methods ----------------- Begin ----------------------- Access Methods    

    /**
     * returns the access facet values for a record.  A record can have multiple
     *  values: online, on campus and upon request are not mutually exclusive.
     * @param record
     * @return Set of Strings containing access facet values.
     */
    @SuppressWarnings("unchecked")
	public Set<String> getAccessMethods(final Record record)
    {
        Set<String> resultSet = new HashSet<String>();
        
        List<VariableField> list999 = record.getVariableFields("999");
        for (VariableField vf : list999) {
        	DataField f999 = (DataField) vf;
        	if (!ignoreItem(f999)) {
        		if (onlineItem(f999))
        			resultSet.add(Access.ONLINE.toString());
        		else 
        			resultSet.add(Access.AT_LIBRARY.toString());
        	}
        }
		
        if (getFullTextUrls(record).size() > 0)
        	resultSet.add(Access.ONLINE.toString());

        if (getSFXUrls(record).size() > 0)
        	resultSet.add(Access.ONLINE.toString());

    	return resultSet;
    }

// Access Methods -----------------  End  ----------------------- Access Methods    
    
// URL Methods -------------------- Begin -------------------------- URL Methods    
    
    /**
     * retruns a set of strings containing the sfx urls in a record.  Returns
     *   empty set if none
     * @param record
     */
    public Set<String> getSFXUrls(final Record record)
    {
        Set<String> resultSet = new HashSet<String>();
        // all 956 subfield u contain fulltext urls that aren't SFX
        Set<String> f956urls = getFieldList(record, "956u");
        for (String url: f956urls) {
        	if (isSFXUrl(url))
                resultSet.add(url);
        }
        return resultSet;
    }
    
    /**
     * returns the URLs for the full text of a resource described by the record
     * @param record
     * @return Set of Strings containing full text urls, or empty set if none
     */
    @SuppressWarnings("unchecked")
	public Set<String> getFullTextUrls(final Record record)
    {
        Set<String> resultSet = new HashSet<String>();

        // all 956 subfield u containing fulltext urls that aren't SFX
        Set<String> f956urls = getFieldList(record, "956u");
        for (String url: f956urls) {
        	if (!isSFXUrl(url))
        		resultSet.add(url);
        }
        
        List<VariableField> list856 = record.getVariableFields("856");
        for (VariableField vf : list856) {
        	DataField df = (DataField) vf;
        	List<String> possUrls = Utils.getSubfieldStrings(df, 'u');
        	// the next two are for a GSB request form for offsite materials
        	possUrls.remove("http://www.gsb.stanford.edu/jacksonlibrary/services/sal3_request.html");
        	possUrls.remove("https://www.gsb.stanford.edu/jacksonlibrary/services/sal3_request.html");
        	char ind2 = df.getIndicator2();
        	switch (ind2) {
    	    	case '0':
    	    		resultSet.addAll(possUrls);
    	    		break;
    	    	case '2':
    	    		break;
    	    	default:
    	    		if (!isSupplementalUrl(df))
    	    			resultSet.addAll(possUrls);
    	    		break;
        	}
        }        
        
        return resultSet;
    }
    
    /**
     * returns the URLs for supplementary information (rather than fulltext)
     * @param record
     * @return Set of Strings containing supplementary urls, or empty string if none
     */
    @SuppressWarnings("unchecked")
	public Set<String> getSupplUrls(final Record record)
    {
        Set<String> resultSet = new HashSet<String>();

        List<VariableField> list856 = record.getVariableFields("856");
        for (VariableField vf : list856) {
         	DataField df = (DataField) vf;
        	List<String> possUrls = Utils.getSubfieldStrings(df, 'u');
        	char ind2 = df.getIndicator2();
        	switch (ind2) {
    	    	case '2':
    	    		resultSet.addAll(possUrls);
    	    		break;
    	    	case '0':
    	    		break;
    	    	default:
    	    		if (isSupplementalUrl(df))
    	    			resultSet.addAll(possUrls);
    	    		break;
        	}
        }        
    	return resultSet;
    }
    
    private boolean isSFXUrl(String urlStr) {
    	if (urlStr.startsWith("http://caslon.stanford.edu:3210/sfxlcl3?") )
    		return true;
    	else return false;
    }
    
    /**
     * return true if passed 856 field contains a supplementary url (rather than
     *  a fulltext URL.
     * Determine by presence of "table of contents" or "sample text" string
     *  (ignoring case) in subfield 3 or z.
     * Note:  Called only when second indicator is not 0 or 2.
     */
    private boolean isSupplementalUrl(DataField f856) {
		boolean supplmntl = false;
		List<String> list3z = Utils.getSubfieldStrings(f856, '3');
		list3z.addAll(Utils.getSubfieldStrings(f856, 'z'));
		for (String s : list3z) {
			if (s.toLowerCase().contains("table of contents") ||
    			s.toLowerCase().contains("sample text"))
				supplmntl = true;
		}
		return supplmntl;
    }

// URL Methods --------------------  End  -------------------------- URL Methods    
    

// Pub Date Methods  -------------- Begin --------------------- Pub Date Methods    
    
    /**
     * returns the publication date from a record, if it is present
     * @param record
     * @return String containing publication date, or null if none
     */
    public String getPubDate(final Record record)
    {
// TODO: should pubDate be multiValued?

    	// date1 is bytes 7-10 (0 based index) in 008 field
    	ControlField cf = (ControlField) record.getVariableField("008");
    	if (cf != null)
    	{
        	String date1Str = cf.getData().substring(7,11);
        	
        	if (isdddd(date1Str))
        		return date1Str;
        	else if (isdddu(date1Str))
        		return date1Str.substring(0, 3) + "0s";
        	else if (isdduu(date1Str)) 
        		return getCenturyString(date1Str.substring(0,2));
    	}
     		
    	return null;
    }

    /**
     * returns the sortable publication date from a record, if it is present
     * @param record
     * @return String containing publication date, or null if none
     */
    public String getPubDateSort(final Record record)
    {
    	// date1 is bytes 7-10 (0 based index) in 008 field
    	ControlField cf = (ControlField) record.getVariableField("008");
    	if (cf != null)
    	{
        	String dateStr = cf.getData().substring(7,11);
        	// hyphens sort before 0, so the lexical sorting will be correct.  I think.
        	if (isdddd(dateStr))
        		return dateStr;
        	else if (isdddu(dateStr))
        		return dateStr.substring(0, 3) + "-";
        	else if (isdduu(dateStr)) 
        		return dateStr.substring(0, 2) + "--";
        	else if (isduuu(dateStr)) 
        		return dateStr.substring(0, 1) + "---";
    	}
     		
    	return null;
    }
    
 
    /** access facet values */
	public static enum PubDateGroup {
		THIS_YEAR,
		LAST_3_YEARS,
		LAST_10_YEARS,
		LAST_50_YEARS,
		MORE_THAN_50_YEARS_AGO,
		;
		
		/**
		 * need to override for text of multiple words
		 */
		@Override 
		public String toString() {
			switch(this) {
				case THIS_YEAR:
					return "This year";
				case LAST_3_YEARS:
					return "Last 3 years";
				case LAST_10_YEARS:
					return "Last 10 years";
				case LAST_50_YEARS:
					return "Last 50 years";
				case MORE_THAN_50_YEARS_AGO:
					return "More than 50 years ago";
			}
			String lc = super.toString().toLowerCase();
			String firstchar = lc.substring(0, 1).toUpperCase();
			return lc.replaceFirst(".{1}", firstchar);
		}

	}
    
	private int cYearInt = Calendar.getInstance().get(Calendar.YEAR);
	private String cYearStr = Integer.toString(cYearInt);
	
    /**
     * returns the publication date groupings from a record, if it is present
     * @param record
     * @return Set of Strings containing the publication date groupings associated
     *   with the publish date
     */
    public Set<String> getPubDateGroups(final Record record)
    {
        Set<String> resultSet = new HashSet<String>();
        
       	// get the pub date, with decimals assigned for inclusion in ranges
       	ControlField cf = (ControlField) record.getVariableField("008");
       	if (cf != null)
       	{
           	String dateStr = cf.getData().substring(7,11);
           	if (isdddd(dateStr))  // exact year
           	{
           		int year = Integer.parseInt(dateStr);
           		// "this year" and "last three years" are for 4 digits only
                if ( year >= (cYearInt - 1) )
               		resultSet.add(PubDateGroup.THIS_YEAR.toString());
               	if ( year >= (cYearInt - 3) )
               		resultSet.add(PubDateGroup.LAST_3_YEARS.toString());

               	resultSet.addAll(getPubDateGroupsForYear(year));
           	}
           	else if (isdddu(dateStr))  // decade
           	{
           		String first3Str = dateStr.substring(0, 3);
           		int first3int = Integer.parseInt(first3Str);
           		
           		if (first3Str.equals(cYearStr.substring(0, 3)))  // this decade?
           		{  
               		resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
           			resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
               		if (cYearInt %10 <= 3)
               			resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
           		}
           		else {  // not current decade
           			if (cYearInt % 10 <= 4)  // which half of decade?
               		{
               			// first half of decade - current year ends in 0-4
               			if (first3int == (cYearInt / 10) - 1)
                   			resultSet.add(PubDateGroup.LAST_10_YEARS.toString());

               			if (first3int >= (cYearInt / 10) - 6)
                   			resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
               			else
               				resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
               		}
               		else { 
               			// second half of decade - currend year ends in 5-9
               			if (first3int > (cYearInt / 10) - 5)
                   			resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
               			else
               				resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
               		}
           		}
           	}
           	else if (isdduu(dateStr)) {   // century
           		String first2Str = dateStr.substring(0, 2);
           		int first2int = Integer.parseInt(first2Str);
           		
           		if (first2Str.equals(cYearStr.substring(0, 2))) {
           			// current century
           			resultSet.add(PubDateGroup.LAST_50_YEARS.toString());

           			if (cYearInt % 100 <= 19) 
               			resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
           		}
           		else {
           			if ( first2int == (cYearInt / 100) - 1) 
           			{
           				// previous century
       					if (cYearInt % 100 <= 25)
       						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
       					else
               				resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
           			}
           			else
           				resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
           		}
           	}
           	// we don't work with duuu or uuuu or other date strings
       	}
       	
    	return resultSet;
    }
    
    
    private Set<String> getPubDateGroupsForYear(int year) 
    {
        Set<String> resultSet = new HashSet<String>();

       	if ( year >= (cYearInt - 10) )
       		resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
       	if ( year >= (cYearInt - 50))
       		resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
       	if (year < (cYearInt - 50) && (year > -1.0))
       		resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
    	return resultSet;	
    }
    
    
    /**
     * given a string containing two digits representing the year, return
     *  the century in a sting, including "century":
     *    00 --> 1st century   11 --> 12th century   etc.
     */
	private String getCenturyString(String yearDigits) {
		int centuryYearInt = Integer.parseInt(yearDigits) + 1;
		String centuryYearStr = String.valueOf(centuryYearInt);
		return centuryYearStr + getNumberSuffix(centuryYearStr) + " century";
	}

	/**
	 * given a positive number, return the correct adjective suffix for that number
	 *   e.g.:  1 -->  "st"  3 --> "rd"  11 --> "th" 22 --> "nd"
	 */
	private String getNumberSuffix(String numberStr) {
		int len = numberStr.length();
		// teens are a special case
		if (len == 2 && numberStr.charAt(0) == '1')
			return("th");

		switch (numberStr.charAt(len-1)) {
			case '1': 
				return ("st");
			case '2':
				return ("nd");
			case '3':
				return ("rd");
			default:
				return ("th");
		}
	}
    
    private boolean isdddd(String str) {
        Pattern p = Pattern.compile("^\\d{4}$");
        if (p.matcher(str).matches()) 
        	return true;
    	return false;
    }
    
    private boolean isdddu(String str) {
        Pattern p = Pattern.compile("^\\d{3}u$");
        if (p.matcher(str).matches()) 
        	return true;
    	return false;
    }
    
    private boolean isdduu(String str) {
        Pattern p = Pattern.compile("^\\d{2}uu$");
        if (p.matcher(str).matches()) 
        	return true;
    	return false;
    }
    
    private boolean isduuu(String str) {
        Pattern p = Pattern.compile("^\\duuu$");
        if (p.matcher(str).matches()) 
        	return true;
    	return false;
    }

// Pub Date Methods  --------------  End  --------------------- Pub Date Methods    
    

// Subject Methods  ---------------- Begin --------------------- Subject Methods    

    /**
	 * returns era strings derived from 650y and 651y, or 045a if no 650 or 651
	 * @param record Marc record to extract data from
	 */
// TODO:  should be a way to specify a default value in the properties file
	public Set<String> getEras(final Record record)
    {
        Set<String> result = getFieldList(record, "650y:651y");
/* era from SolrIndexer routine not polished yet ...
        if (result.size() == 0)
        	// get era information from 045a 
        	result = super.getEra(record);
*/
        if (result.size() == 0)
        	result.add("other");
        return result;
	}
	
// Subject Methods  ----------------  End  --------------------- Subject Methods    

	
// AllFields Methods  --------------- Begin ------------------ AllFields Methods    
		
	/**
	 * fields in the 0xx range (not including control fields) that should be
	 *  indexed in allfields
	 */
	Set<String> keepers0xx = new HashSet<String>();
	{
		keepers0xx.add("024");
		keepers0xx.add("027");
		keepers0xx.add("028");
	}
	
	/**
	 * Returns all subfield contents of all the data fields (non control fields)
	 * @param record Marc record to extract data from
	 */
	@SuppressWarnings("unchecked")
	public String getAllFields(final Record record)
    {
        StringBuffer result = new StringBuffer(5000);
// TODO: do we really want ALL the subfields of ALL the DataFields in the allFields value ... 
//   but it's much easier to include everything than cherry pick
        List<DataField> dataFieldList = record.getDataFields();
        for (DataField df : dataFieldList) {
        	String tag = df.getTag();
        	if (!tag.startsWith("9") && !tag.startsWith("0")
        			|| (tag.startsWith("0") && keepers0xx.contains(tag))) {
            	List<Subfield> subfieldList = df.getSubfields();
            	for (Subfield sf : subfieldList) {
            		result.append(sf.getData() + " ");
            	}
        	}
        }
		return result.toString().trim();
	}

// AllFields Methods  ---------------  End  ------------------ AllFields Methods    

	
// Item Related Methods ------------- Begin --------------- Item Related Methods    
	
	
	// 999 scheme:
	// a - call num
	// w - call num scheme
	// k - current location
	// l - home location
	// m - library code
	
	@SuppressWarnings("unchecked")
	public final Set<String> getBuildings(final Record record) {
		Set<String> result = new HashSet<String>();
        List<VariableField> list999 = record.getVariableFields("999");
        for (VariableField vf : list999) {
        	String buildingStr = getBuildingFrom999((DataField) vf);
        	if (buildingStr != null)
        		result.add(buildingStr);
        }
		return result;
	}
	
	/**
	 * return the building from the 999m for non-ignored item
	 */
	private String getBuildingFrom999(DataField f999) 
	{
		if (ignoreItem(f999))
			return null;

		String subm = Utils.getSubfieldData(f999, 'm');
		if (subm != null)
			return subm.trim();

		return null;
	}
	
	/**
	 * return the barcode from the 999i for non-ignored item
	 */
	private String getBarcodeFrom999(DataField f999) 
	{
		if (ignoreItem(f999))
			return null;

		String subi = Utils.getSubfieldData(f999, 'i');
		if (subi != null)
			return subi.trim();

		return null;
	}
	
	/**
	 * return the location from the 999l (that's L) for non-ignored item
	 */
	private String getLocationFrom999(DataField f999) 
	{
		if (ignoreItem(f999))
			return null;

		String subl = Utils.getSubfieldData(f999, 'l');
		if (subl != null)
			return subl.trim();

		return null;
	}

	// load translation maps for building and location
	private String bldgMapName = "";
	private String locationMapName = "";
	{
        try
        {
            bldgMapName = loadTranslationMap(null, "library_short_map.properties");
            locationMapName = loadTranslationMap(null, "location_map.properties");
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
	}
	
	/**
	 * for search result display:
	 * @return set of barcode - lib - location - callnum fields from 999s
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getItemDisplay(final Record record) {
		Set<String> result = new HashSet<String>();
// FIXME: sep should be globally avail constant (for tests also?)
		String sep = " -|- ";
		List<VariableField> list999 = record.getVariableFields("999");
		for (VariableField vf: list999) {
			DataField df = (DataField) vf;
			if (!onlineItem(df)) {
	        	String barcode = getBarcodeFrom999(df); 

	        	// building --> short name from mapping
	        	String building = "";
	        	String origBldg = getBuildingFrom999(df);
	        	if (origBldg != null && origBldg.length() > 0) 
	        		building = Utils.remap(origBldg, findMap(bldgMapName), true);
	        	
	        	// location --> mapped
	        	String location = "";
	        	String origLoc = getLocationFrom999(df);
	        	if (origLoc != null && origLoc.length() > 0) 
	        		location = Utils.remap(origLoc, findMap(locationMapName), true);

// FIXME:  need to get all call numbers, except a few (shelve-by, internet, etc)        	
	        	String callnum = getLCCallNumberFrom999(df);
	        	if (callnum != null)
	        		callnum = CallNumUtils.removeLCVolSuffix(callnum);
	        	else {
	        		callnum = getDeweyCallNumberFrom999((DataField) vf);
	            	if (callnum != null)
	            		callnum = CallNumUtils.removeDeweyVolSuffix(callnum);
	        	}
	        	if (barcode != null && barcode.length() > 0 &&
	        			building.length() > 0 &&
	        			location != null && location.length() > 0 &&
	        			callnum != null && callnum.length() > 0)
	        		result.add(barcode + sep + building + sep + location + sep + callnum);
System.out.println("adding result: " + barcode + sep + building + sep + location + sep + callnum);
				
			}  // end physical item (not online)
		} // end loop through 999s
		return result;
	}
	
// Item Related Methods -------------  End  --------------- Item Related Methods    

	
// Call Number Methods -------------- Begin ---------------- Call Number Methods    
    
	/**
	 * This field only gets call numbers from 999, where our local
	 *   call numbers are, and only selects the LC and Gov Docs call numbers.
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getLocalLCCallNums(final Record record) {
        Set<String> result = new HashSet<String>();
        
        List<VariableField> list999 = record.getVariableFields("999");
        for (VariableField vf : list999) {
        	String callnumStr = getLCCallNumberFrom999((DataField) vf);
        	if (callnumStr != null)
        		result.add(callnumStr);
        }
        
        // TODO: also take LC if it fits pattern, but starts with X?

        return result;
	}
	
	/**
	 * This field only gets call numbers from 999, where our local
	 *   call numbers are, and only selects the Dewey call numbers.
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getLocalDeweyCallNums(final Record record) {
        Set<String> result = new HashSet<String>();
        
        List<VariableField> list999 = record.getVariableFields("999");
        for (VariableField vf : list999) {
        	String callnumStr = getDeweyCallNumberFrom999((DataField) vf);
        	if (callnumStr != null)
        		result.add(callnumStr);
        }
        
        // TODO: also take LC if it fits pattern, but starts with X?

        return result;
	}
	
	/**
	 * This is a facet field to enable discovery by subject, as designated by
	 *  call number.  It looks at our local values in the 999 and returns
	 *  the broad category strings (for LC, the first letter; for government 
	 *  docs, the constant String in GOV_DOC_FACET_VAL)
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getLCCallNumBroadCats(final Record record) {
        Set<String> result = new HashSet<String>();
        
        Set<String> lcSet = getLCforClassification(record);
        for (String lc : lcSet) {
        	if (lc != null)
        		result.add(lc.substring(0, 1).toUpperCase());
        }
        
        // also include the constant for government documents
		List<VariableField> list = record.getVariableFields("999");
        for (VariableField vf : list) {
        	DataField df = (DataField) vf;
        	if (!ignoreCallNum(df)) {
            	String subw = Utils.getSubfieldData(df, 'w');
           		if (subw != null && subw.trim().equalsIgnoreCase("SUDOC"))
               		result.add(GOV_DOC_FACET_VAL);
        	}
        }

        // presence of 086 implies it's a government document
		list = record.getVariableFields("086");
		if (!list.isEmpty())
   			result.add(GOV_DOC_FACET_VAL);
        
		return result;
	}
	
	/**
	 * This is a facet field to enable discovery by subject, as designated by
	 *  call number.  It looks at our local values in the 999, and returns
	 *  the broad category strings (for Dewey, "x00s";
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getDeweyCallNumBroadCats(final Record record) {
        Set<String> result = new HashSet<String>();
        
        Set<String> deweySet = getDeweyforClassification(record);
        for (String dewey: deweySet) {
        	if (dewey != null && dewey.length() > 2)
        		result.add(dewey.substring(0, 1) + "00s");
        }
        
		return result;
	}
	
	/**
	 * This is for a facet field to enable discovery by subject, as designated by
	 *  call number.  It looks at our local values in the 999 and returns
	 *  the secondary level category strings (for LC, the 1-3 letters at the 
	 *  beginning)
	 */
	public final Set<String> getLCCallNumCats(final Record record) {
        Set<String> result = new HashSet<String>();

        Set<String> lcSet = getLCforClassification(record);
        for (String lc : lcSet) {
        	String letters = CallNumUtils.getLCstartLetters(lc);
        	if (letters != null)
        		result.add(letters);
        }
        
		return result;
	}

	/**
	 * This is for a facet field to enable discovery by subject, as designated by
	 *  call number.  It looks at our local values in the 999, and returns
	 *  the secondary level category strings (for Dewey, "xx0s")
	 */
	public final Set<String> getDeweyCallNumCats(final Record record) {
        Set<String> result = new HashSet<String>();

        Set<String> deweySet = getDeweyforClassification(record);
        for (String dewey: deweySet) {
        	if (dewey != null && dewey.length() > 2)
        		result.add(dewey.substring(0, 2) + "0s");
        }
        
		return result;
	}

	/**
	 * This is for a facet field to enable discovery by subject, as designated by
	 *  call number.  It looks at our local LC values in the 999 and returns 
	 *  the Strings before the Cutters in the call numbers (LC only)
	 */
	public final Set<String> getLCCallNumsB4Cutter(final Record record) {
        Set<String> result = new HashSet<String>();

        Set<String> lcSet = getLCforClassification(record);
        for (String lc : lcSet) {
        	result.add(CallNumUtils.getPortionBeforeCutter(lc));
        }
        
		return result;
	}

	/**
	 * This is for a facet field to enable discovery by subject, as designated by
	 *  call number.  It looks at our local Dewey values in the 999 and returns 
	 *  the Strings before the Cutters in the call numbers (Dewey only)
	 */
	public final Set<String> getDeweyCallNumsB4Cutter(final Record record) {
        Set<String> result = new HashSet<String>();

        Set<String> deweySet = getDeweyforClassification(record);
        for (String dewey: deweySet) {
        	result.add(CallNumUtils.getPortionBeforeCutter(addLeadingZeros(dewey)));
        }
		return result;
	}

	// 999 scheme:
	// a - call num
	// w - call num scheme
	// k - current location
	// l - home location
	// m - library code
	
	/**
	 * get LC call number (portion)s from the bib record: 999
	 * (not currently 050, 051, 090, 099)
	 *  for deriving classifications
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getLCforClassification(Record record) {

		Set<String> result = new HashSet<String>();
		
        List<VariableField> list999 = record.getVariableFields("999");
        for (VariableField vf : list999) {
        	DataField df999 = (DataField) vf;
        	if (!ignoreCallNum(df999)) {
            	String callnumStr = getLCCallNumberFrom999(df999);
            	if (callnumStr != null)
            		result.add(callnumStr);
        	}
        }

/*        
		// look in other LC tags 
		String [] tagsLC = {"050", "051", "090", "099"};
		List<VariableField> listLCfields = record.getVariableFields(tagsLC);
        for (VariableField vf : listLCfields) {
        	String suba = getSubfieldData((DataField) vf, 'a');
        	if (suba != null) {
        		suba = suba.trim();
               	if (isValidLC(suba))
            		result.add(suba);
        	}
        }
*/		
        return result;
	}
	
	/**
	 * get Dewey call number (portion)s from the bib record: 999
	 *  (not currently 082, 092, 099)
	 *  for deriving classifications
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getDeweyforClassification(Record record) {

		Set<String> result = new HashSet<String>();
		
        List<VariableField> list999 = record.getVariableFields("999");
        for (VariableField vf : list999) {
        	DataField df999 = (DataField) vf;
        	if (!ignoreCallNum(df999)) {
	        	String callnumStr = getDeweyCallNumberFrom999(df999);
	        	if (callnumStr != null)
	        		result.add(callnumStr);
        	}
        }
/*        
		// look in other Dewey 
        String [] tagsDewey = {"082", "092", "099"};
		List<VariableField> listDeweyfields = record.getVariableFields(tagsDewey);
        for (VariableField vf : listDeweyfields) {
        	String suba = getSubfieldData((DataField) vf, 'a');
        	if (suba != null) {
        		suba = suba.trim();
	           	if (isValidDewey(suba)) 
	        		result.add(addLeadingZeros(suba));
        	}
        }
*/		
        return result;
	}
	
	/**
	 * return the call number from the 999 if it is not withdrawn, if it is LC
	 * or Dewey.  Also return an appropriate constant
	 */
	private String getCallNumberFrom999(DataField f999) 
	{
		if (ignoreItem(f999) || onlineItem(f999) || ignoreCallNum(f999))
			return null;

		String callnum = getLCCallNumberFrom999(f999);
		if (callnum != null)
			return callnum;
		callnum = getDeweyCallNumberFrom999(f999);
		if (callnum != null)
			return callnum;

		String subw = Utils.getSubfieldData(f999, 'w');
		if (subw != null) {
			if (subw.trim().equalsIgnoreCase("SUDOC")) {
				String suba = Utils.getSubfieldData(f999, 'a');
				if (suba != null)
					return GOV_DOC_CALLNO_PFX + suba.trim(); 
			}
		}

		return null;
	}

	/**
	 * if there is an LC call number in the 999, return it.  If Otherwise, return
	 *  null.
	 * N.B.  Government docs are currently lumped in with LC.
	 */
	private String getLCCallNumberFrom999(DataField f999)
	{
		if (ignoreItem(f999) || onlineItem(f999) || ignoreCallNum(f999))
			return null;

		String suba = Utils.getSubfieldData(f999, 'a');
		String subw = Utils.getSubfieldData(f999, 'w');
		if (suba != null && subw != null) {
			subw = subw.trim();
			if ( (subw.equalsIgnoreCase("LC") || subw.equalsIgnoreCase("LCPER")) 
					&& CallNumUtils.isValidLC(suba.trim()))
				return suba.trim();
		}

		// Government docs are currently lumped in with LC call numbers
		if (subw != null && subw.trim().equalsIgnoreCase("SUDOC") && suba != null)
			return GOV_DOC_CALLNO_PFX + suba.trim(); 

		return null;
	}

	/**
	 * if there is a Dewey call number in the 999, return it.  Otherwise, return
	 *  null
	 */
	private String getDeweyCallNumberFrom999(DataField f999)
	{
		if (ignoreItem(f999) || onlineItem(f999) || ignoreCallNum(f999))
			return null;

		String suba = Utils.getSubfieldData(f999, 'a');
		if (suba != null) {
			String subw = Utils.getSubfieldData(f999, 'w');
			if (subw != null) {
				subw = subw.trim();
				if ( (subw.equalsIgnoreCase("DEWEY") || subw.equalsIgnoreCase("DEWEYPER")) 
						&& CallNumUtils.isValidDewey(suba.trim()))
						return addLeadingZeros(suba.trim());
			}
		}

		return null;
	}
	
	/**
	 * adds leading zeros to a dewey call number, when they're missing.
	 * @param deweyCallNum
	 * @return the dewey call number with leading zeros
	 */
	private String addLeadingZeros(String deweyCallNum)
	{
		String result = deweyCallNum;
		String b4Cutter = CallNumUtils.getPortionBeforeCutter(deweyCallNum);

		// TODO: could call Utils.normalizeFloat(b4Cutter.trim(), 3, -1);
		//  but still need to add back part after cutter
		
		String b4dec = null;
		int decIx = b4Cutter.indexOf(".");
		if (decIx >= 0)
			b4dec = deweyCallNum.substring(0, decIx).trim();
		else 
			b4dec = b4Cutter.trim();

		if (b4dec != null) {
			switch (b4dec.length()) 
			{
				case 1: 
					result = "00" + deweyCallNum;
					break;
				case 2:
					result = "0" + deweyCallNum;
			}
		}
		
		return result;
	}

	
// TODO:  this should be read in from a config file
	/**
	 * location codes implying call numbers should be ignored
	 */
	Set<String> ignoreCallNumLocs = new HashSet<String>();
	{
		ignoreCallNumLocs.add("SHELBYTITL");
		ignoreCallNumLocs.add("SHELBYSER");
		ignoreCallNumLocs.add("STORBYTITL");
	}
	/**
	 * return true if call number should be ignored
	 */
	protected boolean ignoreCallNum(DataField f999) {
		String sub = Utils.getSubfieldData(f999, 'l');
		if (sub != null && ignoreCallNumLocs.contains(sub.trim().toUpperCase()))
			return true;

		// subfield k is the "current location" which is only present if it is
		//   different from the "home location" in subfield l (letter L).
		sub = Utils.getSubfieldData(f999, 'k');
		if (sub != null && ignoreCallNumLocs.contains(sub.trim().toUpperCase()))
			return true;
		return false;
	}

// Call Number Methods --------------  End  ---------------- Call Number Methods    
	

// TODO:  this should be read in from a config file
	/**
	 * online location codes that may appear in the 999
	 */
	Set<String> onlineLocs = new HashSet<String>();
	{
		onlineLocs.add("ELECTR-LOC");
		onlineLocs.add("INTERNET");
		onlineLocs.add("ONLINE-TXT");
		onlineLocs.add("RESV-URL");  // Internet Reserved
	}

	
	/**
	 * return true if 999 field has a location code indicating it is online
	 */
	private boolean onlineItem(DataField f999) {
		String sub = Utils.getSubfieldData(f999, 'l');
		if (sub != null && onlineLocs.contains(sub.trim().toUpperCase()))
			return true;

		// subfield k is the "current location" which is only present if it is
		//   different from the "home location" in subfield l (letter L).
		sub = Utils.getSubfieldData(f999, 'k');
		if (sub != null && onlineLocs.contains(sub.trim().toUpperCase()))
			return true;

		return false;
	}
	

// TODO:  this should be read in from a config file	
	/**
	 * a list of locations indicating a 999 field should be ignored, for the 
	 *   purpose of discoverability.
	 */
	Set<String> ignoredLocs = new HashSet<String>();
	{
		ignoredLocs.add("3FL-REF-S"); //meyer 3rd floor reference shadowed
		ignoredLocs.add("BENDER-S"); //temporary shadowed location for the Bender Reading Room
		ignoredLocs.add("CDPSHADOW"); //All items in CDP which are shadowed
		ignoredLocs.add("DISCARD"); //discard shadowed
		ignoredLocs.add("DISCARD-NS"); // obsolete location
		ignoredLocs.add("EAL-TEMP-S"); //East Asia Library Temporary Shadowed
		ignoredLocs.add("FED-DOCS-S"); //Shadowed location for loading Marcive SLS records
		ignoredLocs.add("MAPCASES-S"); //Shadowed location for loading Marcive SLS records
		ignoredLocs.add("MAPFILE-S"); //Shadowed location for loading Marcive SLS records
		ignoredLocs.add("LOCKSS"); // Locks shadowed copy
		ignoredLocs.add("LOST"); //LOST shadowed
		ignoredLocs.add("SEL-NOTIFY");
		ignoredLocs.add("SHADOW"); //Use for all items which are to be shadowed
		ignoredLocs.add("SPECA-S"); //Special Collections-- Shadowed Archives
		ignoredLocs.add("SPECAX-S"); //Special Collections-- Shadowed Archives, Restricted Access
		ignoredLocs.add("SPECB-S"); //Special Collections-- Shadowed Books
		ignoredLocs.add("SPECBX-S"); //Special Collections-- Shadowed Books Restricted Access
		ignoredLocs.add("SPECM-S"); //Special Collections-- Shadowed Manuscripts
		ignoredLocs.add("SPECMED-S"); //Special Collections-- Shadowed Media
		ignoredLocs.add("SPECMEDX-S"); //Special Collections-- Shadowed Media, Restricted Access
		ignoredLocs.add("SPECMX-S"); //Special Collections-- Shadowed Manuscripts, Restricted Acces
		ignoredLocs.add("SSRC-FIC-S"); //Shadowed location for loading Marcive SLS records
		ignoredLocs.add("SSRC-SLS"); //Shadowed location for loading Marcive SLS records
		ignoredLocs.add("STAFSHADOW"); //All staff items which are shadowed
		ignoredLocs.add("SUPERSEDED");
		ignoredLocs.add("TECHSHADOW"); //Technical Services Shadowed
		ignoredLocs.add("WITHDRAWN");
	}
	
	
	/**
	 * return true if 999 field has a location code indicating it should be 
	 *  ignored.
	 */
	protected boolean ignoreItem(DataField f999) {
		String sub = Utils.getSubfieldData(f999, 'l');
		if (sub != null && ignoredLocs.contains(sub.trim().toUpperCase()))
			return true;

		// subfield k is the "current location" which is only present if it is
		//   different from the "home location" in subfield l (letter L).
		sub = Utils.getSubfieldData(f999, 'k');
		if (sub != null && ignoredLocs.contains(sub.trim().toUpperCase()))
			return true;

		return false;
	}
	

// TODO:  this should be read in from a config file
	/**
	 * off campus libraries that may appear in the 999
	 */
	Set<String> offCampusLibs = new HashSet<String>();
	{
		offCampusLibs.add("SAL");
		offCampusLibs.add("SAL3");
		offCampusLibs.add("SAL-NEWARK");
	}
    
	
	/**
	 * return true if 999 field has a library code indicating it is stored off
	 *  campus.
	 */
	private boolean offCampusItem(DataField f999) {
		String subm = Utils.getSubfieldData(f999, 'm');
		if (subm != null && offCampusLibs.contains(subm.trim().toUpperCase()))
			return true;

		return false;
	}
	
	
	/**
	 * return true if the record has an 856 subfield u  for the GSB offsite
	 *  request form (for materials at SAL3)
	 */
	@SuppressWarnings("unchecked")
	private boolean gsbOffsiteItemByRequest(Record record) {
		
        List<VariableField> list856 = record.getVariableFields("856");
        for (VariableField vf : list856) {
        	DataField df = (DataField) vf;
        	List<String> urlList = Utils.getSubfieldStrings(df, 'u');
        	if (urlList.contains("http://www.gsb.stanford.edu/jacksonlibrary/services/sal3_request.html")
        		|| urlList.contains("https://www.gsb.stanford.edu/jacksonlibrary/services/sal3_request.html"))
        		return true;
        }
		
		return false;
	}
	

// Utility Methods ---------------- Begin ---------------------- Utility Methods    
    
    /**
     * @param DataField with ind2 containing # non-filing chars, or has value ' '
     * @param skipSubFldc true if subfield c contents should be skipped
     * @return StringBuffer of the contents of the subfields - with a trailing 
     *  space
     */
 	@SuppressWarnings("unchecked")
	private StringBuffer getAlphaSubfldsAsSortStr(DataField df, boolean skipSubFldc)
    {
    	StringBuffer result = new StringBuffer();
       	int nonFilingInt = getInd2AsInt(df);
    	boolean firstSubfld = true;
    	
    	List<Subfield> subList = df.getSubfields();
		for (Subfield sub : subList) {
			char subcode = sub.getCode();
			if (Character.isLetter(subcode) && (!skipSubFldc || subcode != 'c'))
			{
				String data = sub.getData();
				if (firstSubfld) {
					if (nonFilingInt < data.length() -1)
						data = data.substring(nonFilingInt);
					firstSubfld = false;
				}
				// eliminate ascii punctuation marks from sorting as well
				result.append(data.replaceAll("\\p{Punct}*", "").trim() + ' ');
			}
		}
    	return result;
    }
 	
    /**
     * @param df a DataField
     * @return the integer (0-9, 0 if blank) in the 2nd indicator
     */
    private int getInd2AsInt(DataField df) {
    	char int2char = df.getIndicator2();
       	int result = 0;
       	if (Character.isDigit(int2char))
       		result = Integer.valueOf(String.valueOf(int2char));
       	return result;
    }
    
    /**
     * Removes trailing periods or commas at the ends of the value strings 
     *  indicated by the fieldSpec argument
     * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
     * @return Set of strings containing values without trailing commas or periods
     */
    public Set<String> removeTrailingPunct(final Record record, final String fieldSpec) 
    {
    	Set<String> resultSet = new HashSet<String>();
    	for (String val : getFieldList(record, fieldSpec)) {
    		if (val.endsWith(",") || val.endsWith(".") || val.endsWith("/")&& val.length() > 1)
    			resultSet.add(val.substring(0, val.length() - 1).trim());
    		else
    			resultSet.add(val.trim());
    	}

    	return resultSet;
    }
        
    
    /**
    * Removes trailing characters indicated in regular expression, PLUS
     *  trailing period if it is preceded by its regular expression.
     *
     * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
     * @param trailingCharsRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately 
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED. 
     *  Note that the regular expression will NOT have the period or '$' at 
     *  the end. 
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately 
     *   precede the period for it to be removed.) 
     * 
     * @return Set of strings containing values without trailing characters
     */
    public Set<String> removeTrailingPunct(final Record record, final String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx) 
    {
    	Set<String> resultSet = new HashSet<String>();
    	for (String val : getFieldList(record, fieldSpec)) {
    		String result = Utils.removeAllTrailingCharAndPeriod(val, "(" + charsToReplaceRegEx + ")+", charsB4periodRegEx);
   			resultSet.add(result);
    	}

    	return resultSet;
    }

// Vernacular Methods --------------- Begin ----------------- Vernacular Methods    
	
	/**
	 * Get the vernacular (880) fields which corresponds to the marc field
	 *  in the 880 subfield 6 linkage 
     * @param marc field - which marc field to seek in 880 fields (via linkages)
	 */
	@SuppressWarnings("unchecked")
	protected final Set<VariableField> getVernacularFields(final Record record, String marcField) 
	{
        if (marcField.length() != 3)
            System.err.println("marc field tag must be three characters: " + marcField);
        
        Set<VariableField> resultSet = new LinkedHashSet<VariableField>();
        
        List<VariableField> list880s = record.getVariableFields("880");
        if (list880s == null || list880s.size() == 0)
        	return resultSet;
		
        // we know which 880s we're looking for by matching the marc field and 
        //  subfield 6 (linkage info) in the 880
        for (VariableField vf : list880s) {
         	DataField df880 = (DataField) vf;
        	String linkage = Utils.getSubfieldData(df880, '6');
       		int dashIx = linkage.indexOf('-');
        	if (dashIx == 3 && marcField.equals(linkage.substring(0, dashIx)) ) {
        		resultSet.add(df880);
          	}
        }        
        return (resultSet);
	}
	

	/**
	 * Get the vernacular (880) field based which corresponds to the fieldSpec
	 *  in the subfield 6 linkage 
     * @param fieldSpec - which marc fields / subfields need to be sought in 
     *  880 fields (via linkages)
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getVernacular(final Record record, String fieldSpec) 
	{
        Set<String> result = new LinkedHashSet<String>();
		// find the right 880 field
		//  then do the regular thing
        
        List<VariableField> list880s = record.getVariableFields("880");
        if (list880s == null || list880s.size() == 0)
        	return result;
		
        // is this a right to left language? - determine at record level
        // TODO: it would be more appropriate to autodetect script of 880 field
        VariableField vf8 = record.getVariableField("008");
        ControlField f008 = (ControlField) vf8;
        String langcode = f008.getData().substring(35, 38);
        boolean rightToLeft = Utils.isRightToLeftLanguage(langcode);
        
        // we know which 880s we're looking for by the fieldSpec and 
        //  subfield 6 (linkage info) in the 880
        String[] linkPieces = fieldSpec.split(":");
        
        for (int i = 0; i < linkPieces.length; i++)
        {
        	String linkTOspec = linkPieces[i];
            String linkTOfield = linkTOspec.substring(0, 3);
        	Set<VariableField> matching880s = getVernacularFields(record, linkTOfield);
            
            // look for 880s that link to the right field
            for (VariableField vf : matching880s) {
             	DataField df880 = (DataField) vf;
                String subfldStr = linkTOspec.substring(3);
                result.addAll(getSubfieldsAsSet(df880, subfldStr, rightToLeft));
            }        
        }
        return (result);
	}
	

	/**
	 * Get the vernacular (880) field based which corresponds to the fieldSpec
	 *  in the subfield 6 linkage, handling multiple occurrences as indicated 
     * @param fieldSpec - which marc fields / subfields need to be sought in 
     *  880 fields (via linkages)
     * @param multOccurs - "first", "join" or "all" indicating how to handle
     *  multiple occurrences of field values
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getVernacular(final Record record, String fieldSpec, String multOccurs) 
	{
        Set<String> result = getVernacular(record, fieldSpec);
        
        if (multOccurs.equals("first")) {
        	Set<String> first = new HashSet<String>();
        	for (String r : result) {
        		first.add(r);
        		return first;
        	}
        } else if (multOccurs.equals("join")) {
        	StringBuffer resultBuf = new StringBuffer(); 
        	for (String r : result) {
        		if (resultBuf.length() > 0)
        			resultBuf.append(' ');
        		resultBuf.append(r);
        	}
        	Set<String> resultAsSet = new HashSet<String>();
        	resultAsSet.add(resultBuf.toString());
        	return resultAsSet;
        }
        // "all" is default
        
        return result;
	}

	
	
	/**
	 *  
     * For each occurrence of a marc field in the fieldSpec list, get the 
     * matching vernacular (880) field (per subfield 6) and extract the
     * contents of all subfields except the ones specified, concatenate the 
     * subfield contents with a space separator and add the string to the result
     * set.
     * @param record - the marc record
     * @param fieldSpec - the marc fields (e.g. 600:655) for which we will grab
     *  the corresponding 880 field containing subfields other than the ones
     *  indicated.  
     * @return a set of strings, where each string is the concatenated values
     *  of all the alphabetic subfields in the 880s except those specified.
     */
	public final Set<String> getVernAllAlphaExcept(final Record record, String fieldSpec)
	{
        Set<String> resultSet = new LinkedHashSet<String>();
        
		String[] fldTags = fieldSpec.split(":");
        for (int i = 0; i < fldTags.length; i++)
        {
        	String fldTag = fldTags[i].substring(0, 3);
            if (fldTag.length() < 3 || Integer.parseInt(fldTag) < 10 )
            {
                System.err.println("Invalid marc field specified for getAllAlphaExcept: " + fldTag);
                continue;
            }
    		
            String tabooSubfldTags = fldTags[i].substring(3);

            Set<VariableField> vernFlds = getVernacularFields(record, fldTag);
    		
            for (VariableField vf : vernFlds) 
            {
                StringBuffer buffer = new StringBuffer(500);
                DataField df = (DataField) vf;
        		if (df != null) 
        		{
                    List<Subfield> subfields = df.getSubfields();
                    for (Subfield sf: subfields) 
                    {
                    	if (Character.isLetter(sf.getCode()) &&
                        		tabooSubfldTags.indexOf(sf.getCode()) == -1)
                        {
                            if (buffer.length() > 0)  
                            	buffer.append(' ' + sf.getData());
                            else
                            	buffer.append(sf.getData());
                        }
                    }
                    if (buffer.length() > 0) 
                    	resultSet.add(buffer.toString());
            	}
            }
        }
        
		return resultSet;
	}

	
	/**
	 * Get the vernacular (880) field based which corresponds to the fieldSpec
	 *  in the subfield 6 linkage, handling trailing punctuation as incidated 
     * @param fieldSpec - which marc fields / subfields need to be sought in 
     *  880 fields (via linkages)
     * @param trailingCharsRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately 
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED. 
     *  Note that the regular expression will NOT have the period or '$' at 
     *  the end. 
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately 
     *   precede the period for it to be removed.) 
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> vernRemoveTrailingPunc(final Record record, String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx) 
	{
        Set<String> origVals = getVernacular(record, fieldSpec);
        Set<String> result = new LinkedHashSet<String>();
        
    	for (String val : origVals) {
    		result.add(Utils.removeAllTrailingCharAndPeriod(val, "(" + charsToReplaceRegEx + ")+", charsB4periodRegEx));
    	}
        return result;
	}
	
// Vernacular Methods ---------------  End  ----------------- Vernacular Methods    

    /**
     * Get the specified subfields from the MARC data field, returned as
     *  a string
     * @param df - DataField from which to get the subfields
     * @param subfldsStr - the string containing the desired subfields
     * @param RTL - true if this is a right to left language.  In this case, 
     *  each subfield is prepended due to LTR and MARC end-of-subfield punctuation
     *  is moved from the last character to the first.
     * @returns a set of strings of desired subfields concatenated with space separator
     */
    @SuppressWarnings("unchecked")
	protected static Set<String> getSubfieldsAsSet(DataField df, String subfldsStr, boolean RTL) 
    {
    	Set<String> resultSet = new LinkedHashSet<String>();

    	if (subfldsStr.length() > 1) {
            // concatenate desired subfields with space separator
            StringBuffer buffer = new StringBuffer();
            List<Subfield> subFlds = df.getSubfields();
            for (Subfield sf : subFlds) {
            	if (subfldsStr.contains(String.valueOf(sf.getCode()))) {
// TODO:  clean this up, if this works, or find a way to test it            		
//            		if (RTL) { // right to left language, but this is LTR field+
//	                    if (buffer.length() > 0)
//	                        buffer.insert(0, ' ');
//	                    buffer.insert(0, sf.getData().trim());
//            		} else { // left to right language
	                    if (buffer.length() > 0)
	                        buffer.append(' ');
	                    buffer.append(sf.getData().trim());
//            		}
            	}
            } 
            resultSet.add(buffer.toString());
        } else {
        	// for single subfield, each occurrence is separate field in lucene doc
        	List<Subfield> subFlds = df.getSubfields(subfldsStr.charAt(0));
        	for (Subfield sf : subFlds) {
        		resultSet.add(sf.getData().trim());
        	}
        } 
    	return resultSet;
    }

    
    /**
     * Get the specified subfields from the MARC data field, returned as
     *  a string
     * @param df - DataField from which to get the subfields
     * @param subfldsStr - the string containing the desired subfields
     * @param beginIx - the beginning index of the substring of the subfield value
     * @param endIx - the endind index of the substring of the subfield value
     * @param RTL - true if this is a right to left language.  In this case, 
     *  each subfield is prepended due to LTR and MARC end-of-subfield punctuation
     *  is moved from the last character to the first.
     * @returns a set of strings of desired subfields concatenated with space separator
     */
    @SuppressWarnings("unchecked")
	protected static Set<String> getSubfieldsAsSet(DataField df, String subfldsStr, int beginIx, int endIx, boolean RTL) 
    {
    	Set<String> resultSet = new LinkedHashSet<String>();
        if (subfldsStr.length() > 1) {
            // concatenate desired subfields with space separator
            StringBuffer buffer = new StringBuffer();
            List<Subfield> subFlds = df.getSubfields();
            for (Subfield sf : subFlds) {
            	if (subfldsStr.contains(String.valueOf(sf.getCode()))) {
            		if (sf.getData().length() >= endIx) {
// TODO:  clean this up, if this works, or find a way to test it            		
//            			if (RTL) { // right to left language
//                            if (buffer.length() > 0) 
//                                buffer.insert(0, ' ');
//                            buffer.insert(0, sf.getData().trim().substring(beginIx, endIx));
//            			} else { // left to right language
                            if (buffer.length() > 0) 
                                buffer.append(' ');
                            buffer.append(sf.getData().trim().substring(beginIx, endIx));
//            			}
            		}
                }
            }
            resultSet.add(buffer.toString());
        } else {
        	// for single subfield, each occurrence is separate field in lucene doc
        	List<Subfield> subFlds = df.getSubfields(subfldsStr.charAt(0));
        	for (Subfield sf : subFlds) {
        		if (sf.getData().length() >= endIx) 
            		resultSet.add(sf.getData().trim().substring(beginIx, endIx));
        	}
        }
        return resultSet;
    }
    
    
// Utility Methods ----------------- End ----------------------- Utility Methods    
    
	
}
