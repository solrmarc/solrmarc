package org.solrmarc.mixin;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.index.SolrIndexerMixin;
import org.solrmarc.tools.DataUtil;

public class RegionFacetMixin extends SolrIndexerMixin
{
    
    final static String stateTable[][] = 
    {{"Alabama", "Ala."}, {"Alaska", "Alaska"}, {"Arizona", "Ariz."}, {"Arkansas", "Ark."}, {"California", "Calif."}, 
     {"Colorado", "Colo."}, {"Connecticut", "Conn."}, {"Delaware", "Del."}, {"Florida", "Fla."}, {"Georgia", "Ga."}, 
     {"Hawaii", "Hawaii"}, {"Idaho", "Idaho"}, {"Illinois", "Ill."}, {"Indiana", "Ind."}, {"Iowa", "Iowa"},  
     {"Kansas", "Kan."},{"Kentucky", "Ky."}, {"Louisiana", "La."}, {"Maine", "Maine"}, {"Maryland", "Md."}, 
     {"Massachusetts", "Mass."}, {"Michigan", "Mich."}, {"Minnesota", "Minn."}, {"Mississippi", "Miss."}, {"Missouri", "Mo."},
     {"Montana", "Mont."}, {"Nebraska", "Neb."}, {"Nevada", "Nev."}, {"New Hampshire", "N.H."}, {"New Jersey", "N.J."},  
     {"New Mexico", "N.M."},{"New York", "N.Y."}, {"North Carolina", "N.C."}, {"North Dakota", "N.D."}, {"Ohio", "Ohio"}, 
     {"Oklahoma", "Okla."}, {"Oregon", "Or."}, {"Pennsylvania", "Pa."}, {"Rhode Island", "R.I."}, {"South Carolina", "S.C."},  
     {"South Dakota", "S.D."},{"Tennessee", "Tenn."}, {"Texas", "Tex."}, {"Utah", "Utah"}, {"Vermont", "Vt."}, 
     {"Virginia", "Va."}, {"Washington", "Wash."}, {"West Virginia", "W. Va."}, {"Wisconsin", "Wis."}, {"Wyoming", "Wyo."}, 
     {"New York (State)", "N.Y."}, {"District of Columbia", "D.C."}, {"Puerto Rico", "P.R."}, {"Virgin Islands", "V.I."}, 
     {"Alberta", "Alta."}, {"British Columbia", "B.C."}, {"Manitoba", "Man."}, {"Newfoundland and Labrador", "N.L."}, 
     {"New Brunswick", "N.B."}, {"Northwest Territories", "N.W.T."}, {"Nova Scotia", "N.S."}, {"Nunavut", "Nunavut"}, 
     {"Ontario", "Ont."}, {"Prince Edward Island", "P.E.I."}, {"Quebec", "Que'bec"}, {"Saskatoon", "Sask."}, {"Yukon", "Yukon"},
     {"Australian Capital Territory", "A.C.T."}, {"New South Wales", "N.S.W."}, {"Northern Territory", "N.T."}, {"Queensland", "Qld."}, 
     {"South Australia", "S. Aust."}, {"Tasmania", "Tas."}, {"Victoria", "Vic."}, {"Western Australia", "W.A." }};
    
    static Map<String, String> stateMap = null;
    
    private String getStateNameAbbrev(String stateName)
    {
        if (stateMap == null)
        {
            stateMap = new LinkedHashMap<String, String>();
            for (int i = 0; i < stateTable.length; i++)
            {
                stateMap.put(stateTable[i][0], stateTable[i][1]);
            }
        }
        return(stateMap.get(stateName));
    }

