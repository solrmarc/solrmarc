package org.solrmarc.index;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.tools.Utils;

public class UrlMixin extends SolrIndexerMixin
{

    // URL Methods -------------------- Begin -------------------------- URL
    // Methods

    /**
     * returns the URLs for the full text of a resource described by the record
     * 
     * @param record
     * @return Set of Strings containing full text urls, or empty set if none
     */
    @SuppressWarnings("unchecked")
    public Set<String> getFullTextUrls(final Record record)
    {
        Set<String> resultSet = new LinkedHashSet<String>();

        List<VariableField> list856 = record.getVariableFields("856");
        for (VariableField vf : list856)
        {
            DataField df = (DataField) vf;
            List<String> possUrls = Utils.getSubfieldStrings(df, 'u');
            if (possUrls.size() > 0)
            {
                char ind2 = df.getIndicator2();
                switch (ind2)
                {
                    case '0':
                        resultSet.addAll(possUrls);
                        break;
                    case '2':
                        break;
                    default:
                        if (!isSupplementalUrl(df))
                            resultSet.addAll(possUrls);
                        break;
                }
            }
        }

        return resultSet;
    }
    
    /**
     * return true if passed 856 field contains a supplementary url (rather than
     * a fulltext URL. Determine by presence of "table of contents" or "sample
     * text" string (ignoring case) in subfield 3 or z. Note: Called only when
     * second indicator is not 0 or 2.
     */
    protected boolean isSupplementalUrl(DataField f856)
    {
        boolean supplmntl = false;
        List<String> list3z = Utils.getSubfieldStrings(f856, '3');
        list3z.addAll(Utils.getSubfieldStrings(f856, 'z'));
        for (String s : list3z)
        {
            String lc = s.toLowerCase();
            if (lc.contains("table") || lc.contains("some") || lc.contains("suppl") || lc.contains("errata")
               || lc.matches("^no[t]? ") || lc.contains("front") || lc.contains("search") || lc.contains("summary")
               || lc.contains("cover") || lc.contains("additional") || lc.contains("glosser") || lc.contains("appendix")
               || lc.contains("guide") || lc.contains("inhalts") || lc.contains("version") || lc.contains("addendum")
               || lc.contains("abstract") || lc.contains("index") || lc.contains("digest") || lc.contains("contents")
               || lc.contains("klappentext") || lc.contains("verlagsinformation") || lc.contains("lizenzfrei") || lc.contains("rezension") 
               || lc.contains("sample") || lc.contains("related"))
                
                supplmntl = true;
        }
        return supplmntl;
    }

    // URL Methods -------------------- End -------------------------- URL

    /**
     * returns the URLs for supplementary information (rather than fulltext)
     * 
     * @param record
     * @return Set of Strings containing supplementary urls, or empty string if
     *         none
     */
    @SuppressWarnings("unchecked")
    public Set<String> getSupplUrls(final Record record)
    {
        Set<String> resultSet = new LinkedHashSet<String>();

        List<VariableField> list856 = record.getVariableFields("856");
        for (VariableField vf : list856)
        {
            DataField df = (DataField) vf;
            List<String> possUrls = Utils.getSubfieldStrings(df, 'u');
            char ind2 = df.getIndicator2();
            switch (ind2)
            {
                case '2':
                    resultSet.addAll(possUrls);
                    break;
                case '0':
                    break;
                default:
                    if (isSupplementalUrl(df))
                        resultSet.addAll(possUrls);
                    break;
            }
        }
        return resultSet;
    }

    private String getURLLabelFrom3andZ(DataField df, String defaultLabel)
    {
        String label = "";
        
        List<Subfield> subs = (List<Subfield>)df.getSubfields();
        for (Subfield sf : subs)
        {
            if (sf.getCode() == 'z' || sf.getCode() == '3')
            {
                label = label + sf.getData() + " ";
            }
        }
        label = label.replaceAll("[ ]+$", "");
        if (label.length()== 0) label = defaultLabel;
        return(label);
    }
    
    private String buildParsableURLString(DataField df, String defaultLabel) throws Exception
    {
        return(buildParsableURLString(df, defaultLabel, null));
    }
    
    private String buildParsableURLString(DataField df, String defaultLabel, Object patternMap) throws Exception
    {
        String label = getURLLabelFrom3andZ(df, defaultLabel);
        String url = df.getSubfield('u').getData(); 
        if (patternMap != null)
        {
            url = SolrIndexer.instance().remap(url, patternMap, true);
        }
//        if (url.startsWith("http://www.jstor.org"))
//        {
//            url = "http://proxy.its.virginia.edu/login?url=" + url;
//        }
        String result = url + "||" + label;
        return(result);
    }
    
