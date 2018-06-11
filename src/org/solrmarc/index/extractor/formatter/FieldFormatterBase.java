package org.solrmarc.index.extractor.formatter;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;
import org.solrmarc.tools.DataUtil;
import org.solrmarc.tools.Utils;

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
     * @see playground.solrmarc.index.fieldmatch.FieldFormatter#getFieldTagFmt()
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#setFieldTagFmt(java.
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#getIndicatorFmt()
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#setIndicatorFmt(java.
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
//     * @see playground.solrmarc.index.fieldmatch.FieldFormatter#getSfCodeFmt()
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#setSfCodeFmt(java.
     * lang.String)
     */
    @Override
    public FieldFormatter setSfCodeFmt(String[] mapParts)
    {
        if (sfCodeMap == null)  sfCodeMap = new LinkedHashMap<String, String>();
        for (String part : mapParts)
        {
            String[] pieces = part.split("=>", 2);
            if (pieces.length == 2 && pieces[0].length() == 1)
            {
                sfCodeMap.put(pieces[0], pieces[1]);
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
     * @see playground.solrmarc.index.fieldmatch.FieldFormatter#getSeparator()
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#setSeparator(java.
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
     * @see playground.solrmarc.index.fieldmatch.FieldFormatter#getCleanVal()
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#setCleanVal(java.util
     * .EnumSet)
     */
    @Override
    public FieldFormatter setCleanVal(EnumSet<eCleanVal> cleanVal)
    {
        this.cleanVal = cleanVal;
        return(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see playground.solrmarc.index.fieldmatch.FieldFormatter#addCleanVal(
     * playground.solrmarc.index.fieldmatch.FieldFormatterBase.eCleanVal)
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
     * @see playground.solrmarc.index.fieldmatch.FieldFormatter#start(java.lang.
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addTag(org.marc4j.
     * marc.VariableField, java.lang.StringBuilder)
     */
    @Override
    public void addTag(StringBuilder sb, VariableField df)
    {
        if (fieldFormat != null && fieldFormat.contains("%tag"))
        {
            sbReplace(sb, "%tag", df.getTag());
        }
        else if (fieldTagFmt != null)
        {
            sb.append(fieldTagFmt.contains("%tag") ? fieldTagFmt.replaceAll("%tag", df.getTag()) : df.getTag());
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addIndicators(org.
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
        else if (indicatorFmt != null && df instanceof DataField)
        {
            String result = indicatorFmt.replaceAll("%1", "" + ((DataField) df).getIndicator1()).replaceAll("%2",
                    "" + ((DataField) df).getIndicator1());
            sb.append(result);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addCode(java.lang.
     * String, java.lang.StringBuilder)
     */
    @Override
    public void addCode(StringBuilder sb, String codeStr)
    {
//        if (sfCodeFmt != null)
//        {
//            buffer.append(sfCodeFmt.replaceAll("%sf", codeStr));
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
    public String handleSubFieldFormat(String sfCode, String mappedDataVal)
    {
        if (sfCodeMap == null || !sfCodeMap.containsKey(sfCode)) return (mappedDataVal);
        String value = sfCodeMap.get(sfCode);
        value = value.replace("$"+sfCode, mappedDataVal);
        return(value);
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

    private static Pattern ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static Pattern PUNCT_OR_SPACE = Pattern.compile("[ \\p{Punct}]+", Pattern.UNICODE_CHARACTER_CLASS);

    public String cleanData(VariableField vf, boolean isSubfieldA, String data)
    {
        final EnumSet<eCleanVal> cleanVal = getCleanVal();
        int numToDel = 0;

        String trimmed = data;
        if (cleanVal.contains(eCleanVal.STRIP_INDICATOR_2) && isSubfieldA && vf instanceof DataField)
        {
            DataField df = (DataField) vf;
            char ind2Val = df.getIndicator2();
            numToDel = (ind2Val >= '0' && ind2Val <= '9') ? ind2Val - '0' : 0;
            if (numToDel > 0) trimmed = trimmed.substring(numToDel);
        }
        trimmed = cleanVal.contains(eCleanVal.UNTRIMMED) ? getSubstring(trimmed) : getSubstring(trimmed).trim();

        String str = (cleanVal.contains(eCleanVal.CLEAN_EACH)) ? DataUtil.cleanData(trimmed) : trimmed;
        if (!cleanVal.contains(eCleanVal.STRIP_ACCCENTS) && !cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT)
                && !cleanVal.contains(eCleanVal.TO_LOWER) && !cleanVal.contains(eCleanVal.TO_UPPER)
                && !cleanVal.contains(eCleanVal.TO_TITLECASE) && !cleanVal.contains(eCleanVal.STRIP_INDICATOR_2))
        {
            return (str);
        }
        // Do more extensive cleaning of data.
        if (cleanVal.contains(eCleanVal.STRIP_ACCCENTS))
        {
            str = ACCENTS.matcher(Normalizer.normalize(str, Form.NFD)).replaceAll("");
            StringBuilder folded = new StringBuilder();
            boolean replaced = false;
            for (char c : str.toCharArray())
            {
                char newc = Utils.foldDiacriticLatinChar(c);
                if (newc != 0x00)
                {
                    folded.append(newc);
                    replaced = true;
                }
                else
                {
                    folded.append(c);
                }
            }
            if (replaced) str = folded.toString();
        }
        if (cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT)) 
        {
            String str1 = str.replaceAll("( |\\p{Punct})+", " ");
            String str2 = PUNCT_OR_SPACE.matcher(str).replaceAll(" ");
            if (str1.equals(str2)) 
            {
                str = str1;
            }
            else
            {
                str = str2;
                str = str.replaceAll("( |\\p{Punct})+", " ");
            }
        }
        if (!cleanVal.contains(eCleanVal.UNTRIMMED))  str = str.trim();

        if (cleanVal.contains(eCleanVal.TO_LOWER))
        {
            str = str.toLowerCase();
        }
        else if (cleanVal.contains(eCleanVal.TO_UPPER))
        {
            str = str.toUpperCase();
        }
        else if (cleanVal.contains(eCleanVal.TO_TITLECASE))
        {
            str = DataUtil.toTitleCase(str);
        }
        return str;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addVal(java.lang.
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addSeparator(int,
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addAfterSubfield(java
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
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addAfterField(java.
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
