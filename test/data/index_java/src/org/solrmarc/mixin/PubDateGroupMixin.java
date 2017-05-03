package org.solrmarc.mixin;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.index.mapping.AbstractMultiValueMapping;

public class PubDateGroupMixin
{
    /**
     * returns the publication date groupings from a record, if it is present
     * 
     * @param record
     * @return Set of Strings containing the publication date groupings associated with the publish date
     * @throws Exception
     */
    public Collection<String> getPubDateGroups(final Record record, String mapfilename) throws Exception
    {
        Collection<String> resultSet = getPubDateGroups(record);
        AbstractMultiValueMapping map = ValueIndexerFactory.instance().createMultiValueMapping(mapfilename);
        resultSet = map.map(resultSet);
        return resultSet;
    }

    /**
     * returns the publication date groupings from a record, if it is present
     * 
     * @param record
     * @return Set of Strings containing the publication date groupings associated with the publish date
     * @throws Exception
     */
    public Collection<String> getPubDateGroups(final Record record) throws Exception
    {
        Collection<String> resultSet = new LinkedHashSet<String>();
        int cYearInt = Calendar.getInstance().get(Calendar.YEAR);

        // get the pub date, with decimals assigned for inclusion in ranges
        String publicationDate = SolrIndexer.instance().getPublicationDate(record);

        if (publicationDate != null)
        {
            int year;
            try
            {
                year = Integer.parseInt(publicationDate);
                // "this year" and "last three years" are for 4 digits only
                if (year >= (cYearInt - 1)) resultSet.add("thisyear");
                if (year >= (cYearInt - 2)) resultSet.add("lasttwoyears");
                if (year >= (cYearInt - 3)) resultSet.add("lastthreeyears");
                if (year >= (cYearInt - 5)) resultSet.add("lastfiveyears");
                if (year >= (cYearInt - 10)) resultSet.add("lasttenyears");
                if (year >= (cYearInt - 20)) resultSet.add("lasttwentyyears");
                if (year >= (cYearInt - 50)) resultSet.add("last50years");
                if (year < (cYearInt - 50) && (year > -1.0)) resultSet.add("morethan50years");
            }
            catch (NumberFormatException nfe)
            {
                // bad year format, skip it.
            }
        }
        return resultSet;
    }
}
