package org.solrmarc.index;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.marc4j.marc.Record;

public abstract class SolrIndexerMixin
{    
    protected SolrIndexer indexer = null;
    Map<String,Method> methodMap = null;
    
    public SolrIndexerMixin()
    {
       if (methodMap == null)
       {
           methodMap = new LinkedHashMap<String, Method>();
           Method[] methods = this.getClass().getDeclaredMethods();
           for (Method method : methods)
           {
               Class<?>[] params = method.getParameterTypes();
               Class<?> returnType = method.getReturnType();
               if (!Set.class.isAssignableFrom(returnType) &&
                   !String.class.equals(returnType))
               {
                   // invalid return type, ignore method
                   continue;
               }
               if (!Modifier.isPublic(method.getModifiers()))
               {
                   // method not public, ignore it
                   continue;
               }
               if (params.length == 0 || !Record.class.isAssignableFrom(params[0]))
               {
                   // methods first param not a Marc Record object, ignore it
                   continue;
               }
               boolean parmsGood = true;
               for (int i = 1; i < params.length; i++)
               {
                   Class<?> param = params[i];
                   if (!String.class.equals(param))
                   {
                       parmsGood = false;
                       break;
                   }
               }
               if (parmsGood)
               {
                   methodMap.put(method.getName(), method);
               }
           }
       }
    }
    
    public void perRecordInit(Record record)
    {
        
    }
    
    public final void setMainIndexer(SolrIndexer mainIndexer)
    {
        indexer = mainIndexer;
    }
    
    @SuppressWarnings("unchecked")
    public Set<String> invokeByName(String methodName, Record record, String ... parameters )
    {
        Method toInvoke = methodMap.get(methodName);
        Object args[] = new Object[parameters.length+1];
        args[0] = record;
        System.arraycopy(parameters, 0, args, 1, parameters.length);
        try
        {
            if (String.class.isAssignableFrom(toInvoke.getReturnType()))
            {
                Set<String> result = new LinkedHashSet<String>();
                String resultStr = (String)toInvoke.invoke(this, args);
                result.add(resultStr);
            }
            else
            {
                return (Set<String>) (toInvoke.invoke(this, args));
            }
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(null);
    }
}
