package org.solrmarc.mixin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.tools.Utils;

public class VuFindGetDate
{
    /**
     * Get all available dates from the record.
     *
     * @param  record MARC record
     * @return set of dates
     */
    public Set<String> getDates(final Record record) {
        Set<String> dates = new LinkedHashSet<String>();

        // First check old-style 260c date:
        List<VariableField> list260 = record.getVariableFields("260");
        for (VariableField vf : list260) {
            DataField df = (DataField) vf;
            List<Subfield> currentDates = df.getSubfields('c');
            for (Subfield sf : currentDates) {
                String currentDateStr = Utils.cleanDate(sf.getData());
                if (currentDateStr != null) dates.add(currentDateStr);
            }
        }

        // Now track down relevant RDA-style 264c dates; we only care about
        // copyright and publication dates (and ignore copyright dates if
        // publication dates are present).
        Set<String> pubDates = new LinkedHashSet<String>();
        Set<String> copyDates = new LinkedHashSet<String>();
        List<VariableField> list264 = record.getVariableFields("264");
        for (VariableField vf : list264) {
            DataField df = (DataField) vf;
            List<Subfield> currentDates = df.getSubfields('c');
            for (Subfield sf : currentDates) {
                String currentDateStr = Utils.cleanDate(sf.getData());
                char ind2 = df.getIndicator2();
                switch (ind2)
                {
                    case '1':
                        if (currentDateStr != null) pubDates.add(currentDateStr);
                        break;
                    case '4':
                        if (currentDateStr != null) copyDates.add(currentDateStr);
                        break;
                }
            }
        }
        if (pubDates.size() > 0) {
            dates.addAll(pubDates);
        } else if (copyDates.size() > 0) {
            dates.addAll(copyDates);
        }

        return dates;
    }

}
