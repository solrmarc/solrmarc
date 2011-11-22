package org.solrmarc.index;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.index.SolrIndexerMixin;
import org.solrmarc.tools.Utils;

public class VideoInfoMixin extends SolrIndexerMixin
{
    Pattern releaseDatePattern = null;
    boolean isVideo = false;
    SolrIndexerMixin formatmixin = null;
    public void perRecordInit(Record record)
    {
        Class<?> blIndexer = null;
        try
        {
            blIndexer = Class.forName("org.blacklight.BlacklightIndexer");
        }
        catch (ClassNotFoundException e)
        {
            blIndexer = null;
        }
        if (blIndexer != null && blIndexer.isInstance(indexer))
        {
            Set<String> format = ((org.blacklight.BlacklightIndexer)indexer).getCombinedFormatNew2(record);
            isVideo = (Utils.setItemContains(format, "Video"));
        }
        else
        {
            if (formatmixin == null) 
            {
                formatmixin = indexer.findMixin("org.solrmarc.index.GetFormatMixin");
            }
            Set<String> format = ((org.solrmarc.index.GetFormatMixin)formatmixin).getContentTypesAndMediaTypes(record);
            isVideo = (Utils.setItemContains(format, "Video"));
        }
    }
    
    /**
     * Extract the runtime of a video item from characters 18-20 of the 008 field
     * 
     * @param record   the record being processed
     * @return  String  representing the runtime in minutes for the video (or null)
     */

    public String getVideoRunTime(Record record)
    {
        if (isVideo)
        {
            String runtime = indexer.getFirstFieldVal(record, null, "008[18-20]");
            if (runtime != null && runtime.matches("[0-9][0-9][0-9]"))
            {
                return(runtime.replaceAll("^0*", ""));
            }
        }
        return(null);
    }

    /**
     * Extract the targetAudience of a video item from the 521a subfield (if present)
     * 
     * @param record   the record being processed
     * @return  String  representing the targetAudience for the video (or null)
     */
    public String getVideoTargetAudience(Record record)
    {
        if (isVideo)
        {
            Set<String> target = indexer.removeTrailingPunct(record, "521a");
            if (target == null || target.size() == 0)
            {
                return (null);
            }
            return(target.iterator().next());
        }
        return(null);
    }

    /**
     * Attempt to heuristically determine the Rating of a video item from the 521a subfield (if present)
     * 
     * @param record   the record being processed
     * @return  String  a normalized String representing the "Rating" of the video item
     */
    public String getVideoRating(Record record)
    {
        if (isVideo)
        {
            Set<String> target = indexer.removeTrailingPunct(record, "521a");
            if (target == null || target.size() == 0)
            {
                return ("None Listed");
            }
            String ratingString = target.iterator().next();
            String rating = getRating(ratingString);
            return(rating);
        }
        return(null);
    }

