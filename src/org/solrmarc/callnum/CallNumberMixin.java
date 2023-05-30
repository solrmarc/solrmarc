package org.solrmarc.callnum;

import java.util.Collection;

import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.impl.custom.Mixin;


public class CallNumberMixin implements Mixin
{
    public static String LCCallNumberShelfKey(String LCNum)
    {
        LCCallNumber callNum = new LCCallNumber(LCNum);
        return(callNum.getShelfKey());
    }
    
    public static String LCCallNumberReverseShelfKey(String LCNum)
    {
        LCCallNumber callNum = new LCCallNumber(LCNum);
        String shelfKey = callNum.getShelfKey();
        return(Utils.reverseAlphanum(shelfKey));
    }
    
    public static String LCCallNumberPaddedShelfKey(String LCNum)
    {
        LCCallNumber callNum = new LCCallNumber(LCNum);
        return(callNum.getPaddedShelfKey());
    }
    
    public static String LCCallNumberReversePaddedShelfKey(String LCNum)
    {
        LCCallNumber callNum = new LCCallNumber(LCNum);
        String shelfKey = callNum.getPaddedShelfKey();
        return(Utils.reverseAlphanum(shelfKey));
    }
    
    public static String DeweyCallNumberShelfKey(String DeweyNum)
    {
        DeweyCallNumber callNum = new DeweyCallNumber(DeweyNum);
        return(callNum.shelfKey);
    }
    
    public static Collection<String> CallNumberCombineAndSortMap(Collection<String> in)
    {
        return(null);
    }
   
    public static Collection<String> CallNumberCombineAndSortExtract(Record rec, String tagsSpec)
    {
        return(null);
    }

}
