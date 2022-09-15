package org.solrmarc.index.extractor.impl.direct;

import java.util.EnumSet;

import org.solrmarc.index.extractor.formatter.FieldFormatter.eCleanVal;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public interface ModifyableMultiValueExtractor
{

    public void setJoinVal(eJoinVal join);

    public void setSeparator(String string);

    public void setFormatPatterns(String[] mapParts);

    public void setSubstring(String string, String string2);

    public void addCleanVal(eCleanVal cleanEach);

    public void setCleanVal(EnumSet<eCleanVal> of);

    public void addMap(AbstractMultiValueMapping valueMapping);

}
