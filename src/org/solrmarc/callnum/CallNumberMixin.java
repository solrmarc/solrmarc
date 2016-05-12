package org.solrmarc.callnum;

import playground.solrmarc.index.extractor.impl.custom.Mixin;

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

}
