package org.solrmarc.index.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

// org.solrmarc.index.collector.FieldMatchCollector;
import org.solrmarc.index.extractor.formatter.FieldFormatter;
import org.solrmarc.index.extractor.formatter.FieldFormatter.eJoinVal;
import org.solrmarc.index.extractor.formatter.FieldFormatterBase;
//import org.solrmarc.index.extractor.FieldFormatterJoin;
import org.solrmarc.index.extractor.formatter.FieldFormatterPatternMapped;
import org.solrmarc.index.extractor.formatter.FieldFormatterTranslationMapped;
import org.solrmarc.index.extractor.impl.direct.FieldMatch;
import org.solrmarc.index.specification.Specification;
import org.solrmarc.index.specification.conditional.ConditionalParser;


public class ConditionalParserTest
{

    static
    {
        PropertyConfigurator.configure(new File("log4j.properties").getAbsolutePath());
    }

    /**
     * @param args
     */
    static boolean do_debug_parse = true;
    static ConditionalParser parser = null;
    
    public static Specification testString(String conditional)
    {
    	if (parser == null)  parser = new ConditionalParser(do_debug_parse);
        Specification result = null;
        result = (Specification) parser.parse(conditional, do_debug_parse);
        result.setSpecLabel(conditional); 
        return(result);
    }

    public static void main(String args[])
    {
//        TranslationMapFactory.theMaps.setPropertyFilePaths(new String[]{"C:/Development/GoogleCodeSVNBranch2.0/dist", "C:/Development/GoogleCodeSVNBranch2.0/dist/translation_maps", "C:/Development/GoogleCodeSVNBranch2.0/dist/scripts"});
//      System.out.println("Mapped: "+fm.toString("", null, false, false, "datemap:{\"(^|.*[^0-9])((20|1[5-9])[0-9][0-9])([^0-9]|$)=>$2\", \".*[^0-9].*=>\"}"));
//      System.out.println(fm.toString(" ", null, false, true, "oclcnum:{\".*\\(OCoLC\\)(.*)=>$1\"}"));
//      System.out.println(fm.toString(" ", null, false, true, "library_map.properties"));
        
        Specification spec1 = testString("{600[a-z]:610[a-z]:611[a-z]:630[a-z]:650[a-z]:651[a-z]:655[a-z]:690[a-z]}?(ind2!=7|(ind2 = 7&$2~\"fast|lcsh|tgn|aat\")):520a");
        Specification spec2 = testString("008[7-10]:008[11-14]:260c:264c?(ind2=1)");
//        Specification spec2 = testString(sf, "008[7-10]?([7-10]~\"[0-9][0-9][0-9][0-9]\") : 008[11-14]?([11-14]~\"[0-9][0-9][0-9][0-9]\") : 260c : 264c?(ind2=1)");
        Specification spec3 = testString("245[^ch]");
        Specification spec4 = testString("035a?($a~\"\\(OCoLC\\)[0-9]+\")");
        Specification spec5 = testString("999m");
        Specification spec6 = testString("000[6]?([6]~\"[cdj]\")");
        spec1.setFormatter(new org.solrmarc.index.extractor.formatter.FieldFormatterBase(true).setSeparator(" -- ").setJoinVal(eJoinVal.JOIN));
        spec2.setFormatter(new FieldFormatterPatternMapped(new FieldFormatterBase(true), "(^|.*[^0-9])((20|1[5-9])[0-9][0-9])([^0-9]|$)=>$2||.*[^0-9].*=>"));
        spec3.setFormatter(new FieldFormatterBase(FieldFormatter.TITLE_SORT_LOWER).setSeparator(" : ").setJoinVal(eJoinVal.JOIN));
        spec4.setFormatter(new FieldFormatterPatternMapped(new FieldFormatterBase(true), ".*\\(OCoLC\\)([0-9]*)=>$1"));
        spec5.setFormatter(new FieldFormatterTranslationMapped(new FieldFormatterBase(true), "library_map.properties"));
        spec6.setFormatter(new FieldFormatterPatternMapped(new FieldFormatterBase(true), "[cd]=>Scores||[j]=>Recordings||[cdj]=>Recordings and/or Scores"));
        
//        FieldMatchCollector fmc = new FieldMatchCollector();
//        FieldMatchCollector fmcu = new FieldMatchCollector(true);

//        ConditionalSpecification parse_tree = testString(sf, "(!(ind1 = 2))");
//        ConditionalSpecification parse_tree2 = testString(sf, "ind1 != 2 & $i ~ \".*1994.*\"");        
//        ConditionalSpecification parse_tree3 = testString(sf, "(ind2 != 7 | (ind2 = 7 & $2 ~ \"fast|lcsh|tgn|aat\"))");
        
        // java -jar jflex-1.6.0.jar -d ..\src\org\solrmarc\index\specification\conditional ..\src\org\solrmarc\index\specification\conditional\Scanner.flex
        // java -jar java-cup-11b.jar -package org.solrmarc.index.specification.conditional -parser ConditionalParser  ..\src\org\solrmarc\index\specification\conditional\ConditionalParser.cup
        
        try
        {
//            MarcReader reader = new MarcPermissiveStreamReader(new FileInputStream("./records/subject_RDA.mrc"), true, true);
            MarcReader reader = new MarcPermissiveStreamReader(new FileInputStream("./records/specTestRecs.mrc"), true, true);
            Record record = null;
            while (reader.hasNext())
            {
                record = reader.next();
                System.out.println("--------------------------------------------");
                System.out.println(record.toString());
                showResult(spec1.getSpecLabel(), record, fmcu, spec1.getFieldMatches(record));
                showResult(spec2.getSpecLabel(), record, fmcu, spec2.getFieldMatches(record));
                showResult(spec3.getSpecLabel(), record, fmc, spec3.getFieldMatches(record));
                showResult(spec4.getSpecLabel(), record, fmcu, spec4.getFieldMatches(record));
                showResult(spec5.getSpecLabel(), record, fmcu, spec5.getFieldMatches(record));
                showResult(spec6.getSpecLabel(), record, fmcu, spec6.getFieldMatches(record));
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void showResult(String specLabel, Record record, FieldMatchCollector fmc, List<FieldMatch> fieldMatches)
    {
        System.out.println();
        System.out.println(specLabel);
       
        Object result = null;
		try {
			result = fmc.collect(fieldMatches);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (result == null)
        {
        }
        else if (result instanceof String)
        {
            System.out.println("Result: "+result);            
        }
        else
        {
            @SuppressWarnings("unchecked")
			Collection<String> resultList = (Collection<String>)result;
            for (String fm : resultList)
            {
                System.out.println("Result: "+fm.toString());
            }
        }
    }

}
