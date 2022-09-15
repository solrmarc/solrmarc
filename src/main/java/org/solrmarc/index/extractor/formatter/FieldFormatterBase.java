package org.solrmarc.index.extractor.formatter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.solrmarc.tools.DataUtil;

import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public class FieldFormatterBase implements FieldFormatter
{
    String indicatorFmt = null;
    Map<String, String> sfCodeMap = null;
    String fieldFormat = null;
    String separator = null;
    List<AbstractMultiValueMapping> maps = null;
//    boolean unique = false;
    eJoinVal joinVal = eJoinVal.SEPARATE;
    int substringStart = -1;
    int substringEnd = -1;
    EnumSet<eCleanVal> cleanVal = EnumSet.noneOf(eCleanVal.class);

    String fieldTagFmt = null;

    public FieldFormatterBase(boolean clean)
    {
        if (clean)
        {
            cleanVal.add(eCleanVal.CLEAN_EACH);
            cleanVal.add(eCleanVal.CLEAN_END);
        }
    }

    public FieldFormatterBase(EnumSet<eCleanVal> cleanVal)
    {
        this.cleanVal = cleanVal;
    }

    public FieldFormatterBase(FieldFormatterBase toClone)
    {
        this.indicatorFmt = toClone.indicatorFmt;
        this.sfCodeMap = toClone.sfCodeMap;
        this.separator = toClone.separator;
        this.joinVal = toClone.joinVal;
        this.substringStart = toClone.substringStart;
        this.substringEnd = toClone.substringEnd;
        this.cleanVal = toClone.cleanVal;
        if (toClone.maps != null)
        {
            if (this.maps == null) this.maps = new LinkedList<>();
            for (AbstractMultiValueMapping map : toClone.maps)
            {
                if (map instanceof ExternalMethod && !((ExternalMethod)map).isThreadSafe())
                {
                    this.maps.add((AbstractMultiValueMapping) ((ExternalMethod)map).makeThreadSafeCopy());
                }
                else
                {
                    this.maps.add(map);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see playground.org.solrmarc.index.fieldmatch.FieldFormatter#getFieldTagFmt()
     */
    @Override
    public String getFieldTagFmt()
    {
        return fieldTagFmt;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#setFieldTagFmt(java.
     * lang.String)
     */
    @Override
    public FieldFormatter setFieldTagFmt(String fieldTagFmt)
    {
        this.fieldTagFmt = fieldTagFmt;
        return(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#getIndicatorFmt()
     */
    @Override
    public String getIndicatorFmt()
    {
        return indicatorFmt;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#setIndicatorFmt(java.
     * lang.String)
     */
    @Override
    public FieldFormatter setIndicatorFmt(String indicatorFmt)
    {
        this.indicatorFmt = indicatorFmt;
        return(this);
    }

//    /*
//     * (non-Javadoc)
//     *
//     * @see playground.org.solrmarc.index.fieldmatch.FieldFormatter#getSfCodeFmt()
//     */
//    @Override
//    public String getSfCodeFmt(char sfCode)
//    {
//        if (sfCodeMap != null && sfCodeMap.containsKey(sfCode))
//        {
//            return(sfCodeMap.get(sfCode));
//        }
//        return(null);
//    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#setSfCodeFmt(java.
     * lang.String)
     */
    @Override
    public FieldFormatter setSfCodeFmt(String[] mapParts)
    {
        if (sfCodeMap == null)  sfCodeMap = new LinkedHashMap<String, String>();
        for (String part : mapParts)
        {
            String[] pieces = part.split("=>", 2);
            if (pieces.length == 2 && pieces[1].contains("=>"))
            {
                // something here related to per-subfield map??
            }
            if (pieces.length == 2 && pieces[0].length() == 1)
            {
                sfCodeMap.put(pieces[0], pieces[1]);
            }
            else if (pieces.length == 2 && pieces[0].matches("\\[[-a-z0-9]*\\]"))
            {
                for (char c = 'a'; c <= 'z' ; c++ )
                {
                    String cstr = Character.toString(c);
                    if (cstr.matches(pieces[0]))
                    {
                        sfCodeMap.put(cstr, pieces[1]);
                    }
                }
                for (char c = '0'; c <= '9' ; c++ )
                {
                    String cstr = Character.toString(c);
                    if (cstr.matches(pieces[0]))
                    {
                        sfCodeMap.put(cstr, pieces[1]);
                    }
                }
            }
            else if (pieces.length == 2 && pieces[0].contains("tag"))
            {
                this.setFieldTagFmt(pieces[1]);
            }
            else if (pieces.length == 2 && pieces[0].contains("ind"))
            {
                this.setIndicatorFmt(pieces[1]);
            }
            else if (fieldFormat == null && !part.equals("format"))
            {
                fieldFormat = part;
            }
        }
        return(this);
    }

    @Override
    public String getFieldFormat()
    {
        return fieldFormat;
    }

    @Override
    public boolean hasFieldFormat()
    {
        return (fieldFormat != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see playground.org.solrmarc.index.fieldmatch.FieldFormatter#getSeparator()
     */
    @Override
    public String getSeparator()
    {
        return separator;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#setSeparator(java.
     * lang.String)
     */
    @Override
    public FieldFormatter setSeparator(String separator)
    {
        this.separator = separator;
        return(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see playground.org.solrmarc.index.fieldmatch.FieldFormatter#getCleanVal()
     */
    @Override
    public EnumSet<eCleanVal> getCleanVal()
    {
        return cleanVal;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#setCleanVal(java.util
     * .EnumSet)
     */
    @Override
    public FieldFormatter setCleanVal(EnumSet<eCleanVal> cleanVal)
    {
        this.cleanVal.addAll(cleanVal);
        return(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see playground.org.solrmarc.index.fieldmatch.FieldFormatter#addCleanVal(
     * playground.org.solrmarc.index.fieldmatch.FieldFormatterBase.eCleanVal)
     */
    @Override
    public FieldFormatter addCleanVal(eCleanVal cleanVal)
    {
        this.cleanVal.add(cleanVal);
        return(this);
    }

    @Override
    public eJoinVal getJoinVal()
    {
        return joinVal;
    }

    @Override
    public FieldFormatter setJoinVal(eJoinVal joinVal)
    {
        this.joinVal = joinVal;
        return(this);
    }

    @Override
    public FieldFormatter setSubstring(int offset, int endOffset)
    {
        this.substringStart = offset;
        this.substringEnd = endOffset;
        return(this);
    }

    @Override
    public FieldFormatter addMap(AbstractMultiValueMapping valueMapping)
    {
        if (maps == null) maps = new LinkedList<>();
        maps.add(valueMapping);
        return(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see playground.org.solrmarc.index.fieldmatch.FieldFormatter#start(java.lang.
     * StringBuilder)
     */
    @Override
    public StringBuilder start()
    {
        return new StringBuilder();
//        return (makeResult());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#addTag(org.marc4j.
     * marc.VariableField, java.lang.StringBuilder)
     */
    @Override
    public void addTag(StringBuilder sb, VariableField df)
    {
        if (fieldFormat != null && fieldFormat.contains("%tag"))
        {
            sbReplace(sb, "%tag", df.getTag());
        }
        else if (fieldTagFmt != null && fieldTagFmt.length() > 0)
        {
            sb.append(fieldTagFmt.contains("%tag") ? fieldTagFmt.replace("%tag", df.getTag()) : fieldTagFmt);
        }
    }

    private void sbReplace(StringBuilder sb, String pattern, String value)
    {
        int indexOf = sb.indexOf(pattern);
        if (indexOf != -1)
        {
            sb.replace(indexOf, indexOf + pattern.length(), value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#addIndicators(org.
     * marc4j.marc.VariableField, java.lang.StringBuilder)
     */
    @Override
    public void addIndicators(StringBuilder sb, VariableField df)
    {
        if (fieldFormat != null && (fieldFormat.contains("%1") || fieldFormat.contains("%2")))
        {
            sbReplace(sb, "%1", ""+((DataField) df).getIndicator1());
            sbReplace(sb, "%2", ""+((DataField) df).getIndicator2());
        }
        else if (indicatorFmt != null && indicatorFmt.length() > 0 && df instanceof DataField)
        {
            String result = indicatorFmt.replace("%1", "" + ((DataField) df).getIndicator1()).replace("%2",
                    "" + ((DataField) df).getIndicator2());
            sb.append(result);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#addCode(java.lang.
     * String, java.lang.StringBuilder)
     */
    @Override
    public void addCode(StringBuilder sb, String codeStr)
    {
//        if (sfCodeMap != null)
//        {
//            String pattern = null;
//            if (sfCodeMap.containsKey(codeStr))
//            {
//                pattern = sfCodeMap.get(codeStr);
//            }
//            else if (sfCodeMap.containsKey("*"))
//            {
//                pattern = sfCodeMap.get("*");
//            }
//            if (pattern != null && pattern.length() != 0)
//            {
//                sb.append(pattern.replaceAll("%sf", codeStr));
//            }
//        }
    }

    @Override
    public Collection<String> handleMapping(Collection<String> cleaned) throws Exception
    {
        if (maps == null) return (cleaned);
        Collection<String> mapped = cleaned;
        for (AbstractMultiValueMapping map : maps)
        {
            mapped = map.map(mapped);
        }
        return(mapped);
    }

    @Override
    public String handleSubFieldFormat(String sfCode, VariableField vf, String mappedDataVal)
    {
        if (sfCodeMap == null || (!sfCodeMap.containsKey(sfCode) && !sfCodeMap.containsKey("*"))) 
            return (mappedDataVal);
        String pattern = null;
        if (sfCodeMap.containsKey(sfCode))
        {
            pattern = sfCodeMap.get(sfCode);
        }
        else if (sfCodeMap.containsKey("*"))
        {
            pattern = sfCodeMap.get("*");
        }
        String ind1 = (vf instanceof DataField) ? ""+ ((DataField)vf).getIndicator1() : " ";
        String ind2 = (vf instanceof DataField) ? ""+ ((DataField)vf).getIndicator2() : " ";
        if (pattern.contains("$"+sfCode))
        {
            pattern = pattern .replace("$"+sfCode, "%d");
        }
        pattern = pattern.replace("%tag", vf.getTag()).replace("%1", ind1).replace("%2", ind2).replace("%sf", sfCode).replace("%d", mappedDataVal);
        return(pattern);
    }

    private final String getSubstring(final String data)
    {
        try
        {
            if (substringStart != -1)
            {
                if (substringEnd != -1) return data.substring(substringStart, substringEnd);
                else                    return data.substring(substringStart);
            }
            else
            {
                return(data);
            }
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            return("");
        }
    }

    public String cleanData(VariableField vf, boolean isSubfieldA, String data)
    {
        final EnumSet<eCleanVal> cleanVal = getCleanVal();
        int numToDel = 0;

        String trimmed = data;
        if ((cleanVal.contains(eCleanVal.STRIP_INDICATOR_1) || cleanVal.contains(eCleanVal.STRIP_INDICATOR_2) || cleanVal.contains(eCleanVal.STRIP_INDICATOR)) 
                && isSubfieldA && vf instanceof DataField)
        {
            DataField df = (DataField) vf;
            char indVal = getIndicatorValueToStrip(df, cleanVal);
            numToDel = (indVal >= '0' && indVal <= '9') ? indVal - '0' : 0;
            if (numToDel > trimmed.length()) 
                numToDel = trimmed.length();
            if (numToDel > 0) trimmed = trimmed.substring(numToDel);
        }
        trimmed = cleanVal.contains(eCleanVal.UNTRIMMED) ? getSubstring(trimmed) : getSubstring(trimmed).trim();

        String str = DataUtil.cleanByVal(trimmed, cleanVal);
        return(str);
//        
//        String str = (cleanVal.contains(eCleanVal.CLEAN_EACH)) ? DataUtil.cleanData(trimmed) : trimmed;
//        if (!cleanVal.contains(eCleanVal.STRIP_ACCCENTS) && !cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT)
//                && !cleanVal.contains(eCleanVal.TO_LOWER) && !cleanVal.contains(eCleanVal.TO_UPPER)
//                && !cleanVal.contains(eCleanVal.TO_TITLECASE) && !cleanVal.contains(eCleanVal.STRIP_INDICATOR_1) 
//                && !cleanVal.contains(eCleanVal.STRIP_INDICATOR_2) && !cleanVal.contains(eCleanVal.STRIP_INDICATOR))
//        {
//            return (str);
//        }
//        // Do more extensive cleaning of data.
//        if (cleanVal.contains(eCleanVal.STRIP_ACCCENTS))
//        {
//            str = DataUtil.stripAccents(str);
//        }
//        if (cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT)) 
//        {
//            str = DataUtil.stripAllPunct(str);
//        }
//        if (!cleanVal.contains(eCleanVal.UNTRIMMED))  str = str.trim();
//
//        if (cleanVal.contains(eCleanVal.TO_LOWER))
//        {
//            str = str.toLowerCase();
//        }
//        else if (cleanVal.contains(eCleanVal.TO_UPPER))
//        {
//            str = str.toUpperCase();
//        }
//        else if (cleanVal.contains(eCleanVal.TO_TITLECASE))
//        {
//            str = DataUtil.toTitleCase(str);
//        }
//        return str;
    }

    private char getIndicatorValueToStrip(DataField df, EnumSet<eCleanVal> cleanVal)
    {
        final String ind1Fields = "130:630:730:740";
        final String ind2Fields = "222:240:242:243:245:440:830";
        if ( cleanVal.contains(eCleanVal.STRIP_INDICATOR) || ((cleanVal.contains(eCleanVal.STRIP_INDICATOR_1) && cleanVal.contains(eCleanVal.STRIP_INDICATOR_2))))
        {
            if (ind1Fields.contains(df.getTag()) )
                return(df.getIndicator1());
            else if (ind2Fields.contains(df.getTag()))
                return(df.getIndicator2());
            else
                return(0);
        }
        else if (cleanVal.contains(eCleanVal.STRIP_INDICATOR_1))   
        {
            return(df.getIndicator1());
        }
        else if (cleanVal.contains(eCleanVal.STRIP_INDICATOR_2))   
        {
            return(df.getIndicator2());
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#addVal(java.lang.
     * String, java.lang.StringBuilder)
     */
    @Override
    public void addVal(StringBuilder sb, String sfcode, String data)
    {
        if (fieldFormat != null && sfcode != null)
        {
            sbReplace(sb, "$"+sfcode, data);
        }
        else
        {
            sb.append(data);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#addSeparator(int,
     * java.lang.StringBuilder)
     */
    @Override
    public void addSeparator(StringBuilder sb, int cnt)
    {
        if (fieldFormat != null)
        {
            // if formatting field ignore "separate"
        }
        else if (joinVal == eJoinVal.JOIN && getSeparator() != null)
        {
            if (cnt != 0) sb.append(getSeparator());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#addAfterSubfield(java
     * .util.Collection, java.lang.StringBuilder)
     */
    @Override
    public void addAfterSubfield(StringBuilder sb, Collection<String> result)
    {
        if (fieldFormat != null)
        {
            // if formatting field ignore "separate"
        }
        else if (joinVal == eJoinVal.SEPARATE)
        {
            if (sb.length() == 0) return;
            final String field = (this.getCleanVal().contains(eCleanVal.CLEAN_END)) ? DataUtil.cleanData(sb.toString()) : sb.toString();
            if (field.length() > 0) result.add(field);
            sb.setLength(0);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.org.solrmarc.index.fieldmatch.FieldFormatter#addAfterField(java.
     * util.Collection, java.lang.StringBuilder)
     */
    @Override
    public void addAfterField(StringBuilder sb, Collection<String> result)
    {
        if (fieldFormat != null)
        {
            String fieldVal = sb.toString().replaceAll("\\$[a-z0-9]", "");
            if (fieldVal.length() == 0) return;
            final String field = (this.getCleanVal().contains(eCleanVal.CLEAN_END)) ? DataUtil.cleanData(fieldVal) : fieldVal;
            if (field.length() > 0) result.add(field);
            sb.setLength(0);
        }
        else if (joinVal == eJoinVal.JOIN)
        {
            if (sb.length() == 0) return;
            final String field = (this.getCleanVal().contains(eCleanVal.CLEAN_END)) ? DataUtil.cleanData(sb.toString()) : sb.toString();
            if (field.length() > 0) result.add(field);
            sb.setLength(0);
        }
    }

//    @Override
//    public Collection<String> makeResult()
//    {
//        Collection<String> result;
//        if (unique)
//        {
//            result = new LinkedHashSet<String>();
//        }
//        else
//        {
//            result = new ArrayList<String>();
//        }
//        return result;
//    }

    @Override
    public Collection<String> prepData(VariableField vf, boolean isSubfieldA, String data) throws Exception
    {
        final String cleaned = cleanData(vf, isSubfieldA, data);
        @SuppressWarnings("unchecked")
        final List<String> cleanedDataAsList = (cleaned == null || cleaned.length() == 0) ? Collections.EMPTY_LIST : Collections.singletonList(cleaned);
        Collection<String> result = handleMapping(cleanedDataAsList);
        return (result);
    }

    @Override
    public boolean isThreadSafe()
    {
        if (maps == null) return(true);
        for (AbstractMultiValueMapping map : maps)
        {
            if (map instanceof ExternalMethod && !((ExternalMethod)map).isThreadSafe())
            {
                return(false);
            }
        }
        return(true);
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new FieldFormatterBase(this);
    }


}