    /**
     * Routine that actually does the work to heuristically determine the Rating of a video item from the 521a subfield (if present)
     * 
     * @param ratingString   the value of the 521a subfield extracted from the record being processed
     * @return  String       a normalized String representing the "Rating" of the video item
     */
    private String getRating(String ratingString)
    {
        String rating = "Can't Determine";
        if (ratingString.matches(".*PG[- ]?13.*"))  rating = "Rated: PG-13";
        else if (ratingString.matches(".*TV[- ]?14.*"))  rating = "Rated: TV-14";
        else if (ratingString.matches(".*TV[- ]?G.*"))  rating = "Rated: TV-G";
        else if (ratingString.matches(".*TV[- ]?PG.*"))  rating = "Rated: TV-PG";
        else if (ratingString.matches(".*TV[- ]?Y7.*"))  rating = "Rated: TV-Y7";
        else if (ratingString.matches(".*TV[- ]?Y.*"))  rating = "Rated: TV-Y";
        else if (ratingString.matches(".*TV[- ]?MA.*"))  rating = "Rated: TV-MA";
        else if (ratingString.matches(".*NC[- ]?17.*"))  rating = "Rated: NC-17";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*UR.*"))  rating = "Unrated";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*NR.*"))  rating = "Not Rated";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*13([ ]?(and )?)[Uu][Pp].*"))  rating = "Rated: PG-13";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*X[^A-Za-z].*"))  rating = "Rated: X ";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*M[^A-Za-z].*"))  rating = "Rated: M";
        else if (ratingString.matches(".*[Rr]at(ed|ing)?[^A-Za-z]*R[^A-Za-z].*"))  rating = "Rated: R";
        else if (ratingString.matches(".*[Rr]at(ed|ing)?[^A-Za-z]*R"))  rating = "Rated: R";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*PG.*"))  rating = "Rated: PG";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*18.*"))  rating = "Rated: 18+ years";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*T[^A-Za-z].*"))  rating = "Rated: T";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*T"))  rating = "Rated: T";
        else if (ratingString.matches(".*R [Rr]at(ed|ing).*"))  rating = "Rated: R";
        else if (ratingString.matches(".*: R[^A-Za-z].*"))  rating = "Rated: R";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*G[^A-Za-z].*"))  rating = "Rated: G";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*U[^A-Za-z].*"))  rating = "Rated: G";
        else if (ratingString.matches(".*[Rr]at(ed|ing)[^A-Za-z]*G"))  rating = "Rated: G";
        else if (ratingString.matches(".*[Uu]n[-]?rated.*"))  rating = "Unrated";
        else if (ratingString.matches(".*PG.*"))  rating = "Rated: PG";
        else if (ratingString.matches(".*NR.*"))  rating = "Not Rated";
        else if (ratingString.matches(".*[Nn]ot [Rr]ated.*"))  rating = "Not Rated";
        else if (ratingString.matches(".*[Gg]rade[s]?( level)?[^0-9A-Za-z]*[K0-9]+.*"))  
        {
            rating = ratingString.replaceAll(".*[Gg]rade[s]?( level)?[^0-9A-Za-z]*([K0-9]+).*", "Rated: $2+ grade" );
        }
        else if (ratingString.matches(".*[Nn]o[t]? (be )?[Rr]ecom[m]?[ea]nd[^0-9]*[0-9]+.*"))  
        {
            rating = ratingString.replaceAll(".*[Nn]o[t]? (be )?[Rr]ecom[m]?[ea]nd[^0-9]*([0-9]+).*", "Rated: $2+ years" );
        }
        else if (ratingString.matches(".*([Ss]uitable|[Rr]ecommended|[Ii]tended|[Ss]uggested).*[Ff]or[^0-9]*[0-9]+.*"))  
        {
            rating = ratingString.replaceAll(".*([Ss]uitable|[Rr]ecommended|[Ii]tended|[Ss]uggested).*[Ff]or[^0-9]*([0-9]+).*", "Rated: $2+ years" );
        }
        else if (ratingString.matches(".*[Rr]estricted.*[Tt]o[^0-9]*[0-9]+.*"))  
        {
            rating = ratingString.replaceAll(".*[Rr]estricted.*[Tt]o[^0-9]*([0-9]+).*", "Rated: $1+ years" );
        }
        else if (ratingString.matches(".*[Mm]ayores [Dd]e[^0-9]*[0-9]+.*"))  
        {
            rating = ratingString.replaceAll(".*[Mm]ayores [Dd]e[^0-9]*([0-9]+).*", "Rated: $1+ years" );
        }
        else if (ratingString.matches(".*[Ff]reigegeben [Aa]b[^0-9]*[0-9]+.*"))  
        {
            rating = ratingString.replaceAll(".*[Ff]reigegeben [Aa]b[^0-9]*([0-9]+).*", "Rated: $1+ years" );
        }
        else if (ratingString.matches(".*[Jj]unior.*([Hh]igh.*|[Aa]dult|[Cc]ollege).*"))  rating = "Rated: Junior High+";
        else if (ratingString.matches(".*[Hh]igh.*([Ss]chool|[Aa]dult|[Cc]ollege).*"))  rating = "Rated: High School+";
        else if (ratingString.matches(".*([Cc]ollege).*"))  rating = "Rated: College+";
        else if (ratingString.matches(".*[Oo]ver [0-9]+ [Yy]ears.*"))  
        {
            rating = ratingString.replaceAll(".*[Oo]ver ([0-9]+) [Yy]ears.*", "Rated: $1+ years" );
        }
        else if (ratingString.matches(".*[Aa]ge[sd]? [0-9]+.*([Aa]bove|[Aa]dult|[Oo]lder|[Oo]ver|[Uu]p).*"))  
        {
            rating = ratingString.replaceAll(".*[Aa]ge[sd]? ([0-9]+).*", "Rated: $1+ years" );
        }
        else if (ratingString.matches("[0-9]+(years)?.*([Aa]dult|[Oo]lder|[Oo]ver|[Uu]p).*"))  
        {
            rating = ratingString.replaceAll("([0-9]+).*", "Rated: $1+ years" );
        }
        else if (ratingString.matches(".* [0-9]+(years)?.*([^A-Za-z])([Aa]dult|[Oo]lder|[Oo]ver|[Uu]p).*"))  
        {
            rating = ratingString.replaceAll(".* ([0-9]+).*", "Rated: $1+ years" );
        }
        else if (ratingString.matches(".*[Ss]uggested[^0-9]*[0-9]*[+]"))  
        {
            rating = ratingString.replaceAll(".*[Ss]uggested[^0-9]*([0-9]+).*", "Rated: $1+ years" );
        }
        else if (ratingString.matches("^General.*"))  rating = "Rated: G";
        else if (ratingString.matches("^Ge.?ne.?ral.*"))  rating = "Rated: G";
        else if (ratingString.matches(".*: General.*"))  rating = "Rated: G";
        else if (ratingString.matches(".*Universal.*"))  rating = "Rated: G";
        else if (ratingString.matches(".*[Pp]er [Tt]utti.*"))  rating = "Rated: G";
        else if (ratingString.matches("^[\"]?Restricted.*"))  rating = "Rated: R";
        else if (ratingString.matches("^R[^A-Za-z].*"))  rating = "Rated: R";
        else if (ratingString.matches(".*(:|[Ff]or) [Aa]dult.*"))  rating = "Rated: Adult";
        else if (ratingString.matches("^Adult.*"))  rating = "Rated: Adult";
        else if (ratingString.matches("^Mature.*"))  rating = "Rated: Mature";
        else if (ratingString.matches(".*(:|[Ff]or) [Mm]ature.*"))  rating = "Rated: Mature";
        else if (ratingString.matches(".*14A.*"))  rating = "Rated: 14A";
        else if (ratingString.matches(".*15A.*"))  rating = "Rated: 15A";
        else if (ratingString.matches(".*18A.*"))  rating = "Rated: 18A";
        else if (ratingString.matches(".*[0-9]+.*([Uu]p|[Oo]lder|[+])A.*"))  
        {
            rating = ratingString.replaceAll(".*([0-9]+).*75k", "Rated: $1+ years" );
        }
        else if (ratingString.matches(".*[Vv]iewer [Dd]iscretion.*"))  rating = "Discretion Advised";
        else if (ratingString.matches(".*[Pp]arental ([Dd]iscretion|[Gg]uidance).*"))  rating = "Discretion Advised";
        else if (ratingString.matches(".*[Ss]uitable.*[Ff]or.*([Gg]eneral|[Aa]ll).*") &&
                 !ratingString.matches("[Nn]ot.*[Ss]uitable.*[Ff]or.*([Gg]eneral|[Aa]ll).*"))  rating = "Rated: G";
        else if (ratingString.matches(".*[Aa]ll [Aa]ge[s]?.*"))  rating = "Rated: G";
        else if (ratingString.matches(".*G [Rr]at(ed|ing).*"))  rating = "Rated: G";

        return(rating);
    }

    /**
     * Attempt to heuristically determine the Director of a video item based on looking in the 245c, subfield, the 508a subfield and 700 fields
     * 
     * @param record   the record being processed
     * @return  Set<String>  a set of Strings representing the name(s) of the director(s) of the video  (or an empty set, if none can be found)
     */
    public Set<String> getVideoDirector(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        if (isVideo)
        {
            String responsibility = indexer.getFirstFieldVal(record, null, "245c");
            if (responsibility != null)
            {
                Set<String> directors = getVideoDirectorsFromTextField(responsibility);
                result.addAll(directors);
            }
            Set<String> credits = indexer.getFieldList(record, "508a");
            for (String credit : credits)
            {
                Set<String> directors = getVideoDirectorsFromTextField(credit);
                result.addAll(directors);
            }
            List<VariableField> personalNames = record.getVariableFields("700");
            for (VariableField vf : personalNames)
            {
                DataField df = (DataField)vf;
                // this could be overly broad, and could also grab music directors or other roles containing the word "director"
                if (ChkSubfield(df, '4', "drt") || ChkSubfield(df, 'c', ".*director.*") || ChkSubfield(df, 'e', ".*direct.*"))
                {
                    String name = df.getSubfield('a').getData();
                    name = Utils.cleanData(name);
                    name = name.replaceAll("([A-Z][^,]*),[ ]?(.*)", "$2 $1");
                    result.add(name);
                }
            }
        }
        return(result);
    }
    
    private boolean ChkSubfield(DataField df, char c, String pattern)
    {
        List<Subfield> sfs = df.getSubfields(c);
        for (Subfield sf : sfs)
        {
            if (sf.getData().matches(pattern))  return(true);
        }
        return false;
    }

    /**
     * Routine that actually does the work to heuristically determine the Director of a video based on the text of the string passed in
     * 
     * @param ratingString   the value of 245c subfield or a 508a subfield extracted from the record being processed
     * @return  Set<String>  a set of Strings representing the name(s) of the director(s) of the video  (or an empty set, if none can be found)
     */
    public static Set<String> getVideoDirectorsFromTextField(String responsibility)
    {
        // First do some initital processing on the passed in string
        Set<String> result = new LinkedHashSet<String>();
        Set<String> squeezedresult = new LinkedHashSet<String>();
        String responsibility1 = responsibility.replaceAll("\\[sic[.]?[]]", "");
        responsibility1 = responsibility1.replaceAll("([a-z][a-z][a-z])[.]", "$1;");
        responsibility1 = responsibility1.replaceAll("([a-z][a-z])[.]  ", "$1;  ");
        responsibility1 = responsibility1.replaceAll("Dirigido", "Director");
        if (!responsibility1.equals(responsibility))
        {
            responsibility = responsibility1;
        }
        // Now split the string into subparts separated by ;  (or -- or  : )
        String semiparts[] = responsibility.split(";|--| : ");
        boolean respHasDirected = responsibility.matches(".*[Dd]irect(ed|or|ion).*");
        for (String part : semiparts)
        {
            part = part.trim();
            if (part.matches(".*[Dd]irect(ed|or|ion).*") || (!respHasDirected && part.matches(".*a film by.*")))
            {
                String trimmed;
            //    part = part.replaceAll("\\[sic[.][]]", "");
                String part1 = part.replaceAll("[ ]?[(][^)]*[)]", "");
                if (!part.equals(part1))
                {
                    part = part1;
                }
                if (part.matches(".*[Dd]irector for .*"))
                {
                    part = part.replaceFirst("[Dd]irector for [A-Za-z ]*", "director");
                }
                //  Pattern matching when the subpart is of the form:   Some Name Director
                if (part.matches(".*[Dd]irector[^A-Z]*") )
                {
                    if (part.matches(".*([Aa]rt(istic)?|[Mm]usic(al)?|[Ss]tage|[Pp]roduction|[Pp]roject|[Pp]hotography|[Aa]nimation|[Mm]edical|[Cc]asting|[Tt]echnical) [Dd]irector.*" ) ||
                            part.matches(".*[Dd]irector[s]? ((of[ ]?(([Pp]hotography)))|(de (la )?fotografia)).*"))
                        continue;
                    part = part.replaceAll(" *[\\[]", ", ");
                    part = part.replaceAll("[\\]]", "");
                    part = part.replaceAll("director.*", "director");
                    part = part.replaceAll(" [-A-Za-z/]*director[-A-Za-z]*", " director");
                    part = part.replaceAll(" [a-z/]+/director", " director");
                    part = part.replaceAll(" co-director", " director");
                    part = part.replaceAll(" [a-z ,]+ director", " director");
                    part = part.replaceAll(".*: (([A-Z][a-z.]*)+)(, )?director", "$1, director");
                    part = part.replaceFirst(".* (of|by)", "by");
                    part = part.replaceAll(" (and) ", " & ");
                    part = part.replaceFirst("by ", "");
                    part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "* $1");
                    part = part.replaceFirst("[,]?[ ]?director", "");
                    part = part.replaceAll("([,][ ]?|[ ]?&[ ]?)", "|");
                    part = part.replaceAll("[*]", ",");

                    String commaparts[] = part.split("[|]+");
                    for (String subpart : commaparts)
                    {
                        addCleanedName(result, squeezedresult, subpart);
                    }
                }
                //  Pattern matching when the subpart is of the form:   Directed by Some Name 
                else if (part.matches(".*[Dd]irect(ed|ion).*?by.*")|| part.matches(".*a film by.*"))
                {
                    part = part.replaceFirst(".*[Dd]irect(ed|ion).*?by[]:,)]? ", "directified by ");
                    part = part.replaceFirst(".*a film by", "directified by ");
                    part = part.replaceAll("[]]", "");
                    part = part.replaceFirst("et al", "");
                    part = part.replaceAll(" (and|with|et) ", " & ");
                    part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "* $1");
                    part = part.replaceAll("([A-Z][^ .][^ .][^ .]+)[.].*", "$1");
                    part = part.replaceAll("brothers", "Brothers");
                    part = part.replaceAll("directified by[ ]*(([\"]?([A-Z]|\\p{Lu})[^ ]+[\"]?[,]?[ ]*|[ ]?&[ ]|von |van |de[rl]?[ ]?|the |d[']|al-)+).*", "$1");
                    part = part.replaceAll("^([A-Z][^ .]+) & ([A-Z][^ .,]+) ([A-Z][^ .,]+)", "$1 $3 & $2 $3");
                    part = part.replaceAll("([,][ ]?|[ ]?&[ ]?)", "|");
                    part = part.replaceAll("[*]", ",");
                    part = part.replaceAll("[ ][ ]+", " ");
                    String commaparts[] = part.split("[|]+");
                    for (String subpart : commaparts)
                    {
                        addCleanedName(result, squeezedresult, subpart);
                    }
                }
                //  Pattern matching when the subpart is of the form:   Director Some Name 
                else if (part.matches(".*[Dd]irector[^a-rt-z\'].*[A-Z].*"))
                {
                    if (part.matches(".*([Aa]rt(istic)?|[Mm]usic(al)?|[Ss]tage|[Pp]roduction|[Pp]roject|[Pp]hotography|[Aa]nimation|[Mm]edical|[Cc]asting|[Tt]echnical) [Dd]irector.*" ) ||
                            part.matches(".*[Dd]irector[s]? ((of[ ]?(([Pp]hotography)))|(de la fotografia)).*"))
                        continue;
                    part = part.replaceFirst("Writer", "writer");
                    part = part.replaceFirst("Producer", "producer");
                    part = part.replaceFirst("Researcher", "researcher");
                    part = part.replaceFirst(".*[Dd]irector", "director");
                    part = part.replaceAll("[ ]?([.][.][.])?[ ]?[\\[][^\\]]*[\\]]", "");
                    part = part.replaceAll("[]]", "");
                    part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "* $1");
                    part = part.replaceAll("director[-A-Za-z/]*", "director");
                    part = part.replaceAll("director (for|and|of)( [A-Z][A-Za-z]*)+", "director");
                    part = part.replaceFirst("director[^A-Z]*", "director= ");
                    part = part.replaceFirst("with the [a-z][A-Za-z ]*", "");
                    part = part.replaceFirst("et al", "");
                    part = part.replaceAll(" (and|with|et) ", " & ");
                    part = part.replaceAll(",[ ]?[a-z].*", "");
                    part = part.replaceAll("= [^(]*[)], ", ": ");
                    part = part.replaceFirst("director= ", "");
                    part = part.replaceAll("^([A-Z][^ .]+) & ([A-Z][^ .]+) ([A-Z][^ .]+)", "$1 $3 & $2 $3");
                    part = part.replaceAll("([,][ ]?|[ ]?&[ ]?)", "|");
                    part = part.replaceAll("[*]", ",");
                    part = part.replaceAll("[ ][ ]+", " ");
                    String commaparts[] = part.split("[|]+");
                    for (String subpart : commaparts)
                    {
                        addCleanedName(result, squeezedresult, subpart);
                    }
                }
                //  Pattern matching when the subpart is of the form:   Direction Some Name 
                else if (part.matches(".*[Dd]irection.*"))
                {
                    if (part.matches(".*([Aa]rt|[Mm]usic(al)?|[Ss]tage|[Pp]roject|[Aa]nimation|[Mm]edical|[Cc]asting|[Tt]echnical) [Dd]irectoion.*" ))
                        continue;

                    part = part.replaceFirst(".*[Dd]irection[^A-Z]*", "direction: ");
                    part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "* $1");
                    part = part.replaceAll(" (and|with|et) ", " & ");
                    part = part.replaceAll("[\\]]", "");
                    part = part.replaceAll("([,][ ]?|[ ]?&[ ]?)", "|");
                    part = part.replaceAll("[*]", ",");
                    part = part.replaceAll("[ ][ ]+", " ");
                    part = part.replaceFirst("direction: ", "");
                    String commaparts[] = part.split("[|]+");
                    for (String subpart : commaparts)
                    {
                        addCleanedName(result, squeezedresult, subpart);
                    }
                }
            }
        }
        return(result);
    }

    private static void addCleanedName(Set<String> result, Set<String> squeezedresult, String subpart)
    {
        subpart = nameClean(subpart);
        if (subpart == null) return;
        String squeezedpart = subpart.replaceAll(" ", "");
        if (!squeezedresult.contains(squeezedpart))
        {
            squeezedresult.add(squeezedpart);
            result.add(subpart);
        }
    }

    private static String nameClean(String subpart)
    {
        if (subpart.matches(".*(.*)"));
            subpart = subpart.replaceAll("(.*)[(].*[)]", "$1");
        if (subpart.matches(".* for .*"))
            subpart = subpart.replaceAll("(.*) for .*", "$1");
        if (subpart.matches(".*, Inc[.]"))
            return(null);
        if (subpart.matches(".*( of | a | in ).*"))
            return(null);
        if (subpart.matches(".*? [a-z][a-z ]*"))
            subpart = subpart.replaceAll("(.*?) [a-z][a-z ]*", "$1");
        if (subpart.matches("[a-z]*"))
            return(null);
        if (subpart.matches(".*[ .]+$"))
            subpart = subpart.replaceAll("(.*?)[ .][ .]+$", "$1");
        if (subpart.equalsIgnoreCase("Writer")) return(null);
        if (subpart.equalsIgnoreCase("Editor")) return(null);
        if (subpart.equalsIgnoreCase("Executive")) return(null);
        if (subpart.equalsIgnoreCase("Story")) return(null);
        if (subpart.matches(".*[Pp]roducer.*")) return(null);
        if (subpart.matches("[Dd]irector[s]?")) return(null);
        if (subpart.equalsIgnoreCase("Screenplay")) return(null);
        if (subpart.contains(":")|| subpart.replaceAll("[^ ]", "").length() > 5)
            return(null);
        subpart = Utils.cleanData(subpart);
        if (subpart.length() == 0) return(null);
        return(subpart);
    }

    /**
     * Attempt to heuristically determine the original release of a video item based on 500a, subfield
     * 
     * @param record    the record being processed
     * @return  String  a String representing the original of the video  (or an empty set, if none can be found)
     */
    public String getOriginalReleaseDate(Record record)
    {
        if (releaseDatePattern == null)
        {
            releaseDatePattern = Pattern.compile(".*?([Rr]eleased|[Rr]elease [Oo]f|[Vv]ideorecording|[Vv]ideocassette|[Ii]ssued|[Rr]ecorded|[Bb]roadcast|[Ff]ilmed|[Ee]dited|[Pp]roduced|[Mm]ade|[Dd]elivered).*?[^0-9]([0-9][0-9][0-9][0-9])([^0-9].*)?$");
        }
        if (isVideo)
        {
            String date008 = indexer.getFirstFieldVal(record, null, "008[11-14]");
            Set<String> notesFields = indexer.getFieldList(record, "500a");
            String date500 = null;
            for (String note : notesFields)
            {
                Matcher match = releaseDatePattern.matcher(note);
                if (match.matches()) 
                {
                    date500 = match.group(2);
                    break;
                }
            }
            String datePub = indexer.getPublicationDate(record);
            boolean validDatePub = false;
            int iPub = 0;
            if (datePub != null && datePub.matches("[1-2][0189][0-9][0-9]"))
            {
                validDatePub = true;
                iPub = Integer.parseInt(datePub);
            }
            String dateReturn = null;
            if (date008 != null && date500 != null)
            {
                boolean m008 = date008.matches("[1-2][0189][0-9][0-9]");
                boolean m500 = date500.matches("[1-2][0189][0-9][0-9]");
                if (m008 && m500) 
                {
                    int i008 = Integer.parseInt(date008);
                    int i500 = Integer.parseInt(date500);
                    if (i008 <= i500) 
                        dateReturn = date008;
                    else if (i008 > i500)
                        dateReturn = date500;
                }
                else if (m008) 
                {
                    dateReturn = date008;
                }
                else if (m500) 
                {
                    dateReturn = date500;
                }
            }
            else if (date008 != null && date008.matches("[1-2][0189][0-9][0-9]"))
            {
                dateReturn = date008;
            }
            else if (date500 != null && date500.matches("[1-2][0189][0-9][0-9]"))
            {
                dateReturn = date500;
            }
            if (dateReturn != null)
            {
                int iReturn = Integer.parseInt(dateReturn);
                if (validDatePub && iPub < iReturn)
                {
                    dateReturn = datePub;
                }
            }
            else if (validDatePub)
            {
                dateReturn = datePub;
            }
            return(dateReturn);
        }
        return(null);
    }
    
    /**
     * Attempt to heuristically determine the "genre" of a video item based on 650 and 655 fields
     * 
     * @param record    the record being processed
     * @return  String  a String representing the original of the video  (or an empty set, if none can be found)
     */
    public static Pattern genreActionAdventure = Pattern.compile("(^|[^a-z])(action|adventure|espionage|martial arts|samurai|spies|spy thriller|bond, james)([^a-z]|$)");
    public static Pattern genreAnimation = Pattern.compile("(^|[^a-z])(animated|animation)([^a-z]|$)");
    public static Pattern genreBiography = Pattern.compile("(^|[^a-z])(biograph(ical|y))([^a-z]|$)");
    public static Pattern genreChildren = Pattern.compile("(^|[^a-z])(children('s)?)([^a-z]|$)");
    public static Pattern genreComedy = Pattern.compile("(^|[^a-z])(comed(y|ies)|humor)([^a-z]|$)");
    public static Pattern genreCrimeMystery = Pattern.compile("(^|[^a-z])(assassins|cop|crime|criminal[s]?|detective[s]?|fugitives|gangster[s]?|investigation|kidnapping|legal|murder(ers)?|mystery|police|prison[s]?|robbery|suspense|swindlers|thrillers|thieves)([^a-z]|$)");
    public static Pattern genreDocumentary = Pattern.compile("(^|[^a-z])(documentary|newsreels)([^a-z]|$)");
    public static Pattern genreDrama = Pattern.compile("(^|[^a-z])(drama|melodrama[s]?)([^a-z]|$)");
    public static Pattern genreExperimental = Pattern.compile("(^|[^a-z])(experimental|performance art|video art)([^a-z]|$)");
    public static Pattern genreFilmNoir = Pattern.compile("(^|[^a-z])(noir)([^a-z]|$)");
    public static Pattern genreHistorical = Pattern.compile("(^|[^a-z])(apartheid|civil rights|historical|history|holocaust)([^a-z]|$)");
    public static Pattern genreHorror = Pattern.compile("(^|[^a-z])(ghost[s]?|horror|monster[s]?|supernatural|vampire[s]?|zombie[s]?)([^a-z]|$)");
    public static Pattern genreMusical = Pattern.compile("(^|[^a-z])(blues|concert[s]?|jazz|music|musical|musicals|operas|rock)([^a-z]|$)");
    public static Pattern genreRomance = Pattern.compile("(^|[^a-z])(love|roman(tic|ce))([^a-z]|$)");
    public static Pattern genreSciFiFantasy = Pattern.compile("(^|[^a-z])(alien|fantasy|interplanetary|planets|science fiction|time travel)([^a-z]|$)");
    public static Pattern genreTelevision = Pattern.compile("(^|[^a-z])(television|tv)([^a-z]|$)");
    public static Pattern genreWar = Pattern.compile("(^|[^a-z])(war|warfare|bomb|pearl harbor)([^a-z]|$)");
    public static Pattern genreWestern = Pattern.compile("(^|[^a-z])(western)([^a-z]|$)");
    
    public static Object[][] genreMap ={{genreActionAdventure, "Action/Adventure"}, {genreAnimation, "Animation"}, {genreBiography, "Biography"},
                                        {genreChildren, "Children/Family"}, {genreComedy , "Comedy"}, {genreCrimeMystery, "Crime/Mystery"},
                                        {genreDocumentary, "Documentary"}, {genreDrama, "Drama" }, { genreExperimental, "Experimental" },
                                        {genreFilmNoir, "Film Noir"}, { genreHistorical, "Historical" }, {genreHorror, "Horror"},
                                        {genreMusical, "Music/Musical"}, {genreRomance, "Romance"}, {genreSciFiFantasy, "SciFi/Fantasy"},
                                        {genreTelevision, "Television"}, {genreWar, "War"}, {genreWestern, "Western" }};
    
    public Set<String> getVideoGenre(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        if (isVideo)
        {
            Set<String> subjectFields = indexer.getFieldList(record, "650a:655a");
            for (String subject : subjectFields)
            {
                subject = subject.toLowerCase();
                for (Object[] mapEntry : genreMap)
                {
                    Pattern patternToMatch = (Pattern) mapEntry[0];
                    String valueToAssign = (String) mapEntry[1];
                    if (patternToMatch.matcher(subject).find()) 
                    {
                        result.add(valueToAssign);
                    }
                }
            }
        }
        return(result);
    }

}