    final static String locationTypeNames[] = {"State", "Republic", "Principality", "Province", "Township", "County", "Town",
                                               "Judicial district", "Prefecture", "Region", "District", "Dept.", "Kingdom", 
                                               "Canton", "City", "Division", "Duchy", "Emirate", "Government", "Country",
                                               /* India: */ "Princely State", "Presidency", "Tahsil", "Taluka", "Tehsil", "Thana",
                                               /* China: */ "Sheng",
                                               /* Denmark: */  "Amt", "Herred",
                                               /* Thailand: */  "Amphoe",
                                               /* France: */ "Comte´",
                                               /* South/Central America: */ "Corregimiento", "Distrito Federal", "Intendancy", "Partido", 
                                               /* Religious: */ "Diocese", "diocese", "Archdiocese", "Archdeaconry", "Ecclesiastical principality",
                                               /* Poland: */ "Voivodeship", "Powiat",
                                               /* Germany:*/ "Landkreis", "Kreis", "Bezirk", "Electorate", "Grafschaft",
                                               /* Czech: */ "Okres", 
                                               /* Russia: */ "Oblast'", "Oblast", "Kray", 
                                               /* Hungary: */ "Comitat",
                                               /* Romania: */ "Judet",
                                               /* Indonesia: */ "Kabupaten", 
                                               /* Former: */ "Ancient city", "Ancient sanctuary", "Ancient site", "Extinct city", "Concentration camp", "Colony", "Site",
                                               /* Descriptive: */ "Peninsula", "Coast", "Cape", "Harbor", "Island", "Lake", "Oasis", "Tribal area" };
    static Set<String> locationTypeNameSet = null;

    private static boolean isLocationTypeWord(String name)
    {
        if (locationTypeNameSet == null)
        {
            locationTypeNameSet = new LinkedHashSet<String>();
            for (String locType : locationTypeNames)
            {
                locationTypeNameSet.add(locType);
            }
        }
        if (locationTypeNameSet.contains(name))  return(true);
        return(false);
    }
    
    private boolean isEqualsOrContains(String string1, String string2)
    {
        if (string1.equals(string2))  return(true);
        if (string1.contains(" and "+ string2)) return(true);
        if (string1.contains(string2 + " and ")) return(true);
        if (string1.contains(", "+ string2)) return(true);
        String tmp = getStateNameAbbrev(string2);
        if (tmp != null && tmp.equals(string1)) 
            return(true);
        return(false);
    }

    private Set<String> getSet650z(Record record)
    {
        Set<String> result = new LinkedHashSet<String>();
        List<VariableField> fields = (List<VariableField>)record.getVariableFields("650");
        for (VariableField f : fields)
        {
            DataField df = (DataField)f;
            List<Subfield> sfs = (List<Subfield>)df.getSubfields();
            boolean prevWasZ = false;
            StringBuffer part = new StringBuffer();
            boolean hadPrevZ = false;
            int zCount = 0;
            for (Subfield sf : sfs)
            {
                if (sf.getCode() == 'z')
                {
                    zCount++;
                    if (zCount > 2)
                    {
                        prevWasZ = true;
                    }
                    if (prevWasZ)
                    {
                        String data = DataUtil.cleanData(sf.getData());
                        if (data.equals("South America") || data.equals("Central America") || data.equals("United States"))
                        {
                            //part.insert(0, data+"#");
                        }
                        else
                        {
                            part.append("#");
                            part.append(DataUtil.cleanData(sf.getData()));
                        }
                    }
                    else
                    {
                        if (hadPrevZ)
                        {
                            hadPrevZ = true;
                        }
                        part.append(DataUtil.cleanData(sf.getData()));
                    }
                    prevWasZ = true;
                    hadPrevZ = true;
                }
                else
                {
                    zCount = 0;
                    if (prevWasZ)
                    {
                        result.add(part.toString());
                        part.setLength(0);
                        prevWasZ = false;
                    }
                }
            }
            if (prevWasZ)
            {
                result.add(part.toString());
                part.setLength(0);
                prevWasZ = false;
            }
        }
        return(result);   
    }
    
