package org.solrmarc.index.collector;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.tools.SolrMarcIndexerException;
import org.solrmarc.tools.StringNaturalCompare;


public class MultiValueCollector //implements AbstractValueCollector<Collection<String>>
{
    boolean isUnique = false;
    public enum eAction { NOACTION, SKIP, DELETE };
    eAction ifFieldEmpty = eAction.NOACTION;
    eAction ifFieldNotEmpty = eAction.NOACTION;
    public enum eNormalize { NOCHANGE, COMPOSE, DECOMPOSE };
    eNormalize normalize = eNormalize.NOCHANGE;
    Comparator<String> sortComparator = null;
    public enum eFirstVal { ALL, FIRST, NOTFIRST };
    public static eFirstVal fromString(String str)
    {
        if (str.equals("first")) return(eFirstVal.FIRST);
        if (str.equals("notfirst")) return(eFirstVal.NOTFIRST);
        return(eFirstVal.ALL);
    }; 
    eFirstVal first = eFirstVal.ALL;

    public MultiValueCollector(String comp, String dir, boolean unique, String first)
    {
        isUnique = unique;
        this.first = fromString(first);;
        sortComparator = setComparator(comp, dir);
    }

    public MultiValueCollector(boolean unique, String first)
    {
        isUnique = unique;
        this.first = fromString(first);;
        sortComparator = null;
    }

    public MultiValueCollector()
    {
        isUnique = false;
        this.first = eFirstVal.ALL;
        sortComparator = null;
    }

    private class LengthComp implements Comparator<String> {

        public int compare(String s1, String s2)
        {
            return s1.length() - s2.length();
        }
    }

    private class NaturalComp implements Comparator<String> {

        public int compare(String s1, String s2)
        {
            return s1.compareTo(s2);
        }
    }

    private Comparator<String> setComparator(String compStr, String dir)
    {
        Comparator<String> comp = null;
        if (compStr == null) return(null);
        if (compStr.equals("str") && dir.equals("desc")) 
            comp = Collections.reverseOrder();
        else if (compStr.equals("str") && dir.equals("asc")) 
            comp = new NaturalComp();
        else if (compStr.equals("num") && dir.equals("asc")) 
            comp = new StringNaturalCompare();
        else if (compStr.equals("num") && dir.equals("desc")) 
            comp = Collections.reverseOrder( new StringNaturalCompare()); 
        else if (compStr.equals("length") && dir.equals("asc")) 
            comp = new LengthComp();
        else if (compStr.equals("length") && dir.equals("desc")) 
            comp = Collections.reverseOrder( new LengthComp()); 
        else if (!compStr.equals("length") && !compStr.equals("num") && !compStr.equals("str"))
        { 
            throw new IndexerSpecException("Invalid sort specification, valid values are \"str\"  \"num\"  \"length\" ");
        }
        else 
        { 
            throw new IndexerSpecException("Invalid sort direction, valid values are \"asc\"  \"desc\" ");
        }
        return(comp);
    }

    public Collection<String> collect(final Collection<String> values)
    {
        if ((!isUnique || values instanceof Set<?> ) && sortComparator == null && 
                first == eFirstVal.ALL && ifFieldEmpty == eAction.NOACTION && ifFieldNotEmpty == eAction.NOACTION && normalize == eNormalize.NOCHANGE) 
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
            addAllNormalized(result, values, normalize);
        }
        else if (sortComparator != null)
        {
            List<String> resultL = new ArrayList<String>();
            addAllNormalized(resultL, values, normalize);
            Collections.sort(resultL, sortComparator);
            result = resultL;
        }
        else
        {
            result = values;
        }
        if (result == null || result.isEmpty())
        {
            if (ifFieldEmpty == eAction.DELETE)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.DELETE);
            }
            else if (ifFieldEmpty == eAction.SKIP)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.IGNORE);
            }
            return (result);
        }
        else 
        {
            if (ifFieldNotEmpty == eAction.DELETE)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.DELETE);
            }
            else if (ifFieldNotEmpty == eAction.SKIP)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.IGNORE);
            }
        }
        if (first == eFirstVal.FIRST)
        {
            return Collections.singletonList(result.iterator().next());
        }
        else if (first == eFirstVal.NOTFIRST)
        {
            if (result instanceof List)
            {
                result = ((List<String>) result).subList(1, result.size());
            }
            else
            {
                result = Collections.list(Collections.enumeration(result)).subList(1,  result.size());
            }
        }
        if (result.isEmpty() && ifFieldEmpty != eAction.NOACTION)
        {
            if (ifFieldEmpty == eAction.DELETE)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.DELETE);
            }
            else if (ifFieldEmpty == eAction.SKIP)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.IGNORE);
            }
        }
        if (!result.isEmpty() && ifFieldNotEmpty != eAction.NOACTION)
        {
            if (ifFieldNotEmpty == eAction.DELETE)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.DELETE);
            }
            else if (ifFieldNotEmpty == eAction.SKIP)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.IGNORE);
            }
        }
        return(result);
    }

    private void addAllNormalized(Collection<String> result, Collection<String> values, eNormalize normalize)
    {
        if (normalize == eNormalize.NOCHANGE) 
        {
            result.addAll(values);
        }
        else if (normalize == eNormalize.COMPOSE)
        {
            for (String value : values)
            {
                String normedValue = Normalizer.normalize(value,  Normalizer.Form.NFC);
                result.add(normedValue);
            }
        }
        else if (normalize == eNormalize.DECOMPOSE)
        {
            for (String value : values)
            {
                String normedValue = Normalizer.normalize(value,  Normalizer.Form.NFD);
                result.add(normedValue);
            }
        }
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

//    public boolean isFirst()
//    {
//        return first;
//    }

    public MultiValueCollector setFirst(eFirstVal first)
    {
        this.first = first;
        return(this);
    }
    public MultiValueCollector setFirst(String firstStr)
    {
        this.first = fromString(firstStr);
        return(this);
    }

    public void setIfFieldEmpty(eAction action)
    {
        this.ifFieldEmpty = action;
    }

    public void setIfFieldNotEmpty(eAction action)
    {
        this.ifFieldNotEmpty = action;
    }

    public void setNormalize(String normVal)
    {
        if (normVal.equals("C"))
        {
           this.normalize = eNormalize.COMPOSE; 
        }
        else if (normVal.equals("D"))
        {
            this.normalize = eNormalize.DECOMPOSE; 
        }
        else
        {
            this.normalize = eNormalize.NOCHANGE; 
        }
    }
}
