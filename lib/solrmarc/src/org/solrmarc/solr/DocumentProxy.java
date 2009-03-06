package org.solrmarc.solr;

import java.lang.reflect.InvocationTargetException;

public class DocumentProxy
{
    Object docObj;
    
    public DocumentProxy(Object obj)
    {
        docObj = obj;
    }

    public boolean hasFieldWithValue(String fldName, String fldVal)
    {
        String values[] = getValuesForField(fldName);
        for (String value : values)
        {
            if (fldVal.equals(value))  return(true);
        }
        return (false);
    }

    public String[] getValuesForField(String fldName)
    {
        Object result;
        try
        {
            result = docObj.getClass().getMethod("getValues", String.class).invoke(docObj, fldName);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return(null);
        }
        if (result instanceof String[])  return((String[]) result);
        return(new String[0]);
    }
    
    
}