    public Set<String> getRegionFacet(final Record record)
    {
        Set<String> result = SolrIndexer.instance().removeTrailingPunct(record, "651a");
        Set<String> sub650z = getSet650z(record);
        for (String fields650 : sub650z)
        {
            String parts[] = fields650.split("#");
            parts[0] = DataUtil.cleanData(parts[0]);
            result.add(parts[0]);
            for (int i = 1; i < parts.length; i++)
            {
                if (i == 2)  // 650  0$aEthnology$zRussia (Federation)$zSiberia$xResearch$zPoland$xHistory$vCongresses.  0$aLabor movement$zBrazil$zNatal$zDurban.
                {
                //  things to decide :  $z Colombia $z Zipaquira $z South America    or $z Germany $z Berlin $z Tiergarten or $z Nicaragua $z Rivas (Dept.)$z Central America  or  $z Italy $z Sicily  $z Camarina (Extinct city)
                    parts[0] = parts[1].replaceAll("((\\p{L}\\p{M}*|\\.|[- ])+(\\p{L}\\p{M}*|\\.))[ ]?\\(((\\p{L}\\p{M}*|\\.|[- ])+).*", "$1, $4");
                    i = 2;  
                }
                parts[i] = DataUtil.cleanData(parts[i]);
                String abbrev = getStateNameAbbrev(parts[0]);
                if (abbrev != null)
                {
                    parts[i] = parts[i] + " (" + abbrev + ")";
                }
                else if (parts[i].endsWith(")"))
                {
                    if (!parts[i].contains("("))
                    {
                        parts[i] = parts[i].substring(0, parts[i].length()-1) + " (" + parts[0] + ")";
                    }
                    else if (parts[i].matches(".*[ ]?\\((\\p{L}\\p{M}*|\\.|[- ])+\\)"))
                    {
                        String subparts[] = parts[i].split("[ ]?\\(", 2);
                        if (subparts.length > 1)
                        {
                            subparts[1] = subparts[1].substring(0, subparts[1].length()-1);
                            if (!subparts[1].equals(parts[0]) && isLocationTypeWord(subparts[1]))
                            {
                                parts[i] = subparts[0] + " (" + parts[0] + " : " + subparts[1] + ")";
                            }
                            else if (!isEqualsOrContains(subparts[1], parts[0]))
                            {
                                parts[i] = parts[i].substring(0, parts[i].length()-1) + ", " + parts[0] + ")";
                            }
                            //else leave parts[i] as is.
                            else
                            {
                                parts[i] = parts[i];
                            }
                        }
                        else
                        {
                            parts[i] = parts[i].substring(0, parts[i].length()-1) + ", " + parts[0] + ")";
                        }
                    }
                    else  //  things to decide :  $z Germany $z Blah (Something : District) or $z Italy $z Satricum (Lazio : Extinct city)
                    {
                        if (parts[i].matches("(\\p{L}\\p{M}*|\\.|[- ])+[ ]?\\((\\p{L}\\p{M}*|\\.|[- ])+ : (\\p{L}\\p{M}*|\\.|[- ])+\\)"))
                        {    
                        // equivalent of, but with expanded character sets to include unicode accented letters and accent marks : 
                        //    parts[i] = parts[i].replaceFirst("([-A-Za-z ]+[A-Za-z])[ ]?\\(([-A-Za-z ]+) : ([-A-Za-z ]+)\\)", 
                        //                                     "$1 ($2, "+parts[0]+" : $3)");
                            parts[i] = parts[i].replaceFirst("((\\p{L}\\p{M}*|\\.|[- ])+(\\p{L}\\p{M}*|\\.))[ ]?\\(((\\p{L}\\p{M}*|\\.|[- ])+) : ((\\p{L}\\p{M}*|\\.|[- ])+)\\)", 
                                                             "$1 ($4, "+parts[0]+" : $6)");
                        }
                        else
                            parts[i] = parts[i];
                    }
                }
                else 
                {
                    parts[i] = parts[i] + " (" + parts[0] + ")";
                }                
                result.add(parts[i]);
            }
        }
        return(result);
    }


}
