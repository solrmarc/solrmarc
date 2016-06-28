package org.solrmarc.callnum;

import java.util.Collection;

import org.marc4j.marc.Record;
import org.solrmarc.index.extractor.impl.custom.Mixin;


public class CallNumberMixin implements Mixin
{
    public static String LCCallNumberShelfKey(String LCNum)
    {
        LCCallNumber callNum = new LCCallNumber(LCNum);
        return(callNum.shelfKey);
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
