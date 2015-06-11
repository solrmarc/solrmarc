package org.solrmarc.z3950;

import java.util.Iterator;

import org.marc4j.marc.Record;

public class Z3950RecordIter implements Iterator<Record>
{
    ZClient client;
    String resultSetName;
    int numInResultSet;
    int currentCursor;
    String remapStr = null;
    
    public Z3950RecordIter(ZClient client, String resultSetName, int numInResultSet)
    {
        this.client = client;
        this.resultSetName = resultSetName;
        this.numInResultSet = numInResultSet;
        currentCursor = 0;
    }
    
    public Z3950RecordIter(ZClient zClient, String resultSetName, int numInResultSet, String remapStr)
    {
        this(zClient, resultSetName, numInResultSet);
        this.remapStr = remapStr;
    }

    @Override
    public boolean hasNext()
    {
        if (currentCursor < numInResultSet) return(true); 
        return false;
    }

    @Override
    public Record next()
    {
        Record rec = client.getRecord(++this.currentCursor, this.resultSetName);
        return(rec);
    }

    @Override
    public void remove()
    {
        // TODO Auto-generated method stub
        
    }

}
