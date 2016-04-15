package playground.solrmarc.index.fieldmatch;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;

import playground.solrmarc.index.fieldmatch.FieldFormatter.eCleanVal;
import playground.solrmarc.tools.Utils;

public final class FieldFormatterForSort extends FieldFormatterBase
{
//    public final static EnumSet<eCleanVal> TITLE_SORT_UPPER = EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.STRIP_ACCCENTS, eCleanVal.STRIP_ALL_PUNCT, eCleanVal.TO_UPPER, eCleanVal.STRIP_INDICATOR_2);
//    public final static EnumSet<eCleanVal> TITLE_SORT_LOWER = EnumSet.of(eCleanVal.CLEAN_EACH, eCleanVal.STRIP_ACCCENTS, eCleanVal.STRIP_ALL_PUNCT, eCleanVal.TO_LOWER, eCleanVal.STRIP_INDICATOR_2);
//	
//	public FieldFormatterForSort(EnumSet<eCleanVal> cleanVal)
//    {
//        super(false);
//        this.setCleanVal(cleanVal);
//    }
//
//    public FieldFormatterForSort(EnumSet<eCleanVal> cleanVal, String separator)
//    {
//        super(false);
//        this.setCleanVal(cleanVal);
//        this.setSeparator(separator);
//    }
//
	public String cleanData(VariableField vf, boolean isSubfieldA, String data) 
    {
		final EnumSet<eCleanVal> cleanVal = getCleanVal();
		String str = (cleanVal.contains(eCleanVal.CLEAN_EACH)) ? Utils.cleanData(data) : data;
        if (cleanVal.contains(eCleanVal.STRIP_ACCCENTS))
        {
            str = Normalizer.normalize(str, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); 
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
        if (cleanVal.contains(eCleanVal.STRIP_ALL_PUNCT))  str = str.replaceAll("[^A-Za-z0-9]+", " ");
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
            DataField df = (DataField)vf;
            char ind2Val = df.getIndicator2();
            numToDel = (ind2Val >= '0' && ind2Val <= '9') ? ind2Val - '0' : 0;
            if (numToDel > 0)  str = str.substring(numToDel);
        }
        return str;
    }
}
