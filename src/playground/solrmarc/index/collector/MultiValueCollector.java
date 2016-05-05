package playground.solrmarc.index.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.solrmarc.tools.StringNaturalCompare;

public class MultiValueCollector //implements AbstractValueCollector<Collection<String>>
{
    boolean isUnique = false;

    Comparator<String> sortComparator = null;
    boolean first = false;
    
    public MultiValueCollector(String comp, String dir, boolean unique, boolean first)
    {
        isUnique = unique;
        this.first = first;
        sortComparator = setComparator(comp, dir);
    }
    
    public MultiValueCollector(boolean unique, boolean first)
    {
        isUnique = unique;
        this.first = first;
        sortComparator = null;
    }
        
    public MultiValueCollector()
    {
        isUnique = false;
        this.first = false;
        sortComparator = null;
    }
   
    private class LengthComp implements Comparator<String> {

        public int compare(String s1, String s2)
        {
            return s1.length() - s2.length();
        }
    }
    
    private Comparator<String> setComparator(String compStr, String dir)                       
    {
        Comparator<String> comp = null;
        if (compStr == null) return(null);
        if (compStr.equals("str") && dir.equals("desc")) 
            comp = Collections.reverseOrder();
        else if (compStr.equals("str") && dir.equals("asc")) 
            comp = Collections.reverseOrder(Collections.reverseOrder()); // really?
        else if (compStr.equals("num") && dir.equals("asc")) 
            comp = new StringNaturalCompare();
        else if (compStr.equals("num") && dir.equals("desc")) 
            comp = Collections.reverseOrder( new StringNaturalCompare()); 
        else if (compStr.equals("length") && dir.equals("asc")) 
            comp = new LengthComp();
        else if (compStr.equals("length") && dir.equals("desc")) 
            comp = Collections.reverseOrder( new LengthComp()); 
        else
        { /* throw an IndexerSpecException here */ }
        return(comp);
    }

    public Collection<String> collect(final Collection<String> values)
    {
        if ((!isUnique || values instanceof Set<?> ) && sortComparator == null && !first) 
            return values;
        Collection<String> result;
        if (isUnique)
        {
            if (sortComparator == null)
            {
                result = new LinkedHashSet<String>();
            }
            else 
            {
                result = new TreeSet<String>(sortComparator);
            }
            result.addAll(values);
        }
        else if (sortComparator != null)
        {
            List<String> resultL = new ArrayList<String>();
            resultL.addAll(values);
            Collections.sort(resultL, sortComparator);
            result = resultL;
        }
        else
        {
            result = values;
        }
        if (first)
        {
            if (result == null || result.isEmpty())
            {
                return (result);
            }
            return Collections.singletonList(result.iterator().next());
        }
        return(result);
    }
    
    public boolean isUnique()
    {
        return isUnique;
    }

    public MultiValueCollector setUnique(boolean isUnique)
    {
        this.isUnique = isUnique;
        return(this);
    }

    public Comparator<String> getSortComparator()
    {
        return sortComparator;
    }

    public MultiValueCollector setSortComparator(String comp, String dir)
    {
        this.sortComparator = setComparator(comp, dir);
        return(this);
    }

    public boolean isFirst()
    {
        return first;
    }

    public MultiValueCollector setFirst(boolean first)
    {
        this.first = first;
        return(this);
    }
}
