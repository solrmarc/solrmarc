package playground.solrmarc.index.fieldmatch;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.marc4j.marc.VariableField;

public abstract class FieldFormatter
{

    public final static EnumSet<eCleanVal> TITLE_SORT_UPPER = EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.STRIP_ACCCENTS,
            eCleanVal.STRIP_ALL_PUNCT, eCleanVal.TO_UPPER, eCleanVal.STRIP_INDICATOR_2);
    public final static EnumSet<eCleanVal> TITLE_SORT_LOWER = EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.STRIP_ACCCENTS,
            eCleanVal.STRIP_ALL_PUNCT, eCleanVal.TO_LOWER, eCleanVal.STRIP_INDICATOR_2);

    protected static StringBuilder buffer = new StringBuilder();

    public enum eCleanVal
    {
        CLEAN_END, CLEAN_EACH, STRIP_ALL_PUNCT, STRIP_ACCCENTS, TO_UPPER, TO_LOWER, STRIP_INDICATOR_2;
    };

    public abstract String getFieldTagFmt();

    public abstract void setFieldTagFmt(String fieldTagFmt);

    public abstract String getIndicatorFmt();

    public abstract void setIndicatorFmt(String indicatorFmt);

    public abstract String getSfCodeFmt();

    public abstract void setSfCodeFmt(String sfCodeFmt);

    public abstract String getSeparator();

    public abstract void setSeparator(String separator);

    public abstract EnumSet<eCleanVal> getCleanVal();

    public abstract void setCleanVal(EnumSet<eCleanVal> cleanVal);

    public abstract void addCleanVal(eCleanVal cleanVal);

    public abstract Collection<String> start();

    public abstract Collection<String> makeResult();

    public abstract void addTag(VariableField df);

    public abstract void addIndicators(VariableField df);

    public abstract void addCode(String codeStr);

    public Collection<String> prepData(VariableField vf, boolean isSubfieldA, String data) throws Exception
    {
        final String cleaned = cleanData(vf, isSubfieldA, data);
        final List<String> cleanedDataAsList = (cleaned == null || cleaned.length() == 0) ? Collections.emptyList()
                : Collections.singletonList(cleaned);
        Collection<String> result = handleMapping(cleanedDataAsList);
        return (result);
    }

    public abstract void addVal(String data);

    public abstract void addSeparator(int cnt);

    public abstract void addAfterSubfield(Collection<String> result);

    public abstract void addAfterField(Collection<String> result);

    public abstract String cleanData(VariableField vf, boolean isSubfieldA, String data);

    public abstract Collection<String> handleMapping(Collection<String> cleaned) throws Exception;

    public abstract void setUnique();

    public abstract void unsetUnique();

    public abstract boolean isUnique();

}