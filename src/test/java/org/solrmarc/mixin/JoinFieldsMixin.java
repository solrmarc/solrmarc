package org.solrmarc.mixin;

import java.util.*;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.SolrIndexerMixin;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.MultiValueIndexer;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.tools.DataUtil;

public class JoinFieldsMixin extends SolrIndexerMixin {

    private ThreadLocal<Map<String, AbstractValueIndexer<?>>> indexerCache =
        new ThreadLocal<Map<String, AbstractValueIndexer<?>>>()
        {
            @Override
            protected Map<String, AbstractValueIndexer<?>> initialValue()
            {
                return new HashMap<>();
            }
        };

    public List<String> getSimpleJoinedFields(final Record record, String firstFieldSpec, String secondFieldSpec, String separator) 
    {
		List<String> result = new ArrayList<String>();
        List<VariableField> firstFields = record.getVariableFields(firstFieldSpec.substring(0, 3));
        List<VariableField> secondFields = record.getVariableFields(secondFieldSpec.substring(0, 3));
        ArrayList<Subfield> firstSubfields = getSubfieldsMatching(firstFields, firstFieldSpec.substring(3));
        ArrayList<Subfield> secondSubfields = getSubfieldsMatching(secondFields, secondFieldSpec.substring(3));
		int i;
		for (i = 0; i < firstSubfields.size() && i < secondSubfields.size(); i++)
		{
		    String resultStr = firstSubfields.get(i).getData() + separator + secondSubfields.get(i).getData();
		    result.add(resultStr);
		}
        for (; i < firstSubfields.size(); i++)
        {
            String resultStr = firstSubfields.get(i).getData();
            result.add(resultStr);
        }
        for (; i < secondSubfields.size(); i++)
        {
            String resultStr = secondSubfields.get(i).getData();
            result.add(resultStr);
        }
		return result;
    }

    public Collection<String> mapViaFormatterValue(Collection<String> values, String cleanValParam)
    {
        EnumSet<FieldFormatter.eCleanVal> cleanValue = DataUtil.getCleanValForParam(cleanValParam);
        Collection<String> result = (values instanceof Set<?>) ? new LinkedHashSet<String>() : new ArrayList<String>();
        for (String value : values)
        {
            result.add(DataUtil.cleanByVal(value, cleanValue));
        }
        return(result);
    }
    
    public List<String> getComplexJoinedFields(final Record record, String firstFieldSpec, String secondFieldSpec, String separator) throws Exception 
    {
        List<String> result = new ArrayList<String>();
        AbstractValueIndexer<?> indexer1 = getOrCreateIndexerFullSpec(firstFieldSpec);
        AbstractValueIndexer<?> indexer2 = getOrCreateIndexerFullSpec(secondFieldSpec);
        
        ArrayList<String> firstData = new ArrayList<String>();  
        firstData.addAll(indexer1.getFieldData(record));
        ArrayList<String> secondData = new ArrayList<String>();  
        secondData.addAll(indexer2.getFieldData(record));
        
        int i;
        for (i = 0; i < firstData.size() && i < secondData.size(); i++)
        {
            String resultStr = firstData.get(i) + separator + secondData.get(i);
            result.add(resultStr);
        }
        for (; i < firstData.size(); i++)
        {
            String resultStr = firstData.get(i);
            result.add(resultStr);
        }
        for (; i < secondData.size(); i++)
        {
            String resultStr = secondData.get(i);
            result.add(resultStr);
        }

        return(result);
    }

    public List<String> getComplexJoinedFields(final Record record, String firstFieldSpec, String secondFieldSpec, String thirdFieldSpec, String separator) throws Exception 
    {
        List<String> result = new ArrayList<String>();
        AbstractValueIndexer<?> indexer1 = getOrCreateIndexerFullSpec(firstFieldSpec);
        AbstractValueIndexer<?> indexer2 = getOrCreateIndexerFullSpec(secondFieldSpec);
        AbstractValueIndexer<?> indexer3 = getOrCreateIndexerFullSpec(thirdFieldSpec);
        
        ArrayList<String> firstData = new ArrayList<String>();  
        firstData.addAll(indexer1.getFieldData(record));
        ArrayList<String> secondData = new ArrayList<String>();  
        secondData.addAll(indexer2.getFieldData(record));
        ArrayList<String> thirdData = new ArrayList<String>();  
        thirdData.addAll(indexer3.getFieldData(record));
        
        int i;
        int maxSize = firstData.size() > secondData.size() ? firstData.size() : secondData.size();
        maxSize = maxSize > thirdData.size() ? maxSize : thirdData.size();
        for (i = 0; i < maxSize; i++)
        {
            String resultStr = ((i < firstData.size())? firstData.get(i) : "") + separator + 
                               ((i < secondData.size())? secondData.get(i): "") + separator + 
                               ((i < thirdData.size())? thirdData.get(i): "");
            result.add(resultStr);
        }
        return(result);
    }

    private ArrayList<Subfield> getSubfieldsMatching(List<VariableField> fields, String subfieldCodes)
    {
	    ArrayList<Subfield> subfields = new ArrayList<Subfield>();
	    for (VariableField vf : fields)
	    {
	        if (!(vf instanceof DataField)) return(subfields);
	        DataField df = (DataField)vf;
	        subfields.addAll(df.getSubfields(subfieldCodes));
	    }
	    return(subfields);
    }
	
    private AbstractValueIndexer<?> getOrCreateIndexerFullSpec(String fullSpec)
    {
        if (indexerCache.get().containsKey(fullSpec))
        {
            return(indexerCache.get().get(fullSpec));
        }
        else
        {
            AbstractValueIndexer<?> indexer;
            synchronized (ValueIndexerFactory.instance())
            {
                indexer = ValueIndexerFactory.instance().createValueIndexer("", fullSpec);
                indexerCache.get().put(fullSpec, indexer);
            }
            return(indexer);
        }
    }

}
