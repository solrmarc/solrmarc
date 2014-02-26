package org.solrmarc.callnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A representation of call numbers which are coded as Dewey at UChicago. Primarily actual,
 * valid Dewey numbers, but also includes accession numbers inherited from Crerar Library.
 *
 * <code>parse</code> first tries to treat the call number as authentic Dewey, but if that
 * fails will try to treat it as an accession number. Examples of the accession numbers are:
 * <ul>
 * <li>M1 112</li>
 * <li>M1a 64 no.6</li>
 * <li>T1 79 1972 Oct23</li>
 * <li>T3 628 no.75-021</li>
 * <li>T1584 cong.86 sess.1-2 1959-60</li>
 * <li>T1751</li>
 * <li>TE1f 841</li>
 * </ul>
 *
 * @author Tod Olson, University of Chicago
 *
 */
public class UChicagoDeweyCallNumber extends DeweyCallNumber {

    /**
     * Regexp to match old Crerar accession numbers.
     */
    final static private String crerarAccessionRegexp = "([MT][A-F]?\\d+[a-f]?) ?(\\d*) ?(.*)";

    private boolean crerarAccession = false;
    private String crerarAccessionSeries = null;
    private String crerarAccessionNumber = null;

    public UChicagoDeweyCallNumber() {
        this.init();
    }

    public UChicagoDeweyCallNumber(String call) {
        parse(call);
        // TODO Auto-generated constructor stub
    }

    public void init() {
        super.init();
        this.crerarAccession = false;
        this.crerarAccessionSeries = null;
        this.crerarAccessionSeries = null;
    }

    /**
     * parses call number first as a regular Dewey call number,
     * but if not valid treats it as a Crerar accession numbers.
     *
     * @param call number to parse
     */
    public void parse(String call) {
        this.init();
        super.parse(call);
        // if not valid Dewey, try as accession number
        if (!this.valid && call != null) {
            Pattern pat = Pattern.compile(crerarAccessionRegexp);
            Matcher match = pat.matcher(call);
            if (match.matches()) {
                this.classification = call;
                this.crerarAccession = true;
                this.crerarAccessionSeries = match.group(1);
                this.crerarAccessionNumber = match.group(2);
                if (match.group(2).length() == 0)
                    this.crerarAccessionNumber = null;
                else
                    this.crerarAccessionNumber = match.group(2);
                if (match.group(3).length() == 0)
                    this.suffix = null;
                else
                    this.suffix = match.group(3);
            }
        }
    }

    public boolean isAccession() {
        return this.crerarAccession;
    }
    public String getAccessionSeries() {
        return this.crerarAccessionSeries;
    }
    public String getAccessionNumber() {
        return this.crerarAccessionNumber;
    }

    public String getShelfKey() {
        // First try as dewey
        if (this.isValid()) {
            return super.getShelfKey();
        }
        // then and accession number
        if (this.isAccession()) {
            StringBuilder keyBuf = new StringBuilder();
            // Allow five spaces for class portion
            keyBuf.append("     ");
            keyBuf.replace(0, this.crerarAccessionSeries.length(), this.crerarAccessionSeries);
            if (this.crerarAccessionNumber != null) {
                keyBuf.append(Utils.normalizeFloat(this.crerarAccessionNumber, 4, 0));
            }
            if (this.suffix != null) {
                keyBuf.append(' ');
                //TODO: generic normalize utility method
                keyBuf.append(suffix);
            }
            return keyBuf.toString();
        }
        // then punt
        //TODO: generic normalize utility method
        return this.raw;
    }
}