    public Set<String> getLabelledURLmapped(final Record record, String defaultLabel, String patternMapFileName) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        Set<String> backupResultSet = new LinkedHashSet<String>();
        List<?> urlFields = record.getVariableFields("856");
        String mapName = SolrIndexer.instance().loadTranslationMap(patternMapFileName);
        Object patternMap = SolrIndexer.instance().findMap(mapName);
        for (Object field : urlFields)
        {
            if (field instanceof DataField)
            {
                DataField dField = (DataField)field;
                if (dField.getIndicator1() == '4' && dField.getIndicator2() == '0')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, defaultLabel, patternMap));
                    }
                }
                else if (dField.getIndicator1() == '4' && dField.getIndicator2() == '1')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related resources";
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label, patternMap));
                    }
                }
                else if (dField.getIndicator1() == '4' && dField.getIndicator2() == ' ')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : defaultLabel;
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label, patternMap));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '0')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, defaultLabel, patternMap));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '1')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related resources";
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label, patternMap));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == ' ')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : defaultLabel;
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label, patternMap));
                    }
                }
            }
        }
        if (resultSet.size() == 0 && backupResultSet.size() != 0)
        {
            return(backupResultSet);
        }
        return(resultSet);
    }
    
    public Set<String> getLabelledURLnew(final Record record, String defaultLabel) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        Set<String> backupResultSet = new LinkedHashSet<String>();
        List<?> urlFields = record.getVariableFields("856");
        for (Object field : urlFields)
        {
            if (field instanceof DataField)
            {
                DataField dField = (DataField)field;
                if (firstIndicatorValid(dField) && dField.getIndicator2() == '0')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
                else if (firstIndicatorValid(dField) && dField.getIndicator2() == '1' && !isSupplementalUrl(dField))
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "";
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label));
                    }
                }
//                if (dField.getIndicator1() == '4' && dField.getIndicator2() == '2')
//                {
//                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related Info";
//                    if (dField.getSubfield('u') != null) 
//                    {
//                        resultSet.add(buildParsableURLString(dField, label));
//                    }
//                }
                else if (firstIndicatorValid(dField) && dField.getIndicator2() == ' ' && !isSupplementalUrl(dField))
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : defaultLabel;
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '0')
                {
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, defaultLabel));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '1' && !isSupplementalUrl(dField))
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related resources";
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label));
                    }
                }
//                if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '2')
//                {
//                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related Info";
//                    if (dField.getSubfield('u') != null) 
//                    {
//                        backupResultSet.add(buildParsableURLString(dField, label));
//                    }
//                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == ' ' && !isSupplementalUrl(dField))
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : defaultLabel;
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label));
                    }
                }
            }
        }
        if (resultSet.size() == 0 && backupResultSet.size() != 0)
        {
            return(backupResultSet);
        }
        return(resultSet);
    }

    private boolean firstIndicatorValid(DataField dField)
    {
        if (dField.getIndicator1() == '4' || dField.getIndicator1() == '1') return(true);
        if (dField.getIndicator1() == '7')
        {
            Subfield sf = dField.getSubfield('2');
            if (sf != null && (sf.getData().equals("http") || sf.getData().equals("ftp")))
            {
                return(true);
            }
        }
        return(false);
    }

    public Set<String> getLabelledSupplURLnew(final Record record, String defaultLabel) throws Exception
    {
        Set<String> resultSet = new LinkedHashSet<String>();
        Set<String> backupResultSet = new LinkedHashSet<String>();
        List<?> urlFields = record.getVariableFields("856");
        for (Object field : urlFields)
        {
            if (field instanceof DataField)
            {
                DataField dField = (DataField)field;
//                if (dField.getIndicator1() == '4' && dField.getIndicator2() == '0')
//                {
//                    if (dField.getSubfield('u') != null) 
//                    {
//                        resultSet.add(buildParsableURLString(dField, defaultLabel));
//                    }
//                }
                if (firstIndicatorValid(dField) && dField.getIndicator2() == '1' && isSupplementalUrl(dField))
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "";
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label));
                    }
                }
                else if (firstIndicatorValid(dField) && dField.getIndicator2() == '2')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "";
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label));
                    }
                }
                else if (firstIndicatorValid(dField) && dField.getIndicator2() == ' ' && isSupplementalUrl(dField))
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : defaultLabel;
                    if (dField.getSubfield('u') != null) 
                    {
                        resultSet.add(buildParsableURLString(dField, label));
                    }
                }
//                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '0')
//                {
//                    if (dField.getSubfield('u') != null) 
//                    {
//                        backupResultSet.add(buildParsableURLString(dField, defaultLabel));
//                    }
//                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '1' && isSupplementalUrl(dField))
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related resources";
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == '2')
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : "Related Info";
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label));
                    }
                }
                else if (dField.getIndicator1() == ' ' && dField.getIndicator2() == ' ' && isSupplementalUrl(dField))
                {
                    String label = (dField.getSubfield('3') != null) ? dField.getSubfield('3').getData() : defaultLabel;
                    if (dField.getSubfield('u') != null) 
                    {
                        backupResultSet.add(buildParsableURLString(dField, label));
                    }
                }
            }
        }
        if (resultSet.size() == 0 && backupResultSet.size() != 0)
        {
            return(backupResultSet);
        }
        return(resultSet);
    }

}
