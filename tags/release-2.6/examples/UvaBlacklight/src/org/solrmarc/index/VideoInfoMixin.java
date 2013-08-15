package org.solrmarc.index;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.ErrorHandler;
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
        Set<String> formats = indexer.getFormat(record);
        isVideo = (Utils.setItemContains(formats, "Video"));
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
                Set<String> directors = getVideoDirectorsFromTextField(responsibility, true);
                result.addAll(directors);
            }
            Set<String> credits = indexer.getFieldList(record, "508a");
            for (String credit : credits)
            {
                Set<String> directors = getVideoDirectorsFromTextField(credit, true);
                result.addAll(directors);
            }
            if (result.size() == 0)
            {
                Set<String> notes = indexer.getFieldList(record, "500a:505a:505t");
                for (String note : notes)
                {
                    if (note.contains("direct") || note.contains("Direct"))
                    {
                        Set<String> directors = getVideoDirectorsFromTextField(note, false);
                        result.addAll(directors);
                    }
                }
                String subtitle = indexer.getFirstFieldVal(record, null, "245b");
                if (subtitle != null && (subtitle.contains("direct") || subtitle.contains("Direct")))
                {
                    
                    if (indexer.getErrorHandler() != null)
                    {
                        indexer.getErrorHandler().addError(record.getControlNumber(), "245", "b", ErrorHandler.MINOR_ERROR, 
                                                           "Director information erroneously included in the 245b subtitle field");
                    }
                    Set<String> directors = getVideoDirectorsFromTextField(subtitle, false);
                    result.addAll(directors);
                }
                String medium = indexer.getFirstFieldVal(record, null, "245h");
                if (medium != null && (medium.contains("direct") || medium.contains("Direct")))
                {
                    
                    if (indexer.getErrorHandler() != null)
                    {
                        indexer.getErrorHandler().addError(record.getControlNumber(), "245", "h", ErrorHandler.MINOR_ERROR, 
                                                           "Director information erroneously included in the 245h medium field");
                    }
                    Set<String> directors = getVideoDirectorsFromTextField(medium, false);
                    result.addAll(directors);
                }

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
    public static Set<String> getVideoDirectorsFromTextField(String responsibility, boolean greedy)
    {
        // First do some initital processing on the passed in string
        boolean reverseName = false;
        Set<String> result = new LinkedHashSet<String>();
        Set<String> squeezedresult = new LinkedHashSet<String>();
         responsibility = responsibility.replaceAll("\\[sic[.]?[]]", "");
        responsibility = responsibility.replaceAll("([a-z][a-z][a-z])[.]", "$1;");
        responsibility = responsibility.replaceAll("([a-z][a-z])[.]  ", "$1;  ");
        responsibility = responsibility.replaceAll("direção de produção", "producer");//porteguese
        responsibility = responsibility.replaceAll("direct(or|ion|eur) de (la )?[ ]?produc[ct]i(ó|o)n", "producer");//french/spanish
        responsibility = responsibility.replaceAll("direcci(ó|o)n art\\B*", "artguy");//spanish
        responsibility = responsibility.replaceAll("produit par\\b", "produced by");

        String responsibility1 = responsibility.replaceAll("[Rr]eg(i|í)[ea]?\\b", "didrector");//german/italian/swedish
        responsibility1 = responsibility1.replaceAll("[Rr]eggia\\b", "didrector");//spanish
        responsibility1 = responsibility1.replaceAll("[Rr]e(ž|z)ie", "didrector");//czech
        responsibility1 = responsibility1.replaceAll("[Rr]e(ż|z|a)yseria", "didrector");//polish
        responsibility1 = responsibility1.replaceAll("[Dd]irecci(ó|o)n", "didrector");//spanish
        responsibility1 = responsibility1.replaceAll("[Rr](é|e)alisation", "didrection");//french
        responsibility1 = responsibility1.replaceAll("[Rr]ealizaci(ó|o)n", "didrection");//spanish
        responsibility1 = responsibility1.replaceAll("[Rr]éalisé( et [a-z]*)? (par|by)", "didrected$1 by");//french
        responsibility1 = responsibility1.replaceAll("[Dd]irected por", "didrected by");//spanish
        responsibility1 = responsibility1.replaceAll("[Dd]irigé par", "didrected by");//french
        responsibility1 = responsibility1.replaceAll("[Dd]irreción", "didrection");//spanish
        responsibility1 = responsibility1.replaceAll("[Dd]ireccíon", "didrection");//spanish
        responsibility1 = responsibility1.replaceAll("[Dd]irector(a|es)", "didrector");//spanish
        responsibility1 = responsibility1.replaceAll("[Dd]ire(ç|c)ão", "didrector");//porteguese
        responsibility1 = responsibility1.replaceAll("[Dd]iretto da", "didrector");//italian
        responsibility1 = responsibility1.replaceAll("[Dd]irecteur", "didrector");//french
        responsibility1 = responsibility1.replaceAll("[Dd]irect(e|io)r", "didrector");//typo
        responsibility1 = responsibility1.replaceAll("[Dd]irigid[oa]", "didrector");//porteguese
        responsibility1 = responsibility1.replaceAll("[Tt]asriṭ u-vimui", "didrector");//hebrew
        responsibility1 = responsibility1.replaceAll("[Ii]khr(ā|a)j", "didrector");//arabic
        responsibility1 = responsibility1.replaceAll("[Rr]ezhisser[a]?", "didrector");//russian
        responsibility1 = responsibility1.replaceAll("[Yy]öneten", "didrector");//turkish
        responsibility1 = responsibility1.replaceAll("[Nn]irdeśaka", "didrector");//hindi
        responsibility1 = responsibility1.replaceAll("[Pp]ostanovka", "didrector");//russian
        responsibility1 = responsibility1.replaceAll("(un )?film[e]? d[ei]\\b", "a flim by");//french
        responsibility1 = responsibility1.replaceAll("un film d'", "a flim by ");//french
        responsibility1 = responsibility1.replaceAll("(an|en) film av", "a flim by"); //swedish
        responsibility1 = responsibility1.replaceAll("[Ee]in [Ff]ilm von", "a flim by"); //german
        responsibility1 = responsibility1.replaceAll("un(e|a) pel(í|i)cula de", "a flim by");//spanish
        responsibility1 = responsibility1.replaceAll("[Mm]is[e]? en sc(e|è)ne( de)?", "a flim by");//french
        responsibility1 = responsibility1.replaceAll("Film by", "a flim by");
        //responsibility1 = responsibility1.replaceAll("\\bpar\\b", "maybe by");
        //responsibility1 = responsibility1.replaceAll("^by", "maybe by");
        //responsibility1 = responsibility1.replaceAll("^von", "maybe by");
//        if (!responsibility1.equals(responsibility))
//        {
//            responsibility = responsibility1;
//        }
        responsibility1 = handleASoAndSoFilm(responsibility1);

        responsibility1 = responsibility1.replaceAll("[Dd]ao yan", "didrector");//chinese
        responsibility1 = responsibility1.replaceAll("[Kk]antoku", "didrector");//japanese
        responsibility1 = responsibility1.replaceAll("[Kk]amdok", "didrector");//korean
        responsibility1 = responsibility1.replaceAll("[Yy]ŏnchʻul", "didrector");//korean
        if (!responsibility1.equals(responsibility))
        {
            responsibility = responsibility1;
        }

        // Now split the string into subparts separated by ;  (or -- or  : )
        
        for (int loop = 0; loop < 5; loop++)
        {
            if (loop == 3)
            {
                responsibility = responsibility.replaceAll("didrect", "direct"); 
                responsibility = responsibility.replaceAll("a flim by", "a film by"); 
                if (responsibility.matches(".*filmed[,a-z ]*by\\b.*"))
                {
                    responsibility = responsibility.replaceAll("filmed", "directed");
                }            
            }
            String semiparts[] = responsibility.split(";|--| : ");
            for (String part0 : semiparts)
            {
                String part = part0;
                part = part.trim();
                if (((loop == 0 || loop == 3) && part.matches(".*[Dd]irect(ed|or[s]?|ion)\\b.*")) ||
                    ((loop == 1 || loop == 4) && part.matches(".*a film by.*"))) // ||
         //           (loop == 5 && part.matches(".*maybe by.*")) )
                {
                    String trimmed;
                //    part = part.replaceAll("\\[sic[.][]]", "");
                    if (!greedy && part.matches(".*[Dd]irector[']?s.*"))
                    {
                        continue;
                    }
                    if (!greedy && part.matches(".*\"[^,\"]*[Dd]irect(ed by|or[s]?|ion)[^,\"]*\".*"))
                    {
                        part = part.replaceAll("\"[^\",]*?\"", "XXX");
                    }
                    
                    // Try to split apart "brothers" ie.  the Hughes Brothers  becomes  Albert Hughes and Allen Hughes
                    if (part.matches(".*(the )?[A-Z][^ ]* [Bb]rothers.*"))
                    {
                        String name = part.replaceFirst(".*(the )?([A-Z][^ ]*) [Bb]rothers.*", "$2");
                        if (part.matches(".*(the )?[A-Z][^ ]* [Bb]rothers(, | \\()[A-Z][a-z]* and [A-Z][a-z]* "+name+".*"))
                        {
                            part = part.replaceFirst("((the )?([A-Z][^ ]*) [Bb]rothers)(, | \\()([A-Z][a-z]*) and ([A-Z][a-z]*) "+name+"\\)?", "$5 $3 and $6 $3");
                        }
                        else if (part.matches(".*(the )?[A-Z][^ ]* [Bb]rothers(, | \\()[A-Z][a-z]* and [A-Z][a-z]*.*"))
                        {
                            part = part.replaceFirst("((the )?([A-Z][^ ]*) [Bb]rothers)(, | \\()([A-Z][a-z]*) and ([A-Z][a-z]*)\\)?", "$5 $3 and $6 $3");
                        }
                        else 
                        {
                            for (String otherPart : semiparts)
                            {
                                if (otherPart.equals(part0)) continue;
                                if (otherPart.matches(".*[A-Z][^ ]* "+name+".*[A-Z][^ ]* "+name+".*"))
                                {
                                    String names = otherPart.replaceFirst(".*([A-Z]([a-z]*|[.]) )+"+name+"[, ].*([A-Z]([a-z]*|[.]) )+"+name+"[^A-Za-z].*",
                                                                        "$1"+name+" and $3"+name);
                                    part = part.replaceFirst("(.*?)([Tt]he )?([A-Z][^ ]*) [Bb]rothers(.*)", "$1"+names+"$4");
                                    break;
                                }
                            }
                        }
                    }
                    String part1 = part.replaceAll("[ ]?[(][ ]*([a-z]*[/]|assistant )?[Dd]irector([/][a-z]*)?[ ]*[)]", " director"); // change (director)  to director
                    part1 = part1.replaceAll("[ ]?[(][^)]*[)]", ""); // throw away parenthetical phrases
                    if (!part.equals(part1))
                    {
                        part = part1;
                    }
                    if (part.matches(".*[Dd]irector[s]? (of|de|de la) ([Pp]h|[Ff])otogra(ph|f)(y|(i|í)[ea]).*"))
                    {
                        part = part.replaceFirst("[Dd]irector[s]? (of|de|de la) ([Pp]h|[Ff])otogra(ph|f)(y|(i|í)[ea])", "cinematographer");
                    }
                    if (part.matches(".*[Dd]irector[s]? (of|de|de la) ([Aa]nimation).*"))
                    {
                        part = part.replaceFirst("[Dd]irector[s]? (of|de|de la) ([Aa]nimation)", "supervising animator");
                    }
                    else if (part.matches(".*, [Dd]irector[s]? (of|for) .*"))
                    {
                        part = part.replaceFirst("[Dd]irector[s]? (of|for)((( ([A-Z][a-z]*|the|of|and|a))+)[,]?)+", "director");
                    }
                    else if (part.matches(".*[Dd]irector[s]?( and writer)? (of|for) .*"))
                    {
                        part = part.replaceFirst("[Dd]irector[s]?( and writer)? (of|for) [A-Za-z' ]*", "director");
                    }
                    if (part.contains("collaboration") || part.contains("participation"))
                    {
                        part = part.replaceAll("(in collaboration with)|(with the collaboration of)|(with the participation of)", "with");
                    }
                    if (part.matches(".*[Dd]irector .* director.*"))
                    {
                        part = part.replaceAll(".*([Dd]irector .*)director.*", "$1");
                    }
                    //  Pattern matching when the subpart is of the form:   Some Name Director
                    if (part.matches(".*[Dd]irector[^A-Z]*") && !part.matches(".*of the [Dd]irector.*"))
                    {
                        if (part.matches(".*([Aa]rt(istic)?|[Mm]usic(al)?|[Ss]tage|[Pp]roduction|[Pp]roject|[Pp]hotography|[Aa]nimation|[Mm]edical|[Cc]asting|[Tt]echnical|[Dd]ance|[Ee]diting) [Dd]irector.*" ) ||
                                part.matches(".*[Dd]irector[s]? ((of[ ]?(([Pp]hotography)))|(de (la )?fotograf(i|í)a)|(de arte)).*"))
                            continue;
                        part = part.replaceAll(" *[\\[]", ", ");
                        part = part.replaceAll("^\"", "");
                        part = part.replaceAll("[\\]]", "");
                        part = part.replaceAll("director.*", "director");
                        part = part.replaceAll(" [-A-Za-z/]*director[-A-Za-z]*", " director");
                        part = part.replaceAll(" [a-z/]+/director", " director");
                        part = part.replaceAll(" co-director", " director");
                        part = part.replaceAll(" [a-z ,]+ director", " director");
                        if (greedy)
                            part = part.replaceAll(".*: (([A-Z][A-Za-z.]* )*[A-Z][A-Za-z.]*)(, |,| )director", "$1, director");
                        else
                            part = part.replaceAll(".*? (([A-Z][A-Za-z.]* )*[A-Z][A-Za-z.]*)(, )?director", "$1, director");

                        part = part.replaceFirst(".* (of|by)", "by");
                        part = part.replaceAll(" (and|und|/) ", " & ");
                        part = part.replaceFirst("by ", "");
                        part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "# $1");
                        part = part.replaceFirst("[,]?[ ]?director", "");
                        part = part.replaceAll("([,][ ]?|[ ]?&[ ]?)", "|");
                        part = part.replaceAll("[#]", ",");
    
                        String commaparts[] = part.split("[|]+");
                        for (String subpart : commaparts)
                        {
                            addCleanedName(result, squeezedresult, subpart, reverseName, greedy);
                        }
                    }
                    else if (!greedy && part.matches(".*, [Dd]irector (of|for|on).*"))
                    {
                        continue;
                    }
                    //  Pattern matching when the subpart is of the form:   Directed by Some Name 
                    else if (part.matches(".*[Dd]irect(ed|ion).*?by.*")|| part.matches(".*a film by.*") || part.matches(".*maybe by.*"))
                    {
                        if (part.matches(".*([Aa]rt(istic)?|[Mm]usic(al)?|[Ss]tage|[Pp]roduction|[Pp]roject|[Pp]hotographic|[Aa]nimation|[Mm]edical|[Cc]asting|[Tt]echnical|[Dd]ance|[Ee]diting) [Dd]irection.*?by.*" ) ||
                                part.matches(".*[Dd]irector[s]? ((of[ ]?(([Pp]hotography)))|(de (la )?fotograf(i|í)a)|(de arte)).*"))
                            continue;
                        part = part.replaceFirst(".*[Dd]irect(ed|ion).*?by[]:,)]?[ ]?", "directified by ");
                        part = part.replaceFirst(".*a film by", "directified by ");
                        part = part.replaceFirst(".*maybe by", "directified by ");
                        part = part.replaceAll("/", " & ");
                        part = part.replaceAll("\\[|\\]", "");
                        part = part.replaceFirst("et al", "");
                        part = part.replaceAll(" (and|with|et|und) ", " & ");
                        part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "# $1");
                        part = part.replaceAll("[.][.][.]", "");
                        part = part.replaceAll("([A-Z][^ .][^ .][^ .]+)[.].*", "$1");
                        part = part.replaceAll("brothers", "Brothers");
                        part = part.replaceAll("directified by[ a-z,]*(([\"]?([A-Z]|\\p{Lu}|[*ʻ]|\\p{M})[^ ]*[\"]?[,]?[ ]*|[ ]?&[ ]|zur |von |van |de[rl]?[ ]?|the |d[']|al-|da-)+).*", "$1");
                        part = part.replaceAll("^([A-Z][^ .]+) & ([A-Z][^ .,]+) ([A-Z][^ .,]+)", "$1 $3 & $2 $3");
                        part = part.replaceAll("([,][ ]?|[ ]?&[ ]?)", "|");
                        part = part.replaceAll("[#]", ",");
                        part = part.replaceAll("[ ][ ]+", " ");
                        part = part.replaceFirst("directified by", "");
                        String commaparts[] = part.split("[|]+");
                        for (String subpart : commaparts)
                        {
                            addCleanedName(result, squeezedresult, subpart, reverseName, greedy);
                        }
                    }
                    else if (part.matches(".*[Dd]irected.*") || part.matches(".*[Dd]irected( (and|&) [-a-z]*)?,.*"))
                    {
                        part = part.replaceFirst(".*[Dd]irected( (and|&) [-a-z]*),","directified by");  
                        part = part.replaceFirst(".*[Dd]irected[]:,)]?[ ]?", "directified by ");
                        part = part.replaceAll("/", " & ");
                        part = part.replaceAll("\\[|\\]", "");
                        part = part.replaceFirst("et al", "");
                        part = part.replaceAll(" (and|with|et|und) ", " & ");
                        part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "# $1");
                        part = part.replaceAll("[.][.][.]", "");
                        part = part.replaceAll("([A-Z][^ .][^ .][^ .]+)[.].*", "$1");
                        part = part.replaceAll("brothers", "Brothers");
                        part = part.replaceAll("directified by[ a-z,]*(([\"]?([A-Z]|\\p{Lu}|[*ʻ]|\\p{M})[^ ]*[\"]?[,]?[ ]*|[ ]?&[ ]|zur |von |van |de[rl]?[ ]?|the |d[']|al-|da-)+).*", "$1");
                        part = part.replaceAll("^([A-Z][^ .]+) & ([A-Z][^ .,]+) ([A-Z][^ .,]+)", "$1 $3 & $2 $3");
                        part = part.replaceAll("([,][ ]?|[ ]?&[ ]?)", "|");
                        part = part.replaceAll("[#]", ",");
                        part = part.replaceAll("[ ][ ]+", " ");
                        part = part.replaceFirst("directified by", "");
                        String commaparts[] = part.split("[|]+");
                        for (String subpart : commaparts)
                        {
                            addCleanedName(result, squeezedresult, subpart, reverseName, greedy);
                        }
                    }
                    //  Pattern matching when the subpart is of the form:   Director Some Name 
                    else if (part.matches(".*[Dd]irector[^a-rt-z\'].*[A-Z].*"))
                    {
                        if (part.matches(".*([Aa]rt(istic)?|[Mm]usic(al)?|[Ss]tage|[Pp]roduction|[Pp]roject|[Pp]hotography|[Aa]nimation|[Mm]edical|[Cc]asting|[Tt]echnical|[Dd]ance|[Ee]diting) [Dd]irector.*" ) ||
                                part.matches(".*[Dd]irector[s]? ((of[ ]?(([Pp]hotography)))|(de (la )?fotograf(i|í)a)|(de arte)).*"))
                            continue;
                        part = part.replaceFirst("Executive", "executive");
                        part = part.replaceFirst("Writer", "writer");
                        part = part.replaceFirst("Story", "story");
                        part = part.replaceFirst("Producer", "producer");
                        part = part.replaceFirst("Produced", "produced");
                        part = part.replaceFirst(", English", ", english");
                        part = part.replaceFirst("Researcher", "researcher");
                        part = part.replaceFirst(".*?[Dd]irector", "director");
                        part = part.replaceAll("[ ]?([.][.][.])?[ ]?[\\[][^\\]]*[\\]]", "");
                        part = part.replaceAll("[]]", "");
                        part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "# $1");
                        part = part.replaceAll("director[-A-Za-z/]*", "director");
                        part = part.replaceAll("director (for|and|of)( [A-Z][A-Za-z]*)+", "director");
                        if (!greedy)
                            part = part.replaceFirst("director( and [a-z]*)?, [a-z].*", "");
                        part = part.replaceFirst("director[^A-Z]* ([ʻ*]?[A-Z])", "director= $1");
                        part = part.replaceFirst("with the [a-z][A-Za-z ]*", "");
                        part = part.replaceFirst("et al", "");
                        if (!greedy)
                            part = part.replaceAll("[,]? and [a-z].*", "");
                        part = part.replaceAll(" (and|with|et) ", " & ");
                        part = part.replaceAll(",[ ]?[a-z].*", "");
                        part = part.replaceAll("= [^(]*[)], ", ": ");
                        if (greedy)
                            part = part.replaceAll("director=[ a-z,]*(([\"]?([A-Z]|\\p{Lu}|[*ʻ]|\\p{M})[^ ]*[\"]?[,]?[ ]*|[ ]?&[ ]|von |zur |van |de[rl]?[ ]?|the |in |d[']|al-|da-)+)[^|&]*", "$1");
                        else // strict
                            part = part.replaceAll("director=[ a-z,]*(([\"]?([A-Z]|\\p{Lu}|[*ʻ]|\\p{M})[^ ]*[\"]?[,]?[ ]*|[ ]?&[ ]|von |zur |van |de[rl]?[ ]?|the |in |d[']|al-|da-)+)( [a-z].*|$)", "$1");
                        part = part.replaceAll("^([A-Z][^ .]+) & ([A-Z][^ .]+) ([A-Z][^ .]+)", "$1 $3 & $2 $3");
                        part = part.replaceAll("([,][ ]?|[ ]?[|&][ ]?)", "|");
                        part = part.replaceAll("[#]", ",");
                        part = part.replaceAll("[ ][ ]+", " ");
                        String commaparts[] = part.split("[|]+");
                        for (String subpart : commaparts)
                        {
                            addCleanedName(result, squeezedresult, subpart, reverseName, greedy);
                        }
                    }
                    //  Pattern matching when the subpart is of the form:   Direction Some Name 
                    else if (part.matches(".*[Dd]irection.*"))
                    {
                        if (part.matches(".*([Aa]rt|[Mm]usic(al)?|[Ss]tage|[Pp]roject|[Aa]nimation|[Mm]edical|[Cc]asting|[Tt]echnical|[Oo]rchestra|[Ee]diting) [Dd]irection.*" )||
                                part.matches(".*[Dd]irection (of )?(de )?(la )?((f|ph)otogra(f|ph)ie|production|[Cc]in(é|e)matographie|artistique|art[e]?|musicale).*"))
                            continue;
                        part = part.replaceFirst(".*[Dd]irection[^A-Z]*", "direction: ");
                        part = part.replaceAll(", (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.])", "# $1");
                        part = part.replaceAll(" (and|with|et) ", " & ");
                        part = part.replaceAll("[\\]]", "");
                        part = part.replaceAll("([,][ ]?|[ ]?&[ ]?)", "|");
                        part = part.replaceAll("[#]", ",");
                        part = part.replaceAll("[ ][ ]+", " ");
                        part = part.replaceFirst("direction: ", "");
                        String commaparts[] = part.split("[|]+");
                        for (String subpart : commaparts)
                        {
                            addCleanedName(result, squeezedresult, subpart, reverseName, greedy);
                        }
                    }
                }
            }
            if (result.size() > 0) break;
        }
        return(result);
    }

    private static String handleASoAndSoFilm(String responsibility)
    {
        String aOrAn = "\\ba[n]?[ ]+";   // matches   a or an  but has boundary marker so as to not match   pa or man   
        String namePart = "(?:\\p{Lu}(?:\\p{L}|\\p{M}|[-'])*(?:\\p{Ll}|\\p{M}))"; //  Example matches:   Jadme-Lillo  or  Sánchez  or SiCa  or  O'Malley
        String initialOrNamePart = "(?:\\p{Lu}[.]|"+namePart+")"; //  Example matches:   B.  or  Jadme-Lillo  or  Sánchez  or SiCa  or  O'Malley
        String optionalSuffix = "(?:[,]? (?:Jr[.]?|Sr[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.]))?"; //  Example matches:    , Jr.  or , III   or   , M.D.
        String nameGap = "[- ]";
        String film = " film\\b";  // matches film  but not films uses boundary marker
        String name = initialOrNamePart+nameGap+"(?:"+initialOrNamePart+nameGap+")?"+namePart+optionalSuffix;
        String multiNameConnector = "(?:, | - | and | ?/ ?)";
        String responsibility1;
        responsibility1= responsibility.replaceAll(aOrAn+"("+name+")"+film, "a film by $1"); 
        responsibility1= responsibility1.replaceAll(aOrAn+"("+name+")"+multiNameConnector+"("+name+")"+film, "a film by $1, $2"); 
        responsibility1= responsibility1.replaceAll("("+name+")'s"+film, "a film by $1"); 

//        responsibility1= responsibility.replaceAll("(^|[^a-z])a(n)?[ ]+(([A-Z]([.]|(?:[-a-zA-Z']|\\p{M})*(?:[a-z]|\\p{M})) ([A-Z]([.]|(?:[-a-zA-Z']|\\p{M})*(?:[a-z]|\\p{M}))[- ]?)?[A-Z](?:[-a-zA-Z']|\\p{M})*(?:[a-z]|\\p{M})(, (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.]))?) film([^a-z]|$)", "$1a film by $3$10"); 
//        responsibility1 = responsibility1.replaceAll("(^|[^a-z])a(n)?[ ]+((([A-Z]([.]|[-a-zA-Z']*[a-z])) (([A-Z]([.]|[-a-zA-Z']*[a-z])[- ]?)?[A-Z][-a-zA-Z']*[a-z](, (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.]))?))(, | - | and | ?/ ?))+(([A-Z]([.]|[-a-z]*[a-z])) ([A-Z]([.]|[-a-z]*[a-z])[- ]?)?[A-Z][-a-z]*[a-z](, (Jr[.]?|Sr[.]?|Inc[.]?|II|III|IV|M[.]D[.]|B[.]S[.]N[.]))?) film([^a-z]|$)", "$1a film by $4, $13$20");
        if (!responsibility1.equals(responsibility))
        {
            responsibility = responsibility1;
        }
        return(responsibility);

    }

    private static void addCleanedName(Set<String> result, Set<String> squeezedresult, String subpart, boolean reverseName, boolean greedy)
    {
        subpart = nameClean(subpart, greedy);
        if (subpart == null) return;
        if (reverseName) 
            subpart = subpart.replaceFirst("([^ ]+)[ ]+(.*)", "$2 $1");
        String squeezedpart = subpart.replaceAll(" ", "");
        if (!squeezedresult.contains(squeezedpart))
        {
            squeezedresult.add(squeezedpart);
            result.add(subpart);
        }
    }

    private static String nameClean(String subpart, boolean greedy)
    {
        if (subpart.matches(".*[(].*[)]"));
            subpart = subpart.replaceAll("(.*)[(].*[)]", "$1");
        if (subpart.matches("\".*") || subpart.matches(".*\""))
            subpart = subpart.replaceAll("\"?(.*?)\"?", "$1");
        if (subpart.matches(".* for .*"))
            subpart = subpart.replaceAll("(.*) for .*", "$1");
        if (subpart.matches(".*, Inc[.]"))
            return(null);
        if (subpart.matches(".*( of | a | in ).*"))
            return(null);
        if (subpart.matches(".*'s"))
            subpart = subpart.replaceFirst("'s$", "");
        if (subpart.matches(".*? [a-z][a-z ]*"))
            subpart = subpart.replaceAll("(.*?) [a-z][a-z ]*", "$1");
        if (subpart.matches("[a-z]*"))
            return(null);
        if (subpart.contains("didrector"))
            subpart = subpart.replaceAll("didrector", "");
        if (subpart.matches(".*[ .]+$"))
            subpart = subpart.replaceAll("(.*?)[ .][ .]+$", "$1");
        if (!greedy && subpart.matches(".*[0-9]+.*"))
            return(null);
        if (!greedy && subpart.matches("[Tt]he( .*|$)"))
            return(null);
        if (!greedy && subpart.matches("[A-Z][a-z]*"))
            return(null);
        if (!greedy && subpart.matches("^[\"]?[a-z].*"))
            return(null);
        if (subpart.matches("[Tt]he( .*|$)"))
            subpart = subpart.replaceFirst("[Tt]he[ ]?", "");
        if (subpart.contains("Group")) return(null);
        if (subpart.contains("Studio[s]?")) return(null);
        if (subpart.contains("Entertainment")) return(null);
        if (subpart.contains("Department")) return(null);
        if (subpart.contains("National")) return(null);
        if (subpart.contains("Museum")) return(null);
        if (subpart.contains("Films")) return(null);
        if (subpart.contains("TV")) return(null);
        if (subpart.contains("Response")) return(null);
        if (subpart.contains("Cities")) return(null);
        if (subpart.contains("High")) return(null);
        if (subpart.endsWith("Productions")) return(null);
        if (subpart.startsWith("Written")) return(null);
        if (subpart.startsWith("Writer")) return(null);
        if (subpart.equalsIgnoreCase("Various")) return(null);
        if (subpart.equalsIgnoreCase("Editor")) return(null);
        if (subpart.equalsIgnoreCase("Executive")) return(null);
        if (subpart.equalsIgnoreCase("Story")) return(null);
        if (subpart.startsWith("English")) return(null);
        if (subpart.startsWith("Company")) return(null);
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
    public static Pattern genreChildren = Pattern.compile("(^|[^a-z])((children's) (stories|films|literature|poetry|songs|television programs))|(television programs|video recordings|dance) for children([^a-z]|$)");
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

