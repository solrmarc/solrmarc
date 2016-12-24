package org.solrmarc.index.extractor.formatter;

import java.util.Collection;
import java.util.EnumSet;

import org.marc4j.marc.VariableField;
import org.solrmarc.index.extractor.ExternalMethod;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public interface FieldFormatter extends ExternalMethod
{

    public final static EnumSet<eCleanVal> TITLE_SORT_UPPER = EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.STRIP_ACCCENTS,
            eCleanVal.STRIP_ALL_PUNCT, eCleanVal.TO_UPPER, eCleanVal.STRIP_INDICATOR_2);
    public final static EnumSet<eCleanVal> TITLE_SORT_LOWER = EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.STRIP_ACCCENTS,
            eCleanVal.STRIP_ALL_PUNCT, eCleanVal.TO_LOWER, eCleanVal.STRIP_INDICATOR_2);

    public enum eJoinVal
    {
        SEPARATE, JOIN;
    };

    public enum eCleanVal
    {
        CLEAN_END, CLEAN_EACH, STRIP_ALL_PUNCT, STRIP_ACCCENTS, TO_UPPER, TO_LOWER, STRIP_INDICATOR_2, UNTRIMMED, TO_TITLECASE;
    };

    public abstract String getFieldTagFmt();

    public abstract FieldFormatter setFieldTagFmt(String fieldTagFmt);

    public abstract String getIndicatorFmt();

    public abstract FieldFormatter setIndicatorFmt(String indicatorFmt);

    public abstract FieldFormatter setSfCodeFmt(String[] sfCodeFmt);

    public abstract String getSeparator();

    public abstract FieldFormatter setSeparator(String separator);

    public abstract eJoinVal getJoinVal();

    public abstract FieldFormatter setJoinVal(eJoinVal joinVal);
    
    public abstract FieldFormatter setSubstring(int offset, int endOffset);
 
    public abstract EnumSet<eCleanVal> getCleanVal();

    public abstract FieldFormatter setCleanVal(EnumSet<eCleanVal> cleanVal);

    public abstract FieldFormatter addCleanVal(eCleanVal cleanVal);

    public abstract FieldFormatter addMap(AbstractMultiValueMapping valueMapping);

    public abstract StringBuilder start();

    public abstract void addTag(StringBuilder sb, VariableField df);

    public abstract void addIndicators(StringBuilder sb, VariableField df);

    public abstract void addCode(StringBuilder sb, String codeStr);

    public Collection<String> prepData(VariableField vf, boolean isSubfieldA, String data) throws Exception;

    public abstract void addVal(StringBuilder sb, String sfcode, String data);

    public abstract void addSeparator(StringBuilder sb, int cnt);

    public abstract void addAfterSubfield(StringBuilder sb, Collection<String> result);

    public abstract void addAfterField(StringBuilder sb, Collection<String> result);

    public abstract String cleanData(VariableField vf, boolean isSubfieldA, String data);

    public abstract Collection<String> handleMapping(Collection<String> cleaned) throws Exception;

    public abstract String handleSubFieldFormat(String sfCode, String mappedDataVal);

    public abstract boolean hasFieldFormat();

    public abstract String getFieldFormat();
}