package org.solrmarc.index.mapping;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractMultiValueMapping implements AbstractValueMapping<Collection<String>>
{
    public String mapSingle(String value) throws Exception
    {
        Collection<String> values = Collections.singletonList(value);
        Collection<String> result = this.map(values);
        if (result.size() == 0) 
            return(null);
        else 
            return result.iterator().next();
    }

}
