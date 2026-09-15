package org.solrmarc.mixin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import org.solrmarc.index.SolrIndexerMixin;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.index.indexer.IndexerSpecException.eErrorSeverity;
import org.solrmarc.tools.DataUtil;

public class DirectorMixin extends SolrIndexerMixin
{
//    Pattern releaseDatePattern = null;
//    boolean isVideo = false;
//    SolrIndexerMixin formatmixin = null;
//    public void perRecordInit(Record record)
//    {
//    	ControlField cf008 = (ControlField)record.getVariableField("008");
//    	isVideo = (cf008 != null && cf008.getData().charAt(33) == 'v') && record.getLeader().getTypeOfRecord() == 'g';
////        Set<String> formats = indexer.getFormat(record);
////        isVideo = (Utils.setItemContains(formats, "Video"));
//    }

	private static final String[] FIELDS_ARRAY = {"500", "505"};

    /**
     * Attempt to heuristically determine the Director of a video item based on looking in the 245c, subfield, the 508a subfield and 700 fields
     * 
     * @param record   the record being processed
     * @return  Set<String>  a set of Strings representing the name(s) of the director(s) of the video  (or an empty set, if none can be found)
     */
    public Set<String> getVideoDirector(Record record)
    {
        ControlField cf008 = (ControlField)record.getVariableField("008");
        List<VariableField> cf007 = record.find("007","^v");
    	boolean isVideo = ((cf008 != null && cf008.getData().length() > 33 && cf008.getData().charAt(33) == 'v') || cf007.size() > 0) && 
    	                   record.getLeader().getTypeOfRecord() == 'g';
    	Set<String> result = new LinkedHashSet<String>();
    	
    	DataField f245 = ((DataField)record.getVariableField("245"));
        if (isVideo)
        {
            String responsibility = (f245 != null) ? f245.getSubfieldsAsString("c") : null;
            if (responsibility != null)
            {
                Set<String> directors = getVideoDirectorsFromTextField(responsibility, true);
                result.addAll(directors);
            }
        	for (VariableField df : record.getVariableFields("508"))
            {
                for (Subfield credit : ((DataField)df).getSubfields('a'))
                {
                	Set<String> directors = getVideoDirectorsFromTextField(credit.getData(), true);
                    result.addAll(directors);
                }
            }
        	if (result.size() == 0)
            {
                for (VariableField df : record.getVariableFields(FIELDS_ARRAY) ) 
                {
                    for (Subfield noteField : ((DataField)df).getSubfields("at"))
                    {
                    	final String note = noteField.getData();
                    	if (note.contains("direct") || note.contains("Direct") || note.contains("A film by") || note.contains("a film by"))
                    	{
                    		Set<String> directors = getVideoDirectorsFromTextField(note, false);
                    		result.addAll(directors);
                    	}
                    }
                }
                String subtitle = f245 != null ? f245.getSubfieldsAsString("b") : null;
                if (subtitle != null && (subtitle.contains("direct") || subtitle.contains("Direct")))
                {
                    
                    addError(new IndexerSpecException(eErrorSeverity.WARN, "Director information erroneously included in the 245b subtitle field"));
                    Set<String> directors = getVideoDirectorsFromTextField(subtitle, false);
                    result.addAll(directors);
                }
                
                String medium = f245 != null ? f245.getSubfieldsAsString("h") : null;
                if (medium != null && (medium.contains("direct") || medium.contains("Direct")))
                {
                    
                    addError(new IndexerSpecException(eErrorSeverity.WARN, "Director information erroneously included in the 245h medium field"));
                    Set<String> directors = getVideoDirectorsFromTextField(medium, false);
                    result.addAll(directors);
                }

            }
            
            List<VariableField> personalNames = record.getVariableFields("700");
            for (VariableField vf : personalNames)
            {
                DataField df = (DataField)vf;
                // this could be overly broad, and could also grab music directors or other roles containing the word "director"
                if (ChkSubfield(df, '4', "drt") || ChkSubfield(df, 'c', "[(]?([Ff]ilm )?[Dd]irector[)]?[^a-z]*") || 
                        (ChkSubfield(df, 'e', "(co-|film )?[Dd]irect(or|er|ion|eur|ed by)[^a-z]*") ))
                {
                    String name = df.getSubfield('a').getData();
                    name = DataUtil.cleanData(name);
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
        responsibility = responsibility.replaceAll("direc\u0327a\u0303o de produc\u0327a\u0303o", "producer");//porteguese
        responsibility = responsibility.replaceAll("direct(or|ion|eur) de (la )?[ ]?produc[ct]i(o\u0301|o)n", "producer");//french/spanish
        responsibility = responsibility.replaceAll("direcci(o\u0301|o)n art\\B*", "artguy");//spanish
        responsibility = responsibility.replaceAll("produit par\\b", "produced by");

        String responsibility1 = responsibility.replaceAll("[Rr]eg(i|i\u0301)[ea]?\\b", "didrector");//german/italian/swedish
        responsibility1 = responsibility1.replaceAll("[Rr]eggia\\b", "didrector");//spanish
        responsibility1 = responsibility1.replaceAll("[Rr]e(z\u030c|z)ie", "didrector");//czech
        responsibility1 = responsibility1.replaceAll("[Rr]e(z\u0307|z|a)yseria", "didrector");//polish
        responsibility1 = responsibility1.replaceAll("[Dd]irecci(o\u0301|o)n", "didrector");//spanish
        responsibility1 = responsibility1.replaceAll("[Rr](e\u0301|e)alisation", "didrection");//french
        responsibility1 = responsibility1.replaceAll("[Rr]ealizaci(o\u0301|o)n", "didrection");//spanish
        responsibility1 = responsibility1.replaceAll("[Rr]e\u0301alise\u0301( et [a-z]*)? (par|by)", "didrected$1 by");//french
        responsibility1 = responsibility1.replaceAll("[Dd]irected por", "didrected by");//spanish
        responsibility1 = responsibility1.replaceAll("[Dd]irige\u0301 par", "didrected by");//french
        responsibility1 = responsibility1.replaceAll("[Dd]irrecio\u0301n", "didrection");//spanish
        responsibility1 = responsibility1.replaceAll("[Dd]irecci\u0301on", "didrection");//spanish
        responsibility1 = responsibility1.replaceAll("[Dd]irector(a|es)", "didrector");//spanish
        responsibility1 = responsibility1.replaceAll("[Dd]ire(c\u0327|c)a\u0303o", "didrector");//porteguese
        responsibility1 = responsibility1.replaceAll("[Dd]iretto da", "didrector");//italian
        responsibility1 = responsibility1.replaceAll("[Dd]irecteur", "didrector");//french
        responsibility1 = responsibility1.replaceAll("[Dd]irect(e|io)r", "didrector");//typo
        responsibility1 = responsibility1.replaceAll("[Dd]irigid[oa]", "didrector");//porteguese
        responsibility1 = responsibility1.replaceAll("[Tt]asrit\u0323 u-vimui", "didrector");//hebrew
        responsibility1 = responsibility1.replaceAll("[Ii]khr(a\u0304|a)j", "didrector");//arabic
        responsibility1 = responsibility1.replaceAll("[Rr]ezhisser[a]?", "didrector");//russian
        responsibility1 = responsibility1.replaceAll("[Yy]o\u0308neten", "didrector");//turkish
        responsibility1 = responsibility1.replaceAll("[Nn]irdes\u0301aka", "didrector");//hindi
        responsibility1 = responsibility1.replaceAll("[Pp]ostanovka", "didrector");//russian
        responsibility1 = responsibility1.replaceAll("(un )?film[e]? d[ei]\\b", "a flim by");//french
        responsibility1 = responsibility1.replaceAll("un film d'", "a flim by ");//french
        responsibility1 = responsibility1.replaceAll("(an|en) film av", "a flim by"); //swedish
        responsibility1 = responsibility1.replaceAll("[Ee]in [Ff]ilm von", "a flim by"); //german
        responsibility1 = responsibility1.replaceAll("un(e|a) pel(i\u0301|i)cula de", "a flim by");//spanish
        responsibility1 = responsibility1.replaceAll("[Mm]is[e]? en sc(e|e\u0300)ne( de)?", "a flim by");//french
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
        responsibility1 = responsibility1.replaceAll("[Yy]o\u0306nch\u02bbul", "didrector");//korean
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
                        if (part.matches(".*([Tt]he )?[A-Z][^ ]* [Bb]rothers(, | \\()[A-Z][a-z]* and [A-Z][a-z]* "+name+".*"))
                        {
                            part = part.replaceFirst("(([Tt]he )?([A-Z][^ ]*) [Bb]rothers)(, | \\()([A-Z][a-z]*) and ([A-Z][a-z]*) "+name+"\\)?", "$5 $3 and $6 $3");
                        }
                        else if (part.matches(".*([Tt]he )?[A-Z][^ ]* [Bb]rothers(, | \\()[A-Z][a-z]* and [A-Z][a-z]*.*"))
                        {
                            part = part.replaceFirst("(([Tt]he )?([A-Z][^ ]*) [Bb]rothers)(, | \\()([A-Z][a-z]*) and ([A-Z][a-z]*)\\)?", "$5 $3 and $6 $3");
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
                    if (part.matches(".*[Dd]irector[s]? (of|de|de la) ([Pp]h|[Ff])otogra(ph|f)(y|(i|i\u0301)[ea]).*"))
                    {
                        part = part.replaceFirst("[Dd]irector[s]? (of|de|de la) ([Pp]h|[Ff])otogra(ph|f)(y|(i|i\u0301)[ea])", "cinematographer");
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
                                part.matches(".*[Dd]irector[s]? ((of[ ]?(([Pp]hotography)))|(de (la )?fotograf(i|i\u0301)a)|(de arte)).*"))
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
                                part.matches(".*[Dd]irector[s]? ((of[ ]?(([Pp]hotography)))|(de (la )?fotograf(i|i\u0301)a)|(de arte)).*"))
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
                        part = part.replaceAll("directified by[ a-z,]*(([\"]?([A-Z]|\\p{Lu}|[*\u02bb]|\\p{M})[^ ]*[\"]?[,]?[ ]*|[ ]?&[ ]|zur |von |van |de[rl]?[ ]?|the |d[']|al-|da-)+).*", "$1");
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
                        part = part.replaceAll("directified by[ a-z,]*(([\"]?([A-Z]|\\p{Lu}|[*\u02bb]|\\p{M})[^ ]*[\"]?[,]?[ ]*|[ ]?&[ ]|zur |von |van |de[rl]?[ ]?|the |d[']|al-|da-)+).*", "$1");
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
                                part.matches(".*[Dd]irector[s]? ((of[ ]?(([Pp]hotography)))|(de (la )?fotograf(i|i\u0301)a)|(de arte)).*"))
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
                        part = part.replaceFirst("director[^A-Z]* ([\u02bb*]?[A-Z])", "director= $1");
                        part = part.replaceFirst("with the [a-z][A-Za-z ]*", "");
                        part = part.replaceFirst("et al", "");
                        if (!greedy)
                            part = part.replaceAll("[,]? and [a-z].*", "");
                        part = part.replaceAll(" (and|with|et) ", " & ");
                        part = part.replaceAll(",[ ]?[a-z].*", "");
                        part = part.replaceAll("= [^(]*[)], ", ": ");
                        if (greedy)
                            part = part.replaceAll("director=[ a-z,]*(([\"]?([A-Z]|\\p{Lu}|[*\u02bb]|\\p{M})[^ ]*[\"]?[,]?[ ]*|[ ]?&[ ]|von |zur |van |de[rl]?[ ]?|the |in |d[']|al-|da-)+)[^|&]*", "$1");
                        else // strict
                            part = part.replaceAll("director=[ a-z,]*(([\"]?([A-Z]|\\p{Lu}|[*\u02bb]|\\p{M})[^ ]*[\"]?[,]?[ ]*|[ ]?&[ ]|von |zur |van |de[rl]?[ ]?|the |in |d[']|al-|da-)+)( [a-z].*|$)", "$1");
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
                                part.matches(".*[Dd]irection (of )?(de )?(la )?((f|ph)otogra(f|ph)ie|production|[Cc]in(e\u0301|e)matographie|artistique|art[e]?|musicale).*"))
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
        String namePart = "(?:\\p{Lu}(?:\\p{L}|\\p{M}|[-'])*(?:\\p{Ll}|\\p{M}))"; //  Example matches:   Jadme-Lillo  or  Sa\u0301nchez  or SiCa  or  O'Malley
        String initialOrNamePart = "(?:\\p{Lu}[.]|"+namePart+")"; //  Example matches:   B.  or  Jadme-Lillo  or  Sa\u0301nchez  or SiCa  or  O'Malley
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
        if (subpart.matches("[^()]*[)]"));
            subpart = subpart.replaceAll("([^()]*)[)]", "$1");
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
        subpart = DataUtil.cleanData(subpart);
        if (subpart.length() == 0) return(null);
        return(subpart);
    }

}

