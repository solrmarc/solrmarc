package playground.solrmarc.index.extractor.formatter;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;
import org.solrmarc.tools.Utils;



public class FieldFormatterBase implements FieldFormatter
{
    String indicatorFmt = null;
    Map<String, String> sfCodeMap = null;
    String separator = null;
    
    boolean unique = false;
    eJoinVal joinVal = eJoinVal.SEPARATE;
    int trimStart = -1;
    int trimEnd = -1;
    EnumSet<eCleanVal> cleanVal = EnumSet.noneOf(eCleanVal.class);
 //   protected static StringBuilder buffer = new StringBuilder();
 //   protected static List<String> emptyList = Collections.emptyList();

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

    /*
     * (non-Javadoc)
     * 
     * @see playground.solrmarc.index.fieldmatch.FieldFormatter#getSfCodeFmt()
     */
    @Override
    public String getSfCodeFmt(char sfCode)
    {
        if (sfCodeMap != null && sfCodeMap.containsKey(sfCode))
        {
            return(sfCodeMap.get(sfCode));
        }
        return(null);
    }

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
            if (pieces.length == 2) sfCodeMap.put(pieces[0], pieces[1]);
        }
        return(this);
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
        this.trimStart = offset;
        this.trimEnd = endOffset;
        return(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see playground.solrmarc.index.fieldmatch.FieldFormatter#start(java.lang.
     * StringBuilder)
     */
    @Override
    public Collection<String> start()
    {
        buffer.setLength(0);
        return (makeResult());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addTag(org.marc4j.
     * marc.VariableField, java.lang.StringBuilder)
     */
    @Override
    public void addTag(VariableField df)
    {
        if (fieldTagFmt != null)
        {
            buffer.append(fieldTagFmt.contains("%tag") ? fieldTagFmt.replaceAll("%tag", df.getTag()) : df.getTag());
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
    public void addIndicators(VariableField df)
    {
        if (indicatorFmt != null && df instanceof DataField)
        {
            String result = indicatorFmt.replaceAll("%1", "" + ((DataField) df).getIndicator1()).replaceAll("%2",
                    "" + ((DataField) df).getIndicator1());
            buffer.append(result);
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
    public void addCode(String codeStr)
    {
//        if (sfCodeFmt != null)
//        {
//            buffer.append(sfCodeFmt.replaceAll("%sf", codeStr));
//        }
    }

    @Override
    public Collection<String> handleMapping(Collection<String> cleaned)
    {
        return (cleaned);
    }

    @Override
    public String handleSubFieldFormat(String sfCode, String mappedDataVal)
    {
        if (sfCodeMap == null || !sfCodeMap.containsKey(sfCode)) return (mappedDataVal);
        String value = sfCodeMap.get(sfCode);
        value = value.replace("$"+sfCode, mappedDataVal);
        return(value);
    }

    private final String trimData(final String data)
    {
        try
        {
            return (trimStart == -1 && trimEnd == -1) ? data : data.substring(trimStart, trimEnd + 1);
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            return("");
        }
    }
    
    private static Pattern ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    
    public String cleanData(VariableField vf, boolean isSubfieldA, String data)
    {
        final String trimmed = trimData(data);
        final EnumSet<eCleanVal> cleanVal = getCleanVal();
        String str = (cleanVal.contains(eCleanVal.CLEAN_EACH)) ? Utils.cleanData(trimmed) : trimmed;
        if (!cleanVal.contains(eCleanVal.STRIP_ACCCENTS) && !cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT)
                && !cleanVal.contains(eCleanVal.TO_LOWER) && !cleanVal.contains(eCleanVal.TO_UPPER)
                && !cleanVal.contains(eCleanVal.STRIP_INDICATOR_2))
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
        if (cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT)) str = str.replaceAll("[^A-Za-z0-9]+", " ");
        if (cleanVal.contains(eCleanVal.TO_LOWER))
        {
            str = str.toLowerCase();
        }
        else if (cleanVal.contains(eCleanVal.TO_UPPER))
        {
            str = str.toUpperCase();
        }
        int numToDel = 0;
        if (cleanVal.contains(eCleanVal.STRIP_INDICATOR_2) && isSubfieldA && vf instanceof DataField)
        {
            DataField df = (DataField) vf;
            char ind2Val = df.getIndicator2();
            numToDel = (ind2Val >= '0' && ind2Val <= '9') ? ind2Val - '0' : 0;
            if (numToDel > 0) str = str.substring(numToDel);
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
    public void addVal(String data)
    {
        buffer.append(data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * playground.solrmarc.index.fieldmatch.FieldFormatter#addSeparator(int,
     * java.lang.StringBuilder)
     */
    @Override
    public void addSeparator(int cnt)
    {
        if (getSeparator() != null)
        {
            if (cnt != 0) buffer.append(getSeparator());
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
    public void addAfterSubfield(Collection<String> result)
    {
        if (joinVal == eJoinVal.SEPARATE)
        {
            result.add(buffer.toString());
            buffer.setLength(0);
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
    public void addAfterField(Collection<String> result)
    {
        if (joinVal == eJoinVal.JOIN)
        {
            if (buffer.length() == 0) return;
            final String field = (this.getCleanVal().contains(eCleanVal.CLEAN_END)) ? Utils.cleanData(buffer.toString()) : buffer.toString();
            if (field.length() > 0) result.add(field);
            buffer.setLength(0);
        }
    }

    @Override
    public Collection<String> makeResult()
    {
        Collection<String> result;
        if (unique)
        {
            result = new LinkedHashSet<String>();
        }
        else
        {
            result = new ArrayList<String>();
        }
        return result;
    }

    @Override
    public Collection<String> prepData(VariableField vf, boolean isSubfieldA, String data)
    {
        final String cleaned = cleanData(vf, isSubfieldA, data);
        final List<String> cleanedDataAsList = (cleaned == null || cleaned.length() == 0) ? emptyList : Collections.singletonList(cleaned);
        Collection<String> result = handleMapping(cleanedDataAsList);
        return (result);
    }

}
